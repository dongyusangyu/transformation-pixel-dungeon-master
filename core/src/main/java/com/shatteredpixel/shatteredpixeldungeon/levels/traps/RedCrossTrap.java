/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.watabou.noosa.audio.Sample;

public class RedCrossTrap extends Trap {

	private static final int MAX_HEALING = 200;

	{
		color = RED;
		shape = CROSSHAIR;
	}

	@Override
	public void activate() {
		if (Dungeon.level == null || Dungeon.level.mobs == null) return;

		int healing = healingAmountForTowerFloor(Dungeon.depth);
		boolean healedSomeone = false;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (mob == null || !mob.isAlive() || mob.alignment != Char.Alignment.ENEMY) continue;

			boolean visible = Dungeon.level.heroFOV != null
					&& mob.pos >= 0
					&& mob.pos < Dungeon.level.heroFOV.length
					&& Dungeon.level.heroFOV[mob.pos];
			if (mob.heal(healing, visible) > 0) {
				healedSomeone = true;
				if (visible && mob.sprite != null) {
					mob.sprite.emitter().burst(Speck.factory(Speck.HEALING), 2);
				}
			}
		}

		if (healedSomeone && Dungeon.level.heroFOV != null
				&& pos >= 0 && pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos]) {
			Sample.INSTANCE.play(Assets.Sounds.DEWDROP);
		}
	}

	static int healingAmountForTowerFloor(int towerFloor) {
		return Math.min(Math.max(1, towerFloor) * 2, MAX_HEALING);
	}
}
