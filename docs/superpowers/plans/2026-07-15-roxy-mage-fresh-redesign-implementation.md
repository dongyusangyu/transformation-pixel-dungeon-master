# 洛琪希法师皮肤全新重绘实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法跟踪进度。

**目标：** 在 `D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin` 中从零生成可用于法师 `mage2.png` 槽位的洛琪希皮肤，七个护甲层外观完全相同，并通过专用单元测试和 `audit_skin.py`。

**架构：** 使用技能脚手架创建隔离目录；生成器将 12×15 帧拆分为帽子、头发/脸、披风/躯干、肢体、动态附件和项目模板复制六类职责。测试先锁定项目协议、身份锚点、单一护甲策略和动作逻辑，再以 Pillow 硬像素生成最终表、验证蒙太奇及 56 个 GIF。

**技术栈：** Python 3.12、Pillow、NumPy、imageio、`unittest`、Pixel Dungeon Hero Skin 工具、Codex image generation。

---

## 文件结构

- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/code/generate_roxy_migurdia_mage_skin.py`——唯一确定性生成入口，负责帧、精灵表、预览、GIF 和规则 JSON。
- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/code/test_generate_roxy_migurdia_mage_skin.py`——协议、身份、动作、模板和产物测试。
- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/figure/reference/roxy_motion_reference.png`——原创非像素画空手多姿势参考。
- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/figure/reference/roxy_motion_reference_prompt.txt`——图像生成精确提示词。
- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/figure/reference/roxy_official_front.*`、`roxy_official_three_quarter.*`、`roxy_official_motion.*`——外部身份与动作证据。
- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/figure/mage2.png` 和 `roxy_migurdia_mage.png`——字节一致的最终精灵表与别名。
- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/figure/verification/*.png`——1×/12×、动作、帽子、身份、护甲等验证图。
- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/gif/tier_*/{idle,run,die,attack,zap,operate,fly,read}.gif`——七层共 56 个 GIF。
- 创建：`D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/rules/{design.md,reference_sources.json,style_notes.json,motion_audit.json,audit.json}`——可复现规则与审核记录。

### 任务 1：脚手架与测试红灯

**文件：**
- 创建：上述新皮肤目录树及 `rules/skin_manifest.json`
- 创建：`code/test_generate_roxy_migurdia_mage_skin.py`

- [ ] **步骤 1：运行脚手架工具**

```powershell
python D:/STUDY/Dungeon/skin/skills/pixel-dungeon-hero-skin/scripts/scaffold_skin.py roxy_migurdia_mage_fresh_skin --hero mage --slot mage2.png --root D:/STUDY/Dungeon/skin --project-root D:/STUDY/Dungeon/transformation-pixel-dungeon-master
```

预期：创建全新目录且不覆盖任何已有皮肤。

- [ ] **步骤 2：写入首批失败测试**

```python
class RoxyGeneratorTests(unittest.TestCase):
    def test_sheet_contract(self):
        image = Image.open(FINAL)
        self.assertEqual(image.mode, "RGBA")
        self.assertEqual(image.size, (256, 128))
        self.assertEqual(image.crop((252, 0, 256, 128)).getbbox(), None)
        self.assertEqual(image.crop((0, 105, 256, 128)).getbbox(), None)

    def test_all_armor_rows_are_identical(self):
        image = Image.open(FINAL)
        row0 = image.crop((0, 0, 252, 15)).tobytes()
        for tier in range(1, 7):
            self.assertEqual(row0, image.crop((0, tier * 15, 252, tier * 15 + 15)).tobytes())
```

- [ ] **步骤 3：运行测试确认红灯**

```powershell
python -m unittest D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/code/test_generate_roxy_migurdia_mage_skin.py -v
```

预期：FAIL，原因是 `mage2.png` 和生成器尚不存在。

### 任务 2：参考证据与原创动作参考

**文件：**
- 创建：`figure/reference/*`
- 创建：`rules/reference_sources.json`

- [ ] **步骤 1：从官方动画网站保存正面、三分之四和动作候选**

记录每个下载文件的原始页面 URL、直接图片 URL、检查日期 `2026-07-15`，以及只用于蓝发、黑帽、黄褐披风、发束和衣物惯性的视觉事实；不得把参考像素直接移入精灵表。

- [ ] **步骤 2：用图像模型生成非像素画动作参考**

提示词必须明确：洛琪希式蓝发少女魔术师、黑色宽檐尖帽、黄褐披风、约二头身、白底、三个不同跑姿、三个不同空手施法攻击姿势、正面和侧面姿势；双手均空，不得出现法杖、书、卷轴、武器、像素画、精灵表或放大游戏帧。

- [ ] **步骤 3：视觉检查动作参考**

确认至少 3 个跑姿、3 个攻击姿势、1 个正面、1 个侧面，所有姿势手部为空；将检查结论写入 `reference_sources.json`。

### 任务 3：生成器骨架与协议绿灯

**文件：**
- 创建：`code/generate_roxy_migurdia_mage_skin.py`
- 修改：`code/test_generate_roxy_migurdia_mage_skin.py`

- [ ] **步骤 1：实现基础常量和帧接口**

```python
FRAME_W, FRAME_H = 12, 15
SHEET_W, SHEET_H = 256, 128
COLS, TIERS = 21, 7

def blank_frame():
    return Image.new("RGBA", (FRAME_W, FRAME_H), (0, 0, 0, 0))

def paste_frame(sheet, frame, col, tier):
    sheet.paste(frame, (col * FRAME_W, tier * FRAME_H), frame)
```

- [ ] **步骤 2：实现单层后复制七层**

```python
def build_sheet(frames):
    sheet = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    for tier in range(TIERS):
        for col, frame in enumerate(frames):
            paste_frame(sheet, frame, col, tier)
    return sheet
```

- [ ] **步骤 3：生成占位协议表并运行测试**

预期：尺寸、模式、留白、七层一致性通过；身份与动作测试仍失败。

### 任务 4：身份锚点与静态视图

**文件：**
- 修改：`code/generate_roxy_migurdia_mage_skin.py`
- 修改：`code/test_generate_roxy_migurdia_mage_skin.py`

- [ ] **步骤 1：添加失败的身份测试**

```python
def test_roxy_identity_colors_exist(self):
    colors = set(Image.open(FINAL).getdata())
    self.assertTrue(colors.intersection(BRIGHT_BLUE_HAIR))
    self.assertTrue(colors.intersection(TAN_CAPE))
    self.assertIn(BLACK_HAT, colors)
```

同时测试侧视帽檐宽度 7–9 像素、正面双眼数量为 2、所有活体帧不存在半透明像素。

- [ ] **步骤 2：实现分层绘制函数**

```python
def draw_side_head(frame, phase, front=False): ...
def draw_hat(frame, view, phase): ...
def draw_cape_torso(frame, pose, cape_phase): ...
def draw_limbs(frame, arm_pose, leg_pose): ...
```

绘制顺序为后侧披风/发束、腿、躯干、脸与头发、手臂、帽子；帽檐最后覆盖头顶但不得遮掉逻辑眼睛。

- [ ] **步骤 3：实现 Idle、Operate、Fly、Read 活体结构**

Idle 0/1 身体像素一致，只有头组变化；Operate/Read 为正面，Fly 为右侧。运行身份测试，预期 PASS。

### 任务 5：真实步态、攻击和项目模板

**文件：**
- 修改：生成器和测试文件

- [ ] **步骤 1：添加失败的动作测试**

测试 run 2–7 至少四个脚部签名、每帧双脚朝右、相邻帧头/躯干/腿至少三处变化；attack 13–15 包含蓄势、伸展和最大触及；帽子归一化水平轮廓不漂移。

- [ ] **步骤 2：实现六帧步态表**

```python
RUN_PHASES = (
    {"body_y": 0, "front_leg": "contact", "rear_leg": "back", "cape": 5},
    {"body_y": 1, "front_leg": "down", "rear_leg": "push", "cape": 0},
    {"body_y": 0, "front_leg": "pass", "rear_leg": "lift", "cape": 1},
    {"body_y": 0, "front_leg": "back", "rear_leg": "contact", "cape": 2},
    {"body_y": 1, "front_leg": "push", "rear_leg": "down", "cape": 3},
    {"body_y": 0, "front_leg": "lift", "rear_leg": "pass", "cape": 4},
)
```

- [ ] **步骤 3：实现攻击三阶段**

使用空手手臂轮廓制造触及变化，不绘制法杖或特效；保持帽子与刚性躯干宽度稳定。

- [ ] **步骤 4：逐像素复制法师死亡帧和卷轴模板**

从 `mage.png` 的第 0 层复制 8–12 帧；Read 19–20 只复制卷轴规定坐标与颜色，不复制原法师身体。

- [ ] **步骤 5：运行完整单元测试**

预期：动作、模板、身份、协议测试全部 PASS。

### 任务 6：完整产物集

**文件：**
- 修改：生成器
- 创建：最终 PNG、验证 PNG、56 个 GIF、规则文件

- [ ] **步骤 1：保存字节一致的最终表和别名**

```python
sheet.save(FINAL, format="PNG", optimize=False)
ALIAS.write_bytes(FINAL.read_bytes())
```

- [ ] **步骤 2：生成至少六张验证图**

生成 `sheet_1x_and_12x.png`、`identity_anchors.png`、`hat_stability.png`、`run_breakdown.png`、`front_actions.png`、`all_tiers.png`，全部使用 nearest-neighbor 整数缩放。

- [ ] **步骤 3：生成七层八动作 GIF**

使用协议帧序和可表示的 10ms 时长：idle 1000ms、run 50ms、die 50ms、attack/zap 70ms、operate 120ms、fly 1000ms、read 50ms。

- [ ] **步骤 4：写入规则与动作审计 JSON**

记录调色板、锚点、帧视图、步态脚签名、帽子签名、眼睛位置、模板来源、输出计数和参考来源。

### 任务 7：视觉审查与迭代

**文件：**
- 修改：生成器、测试和重新生成的产物

- [ ] **步骤 1：检查 1× 与 12× 验证图**

在 1× 确认黑帽、蓝发、黄褐披风三锚点可读；在 12× 检查脸、手、脚、披风边缘和硬 alpha。

- [ ] **步骤 2：检查 run/attack/operate/read GIF**

若观察到漂移、额外眼睛、假步态、披风遮脚或帽檐闪烁，先把原因转成失败回归测试，再只修改一个原因并重新生成。

- [ ] **步骤 3：重复测试和视觉检查直至无已知缺陷**

每次修改后重新生成全部产物，禁止保留过期 GIF 或审核数据。

### 任务 8：最终确定性验证

**文件：**
- 创建：`rules/audit.json`

- [ ] **步骤 1：运行皮肤单元测试**

```powershell
python -m unittest D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/code/test_generate_roxy_migurdia_mage_skin.py -v
```

预期：全部测试 PASS，零失败。

- [ ] **步骤 2：运行专用审计**

```powershell
python D:/STUDY/Dungeon/skin/skills/pixel-dungeon-hero-skin/scripts/audit_skin.py --sheet D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/figure/mage2.png --source D:/STUDY/Dungeon/transformation-pixel-dungeon-master/core/src/main/assets/sprites/mage.png --gif-dir D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/gif --report D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin/rules/audit.json
```

预期：退出码 0，56 个 GIF，timing errors 为空，death/read 模板通过。

- [ ] **步骤 3：核对最终交付计数与哈希**

确认两个 PNG 字节一致、验证图不少于 5 张、参考图和提示词存在、所有开发文件均位于 `D:/STUDY/Dungeon/skin/roxy_migurdia_mage_fresh_skin`，且未把 `mage2.png` 安装进游戏工程。

