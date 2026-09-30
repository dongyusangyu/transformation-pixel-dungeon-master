package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SurfaceShopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.ChainMace;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSword;
import com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SurfaceTownLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameSettings;
import com.watabou.utils.PathFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class RenovatedTextIntegrationTest {
    private static Application oldApp;
    private static Files oldFiles;
    private static Preferences oldPreferences;
    private static Languages oldLanguage;
    private static String oldVersion;
    private static HeadlessItemSprites sprites;
    private static final Map<Field, Object> pathFinderState = new LinkedHashMap<>();

    @BeforeClass public static void loadRealAssets() throws Exception {
        oldApp = Gdx.app;
        oldFiles = Gdx.files;
        oldVersion = Game.version;
        Game.version = "0.3.0";
        for (Field field : PathFinder.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;
            field.setAccessible(true);
            pathFinderState.put(field, field.get(null));
        }
        Field field = GameSettings.class.getDeclaredField("prefs");
        field.setAccessible(true);
        oldPreferences = (Preferences) field.get(null);
        Preferences preferences = (Preferences) Proxy.newProxyInstance(
                Preferences.class.getClassLoader(), new Class<?>[]{Preferences.class},
                (proxy, method, args) -> {
                    if (method.getName().startsWith("get") && args != null && args.length == 2) return args[1];
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == Preferences.class) return proxy;
                    return null;
                });
        GameSettings.set(preferences);
        Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
                new Class<?>[]{Application.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getType")) return Application.ApplicationType.Desktop;
                    if (method.getName().equals("getPreferences")) return preferences;
                    return null;
                });
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
                new Class<?>[]{Files.class}, (proxy, method, args) -> {
                    File root = new File("src/main/assets");
                    if (!root.isDirectory()) root = new File("core/src/main/assets");
                    return new FileHandle(new File(root, (String) args[0]));
                });
        sprites = new HeadlessItemSprites();
        oldLanguage = Messages.lang();
        Messages.setup(Languages.CHI_SMPL);
    }

    @AfterClass public static void restoreEnvironment() throws Exception {
        for (Map.Entry<Field, Object> entry : pathFinderState.entrySet()) entry.getKey().set(null, entry.getValue());
        Messages.setup(oldLanguage);
        sprites.close();
        Gdx.app = oldApp;
        Gdx.files = oldFiles;
        GameSettings.set(oldPreferences);
        Game.version = oldVersion;
    }

    @Test public void surfaceStairsAndForestDescribeTheirActualDestinations() {
        SurfaceTownLevel level = new SurfaceTownLevel();
        assertEquals("地牢入口", level.tileName(Terrain.EXIT));
        assertEquals("高塔入口", level.tileName(Terrain.ENTRANCE));
        assertTrue(level.tileDesc(Terrain.EXIT).contains("没有必要"));
        assertTrue(level.tileDesc(Terrain.ENTRANCE).contains("无法返回"));
        assertEquals("树林", level.tileName(Terrain.REGION_DECO));
        assertEquals(level.tileDesc(Terrain.REGION_DECO), level.tileDesc(Terrain.REGION_DECO_ALT));
        assertTrue(level.tileDesc(Terrain.BOOKSHELF).contains("科学与魔法"));
        assertEquals(new TowerLevel().tileName(Terrain.WALL), level.tileName(Terrain.WALL));
    }

    @Test public void huntressCityStatuesAndExitUseDwarvenCityDescriptions() {
        HuntressBossLevel level = new HuntressBossLevel();
        assertEquals(Messages.get(com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel.class,
                "statue_desc"), level.tileDesc(Terrain.STATUE));
        assertEquals(level.tileDesc(Terrain.STATUE), level.tileDesc(Terrain.STATUE_SP));
        assertEquals("楼层出口", level.tileName(Terrain.EXIT));
        assertEquals("通向下一层的斜坡。", level.tileDesc(Terrain.EXIT));
    }

    @Test public void huntressMetalSupportsUseCavesBossTextInBothLanguages() {
        Languages previousLanguage = Messages.lang();
        try {
            HuntressBossLevel huntress = new HuntressBossLevel();
            CavesBossLevel dm300 = new CavesBossLevel();
            for (Languages language : new Languages[]{Languages.ENGLISH, Languages.CHI_SMPL}) {
                Messages.setup(language);
                for (int tile : new int[]{Terrain.REGION_DECO, Terrain.REGION_DECO_ALT}) {
                    assertEquals(dm300.tileName(tile), huntress.tileName(tile));
                    assertEquals(dm300.tileDesc(tile), huntress.tileDesc(tile));
                    assertFalse(huntress.tileName(tile).isEmpty());
                    assertFalse(huntress.tileDesc(tile).isEmpty());
                }
            }
        } finally {
            Messages.setup(previousLanguage);
        }
    }

    @Test public void raidTerrainUsesShipDescriptionsWithoutChangingOrdinaryStairs() {
        ExtractionRaidLevel level = new ExtractionRaidLevel();
        assertEquals("污浊水潭", level.tileName(Terrain.WATER));
        assertEquals("杂草", level.tileName(Terrain.GRASS));
        assertEquals("传送装置", level.tileName(Terrain.EXIT));
        assertTrue(level.tileDesc(Terrain.EXIT).contains("门禁卡"));
        for (int tile : new int[]{Terrain.STATUE, Terrain.STATUE_SP, Terrain.REGION_DECO, Terrain.REGION_DECO_ALT}) {
            assertEquals("飞船货箱", level.tileName(tile));
            assertTrue(level.tileDesc(tile).contains("货箱"));
        }
        assertTrue(level.tileDesc(Terrain.BOOKSHELF).contains("船员手册"));
        assertNotEquals("高塔入口", level.tileName(Terrain.ENTRANCE));
    }

    @Test public void towerAndBossTerrainHaveConsistentNamesAndStairDirections() {
        int oldDepth = Dungeon.depth;
        int oldBranch = Dungeon.branch;
        try {
            Dungeon.branch = TowerLevel.BRANCH;
            for (Level level : new Level[]{new TowerLevel(), new TowerBossLevel()}) {
                Dungeon.depth = 2;
                assertEquals("高塔水潭", level.tileName(Terrain.WATER));
                assertEquals("高塔杂草", level.tileName(Terrain.GRASS));
                for (int tile : new int[]{Terrain.STATUE, Terrain.STATUE_SP, Terrain.REGION_DECO, Terrain.REGION_DECO_ALT}) {
                    assertEquals("高塔装饰", level.tileName(tile));
                    assertTrue(level.tileDesc(tile).contains("装饰高塔"));
                }
                assertTrue(level.tileDesc(Terrain.BOOKSHELF).contains("空白"));
                assertTrue(level.tileDesc(Terrain.ENTRANCE).contains("下层"));
                assertTrue(level.tileDesc(Terrain.EXIT).contains("上层"));
                assertEquals(level.tileDesc(Terrain.EXIT), level.tileDesc(Terrain.UNLOCKED_EXIT));
                Dungeon.depth = 1;
                assertTrue(level.tileDesc(Terrain.ENTRANCE).contains("封死"));
            }
        } finally {
            Dungeon.depth = oldDepth;
            Dungeon.branch = oldBranch;
        }
    }

    @Test public void unknownLakeSwordDoesNotRevealItsActualAbilityDuration() {
        LakeSword sword = new LakeSword();
        sword.level(5);
        sword.levelKnown = false;
        assertTrue(sword.abilityInfo().contains("一般"));
        assertTrue(sword.abilityInfo().contains("_2回合隐形_"));
        sword.levelKnown = true;
        assertFalse(sword.abilityInfo().contains("一般"));
        assertTrue(sword.abilityInfo().contains("_7回合隐形_"));
    }

    @Test public void unknownChainMaceHasChineseBaseDamageDescription() {
        ChainMace mace = new ChainMace();
        mace.level(5);
        mace.levelKnown = false;
        assertTrue(mace.statsInfo().contains("通常"));
        assertTrue(mace.statsInfo().contains("不能用来伏击"));
        assertFalse(mace.statsInfo().contains("java.lang.object"));
    }

    @Test public void towerShopkeeperHasItsOwnDescriptionAndConversation() {
        int oldBranch = Dungeon.branch;
        try {
            Dungeon.branch = TowerLevel.BRANCH;
            Shopkeeper merchant = new Shopkeeper();
            assertTrue(merchant.description().contains("地表商店店主"));
            assertTrue(merchant.chatText().contains("长久探索"));
            Dungeon.branch = 0;
            assertFalse(merchant.description().contains("高塔"));
            assertNotEquals(merchant.description(), new SurfaceShopkeeper().description());
        } finally { Dungeon.branch = oldBranch; }
    }

    @Test public void huntressGateDescriptionTracksClosedAndBrokenTerrain() {
        Level oldLevel = Dungeon.level;
        try {
            HuntressBossLevel level = new HuntressBossLevel();
            level.setSize(32, 32);
            Dungeon.level = level;
            HuntressBossLevel.CityFence gate = new HuntressBossLevel.CityFence();
            gate.tileX = 13;
            gate.tileY = 12;
            gate.tileW = 5;
            gate.tileH = 1;
            level.solid[13 + 12 * level.width()] = true;
            assertEquals("金属城门", gate.name(0, 0));
            assertTrue(gate.desc(0, 0).contains("击败女猎手"));
            level.solid[13 + 12 * level.width()] = false;
            assertTrue(gate.desc(0, 0).contains("碎片"));
            Bundle bundle = new Bundle();
            gate.storeInBundle(bundle);
            HuntressBossLevel.CityFence restored = new HuntressBossLevel.CityFence();
            restored.restoreFromBundle(bundle);
            assertEquals(gate.desc(0, 0), restored.desc(0, 0));
            assertTrue(Messages.get(com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel.class,
                    "gate_desc").contains("DM-300"));
            assertNull(gate.desc(-1, 0));
            assertNull(gate.name(5, 0));
        } finally { Dungeon.level = oldLevel; }
    }

    @Test public void towerLoadingFollowsPhysicalDirectionNotTheDungeonMode() throws Exception {
        assertEquals("上楼中", loading(InterlevelScene.Mode.DESCEND, 0, 0, 1, TowerLevel.BRANCH));
        assertEquals("上楼中", loading(InterlevelScene.Mode.DESCEND, 1, TowerLevel.BRANCH, 2, TowerLevel.BRANCH));
        assertEquals("下楼中", loading(InterlevelScene.Mode.ASCEND, 2, TowerLevel.BRANCH, 1, TowerLevel.BRANCH));
        assertEquals("下楼中", loading(InterlevelScene.Mode.DESCEND, 1, 0, 2, 0));
        assertEquals("上楼中", loading(InterlevelScene.Mode.ASCEND, 2, 0, 1, 0));
        assertEquals(Messages.get(InterlevelScene.Mode.class, "CONTINUE"),
                loading(InterlevelScene.Mode.CONTINUE, 0, 0, 2, TowerLevel.BRANCH));
    }

    @Test public void raidEntryAndReturnUseDifferentLoadingText() throws Exception {
        assertEquals("正在进入飞船内部", loading(InterlevelScene.Mode.RETURN, 0, 0,
                ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH));
        assertEquals("返回中", loading(InterlevelScene.Mode.RETURN,
                ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH, 0, 0));
        assertEquals("读取中", loading(InterlevelScene.Mode.CONTINUE,
                0, 0, ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH));
    }

    @Test public void everyRegisteredBossHasAFlavorPredictionUsingTheTowerLabel() throws Exception {
        Method method = GameScene.class.getDeclaredMethod("towerBossPrediction", int.class, String.class);
        method.setAccessible(true);
        HashSet<String> predictions = new HashSet<>();
        for (TowerBossGenerator.Entry entry : TowerBossGenerator.entries()) {
            String message = (String) method.invoke(null, 10, entry.id());
            assertTrue(message.contains("T10"));
            assertFalse(message.contains("Boss可能"));
            assertTrue(predictions.add(message));
        }
        String fallback = (String) method.invoke(null, 20, "future_boss");
        assertTrue(fallback.contains("T20"));
        assertFalse(fallback.contains("java.lang.object"));
    }

    @Test public void chainMaceWallHintOnlyAppliesToVisibleWallCells() throws Exception {
        Level oldLevel = Dungeon.level;
        try {
            SurfaceTownLevel level = new SurfaceTownLevel();
            level.setSize(5, 5);
            Dungeon.level = level;
            Hero hero = TestHeroFactory.create();
            hero.fieldOfView = new boolean[level.length()];
            hero.fieldOfView[12] = true;
            Method method = ChainMace.class.getDeclaredMethod("isVisibleWallTarget", Hero.class, int.class);
            method.setAccessible(true);
            ChainMace mace = new ChainMace();
            for (int terrain : new int[]{Terrain.WALL, Terrain.WALL_DECO, Terrain.SECRET_DOOR}) {
                level.map[12] = terrain;
                assertEquals(true, method.invoke(mace, hero, 12));
                hero.fieldOfView[12] = false;
                assertEquals(false, method.invoke(mace, hero, 12));
                hero.fieldOfView[12] = true;
            }
            for (int terrain : new int[]{Terrain.DOOR, Terrain.STATUE, Terrain.CHASM, Terrain.EMPTY}) {
                level.map[12] = terrain;
                assertEquals(false, method.invoke(mace, hero, 12));
            }
            assertEquals(false, method.invoke(mace, hero, -1));
            assertEquals(false, method.invoke(mace, hero, level.length()));
        } finally { Dungeon.level = oldLevel; }
    }

    @Test public void activatedKeysExistInBothLanguagesWithMatchingFormatArguments() throws Exception {
        String[] groups = {"scenes", "actors", "items", "levels"};
        String[] prefixes = {"scenes.gamescene.tower_boss_prediction_", "scenes.interlevelscene.tower_",
                "scenes.interlevelscene.raid_enter", "actors.mobs.npcs.shopkeeper.tower_desc",
                "actors.mobs.npcs.shopkeeper.talk_tower", "items.weapon.melee.tier6.chainmace.typical_stats_desc",
                "items.weapon.melee.tier6.chainmace.wall_target", "items.weapon.melee.tier6.lakesword.typical_ability_desc",
                "levels.huntressbosslevel.gate_desc", "levels.surfacetownlevel.dungeon_entrance_",
                "levels.surfacetownlevel.tower_entrance_", "levels.surfacetownlevel.bookshelf_desc",
                "levels.surfacetownlevel.region_deco_", "levels.minigame.extraction.extractionraidlevel.",
                "levels.towers.towerlevel.water_name", "levels.towers.towerlevel.grass_name",
                "levels.towers.towerlevel.decoration_", "levels.towers.towerlevel.bookshelf_desc",
                "levels.towers.towerlevel.entrance_desc", "levels.towers.towerlevel.exit_desc"};
        int count = 0;
        for (String group : groups) {
            Properties english = properties(group, "");
            Properties chinese = properties(group, "_zh");
            for (String key : chinese.stringPropertyNames()) {
                boolean selected = false;
                for (String prefix : prefixes) selected |= key.startsWith(prefix);
                if (!selected) continue;
                count++;
                assertTrue(key, english.containsKey(key));
                String zh = chinese.getProperty(key);
                String en = english.getProperty(key);
                Object[] args = key.endsWith("typical_stats_desc") ? new Object[]{12, 46}
                        : key.endsWith("typical_ability_desc") ? new Object[]{2}
                        : key.contains("tower_boss_prediction_") ? new Object[]{"T10"} : new Object[]{};
                assertFalse(key, Messages.format(zh, args).contains("%"));
                assertFalse(key, Messages.format(en, args).contains("%"));
                assertEquals(key, zh.chars().filter(c -> c == '_').count(), en.chars().filter(c -> c == '_').count());
            }
        }
        assertEquals(35, count);
    }

    private static Properties properties(String group, String language) throws Exception {
        File root = new File("src/main/assets");
        if (!root.isDirectory()) root = new File("core/src/main/assets");
        Path path = new File(root, "messages/" + group + "/" + group + language + ".properties").toPath();
        Properties properties = new Properties();
        properties.load(new StringReader(new String(java.nio.file.Files.readAllBytes(path), StandardCharsets.UTF_8)));
        return properties;
    }

    @Test public void defaultEnglishResourcesAreUsedByActualDisplayEntrypoints() throws Exception {
        int oldBranch = Dungeon.branch;
        try {
            Messages.setup(Languages.ENGLISH);
            assertEquals("Tower entrance", new SurfaceTownLevel().tileName(Terrain.ENTRANCE));
            assertEquals("Ship cargo crate", new ExtractionRaidLevel().tileName(Terrain.STATUE));
            assertEquals("Tower decoration", new TowerLevel().tileName(Terrain.REGION_DECO));
            assertEquals("Ascending", loading(InterlevelScene.Mode.DESCEND, 0, 0, 1, TowerLevel.BRANCH));
            assertEquals("Entering the ship", loading(InterlevelScene.Mode.RETURN, 0, 0,
                    ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH));
            assertTrue(new LakeSword().abilityInfo().contains("typically"));
            Dungeon.branch = TowerLevel.BRANCH;
            assertTrue(new Shopkeeper().chatText().contains("tower"));
            assertTrue(GameScene.towerBossPrediction(10, TowerBossGenerator.HUNGER_KNIGHT_ID).contains("hunger"));
        } finally {
            Dungeon.branch = oldBranch;
            Messages.setup(Languages.CHI_SMPL);
        }
    }

    @Test public void loadingDotsReuseTheMessageResolvedBeforeTheWorkerStarts() throws Exception {
        File root = new File("src/main/java");
        if (!root.isDirectory()) root = new File("core/src/main/java");
        String source = new String(java.nio.file.Files.readAllBytes(new File(root,
                "com/shatteredpixel/shatteredpixeldungeon/scenes/InterlevelScene.java").toPath()), StandardCharsets.UTF_8);
        int snapshot = source.indexOf("loadingMessage = resolveLoadingMessage(");
        assertTrue(snapshot >= 0);
        assertTrue(source.indexOf("thread = new Thread", snapshot) > snapshot);
        String update = source.substring(source.indexOf("public void update()"));
        assertTrue(update.contains("String text = loadingMessage;"));
        assertFalse(update.contains("Messages.get(Mode.class, mode.name())"));
        assertFalse(update.contains("resolveLoadingMessage("));
    }

    @Test public void loadingTextAndBlackBackgroundAgreeOnSurfaceAndRaidDestinations() throws Exception {
        assertTrue(InterlevelScene.usesBlackBackground(0, 0));
        assertTrue(InterlevelScene.usesBlackBackground(ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH));
        assertFalse(InterlevelScene.usesBlackBackground(1, TowerLevel.BRANCH));
        assertFalse(InterlevelScene.usesBlackBackground(20, 0));
        assertEquals("上楼中", loading(InterlevelScene.Mode.DESCEND, 0, 0, 1, TowerLevel.BRANCH));
        assertEquals("正在进入飞船内部", loading(InterlevelScene.Mode.RETURN, 0, 0,
                ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH));
        assertEquals("返回中", loading(InterlevelScene.Mode.RETURN,
                ExtractionRaidLevel.DEPTH, ExtractionRaidLevel.BRANCH, 0, 0));
    }

    private static String loading(InterlevelScene.Mode mode, int fromDepth, int fromBranch,
                                  int toDepth, int toBranch) throws Exception {
        Method method = InterlevelScene.class.getDeclaredMethod("resolveLoadingMessage",
                InterlevelScene.Mode.class, int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        return (String) method.invoke(null, mode, fromDepth, fromBranch, toDepth, toBranch);
    }
}
