package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import org.junit.Test;

import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class RankingsHeroHallTest {

	@Test
	public void copiesNormalRecordAndRejectsDuplicateAndNewCycleRecord() {
		ArrayList<Rankings.Record> hall = new ArrayList<>();
		Rankings.Record normal = record(100, false, "normal");

		assertTrue(Rankings.copyToHeroHall(hall, normal));
		assertEquals(1, hall.size());
		assertNotSame(normal, hall.get(0));
		assertEquals(normal.gameID, hall.get(0).gameID);
		assertEquals(normal.score, hall.get(0).score, 0d);
		assertFalse(Rankings.copyToHeroHall(hall, normal));
		assertFalse(Rankings.copyToHeroHall(hall, record(200, true, "cycle")));
	}

	@Test
	public void hallHasNoRankingTableCapacityLimit() {
		ArrayList<Rankings.Record> hall = new ArrayList<>();
		for (int i = 0; i < Rankings.TABLE_SIZE * 3; i++) {
			assertTrue(Rankings.copyToHeroHall(hall, record(i, false, "record-" + i)));
		}

		assertEquals(Rankings.TABLE_SIZE * 3, hall.size());
	}

	@Test
	public void removesHallRecordByGameId() {
		ArrayList<Rankings.Record> hall = new ArrayList<>();
		Rankings.copyToHeroHall(hall, record(100, false, "first"));
		Rankings.copyToHeroHall(hall, record(200, false, "second"));

		assertTrue(Rankings.removeFromHeroHall(hall, "first"));
		assertFalse(Rankings.containsHeroHallRecord(hall, "first"));
		assertTrue(Rankings.containsHeroHallRecord(hall, "second"));
		assertFalse(Rankings.removeFromHeroHall(hall, "missing"));
	}

	@Test
	public void restartStateIsMirroredAcrossNormalAndHeroHallCopies() {
		ArrayList<Rankings.Record> records = new ArrayList<>();
		ArrayList<Rankings.Record> hall = new ArrayList<>();
		Rankings.Record normal = record(100, false, "shared");
		Rankings.Record hallCopy = record(100, false, "shared");
		records.add(normal);
		hall.add(hallCopy);

		Rankings.setRestarted(records, hall, normal, true);

		assertTrue(normal.restarted);
		assertTrue(hallCopy.restarted);

		Rankings.setRestarted(records, hall, hallCopy, false);

		assertFalse(normal.restarted);
		assertFalse(hallCopy.restarted);
	}

	@Test
	public void removingAndReaddingHallOnlyCopyDoesNotResetRestartReservation() throws Exception {
		ArrayList<Rankings.Record> previousRecords = Rankings.INSTANCE.records;
		ArrayList<Rankings.Record> previousHall = getHeroHallRecords();
		ArrayList<Rankings.Record> hall = new ArrayList<>();
		Rankings.Record original = record(100, false, "shared-hall-only");
		original.win = true;
		original.gameData = new Bundle();
		assertTrue(Rankings.copyToHeroHall(hall, original));
		Rankings.Record firstCopy = hall.get(0);
		try {
			Rankings.INSTANCE.records = new ArrayList<>();
			setHeroHallRecords(hall);
			Rankings.INSTANCE.setRestarted(firstCopy, true);

			assertTrue(Rankings.removeFromHeroHall(hall, original.gameID));
			assertTrue(Rankings.copyToHeroHall(hall, original));

			assertFalse(RankingRestart.isEligible(hall.get(0)));
		} finally {
			Rankings.INSTANCE.setRestarted(original, false);
			Rankings.INSTANCE.records = previousRecords;
			setHeroHallRecords(previousHall);
		}
	}

	private static void setHeroHallRecords(ArrayList<Rankings.Record> records) throws Exception {
		java.lang.reflect.Field field = Rankings.class.getDeclaredField("heroHallRecords");
		field.setAccessible(true);
		field.set(Rankings.INSTANCE, records);
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<Rankings.Record> getHeroHallRecords() throws Exception {
		java.lang.reflect.Field field = Rankings.class.getDeclaredField("heroHallRecords");
		field.setAccessible(true);
		return (ArrayList<Rankings.Record>) field.get(Rankings.INSTANCE);
	}

	@Test
	public void preservesLocalHallWhenLegacyCloudResponseOmitsIt() {
		ArrayList<Rankings.Record> hall = new ArrayList<>();
		hall.add(record(100, false, "local"));
		Bundle localRankings = new Bundle();
		localRankings.put("hero_hall_records", hall);
		Bundle cloudRankings = new Bundle();

		Rankings.preserveLocalHeroHall(localRankings, cloudRankings);

		assertEquals(1, cloudRankings.getCollection("hero_hall_records").size());
	}

	@Test
	public void explicitEmptyCloudHallIsAllowedToClearLocalHall() {
		ArrayList<Rankings.Record> hall = new ArrayList<>();
		hall.add(record(100, false, "local"));
		Bundle localRankings = new Bundle();
		localRankings.put("hero_hall_records", hall);
		Bundle cloudRankings = new Bundle();
		cloudRankings.put("hero_hall_records", new ArrayList<Rankings.Record>());

		Rankings.preserveLocalHeroHall(localRankings, cloudRankings);

		assertEquals(0, cloudRankings.getCollection("hero_hall_records").size());
	}

	@Test
	public void restartReservationsLoadStoredIdsAndMigrateLegacyFlags() {
		Bundle rankings = new Bundle();
		rankings.put(Rankings.RESTART_SOURCE_GAME_IDS, new String[]{"stored"});
		Rankings.Record legacy = record(100, false, "legacy");
		legacy.restarted = true;
		ArrayList<Rankings.Record> records = new ArrayList<>();
		records.add(legacy);
		rankings.put("records", records);

		LinkedHashSet<String> restored = Rankings.restartReservationsFromBundle(rankings);

		assertEquals(2, restored.size());
		assertTrue(restored.contains("stored"));
		assertTrue(restored.contains("legacy"));
	}

	@Test
	public void cloudRestoreUnionsLocalAndCloudRestartReservations() {
		Bundle localRankings = new Bundle();
		localRankings.put(Rankings.RESTART_SOURCE_GAME_IDS, new String[]{"local"});
		Bundle cloudRankings = new Bundle();
		cloudRankings.put(Rankings.RESTART_SOURCE_GAME_IDS, new String[]{"cloud"});

		Rankings.preserveLocalHeroHall(localRankings, cloudRankings);

		LinkedHashSet<String> merged = Rankings.restartReservationsFromBundle(cloudRankings);
		assertEquals(2, merged.size());
		assertTrue(merged.contains("local"));
		assertTrue(merged.contains("cloud"));
	}

	@Test
	public void restartReservationDoesNotRequireEitherRankingCopyToRemain() {
		ArrayList<Rankings.Record> previousRecords = Rankings.INSTANCE.records;
		try {
			Rankings.INSTANCE.records = new ArrayList<>();
			Rankings.INSTANCE.setRestartReserved("trimmed-source", true);

			assertTrue(Rankings.INSTANCE.isRestartReserved("trimmed-source"));

			Rankings.INSTANCE.setRestartReserved("trimmed-source", false);
			assertFalse(Rankings.INSTANCE.isRestartReserved("trimmed-source"));
		} finally {
			Rankings.INSTANCE.setRestartReserved("trimmed-source", false);
			Rankings.INSTANCE.records = previousRecords;
		}
	}

	private Rankings.Record record(int score, boolean newCycle, String gameID) {
		Rankings.Record record = new Rankings.Record();
		record.score = score;
		record.heroClass = HeroClass.WARRIOR;
		record.newCycle = newCycle;
		record.customSeed = "";
		record.gameID = gameID;
		record.randomTalents = new String[0];
		record.selectedTalents = new String[0];
		return record;
	}
}
