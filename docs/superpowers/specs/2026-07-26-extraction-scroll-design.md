# 提取卷轴设计规格

## 目标

新增一种固定名称、固定贴图且不参与普通卷轴匿名系统的“提取卷轴”。使用后打开背包物品选择界面，仅允许选择记录过升级卷轴或相关永久升级制品使用次数的可升级物品。提取会返还相同数量的升级卷轴，并移除对应的装备等级与计数。

## 像素贴图

- 精灵表：`core/src/main/assets/sprites/ex_items.png`
- 帧尺寸：`16x16`
- 目标位置：第 3 行第 1 格，零基帧索引 `32`，像素原点 `(0, 32)`
- 基底：逐像素复制 `items.png` 的 `ItemSpriteSheet.SCROLL_META`
- `SCROLL_META` 的帧索引为 `303`，像素区域为 `(240, 288, 16, 16)`
- 中心符号采用已确认的 B“晶核上浮”
- 新符号严格限制在原符文的 `5x5` 区域，不覆盖卷轴纸张轮廓、卷边或阴影
- 不缩放、不抗锯齿，保持 RGBA PNG 和原精灵表 `256x512` 尺寸

`EXItemSpriteSheet` 新增公开常量：

```java
public static final int SCROLL_EXTRACTION = encode(32);
```

## 道具结构

新增：

```text
items.scrolls.ScrollOfExtraction
```

该类继承 `InventoryScroll`，以复用现有卷轴阅读、消耗、背包选择器、取消确认、阅读动画和卷轴天赋流程。

### 固定识别规则

- `anonymous` 保持默认值 `false`
- 覆盖 `isKnown()` 并固定返回 `true`
- 不加入 `Generator.Category.SCROLL.classes`
- 不加入普通卷轴符文 `ItemStatusHandler`
- 覆盖 `reset()`：调用父类后强制恢复 `EXItemSpriteSheet.SCROLL_EXTRACTION`

这样既不会显示随机符文或未知卷轴名称，也不会被当作匿名效果卷轴；正常阅读仍会触发阅读动画、卷轴使用统计和卷轴相关天赋。

## 背包筛选

`usableOnItem(Item item)` 必须同时满足：

```java
item.isUpgradable() && item.upgradeScrollUses > 0
```

因此没有记录升级制品次数的物品不可选择。法杖、戒指、武器、护甲等只要符合现有 `isUpgradable()` 规则即可使用，不额外依赖具体装备继承层级。

## 提取算法

选中物品后按以下顺序处理：

1. 保存 `extracted = item.upgradeScrollUses`
2. 保存物品当前真实升级等级 `currentLevel = max(0, item.trueLevel())`
3. 执行 `item.degrade(min(currentLevel, extracted))`
4. 将 `item.upgradeScrollUses` 设置为 `0`
5. 创建数量为 `extracted` 的 `ScrollOfUpgrade`
6. 优先收进英雄背包；背包无法容纳时，在英雄当前位置掉落整叠卷轴
7. 刷新快捷栏并显示成功日志

等级降低以 `trueLevel()` 为准，不受诅咒灌注等额外等级或临时增减益影响。即使记录次数高于当前真实等级，也仍返还全部已记录的升级卷轴，但物品等级最低只降到 `0`。

提取后必须清零 `upgradeScrollUses`，这是防止同一件装备重复生成升级卷轴的安全边界。

## 文本

至少补充英文与简体中文：

- `name`
- `desc`
- `inv_title`
- `warning`
- `yes`
- `no`
- `extract`

描述需要明确：

- 只能选择使用过升级卷轴或相应永久升级制品的物品
- 返还数量等于该物品记录的 `upgradeScrollUses`
- 物品会降低对应等级但不低于 `0`
- 提取后记录被清空

## 测试

新增定向测试覆盖：

- `EXItemSpriteSheet.SCROLL_EXTRACTION` 解码为 EX 精灵表帧 `32`
- 提取卷轴保持非匿名且固定已知
- `upgradeScrollUses == 0` 的物品不可选择
- 不可升级物品即使计数大于 `0` 也不可选择
- 可升级且计数大于 `0` 的物品可选择
- 等级高于计数时，降低计数对应的等级
- 等级低于计数时，最低降到 `0`，但返还完整计数
- 提取后 `upgradeScrollUses == 0`
- 诅咒灌注附加等级不作为可降低的真实等级
- 正式 PNG 保持 `256x512`，目标帧以外像素不发生变化

## 非目标

- 不加入普通地牢随机卷轴掉落池
- 不新增匿名符文、卷轴鉴定配对或对应符石
- 不改变现有升级卷轴、注魔菱晶和诅咒菱晶的计数规则
- 不改变 `upgradeScrollUses` 的存档与嬗变继承逻辑
