package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TwoHandedGreatswordTest {

	@Test
	public void sixthTierSwordUsesExpectedBaseStatsAndExtendedSprite() throws IOException {
		String source = source();

		assertTrue(source.contains("extends Greatsword"));
		assertTrue(source.contains("image = EXItemSpriteSheet.TWO_HANDED_GREATSWORD"));
		assertTrue(source.contains("tier = 6"));
		assertTrue(source.contains("RCH = 1"));
		assertTrue(source.contains("DLY = 0.5f"));
	}

	@Test
	public void heldInEitherWeaponSlotAddsExactlyOneExtraTurn() {
		assertEquals(0f, TwoHandedGreatsword.extraActionDelay(false, false), 0f);
		assertEquals(1f, TwoHandedGreatsword.extraActionDelay(true, false), 0f);
		assertEquals(1f, TwoHandedGreatsword.extraActionDelay(false, true), 0f);
		assertEquals(1f, TwoHandedGreatsword.extraActionDelay(true, true), 0f);
	}

	@Test
	public void trainSlashRequiresVisibilityEnemyAndRangeAndOnlyProjectingBypassesTrajectory() {
		assertTrue(TwoHandedGreatsword.trainSlashTargetAllowed(true, true, false, 2, 42, 42));
		assertFalse(TwoHandedGreatsword.trainSlashTargetAllowed(false, true, true, 1, 42, 42));
		assertFalse(TwoHandedGreatsword.trainSlashTargetAllowed(true, false, true, 1, 42, 42));
		assertFalse(TwoHandedGreatsword.trainSlashTargetAllowed(true, true, false, 2, 42, 41));
		assertTrue(TwoHandedGreatsword.trainSlashTargetAllowed(true, true, true, 2, 42, 41));
		assertFalse(TwoHandedGreatsword.trainSlashTargetAllowed(true, true, true, 3, 42, 41));
	}

	@Test
	public void implementationUsesOneChargeOneHalfTurnAndOnlyForcesItsOwnEnchantment()
			throws IOException {
		String source = source();

		assertTrue(source.contains("beforeAbilityUsed(hero, firstTarget)"));
		assertTrue(source.contains("hero.spendAndNext(hero.attackDelay())"));
		assertTrue(source.contains("forceEnchantmentProc = true"));
		assertTrue(source.contains("Weapon.Enchantment.forceProc(enchantment)"));
		assertTrue(source.contains("Weapon.Enchantment.clearForcedProc(enchantment)"));
		assertTrue(source.contains("hero.attack(target, 1f, 0f, Char.INFINITE_ACCURACY)"));
	}

	@Test
	public void wandAndMissileDelaysUseTheCentralSingleTurnPenalty() throws IOException {
		String itemSource = sourceAt("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java");
		String wandSource = sourceAt("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/wands/Wand.java");

		assertTrue(itemSource.contains("this instanceof MissileWeapon ? TwoHandedGreatsword.extraActionDelay(user) : 0f"));
		assertTrue(wandSource.contains("TIME_TO_ZAP + TwoHandedGreatsword.extraActionDelay(curUser)"));
	}

	private static String source() throws IOException {
		return sourceAt("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/TwoHandedGreatsword.java");
	}

	private static String sourceAt(String path) throws IOException {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}
