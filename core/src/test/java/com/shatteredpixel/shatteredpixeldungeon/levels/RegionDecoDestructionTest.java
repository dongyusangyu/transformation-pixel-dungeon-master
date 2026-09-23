package com.shatteredpixel.shatteredpixeldungeon.levels;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegionDecoDestructionTest {

	@Test
	public void boundaryRegionDecoCannotBeDestroyed() {
		SurfaceTownLevel level = testLevel();
		level.create();

		int boundary = cell(level, 25, 33);
		int original = level.map[boundary];
		assertTrue(original == Terrain.REGION_DECO || original == Terrain.REGION_DECO_ALT);

		assertFalse(level.destroyRegionDeco(boundary));
		assertEquals(original, level.map[boundary]);
	}

	@Test
	public void interiorRegionDecoCanBeDestroyed() {
		SurfaceTownLevel level = testLevel();
		level.create();

		int interior = cell(level, 2, 2);
		assertTrue(level.map[interior] == Terrain.REGION_DECO
				|| level.map[interior] == Terrain.REGION_DECO_ALT);

		assertTrue(level.destroyRegionDeco(interior));
		assertEquals(Terrain.EMPTY_DECO, level.map[interior]);
	}

	private static SurfaceTownLevel testLevel() {
		return new SurfaceTownLevel() {
			@Override
			protected void createItems() {
				// Terrain tests do not need item sprites or a running scene.
			}
		};
	}

	private static int cell(SurfaceTownLevel level, int x, int y) {
		return x + y * level.width();
	}
}
