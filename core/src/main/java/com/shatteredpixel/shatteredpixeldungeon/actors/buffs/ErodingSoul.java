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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class ErodingSoul extends Buff {

	{
		type = buffType.NEGATIVE;
		announced = true;
		revivePersists = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

	public static boolean shouldRaise(Mob mob) {
		ErodingSoul erodingSoul = mob.buff(ErodingSoul.class);
		if (erodingSoul == null || mob instanceof Wraith) {
			return false;
		}

		if (mob.buff(Corruption.class) != null || mob.buff(Doom.class) != null) {
			return true;
		}

		int debuffs = 0;
		for (Buff buff : mob.buffs()) {
			if (buff != erodingSoul && buff.type == buffType.NEGATIVE) {
				debuffs++;
			}
		}

		return Random.Float() < 0.2f + 0.05f * debuffs;
	}

	public static void raiseWraith(Mob mob) {
		Wraith wraith = Wraith.spawnAt(mob.pos, Wraith.class);
		if (wraith != null) {
			Buff.affect(wraith, Corruption.class);
			if (Dungeon.level.heroFOV[mob.pos]) {
				CellEmitter.get(mob.pos).burst(ShadowParticle.CURSE, 6);
				Sample.INSTANCE.play(Assets.Sounds.CURSED);
			}
		}
	}
}
