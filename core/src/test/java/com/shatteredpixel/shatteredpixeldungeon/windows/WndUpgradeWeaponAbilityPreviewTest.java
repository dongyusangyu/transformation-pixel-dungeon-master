package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.HundredTonHammer;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.LakeSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.MercuryBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.MountainGuard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.SoulBlade;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WndUpgradeWeaponAbilityPreviewTest {

	@Test
	public void upgradePreviewUsesCentralWeaponAbilityCapability() throws IOException {
		String source = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndUpgrade.java");

		assertTrue(source.contains("MeleeWeapon.canUseWeaponAbility(Dungeon.hero)"));
		assertTrue(source.contains("upgradeAbilityStats(levelFrom)"));
		assertFalse(source.contains("Dungeon.hero.heroClass == HeroClass.DUELIST"));
		assertFalse(source.contains("Dungeon.hero.subClass.is(HeroSubClass.CHAMPION)"));
		assertFalse(source.contains("Dungeon.hero.hasTalent(Talent.MARTIAL_TRAIN)"));
	}

	@Test
	public void identicalBeforeAndAfterStatsRemainEligibleForDisplay() throws IOException {
		String source = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndUpgrade.java");

		assertTrue(source.contains("upgradeAbilityStats(levelTo)"));
		assertFalse(source.contains("levelFrom).equals(levelTo)"));
		assertFalse(source.contains("levelFrom == levelTo"));
	}

	@Test
	public void chainMaceAndTwoHandedGreatswordPreviewDamageRanges() throws IOException {
		String chainMace = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/ChainMace.java");
		String greatsword = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/TwoHandedGreatsword.java");

		assertTrue(chainMace.contains("int bonus = sweepDamageBoost(level);"));
		assertTrue(chainMace.contains("augment.damageFactor(min(level)) + bonus + \"-\""));
		assertTrue(chainMace.contains("upgradeThrowDamageStat(int level)"));
		assertTrue(greatsword.contains("augment.damageFactor(min(level)) + \"-\" + augment.damageFactor(max(level))"));
		assertFalse(chainMace.contains("Messages.get(this, \"upgrade_ability_stat\")"));
		assertFalse(greatsword.contains("Messages.get(this, \"upgrade_ability_stat\")"));
	}

	@Test
	public void fixedAbilityStatsAndUnsupportedAbilitiesKeepTheirExistingPolicy() throws IOException {
		String auxiliaryCore = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/AuxiliaryCore.java");
		String mercuryBlade = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/MercuryBlade.java");
		String venomousSickle = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/VenomousSickle.java");

		assertTrue(auxiliaryCore.contains("public String upgradeAbilityStat(int level)"));
		assertTrue(mercuryBlade.contains("public String upgradeAbilityStat(int level)"));
		assertFalse(venomousSickle.contains("upgradeAbilityStat(int level)"));
	}

	@Test
	public void damageRangeIsNamedAsAbilityDamage() throws IOException {
		String messages = sourceFile("src/main/assets/messages/items/items_zh.properties");

		assertTrue(messages.contains("items.weapon.melee.tier6.chainmace.upgrade_ability_stat_name=武技伤害"));
		assertTrue(messages.contains("items.weapon.melee.tier6.twohandedgreatsword.upgrade_ability_stat_name=武技伤害"));
	}

	@Test
	public void upgradePreviewUsesTypedRowsAndGenericBlockingCapability() throws IOException {
		String meleeWeapon = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java");
		String wndUpgrade = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndUpgrade.java");

		assertTrue(meleeWeapon.contains("class UpgradeAbilityStat"));
		assertTrue(meleeWeapon.contains("upgradeAbilityStats(int level)"));
		assertTrue(meleeWeapon.contains("upgradeBlockingStat(int level)"));
		assertTrue(wndUpgrade.contains("upgradeAbilityStats(levelFrom)"));
		assertTrue(wndUpgrade.contains("upgradeBlockingStat(levelFrom)"));
		assertFalse(wndUpgrade.contains("toUpgrade instanceof RoundShield"));
		assertFalse(wndUpgrade.contains("toUpgrade instanceof Greatshield"));
		assertFalse(wndUpgrade.contains("toUpgrade instanceof Bracer"));
	}

	@Test
	public void tierSixPreviewRowsMatchTheirActualMechanics() throws IOException {
		String hundredTonHammer = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/HundredTonHammer.java");
		String mercuryBlade = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/MercuryBlade.java");
		String lakeSword = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/LakeSword.java");
		String mountainGuard = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/MountainGuard.java");
		String soulBlade = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/SoulBlade.java");

		assertTrue(hundredTonHammer.contains("KNOCKBACK_DISTANCE"));
		assertTrue(mercuryBlade.contains("UpgradeAbilityStatType.DURATION"));
		assertTrue(lakeSword.contains("DRAW_RANGE"));
		assertTrue(mountainGuard.contains("UpgradeAbilityStatType.DURATION"));
		assertTrue(soulBlade.contains("upgradeBlockingStat(int level)"));
	}

	@Test
	public void typedRowsExposeTheExpectedValues() {
		HundredTonHammer hammer = new HundredTonHammer();
		assertEquals("ability_ambush_damage",
				hammer.upgradeAbilityStats(3).get(0).type.messageKey());
		assertEquals("21-54", hammer.upgradeAbilityStats(3).get(0).value);
		MeleeWeapon.UpgradeAbilityStat hammerDistance = hammer.upgradeFeatureStats(3).get(0);
		assertEquals(MeleeWeapon.UpgradeAbilityStatType.KNOCKBACK_DISTANCE, hammerDistance.type);
		assertEquals("2", hammerDistance.value);

		MeleeWeapon.UpgradeAbilityStat mercuryDuration = new MercuryBlade()
				.upgradeAbilityStats(3).get(0);
		assertEquals(MeleeWeapon.UpgradeAbilityStatType.DURATION, mercuryDuration.type);
		assertEquals("5", mercuryDuration.value);

		MeleeWeapon.UpgradeAbilityStat lakeRange = new LakeSword()
				.upgradeFeatureStats(3).get(1);
		assertEquals(MeleeWeapon.UpgradeAbilityStatType.DRAW_RANGE, lakeRange.type);
		assertEquals("8", lakeRange.value);

		assertEquals(16, MountainGuard.maxBlockForLevel(3));
		assertEquals(12, SoulBlade.maxBlockForLevel(3));
	}

	@Test
	public void tailWhipRangeIsAnUnconditionalFeatureIncludingUnchangedUpgradeSteps() throws IOException {
		com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhip whip =
				new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhip();
		int[] levels = {-7, 0, 1, 6, 7, 13, 14, 20, 21};
		String[] ranges = {"3", "3", "3", "3", "4", "4", "5", "5", "6"};
		for (int i = 0; i < levels.length; i++) {
			assertEquals(1, whip.upgradeFeatureStats(levels[i]).size());
			assertEquals("feature_attack_range", whip.upgradeFeatureStats(levels[i]).get(0).type.messageKey());
			assertEquals(ranges[i], whip.upgradeFeatureStats(levels[i]).get(0).value);
		}
		String source = sourceFile("src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndUpgrade.java");
		assertTrue(source.indexOf("weapon.upgradeFeatureStats(levelFrom)")
				< source.indexOf("if (canViewWeaponAbilityUpgrade"));
	}

	@Test
	public void blockingDescriptionsIncludeCurrentMaximum() throws IOException {
		String messages = sourceFile("src/main/assets/messages/items/items_zh.properties");

		assertTrue(messages.contains("items.weapon.melee.tier6.mountainguard.stats_desc="));
		assertTrue(messages.contains("items.weapon.melee.tier6.soulblade.stats_desc="));
		assertTrue(messages.contains("items.weapon.melee.tier6.mountainguard.typical_stats_desc="));
		assertTrue(messages.contains("items.weapon.melee.tier6.soulblade.typical_stats_desc="));
	}

	private static String sourceFile(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)), StandardCharsets.UTF_8);
	}
}
