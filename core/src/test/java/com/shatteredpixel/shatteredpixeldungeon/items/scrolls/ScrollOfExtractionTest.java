package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
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
		MissileWeapon.UpgradedSetTracker tracker =
				new MissileWeapon.UpgradedSetTracker();
		tracker.levelThresholds.put(41L, 4);
		tracker.upgradeScrollCredits.put(41L, 2);

		assertEquals(2, UpgradeExtraction.extractUpgradeUses(selected, backpack, tracker));

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

	@Test(expected = IllegalArgumentException.class)
	public void missileExtractionRequiresSetTrackerContext() {
		TestMissile missile = missile(43L, 2, 2, 1);

		assertFalse(UpgradeExtraction.canExtract(missile));
		UpgradeExtraction.extractUpgradeUses(missile);
	}

	@Test
	public void thrownMissileMemberCannotRestoreConsumedUpgradeCredits() {
		MissileWeapon.UpgradedSetTracker tracker =
				new MissileWeapon.UpgradedSetTracker();
		TestMissile carried = missile(51L, 3, 3, 2);
		TestMissile thrown = missile(51L, 3, 3, 1);
		Bag backpack = new Bag();
		backpack.items.add(carried);
		tracker.levelThresholds.put(51L, 3);
		tracker.upgradeScrollCredits.put(51L, 3);

		assertEquals(3, UpgradeExtraction.extractUpgradeUses(carried, backpack, tracker));
		assertTrue(tracker.synchronizeMember(thrown));

		assertEquals(0, thrown.trueLevel());
		assertEquals(0, thrown.upgradeScrollUses);
		assertFalse(UpgradeExtraction.canExtract(thrown, tracker));
	}

	@Test
	public void onlyNewUpgradeCreditSurvivesAfterExtraction() {
		MissileWeapon.UpgradedSetTracker tracker =
				new MissileWeapon.UpgradedSetTracker();
		TestMissile carried = missile(52L, 3, 3, 2);
		TestMissile staleThrown = missile(52L, 3, 3, 1);
		Bag backpack = new Bag();
		backpack.items.add(carried);
		tracker.levelThresholds.put(52L, 3);
		tracker.upgradeScrollCredits.put(52L, 3);

		assertEquals(3, UpgradeExtraction.extractUpgradeUses(carried, backpack, tracker));
		carried.level(1);
		tracker.levelThresholds.put(52L, 1);
		tracker.recordUpgradeScrollUse(carried);

		assertTrue(tracker.synchronizeMember(staleThrown));
		assertEquals(1, staleThrown.trueLevel());
		assertEquals(1, staleThrown.upgradeScrollUses);
		assertTrue(UpgradeExtraction.canExtract(staleThrown, tracker));
		assertEquals(1, UpgradeExtraction.extractUpgradeUses(staleThrown, null, tracker));
		assertFalse(UpgradeExtraction.canExtract(staleThrown, tracker));
	}

	@Test
	public void missileUpgradeCreditLedgerSurvivesSaveAndLoad() {
		MissileWeapon.UpgradedSetTracker original =
				new MissileWeapon.UpgradedSetTracker();
		original.levelThresholds.put(53L, 4);
		original.upgradeScrollCredits.put(53L, 2);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		MissileWeapon.UpgradedSetTracker restored =
				new MissileWeapon.UpgradedSetTracker();
		restored.restoreFromBundle(bundle);
		TestMissile member = missile(53L, 4, 99, 1);

		assertTrue(restored.synchronizeMember(member));
		assertEquals(4, member.trueLevel());
		assertEquals(2, member.upgradeScrollUses);
	}

	@Test
	public void legacyStaleMemberDoesNotMigrateConsumedCredits() {
		Bundle legacy = new Bundle();
		legacy.put(MissileWeapon.UpgradedSetTracker.SET_IDS, new long[]{54L});
		legacy.put(MissileWeapon.UpgradedSetTracker.SET_LEVELS, new int[]{0});
		MissileWeapon.UpgradedSetTracker restored =
				new MissileWeapon.UpgradedSetTracker();
		restored.restoreFromBundle(legacy);
		TestMissile stale = missile(54L, 3, 3, 1);

		assertTrue(restored.synchronizeMember(stale));
		assertEquals(0, stale.trueLevel());
		assertEquals(0, stale.upgradeScrollUses);
	}

	@Test
	public void lowerLevelDuplicateIsRejectedAsStale() {
		MissileWeapon.UpgradedSetTracker tracker =
				new MissileWeapon.UpgradedSetTracker();
		tracker.levelThresholds.put(55L, 3);
		tracker.upgradeScrollCredits.put(55L, 2);
		TestMissile duplicate = missile(55L, 2, 2, 1);

		assertFalse(tracker.synchronizeMember(duplicate));
		assertEquals(2, duplicate.trueLevel());
		assertEquals(0, duplicate.upgradeScrollUses);
	}

	@Test
	public void invalidatedSetCannotBeReopenedByAStaleMember() {
		MissileWeapon.UpgradedSetTracker tracker =
				new MissileWeapon.UpgradedSetTracker();
		TestMissile stale = missile(58L, 5, 5, 1);

		tracker.setCanonicalLevel(stale, 5);
		tracker.invalidateSet(stale);
		tracker.setCanonicalLevel(stale, 0);

		assertFalse(tracker.synchronizeMember(stale));
		assertEquals(0, stale.upgradeScrollUses);
		assertEquals(0, tracker.availableUpgradeScrollUses(stale));
	}

	@Test
	public void merchantAndContainerBoundariesUseTheCentralMissileLedger() throws IOException {
		String tradeSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndTradeItem.java");
		String shopSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Shopkeeper.java");
		String surfaceShopSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/SurfaceShopkeeper.java");
		String bagSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/bags/Bag.java");

		assertFalse(tradeSource.contains("levelThresholds.put(((MissileWeapon) item).setID"));
		assertFalse(shopSource.contains("levelThresholds.put(((MissileWeapon) returned).setID"));
		assertFalse(surfaceShopSource.contains("levelThresholds.put(((MissileWeapon) returned).setID"));
		assertTrue(bagSource.contains("MissileWeapon.sanitizeInventorySets"));
		assertTrue(bagSource.contains("MissileWeapon.prepareIncomingBag"));
		assertTrue(tradeSource.contains("UpgradedSetTracker.markSold"));
	}

	@Test
	public void legacyNewCycleResetKeepsZeroLevelMissileValid() {
		Bundle legacy = new Bundle();
		legacy.put(MissileWeapon.UpgradedSetTracker.SET_IDS, new long[]{56L});
		legacy.put(MissileWeapon.UpgradedSetTracker.SET_LEVELS, new int[]{3});
		MissileWeapon.UpgradedSetTracker restored =
				new MissileWeapon.UpgradedSetTracker();
		restored.restoreFromBundle(legacy);
		restored.resetLegacyForNewCycle();
		TestMissile resetForNewCycle = missile(56L, 0, 3, 3);

		assertTrue(restored.synchronizeMember(resetForNewCycle));
		assertEquals(0, resetForNewCycle.trueLevel());
		assertEquals(0, resetForNewCycle.upgradeScrollUses);

		Bundle migrated = new Bundle();
		restored.storeInBundle(migrated);
		MissileWeapon.UpgradedSetTracker reloaded =
				new MissileWeapon.UpgradedSetTracker();
		reloaded.restoreFromBundle(migrated);
		TestMissile oldGroundMember = missile(57L, 0, 4, 1);
		assertTrue(reloaded.synchronizeMember(oldGroundMember));
		assertEquals(0, oldGroundMember.upgradeScrollUses);
	}

	@Test
	public void extractionScrollUsesFixedKnownInventoryScrollFlow() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/scrolls/ScrollOfExtraction.java");

		assertTrue(source.contains("extends InventoryScroll"));
		assertTrue(source.contains("public boolean isKnown()"));
		assertTrue(source.contains("image = EXItemSpriteSheet.SCROLL_EXTRACTION;"));
		assertTrue(source.contains("new ScrollOfUpgrade().quantity(extracted)"));
		assertTrue(source.contains("curUser.belongings.backpack, curUser"));
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
