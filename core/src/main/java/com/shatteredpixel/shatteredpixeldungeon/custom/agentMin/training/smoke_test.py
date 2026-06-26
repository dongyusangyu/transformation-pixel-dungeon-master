from __future__ import annotations

import torch

from agent_min_model import AgentMinModelConfig, AgentMinActorCritic
from ppo_trainer import PPOConfig, PPOTrainer, RolloutBatch


def make_dummy_batch(batch_size: int = 8, height: int = 32, width: int = 32, device: str = "cpu") -> dict[str, torch.Tensor]:
    cfg = AgentMinModelConfig()
    action_mask = torch.ones(batch_size, cfg.action_rows, device=device)
    action_mask[:, 40:] = 0
    monitor_item_mask = torch.zeros(batch_size, cfg.inventory_rows, device=device)
    monitor_item_mask[:, :8] = 1
    monitor_cell_mask = torch.ones(batch_size, cfg.monitor_cell_count, device=device)
    monitor_option_mask = torch.zeros(batch_size, cfg.monitor_option_count, device=device)
    monitor_option_mask[:, :4] = 1
    skill_mask = torch.zeros(batch_size, cfg.skill_count, device=device)
    action_skill_mask = torch.zeros(batch_size, cfg.skill_count, cfg.action_rows, device=device)
    for action in range(40):
        skill = action % cfg.skill_count
        skill_mask[:, skill] = 1
        action_skill_mask[:, skill, action] = 1
    return {
        "level_tensor": torch.rand(batch_size, cfg.level_channels - 1, height, width, device=device),
        "explored_global_matrix": torch.rand(batch_size, 127, 127, device=device),
        "agent_visited_matrix": torch.rand(batch_size, height, width, device=device),
        "hero_vector": torch.rand(batch_size, cfg.full_hero_dim, device=device),
        "inventory_matrix": torch.rand(batch_size, cfg.inventory_rows, cfg.inventory_features, device=device),
        "inventory_summary_vector": torch.rand(batch_size, cfg.inventory_summary_dim, device=device),
        "option_vector": torch.rand(batch_size, cfg.option_dim, device=device),
        "mob_matrix": torch.rand(batch_size, cfg.mob_rows, cfg.mob_features, device=device),
        "history_matrix": torch.rand(batch_size, cfg.history_rows, cfg.history_features, device=device),
        "action_matrix": torch.rand(batch_size, cfg.action_rows, cfg.action_features, device=device),
        "action_mask": action_mask,
        "monitor_item_mask": monitor_item_mask,
        "monitor_cell_mask": monitor_cell_mask,
        "monitor_option_mask": monitor_option_mask,
        "skill_mask": skill_mask,
        "action_skill_mask": action_skill_mask,
        "forced_skill": torch.full((batch_size,), -1, dtype=torch.long, device=device),
    }


def main() -> None:
    device = "cuda" if torch.cuda.is_available() else "cpu"
    cfg = AgentMinModelConfig()
    model = AgentMinActorCritic(cfg).to(device)
    batch = make_dummy_batch(device=device)

    logits, value = model(**batch)
    assert logits.shape == (8, cfg.action_rows), logits.shape
    assert value.shape == (8,), value.shape
    action_out = model.act(batch)
    assert action_out["action"].shape == (8,), action_out["action"].shape

    rollout = RolloutBatch(
        **batch,
        actions=action_out["action"],
        skills=action_out["skill"],
        heads=action_out["head"],
        talent_target_embeddings=action_out["talent_target_embedding"],
        talent_target_types=action_out["talent_target_type"],
        monitor_item_rows=action_out["monitor_item_row"],
        monitor_cell_indices=action_out["monitor_cell_index"],
        monitor_option_indices=action_out["monitor_option_index"],
        old_log_probs=action_out["log_prob"].detach(),
        returns=torch.randn(8, device=device),
        advantages=torch.randn(8, device=device),
    )
    trainer = PPOTrainer(model, PPOConfig(epochs=1, minibatch_size=4))
    metrics = trainer.update(rollout)
    print("AgentMin smoke test OK")
    print({key: round(value, 5) for key, value in metrics.items()})


if __name__ == "__main__":
    main()
