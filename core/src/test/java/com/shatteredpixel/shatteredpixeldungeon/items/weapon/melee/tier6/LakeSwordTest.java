package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;
import com.watabou.utils.Bundle;

import static org.junit.Assert.assertEquals;

public class LakeSwordTest {

	@Test
	public void baseStatsAndStateTimingMatchDesign() {
		LakeSword sword = new LakeSword();
		assertEquals(6, sword.weaponTier());
		assertEquals(5, sword.min(0));
		assertEquals(30, sword.max(0));
		assertEquals(8, sword.min(3));
		assertEquals(51, sword.max(3));
		assertEquals(20, sword.STRReq());
		assertEquals(EXItemSpriteSheet.LAKE_SWORD_SHEATHED, sword.image);
		assertEquals(7, LakeSword.chargeDurationForLevel(3));
		assertEquals(8, LakeSword.drawRangeForLevel(3));
	}

	@Test
	public void chargedStateExpiresAndUsesTheFixedDuration() {
		LakeSword sword = new LakeSword();
		sword.enterCharged(null, 0);
		assertEquals(LakeSword.State.CHARGED, sword.state());
		assertEquals(4, sword.magicTurns());
		sword.tickMagic();
		sword.tickMagic();
		sword.tickMagic();
		assertEquals(1, sword.magicTurns());
		sword.tickMagic();
		assertEquals(LakeSword.State.SPENT, sword.state());
		assertEquals(0, sword.magicTurns());
	}

	@Test
	public void chargedDamageUsesRoundedOnePointFiveMultiplier() {
		assertEquals(15, LakeSword.chargedDamage(10));
		assertEquals(17, LakeSword.chargedDamage(11));
	}

	@Test
	public void stateRoundTripKeepsChargedTimerAndLegacyBundlesDefaultToSheathed() {
		LakeSword original = new LakeSword();
		original.enterCharged(null, 3);
		Bundle saved = new Bundle();
		original.storeInBundle(saved);

		LakeSword restored = new LakeSword();
		restored.restoreFromBundle(saved);
		assertEquals(LakeSword.State.CHARGED, restored.state());
		assertEquals(7, restored.magicTurns());

		LakeSword legacy = new LakeSword();
		legacy.restoreFromBundle(new Bundle());
		assertEquals(LakeSword.State.SHEATHED, legacy.state());
		assertEquals(0, legacy.magicTurns());
	}

	@Test
	public void windProtectionDurationAndRegenFactorHaveStablePureRules() {
		assertEquals(2, LakeSword.windProtectionDurationForLevel(0));
		assertEquals(7, LakeSword.windProtectionDurationForLevel(5));
		assertEquals(1f, LakeSword.naturalRegenDelayFactor(null), 0f);
	}
}
