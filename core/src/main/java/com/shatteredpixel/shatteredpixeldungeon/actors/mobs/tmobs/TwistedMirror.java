package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/** Independent hostile offspring split from an alienated prismatic guard. */
public class TwistedMirror extends HeroReplicaMob {

	{
		HP = HT = 1;
		defenseSkill = 20;

		EXP = 0;
		maxLvl = 30;

		alignment = Alignment.ENEMY;
		state = HUNTING;

		loot = null;
		lootChance = 0f;
	}

	@Override
	protected HeroEquipmentReplica.Scope replicaScope() {
		return HeroEquipmentReplica.Scope.MIRROR;
	}

	@Override
	protected int armedForceDamageBonus() {
		return 0;
	}

	@Override
	protected float accuracyRingMultiplier() {
		return 1f;
	}

	@Override
	protected float evasionRingMultiplier() {
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
	protected float tenacityRingMultiplier() {
		return 1f;
	}

	@Override
	public float lootChance() {
		return 0f;
	}

	@Override
	public Item createLoot() {
		return null;
	}
}
