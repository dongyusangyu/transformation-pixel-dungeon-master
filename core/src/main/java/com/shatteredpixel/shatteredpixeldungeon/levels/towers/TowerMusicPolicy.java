/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

final class TowerMusicPolicy {

	enum State {
		CALM(Assets.Music.TOWER_3, 0.65f),
		ALERT(Assets.Music.TOWER_2, 0.70f),
		COMBAT(Assets.Music.TOWER, 1f),
		CRITICAL(Assets.Music.TOWER_BUSY, 0.48f);

		final String track;
		final float gain;

		State(String track, float gain) {
			this.track = track;
			this.gain = gain;
		}
	}

	static final float CHECK_INTERVAL = 0.25f;
	static final float MINIMUM_HOLD = 6f;

	private TowerMusicPolicy() {
	}

	static State rawState(int hp, int maxHp, int visibleEnemies) {
		if ((maxHp > 0 && (long) hp * 3 <= maxHp) || visibleEnemies >= 8) {
			return State.CRITICAL;
		} else if (visibleEnemies >= 5) {
			return State.COMBAT;
		} else if (visibleEnemies >= 1) {
			return State.ALERT;
		} else {
			return State.CALM;
		}
	}

	static State stateWithHysteresis(State current, int hp, int maxHp, int visibleEnemies) {
		if (current == State.CRITICAL
				&& ((maxHp > 0 && (long) hp * 5 <= (long) maxHp * 2)
				|| visibleEnemies >= 7)) {
			return State.CRITICAL;
		}

		State next = rawState(hp, maxHp, visibleEnemies);
		if (current == State.COMBAT && next == State.ALERT && visibleEnemies == 4) {
			return State.COMBAT;
		}
		return next;
	}

	static float transitionDelay(State current, State next, boolean lowHealthCritical) {
		if (next == current) return 0f;
		if (next == State.CRITICAL && lowHealthCritical) return 0f;
		return next.ordinal() > current.ordinal() ? 0.5f : 3f;
	}

	static float fadeOutDuration(State next) {
		return next == State.CRITICAL ? 0.5f : 1.25f;
	}

	static float fadeInDuration(State next) {
		return next == State.CRITICAL ? 0.8f : 1.75f;
	}
}
