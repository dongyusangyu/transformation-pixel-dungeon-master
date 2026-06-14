from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

import torch
from torch import Tensor, nn
import torch.nn.functional as F


@dataclass(frozen=True)
class AgentMinModelConfig:
    level_channels: int = 18
    hero_dim: int = 28
    talent_dim: int = 0
    inventory_rows: int = 80
    inventory_features: int = 32
    mob_rows: int = 32
    mob_features: int = 20
    history_rows: int = 16
    history_features: int = 16
    action_rows: int = 96
    action_features: int = 24
    hidden_dim: int = 256
    action_embed_dim: int = 128
    attention_heads: int = 4
    dropout: float = 0.1

    @property
    def full_hero_dim(self) -> int:
        return self.hero_dim + self.talent_dim


class MapCNN(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.net = nn.Sequential(
            nn.Conv2d(cfg.level_channels, 48, kernel_size=3, padding=1),
            nn.GroupNorm(8, 48),
            nn.SiLU(),
            nn.Conv2d(48, 96, kernel_size=3, padding=1),
            nn.GroupNorm(8, 96),
            nn.SiLU(),
            nn.Conv2d(96, 128, kernel_size=3, padding=1),
            nn.GroupNorm(8, 128),
            nn.SiLU(),
        )
        self.pool = nn.AdaptiveAvgPool2d((1, 1))
        self.proj = nn.Linear(128, cfg.hidden_dim)

    def forward(self, level_tensor: Tensor) -> Tensor:
        x = self.net(level_tensor)
        x = self.pool(x).flatten(1)
        return self.proj(x)


class RowSetEncoder(nn.Module):
    def __init__(self, in_dim: int, hidden_dim: int, dropout: float):
        super().__init__()
        self.row = nn.Sequential(
            nn.Linear(in_dim, hidden_dim),
            nn.SiLU(),
            nn.Dropout(dropout),
            nn.Linear(hidden_dim, hidden_dim),
            nn.SiLU(),
        )
        self.attn_score = nn.Linear(hidden_dim, 1)

    def forward(self, matrix: Tensor) -> Tensor:
        row_emb = self.row(matrix)
        present = matrix.abs().sum(dim=-1) > 0
        scores = self.attn_score(row_emb).squeeze(-1)
        scores = scores.masked_fill(~present, -1e9)
        all_empty = ~present.any(dim=1)
        weights = torch.softmax(scores, dim=1).unsqueeze(-1)
        pooled = (row_emb * weights).sum(dim=1)
        pooled = torch.where(all_empty.unsqueeze(-1), torch.zeros_like(pooled), pooled)
        return pooled


class HistoryRNN(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.input = nn.Linear(cfg.history_features, cfg.hidden_dim // 2)
        self.gru = nn.GRU(
            input_size=cfg.hidden_dim // 2,
            hidden_size=cfg.hidden_dim // 2,
            num_layers=1,
            batch_first=True,
        )
        self.out = nn.Linear(cfg.hidden_dim // 2, cfg.hidden_dim)

    def forward(self, history_matrix: Tensor) -> Tensor:
        x = F.silu(self.input(history_matrix))
        _, h = self.gru(x)
        return self.out(h[-1])


class AgentMinActorCritic(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.cfg = cfg
        self.map_cnn = MapCNN(cfg)
        self.hero_encoder = nn.Sequential(
            nn.Linear(cfg.full_hero_dim, cfg.hidden_dim),
            nn.SiLU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(cfg.hidden_dim, cfg.hidden_dim),
        )
        self.inventory_encoder = RowSetEncoder(cfg.inventory_features, cfg.hidden_dim, cfg.dropout)
        self.mob_encoder = RowSetEncoder(cfg.mob_features, cfg.hidden_dim, cfg.dropout)
        self.history_rnn = HistoryRNN(cfg)

        self.state_fusion = nn.Sequential(
            nn.Linear(cfg.hidden_dim * 5, cfg.hidden_dim),
            nn.SiLU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(cfg.hidden_dim, cfg.hidden_dim),
        )

        self.action_encoder = nn.Sequential(
            nn.Linear(cfg.action_features, cfg.action_embed_dim),
            nn.SiLU(),
            nn.Dropout(cfg.dropout),
            nn.Linear(cfg.action_embed_dim, cfg.action_embed_dim),
        )
        self.state_to_query = nn.Linear(cfg.hidden_dim, cfg.action_embed_dim)
        self.action_attention = nn.MultiheadAttention(
            embed_dim=cfg.action_embed_dim,
            num_heads=cfg.attention_heads,
            dropout=cfg.dropout,
            batch_first=True,
        )
        self.policy_head = nn.Linear(cfg.action_embed_dim, 1)
        self.value_head = nn.Sequential(
            nn.Linear(cfg.hidden_dim, cfg.hidden_dim),
            nn.SiLU(),
            nn.Linear(cfg.hidden_dim, 1),
        )

    def encode_state(
        self,
        level_tensor: Tensor,
        hero_vector: Tensor,
        inventory_matrix: Tensor,
        mob_matrix: Tensor,
        history_matrix: Tensor,
    ) -> Tensor:
        map_emb = self.map_cnn(level_tensor)
        hero_emb = self.hero_encoder(hero_vector)
        inv_emb = self.inventory_encoder(inventory_matrix)
        mob_emb = self.mob_encoder(mob_matrix)
        hist_emb = self.history_rnn(history_matrix)
        return self.state_fusion(torch.cat([map_emb, hero_emb, inv_emb, mob_emb, hist_emb], dim=-1))

    def forward(
        self,
        level_tensor: Tensor,
        hero_vector: Tensor,
        inventory_matrix: Tensor,
        mob_matrix: Tensor,
        history_matrix: Tensor,
        action_matrix: Tensor,
        action_mask: Optional[Tensor] = None,
    ) -> tuple[Tensor, Tensor]:
        state = self.encode_state(
            level_tensor=level_tensor,
            hero_vector=hero_vector,
            inventory_matrix=inventory_matrix,
            mob_matrix=mob_matrix,
            history_matrix=history_matrix,
        )
        action_emb = self.action_encoder(action_matrix)
        query = self.state_to_query(state).unsqueeze(1)
        attended, _ = self.action_attention(query=query, key=action_emb, value=action_emb)
        conditioned_actions = action_emb + attended
        logits = self.policy_head(conditioned_actions).squeeze(-1)
        if action_mask is not None:
            logits = logits.masked_fill(action_mask <= 0, -1e9)
        value = self.value_head(state).squeeze(-1)
        return logits, value

    @torch.no_grad()
    def act(self, batch: dict[str, Tensor], deterministic: bool = False) -> dict[str, Tensor]:
        logits, value = self.forward(**batch)
        dist = torch.distributions.Categorical(logits=logits)
        action = logits.argmax(dim=-1) if deterministic else dist.sample()
        return {
            "action": action,
            "log_prob": dist.log_prob(action),
            "entropy": dist.entropy(),
            "value": value,
        }


def build_model(
    hero_vector_size: int,
    level_channels: int = 18,
    history_features: int = 16,
    hidden_dim: int = 256,
) -> AgentMinActorCritic:
    cfg = AgentMinModelConfig(
        level_channels=level_channels,
        hero_dim=hero_vector_size,
        talent_dim=0,
        history_features=history_features,
        hidden_dim=hidden_dim,
    )
    return AgentMinActorCritic(cfg)
