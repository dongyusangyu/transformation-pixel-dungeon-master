# 登山包设计规格

## 1. 目标

新增独立背包道具“登山包”。它是主背包的最低优先级容量扩展：所有更合适的专用背包和主背包都无法接收物品时，物品才进入登山包；高优先级容器重新出现空间后，登山包中的物品自动归位。

登山包由地牢第 20 层最终的小恶魔商人出售，容量与常规专用背包一致，拥有独立背包页签、物品文本和日志条目。

## 2. 已确认数值与表现

- Java 类名：`HikingBackpack`。
- 中文名称：`登山包`。
- 容量：19 格。
- 基础价值：40。
- 第 20 层标准购买价格：1000 金币，继续使用现有 `Shopkeeper.sellPrice` 公式。
- 贴图：复用 `items.png` 中水袋后一格的背包贴图，即现有 `ItemSpriteSheet.BACKPACK`。
- 不修改 `items.png`，不新增精灵帧。
- 道具不可升级、不可堆叠，并沿用 `Bag` 的唯一物品规则。

## 3. 收纳范围

登山包可收纳所有非 `Bag` 类型的物品，包括装备、消耗品、任务物品、唯一物品和其他可进入玩家物品栏的道具。

登山包不能收纳主背包或其他专用背包。背包必须继续作为主背包的直接子项存在，否则专用收纳、页签展示、所有者绑定和递归遍历会产生不一致。

登山包继续遵守 `Bag.canHold` 的通用限制：

- 遗失背包状态下，不得绕过 `LostInventory` 存取限制。
- 已有相似堆叠且来源标识一致时，即使目标容器没有空格，也允许合并。
- 容量已满且不能合并时拒绝收纳。

## 4. 收纳优先级

从地面拾取、购买、炼金产出或其他正常 `Item.collect` 路径进入英雄主背包体系时，使用以下固定顺序：

1. 能接收该物品的非溢出专用背包，例如种子袋、卷轴筒、药剂挎带或魔法筒袋。
2. 主背包的直接物品栏。
3. 登山包。

登山包被定义为 `fallback storage`。`Item.collect(Bag)` 在遍历子背包时先跳过 fallback storage，尝试当前容器后，最后才尝试 fallback storage。非英雄背包和不存在登山包的存档保持原有行为。

收纳顺序同时适用于可合并堆叠。若主背包或专用背包能接收新物品，新物品不得仅因登山包中已有相似堆叠而优先进入登山包；随后的再平衡会把登山包旧堆叠迁往更高优先级位置并按既有相似性规则合并。

## 5. 自动再平衡

主背包负责执行一次幂等的 fallback storage 再平衡。每次再平衡按 `Item.itemComparator` 的稳定顺序扫描登山包内容，并为每件物品执行：

1. 尝试所有可接收它的非溢出专用背包。
2. 尝试主背包直接物品栏。
3. 两者都失败时保留在登山包。

再平衡在以下事件后触发：

- 英雄购买或获得登山包。
- 英雄获得新的专用背包。
- 主背包中的整件或整组物品被取出，产生空格。
- 专用背包中的整件或整组物品被取出，产生空格。
- 物品被装备、丢弃、售卖或消耗完毕，并通过现有 `detachAll` 路径释放格位。
- 英雄物品栏完整读档恢复结束。

再平衡不得在背包 Bundle 尚未恢复完毕、背包没有英雄所有者或英雄处于 `LostInventory` 限制期间执行。

主背包维护非持久化的重入保护。再平衡内部执行的 `detachAll`、`collect` 和堆叠合并不会再次启动嵌套再平衡，避免无限递归和重复移动。

迁移必须保留：

- 原始物品实例或合并后的合法实例。
- 数量、等级、诅咒、鉴定、投掷武器集合 ID 和提取来源标识。
- 快捷栏中的物品引用或占位符。
- 专用背包已有的收纳副作用，例如魔法筒袋中的法杖充能和投掷武器耐久标记。

## 6. 背包页签顺序

`Belongings.getBags()` 返回稳定、显式的背包顺序，`WndBag` 继续直接使用该列表创建页签：

1. 主背包 `Belongings.Backpack`。
2. 登山包 `HikingBackpack`。
3. 绒布袋（种子带）`VelvetPouch`。
4. 卷轴筒 `ScrollHolder`。
5. 药剂挎带 `PotionBandolier`。
6. 魔法筒袋 `MagicalHolster`。

未来新增且未显式登记顺序的背包追加在上述已知背包之后。列表不得重复返回嵌套遍历中遇到的同一背包实例。

因此登山包按钮始终位于主背包按钮之后、种子袋按钮之前，普通查看与带 `ItemSelector` 的选择窗口保持一致。

## 7. 商店生成与唯一性

登山包不加入 `ShopRoom.ChooseBag`，因此不会出现在第 6、11、16 层普通商店，也不会挤占原有四种专用背包的随机出售位置。

`ImpShopRoom` 在生成第 20 层最终小恶魔商店库存时额外加入一个登山包。生成条件为：

- `Dungeon.LimitedDrops.HIKING_BACKPACK` 尚未标记；
- 英雄物品栏中不存在 `HikingBackpack`。

库存生成时写入 limited-drop 标记，确保读档、房间重复初始化和商店恢复不会复制商品。小恶魔任务未完成、最终商店未出现时，该局不能购买登山包，这与现有最终小恶魔商店规则一致。

旧存档缺少 `HIKING_BACKPACK` Bundle 字段时由 `LimitedDrops.restore` 自动恢复为 0，不需要存档迁移版本分支。

## 8. 日志、徽章与文本

`HikingBackpack.class` 加入 `Catalog.MISC_EQUIPMENT`，位置在 `MagicalHolster.class` 之后、`Amulet.class` 之前。拾取后由现有 `Catalog.setSeen` 与 `Statistics.itemTypesDiscovered` 逻辑解锁。

本功能不修改现有“购齐全部背包”徽章。该徽章仍按绒布袋、卷轴筒、药剂挎带和魔法筒袋四种旧背包判定，避免改变旧存档已经获得的徽章语义。

中文文本：

```properties
items.bags.hikingbackpack.name=登山包
items.bags.hikingbackpack.desc=这只结实宽大的背包原本用于长途攀登，层叠的绑带与暗袋足以收纳各种冒险用品。\n\n当主背包与其他专用容器都无处存放物品时，它会接纳多出的物品；一旦更合适的容器腾出空间，物品便会自动归位。
```

英文回退文本：

```properties
items.bags.hikingbackpack.name=hiking backpack
items.bags.hikingbackpack.desc=This rugged, roomy backpack was made for long climbs, with layered straps and hidden pockets that can carry almost any adventuring supply.\n\nIt stores items only when your main backpack and other containers have no room. As soon as a more suitable container has space, those items are moved back automatically.
```

其他语言缺少对应键时沿用项目现有英文回退机制，本次不批量生成机械翻译。

## 9. 文件职责

- `items/bags/HikingBackpack.java`：登山包属性、容量、价值、收纳范围和 fallback storage 标识。
- `items/bags/Bag.java`：通用 fallback storage 契约、空位产生后的再平衡入口及已有 `grabItems` 协作。
- `items/Item.java`：正常收集流程中将 fallback storage 延迟到当前容器之后。
- `actors/hero/Belongings.java`：主背包再平衡、读档完成触发和稳定页签顺序。
- `levels/rooms/standard/ImpShopRoom.java`：第 20 层最终商店稳定追加登山包。
- `Dungeon.java`：登山包 limited-drop 状态。
- `journal/Catalog.java`：杂项装备图鉴登记。
- `messages/items/items.properties` 与 `items_zh.properties`：英文回退及中文文本。

`WndBag.java` 不承担排序逻辑，只验证它继续按 `Belongings.getBags()` 返回顺序创建按钮。

## 10. 测试要求

### 收纳与容量

- 登山包容量为 19，基础价值为 40，贴图索引为 `ItemSpriteSheet.BACKPACK`。
- 任意非背包物品可进入登山包，任何 `Bag` 子类不能进入登山包。
- 专用背包可接收时，物品不进入主背包或登山包。
- 专用背包已满而主背包有空位时，物品进入主背包。
- 专用背包和主背包都已满时，物品进入登山包。
- 三者都无法接收时，拾取失败且物品保持在原位置。

### 再平衡

- 专用背包释放格位后，对应类型物品从登山包迁入专用背包。
- 主背包释放格位后，没有合适专用背包的物品从登山包迁入主背包。
- 新获得专用背包时，登山包中的对应物品立即迁入。
- 可合并堆叠在目标背包已满时仍能归位，不产生数量变化或重复实例。
- 再平衡不会递归卡死，连续调用结果相同。
- 快捷栏引用、投掷武器集合信息与魔法筒袋副作用在迁移后正确。
- Bundle 往返后内容完整，并在完整恢复结束后归一到正确容器。

### 商店与 UI

- 普通 `ShopRoom.ChooseBag` 永远不返回登山包。
- `ImpShopRoom` 首次库存恰好包含一个登山包。
- limited-drop 已标记或英雄已持有时不再生成。
- 登山包售价按第 20 层公式为 1000 金币。
- `Belongings.getBags()` 页签顺序固定为主背包、登山包、绒布袋（种子带）、卷轴筒、药剂挎带、魔法筒袋。
- 没有登山包时，原有背包顺序和窗口行为不变。

## 11. 验收标准

玩家在第 20 层最终小恶魔商店购买登山包后，可以在背包窗口中通过主背包后的第二个按钮打开它。正常拾取不会提前把物品塞入登山包；只有专用背包与主背包均不能接收时才使用其 19 格容量。任何更高优先级容器出现空间后，相关物品无需玩家操作即可安全归位。保存、读档、快捷栏、堆叠、旧存档和原有四种背包行为均无回归。
