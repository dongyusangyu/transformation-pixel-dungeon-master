package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.watabou.utils.Random;

/**
 * A scorpio that attacks six times as often with low-damage strikes.
 */
public class DeferredScorpio extends Scorpio {

	public static final int ATTACKS_PER_TURN = 6;
	public static final float ATTACK_INTERVAL_FACTOR = 1f / ATTACKS_PER_TURN;
	public static final int MIN_DAMAGE = 2;
	public static final int MAX_DAMAGE = 8;
	public static final float ARMOR_PIERCE_CHANCE = 0.2f;

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(MIN_DAMAGE, MAX_DAMAGE);
	}

	@Override
	public float attackDelay() {
		return super.attackDelay() * ATTACK_INTERVAL_FACTOR;
	}

	@Override
	public int attackSkill(Char target) {
		return super.attackSkill(target) + 10;
	}

	@Override
	public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
		damage = super.attackProc(enemy, damage, damageTags);
		float roll = Random.Float();
		int armorDamage = roll < ARMOR_PIERCE_CHANCE ? enemy.drRoll() : 0;
		return armorPierceDamage(damage, armorDamage, roll);
	}

	public static int armorPierceDamage(int damage, int targetArmor, float roll) {
		return roll < ARMOR_PIERCE_CHANCE ? damage + targetArmor : damage;
	}

	@Override
	public String description() {
		return RaidKeyCarrier.appendDescription(this, super.description());
	}
}
