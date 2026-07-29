package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidRun;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class ActionIndicatorTest {

	@Test
	public void extractionRaidSessionIsAvailableToActionSelection() {
		assertTrue(Arrays.asList(ActionIndicator.actionBuffClasses)
				.contains(ExtractionRaidRun.RaidSession.class));
	}
}
