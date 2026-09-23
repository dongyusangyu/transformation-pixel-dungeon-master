package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeadEndLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Random;
import com.watabou.utils.PathFinder;
import com.watabou.utils.GameSettings;
import com.watabou.utils.SparseArray;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class StabilityBoundaryTest {
    private Level previousLevel;
    private Hero previousHero;
    private Files previousFiles;
    private TestLevel level;
    private Game previousGame;
    private Application previousApplication;
    private Preferences previousPreferences;

    @Before public void setUp() throws Exception {
        GdxNativesLoader.load();
        previousLevel = Dungeon.level;
        previousHero = Dungeon.hero;
        previousFiles = Gdx.files;
        previousGame = Game.instance;
        previousApplication = Gdx.app;
        Field preferences = GameSettings.class.getDeclaredField("prefs");
        preferences.setAccessible(true);
        previousPreferences = (Preferences) preferences.get(null);
        GameSettings.set((Preferences) Proxy.newProxyInstance(Preferences.class.getClassLoader(),
                new Class[]{Preferences.class}, (proxy, method, args) -> {
                    if (method.getName().startsWith("get") && args != null && args.length == 2) return args[1];
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == Preferences.class) return proxy;
                    return null;
                }));
        Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
                new Class[]{Application.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getType")) return Application.ApplicationType.Desktop;
                    return null;
                });
        new Game(Scene.class, null);
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
                new Class[]{Files.class}, (proxy, method, args) -> {
                    java.io.File root = new java.io.File("src/main/assets");
                    if (!root.isDirectory()) root = new java.io.File("core/src/main/assets");
                    return new FileHandle(new java.io.File(root, (String) args[0]));
                });
        Dungeon.hero = TestHeroFactory.create();
        Dungeon.hero.heroClass = HeroClass.WARRIOR;
        Dungeon.hero.subClass = HeroSubClass.NONE;
        // These fixtures explicitly control visibility rather than recomputing it on Light changes.
        Dungeon.hero.pos = -1;
        level = new TestLevel();
        level.setSize(7, 7);
        level.heaps = new SparseArray<>();
        level.mobs = new HashSet<>();
        Arrays.fill(level.solid, true);
        Dungeon.level = level;

        // Abort old unbounded retry loops without leaving a spinning timeout thread.
        Field field = Random.class.getDeclaredField("generators");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayDeque<java.util.Random> generators = (ArrayDeque<java.util.Random>) field.get(null);
        generators.push(new java.util.Random(1) {
            private int calls;
            @Override public int nextInt(int bound) {
                if (++calls > 2000) throw new AssertionError("Unbounded random retry loop");
                return super.nextInt(bound);
            }
        });
    }

    @After public void tearDown() {
        Random.popGenerator();
        Actor.clear();
        Dungeon.level = previousLevel;
        Dungeon.hero = previousHero;
        Gdx.files = previousFiles;
        Game.instance = previousGame;
        Gdx.app = previousApplication;
        GameSettings.set(previousPreferences);
    }

    @Test public void transformOnUnsupportedFloorDetachesWithoutSpawning() {
        DeadEndLevel unsupported = new DeadEndLevel();
        unsupported.setSize(7, 7);
        Dungeon.level = unsupported;
        Rat rat = new Rat();
        rat.pos = 24;
        ChampionEnemy.Transform buff = Buff.affect(rat, ChampionEnemy.Transform.class);
        buff.detach();
        assertNull(rat.buff(ChampionEnemy.Transform.class));
    }

    @Test public void transformWithMissingLevelStillDetaches() {
        Rat rat = new Rat();
        ChampionEnemy.Transform buff = Buff.affect(rat, ChampionEnemy.Transform.class);
        Dungeon.level = null;
        buff.detach();
        assertNull(rat.buff(ChampionEnemy.Transform.class));
    }

    @Test public void transformSpawnsExactlyOnceAndPreservesAlignment() {
        Rat rat = new Rat();
        rat.pos = 24;
        rat.alignment = Char.Alignment.ALLY;
        level.replacement = new Rat();
        ChampionEnemy.Transform buff = Buff.affect(rat, ChampionEnemy.Transform.class);
        buff.detach();
        buff.detach();
        assertEquals(1, level.spawnCalls);
        assertNull(rat.buff(ChampionEnemy.Transform.class));
        assertTrue(level.mobs.contains(level.replacement));
        assertEquals(24, level.replacement.pos);
        assertEquals(Char.Alignment.ALLY, level.replacement.alignment);
        assertEquals(Math.round(level.replacement.HT / 3), level.replacement.HP);
        assertFalse(level.replacement.buffs(ChampionEnemy.class).isEmpty());
    }

    @Test public void actorRemovalAlsoDetachesTransformOnlyOnce() {
        Rat rat = new Rat();
        rat.pos = 24;
        ChampionEnemy.Transform buff = Buff.affect(rat, ChampionEnemy.Transform.class);
        Actor.add(rat);
        Actor.remove(rat);
        buff.detach();
        assertEquals(1, level.spawnCalls);
        assertNull(rat.buff(ChampionEnemy.Transform.class));
    }

    @Test public void unattachedTransformCanBeRemovedSafely() {
        new ChampionEnemy.Transform().detach();
        assertEquals(0, level.spawnCalls);
    }

    @Test public void transformKeepsPassiveReplacementPassive() {
        Rat source = new Rat();
        source.pos = 24;
        level.replacement = new Rat();
        level.replacement.state = level.replacement.PASSIVE;
        Buff.affect(source, ChampionEnemy.Transform.class).detach();
        assertSame(level.replacement.PASSIVE, level.replacement.state);
    }

    @Test public void invalidTransformPositionDoesNotSpawnOutsideTheMap() {
        Rat source = new Rat();
        source.pos = level.length();
        Buff.affect(source, ChampionEnemy.Transform.class).detach();
        assertEquals(0, level.spawnCalls);
        assertNull(source.buff(ChampionEnemy.Transform.class));
    }

    @Test public void brightFistCompletesHalfHealthWhenAllCellsAreVisible() {
        halfHealthWithoutDestination(new YogFist.BrightFist() {
            @Override protected boolean isNearYog() { return false; }
        });
        assertNotNull(Dungeon.hero.buff(Blindness.class));
    }

    @Test public void darkFistCompletesHalfHealthWhenAllCellsAreVisible() {
        Buff.affect(Dungeon.hero, Light.class);
        halfHealthWithoutDestination(new YogFist.DarkFist() {
            @Override protected boolean isNearYog() { return false; }
        });
        assertNull(Dungeon.hero.buff(Light.class));
    }

    @Test public void brightFistStillTeleportsToTheOnlyLegalDestination() {
        destinationFixture();
        TestBrightFist fist = new TestBrightFist();
        fist.pos = 40;
        fist.damage(160, new Object(), DamageTag.PHYSICAL);
        assertEquals(24, fist.pos);
        assertEquals(150, fist.HP);
        assertNotNull(Dungeon.hero.buff(Blindness.class));
        assertSame(fist.WANDERING, fist.state);
        fist.damage(10, new Object(), DamageTag.PHYSICAL);
        assertEquals(140, fist.HP);
    }

    @Test public void darkFistStillTeleportsAndRemovesLight() {
        destinationFixture();
        Buff.affect(Dungeon.hero, Light.class);
        YogFist fist = new YogFist.DarkFist() {
            @Override protected boolean isNearYog() { return false; }
        };
        fist.pos = 40;
        fist.damage(160, new Object(), DamageTag.PHYSICAL);
        assertEquals(24, fist.pos);
        assertEquals(150, fist.HP);
        assertNull(Dungeon.hero.buff(Light.class));
        assertSame(fist.WANDERING, fist.state);
    }

    @Test public void occupiedDestinationIsRejected() {
        destinationFixture();
        Rat occupant = new Rat();
        occupant.pos = 24;
        Actor.add(occupant);
        assertEquals(-1, new TestBrightFist().destination());
    }

    @Test public void disconnectedDestinationIsRejected() {
        destinationFixture();
        level.passable[16] = false;
        assertEquals(-1, new TestBrightFist().destination());
    }

    @Test public void invalidExitAndMissingLevelHaveNoDestination() {
        destinationFixture();
        level.exitCell = -1;
        assertEquals(-1, new TestBrightFist().destination());
        level.exitCell = level.length();
        assertEquals(-1, new TestBrightFist().destination());
        Dungeon.level = null;
        assertEquals(-1, new TestBrightFist().destination());
    }

    @Test public void nonPassableStartNextToPathRemainsEligibleAsBefore() {
        destinationFixture();
        level.passable[24] = false;
        assertTrue(PathFinder.getStep(24, 8, level.passable) >= 0);
        assertEquals(24, new TestBrightFist().destination());
    }

    private void destinationFixture() {
        floor(8);
        floor(16);
        floor(24);
        floor(40);
        Arrays.fill(level.heroFOV, true);
        level.heroFOV[24] = false;
    }

    private void halfHealthWithoutDestination(YogFist fist) {
        Arrays.fill(level.heroFOV, true);
        floor(24);
        fist.pos = 24;
        fist.damage(160, new Object(), DamageTag.PHYSICAL);
        assertEquals(fist.HT / 2, fist.HP);
        assertEquals(24, fist.pos);
        assertSame(fist.WANDERING, fist.state);
    }

    @Test public void enclosedChestPreservesTheItemAndItsLock() {
        Heap chest = chest(24, Heap.Type.LOCKED_CHEST);
        Item item = new Item();
        assertSame(chest, level.drop(item, 24));
        assertTrue(chest.items.contains(item));
        assertEquals(Heap.Type.LOCKED_CHEST, chest.type);
    }

    @Test public void closedChestCyclePreservesTheItemWithoutRecursion() {
        Heap first = chest(24, Heap.Type.LOCKED_CHEST);
        Heap second = chest(25, Heap.Type.CRYSTAL_CHEST);
        Item item = new Item();
        assertSame(first, level.drop(item, 24));
        assertTrue(first.items.contains(item));
        assertFalse(second.items.contains(item));
        assertEquals(Heap.Type.CRYSTAL_CHEST, second.type);
    }

    @Test public void chestChainFindsAFreeDropCell() {
        chest(24, Heap.Type.LOCKED_CHEST);
        chest(25, Heap.Type.CRYSTAL_CHEST);
        floor(26);
        Item item = new Item();
        Heap result = level.drop(item, 24);
        assertEquals(26, result.pos);
        assertTrue(result.items.contains(item));
    }

    @Test public void edgeChestDoesNotReadOutsideMapOrWrapRows() {
        Heap chest = chest(0, Heap.Type.LOCKED_CHEST);
        floor(6);
        Item item = new Item();
        assertSame(chest, level.drop(item, 0));
        assertTrue(chest.items.contains(item));
    }

    @Test public void ordinaryDropStillMergesIntoTheExistingHeap() {
        floor(24);
        Heap first = level.drop(new Item(), 24);
        Item item = new Item();
        assertSame(first, level.drop(item, 24));
        assertEquals(2, first.items.size());
    }

    @Test public void chestStillDropsIntoAnAdjacentAvoidCell() {
        chest(24, Heap.Type.LOCKED_CHEST);
        level.map[25] = Terrain.EMPTY;
        level.avoid[25] = true;
        Heap result = level.drop(new Item(), 24);
        assertEquals(25, result.pos);
        assertEquals(Heap.Type.HEAP, result.type);
    }

    @Test public void chestStillSendsAnItemIntoTheChasmExactlyOnce() {
        chest(24, Heap.Type.LOCKED_CHEST);
        level.map[25] = Terrain.CHASM;
        level.avoid[25] = true;
        SparseArray<java.util.ArrayList<Item>> previous = Dungeon.droppedItems;
        Dungeon.droppedItems = new SparseArray<>();
        try {
            Item item = new Item();
            level.drop(item, 24);
            int count = 0;
            for (java.util.ArrayList<Item> items : Dungeon.droppedItems.valueList()) {
                for (Item dropped : items) if (dropped == item) count++;
            }
            assertEquals(1, count);
            assertNull(level.heaps.get(25));
            assertFalse(level.heaps.get(24).items.contains(item));
        } finally {
            Dungeon.droppedItems = previous;
        }
    }

    private void floor(int cell) {
        level.map[cell] = Terrain.EMPTY;
        level.passable[cell] = true;
        level.solid[cell] = false;
    }

    private Heap chest(int cell, Heap.Type type) {
        floor(cell);
        Heap heap = new Heap();
        heap.pos = cell;
        heap.type = type;
        heap.drop(new Item());
        level.heaps.put(cell, heap);
        return heap;
    }

    private static class TestLevel extends Level {
        Mob replacement;
        int spawnCalls;
        int exitCell = 8;
        @Override public Mob createMob() { spawnCalls++; return replacement; }
        @Override public int exit() { return exitCell; }
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() { }
        @Override protected void createItems() { }
    }

    private static class TestBrightFist extends YogFist.BrightFist {
        @Override protected boolean isNearYog() { return false; }
        int destination() { return halfHealthTeleportDestination(); }
    }
}
