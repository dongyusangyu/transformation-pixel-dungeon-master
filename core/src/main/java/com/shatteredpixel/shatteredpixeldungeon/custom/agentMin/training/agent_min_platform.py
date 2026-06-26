from __future__ import annotations

import argparse
import logging
import random
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Protocol

import torch
from torch import Tensor

from agent_min_model import AgentMinActorCritic, AgentMinModelConfig
from ppo_trainer import PPOConfig, PPOTrainer, RolloutBatch, compute_gae


LOG_DIR = Path(__file__).resolve().parent / "logs"


def setup_logger(verbose: bool = True) -> logging.Logger:
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    logger = logging.getLogger("agent_min_platform")
    logger.setLevel(logging.DEBUG)
    logger.handlers.clear()

    formatter = logging.Formatter("%(asctime)s | %(levelname)s | %(message)s")
    file_handler = logging.FileHandler(LOG_DIR / "platform_test.log", encoding="utf-8")
    file_handler.setLevel(logging.DEBUG)
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)

    if verbose:
        stream_handler = logging.StreamHandler(sys.stdout)
        stream_handler.setLevel(logging.INFO)
        stream_handler.setFormatter(formatter)
        logger.addHandler(stream_handler)

    return logger


@dataclass
class AgentMinObservation:
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
    forced_skill: Tensor | None = None
    pending_reward: float = 0.0
    episode_reward: float = 0.0

    def to_batch(self, device: torch.device | None = None) -> dict[str, Tensor]:
        def batched(tensor: Tensor) -> Tensor:
            tensor = tensor.unsqueeze(0)
            return tensor if device is None or tensor.device == device else tensor.to(device)

        return {
            "level_tensor": batched(self.level_tensor),
            "explored_global_matrix": batched(self.explored_global_matrix),
            "agent_visited_matrix": batched(self.agent_visited_matrix),
            "hero_vector": batched(self.hero_vector),
            "inventory_matrix": batched(self.inventory_matrix),
            "inventory_summary_vector": batched(self.inventory_summary_vector),
            "option_vector": batched(self.option_vector),
            "mob_matrix": batched(self.mob_matrix),
            "history_matrix": batched(self.history_matrix),
            "action_matrix": batched(self.action_matrix),
            "action_mask": batched(self.action_mask),
            "monitor_item_mask": batched(self.monitor_item_mask),
            "monitor_cell_mask": batched(self.monitor_cell_mask),
            "monitor_option_mask": batched(self.monitor_option_mask),
            "skill_mask": batched(self.skill_mask),
            "action_skill_mask": batched(self.action_skill_mask),
            "forced_skill": batched(self.forced_skill if self.forced_skill is not None else torch.full((), -1, dtype=torch.long)),
        }


class GameAdapter(Protocol):
    def reset(self) -> AgentMinObservation:
        ...

    def step(self, action_id: int) -> tuple[AgentMinObservation, float, bool, dict[str, float]]:
        ...


class ObservationValidator:
    @staticmethod
    def validate(obs: AgentMinObservation) -> None:
        checks = {
            "level_tensor": obs.level_tensor.ndim == 3,
            "explored_global_matrix": obs.explored_global_matrix.ndim == 2,
            "agent_visited_matrix": obs.agent_visited_matrix.ndim == 2,
            "hero_vector": obs.hero_vector.ndim == 1,
            "inventory_matrix": obs.inventory_matrix.ndim == 2,
            "inventory_summary_vector": obs.inventory_summary_vector.ndim == 1,
            "option_vector": obs.option_vector.ndim == 1,
            "mob_matrix": obs.mob_matrix.ndim == 2,
            "history_matrix": obs.history_matrix.ndim == 2,
            "action_matrix": obs.action_matrix.ndim == 2,
            "action_mask": obs.action_mask.ndim == 1,
            "monitor_item_mask": obs.monitor_item_mask.ndim == 1,
            "monitor_cell_mask": obs.monitor_cell_mask.ndim == 1,
            "monitor_option_mask": obs.monitor_option_mask.ndim == 1,
            "skill_mask": obs.skill_mask.ndim == 1,
            "action_skill_mask": obs.action_skill_mask.ndim == 2,
            "action_rows_match": obs.action_matrix.shape[0] == obs.action_mask.shape[0],
            "monitor_rows_match": obs.monitor_item_mask.shape[0] == obs.inventory_matrix.shape[0],
            "monitor_cell_rows_match": obs.monitor_cell_mask.shape[0] == 31 * 31,
            "skill_rows_match": obs.action_skill_mask.shape[0] == obs.skill_mask.shape[0],
            "skill_action_cols_match": obs.action_skill_mask.shape[1] == obs.action_mask.shape[0],
            "forced_skill_scalar": obs.forced_skill is None or obs.forced_skill.ndim == 0,
        }
        failed = [name for name, ok in checks.items() if not ok]
        if failed:
            raise ValueError(f"Invalid AgentMin observation: {failed}")
        if torch.isnan(obs.level_tensor).any() or torch.isnan(obs.hero_vector).any():
            raise ValueError("Invalid AgentMin observation: NaN in level_tensor or hero_vector")
        if obs.agent_visited_matrix.shape != obs.level_tensor.shape[1:]:
            raise ValueError("Invalid AgentMin observation: agent_visited_matrix shape does not match level map")
        if obs.explored_global_matrix.shape != (127, 127):
            raise ValueError("Invalid AgentMin observation: explored_global_matrix must be 127x127")
        if obs.action_mask.sum().item() <= 0:
            raise ValueError("Invalid AgentMin observation: action_mask has no valid action")
        if obs.skill_mask.sum().item() <= 0:
            raise ValueError("Invalid AgentMin observation: skill_mask has no valid skill")


def build_rollout(transitions: list[dict[str, Tensor | float | bool]], returns: Tensor, advantages: Tensor) -> RolloutBatch:
    return RolloutBatch(
        level_tensor=torch.cat([t["level_tensor"].detach().cpu() for t in transitions], dim=0),
        explored_global_matrix=torch.cat([t["explored_global_matrix"].detach().cpu() for t in transitions], dim=0),
        agent_visited_matrix=torch.cat([t["agent_visited_matrix"].detach().cpu() for t in transitions], dim=0),
        hero_vector=torch.cat([t["hero_vector"].detach().cpu() for t in transitions], dim=0),
        inventory_matrix=torch.cat([t["inventory_matrix"].detach().cpu() for t in transitions], dim=0),
        inventory_summary_vector=torch.cat([t["inventory_summary_vector"].detach().cpu() for t in transitions], dim=0),
        option_vector=torch.cat([t["option_vector"].detach().cpu() for t in transitions], dim=0),
        mob_matrix=torch.cat([t["mob_matrix"].detach().cpu() for t in transitions], dim=0),
        history_matrix=torch.cat([t["history_matrix"].detach().cpu() for t in transitions], dim=0),
        action_matrix=torch.cat([t["action_matrix"].detach().cpu() for t in transitions], dim=0),
        action_mask=torch.cat([t["action_mask"].detach().cpu() for t in transitions], dim=0),
        monitor_item_mask=torch.cat([t["monitor_item_mask"].detach().cpu() for t in transitions], dim=0),
        monitor_cell_mask=torch.cat([t["monitor_cell_mask"].detach().cpu() for t in transitions], dim=0),
        monitor_option_mask=torch.cat([t["monitor_option_mask"].detach().cpu() for t in transitions], dim=0),
        skill_mask=torch.cat([t["skill_mask"].detach().cpu() for t in transitions], dim=0),
        action_skill_mask=torch.cat([t["action_skill_mask"].detach().cpu() for t in transitions], dim=0),
        forced_skill=torch.cat([t["forced_skill"].detach().view(1).cpu() for t in transitions], dim=0),
        actions=torch.cat([t["action"].detach().view(1).cpu() for t in transitions], dim=0),
        skills=torch.cat([t["skill"].detach().view(1).cpu() for t in transitions], dim=0),
        heads=torch.cat([t["head"].detach().view(1).cpu() for t in transitions], dim=0),
        talent_target_embeddings=torch.cat([t["talent_target_embedding"].detach().cpu() for t in transitions], dim=0),
        talent_target_types=torch.cat([t["talent_target_type"].detach().view(1).cpu() for t in transitions], dim=0),
        monitor_item_rows=torch.cat([t["monitor_item_row"].detach().view(1).cpu() for t in transitions], dim=0),
        monitor_cell_indices=torch.cat([t["monitor_cell_index"].detach().view(1).cpu() for t in transitions], dim=0),
        monitor_option_indices=torch.cat([t["monitor_option_index"].detach().view(1).cpu() for t in transitions], dim=0),
        old_log_probs=torch.cat([t["log_prob"].detach().view(1).cpu() for t in transitions], dim=0),
        returns=returns.detach().cpu(),
        advantages=advantages.detach().cpu(),
    )


def move_rollout_to_device(rollout: RolloutBatch, device: torch.device) -> RolloutBatch:
    return RolloutBatch(
        level_tensor=rollout.level_tensor.to(device),
        explored_global_matrix=rollout.explored_global_matrix.to(device),
        agent_visited_matrix=rollout.agent_visited_matrix.to(device),
        hero_vector=rollout.hero_vector.to(device),
        inventory_matrix=rollout.inventory_matrix.to(device),
        inventory_summary_vector=rollout.inventory_summary_vector.to(device),
        option_vector=rollout.option_vector.to(device),
        mob_matrix=rollout.mob_matrix.to(device),
        history_matrix=rollout.history_matrix.to(device),
        action_matrix=rollout.action_matrix.to(device),
        action_mask=rollout.action_mask.to(device),
        monitor_item_mask=rollout.monitor_item_mask.to(device),
        monitor_cell_mask=rollout.monitor_cell_mask.to(device),
        monitor_option_mask=rollout.monitor_option_mask.to(device),
        skill_mask=rollout.skill_mask.to(device),
        action_skill_mask=rollout.action_skill_mask.to(device),
        forced_skill=rollout.forced_skill.to(device),
        actions=rollout.actions.to(device),
        skills=rollout.skills.to(device),
        heads=rollout.heads.to(device),
        talent_target_embeddings=rollout.talent_target_embeddings.to(device),
        talent_target_types=rollout.talent_target_types.to(device),
        monitor_item_rows=rollout.monitor_item_rows.to(device),
        monitor_cell_indices=rollout.monitor_cell_indices.to(device),
        monitor_option_indices=rollout.monitor_option_indices.to(device),
        old_log_probs=rollout.old_log_probs.to(device),
        returns=rollout.returns.to(device),
        advantages=rollout.advantages.to(device),
    )


class AgentMinRuntime:
    def __init__(self, device: str | None = None, logger: logging.Logger | None = None):
        self.device = torch.device(device or ("cuda" if torch.cuda.is_available() else "cpu"))
        self.logger = logger or setup_logger()
        self.model: AgentMinActorCritic | None = None
        self.trainer: PPOTrainer | None = None

    def ensure_model(self, obs: AgentMinObservation) -> None:
        if self.model is not None:
            return
        cfg = AgentMinModelConfig(
            level_channels=obs.level_tensor.shape[0] + 1,
            hero_dim=obs.hero_vector.shape[0],
            inventory_rows=obs.inventory_matrix.shape[0],
            inventory_features=obs.inventory_matrix.shape[1],
            inventory_summary_dim=obs.inventory_summary_vector.shape[0],
            option_dim=obs.option_vector.shape[0],
            mob_rows=obs.mob_matrix.shape[0],
            mob_features=obs.mob_matrix.shape[1],
            history_rows=obs.history_matrix.shape[0],
            history_features=obs.history_matrix.shape[1],
            action_rows=obs.action_matrix.shape[0],
            action_features=obs.action_matrix.shape[1],
            skill_count=obs.skill_mask.shape[0],
        )
        self.model = AgentMinActorCritic(cfg).to(self.device)
        self.trainer = PPOTrainer(self.model, PPOConfig(epochs=1, minibatch_size=8))
        self.logger.info(
            "model initialized | device=%s | hero_dim=%s | map=%sx%s | actions=%s",
            self.device,
            cfg.hero_dim,
            obs.level_tensor.shape[1],
            obs.level_tensor.shape[2],
            cfg.action_rows,
        )

    @torch.no_grad()
    def act(self, obs: AgentMinObservation, deterministic: bool = False) -> dict[str, Tensor]:
        ObservationValidator.validate(obs)
        self.ensure_model(obs)
        assert self.model is not None
        self.model.eval()
        return self.model.act(obs.to_batch(self.device), deterministic=deterministic)

    def update(self, transitions: list[dict[str, Tensor | float | bool]]) -> dict[str, float]:
        if not transitions:
            return {}
        assert self.model is not None and self.trainer is not None

        rewards = torch.tensor([float(t["reward"]) for t in transitions], dtype=torch.float32)
        dones = torch.tensor([float(t["done"]) for t in transitions], dtype=torch.float32)
        values = torch.cat([t["value"].detach().view(1).cpu() for t in transitions], dim=0)
        last_value = torch.zeros(())
        returns, advantages = compute_gae(rewards, values, dones, last_value)

        rollout = build_rollout(transitions, returns, advantages)
        return self.trainer.update(rollout)


class MockDungeonAdapter:
    def __init__(self, seed: int = 7, height: int = 31, width: int = 31):
        self.random = random.Random(seed)
        self.height = height
        self.width = width
        self.step_id = 0
        self.hp_ratio = 1.0
        self.episode_reward = 0.0
        self.history = torch.zeros(16, 16)

    def reset(self) -> AgentMinObservation:
        self.step_id = 0
        self.hp_ratio = 1.0
        self.episode_reward = 0.0
        self.history.zero_()
        return self._make_observation(0.0)

    def step(self, action_id: int) -> tuple[AgentMinObservation, float, bool, dict[str, float]]:
        self.step_id += 1
        kind = action_id % 8
        reward = -0.02
        if kind == 1:
            reward += 0.08
        elif kind == 2:
            reward += 0.18
        elif kind == 3:
            reward += 0.35
        elif kind == 4 and self.step_id > 10:
            reward += 4.0
        if self.random.random() < 0.18:
            self.hp_ratio = max(0.0, self.hp_ratio - 0.05)
            reward -= 0.4

        done = self.step_id >= 24 or self.hp_ratio <= 0.0 or reward > 3.0
        self.episode_reward += reward
        self._push_history(action_id, reward)
        obs = self._make_observation(reward)
        info = {"hp_ratio": self.hp_ratio, "episode_reward": self.episode_reward}
        return obs, reward, done, info

    def _make_observation(self, pending_reward: float) -> AgentMinObservation:
        cfg = AgentMinModelConfig()
        level = torch.rand(cfg.level_channels - 1, self.height, self.width)
        explored_global = torch.zeros(127, 127)
        explored_global[63, 63] = 1.0
        agent_visited = torch.zeros(self.height, self.width)
        agent_visited[:min(self.step_id + 1, self.height), 0] = 1.0
        hero = torch.rand(cfg.full_hero_dim)
        hero[7] = self.hp_ratio
        inventory = torch.rand(cfg.inventory_rows, cfg.inventory_features) * 0.2
        inventory_summary = torch.rand(cfg.inventory_summary_dim)
        option_vector = torch.rand(cfg.option_dim)
        mobs = torch.rand(cfg.mob_rows, cfg.mob_features) * 0.2
        actions = torch.rand(cfg.action_rows, cfg.action_features) * 0.3
        mask = torch.zeros(cfg.action_rows)
        monitor_item_mask = torch.zeros(cfg.inventory_rows)
        monitor_item_mask[:8] = 1.0
        monitor_cell_mask = torch.ones(31 * 31)
        monitor_option_mask = torch.zeros(32)
        monitor_option_mask[:4] = 1.0
        valid_count = 8 + min(self.step_id, 12)
        mask[:valid_count] = 1.0
        skill_mask = torch.zeros(cfg.skill_count)
        action_skill_mask = torch.zeros(cfg.skill_count, cfg.action_rows)
        for action in range(valid_count):
            skill = action % cfg.skill_count
            skill_mask[skill] = 1.0
            action_skill_mask[skill, action] = 1.0
        return AgentMinObservation(
            level_tensor=level,
            explored_global_matrix=explored_global,
            agent_visited_matrix=agent_visited,
            hero_vector=hero,
            inventory_matrix=inventory,
            inventory_summary_vector=inventory_summary,
            option_vector=option_vector,
            mob_matrix=mobs,
            history_matrix=self.history.clone(),
            action_matrix=actions,
            action_mask=mask,
            monitor_item_mask=monitor_item_mask,
            monitor_cell_mask=monitor_cell_mask,
            monitor_option_mask=monitor_option_mask,
            skill_mask=skill_mask,
            action_skill_mask=action_skill_mask,
            forced_skill=torch.full((), -1, dtype=torch.long),
            pending_reward=pending_reward,
            episode_reward=self.episode_reward,
        )

    def _push_history(self, action_id: int, reward: float) -> None:
        self.history = torch.roll(self.history, shifts=1, dims=0)
        self.history[0].zero_()
        self.history[0, 0] = 1.0
        self.history[0, 1] = (action_id % 8 + 1) / 8.0
        self.history[0, 2] = max(-1.0, min(1.0, reward / 10.0))
        self.history[0, 11] = self.hp_ratio
        self.history[0, 14] = min(1.0, self.step_id / 30.0)


def run_one_round(args: argparse.Namespace) -> int:
    logger = setup_logger(verbose=True)
    logger.info("platform test started")
    runtime = AgentMinRuntime(device=args.device, logger=logger)
    adapter: GameAdapter = MockDungeonAdapter(seed=args.seed, height=args.height, width=args.width)
    obs = adapter.reset()
    transitions: list[dict[str, Tensor | float | bool]] = []
    total_reward = 0.0

    try:
        for step in range(args.steps):
            action_out = runtime.act(obs, deterministic=args.deterministic)
            action_id = int(action_out["action"].item())
            batch = obs.to_batch()
            next_obs, reward, done, info = adapter.step(action_id)
            transitions.append(
                {
                    **batch,
                    "action": action_out["action"].detach().cpu(),
                    "skill": action_out["skill"].detach().cpu(),
                    "head": action_out["head"].detach().cpu(),
                    "talent_target_embedding": action_out["talent_target_embedding"].detach().cpu(),
                    "talent_target_type": action_out["talent_target_type"].detach().cpu(),
                    "monitor_item_row": action_out["monitor_item_row"].detach().cpu(),
                    "monitor_cell_index": action_out["monitor_cell_index"].detach().cpu(),
                    "monitor_option_index": action_out["monitor_option_index"].detach().cpu(),
                    "log_prob": action_out["log_prob"].detach().cpu(),
                    "value": action_out["value"].detach().cpu(),
                    "reward": float(reward),
                    "done": bool(done),
                }
            )
            total_reward += reward
            logger.info(
                "step=%02d action=%02d reward=%+.3f total=%+.3f hp=%.2f done=%s valid_actions=%d",
                step,
                action_id,
                reward,
                total_reward,
                info["hp_ratio"],
                done,
                int(obs.action_mask.sum().item()),
            )
            obs = next_obs
            if done:
                break

        metrics = runtime.update(transitions)
        logger.info("ppo update metrics=%s", {k: round(v, 6) for k, v in metrics.items()})
        logger.info("platform test finished | transitions=%d | total_reward=%+.3f", len(transitions), total_reward)
        return 0
    except Exception:
        logger.exception("platform test failed")
        return 1


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run one AgentMin RL platform test round.")
    parser.add_argument("--steps", type=int, default=24)
    parser.add_argument("--seed", type=int, default=7)
    parser.add_argument("--height", type=int, default=32)
    parser.add_argument("--width", type=int, default=32)
    parser.add_argument("--device", type=str, default=None)
    parser.add_argument("--deterministic", action="store_true")
    return parser.parse_args()


if __name__ == "__main__":
    raise SystemExit(run_one_round(parse_args()))
