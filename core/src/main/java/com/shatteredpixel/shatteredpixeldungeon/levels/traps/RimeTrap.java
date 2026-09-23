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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blizzard;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConfusionGas;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class RimeTrap extends Trap {

	private static final int AREA_SIZE = 5;
	private static final int AREA_CELLS = AREA_SIZE * AREA_SIZE;
	private static final int CENTER_INDEX = AREA_CELLS / 2;
	private static final int VOLUME_PER_CELL = 40;
	private static final int BLIZZARD_CENTER_BONUS = 80;

	private static final String PENDING = "pending_rime";

	private boolean pending;

	{
		// The existing grey wave slot matches the frosty diagonal visual without
		// introducing another trap sprite sheet entry.
		color = GREY;
		shape = WAVES;
	}

	@Override
	public void activate() {
		if (!pending) {
			pending = true;
			PendingRime delayed = new PendingRime(pos);
			Actor.addDelayed(delayed, Actor.TICK);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		pending = bundle.getBoolean(PENDING);
		if (pending) {
			Actor.addDelayed(new PendingRime(pos), Actor.TICK);
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PENDING, pending);
	}

	private void release() {
		if (Dungeon.level == null || !Dungeon.level.insideMap(pos)) return;

		if (Dungeon.level.heroFOV != null && Dungeon.level.heroFOV[pos]) {
			Sample.INSTANCE.play(Assets.Sounds.GAS);
		}

		boolean[] openCells = new boolean[AREA_CELLS];
		int width = Dungeon.level.width();
		PathFinder.buildDistanceMap(pos, BArray.not(Dungeon.level.solid, null), 2);
		for (int y = -2; y <= 2; y++) {
			for (int x = -2; x <= 2; x++) {
				int index = (y + 2) * AREA_SIZE + x + 2;
				int cell = pos + x + y * width;
				openCells[index] = Dungeon.level.insideMap(cell)
						&& PathFinder.distance[cell] <= 2;
			}
		}

		int[] confusionVolumes = gasVolumesForArea(openCells, 0);
		int[] blizzardVolumes = gasVolumesForArea(openCells, BLIZZARD_CENTER_BONUS);
		for (int i = 0; i < AREA_CELLS; i++) {
			int x = i % AREA_SIZE - 2;
			int y = i / AREA_SIZE - 2;
			int cell = pos + x + y * width;
			if (openCells[i]) {
				GameScene.add(Blob.seed(cell, confusionVolumes[i], ConfusionGas.class));
				GameScene.add(Blob.seed(cell, blizzardVolumes[i], Blizzard.class));
			}
		}
	}

	/** Returns one volume for each cell in row-major order, with index 12 as center. */
	static int[] gasVolumesForArea(boolean[] openCells, int centerBonus) {
		if (openCells == null || openCells.length != AREA_CELLS) {
			throw new IllegalArgumentException("Rime trap requires a five by five area");
		}

		int[] volumes = new int[AREA_CELLS];
		int blockedCells = 0;
		for (int i = 0; i < AREA_CELLS; i++) {
			if (openCells[i]) {
				volumes[i] = VOLUME_PER_CELL;
			} else {
				blockedCells++;
			}
		}
		volumes[CENTER_INDEX] += blockedCells * VOLUME_PER_CELL + centerBonus;
		return volumes;
	}

	static class PendingRime extends Actor {

		private static final String CELL = "cell";
		private int cell;

		{
			actPriority = VFX_PRIO;
		}

		PendingRime() {
		}

		PendingRime(int cell) {
			this.cell = cell;
		}

		@Override
		protected boolean act() {
			spend(TICK);
			if (Dungeon.level != null && Dungeon.level.traps != null) {
				Trap trap = Dungeon.level.traps.get(cell);
				if (trap instanceof RimeTrap) {
					RimeTrap rimeTrap = (RimeTrap) trap;
					rimeTrap.pending = false;
					rimeTrap.release();
				}
			}
			Actor.remove(this);
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(CELL, cell);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			cell = bundle.getInt(CELL);
		}
	}
}
