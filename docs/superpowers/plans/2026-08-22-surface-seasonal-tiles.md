# 第 0 层现实月份季节素材切换实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 游戏启动时按设备现实月份固定选择第 0 层的葱郁或冬季地表与水面 PNG，重启后重新判定，且不改变地图生成和存档。

**架构：** 新增专注于第 0 层季节选择的 `SurfaceSeason` 静态状态类，由 `ShatteredPixelDungeon.create()` 在首个场景创建前初始化一次。`SurfaceTownLevel` 的基础纹理、水面纹理和三个自定义 Tilemap 全部读取同一季节状态；草地净化纹理按季节使用独立缓存键。

**技术栈：** Java 8、LibGDX/Noosa 纹理缓存、JUnit 4、Gradle、`java.awt.image.BufferedImage`/`ImageIO` 资源契约测试

**月份规则：** 1 月、2 月、11 月和 12 月选择冬季素材；3 月至 10 月选择葱郁素材。状态不写入存档，只在进程启动时初始化，运行期间不会因加载存档或跨月而改变。

---

## 文件结构

- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeason.java`
  - 唯一职责是启动期月份判定、当前季节缓存和两套素材路径选择。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeasonTest.java`
  - 覆盖月份边界、非法月份、素材路径和季节专用草地缓存键。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeonSeasonStartupTest.java`
  - 锁定季节初始化必须发生在首个场景创建之前的启动顺序。
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeasonAssetTest.java`
  - 验证四张 PNG 存在、RGBA/透明通道契约及固定尺寸。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java:48-65`
  - 声明冬季地表和冬季水面不可变资源路径。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java:122-136`
  - 在 `super.create()` 前完成本次进程唯一一次季节初始化。
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java:132-139,328-530`
  - 让基础地表、水面、道路、草地和室内地板统一使用启动期季节素材，并隔离草地纹理缓存。
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevelDongyusangyuTest.java:120-240`
  - 验证第 0 层及三个覆盖层在两季中始终选择一致素材，保留既有草地测试。

## 任务 1：建立季节状态与资源路径

**文件：**
- 创建：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeason.java`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeasonTest.java`
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java:48-65`

- [ ] **步骤 1：编写月份边界与素材映射的失败测试**

创建 `SurfaceSeasonTest.java`：

```java
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SurfaceSeasonTest {

	@After
	public void restoreLushSeason() {
		SurfaceSeason.initializeForMonth(3);
	}

	@Test
	public void selectsWinterOnlyFromNovemberThroughFebruary() {
		for (int month : new int[]{1, 2, 11, 12}) {
			SurfaceSeason.initializeForMonth(month);
			assertEquals(SurfaceSeason.Theme.WINTER, SurfaceSeason.current());
		}
		for (int month : new int[]{3, 4, 5, 6, 7, 8, 9, 10}) {
			SurfaceSeason.initializeForMonth(month);
			assertEquals(SurfaceSeason.Theme.LUSH, SurfaceSeason.current());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMonthZero() {
		SurfaceSeason.initializeForMonth(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMonthThirteen() {
		SurfaceSeason.initializeForMonth(13);
	}

	@Test
	public void returnsACompleteMatchingAssetSetForEachSeason() {
		SurfaceSeason.initializeForMonth(3);
		assertEquals(Assets.Environment.TILES_SURFACE_LUSH, SurfaceSeason.tilesTexture());
		assertEquals(Assets.Environment.WATER_SURFACE_LUSH, SurfaceSeason.waterTexture());
		assertEquals("surface-town-clean-grass-lush-v1", SurfaceSeason.grassTextureCacheKey());

		SurfaceSeason.initializeForMonth(11);
		assertEquals(Assets.Environment.TILES_SURFACE_WINTER, SurfaceSeason.tilesTexture());
		assertEquals(Assets.Environment.WATER_SURFACE_WINTER, SurfaceSeason.waterTexture());
		assertEquals("surface-town-clean-grass-winter-v1", SurfaceSeason.grassTextureCacheKey());
	}

	@Test
	public void keepsInitializedSeasonUntilTheNextExplicitInitialization() {
		SurfaceSeason.initializeForMonth(10);
		assertEquals(SurfaceSeason.Theme.LUSH, SurfaceSeason.current());
		assertEquals(Assets.Environment.TILES_SURFACE_LUSH, SurfaceSeason.tilesTexture());

		assertEquals(SurfaceSeason.Theme.LUSH, SurfaceSeason.current());
		assertEquals(Assets.Environment.TILES_SURFACE_LUSH, SurfaceSeason.tilesTexture());

		SurfaceSeason.initializeForMonth(11);
		assertEquals(SurfaceSeason.Theme.WINTER, SurfaceSeason.current());
		assertEquals(Assets.Environment.TILES_SURFACE_WINTER, SurfaceSeason.tilesTexture());
	}
}
```

- [ ] **步骤 2：运行测试并确认因缺少季节类型与冬季资源常量而失败**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceSeasonTest
```

预期：`compileTestJava FAILED`，错误包含找不到 `SurfaceSeason` 或 `TILES_SURFACE_WINTER`。

- [ ] **步骤 3：在 Assets 中声明冬季 PNG 路径**

在 `Assets.Environment` 的地表常量旁加入：

```java
public static final String TILES_SURFACE_LUSH   = "environment/tiles_surface_lush.png";
public static final String TILES_SURFACE_WINTER = "environment/tiles_surface_winter.png";
```

在水面常量旁加入：

```java
public static final String WATER_SURFACE_LUSH   = "environment/water_surface_lush.png";
public static final String WATER_SURFACE_WINTER = "environment/water_surface_winter.png";
```

- [ ] **步骤 4：实现最小季节状态类**

创建 `SurfaceSeason.java`：

```java
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import java.util.Calendar;
import java.util.GregorianCalendar;

public final class SurfaceSeason {

	enum Theme {
		LUSH,
		WINTER
	}

	private static Theme current = Theme.LUSH;

	private SurfaceSeason() {
	}

	public static void initializeFromSystemDate() {
		Calendar calendar = GregorianCalendar.getInstance();
		initializeForMonth(calendar.get(Calendar.MONTH) + 1);
	}

	static void initializeForMonth(int month) {
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException("month must be between 1 and 12: " + month);
		}
		current = month <= 2 || month >= 11 ? Theme.WINTER : Theme.LUSH;
	}

	static Theme current() {
		return current;
	}

	public static String tilesTexture() {
		return current == Theme.WINTER
				? Assets.Environment.TILES_SURFACE_WINTER
				: Assets.Environment.TILES_SURFACE_LUSH;
	}

	public static String waterTexture() {
		return current == Theme.WINTER
				? Assets.Environment.WATER_SURFACE_WINTER
				: Assets.Environment.WATER_SURFACE_LUSH;
	}

	static String grassTextureCacheKey() {
		return current == Theme.WINTER
				? "surface-town-clean-grass-winter-v1"
				: "surface-town-clean-grass-lush-v1";
	}
}
```

- [ ] **步骤 5：运行季节状态测试并确认通过**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceSeasonTest
```

预期：`BUILD SUCCESSFUL`，`SurfaceSeasonTest` 5 项测试全部通过。

- [ ] **步骤 6：提交季节状态与资源声明**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Assets.java core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeason.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeasonTest.java
git commit -m "feat: 添加第0层启动期季节状态"
```

## 任务 2：在游戏启动时固定本次会话季节

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java:122-136`
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeonSeasonStartupTest.java`

- [ ] **步骤 1：编写启动初始化顺序的失败测试**

创建 `ShatteredPixelDungeonSeasonStartupTest.java`：

```java
package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class ShatteredPixelDungeonSeasonStartupTest {

	@Test
	public void initializesSurfaceSeasonBeforeFirstSceneCreation() throws IOException {
		String source = new String(
				Files.readAllBytes(shatteredPixelDungeonSource()), StandardCharsets.UTF_8);
		int seasonInitialization = source.indexOf("SurfaceSeason.initializeFromSystemDate();");
		int firstSceneCreation = source.indexOf("super.create();");

		assertTrue("startup must initialize SurfaceSeason", seasonInitialization >= 0);
		assertTrue("season must be fixed before the first scene is created",
				firstSceneCreation >= 0 && seasonInitialization < firstSceneCreation);
	}

	private static Path shatteredPixelDungeonSource() {
		Path workingDirectory = Paths.get("").toAbsolutePath();
		Path fromRoot = workingDirectory.resolve(
				"core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java");
		if (Files.isRegularFile(fromRoot)) {
			return fromRoot;
		}
		return workingDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java");
	}
}
```

- [ ] **步骤 2：运行测试并确认启动入口尚未初始化季节而失败**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeonSeasonStartupTest
```

预期：FAIL，错误消息包含 `startup must initialize SurfaceSeason`。

- [ ] **步骤 3：把系统日期初始化接到真正的游戏启动入口**

在 `ShatteredPixelDungeon.java` 添加导入：

```java
import com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceSeason;
```

将 `create()` 开头改为：

```java
@Override
public void create() {
	SurfaceSeason.initializeFromSystemDate();
	super.create();

	updateSystemUI();
```

初始化必须位于 `super.create()` 之前，确保首个场景及其可能创建的第 0 层对象看到已经固定的季节。

- [ ] **步骤 4：编译核心模块并运行季节状态测试**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:compileJava core:test `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceSeasonTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeonSeasonStartupTest
```

预期：`BUILD SUCCESSFUL`，启动类编译通过，季节状态 5 项测试和启动顺序 1 项测试全部通过。

- [ ] **步骤 5：提交启动初始化**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeon.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeonSeasonStartupTest.java
git commit -m "feat: 启动时固定第0层季节"
```

## 任务 3：切换第 0 层基础地表与水面素材

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java:132-139`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevelDongyusangyuTest.java`

- [ ] **步骤 1：编写两季基础纹理选择的失败测试**

在 `SurfaceTownLevelDongyusangyuTest` 中加入，并在测试结束时恢复葱郁状态：

```java
@Test
public void selectsMatchingBaseTilesAndWaterForTheStartupSeason() {
	SurfaceTownLevel level = testLevel();

	SurfaceSeason.initializeForMonth(3);
	assertEquals(Assets.Environment.TILES_SURFACE_LUSH, level.tilesTex());
	assertEquals(Assets.Environment.WATER_SURFACE_LUSH, level.waterTex());

	SurfaceSeason.initializeForMonth(11);
	assertEquals(Assets.Environment.TILES_SURFACE_WINTER, level.tilesTex());
	assertEquals(Assets.Environment.WATER_SURFACE_WINTER, level.waterTex());

	SurfaceSeason.initializeForMonth(3);
}
```

同时添加导入：

```java
import com.shatteredpixel.shatteredpixeldungeon.Assets;
```

- [ ] **步骤 2：运行测试并确认仍返回葱郁素材而失败**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelDongyusangyuTest.selectsMatchingBaseTilesAndWaterForTheStartupSeason
```

预期：FAIL；冬季断言中实际值仍为 `environment/tiles_surface_lush.png` 或 `environment/water_surface_lush.png`。

- [ ] **步骤 3：让第 0 层基础纹理读取季节状态**

将 `SurfaceTownLevel` 中两个方法改为：

```java
@Override
public String tilesTex() {
	return SurfaceSeason.tilesTexture();
}

@Override
public String waterTex() {
	return SurfaceSeason.waterTexture();
}
```

- [ ] **步骤 4：运行基础纹理测试并确认通过**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelDongyusangyuTest.selectsMatchingBaseTilesAndWaterForTheStartupSeason
```

预期：`BUILD SUCCESSFUL`。

- [ ] **步骤 5：提交基础地表与水面切换**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevelDongyusangyuTest.java
git commit -m "feat: 切换第0层季节地表与水面"
```

## 任务 4：统一道路、草地和室内覆盖层并隔离纹理缓存

**文件：**
- 修改：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java:328-530`
- 修改：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevelDongyusangyuTest.java`

- [ ] **步骤 1：编写覆盖层素材一致性和缓存键隔离的失败测试**

在 `SurfaceTownLevelDongyusangyuTest` 中加入：

```java
@Test
public void keepsEverySurfaceOverlayOnTheSameSeasonalTileset() {
	SurfaceSeason.initializeForMonth(3);
	assertEquals(Assets.Environment.TILES_SURFACE_LUSH,
			SurfaceTownLevel.SurfacePathTilemap.tilesTexture());
	assertEquals(Assets.Environment.TILES_SURFACE_LUSH,
			SurfaceTownLevel.SurfaceGrassTilemap.tilesTexture());
	assertEquals(Assets.Environment.TILES_SURFACE_LUSH,
			SurfaceTownLevel.SurfaceInteriorFloorTilemap.tilesTexture());
	assertEquals("surface-town-clean-grass-lush-v1",
			SurfaceTownLevel.SurfaceGrassTilemap.cleanGrassTextureKey());

	SurfaceSeason.initializeForMonth(11);
	assertEquals(Assets.Environment.TILES_SURFACE_WINTER,
			SurfaceTownLevel.SurfacePathTilemap.tilesTexture());
	assertEquals(Assets.Environment.TILES_SURFACE_WINTER,
			SurfaceTownLevel.SurfaceGrassTilemap.tilesTexture());
	assertEquals(Assets.Environment.TILES_SURFACE_WINTER,
			SurfaceTownLevel.SurfaceInteriorFloorTilemap.tilesTexture());
	assertEquals("surface-town-clean-grass-winter-v1",
			SurfaceTownLevel.SurfaceGrassTilemap.cleanGrassTextureKey());

	SurfaceSeason.initializeForMonth(3);
}
```

- [ ] **步骤 2：运行测试并确认缺少覆盖层季节接口而失败**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelDongyusangyuTest.keepsEverySurfaceOverlayOnTheSameSeasonalTileset
```

预期：`compileTestJava FAILED`，错误包含找不到 `tilesTexture()` 或 `cleanGrassTextureKey()`。

- [ ] **步骤 3：让道路覆盖层读取当前季节图集**

在 `SurfacePathTilemap` 中加入并使用：

```java
static String tilesTexture() {
	return SurfaceSeason.tilesTexture();
}

{
	texture = tilesTexture();
	tileW = WIDTH;
	tileH = HEIGHT;
}
```

- [ ] **步骤 4：让室内地板覆盖层读取当前季节图集**

在 `SurfaceInteriorFloorTilemap` 中加入并使用：

```java
static String tilesTexture() {
	return SurfaceSeason.tilesTexture();
}

{
	texture = tilesTexture();
	tileW = WIDTH;
	tileH = HEIGHT;
}
```

- [ ] **步骤 5：让草地覆盖层按季节选择源图集和缓存键**

删除单一常量：

```java
private static final String CLEAN_GRASS_TEXTURE = "surface-town-clean-grass-v1";
```

在 `SurfaceGrassTilemap` 中加入：

```java
static String tilesTexture() {
	return SurfaceSeason.tilesTexture();
}

static String cleanGrassTextureKey() {
	return SurfaceSeason.grassTextureCacheKey();
}

{
	texture = tilesTexture();
	tileW = WIDTH;
	tileH = HEIGHT;
}
```

将 `cleanGrassTexture()` 开头和资源复制部分改为：

```java
private static Object cleanGrassTexture() {
	String sourceTexture = tilesTexture();
	String cacheKey = cleanGrassTextureKey();
	if (TextureCache.contains(cacheKey)) {
		return cacheKey;
	}

	Pixmap source = TextureCache.getBitmap(sourceTexture);
	if (source == null) {
		return sourceTexture;
	}

	SmartTexture cleaned = TextureCache.create(
			cacheKey, source.getWidth(), source.getHeight());
	cleaned.filter(SmartTexture.NEAREST, SmartTexture.NEAREST);
	cleaned.bitmap.setBlending(Pixmap.Blending.None);
	cleaned.bitmap.drawPixmap(source, 0, 0);
```

保留现有 `SURFACE_GRASS_TILES` 循环和 `cleanGrassPixel()`。将方法结尾改为：

```java
	source.dispose();
	return cacheKey;
}
```

- [ ] **步骤 6：运行覆盖层测试与既有草地测试**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelDongyusangyuTest.keepsEverySurfaceOverlayOnTheSameSeasonalTileset `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelDongyusangyuTest.usesFiveDeterministicGrassTilesWithoutVarianceNoise `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelDongyusangyuTest.removesEmbeddedGrassSpecklesWithoutRemovingFlowers `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelDongyusangyuTest.keepsFloweredGrassSparseWhileUsingEveryFlowerVariant
```

预期：`BUILD SUCCESSFUL`，4 项测试全部通过。

- [ ] **步骤 7：提交覆盖层和缓存隔离**

```powershell
git add core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevelDongyusangyuTest.java
git commit -m "fix: 统一第0层季节覆盖层纹理"
```

## 任务 5：锁定季节素材文件契约

**文件：**
- 创建：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeasonAssetTest.java`

- [ ] **步骤 1：编写四张环境 PNG 的契约测试**

创建 `SurfaceSeasonAssetTest.java`：

```java
package com.shatteredpixel.shatteredpixeldungeon.levels;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SurfaceSeasonAssetTest {

	@Test
	public void surfaceTilesetsShareTheFixedTileGridContract() throws IOException {
		for (String name : new String[]{"tiles_surface_lush.png", "tiles_surface_winter.png"}) {
			BufferedImage image = readEnvironmentImage(name);
			assertEquals(name, 256, image.getWidth());
			assertEquals(name, 256, image.getHeight());
			assertTrue(name, image.getColorModel().hasAlpha());
			assertEquals(name, 0, image.getWidth() % 16);
			assertEquals(name, 0, image.getHeight() % 16);
		}
	}

	@Test
	public void surfaceWaterTexturesShareTheFixedAnimationContract() throws IOException {
		for (String name : new String[]{"water_surface_lush.png", "water_surface_winter.png"}) {
			BufferedImage image = readEnvironmentImage(name);
			assertEquals(name, 32, image.getWidth());
			assertEquals(name, 32, image.getHeight());
			assertTrue(name, image.getColorModel().hasAlpha());
		}
	}

	private static BufferedImage readEnvironmentImage(String name) throws IOException {
		Path path = environmentDirectory().resolve(name);
		assertTrue("missing environment asset: " + path, Files.isRegularFile(path));
		BufferedImage image = ImageIO.read(path.toFile());
		assertNotNull("unreadable PNG: " + path, image);
		return image;
	}

	private static Path environmentDirectory() {
		Path workingDirectory = Paths.get("").toAbsolutePath();
		Path fromRoot = workingDirectory.resolve("core/src/main/assets/environment");
		if (Files.isDirectory(fromRoot)) {
			return fromRoot;
		}
		return workingDirectory.resolve("src/main/assets/environment");
	}
}
```

- [ ] **步骤 2：运行资源契约测试**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceSeasonAssetTest
```

预期：`BUILD SUCCESSFUL`，2 项素材契约测试通过。

- [ ] **步骤 3：提交素材契约测试**

```powershell
git add core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeasonAssetTest.java
git commit -m "test: 锁定第0层季节素材契约"
```

## 任务 6：完整回归与人工验收

**文件：**
- 验证：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeason.java`
- 验证：`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java`
- 验证：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeasonTest.java`
- 验证：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/ShatteredPixelDungeonSeasonStartupTest.java`
- 验证：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceSeasonAssetTest.java`
- 验证：`core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevelDongyusangyuTest.java`

- [ ] **步骤 1：运行季节功能与第 0 层完整测试类**

运行：

```powershell
.\gradlew.bat --no-daemon --offline --no-problems-report core:test `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceSeasonTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeonSeasonStartupTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceSeasonAssetTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelDongyusangyuTest `
  --tests com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevelTest
```

预期：`BUILD SUCCESSFUL`，上述测试类均为 0 failures、0 errors。

- [ ] **步骤 2：检查格式和意外改动**

运行：

```powershell
git diff --check
git status --short
```

预期：`git diff --check` 无输出；`git status --short` 中仅保留本功能改动及开始任务前已经存在的用户改动。

- [ ] **步骤 3：进行葱郁月份人工启动验收**

临时在测试构建中调用 `SurfaceSeason.initializeForMonth(3)`，启动桌面版并进入第 0 层，逐项确认：

- 主地表、道路、草地、室内木地板均来自 `tiles_surface_lush.png`。
- 湖面来自 `water_surface_lush.png`。
- 草地无灰色随机碎屑，带花草地维持约 20% 的既有比例。
- 地图布局、NPC、门、楼梯和湖泊位置不改变。

验收后撤销该临时月份注入，只保留正式的 `initializeFromSystemDate()` 启动调用。

- [ ] **步骤 4：进行冬季月份人工启动验收**

临时在测试构建中调用 `SurfaceSeason.initializeForMonth(11)`，启动桌面版并进入第 0 层，逐项确认：

- 主地表、道路、草地、室内地板均来自 `tiles_surface_winter.png` 的同语义槽位。
- 湖面来自 `water_surface_winter.png`，岸线与水体颜色自然衔接。
- 墙、门、书架、井、雕像、树木与楼梯没有瓦片错位。
- 同一画面中不存在葱郁和冬季地表图集混用。

验收后撤销该临时月份注入，并重新运行步骤 1 的自动化测试。

- [ ] **步骤 5：确认提交历史和最终差异**

运行：

```powershell
git log -5 --oneline
git diff HEAD~5..HEAD --stat
```

预期：提交历史包含季节状态、启动初始化、基础纹理切换、覆盖层统一和素材契约测试；最终差异不包含对四张 PNG 的重新编码或像素修改。
