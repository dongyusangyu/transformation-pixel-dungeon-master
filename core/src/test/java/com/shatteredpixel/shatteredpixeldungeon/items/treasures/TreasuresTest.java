package com.shatteredpixel.shatteredpixeldungeon.items.treasures;

import com.watabou.utils.Bundle;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreasuresTest {

	@Test
	public void rarityThresholdsMatchConfiguredProbabilities() {
		assertEquals(Treasures.Rarity.LEGENDARY, Treasures.rarityForRoll(0f));
		assertEquals(Treasures.Rarity.LEGENDARY, Treasures.rarityForRoll(0.000999f));
		assertEquals(Treasures.Rarity.EPIC, Treasures.rarityForRoll(0.001f));
		assertEquals(Treasures.Rarity.EPIC, Treasures.rarityForRoll(0.010999f));
		assertEquals(Treasures.Rarity.RARE, Treasures.rarityForRoll(0.011f));
		assertEquals(Treasures.Rarity.RARE, Treasures.rarityForRoll(0.110999f));
		assertEquals(Treasures.Rarity.COMMON, Treasures.rarityForRoll(0.111f));
		assertEquals(Treasures.Rarity.COMMON, Treasures.rarityForRoll(0.999999f));
	}

	@Test
	public void storedRarityFallsBackSafely() {
		assertEquals(Treasures.Rarity.EPIC, Treasures.rarityFromStoredName("EPIC"));
		assertEquals(Treasures.Rarity.COMMON, Treasures.rarityFromStoredName("UNKNOWN"));
		assertEquals(Treasures.Rarity.COMMON, Treasures.rarityFromStoredName(null));
	}

	@Test
	public void collectionRarityAndValueUseQualityMultiplierAndTopLegendaryPremium() {
		Treasures commonQuality = new Treasures(
				7, Treasures.CollectionRarity.TOP, 3000, Treasures.Rarity.COMMON);
		Treasures legendaryQuality = new Treasures(
				7, Treasures.CollectionRarity.TOP, 3000, Treasures.Rarity.LEGENDARY);

		assertEquals(Treasures.Rarity.COMMON, commonQuality.rarity());
		assertEquals(Treasures.Rarity.LEGENDARY, legendaryQuality.rarity());
		assertEquals(Treasures.CollectionRarity.TOP, commonQuality.collectionRarity());
		assertEquals(Treasures.CollectionRarity.TOP, legendaryQuality.collectionRarity());
		assertEquals(3000, commonQuality.value());
		assertEquals(4_980_000, legendaryQuality.value());
		assertEquals(7, commonQuality.image);
		assertEquals(7, legendaryQuality.image);
	}

	@Test
	public void qualityMultipliersAndQuantityApplyPerUnit() {
		assertEquals(1, Treasures.qualityMultiplier(Treasures.Rarity.COMMON));
		assertEquals(2, Treasures.qualityMultiplier(Treasures.Rarity.RARE));
		assertEquals(5, Treasures.qualityMultiplier(Treasures.Rarity.EPIC));
		assertEquals(10, Treasures.qualityMultiplier(Treasures.Rarity.LEGENDARY));

		Treasures epic = new Treasures(
				7, Treasures.CollectionRarity.COMMON, 500, Treasures.Rarity.EPIC);
		epic.quantity(2);
		assertEquals(5_000, epic.value());

		Treasures topLegendary = new Treasures(
				7, Treasures.CollectionRarity.TOP, 5_000, Treasures.Rarity.LEGENDARY);
		topLegendary.quantity(2);
		assertEquals(10_000_000, topLegendary.value());
	}

	@Test
	public void totalValueSaturatesInsteadOfOverflowing() {
		Treasures topLegendary = new Treasures(
				7, Treasures.CollectionRarity.TOP, 5_000, Treasures.Rarity.LEGENDARY);

		topLegendary.quantity(429);
		assertEquals(2_145_000_000, topLegendary.value());

		topLegendary.quantity(430);
		assertEquals(Integer.MAX_VALUE, topLegendary.value());

		topLegendary.quantity(0);
		assertEquals(0, topLegendary.value());

		topLegendary.quantity(-1);
		assertEquals(0, topLegendary.value());
	}

	@Test
	public void generatedQualitySurvivesBundleRestore() {
		Treasures original = new Treasures(
				7, Treasures.CollectionRarity.TOP, 3000, Treasures.Rarity.EPIC);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		Treasures restored = new Treasures(
				7, Treasures.CollectionRarity.TOP, 3000, Treasures.Rarity.COMMON);
		restored.restoreFromBundle(bundle);

		assertEquals(Treasures.Rarity.EPIC, restored.rarity());
		assertEquals(Treasures.CollectionRarity.TOP, restored.collectionRarity());
		assertEquals(15000, restored.value());
	}

	@Test
	public void legacyBundleWithoutQualityFallsBackToCommon() {
		Treasures legacy = new Treasures(
				7, Treasures.CollectionRarity.TOP, 3000, Treasures.Rarity.LEGENDARY);
		Bundle legacyBundle = new Bundle();
		legacy.storeInBundle(legacyBundle);
		legacyBundle.remove("rarity");

		Treasures restored = new Treasures(
				7, Treasures.CollectionRarity.TOP, 3000, Treasures.Rarity.LEGENDARY);

		restored.restoreFromBundle(legacyBundle);

		assertEquals(Treasures.Rarity.COMMON, restored.rarity());
		assertEquals(3000, restored.value());
	}

	@Test
	public void journalDescriptionKeepsOnlyBaseDescriptionAndCollectionRarity()
			throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/treasures/Treasures.java");
		int start = source.indexOf("public String journalDesc()");
		int end = source.indexOf("@Override", start);

		assertTrue("missing journalDesc()", start >= 0);
		assertTrue("journalDesc() must end before the next override", end > start);
		String method = source.substring(start, end);
		assertTrue(method.contains("super.desc()"));
		assertTrue(method.contains("collectionRarityName()"));
		assertFalse(method.contains("rarityName()"));
		assertFalse(method.contains("value()"));
	}

	@Test
	public void treasureDeclaresRequiredItemContract() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/treasures/Treasures.java");
		assertTrue(source.contains(
				"this(ItemSpriteSheet.CHEST, CollectionRarity.COMMON, 0,"));
		assertTrue(source.contains("this.image = image;"));
		assertTrue(source.contains("stackable = false;"));
		assertTrue(source.contains("public boolean isUpgradable()"));
		assertTrue(source.contains("return false;"));
		assertTrue(source.contains("public Treasures setRarity(Rarity rarity)"));
		assertTrue(source.contains("bundle.put(RARITY, rarity.name());"));
		assertTrue(source.contains("rarity = rarityFromStoredName(bundle.contains(RARITY)"));
		assertTrue(source.contains("? bundle.getString(RARITY)"));
	}

	@Test
	public void transmutationExplicitlyRejectsTreasures() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfTransmutation.java");
		assertTrue(source.contains("if (item instanceof Treasures)"));
		assertTrue(source.contains("import com.shatteredpixel.shatteredpixeldungeon.items.treasures.Treasures;"));
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
