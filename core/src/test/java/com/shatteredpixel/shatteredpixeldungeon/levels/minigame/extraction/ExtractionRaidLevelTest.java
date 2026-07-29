package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.Treasures;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ChronoSuccubus;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.RaidKeyCarrier;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.VaultArmoredStatue;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ExtractionRaidLevelTest {

	private int previousDepth;
	private int previousBranch;
	private Level previousLevel;
	private Hero previousHero;

	@Before
	public void setUp() {
		previousDepth = Dungeon.depth;
		previousBranch = Dungeon.branch;
		previousLevel = Dungeon.level;
		previousHero = Dungeon.hero;
		Dungeon.depth = 31;
		Dungeon.branch = 1;
		Dungeon.hero = null;
		Notes.reset();
	}

	@After
	public void tearDown() {
		Dungeon.depth = previousDepth;
		Dungeon.branch = previousBranch;
		Dungeon.level = previousLevel;
		Dungeon.hero = previousHero;
		Notes.reset();
	}

	@Test
	public void belongsOnlyToDepthThirtyOneBranchOneAndUsesFreshSeeds() throws Exception {
		Class<?> type = raidLevelType();
		Method isRaidLocation = type.getMethod("isRaidLocation", int.class, int.class);
		assertTrue((Boolean) isRaidLocation.invoke(null, 31, 1));
		assertFalse((Boolean) isRaidLocation.invoke(null, 31, 0));
		assertFalse((Boolean) isRaidLocation.invoke(null, 30, 1));

		Object first = type.getDeclaredConstructor().newInstance();
		Object second = type.getDeclaredConstructor().newInstance();
		Method raidSeed = type.getMethod("raidSeed");
		assertNotEquals(raidSeed.invoke(first), raidSeed.invoke(second));
	}

	@Test
	public void createsConnectedFloorWithSafeEntranceAndExtractionTransition() throws Exception {
		for (int generation = 0; generation < 8; generation++) {
			Level level = createRaidLevel();
			Class<?> type = level.getClass().getSuperclass();

			assertTrue(countTerrain(level, Terrain.CHASM) == 0);
			assertEquals(1, countTerrain(level, Terrain.ENTRANCE));
			assertEquals(1, countTerrain(level, Terrain.EXIT));
			assertEquals(2, level.transitions.size());
			assertEquals(level.entrance(),
					level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE).cell());
			assertEquals(level.exit(),
					level.getTransition(LevelTransition.Type.REGULAR_EXIT).cell());

			int raidRoomCount = (Integer) type.getMethod("raidRoomCount").invoke(level);
			assertTrue(raidRoomCount >= 12 && raidRoomCount <= 15);

			int extractionCell = (Integer) type.getMethod("extractionCell").invoke(level);
			assertTrue(level.passable[level.entrance()]);
			assertTrue(level.passable[extractionCell]);
			assertEquals(countWalkable(level), connectedWalkable(level, level.entrance()));
		}
	}

	@Test
	public void createsTenToFifteenRaidMobsPlusTwoArmoredStatues() throws Exception {
		Class<?> statueType = Class.forName(
				"com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.VaultArmoredStatue");
		Class<?> eyeType = Class.forName(
				"com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.VeilbreakerEye");
		Class<?> succubusType = Class.forName(
				"com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.ChronoSuccubus");
		Class<?> scorpioType = Class.forName(
				"com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.DeferredScorpio");

		for (int generation = 0; generation < 8; generation++) {
			Level level = createRaidLevel();
			Class<?> type = level.getClass().getSuperclass();
			int statues = 0;
			int raidMobs = 0;
			for (Mob mob : level.mobs) {
				if (statueType.isInstance(mob)) {
					statues++;
				} else if (eyeType.isInstance(mob)
						|| succubusType.isInstance(mob)
						|| scorpioType.isInstance(mob)) {
					raidMobs++;
				}
			}

			assertEquals(2, statues);
			assertTrue(raidMobs >= 10 && raidMobs <= 15);
			assertEquals(type.getMethod("raidMobTarget").invoke(level), raidMobs);
			assertEquals(raidMobs + statues, level.mobs.size());
		}
	}

	@Test
	public void restoresRaidRoomsForPostLoadSpawnSelection() {
		ExtractionRaidLevel original = createRaidLevel();
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);
		// Headless tests do not initialize Game.versionCode, but Level rejects old saves.
		bundle.put("version", ShatteredPixelDungeon.v2_4_2);

		ExtractionRaidLevel restored = newRaidLevel();
		Dungeon.level = restored;
		restored.restoreFromBundle(bundle);

		assertEquals(original.raidSeed(), restored.raidSeed());
		assertEquals(original.raidRoomCount(), restored.raidRoomCount());
		int respawnCell = restored.randomRespawnCell(new ChronoSuccubus());
		assertTrue(respawnCell >= 0);
		assertTrue(restored.passable[respawnCell]);
	}

	@Test
	public void createsOnlySpecifiedLooseLootAndMarksItWithTheRaidId() {
		for (int generation = 0; generation < 8; generation++) {
			ExtractionRaidLevel level = createRaidLevel();
			int treasures = 0;
			int missiles = 0;
			int goldenKeys = 0;
			int lockedCrystalKeys = 0;

			for (Heap heap : level.heaps.valueList()) {
				assertTrue(level.passable[heap.pos]);
				assertFalse(level.solid[heap.pos]);
				assertNotEquals(level.entrance(), heap.pos);
				assertNotEquals(level.extractionCell(), heap.pos);

				for (Item item : heap.items) {
					if (item instanceof Treasures) {
						treasures++;
						assertEquals(level.raidId(), item.extractionRaidId());
					} else if (item instanceof MissileWeapon) {
						MissileWeapon missile = (MissileWeapon) item;
						missiles++;
						assertEquals(3, missile.quantity());
						assertTrue(missile.tier == 4 || missile.tier == 5);
						assertTrue(missile.getEnchant() != null);
						assertEquals(level.raidId(), item.extractionRaidId());
					} else if (item instanceof TestGoldenKey) {
						goldenKeys++;
						assertEquals(Heap.Type.HEAP, heap.type);
					} else if (item instanceof TestCrystalKey) {
						lockedCrystalKeys++;
						assertEquals(Heap.Type.LOCKED_CHEST, heap.type);
					} else {
						throw new AssertionError("Unexpected loose raid item: " + item.getClass());
					}
				}
			}

			assertTrue(treasures >= 3 && treasures <= 4);
			assertTrue(missiles >= 1 && missiles <= 2);
			assertEquals(1, goldenKeys);
			assertEquals(1, lockedCrystalKeys);
		}
	}

	@Test
	public void assignsExactlyOneOrdinaryMonsterAsTheVisibleCrystalKeyCarrier() {
		ExtractionRaidLevel level = createRaidLevel();
		int carriers = 0;
		for (Mob mob : level.mobs) {
			if (mob.buff(RaidKeyCarrier.class) != null) {
				carriers++;
				assertFalse(mob instanceof VaultArmoredStatue);
				assertEquals(com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.MARK,
						mob.buff(RaidKeyCarrier.class).icon());
			}
		}
		assertEquals(1, carriers);
	}

	@Test
	public void removingDeadCarrierMarkerDropsExactlyOneCrystalKey() {
		ExtractionRaidLevel level = createRaidLevel();
		Mob carrier = null;
		for (Mob mob : level.mobs) {
			if (mob.buff(RaidKeyCarrier.class) != null) {
				carrier = mob;
				break;
			}
		}
		assertTrue(carrier != null);

		int before = countTestCrystalKeys(level);
		carrier.HP = 0;
		carrier.buff(RaidKeyCarrier.class).detach();
		assertEquals(before + 1, countTestCrystalKeys(level));
		assertTrue(carrier.buff(RaidKeyCarrier.class) == null);
	}

	@Test
	public void extractionRequiresHeroAtExtractionCellAndAtLeastOneCrystalKey() {
		ExtractionRaidLevel level = createRaidLevel();
		assertFalse(ExtractionRaidLevel.meetsExtractionConditions(
				level.extractionCell(), level.extractionCell(), 0));
		assertTrue(ExtractionRaidLevel.meetsExtractionConditions(
				level.extractionCell(), level.extractionCell(), 1));
		assertFalse(ExtractionRaidLevel.meetsExtractionConditions(
				level.entrance(), level.extractionCell(), 1));
	}

	@Test
	public void raidIdSurvivesBundleRoundTrip() {
		ExtractionRaidLevel original = createRaidLevel();
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);
		bundle.put("version", ShatteredPixelDungeon.v2_4_2);

		ExtractionRaidLevel restored = newRaidLevel();
		Dungeon.level = restored;
		restored.restoreFromBundle(bundle);

		assertTrue(original.raidId() > 0);
		assertEquals(original.raidId(), restored.raidId());
		int carriers = 0;
		for (Mob mob : restored.mobs) {
			if (mob.buff(RaidKeyCarrier.class) != null) carriers++;
		}
		assertEquals(1, carriers);
		for (Heap heap : restored.heaps.valueList()) {
			for (Item item : heap.items) {
				if (item instanceof Treasures || item instanceof MissileWeapon) {
					assertEquals(restored.raidId(), item.extractionRaidId());
				}
			}
		}
	}

	@Test
	public void missileTierRollIsTwentyPercentTierFourAndEightyPercentTierFive() {
		assertEquals(4, ExtractionRaidLevel.missileTierForRoll(0f));
		assertEquals(4, ExtractionRaidLevel.missileTierForRoll(0.199999f));
		assertEquals(5, ExtractionRaidLevel.missileTierForRoll(0.2f));
		assertEquals(5, ExtractionRaidLevel.missileTierForRoll(0.999999f));
	}

	private static Class<?> raidLevelType() throws ClassNotFoundException {
		return Class.forName(
				"com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevel");
	}

	private static ExtractionRaidLevel createRaidLevel() {
		ExtractionRaidLevel level = newRaidLevel();
		Dungeon.level = level;
		level.create();
		return level;
	}

	private static ExtractionRaidLevel newRaidLevel() {
		return new ExtractionRaidLevel() {
			@Override
			public Heap drop(Item item, int cell) {
				// Level.drop also updates the live GameScene, which is unavailable in headless tests.
				Heap heap = heaps.get(cell);
				if (heap == null) {
					heap = new Heap();
					heap.pos = cell;
					heaps.put(cell, heap);
				}
				heap.drop(item);
				return heap;
			}

			@Override
			public Mob createMob() {
				// Eye loot initializes item textures; room placement does not need that dependency.
				return new ChronoSuccubus();
			}

			@Override
			protected VaultArmoredStatue createVaultStatue() {
				// Equipment sprites require a running libGDX application; topology does not.
				return new VaultArmoredStatue();
			}

			@Override
			protected Treasures createRaidTreasure() {
				return new TestTreasure();
			}

			@Override
			protected MissileWeapon createRaidMissile() {
				TestMissile missile = new TestMissile();
				missile.tier = 4;
				return missile;
			}

			@Override
			protected Item createRaidGoldenKey() {
				return new TestGoldenKey();
			}

			@Override
			protected Item createRaidCrystalKey() {
				return new TestCrystalKey();
			}
		};
	}

	private static class TestMissile extends MissileWeapon {
	}

	private static class TestTreasure extends Treasures {
		TestTreasure() {
			super(7, CollectionRarity.COMMON, 500);
		}
	}

	private static class TestGoldenKey extends Item {
	}

	private static class TestCrystalKey extends Item {
	}

	private static int countTerrain(Level level, int terrain) {
		int result = 0;
		for (int tile : level.map) {
			if (tile == terrain) {
				result++;
			}
		}
		return result;
	}

	private static int countWalkable(Level level) {
		int result = 0;
		for (int i = 0; i < level.length(); i++) {
			if (level.passable[i] || level.avoid[i]) {
				result++;
			}
		}
		return result;
	}

	private static int connectedWalkable(Level level, int start) {
		boolean[] visited = new boolean[level.length()];
		Queue<Integer> pending = new ArrayDeque<>();
		visited[start] = true;
		pending.add(start);
		int result = 0;

		int[] offsets = {-level.width(), 1, level.width(), -1};
		while (!pending.isEmpty()) {
			int current = pending.remove();
			result++;
			for (int offset : offsets) {
				int next = current + offset;
				if (next < 0 || next >= level.length() || visited[next]) {
					continue;
				}
				if (offset == 1 && next % level.width() == 0
						|| offset == -1 && current % level.width() == 0) {
					continue;
				}
				if (level.passable[next] || level.avoid[next]) {
					visited[next] = true;
					pending.add(next);
				}
			}
		}
		return result;
	}

	private static int countTestCrystalKeys(ExtractionRaidLevel level) {
		int result = 0;
		for (Heap heap : level.heaps.valueList()) {
			for (Item item : heap.items) {
				if (item instanceof TestCrystalKey) result++;
			}
		}
		return result;
	}
}
