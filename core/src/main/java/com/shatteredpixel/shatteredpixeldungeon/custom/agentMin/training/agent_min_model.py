from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

import torch
from torch import Tensor, nn
import torch.nn.functional as F

# Keep masks finite so checkpoint tensors and Categorical stay stable.  Every
# masked policy tensor is promoted to float32 first; masked_fill then blocks
# gradients to invalid source logits while -1e6 gives them zero probability.
MASKED_LOGIT = -1e9
TALENT_ID_SCALE = 800
ITEM_ID_SCALE = 512
MODIFIER_ID_SCALE = 64
MOB_ID_SCALE = 384
ITEM_ID_FEATURE = 72
MODIFIER_ID_FEATURE = 73
MOB_ID_FEATURE = 28


def hard_mask_logits(logits: Tensor, valid: Tensor) -> Tensor:
    """Hard-mask invalid actions without fp16 overflow or NaN propagation."""
    return logits.float().masked_fill(~valid.bool(), MASKED_LOGIT)

ACTION_KIND_FEATURE = 1
ACTION_TARGET_MOB_FEATURE = 7
ACTION_ITEM_ROW_FEATURE = 8
ACTION_STAIR_ASCEND_FEATURE = 11
ACTION_STAIR_DESCEND_FEATURE = 12
ACTION_PRIORITY_FEATURE = 13
ACTION_ITEM_VERB_FEATURE = 15
ACTION_MOVE_FEATURE = 16
ACTION_MELEE_FEATURE = 17
ACTION_HEALING_FEATURE = 18
ACTION_FOOD_FEATURE = 19
ACTION_THROW_FEATURE = 20
ACTION_WAND_FEATURE = 21
ACTION_TARGETED_FEATURE = 46
ACTION_ITEM_ACTION_FEATURE = 42
ACTION_ALCHEMY_FEATURE = 43
ACTION_TALENT_FEATURE = 44
ACTION_DROP_FEATURE = 45
ACTION_OPTION_INDEX_FEATURE = 48
ACTION_OPTION_FEATURE = 49
ACTION_TALENT_RESOURCE_FEATURE = 50
ACTION_ITEM_HEAD_HINT_FEATURE = 51

HEAD_MOVE = 0
HEAD_WAIT = 1
HEAD_UNLOCK = 2
HEAD_PICKUP = 3
HEAD_STAIRS = 4
HEAD_MELEE = 5
HEAD_THROW = 6
HEAD_WAND = 7
HEAD_COMBAT_ITEM = 8
HEAD_ACTION_INDICATOR = 9
HEAD_FOOD = 10
HEAD_HEALING = 11
HEAD_ITEM_SELECT = 12
HEAD_ITEM_ACTION = 13
HEAD_TALENT = 14
HEAD_ALCHEMY = 15
HEAD_DROP = 16
HEAD_MAP_MONITOR = 17
HEAD_INVENTORY_MONITOR = 18
HEAD_OPTION = 19
HEAD_EQUIP = 20
HEAD_UNEQUIP = 21
HEAD_READ = 22
HEAD_CAST = 23
HEAD_PLANT = 24
HEAD_OPEN = 25
HEAD_INSPECT = 26
HEAD_BLESS = 27
HEAD_ACTIVATE = 28
HEAD_LIGHT = 29
HEAD_SNACK = 30
HEAD_STEALTH = 31
HEAD_ROOT = 32
HEAD_TRANSMUTE = 33
HEAD_ALCHEMY_ACTION = 34
HEAD_GENERIC = 35
SKILL_ITEM = 6


@dataclass(frozen=True)
class AgentMinModelConfig:
    level_channels: int = 19
    hero_dim: int = 32
    talent_dim: int = 0
    inventory_rows: int = 80
    inventory_features: int = 12
    inventory_summary_dim: int = 6
    option_dim: int = 8
    mob_rows: int = 32
    mob_features: int = 12
    history_rows: int = 16
    history_features: int = 16
    action_rows: int = 96
    action_features: int = 52
    skill_count: int = 10
    skill_head_count: int = 36
    action_kind_count: int = 18
    action_verb_count: int = 32
    action_index_scale: int = 160
    monitor_cell_count: int = 31 * 31
    monitor_option_count: int = 32
    talent_slot_count: int = 24
    talent_slot_features: int = 4
    talent_embedding_dim: int = 64
    talent_vocab_size: int = 800
    talent_type_count: int = 16
    item_embedding_dim: int = 32
    item_vocab_size: int = 512
    modifier_embedding_dim: int = 16
    modifier_vocab_size: int = 64
    mob_embedding_dim: int = 16
    mob_vocab_size: int = 384
    hidden_dim: int = 192
    action_embed_dim: int = 192
    attention_heads: int = 8
    set_transformer_layers: int = 1
    state_transformer_layers: int = 1
    action_transformer_layers: int = 1
    transformer_ff_mult: int = 4
    dropout: float = 0.08
    priority_logit_scale: float = 1.5

    @property
    def full_hero_dim(self) -> int:
        return self.hero_dim + self.talent_dim

    @property
    def talent_slot_raw_dim(self) -> int:
        return self.talent_slot_count * self.talent_slot_features

    @property
    def embedded_hero_dim(self) -> int:
        if self.full_hero_dim >= self.talent_slot_raw_dim:
            return self.full_hero_dim - self.talent_slot_raw_dim + self.talent_slot_count * self.talent_embedding_dim
        return self.full_hero_dim + self.talent_slot_count * self.talent_embedding_dim

    @property
    def embedded_inventory_features(self) -> int:
        return self.inventory_features + self.item_embedding_dim + self.modifier_embedding_dim

    @property
    def embedded_mob_features(self) -> int:
        return self.mob_features + self.mob_embedding_dim


class MLPBlock(nn.Module):
    def __init__(self, dim: int, dropout: float):
        super().__init__()
        self.net = nn.Sequential(
            nn.LayerNorm(dim),
            nn.Linear(dim, dim * 4),
            nn.GELU(),
            nn.Dropout(dropout),
            nn.Linear(dim * 4, dim),
            nn.Dropout(dropout),
        )

    def forward(self, x: Tensor) -> Tensor:
        return x + self.net(x)


class MapCNN(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.net = nn.Sequential(
            nn.Conv2d(cfg.level_channels, 64, kernel_size=3, padding=1),
            nn.GroupNorm(8, 64),
            nn.SiLU(),
            nn.Conv2d(64, 128, kernel_size=3, padding=1),
            nn.GroupNorm(8, 128),
            nn.SiLU(),
            nn.Conv2d(128, 192, kernel_size=3, padding=1),
            nn.GroupNorm(8, 192),
            nn.SiLU(),
            nn.Conv2d(192, 256, kernel_size=3, padding=1),
            nn.GroupNorm(8, 256),
            nn.SiLU(),
        )
        self.pool = nn.AdaptiveAvgPool2d((3, 3))
        self.proj = nn.Sequential(
            nn.Linear(256 * 9, cfg.hidden_dim),
            nn.GELU(),
            nn.LayerNorm(cfg.hidden_dim),
        )

    def forward(self, level_tensor: Tensor) -> Tensor:
        x = self.net(level_tensor)
        x = self.pool(x).flatten(1)
        return self.proj(x)


class GlobalMapCNN(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.net = nn.Sequential(
            nn.Conv2d(1, 32, kernel_size=5, stride=2, padding=2),
            nn.GroupNorm(8, 32),
            nn.SiLU(),
            nn.Conv2d(32, 64, kernel_size=3, stride=2, padding=1),
            nn.GroupNorm(8, 64),
            nn.SiLU(),
            nn.Conv2d(64, 128, kernel_size=3, stride=2, padding=1),
            nn.GroupNorm(8, 128),
            nn.SiLU(),
            nn.Conv2d(128, 256, kernel_size=3, stride=2, padding=1),
            nn.GroupNorm(8, 256),
            nn.SiLU(),
            nn.Conv2d(256, 256, kernel_size=3, stride=2, padding=1),
            nn.GroupNorm(8, 256),
            nn.SiLU(),
        )
        self.pool = nn.AdaptiveAvgPool2d((2, 2))
        self.proj = nn.Sequential(
            nn.Linear(256 * 4, cfg.hidden_dim),
            nn.GELU(),
            nn.LayerNorm(cfg.hidden_dim),
        )

    def forward(self, explored_global_matrix: Tensor) -> Tensor:
        if explored_global_matrix.dim() == 3:
            explored_global_matrix = explored_global_matrix.unsqueeze(1)
        x = self.net(explored_global_matrix)
        x = self.pool(x).flatten(1)
        return self.proj(x)


class SetTransformerEncoder(nn.Module):
    def __init__(self, in_dim: int, cfg: AgentMinModelConfig):
        super().__init__()
        hidden = cfg.hidden_dim
        self.row = nn.Sequential(
            nn.Linear(in_dim, hidden),
            nn.GELU(),
            nn.LayerNorm(hidden),
            MLPBlock(hidden, cfg.dropout),
        )
        layer = nn.TransformerEncoderLayer(
            d_model=hidden,
            nhead=cfg.attention_heads,
            dim_feedforward=hidden * cfg.transformer_ff_mult,
            dropout=cfg.dropout,
            activation="gelu",
            batch_first=True,
            norm_first=False,
        )
        self.transformer = nn.TransformerEncoder(layer, num_layers=cfg.set_transformer_layers)
        self.query = nn.Parameter(torch.zeros(1, 1, hidden))
        self.pool = nn.MultiheadAttention(
            embed_dim=hidden,
            num_heads=cfg.attention_heads,
            dropout=cfg.dropout,
            batch_first=True,
        )
        self.out = nn.Sequential(nn.LayerNorm(hidden), MLPBlock(hidden, cfg.dropout))

    def forward(self, matrix: Tensor) -> Tensor:
        row_emb = self.row(matrix)
        present = matrix.abs().sum(dim=-1) > 0
        all_empty = ~present.any(dim=1)
        key_padding_mask = ~present
        if all_empty.any():
            row_emb = row_emb.clone()
            key_padding_mask = key_padding_mask.clone()
            row_emb[all_empty, 0] = 0
            key_padding_mask[all_empty, 0] = False
        row_emb = self.transformer(row_emb, src_key_padding_mask=key_padding_mask)
        query = self.query.expand(matrix.shape[0], -1, -1)
        pooled, _ = self.pool(query=query, key=row_emb, value=row_emb, key_padding_mask=key_padding_mask)
        pooled = pooled.squeeze(1)
        pooled = torch.where(all_empty.unsqueeze(-1), torch.zeros_like(pooled), pooled)
        return self.out(pooled)


class TalentEmbeddingEncoder(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.cfg = cfg
        self.embedding = nn.Embedding(cfg.talent_vocab_size, cfg.talent_embedding_dim, padding_idx=0)

    def forward(self, hero_vector: Tensor) -> Tensor:
        base, slot_features, talent_emb, present = self.slot_embeddings(hero_vector)
        return torch.cat([base, talent_emb.flatten(1).to(dtype=hero_vector.dtype)], dim=-1)

    def slot_embeddings(self, hero_vector: Tensor) -> tuple[Tensor, Tensor, Tensor, Tensor]:
        raw_dim = self.cfg.talent_slot_raw_dim
        if hero_vector.shape[-1] >= raw_dim:
            base = hero_vector[..., :-raw_dim]
            slot_features = hero_vector[..., -raw_dim:].reshape(
                hero_vector.shape[0],
                self.cfg.talent_slot_count,
                self.cfg.talent_slot_features,
            )
        else:
            base = hero_vector
            slot_features = hero_vector.new_zeros(
                hero_vector.shape[0],
                self.cfg.talent_slot_count,
                self.cfg.talent_slot_features,
            )
        talent_ids = torch.round(slot_features[..., 0] * TALENT_ID_SCALE).long()
        talent_ids = talent_ids.clamp(0, self.cfg.talent_vocab_size - 1)
        present = (slot_features[..., 3] > 0.5) & (talent_ids > 0)
        talent_emb = self.embedding(talent_ids) * present.unsqueeze(-1).to(dtype=hero_vector.dtype)
        return base, slot_features, talent_emb, present


class InventoryEmbeddingEncoder(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.cfg = cfg
        self.item_embedding = nn.Embedding(cfg.item_vocab_size, cfg.item_embedding_dim, padding_idx=0)
        self.modifier_embedding = nn.Embedding(cfg.modifier_vocab_size, cfg.modifier_embedding_dim, padding_idx=0)

    def forward(self, inventory_matrix: Tensor) -> Tensor:
        present = inventory_matrix[..., 0] > 0
        item_ids = self._index_from_feature(inventory_matrix, ITEM_ID_FEATURE, ITEM_ID_SCALE, self.cfg.item_vocab_size)
        modifier_ids = self._index_from_feature(inventory_matrix, MODIFIER_ID_FEATURE, MODIFIER_ID_SCALE, self.cfg.modifier_vocab_size)
        item_emb = self.item_embedding(item_ids) * present.unsqueeze(-1).to(dtype=inventory_matrix.dtype)
        modifier_emb = self.modifier_embedding(modifier_ids) * present.unsqueeze(-1).to(dtype=inventory_matrix.dtype)
        return torch.cat(
            [
                inventory_matrix,
                item_emb.to(dtype=inventory_matrix.dtype),
                modifier_emb.to(dtype=inventory_matrix.dtype),
            ],
            dim=-1,
        )

    def _index_from_feature(self, matrix: Tensor, feature: int, scale: int, vocab_size: int) -> Tensor:
        if matrix.shape[-1] <= feature:
            return torch.zeros(matrix.shape[:-1], dtype=torch.long, device=matrix.device)
        ids = torch.round(matrix[..., feature].clamp(0, 1) * scale).long()
        return ids.clamp(0, vocab_size - 1)


class MobEmbeddingEncoder(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.cfg = cfg
        self.mob_embedding = nn.Embedding(cfg.mob_vocab_size, cfg.mob_embedding_dim, padding_idx=0)

    def forward(self, mob_matrix: Tensor) -> Tensor:
        present = mob_matrix[..., 0] > 0
        mob_ids = self._index_from_feature(mob_matrix, MOB_ID_FEATURE, MOB_ID_SCALE, self.cfg.mob_vocab_size)
        mob_emb = self.mob_embedding(mob_ids) * present.unsqueeze(-1).to(dtype=mob_matrix.dtype)
        return torch.cat([mob_matrix, mob_emb.to(dtype=mob_matrix.dtype)], dim=-1)

    def _index_from_feature(self, matrix: Tensor, feature: int, scale: int, vocab_size: int) -> Tensor:
        if matrix.shape[-1] <= feature:
            return torch.zeros(matrix.shape[:-1], dtype=torch.long, device=matrix.device)
        ids = torch.round(matrix[..., feature].clamp(0, 1) * scale).long()
        return ids.clamp(0, vocab_size - 1)


class HistoryRNN(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.input = nn.Sequential(
            nn.Linear(cfg.history_features, cfg.hidden_dim),
            nn.GELU(),
            nn.LayerNorm(cfg.hidden_dim),
        )
        self.gru = nn.GRU(
            input_size=cfg.hidden_dim,
            hidden_size=cfg.hidden_dim,
            num_layers=2,
            dropout=cfg.dropout,
            batch_first=True,
        )
        self.out = nn.Sequential(nn.LayerNorm(cfg.hidden_dim), MLPBlock(cfg.hidden_dim, cfg.dropout))

    def forward(self, history_matrix: Tensor) -> Tensor:
        x = self.input(history_matrix)
        if x.is_cuda:
            # 当前 Windows + RTX 50 系 + cuDNN RNN 后端在进程退出阶段偶发 0xC0000409。
            # 关闭 cuDNN 路径可以保留 RNN 结构，同时让训练/测试进程稳定收尾。
            with torch.backends.cudnn.flags(enabled=False):
                _, h = self.gru(x)
        else:
            _, h = self.gru(x)
        return self.out(h[-1])


class HierarchicalActionPolicy(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.cfg = cfg
        hidden = cfg.hidden_dim
        action_hidden = cfg.action_embed_dim

        self.agent_hidden = nn.Sequential(
            nn.LayerNorm(hidden),
            nn.Linear(hidden, hidden),
            nn.GELU(),
            MLPBlock(hidden, cfg.dropout),
        )
        self.skill_policy = nn.Sequential(
            nn.LayerNorm(hidden),
            nn.Linear(hidden, hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(hidden, cfg.skill_count),
        )
        self.skill_hidden_layers = nn.ModuleList(
            [
                nn.Sequential(
                    nn.LayerNorm(hidden),
                    nn.Linear(hidden, hidden),
                    nn.GELU(),
                    MLPBlock(hidden, cfg.dropout),
                )
                for _ in range(cfg.skill_count)
            ]
        )
        self.head_policy_layers = nn.ModuleList(
            [
                nn.Sequential(
                    nn.LayerNorm(hidden),
                    nn.Linear(hidden, hidden),
                    nn.GELU(),
                    nn.Dropout(cfg.dropout),
                    nn.Linear(hidden, cfg.skill_head_count),
                )
                for _ in range(cfg.skill_count)
            ]
        )
        self.skill_to_action = nn.Linear(hidden, action_hidden)
        self.head_type_embedding = nn.Parameter(torch.zeros(cfg.skill_head_count, action_hidden))

        self.inventory_row_encoder = nn.Sequential(
            nn.Linear(cfg.embedded_inventory_features, action_hidden),
            nn.GELU(),
            nn.LayerNorm(action_hidden),
        )
        self.mob_row_encoder = nn.Sequential(
            nn.Linear(cfg.embedded_mob_features, action_hidden),
            nn.GELU(),
            nn.LayerNorm(action_hidden),
        )
        self.talent_slot_encoder = nn.Sequential(
            nn.Linear(cfg.talent_embedding_dim + cfg.talent_slot_features, action_hidden),
            nn.GELU(),
            nn.LayerNorm(action_hidden),
        )
        self.talent_query = nn.Parameter(torch.zeros(1, 1, action_hidden))
        self.talent_pool = nn.MultiheadAttention(
            embed_dim=action_hidden,
            num_heads=cfg.attention_heads,
            dropout=cfg.dropout,
            batch_first=True,
        )
        self.context_gate = nn.Parameter(torch.zeros(cfg.skill_head_count, 4))
        self.head_scorers = nn.ModuleList(
            [
                nn.Sequential(
                    nn.LayerNorm(action_hidden),
                    nn.Linear(action_hidden, action_hidden),
                    nn.GELU(),
                    nn.Dropout(cfg.dropout),
                    nn.Linear(action_hidden, 1),
                )
                for _ in range(cfg.skill_head_count)
            ]
        )
        self.item_action_selector = nn.Sequential(
            nn.LayerNorm(action_hidden),
            nn.Linear(action_hidden, action_hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(action_hidden, 1),
        )
        self.talent_target_mu = nn.Sequential(
            nn.LayerNorm(hidden),
            nn.Linear(hidden, hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(hidden, cfg.talent_embedding_dim),
        )
        self.talent_type_policy = nn.Sequential(
            nn.LayerNorm(hidden),
            nn.Linear(hidden, hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(hidden, cfg.talent_type_count),
        )
        self.inventory_monitor_policy = nn.Sequential(
            nn.LayerNorm(hidden),
            nn.Linear(hidden, hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(hidden, cfg.inventory_rows),
        )
        self.monitor_context = nn.Sequential(
            nn.LayerNorm(hidden + action_hidden),
            nn.Linear(hidden + action_hidden, hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
        )
        self.cell_monitor_policy = nn.Sequential(
            nn.LayerNorm(hidden),
            nn.Linear(hidden, hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(hidden, cfg.monitor_cell_count),
        )
        self.option_monitor_policy = nn.Sequential(
            nn.LayerNorm(hidden),
            nn.Linear(hidden, hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(hidden, cfg.monitor_option_count),
        )
        self.talent_target_log_std = nn.Parameter(torch.full((cfg.talent_embedding_dim,), -0.35))
        self.register_buffer("skill_head_affinity", self._default_skill_head_affinity(), persistent=False)
        self._reset_parameters()

    def _reset_parameters(self) -> None:
        nn.init.normal_(self.head_type_embedding, std=0.02)
        nn.init.normal_(self.talent_query, std=0.02)
        with torch.no_grad():
            self.context_gate.zero_()
            self.context_gate[:, 0] = 1.0
            for head in (HEAD_MELEE, HEAD_THROW, HEAD_WAND, HEAD_COMBAT_ITEM, HEAD_ACTION_INDICATOR, HEAD_MAP_MONITOR):
                self.context_gate[head, 2] = 1.0
            for head in (
                HEAD_HEALING,
                HEAD_FOOD,
                HEAD_ITEM_SELECT,
                HEAD_ITEM_ACTION,
                HEAD_TALENT,
                HEAD_DROP,
                HEAD_INVENTORY_MONITOR,
                HEAD_OPTION,
                HEAD_EQUIP,
                HEAD_UNEQUIP,
                HEAD_READ,
                HEAD_CAST,
                HEAD_PLANT,
                HEAD_OPEN,
                HEAD_INSPECT,
                HEAD_BLESS,
                HEAD_ACTIVATE,
                HEAD_LIGHT,
                HEAD_SNACK,
                HEAD_STEALTH,
                HEAD_ROOT,
                HEAD_TRANSMUTE,
                HEAD_ALCHEMY_ACTION,
            ):
                self.context_gate[head, 1] = 1.0
            for head in (HEAD_TALENT, HEAD_READ, HEAD_CAST, HEAD_TRANSMUTE):
                self.context_gate[head, 3] = 1.0

    def _default_skill_head_affinity(self) -> Tensor:
        affinity = torch.zeros(self.cfg.skill_count, self.cfg.skill_head_count, dtype=torch.bool)

        def allow(skill: int, *heads: int) -> None:
            if skill >= self.cfg.skill_count:
                return
            for head in heads:
                if 0 <= head < self.cfg.skill_head_count:
                    affinity[skill, head] = True

        allow(0, HEAD_MOVE, HEAD_WAIT, HEAD_UNLOCK, HEAD_PICKUP, HEAD_STAIRS, HEAD_GENERIC)
        allow(1, HEAD_MELEE, HEAD_THROW, HEAD_WAND, HEAD_COMBAT_ITEM, HEAD_ACTION_INDICATOR, HEAD_MAP_MONITOR, HEAD_GENERIC)
        allow(2, HEAD_FOOD, HEAD_HEALING, HEAD_ITEM_ACTION, HEAD_GENERIC)
        allow(3, HEAD_UNLOCK, HEAD_GENERIC)
        allow(4, HEAD_PICKUP, HEAD_GENERIC)
        allow(5, HEAD_STAIRS, HEAD_GENERIC)
        allow(
            SKILL_ITEM,
            HEAD_ITEM_SELECT,
            HEAD_ITEM_ACTION,
            HEAD_THROW,
            HEAD_WAND,
            HEAD_FOOD,
            HEAD_HEALING,
            HEAD_MAP_MONITOR,
            HEAD_INVENTORY_MONITOR,
            HEAD_OPTION,
            HEAD_EQUIP,
            HEAD_UNEQUIP,
            HEAD_READ,
            HEAD_CAST,
            HEAD_PLANT,
            HEAD_OPEN,
            HEAD_INSPECT,
            HEAD_BLESS,
            HEAD_ACTIVATE,
            HEAD_LIGHT,
            HEAD_SNACK,
            HEAD_STEALTH,
            HEAD_ROOT,
            HEAD_TALENT,
            HEAD_TRANSMUTE,
            HEAD_ALCHEMY_ACTION,
            HEAD_GENERIC,
        )
        allow(7, HEAD_ALCHEMY, HEAD_GENERIC)
        allow(8, HEAD_TALENT, HEAD_ITEM_SELECT, HEAD_ITEM_ACTION, HEAD_GENERIC)
        allow(9, HEAD_DROP, HEAD_ITEM_ACTION, HEAD_GENERIC)
        if self.cfg.skill_count > 10:
            affinity[10:, HEAD_GENERIC] = True
        if not affinity.any():
            affinity[:, HEAD_GENERIC] = True
        return affinity

    def forward(
        self,
        state: Tensor,
        action_tokens: Tensor,
        action_matrix: Tensor,
        action_mask: Optional[Tensor],
        skill_mask: Optional[Tensor],
        action_skill_mask: Optional[Tensor],
        inventory_features: Tensor,
        mob_matrix: Tensor,
        talent_slot_features: Tensor,
        talent_slot_embeddings: Tensor,
        talent_slot_present: Tensor,
        forced_skill: Optional[Tensor] = None,
    ) -> Tensor:
        components = self.components(
            state=state,
            action_tokens=action_tokens,
            action_matrix=action_matrix,
            action_mask=action_mask,
            skill_mask=skill_mask,
            action_skill_mask=action_skill_mask,
            inventory_features=inventory_features,
            mob_matrix=mob_matrix,
            talent_slot_features=talent_slot_features,
            talent_slot_embeddings=talent_slot_embeddings,
            talent_slot_present=talent_slot_present,
            forced_skill=forced_skill,
        )
        return components["logits"]

    def components(
        self,
        state: Tensor,
        action_tokens: Tensor,
        action_matrix: Tensor,
        action_mask: Optional[Tensor],
        skill_mask: Optional[Tensor],
        action_skill_mask: Optional[Tensor],
        inventory_features: Tensor,
        mob_matrix: Tensor,
        talent_slot_features: Tensor,
        talent_slot_embeddings: Tensor,
        talent_slot_present: Tensor,
        forced_skill: Optional[Tensor] = None,
    ) -> dict[str, Tensor]:
        valid_actions = self._valid_actions(action_matrix, action_mask)
        action_head_mask = self.action_head_mask(action_matrix, valid_actions)
        action_contexts = self.action_contexts(
            action_matrix=action_matrix,
            inventory_features=inventory_features,
            mob_matrix=mob_matrix,
            talent_slot_features=talent_slot_features,
            talent_slot_embeddings=talent_slot_embeddings,
            talent_slot_present=talent_slot_present,
        )
        skill_action_mask = self.skill_action_mask(action_head_mask, action_skill_mask, valid_actions)
        item_preferred_heads = self.item_preferred_heads(action_head_mask, action_matrix)
        skill_valid = self.skill_valid_mask(skill_action_mask, skill_mask, valid_actions, forced_skill)

        agent_hidden = self.agent_hidden(state)
        skill_logits = hard_mask_logits(self.skill_policy(agent_hidden), skill_valid)
        skill_logits = self._force_skill_logits(skill_logits, forced_skill, skill_valid)
        skill_log_probs = F.log_softmax(skill_logits, dim=-1)

        skill_hiddens = torch.stack([layer(agent_hidden) for layer in self.skill_hidden_layers], dim=1)
        head_valid = self.head_valid_mask(skill_action_mask, action_head_mask, valid_actions, item_preferred_heads)
        head_logits = torch.stack(
            [
                self.head_policy_layers[skill](skill_hiddens[:, skill, :])
                for skill in range(self.cfg.skill_count)
            ],
            dim=1,
        )
        head_logits = hard_mask_logits(head_logits, head_valid)
        head_log_probs = F.log_softmax(head_logits, dim=-1)

        head_action_logits = self.head_action_logits(skill_hiddens, action_tokens, action_contexts)
        route_mask = self.route_mask(skill_action_mask, action_head_mask, valid_actions, item_preferred_heads)
        routed_action_logits = hard_mask_logits(head_action_logits, route_mask)
        routed_action_log_probs = F.log_softmax(routed_action_logits, dim=-1)
        item_action_logits = self.item_action_logits(
            skill_hiddens[:, SKILL_ITEM, :],
            action_tokens,
            action_contexts,
            action_matrix,
            skill_action_mask[:, SKILL_ITEM, :] & valid_actions,
            item_preferred_heads,
        )
        item_action_log_probs = F.log_softmax(item_action_logits, dim=-1)
        joint_log_probs = (
            skill_log_probs.unsqueeze(-1).unsqueeze(-1)
            + head_log_probs.unsqueeze(-1)
            + routed_action_log_probs
        )
        joint_log_probs = hard_mask_logits(joint_log_probs, route_mask)
        logits = torch.logsumexp(joint_log_probs.flatten(1, 2), dim=1)
        logits = logits + action_matrix[..., ACTION_PRIORITY_FEATURE] * self.cfg.priority_logit_scale
        logits = hard_mask_logits(logits, valid_actions)
        return {
            "agent_hidden": agent_hidden,
            "skill_hiddens": skill_hiddens,
            "skill_logits": skill_logits,
            "skill_log_probs": skill_log_probs,
            "head_logits": head_logits,
            "head_log_probs": head_log_probs,
            "head_action_logits": routed_action_logits,
            "head_action_log_probs": routed_action_log_probs,
            "item_action_logits": item_action_logits,
            "item_action_log_probs": item_action_log_probs,
            "item_preferred_heads": item_preferred_heads,
            "action_contexts": action_contexts,
            "route_mask": route_mask,
            "valid_actions": valid_actions,
            "logits": logits,
        }

    def sample(
        self,
        state: Tensor,
        action_tokens: Tensor,
        action_matrix: Tensor,
        action_mask: Optional[Tensor],
        skill_mask: Optional[Tensor],
        action_skill_mask: Optional[Tensor],
        inventory_features: Tensor,
        mob_matrix: Tensor,
        talent_slot_features: Tensor,
        talent_slot_embeddings: Tensor,
        talent_slot_present: Tensor,
        forced_skill: Optional[Tensor] = None,
        monitor_item_mask: Optional[Tensor] = None,
        monitor_cell_mask: Optional[Tensor] = None,
        monitor_option_mask: Optional[Tensor] = None,
        deterministic: bool = False,
    ) -> dict[str, Tensor]:
        c = self.components(
            state=state,
            action_tokens=action_tokens,
            action_matrix=action_matrix,
            action_mask=action_mask,
            skill_mask=skill_mask,
            action_skill_mask=action_skill_mask,
            inventory_features=inventory_features,
            mob_matrix=mob_matrix,
            talent_slot_features=talent_slot_features,
            talent_slot_embeddings=talent_slot_embeddings,
            talent_slot_present=talent_slot_present,
            forced_skill=forced_skill,
        )
        skill_dist = torch.distributions.Categorical(logits=c["skill_logits"].float())
        skill = c["skill_logits"].argmax(dim=-1) if deterministic else skill_dist.sample()
        batch_index = torch.arange(skill.shape[0], device=skill.device)
        head_logits = c["head_logits"][batch_index, skill]
        head_dist = torch.distributions.Categorical(logits=head_logits.float())
        sampled_head = head_logits.argmax(dim=-1) if deterministic else head_dist.sample()
        action_logits = c["head_action_logits"][batch_index, skill, sampled_head]
        action_dist = torch.distributions.Categorical(logits=action_logits.float())
        sampled_action = action_logits.argmax(dim=-1) if deterministic else action_dist.sample()
        item_action_dist = torch.distributions.Categorical(logits=c["item_action_logits"].float())
        item_action = c["item_action_logits"].argmax(dim=-1) if deterministic else item_action_dist.sample()
        item_head = c["item_preferred_heads"][batch_index, item_action]
        item_skill_active = skill == SKILL_ITEM
        head = torch.where(item_skill_active, item_head, sampled_head)
        action = torch.where(item_skill_active, item_action, sampled_action)
        selected_skill_hidden = c["skill_hiddens"][batch_index, skill]
        selected_item_context = c["action_contexts"][0][batch_index, action]
        monitor_hidden = self.monitor_context(torch.cat([selected_skill_hidden, selected_item_context], dim=-1))
        talent_target_dist, talent_target_mu = self.talent_target_distribution(selected_skill_hidden)
        talent_target_embedding = talent_target_mu if deterministic else talent_target_dist.rsample()
        talent_type_logits = self.talent_type_policy(selected_skill_hidden)
        talent_type_dist = torch.distributions.Categorical(logits=talent_type_logits.float())
        talent_target_type = talent_type_logits.argmax(dim=-1) if deterministic else talent_type_dist.sample()
        monitor_item_logits = self.inventory_monitor_policy(monitor_hidden)
        monitor_item_logits = self.mask_monitor_item_logits(monitor_item_logits, monitor_item_mask)
        monitor_item_dist = torch.distributions.Categorical(logits=monitor_item_logits.float())
        monitor_item_row = monitor_item_logits.argmax(dim=-1) if deterministic else monitor_item_dist.sample()
        monitor_cell_logits = self.mask_monitor_logits(self.cell_monitor_policy(monitor_hidden), monitor_cell_mask)
        monitor_cell_dist = torch.distributions.Categorical(logits=monitor_cell_logits.float())
        monitor_cell_index = monitor_cell_logits.argmax(dim=-1) if deterministic else monitor_cell_dist.sample()
        monitor_option_logits = self.mask_monitor_logits(self.option_monitor_policy(monitor_hidden), monitor_option_mask)
        monitor_option_dist = torch.distributions.Categorical(logits=monitor_option_logits.float())
        monitor_option_index = monitor_option_logits.argmax(dim=-1) if deterministic else monitor_option_dist.sample()
        skill_log_prob = skill_dist.log_prob(skill)
        if forced_skill is not None:
            forced = (forced_skill >= 0) & (forced_skill < self.cfg.skill_count)
            skill_log_prob = torch.where(forced, torch.zeros_like(skill_log_prob), skill_log_prob)
        path_log_prob = torch.where(
            item_skill_active,
            item_action_dist.log_prob(item_action),
            head_dist.log_prob(sampled_head) + action_dist.log_prob(sampled_action),
        )
        path_entropy = torch.where(
            item_skill_active,
            item_action_dist.entropy(),
            head_dist.entropy() + action_dist.entropy(),
        )
        target_active = self._feature(action_matrix[batch_index, action], ACTION_TALENT_RESOURCE_FEATURE) > 0.5
        target_log_prob = torch.where(
            target_active,
            talent_target_dist.log_prob(talent_target_embedding),
            torch.zeros_like(skill_log_prob),
        )
        target_type_log_prob = torch.where(
            target_active,
            talent_type_dist.log_prob(talent_target_type),
            torch.zeros_like(skill_log_prob),
        )
        target_entropy = torch.where(
            target_active,
            talent_target_dist.entropy() + talent_type_dist.entropy(),
            torch.zeros_like(skill_log_prob),
        )
        selected_kind = self._scaled_index(action_matrix[batch_index, action], ACTION_KIND_FEATURE, self.cfg.action_kind_count)
        monitor_cell_active = (selected_kind == 15) | (
            self._feature(action_matrix[batch_index, action], ACTION_TARGETED_FEATURE) > 0.5
        )
        monitor_item_active = selected_kind == 16
        monitor_option_active = selected_kind == 17
        monitor_cell_log_prob = torch.where(
            monitor_cell_active,
            monitor_cell_dist.log_prob(monitor_cell_index),
            torch.zeros_like(skill_log_prob),
        )
        monitor_item_log_prob = torch.where(
            monitor_item_active,
            monitor_item_dist.log_prob(monitor_item_row),
            torch.zeros_like(skill_log_prob),
        )
        monitor_option_log_prob = torch.where(
            monitor_option_active,
            monitor_option_dist.log_prob(monitor_option_index),
            torch.zeros_like(skill_log_prob),
        )
        monitor_cell_entropy = torch.where(
            monitor_cell_active,
            monitor_cell_dist.entropy(),
            torch.zeros_like(skill_log_prob),
        )
        monitor_item_entropy = torch.where(
            monitor_item_active,
            monitor_item_dist.entropy(),
            torch.zeros_like(skill_log_prob),
        )
        monitor_option_entropy = torch.where(
            monitor_option_active,
            monitor_option_dist.entropy(),
            torch.zeros_like(skill_log_prob),
        )
        return {
            "action": action,
            "skill": skill,
            "head": head,
            "log_prob": skill_log_prob + path_log_prob + target_log_prob + target_type_log_prob
            + monitor_cell_log_prob + monitor_item_log_prob + monitor_option_log_prob,
            "entropy": skill_dist.entropy() + path_entropy + target_entropy
            + monitor_cell_entropy + monitor_item_entropy + monitor_option_entropy,
            "agent_hidden": c["agent_hidden"],
            "skill_hidden": c["skill_hiddens"][batch_index, skill],
            "talent_target_embedding": talent_target_embedding,
            "talent_target_type": talent_target_type,
            "monitor_item_row": monitor_item_row,
            "monitor_cell_index": monitor_cell_index,
            "monitor_option_index": monitor_option_index,
            "logits": c["logits"],
        }

    def evaluate_path(
        self,
        state: Tensor,
        action_tokens: Tensor,
        action_matrix: Tensor,
        action_mask: Optional[Tensor],
        skill_mask: Optional[Tensor],
        action_skill_mask: Optional[Tensor],
        inventory_features: Tensor,
        mob_matrix: Tensor,
        talent_slot_features: Tensor,
        talent_slot_embeddings: Tensor,
        talent_slot_present: Tensor,
        actions: Tensor,
        skills: Optional[Tensor],
        heads: Optional[Tensor],
        talent_target_embeddings: Optional[Tensor] = None,
        talent_target_types: Optional[Tensor] = None,
        monitor_item_rows: Optional[Tensor] = None,
        monitor_item_mask: Optional[Tensor] = None,
        monitor_cell_indices: Optional[Tensor] = None,
        monitor_cell_mask: Optional[Tensor] = None,
        monitor_option_indices: Optional[Tensor] = None,
        monitor_option_mask: Optional[Tensor] = None,
        forced_skill: Optional[Tensor] = None,
    ) -> tuple[Tensor, Tensor, Tensor]:
        c = self.components(
            state=state,
            action_tokens=action_tokens,
            action_matrix=action_matrix,
            action_mask=action_mask,
            skill_mask=skill_mask,
            action_skill_mask=action_skill_mask,
            inventory_features=inventory_features,
            mob_matrix=mob_matrix,
            talent_slot_features=talent_slot_features,
            talent_slot_embeddings=talent_slot_embeddings,
            talent_slot_present=talent_slot_present,
            forced_skill=forced_skill,
        )
        if skills is None or heads is None:
            dist = torch.distributions.Categorical(logits=c["logits"].float())
            return dist.log_prob(actions), dist.entropy(), c["logits"]
        skills = skills.clamp(0, self.cfg.skill_count - 1)
        heads = heads.clamp(0, self.cfg.skill_head_count - 1)
        batch_index = torch.arange(actions.shape[0], device=actions.device)
        skill_log_prob = c["skill_log_probs"][batch_index, skills]
        if forced_skill is not None:
            forced = (forced_skill >= 0) & (forced_skill < self.cfg.skill_count)
            skill_log_prob = torch.where(forced, torch.zeros_like(skill_log_prob), skill_log_prob)
        head_log_prob = c["head_log_probs"][batch_index, skills, heads]
        action_log_prob = c["head_action_log_probs"][batch_index, skills, heads, actions]
        route_valid = c["route_mask"][batch_index, skills, heads, actions]
        non_item_log_prob = hard_mask_logits(skill_log_prob + head_log_prob + action_log_prob, route_valid)
        item_expected_heads = c["item_preferred_heads"][batch_index, actions]
        item_route_valid = (
            (skills == SKILL_ITEM)
            & c["valid_actions"][batch_index, actions]
            & (heads == item_expected_heads)
        )
        item_log_prob = hard_mask_logits(
            skill_log_prob + c["item_action_log_probs"][batch_index, actions], item_route_valid
        )
        log_prob = torch.where(skills == SKILL_ITEM, item_log_prob, non_item_log_prob)
        dist = torch.distributions.Categorical(logits=c["logits"].float())
        entropy = dist.entropy()
        target_active = self._feature(action_matrix[batch_index, actions], ACTION_TALENT_RESOURCE_FEATURE) > 0.5
        if talent_target_embeddings is not None:
            target_dist, _target_mu = self.talent_target_distribution(c["skill_hiddens"][batch_index, skills])
            target_values = talent_target_embeddings.to(device=log_prob.device, dtype=log_prob.dtype)
            target_log_prob = target_dist.log_prob(target_values)
            target_entropy = target_dist.entropy()
            log_prob = log_prob + torch.where(target_active, target_log_prob, torch.zeros_like(log_prob))
            entropy = entropy + torch.where(target_active, target_entropy, torch.zeros_like(entropy))
        if talent_target_types is not None:
            type_logits = self.talent_type_policy(c["skill_hiddens"][batch_index, skills])
            type_dist = torch.distributions.Categorical(logits=type_logits.float())
            type_values = talent_target_types.to(device=log_prob.device).long().clamp(0, self.cfg.talent_type_count - 1)
            type_log_prob = type_dist.log_prob(type_values)
            type_entropy = type_dist.entropy()
            log_prob = log_prob + torch.where(target_active, type_log_prob, torch.zeros_like(log_prob))
            entropy = entropy + torch.where(target_active, type_entropy, torch.zeros_like(entropy))
        selected_kind = self._scaled_index(action_matrix[batch_index, actions], ACTION_KIND_FEATURE, self.cfg.action_kind_count)
        selected_action_context = c["action_contexts"][0][batch_index, actions]
        monitor_hidden = self.monitor_context(
            torch.cat([c["skill_hiddens"][batch_index, skills], selected_action_context], dim=-1)
        )
        monitor_cell_active = (selected_kind == 15) | (
            self._feature(action_matrix[batch_index, actions], ACTION_TARGETED_FEATURE) > 0.5
        )
        if monitor_item_rows is not None:
            monitor_item_active = selected_kind == 16
            monitor_logits = self.inventory_monitor_policy(monitor_hidden)
            monitor_logits = self.mask_monitor_item_logits(monitor_logits, monitor_item_mask)
            monitor_dist = torch.distributions.Categorical(logits=monitor_logits.float())
            monitor_values = monitor_item_rows.to(device=log_prob.device).long().clamp(0, self.cfg.inventory_rows - 1)
            monitor_log_prob = monitor_dist.log_prob(monitor_values)
            monitor_entropy = monitor_dist.entropy()
            log_prob = log_prob + torch.where(monitor_item_active, monitor_log_prob, torch.zeros_like(log_prob))
            entropy = entropy + torch.where(monitor_item_active, monitor_entropy, torch.zeros_like(entropy))
        if monitor_cell_indices is not None:
            cell_logits = self.mask_monitor_logits(self.cell_monitor_policy(monitor_hidden), monitor_cell_mask)
            cell_dist = torch.distributions.Categorical(logits=cell_logits.float())
            cell_values = monitor_cell_indices.to(device=log_prob.device).long().clamp(0, self.cfg.monitor_cell_count - 1)
            cell_log_prob = cell_dist.log_prob(cell_values)
            cell_entropy = cell_dist.entropy()
            log_prob = log_prob + torch.where(monitor_cell_active, cell_log_prob, torch.zeros_like(log_prob))
            entropy = entropy + torch.where(monitor_cell_active, cell_entropy, torch.zeros_like(entropy))
        if monitor_option_indices is not None:
            monitor_option_active = selected_kind == 17
            option_logits = self.mask_monitor_logits(self.option_monitor_policy(monitor_hidden), monitor_option_mask)
            option_dist = torch.distributions.Categorical(logits=option_logits.float())
            option_values = monitor_option_indices.to(device=log_prob.device).long().clamp(0, self.cfg.monitor_option_count - 1)
            option_log_prob = option_dist.log_prob(option_values)
            option_entropy = option_dist.entropy()
            log_prob = log_prob + torch.where(monitor_option_active, option_log_prob, torch.zeros_like(log_prob))
            entropy = entropy + torch.where(monitor_option_active, option_entropy, torch.zeros_like(entropy))
        return log_prob, entropy, c["logits"]

    def talent_target_distribution(self, selected_skill_hidden: Tensor):
        mu = self.talent_target_mu(selected_skill_hidden)
        log_std = self.talent_target_log_std.clamp(-4.0, 1.0)
        std = log_std.exp().view(1, -1).expand_as(mu)
        return torch.distributions.Independent(torch.distributions.Normal(mu, std), 1), mu

    def mask_monitor_item_logits(self, logits: Tensor, monitor_item_mask: Optional[Tensor]) -> Tensor:
        return self.mask_monitor_logits(logits, monitor_item_mask)

    def mask_monitor_logits(self, logits: Tensor, monitor_mask: Optional[Tensor]) -> Tensor:
        if monitor_mask is None or monitor_mask.shape != logits.shape:
            return logits
        valid = monitor_mask > 0
        has_valid = valid.any(dim=-1, keepdim=True)
        masked = hard_mask_logits(logits, valid)
        return torch.where(has_valid, masked, logits)

    def action_contexts(
        self,
        action_matrix: Tensor,
        inventory_features: Tensor,
        mob_matrix: Tensor,
        talent_slot_features: Tensor,
        talent_slot_embeddings: Tensor,
        talent_slot_present: Tensor,
    ) -> tuple[Tensor, Tensor, Tensor]:
        item_rows = self.inventory_row_encoder(inventory_features)
        item_context = self._gather_rows(
            item_rows,
            action_matrix[..., ACTION_ITEM_ROW_FEATURE],
            self.cfg.action_index_scale,
        )
        mob_rows = self.mob_row_encoder(mob_matrix)
        mob_context = self._gather_rows(
            mob_rows,
            action_matrix[..., ACTION_TARGET_MOB_FEATURE],
            self.cfg.action_index_scale,
        )
        talent_context = self.talent_candidate_context(
            talent_slot_features,
            talent_slot_embeddings,
            talent_slot_present,
            action_matrix,
        )
        return item_context, mob_context, talent_context

    def talent_candidate_context(
        self,
        talent_slot_features: Tensor,
        talent_slot_embeddings: Tensor,
        talent_slot_present: Tensor,
        action_matrix: Tensor,
    ) -> Tensor:
        slot_input = torch.cat(
            [
                talent_slot_embeddings.to(dtype=talent_slot_features.dtype),
                talent_slot_features,
            ],
            dim=-1,
        )
        slot_tokens = self.talent_slot_encoder(slot_input)
        source_candidate = talent_slot_present & (talent_slot_features[..., 1] > 0)
        all_empty = ~source_candidate.any(dim=1)
        key_padding_mask = ~source_candidate
        if all_empty.any():
            key_padding_mask = key_padding_mask.clone()
            key_padding_mask[all_empty, 0] = False
        query = self.talent_query.expand(slot_tokens.shape[0], -1, -1)
        pooled, _ = self.talent_pool(query=query, key=slot_tokens, value=slot_tokens, key_padding_mask=key_padding_mask)
        pooled = pooled.squeeze(1)
        pooled = torch.where(all_empty.unsqueeze(-1), torch.zeros_like(pooled), pooled)
        talent_actions = self._feature(action_matrix, ACTION_TALENT_FEATURE) > 0.5
        return pooled.unsqueeze(1) * talent_actions.unsqueeze(-1).to(dtype=pooled.dtype)

    def head_action_logits(
        self,
        skill_hiddens: Tensor,
        action_tokens: Tensor,
        contexts: tuple[Tensor, Tensor, Tensor],
    ) -> Tensor:
        item_context, mob_context, talent_context = contexts
        logits = []
        for skill in range(self.cfg.skill_count):
            skill_base = self.skill_to_action(skill_hiddens[:, skill, :]).unsqueeze(1).expand_as(action_tokens)
            skill_logits = []
            for head, scorer in enumerate(self.head_scorers):
                gate = self.context_gate[head]
                context = (
                    skill_base * gate[0].view(1, 1, 1)
                    + item_context * gate[1].view(1, 1, 1)
                    + mob_context * gate[2].view(1, 1, 1)
                    + talent_context * gate[3].view(1, 1, 1)
                )
                head_token = action_tokens + context + self.head_type_embedding[head].view(1, 1, -1)
                skill_logits.append(scorer(head_token).squeeze(-1))
            logits.append(torch.stack(skill_logits, dim=1))
        return torch.stack(logits, dim=1)

    def item_action_logits(
        self,
        item_skill_hidden: Tensor,
        action_tokens: Tensor,
        contexts: tuple[Tensor, Tensor, Tensor],
        action_matrix: Tensor,
        valid_actions: Tensor,
        preferred_heads: Tensor,
    ) -> Tensor:
        item_context, mob_context, talent_context = contexts
        skill_base = self.skill_to_action(item_skill_hidden).unsqueeze(1).expand_as(action_tokens)
        safe_heads = preferred_heads.clamp(0, self.cfg.skill_head_count - 1)
        head_context = F.embedding(safe_heads, self.head_type_embedding)
        head_gate = self.context_gate[safe_heads].unsqueeze(-1)
        context = (
            skill_base * head_gate[:, :, 0, :]
            + item_context * head_gate[:, :, 1, :]
            + mob_context * head_gate[:, :, 2, :]
            + talent_context * head_gate[:, :, 3, :]
        )
        token = action_tokens + context + head_context
        logits = self.item_action_selector(token).squeeze(-1)
        logits = logits + action_matrix[..., ACTION_PRIORITY_FEATURE] * self.cfg.priority_logit_scale
        logits = logits + self._feature(action_matrix, ACTION_ITEM_HEAD_HINT_FEATURE) * 0.25
        return hard_mask_logits(logits, valid_actions)

    def item_preferred_heads(self, action_head_mask: Tensor, action_matrix: Tensor) -> Tensor:
        kind = self._scaled_index(action_matrix, ACTION_KIND_FEATURE, self.cfg.action_kind_count)
        verb = self._scaled_index(action_matrix, ACTION_ITEM_VERB_FEATURE, self.cfg.action_verb_count)
        is_item = self._feature(action_matrix, ACTION_ITEM_ACTION_FEATURE) > 0.5
        is_option_monitor = (kind == 17) | (self._feature(action_matrix, ACTION_OPTION_FEATURE) > 0.5)
        preferred = torch.full(
            kind.shape,
            HEAD_GENERIC,
            dtype=torch.long,
            device=action_matrix.device,
        )

        def choose(head: int, condition: Tensor) -> None:
            nonlocal preferred
            if 0 <= head < action_head_mask.shape[1]:
                preferred = torch.where(condition & action_head_mask[:, head, :], preferred.new_full(preferred.shape, head), preferred)

        choose(HEAD_GENERIC, action_head_mask[:, HEAD_GENERIC, :])
        choose(HEAD_ITEM_ACTION, is_item)
        choose(HEAD_MAP_MONITOR, kind == 15)
        choose(HEAD_INVENTORY_MONITOR, kind == 16)
        choose(HEAD_OPTION, is_option_monitor)
        choose(HEAD_DROP, (self._feature(action_matrix, ACTION_DROP_FEATURE) > 0.5) | (verb == 1) | (verb == 4))
        choose(HEAD_THROW, (self._feature(action_matrix, ACTION_THROW_FEATURE) > 0.5) | (verb == 2) | (verb == 14))
        choose(HEAD_WAND, (self._feature(action_matrix, ACTION_WAND_FEATURE) > 0.5) | (verb == 8))
        choose(HEAD_HEALING, (self._feature(action_matrix, ACTION_HEALING_FEATURE) > 0.5) | (verb == 5))
        choose(HEAD_FOOD, (self._feature(action_matrix, ACTION_FOOD_FEATURE) > 0.5) | (verb == 7))
        choose(HEAD_EQUIP, verb == 3)
        choose(HEAD_UNEQUIP, verb == 4)
        choose(HEAD_READ, verb == 6)
        choose(HEAD_CAST, verb == 9)
        choose(HEAD_OPEN, verb == 10)
        choose(HEAD_INSPECT, verb == 11)
        choose(HEAD_PLANT, (verb == 12) | (verb == 13))
        choose(HEAD_LIGHT, verb == 15)
        choose(HEAD_ACTIVATE, (verb == 16) | (verb == 21))
        choose(HEAD_BLESS, verb == 17)
        choose(HEAD_SNACK, verb == 18)
        choose(HEAD_STEALTH, verb == 19)
        choose(HEAD_ROOT, verb == 20)
        choose(HEAD_TALENT, self._feature(action_matrix, ACTION_TALENT_RESOURCE_FEATURE) > 0.5)
        choose(HEAD_TRANSMUTE, (verb == 24) | (verb == 25) | (verb == 26) | (verb == 27))
        choose(HEAD_ALCHEMY_ACTION, (verb == 28) | (verb == 29) | (verb == 30) | (verb == 31))
        return preferred

    def action_head_mask(self, action_matrix: Tensor, valid_actions: Tensor) -> Tensor:
        batch, actions = action_matrix.shape[:2]
        mask = torch.zeros(
            batch,
            self.cfg.skill_head_count,
            actions,
            dtype=torch.bool,
            device=action_matrix.device,
        )
        kind = self._scaled_index(action_matrix, ACTION_KIND_FEATURE, self.cfg.action_kind_count)
        verb = self._scaled_index(action_matrix, ACTION_ITEM_VERB_FEATURE, self.cfg.action_verb_count)
        is_item = self._feature(action_matrix, ACTION_ITEM_ACTION_FEATURE) > 0.5
        is_targeted = self._feature(action_matrix, ACTION_TARGETED_FEATURE) > 0.5
        is_stairs = (self._feature(action_matrix, ACTION_STAIR_ASCEND_FEATURE) > 0.5) | (
            self._feature(action_matrix, ACTION_STAIR_DESCEND_FEATURE) > 0.5
        ) | (kind == 7)
        is_unlock = kind == 6
        is_pickup = kind == 5
        is_wait = kind == 3
        is_action_indicator = (kind == 14) | (verb == 22) | (verb == 23)
        is_map_monitor = kind == 15
        is_inventory_monitor = kind == 16
        is_option_monitor = (kind == 17) | (self._feature(action_matrix, ACTION_OPTION_FEATURE) > 0.5)
        is_throw_verb = (verb == 2) | (verb == 14)
        is_zap_verb = verb == 8
        is_eat_verb = verb == 7
        is_drink_verb = verb == 5
        is_drop_verb = (verb == 1) | (verb == 4)
        is_equip_verb = verb == 3
        is_unequip_verb = verb == 4
        is_read_verb = verb == 6
        is_cast_verb = verb == 9
        is_open_verb = verb == 10
        is_inspect_verb = verb == 11
        is_plant_verb = (verb == 12) | (verb == 13)
        is_light_verb = verb == 15
        is_activate_verb = (verb == 16) | (verb == 21)
        is_bless_verb = verb == 17
        is_snack_verb = verb == 18
        is_stealth_verb = verb == 19
        is_root_verb = verb == 20
        is_transmute_verb = (verb == 24) | (verb == 25) | (verb == 26) | (verb == 27)
        is_alchemy_action_verb = (verb == 28) | (verb == 29) | (verb == 30) | (verb == 31)

        self._put(mask, HEAD_MOVE, (self._feature(action_matrix, ACTION_MOVE_FEATURE) > 0.5) | (kind == 1) | (kind == 2))
        self._put(mask, HEAD_WAIT, is_wait)
        self._put(mask, HEAD_UNLOCK, is_unlock)
        self._put(mask, HEAD_PICKUP, is_pickup)
        self._put(mask, HEAD_STAIRS, is_stairs)
        self._put(mask, HEAD_MELEE, (self._feature(action_matrix, ACTION_MELEE_FEATURE) > 0.5) | (kind == 4))
        self._put(mask, HEAD_THROW, (self._feature(action_matrix, ACTION_THROW_FEATURE) > 0.5) | is_throw_verb)
        self._put(mask, HEAD_WAND, (self._feature(action_matrix, ACTION_WAND_FEATURE) > 0.5) | is_zap_verb)
        self._put(mask, HEAD_COMBAT_ITEM, is_item & is_targeted & ~is_throw_verb & ~is_zap_verb)
        self._put(mask, HEAD_ACTION_INDICATOR, is_action_indicator)
        self._put(mask, HEAD_FOOD, (self._feature(action_matrix, ACTION_FOOD_FEATURE) > 0.5) | is_eat_verb)
        self._put(mask, HEAD_HEALING, (self._feature(action_matrix, ACTION_HEALING_FEATURE) > 0.5) | is_drink_verb)
        self._put(mask, HEAD_ITEM_SELECT, is_item | (kind == 8) | (kind == 9) | (kind == 10) | (kind == 11))
        self._put(mask, HEAD_ITEM_ACTION, is_item & ~is_action_indicator)
        self._put(mask, HEAD_TALENT, self._feature(action_matrix, ACTION_TALENT_FEATURE) > 0.5)
        self._put(mask, HEAD_ALCHEMY, (self._feature(action_matrix, ACTION_ALCHEMY_FEATURE) > 0.5) | (kind == 13))
        self._put(mask, HEAD_DROP, (self._feature(action_matrix, ACTION_DROP_FEATURE) > 0.5) | is_drop_verb)
        self._put(mask, HEAD_MAP_MONITOR, is_map_monitor)
        self._put(mask, HEAD_INVENTORY_MONITOR, is_inventory_monitor)
        self._put(mask, HEAD_OPTION, is_option_monitor)
        self._put(mask, HEAD_EQUIP, is_item & is_equip_verb)
        self._put(mask, HEAD_UNEQUIP, is_item & is_unequip_verb)
        self._put(mask, HEAD_READ, is_item & is_read_verb)
        self._put(mask, HEAD_CAST, is_item & is_cast_verb)
        self._put(mask, HEAD_PLANT, is_item & is_plant_verb)
        self._put(mask, HEAD_OPEN, is_item & is_open_verb)
        self._put(mask, HEAD_INSPECT, is_item & is_inspect_verb)
        self._put(mask, HEAD_BLESS, is_item & is_bless_verb)
        self._put(mask, HEAD_ACTIVATE, is_item & is_activate_verb)
        self._put(mask, HEAD_LIGHT, is_item & is_light_verb)
        self._put(mask, HEAD_SNACK, is_item & is_snack_verb)
        self._put(mask, HEAD_STEALTH, is_item & is_stealth_verb)
        self._put(mask, HEAD_ROOT, is_item & is_root_verb)
        self._put(mask, HEAD_TRANSMUTE, is_item & is_transmute_verb)
        self._put(mask, HEAD_ALCHEMY_ACTION, is_item & is_alchemy_action_verb)

        matched = mask.any(dim=1)
        self._put(mask, HEAD_GENERIC, ~matched)
        return mask & valid_actions.unsqueeze(1)

    def skill_action_mask(
        self,
        action_head_mask: Tensor,
        action_skill_mask: Optional[Tensor],
        valid_actions: Tensor,
    ) -> Tensor:
        affinity = self.skill_head_affinity.to(device=action_head_mask.device)
        routed = (action_head_mask.unsqueeze(1) & affinity.view(1, self.cfg.skill_count, self.cfg.skill_head_count, 1)).any(dim=2)
        if action_skill_mask is not None and action_skill_mask.shape[:2] == routed.shape[:2]:
            routed = routed | (action_skill_mask > 0)
        return routed & valid_actions.unsqueeze(1)

    def skill_valid_mask(
        self,
        skill_action_mask: Tensor,
        skill_mask: Optional[Tensor],
        valid_actions: Tensor,
        forced_skill: Optional[Tensor],
    ) -> Tensor:
        skill_valid = skill_action_mask.any(dim=-1)
        if skill_mask is not None and skill_mask.shape == skill_valid.shape:
            skill_valid = skill_valid | ((skill_mask > 0) & skill_action_mask.any(dim=-1))
        if forced_skill is not None:
            forced = (forced_skill >= 0) & (forced_skill < self.cfg.skill_count)
            forced_valid = torch.zeros_like(skill_valid)
            safe = forced_skill.clamp(0, self.cfg.skill_count - 1)
            forced_valid.scatter_(1, safe.unsqueeze(1), forced.unsqueeze(1))
            skill_valid = torch.where(forced.unsqueeze(1), forced_valid, skill_valid)
        if not skill_valid.any(dim=-1).all():
            fallback_skill = torch.zeros_like(skill_valid)
            fallback_skill[:, 0] = valid_actions.any(dim=-1)
            skill_valid = torch.where(skill_valid.any(dim=-1, keepdim=True), skill_valid, fallback_skill)
        return skill_valid

    def _force_skill_logits(self, skill_logits: Tensor, forced_skill: Optional[Tensor], skill_valid: Tensor) -> Tensor:
        if forced_skill is None:
            return skill_logits
        forced = (forced_skill >= 0) & (forced_skill < self.cfg.skill_count)
        if not forced.any():
            return skill_logits
        forced_logits = skill_logits.new_full(skill_logits.shape, MASKED_LOGIT)
        safe = forced_skill.clamp(0, self.cfg.skill_count - 1)
        forced_logits.scatter_(1, safe.unsqueeze(1), 0.0)
        forced_logits = hard_mask_logits(forced_logits, skill_valid)
        return torch.where(forced.unsqueeze(1), forced_logits, skill_logits)

    def head_valid_mask(
        self,
        skill_action_mask: Tensor,
        action_head_mask: Tensor,
        valid_actions: Tensor,
        item_preferred_heads: Tensor,
    ) -> Tensor:
        affinity = self.skill_head_affinity.to(device=action_head_mask.device)
        route = (
            action_head_mask.unsqueeze(1)
            & affinity.view(1, self.cfg.skill_count, self.cfg.skill_head_count, 1)
            & skill_action_mask.unsqueeze(2)
            & valid_actions.unsqueeze(1).unsqueeze(2)
        )
        if SKILL_ITEM < self.cfg.skill_count:
            item_route = F.one_hot(
                item_preferred_heads.clamp(0, self.cfg.skill_head_count - 1),
                num_classes=self.cfg.skill_head_count,
            ).permute(0, 2, 1).to(dtype=torch.bool)
            route[:, SKILL_ITEM, :, :] = route[:, SKILL_ITEM, :, :] & item_route
        head_valid = route.any(dim=-1)
        if not head_valid.any(dim=-1).all():
            fallback = torch.zeros_like(head_valid)
            fallback[:, :, HEAD_GENERIC] = skill_action_mask.any(dim=-1)
            head_valid = torch.where(head_valid.any(dim=-1, keepdim=True), head_valid, fallback)
        return head_valid

    def route_mask(
        self,
        skill_action_mask: Tensor,
        action_head_mask: Tensor,
        valid_actions: Tensor,
        item_preferred_heads: Tensor,
    ) -> Tensor:
        affinity = self.skill_head_affinity.to(device=action_head_mask.device)
        route = (
            action_head_mask.unsqueeze(1)
            & affinity.view(1, self.cfg.skill_count, self.cfg.skill_head_count, 1)
            & skill_action_mask.unsqueeze(2)
            & valid_actions.unsqueeze(1).unsqueeze(2)
        )
        if SKILL_ITEM < self.cfg.skill_count:
            item_route = F.one_hot(
                item_preferred_heads.clamp(0, self.cfg.skill_head_count - 1),
                num_classes=self.cfg.skill_head_count,
            ).permute(0, 2, 1).to(dtype=torch.bool)
            route[:, SKILL_ITEM, :, :] = route[:, SKILL_ITEM, :, :] & item_route
        return route

    def _valid_actions(self, action_matrix: Tensor, action_mask: Optional[Tensor]) -> Tensor:
        valid = self._feature(action_matrix, 0) > 0
        if action_mask is not None:
            valid = valid & (action_mask > 0)
        return valid

    def _gather_rows(self, rows: Tensor, normalized_index: Tensor, scale: int) -> Tensor:
        if rows.shape[1] == 0:
            return torch.zeros(normalized_index.shape + (rows.shape[-1],), device=rows.device, dtype=rows.dtype)
        index = torch.round(normalized_index.clamp(0, 1) * float(scale)).long() - 1
        valid = (index >= 0) & (index < rows.shape[1])
        safe = index.clamp(0, rows.shape[1] - 1)
        gathered = rows.gather(1, safe.unsqueeze(-1).expand(-1, -1, rows.shape[-1]))
        return gathered * valid.unsqueeze(-1).to(dtype=rows.dtype)

    def _feature(self, matrix: Tensor, index: int) -> Tensor:
        if matrix.shape[-1] <= index:
            return matrix.new_zeros(matrix.shape[:-1])
        return matrix[..., index]

    def _scaled_index(self, matrix: Tensor, index: int, scale: int) -> Tensor:
        return torch.round(self._feature(matrix, index).clamp(0, 1) * float(scale)).long()

    def _put(self, mask: Tensor, head: int, values: Tensor) -> None:
        if 0 <= head < mask.shape[1]:
            mask[:, head, :] = mask[:, head, :] | values


class AgentMinActorCritic(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.cfg = cfg
        hidden = cfg.hidden_dim
        action_hidden = cfg.action_embed_dim

        self.map_cnn = MapCNN(cfg)
        self.global_map_cnn = GlobalMapCNN(cfg)
        self.talent_embedding_encoder = TalentEmbeddingEncoder(cfg)
        self.hero_encoder = nn.Sequential(
            nn.Linear(cfg.embedded_hero_dim, hidden),
            nn.GELU(),
            nn.LayerNorm(hidden),
            MLPBlock(hidden, cfg.dropout),
            MLPBlock(hidden, cfg.dropout),
        )
        self.inventory_embedding_encoder = InventoryEmbeddingEncoder(cfg)
        self.inventory_encoder = SetTransformerEncoder(cfg.embedded_inventory_features, cfg)
        self.inventory_summary_encoder = nn.Sequential(
            nn.Linear(cfg.inventory_summary_dim, hidden),
            nn.GELU(),
            nn.LayerNorm(hidden),
            MLPBlock(hidden, cfg.dropout),
        )
        self.option_encoder = nn.Sequential(
            nn.Linear(cfg.option_dim, hidden),
            nn.GELU(),
            nn.LayerNorm(hidden),
            MLPBlock(hidden, cfg.dropout),
        )
        self.mob_embedding_encoder = MobEmbeddingEncoder(cfg)
        self.mob_encoder = SetTransformerEncoder(cfg.embedded_mob_features, cfg)
        self.history_rnn = HistoryRNN(cfg)

        self.state_type_embedding = nn.Parameter(torch.zeros(8, hidden))
        state_layer = nn.TransformerEncoderLayer(
            d_model=hidden,
            nhead=cfg.attention_heads,
            dim_feedforward=hidden * cfg.transformer_ff_mult,
            dropout=cfg.dropout,
            activation="gelu",
            batch_first=True,
            norm_first=False,
        )
        self.state_transformer = nn.TransformerEncoder(state_layer, num_layers=cfg.state_transformer_layers)
        self.state_fusion = nn.Sequential(
            nn.LayerNorm(hidden),
            MLPBlock(hidden, cfg.dropout),
            MLPBlock(hidden, cfg.dropout),
        )

        self.action_encoder = nn.Sequential(
            nn.Linear(cfg.action_features, action_hidden),
            nn.GELU(),
            nn.LayerNorm(action_hidden),
            MLPBlock(action_hidden, cfg.dropout),
        )
        action_layer = nn.TransformerEncoderLayer(
            d_model=action_hidden,
            nhead=cfg.attention_heads,
            dim_feedforward=action_hidden * cfg.transformer_ff_mult,
            dropout=cfg.dropout,
            activation="gelu",
            batch_first=True,
            norm_first=False,
        )
        self.action_transformer = nn.TransformerEncoder(action_layer, num_layers=cfg.action_transformer_layers)
        self.state_to_query = nn.Linear(hidden, action_hidden)
        self.action_attention = nn.MultiheadAttention(
            embed_dim=action_hidden,
            num_heads=cfg.attention_heads,
            dropout=cfg.dropout,
            batch_first=True,
        )
        self.policy_head = nn.Sequential(
            nn.LayerNorm(action_hidden),
            nn.Linear(action_hidden, action_hidden),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(action_hidden, 1),
        )
        self.hierarchical_policy = HierarchicalActionPolicy(cfg)
        self.value_head = nn.Sequential(
            nn.LayerNorm(hidden),
            nn.Linear(hidden, hidden * 2),
            nn.GELU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(hidden * 2, hidden),
            nn.GELU(),
            nn.Linear(hidden, 1),
        )

        self._reset_parameters()

    def _reset_parameters(self) -> None:
        nn.init.normal_(self.state_type_embedding, std=0.02)

    def parameter_count(self) -> int:
        return sum(param.numel() for param in self.parameters())

    def encode_state(
        self,
        level_tensor: Tensor,
        explored_global_matrix: Tensor,
        agent_visited_matrix: Tensor,
        hero_vector: Tensor,
        inventory_matrix: Tensor,
        inventory_summary_vector: Tensor,
        option_vector: Tensor,
        mob_matrix: Tensor,
        history_matrix: Tensor,
    ) -> Tensor:
        if agent_visited_matrix.dim() == 3:
            agent_visited_matrix = agent_visited_matrix.unsqueeze(1)
        map_emb = self.map_cnn(torch.cat([level_tensor, agent_visited_matrix], dim=1))
        hero_features = self.talent_embedding_encoder(hero_vector)
        inventory_features = self.inventory_embedding_encoder(inventory_matrix)
        mob_features = self.mob_embedding_encoder(mob_matrix)
        tokens = torch.stack(
            [
                map_emb,
                self.global_map_cnn(explored_global_matrix),
                self.hero_encoder(hero_features),
                self.inventory_encoder(inventory_features),
                self.inventory_summary_encoder(inventory_summary_vector),
                self.option_encoder(option_vector),
                self.mob_encoder(mob_features),
                self.history_rnn(history_matrix),
            ],
            dim=1,
        )
        tokens = tokens + self.state_type_embedding.unsqueeze(0)
        tokens = self.state_transformer(tokens)
        return self.state_fusion(tokens.mean(dim=1))

    def forward(
        self,
        level_tensor: Tensor,
        explored_global_matrix: Tensor,
        agent_visited_matrix: Tensor,
        hero_vector: Tensor,
        inventory_matrix: Tensor,
        inventory_summary_vector: Tensor,
        option_vector: Tensor,
        mob_matrix: Tensor,
        history_matrix: Tensor,
        action_matrix: Tensor,
        action_mask: Optional[Tensor] = None,
        monitor_item_mask: Optional[Tensor] = None,
        monitor_cell_mask: Optional[Tensor] = None,
        monitor_option_mask: Optional[Tensor] = None,
        skill_mask: Optional[Tensor] = None,
        action_skill_mask: Optional[Tensor] = None,
        forced_skill: Optional[Tensor] = None,
    ) -> tuple[Tensor, Tensor]:
        state = self.encode_state(
            level_tensor=level_tensor,
            explored_global_matrix=explored_global_matrix,
            agent_visited_matrix=agent_visited_matrix,
            hero_vector=hero_vector,
            inventory_matrix=inventory_matrix,
            inventory_summary_vector=inventory_summary_vector,
            option_vector=option_vector,
            mob_matrix=mob_matrix,
            history_matrix=history_matrix,
        )
        logits = self.action_logits(
            state=state,
            action_matrix=action_matrix,
            action_mask=action_mask,
            skill_mask=skill_mask,
            action_skill_mask=action_skill_mask,
            forced_skill=forced_skill,
            hero_vector=hero_vector,
            inventory_matrix=inventory_matrix,
            mob_matrix=mob_matrix,
        )
        value = self.value_head(state).squeeze(-1)
        return logits, value

    def encode_action_tokens(self, state: Tensor, action_matrix: Tensor, action_mask: Optional[Tensor]) -> Tensor:
        action_emb = self.action_encoder(action_matrix)
        key_padding_mask = action_mask <= 0 if action_mask is not None else None
        if key_padding_mask is not None and key_padding_mask.any():
            all_invalid = key_padding_mask.all(dim=1)
            if all_invalid.any():
                key_padding_mask = key_padding_mask.clone()
                key_padding_mask[all_invalid, 0] = False
        action_emb = self.action_transformer(action_emb, src_key_padding_mask=key_padding_mask)
        query = self.state_to_query(state).unsqueeze(1)
        attended, _ = self.action_attention(query=query, key=action_emb, value=action_emb, key_padding_mask=key_padding_mask)
        return action_emb + attended

    def raw_action_logits(self, state: Tensor, action_matrix: Tensor, action_mask: Optional[Tensor]) -> Tensor:
        conditioned_actions = self.encode_action_tokens(state, action_matrix, action_mask)
        logits = self.policy_head(conditioned_actions).squeeze(-1)
        logits = logits + action_matrix[..., 13] * self.cfg.priority_logit_scale
        if action_mask is not None:
            logits = hard_mask_logits(logits, action_mask > 0)
        return logits

    def action_logits(
        self,
        state: Tensor,
        action_matrix: Tensor,
        action_mask: Optional[Tensor],
        skill_mask: Optional[Tensor],
        action_skill_mask: Optional[Tensor],
        forced_skill: Optional[Tensor] = None,
        hero_vector: Optional[Tensor] = None,
        inventory_matrix: Optional[Tensor] = None,
        mob_matrix: Optional[Tensor] = None,
    ) -> Tensor:
        action_tokens = self.encode_action_tokens(state, action_matrix, action_mask)
        if hero_vector is None or inventory_matrix is None or mob_matrix is None:
            return self.flat_action_logits(action_tokens, action_matrix, action_mask)

        _base, talent_slot_features, talent_slot_embeddings, talent_slot_present = (
            self.talent_embedding_encoder.slot_embeddings(hero_vector)
        )
        inventory_features = self.inventory_embedding_encoder(inventory_matrix)
        mob_features = self.mob_embedding_encoder(mob_matrix)
        return self.hierarchical_policy(
            state=state,
            action_tokens=action_tokens,
            action_matrix=action_matrix,
            action_mask=action_mask,
            skill_mask=skill_mask,
            action_skill_mask=action_skill_mask,
            forced_skill=forced_skill,
            inventory_features=inventory_features,
            mob_matrix=mob_features,
            talent_slot_features=talent_slot_features,
            talent_slot_embeddings=talent_slot_embeddings,
            talent_slot_present=talent_slot_present,
        )

    def flat_action_logits(self, action_tokens: Tensor, action_matrix: Tensor, action_mask: Optional[Tensor]) -> Tensor:
        logits = self.policy_head(action_tokens).squeeze(-1)
        logits = logits + action_matrix[..., ACTION_PRIORITY_FEATURE] * self.cfg.priority_logit_scale
        if action_mask is not None:
            logits = hard_mask_logits(logits, action_mask > 0)
        return logits

    def valid_skill_mask(
        self,
        skill_mask: Optional[Tensor],
        action_skill_mask: Optional[Tensor],
        action_mask: Optional[Tensor],
    ) -> Optional[Tensor]:
        if skill_mask is None or action_skill_mask is None:
            return None
        action_valid = action_skill_mask > 0
        if action_mask is not None:
            action_valid = action_valid & (action_mask.unsqueeze(1) > 0)
        skill_valid = (skill_mask > 0) & action_valid.any(dim=-1)
        if skill_valid.any(dim=-1).all():
            return skill_valid
        if action_mask is None:
            return None
        fallback = torch.zeros_like(skill_valid)
        fallback[:, 0] = action_mask.sum(dim=-1) > 0
        return torch.where(skill_valid.any(dim=-1, keepdim=True), skill_valid, fallback)

    def evaluate_actions(
        self,
        batch: dict[str, Tensor],
        actions: Tensor,
        skills: Optional[Tensor] = None,
        heads: Optional[Tensor] = None,
        talent_target_embeddings: Optional[Tensor] = None,
        talent_target_types: Optional[Tensor] = None,
        monitor_item_rows: Optional[Tensor] = None,
        monitor_cell_indices: Optional[Tensor] = None,
        monitor_option_indices: Optional[Tensor] = None,
    ) -> tuple[Tensor, Tensor, Tensor]:
        log_probs, entropy, logits, _values = self.evaluate_actions_with_value(
            batch,
            actions,
            skills,
            heads,
            talent_target_embeddings,
            talent_target_types,
            monitor_item_rows,
            monitor_cell_indices,
            monitor_option_indices,
        )
        return log_probs, entropy, logits

    def evaluate_actions_with_value(
        self,
        batch: dict[str, Tensor],
        actions: Tensor,
        skills: Optional[Tensor] = None,
        heads: Optional[Tensor] = None,
        talent_target_embeddings: Optional[Tensor] = None,
        talent_target_types: Optional[Tensor] = None,
        monitor_item_rows: Optional[Tensor] = None,
        monitor_cell_indices: Optional[Tensor] = None,
        monitor_option_indices: Optional[Tensor] = None,
    ) -> tuple[Tensor, Tensor, Tensor, Tensor]:
        state = self.encode_state(
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
        action_tokens = self.encode_action_tokens(state, batch["action_matrix"], batch.get("action_mask"))
        _base, talent_slot_features, talent_slot_embeddings, talent_slot_present = (
            self.talent_embedding_encoder.slot_embeddings(batch["hero_vector"])
        )
        inventory_features = self.inventory_embedding_encoder(batch["inventory_matrix"])
        mob_features = self.mob_embedding_encoder(batch["mob_matrix"])
        log_probs, entropy, logits = self.hierarchical_policy.evaluate_path(
            state=state,
            action_tokens=action_tokens,
            action_matrix=batch["action_matrix"],
            action_mask=batch.get("action_mask"),
            skill_mask=batch.get("skill_mask"),
            action_skill_mask=batch.get("action_skill_mask"),
            forced_skill=batch.get("forced_skill"),
            inventory_features=inventory_features,
            mob_matrix=mob_features,
            talent_slot_features=talent_slot_features,
            talent_slot_embeddings=talent_slot_embeddings,
            talent_slot_present=talent_slot_present,
            actions=actions,
            skills=skills,
            heads=heads,
            talent_target_embeddings=talent_target_embeddings,
            talent_target_types=talent_target_types,
            monitor_item_rows=monitor_item_rows,
            monitor_item_mask=batch.get("monitor_item_mask"),
            monitor_cell_indices=monitor_cell_indices,
            monitor_cell_mask=batch.get("monitor_cell_mask"),
            monitor_option_indices=monitor_option_indices,
            monitor_option_mask=batch.get("monitor_option_mask"),
        )
        return log_probs, entropy, logits, self.value_head(state).squeeze(-1)

    @torch.no_grad()
    def act(self, batch: dict[str, Tensor], deterministic: bool = False) -> dict[str, Tensor]:
        state = self.encode_state(
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
        value = self.value_head(state).squeeze(-1)
        action_tokens = self.encode_action_tokens(state, batch["action_matrix"], batch.get("action_mask"))
        _base, talent_slot_features, talent_slot_embeddings, talent_slot_present = (
            self.talent_embedding_encoder.slot_embeddings(batch["hero_vector"])
        )
        inventory_features = self.inventory_embedding_encoder(batch["inventory_matrix"])
        mob_features = self.mob_embedding_encoder(batch["mob_matrix"])
        out = self.hierarchical_policy.sample(
            state=state,
            action_tokens=action_tokens,
            action_matrix=batch["action_matrix"],
            action_mask=batch.get("action_mask"),
            skill_mask=batch.get("skill_mask"),
            action_skill_mask=batch.get("action_skill_mask"),
            forced_skill=batch.get("forced_skill"),
            inventory_features=inventory_features,
            mob_matrix=mob_features,
            talent_slot_features=talent_slot_features,
            talent_slot_embeddings=talent_slot_embeddings,
            talent_slot_present=talent_slot_present,
            monitor_item_mask=batch.get("monitor_item_mask"),
            monitor_cell_mask=batch.get("monitor_cell_mask"),
            monitor_option_mask=batch.get("monitor_option_mask"),
            deterministic=deterministic,
        )
        return {
            "action": out["action"],
            "skill": out["skill"],
            "head": out["head"],
            "log_prob": out["log_prob"],
            "entropy": out["entropy"],
            "value": value,
            "agent_hidden": out["agent_hidden"],
            "skill_hidden": out["skill_hidden"],
            "talent_target_embedding": out["talent_target_embedding"],
            "talent_target_type": out["talent_target_type"],
            "monitor_item_row": out["monitor_item_row"],
            "monitor_cell_index": out["monitor_cell_index"],
            "monitor_option_index": out["monitor_option_index"],
            "logits": out["logits"],
        }


def build_model(
    hero_vector_size: int,
    level_channels: int = 19,
    history_features: int = 16,
    hidden_dim: int = 192,
) -> AgentMinActorCritic:
    cfg = AgentMinModelConfig(
        level_channels=level_channels,
        hero_dim=hero_vector_size,
        talent_dim=0,
        history_features=history_features,
        hidden_dim=hidden_dim,
        action_embed_dim=hidden_dim,
    )
    return AgentMinActorCritic(cfg)
