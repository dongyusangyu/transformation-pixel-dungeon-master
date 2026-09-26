package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CursedFlameTrapsTest {

    private Level oldLevel;
    private Hero oldHero;
    private Game oldGame;
    private String oldVersion;
    private TestLevel level;
    private Hero hero;
    private HeadlessItemSprites sprites;

    @Before public void setUp() {
        oldLevel = Dungeon.level;
        oldHero = Dungeon.hero;
        oldGame = Game.instance;
        oldVersion = Game.version;
        new Game(Scene.class, null);
        Game.version = "TEST";
        sprites = new HeadlessItemSprites();
        level = new TestLevel();
        level.setSize(7, 7);
        level.blobs = new HashMap<>();
        level.heaps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.traps = new SparseArray<>();
        level.customTiles = new ArrayList<>();
        for (int i = 0; i < level.length(); i++) level.passable[i] = true;
        Dungeon.level = level;
        hero = TestHeroFactory.create();
        hero.pos = 10;
        Dungeon.hero = hero;
    }

    @After public void tearDown() {
        Dungeon.level = oldLevel;
        Dungeon.hero = oldHero;
        Game.instance = oldGame;
        Game.version = oldVersion;
        if (sprites != null) sprites.close();
    }

    @Test public void trapsUseGreenShapesAndRemainSearchableAndHiddenByDefault() {
        CursedFlameTrap flame = new CursedFlameTrap();
        SoulScorchTrap soul = new SoulScorchTrap();

        assertEquals(Trap.GREEN, flame.color);
        assertEquals(Trap.STARS, flame.shape);
        assertEquals(Trap.GREEN, soul.color);
        assertEquals(Trap.LARGE_DOT, soul.shape);
        assertTrue(flame.canBeHidden);
        assertTrue(flame.canBeSearched);
        assertTrue(soul.canBeHidden);
        assertTrue(soul.canBeSearched);
        assertTrue(Bestiary.TRAP.entities().contains(CursedFlameTrap.class));
        assertTrue(Bestiary.TRAP.entities().contains(SoulScorchTrap.class));
    }

    @Test public void randomTrapSetsOnlyContainEachTrapInItsSpecifiedRegion() throws Exception {
        assertTrue(trapClasses(CavesLevel.class).contains(CursedFlameTrap.class));
        assertTrue(trapClasses(CityLevel.class).contains(CursedFlameTrap.class));
        assertFalse(trapClasses(HallsLevel.class).contains(CursedFlameTrap.class));
        assertFalse(trapClasses(CavesLevel.class).contains(SoulScorchTrap.class));
        assertFalse(trapClasses(CityLevel.class).contains(SoulScorchTrap.class));
        assertTrue(trapClasses(HallsLevel.class).contains(SoulScorchTrap.class));
        assertEquals(trapClasses(CavesLevel.class).size(), trapChances(CavesLevel.class).length);
        assertEquals(trapClasses(CityLevel.class).size(), trapChances(CityLevel.class).length);
        assertEquals(trapClasses(HallsLevel.class).size(), trapChances(HallsLevel.class).length);
    }

    @Test public void flameTrapSeedsThreeByThreeForTwoTurnsAndIncludesWaterAndPit() {
        int center = 3 + 3 * level.width();
        int water = center + 1;
        int pit = center + level.width();
        level.water[water] = true;
        level.pit[pit] = true;

        new CursedFlameTrap().set(center).activate();

        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertNotNull(flame);
        assertEquals(9, countSeeded(flame));
        assertEquals(2, flame.cur[center]);
        assertEquals(2, flame.cur[water]);
        assertEquals(2, flame.cur[pit]);
    }

    @Test public void flameTrapSkipsInertWallsAndCanIgniteCombustibleObstacles() {
        int center = 3 + 3 * level.width();
        int wall = center - 1;
        int barricade = center + 1;
        level.solid[wall] = true;
        level.passable[wall] = false;
        level.solid[barricade] = true;
        level.flamable[barricade] = true;
        level.passable[barricade] = false;

        new CursedFlameTrap().set(center).activate();

        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertEquals(0, flame.cur[wall]);
        assertEquals(2, flame.cur[barricade]);
    }

    @Test public void soulTrapSeedsFiveByFiveForFiveTurnsClippedAtMapEdges() {
        int center = 1 + level.width();

        new SoulScorchTrap().set(center).activate();

        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertNotNull(flame);
        assertEquals(9, countSeeded(flame));
        assertEquals(5, flame.cur[center]);
        assertEquals(5, flame.cur[2 + 2 * level.width()]);
        assertEquals(0, flame.cur[4 + 4 * level.width()]);
    }

    @Test public void ingredientWandsOnActiveCursedFlameTrapsTransformAndTriggerOnce() throws Exception {
        CountingCursedFlameTrap trap = new CountingCursedFlameTrap();
        int cell = 3 + 3 * level.width();
        level.set(cell, Terrain.TRAP);
        level.setTrap(trap, cell);
        WandOfMagicMissile input = new WandOfMagicMissile();
        input.identify();

        invokeThrow(input, cell);
        level.pressCell(cell);

        assertFalse(trap.active);
        assertEquals(1, trap.activations);
        assertEquals(9, countSeeded((CursedFlame) level.blobs.get(CursedFlame.class)));
        assertEquals(1, level.heaps.get(cell).items.size());
        assertTrue(level.heaps.get(cell).peek() instanceof WandOfCursedFlame);
        WandOfCursedFlame result = (WandOfCursedFlame) level.heaps.get(cell).peek();
        assertEquals(0, result.level());
        assertTrue(result.cursed);
    }

    @Test public void fireblastConvertsButOtherWandsAndInertTrapsDoNot() throws Exception {
        int cell = 3 + 3 * level.width();
        CountingCursedFlameTrap trap = new CountingCursedFlameTrap();
        level.set(cell, Terrain.TRAP);
        level.setTrap(trap, cell);

        WandOfFireblast fireblast = new WandOfFireblast();
        fireblast.identify();
        invokeThrow(fireblast, cell);
        level.pressCell(cell);
        assertEquals(1, trap.activations);

        CountingSoulScorchTrap inert = new CountingSoulScorchTrap();
        inert.active = false;
        level.set(cell + 1, Terrain.TRAP);
        level.setTrap(inert, cell + 1);
        WandOfMagicMissile notIdentified = new WandOfMagicMissile();
        assertNull(CursedFlameTrapConversion.transformThrownWand(notIdentified, cell + 1));
        assertNull(CursedFlameTrapConversion.transformThrownWand(new WandOfFrost(), cell));
        WandOfMagicMissile offTrap = new WandOfMagicMissile();
        offTrap.identify();
        assertNull(CursedFlameTrapConversion.transformThrownWand(offTrap, cell + 2));
        assertEquals(1, trap.activations);

        WandOfMagicMissile input = new WandOfMagicMissile();
        input.identify();
        invokeThrow(input, cell + 1);
        assertFalse(inert.active);
        assertTrue(level.heaps.get(cell + 1).peek() instanceof WandOfMagicMissile);
        assertFalse(level.heaps.get(cell + 1).peek() instanceof WandOfCursedFlame);
    }

    @Test public void activeTrapConvertsAnUnidentifiedIngredientWand() throws Exception {
        int cell = 3 + 3 * level.width();
        level.set(cell, Terrain.TRAP);
        level.setTrap(new CountingCursedFlameTrap(), cell);
        WandOfMagicMissile input = new WandOfMagicMissile();
        assertFalse(input.isIdentified());

        invokeThrow(input, cell);

        assertNotNull(level.heaps.get(cell));
        assertTrue("trap conversion is an alternative to the identified-wand alchemy recipe",
                level.heaps.get(cell).peek() instanceof WandOfCursedFlame);
    }

    private static int countSeeded(CursedFlame flame) {
        int count = 0;
        for (int value : flame.cur) if (value > 0) count++;
        return count;
    }

    private static java.util.List<Class<?>> trapClasses(Class<?> levelClass) throws Exception {
        java.lang.reflect.Method method = levelClass.getDeclaredMethod("trapClasses");
        method.setAccessible(true);
        return java.util.Arrays.asList((Class<?>[]) method.invoke(levelClass.getDeclaredConstructor().newInstance()));
    }

    private static float[] trapChances(Class<?> levelClass) throws Exception {
        java.lang.reflect.Method method = levelClass.getDeclaredMethod("trapChances");
        method.setAccessible(true);
        return (float[]) method.invoke(levelClass.getDeclaredConstructor().newInstance());
    }

    private static void invokeThrow(Wand wand, int cell) throws Exception {
        java.lang.reflect.Method method = Wand.class.getDeclaredMethod("onThrow", int.class);
        method.setAccessible(true);
        method.invoke(wand, cell);
    }

    private static class CountingCursedFlameTrap extends CursedFlameTrap {
        int activations;
        @Override public void activate() { activations++; super.activate(); }
    }

    private static class CountingSoulScorchTrap extends SoulScorchTrap {
        int activations;
        @Override public void activate() { activations++; super.activate(); }
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
