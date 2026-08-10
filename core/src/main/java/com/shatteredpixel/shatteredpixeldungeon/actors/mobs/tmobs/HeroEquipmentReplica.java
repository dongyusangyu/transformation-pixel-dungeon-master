package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;

import java.util.HashMap;
import java.util.Map;

/** A live, non-owning view of the hero equipment used by hostile replicas. */
public final class HeroEquipmentReplica {

	public enum Scope {
		GUARD,
		MIRROR
	}

	public interface EquipmentSource {
		KindOfWeapon weapon();
		Armor armor();
		int strength();
		int ringBonus(Class<? extends Ring.RingBuff> type, boolean buffed);
	}

	@SuppressWarnings("unchecked")
	private static final Class<? extends Ring.RingBuff>[] COMBAT_RINGS = new Class[]{
			RingOfAccuracy.Accuracy.class,
			RingOfArcana.Arcana.class,
			RingOfElements.Resistance.class,
			RingOfEvasion.Evasion.class,
			RingOfForce.Force.class,
			RingOfFuror.Furor.class,
			RingOfHaste.Haste.class,
			RingOfMight.Might.class,
			RingOfSharpshooting.Aim.class,
			RingOfTenacity.Tenacity.class
	};

	private KindOfWeapon weapon;
	private Armor armor;
	private int strength = 10;
	private final Map<Class<? extends Ring.RingBuff>, Integer> ringBonuses = new HashMap<>();
	private final Map<Class<? extends Ring.RingBuff>, Integer> buffedRingBonuses = new HashMap<>();

	public void refresh(EquipmentSource source, Scope scope) {
		weapon = null;
		armor = null;
		strength = 10;
		ringBonuses.clear();
		buffedRingBonuses.clear();

		if (source == null) {
			return;
		}

		weapon = source.weapon();
		strength = source.strength();
		if (scope == Scope.GUARD) {
			armor = source.armor();
			for (Class<? extends Ring.RingBuff> ring : COMBAT_RINGS) {
				ringBonuses.put(ring, source.ringBonus(ring, false));
				buffedRingBonuses.put(ring, source.ringBonus(ring, true));
			}
		}
	}

	public KindOfWeapon weapon() {
		return weapon;
	}

	public Armor armor() {
		return armor;
	}

	public int strength() {
		return strength;
	}

	public int ringBonus(Class<? extends Ring.RingBuff> type, boolean buffed) {
		Integer result = (buffed ? buffedRingBonuses : ringBonuses).get(type);
		return result == null ? 0 : result;
	}

	public static EquipmentSource dungeonHeroSource() {
		return DungeonHeroSource.INSTANCE;
	}

	private enum DungeonHeroSource implements EquipmentSource {
		INSTANCE;

		@Override
		public KindOfWeapon weapon() {
			return Dungeon.hero == null ? null : Dungeon.hero.belongings.attackingWeapon();
		}

		@Override
		public Armor armor() {
			return Dungeon.hero == null ? null : Dungeon.hero.belongings.armor();
		}

		@Override
		public int strength() {
			return Dungeon.hero == null ? 10 : Dungeon.hero.STR();
		}

		@Override
		public int ringBonus(Class<? extends Ring.RingBuff> type, boolean buffed) {
			if (Dungeon.hero == null) {
				return 0;
			}
			return buffed ? Ring.getBuffedBonus(Dungeon.hero, type) : Ring.getBonus(Dungeon.hero, type);
		}
	}
}
