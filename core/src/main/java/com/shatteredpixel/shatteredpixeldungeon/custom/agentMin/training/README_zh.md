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
level_tensor      [B, 19, H, W]
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

## 平台测试命令

`agent_min_platform.py` 是当前的最小强化学习平台层，包含日志、观测校验、模型推理、rollout 收集和 PPO 更新。

```powershell
D:\anaconda\envs\wy\python.exe core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\custom\agentMin\training\agent_min_platform.py --steps 24
```

运行时会在控制台逐步输出：

```text
step=00 action=04 reward=-0.020 total=-0.020 hp=1.00 done=False valid_actions=8
```

同时会写入日志文件：

```text
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/agentMin/training/logs/platform_test.log
```

当前平台测试使用 `MockDungeonAdapter` 模拟游戏端编码输出，用来验证模型和训练平台是否能正常跑通。

## 真实游戏桥接训练

`agent_min_bridge_server.py` 是 Java 游戏端与 Python 训练端之间的实时 socket 桥接层。Java 侧会在英雄进入可行动状态时捕获 `AgentMinEncodedState`，发送给 Python；Python 侧用模型选择动作并返回动作编号；Java 侧执行动作，同时奖励箱会把本步奖励累积进下一次样本。

先在项目根目录启动 Python 桥接服务：

```powershell
D:\anaconda\envs\wy\python.exe core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\custom\agentMin\training\agent_min_bridge_server.py --host 127.0.0.1 --port 8765 --update-interval 32 --device auto
```

再打开另一个 PowerShell，从项目根目录启动桌面版游戏并开启 AgentMin：

```powershell
$env:AGENTMIN_ENABLED="true"
$env:AGENTMIN_HOST="127.0.0.1"
$env:AGENTMIN_PORT="8765"
.\gradlew.bat desktop:debug
```

也可以使用 JVM 参数开启：

```powershell
.\gradlew.bat -Dagentmin.enabled=true -Dagentmin.host=127.0.0.1 -Dagentmin.port=8765 desktop:debug
```

项目根目录的一键脚本默认使用 `-Device auto`，会在 CUDA 可用时使用 GPU，否则回退到 CPU：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File D:\STUDY\Dungeon\transformation-pixel-dungeon-master\run_agentmin_training.ps1
```

脚本会持续监控当前训练局。英雄死亡时，Java 侧会写入死亡标记文件，PowerShell 检测到后会关闭当前桌面游戏进程，并自动重新启动一个新的桌面游戏进程继续训练；Python 桥接服务和 checkpoint 会保持运行。

如果只想短时间测试监控循环，可以指定秒数：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File D:\STUDY\Dungeon\transformation-pixel-dungeon-master\run_agentmin_training.ps1 -MonitorSeconds 120
```

如需强制指定设备：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File D:\STUDY\Dungeon\transformation-pixel-dungeon-master\run_agentmin_training.ps1 -Device cuda
powershell.exe -NoProfile -ExecutionPolicy Bypass -File D:\STUDY\Dungeon\transformation-pixel-dungeon-master\run_agentmin_training.ps1 -Device cpu
```

桥接服务运行时会持续输出真实游戏样本日志，并写入：

```text
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/agentMin/training/logs/bridge_server.log
```

当前桥接已经支持状态编码、动作掩码、移动、等待、攻击可见敌人、拾取、上下楼、简单饮用治疗药水和投掷武器。法杖动作暂时只进入候选动作和日志，不直接执行完整施法链，避免绕过游戏原有的目标选择、动画和鉴定流程。
