package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;

public class AgentMinExplorationTrackerTest {

	private final int originalLoopMemoryWindow = AgentMinRewardConfig.LOOP_MEMORY_WINDOW;

	@After
	public void tearDown() {
		AgentMinRewardConfig.LOOP_MEMORY_WINDOW = originalLoopMemoryWindow;
		AgentMinExplorationTracker.reset();
	}

	@Test
	public void nonPositiveHistoryWindowDoesNotRemoveFromEmptyDeque() throws Exception {
		AgentMinRewardConfig.LOOP_MEMORY_WINDOW = -1;
		Method recordRecentCell = AgentMinExplorationTracker.class.getDeclaredMethod(
				"recordRecentCell", int.class, int.class);
		recordRecentCell.setAccessible(true);

		recordRecentCell.invoke(null, 1, 2);
	}
}
