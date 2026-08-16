package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.TwistedMirrorSprite;

/** Independent hostile offspring split from an alienated prismatic guard. */
public class TwistedMirror extends HeroReplicaMob {

	{
		spriteClass = TwistedMirrorSprite.class;

		HP = HT = 1;
		defenseSkill = 20;

		EXP = 0;
		maxLvl = 30;

		alignment = Alignment.ENEMY;
		state = HUNTING;
		properties.add(Property.INORGANIC);
		immunities.add(ToxicGas.class);
		immunities.add(CorrosiveGas.class);
		immunities.add(Burning.class);
		immunities.add(AllyBuff.class);

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
