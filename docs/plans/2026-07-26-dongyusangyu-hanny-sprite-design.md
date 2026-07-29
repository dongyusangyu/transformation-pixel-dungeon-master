# Dongyusangyu 哈尼精灵图设计规格

## 目标与范围

为特殊怪物哈尼新增一套可直接被 Shattered Pixel Dungeon 精灵系统加载的素材与动画类，主要名称统一为 `Dongyusangyu`。

正式交付内容：

- `core/src/main/assets/sprites/dongyusangyu.png`
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/DongyusangyuSprite.java`
- 在 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java` 中新增 `Assets.Sprites.DONGYUSANGYU`

本次不创建或修改怪物/NPC 的行为类；项目中当前也没有检索到名为 `Dongyusangyu` 的现有角色类。后续角色类只需把 `spriteClass` 指向 `DongyusangyuSprite.class`。

## 项目实现依据

- `CharSprite.turnTo(int from, int to)` 已按目标格横坐标设置 `flipHorizontal`，因此无需制作两套左右帧，也无需重写转向逻辑。
- `ShopkeeperSprite`、`SheepSprite` 等静止 NPC 会让 `run`、`attack` 复用 `idle`，从而兼容引擎状态切换而不表现位移。
- 新精灵类继承 `MobSprite`，使用 `TextureFilm` 切分帧，并在构造函数末尾播放 `idle`。

## 视觉规格

### 轮廓

- 单帧尺寸：`16×16 px`。
- 采用已确认的方案 A：紧凑、3/4 侧向轮廓。
- 身体为无腿或近乎无腿的陶偶体块，双臂从身体两侧伸出。
- 脸部和手臂位置轻微偏向朝向侧，使水平镜像后能明确看出转向。
- 任何帧均为空手，不出现叉子、三叉戟、法杖或其他道具。
- 不使用平滑缩放、半透明边缘或抗锯齿。

### 色彩

以橙色陶器为主，控制为有限色板：

- 深棕轮廓：`#32190F`
- 陶器暗橙：`#BC5421`
- 陶器主橙：`#ED7B2F`
- 高光浅橙：`#FFBD69`
- 五官近黑：`#170D09`
- 眼部微亮点：`#FFF0D2`
- 完全透明背景

正式落图时允许在不改变总体橙色观感的前提下，对相邻色的明度做小幅调整，以保证在地牢暗背景上可读。

## 精灵表布局

精灵表使用单行布局，尺寸为 `160×16 px`，共 10 帧：

| 帧索引 | 内容 |
|---:|---|
| 0 | 基础站姿 |
| 1 | 轻微摆臂 |
| 2 | 眨眼 |
| 3 | 回稳/反向轻摆 |
| 4 | 第一阶段裂纹 |
| 5 | 裂纹扩大并出现小陶片 |
| 6 | 上部开始崩落 |
| 7 | 主体碎裂 |
| 8 | 陶片下落并收拢到地面 |
| 9 | 最终陶片残骸 |

所有帧共享同一基线与视觉中心。`idle` 中不做整体上下弹跳；死亡陶片不得越出 `16×16` 帧格。

## 动画规格

### idle

- `8 fps`
- 循环播放
- 帧序列：`0, 0, 0, 1, 1, 0, 0, 2, 2, 3, 0, 0`
- 表现为较克制的摆臂与眨眼，保持静止 NPC 的陶偶感。

### turn

- 使用继承自 `CharSprite` 的 `turnTo`。
- 通过 `flipHorizontal` 镜像整套当前动画。
- 不产生位置变化，不额外播放移动帧。

### run / attack / zap

- 全部复用 `idle.clone()`。
- 这些状态不会产生脚步、冲刺、武器或攻击动作。
- 这样即使上层行为意外切换到这些动画状态，也仍保持原地站立。

### die

- `10 fps`
- 单次播放
- 帧序列：`4, 5, 6, 7, 8, 9`
- 以陶器开裂和碎片落地结束，最终停留在第 9 帧。
- 哈尼不是人类，因此不使用 `warrior.png` 的白色骷髅头死亡素材。

## Java 命名与结构

- 类名：`DongyusangyuSprite`
- 资源常量：`Assets.Sprites.DONGYUSANGYU`
- 文件名：`dongyusangyu.png`
- 包：`com.shatteredpixel.shatteredpixeldungeon.sprites`
- 父类：`MobSprite`

代码保持与 `ShopkeeperSprite`、`SheepSprite` 相同的简洁构造方式，不增加粒子、音效、发光、位移或自定义转向副作用。

## 验证标准

1. PNG 尺寸严格为 `160×16`，透明背景，无半透明抗锯齿像素。
2. `TextureFilm(texture, 16, 16)` 能得到 10 个有效帧。
3. `idle` 循环无跳帧、无整体抖动，手臂始终空无一物。
4. 水平镜像前后朝向明确，角色占位和基线一致。
5. `run`、`attack`、`zap` 不产生位移或武器。
6. `die` 只播放一次，陶片顺序连续，最终残骸完整。
7. Java 编译通过，新增资源路径与文件名大小写完全一致。
8. 除上述三个正式交付点外，不改动项目中已有 NPC、怪物和用户的其他未提交文件。

## 已否决方案

- 正面完全对称轮廓：镜像后看不出转向。
- 18×18 高细节轮廓：相对普通 NPC 过于醒目，且不如 `16×16` 对齐现有资源。
- 大幅摆臂和上下弹跳：不符合静止 NPC 的表现。
- 叉子或其他道具：与明确要求冲突。
- 人类白色骷髅死亡帧：哈尼为陶器生物，应使用碎裂死亡表现。
