package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MobPlacerPersistenceTest {

    @Test
    public void selectionPersistsByCategoryAndClassName() {
        MobPlacementState original = new MobPlacementState(
                "tower_mobs", MimicCrocodile.class.getName(), 0b101);
        Bundle bundle = new Bundle();
        original.storeInBundle(bundle);

        MobPlacementState restored = MobPlacementState.restoreMob(bundle);
        restored.normalize(MobPlacementCatalog.mobPages());
        assertEquals("tower_mobs", restored.categoryKey);
        assertEquals(MimicCrocodile.class.getName(), restored.mobClassName);
        assertEquals(0b101, restored.eliteOptions);
    }

    @Test
    public void oldSelectionKeysFallBackToAValidOrdinaryEntry() {
        Bundle oldBundle = new Bundle();
        oldBundle.put("mobTier", 9);
        oldBundle.put("mobIndex", 99);
        oldBundle.put("elite_ops", 3);

        MobPlacementState restored = MobPlacementState.restoreMob(oldBundle);
        restored.normalize(MobPlacementCatalog.mobPages());
        assertEquals("regional", restored.categoryKey);
        assertEquals(MobPlacementCatalog.mobPages().get(0).entries().get(0).getName(),
                restored.mobClassName);
        assertEquals(3, restored.eliteOptions);
    }

    @Test
    public void bossComponentSelectionPersistsByCategoryAndClassName() {
        MobPlacementState original = new MobPlacementState(
                "regional_boss_units", YogFist.DarkFist.class.getName(), 0);
        Bundle bundle = new Bundle();
        original.storeInBundle(bundle);

        MobPlacementState restored = MobPlacementState.restoreBoss(bundle);
        restored.normalize(MobPlacementCatalog.bossPages());

        assertEquals("regional_boss_units", restored.categoryKey);
        assertEquals(YogFist.DarkFist.class.getName(), restored.mobClassName);
        assertEquals(0, restored.eliteOptions);
    }
}
