# AgentMin 强化学习模型原型

本目录是 AgentMin 的第一版深度强化学习主体，训练环境建议使用：

```powershell
D:\anaconda\envs\wy\python.exe
```

当前 `wy` 环境已经检测到 PyTorch 与 CUDA 可用。

## 文件说明

- `agent_min_model.py`
  - `MapCNN`：编码 `levelTensor`，用于处理地图/视野/地形通道。
  - `HistoryRNN`：用 GRU 编码 `historyMatrix`，用于保留最近行为记忆。
  - `RowSetEncoder`：对背包、怪物列表做逐行编码和注意力池化。
  - `AgentMinActorCritic`：Actor-Critic 主体，用多头注意力将状态向量与候选动作矩阵对齐并输出动作 logits。

- `ppo_trainer.py`
  - PPO 更新主体。
  - 包含 `RolloutBatch`、`PPOTrainer`、`compute_gae`。

- `smoke_test.py`
  - 使用随机张量模拟 Java 侧 `AgentMinEncodedState`。
  - 验证模型前向、动作采样和一次 PPO 更新是否能运行。

## 当前输入约定

模型默认接收以下张量：

```text
level_tensor      [B, 18, H, W]
hero_vector       [B, hero_dim]
inventory_matrix  [B, 80, 32]
mob_matrix        [B, 32, 20]
history_matrix    [B, 16, 16]
action_matrix     [B, 96, 24]
action_mask       [B, 96]
```

其中 `action_mask` 为 1 的位置表示合法候选动作，为 0 的位置会被屏蔽。

## 自检命令

在项目根目录执行：

```powershell
D:\anaconda\envs\wy\python.exe core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\custom\agentMin\training\smoke_test.py
```

看到 `AgentMin smoke test OK` 即代表模型主体可以运行。

## 后续对接建议

下一步需要做 Java 到 Python 的样本桥接：

1. Java 侧每步调用 `AgentMinStateBuilder.captureEncoded()`。
2. 将 `AgentMinEncodedState` 写成 JSON、MessagePack、NPZ 或本地 socket 消息。
3. Python 侧读取状态和动作空间，调用模型选择动作。
4. Java 侧执行动作，并用 `AgentMinRewardTracker.consumePendingReward()` 读取奖励。
5. Python 侧累积 rollout，用 PPO 更新模型。

当前目录只负责模型主体和训练更新核心，不直接启动游戏，也不直接控制角色。
