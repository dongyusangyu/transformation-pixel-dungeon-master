from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

import torch
from torch import Tensor, nn
import torch.nn.functional as F


@dataclass(frozen=True)
class AgentMinModelConfig:
    level_channels: int = 19
    hero_dim: int = 32
    talent_dim: int = 0
    inventory_rows: int = 80
    inventory_features: int = 32
    inventory_summary_dim: int = 24
    option_dim: int = 8
    mob_rows: int = 32
    mob_features: int = 28
    history_rows: int = 16
    history_features: int = 16
    action_rows: int = 96
    action_features: int = 40
    hidden_dim: int = 352
    action_embed_dim: int = 352
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
        self.pool = nn.AdaptiveAvgPool2d((4, 4))
        self.proj = nn.Sequential(
            nn.Linear(256 * 16, cfg.hidden_dim),
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


class AgentMinActorCritic(nn.Module):
    def __init__(self, cfg: AgentMinModelConfig):
        super().__init__()
        self.cfg = cfg
        hidden = cfg.hidden_dim
        action_hidden = cfg.action_embed_dim

        self.map_cnn = MapCNN(cfg)
        self.global_map_cnn = GlobalMapCNN(cfg)
        self.hero_encoder = nn.Sequential(
            nn.Linear(cfg.full_hero_dim, hidden),
            nn.GELU(),
            nn.LayerNorm(hidden),
            MLPBlock(hidden, cfg.dropout),
            MLPBlock(hidden, cfg.dropout),
        )
        self.inventory_encoder = SetTransformerEncoder(cfg.inventory_features, cfg)
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
        self.mob_encoder = SetTransformerEncoder(cfg.mob_features, cfg)
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
        tokens = torch.stack(
            [
                map_emb,
                self.global_map_cnn(explored_global_matrix),
                self.hero_encoder(hero_vector),
                self.inventory_encoder(inventory_matrix),
                self.inventory_summary_encoder(inventory_summary_vector),
                self.option_encoder(option_vector),
                self.mob_encoder(mob_matrix),
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
        conditioned_actions = action_emb + attended
        logits = self.policy_head(conditioned_actions).squeeze(-1)
        logits = logits + action_matrix[..., 13] * self.cfg.priority_logit_scale
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
    level_channels: int = 19,
    history_features: int = 16,
    hidden_dim: int = 352,
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
