package com.shatteredpixel.shatteredpixeldungeon.items.bombs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BombTalentEffectsTest {

	@Test
	public void bombManiacScalesDamageForTheSharedBombTalentPath() {
		Hero hero = heroWithTalent(Talent.BOMB_MANIAC, 2);

		assertEquals(150, Bomb.damageWithBombTalents(hero, 100));
	}

	@Test
	public void shockBombAppliesParalysisToASeparateTarget() {
		Hero hero = heroWithTalent(Talent.SHOCK_BOMB, 2);
		Hero target = TestHeroFactory.create();

		Bomb.applyShockBomb(hero, target);

		assertNotNull(target.buff(Paralysis.class));
	}

	@Test
	public void regrowthBombIsTheOnlyExcludedBombType() {
		assertTrue(Bomb.supportsBombTalents(TestHeroFactory.allocateItem(Bomb.class)));
		assertTrue(Bomb.supportsBombTalents(TestHeroFactory.allocateItem(ArcaneBomb.class)));
		assertTrue(Bomb.supportsBombTalents(TestHeroFactory.allocateItem(ShrapnelBomb.class)));
		assertTrue(Bomb.supportsBombTalents(TestHeroFactory.allocateItem(HolyBomb.class)));
		assertFalse(Bomb.supportsBombTalents(TestHeroFactory.allocateItem(RegrowthBomb.class)));
	}

	private static Hero heroWithTalent(Talent talent, int points) {
		Hero hero = TestHeroFactory.create();
		hero.talents.add(new LinkedHashMap<Talent, Integer>());
		hero.talents.get(0).put(talent, points);
		return hero;
	}
}
