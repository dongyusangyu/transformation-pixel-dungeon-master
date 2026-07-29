# EXItemSpriteSheet 可变尺寸编码设计

## 目标

将 `EXItemSpriteSheet.encode` 从只编码素材索引的 `encode(image)` 改为同时编码
索引和显示尺寸的 `encode(image, w, h)`，使扩展物品贴图像
`ItemSpriteSheet.assignItemRect(item, width, height)` 一样，只截取每个 16×16
单元格中实际需要的左上区域。

本次不移动、缩放或重绘 `ex_items.png` 中的像素，只修改编码和运行时 UV 截取规则。

## 坐标与尺寸规则

- 扩展图集继续按每行 16 格、每格 16×16 定位。
- `image` 决定单元格左上角：
  - `x = image % 16 * 16`
  - `y = image / 16 * 16`
- UV 右下角改为：
  - `right = x + w`
  - `bottom = y + h`
- 不裁掉左侧或顶部透明像素，不改变素材原有对齐。
- 对已经绘制的素材，`w`、`h` 分别取最右、最下非透明像素坐标加一。
- 合法范围：
  - `image`：0–65535
  - `w`：1–16
  - `h`：1–16

## 方案 A：单个 int 位打包

继续让物品的 `image` 字段保持 `int`，在一个整数中保存全部元数据：

- 位 0–15：素材索引 `image`
- 位 16：扩展图集标志
- 位 17–21：宽度 `w`
- 位 22–26：高度 `h`
- 位 27–31：保留

辅助方法：

- `isEX(encoded)`：检查位 16。
- `frameFor(encoded)`：扩展编码返回低 16 位索引；普通素材索引原样返回。
- `frameWidth(encoded)`：读取位 17–21。
- `frameHeight(encoded)`：读取位 22–26。
- `frameX/ frameY`：继续根据 `frameFor` 和固定 16×16 网格计算。

该布局不会改变普通 `ItemSpriteSheet` 索引，也不需要运行时 Map。

## 素材尺寸表

尺寸通过读取当前 `ex_items.png` 的 alpha 通道得到：

| 索引 | 常量 | w | h |
|---:|---|---:|---:|
| 0 | `MUISCA_GOLDEN_RAFT` | 14 | 14 |
| 1 | `IMPERIAL_CROWN` | 12 | 14 |
| 2 | `PAKAL_JADE_MASK` | 15 | 16 |
| 3 | `SUTTON_HOO_HELMET` | 15 | 16 |
| 4 | `BOOK_OF_KELLS` | 14 | 16 |
| 5 | `CHOLA_NATARAJA` | 14 | 16 |
| 6 | `DOJIGIRI_YASUTSUNA` | 15 | 16 |
| 7 | `TURQUOISE_SERPENT` | 16 | 12 |
| 8 | `RU_WARE_BOWL` | 16 | 13 |
| 9 | `INCA_GOLDEN_LLAMA` | 14 | 15 |
| 10 | `LEWIS_CHESS_QUEEN` | 13 | 16 |
| 11 | `HARBAVILLE_TRIPTYCH` | 15 | 16 |
| 12 | `AL_MUGHIRA_PYXIS` | 11 | 16 |
| 13 | `BLACAS_EWER` | 16 | 16 |
| 14 | `GREAT_KHAN_PAIZA` | 11 | 16 |
| 15 | `GORYEO_MAEBYEONG` | 11 | 16 |
| 16 | `JAVANESE_GOLD_CUP` | 15 | 14 |
| 17 | `ETHIOPIAN_PROCESSIONAL_CROSS` | 16 | 16 |
| 18 | `GREAT_ZIMBABWE_BIRD` | 11 | 16 |
| 19 | `DJENNE_TERRACOTTA_FIGURE` | 11 | 16 |
| 32 | `SCROLL_EXTRACTION` | 15 | 14 |
| 48 | `META_INFUSE` | 10 | 15 |
| 50 | `SEAL` | 16 | 16 |

`SEAL` 对应的索引 50 在当前扩展贴图中完全透明，无法从像素识别尺寸。为避免
未来补图时被错误裁切，本次保留 16×16。

## ItemSprite 集成

`ItemSprite.frame(int image)` 的扩展贴图分支改为：

1. 根据编码索引计算 16×16 单元格左上角。
2. 分别读取编码中的 `w`、`h`。
3. 使用 `(left, top, left+w, top+h)` 创建 UV。
4. 使用编码高度 `h` 计算短物品的透视抬升。

普通 `ItemSpriteSheet` 分支保持原有 `TextureFilm` 行为。

`ItemSprite.pick` 仍按 16×16 图集网格定位像素，不改变现有拾取逻辑。

## 兼容性

- 所有 `EXItemSpriteSheet` 常量统一重新编码，不保留旧的 `encode(image)` 重载，
  防止后续新增素材遗漏尺寸。
- 物品类仍只引用整数常量，无需修改 20 件藏品、提取卷轴或 META_INFUSE 的调用。
- 普通 `ItemSpriteSheet` 索引不包含扩展标志位，行为不变。
- 不修改 `ex_items.png`。

## 测试与验收

先以 TDD 添加失败测试，再实现：

1. `META_INFUSE` 解码为索引 48、宽 10、高 15。
2. 代表性横向、纵向和满格素材返回正确尺寸。
3. 22 个已绘制素材及 `SEAL` 的编码尺寸与上表完全一致。
4. 普通 `ItemSpriteSheet.SEAL` 不被识别为扩展素材。
5. 扩展常量仍选择 `Assets.Sprites.EX_ITEMS`。
6. `ItemSprite.frame` 使用独立的编码宽高生成 UV，不再固定使用 16×16。
7. 聚焦单元测试通过，`core` 生产代码重新编译成功。

