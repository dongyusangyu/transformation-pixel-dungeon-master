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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFlock;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

public class RoastSheepTrap extends Trap {

	{
		color = ORANGE;
		shape = CROSSHAIR;
	}

	@Override
	public void activate() {
		PathFinder.buildDistanceMap(pos, BArray.not(Dungeon.level.solid, null), 2);
		int width = Dungeon.level.width();
		int originX = pos % width;
		int originY = pos / width;
		for (int cell = 0; cell < PathFinder.distance.length; cell++) {
			if (PathFinder.distance[cell] < Integer.MAX_VALUE
					&& isInFlockArea(cell % width - originX, cell / width - originY)
					&& canSpawnSheep(Dungeon.level.insideMap(cell), Dungeon.level.solid[cell],
					Dungeon.level.pit[cell], Actor.findChar(cell) != null)) {
				Sheep sheep = new Sheep();
				sheep.initialize(StoneOfFlock.sheepLifespan(Dungeon.depth, Dungeon.branch));
				sheep.pos = cell;
				GameScene.add(sheep);
				CellEmitter.get(cell).burst(Speck.factory(Speck.WOOL), 4);
				Dungeon.level.occupyCell(sheep);
			}
		}

		Sample.INSTANCE.play(Assets.Sounds.PUFF);
		Sample.INSTANCE.play(Assets.Sounds.SHEEP);
		BlazingTrap.activateAt(pos);
	}

	/** Returns whether an offset belongs to the five by five radius-two flock area. */
	static boolean isInFlockArea(int dx, int dy) {
		return Math.abs(dx) <= 2 && Math.abs(dy) <= 2 && Math.abs(dx) + Math.abs(dy) <= 2;
	}

	/** Returns whether a cell can receive a summoned sheep. */
	static boolean canSpawnSheep(boolean insideMap, boolean solid, boolean pit, boolean occupied) {
		return insideMap && !solid && !pit && !occupied;
	}
}
