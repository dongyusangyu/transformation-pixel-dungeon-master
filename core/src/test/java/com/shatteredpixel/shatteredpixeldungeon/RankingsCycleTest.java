package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RankingsCycleTest {

	@Test
	public void cycleFlagSurvivesSerializationAndOldRecordsDefaultToNormal() {
		Rankings.Record newCycle = record(100, true);
		Bundle bundle = new Bundle();
		newCycle.storeInBundle(bundle);

		Rankings.Record restored = new Rankings.Record();
		restored.restoreFromBundle(bundle);
		assertTrue(restored.newCycle);

		Rankings.Record oldRecord = new Rankings.Record();
		oldRecord.restoreFromBundle(new Bundle());
		assertFalse(oldRecord.newCycle);
	}

	@Test
	public void filtersNormalAndNewCycleRecordsSeparately() {
		ArrayList<Rankings.Record> records = new ArrayList<>();
		records.add(record(300, false));
		records.add(record(200, true));
		records.add(record(100, false));

		assertEquals(2, Rankings.filterByCycle(records, false).size());
		assertEquals(1, Rankings.filterByCycle(records, true).size());
	}

	@Test
	public void eachCycleKeepsItsOwnRankingCapacity() {
		ArrayList<Rankings.Record> records = new ArrayList<>();
		Rankings.Record latestNormal = null;
		for (int i = 0; i < Rankings.TABLE_SIZE + 1; i++) {
			latestNormal = record(1000 - i, false);
			records.add(latestNormal);
		}
		for (int i = 0; i < Rankings.TABLE_SIZE; i++) {
			records.add(record(500 - i, true));
		}
		Collections.sort(records, Rankings.scoreComparator);

		Rankings.trimCycleRecords(records, false, latestNormal);

		assertEquals(Rankings.TABLE_SIZE, Rankings.filterByCycle(records, false).size());
		assertEquals(Rankings.TABLE_SIZE, Rankings.filterByCycle(records, true).size());
		assertTrue(records.contains(latestNormal));
	}

	@Test
	public void normalizationTrimsBothCyclesAndKeepsLatestRecordIdentity() {
		ArrayList<Rankings.Record> records = new ArrayList<>();
		Rankings.Record latestNewCycle = record(1, true);
		for (int i = 0; i < Rankings.TABLE_SIZE + 1; i++) {
			records.add(record(100 + i, false));
			records.add(record(200 + i, true));
		}
		records.add(latestNewCycle);

		int latestIndex = Rankings.normalizeRecords(records, latestNewCycle);

		assertEquals(Rankings.TABLE_SIZE, Rankings.filterByCycle(records, false).size());
		assertEquals(Rankings.TABLE_SIZE, Rankings.filterByCycle(records, true).size());
		assertEquals(latestNewCycle, records.get(latestIndex));
	}

	private Rankings.Record record(int score, boolean newCycle) {
		Rankings.Record record = new Rankings.Record();
		record.score = score;
		record.newCycle = newCycle;
		record.customSeed = "";
		record.gameID = "record-" + score + "-" + newCycle;
		record.randomTalents = new String[0];
		record.selectedTalents = new String[0];
		return record;
	}
}
