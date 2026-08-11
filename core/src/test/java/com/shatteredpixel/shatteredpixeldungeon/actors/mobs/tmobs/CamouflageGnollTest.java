package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CamouflageGnollTest {

	@Test
	public void dropsGoldAtFiftyPercentBaseChance() {
		TestGnoll gnoll = new TestGnoll(false);

		assertEquals(0.5f, gnoll.lootChance(), 0f);
		assertSame(Gold.class, gnoll.lootType());
	}

	@Test
	public void usesApprovedDemonHallsBaselineStats() {
		TestGnoll gnoll = new TestGnoll(false);
		Gnoll attacker = new Gnoll();

		assertEquals(100, gnoll.HT);
		assertEquals(100, gnoll.HP);
		assertEquals(40, gnoll.attackSkill(null));
		assertEquals(20, gnoll.defenseSkill(attacker));
		assertEquals(1f, gnoll.speed(), 0f);
		assertEquals(1f, gnoll.attackDelay(), 0f);
		assertEquals(13, gnoll.EXP);
		assertEquals(30, gnoll.maxLvl);

		for (int i = 0; i < 500; i++) {
			int damage = gnoll.damageRoll();
			int armor = gnoll.armorRoll();
			assertTrue(damage >= 20 && damage <= 30);
			assertTrue(armor >= 0 && armor <= 10);
		}
	}

	@Test
	public void grassRaisesEvasionByFiftyPercent() {
		TestGnoll plain = new TestGnoll(false);
		TestGnoll grass = new TestGnoll(true);
		Gnoll attacker = new Gnoll();

		assertEquals(20, plain.defenseSkill(attacker));
		assertEquals(30, grass.defenseSkill(attacker));
		assertFalse(plain.isCamouflaged());
		assertTrue(grass.isCamouflaged());
	}

	@Test
	public void everyLandedAttackPoisonsEvenWhenDamageIsZero() {
		TestGnoll gnoll = new TestGnoll(false);
		Mob target = new Gnoll();

		gnoll.applyPoison(target, 0);
		Poison poison = target.buff(Poison.class);
		assertNotNull(poison);
		int duration = Integer.parseInt(poison.iconTextDisplay());
		assertTrue(duration >= 2 && duration <= 4);
	}

	@Test
	public void plainPoisonDurationStacksByTwoToFourTurnsPerHit() {
		TestGnoll gnoll = new TestGnoll(false);
		Mob target = new Gnoll();

		gnoll.applyPoison(target, 0);
		Poison poison = target.buff(Poison.class);
		int firstDuration = Integer.parseInt(poison.iconTextDisplay());

		gnoll.applyPoison(target, 0);
		int stackedDuration = Integer.parseInt(poison.iconTextDisplay());

		assertTrue(firstDuration >= 2 && firstDuration <= 4);
		assertTrue(stackedDuration >= firstDuration + 2);
		assertTrue(stackedDuration <= firstDuration + 4);
	}

	@Test
	public void grassPoisonDurationStacksByTenToTwentyTurnsPerHit() {
		TestGnoll gnoll = new TestGnoll(true);
		Mob target = new Gnoll();

		gnoll.applyPoison(target, 0);
		Poison poison = target.buff(Poison.class);
		int firstDuration = Integer.parseInt(poison.iconTextDisplay());

		gnoll.applyPoison(target, 0);
		int stackedDuration = Integer.parseInt(poison.iconTextDisplay());

		assertTrue(firstDuration >= 10 && firstDuration <= 20);
		assertTrue(stackedDuration >= firstDuration + 10);
		assertTrue(stackedDuration <= firstDuration + 20);
	}

	private static class TestGnoll extends CamouflageGnoll {

		private final boolean grass;

		private TestGnoll(boolean grass) {
			this.grass = grass;
		}

		@Override
		protected boolean isOnGrass() {
			return grass;
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			return baseChance;
		}

		private Object lootType() {
			return loot;
		}

	}
}
