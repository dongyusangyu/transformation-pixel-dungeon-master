package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NaturalChildBarkskinTest {

	@Test
	public void naturalChildBarkskinStacksWithTheStrongestRegularBarkskin() {
		Char target = new Char() { };
		Barkskin regular = Buff.affect(target, Barkskin.class);
		regular.set(12, 1);
		Talent.NaturalChildBarkskin natural =
				Buff.affect(target, Talent.NaturalChildBarkskin.class);
		natural.set(20, 1);

		assertEquals(32, Barkskin.currentLevel(target));
	}

	@Test
	public void naturalChildBarkskinDecaysByOneEachTurn() {
		Char target = new Char() { };
		target.HT = 20;
		target.HP = 20;
		Talent.NaturalChildBarkskin barkskin =
				Buff.affect(target, Talent.NaturalChildBarkskin.class);
		barkskin.set(20, 1);

		barkskin.act();

		assertEquals(19, barkskin.level());
	}

	@Test
	public void naturalChildActionIsDiscoverableByActionIndicator() {
		assertTrue(java.util.Arrays.asList(ActionIndicator.actionBuffClasses)
				.contains(Talent.NaturalChildAction.class));
	}

	@Test
	public void naturalChildUseBudgetIs100Or200Uses() {
		assertEquals(100f, Talent.NaturalChildFurrowCounter.maxUses(1), 0.001f);
		assertEquals(200f, Talent.NaturalChildFurrowCounter.maxUses(2), 0.001f);
	}

	@Test
	public void naturalChildConsumesOneUsePerRelease() {
		Talent.NaturalChildFurrowCounter counter = new Talent.NaturalChildFurrowCounter();
		counter.countUp(99);

		assertTrue(counter.canUse(1));
		counter.consumeUse(1);

		assertEquals(100f, counter.count(), 0.001f);
		assertTrue(!counter.canUse(1));
		counter.consumeUse(1);
		assertEquals(100f, counter.count(), 0.001f);
	}

	@Test
	public void naturalChildUseBudgetRecoversFromExperience() {
		Talent.NaturalChildFurrowCounter counter = new Talent.NaturalChildFurrowCounter();
		counter.countUp(50);

		counter.recoverFromExperience(0.25f, 1);

		assertEquals(25f, counter.count(), 0.001f);
	}
}
