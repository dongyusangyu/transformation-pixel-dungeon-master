package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HealingVialOfBloodTest {

	private Hero previousHero;

	@Before
	public void rememberDungeonHero() {
		previousHero = Dungeon.hero;
	}

	@After
	public void restoreDungeonHero() {
		Dungeon.hero = previousHero;
	}

	@Test
	public void repeatedHealingPotionsDoNotMultiplyExistingVialHealingAgain() {
		Hero hero = TestHeroFactory.create();
		hero.HT = 100;
		hero.HP = 1;
		VialOfBlood vial = TestHeroFactory.allocateItem(VialOfBlood.class);
		vial.level(3);
		hero.belongings.backpack.items.add(vial);
		Dungeon.hero = hero;

		Healing healing = Buff.affect(hero, Healing.class);
		healing.setHeal(94, 0.25f, 0);
		healing.applyVialEffect();

		assertNotNull(healing);
		assertEquals("141", healing.iconTextDisplay());

		healing.setHeal(94, 0.25f, 0);
		healing.applyVialEffect();

		assertEquals("141", healing.iconTextDisplay());
	}
}
