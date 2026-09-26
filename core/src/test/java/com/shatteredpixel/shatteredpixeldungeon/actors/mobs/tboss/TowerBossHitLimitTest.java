package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerBossHitLimitTest {

    @Test
    public void commonBossRulesOnlyApplyInsideTower() {
        int branch = Dungeon.branch;
        int depth = Dungeon.depth;
        try {
            Dungeon.branch = TowerLevel.BRANCH;
            Dungeon.depth = 1;
            assertTrue(TowerBoss.towerRulesActive());
            Dungeon.branch = 0;
            assertFalse(TowerBoss.towerRulesActive());
            Dungeon.branch = TowerLevel.BRANCH;
            Dungeon.depth = 0;
            assertFalse(TowerBoss.towerRulesActive());
        } finally {
            Dungeon.branch = branch;
            Dungeon.depth = depth;
        }
    }

    @Test
    public void periodicHungerCorruptionAndGasDoNotSpendHitAllowance() {
        assertTrue(TowerBoss.isPeriodicDamageSource(new Hunger()));
        assertTrue(TowerBoss.isPeriodicDamageSource(new Corruption()));
        assertTrue(TowerBoss.isPeriodicDamageSource(new ToxicGas()));
        assertFalse(TowerBoss.isPeriodicDamageSource(new Object()));
    }

    @Test
    public void fourthDirectHitInSameWorldTurnIsRejected() {
        TowerBossHitLimit limit = new TowerBossHitLimit();
        for (int i = 0; i < 3; i++) {
            assertTrue(limit.allows(10.5f, false));
            limit.record(10.5f);
        }
        assertFalse(limit.allows(10.9f, false));
        assertTrue(limit.allows(11f, false));
    }

    @Test
    public void periodicDamageDoesNotSpendDirectHitAllowance() {
        TowerBossHitLimit limit = new TowerBossHitLimit();
        for (int i = 0; i < 3; i++) limit.record(10f);
        assertTrue(limit.allows(10f, true));
        assertFalse(limit.allows(10f, false));
    }

    @Test
    public void saveAndRestoreKeepsRemainingWindow() {
        TowerBossHitLimit original = new TowerBossHitLimit();
        for (int i = 0; i < 3; i++) original.record(4.25f);
        Bundle bundle = new Bundle();
        original.store(bundle, 4.5f);

        TowerBossHitLimit restored = new TowerBossHitLimit();
        restored.restore(bundle);
        assertFalse(restored.allows(100f, false));
        assertTrue(restored.allows(100.75f, false));
    }
}
