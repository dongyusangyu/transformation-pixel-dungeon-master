package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TwinDemonEyes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.noosa.Game;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StoneOfBlastTwinDemonEyesTest {

    private static HeadlessItemSprites sprites;

    @BeforeClass public static void installSprites() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void restoreSprites() { sprites.close(); }

    private Hero oldHero;
    private Level oldLevel;
    private Game oldGame;
    private Hero hero;
    private TestLevel level;
    private TwinDemonEyes eyes;

    @Before public void setUp() {
        oldHero = Dungeon.hero;
        oldLevel = Dungeon.level;
        oldGame = Game.instance;
        if (Game.instance == null) Game.instance = headlessGame();
        Actor.clear();
        Actor.resetNextID();

        hero = TestHeroFactory.create();
        hero.subClass = HeroSubClass.NONE;
        hero.pos = 30;
        hero.HP = hero.HT = 100;
        Dungeon.hero = hero;

        level = new TestLevel();
        level.setSize(9, 9);
        level.blobs = new HashMap<>();
        level.mobs = new HashSet<>();
        level.heaps = new com.watabou.utils.SparseArray<>();
        level.plants = new com.watabou.utils.SparseArray<>();
        level.traps = new com.watabou.utils.SparseArray<>();
        level.customTiles = new java.util.ArrayList<>();
        level.heroFOV = new boolean[level.length()];
        level.discoverable = new boolean[level.length()];
        for (int cell = 0; cell < level.length(); cell++) {
            level.passable[cell] = true;
            level.discoverable[cell] = true;
        }
        Dungeon.level = level;

        eyes = new TwinDemonEyes();
        hero.belongings.backpack.items.add(eyes);
        Actor.add(hero);
    }

    @After public void tearDown() {
        Actor.clear();
        Actor.resetNextID();
        Dungeon.hero = oldHero;
        Dungeon.level = oldLevel;
        Game.instance = oldGame;
    }

    @Test public void blastLocksNearestSurvivingHostileInItsAreaOnlyOnce() {
        Rat nearer = mob(31);
        Rat farther = mob(49);
        mob(41);

        stone().detonate(40);

        assertTrue(eyes.isLocked());
        assertEquals(nearer.id(), eyes.lockedTargetId());
        assertTrue(nearer.isAlive());
        assertTrue(farther.isAlive());
    }

    @Test public void blastWithoutHostilesDoesNotStartLock() {
        Rat ally = mob(41);
        ally.alignment = Char.Alignment.ALLY;

        stone().detonate(40);

        assertFalse(eyes.isLocked());
    }

    private Rat mob(int cell) {
        Rat rat = new Rat();
        rat.pos = cell;
        rat.HP = rat.HT = 1000;
        Actor.add(rat);
        level.mobs.add(rat);
        return rat;
    }

    private static TestStone stone() { return new TestStone(); }

    private static Game headlessGame() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Game) ((Unsafe) field.get(null)).allocateInstance(Game.class);
        } catch (Exception error) {
            throw new AssertionError(error);
        }
    }

    private static class TestStone extends StoneOfBlast {
        void detonate(int cell) { activate(cell); }
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
