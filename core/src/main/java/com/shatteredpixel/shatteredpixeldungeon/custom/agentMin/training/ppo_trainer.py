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
    lr: float = 6e-5
    # 推荐区间：0.990 ~ 0.998。走出房间、穿门、下楼都是长延迟收益，默认提高到 0.997。
    gamma: float = 0.997
    # 推荐区间：0.93 ~ 0.98。更高 GAE 有助于把穿门/新房间收益回传到前几步移动。
    gae_lambda: float = 0.97
    # 推荐区间：0.12 ~ 0.25。默认 0.18，减少 PPO 每次更新对策略分布的猛推。
    clip_range: float = 0.18
    # 推荐区间：0.35 ~ 0.80。奖励尺度被收紧后 0.55 比 0.5 略重视价值估计。
    value_coef: float = 0.55
    # 推荐区间：0.008 ~ 0.03。默认 0.018，用于维持探索，但避免纯随机打转。
    entropy_coef: float = 0.018
    # 推荐区间：0.4 ~ 1.0。默认 0.6，压住新奖励箱下偶发大梯度。
    max_grad_norm: float = 0.6
    # 推荐区间：3 ~ 8。默认 4，在线样本相关性强，不宜过度重复更新。
    epochs: int = 4
    # 推荐区间：64 ~ 512。默认 128；若 update_interval=256，则每轮两个 minibatch。
    minibatch_size: int = 128
    # 推荐区间：0 ~ 0.03。默认 0.01，减少大模型过拟合最近几十步循环。
    weight_decay: float = 0.01
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
    actions: Tensor
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
                actions=self.actions[idx],
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
        advantages = (rollout.advantages - rollout.advantages.mean()) / (rollout.advantages.std() + 1e-8)
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
            actions=rollout.actions,
            old_log_probs=rollout.old_log_probs,
            returns=rollout.returns,
            advantages=advantages,
        )

        metrics = {"policy_loss": 0.0, "value_loss": 0.0, "entropy": 0.0, "loss": 0.0}
        steps = 0
        for _ in range(self.cfg.epochs):
            for batch in rollout.minibatches(self.cfg.minibatch_size):
                with self._autocast():
                    logits, values = self.model(
                        level_tensor=batch.level_tensor,
                        explored_global_matrix=batch.explored_global_matrix,
                        agent_visited_matrix=batch.agent_visited_matrix,
                        hero_vector=batch.hero_vector,
                        inventory_matrix=batch.inventory_matrix,
                        inventory_summary_vector=batch.inventory_summary_vector,
                        option_vector=batch.option_vector,
                        mob_matrix=batch.mob_matrix,
                        history_matrix=batch.history_matrix,
                        action_matrix=batch.action_matrix,
                        action_mask=batch.action_mask,
                    )
                    dist = torch.distributions.Categorical(logits=logits.float())
                    log_probs = dist.log_prob(batch.actions)
                    entropy = dist.entropy().mean()

                    ratio = torch.exp(log_probs - batch.old_log_probs)
                    unclipped = ratio * batch.advantages
                    clipped = torch.clamp(ratio, 1.0 - self.cfg.clip_range, 1.0 + self.cfg.clip_range) * batch.advantages
                    policy_loss = -torch.min(unclipped, clipped).mean()
                    value_loss = nn.functional.smooth_l1_loss(values.float(), batch.returns.float())
                    loss = policy_loss + self.cfg.value_coef * value_loss - self.cfg.entropy_coef * entropy

                self.optimizer.zero_grad(set_to_none=True)
                self.scaler.scale(loss).backward()
                self.scaler.unscale_(self.optimizer)
                nn.utils.clip_grad_norm_(self.model.parameters(), self.cfg.max_grad_norm)
                self.scaler.step(self.optimizer)
                self.scaler.update()

                metrics["policy_loss"] += float(policy_loss.detach().cpu())
                metrics["value_loss"] += float(value_loss.detach().cpu())
                metrics["entropy"] += float(entropy.detach().cpu())
                metrics["loss"] += float(loss.detach().cpu())
                steps += 1

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
