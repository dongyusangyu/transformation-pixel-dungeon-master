package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TowerStyleScheduleTest {

	private static final String[] INITIAL_STYLES = {
			"chinese_hall", "gothic_castle", "astral_library", "tower_core_gearworks"
	};

	@Test
	public void eachStyleCoversExactlyFiveConsecutiveFloors() {
		TowerStyleSchedule schedule = new TowerStyleSchedule(INITIAL_STYLES);
		for (int firstFloor = 1; firstFloor <= 16; firstFloor += 5) {
			String style = schedule.styleIdForFloor(firstFloor, 123456L);
			for (int floor = firstFloor; floor < firstFloor + 5; floor++) {
				assertEquals(style, schedule.styleIdForFloor(floor, 123456L));
			}
		}
	}

	@Test
	public void sameSeedProducesSameRandomizedOrder() {
		TowerStyleSchedule first = new TowerStyleSchedule(INITIAL_STYLES);
		TowerStyleSchedule second = new TowerStyleSchedule(INITIAL_STYLES);

		for (int floor = 1; floor <= 40; floor += 5) {
			assertEquals(first.styleIdForFloor(floor, 987654321L),
					second.styleIdForFloor(floor, 987654321L));
		}
		assertArrayEquals(first.styleOrder(), second.styleOrder());
	}

	@Test
	public void firstStyleCycleUsesEveryRegisteredTowerLevelClass() {
		TowerStyleSchedule schedule = new TowerStyleSchedule();
		HashSet<Class<?>> classes = new HashSet<>();
		for (int floor = 1; floor <= 26; floor += 5) {
			classes.add(schedule.levelClassForFloor(floor, 24680L));
		}

		assertEquals(6, classes.size());
		assertTrue(classes.contains(TowerLevel.class));
		assertTrue(classes.contains(GothicCastleLevel.class));
		assertTrue(classes.contains(AstralLibraryLevel.class));
		assertTrue(classes.contains(TowerCoreGearworksLevel.class));
		assertTrue(classes.contains(SkyAlchemyGreenhouseLevel.class));
		assertTrue(classes.contains(FrostArchiveLevel.class));
	}

	@Test
	public void severalSeedsDoNotAllProduceTheSameOrder() {
		TowerStyleSchedule first = new TowerStyleSchedule(INITIAL_STYLES);
		TowerStyleSchedule second = new TowerStyleSchedule(INITIAL_STYLES);
		first.styleIdForFloor(1, 1L);
		second.styleIdForFloor(1, 2L);

		assertNotEquals(String.join(",", first.styleOrder()),
				String.join(",", second.styleOrder()));
	}

	@Test
	public void savedAssignmentsSurviveRestore() {
		TowerStyleSchedule original = new TowerStyleSchedule(INITIAL_STYLES);
		String firstBand = original.styleIdForFloor(1, 42L);
		String thirdBand = original.styleIdForFloor(11, 42L);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		TowerStyleSchedule restored = new TowerStyleSchedule(INITIAL_STYLES);
		restored.restoreFromBundle(bundle, 999L);

		assertEquals(firstBand, restored.styleIdForFloor(5, 999L));
		assertEquals(thirdBand, restored.styleIdForFloor(15, 999L));
		assertArrayEquals(original.styleOrder(), restored.styleOrder());
	}

	@Test
	public void appendingAStyleNeverChangesAssignedBandsOrExistingOrderPrefix() {
		TowerStyleSchedule original = new TowerStyleSchedule(INITIAL_STYLES);
		String firstBand = original.styleIdForFloor(1, 314159L);
		String fourthBand = original.styleIdForFloor(16, 314159L);
		String[] oldOrder = original.styleOrder();
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		TowerStyleSchedule expanded = new TowerStyleSchedule(
				"chinese_hall", "gothic_castle", "astral_library",
				"tower_core_gearworks", "future_style");
		expanded.restoreFromBundle(bundle, 314159L);

		assertEquals(firstBand, expanded.styleIdForFloor(1, 314159L));
		assertEquals(fourthBand, expanded.styleIdForFloor(20, 314159L));
		String[] expandedOrder = expanded.styleOrder();
		for (int i = 0; i < oldOrder.length; i++) {
			assertEquals(oldOrder[i], expandedOrder[i]);
		}
		assertEquals("future_style", expandedOrder[expandedOrder.length - 1]);
	}

	@Test
	public void appendingAStyleStillSwitchesAtTheNextFiveFloorBoundary() {
		TowerStyleSchedule original = new TowerStyleSchedule(INITIAL_STYLES);
		String lastAssigned = original.styleIdForFloor(21, 271828L);
		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);

		TowerStyleSchedule expanded = new TowerStyleSchedule(
				"chinese_hall", "gothic_castle", "astral_library",
				"tower_core_gearworks", "future_style");
		expanded.restoreFromBundle(bundle, 271828L);

		assertNotEquals(lastAssigned, expanded.styleIdForFloor(26, 271828L));
	}

	@Test
	public void legacyGeneratedBandCanBePinnedToChineseHall() {
		TowerStyleSchedule schedule = new TowerStyleSchedule(INITIAL_STYLES);
		schedule.preserveLegacyGeneratedFloor(8, 77L);

		for (int floor = 6; floor <= 10; floor++) {
			assertEquals("chinese_hall", schedule.styleIdForFloor(floor, 77L));
		}
	}
}
