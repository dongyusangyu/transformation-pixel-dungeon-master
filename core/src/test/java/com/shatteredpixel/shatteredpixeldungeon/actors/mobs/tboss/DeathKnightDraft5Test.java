package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.DeathKnightExecutionMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.watabou.utils.Bundle;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeathKnightDraft5Test {

    @Test
    public void executeUsesStrictCurrentHealthThresholdWithoutChangingMaxHealth() {
        DeathKnight boss = new DeathKnight();
        Gnoll target = new Gnoll();
        target.HT = 100;
        target.HP = 5;
        DeathKnightExecutionMark mark = DeathKnightExecutionMark.addHit(target, boss.id());
        assertEquals(100, target.HT);
        assertEquals(1, mark.stacks());
        assertFalse(mark.shouldExecute());
        target.HP = 4;
        assertTrue(mark.shouldExecute());
        target.HT = 80;
        assertFalse(mark.shouldExecute());
        mark.detach();
    }

    @Test
    public void executionStacksAndOwnerSurviveSaveAndCleanupIsScoped() {
        DeathKnight boss = new DeathKnight();
        DeathKnight other = new DeathKnight();
        Gnoll target = new Gnoll();
        DeathKnightExecutionMark.addHit(target, boss.id());
        DeathKnightExecutionMark.addHit(target, boss.id());
        DeathKnightExecutionMark.addHit(target, other.id());
        Bundle saved = new Bundle();
        target.storeInBundle(saved);
        Gnoll restored = new Gnoll();
        restored.restoreFromBundle(saved);
        assertEquals(2, DeathKnightExecutionMark.stacks(restored, boss.id()));
        assertEquals(1, DeathKnightExecutionMark.stacks(restored, other.id()));
        DeathKnightExecutionMark.clear(restored, boss.id());
        assertEquals(0, DeathKnightExecutionMark.stacks(restored, boss.id()));
        assertEquals(1, DeathKnightExecutionMark.stacks(restored, other.id()));
    }

    @Test
    public void resurrectionCleanupCanRemoveAllExecutionOwners() {
        Gnoll target = new Gnoll();
        DeathKnightExecutionMark.addHit(target, 11);
        DeathKnightExecutionMark.addHit(target, 12);

        DeathKnightExecutionMark.clearAll(target);

        assertNull(target.buff(DeathKnightExecutionMark.class));
    }

    @Test
    public void skillDispelRemovesTemporaryBenefitButKeepsDebuff() {
        Gnoll target = new Gnoll();
        Buff.affect(target, Bless.class);
        Buff.affect(target, TestDebuff.class);

        DeathKnight.dispelTemporaryBenefits(target);

        assertNull(target.buff(Bless.class));
        assertNotNull(target.buff(TestDebuff.class));
    }

    @Test
    public void successfulBasicMeleeHitDispelsTemporaryBenefitsIncludingCleansing() {
        DeathKnight boss = new DeathKnight();
        Gnoll target = new Gnoll();
        Buff.affect(target, Bless.class);
        Buff.affect(target, PotionOfCleansing.Cleanse.class);
        Buff.affect(target, TestDebuff.class);

        boss.onAttackResolved(target, true, 1, DamageTag.PHYSICAL, DamageTag.MELEE);

        assertNull(target.buff(Bless.class));
        assertNull(target.buff(PotionOfCleansing.Cleanse.class));
        assertNotNull(target.buff(TestDebuff.class));
    }

    @Test
    public void bothTransitionsStartFiveSlashesAndCleanupCancelsThem() {
        DeathKnight boss = new DeathKnight();
        boss.HP = DeathKnight.FIRST_LOCK_HP;
        boss.armTransitionForTest();
        assertTrue(boss.advanceTransitionForTest());
        assertEquals(4, boss.slashesRemainingForTest());
        assertTrue(boss.isInvulnerable(Object.class));
        boss.cleanupEncounterEffects();
        assertEquals(0, boss.slashesRemainingForTest());
        assertFalse(boss.isInvulnerable(Object.class));

        boss.HP = DeathKnight.SECOND_LOCK_HP;
        boss.armTransitionForTest();
        assertTrue(boss.advanceTransitionForTest());
        assertEquals(4, boss.slashesRemainingForTest());
    }

    @Test
    public void slashProgressAndDelaySurviveSave() {
        DeathKnight boss = new DeathKnight();
        boss.HP = DeathKnight.FIRST_LOCK_HP;
        boss.armTransitionForTest();
        boss.advanceTransitionForTest();
        assertEquals(4, boss.slashesRemainingForTest());
        assertEquals(3f, boss.nextSlashDelayForTest(), 0.001f);
        Bundle saved = new Bundle();
        boss.storeInBundle(saved);
        DeathKnight restored = new DeathKnight();
        restored.restoreFromBundle(saved);
        assertEquals(4, restored.slashesRemainingForTest());
        assertEquals(3f, restored.nextSlashDelayForTest(), 0.001f);
    }

    @Test
    public void lostHealthShrinksNaturalVisionButTorchRestoresIt() {
        assertEquals(8, DeathKnight.visionDistance(8, 0, false));
        assertEquals(7, DeathKnight.visionDistance(8, 200, false));
        assertEquals(2, DeathKnight.visionDistance(8, 2000, false));
        assertEquals(6, DeathKnight.visionDistance(8, 2000, true));
    }

    public static class TestDebuff extends Buff { }
}
