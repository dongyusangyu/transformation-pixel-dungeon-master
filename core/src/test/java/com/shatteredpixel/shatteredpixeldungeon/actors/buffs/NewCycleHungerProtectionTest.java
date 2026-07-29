package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NewCycleHungerProtectionTest {

	@After
	public void resetDungeonState() {
		Dungeon.newCycle = false;
		Dungeon.depth = 0;
	}

	@Test
	public void attachesOnlyOnNewCycleFloorZeroAndDetachesAfterLeaving() {
		Char hero = new Char() {
		};

		Dungeon.newCycle = false;
		Dungeon.depth = 0;
		NewCycleHungerProtection.updateForCurrentFloor(hero);
		assertNull(hero.buff(NewCycleHungerProtection.class));

		Dungeon.newCycle = true;
		NewCycleHungerProtection.updateForCurrentFloor(hero);
		NewCycleHungerProtection protection =
				hero.buff(NewCycleHungerProtection.class);
		assertNotNull(protection);
		assertEquals(BuffIndicator.NONE, protection.icon());
		assertTrue(protection.revivePersists);

		Dungeon.depth = 1;
		NewCycleHungerProtection.updateForCurrentFloor(hero);
		assertNull(hero.buff(NewCycleHungerProtection.class));
	}

	@Test
	public void blocksHungerLossButStillAllowsHungerRecovery() {
		Char hero = new Char() {
		};
		Hunger hunger = Buff.affect(hero, Hunger.class);
		hunger.level = 100;
		Buff.affect(hero, NewCycleHungerProtection.class);

		hunger.affectHunger(-20);
		assertEquals(100f, hunger.level, 0f);

		hunger.affectHunger(20);
		assertEquals(80f, hunger.level, 0f);
	}
}
