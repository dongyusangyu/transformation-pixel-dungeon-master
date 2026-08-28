package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.SparseArray;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FalconEyeTalentTest {

	@Test
	public void piercingRangeUsesCurrentViewDistance() {
		assertEquals(6, Talent.falconEyePiercingDistance(1, 8, true));
		assertEquals(8, Talent.falconEyePiercingDistance(2, 8, true));
	}

	@Test
	public void piercingRangeDoesNotBypassBlindnessOrOtherUnsightedStates() {
		assertEquals(0, Talent.falconEyePiercingDistance(1, 8, false));
		assertEquals(0, Talent.falconEyePiercingDistance(2, 8, false));
	}

	@Test
	public void piercingRangeFollowsAlreadyAdjustedViewDistance() {
		assertEquals(5, Talent.falconEyePiercingDistance(1, 7, true));
		assertEquals(9, Talent.falconEyePiercingDistance(1, 12, true));
	}

	@Test
	public void fieldOfViewDistanceNeverExceedsShadowCasterRoundingTable() {
		assertEquals(0, ShadowCaster.limitDistance(-1));
		assertEquals(12, ShadowCaster.limitDistance(12));
		assertEquals(ShadowCaster.MAX_DISTANCE,
				ShadowCaster.limitDistance(ShadowCaster.MAX_DISTANCE + 1));
		assertEquals(ShadowCaster.MAX_DISTANCE,
				ShadowCaster.limitDistance(Integer.MAX_VALUE));
	}

	@Test
	public void stackedFarsightAndFalconEyeFitSmallLevelFieldOfView() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		try {
			TestLevel level = new TestLevel();
			level.blobs = new HashMap<>();
			level.mobs = new HashSet<>();
			level.heaps = new SparseArray<>();
			level.setSize(21, 21);
			Arrays.fill(level.map, Terrain.EMPTY);
			level.buildFlagMaps();
			level.cleanWalls();

			Hero hero = TestHeroFactory.create();
			hero.HP = hero.HT = 10;
			hero.pos = 10 + 10 * level.width();
			hero.viewDistance = 12;
			hero.subClass = HeroSubClass.SNIPER;
			hero.mindVisionEnemies = new ArrayList<>();
			LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
			tier.put(Talent.FARSIGHT, 3);
			tier.put(Talent.FALCON_EYE, 2);
			hero.talents.add(tier);

			Dungeon.level = level;
			Dungeon.hero = hero;
			boolean[] fieldOfView = new boolean[level.length()];
			level.updateFieldOfView(hero, fieldOfView);

			assertTrue(fieldOfView[hero.pos]);
		} finally {
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	private static class TestLevel extends Level {
		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}

}
