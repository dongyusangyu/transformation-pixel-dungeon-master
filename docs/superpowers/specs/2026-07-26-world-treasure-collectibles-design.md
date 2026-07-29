# 世界文明藏品道具设计规格

## 目标

在 `items.treasures` 包内实现 20 件世界文明藏品。每件藏品继承
`Treasures`，使用 `ex_items.png` 的第 0–19 格图标，拥有固定的藏品
稀有度与价格，并在实例创建时独立随机、随后持久化一个 `Rarity` 品质。

## 两套概念必须分离

- `Treasures.Rarity` 表示物品品质，只控制藏品实例的品质文字和背包格
  背景。生成概率沿用现有规则：传奇 0.1%、史诗 1%、稀有 10%、普通
  88.9%。品质在构造时生成，并通过 Bundle 保存和恢复。
- `Treasures.CollectionRarity` 表示藏品类别的固定稀有度，取值为
  `TOP`、`RARE`、`UNCOMMON`、`COMMON`。它由具体 Java 类决定，不随机，
  不随读档改变。
- `value()` 只取决于具体藏品的固定价格，不乘以随机品质系数。

物品描述末尾同时显示“藏品稀有度”和“品质”，避免玩家混淆。

## 藏品、图标、固定稀有度与价格

| 格 | Java 类 | 中文名称 | 固定稀有度 | value |
|---:|---|---|---|---:|
| 0 | `MuiscaGoldenRaft` | 穆伊斯卡黄金祭筏 | TOP | 3000 |
| 1 | `ImperialCrown` | 神圣罗马帝国皇冠 | RARE | 1600 |
| 2 | `PakalJadeMask` | 帕卡尔翡翠面具 | RARE | 1500 |
| 3 | `SuttonHooHelmet` | 萨顿胡龙纹头盔 | RARE | 1400 |
| 4 | `BookOfKells` | 《凯尔经》 | UNCOMMON | 950 |
| 5 | `CholaNataraja` | 朱罗王朝舞王湿婆像 | UNCOMMON | 900 |
| 6 | `DojigiriYasutsuna` | 童子切安纲 | UNCOMMON | 850 |
| 7 | `TurquoiseSerpent` | 阿兹特克双头绿松石蛇 | UNCOMMON | 800 |
| 8 | `RuWareBowl` | 汝窑天青莲花式温碗 | UNCOMMON | 750 |
| 9 | `IncaGoldenLlama` | 印加金羊驼像 | UNCOMMON | 700 |
| 10 | `LewisChessQueen` | 刘易斯棋子王后 | COMMON | 550 |
| 11 | `HarbavilleTriptych` | 哈巴维尔象牙三联圣像 | COMMON | 520 |
| 12 | `AlMughiraPyxis` | 穆吉拉王子象牙盒 | COMMON | 490 |
| 13 | `BlacasEwer` | 布拉卡斯嵌银执壶 | COMMON | 460 |
| 14 | `GreatKhanPaiza` | 大汗八思巴文牌符 | COMMON | 430 |
| 15 | `GoryeoMaebyeong` | 高丽青瓷梅瓶 | COMMON | 400 |
| 16 | `JavaneseGoldCup` | 爪哇金叶纹杯 | COMMON | 370 |
| 17 | `EthiopianProcessionalCross` | 拉斯塔巡游十字架 | COMMON | 340 |
| 18 | `GreatZimbabweBird` | 大津巴布韦皂石鸟 | COMMON | 310 |
| 19 | `DjenneTerracottaFigure` | 杰内沉思者陶像 | COMMON | 280 |

## 藏品生成器

新增 `TreasureGenerator`，不把藏品注册进普通 `Generator` 掉落池。

1. 先掷一次 `[0, 1)` 浮点数选择固定稀有度组：
   - `[0.00, 0.01)`：TOP，1%
   - `[0.01, 0.05)`：RARE，4%
   - `[0.05, 0.15)`：UNCOMMON，10%
   - `[0.15, 1.00)`：COMMON，85%
2. 在选中的组内等概率选择一个具体类。
3. 反射创建该类；构造函数独立生成一次 `Treasures.Rarity` 品质。

边界判断集中在可测试的包级方法中，生产入口为
`TreasureGenerator.random()`。

## 扩展贴图读取

`EXItemSpriteSheet` 为第 0–19 格提供具名常量。由于原
`ItemSpriteSheet.film` 的前 20 帧带有针对 `items.png` 的不同裁剪尺寸，
扩展贴图必须绕过这些裁剪数据，按 16×16 完整格计算 UV，否则藏品会被截断。
`ItemSprite.frame()` 根据编码后的图片编号选择正确纹理，并为 EX 图标应用完整格 UV。

## 文本

默认英文资源提供兜底名称和描述，中文资源提供正式文本。每段中文描述
用一至两句话交代藏品的来源地区或文明、大致时代和最显著的材质或造型
特点。描述不宣称游戏内复制品等同于现实文物原件。

## 验证

- JUnit 验证 20 个类的继承关系、图标编号、固定稀有度和价格。
- JUnit 验证 1%/4%/10%/85% 的边界以及各组成员数量。
- JUnit 验证 EX 图标的 16×16 完整格坐标。
- JUnit 验证默认和中文资源均包含 20 组 `name`、`desc` 键。
- 执行 `:core:test` 聚焦测试、`:core:compileJava` 和 `git diff --check`。
