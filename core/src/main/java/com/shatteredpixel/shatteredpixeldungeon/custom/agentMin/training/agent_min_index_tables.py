from __future__ import annotations

from typing import Any

import torch
from torch import Tensor

from agent_min_model import (
    ITEM_ID_FEATURE,
    ITEM_ID_SCALE,
    MOB_ID_FEATURE,
    MOB_ID_SCALE,
    MODIFIER_ID_FEATURE,
    MODIFIER_ID_SCALE,
)


class AgentMinIndexTables:
    """Stable class-name to embedding-index tables shared by BC and PPO."""

    def __init__(
        self,
        item_limit: int = ITEM_ID_SCALE,
        modifier_limit: int = MODIFIER_ID_SCALE,
        mob_limit: int = MOB_ID_SCALE,
    ) -> None:
        self.item_limit = int(item_limit)
        self.modifier_limit = int(modifier_limit)
        self.mob_limit = int(mob_limit)
        self.item_index: dict[str, int] = {}
        self.modifier_index: dict[str, int] = {}
        self.mob_index: dict[str, int] = {}

    def apply(self, inventory_matrix: Tensor, item_keys: Any, modifier_keys: Any) -> None:
        self._apply_feature(inventory_matrix, item_keys, self.item_index, self.item_limit, ITEM_ID_FEATURE)
        self._apply_feature(inventory_matrix, modifier_keys, self.modifier_index, self.modifier_limit, MODIFIER_ID_FEATURE)

    def apply_mobs(self, mob_matrix: Tensor, mob_keys: Any) -> None:
        self._apply_feature(mob_matrix, mob_keys, self.mob_index, self.mob_limit, MOB_ID_FEATURE)

    def load(self, item_index: Any, modifier_index: Any, mob_index: Any = None) -> None:
        self.item_index = self._clean_index(item_index, self.item_limit)
        self.modifier_index = self._clean_index(modifier_index, self.modifier_limit)
        self.mob_index = self._clean_index(mob_index, self.mob_limit)

    def load_checkpoint(self, payload: Any) -> None:
        if not isinstance(payload, dict):
            return
        self.load(payload.get("item_index"), payload.get("modifier_index"), payload.get("mob_index"))

    def state_dict(self) -> dict[str, dict[str, int] | int]:
        return {
            "item_index": dict(self.item_index),
            "modifier_index": dict(self.modifier_index),
            "mob_index": dict(self.mob_index),
            "item_limit": self.item_limit,
            "modifier_limit": self.modifier_limit,
            "mob_limit": self.mob_limit,
        }

    def _apply_feature(self, matrix: Tensor, keys: Any, table: dict[str, int], limit: int, feature: int) -> None:
        if not isinstance(keys, list) or matrix.ndim != 2 or matrix.shape[1] <= feature:
            return
        rows = min(matrix.shape[0], len(keys))
        for row in range(rows):
            if float(matrix[row, 0].item()) <= 0:
                continue
            matrix[row, feature] = float(self._index_for(table, keys[row], limit)) / float(limit)

    def _index_for(self, table: dict[str, int], key: Any, limit: int) -> int:
        normalized = "" if key is None else str(key).strip()
        if not normalized:
            return 0
        current = table.get(normalized)
        if current is not None:
            return current
        used = set(table.values())
        for candidate in range(1, limit):
            if candidate not in used:
                table[normalized] = candidate
                return candidate
        return 0

    @staticmethod
    def _clean_index(value: Any, limit: int) -> dict[str, int]:
        if not isinstance(value, dict):
            return {}
        cleaned: dict[str, int] = {}
        used: set[int] = set()
        for key, raw_index in value.items():
            try:
                index = int(raw_index)
            except (TypeError, ValueError):
                continue
            normalized = str(key).strip()
            if not normalized or index <= 0 or index >= limit or index in used:
                continue
            cleaned[normalized] = index
            used.add(index)
        return cleaned
