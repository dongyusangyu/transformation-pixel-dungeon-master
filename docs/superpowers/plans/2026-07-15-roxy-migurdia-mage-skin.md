# 洛琪希法师皮肤实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在 `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin` 中生成一套七行完全相同、符合 Pixel Dungeon 法师动作协议的洛琪希皮肤及完整验证产物。

**架构：** 使用技能脚手架创建与游戏项目隔离的目录；把官方/可核查参考和图像模型生成的非像素动作参考作为身份与运动证据；用一个确定性 Pillow 生成器从 12x15 空白帧绘制第 0 行，再复制为 7 个护甲行，并生成预览与 56 个 GIF。单元测试与技能审计分别验证用户规则和通用协议。

**技术栈：** Python 3.12、Pillow、NumPy、imageio、`unittest`、Pixel Dungeon Hero Skin 技能脚本、内置 image generation。

---

## 文件结构

实现只创建下列皮肤目录文件；游戏项目仅新增本计划文档，不修改资源或 Java：

- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\generate_roxy_migurdia_mage_skin.py`：绘制帧、拼装贴图、生成预览和 GIF。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\test_generate_roxy_migurdia_mage_skin.py`：协议、身份、七行一致性、动作和输出测试。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\mage4.png`：最终槽位贴图。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\mage4_roxy_migurdia.png`：字节相同的可读别名。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\reference\`：下载的正面、侧面/三视图、动作参考和生成动作概念图。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\verification\`：动作总览、身份锚点、帽子、跑步、正面动作、护甲一致性和 1x 对比图。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\gif\tier_0_no_armor` 至 `tier_6_class_armor`：每行 8 个 GIF。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\design.md`：可执行设计契约。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\motion_reference_prompt.md`：动作参考完整提示词。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\reference_sources.json`：参考 URL、文件名、日期和采用事实。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\style_notes.json`：颜色、视角和护甲策略。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\motion_audit.json`：生成器计算的动作签名。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\mage_tier0_all_tiers_template.png`：从原版 `mage.png` 第 0 行派生、重复七行的用户例外审计模板。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\source_derivation.json`：记录审计模板的来源路径、源文件 SHA-256 和派生算法。
- `D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\audit.json`：技能审计结果。

### 任务 1：创建隔离目录并锁定项目来源

**文件：**
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\skin_manifest.json`
- 读取：`D:\STUDY\Dungeon\transformation-pixel-dungeon-master\core\src\main\assets\sprites\mage.png`
- 读取：`D:\STUDY\Dungeon\transformation-pixel-dungeon-master\core\src\main\assets\sprites\mage1.png`
- 读取：`D:\STUDY\Dungeon\transformation-pixel-dungeon-master\core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\sprites\HeroSprite.java`

- [ ] **步骤 1：确认目标目录尚不存在**

运行：

```powershell
Test-Path 'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin'
```

预期：输出 `False`；若为 `True`，停止而不覆盖。

- [ ] **步骤 2：运行技能脚手架**

运行：

```powershell
python 'D:\STUDY\Dungeon\skin\skills\pixel-dungeon-hero-skin\scripts\scaffold_skin.py' roxy_migurdia_mage_skin --hero mage --slot mage4.png --root 'D:\STUDY\Dungeon\skin' --project-root 'D:\STUDY\Dungeon\transformation-pixel-dungeon-master'
```

预期：退出码 0，清单中的 `hero` 为 `mage`、`slot` 为 `mage4.png`、`source_sprite` 指向项目的 `mage.png`。

- [ ] **步骤 3：复核动作协议来源**

运行：

```powershell
rg -n -C 3 'idle.frames|run.frames|die.frames|attack.frames|operate.frames|fly.frames|read.frames' 'D:\STUDY\Dungeon\transformation-pixel-dungeon-master\core\src\main\java\com\shatteredpixel\shatteredpixeldungeon\sprites\HeroSprite.java'
```

预期：帧映射分别为 `0/1`、`2..7`、`8..12`、`13..15`、`16/17`、`18`、`19/20`。

### 任务 2：持久化身份证据和动作参考

**文件：**
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\reference\roxy_official_front.png`
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\reference\roxy_character_sheet_side.png`
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\reference\roxy_official_action.png`
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\reference\roxy_two_head_motion_reference.png`
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\motion_reference_prompt.md`
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\reference_sources.json`

- [ ] **步骤 1：下载三类外部参考**

优先从 `https://mushokutensei.jp/character/` 及其官方静态资源保存正面和动作宣传图；侧面/三视图不足时使用带明确 Studio Bind/官方设定归属的角色设定图。每张图片下载后用 Pillow `Image.verify()` 检查，拒绝 HTML 错误页。

预期：三个文件均可被 Pillow 打开，且分别覆盖正面、侧面/三视图和动作。

- [ ] **步骤 2：写入动作参考提示词**

提示词必须原样保存并用于内置图像模型：

```text
Use case: stylized-concept
Asset type: character motion reference sheet for a 12x15 game-sprite animator
Primary request: Create one original non-pixel-art motion reference sheet of Roxy Migurdia from Mushoku Tensei, drawn as a clean two-head-tall chibi anime character.
Subject: youthful adult female mage with bright blue hair, straight fringe, long blue braid, wide-brimmed pointed black wizard hat, ochre-tan cloak, white collar, dark dress and pale boots.
Composition/framing: pure white canvas, eight separated full-body poses with generous spacing: three distinct running poses, three distinct empty-handed attack or spellcasting poses, one front-facing neutral pose, and one right-facing side neutral pose.
Motion: connected readable limbs, opposite arm and leg swing, clear foot contact, cloak and braid inertia one phase behind the torso, stable hat brim.
Constraints: every hand visibly empty; no staff, weapon, wand, book, scroll, bag, text, labels, watermark, pixel art, sprite sheet grid, magnified game frame, or 8x pixel-art enlargement. Preserve the same costume and identity in all poses.
```

预期：`rules/motion_reference_prompt.md` 包含上述完整文本，无省略。

- [ ] **步骤 3：生成并保存非像素动作参考**

使用内置 image generation 生成单张图；把选中输出保存为 `figure/reference/roxy_two_head_motion_reference.png`，然后视觉检查八个姿势、空手、肢体连接、服装一致性和白底分隔。

预期：至少三种跑姿、三种空手攻击/施法姿、一正面和一右侧面；没有任何手持物，且不是像素画。

- [ ] **步骤 4：记录来源事实**

`reference_sources.json` 使用以下结构并填入实际最终 URL：

```json
{
  "checked_date": "2026-07-15",
  "sources": [
    {"kind": "front", "url": "https://mushokutensei.jp/character/", "file": "roxy_official_front.png", "facts": ["bright blue hair", "black wide-brim pointed hat", "ochre-tan cloak"]},
    {"kind": "side_sheet", "url": "https://anime.reactor.cc/post/5253568", "file": "roxy_character_sheet_side.png", "facts": ["long braid", "side hat silhouette", "cloak length"]},
    {"kind": "motion", "url": "https://www.zerochan.net/4190503", "file": "roxy_official_action.png", "facts": ["braid trail", "cloak inertia", "leg articulation"]},
    {"kind": "generated_motion", "url": null, "file": "roxy_two_head_motion_reference.png", "facts": ["three run phases", "three empty-hand attack phases", "front and side proportions"]}
  ]
}
```

预期：所有外部 URL 可追溯，四个文件存在。

### 任务 3：先写失败测试

**文件：**
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\test_generate_roxy_migurdia_mage_skin.py`
- 尚不存在：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\generate_roxy_migurdia_mage_skin.py`

- [ ] **步骤 1：写入导入和基础契约测试**

测试文件先定义绝对项目/皮肤路径，并导入尚不存在的生成器：

```python
import importlib.util
import json
import unittest
from pathlib import Path
from PIL import Image, ImageChops

ROOT = Path(r"D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin")
PROJECT = Path(r"D:\STUDY\Dungeon\transformation-pixel-dungeon-master")
GEN = ROOT / "code" / "generate_roxy_migurdia_mage_skin.py"
spec = importlib.util.spec_from_file_location("roxy_gen", GEN)
roxy_gen = importlib.util.module_from_spec(spec)
spec.loader.exec_module(roxy_gen)

class RoxySkinTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        roxy_gen.generate_all()
        cls.sheet = Image.open(ROOT / "figure" / "mage4.png").convert("RGBA")

    def test_sheet_contract(self):
        self.assertEqual(self.sheet.size, (256, 128))
        self.assertEqual(self.sheet.mode, "RGBA")
        self.assertIsNone(self.sheet.crop((252, 0, 256, 128)).getbbox())
        self.assertIsNone(self.sheet.crop((0, 105, 256, 128)).getbbox())

    def test_all_tiers_are_pixel_identical(self):
        base = self.sheet.crop((0, 0, 252, 15))
        for tier in range(1, 7):
            row = self.sheet.crop((0, tier * 15, 252, tier * 15 + 15))
            self.assertIsNone(ImageChops.difference(base, row).getbbox())
```

- [ ] **步骤 2：加入身份、动作和模板断言**

增加测试：主色计数、21 个帧占用、待机身体一致、跑步脚签名至少四种、帽檐归一化横向签名唯一、正面眼睛、死亡字节、卷轴坐标、两个 PNG 字节一致、参考文件、验证图和 GIF 数量/时长。

关键断言使用生成器导出的常量：

```python
    def test_identity_palette(self):
        colors = self.sheet.getdata()
        for color in (roxy_gen.HAT_BLACK, roxy_gen.HAIR_BLUE, roxy_gen.CLOAK_OCHRE):
            self.assertGreaterEqual(sum(px == color for px in colors), 21)

    def test_death_uses_mage_tier_zero(self):
        source = Image.open(PROJECT / "core/src/main/assets/sprites/mage.png").convert("RGBA")
        for frame in range(8, 13):
            expected = source.crop((frame*12, 0, frame*12+12, 15))
            actual = self.sheet.crop((frame*12, 0, frame*12+12, 15))
            self.assertEqual(actual.tobytes(), expected.tobytes())

    def test_complete_output_set(self):
        self.assertEqual(len(list((ROOT / "gif").rglob("*.gif"))), 56)
        self.assertGreaterEqual(len(list((ROOT / "figure/verification").glob("*.png"))), 5)
```

- [ ] **步骤 3：运行测试确认失败**

运行：

```powershell
python 'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\test_generate_roxy_migurdia_mage_skin.py'
```

预期：FAIL/ERROR，原因是 `generate_roxy_migurdia_mage_skin.py` 尚不存在；保存失败输出作为 TDD 证据。

### 任务 4：实现确定性硬像素生成器

**文件：**
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\generate_roxy_migurdia_mage_skin.py`
- 修改：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\test_generate_roxy_migurdia_mage_skin.py`

- [ ] **步骤 1：建立颜色与帧绘制接口**

生成器必须提供稳定常量和单一入口：

```python
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
PROJECT = Path(r"D:\STUDY\Dungeon\transformation-pixel-dungeon-master")
FRAME = (12, 15)
SHEET = (256, 128)
HAT_BLACK = (25, 22, 34, 255)
HAT_LIGHT = (54, 48, 67, 255)
HAIR_BLUE = (65, 159, 214, 255)
HAIR_LIGHT = (99, 190, 231, 255)
CLOAK_OCHRE = (171, 125, 65, 255)
CLOAK_LIGHT = (207, 162, 88, 255)
SKIN = (238, 190, 160, 255)
EYE = (49, 124, 142, 255)

def blank_frame():
    return Image.new("RGBA", FRAME, (0, 0, 0, 0))

def draw_live_frame(frame_index: int) -> Image.Image:
    frame = blank_frame()
    # Dispatch to explicit side/front action phase functions.
    return frame

def generate_all() -> None:
    pass

if __name__ == "__main__":
    generate_all()
```

- [ ] **步骤 2：实现侧视与正视结构**

实现 `draw_side_head(draw, phase, y, front_turn=False)`、`draw_front_head(draw, y)`、`draw_side_body(draw, phase, y)`、`draw_front_body(draw, phase, y)`；黑帽轮廓、蓝发、黄褐披风先绘制为稳定结构，再绘制眼睛、手脚和柔性辫子/披风尾。

预期：帧 0 侧视单眼，帧 1 只改变头部并显示正面双眼；帧 16、17、19、20 为正面双眼。

- [ ] **步骤 3：实现六帧步态与攻击相位**

使用显式相位表，禁止整体平移代替步态：

```python
RUN_PHASES = (
    {"body_y": 0, "front_leg": 2, "rear_leg": -1, "arm": -1, "trail": -1},
    {"body_y": 0, "front_leg": 1, "rear_leg": 0,  "arm": 0,  "trail": -1},
    {"body_y": -1,"front_leg": 0, "rear_leg": 1,  "arm": 1,  "trail": 0},
    {"body_y": 0, "front_leg": -1,"rear_leg": 2,  "arm": 1,  "trail": 1},
    {"body_y": 0, "front_leg": 0, "rear_leg": 1,  "arm": 0,  "trail": 1},
    {"body_y": -1,"front_leg": 1, "rear_leg": 0,  "arm": -1, "trail": 0},
)
ATTACK_PHASES = (
    {"hand_x": 7, "hand_y": 8, "lean": 0},
    {"hand_x": 9, "hand_y": 7, "lean": 0},
    {"hand_x": 11,"hand_y": 7, "lean": 1},
)
```

预期：四种以上脚部签名；攻击帧 15 达到 x=11；所有侧视脚尖朝右。

- [ ] **步骤 4：复制死亡与卷轴模板**

从 `mage.png` 第 0 行逐字节裁剪 8–12 帧；阅读帧只从模板复制卷轴像素和颜色，不复制法师身体。将第 0 行全部 21 帧拼装完成后，复制相同 RGBA 行到 tier 1–6。

预期：七行逐像素一致；死亡测试与卷轴坐标测试通过。

- [ ] **步骤 5：生成最终 PNG、验证图和动作审计**

保存 `mage4.png` 后用 `shutil.copyfile` 创建别名；所有预览从最终贴图重新裁剪并用 `Image.Resampling.NEAREST` 放大。`motion_audit.json` 至少包含 `run_foot_signatures`、`hat_normalized_x_signatures`、`attack_max_x`、`front_eye_counts`、`tier_rows_identical`。

预期：至少 7 张验证 PNG，两个最终 PNG 字节一致。

- [ ] **步骤 6：生成明确的七行一致审计模板**

从项目原版 `mage.png` 裁剪完整第 0 行 `x=0..251, y=0..14`，原样复制到新建 `256x128` RGBA 图的七行，保留透明右侧和底部边距，保存为 `rules/mage_tier0_all_tiers_template.png`。同时在 `rules/source_derivation.json` 写入原版路径、原版 SHA-256、派生模板 SHA-256、`source_tier: 0`、`target_tiers: [0,1,2,3,4,5,6]` 和算法说明。

预期：模板的七个动作行逐像素一致，且第 0 行与原版 `mage.png` 第 0 行逐字节相同；模板只用于把用户批准的例外表达给审计器，不作为绘画参考。

- [ ] **步骤 7：生成 56 个 GIF**

动作源帧和时长固定为：idle `[0,0,0,1,0,0,1,1]` 1000ms；run `[2,3,4,5,6,7]` 50ms；die `[8,9,10,11,12,11]` 50ms；attack/zap `[13,14,15,0]` 70ms；operate `[16,17,16,17]` 120ms；fly `[18]` 1000ms；read `[19,20,20,20,20,20,20,20,20,19]` 50ms。

预期：7 个 tier 目录各有 8 个 GIF，总数恰好 56。

- [ ] **步骤 8：运行测试并逐项修复绘制**

运行：

```powershell
python 'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\test_generate_roxy_migurdia_mage_skin.py'
```

预期：所有测试 PASS。若视觉或断言失败，只修改生成器中的观察原因，不降低与规格一致的测试门槛。

### 任务 5：视觉检查与最终审计

**文件：**
- 检查：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\mage4.png`
- 检查：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\verification\*.png`
- 检查：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\gif\**\*.gif`
- 创建：`D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\audit.json`

- [ ] **步骤 1：视觉检查身份和动作**

依次查看原始 1x 贴图、放大动作总览、帽子轮廓、run、attack、operate 和 read。检查：洛琪希是否先由黑帽/蓝发/黄褐披风读出；正面没有第三只眼；帽子不横向抖动；腿是真实交替；辫子与披风慢一相位；手脚和卷轴未被遮挡。

预期：每项均能在最终贴图或 GIF 中直接观察到；若失败，先给测试增加对应回归断言，再修改生成器并重新生成。

- [ ] **步骤 2：运行单元测试的最终新鲜证据**

运行：

```powershell
python 'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\code\test_generate_roxy_migurdia_mage_skin.py'
```

预期：退出码 0，零失败、零错误。

- [ ] **步骤 3：运行技能审计**

运行：

```powershell
python 'D:\STUDY\Dungeon\skin\skills\pixel-dungeon-hero-skin\scripts\audit_skin.py' --sheet 'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\figure\mage4.png' --source 'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\mage_tier0_all_tiers_template.png' --gif-dir 'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\gif' --report 'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin\rules\audit.json'
```

该审计源模板由原版法师第 0 行确定性派生，并由 `source_derivation.json` 提供可追溯证据。

预期：退出码 0；sheet、padding、actions、run、attack、read 和 56 个 GIF 的检查全部通过。不得修改技能审计脚本，也不得通过改变七行一致性来规避。

- [ ] **步骤 4：核对最终数量和字节一致性**

运行：

```powershell
python -c "from pathlib import Path; import hashlib; r=Path(r'D:\STUDY\Dungeon\skin\roxy_migurdia_mage_skin'); a=(r/'figure/mage4.png').read_bytes(); b=(r/'figure/mage4_roxy_migurdia.png').read_bytes(); print('png_equal', a==b); print('gifs', len(list((r/'gif').rglob('*.gif')))); print('previews', len(list((r/'figure/verification').glob('*.png')))); print('sha256', hashlib.sha256(a).hexdigest())"
```

预期：`png_equal True`、`gifs 56`、`previews` 至少 5，并输出最终 SHA-256。

- [ ] **步骤 5：确认项目资源未被安装或修改**

运行：

```powershell
git status --short -- 'core/src/main/assets/sprites' 'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/HeroClass.java'
```

预期：本任务未新增 `mage4.png`，也未引入新的 `HeroClass.java` 差异；若该文件本来已有用户修改，保持原状态且不暂存。

---

计划执行采用当前会话的 `executing-plans` 路径，因为本任务不允许调度子代理。皮肤目录不属于 Git 项目，不在其中创建提交；游戏仓库只提交设计与计划文档。
