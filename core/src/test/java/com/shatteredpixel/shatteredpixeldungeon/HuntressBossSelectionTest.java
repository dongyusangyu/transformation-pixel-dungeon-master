package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HuntressBossSelectionTest {

	@Test
	public void bossSelectionIsStableForTheSameRunSeed() {
		for (long seed = 0; seed < 32; seed++) {
			assertEquals(Dungeon.huntressBossForSeed(seed),
					Dungeon.huntressBossForSeed(seed));
		}
	}

	@Test
	public void bossSelectionContainsBothAlternatives() {
		int huntressRuns = 0;
		for (long seed = 0; seed < 64; seed++) {
			if (Dungeon.huntressBossForSeed(seed)) {
				huntressRuns++;
			}
		}

		assertTrue(huntressRuns > 0);
		assertTrue(huntressRuns < 64);
	}

	@Test
	public void falconEyeIsRegisteredAsTheHuntressBossTalent() {
		assertTrue(Talent.isBossTalent(Talent.FALCON_EYE));
		assertSame(Talent.BOSS_TALENT_SLOT_3, Talent.bossTalentSlot(Talent.FALCON_EYE));
	}
}
