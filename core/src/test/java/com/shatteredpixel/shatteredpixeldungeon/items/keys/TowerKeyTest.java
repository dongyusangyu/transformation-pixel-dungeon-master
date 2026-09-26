package com.shatteredpixel.shatteredpixeldungeon.items.keys;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerKeyDepth;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerKeyTest {

	private int previousDepth;
	private int previousBranch;
	private String previousVersion;

	@Before
	public void setUp() {
		previousDepth = Dungeon.depth;
		previousBranch = Dungeon.branch;
		previousVersion = Game.version;
		Game.version = "test";
		Notes.reset();
	}

	@After
	public void tearDown() {
		Notes.reset();
		Dungeon.depth = previousDepth;
		Dungeon.branch = previousBranch;
		Game.version = previousVersion;
	}

	@Test
	public void pickupKeepsTheGenerationDepthWhenKeyFallsToAnotherTowerFloor() {
		Dungeon.branch = TowerLevel.BRANCH;
		Dungeon.depth = 18;
		TestIronKey key = new TestIronKey(Dungeon.depth);

		Dungeon.depth = 3;
		Notes.add(key);

		assertEquals(18, key.depth);
		assertEquals(TowerLevel.BRANCH, key.branch);
		assertEquals(1, Notes.keyCount(new TestIronKey(18)));
		assertEquals(0, Notes.keyCount(new TestIronKey(3)));
	}

	@Test
	public void pickupDoesNotRebindKeyToTheDestinationFloor() throws Exception {
		String source = readKeySource();
		int pickupStart = source.indexOf("public boolean doPickUp(Hero hero, int pos)");
		int bundleFields = source.indexOf("private static final String DEPTH", pickupStart);

		assertTrue(pickupStart >= 0);
		assertTrue(bundleFields > pickupStart);
		assertFalse(source.substring(pickupStart, bundleFields).contains("bindToCurrentLocation();"));
	}

	@Test
	public void noArgKeyConstructorsUseTheCurrentLocation() throws Exception {
		Dungeon.depth = 4;
		Dungeon.branch = TowerLevel.BRANCH;
		CurrentLocationKey key = new CurrentLocationKey();
		assertEquals(4, key.depth);
		assertEquals(TowerLevel.BRANCH, key.branch);

		assertTrue(readSource("IronKey.java").contains("this( Dungeon.depth );"));
		assertTrue(readSource("GoldenKey.java").contains("this( Dungeon.depth );"));
		assertTrue(readSource("CrystalKey.java").contains("this( Dungeon.depth );"));
		assertTrue(readSource("WornKey.java").contains("this( Dungeon.depth );"));
		assertTrue(readSource("SkeletonKey.java").contains("this( Dungeon.depth );"));
	}

	private static String readSource(String fileName) throws Exception {
		Path root = Paths.get(System.getProperty("user.dir"));
		if (!root.endsWith("core")) root = root.resolve("core");
		return new String(Files.readAllBytes(root.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/keys/" + fileName)),
				StandardCharsets.UTF_8);
	}

	private static String readKeySource() throws Exception {
		Path root = Paths.get(System.getProperty("user.dir"));
		if (root.endsWith("core")) {
			return new String(Files.readAllBytes(root.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/keys/Key.java")), StandardCharsets.UTF_8);
		}
		return new String(Files.readAllBytes(root.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/keys/Key.java")), StandardCharsets.UTF_8);
	}

	@Test
	public void keysFromTheMainDungeonAndTowerDoNotMix() {
		Dungeon.depth = 3;
		Dungeon.branch = TowerLevel.BRANCH;
		Notes.add(new TestIronKey(3));

		assertEquals(1, Notes.keyCount(new TestIronKey(3)));

		Dungeon.branch = 0;
		assertEquals(0, Notes.keyCount(new TestIronKey(3)));
		assertFalse(Notes.remove(new TestIronKey(3)));
	}

	@Test
	public void towerDoorAndChestQueriesFindTheirMatchingKeys() {
		Dungeon.depth = 7;
		Dungeon.branch = TowerLevel.BRANCH;
		Notes.add(new TestIronKey(7));
		Notes.add(new TestGoldenKey(7));
		Notes.add(new TestCrystalKey(7));

		assertEquals(1, Notes.keyCount(new TestIronKey(Dungeon.depth)));
		assertEquals(1, Notes.keyCount(new TestGoldenKey(Dungeon.depth)));
		assertEquals(1, Notes.keyCount(new TestCrystalKey(Dungeon.depth)));
	}

	@Test
	public void secretChestKeysUseTheTowerFloorInsteadOfItsContentDepth() {
		assertEquals(3, TowerKeyDepth.forGeneration(3, 18));
		assertEquals(10, TowerKeyDepth.forGeneration(10, 25));
		assertEquals(25, TowerKeyDepth.forGeneration(0, 25));
		assertEquals(18, TowerKeyDepth.legacyContentDepth(3));
		assertEquals(25, TowerKeyDepth.legacyContentDepth(10));
		assertEquals(25, TowerKeyDepth.legacyContentDepth(70));
	}

	@Test
	public void secretChestRoomUsesTowerDepthAndScopesLegacyKeyFallback() throws Exception {
		String room = readLevelSource("rooms/secret/SecretChestChasmRoom.java");
		String hero = readLevelSource("../actors/hero/Hero.java");
		assertTrue(room.contains("((TowerLevel) level).secretRoomKeyDepth()"));
		assertEquals(4, occurrences(room, "new GoldenKey(keyDepth(level))"));
		assertTrue(hero.contains("private boolean isLegacyTowerSecretChest"));
		assertTrue(hero.contains("SecretChestChasmRoom.containsCell(Dungeon.level, cell)"));
		assertTrue(hero.contains("TowerLevel.legacyContentDepthForFloor(Dungeon.depth)"));
	}

	private static String readLevelSource(String relativePath) throws Exception {
		Path root = Paths.get(System.getProperty("user.dir"));
		if (!root.endsWith("core")) root = root.resolve("core");
		return new String(Files.readAllBytes(root.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/" + relativePath)),
				StandardCharsets.UTF_8);
	}

	private static int occurrences(String text, String token) {
		int count = 0;
		int offset = 0;
		while ((offset = text.indexOf(token, offset)) >= 0) {
			count++;
			offset += token.length();
		}
		return count;
	}

	@Test
	public void keyBranchSurvivesSaveAndLoad() {
		Dungeon.depth = 5;
		Dungeon.branch = TowerLevel.BRANCH;
		TestIronKey original = new TestIronKey(5);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		Dungeon.branch = 0;
		TestIronKey restored = new TestIronKey();
		restored.restoreFromBundle(bundle);

		assertEquals(5, restored.depth);
		assertEquals(TowerLevel.BRANCH, restored.branch);
	}

	@Test
	public void legacyTowerKeyRecordMigratesFromContentDepth() {
		Dungeon.depth = 3;
		Dungeon.branch = TowerLevel.BRANCH;
		Bundle legacyKeyBundle = new Bundle();
		legacyKeyBundle.put("depth", 18);
		legacyKeyBundle.put("quantity", 1);
		TestIronKey legacyKey = new TestIronKey();
		legacyKey.restoreFromBundle(legacyKeyBundle);
		Notes.add(legacyKey);

		Notes.migrateLegacyKeys(Dungeon.depth, Dungeon.branch);

		assertEquals(1, Notes.keyCount(new TestIronKey(3)));
		assertEquals(0, Notes.keyCount(new TestIronKey(18)));
	}

	public static class TestIronKey extends Key {
		public TestIronKey() {
			this(0);
		}

		public TestIronKey(int depth) {
			this.depth = depth;
		}
	}

	public static class CurrentLocationKey extends Key {
	}

	public static class TestGoldenKey extends Key {
		public TestGoldenKey() {
			this(0);
		}

		public TestGoldenKey(int depth) {
			this.depth = depth;
		}
	}

	public static class TestCrystalKey extends Key {
		public TestCrystalKey() {
			this(0);
		}

		public TestCrystalKey(int depth) {
			this.depth = depth;
		}
	}
}
