package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Bundle;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TwinDemonEyesTriggerTest {

    private Hero oldHero;
    private Level oldLevel;

    @Before public void setUp() {
        oldHero = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
        oldLevel = com.shatteredpixel.shatteredpixeldungeon.Dungeon.level;
        Actor.clear();
        Actor.resetNextID();
    }

    @After public void tearDown() {
        Actor.clear();
        Actor.resetNextID();
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = oldHero;
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.level = oldLevel;
    }

    @Test
    public void itemOnlyStartsOneLockOnLivingHostileTargetWhileInBackpack() {
        TwinDemonEyes eyes = new TwinDemonEyes();
        Hero hero = hero();
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = hero;
        Rat target = new Rat();
        target.HP = target.HT = 10;
        target.pos = 41;

        assertFalse(eyes.tryBeginLock(hero, target));
        TwinDemonEyes.Mode initialMode = eyes.mode();
        assertFalse("an inactive lock cannot switch modes", eyes.finishLock());
        assertEquals(initialMode, eyes.mode());
        hero.belongings.backpack.items.add(eyes);
        assertTrue(eyes.tryBeginLock(hero, target));
        assertTrue("item blindness is bounded by the current lock tier",
                hero.buff(Blindness.class).cooldown() <= eyes.lockDuration() + 0.001f);
        assertEquals(target.id(), eyes.lockedTargetId());
        assertEquals(41, eyes.lastTargetCell());
        assertFalse("a second hit during one lock cannot replace or restart it",
                eyes.tryBeginLock(hero, new Rat()));
        assertEquals(target.id(), eyes.lockedTargetId());
        assertEquals(0, eyes.elapsedLockTurns());
    }

    @Test
    public void deadAndFriendlyTargetsCannotStartALock() {
        TwinDemonEyes eyes = new TwinDemonEyes();
        Hero hero = hero();
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = hero;
        hero.belongings.backpack.items.add(eyes);

        Rat dead = new Rat();
        dead.HP = 0;
        assertFalse(eyes.tryBeginLock(hero, dead));

        Rat ally = new Rat();
        ally.alignment = com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ALLY;
        assertFalse(eyes.tryBeginLock(hero, ally));
        assertFalse(eyes.isLocked());
    }

    @Test
    public void lockDurationAndUpgradeEnergyUseTrinketTier() {
        TwinDemonEyes eyes = new TwinDemonEyes();
        int[] turns = {4, 6, 8, 10};
        int[] energy = {20, 25, 30};
        for (int level = 0; level <= 3; level++) {
            eyes.level(level);
            assertEquals(turns[level], eyes.lockDuration());
            if (level < 3) assertEquals(energy[level], eyes.upgradeEnergyCost());
        }
    }

    @Test
    public void trinketIsIncludedInTheStandardTrinketPool() {
        assertTrue(Arrays.asList(Generator.Category.TRINKET.classes).contains(TwinDemonEyes.class));
    }

    @Test
    public void olderSaveWithoutEyeFieldsRestoresAnUnlockedRandomMode() {
        TwinDemonEyes restored = new TwinDemonEyes();
        restored.restoreFromBundle(new Bundle());

        assertFalse(restored.isLocked());
        assertTrue(restored.mode() == TwinDemonEyes.Mode.FLAME_EYE
                || restored.mode() == TwinDemonEyes.Mode.LASER_EYE);
        assertEquals(-1, restored.lastTargetCell());
        assertEquals(0, restored.elapsedLockTurns());
    }

    @Test
    public void savedLockRestoresTargetModeElapsedTurnsAndSingleFinishState() {
        TwinDemonEyes eyes = new TwinDemonEyes();
        Hero hero = hero();
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = hero;
        hero.belongings.backpack.items.add(eyes);
        Rat target = new Rat();
        target.HP = target.HT = 10;
        target.pos = 52;
        assertTrue(eyes.tryBeginLock(hero, target));
        eyes.recordLockTurn(52);

        Bundle saved = new Bundle();
        eyes.storeInBundle(saved);
        TwinDemonEyes loaded = new TwinDemonEyes();
        loaded.restoreFromBundle(saved);

        assertEquals(target.id(), loaded.lockedTargetId());
        assertEquals(52, loaded.lastTargetCell());
        assertEquals(1, loaded.elapsedLockTurns());
        assertEquals(eyes.mode(), loaded.mode());
        assertTrue(loaded.finishLock());
        assertFalse("repeated async completion cannot switch twice", loaded.finishLock());
        assertNotEquals(eyes.mode(), loaded.mode());
    }

    @Test
    public void blindnessKeepsOnlyLockedTargetAwareAndDoesNotDeleteOtherAwareness() {
        TestLevel level = new TestLevel();
        level.setSize(9, 9);
        level.blobs = new HashMap<>();
        level.mobs = new HashSet<>();
        level.heaps = new SparseArray<>();
        level.discoverable = new boolean[level.length()];
        for (int cell = 0; cell < level.length(); cell++) {
            level.passable[cell] = true;
            level.discoverable[cell] = true;
        }
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.level = level;

        Hero hero = hero();
        hero.pos = 40;
        hero.viewDistance = 8;
        hero.mindVisionEnemies = new ArrayList<>();
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = hero;
        Rat locked = new Rat();
        locked.HP = locked.HT = 10;
        locked.pos = 44;
        Actor.add(locked);
        level.mobs.add(locked);
        Rat other = new Rat();
        other.HP = other.HT = 10;
        other.pos = 30;
        Actor.add(other);
        level.mobs.add(other);

        TalismanAwarenessFixture existing = new TalismanAwarenessFixture();
        existing.charID = other.id();
        existing.attachTo(hero);
        TwinDemonEyes eyes = new TwinDemonEyes();
        hero.belongings.backpack.items.add(eyes);
        Blindness existingBlind = Buff.affect(hero, Blindness.class, 15f);
        float externalDuration = existingBlind.cooldown();
        assertTrue(eyes.tryBeginLock(hero, locked));
        assertEquals("eye lock must not shorten a longer blindness source",
                externalDuration, existingBlind.cooldown(), 0.001f);

        boolean[] fov = new boolean[level.length()];
        level.updateFieldOfView(hero, fov);
        assertTrue("the lock should reveal its tracked target during blindness", fov[locked.pos]);
        assertFalse("precise lock should not reveal a neighboring tile", fov[locked.pos - 1]);
        assertFalse("precise lock should not reveal a neighboring tile", fov[locked.pos - level.width()]);
        assertFalse("precise lock should not reveal a neighboring tile", fov[locked.pos + level.width()]);
        assertTrue("an unrelated source's awareness remains intact", fov[other.pos]);
        assertFalse("lock awareness must not expand general vision", fov[0]);

        eyes.finishLock();
        assertEquals("finishing removes only the awareness contributed by these eyes",
                1, hero.buffs(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight.CharAwareness.class).size());
    }

    @Test
    public void eyeLockUsesTheDedicatedPreciseLockIcon() {
        assertEquals(BuffIndicator.PRECISE_LOCK, new TwinDemonEyes.EyeLock().icon());
    }

    @Test
    public void remoteAllyAttackCanTriggerTheItemHitHookWithoutARangeLimit() {
        TwinDemonEyes eyes = new TwinDemonEyes();
        Hero hero = hero();
        hero.pos = 40;
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = hero;
        hero.belongings.backpack.items.add(eyes);
        Rat distant = new Rat();
        distant.HP = distant.HT = 10;
        distant.pos = 1;
        Actor.add(distant);

        assertTrue(TwinDemonEyes.onSuccessfulRangedHit(hero, distant));
        assertEquals(distant.id(), eyes.lockedTargetId());
    }

    private static Hero hero() {
        Hero hero = TestHeroFactory.create();
        hero.HP = hero.HT = 20;
        return hero;
    }

    private static class TalismanAwarenessFixture extends
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight.CharAwareness {
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
