package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

/** Persistent pre-spawn state for the gentleman elf's opening choice. */
final class GentlemanElfPrelude implements Bundlable {

	enum Action {
		NONE,
		SHOW_WINDOW,
		START_ENCOUNTER
	}

	private static final String STARTED = "gentleman_prelude_started";
	private static final String RESOLVED = "gentleman_prelude_resolved";
	private static final String DRINK = "gentleman_prelude_drink";
	private static final String SPAWN_CELL = "gentleman_prelude_spawn_cell";

	private boolean started;
	private boolean resolved;
	private boolean drink;
	private int spawnCell = -1;

	boolean begin(int spawnCell) {
		if (started) return false;
		started = true;
		this.spawnCell = spawnCell;
		return true;
	}

	boolean resolve(boolean drink) {
		if (!started || resolved) return false;
		this.drink = drink;
		resolved = true;
		return true;
	}

	Action nextAction(boolean bossStarted) {
		if (!started) return Action.NONE;
		if (!resolved) return Action.SHOW_WINDOW;
		return bossStarted ? Action.NONE : Action.START_ENCOUNTER;
	}

	boolean started() {
		return started;
	}

	boolean resolved() {
		return resolved;
	}

	boolean drink() {
		return drink;
	}

	int spawnCell() {
		return spawnCell;
	}

	void reset() {
		started = false;
		resolved = false;
		drink = false;
		spawnCell = -1;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(STARTED, started);
		bundle.put(RESOLVED, resolved);
		bundle.put(DRINK, drink);
		bundle.put(SPAWN_CELL, spawnCell);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		started = bundle.getBoolean(STARTED);
		resolved = started && bundle.getBoolean(RESOLVED);
		drink = resolved && bundle.getBoolean(DRINK);
		spawnCell = started && bundle.contains(SPAWN_CELL) ? bundle.getInt(SPAWN_CELL) : -1;
	}
}
