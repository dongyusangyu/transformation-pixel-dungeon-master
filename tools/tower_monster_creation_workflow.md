# 高塔怪物标准制作流程

本流程用于在 `actors.mobs.tmobs`、`sprites.tmobs` 和高塔生成系统中新增怪物。目标是让数值、行为、美术、动画、图鉴和测试在同一份规格下闭环，避免“代码能运行但素材不匹配”或“精灵图存在但怪物不会生成”。

## 1. 开始前填写怪物规格卡

每个怪物先固定以下信息，不得直接从代码或生图开始：

| 项目 | 必填内容 |
|---|---|
| 中文名 / Java 类名 | 例如：迷彩豺狼 / `CamouflageGnoll` |
| 战斗定位 | 近战、远程、控制、召唤、精英或 Boss |
| 出现楼层 | 高塔起始层、截止层、生成概率或权重 |
| 基准倍率 | HP、伤害、命中、闪避、护甲、移速、攻击间隔 |
| 普通能力 | 触发条件、概率、持续时间、可否叠加 |
| 地形能力 | 生效地形、离开地形后的行为 |
| 相似怪物 | 数值参考、行为参考、美术参考分别列出 |
| 动作列表 | 待机、行走、攻击、死亡、特殊动作 |
| 掉落与经验 | 明确填写“无额外掉落”也可以 |

能力描述必须能转写成确定条件。例如不要写“在草地上更强”，而应写成“处于 `GRASS`、`HIGH_GRASS` 或 `FURROWED_GRASS` 时，闪避乘 1.5，施毒概率变为 1”。

## 2. 数值设计：以 21–25 层为基准

高塔后段普通怪物的 100% 基准如下：

| 数值 | 基准 | 推荐实现 |
|---|---:|---|
| HP | 100 | `HP = HT = 100` |
| 平均伤害 | 25 | `NormalIntRange(20, 30)` |
| 命中 | 40 | `attackSkill()` 返回 40 |
| 闪避 | 20 | `defenseSkill = 20` |
| 平均护甲 | 5 | `NormalIntRange(0, 10)` |
| 移速 | 1 | 保留 `Char` 默认速度 |
| 攻击间隔 | 1 | 保留默认 `attackDelay()` |
| 经验 | 13 | 普通后段怪物建议值 |
| 经验上限 | 26 | `maxLvl = 26` |

倍率先作用于平均值，再选择整数区间。例如 120% 平均伤害为 30，可以用 25–35；护甲必须检查区间平均值，而不能只比较最大值。

设计检查：

1. 同时提高生存和输出时，应降低生成概率或增加清晰弱点。
2. 强地形能力只在地图确实会生成该地形时使用。
3. 速度与攻击间隔保持默认值时不要重复覆写，以免绕过迟缓、加速等 Buff。
4. 概率能力应提供可测试的纯判定或钩子，随机调用只留在最外层。

## 3. 检索并拆分相似怪物

不要只找一个“整体最像”的怪物。分别为物种轮廓、战斗行为、状态效果和地形交互找参考：

| 参考维度 | 迷彩豺狼采用的参考 | 可复用内容 |
|---|---|---|
| 物种与近战 | `Gnoll` | 豺狼人比例、近战状态机 |
| 动画语言 | `GnollSprite`、`GnollGuardSprite`、`GnollSapperSprite` | 2 帧待机、4 帧行走、2 帧攻击、3 帧死亡 |
| 复杂豺狼造型 | `GnollGeomancerSprite` | 叶片/披肩仍需保持头、耳、吻部可读 |
| 中毒 | `FungalSentry.attackProc()` | `Buff.affect(..., Poison.class).set/extend()` |
| 草地联动 | `FungalSpinner` | `Terrain` 判定和草地相关防御收益 |
| 21–25 层数值 | `Eye`、`Succubus`、`Scorpio` | HP、命中、伤害和护甲量级 |

检查参考代码时记录文件路径和具体方法，不要仅凭游戏印象复刻。

## 4. 行为代码：先测试，再实现

### 4.1 文件位置

```text
core/src/main/java/.../actors/mobs/tmobs/<Monster>.java
core/src/test/java/.../actors/mobs/tmobs/<Monster>Test.java
```

测试至少覆盖：

- HP、伤害上下界、命中、基础闪避、护甲上下界。
- 地形外和地形内的能力差异。
- 状态效果持续时间。
- 0 伤害、地图未初始化、位置越界等边界。
- 默认移速和攻击间隔未被意外改变。

推荐把随机与效果拆开：

```java
protected float effectChance() {
    return isOnSpecialTerrain() ? 1f : 1f / 3f;
}

protected boolean rollEffect() {
    return Random.Float() < effectChance();
}

protected void applyEffect(Char enemy, int damage) {
    if (damage > 0 && rollEffect()) {
        Buff.affect(enemy, Poison.class).set(4f);
    }
}
```

`attackProc()` 仍先调用父类，再调用自己的效果钩子，保证全局战斗修正继续生效。

## 5. 概念图：先锁定动作逻辑

生图前必须先查看已有精灵图，概念图只是动作和造型参考，不能直接缩小当成最终资源。

概念图提示词至少包含：

- 同一角色、统一朝向、无遮挡分栏。
- 待机、两个行走极值、攻击蓄力、攻击命中、死亡过程、一个特殊动作。
- 攻击时支撑脚、重心、武器轨迹和披风/尾巴跟随关系。
- “适合转为最大 16×16 像素单帧”。
- 限色、硬边、无抗锯齿、无渐变、无文字、无场景。

概念图验收：

1. 每个动作的角色装备和身体比例一致。
2. 攻击武器没有换手或穿过身体。
3. 行走动作左右腿交替，身体起伏幅度不超过 1 像素的映射量。
4. 死亡动作重心持续降低，不出现倒地后重新站高。
5. 特殊动作在轮廓上可识别，而不是只靠粒子特效。

## 6. 像素素材：最大 16×16 的硬约束

推荐普通人形怪物使用 12×16，只有轮廓确实需要时才使用 16×16。

硬性规则：

- PNG、RGBA、透明背景。
- 单帧宽高均不得超过 16。
- 禁止双线性缩放、抗锯齿、半透明边缘和柔光。
- 使用 1 像素统一外轮廓。
- 推荐 8–12 个可见颜色；同一材质最多使用阴影、基色、高光三阶。
- 在 1 倍大小检查轮廓，在 8 倍最近邻预览检查坏像素。
- 所有帧脚底基线一致；跳跃或死亡动作除外。
- 每一帧都必须非空，透明像素 RGB 清零，避免采样光晕。

### 标准 13 帧布局

| 帧 | 用途 |
|---:|---|
| 0–1 | 待机循环 |
| 2–3 | 攻击蓄力与命中，动画结束回到帧 0 |
| 4–7 | 四相行走循环 |
| 8–10 | 三阶段死亡 |
| 11–12 | 怪物专属动作，例如伪装、蓄力或施法 |

完成后运行：

```powershell
python validate_tower_monster_sprite.py <sprite.png> `
  --frame-width 12 --frame-height 16 `
  --expected-frames 13 --max-colors 12
```

## 7. 动画代码

文件位置：

```text
core/src/main/java/.../sprites/tmobs/<Monster>Sprite.java
```

标准速度：

```java
idle   = new Animation(2, true);
run    = new Animation(12, true);
attack = new Animation(12, false);
die    = new Animation(12, false);
```

特殊动作应独立定义，不要覆盖标准帧。地形待机动画可在 `idle()` 中根据怪物公开的只读状态切换。构造函数内最后播放标准待机，避免角色尚未 link 时访问地图。

## 8. 完整接入清单

1. 在 `Assets.Sprites` 新增 PNG 路径常量。
2. 怪物的 `spriteClass` 指向 `sprites.tmobs` 中的专属类。
3. 在 `actors.properties` 和 `actors_zh.properties` 增加 `name`、`desc`、`discover_hint`。
4. 普通高塔怪物注册到 `Bestiary.TOWER_MOBS`；Boss 注册到 `TOWER_BOSSES`。
5. 在高塔生成规则中设置起始层与概率。
6. 生成规则优先写成不依赖纹理或地图实例的纯函数，以便单元测试。
7. 高数值怪物不得在其参考深度之前出现。

## 9. 完成前验证

最低验证命令：

```powershell
python test_validate_tower_monster_sprite.py
python validate_tower_monster_sprite.py <sprite.png> --frame-width 12 --frame-height 16 --expected-frames 13
gradlew.bat --no-problems-report core:test --tests "*<Monster>*"
gradlew.bat --no-problems-report core:compileJava
git diff --check
```

人工检查：

- 实际资源帧尺寸、代码 `TextureFilm` 尺寸和验证参数完全一致。
- 攻击命中帧和实际伤害结算时点观感一致。
- 草地、高草和犁过的草地是否都按规格处理。
- 图鉴分类正确，发现提示不会提前透露未完成内容。
- 至少在普通地面与特殊地形各观察一次完整待机、行走、攻击和死亡。

## 10. 迷彩豺狼成品规格

- 第 6 层开始出现，生成概率 20%。
- HP 100，伤害 20–30，命中 40，闪避 20，护甲 0–10。
- 普通地形成功攻击有 33% 概率施加 4 回合中毒。
- 三类草地上闪避提升至 30，成功攻击必定中毒。
- 单帧 12×16，共 13 帧，11 个可见颜色。
- 专属动作是两帧草地伪装待机。
- 无额外掉落，经验 13，经验上限 26。
