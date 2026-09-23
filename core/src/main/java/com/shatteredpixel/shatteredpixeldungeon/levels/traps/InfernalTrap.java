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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Inferno;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

public class InfernalTrap extends Trap {

	private static final int VOLUME_PER_CELL = 120;
	private static final int NEIGHBOUR_COUNT = 8;

	{
		color = ORANGE;
		shape = GRILL;
	}

	@Override
	public void activate() {
		if (Dungeon.level.heroFOV[pos]) {
			Sample.INSTANCE.play(Assets.Sounds.GAS);
		}

		boolean[] openNeighbours = new boolean[NEIGHBOUR_COUNT];
		PathFinder.buildDistanceMap(pos, BArray.not(Dungeon.level.solid, null), 1);
		for (int i = 0; i < NEIGHBOUR_COUNT; i++) {
			int cell = pos + PathFinder.NEIGHBOURS8[i];
			openNeighbours[i] = Dungeon.level.insideMap(cell)
					&& PathFinder.distance[cell] <= 1;
		}

		int[] volumes = infernoVolumesForNeighbours(openNeighbours);
		for (int i = 0; i < NEIGHBOUR_COUNT; i++) {
			if (volumes[i] > 0) {
				GameScene.add(Blob.seed(pos + PathFinder.NEIGHBOURS8[i], volumes[i], Inferno.class));
			}
		}
		GameScene.add(Blob.seed(pos, volumes[NEIGHBOUR_COUNT], Inferno.class));
	}

	/** Returns eight neighbour volumes followed by the center volume. */
	static int[] infernoVolumesForNeighbours(boolean[] openNeighbours) {
		if (openNeighbours == null || openNeighbours.length != NEIGHBOUR_COUNT) {
			throw new IllegalArgumentException("Infernal trap requires eight neighbour cells");
		}

		int[] volumes = new int[NEIGHBOUR_COUNT + 1];
		volumes[NEIGHBOUR_COUNT] = VOLUME_PER_CELL;
		for (int i = 0; i < NEIGHBOUR_COUNT; i++) {
			if (openNeighbours[i]) {
				volumes[i] = VOLUME_PER_CELL;
			} else {
				volumes[NEIGHBOUR_COUNT] += VOLUME_PER_CELL;
			}
		}
		return volumes;
	}
}
