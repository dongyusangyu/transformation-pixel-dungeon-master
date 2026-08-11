/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.SoulCollector;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SoulCollectorSprite extends MobSprite {

	private final Map<Integer, Emitter> soulMarkers = new HashMap<>();

	public SoulCollectorSprite() {
		texture(Assets.Sprites.SOUL_COLLECTOR);
		TextureFilm frames = new TextureFilm(texture, 16, 16);

		idle = new Animation(1, true);
		idle.frames(frames, 0, 0, 0, 1, 0, 0, 0, 0, 1);

		run = new Animation(8, true);
		run.frames(frames, 0, 0, 0, 2, 3, 4);

		zap = new Animation(10, false);
		zap.frames(frames, 5, 6, 7, 8);
		attack = zap.clone();

		die = new Animation(10, false);
		die.frames(frames, 9, 10, 11, 12);

		play(idle);
	}

	@Override
	public void link(Char ch) {
		super.link(ch);
		syncSoulMarkers();
	}

	@Override
	public void update() {
		super.update();
		syncSoulMarkers();
	}

	public void channel(int cell) {
		if (ch != null) {
			turnTo(ch.pos, cell);
			play(zap);
		}
	}

	public void finishSoul(int cell) {
		Emitter marker = soulMarkers.remove(cell);
		if (marker == null) {
			marker = CellEmitter.get(cell);
		}
		marker.burst(Speck.factory(Speck.RATTLE), 10);
	}

	private void syncSoulMarkers() {
		if (!(ch instanceof SoulCollector) || Dungeon.level == null) {
			clearSoulMarkers();
			return;
		}

		Set<Integer> wanted = new HashSet<>();
		for (int cell : ((SoulCollector) ch).markedSoulCells()) {
			if (cell >= 0 && cell < Dungeon.level.length()) {
				wanted.add(cell);
			}
		}

		Iterator<Map.Entry<Integer, Emitter>> iterator = soulMarkers.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Emitter> entry = iterator.next();
			if (!wanted.contains(entry.getKey()) || entry.getValue().parent == null) {
				entry.getValue().on = false;
				iterator.remove();
			}
		}

		for (int cell : wanted) {
			Emitter marker = soulMarkers.get(cell);
			if (marker == null) {
				marker = CellEmitter.get(cell);
				marker.pour(Speck.factory(Speck.RATTLE), 0.1f);
				soulMarkers.put(cell, marker);
			}
			marker.visible = Dungeon.level.heroFOV[cell];
		}
	}

	private void clearSoulMarkers() {
		for (Emitter marker : soulMarkers.values()) {
			marker.on = false;
		}
		soulMarkers.clear();
	}

	@Override
	public void die() {
		clearSoulMarkers();
		super.die();
	}

	@Override
	public void kill() {
		clearSoulMarkers();
		super.kill();
	}

	@Override
	public void onComplete(Animation animation) {
		if (animation == zap) {
			idle();
		}
		super.onComplete(animation);
	}

	@Override
	public int blood() {
		return 0xFF38CDB6;
	}
}
