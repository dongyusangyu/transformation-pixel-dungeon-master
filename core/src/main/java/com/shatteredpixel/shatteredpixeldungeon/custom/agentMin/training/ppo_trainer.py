from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable

import torch
from torch import Tensor, nn
from torch.optim import AdamW

from agent_min_model import AgentMinActorCritic


@dataclass
class PPOConfig:
    lr: float = 3e-4
    gamma: float = 0.99
    gae_lambda: float = 0.95
    clip_range: float = 0.2
    value_coef: float = 0.5
    entropy_coef: float = 0.01
    max_grad_norm: float = 0.5
    epochs: int = 4
    minibatch_size: int = 128


@dataclass
class RolloutBatch:
    level_tensor: Tensor
    hero_vector: Tensor
    inventory_matrix: Tensor
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
                hero_vector=self.hero_vector[idx],
                inventory_matrix=self.inventory_matrix[idx],
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
        self.optimizer = AdamW(model.parameters(), lr=self.cfg.lr)

    def update(self, rollout: RolloutBatch) -> dict[str, float]:
        self.model.train()
        advantages = (rollout.advantages - rollout.advantages.mean()) / (rollout.advantages.std() + 1e-8)
        rollout = RolloutBatch(
            level_tensor=rollout.level_tensor,
            hero_vector=rollout.hero_vector,
            inventory_matrix=rollout.inventory_matrix,
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
                logits, values = self.model(
                    level_tensor=batch.level_tensor,
                    hero_vector=batch.hero_vector,
                    inventory_matrix=batch.inventory_matrix,
                    mob_matrix=batch.mob_matrix,
                    history_matrix=batch.history_matrix,
                    action_matrix=batch.action_matrix,
                    action_mask=batch.action_mask,
                )
                dist = torch.distributions.Categorical(logits=logits)
                log_probs = dist.log_prob(batch.actions)
                entropy = dist.entropy().mean()

                ratio = torch.exp(log_probs - batch.old_log_probs)
                unclipped = ratio * batch.advantages
                clipped = torch.clamp(ratio, 1.0 - self.cfg.clip_range, 1.0 + self.cfg.clip_range) * batch.advantages
                policy_loss = -torch.min(unclipped, clipped).mean()
                value_loss = nn.functional.mse_loss(values, batch.returns)
                loss = policy_loss + self.cfg.value_coef * value_loss - self.cfg.entropy_coef * entropy

                self.optimizer.zero_grad(set_to_none=True)
                loss.backward()
                nn.utils.clip_grad_norm_(self.model.parameters(), self.cfg.max_grad_norm)
                self.optimizer.step()

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
    gamma: float = 0.99,
    gae_lambda: float = 0.95,
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
