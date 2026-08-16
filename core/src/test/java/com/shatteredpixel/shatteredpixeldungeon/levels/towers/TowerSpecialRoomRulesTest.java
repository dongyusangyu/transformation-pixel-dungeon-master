package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ArmoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalChoiceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalPathRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalVaultRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.LaboratoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PoolRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SentryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ShopRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.StorageRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ToxicGasRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.TrapsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.TreasuryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.WeakFloorRoom;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerSpecialRoomRulesTest {
	private static final Set<Class<? extends SpecialRoom>> CRYSTAL_KEY_ROOMS =
			new HashSet<>(java.util.Arrays.asList(
					CrystalChoiceRoom.class,
					CrystalPathRoom.class,
					CrystalVaultRoom.class));
	private static final Set<Class<? extends SpecialRoom>> POTION_PUZZLE_ROOMS =
			new HashSet<>(java.util.Arrays.asList(
					PoolRoom.class,
					SentryRoom.class,
					StorageRoom.class,
					ToxicGasRoom.class,
					MagicalFireRoom.class,
					TrapsRoom.class));

	@Test
	public void towerPoolContainsRequestedRoomsAndExcludesCrossFloorOrShopRooms() {
		List<Class<? extends SpecialRoom>> pool = TowerSpecialRoomRules.eligibleRoomTypes();

		assertTrue(pool.contains(ArmoryRoom.class));
		assertTrue(pool.contains(TreasuryRoom.class));
		assertTrue(pool.contains(PoolRoom.class));
		assertTrue(pool.contains(SentryRoom.class));
		assertFalse(pool.contains(LaboratoryRoom.class));
		assertFalse(pool.contains(WeakFloorRoom.class));
		assertFalse(pool.contains(PitRoom.class));
		assertFalse(pool.contains(ShopRoom.class));
	}

	@Test
	public void ordinaryAndLargeFloorsUseExpectedSpecialRoomCountsWithoutDuplicates() {
		for (long seed = 1; seed <= 64; seed++) {
			for (int floor = 1; floor <= 24; floor++) {
				if (floor % TowerBossLevel.FLOORS_PER_BOSS == 0) {
					assertTrue(TowerSpecialRoomRules.roomTypesForFloor(floor, seed, false).isEmpty());
					continue;
				}
				assertCountAndUniqueness(
						TowerSpecialRoomRules.roomTypesForFloor(floor, seed, false), 1, 2);
				assertCountAndUniqueness(
						TowerSpecialRoomRules.roomTypesForFloor(floor, seed, true), 2, 3);
			}
		}
	}

	@Test
	public void eachFiveFloorBandSchedulesOneLaboratoryBeforeItsBossFloor() {
		for (long seed = 1; seed <= 32; seed++) {
			for (int firstFloor = 1; firstFloor <= 26; firstFloor += 5) {
				int laboratories = 0;
				int laboratoryFloor = -1;
				for (int floor = firstFloor; floor < firstFloor + 5; floor++) {
					if (TowerSpecialRoomRules.roomTypesForFloor(floor, seed, false)
							.contains(LaboratoryRoom.class)) {
						laboratories++;
						laboratoryFloor = floor;
					}
				}
				assertEquals(1, laboratories);
				int localFloor = laboratoryFloor - firstFloor + 1;
				assertTrue(localFloor >= 2 && localFloor <= 4);
			}
		}
	}

	@Test
	public void sameSeedAndFloorProduceTheSameRoomTypes() {
		for (int floor = 1; floor <= 20; floor++) {
			assertEquals(
					TowerSpecialRoomRules.roomTypesForFloor(floor, 998877L, false),
					TowerSpecialRoomRules.roomTypesForFloor(floor, 998877L, false));
		}
	}

	@Test
	public void eachFloorUsesAtMostOneRoomFromEachSharedKeyOrPotionGroup() {
		for (long seed = 1; seed <= 256; seed++) {
			for (int floor = 1; floor <= 40; floor++) {
				List<Class<? extends SpecialRoom>> rooms =
						TowerSpecialRoomRules.roomTypesForFloor(floor, seed, true);
				assertTrue(countMatches(rooms, CRYSTAL_KEY_ROOMS) <= 1);
				assertTrue(countMatches(rooms, POTION_PUZZLE_ROOMS) <= 1);
			}
		}
	}

	private static void assertCountAndUniqueness(
			List<Class<? extends SpecialRoom>> rooms, int min, int max) {
		assertTrue(rooms.size() >= min && rooms.size() <= max);
		Set<Class<? extends SpecialRoom>> unique = new HashSet<>(rooms);
		assertEquals(rooms.size(), unique.size());
	}

	private static int countMatches(
			List<Class<? extends SpecialRoom>> rooms,
			Set<Class<? extends SpecialRoom>> group) {
		int matches = 0;
		for (Class<? extends SpecialRoom> room : rooms) {
			if (group.contains(room)) {
				matches++;
			}
		}
		return matches;
	}
}
