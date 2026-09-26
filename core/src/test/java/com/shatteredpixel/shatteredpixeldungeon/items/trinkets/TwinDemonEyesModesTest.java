package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Bundle;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

public class TwinDemonEyesModesTest {

    private static HeadlessItemSprites sprites;
    private Level oldLevel;
    private Hero oldHero;
    private TestLevel level;
    private Hero hero;

    @BeforeClass public static void installSheets() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void restoreSheets() { sprites.close(); }

    @Before public void setUp() {
        oldLevel = Dungeon.level;
        oldHero = Dungeon.hero;
        level = new TestLevel();
        level.setSize(9, 9);
        level.blobs = new HashMap<>();
        level.mobs = new HashSet<>();
        level.heaps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.traps = new SparseArray<>();
        level.customTiles = new ArrayList<>();
        level.discoverable = new boolean[level.length()];
        level.heroFOV = new boolean[level.length()];
        for (int i = 0; i < level.length(); i++) level.passable[i] = true;
        Dungeon.level = level;
        Dungeon.depth = 5;
        Dungeon.branch = 0;
        hero = TestHeroFactory.create();
        hero.pos = 40;
        hero.HP = hero.HT = 20;
        hero.mindVisionEnemies = new ArrayList<>();
        Dungeon.hero = hero;
    }

    @After public void tearDown() {
        Dungeon.level = oldLevel;
        Dungeon.hero = oldHero;
        Actor.clear();
    }

    @Test public void flameModeHitsFirstProjectileCollisionAndAppliesShortBurn() {
        RecordingGnoll blocker = mob(42);
        RecordingGnoll target = mob(44);
        TwinDemonEyes eyes = eyesAtLevel(2, TwinDemonEyes.Mode.FLAME_EYE, target);

        eyes.recordLockTurn(target.pos);
        eyes.onLockRound(target);

        assertTrue("the direct 1+2L hit also applies the shared burn's initial tick",
                blocker.totalDamage >= 5 && blocker.totalDamage <= 11);
        assertEquals(0, target.totalDamage);
        assertTrue("flame-eye impacts follow cursed-burning resistance",
                blocker.allTags.contains(DamageTag.PHYSICAL));
        assertFalse(blocker.allTags.contains(DamageTag.MAGICAL));
        assertTrue(blocker.allTags.contains(DamageTag.FIRE));
		assertEquals(4f, blocker.buff(CursedBurning.class).remaining(), 0.01f);
    }

    @Test public void flameEyeFiresEveryOtherLockTurnAtEachLevel() {
        int[] expectedShots = {2, 3, 4, 5};
        for (int level = 0; level <= 3; level++) {
            TwinDemonEyes eyes = new TwinDemonEyes();
            eyes.level(level);
            int shots = 0;
            for (int turn = 1; turn <= eyes.lockDuration(); turn++) {
                if (TwinDemonEyes.isFlameShotTurn(turn)) shots++;
            }
            assertEquals(expectedShots[level], shots);
            assertEquals(expectedShots[level], (eyes.lockDuration() + 1) / 2);
        }
    }

    @Test public void flameEyeLeavesOneFullLockTurnBetweenShots() {
        RecordingGnoll target = mob(42);
        TwinDemonEyes eyes = eyesAtLevel(0, TwinDemonEyes.Mode.FLAME_EYE, target);

        eyes.recordLockTurn(target.pos);
        eyes.onLockRound(target);
        int afterFirstShot = target.totalDamage;
        eyes.recordLockTurn(target.pos);
        eyes.onLockRound(target);
        assertEquals(afterFirstShot, target.totalDamage);
        eyes.recordLockTurn(target.pos);
        eyes.onLockRound(target);
        assertTrue(target.totalDamage > afterFirstShot);
    }

    @Test public void laterFlameShotUsesOnePlusTrinketLevelBurnDuration() {
        RecordingGnoll target = mob(42);
        TwinDemonEyes eyes = eyesAtLevel(2, TwinDemonEyes.Mode.FLAME_EYE, target);

        eyes.recordLockTurn(target.pos);
        eyes.recordLockTurn(target.pos);
        eyes.recordLockTurn(target.pos);
        eyes.onLockRound(target);

        assertEquals(3f, target.buff(CursedBurning.class).remaining(), 0.01f);
    }

    @Test public void flameModeCannotHitThroughWallAndSeedsFlameOnTheBlockingWall() {
        level.solid[43] = true;
        level.passable[43] = false;
        level.flamable[43] = true;
        RecordingGnoll target = mob(44);
        TwinDemonEyes eyes = eyesAtLevel(1, TwinDemonEyes.Mode.FLAME_EYE, target);

        eyes.recordLockTurn(target.pos);
        eyes.onLockRound(target);

        assertEquals(0, target.totalDamage);
        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertNotNull(flame);
        assertTrue("a flammable collision receives cursed flame", flame.cur[43] > 0);
    }

    @Test public void nonFlammableWallSeedsTheLastFreeCellInstead() {
        level.solid[43] = true;
        level.passable[43] = false;
        RecordingGnoll target = mob(44);
        TwinDemonEyes eyes = eyesAtLevel(1, TwinDemonEyes.Mode.FLAME_EYE, target);

        eyes.recordLockTurn(target.pos);
        eyes.onLockRound(target);

        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertNotNull(flame);
        assertEquals(2, flame.cur[42]);
        assertEquals(0, flame.cur[43]);
    }

    @Test public void laserPiercesCharactersAndSolidTerrainButDamagesEachCharOnce() {
        hero.pos = 22;
        level.solid[40] = true;
        level.passable[40] = false;
        RecordingGnoll first = mob(31);
        RecordingGnoll second = mob(40);
        RecordingGnoll aimed = mob(58);
        TwinDemonEyes eyes = eyesAtLevel(2, TwinDemonEyes.Mode.LASER_EYE, aimed);
        assertSame(aimed, Actor.findChar(aimed.pos));
        assertTrue(new Ballistica(hero.pos, aimed.pos, Ballistica.WONT_STOP).path.contains(aimed.pos));
        eyes.recordLockTurn(aimed.pos);

        eyes.finishLock(TwinDemonEyes.FinishReason.TURN_LIMIT);

        int minimum = 5 + 2;
        int maximum = minimum;
        assertTrue(first.totalDamage >= minimum && first.totalDamage <= maximum);
        assertTrue(second.totalDamage >= minimum && second.totalDamage <= maximum);
        assertTrue(aimed.totalDamage >= minimum && aimed.totalDamage <= maximum);
        assertTrue(first.allTags.contains(DamageTag.MAGICAL));
        assertEquals(TwinDemonEyes.Mode.FLAME_EYE, eyes.mode());
    }

    @Test public void laserEarlyFinishSettlesOnceAndFloorChangeNeverDamagesOldLevel() {
        hero.pos = 22;
        RecordingGnoll target = mob(49);
        RecordingGnoll alongRay = mob(40);
        TwinDemonEyes eyes = eyesAtLevel(0, TwinDemonEyes.Mode.LASER_EYE, target);
        assertTrue(eyes.isLocked());
        eyes.recordLockTurn(target.pos);
        eyes.recordLockTurn(target.pos);
        Actor.remove(target);
        eyes.finishLock(TwinDemonEyes.FinishReason.TARGET_LOST);
        int damageAfterFinish = target.totalDamage;
        assertTrue("a dead target's saved cell still directs the charged ray",
                alongRay.totalDamage >= 5 && alongRay.totalDamage <= 10);
        eyes.finishLock(TwinDemonEyes.FinishReason.TARGET_LOST);
        assertEquals(0, damageAfterFinish);
        assertEquals(damageAfterFinish, target.totalDamage);

        RecordingGnoll oldTarget = mob(45);
        TwinDemonEyes next = eyesAtLevel(0, TwinDemonEyes.Mode.LASER_EYE, oldTarget);
        next.recordLockTurn(oldTarget.pos);
        next.recordLockTurn(oldTarget.pos);
        Dungeon.level = new TestLevel();
        Dungeon.level.setSize(9, 9);
        Dungeon.level.blobs = new HashMap<>();
        Dungeon.level.mobs = new HashSet<>();
        Dungeon.level.heaps = new SparseArray<>();
        Dungeon.level.plants = new SparseArray<>();
        Dungeon.level.traps = new SparseArray<>();
        Dungeon.level.customTiles = new ArrayList<>();
        Dungeon.level.discoverable = new boolean[Dungeon.level.length()];
        Dungeon.level.heroFOV = new boolean[Dungeon.level.length()];
        hero.mindVisionEnemies = new ArrayList<>();
        next.finishLock(TwinDemonEyes.FinishReason.FLOOR_CHANGED);
        assertEquals("a delayed callback from another floor must not hit the unloaded target", 0,
                oldTarget.totalDamage);
    }

    @Test public void laserRangeStopsAtTwelveCellsEvenWhenAimIsFartherAway() {
        level.setSize(20, 20);
        level.blobs = new HashMap<>();
        level.mobs = new HashSet<>();
        level.heaps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.traps = new SparseArray<>();
        level.customTiles = new ArrayList<>();
        level.discoverable = new boolean[level.length()];
        level.heroFOV = new boolean[level.length()];
        for (int i = 0; i < level.length(); i++) level.passable[i] = true;
        hero.pos = 50;
        RecordingGnoll atRange = mob(290);
        RecordingGnoll pastRange = mob(310);
        RecordingGnoll aimed = mob(110);
        TwinDemonEyes eyes = eyesAtLevel(0, TwinDemonEyes.Mode.LASER_EYE, aimed);
        eyes.recordLockTurn(aimed.pos);

        eyes.finishLock(TwinDemonEyes.FinishReason.TURN_LIMIT);

        assertTrue(atRange.totalDamage >= 5 && atRange.totalDamage <= 10);
        assertEquals(0, pastRange.totalDamage);
        assertTrue(aimed.totalDamage >= 5 && aimed.totalDamage <= 10);
    }

    private TwinDemonEyes eyesAtLevel(int itemLevel, TwinDemonEyes.Mode mode, Char target) {
        TwinDemonEyes eyes = new TwinDemonEyes();
        eyes.level(itemLevel);
        Bundle savedMode = new Bundle();
        savedMode.put("twin_demon_eyes_mode", mode.ordinal());
        eyes.restoreFromBundle(savedMode);
        hero.belongings.backpack.items.add(eyes);
        assertTrue(eyes.tryBeginLock(hero, target));
        return eyes;
    }

    private RecordingGnoll mob(int cell) {
        RecordingGnoll mob = new RecordingGnoll(cell);
        Actor.add(mob);
        level.mobs.add(mob);
        return mob;
    }

    private static class RecordingGnoll extends Gnoll {
        int totalDamage;
        java.util.EnumSet<DamageTag> allTags = java.util.EnumSet.noneOf(DamageTag.class);
        RecordingGnoll(int cell) { pos = cell; HT = HP = 500; }
        @Override public void damage(int amount, Object source, DamageTag... tags) {
            totalDamage += amount;
            allTags.addAll(DamageTag.of(tags));
            HP -= amount;
        }
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
