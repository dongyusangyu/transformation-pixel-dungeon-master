package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfToxicGas;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CorpseTest {

	@Test
	public void dropsToxicGasPotionAtOneInEightBaseChance() {
		TestCorpse corpse = new TestCorpse();

		assertEquals(1f / 8f, corpse.lootChance(), 0f);
		assertSame(PotionOfToxicGas.class, corpse.lootType());
	}

	@Test
	public void usesApprovedTowerStats() {
		Corpse corpse = new Corpse();
		Gnoll attacker = new Gnoll();

		assertEquals(1, corpse.HT);
		assertEquals(1, corpse.HP);
		assertEquals(40, corpse.attackSkill(null));
		assertEquals(20, corpse.defenseSkill(attacker));
		assertEquals(0, corpse.drRoll());
		assertEquals(1f, corpse.speed(), 0f);
		assertEquals(1f, corpse.attackDelay(), 0f);
		assertEquals(13, corpse.EXP);
		assertEquals(30, corpse.maxLvl);
		assertTrue(corpse.properties().contains(Char.Property.UNDEAD));

		for (int i = 0; i < 500; i++) {
			int damage = corpse.damageRoll();
			assertTrue(damage >= 10 && damage <= 40);
		}
	}

	@Test
	public void ignoresOrdinaryDamageRegardlessOfTag() {
		Corpse corpse = new Corpse();

		corpse.damage(100, new Object(), DamageTag.PHYSICAL);
		corpse.damage(100, new Object(), DamageTag.MAGICAL);
		corpse.damage(100, new Object(), DamageTag.POISON);

		assertEquals(1, corpse.HP);
		assertTrue(corpse.isAlive());
	}

	@Test
	public void positiveHealingAtFullHealthKillsAndReturnsZero() {
		RecordingCorpse corpse = new RecordingCorpse();

		assertEquals(0, corpse.heal(1));
		assertFalse(corpse.isAlive());
		assertTrue(corpse.died);
	}

	@Test
	public void silentPositiveHealingAlsoKills() {
		RecordingCorpse corpse = new RecordingCorpse();

		assertEquals(0, corpse.heal(1, false));
		assertFalse(corpse.isAlive());
		assertTrue(corpse.died);
	}

	@Test
	public void nonPositiveHealingDoesNothing() {
		Corpse corpse = new Corpse();

		assertEquals(0, corpse.heal(0));
		assertEquals(0, corpse.heal(-1, false));
		assertEquals(1, corpse.HP);
		assertTrue(corpse.isAlive());
	}

	private static class RecordingCorpse extends Corpse {

		private boolean died;

		@Override
		public void die(Object cause) {
			died = true;
			HP = 0;
		}
	}

	private static class TestCorpse extends Corpse {

		@Override
		protected float adjustedLootChance(float baseChance) {
			return baseChance;
		}

		private Object lootType() {
			return loot;
		}
	}
}
