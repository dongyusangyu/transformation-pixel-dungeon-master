package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InfectionTest {

    @Test
    public void eachStackReducesIncomingHealingByEightPercent() {
        for (int stacks = 1; stacks <= 4; stacks++) {
            TestChar target = freshTarget();
            Infection.set(target, stacks);
            assertEquals((int) (100 * (1f - stacks * 0.08f)), target.heal(100));
        }
    }

    @Test
    public void infectionExposesItsStackCountInTheBuffIndicator() {
        TestChar target = freshTarget();
        Infection.set(target, 2);

        Infection infection = target.buff(Infection.class);
        assertEquals(BuffIndicator.POISON, infection.icon());
        assertEquals("2", infection.iconTextDisplay());
    }

    @Test
    public void infectionLosesOneStackEveryTenGameTurns() {
        TestChar target = freshTarget();
        target.HP = target.HT;
        Infection.set(target, 4);

        assertEquals(10, Infection.DECAY_TURNS);
        target.buff(Infection.class).decayOneIntervalForTest();
        assertEquals(3, Infection.stacks(target));

        target.buff(Infection.class).decayOneIntervalForTest();
        assertEquals(2, Infection.stacks(target));
    }

    @Test
    public void infectionColorChangesAtThreeAndFourStacks() {
        assertEquals(Infection.DEFAULT_ICON_COLOR, Infection.iconColorForStacks(2));
        assertEquals(Infection.WARNING_ICON_COLOR, Infection.iconColorForStacks(3));
        assertEquals(Infection.DANGER_ICON_COLOR, Infection.iconColorForStacks(4));
        assertEquals(Infection.DANGER_ICON_COLOR, Infection.iconColorForStacks(5));
    }

    @Test
    public void crossingThreeTriggersOnceUntilStacksFallBelowThree() {
        TestChar target = freshTarget();
        Infection.addStacks(target, 3);
        assertNotNull(target.buff(Weakness.class));
        assertNotNull(target.buff(Hex.class));

        Buff.detach(target, Weakness.class);
        Buff.detach(target, Hex.class);
        Infection.addStacks(target, 0);
        assertTrue(target.buff(Infection.class).thirdThresholdArmedForTest());
        assertEquals(null, target.buff(Weakness.class));

        Infection.set(target, 2);
        Infection.addStacks(target, 1);
        assertNotNull(target.buff(Weakness.class));
        assertNotNull(target.buff(Hex.class));
    }

    @Test
    public void fifthStackBurstsAndFallsBackToThree() {
        TestChar target = freshTarget();
        Infection.set(target, 4);
        target.damageTaken = 0;

        Infection.addStacks(target, 1);

        assertEquals(25, target.damageTaken);
        assertEquals(3, Infection.stacks(target));
        assertNotNull(target.buff(Vulnerable.class));
    }

    @Test
    public void healingPotionReliefHappensAfterReducedFirstTick() {
        TestChar target = freshTarget();
        Infection.set(target, 4);
        Infection.markHealingPotionRelief(target);
        assertEquals(6, target.heal(10));
        assertEquals(3, Infection.stacks(target));
        assertEquals(7, target.heal(10));
    }

    @Test
    public void reductionsAddTogetherAndCapAtNinetyFivePercent() {
        TestChar target = freshTarget();
        Infection.set(target, 5);
        TerminalHealingPenalty.set(target, 0.80f);
        assertEquals(5, target.heal(100));
    }

    @Test
    public void purgeClearsInfectionAndPendingPotionRelief() {
        TestChar target = freshTarget();
        Infection.set(target, 4);
        Infection.markHealingPotionRelief(target);
        Infection.clear(target);
        assertEquals(0, Infection.stacks(target));
        assertEquals(100, target.heal(100));
    }

    @Test
    public void bundlePreservesStacksLatchPotionReliefAndDecaySchedule() {
        TestChar target = freshTarget();
        Infection.set(target, 4);
        Infection.markHealingPotionRelief(target);
        Infection infection = target.buff(Infection.class);
        float decayRemaining = infection.cooldown();
        Bundle bundle = new Bundle();
        infection.storeInBundle(bundle);

        Infection restored = new Infection();
        restored.restoreFromBundle(bundle);

        assertEquals(4, restored.stacksForTest());
        assertTrue(restored.thirdThresholdArmedForTest());
        assertTrue(restored.potionReliefPendingForTest());
        assertEquals(decayRemaining, restored.cooldown(), 0.001f);
    }

    @Test
    public void legacyBundleStartsAFullDecayIntervalAfterLoading() {
        Bundle legacy = new Bundle();
        legacy.put("stacks", 3);

        Infection restored = new Infection();
        restored.restoreFromBundle(legacy);

        assertEquals(Infection.DECAY_TURNS, restored.cooldown(), 0.001f);
    }

    @Test
    public void postPlagueFatigueUsesExpectedMultipliers() {
        assertEquals(1.1f, PostPlagueFatigue.MOVE_DELAY_MULTIPLIER, 0.0001f);
        assertEquals(1.1f, PostPlagueFatigue.ATTACK_DELAY_MULTIPLIER, 0.0001f);
        assertEquals(0.5f, PostPlagueFatigue.NATURAL_REGEN_MULTIPLIER, 0.0001f);
    }

    private static TestChar freshTarget() {
        TestChar target = new TestChar();
        target.HT = 1000;
        target.HP = 0;
        return target;
    }

    private static final class TestChar extends Char {
        int damageTaken;

        @Override
        public void damage(int damage, Object source) {
            damageTaken += damage;
        }

        @Override public int attackSkill(Char target) { return 0; }
        @Override public int defenseSkill(Char enemy) { return 0; }
        @Override public int drRoll() { return 0; }
        @Override public float resist(Class effect) { return 1f; }
    }
}
