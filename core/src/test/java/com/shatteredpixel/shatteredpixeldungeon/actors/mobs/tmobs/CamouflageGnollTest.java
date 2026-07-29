package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CamouflageGnollTest {

	@Test
	public void usesApprovedDemonHallsBaselineStats() {
		TestGnoll gnoll = new TestGnoll(false, false);
		Gnoll attacker = new Gnoll();

		assertEquals(100, gnoll.HT);
		assertEquals(100, gnoll.HP);
		assertEquals(40, gnoll.attackSkill(null));
		assertEquals(20, gnoll.defenseSkill(attacker));
		assertEquals(1f, gnoll.speed(), 0f);
		assertEquals(1f, gnoll.attackDelay(), 0f);
		assertEquals(13, gnoll.EXP);
		assertEquals(26, gnoll.maxLvl);

		for (int i = 0; i < 500; i++) {
			int damage = gnoll.damageRoll();
			int armor = gnoll.armorRoll();
			assertTrue(damage >= 20 && damage <= 30);
			assertTrue(armor >= 0 && armor <= 10);
		}
	}

	@Test
	public void grassRaisesEvasionByFiftyPercentAndGuaranteesPoison() {
		TestGnoll plain = new TestGnoll(false, false);
		TestGnoll grass = new TestGnoll(true, false);
		Gnoll attacker = new Gnoll();

		assertEquals(20, plain.defenseSkill(attacker));
		assertEquals(30, grass.defenseSkill(attacker));
		assertEquals(1f / 3f, plain.poisonChance(), 0.0001f);
		assertEquals(1f, grass.poisonChance(), 0f);
		assertFalse(plain.isCamouflaged());
		assertTrue(grass.isCamouflaged());
	}

	@Test
	public void successfulPoisonStrikeAppliesFourTurnsOfPoison() {
		TestGnoll gnoll = new TestGnoll(false, true);
		Mob target = new Gnoll();

		gnoll.applyPoison(target, 12);
		Poison poison = target.buff(Poison.class);
		assertNotNull(poison);
		assertEquals("4", poison.iconTextDisplay());
	}

	@Test
	public void blockedStrikeDoesNotPoison() {
		TestGnoll gnoll = new TestGnoll(true, true);
		Mob target = new Gnoll();

		gnoll.applyPoison(target, 0);
		assertNull(target.buff(Poison.class));
	}

	private static class TestGnoll extends CamouflageGnoll {

		private final boolean grass;
		private final boolean poisonRoll;

		private TestGnoll(boolean grass, boolean poisonRoll) {
			this.grass = grass;
			this.poisonRoll = poisonRoll;
		}

		@Override
		protected boolean isOnGrass() {
			return grass;
		}

		@Override
		protected boolean rollPoison() {
			return poisonRoll;
		}
	}
}
