package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.watabou.utils.Bundle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GentlemanElfPreludeTest {

	@Test
	public void enteringArenaRequestsChoiceBeforeBossSpawn() {
		GentlemanElfPrelude prelude = new GentlemanElfPrelude();

		assertEquals(GentlemanElfPrelude.Action.NONE, prelude.nextAction(false));
		assertTrue(prelude.begin(321));
		assertEquals(321, prelude.spawnCell());
		assertEquals(GentlemanElfPrelude.Action.SHOW_WINDOW, prelude.nextAction(false));
		assertFalse(prelude.resolved());
	}

	@Test
	public void resolvingChoiceStartsEncounterOnlyOnce() {
		GentlemanElfPrelude prelude = new GentlemanElfPrelude();
		prelude.begin(321);

		assertTrue(prelude.resolve(true));
		assertTrue(prelude.resolved());
		assertTrue(prelude.drink());
		assertEquals(GentlemanElfPrelude.Action.START_ENCOUNTER, prelude.nextAction(false));
		assertEquals(GentlemanElfPrelude.Action.NONE, prelude.nextAction(true));
		assertFalse(prelude.resolve(false));
		assertTrue(prelude.drink());
	}

	@Test
	public void pendingChoiceSurvivesSaveAndRequestsWindowAgain() {
		GentlemanElfPrelude prelude = new GentlemanElfPrelude();
		prelude.begin(654);
		Bundle bundle = new Bundle();
		prelude.storeInBundle(bundle);

		GentlemanElfPrelude restored = new GentlemanElfPrelude();
		restored.restoreFromBundle(bundle);

		assertEquals(654, restored.spawnCell());
		assertEquals(GentlemanElfPrelude.Action.SHOW_WINDOW, restored.nextAction(false));
		assertEquals(GentlemanElfPrelude.Action.SHOW_WINDOW, restored.nextAction(true));
	}

	@Test
	public void resolvedChoiceSurvivesSaveAndFinishesMissingSpawn() {
		GentlemanElfPrelude prelude = new GentlemanElfPrelude();
		prelude.begin(987);
		prelude.resolve(false);
		Bundle bundle = new Bundle();
		prelude.storeInBundle(bundle);

		GentlemanElfPrelude restored = new GentlemanElfPrelude();
		restored.restoreFromBundle(bundle);

		assertTrue(restored.resolved());
		assertFalse(restored.drink());
		assertEquals(GentlemanElfPrelude.Action.START_ENCOUNTER, restored.nextAction(false));
	}

	@Test
	public void resetReturnsPreludeToUnstartedState() {
		GentlemanElfPrelude prelude = new GentlemanElfPrelude();
		prelude.begin(321);
		prelude.resolve(true);

		prelude.reset();

		assertEquals(-1, prelude.spawnCell());
		assertEquals(GentlemanElfPrelude.Action.NONE, prelude.nextAction(false));
	}
}
