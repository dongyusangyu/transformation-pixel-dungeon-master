# Transformation Pixel Dungeon / 蜕变地牢

蜕变地牢是基于 [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon) 的中文同人改版。项目保留了原作的像素 Roguelike、回合制探索、随机地牢和资源管理框架，并在此基础上围绕角色成长与高难内容做了大幅扩展。

当前版本应用名为 `蜕变地牢`，包名为 `com.Transform.Transformpixeldungeon`，版本号为 `0.2.9`。项目内容目前同步到 Shattered Pixel Dungeon v3.2 体系。

## 核心两点

1. **以天赋和蜕变为核心的构筑重写**

   本项目的主要设计重心是重新组织角色成长。天赋系统经过大规模调整，并引入蜕变卷轴、蜕变结晶、升华秘卷、Boss 天赋等机制，让玩家可以在职业、装备、炼金、Boss 掉落和随机选项之间持续塑造构筑，而不是只沿着原版固定路线成长。

2. **面向中文玩家的内容扩展与难度扩展**

   项目加入了大量中文本地化说明、图鉴/词典信息、自定义 Buff、挑战内容、强化 Boss、怪物变体、测试模式和辅助工具。它不是简单换皮版本，而是在 Shattered Pixel Dungeon 的底层规则上继续堆叠新敌人、新道具、新机制和调试入口。

## 项目结构

```text
.
├── SPD-classes/  原版/通用底层类
├── core/         游戏主体逻辑、资源、角色、怪物、物品、关卡和自定义内容
├── desktop/      桌面端启动器与打包配置
├── android/      Android 端启动器、Manifest、资源和 APK/AAB 构建配置
├── ios/          iOS 端 RoboVM 启动器、Info.plist 和资源配置
├── services/     新闻、更新等平台服务实现
└── docs/         各平台构建说明
```

核心代码主要在 `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon`。其中 `custom` 目录集中放置了本项目新增的挑战、Boss、测试工具、图鉴、特效和辅助逻辑。

## 技术栈

- Java 8 兼容源码，推荐使用 JDK 17 构建
- Gradle Wrapper `8.13`
- LibGDX `1.12.1`
- Android Gradle Plugin `8.1.0`
- RoboVM `2.3.23` 用于 iOS 构建

## 快速运行

在项目根目录执行：

```powershell
.\gradlew.bat :desktop:debug
```

这会以桌面端调试模式启动游戏，适合日常开发和玩法验证。

构建桌面端发布 JAR：

```powershell
.\gradlew.bat :desktop:release
```

产物位于：

```text
desktop/build/libs/
```

## Android 构建

调试包：

```powershell
.\gradlew.bat :android:assembleDebug
```

发布包建议使用 Android Studio 的 `Generate Signed Bundle / APK`，因为正式分发需要配置签名密钥。Android 相关说明可参考 `docs/getting-started-android.md`。

## iOS 构建说明

iOS 模块使用 RoboVM：

```bash
./gradlew :ios:createIPA
```

注意：iOS 可安装包必须在 macOS 上构建，并且需要 Xcode、Apple Developer 证书和 Provisioning Profile。Windows 环境可以完成 Java 编译检查，但无法生成真正可安装的 `.ipa`，因为 RoboVM 打包阶段会调用 macOS 的 `security`、签名和钥匙串工具。

iOS 相关说明可参考 `docs/getting-started-ios.md`。

## 本地化状态

当前版本以中文内容为主。项目中仍保留了部分 Shattered Pixel Dungeon 的多语言资源，但新增机制、图鉴和自定义内容主要面向中文玩家维护。

## 许可证

本项目继承 Shattered Pixel Dungeon / Pixel Dungeon 的 GPLv3 许可要求。发布修改版时，请保留开源协议并公开对应源码。
