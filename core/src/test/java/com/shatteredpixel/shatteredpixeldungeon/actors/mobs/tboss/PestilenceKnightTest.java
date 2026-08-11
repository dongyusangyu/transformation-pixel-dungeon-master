package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PestilenceKnightTest {

    @Test
    public void baseAndGrowingPanelsUseEncounterDepthAndCapAtSix() {
        PestilenceKnight first = new PestilenceKnight(5);
        assertEquals(1500, first.HT);
        assertEquals(1f, first.activeDamageMultiplier(), 0.001f);
        assertEquals(10, first.drRollMinForTest());
        assertEquals(25, first.drRollMaxForTest());

        PestilenceKnight second = new PestilenceKnight(10);
        assertEquals(1575, second.HT);
        assertEquals(1.03f, second.activeDamageMultiplier(), 0.001f);

        PestilenceKnight capped = new PestilenceKnight(40);
        assertEquals(1950, capped.HT);
        assertEquals(1.18f, capped.activeDamageMultiplier(), 0.001f);
        assertEquals(13, capped.drRollMinForTest());
        assertEquals(28, capped.drRollMaxForTest());
    }

    @Test
    public void combatPanelAndRestrictionsMatchBossSpecification() {
        PestilenceKnight boss = new PestilenceKnight(5);
        assertEquals(50, boss.attackSkill(null));
        assertEquals(30, boss.defenseSkill);
        assertEquals(20, boss.damageRollMinForTest());
        assertEquals(30, boss.damageRollMaxForTest());
        assertEquals(1f, boss.speed(), 0.001f);
        assertEquals(1f, boss.attackDelay(), 0.001f);
        assertEquals(0, boss.EXP);
        assertEquals(30, boss.maxLvl);
        assertEquals(1, PestilenceKnight.KNOCKBACK_DISTANCE);
        assertTrue(boss.properties().contains(Char.Property.BOSS));
        assertTrue(boss.properties().contains(Char.Property.IMMOVABLE));
        assertTrue(boss.properties().contains(Char.Property.UNSLEEP));
    }

    @Test
    public void eachFinalDamageEventIsCappedAtOneHundredFifty() {
        PestilenceKnight boss = new PestilenceKnight(5);
        assertEquals(150, boss.capFinalDamageForTest(999));
        assertEquals(80, boss.capFinalDamageForTest(80));
        assertEquals(150, boss.capFinalDamageForTest(400));
    }

    @Test
    public void bundleRestoresGrowthAndSafeStateDefaults() {
        PestilenceKnight original = new PestilenceKnight(35);
        Bundle bundle = new Bundle();
        original.storeInBundle(bundle);

        PestilenceKnight restored = new PestilenceKnight(5);
        restored.restoreFromBundle(bundle);

        assertEquals(1950, restored.HT);
        assertEquals(6, restored.growthForTest());
        assertEquals(PestilenceKnight.Phase.INCUBATION, restored.phaseForTest());
        assertEquals(PestilenceKnight.HarvestState.NONE, restored.harvestForTest());
    }
}
