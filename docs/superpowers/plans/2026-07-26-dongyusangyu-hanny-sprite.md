# Dongyusangyu 哈尼精灵实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:executing-plans 在当前会话逐项执行本计划，并遵循 superpowers:test-driven-development 的红灯—绿灯顺序。

**目标：** 新增橙色、空手、原地站立但可左右转向的 `Dongyusangyu` 哈尼精灵图及其 `MobSprite` 动画代码。

**架构：** 使用单张 `160×16` 透明 PNG 容纳 10 个 `16×16` 帧；`DongyusangyuSprite` 通过 `TextureFilm` 定义 idle 与陶片碎裂 die，run/attack/zap 复用 idle。转向直接使用 `CharSprite.turnTo` 的水平镜像行为，并在 `Assets.Sprites` 中注册资源路径。

**技术栈：** Java 8、JUnit 4、Noosa `TextureFilm`/`MovieClip.Animation`、PNG RGBA、Pillow（仅用于确定性生成像素素材）。

---

## 文件职责

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSpriteTest.java`
  - 回归检查资源常量、Java 动画声明、PNG 尺寸、透明度和逐帧非空。
- 创建：`core/src/main/assets/sprites/dongyusangyu.png`
  - 10 帧橙色哈尼像素素材；帧 0–3 为 idle，帧 4–9 为碎裂死亡。
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSprite.java`
  - 加载资源并定义 idle/run/attack/zap/die 动画。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`
  - 新增 `DONGYUSANGYU = "sprites/dongyusangyu.png"`。
- 已存在设计依据：`docs/plans/2026-07-26-dongyusangyu-hanny-sprite-design.md`
  - 记录已获用户确认的视觉和动画规格。

由于当前工作区含用户未提交改动，计划不创建提交、不暂存文件，也不修改无关文件。

### 任务 1：建立失败的资源与动画契约测试

**文件：**

- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSpriteTest.java`

- [x] **步骤 1：创建只读取真实产物的 JUnit 测试**

测试类使用 `System.getProperty("user.dir")` 同时兼容从仓库根目录或 `core` 模块目录运行，并检查：

```java
assertTrue(Files.exists(corePath("src/main/assets/sprites/dongyusangyu.png")));
assertTrue(Files.exists(corePath(
        "src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSprite.java")));
assertTrue(assetsSource.contains(
        "DONGYUSANGYU = \"sprites/dongyusangyu.png\""));
assertTrue(spriteSource.contains("new TextureFilm( texture, 16, 16 )"));
assertTrue(spriteSource.contains("idle = new Animation( 8, true )"));
assertTrue(spriteSource.contains("die = new Animation( 10, false )"));
```

PNG 检查使用 `ImageIO.read`：

```java
assertEquals(160, image.getWidth());
assertEquals(16, image.getHeight());
for (int y = 0; y < image.getHeight(); y++) {
    for (int x = 0; x < image.getWidth(); x++) {
        int alpha = image.getRGB(x, y) >>> 24;
        assertTrue(alpha == 0 || alpha == 255);
    }
}
```

另外按每 16 像素统计 10 个帧格，断言每帧至少包含一个不透明像素。

- [x] **步骤 2：运行测试并确认因正式产物不存在而失败**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.sprites.DongyusangyuSpriteTest
```

预期：`FAIL`，明确指出 `dongyusangyu.png` 或 `DongyusangyuSprite.java` 尚不存在，而不是测试代码编译错误。

### 任务 2：绘制确定性的橙色哈尼精灵表

**文件：**

- 创建：`core/src/main/assets/sprites/dongyusangyu.png`

- [x] **步骤 1：用逐像素坐标生成 10 帧 RGBA PNG**

使用 Pillow 建立 `160×16` 的完全透明画布，严格采用以下主色板：

```python
OUTLINE = (0x32, 0x19, 0x0F, 255)
SHADOW  = (0xBC, 0x54, 0x21, 255)
ORANGE  = (0xED, 0x7B, 0x2F, 255)
LIGHT   = (0xFF, 0xBD, 0x69, 255)
FACE    = (0x17, 0x0D, 0x09, 255)
EYE     = (0xFF, 0xF0, 0xD2, 255)
```

逐帧内容：

```text
0 基础 3/4 侧向站姿，双手空无一物
1 左右手臂各偏移 1 px 的轻微摆臂
2 眼睛压为 1 px 高的眨眼
3 反方向轻摆并回稳
4 主体出现细裂纹
5 裂纹扩大，边缘脱落小陶片
6 头部与上半身开始崩落
7 主体分成数块
8 陶片向地面下落
9 陶片在底部形成最终残骸
```

不得绘制叉子、三叉戟、法杖、武器或其他手持物。所有帧只使用全透明或完全不透明像素。

- [x] **步骤 2：运行测试并确认 PNG 结构检查仍只因 Java/常量缺失而失败**

运行同一 JUnit 命令。

预期：PNG 尺寸、硬透明度和逐帧非空检查通过；测试仍因 Java 类/资源常量未实现而失败。

- [x] **步骤 3：生成最近邻放大预览并人工检查像素逻辑**

将精灵表以最近邻放大到临时预览文件，检查：

- idle 四帧基线一致且身体无上下弹跳；
- 朝向不对称足以支持镜像转向；
- 所有手部为空；
- 死亡从裂纹到陶片连续；
- 透明背景无杂点。

临时预览不加入项目交付文件。

### 任务 3：接入资源常量与动画类

**文件：**

- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSprite.java`

- [x] **步骤 1：在 `Assets.Sprites` 中注册资源**

在 NPC 资源常量附近加入：

```java
public static final String DONGYUSANGYU = "sprites/dongyusangyu.png";
```

- [x] **步骤 2：实现最小动画类**

```java
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class DongyusangyuSprite extends MobSprite {

    public DongyusangyuSprite() {
        super();

        texture( Assets.Sprites.DONGYUSANGYU );

        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle = new Animation( 8, true );
        idle.frames( frames, 0, 0, 0, 1, 1, 0, 0, 2, 2, 3, 0, 0 );

        run = idle.clone();
        attack = idle.clone();
        zap = idle.clone();

        die = new Animation( 10, false );
        die.frames( frames, 4, 5, 6, 7, 8, 9 );

        play( idle );
    }
}
```

不重写 `turnTo`，从而继承项目已验证的左右镜像逻辑。

- [x] **步骤 3：运行目标测试并确认转为绿色**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.sprites.DongyusangyuSpriteTest
```

预期：`PASS`。

### 任务 4：编译与视觉验证

**文件：**

- 验证：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java`
- 验证：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSprite.java`
- 验证：`core/src/main/assets/sprites/dongyusangyu.png`

- [x] **步骤 1：编译 core 模块**

运行：

```powershell
.\gradlew.bat :core:compileJava
```

预期：`BUILD SUCCESSFUL`。

- [x] **步骤 2：执行目标测试与相邻测试**

运行：

```powershell
.\gradlew.bat :core:test --tests com.shatteredpixel.shatteredpixeldungeon.sprites.DongyusangyuSpriteTest
```

预期：`BUILD SUCCESSFUL` 且目标测试全绿。

- [x] **步骤 3：生成 idle、turn、die GIF 到临时目录做最终检查**

虽然 GIF 不作为正式交付，仍用最近邻缩放检查：

- idle 循环首尾连续；
- turn 为同一帧的水平镜像且占位一致；
- die 单次播放，最终停留在第 9 帧；
- 空手约束在所有帧成立。

- [x] **步骤 4：检查变更范围**

运行：

```powershell
git status --short -- `
  core/src/main/assets/sprites/dongyusangyu.png `
  core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java `
  core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSprite.java `
  core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSpriteTest.java `
  docs/plans/2026-07-26-dongyusangyu-hanny-sprite-design.md `
  docs/superpowers/plans/2026-07-26-dongyusangyu-hanny-sprite.md
```

确认没有改动其他用户文件，也不暂存、不提交。

## 计划自检

- 规格覆盖：橙色、方案 A、16×16、3/4 侧向、空手、静止 idle、镜像转向、陶片死亡、命名与路径均有对应任务。
- 占位符扫描：无待实现占位语句、无“后续补充”、无未定义方法或文件。
- 类型一致性：资源常量、Java 类、PNG 文件和测试中统一使用 `Dongyusangyu` / `DONGYUSANGYU` / `dongyusangyu.png`。
- 范围控制：不创建角色行为类，不修改现有 NPC，不触碰用户的其他未提交文件。
