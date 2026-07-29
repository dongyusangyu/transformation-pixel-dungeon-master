# 云端全局数据恢复实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 恢复客户端“同步服务器数据”入口，通过可选 UUID 一次性恢复云端全局统计，并将当前设备从原 UUID 重绑到恢复 UUID。

**架构：** 客户端使用独立 UUID 规范化函数和恢复结果类型，设置页依次完成 UUID 输入、范围确认和恢复请求。服务端沿用 `/api/download`，在恢复许可有效时于同一 SQLite 事务中消费许可并重绑稳定设备指纹，保留原 UUID 玩家记录。

**技术栈：** Java 8、libGDX UI、JUnit 4、Python 3、SQLite、unittest。

---

## 文件职责

- `server/test_cloud_restore.py`：验证一次性许可、UUID 查询和 A 到 B 设备重绑。
- `server/cloud_backend.py`：消费恢复许可并更新 `device_uuid_map`。
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/services/cloud/CloudRestoreIdentityTest.java`：验证 UUID 输入规范化。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/services/cloud/CloudRestoreIdentity.java`：纯 Java UUID 输入边界。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/services/cloud/CloudSyncService.java`：按指定 UUID 请求恢复并返回可区分错误。
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndSettings.java`：恢复按键、UUID 输入、确认和反馈。
- `core/src/main/assets/messages/windows/windows.properties`：英文恢复文案。
- `core/src/main/assets/messages/windows/windows_zh.properties`：中文恢复文案。

### 任务 1：服务端一次性恢复和设备重绑

- [x] 创建临时 SQLite 数据库测试，插入 UUID A、UUID B，并把当前设备映射到 A。
- [x] 运行 `python -m unittest server.test_cloud_restore -v`，确认测试因下载后设备仍映射 A 而失败。
- [x] 修改恢复流程：准备阶段返回持久化 token，确认阶段消费许可、释放 A 的设备主键并重绑 B。
- [x] 重跑服务端测试，确认 B 数据被返回、许可为 false、设备映射为 B、A/B 玩家记录均保留且 B 可以继续上传。

### 任务 2：客户端 UUID 输入边界和恢复 API

- [x] 创建 `CloudRestoreIdentityTest`，覆盖预填 UUID 规范化、空值设备回退、非法 UUID 和响应 UUID 匹配。
- [x] 运行目标测试，确认缺少实现导致失败。
- [x] 创建 `CloudRestoreIdentity`，空值返回空字符串，合法 UUID 返回小写标准形式，非法值返回 `null`。
- [x] 为 `CloudSyncService` 增加指定 UUID、两阶段恢复、持久化待处理事务和可区分错误。
- [x] 单独运行客户端 UUID 单元测试，确认 4 项通过。

### 任务 3：设置页恢复交互

- [x] 恢复 `btnSyncData` 的 `add` 和布局。
- [x] 点击后打开 `WndTextInput`，预填当前 UUID，允许留空自动识别，并拒绝非空非法 UUID。
- [x] 输入通过后打开 `WndOptions`，说明恢复白名单、不会修改局内存档、会消费一次许可。
- [x] 确认后请求恢复；成功刷新 UUID，失败按错误类型显示中文或英文文案。
- [x] 运行 Java 编译和相关单元测试。

### 任务 4：完整验证与部署

- [x] 运行服务端 unittest、客户端相关测试及 `compileJava`。
- [x] 检查 Git diff，确认没有改动存档处理、没有覆盖无关工作区变更。
- [x] 备份远程 `cloud_backend.py` 和生产 SQLite 数据库。
- [x] 部署服务端代码，重启 `talent-cloud.service`。
- [x] 使用只读数据库与聚合接口检查验证远程服务健康；不消费真实玩家恢复许可。
