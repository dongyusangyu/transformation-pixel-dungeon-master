package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AlienatedPrismaticGuardTest {

	@Test
	public void hasTowerBaseStatsAndConfiguredExperience() {
		TestGuard guard = new TestGuard(null);

		assertEquals(100, guard.HT);
		assertEquals(100, guard.HP);
		assertEquals(20, guard.defenseSkill(null));
		assertEquals(13, guard.EXP);
		assertEquals(30, guard.maxLvl);
	}

	@Test
	public void mirrorScrollUsesOneEighthAdjustedDropPipeline() {
		TestGuard guard = new TestGuard(null);

		assertEquals(1f / 8f, guard.lootChance(), 0f);
		assertEquals(1f / 8f, guard.adjustedBaseChance, 0f);
		assertSame(ScrollOfMirrorImage.class, guard.lootClassForTest());
	}

	@Test
	public void armorEvasionAndProcUseGuardAsEquipmentOwner() {
		RecordingArmor armor = new RecordingArmor();
		TestGuard guard = new TestGuard(new FixedSource(null, armor, 18, 0));
		Char attacker = new Char() {
		};
		guard.refreshForTest();

		assertEquals(54, guard.defenseSkill(attacker));
		assertEquals(26, guard.defenseProc(attacker, 10, DamageTag.MELEE));
		assertSame(guard, armor.evasionOwner);
		assertSame(attacker, armor.procAttacker);
		assertSame(guard, armor.procDefender);
	}

	@Test
	public void forceCanEnhanceUnarmedDamageWithoutChangingFixedHealth() {
		TestGuard guard = new TestGuard(new FixedSource(null, null, 18, 2));
		guard.refreshForTest();

		int damage = guard.unarmedDamageForTest();
		assertTrue(damage >= 7 && damage <= 42);
		assertEquals(100, guard.HT);
		assertEquals(100, guard.HP);
	}

	private static final class TestGuard extends AlienatedPrismaticGuard {

		private final HeroEquipmentReplica.EquipmentSource source;
		private float adjustedBaseChance;

		private TestGuard(HeroEquipmentReplica.EquipmentSource source) {
			this.source = source;
		}

		@Override
		protected HeroEquipmentReplica.EquipmentSource equipmentSource() {
			return source;
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			adjustedBaseChance = baseChance;
			return baseChance;
		}

		@Override
		protected float evasionRingMultiplier() {
			return source == null ? 1f : 2f;
		}

		@Override
		protected int processBaseDefenseProc(Char enemy, int damage, DamageTag... tags) {
			return damage + 3;
		}

		private void refreshForTest() {
			refreshReplica();
		}

		private int unarmedDamageForTest() {
			return unarmedDamageRoll();
		}

		private Object lootClassForTest() {
			return loot;
		}
	}

	private static final class FixedSource implements HeroEquipmentReplica.EquipmentSource {

		private final KindOfWeapon weapon;
		private final Armor armor;
		private final int strength;
		private final int force;

		private FixedSource(KindOfWeapon weapon, Armor armor, int strength, int force) {
			this.weapon = weapon;
			this.armor = armor;
			this.strength = strength;
			this.force = force;
		}

		@Override
		public KindOfWeapon weapon() {
			return weapon;
		}

		@Override
		public Armor armor() {
			return armor;
		}

		@Override
		public int strength() {
			return strength;
		}

		@Override
		public int ringBonus(Class<? extends Ring.RingBuff> type, boolean buffed) {
			return type == RingOfForce.Force.class ? force : 0;
		}
	}

	private static final class RecordingArmor extends Armor {

		private Char evasionOwner;
		private Char procAttacker;
		private Char procDefender;

		private RecordingArmor() {
			super(4);
		}

		@Override
		public float evasionFactor(Char owner, float evasion) {
			evasionOwner = owner;
			return evasion + 7f;
		}

		@Override
		public int proc(Char attacker, Char defender, int damage) {
			procAttacker = attacker;
			procDefender = defender;
			return damage * 2;
		}
	}
}
