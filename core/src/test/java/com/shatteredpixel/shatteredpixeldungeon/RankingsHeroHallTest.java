package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import com.watabou.utils.Bundle;

import java.util.ArrayList;

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
		assertEquals(normal.score, hall.get(0).score);
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

	private Rankings.Record record(int score, boolean newCycle, String gameID) {
		Rankings.Record record = new Rankings.Record();
		record.score = score;
		record.newCycle = newCycle;
		record.customSeed = "";
		record.gameID = gameID;
		record.randomTalents = new String[0];
		record.selectedTalents = new String[0];
		return record;
	}
}
