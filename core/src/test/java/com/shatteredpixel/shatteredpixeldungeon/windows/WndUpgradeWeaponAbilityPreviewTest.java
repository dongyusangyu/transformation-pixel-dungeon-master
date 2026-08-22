package com.shatteredpixel.shatteredpixeldungeon.windows;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WndUpgradeWeaponAbilityPreviewTest {

	@Test
	public void upgradePreviewUsesCentralWeaponAbilityCapability() throws IOException {
		String source = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndUpgrade.java");

		assertTrue(source.contains("MeleeWeapon.canUseWeaponAbility(Dungeon.hero)"));
		assertTrue(source.contains("upgradeAbilityStat(levelFrom) == null"));
		assertFalse(source.contains("Dungeon.hero.heroClass == HeroClass.DUELIST"));
		assertFalse(source.contains("Dungeon.hero.subClass.is(HeroSubClass.CHAMPION)"));
		assertFalse(source.contains("Dungeon.hero.hasTalent(Talent.MARTIAL_TRAIN)"));
	}

	@Test
	public void identicalBeforeAndAfterStatsRemainEligibleForDisplay() throws IOException {
		String source = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndUpgrade.java");

		assertTrue(source.contains("bottom = fillFields(Messages.get(toUpgrade, \"upgrade_ability_stat_name\")"));
		assertFalse(source.contains("levelFrom).equals(levelTo)"));
		assertFalse(source.contains("levelFrom == levelTo"));
	}

	@Test
	public void chainMaceAndTwoHandedGreatswordPreviewDamageRanges() throws IOException {
		String chainMace = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/ChainMace.java");
		String greatsword = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/TwoHandedGreatsword.java");

		assertTrue(chainMace.contains("augment.damageFactor(min(level)) + \"-\" + augment.damageFactor(max(level))"));
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

	private static String sourceFile(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)), StandardCharsets.UTF_8);
	}
}
