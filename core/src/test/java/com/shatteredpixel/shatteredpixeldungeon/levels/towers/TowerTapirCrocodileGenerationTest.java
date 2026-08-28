package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TapirCrocodile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerTapirCrocodileGenerationTest {

	@Test
	public void chanceUsesRatSkullMultiplierAndCapsAtOne() {
		assertEquals(0.025f, TowerMobRules.tapirCrocodileChance(1f), 0f);
		assertEquals(0.050f, TowerMobRules.tapirCrocodileChance(2f), 0f);
		assertEquals(0.125f, TowerMobRules.tapirCrocodileChance(5f), 0f);
		assertEquals(1f, TowerMobRules.tapirCrocodileChance(100f), 0f);
	}

	@Test
	public void strictBoundaryReplacesOnlyMimicCrocodileResult() {
		assertTrue(TowerMobRules.createMimicCrocodile(0.024999f, 1f)
				instanceof TapirCrocodile);
		Mob ordinary = TowerMobRules.createMimicCrocodile(0.025f, 1f);
		assertTrue(ordinary instanceof MimicCrocodile);
		assertFalse(ordinary instanceof TapirCrocodile);
	}

	@Test
	public void variantDoesNotAddAnotherBaseSelectionSlot() {
		assertEquals(15, TowerMobRules.Selection.values().length);
		for (TowerMobRules.Selection selection : TowerMobRules.Selection.values()) {
			assertFalse(selection.name().equals("TAPIR_CROCODILE"));
		}
	}

	@Test
	public void naturalSpawnPreparationKeepsVariantWandering() {
		Mob tapir = TowerMobRules.prepareNaturalSpawn(new TapirCrocodile());
		assertTrue(tapir.state == tapir.WANDERING);
	}
}
