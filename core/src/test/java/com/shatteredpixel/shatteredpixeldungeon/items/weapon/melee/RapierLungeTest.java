package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RapierLungeTest {

	@After
	public void clearHero() {
		Dungeon.hero = null;
	}

	@Test
	public void missedLungeSpendsChargeAndClearsTemporaryAbilityWeapon() {
		Hero hero = TestHeroFactory.create();
		Dungeon.hero = hero;
		hero.belongings.abilityWeapon = TestHeroFactory.allocateItem(Rapier.class);
		MeleeWeapon.Charger charger = Buff.affect(hero, MeleeWeapon.Charger.class);
		charger.charges = 2;
		charger.partialCharge = 0.5f;

		Rapier.spendChargeForMissedLunge(hero);

		assertEquals(1, charger.charges);
		assertEquals(0.5f, charger.partialCharge, 0.0001f);
		assertNull(hero.belongings.abilityWeapon);
	}
}
