from __future__ import annotations

import argparse
import json
import logging
import math
import socketserver
import sys
from pathlib import Path
from typing import Any

import torch
from torch import Tensor

from agent_min_model import AgentMinActorCritic, AgentMinModelConfig
from agent_min_platform import LOG_DIR, ObservationValidator, AgentMinObservation, build_rollout, move_rollout_to_device
from ppo_trainer import PPOConfig, PPOTrainer, compute_gae


def setup_logger() -> logging.Logger:
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    logger = logging.getLogger("agent_min_bridge")
    logger.setLevel(logging.DEBUG)
    logger.handlers.clear()
    formatter = logging.Formatter("%(asctime)s | %(levelname)s | %(message)s")

    stream = logging.StreamHandler(sys.stdout)
    stream.setLevel(logging.INFO)
    stream.setFormatter(formatter)
    logger.addHandler(stream)

    file_handler = logging.FileHandler(LOG_DIR / "bridge_server.log", encoding="utf-8")
    file_handler.setLevel(logging.DEBUG)
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)
    return logger


class OnlineTrainer:
    def __init__(self, device: str | None, update_interval: int, checkpoint: Path | None, ppo_config: PPOConfig, logger: logging.Logger):
        self.device = self._resolve_device(device)
        if self.device.type == "cpu":
            torch.set_num_threads(1)
        else:
            self._configure_cuda_runtime()
        self.update_interval = update_interval
        self.checkpoint = checkpoint
        self.ppo_config = ppo_config
        self.logger = logger
        self.model: AgentMinActorCritic | None = None
        self.trainer: PPOTrainer | None = None
        self.signature: tuple[tuple[int, ...], ...] | None = None
        self.previous: dict[str, Tensor] | None = None
        self.transitions: list[dict[str, Tensor | float | bool]] = []
        self.step = 0
        self.total_reward = 0.0
        self.last_episode_reward = 0.0
        self.last_depth_bucket = 0
        self.explorer = EpisodicExplorer()

    def _configure_cuda_runtime(self) -> None:
        try:
            torch.backends.cuda.matmul.allow_tf32 = True
            torch.backends.cudnn.allow_tf32 = True
            torch.backends.cudnn.benchmark = True
            torch.set_float32_matmul_precision("high")
        except Exception:
            pass

    def _resolve_device(self, device: str | None) -> torch.device:
        if device is None or device == "" or device.lower() == "auto":
            if torch.cuda.is_available():
                try:
                    free, _total = torch.cuda.mem_get_info()
                    if free >= 512 * 1024 * 1024:
                        return torch.device("cuda")
                    self.logger.warning("cuda free memory is low (%d MB); falling back to cpu", free // (1024 * 1024))
                except Exception as exc:
                    self.logger.warning("cuda memory probe failed; falling back to cpu: %s", exc)
            return torch.device("cpu")
        return torch.device(device)

    def handle_observation(self, payload: dict[str, Any]) -> int:
        obs = self._payload_to_observation(payload)
        ObservationValidator.validate(obs)
        self._ensure_model(obs)

        self._maybe_reset_episode(obs)
        intrinsic_bonus = self.explorer.bonus(obs)
        reward = float(payload.get("pending_reward", 0.0)) + intrinsic_bonus
        self.total_reward += reward
        if self.previous is not None:
            self.previous["reward"] = reward
            self.previous["done"] = False
            self.transitions.append(self.previous)
            if len(self.transitions) >= self.update_interval:
                self._update()

        assert self.model is not None
        self.model.eval()
        with torch.no_grad():
            batch = obs.to_batch(self.device)
            out = self.model.act(batch)
        rollout_entry = obs.to_batch()
        action = int(out["action"].item())
        self.previous = {
            **rollout_entry,
            "action": out["action"].detach().cpu(),
            "log_prob": out["log_prob"].detach().cpu(),
            "value": out["value"].detach().cpu(),
        }
        self.step += 1
        self.logger.info(
            "step=%05d action=%02d reward=%+.3f intrinsic=%+.3f total=%+.3f valid_actions=%d",
            self.step,
            action,
            reward,
            intrinsic_bonus,
            self.total_reward,
            int(obs.action_mask.sum().item()),
        )
        self.last_episode_reward = obs.episode_reward
        self.last_depth_bucket = int(round(float(obs.hero_vector[0].item()) * 30.0))
        return action

    def _maybe_reset_episode(self, obs: AgentMinObservation) -> None:
        depth_bucket = int(round(float(obs.hero_vector[0].item()) * 30.0))
        reward_reset = abs(obs.episode_reward) < 1e-6 and abs(self.last_episode_reward) > 1.0
        depth_reset = depth_bucket == 1 and self.last_depth_bucket > max(3, depth_bucket)
        if reward_reset or depth_reset:
            self.explorer.reset()
            self.previous = None
            self.transitions.clear()
            self.logger.info("episodic exploration memory reset")

    def _payload_to_observation(self, payload: dict[str, Any]) -> AgentMinObservation:
        level_tensor = self._tensor(payload["level_tensor"], dtype=torch.float32)
        agent_visited = payload.get("agent_visited_matrix")
        if agent_visited is None:
            agent_visited_tensor = torch.zeros(level_tensor.shape[1:], dtype=torch.float32)
        else:
            agent_visited_tensor = self._tensor(agent_visited, dtype=torch.float32)
        explored_global = payload.get("explored_global_matrix")
        if explored_global is None:
            explored_global_tensor = torch.zeros((127, 127), dtype=torch.float32)
        else:
            explored_global_tensor = self._tensor(explored_global, dtype=torch.float32)
        return AgentMinObservation(
            level_tensor=level_tensor,
            explored_global_matrix=explored_global_tensor,
            agent_visited_matrix=agent_visited_tensor,
            hero_vector=self._tensor(payload["hero_vector"], dtype=torch.float32),
            inventory_matrix=self._tensor(payload["inventory_matrix"], dtype=torch.float32),
            inventory_summary_vector=self._tensor(payload["inventory_summary_vector"], dtype=torch.float32),
            option_vector=self._tensor(payload["option_vector"], dtype=torch.float32),
            mob_matrix=self._tensor(payload["mob_matrix"], dtype=torch.float32),
            history_matrix=self._tensor(payload["history_matrix"], dtype=torch.float32),
            action_matrix=self._tensor(payload["action_matrix"], dtype=torch.float32),
            action_mask=self._tensor(payload["action_mask"], dtype=torch.float32),
            pending_reward=float(payload.get("pending_reward", 0.0)),
            episode_reward=float(payload.get("episode_reward", 0.0)),
        )

    def _tensor(self, value: Any, dtype: torch.dtype) -> Tensor:
        return torch.as_tensor(value, dtype=dtype).contiguous()

    def _ensure_model(self, obs: AgentMinObservation) -> None:
        signature = self._observation_signature(obs)
        if self.model is not None and self.signature == signature:
            return
        if self.model is not None:
            self.logger.warning("observation shape changed from %s to %s; rebuilding model and clearing rollout", self.signature, signature)
            self.previous = None
            self.transitions.clear()
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
        )
        try:
            self._init_model(cfg, obs)
        except RuntimeError as exc:
            if self.device.type == "cuda" and self._is_cuda_oom(exc):
                self.logger.warning("cuda out of memory during model init; falling back to cpu")
                torch.cuda.empty_cache()
                self.device = torch.device("cpu")
                torch.set_num_threads(1)
                self._init_model(cfg, obs)
            else:
                raise
        self.signature = signature
        params = self.model.parameter_count() if self.model is not None else 0
        size_mb = params * 4 / (1024 * 1024)
        self.logger.info(
            "model initialized | device=%s | hero_dim=%d | actions=%d | params=%.2fM | fp32_state_dict~%.1fMB | ppo_epochs=%d | minibatch=%d | amp=%s",
            self.device,
            cfg.hero_dim,
            cfg.action_rows,
            params / 1_000_000,
            size_mb,
            self.ppo_config.epochs,
            self.ppo_config.minibatch_size,
            self.ppo_config.use_amp,
        )

    def _init_model(self, cfg: AgentMinModelConfig, obs: AgentMinObservation) -> None:
        self.model = AgentMinActorCritic(cfg).to(self.device)
        if self.checkpoint is not None and self.checkpoint.exists():
            try:
                self.model.load_state_dict(torch.load(self.checkpoint, map_location=self.device))
                self.logger.info("checkpoint loaded: %s", self.checkpoint)
            except RuntimeError as exc:
                self.logger.warning("checkpoint skipped because its tensor shapes do not match this run: %s", exc)
        cfg = PPOConfig(
            lr=self.ppo_config.lr,
            gamma=self.ppo_config.gamma,
            gae_lambda=self.ppo_config.gae_lambda,
            clip_range=self.ppo_config.clip_range,
            value_coef=self.ppo_config.value_coef,
            entropy_coef=self.ppo_config.entropy_coef,
            max_grad_norm=self.ppo_config.max_grad_norm,
            epochs=self.ppo_config.epochs,
            minibatch_size=min(self.ppo_config.minibatch_size, max(2, self.update_interval)),
            weight_decay=self.ppo_config.weight_decay,
            use_amp=self.ppo_config.use_amp and self.device.type == "cuda",
        )
        self.trainer = PPOTrainer(self.model, cfg)
        self._warmup(obs)

    def _observation_signature(self, obs: AgentMinObservation) -> tuple[tuple[int, ...], ...]:
        return (
            tuple(obs.level_tensor.shape),
            tuple(obs.explored_global_matrix.shape),
            tuple(obs.agent_visited_matrix.shape),
            tuple(obs.hero_vector.shape),
            tuple(obs.inventory_matrix.shape),
            tuple(obs.inventory_summary_vector.shape),
            tuple(obs.option_vector.shape),
            tuple(obs.mob_matrix.shape),
            tuple(obs.history_matrix.shape),
            tuple(obs.action_matrix.shape),
            tuple(obs.action_mask.shape),
        )

    def _warmup(self, obs: AgentMinObservation) -> None:
        if self.model is None:
            return
        self.model.eval()
        with torch.inference_mode():
            self.model.act(obs.to_batch(self.device))

    def _is_cuda_oom(self, exc: RuntimeError) -> bool:
        message = str(exc).lower()
        return "cuda" in message and ("out of memory" in message or "memoryallocation" in message)

    def _update(self) -> None:
        if not self.transitions or self.model is None or self.trainer is None:
            return
        rewards = torch.tensor([float(t["reward"]) for t in self.transitions], dtype=torch.float32)
        dones = torch.tensor([float(t["done"]) for t in self.transitions], dtype=torch.float32)
        values = torch.cat([t["value"].detach().view(1).cpu() for t in self.transitions], dim=0)
        last_value = torch.zeros(())
        returns, advantages = compute_gae(
            rewards,
            values,
            dones,
            last_value,
            gamma=self.ppo_config.gamma,
            gae_lambda=self.ppo_config.gae_lambda,
        )
        rollout = move_rollout_to_device(build_rollout(self.transitions, returns, advantages), self.device)
        metrics = self.trainer.update(rollout)
        self.logger.info("ppo_update transitions=%d metrics=%s", len(self.transitions), {k: round(v, 6) for k, v in metrics.items()})
        if self.checkpoint is not None:
            self.checkpoint.parent.mkdir(parents=True, exist_ok=True)
            try:
                torch.save(self.model.state_dict(), self.checkpoint)
                self.logger.info("checkpoint saved: %s", self.checkpoint)
            except RuntimeError as exc:
                self.logger.warning("checkpoint save skipped: %s", exc)
        self.transitions.clear()


class EpisodicExplorer:
    def __init__(self) -> None:
        self.position_counts: dict[tuple[int, int, int], int] = {}
        self.progress_counts: dict[tuple[int, int, int, int], int] = {}
        self.beta_position = 0.10
        self.beta_progress = 0.08
        self.max_bonus = 0.18

    def reset(self) -> None:
        self.position_counts.clear()
        self.progress_counts.clear()

    def bonus(self, obs: AgentMinObservation) -> float:
        depth = int(round(float(obs.hero_vector[0].item()) * 30.0))
        width = max(1, int(round(float(obs.hero_vector[25].item()) * 64.0)))
        height = max(1, int(round(float(obs.hero_vector[26].item()) * 64.0)))
        level_len = max(1, width * height)
        hero_pos = int(round(float(obs.hero_vector[27].item()) * level_len))
        hp_ratio = float(obs.hero_vector[7].item())
        visible_threat = float(obs.hero_vector[22].item())

        explored_bucket = int(round(float(obs.explored_global_matrix.sum().item()) / 12.0))
        door_bucket = int(round(float(obs.level_tensor[17].sum().item()) * 2.0))
        frontier_bucket = int(round(max(0.0, float(obs.action_matrix[:, 22].max().item())) * 10.0))
        pos_key = (depth, hero_pos // 3, int(hp_ratio * 10))
        progress_key = (depth, explored_bucket, door_bucket, frontier_bucket)

        pos_count = self.position_counts.get(pos_key, 0)
        self.position_counts[pos_key] = pos_count + 1
        progress_count = self.progress_counts.get(progress_key, 0)
        self.progress_counts[progress_key] = progress_count + 1

        combat_scale = 0.35 if visible_threat > 0.10 else 1.0
        pos_bonus = self.beta_position / math.sqrt(pos_count + 1.0)
        progress_bonus = self.beta_progress / math.sqrt(progress_count + 1.0)
        bonus = combat_scale * (pos_bonus + progress_bonus)
        return float(max(0.0, min(self.max_bonus, bonus)))


class BridgeHandler(socketserver.StreamRequestHandler):
    def handle(self) -> None:
        trainer: OnlineTrainer = self.server.trainer  # type: ignore[attr-defined]
        logger: logging.Logger = self.server.logger  # type: ignore[attr-defined]
        logger.info("java client connected: %s", self.client_address)
        for raw in self.rfile:
            try:
                payload = json.loads(raw.decode("utf-8"))
                action = trainer.handle_observation(payload)
                self.wfile.write((json.dumps({"action": action}) + "\n").encode("utf-8"))
                self.wfile.flush()
            except Exception as exc:
                logger.exception("failed to handle observation: %s", exc)
                self.wfile.write((json.dumps({"action": -1, "error": str(exc)}) + "\n").encode("utf-8"))
                self.wfile.flush()


class ThreadingBridgeServer(socketserver.ThreadingTCPServer):
    allow_reuse_address = True


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--device", default=None)
    parser.add_argument("--update-interval", type=int, default=32)
    parser.add_argument("--checkpoint", type=Path, default=LOG_DIR / "agent_min_latest.pt")
    parser.add_argument("--ppo-epochs", type=int, default=4)
    parser.add_argument("--ppo-minibatch-size", type=int, default=128)
    parser.add_argument("--ppo-lr", type=float, default=6e-5)
    parser.add_argument("--ppo-gamma", type=float, default=0.997)
    parser.add_argument("--ppo-gae-lambda", type=float, default=0.97)
    parser.add_argument("--ppo-entropy-coef", type=float, default=0.018)
    parser.add_argument("--ppo-clip-range", type=float, default=0.18)
    parser.add_argument("--ppo-value-coef", type=float, default=0.55)
    parser.add_argument("--ppo-max-grad-norm", type=float, default=0.6)
    parser.add_argument("--ppo-weight-decay", type=float, default=0.01)
    parser.add_argument("--disable-amp", action="store_true")
    args = parser.parse_args()

    logger = setup_logger()
    ppo_config = PPOConfig(
        lr=args.ppo_lr,
        gamma=args.ppo_gamma,
        gae_lambda=args.ppo_gae_lambda,
        clip_range=args.ppo_clip_range,
        value_coef=args.ppo_value_coef,
        entropy_coef=args.ppo_entropy_coef,
        max_grad_norm=args.ppo_max_grad_norm,
        epochs=args.ppo_epochs,
        minibatch_size=args.ppo_minibatch_size,
        weight_decay=args.ppo_weight_decay,
        use_amp=not args.disable_amp,
    )
    trainer = OnlineTrainer(args.device, args.update_interval, args.checkpoint, ppo_config, logger)
    with ThreadingBridgeServer((args.host, args.port), BridgeHandler) as server:
        server.trainer = trainer  # type: ignore[attr-defined]
        server.logger = logger  # type: ignore[attr-defined]
        logger.info("AgentMin bridge server listening on %s:%d", args.host, args.port)
        server.serve_forever()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
