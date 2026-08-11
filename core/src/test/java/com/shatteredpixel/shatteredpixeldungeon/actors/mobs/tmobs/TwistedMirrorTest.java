package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TwistedMirrorTest {

	@Test
	public void hasDerivedMobStatsAndNoRewards() {
		TwistedMirror mirror = new TwistedMirror();

		assertEquals(1, mirror.HT);
		assertEquals(1, mirror.HP);
		assertEquals(20, mirror.defenseSkill(null));
		assertEquals(0, mirror.EXP);
		assertEquals(30, mirror.maxLvl);
		assertEquals(0f, mirror.lootChance(), 0f);
		assertNull(mirror.createLoot());
	}

	@Test
	public void copiesOnlyWeaponReferenceAndNeverArmorOrRings() {
		FixedWeapon weapon = new FixedWeapon();
		Armor armor = new Armor(4);
		TestMirror mirror = new TestMirror(new FixedSource(weapon, armor));

		mirror.refreshForTest();

		assertSame(weapon, mirror.weaponForTest());
		assertNull(mirror.armorForTest());
		assertEquals(31, mirror.damageRoll());
		assertEquals(0, mirror.copiedRingBonus(RingOfAccuracy.Accuracy.class, true));
	}

	private static final class TestMirror extends TwistedMirror {

		private final HeroEquipmentReplica.EquipmentSource source;

		private TestMirror(HeroEquipmentReplica.EquipmentSource source) {
			this.source = source;
		}

		@Override
		protected HeroEquipmentReplica.EquipmentSource equipmentSource() {
			return source;
		}

		@Override
		protected int armedForceDamageBonus() {
			return 0;
		}

		private void refreshForTest() {
			refreshReplica();
		}

		private KindOfWeapon weaponForTest() {
			return replica.weapon();
		}

		private Armor armorForTest() {
			return replica.armor();
		}
	}

	private static final class FixedSource implements HeroEquipmentReplica.EquipmentSource {

		private final KindOfWeapon weapon;
		private final Armor armor;

		private FixedSource(KindOfWeapon weapon, Armor armor) {
			this.weapon = weapon;
			this.armor = armor;
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
			return 20;
		}

		@Override
		public int ringBonus(Class<? extends Ring.RingBuff> type, boolean buffed) {
			return 7;
		}
	}

	private static final class FixedWeapon extends KindOfWeapon {

		@Override
		public int min(int lvl) {
			return 31;
		}

		@Override
		public int max(int lvl) {
			return 31;
		}

		@Override
		public int damageRoll(com.shatteredpixel.shatteredpixeldungeon.actors.Char owner) {
			return 31;
		}
	}
}
