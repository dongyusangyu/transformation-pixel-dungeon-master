# 东隅桑榆 NPC 设计规格

## 目标

新增特殊 NPC `Dongyusangyu`。中文名称为“东隅桑榆”，介绍为“一只平平无奇的橙色哈尼”，使用现有 `DongyusangyuSprite`，并固定出现在 0 层地表小镇坐标 `(22, 26)`。

## 行为

- 继承现有 `NPC`，保持中立、被动且不会主动行动。
- 添加 `Property.IMMOVABLE`，不响应位移。
- `defenseSkill` 始终返回 `INFINITE_EVASION`。
- `damage` 忽略任何伤害；`add(Buff)` 返回 `false`，拒绝所有 Buff 形式的影响。
- 玩家交互时 NPC 转向玩家并说：“我是一直平平无奇的橙色哈尼”。
- 非玩家角色触发交互时不产生玩家对话逻辑。

## 生成

`SurfaceTownLevel.createMobs()` 创建且只创建一个 `Dongyusangyu`，将其位置设为 `cell(22, 26)` 后加入关卡 `mobs` 集合。0 层由 `Dungeon.newLevel()` 映射为 `SurfaceTownLevel`，因此该 NPC 不进入其他楼层的随机生成池。

## 本地化

在默认英文资源中提供回退文本，在中文资源中提供用户指定的精确文本：

- `name`: 东隅桑榆
- `desc`: 一只平平无奇的橙色哈尼
- `hello`: 我是一直平平无奇的橙色哈尼

## 验证

- 单元测试验证贴图类、不可移动、无限闪避、拒绝 Buff 和完全免伤。
- 关卡测试验证地表小镇固定生成一个 NPC，且坐标为 `(22, 26)`。
- 运行 core 测试与编译，确认资源键及 Java 集成没有回归。
