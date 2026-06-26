from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable

import torch
from torch import Tensor, nn
from torch.optim import AdamW

from agent_min_model import AgentMinActorCritic


@dataclass
class PPOConfig:
    # 推荐区间：3e-5 ~ 2e-4。探索任务更怕把局部循环快速固化，默认使用较低学习率。
    lr: float = 1.5e-5
    # 推荐区间：0.990 ~ 0.998。走出房间、穿门、下楼都是长延迟收益，默认提高到 0.997。
    gamma: float = 0.995
    # 推荐区间：0.93 ~ 0.98。更高 GAE 有助于把穿门/新房间收益回传到前几步移动。
    gae_lambda: float = 0.95
    # 推荐区间：0.12 ~ 0.25。默认 0.18，减少 PPO 每次更新对策略分布的猛推。
    clip_range: float = 0.10
    # 推荐区间：0.35 ~ 0.80。奖励尺度被收紧后 0.55 比 0.5 略重视价值估计。
    value_coef: float = 0.50
    # 推荐区间：0.008 ~ 0.03。默认 0.018，用于维持探索，但避免纯随机打转。
    entropy_coef: float = 0.012
    # 推荐区间：0.4 ~ 1.0。默认 0.6，压住新奖励箱下偶发大梯度。
    max_grad_norm: float = 0.5
    # 推荐区间：3 ~ 8。默认 4，在线样本相关性强，不宜过度重复更新。
    epochs: int = 2
    # 推荐区间：64 ~ 512。默认 128；若 update_interval=256，则每轮两个 minibatch。
    minibatch_size: int = 16
    # 推荐区间：0 ~ 0.03。默认 0.01，减少大模型过拟合最近几十步循环。
    weight_decay: float = 1e-4
    # Stops a rollout update after the policy has moved too far from the
    # behavior that generated it. This protects supervised basic skills.
    target_kl: float = 0.020
    # 推荐区间：True/False。CUDA 下建议开启 AMP，能提高吞吐并降低显存占用。
    use_amp: bool = True


@dataclass
class RolloutBatch:
    level_tensor: Tensor
    explored_global_matrix: Tensor
    agent_visited_matrix: Tensor
    hero_vector: Tensor
    inventory_matrix: Tensor
    inventory_summary_vector: Tensor
    option_vector: Tensor
    mob_matrix: Tensor
    history_matrix: Tensor
    action_matrix: Tensor
    action_mask: Tensor
    monitor_item_mask: Tensor
    monitor_cell_mask: Tensor
    monitor_option_mask: Tensor
    skill_mask: Tensor
    action_skill_mask: Tensor
    forced_skill: Tensor
    actions: Tensor
    skills: Tensor
    heads: Tensor
    talent_target_embeddings: Tensor
    talent_target_types: Tensor
    monitor_item_rows: Tensor
    monitor_cell_indices: Tensor
    monitor_option_indices: Tensor
    old_log_probs: Tensor
    returns: Tensor
    advantages: Tensor

    def size(self) -> int:
        return int(self.actions.shape[0])

    def minibatches(self, minibatch_size: int) -> Iterable["RolloutBatch"]:
        indices = torch.randperm(self.size(), device=self.actions.device)
        for start in range(0, self.size(), minibatch_size):
            idx = indices[start:start + minibatch_size]
            yield RolloutBatch(
                level_tensor=self.level_tensor[idx],
                explored_global_matrix=self.explored_global_matrix[idx],
                agent_visited_matrix=self.agent_visited_matrix[idx],
                hero_vector=self.hero_vector[idx],
                inventory_matrix=self.inventory_matrix[idx],
                inventory_summary_vector=self.inventory_summary_vector[idx],
                option_vector=self.option_vector[idx],
                mob_matrix=self.mob_matrix[idx],
                history_matrix=self.history_matrix[idx],
                action_matrix=self.action_matrix[idx],
                action_mask=self.action_mask[idx],
                monitor_item_mask=self.monitor_item_mask[idx],
                monitor_cell_mask=self.monitor_cell_mask[idx],
                monitor_option_mask=self.monitor_option_mask[idx],
                skill_mask=self.skill_mask[idx],
                action_skill_mask=self.action_skill_mask[idx],
                forced_skill=self.forced_skill[idx],
                actions=self.actions[idx],
                skills=self.skills[idx],
                heads=self.heads[idx],
                talent_target_embeddings=self.talent_target_embeddings[idx],
                talent_target_types=self.talent_target_types[idx],
                monitor_item_rows=self.monitor_item_rows[idx],
                monitor_cell_indices=self.monitor_cell_indices[idx],
                monitor_option_indices=self.monitor_option_indices[idx],
                old_log_probs=self.old_log_probs[idx],
                returns=self.returns[idx],
                advantages=self.advantages[idx],
            )


class PPOTrainer:
    def __init__(self, model: AgentMinActorCritic, cfg: PPOConfig | None = None):
        self.model = model
        self.cfg = cfg or PPOConfig()
        self.optimizer = AdamW(model.parameters(), lr=self.cfg.lr, weight_decay=self.cfg.weight_decay)
        self.use_amp = self.cfg.use_amp and self._model_on_cuda()
        self.scaler = self._make_grad_scaler()

    def _model_on_cuda(self) -> bool:
        try:
            return next(self.model.parameters()).is_cuda
        except StopIteration:
            return False

    def _model_device(self) -> torch.device:
        try:
            return next(self.model.parameters()).device
        except StopIteration:
            return torch.device("cpu")

    def _batch_to_device(self, batch: RolloutBatch, device: torch.device) -> RolloutBatch:
        if device.type == "cpu":
            return batch

        def move(tensor: Tensor) -> Tensor:
            return tensor.to(device, non_blocking=True)

        return RolloutBatch(
            level_tensor=move(batch.level_tensor),
            explored_global_matrix=move(batch.explored_global_matrix),
            agent_visited_matrix=move(batch.agent_visited_matrix),
            hero_vector=move(batch.hero_vector),
            inventory_matrix=move(batch.inventory_matrix),
            inventory_summary_vector=move(batch.inventory_summary_vector),
            option_vector=move(batch.option_vector),
            mob_matrix=move(batch.mob_matrix),
            history_matrix=move(batch.history_matrix),
            action_matrix=move(batch.action_matrix),
            action_mask=move(batch.action_mask),
            monitor_item_mask=move(batch.monitor_item_mask),
            monitor_cell_mask=move(batch.monitor_cell_mask),
            monitor_option_mask=move(batch.monitor_option_mask),
            skill_mask=move(batch.skill_mask),
            action_skill_mask=move(batch.action_skill_mask),
            forced_skill=move(batch.forced_skill),
            actions=move(batch.actions),
            skills=move(batch.skills),
            heads=move(batch.heads),
            talent_target_embeddings=move(batch.talent_target_embeddings),
            talent_target_types=move(batch.talent_target_types),
            monitor_item_rows=move(batch.monitor_item_rows),
            monitor_cell_indices=move(batch.monitor_cell_indices),
            monitor_option_indices=move(batch.monitor_option_indices),
            old_log_probs=move(batch.old_log_probs),
            returns=move(batch.returns),
            advantages=move(batch.advantages),
        )

    def _make_grad_scaler(self):
        try:
            return torch.amp.GradScaler("cuda", enabled=self.use_amp)
        except TypeError:
            return torch.cuda.amp.GradScaler(enabled=self.use_amp)

    def _autocast(self):
        try:
            return torch.amp.autocast("cuda", enabled=self.use_amp)
        except TypeError:
            return torch.cuda.amp.autocast(enabled=self.use_amp)

    def update(self, rollout: RolloutBatch) -> dict[str, float]:
        self.model.train()
        device = self._model_device()
        if rollout.advantages.numel() > 1:
            advantage_std = rollout.advantages.std(unbiased=False)
            advantages = (rollout.advantages - rollout.advantages.mean()) / (advantage_std + 1e-8)
        else:
            advantages = rollout.advantages
        advantages = torch.nan_to_num(advantages, nan=0.0, posinf=0.0, neginf=0.0)
        rollout = RolloutBatch(
            level_tensor=rollout.level_tensor,
            explored_global_matrix=rollout.explored_global_matrix,
            agent_visited_matrix=rollout.agent_visited_matrix,
            hero_vector=rollout.hero_vector,
            inventory_matrix=rollout.inventory_matrix,
            inventory_summary_vector=rollout.inventory_summary_vector,
            option_vector=rollout.option_vector,
            mob_matrix=rollout.mob_matrix,
            history_matrix=rollout.history_matrix,
            action_matrix=rollout.action_matrix,
            action_mask=rollout.action_mask,
            monitor_item_mask=rollout.monitor_item_mask,
            monitor_cell_mask=rollout.monitor_cell_mask,
            monitor_option_mask=rollout.monitor_option_mask,
            skill_mask=rollout.skill_mask,
            action_skill_mask=rollout.action_skill_mask,
            forced_skill=rollout.forced_skill,
            actions=rollout.actions,
            skills=rollout.skills,
            heads=rollout.heads,
            talent_target_embeddings=rollout.talent_target_embeddings,
            talent_target_types=rollout.talent_target_types,
            monitor_item_rows=rollout.monitor_item_rows,
            monitor_cell_indices=rollout.monitor_cell_indices,
            monitor_option_indices=rollout.monitor_option_indices,
            old_log_probs=rollout.old_log_probs,
            returns=rollout.returns,
            advantages=advantages,
        )

        metrics = {
            "policy_loss": 0.0,
            "value_loss": 0.0,
            "entropy": 0.0,
            "approx_kl": 0.0,
            "early_stop": 0.0,
            "loss": 0.0,
        }
        steps = 0
        stop_early = False
        for _ in range(self.cfg.epochs):
            for cpu_batch in rollout.minibatches(self.cfg.minibatch_size):
                batch = self._batch_to_device(cpu_batch, device)
                with self._autocast():
                    log_probs, entropy_values, _logits, values = self.model.evaluate_actions_with_value(
                        {
                            "level_tensor": batch.level_tensor,
                            "explored_global_matrix": batch.explored_global_matrix,
                            "agent_visited_matrix": batch.agent_visited_matrix,
                            "hero_vector": batch.hero_vector,
                            "inventory_matrix": batch.inventory_matrix,
                            "inventory_summary_vector": batch.inventory_summary_vector,
                            "option_vector": batch.option_vector,
                            "mob_matrix": batch.mob_matrix,
                            "history_matrix": batch.history_matrix,
                            "action_matrix": batch.action_matrix,
                            "action_mask": batch.action_mask,
                            "monitor_item_mask": batch.monitor_item_mask,
                            "monitor_cell_mask": batch.monitor_cell_mask,
                            "monitor_option_mask": batch.monitor_option_mask,
                            "skill_mask": batch.skill_mask,
                            "action_skill_mask": batch.action_skill_mask,
                            "forced_skill": batch.forced_skill,
                        },
                        actions=batch.actions,
                        skills=batch.skills,
                        heads=batch.heads,
                        talent_target_embeddings=batch.talent_target_embeddings,
                        talent_target_types=batch.talent_target_types,
                        monitor_item_rows=batch.monitor_item_rows,
                        monitor_cell_indices=batch.monitor_cell_indices,
                        monitor_option_indices=batch.monitor_option_indices,
                    )
                    entropy = entropy_values.mean()

                    log_ratio = log_probs - batch.old_log_probs
                    ratio = torch.exp(log_ratio)
                    unclipped = ratio * batch.advantages
                    clipped = torch.clamp(ratio, 1.0 - self.cfg.clip_range, 1.0 + self.cfg.clip_range) * batch.advantages
                    policy_loss = -torch.min(unclipped, clipped).mean()
                    value_loss = nn.functional.smooth_l1_loss(values.float(), batch.returns.float())
                    loss = policy_loss + self.cfg.value_coef * value_loss - self.cfg.entropy_coef * entropy
                    approx_kl = ((ratio - 1.0) - log_ratio).mean()

                self.optimizer.zero_grad(set_to_none=True)
                self.scaler.scale(loss).backward()
                self.scaler.unscale_(self.optimizer)
                nn.utils.clip_grad_norm_(self.model.parameters(), self.cfg.max_grad_norm)
                self.scaler.step(self.optimizer)
                self.scaler.update()

                metrics["policy_loss"] += float(policy_loss.detach().cpu())
                metrics["value_loss"] += float(value_loss.detach().cpu())
                metrics["entropy"] += float(entropy.detach().cpu())
                metrics["approx_kl"] += float(approx_kl.detach().cpu())
                metrics["loss"] += float(loss.detach().cpu())
                steps += 1
                if self.cfg.target_kl > 0.0 and float(approx_kl.detach().cpu()) > self.cfg.target_kl * 1.5:
                    metrics["early_stop"] += 1.0
                    stop_early = True
                del batch, log_probs, entropy_values, values, log_ratio, ratio, unclipped, clipped, policy_loss, value_loss, loss, approx_kl
                if device.type == "cuda" and steps % 4 == 0:
                    torch.cuda.empty_cache()

                if stop_early:
                    break
            if stop_early:
                break

        if steps:
            for key in metrics:
                metrics[key] /= steps
        return metrics


def compute_gae(
    rewards: Tensor,
    values: Tensor,
    dones: Tensor,
    last_value: Tensor,
    gamma: float = 0.997,
    gae_lambda: float = 0.97,
) -> tuple[Tensor, Tensor]:
    advantages = torch.zeros_like(rewards)
    next_advantage = torch.zeros_like(last_value)
    next_value = last_value
    for t in reversed(range(rewards.shape[0])):
        mask = 1.0 - dones[t]
        delta = rewards[t] + gamma * next_value * mask - values[t]
        next_advantage = delta + gamma * gae_lambda * mask * next_advantage
        advantages[t] = next_advantage
        next_value = values[t]
    returns = advantages + values
    return returns, advantages
