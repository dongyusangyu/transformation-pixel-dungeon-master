package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HeroEquipmentReplicaTest {

	@Test
	public void guardScopeCopiesWeaponArmorStrengthAndCombatRings() {
		RecordingWeapon weapon = new RecordingWeapon();
		Armor armor = new Armor(4);
		TestSource source = new TestSource(weapon, armor, 17);
		HeroEquipmentReplica replica = new HeroEquipmentReplica();

		replica.refresh(source, HeroEquipmentReplica.Scope.GUARD);

		assertSame(weapon, replica.weapon());
		assertSame(armor, replica.armor());
		assertEquals(17, replica.strength());
		assertEquals(2, replica.ringBonus(RingOfAccuracy.Accuracy.class, false));
		assertEquals(3, replica.ringBonus(RingOfAccuracy.Accuracy.class, true));
		assertEquals(0, replica.ringBonus(RingOfWealth.Wealth.class, false));
		assertEquals(0, replica.ringBonus(RingOfEnergy.Energy.class, true));
	}

	@Test
	public void mirrorScopeCopiesOnlyWeaponAndRefreshReplacesReferences() {
		RecordingWeapon first = new RecordingWeapon();
		RecordingWeapon second = new RecordingWeapon();
		Armor armor = new Armor(4);
		TestSource source = new TestSource(first, armor, 18);
		HeroEquipmentReplica replica = new HeroEquipmentReplica();

		replica.refresh(source, HeroEquipmentReplica.Scope.MIRROR);
		assertSame(first, replica.weapon());
		assertNull(replica.armor());
		assertEquals(0, replica.ringBonus(RingOfForce.Force.class, true));

		source.weapon = second;
		replica.refresh(source, HeroEquipmentReplica.Scope.MIRROR);
		assertSame(second, replica.weapon());
	}

	@Test
	public void sharedMobDelegatesCompleteWeaponCombatContract() {
		RecordingWeapon weapon = new RecordingWeapon();
		TestSource source = new TestSource(weapon, null, 16);
		TestReplicaMob mob = new TestReplicaMob(source);
		Char target = new Char() {
		};
		target.pos = 9;
		mob.pos = 8;
		mob.refreshForTest();

		assertEquals(23, mob.damageRoll());
		assertEquals(60, mob.attackSkill(target));
		assertEquals(2f, mob.attackDelay(), 0f);
		assertTrue(mob.canAttackForTest(target));
		assertEquals(7, mob.drRoll());
		assertEquals(15, mob.attackProc(target, 10, DamageTag.MELEE));

		assertSame(mob, weapon.damageOwner);
		assertSame(mob, weapon.accuracyOwner);
		assertSame(target, weapon.accuracyTarget);
		assertSame(mob, weapon.delayOwner);
		assertSame(mob, weapon.reachOwner);
		assertSame(mob, weapon.defenseOwner);
		assertSame(mob, weapon.procAttacker);
		assertSame(target, weapon.procDefender);
	}

	private static final class TestSource implements HeroEquipmentReplica.EquipmentSource {

		private KindOfWeapon weapon;
		private final Armor armor;
		private final int strength;

		private TestSource(KindOfWeapon weapon, Armor armor, int strength) {
			this.weapon = weapon;
			this.armor = armor;
			this.strength = strength;
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
			if (type == RingOfAccuracy.Accuracy.class) {
				return buffed ? 3 : 2;
			}
			return 9;
		}
	}

	private static final class TestReplicaMob extends HeroReplicaMob {

		private final HeroEquipmentReplica.EquipmentSource source;

		private TestReplicaMob(HeroEquipmentReplica.EquipmentSource source) {
			this.source = source;
		}

		@Override
		protected HeroEquipmentReplica.Scope replicaScope() {
			return HeroEquipmentReplica.Scope.MIRROR;
		}

		@Override
		protected HeroEquipmentReplica.EquipmentSource equipmentSource() {
			return source;
		}

		@Override
		protected float accuracyRingMultiplier() {
			return 1f;
		}

		@Override
		protected float furorRingMultiplier() {
			return 1f;
		}

		@Override
		protected float hasteRingMultiplier() {
			return 1f;
		}

		@Override
		protected int armedForceDamageBonus() {
			return 0;
		}

		@Override
		protected int processBaseAttackProc(Char target, int damage, DamageTag... tags) {
			return damage;
		}

		@Override
		protected int baseDrRoll() {
			return 0;
		}

		@Override
		protected int rollWeaponDefense(int maximum) {
			return maximum;
		}

		private void refreshForTest() {
			refreshReplica();
		}

		private boolean canAttackForTest(Char target) {
			return canAttack(target);
		}
	}

	private static final class RecordingWeapon extends KindOfWeapon {

		private Char damageOwner;
		private Char accuracyOwner;
		private Char accuracyTarget;
		private Char delayOwner;
		private Char reachOwner;
		private Char defenseOwner;
		private Char procAttacker;
		private Char procDefender;

		@Override
		public int min(int lvl) {
			return 1;
		}

		@Override
		public int max(int lvl) {
			return 1;
		}

		@Override
		public int damageRoll(Char owner) {
			damageOwner = owner;
			return 23;
		}

		@Override
		public float accuracyFactor(Char owner, Char target) {
			accuracyOwner = owner;
			accuracyTarget = target;
			return 1.5f;
		}

		@Override
		public float delayFactor(Char owner) {
			delayOwner = owner;
			return 2f;
		}

		@Override
		public boolean canReach(Char owner, int target, int extraReach) {
			reachOwner = owner;
			return true;
		}

		@Override
		public int defenseFactor(Char owner) {
			defenseOwner = owner;
			return 7;
		}

		@Override
		public int proc(Char attacker, Char defender, int damage) {
			procAttacker = attacker;
			procDefender = defender;
			return damage + 5;
		}
	}
}
