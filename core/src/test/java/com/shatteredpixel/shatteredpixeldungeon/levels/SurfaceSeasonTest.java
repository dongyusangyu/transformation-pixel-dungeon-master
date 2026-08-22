package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SurfaceSeasonTest {

	@After
	public void restoreLushSeason() {
		SurfaceSeason.initializeForMonth(3);
	}

	@Test
	public void selectsWinterOnlyFromNovemberThroughFebruary() {
		for (int month : new int[]{1, 2, 11, 12}) {
			SurfaceSeason.initializeForMonth(month);
			assertEquals(SurfaceSeason.Theme.WINTER, SurfaceSeason.current());
		}
		for (int month : new int[]{3, 4, 5, 6, 7, 8, 9, 10}) {
			SurfaceSeason.initializeForMonth(month);
			assertEquals(SurfaceSeason.Theme.LUSH, SurfaceSeason.current());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMonthZero() {
		SurfaceSeason.initializeForMonth(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMonthThirteen() {
		SurfaceSeason.initializeForMonth(13);
	}

	@Test
	public void returnsACompleteMatchingAssetSetForEachSeason() {
		SurfaceSeason.initializeForMonth(3);
		assertEquals(Assets.Environment.TILES_SURFACE_LUSH, SurfaceSeason.tilesTexture());
		assertEquals(Assets.Environment.WATER_SURFACE_LUSH, SurfaceSeason.waterTexture());
		assertEquals("surface-town-clean-grass-lush-v1", SurfaceSeason.grassTextureCacheKey());

		SurfaceSeason.initializeForMonth(11);
		assertEquals(Assets.Environment.TILES_SURFACE_WINTER, SurfaceSeason.tilesTexture());
		assertEquals(Assets.Environment.WATER_SURFACE_WINTER, SurfaceSeason.waterTexture());
		assertEquals("surface-town-clean-grass-winter-v1", SurfaceSeason.grassTextureCacheKey());
	}

	@Test
	public void keepsInitializedSeasonUntilTheNextExplicitInitialization() {
		SurfaceSeason.initializeForMonth(10);
		assertEquals(SurfaceSeason.Theme.LUSH, SurfaceSeason.current());
		assertEquals(Assets.Environment.TILES_SURFACE_LUSH, SurfaceSeason.tilesTexture());

		assertEquals(SurfaceSeason.Theme.LUSH, SurfaceSeason.current());
		assertEquals(Assets.Environment.TILES_SURFACE_LUSH, SurfaceSeason.tilesTexture());

		SurfaceSeason.initializeForMonth(11);
		assertEquals(SurfaceSeason.Theme.WINTER, SurfaceSeason.current());
		assertEquals(Assets.Environment.TILES_SURFACE_WINTER, SurfaceSeason.tilesTexture());
	}
}
