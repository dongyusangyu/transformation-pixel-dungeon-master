package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ItemUpgradeScrollUsesTest {

	@Test
	public void oldSaveWithoutUpgradeScrollUsesDefaultsToZero() {
		Item restored = new Item();

		restored.restoreFromBundle(new Bundle());

		assertEquals(0, restored.upgradeScrollUses);
	}

	@Test
	public void upgradeScrollUsesSurvivesBundleRoundTrip() {
		Item original = new Item();
		original.upgradeScrollUses = 12;
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		Item restored = new Item();
		restored.restoreFromBundle(bundle);

		assertEquals(12, restored.upgradeScrollUses);
	}

	@Test
	public void magicalInfusionCountsAsUpgradeScrollUse() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/spells/MagicalInfusion.java");

		assertTrue(source.contains("((Armor) item).upgradeScrollCreditTarget()"));
		assertTrue(source.contains("upgradeScrollCreditTarget.upgradeScrollUses++;"));
		assertFalse(source.contains("item.upgradeScrollUses++;"));
	}

	@Test
	public void curseInfusionDoesNotCountAsUpgradeScrollUse() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/spells/CurseInfusion.java");

		assertFalse(source.contains("upgradeScrollUses"));
	}

	@Test
	public void zeroLevelAffixedSealReceivesUpgradeScrollCreditInsteadOfArmor() {
		Armor armor = new Armor(1);
		BrokenSeal seal = new BrokenSeal();
		armor.affixSeal(seal);

		assertSame(seal, armor.upgradeScrollCreditTarget());
	}

	@Test
	public void upgradedAffixedSealLeavesLaterUpgradeScrollCreditOnArmor() {
		Armor armor = new Armor(1);
		BrokenSeal seal = new BrokenSeal();
		seal.upgrade();
		armor.affixSeal(seal);

		assertSame(armor, armor.upgradeScrollCreditTarget());
	}

	@Test
	public void upgradeScrollCreditsTheResolvedUpgradeTarget() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfUpgrade.java");

		assertTrue(source.contains("((Armor) item).upgradeScrollCreditTarget()"));
		assertTrue(source.contains("upgradeScrollCreditTarget.upgradeScrollUses++;"));
		assertFalse(source.contains("item.upgradeScrollUses++;"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/java").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
