# 女猎手位置同步、触手与阵营修复实现计划

> 面向 AI 代理的工作者：使用 subagent-driven-development 执行，并在每个实现阶段后完成规格与质量审查。

**目标：** 修复女猎手逻辑坐标与贴图坐标失步，将二阶段触手占比提升至 2%，并确保女猎手、飞鹰和触手作为同阵营单位互不仇恨、互不受女猎手攻击效果伤害。

**架构：** 保留现有 Mob 移动、Fadeleaf 事务和 Warden 场地分配结构，只在自定义移动入口补齐与基础 Mob 相同的精灵同步语义。召唤物阵营通过显式初始化固定，攻击目标获取与延迟结算入口都使用同一阵营过滤，形成纵深防御。

**技术栈：** Java、JUnit、Gradle。

---

### 任务 1：建立失败回归测试

**文件：**
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBossTest.java`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevelTest.java`

- [ ] 测试自然狩猎直接移动与相邻掩体移动后，Boss `pos` 和记录型精灵最终格一致。
- [ ] 测试成功单体翻越、双消逝传送及失败回滚后，Boss 逻辑位置和精灵最终格一致。
- [ ] 测试 400 个合法候选格生成 8 只触手，且触手、植物、枯草仍互斥。
- [ ] 测试飞鹰与触手显式属于女猎手阵营，三者互不成为正常选敌目标。
- [ ] 测试普通射击、贯风箭及植物附加效果在结算时不会作用于飞鹰和触手。
- [ ] 运行两个定向测试类，确认失败来自上述行为缺失。

### 任务 2：最小生产修复

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/HuntressBoss.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/HuntressBossLevel.java`

- [ ] 让所有绕过 `Mob.Hunting` 的成功移动路径使用一次统一的精灵移动/最终放置语义；瞬移和回滚中断旧动画并将精灵放在权威 `pos`。
- [ ] 将二阶段触手数从候选格的 1% 调整为 2%，保留触手→植物→枯草的互斥分配顺序。
- [ ] 显式设置飞鹰和触手为 `Alignment.ENEMY`，并在女猎手选敌、攻击启动和延迟结算时排除同阵营目标。
- [ ] 贯风箭整条射线跳过同阵营目标，普通箭和自然之怒不对同阵营召唤物结算。
- [ ] 运行定向测试直至全绿并执行 `git diff --check`。

### 任务 3：集成验证与双重审查

**文件：**
- 验证上述四个文件及其调用链。

- [ ] 运行女猎手 Boss 与楼层测试类。
- [ ] 运行相关自然之怒、图鉴和选择测试。
- [ ] 运行 Android Java fresh 编译。
- [ ] 先做规格审查，再做代码质量审查；发现问题后交回实现代理修复并复审。

