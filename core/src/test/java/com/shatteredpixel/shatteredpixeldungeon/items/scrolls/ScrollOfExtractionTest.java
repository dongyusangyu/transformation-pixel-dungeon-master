package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScrollOfExtractionTest {

	@Test
	public void extractionScrollUsesBaseValueForty() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtraction.java");

		assertTrue(source.contains("public int value()"));
		assertTrue(source.contains("return 40 * quantity;"));
	}

	@Test
	public void onlyUpgradableItemsWithRecordedUsesCanBeExtracted() {
		TestUpgradableItem noUses = new TestUpgradableItem(true);
		TestUpgradableItem notUpgradable = new TestUpgradableItem(false);
		notUpgradable.upgradeScrollUses = 1;
		TestUpgradableItem eligible = new TestUpgradableItem(true);
		eligible.upgradeScrollUses = 1;

		assertFalse(UpgradeExtraction.canExtract(noUses));
		assertFalse(UpgradeExtraction.canExtract(notUpgradable));
		assertTrue(UpgradeExtraction.canExtract(eligible));
	}

	@Test
	public void extractionRemovesRecordedLevelsAndClearsTheRecord() {
		TestUpgradableItem item = new TestUpgradableItem(true);
		item.upgrade(5);
		item.upgradeScrollUses = 3;

		assertEquals(3, UpgradeExtraction.extractUpgradeUses(item));
		assertEquals(2, item.trueLevel());
		assertEquals(0, item.upgradeScrollUses);
	}

	@Test
	public void extractionReturnsEveryRecordedUseButNeverDegradesBelowZero() {
		TestUpgradableItem item = new TestUpgradableItem(true);
		item.upgrade(2);
		item.upgradeScrollUses = 5;

		assertEquals(5, UpgradeExtraction.extractUpgradeUses(item));
		assertEquals(0, item.trueLevel());
		assertEquals(0, item.upgradeScrollUses);
	}

	@Test
	public void extractionDegradesAndClearsTheWholeSplitMissileSet() {
		TestMissile selected = missile(41L, 4, 2, 2);
		TestMissile nestedMember = missile(41L, 4, 2, 1);
		TestMissile otherSet = missile(42L, 4, 2, 3);
		Bag backpack = new Bag();
		Bag nestedBag = new Bag();
		backpack.items.add(selected);
		backpack.items.add(nestedBag);
		nestedBag.items.add(nestedMember);
		nestedBag.items.add(otherSet);

		assertEquals(2, UpgradeExtraction.extractUpgradeUses(selected, backpack));

		assertEquals(2, selected.trueLevel());
		assertEquals(2, nestedMember.trueLevel());
		assertEquals(4, otherSet.trueLevel());
		assertEquals(0, selected.upgradeScrollUses);
		assertEquals(0, nestedMember.upgradeScrollUses);
		assertEquals(2, otherSet.upgradeScrollUses);
		assertEquals(2, selected.quantity());
		assertEquals(1, nestedMember.quantity());
		assertEquals(3, otherSet.quantity());
	}

	@Test
	public void extractionScrollUsesFixedKnownInventoryScrollFlow() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtraction.java");

		assertTrue(source.contains("extends InventoryScroll"));
		assertTrue(source.contains("public boolean isKnown()"));
		assertTrue(source.contains("image = EXItemSpriteSheet.SCROLL_EXTRACTION;"));
		assertTrue(source.contains("new ScrollOfUpgrade().quantity(extracted)"));
		assertTrue(source.contains(
				"UpgradeExtraction.extractUpgradeUses(item, curUser.belongings.backpack)"));
		assertFalse(source.contains("anonymous = true"));
	}

	@Test
	public void extractionScrollHasCompleteEnglishAndChineseMessages() throws IOException {
		String english = readCoreAsset("messages/items/items.properties");
		String chinese = readCoreAsset("messages/items/items_zh.properties");
		String[] keys = {
				"items.scrolls.scrollofextraction.name=",
				"items.scrolls.scrollofextraction.inv_title=",
				"items.scrolls.scrollofextraction.desc=",
				"items.scrolls.scrollofextraction.extract="
		};

		for (String key : keys) {
			assertEquals(key, 1, occurrences(english, key));
			assertEquals(key, 1, occurrences(chinese, key));
		}
	}

	private static String readCoreSource(String relativePath) throws IOException {
		return readCoreFile("src/main/java", relativePath);
	}

	private static String readCoreAsset(String relativePath) throws IOException {
		return readCoreFile("src/main/assets", relativePath);
	}

	private static String readCoreFile(String sourceRoot, String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve(sourceRoot).resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

	private static int occurrences(String text, String needle) {
		int count = 0;
		for (int index = text.indexOf(needle); index >= 0;
				index = text.indexOf(needle, index + needle.length())) {
			count++;
		}
		return count;
	}

	private static TestMissile missile(long setID, int level, int upgradeUses, int quantity) {
		TestMissile missile = new TestMissile();
		missile.setID = setID;
		missile.level(level);
		missile.upgradeScrollUses = upgradeUses;
		missile.quantity(quantity);
		return missile;
	}

	private static class TestUpgradableItem extends Item {

		private final boolean upgradable;

		private TestUpgradableItem(boolean upgradable) {
			this.upgradable = upgradable;
		}

		@Override
		public boolean isUpgradable() {
			return upgradable;
		}
	}

	private static class TestMissile extends MissileWeapon {
	}
}
