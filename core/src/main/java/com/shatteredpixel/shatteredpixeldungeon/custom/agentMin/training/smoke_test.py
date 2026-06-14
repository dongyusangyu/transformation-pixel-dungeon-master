from __future__ import annotations

import torch

from agent_min_model import AgentMinModelConfig, AgentMinActorCritic
from ppo_trainer import PPOConfig, PPOTrainer, RolloutBatch


def make_dummy_batch(batch_size: int = 8, height: int = 32, width: int = 32, device: str = "cpu") -> dict[str, torch.Tensor]:
    cfg = AgentMinModelConfig()
    action_mask = torch.ones(batch_size, cfg.action_rows, device=device)
    action_mask[:, 40:] = 0
    return {
        "level_tensor": torch.rand(batch_size, cfg.level_channels, height, width, device=device),
        "hero_vector": torch.rand(batch_size, cfg.full_hero_dim, device=device),
        "inventory_matrix": torch.rand(batch_size, cfg.inventory_rows, cfg.inventory_features, device=device),
        "mob_matrix": torch.rand(batch_size, cfg.mob_rows, cfg.mob_features, device=device),
        "history_matrix": torch.rand(batch_size, cfg.history_rows, cfg.history_features, device=device),
        "action_matrix": torch.rand(batch_size, cfg.action_rows, cfg.action_features, device=device),
        "action_mask": action_mask,
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
