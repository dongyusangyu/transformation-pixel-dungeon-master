# 中式宗门大殿室内环境图集实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 生成一套完整、可重复构建且兼容 `DungeonTileSheet` 256 槽位语义的中式宗门大殿室内图集，并通过自动像素检查与人工拼接预览验证其合理性。

**架构：** 新增独立 Pillow 生成器，以项目现有图集为结构模板，按槽位绘制灰砖、暗木、深朱漆、青铜和玉色材质。自动测试负责尺寸、掩模、确定性、文字 API 禁用、对象分层与边缘拼接；临时预览只用于人工识图验证并在结束前删除。

**技术栈：** Python 3、Pillow、`unittest`、PowerShell、项目现有 PNG 图集和 `DungeonTileSheet.java`

---

## 前置约束

- 设计依据：`docs/superpowers/specs/2026-07-28-chinese-sect-hall-tileset-design.md`
- 不修改 `DungeonTileSheet.java` 的索引或拼接算法。
- 不接入 Java 区域枚举、楼层选择和资源加载逻辑。
- 不在成品图集中绘制汉字、伪文字或调用任何文字 API。
- 不覆盖或回退工作区中已有的无关改动。
- 只使用最近邻缩放，不使用抗锯齿。
- 每完成一项任务，更新本计划对应复选框并运行该任务的验证。

## 任务 1：建立生成器骨架和失败测试

**文件：**

- 创建：`tools/generate_chinese_hall_tileset.py`
- 创建：`tools/test_generate_chinese_hall_tileset.py`
- 参考：`tools/generate_lush_surface_tileset.py`
- 参考：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/tiles/DungeonTileSheet.java`

- [ ] 在测试文件中定义输出路径、参考图路径和 16×16 槽位裁剪辅助函数。
- [ ] 先编写失败测试，要求生成器导出以下公开接口：
  - `tile_rect(index)`
  - `build_tileset()`
  - `build_water_texture()`
  - `generate()`
- [ ] 添加输出规格测试：
  - 主图必须为 `256×256 RGBA`。
  - 水纹必须为 `32×32 RGBA`。
  - 所有索引必须落在 `0..255`。
- [ ] 添加生成器源码约束测试，拒绝 `ImageFont`、`.text(`、双线性和双三次缩放。
- [ ] 运行测试，确认因接口或输出尚未实现而失败。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B -m unittest tools.test_generate_chinese_hall_tileset -v
```

预期：测试失败，错误明确指向缺失接口或成品。

- [ ] 实现最小生成器骨架、路径常量、色板常量、固定种子和透明画布，使输出规格测试通过。
- [ ] 保持尚未绘制的语义测试失败，确认测试确实能约束后续工作。

## 任务 2：绘制基础地面、楼梯、井和深渊

**文件：**

- 修改：`tools/generate_chinese_hall_tileset.py`
- 修改：`tools/test_generate_chinese_hall_tileset.py`
- 参考：`core/src/main/assets/environment/tiles_sewers.png`
- 参考：`core/src/main/assets/environment/tiles_city.png`

- [ ] 为索引 0–23 添加失败测试，检查：
  - `FLOOR`、`FLOOR_ALT_1`、`FLOOR_ALT_2` 都是灰砖且彼此不完全相同。
  - `FLOOR_DECO` 与 `FLOOR_DECO_ALT` 是暗红礼道或地毯。
  - `FLOOR_SP` 与 `FLOOR_SP_ALT` 是暗木板。
  - `ENTRANCE`、`EXIT`、`ENTRANCE_SP` 与参考楼梯具有相近的非背景轮廓范围。
  - `WELL`、`EMPTY_WELL`、`PEDESTAL` 的背景分别匹配所在灰砖地面。
- [ ] 添加 5×5 平铺边缘测试，要求灰砖和木板的左右、上下边缘颜色差异不形成高对比度整线。
- [ ] 实现基础地面绘制函数：
  - 灰砖使用低对比度砖缝、少量裂纹和磨亮像素。
  - 暗红礼道使用抽象回纹或铜钉，不形成文字。
  - 木板使用横向接缝和少量木结。
- [ ] 从 `tiles_sewers.png` 提取楼梯轮廓或结构掩模，再用新区域材质重绘，不直接复制原地板。
- [ ] 绘制室内砌石井、空井和低矮供案。
- [ ] 为索引 24–31 添加失败测试，检查 `CHASM` 及灰砖、木板、墙、水四类上缘槽位有不同的连接边。
- [ ] 实现深渊和四类上缘拼接。
- [ ] 运行任务测试直至通过。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B -m unittest tools.test_generate_chinese_hall_tileset.ChineseHallGroundTest -v
```

## 任务 3：实现独立水纹和第三行 16 向过渡

**文件：**

- 修改：`tools/generate_chinese_hall_tileset.py`
- 修改：`tools/test_generate_chinese_hall_tileset.py`
- 生成：`core/src/main/assets/environment/water_chinese_hall.png`
- 参考：`core/src/main/assets/environment/tiles_sewers.png`
- 参考：`core/src/main/assets/environment/water0.png`

- [ ] 添加失败测试，逐个检查索引 32–47 对应 `+1 上、+2 右、+4 下、+8 左`。
- [ ] 测试每个过渡槽：
  - 水侧像素来自蓝黑水色集合。
  - 地面侧像素来自灰砖颜色集合。
  - 轮廓内外不会同时变成灰砖。
  - 方向掩模与 `tiles_sewers.png` 的第三行结构一致。
- [ ] 测试 `water_chinese_hall.png` 与主图第三行水侧的主色距离在允许阈值内。
- [ ] 实现 32×32 蓝黑室内映水纹理，使用稀疏水平反光和固定种子。
- [ ] 使用参考图第三行作为方向掩模模板，只替换水侧颜色和地面侧颜色，不改变透明区域及轮廓位置。
- [ ] 检查 4×4 水纹循环时上下左右接缝不出现单色直线。
- [ ] 运行任务测试直至通过。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B -m unittest tools.test_generate_chinese_hall_tileset.ChineseHallWaterTest -v
```

## 任务 4：绘制平视墙体、书架和门

**文件：**

- 修改：`tools/generate_chinese_hall_tileset.py`
- 修改：`tools/test_generate_chinese_hall_tileset.py`
- 参考：`core/src/main/assets/environment/tiles_prison.png`
- 参考：`core/src/main/assets/environment/tiles_halls.png`
- 参考：`core/src/main/assets/environment/tiles_city.png`

- [ ] 为索引 48–63 添加失败测试，要求普通墙、装饰墙、书架和 ALT 版本具有稳定材质身份。
- [ ] 添加门识别测试：
  - 关闭门中心被门叶占据。
  - 打开门中心为可通行暗区或地面。
  - 上锁门同时包含门叶、横闩和铜锁高光。
  - 特殊门包含玉绿色封印材质但不含文字状笔画。
  - 锁定出口与未锁出口在 1 倍轮廓上可区分。
- [ ] 实现灰砖墙基、深木横梁、暗朱柱和低对比度几何雕板。
- [ ] 实现武器架与卷轴柜；卷轴标签只能是无笔画的空白色块。
- [ ] 实现暗朱双扇门、打开门、铜闩锁门和玉封特殊门。
- [ ] 运行任务测试直至通过。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B -m unittest tools.test_generate_chinese_hall_tileset.ChineseHallFlatWallTest -v
```

## 任务 5：绘制平视与抬高功能物件

**文件：**

- 修改：`tools/generate_chinese_hall_tileset.py`
- 修改：`tools/test_generate_chinese_hall_tileset.py`
- 参考：`core/src/main/assets/environment/tiles_surface_lush.png`

- [ ] 为索引 64–79 和 120–143 添加失败测试，检查每个功能物件的背景属于灰砖或木地板，而不是参考区域背景。
- [ ] 实现以下物件的平视和主体层：
  - 青铜药鼎。
  - 堆叠座椅、屏风和木料路障。
  - 可破坏高屏风或竹帘。
  - 对应的木片、竹片和布片碎片。
  - 灰砖背景石狮与木台背景石狮。
  - 大型暗朱堂鼓主体。
  - 青铜鼎式香炉主体。
  - 玉石簇和三种变体。
  - 坍落砖石和三种变体。
- [ ] 添加形象差异测试，保证堂鼓、鼎、石狮、屏风和药鼎的前景掩模不相同。
- [ ] 添加 1 倍轮廓边界测试，避免对象主体仅由零散小点构成。
- [ ] 运行任务测试直至通过。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B -m unittest tools.test_generate_chinese_hall_tileset.ChineseHallObjectTest -v
```

## 任务 6：完成抬高墙、内墙、门楣和 overhang

**文件：**

- 修改：`tools/generate_chinese_hall_tileset.py`
- 修改：`tools/test_generate_chinese_hall_tileset.py`
- 参考：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/tiles/DungeonTileSheet.java`
- 参考：`core/src/main/assets/environment/tiles_prison.png`

- [ ] 为索引 80–119 添加失败测试，覆盖普通墙、装饰墙、门后墙、书架墙的左右开边组合，以及五类抬高门槽。
- [ ] 实现抬高墙体下层和抬高门体下层，确保柱、砖缝、门框在相邻槽位连续。
- [ ] 为索引 144–191 添加失败测试，枚举内墙的 16 向开边位掩码，检查每一位都能改变对应方向轮廓。
- [ ] 实现普通、装饰和木制书架三组内墙上层。
- [ ] 为索引 192–231 添加失败测试，覆盖墙 overhang、四类横向门 overhang、纵向门楣、横向门体、锁门、玉封门和出口 underhang。
- [ ] 实现墙、门和出口上下层补片。
- [ ] 为索引 232–255 添加主体与 overhang 对齐测试：
  - 堂鼓中心轴和连接宽度一致。
  - 铜鼎的鼎腹、鼎耳和鼎盖连续。
  - 石狮、屏风、药鼎和路障无悬浮。
  - 需要 underhang 的碎片槽位与主体底边相连。
- [ ] 实现其余功能物件的 overhang/underhang。
- [ ] 运行任务测试直至通过。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B -m unittest tools.test_generate_chinese_hall_tileset.ChineseHallLayeringTest -v
```

## 任务 7：生成成品并进行自动完整性验证

**文件：**

- 生成：`core/src/main/assets/environment/tiles_chinese_hall.png`
- 生成：`core/src/main/assets/environment/water_chinese_hall.png`
- 修改：`tools/test_generate_chinese_hall_tileset.py`

- [ ] 增加确定性测试：连续生成两次并比较两个文件的 SHA-256。
- [ ] 增加槽位覆盖测试：所有有语义的槽位必须非空，允许透明的层也必须由白名单明确列出。
- [ ] 增加色板和透明度检查，避免意外出现大量平滑渐变色或半透明抗锯齿边缘。
- [ ] 增加源文件检查，确认未使用字体、文字 API 和非最近邻缩放。
- [ ] 运行生成器写出两张成品。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B tools/generate_chinese_hall_tileset.py
D:\anaconda\envs\wy\python.exe -B -m unittest tools.test_generate_chinese_hall_tileset -v
```

- [ ] 使用 Pillow 输出图像尺寸、模式、非透明像素数和 SHA-256，记录结果。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B -c "from pathlib import Path; from PIL import Image; import hashlib; files=[Path(r'core/src/main/assets/environment/tiles_chinese_hall.png'),Path(r'core/src/main/assets/environment/water_chinese_hall.png')]; [(print(p, Image.open(p).size, Image.open(p).mode, hashlib.sha256(p.read_bytes()).hexdigest())) for p in files]"
```

## 任务 8：构建临时预览并进行识图检查

**文件：**

- 临时创建：`C:\Users\Administrator\.codex\visualizations\2026\06\27\019f0957-6cbf-7853-90cf-f02003e4968b\chinese-hall-tileset-check\atlas-1x.png`
- 临时创建：`C:\Users\Administrator\.codex\visualizations\2026\06\27\019f0957-6cbf-7853-90cf-f02003e4968b\chinese-hall-tileset-check\atlas-4x.png`
- 临时创建：`C:\Users\Administrator\.codex\visualizations\2026\06\27\019f0957-6cbf-7853-90cf-f02003e4968b\chinese-hall-tileset-check\seams.png`
- 临时创建：`C:\Users\Administrator\.codex\visualizations\2026\06\27\019f0957-6cbf-7853-90cf-f02003e4968b\chinese-hall-tileset-check\room-composite.png`

- [ ] 使用 Pillow 和最近邻缩放生成 1 倍图集、4 倍图集、材质平铺图和议事厅组合图。
- [ ] 使用 `view_image` 检查四张图，逐项确认：
  - 画面读作中式宗门大殿，而非日式室内。
  - 不存在汉字、伪文字或文字状装饰。
  - 灰砖、木板、红毯和水纹没有明显横纵接缝。
  - 开门、关门、锁门、玉封门和楼梯方向清楚。
  - 堂鼓、铜鼎、石狮、屏风和药鼎主体与 overhang 合理。
  - 红、铜和玉色只作为受控重点，不形成单色画面。
- [ ] 如发现问题，回到对应生成函数修改，并重跑完整测试与四张预览。
- [ ] 视觉检查通过后删除整个临时预览目录。
- [ ] 检查仓库内没有新增预览、中间 PNG、缓存或 `__pycache__`。

## 任务 9：最终验证和差异审查

**文件：**

- 检查：`tools/generate_chinese_hall_tileset.py`
- 检查：`tools/test_generate_chinese_hall_tileset.py`
- 检查：`core/src/main/assets/environment/tiles_chinese_hall.png`
- 检查：`core/src/main/assets/environment/water_chinese_hall.png`

- [ ] 运行完整生成器测试。
- [ ] 运行 Python 语法编译检查。
- [ ] 运行差异格式检查。
- [ ] 只查看本计划涉及文件的状态和差异，不处理无关工作区改动。

运行：

```powershell
D:\anaconda\envs\wy\python.exe -B -m unittest tools.test_generate_chinese_hall_tileset -v
D:\anaconda\envs\wy\python.exe -B -m py_compile tools/generate_chinese_hall_tileset.py tools/test_generate_chinese_hall_tileset.py
git diff --check -- tools/generate_chinese_hall_tileset.py tools/test_generate_chinese_hall_tileset.py core/src/main/assets/environment/tiles_chinese_hall.png core/src/main/assets/environment/water_chinese_hall.png
git status --short -- tools/generate_chinese_hall_tileset.py tools/test_generate_chinese_hall_tileset.py core/src/main/assets/environment/tiles_chinese_hall.png core/src/main/assets/environment/water_chinese_hall.png
```

- [ ] 最终报告列出生成文件、测试数量、SHA-256、人工检查结果和未执行的 Java 接入工作。
- [ ] 不声称图集已接入游戏区域，因为该工作不在本计划范围内。

