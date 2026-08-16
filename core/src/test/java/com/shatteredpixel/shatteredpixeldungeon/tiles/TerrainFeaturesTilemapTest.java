/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TerrainFeaturesTilemapTest {

	@Test
	public void towerThemesDoNotOverlayRegionalVegetation() {
		for (int terrain : new int[]{
				Terrain.GRASS,
				Terrain.HIGH_GRASS,
				Terrain.FURROWED_GRASS,
				Terrain.EMBERS
		}) {
			assertEquals(-1, TerrainFeaturesTilemap.regionalVegetationVisual(terrain, 2, 75, true));
		}
	}

	@Test
	public void ordinaryLevelsKeepRegionalVegetation() {
		assertEquals(42, TerrainFeaturesTilemap.regionalVegetationVisual(Terrain.HIGH_GRASS, 2, 75, false));
		assertEquals(44, TerrainFeaturesTilemap.regionalVegetationVisual(Terrain.FURROWED_GRASS, 2, 75, false));
		assertEquals(46, TerrainFeaturesTilemap.regionalVegetationVisual(Terrain.GRASS, 2, 75, false));
		assertEquals(90, TerrainFeaturesTilemap.regionalVegetationVisual(Terrain.EMBERS, 2, 75, false));
	}
}
