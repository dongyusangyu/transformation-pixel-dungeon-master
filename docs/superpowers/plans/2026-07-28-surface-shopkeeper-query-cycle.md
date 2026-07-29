# 地表店主查询商品与限购周期实现计划

> 设计已由用户确认。使用测试驱动方式实现，并在完成前运行针对性测试与完整 `core:test`。

## 1. 先添加失败测试

- 在 `ScrollOfExtractionTest` 验证提取卷轴基础价值为 40。
- 在 `SurfaceShopkeeperTest` 验证提取卷轴和小包口粮可查询，而其他普通食物仍不可查询。
- 在 `TreasureHuntRecordsTest` 验证每 5 次有效搜打撤结算推进一次购买周期，并验证存档往返。
- 在 `SurfaceShopkeeperTest` 验证周期推进前维持限购、第五次后清空，以及店主周期随存档持久化。
- 在 `ExtractionRaidRunTest` 验证成功撤离和死亡计数，主动放弃不计数，并验证十步/无尽窗口不再记录限购周期。

## 2. 实现最小功能

- 为 `ScrollOfExtraction` 覆盖 `value()`。
- 将两个新增物品显式加入店主查询目录，并为小包口粮增加唯一类型例外。
- 为 `TreasureHuntRecords` 增加有效搜打撤结算次数、周期计算和 Bundle 持久化。
- 为 `SurfaceShopkeeper` 增加周期同步、计数清空、Bundle 持久化和当前楼层即时同步入口。
- 在 `ExtractionRaidRun.completeSuccess()` 记录成功撤离，在 `fail(..., true)` 记录死亡；主动放弃不记录。
- 从 `WndTreasureHunt` 移除所有商人限购周期计数。

## 3. 验证

- 运行三个相关测试类。
- 运行完整 `core:test`。
- 检查差异，确认没有覆盖工作区中的无关修改。
