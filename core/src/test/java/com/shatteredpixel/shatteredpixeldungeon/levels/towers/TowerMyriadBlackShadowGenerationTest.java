package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.AlienatedPrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MyriadBlackShadow;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerMyriadBlackShadowGenerationTest {

	@Test
	public void rareShadowUsesTheGuardSlotAndRatSkullMultiplier() {
		assertEquals(0.025f, TowerMobRules.myriadBlackShadowChance(1f), 0f);
		assertEquals(0.125f, TowerMobRules.myriadBlackShadowChance(5f), 0f);
		assertEquals(1f, TowerMobRules.myriadBlackShadowChance(100f), 0f);
		assertTrue(TowerMobRules.shouldSpawnMyriadBlackShadow(0.024999f, 1f));
		assertFalse(TowerMobRules.shouldSpawnMyriadBlackShadow(0.025f, 1f));
	}

	@Test
	public void replacementOnlyChangesAlienatedGuardSelection() {
		Mob rare = TowerMobRules.createAlienatedPrismaticGuard(0.024999f, 1f);
		Mob normal = TowerMobRules.createAlienatedPrismaticGuard(0.025f, 1f);
		assertTrue(rare instanceof MyriadBlackShadow);
		assertTrue(normal instanceof AlienatedPrismaticGuard);
		assertFalse(normal instanceof MyriadBlackShadow);
		assertEquals(15, TowerMobRules.Selection.values().length);
		assertTrue(Arrays.stream(TowerMobRules.Selection.values())
				.noneMatch(selection -> selection.name().equals("MYRIAD_BLACK_SHADOW")));
	}
}
