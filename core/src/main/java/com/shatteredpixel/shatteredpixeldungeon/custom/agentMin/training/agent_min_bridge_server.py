from __future__ import annotations

import argparse
import json
import logging
import math
import socketserver
import sys
from dataclasses import asdict
from threading import RLock
from pathlib import Path
from typing import Any

import torch
from torch import Tensor
import torch.nn.functional as F

from agent_min_model import (
    AgentMinActorCritic,
    AgentMinModelConfig,
)
from agent_min_index_tables import AgentMinIndexTables
from agent_min_platform import LOG_DIR, ObservationValidator, AgentMinObservation, build_rollout
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
    def __init__(
        self,
        device: str | None,
        update_interval: int,
        checkpoint: Path | None,
        initial_checkpoint: Path | None,
        prefer_initial_checkpoint: bool,
        ppo_config: PPOConfig,
        logger: logging.Logger,
        max_vram_mb: int = 8192,
    ):
        self.logger = logger
        self.max_vram_mb = max(1024, int(max_vram_mb))
        self.device = self._resolve_device(device)
        if self.device.type == "cpu":
            torch.set_num_threads(1)
        else:
            self._configure_cuda_runtime()
        self.update_interval = update_interval
        self.checkpoint = checkpoint
        self.initial_checkpoint = initial_checkpoint
        self.prefer_initial_checkpoint = bool(prefer_initial_checkpoint)
        self.ppo_config = ppo_config
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
        self.index_tables = AgentMinIndexTables()
        # The Java client can reconnect while a prior socket worker is still unwinding.
        # Model inference and optimizer updates must never overlap on the same parameters.
        self._observation_lock = RLock()
        self.cnt=0

    def _configure_cuda_runtime(self) -> None:
        try:
            torch.backends.cuda.matmul.allow_tf32 = True
            torch.backends.cudnn.allow_tf32 = True
            torch.backends.cudnn.benchmark = True
            torch.set_float32_matmul_precision("high")
        except Exception as exc:
            self.logger.debug("cuda runtime tuning skipped: %s", exc)
        try:
            _free, total = torch.cuda.mem_get_info()
            fraction = min(0.92, max(0.05, self.max_vram_mb * 1024 * 1024 / float(total)))
            device_index = self.device.index if self.device.index is not None else torch.cuda.current_device()
            torch.cuda.set_per_process_memory_fraction(fraction, device_index)
            self.logger.info(
                "cuda memory fraction limit set to %.3f (~%dMB) on device %d",
                fraction,
                int(total * fraction / (1024 * 1024)),
                device_index,
            )
        except Exception as exc:
            self.logger.warning("cuda memory fraction limit could not be set: %s", exc)

    def _resolve_device(self, device: str | None) -> torch.device:
        if device is None or device == "" or device.lower() == "auto":
            if torch.cuda.is_available():
                try:
                    free, _total = torch.cuda.mem_get_info()
                    free_mb = free // (1024 * 1024)
                    minimum_free_mb = min(self.max_vram_mb, max(1024, self.max_vram_mb // 4))
                    if free_mb >= minimum_free_mb:
                        return torch.device("cuda")
                    self.logger.warning(
                        "cuda free memory is low (%d MB, need at least %d MB); falling back to cpu",
                        free_mb,
                        minimum_free_mb,
                    )
                except Exception as exc:
                    self.logger.warning("cuda memory probe failed; falling back to cpu: %s", exc)
            return torch.device("cpu")
        return torch.device(device)

    def handle_observation(self, payload: dict[str, Any]) -> dict[str, Any]:
        with self._observation_lock:
            return self._handle_observation_locked(payload)

    def _handle_observation_locked(self, payload: dict[str, Any]) -> dict[str, Any]:
        obs = self._payload_to_observation(payload)
        ObservationValidator.validate(obs)
        self._ensure_model(obs)
        # Probe actions are selected on the Java side to validate CellSelector and
        # wand execution. Never attach their rewards to a policy action.
        wand_probe = bool(payload.get("wand_probe", False))
        if wand_probe:
            self.previous = None
            self.transitions = []

        episode_reset = self._maybe_reset_episode(obs)
        intrinsic_bonus = self.explorer.bonus(obs)
        reward = float(payload.get("pending_reward", 0.0)) + intrinsic_bonus
        self.total_reward += reward
        update_due = False
        terminal_update = False
        if self.previous is not None and not wand_probe:
            if episode_reset:
                # The observation belongs to a new game. Close the final action
                # of the prior game as terminal instead of silently dropping it.
                self.previous["reward"] = float(payload.get("pending_reward", 0.0))
                self.previous["done"] = True
                terminal_update = True
            else:
                self.previous["reward"] = reward
                self.previous["done"] = False
            self.transitions.append(self.previous)
            self.previous = None
            update_due = terminal_update or len(self.transitions) >= self.update_interval

        assert self.model is not None
        self.model.eval()
        with torch.no_grad():
            batch = obs.to_batch(self.device)
            out = self.model.act(batch)
        if update_due:
            try:
                # For an unfinished rollout, bootstrap from the current state.
                # Treat only a detected new game as a true terminal state.
                bootstrap_value = torch.zeros(()) if terminal_update else out["value"].detach().cpu()
                self._update(bootstrap_value)
            except Exception as exc:
                self.logger.exception("PPO update failed and was skipped to keep realtime control alive: %s", exc)
                self.transitions = []
                if self.device.type == "cuda":
                    torch.cuda.empty_cache()
        rollout_entry = obs.to_batch()
        action = int(out["action"].item())
        if not wand_probe:
            self.previous = {
                **rollout_entry,
                "action": out["action"].detach().cpu(),
                "skill": out["skill"].detach().cpu(),
                "head": out["head"].detach().cpu(),
                "talent_target_embedding": out["talent_target_embedding"].detach().cpu(),
                "talent_target_type": out["talent_target_type"].detach().cpu(),
                "monitor_item_row": out["monitor_item_row"].detach().cpu(),
                "monitor_cell_index": out["monitor_cell_index"].detach().cpu(),
                "monitor_option_index": out["monitor_option_index"].detach().cpu(),
                "log_prob": out["log_prob"].detach().cpu(),
                "value": out["value"].detach().cpu(),
            }
        talent_decisions = self._select_talent_decisions(payload, out)
        self.step += 1
        self.logger.info(
            "step=%05d action=%02d reward=%+.3f intrinsic=%+.3f total=%+.3f valid_actions=%d talent_type=%d source=%s metamorph=%s sublimation=%s upgrade=%s",
            self.step,
            action,
            reward,
            intrinsic_bonus,
            self.total_reward,
            int(obs.action_mask.sum().item()),
            int(out["talent_target_type"].detach().view(-1)[0].cpu().item()),
            talent_decisions.get("metamorph_source_talent", "") or "",
            talent_decisions.get("metamorph_target_talent", "") or "",
            talent_decisions.get("sublimation_target_talent", "") or "",
            talent_decisions.get("talent_upgrade_target_talent", "") or "",
        )
        self.last_episode_reward = obs.episode_reward
        self.last_depth_bucket = int(round(float(obs.hero_vector[0].item()) * 30.0))
        response: dict[str, Any] = {
            "action": action,
            "talent_target_embedding": self._tensor_to_list(out["talent_target_embedding"]),
            "talent_target_type": int(out["talent_target_type"].detach().view(-1)[0].cpu().item()),
            "monitor_item_row": int(out["monitor_item_row"].detach().view(-1)[0].cpu().item()),
            "monitor_cell_index": int(out["monitor_cell_index"].detach().view(-1)[0].cpu().item()),
            "monitor_option_index": int(out["monitor_option_index"].detach().view(-1)[0].cpu().item()),
        }
        response.update({key: value for key, value in talent_decisions.items() if value is not None})
        return response

    def _select_talent_decisions(self, payload: dict[str, Any], out: dict[str, Tensor]) -> dict[str, Any]:
        target_embedding = out["talent_target_embedding"]
        target_type = int(out["talent_target_type"].detach().view(-1)[0].cpu().item())
        decisions: dict[str, Any] = {"metamorph_target_type": target_type}

        source, source_sim = self._best_named_candidate(
            payload.get("metamorph_source_names"),
            payload.get("metamorph_source_icons"),
            target_embedding,
            prefer_min=True,
        )
        target, target_sim, target_source = self._select_metamorph_choice(payload, target_embedding, source, target_type)
        if target:
            decisions["metamorph_source_talent"] = target_source or source
            decisions["metamorph_target_talent"] = target
            decisions["metamorph_target_similarity"] = target_sim
            decisions["metamorph_source_similarity"] = source_sim

        sublimation, sublimation_sim = self._best_named_candidate(
            payload.get("sublimation_candidate_names"),
            payload.get("sublimation_candidate_icons"),
            target_embedding,
            prefer_min=False,
        )
        if sublimation:
            decisions["sublimation_target_talent"] = sublimation
            decisions["sublimation_target_similarity"] = sublimation_sim

        upgrade, upgrade_sim = self._best_named_candidate(
            payload.get("talent_upgrade_candidate_names"),
            payload.get("talent_upgrade_candidate_icons"),
            target_embedding,
            prefer_min=False,
        )
        if upgrade:
            decisions["talent_upgrade_target_talent"] = upgrade
            decisions["talent_upgrade_similarity"] = upgrade_sim
        return decisions

    def _select_metamorph_choice(
        self,
        payload: dict[str, Any],
        target_embedding: Tensor,
        source: str | None,
        target_type: int,
    ) -> tuple[str | None, float, str | None]:
        source_names = payload.get("metamorph_choice_source_names")
        target_names = payload.get("metamorph_choice_target_names")
        target_icons = payload.get("metamorph_choice_target_icons")
        target_types = payload.get("metamorph_choice_target_types")
        if not all(isinstance(value, list) for value in (source_names, target_names, target_icons, target_types)):
            return None, 0.0, None
        count = min(len(source_names), len(target_names), len(target_icons), len(target_types))
        candidates: list[tuple[str, str, int, int]] = []
        limit = int(self.model.cfg.talent_vocab_size) if self.model is not None else 800
        for i in range(count):
            source_name = "" if source_names[i] is None else str(source_names[i]).strip()
            target_name = "" if target_names[i] is None else str(target_names[i]).strip()
            if not source_name or not target_name:
                continue
            try:
                icon = int(target_icons[i])
            except (TypeError, ValueError):
                icon = 0
            try:
                talent_type = int(target_types[i])
            except (TypeError, ValueError):
                talent_type = -1
            candidates.append((source_name, target_name, max(0, min(limit - 1, icon)), talent_type))
        if not candidates:
            return None, 0.0, None
        if source:
            source_filtered = [candidate for candidate in candidates if candidate[0] == source]
            if source_filtered:
                candidates = source_filtered
        type_filtered = [candidate for candidate in candidates if candidate[3] == target_type]
        if type_filtered:
            candidates = type_filtered
        names = [candidate[1] for candidate in candidates]
        icons = [candidate[2] for candidate in candidates]
        target, similarity = self._best_named_candidate(names, icons, target_embedding, prefer_min=False)
        if target is None:
            return None, 0.0, None
        for candidate in candidates:
            if candidate[1] == target:
                return target, similarity, candidate[0]
        return target, similarity, None

    def _best_named_candidate(self, names: Any, icons: Any, target_embedding: Tensor, prefer_min: bool) -> tuple[str | None, float]:
        if self.model is None:
            return None, 0.0
        if not isinstance(names, list) or not isinstance(icons, list) or not names or not icons:
            return None, 0.0
        count = min(len(names), len(icons))
        indices: list[int] = []
        valid_names: list[str] = []
        limit = int(self.model.cfg.talent_vocab_size)
        for i in range(count):
            name = "" if names[i] is None else str(names[i]).strip()
            try:
                icon = int(icons[i])
            except (TypeError, ValueError):
                icon = 0
            if not name:
                continue
            indices.append(max(0, min(limit - 1, icon)))
            valid_names.append(name)
        if not valid_names:
            return None, 0.0
        with torch.no_grad():
            table = self.model.talent_embedding_encoder.embedding.weight.detach()
            idx = torch.tensor(indices, dtype=torch.long, device=table.device)
            candidate_embeddings = table.index_select(0, idx)
            desired = target_embedding.detach().to(device=table.device, dtype=candidate_embeddings.dtype).view(1, -1)
            similarity = F.cosine_similarity(candidate_embeddings, desired.expand_as(candidate_embeddings), dim=-1)
            best = int((torch.argmin(similarity) if prefer_min else torch.argmax(similarity)).item())
            return valid_names[best], float(similarity[best].detach().cpu().item())

    def _tensor_to_list(self, tensor: Tensor) -> list[float]:
        flat = tensor.detach().view(-1).float().cpu()
        return [round(float(v), 6) for v in flat.tolist()]

    def _maybe_reset_episode(self, obs: AgentMinObservation) -> bool:
        depth_bucket = int(round(float(obs.hero_vector[0].item()) * 30.0))
        reward_reset = abs(obs.episode_reward) < 1e-6 and abs(self.last_episode_reward) > 1.0
        depth_reset = depth_bucket == 1 and self.last_depth_bucket > max(3, depth_bucket)
        if reward_reset or depth_reset:
            self.explorer.reset()
            self.logger.info("episodic exploration memory reset")
            return True
        return False

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
        action_mask_tensor = self._tensor(payload["action_mask"], dtype=torch.float32)
        skill_mask, action_skill_mask = self._skill_tensors(payload, action_mask_tensor)
        hero_vector = self._tensor(payload["hero_vector"], dtype=torch.float32)
        inventory_matrix = self._tensor(payload["inventory_matrix"], dtype=torch.float32)
        monitor_item_mask = self._monitor_item_mask(payload, inventory_matrix)
        monitor_cell_mask = self._fixed_mask(payload.get("monitor_cell_mask"), 31 * 31)
        monitor_option_mask = self._fixed_mask(payload.get("monitor_option_mask"), 32)
        self.index_tables.apply(inventory_matrix, payload.get("item_keys"), payload.get("modifier_keys"))
        mob_matrix = self._tensor(payload["mob_matrix"], dtype=torch.float32)
        self.index_tables.apply_mobs(mob_matrix, payload.get("mob_keys"))
        return AgentMinObservation(
            level_tensor=level_tensor,
            explored_global_matrix=explored_global_tensor,
            agent_visited_matrix=agent_visited_tensor,
            hero_vector=hero_vector,
            inventory_matrix=inventory_matrix,
            inventory_summary_vector=self._tensor(payload["inventory_summary_vector"], dtype=torch.float32),
            option_vector=self._tensor(payload["option_vector"], dtype=torch.float32),
            mob_matrix=mob_matrix,
            history_matrix=self._tensor(payload["history_matrix"], dtype=torch.float32),
            action_matrix=self._tensor(payload["action_matrix"], dtype=torch.float32),
            action_mask=action_mask_tensor,
            monitor_item_mask=monitor_item_mask,
            monitor_cell_mask=monitor_cell_mask,
            monitor_option_mask=monitor_option_mask,
            skill_mask=skill_mask,
            action_skill_mask=action_skill_mask,
            forced_skill=torch.tensor(int(payload.get("forced_skill", -1)), dtype=torch.long),
            pending_reward=float(payload.get("pending_reward", 0.0)),
            episode_reward=float(payload.get("episode_reward", 0.0)),
        )

    def _skill_tensors(self, payload: dict[str, Any], action_mask: Tensor) -> tuple[Tensor, Tensor]:
        skill_mask_value = payload.get("skill_mask")
        action_skill_value = payload.get("action_skill_mask")
        if skill_mask_value is None or action_skill_value is None:
            skill_mask = torch.ones(1, dtype=torch.float32)
            action_skill_mask = action_mask.view(1, -1).clone()
            return skill_mask, action_skill_mask
        skill_mask = self._tensor(skill_mask_value, dtype=torch.float32)
        action_skill_mask = self._tensor(action_skill_value, dtype=torch.float32)
        return skill_mask, action_skill_mask

    def _monitor_item_mask(self, payload: dict[str, Any], inventory_matrix: Tensor) -> Tensor:
        rows = int(inventory_matrix.shape[0])
        value = payload.get("monitor_item_mask")
        if value is None:
            return torch.zeros(rows, dtype=torch.float32)
        tensor = self._tensor(value, dtype=torch.float32).view(-1).clamp(0.0, 1.0)
        if tensor.numel() == rows:
            return tensor
        fixed = torch.zeros(rows, dtype=torch.float32)
        count = min(rows, int(tensor.numel()))
        if count > 0:
            fixed[:count] = tensor[:count]
        return fixed

    def _fixed_mask(self, value: Any, rows: int) -> Tensor:
        if value is None:
            return torch.zeros(rows, dtype=torch.float32)
        tensor = self._tensor(value, dtype=torch.float32).view(-1).clamp(0.0, 1.0)
        fixed = torch.zeros(rows, dtype=torch.float32)
        count = min(rows, int(tensor.numel()))
        if count > 0:
            fixed[:count] = tensor[:count]
        return fixed

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
            skill_count=obs.skill_mask.shape[0],
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
            "model initialized | device=%s | hero_dim=%d | actions=%d | params=%.2fM | fp32_state_dict~%.1fMB | ppo_epochs=%d | minibatch=%d | amp=%s | max_vram=%dMB",
            self.device,
            cfg.hero_dim,
            cfg.action_rows,
            params / 1_000_000,
            size_mb,
            self.ppo_config.epochs,
            self.trainer.cfg.minibatch_size if self.trainer is not None else self.ppo_config.minibatch_size,
            self.ppo_config.use_amp,
            self.max_vram_mb,
        )
        if self.device.type == "cuda":
            allocated, reserved = self._cuda_memory_mb()
            self.logger.info("cuda memory after init | allocated=%.1fMB | reserved=%.1fMB", allocated, reserved)

    def _init_model(self, cfg: AgentMinModelConfig, obs: AgentMinObservation) -> None:
        self.model = AgentMinActorCritic(cfg).to(self.device)
        load_path = self._checkpoint_load_path()
        if load_path is not None:
            try:
                payload = torch.load(load_path, map_location="cpu", weights_only=False)
                loaded = payload
                if isinstance(payload, dict):
                    self.index_tables.load_checkpoint(payload)
                    checkpoint_cfg = payload.get("model_config", payload.get("model_cfg"))
                    if isinstance(checkpoint_cfg, dict):
                        current_cfg = asdict(cfg)
                        changed = [
                            key for key, value in checkpoint_cfg.items()
                            if key in current_cfg and current_cfg[key] != value
                        ]
                        if changed:
                            self.logger.warning(
                                "checkpoint config differs from current observation for %s; incompatible tensors will be skipped",
                                ", ".join(changed),
                            )
                    loaded = payload.get("model_state_dict", payload.get("state_dict", payload))
                if not isinstance(loaded, dict):
                    raise RuntimeError("checkpoint does not contain a model state dictionary")
                current = self.model.state_dict()
                matched = {
                    key: value
                    for key, value in loaded.items()
                    if key in current and current[key].shape == value.shape
                }
                current.update(matched)
                self.model.load_state_dict(current)
                skipped = len(current) - len(matched)
                self.logger.info("checkpoint loaded: %s | matched=%d | skipped=%d", load_path, len(matched), skipped)
            except RuntimeError as exc:
                self.logger.warning("checkpoint skipped because its tensor shapes do not match this run: %s", exc)
        effective_minibatch = min(self.ppo_config.minibatch_size, max(2, self.update_interval))
        if self.device.type == "cuda":
            # Keep the realtime desktop bridge responsive and target the configured VRAM budget.
            effective_minibatch = min(effective_minibatch, self._cuda_minibatch_cap())
        cfg = PPOConfig(
            lr=self.ppo_config.lr,
            gamma=self.ppo_config.gamma,
            gae_lambda=self.ppo_config.gae_lambda,
            clip_range=self.ppo_config.clip_range,
            value_coef=self.ppo_config.value_coef,
            entropy_coef=self.ppo_config.entropy_coef,
            max_grad_norm=self.ppo_config.max_grad_norm,
            epochs=self.ppo_config.epochs,
            minibatch_size=effective_minibatch,
            weight_decay=self.ppo_config.weight_decay,
            target_kl=self.ppo_config.target_kl,
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
            tuple(obs.monitor_item_mask.shape),
            tuple(obs.monitor_cell_mask.shape),
            tuple(obs.monitor_option_mask.shape),
            tuple(obs.skill_mask.shape),
            tuple(obs.action_skill_mask.shape),
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

    def _is_autograd_inplace_error(self, exc: RuntimeError) -> bool:
        message = str(exc).lower()
        return "needed for gradient computation has been modified by an inplace operation" in message

    def _cuda_minibatch_cap(self) -> int:
        if self.max_vram_mb <= 4096:
            return 16
        if self.max_vram_mb <= 6144:
            return 32
        if self.max_vram_mb <= 8192:
            return 32
        if self.max_vram_mb <= 12288:
            return 64
        return 96

    def _cuda_memory_mb(self) -> tuple[float, float]:
        if self.device.type != "cuda":
            return 0.0, 0.0
        allocated = torch.cuda.memory_allocated(self.device) / (1024 * 1024)
        reserved = torch.cuda.memory_reserved(self.device) / (1024 * 1024)
        return allocated, reserved

    def _shrink_cuda_batch(self) -> bool:
        if self.trainer is None:
            return False
        old = int(self.trainer.cfg.minibatch_size)
        new = max(8, old // 2)
        if new >= old:
            return False
        self.trainer.cfg.minibatch_size = new
        self.logger.warning("reducing PPO minibatch to %d to stay within VRAM budget", new)
        return True

    def _update(self, last_value: Tensor | None = None) -> None:
        if not self.transitions or self.model is None or self.trainer is None:
            return
        transitions = self.transitions
        self.transitions = []
        rewards = torch.tensor([float(t["reward"]) for t in transitions], dtype=torch.float32)
        dones = torch.tensor([float(t["done"]) for t in transitions], dtype=torch.float32)
        values = torch.cat([t["value"].detach().view(1).cpu() for t in transitions], dim=0)
        if last_value is None:
            last_value = torch.zeros(())
        else:
            last_value = last_value.detach().view(()).cpu()
        returns, advantages = compute_gae(
            rewards,
            values,
            dones,
            last_value,
            gamma=self.ppo_config.gamma,
            gae_lambda=self.ppo_config.gae_lambda,
        )
        rollout = build_rollout(transitions, returns, advantages)
        if self.device.type == "cuda":
            _allocated, reserved = self._cuda_memory_mb()
            if reserved > self.max_vram_mb * 0.90:
                torch.cuda.empty_cache()
                self._shrink_cuda_batch()
            try:
                torch.cuda.reset_peak_memory_stats(self.device)
            except Exception:
                pass
        try:
            metrics = self.trainer.update(rollout)
        except RuntimeError as exc:
            if self.device.type == "cuda" and self._is_cuda_oom(exc):
                self.logger.warning("cuda OOM during PPO update: %s", exc)
                torch.cuda.empty_cache()
                if self._shrink_cuda_batch():
                    try:
                        metrics = self.trainer.update(rollout)
                    except RuntimeError as retry_exc:
                        if self._is_cuda_oom(retry_exc):
                            self.logger.warning("retry after CUDA OOM failed; skipping this PPO update to keep training alive")
                            metrics = {"policy_loss": 0.0, "value_loss": 0.0, "entropy": 0.0, "loss": 0.0}
                        else:
                            raise
                else:
                    metrics = {"policy_loss": 0.0, "value_loss": 0.0, "entropy": 0.0, "loss": 0.0}
            elif self._is_autograd_inplace_error(exc):
                self.trainer.optimizer.zero_grad(set_to_none=True)
                if self.device.type == "cuda":
                    torch.cuda.empty_cache()
                self.logger.exception(
                    "PPO update discarded after an autograd in-place integrity error; "
                    "the serialized observation lock will prevent subsequent overlap"
                )
                return
            else:
                raise
        self.logger.info("ppo_update transitions=%d metrics=%s", len(transitions), {k: round(v, 6) for k, v in metrics.items()})
        if self.device.type == "cuda":
            allocated, reserved = self._cuda_memory_mb()
            try:
                peak_allocated = torch.cuda.max_memory_allocated(self.device) / (1024 * 1024)
                peak_reserved = torch.cuda.max_memory_reserved(self.device) / (1024 * 1024)
            except Exception:
                peak_allocated = allocated
                peak_reserved = reserved
            self.logger.info(
                "cuda memory after update | allocated=%.1fMB | reserved=%.1fMB | peak_allocated=%.1fMB | peak_reserved=%.1fMB | max_vram=%dMB",
                allocated,
                reserved,
                peak_allocated,
                peak_reserved,
                self.max_vram_mb,
            )
            if reserved > self.max_vram_mb:
                self.logger.warning("cuda reserved memory exceeded budget; emptying cache and shrinking minibatch")
                torch.cuda.empty_cache()
                self._shrink_cuda_batch()
        if self.cnt%10==0:
            self._save_checkpoint()
        self.cnt+=1
        if self.device.type == "cuda":
            torch.cuda.empty_cache()

    def _save_checkpoint(self) -> None:
        if self.checkpoint is None or self.model is None:
            return
        self.checkpoint.parent.mkdir(parents=True, exist_ok=True)
        checkpoint = {
            "checkpoint_format": "agentmin_actor_critic_v2",
            "checkpoint_kind": "ppo_online",
            "model_state_dict": {key: value.detach().cpu() for key, value in self.model.state_dict().items()},
            "model_config": asdict(self.model.cfg),
            **self.index_tables.state_dict(),
        }
        tmp_path = self.checkpoint.with_name(self.checkpoint.name + ".tmp")
        fallback_path = self.checkpoint.with_name(self.checkpoint.stem + ".fallback" + self.checkpoint.suffix)
        try:
            torch.save(checkpoint, tmp_path)
            try:
                tmp_path.replace(self.checkpoint)
                self.logger.info("checkpoint saved: %s", self.checkpoint)
            except OSError as exc:
                tmp_path.replace(fallback_path)
                self.logger.warning("checkpoint target was busy (%s); saved fallback checkpoint: %s", exc, fallback_path)
        except (RuntimeError, OSError) as exc:
            self.logger.warning("checkpoint save skipped: %s", exc)
            try:
                if tmp_path.exists():
                    tmp_path.unlink()
            except OSError:
                pass

    def _checkpoint_load_path(self) -> Path | None:
        if self.initial_checkpoint is not None and self.initial_checkpoint.exists():
            if self.prefer_initial_checkpoint:
                return self.initial_checkpoint
        if self.checkpoint is not None:
            candidates = [self.checkpoint, self.checkpoint.with_name(self.checkpoint.stem + ".fallback" + self.checkpoint.suffix)]
            existing = [path for path in candidates if path.exists()]
            if existing:
                return max(existing, key=lambda path: path.stat().st_mtime)
        if self.initial_checkpoint is not None and self.initial_checkpoint.exists():
            return self.initial_checkpoint
        return None


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
                response = trainer.handle_observation(payload)
                self.wfile.write((json.dumps(response) + "\n").encode("utf-8"))
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
    parser.add_argument("--update-interval", type=int, default=1)
    parser.add_argument("--checkpoint", type=Path, default=LOG_DIR / "agent_min_latest.pt")
    parser.add_argument("--initial-checkpoint", type=Path, default=None)
    parser.add_argument("--prefer-initial-checkpoint", action="store_true")
    parser.add_argument("--ppo-epochs", type=int, default=1)
    parser.add_argument("--ppo-minibatch-size", type=int, default=2)
    parser.add_argument("--ppo-lr", type=float, default=5e-5)
    parser.add_argument("--ppo-gamma", type=float, default=0.99)
    parser.add_argument("--ppo-gae-lambda", type=float, default=0.90)
    parser.add_argument("--ppo-entropy-coef", type=float, default=0.045)
    parser.add_argument("--ppo-clip-range", type=float, default=0.12)
    parser.add_argument("--ppo-value-coef", type=float, default=0.40)
    parser.add_argument("--ppo-max-grad-norm", type=float, default=0.5)
    parser.add_argument("--ppo-weight-decay", type=float, default=0.002)
    parser.add_argument("--ppo-target-kl", type=float, default=0.020)
    parser.add_argument("--max-vram-mb", type=int, default=7168)
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
        target_kl=args.ppo_target_kl,
        use_amp=not args.disable_amp,
    )
    trainer = OnlineTrainer(
        args.device,
        args.update_interval,
        args.checkpoint,
        args.initial_checkpoint,
        args.prefer_initial_checkpoint,
        ppo_config,
        logger,
        args.max_vram_mb,
    )
    with ThreadingBridgeServer((args.host, args.port), BridgeHandler) as server:
        server.trainer = trainer  # type: ignore[attr-defined]
        server.logger = logger  # type: ignore[attr-defined]
        logger.info("AgentMin bridge server listening on %s:%d", args.host, args.port)
        server.serve_forever()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
