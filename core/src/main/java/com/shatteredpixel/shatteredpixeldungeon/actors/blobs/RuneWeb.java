/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WebParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/** A fragile, non-blocking web left behind by a rune spinner. */
public class RuneWeb extends Blob {

	public static final int STRENGTH = 3;
	public static final float SLOW_DURATION = 2f;

	{
		actPriority = HERO_PRIO + 1;
	}

	@Override
	protected void evolve() {
		for (int i = area.left; i < area.right; i++) {
			for (int j = area.top; j < area.bottom; j++) {
				int cell = i + j * Dungeon.level.width();
				off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;
				volume += off[cell];
			}
		}
	}

	public static void affectChar(Char ch) {
		Buff.prolong(ch, Slow.class, SLOW_DURATION);
	}

	@Override
	public void use(BlobEmitter emitter) {
		super.use(emitter);
		emitter.pour(WebParticle.FACTORY, 0.25f);
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
