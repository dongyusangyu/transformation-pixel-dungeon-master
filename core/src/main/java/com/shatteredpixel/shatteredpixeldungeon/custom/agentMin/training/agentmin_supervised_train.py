from __future__ import annotations

import argparse
import importlib
import json
import math
import random
import sys
from collections import Counter
from contextlib import nullcontext
from dataclasses import asdict, dataclass
from functools import partial
from pathlib import Path
from typing import Any

import torch
import torch.nn.functional as F
from torch import Tensor
from torch.nn.utils import clip_grad_norm_
from torch.utils.data import DataLoader, Dataset, WeightedRandomSampler


DEFAULT_DATASET_DIR = Path(r"D:\桌面\地牢更新\数据集")
DEFAULT_MODEL_FILE = Path(
    r"D:\STUDY\Dungeon\transformation-pixel-dungeon-master\core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\custom\agentMin\training\agent_min_model.py"
)
DEFAULT_INIT_CHECKPOINT = Path(
    r"D:\STUDY\Dungeon\transformation-pixel-dungeon-master\core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\custom\agentMin\training\logs\agent_min_real_game_8765.pt"
)
DEFAULT_SAVE_DIR = Path(r"D:\桌面\地牢更新\强化学习\py\checkpoints")

SKILL_NAME_TO_ID = {
    "EXPLORE": 0,
    "COMBAT": 1,
    "EAT": 2,
    "UNLOCK": 3,
    "PICKUP": 4,
    "DESCEND": 5,
    "ITEM": 6,
    "ALCHEMY": 7,
    "TALENT": 8,
    "DROP": 9,
}

FLOAT_KEYS = (
    "level_tensor",
    "explored_global_matrix",
    "agent_visited_matrix",
    "hero_vector",
    "inventory_matrix",
    "inventory_summary_vector",
    "option_vector",
    "mob_matrix",
    "history_matrix",
    "action_matrix",
    "action_mask",
    "monitor_item_mask",
    "monitor_cell_mask",
    "monitor_option_mask",
    "skill_mask",
    "action_skill_mask",
)


@dataclass
class TensorShapeSummary:
    level_channels: int
    level_height: int
    level_width: int
    global_height: int
    global_width: int
    hero_dim: int
    inventory_rows: int
    inventory_features: int
    inventory_summary_dim: int
    option_dim: int
    mob_rows: int
    mob_features: int
    history_rows: int
    history_features: int
    action_rows: int
    action_features: int
    monitor_cell_dim: int
    monitor_option_dim: int
    skill_count: int


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="AgentMin supervised imitation training from recorded player samples."
    )
    parser.add_argument("--dataset-dir", type=Path, default=DEFAULT_DATASET_DIR)
    parser.add_argument("--model-file", type=Path, default=DEFAULT_MODEL_FILE)
    parser.add_argument("--init-checkpoint", type=Path, default=None)
    parser.add_argument("--save-checkpoint", type=Path, default=None)
    parser.add_argument("--epochs", type=int, default=10)
    parser.add_argument("--batch-size", type=int, default=16)
    parser.add_argument("--grad-accum-steps", type=int, default=2)
    parser.add_argument("--lr", type=float, default=1e-4)
    parser.add_argument("--weight-decay", type=float, default=1e-4)
    parser.add_argument("--dropout", type=float, default=0.08)
    parser.add_argument("--hidden-dim", type=int, default=192)
    parser.add_argument("--attention-heads", type=int, default=8)
    parser.add_argument("--priority-logit-scale", type=float, default=1.5)
    parser.add_argument("--grad-clip", type=float, default=1.0)
    parser.add_argument("--label-smoothing", type=float, default=0.0)
    parser.add_argument("--action-loss-weight", type=float, default=1.0)
    parser.add_argument("--skill-loss-weight", type=float, default=0.45)
    parser.add_argument("--head-loss-weight", type=float, default=0.20)
    parser.add_argument("--device", type=str, default="cuda")
    parser.add_argument("--num-workers", type=int, default=0)
    parser.add_argument("--pin-memory", action="store_true")
    parser.add_argument("--amp", action="store_true")
    parser.add_argument("--precision", type=str, default="auto", choices=["auto", "float16", "bfloat16"])
    parser.add_argument("--seed", type=int, default=3407)
    parser.add_argument("--max-samples", type=int, default=0)
    parser.add_argument("--log-interval", type=int, default=20)
    parser.add_argument("--shuffle", action="store_true")
    parser.add_argument("--no-shuffle", action="store_true")
    parser.add_argument("--action-oversample-ratio", type=float, default=0.5)
    parser.add_argument("--disable-action-oversample", action="store_true")
    parser.add_argument("--use-skill-balanced-sampler", action="store_true")
    parser.add_argument("--cpu-float16", action="store_true")
    parser.add_argument("--initial-eval", action="store_true")
    return parser.parse_args()


def set_seed(seed: int) -> None:
    random.seed(seed)
    torch.manual_seed(seed)
    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(seed)


def resolve_device(device_name: str) -> torch.device:
    name = device_name.lower()
    if name == "auto":
        return torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
    if name == "cuda":
        if not torch.cuda.is_available():
            raise RuntimeError("CUDA requested but is not available in the current PyTorch environment.")
        return torch.device("cuda:0")
    if name.startswith("cuda") and not torch.cuda.is_available():
        raise RuntimeError("CUDA requested but is not available in the current PyTorch environment.")
    return torch.device(device_name)


def configure_runtime(device: torch.device) -> None:
    if device.type == "cuda":
        torch.backends.cudnn.benchmark = True
        torch.backends.cuda.matmul.allow_tf32 = True
        torch.backends.cudnn.allow_tf32 = True
    else:
        torch.set_num_threads(max(1, min(8, torch.get_num_threads() or 1)))


def resolved_amp_dtype(args: argparse.Namespace, device: torch.device) -> torch.dtype | None:
    if device.type != "cuda":
        return None
    if args.precision == "bfloat16":
        return torch.bfloat16 if torch.cuda.is_bf16_supported() else None
    if args.precision == "float16":
        return torch.float16
    return torch.bfloat16 if torch.cuda.is_bf16_supported() else torch.float16


def amp_enabled(args: argparse.Namespace, device: torch.device) -> bool:
    return device.type == "cuda" and args.amp and resolved_amp_dtype(args, device) is not None


def autocast_context(args: argparse.Namespace, device: torch.device):
    if not amp_enabled(args, device):
        return nullcontext()
    return torch.autocast(device_type="cuda", dtype=resolved_amp_dtype(args, device))


def describe_device(device: torch.device) -> str:
    if device.type != "cuda":
        return str(device)
    index = 0 if device.index is None else device.index
    return f"cuda:{index} ({torch.cuda.get_device_name(index)})"


def load_agentmin_model_module(model_file: Path):
    if not model_file.exists():
        raise FileNotFoundError(f"Model file not found: {model_file}")
    model_dir = str(model_file.resolve().parent)
    if model_dir not in sys.path:
        sys.path.insert(0, model_dir)
    return importlib.import_module("agent_min_model")


def load_index_table_class(model_file: Path):
    model_dir = str(model_file.resolve().parent)
    if model_dir not in sys.path:
        sys.path.insert(0, model_dir)
    module = importlib.import_module("agent_min_index_tables")
    return module.AgentMinIndexTables


class IndexedJsonlDataset(Dataset):
    def __init__(self, dataset_dir: Path, max_samples: int = 0):
        self.dataset_dir = dataset_dir
        self.files = sorted(dataset_dir.rglob("*.jsonl"))
        if not self.files:
            raise FileNotFoundError(f"No jsonl files were found under: {dataset_dir}")
        self.index: list[tuple[Path, int]] = []
        self.skill_targets: list[int] = []
        self.action_targets: list[int] = []
        self.skipped_lines = 0
        for path in self.files:
            with path.open("r", encoding="utf-8") as handle:
                while True:
                    offset = handle.tell()
                    line = handle.readline()
                    if not line:
                        break
                    if not line.strip():
                        self.skipped_lines += 1
                        continue
                    payload = json.loads(line)
                    skill_id = derive_skill_target_from_payload(payload)
                    if skill_id < 0:
                        self.skipped_lines += 1
                        continue
                    try:
                        action_id = int(payload.get("action_id", -1))
                    except (TypeError, ValueError):
                        self.skipped_lines += 1
                        continue
                    if action_id < 0:
                        self.skipped_lines += 1
                        continue
                    self.index.append((path, offset))
                    self.skill_targets.append(skill_id)
                    self.action_targets.append(action_id)
                    if max_samples > 0 and len(self.index) >= max_samples:
                        return

    def __len__(self) -> int:
        return len(self.index)

    def __getitem__(self, idx: int) -> dict[str, Any]:
        path, offset = self.index[idx]
        with path.open("r", encoding="utf-8") as handle:
            handle.seek(offset)
            payload = json.loads(handle.readline())
        payload["_source_file"] = str(path)
        payload["_source_index"] = idx
        payload["_skill_target"] = self.skill_targets[idx]
        payload["_action_target"] = self.action_targets[idx]
        return payload

    def peek(self, idx: int = 0) -> dict[str, Any]:
        return self[idx]

    def oversample_action_minority(self, target_ratio: float = 0.5, seed: int = 0) -> dict[str, Any]:
        if not 0 < target_ratio <= 1:
            raise ValueError("--action-oversample-ratio must be in the range (0, 1].")
        before = Counter(self.action_targets)
        if not before:
            return {
                "enabled": True,
                "target_ratio": target_ratio,
                "target_count": 0,
                "added": 0,
                "before_total": 0,
                "after_total": 0,
                "before": before,
                "after": before,
            }

        target_count = max(1, math.ceil(max(before.values()) * target_ratio))
        by_action: dict[int, list[int]] = {}
        for idx, action_id in enumerate(self.action_targets):
            by_action.setdefault(action_id, []).append(idx)

        rng = random.Random(seed)
        additions: list[tuple[tuple[Path, int], int, int]] = []
        for action_id, indices in sorted(by_action.items()):
            missing = target_count - len(indices)
            if missing <= 0:
                continue
            for _ in range(missing):
                source_idx = rng.choice(indices)
                additions.append((self.index[source_idx], self.skill_targets[source_idx], self.action_targets[source_idx]))
        rng.shuffle(additions)

        for index_entry, skill_target, action_target in additions:
            self.index.append(index_entry)
            self.skill_targets.append(skill_target)
            self.action_targets.append(action_target)

        after = Counter(self.action_targets)
        return {
            "enabled": True,
            "target_ratio": target_ratio,
            "target_count": target_count,
            "added": len(additions),
            "before_total": sum(before.values()),
            "after_total": sum(after.values()),
            "before": before,
            "after": after,
        }


def derive_skill_target_from_payload(sample: dict[str, Any]) -> int:
    name = sample.get("action_skill")
    if isinstance(name, str):
        value = SKILL_NAME_TO_ID.get(name.strip().upper())
        if value is not None:
            return value
    forced_skill = int(sample.get("forced_skill", -1))
    skill_mask = sample.get("skill_mask")
    action_skill_mask = sample.get("action_skill_mask")
    action_id = int(sample.get("action_id", -1))
    if (
        forced_skill >= 0
        and isinstance(skill_mask, list)
        and forced_skill < len(skill_mask)
        and float(skill_mask[forced_skill]) > 0
    ):
        return forced_skill
    if isinstance(action_skill_mask, list) and action_id >= 0:
        for skill_id, row in enumerate(action_skill_mask):
            if action_id < len(row) and float(row[action_id]) > 0:
                return skill_id
    return -1


def infer_shapes(sample: dict[str, Any]) -> TensorShapeSummary:
    return TensorShapeSummary(
        level_channels=len(sample["level_tensor"]),
        level_height=len(sample["level_tensor"][0]),
        level_width=len(sample["level_tensor"][0][0]),
        global_height=len(sample["explored_global_matrix"]),
        global_width=len(sample["explored_global_matrix"][0]),
        hero_dim=len(sample["hero_vector"]),
        inventory_rows=len(sample["inventory_matrix"]),
        inventory_features=len(sample["inventory_matrix"][0]),
        inventory_summary_dim=len(sample.get("inventory_summary_vector", [])),
        option_dim=len(sample.get("option_vector", [])),
        mob_rows=len(sample["mob_matrix"]),
        mob_features=len(sample["mob_matrix"][0]),
        history_rows=len(sample["history_matrix"]),
        history_features=len(sample["history_matrix"][0]),
        action_rows=len(sample["action_matrix"]),
        action_features=len(sample["action_matrix"][0]),
        monitor_cell_dim=len(sample.get("monitor_cell_mask", [])),
        monitor_option_dim=len(sample.get("monitor_option_mask", [])),
        skill_count=len(sample.get("skill_mask", [])),
    )


def validate_sample(sample: dict[str, Any], shapes: TensorShapeSummary) -> str | None:
    action_id = int(sample.get("action_id", -1))
    if action_id < 0 or action_id >= shapes.action_rows:
        return f"action_id out of range: {action_id}"
    action_mask = sample.get("action_mask")
    if not isinstance(action_mask, list) or len(action_mask) != shapes.action_rows:
        return "action_mask has invalid shape"
    if float(action_mask[action_id]) <= 0:
        return f"action_id={action_id} is invalid under action_mask"
    skill_mask = sample.get("skill_mask")
    if not isinstance(skill_mask, list) or len(skill_mask) != shapes.skill_count:
        return "skill_mask has invalid shape"
    if sum(float(v) > 0 for v in skill_mask) <= 0:
        return "skill_mask has no valid skills"
    action_skill_mask = sample.get("action_skill_mask")
    if (
        not isinstance(action_skill_mask, list)
        or len(action_skill_mask) != shapes.skill_count
        or any(len(row) != shapes.action_rows for row in action_skill_mask)
    ):
        return "action_skill_mask has invalid shape"
    skill_target = derive_skill_target_from_payload(sample)
    if skill_target < 0 or skill_target >= shapes.skill_count:
        return f"could not derive a valid skill label: {skill_target}"
    if float(skill_mask[skill_target]) <= 0 or float(action_skill_mask[skill_target][action_id]) <= 0:
        return f"invalid skill/action route: skill={skill_target}, action={action_id}"
    return None


def zeros(length: int) -> list[float]:
    return [0.0] * max(0, length)


def tensorize_sample(
    sample: dict[str, Any],
    shapes: TensorShapeSummary,
    float_dtype: torch.dtype,
    index_tables,
) -> dict[str, Tensor]:
    inventory_matrix = torch.tensor(sample["inventory_matrix"], dtype=float_dtype)
    mob_matrix = torch.tensor(sample["mob_matrix"], dtype=float_dtype)
    # This is deliberately identical to the online bridge: the same class name
    # must resolve to the same learned embedding row during BC and PPO.
    index_tables.apply(inventory_matrix, sample.get("item_keys"), sample.get("modifier_keys"))
    index_tables.apply_mobs(mob_matrix, sample.get("mob_keys"))
    return {
        "level_tensor": torch.tensor(sample["level_tensor"], dtype=float_dtype),
        "explored_global_matrix": torch.tensor(sample["explored_global_matrix"], dtype=float_dtype),
        "agent_visited_matrix": torch.tensor(sample["agent_visited_matrix"], dtype=float_dtype),
        "hero_vector": torch.tensor(sample["hero_vector"], dtype=float_dtype),
        "inventory_matrix": inventory_matrix,
        "inventory_summary_vector": torch.tensor(
            sample.get("inventory_summary_vector", zeros(shapes.inventory_summary_dim)), dtype=float_dtype
        ),
        "option_vector": torch.tensor(sample.get("option_vector", zeros(shapes.option_dim)), dtype=float_dtype),
        "mob_matrix": mob_matrix,
        "history_matrix": torch.tensor(sample["history_matrix"], dtype=float_dtype),
        "action_matrix": torch.tensor(sample["action_matrix"], dtype=float_dtype),
        "action_mask": torch.tensor(sample["action_mask"], dtype=float_dtype),
        "monitor_item_mask": torch.tensor(sample.get("monitor_item_mask", zeros(shapes.inventory_rows)), dtype=float_dtype),
        "monitor_cell_mask": torch.tensor(sample.get("monitor_cell_mask", zeros(shapes.monitor_cell_dim)), dtype=float_dtype),
        "monitor_option_mask": torch.tensor(
            sample.get("monitor_option_mask", zeros(shapes.monitor_option_dim)), dtype=float_dtype
        ),
        "skill_mask": torch.tensor(sample["skill_mask"], dtype=float_dtype),
        "action_skill_mask": torch.tensor(sample["action_skill_mask"], dtype=float_dtype),
        "forced_skill": torch.tensor(int(sample.get("forced_skill", -1)), dtype=torch.long),
        "action_id": torch.tensor(int(sample["action_id"]), dtype=torch.long),
        "skill_target": torch.tensor(int(sample["_skill_target"]), dtype=torch.long),
    }


def collate_batch(
    samples: list[dict[str, Any]],
    shapes: TensorShapeSummary,
    float_dtype: torch.dtype,
    index_tables,
) -> dict[str, Tensor]:
    tensor_samples = [tensorize_sample(sample, shapes, float_dtype, index_tables) for sample in samples]
    return {key: torch.stack([item[key] for item in tensor_samples], dim=0) for key in tensor_samples[0].keys()}


def move_batch_to_device(batch: dict[str, Tensor], device: torch.device) -> dict[str, Tensor]:
    moved: dict[str, Tensor] = {}
    for key, value in batch.items():
        tensor = value.to(device, non_blocking=device.type == "cuda")
        if key in FLOAT_KEYS and tensor.dtype != torch.float32:
            tensor = tensor.float()
        moved[key] = tensor
    return moved


def build_model(module, shapes: TensorShapeSummary, args: argparse.Namespace):
    cfg = module.AgentMinModelConfig(
        level_channels=shapes.level_channels + 1,
        hero_dim=shapes.hero_dim,
        inventory_rows=shapes.inventory_rows,
        inventory_features=shapes.inventory_features,
        inventory_summary_dim=shapes.inventory_summary_dim,
        option_dim=shapes.option_dim,
        mob_rows=shapes.mob_rows,
        mob_features=shapes.mob_features,
        history_rows=shapes.history_rows,
        history_features=shapes.history_features,
        action_rows=shapes.action_rows,
        action_features=shapes.action_features,
        skill_count=shapes.skill_count,
        hidden_dim=args.hidden_dim,
        attention_heads=args.attention_heads,
        dropout=args.dropout,
        priority_logit_scale=args.priority_logit_scale,
    )
    model = module.AgentMinActorCritic(cfg)
    return model, cfg


def load_checkpoint_payload(checkpoint_path: Path) -> dict[str, Any]:
    payload = torch.load(checkpoint_path, map_location="cpu", weights_only=False)
    if not isinstance(payload, dict):
        raise RuntimeError(f"Checkpoint payload is not a dictionary: {checkpoint_path}")
    return payload


def load_checkpoint_flexible(
    model: torch.nn.Module,
    checkpoint_path: Path,
    payload: dict[str, Any] | None = None,
) -> tuple[int, int]:
    payload = payload if payload is not None else load_checkpoint_payload(checkpoint_path)
    state_dict = payload.get("model_state_dict", payload.get("state_dict", payload))
    if not isinstance(state_dict, dict):
        raise RuntimeError(f"Could not parse state_dict from checkpoint: {checkpoint_path}")
    current = model.state_dict()
    matched: dict[str, Tensor] = {}
    skipped = 0
    for key, value in state_dict.items():
        if key in current and tuple(current[key].shape) == tuple(value.shape):
            matched[key] = value
        else:
            skipped += 1
    current.update(matched)
    model.load_state_dict(current)
    return len(matched), skipped


def populate_index_tables(dataset: IndexedJsonlDataset, index_tables) -> None:
    """Reserve deterministic embedding ids before shuffled workers see samples."""
    for idx in range(len(dataset)):
        sample = dataset.peek(idx)
        inventory = torch.tensor(sample["inventory_matrix"], dtype=torch.float32)
        mobs = torch.tensor(sample["mob_matrix"], dtype=torch.float32)
        index_tables.apply(inventory, sample.get("item_keys"), sample.get("modifier_keys"))
        index_tables.apply_mobs(mobs, sample.get("mob_keys"))


def forward_components(model, batch: dict[str, Tensor]) -> tuple[Tensor, dict[str, Tensor]]:
    state = model.encode_state(
        level_tensor=batch["level_tensor"],
        explored_global_matrix=batch["explored_global_matrix"],
        agent_visited_matrix=batch["agent_visited_matrix"],
        hero_vector=batch["hero_vector"],
        inventory_matrix=batch["inventory_matrix"],
        inventory_summary_vector=batch["inventory_summary_vector"],
        option_vector=batch["option_vector"],
        mob_matrix=batch["mob_matrix"],
        history_matrix=batch["history_matrix"],
    )
    action_tokens = model.encode_action_tokens(state, batch["action_matrix"], batch.get("action_mask"))
    _base, talent_slot_features, talent_slot_embeddings, talent_slot_present = (
        model.talent_embedding_encoder.slot_embeddings(batch["hero_vector"])
    )
    inventory_features = model.inventory_embedding_encoder(batch["inventory_matrix"])
    mob_features = model.mob_embedding_encoder(batch["mob_matrix"])
    components = model.hierarchical_policy.components(
        state=state,
        action_tokens=action_tokens,
        action_matrix=batch["action_matrix"],
        action_mask=batch.get("action_mask"),
        skill_mask=batch.get("skill_mask"),
        action_skill_mask=batch.get("action_skill_mask"),
        inventory_features=inventory_features,
        mob_matrix=mob_features,
        talent_slot_features=talent_slot_features,
        talent_slot_embeddings=talent_slot_embeddings,
        talent_slot_present=talent_slot_present,
        forced_skill=batch.get("forced_skill"),
    )
    return state, components


def choose_head_targets(
    batch: dict[str, Tensor],
    components: dict[str, Tensor],
    skill_targets: Tensor,
    item_skill_id: int,
) -> tuple[Tensor, Tensor]:
    batch_index = torch.arange(batch["action_id"].shape[0], device=batch["action_id"].device)
    actions = batch["action_id"]
    route_mask = components["route_mask"][batch_index, skill_targets, :, actions]
    item_preferred_heads = components["item_preferred_heads"][batch_index, actions]
    head_targets = torch.full_like(skill_targets, -1)
    valid = route_mask.any(dim=-1)
    if valid.any():
        head_targets[valid] = route_mask[valid].float().argmax(dim=-1)
    item_active = skill_targets == item_skill_id
    head_targets[item_active] = item_preferred_heads[item_active]
    head_loss_mask = valid & (~item_active)
    return head_targets, head_loss_mask


def supervised_loss(model, batch: dict[str, Tensor], args: argparse.Namespace) -> tuple[Tensor, dict[str, float]]:
    _state, components = forward_components(model, batch)
    action_loss = F.cross_entropy(components["logits"], batch["action_id"], label_smoothing=args.label_smoothing)
    skill_loss = F.cross_entropy(components["skill_logits"], batch["skill_target"], label_smoothing=args.label_smoothing)
    item_skill_id = getattr(sys.modules[model.__class__.__module__], "SKILL_ITEM", 6)
    head_targets, head_loss_mask = choose_head_targets(batch, components, batch["skill_target"], item_skill_id)
    if head_loss_mask.any():
        batch_index = torch.arange(batch["action_id"].shape[0], device=batch["action_id"].device)
        chosen_head_logits = components["head_logits"][batch_index, batch["skill_target"]]
        head_loss = F.cross_entropy(
            chosen_head_logits[head_loss_mask],
            head_targets[head_loss_mask],
            label_smoothing=args.label_smoothing,
        )
    else:
        head_loss = action_loss.new_zeros(())
    total_loss = (
        args.action_loss_weight * action_loss
        + args.skill_loss_weight * skill_loss
        + args.head_loss_weight * head_loss
    )
    action_pred = components["logits"].argmax(dim=-1)
    skill_pred = components["skill_logits"].argmax(dim=-1)
    chosen_head_pred = components["head_logits"][
        torch.arange(batch["action_id"].shape[0], device=batch["action_id"].device),
        batch["skill_target"],
    ].argmax(dim=-1)
    head_correct = ((chosen_head_pred == head_targets) & head_loss_mask).sum().item() if head_loss_mask.any() else 0
    head_count = int(head_loss_mask.sum().item())
    metrics = {
        "loss": float(total_loss.detach().item()),
        "action_loss": float(action_loss.detach().item()),
        "skill_loss": float(skill_loss.detach().item()),
        "head_loss": float(head_loss.detach().item()) if head_loss_mask.any() else 0.0,
        "action_correct": int((action_pred == batch["action_id"]).sum().item()),
        "skill_correct": int((skill_pred == batch["skill_target"]).sum().item()),
        "head_correct": int(head_correct),
        "head_count": head_count,
        "count": int(batch["action_id"].shape[0]),
    }
    return total_loss, metrics


@torch.no_grad()
def evaluate_model(model: torch.nn.Module, data_loader: DataLoader, device: torch.device, args: argparse.Namespace) -> dict[str, float]:
    model.eval()
    totals = Counter()
    for batch in data_loader:
        batch = move_batch_to_device(batch, device)
        with autocast_context(args, device):
            _loss, metrics = supervised_loss(model, batch, args)
        totals.update(metrics)
    sample_count = max(1, totals["count"])
    head_count = max(1, totals["head_count"])
    return {
        "loss": totals["loss"] / sample_count,
        "action_loss": totals["action_loss"] / sample_count,
        "skill_loss": totals["skill_loss"] / sample_count,
        "head_loss": totals["head_loss"] / head_count if totals["head_count"] > 0 else 0.0,
        "action_acc": totals["action_correct"] / sample_count,
        "skill_acc": totals["skill_correct"] / sample_count,
        "head_acc": totals["head_correct"] / head_count if totals["head_count"] > 0 else 0.0,
        "count": float(totals["count"]),
    }


def format_metrics(prefix: str, metrics: dict[str, float]) -> str:
    return (
        f"{prefix} loss={metrics['loss']:.6f} action_loss={metrics['action_loss']:.6f} "
        f"skill_loss={metrics['skill_loss']:.6f} head_loss={metrics['head_loss']:.6f} "
        f"action_acc={metrics['action_acc']:.2%} skill_acc={metrics['skill_acc']:.2%} "
        f"head_acc={metrics['head_acc']:.2%} samples={int(metrics['count'])}"
    )


def train_one_epoch(
    model: torch.nn.Module,
    optimizer: torch.optim.Optimizer,
    data_loader: DataLoader,
    device: torch.device,
    epoch: int,
    args: argparse.Namespace,
    scaler: torch.amp.GradScaler | None,
) -> dict[str, float]:
    model.train()
    totals = Counter()
    optimizer.zero_grad(set_to_none=True)
    for batch_idx, batch in enumerate(data_loader, start=1):
        batch = move_batch_to_device(batch, device)
        with autocast_context(args, device):
            loss, metrics = supervised_loss(model, batch, args)
            scaled_loss = loss / max(1, args.grad_accum_steps)
        if scaler is not None:
            scaler.scale(scaled_loss).backward()
        else:
            scaled_loss.backward()
        should_step = (batch_idx % max(1, args.grad_accum_steps) == 0) or (batch_idx == len(data_loader))
        if should_step:
            if scaler is not None:
                if args.grad_clip > 0:
                    scaler.unscale_(optimizer)
                    clip_grad_norm_(model.parameters(), args.grad_clip)
                scaler.step(optimizer)
                scaler.update()
            else:
                if args.grad_clip > 0:
                    clip_grad_norm_(model.parameters(), args.grad_clip)
                optimizer.step()
            optimizer.zero_grad(set_to_none=True)
        totals.update(metrics)
        if args.log_interval > 0 and batch_idx % args.log_interval == 0:
            sample_count = max(1, totals["count"])
            print(
                f"[train] epoch={epoch:03d} batch={batch_idx:05d}/{len(data_loader):05d} "
                f"loss={totals['loss'] / sample_count:.6f} "
                f"action_acc={totals['action_correct'] / sample_count:.2%} "
                f"skill_acc={totals['skill_correct'] / sample_count:.2%}",
                flush=True,
            )
    sample_count = max(1, totals["count"])
    head_count = max(1, totals["head_count"])
    return {
        "loss": totals["loss"] / sample_count,
        "action_loss": totals["action_loss"] / sample_count,
        "skill_loss": totals["skill_loss"] / sample_count,
        "head_loss": totals["head_loss"] / head_count if totals["head_count"] > 0 else 0.0,
        "action_acc": totals["action_correct"] / sample_count,
        "skill_acc": totals["skill_correct"] / sample_count,
        "head_acc": totals["head_correct"] / head_count if totals["head_count"] > 0 else 0.0,
        "count": float(totals["count"]),
    }


def default_save_checkpoint_path(dataset_dir: Path) -> Path:
    DEFAULT_SAVE_DIR.mkdir(parents=True, exist_ok=True)
    stem = dataset_dir.name if dataset_dir.name else "dataset"
    return DEFAULT_SAVE_DIR / f"agentmin_supervised_{stem}.pt"


def save_checkpoint(
    model: torch.nn.Module,
    save_path: Path,
    args: argparse.Namespace,
    cfg,
    shapes: TensorShapeSummary,
    best_metrics: dict[str, float] | None,
    index_tables,
) -> None:
    save_path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        # Keep this schema identical to the realtime PPO bridge.  A BC model is
        # therefore a valid PPO initialization checkpoint without conversion.
        "checkpoint_format": "agentmin_actor_critic_v2",
        "checkpoint_kind": "supervised_imitation",
        "model_state_dict": {key: value.detach().cpu() for key, value in model.state_dict().items()},
        "model_config": asdict(cfg) if hasattr(cfg, "__dataclass_fields__") else str(cfg),
        "observation_shapes": asdict(shapes),
        "training_args": vars(args),
        "best_metrics": best_metrics or {},
        **index_tables.state_dict(),
    }
    torch.save(payload, save_path)


def format_counter_summary(counter: Counter, limit: int = 24) -> str:
    if not counter:
        return "<empty>"
    items = sorted(counter.items(), key=lambda item: (-item[1], item[0]))
    summary = ", ".join(f"{key}:{value}" for key, value in items[:limit])
    if len(items) > limit:
        summary += f", ...(+{len(items) - limit} classes)"
    return summary


def summarize_dataset(dataset: IndexedJsonlDataset, shapes: TensorShapeSummary, title: str = "Dataset Summary") -> None:
    counter = Counter(dataset.skill_targets)
    skill_summary = ", ".join(f"{skill}:{counter.get(skill, 0)}" for skill in range(shapes.skill_count))
    action_counter = Counter(dataset.action_targets)
    print(f"=== {title} ===", flush=True)
    print(f"Directory: {dataset.dataset_dir}", flush=True)
    print(f"Files: {len(dataset.files)}", flush=True)
    print(f"Samples: {len(dataset)}", flush=True)
    print(f"Skill distribution: {skill_summary}", flush=True)
    print(f"Action distribution: {format_counter_summary(action_counter)}", flush=True)
    print(
        "Shapes: "
        f"level={shapes.level_channels}x{shapes.level_height}x{shapes.level_width}, "
        f"global={shapes.global_height}x{shapes.global_width}, hero={shapes.hero_dim}, "
        f"inventory={shapes.inventory_rows}x{shapes.inventory_features}, "
        f"inv_summary={shapes.inventory_summary_dim}, option={shapes.option_dim}, "
        f"mob={shapes.mob_rows}x{shapes.mob_features}, history={shapes.history_rows}x{shapes.history_features}, "
        f"action={shapes.action_rows}x{shapes.action_features}, skills={shapes.skill_count}",
        flush=True,
    )


def build_loader(
    dataset: IndexedJsonlDataset,
    batch_size: int,
    shuffle: bool,
    num_workers: int,
    device: torch.device,
    pin_memory: bool,
    shapes: TensorShapeSummary,
    float_dtype: torch.dtype,
    index_tables,
    sampler=None,
) -> DataLoader:
    return DataLoader(
        dataset,
        batch_size=batch_size,
        shuffle=shuffle if sampler is None else False,
        sampler=sampler,
        num_workers=num_workers,
        collate_fn=partial(
            collate_batch,
            shapes=shapes,
            float_dtype=float_dtype,
            index_tables=index_tables,
        ),
        pin_memory=device.type == "cuda" and pin_memory,
        persistent_workers=num_workers > 0,
    )


def build_skill_balanced_sampler(dataset: IndexedJsonlDataset) -> WeightedRandomSampler:
    counts = Counter(dataset.skill_targets)
    weights = [1.0 / math.sqrt(max(1, counts[skill])) for skill in dataset.skill_targets]
    return WeightedRandomSampler(weights=weights, num_samples=len(weights), replacement=True)


def main() -> None:
    args = parse_args()
    if args.shuffle and args.no_shuffle:
        raise ValueError("--shuffle and --no-shuffle cannot be used together.")
    shuffle = False if args.no_shuffle else True
    if args.grad_accum_steps < 1:
        raise ValueError("--grad-accum-steps must be >= 1")
    set_seed(args.seed)
    device = resolve_device(args.device)
    configure_runtime(device)

    dataset = IndexedJsonlDataset(args.dataset_dir, max_samples=args.max_samples)
    if len(dataset) == 0:
        raise RuntimeError("Dataset is empty, nothing to train.")
    shapes = infer_shapes(dataset.peek(0))
    summarize_dataset(dataset, shapes, title="Dataset Summary Before Action Oversampling")
    if args.disable_action_oversample:
        print("Action minority oversampling: disabled", flush=True)
    else:
        oversample_report = dataset.oversample_action_minority(args.action_oversample_ratio, seed=args.seed)
        print(
            "Action minority oversampling: "
            f"target_ratio={oversample_report['target_ratio']:.3f} "
            f"target_count={oversample_report['target_count']} "
            f"added={oversample_report['added']} "
            f"samples={oversample_report['before_total']}->{oversample_report['after_total']}",
            flush=True,
        )
        if oversample_report["added"] > 0:
            summarize_dataset(dataset, shapes, title="Dataset Summary After Action Oversampling")

    bad_samples: list[str] = []
    for idx in range(min(len(dataset), 256)):
        sample = dataset.peek(idx)
        reason = validate_sample(sample, shapes)
        if reason is not None:
            bad_samples.append(f"idx={idx} file={sample['_source_file']} reason={reason}")
    if bad_samples:
        for item in bad_samples[:20]:
            print(item, flush=True)
        raise RuntimeError(f"Found {len(bad_samples)} invalid samples in the first validation pass.")

    module = load_agentmin_model_module(args.model_file)
    index_table_class = load_index_table_class(args.model_file)
    if args.init_checkpoint is None and DEFAULT_INIT_CHECKPOINT.exists():
        args.init_checkpoint = DEFAULT_INIT_CHECKPOINT
    initial_payload: dict[str, Any] | None = None
    if args.init_checkpoint is not None and args.init_checkpoint.exists():
        initial_payload = load_checkpoint_payload(args.init_checkpoint)
    index_tables = index_table_class()
    if initial_payload is not None:
        index_tables.load_checkpoint(initial_payload)
    populate_index_tables(dataset, index_tables)
    print(
        "Embedding index tables: "
        f"items={len(index_tables.item_index)} modifiers={len(index_tables.modifier_index)} mobs={len(index_tables.mob_index)}",
        flush=True,
    )

    model, cfg = build_model(module, shapes, args)
    model = model.to(device)
    print("=== Model Config ===", flush=True)
    print(cfg, flush=True)
    print(f"Device: {describe_device(device)}", flush=True)

    if args.init_checkpoint is not None and initial_payload is not None:
        matched, skipped = load_checkpoint_flexible(model, args.init_checkpoint, initial_payload)
        print(f"Loaded initial checkpoint: {args.init_checkpoint} | matched={matched} skipped={skipped}", flush=True)

    float_dtype = torch.float16 if args.cpu_float16 else torch.float32
    sampler = build_skill_balanced_sampler(dataset) if args.use_skill_balanced_sampler else None
    train_loader = build_loader(
        dataset,
        args.batch_size,
        shuffle,
        args.num_workers,
        device,
        args.pin_memory,
        shapes,
        float_dtype,
        index_tables,
        sampler=sampler,
    )
    eval_loader = build_loader(
        dataset,
        args.batch_size,
        False,
        args.num_workers,
        device,
        args.pin_memory,
        shapes,
        float_dtype,
        index_tables,
        sampler=None,
    )

    if args.initial_eval:
        before = evaluate_model(model, eval_loader, device, args)
        print(format_metrics("[before]", before), flush=True)

    optimizer = torch.optim.AdamW(model.parameters(), lr=args.lr, weight_decay=args.weight_decay)
    scaler = torch.amp.GradScaler("cuda", enabled=amp_enabled(args, device)) if device.type == "cuda" else None

    best_score = -1.0
    best_metrics: dict[str, float] | None = None
    save_path = args.save_checkpoint or default_save_checkpoint_path(args.dataset_dir)

    for epoch in range(1, args.epochs + 1):
        train_metrics = train_one_epoch(model, optimizer, train_loader, device, epoch, args, scaler)
        print(format_metrics(f"[epoch {epoch:03d} train]", train_metrics), flush=True)
        eval_metrics = evaluate_model(model, eval_loader, device, args)
        print(format_metrics(f"[epoch {epoch:03d} eval]", eval_metrics), flush=True)
        score = eval_metrics["action_acc"] + 0.35 * eval_metrics["skill_acc"] + 0.15 * eval_metrics["head_acc"]
        if score > best_score:
            best_score = score
            best_metrics = eval_metrics
            save_checkpoint(model, save_path, args, cfg, shapes, best_metrics, index_tables)
            print(f"[checkpoint] saved best weights to: {save_path}", flush=True)
        if device.type == "cuda":
            allocated = torch.cuda.memory_allocated(device) / 1024 ** 2
            reserved = torch.cuda.memory_reserved(device) / 1024 ** 2
            peak = torch.cuda.max_memory_allocated(device) / 1024 ** 2
            print(
                f"[epoch {epoch:03d} memory] allocated={allocated:.1f}MB reserved={reserved:.1f}MB peak={peak:.1f}MB",
                flush=True,
            )

    save_checkpoint(model, save_path, args, cfg, shapes, best_metrics, index_tables)
    print(f"Training finished. Final checkpoint: {save_path}", flush=True)


if __name__ == "__main__":
    main()
