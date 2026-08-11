package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;

/** A hostile prismatic guard which continuously mirrors the hero's combat equipment. */
public class AlienatedPrismaticGuard extends HeroReplicaMob {

	{
		HP = HT = 100;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		alignment = Alignment.ENEMY;

		loot = ScrollOfMirrorImage.class;
		lootChance = 1f / 8f;
	}

	@Override
	protected HeroEquipmentReplica.Scope replicaScope() {
		return HeroEquipmentReplica.Scope.GUARD;
	}

	@Override
	public float lootChance() {
		return adjustedLootChance(1f / 8f);
	}
}
