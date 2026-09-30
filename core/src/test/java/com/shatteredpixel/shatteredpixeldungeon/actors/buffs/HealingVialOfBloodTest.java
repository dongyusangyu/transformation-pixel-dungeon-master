package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

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

	@Test
	public void changingVialAfterHealingStartsDoesNotChangeItsPerTurnLimit() throws Exception {
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

		assertEquals(6, healingThisTick(healing));
		hero.belongings.backpack.items.remove(vial);
		assertEquals(6, healingThisTick(healing));
	}

	@Test
	public void unrelatedHealingSourceReleasesTheBloodVialLimit() throws Exception {
		Hero hero = TestHeroFactory.create();
		hero.HT = 100;
		VialOfBlood vial = TestHeroFactory.allocateItem(VialOfBlood.class);
		vial.level(3);
		hero.belongings.backpack.items.add(vial);
		Dungeon.hero = hero;
		Healing healing = Buff.affect(hero, Healing.class);
		healing.setHeal(94, 0.25f, 0);
		healing.applyVialEffect();
		healing.setHeal(5, 1f, 0);

		assertEquals(141, healingThisTick(healing));
	}

	@Test
	public void savedHealingKeepsTheVialLevelThatStartedIt() throws Exception {
		Hero hero = TestHeroFactory.create();
		hero.HT = 100;
		VialOfBlood vial = TestHeroFactory.allocateItem(VialOfBlood.class);
		vial.level(3);
		hero.belongings.backpack.items.add(vial);
		Dungeon.hero = hero;
		Healing healing = Buff.affect(hero, Healing.class);
		healing.setHeal(94, 0.25f, 0);
		healing.applyVialEffect();
		Bundle saved = new Bundle();
		healing.storeInBundle(saved);
		hero.belongings.backpack.items.remove(vial);
		Healing restored = new Healing();
		restored.restoreFromBundle(saved);

		assertEquals(6, healingThisTick(restored));
		assertEquals("141", restored.iconTextDisplay());
	}

	@Test
	public void legacyHealingLimitSurvivesRestoreBeforeHeroIsAssigned() throws Exception {
		Hero hero = TestHeroFactory.create();
		hero.HT = 100;
		VialOfBlood vial = TestHeroFactory.allocateItem(VialOfBlood.class);
		vial.level(3);
		hero.belongings.backpack.items.add(vial);
		Bundle legacy = new Bundle();
		legacy.put("left", 141);
		legacy.put("percent", 0.25f);
		legacy.put("flat", 0);
		legacy.put("healing_limited", true);
		Dungeon.hero = null;
		Healing restored = new Healing();
		restored.restoreFromBundle(legacy);
		Dungeon.hero = hero;

		assertEquals(6, healingThisTick(restored));
	}

	private static int healingThisTick(Healing healing) throws Exception {
		Method method = Healing.class.getDeclaredMethod("healingThisTick");
		method.setAccessible(true);
		return (int) method.invoke(healing);
	}
}
