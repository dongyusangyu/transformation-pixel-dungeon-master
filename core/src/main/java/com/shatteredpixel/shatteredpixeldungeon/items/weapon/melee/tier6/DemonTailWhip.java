/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Whip;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.watabou.utils.Random;

import java.util.LinkedHashMap;
import java.util.Map;

public class DemonTailWhip extends MeleeWeapon {

	public static final int TIER = 6;
	public static final int BASE_RANGE = 3;
	private static final float DEBUFF_CHANCE = 0.5f;
	private static final float MINOR_TIER_CHANCE = 0.75f;
	private static final float MINOR_DURATION = 4f;
	private static final float MAJOR_DURATION = 2f;

	enum DebuffTier {
		MINOR,
		MAJOR
	}

	private static final LinkedHashMap<Class<? extends FlavourBuff>, Float>
			MINOR_DEBUFFS = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<? extends FlavourBuff>, Float>
			MAJOR_DEBUFFS = new LinkedHashMap<>();

	static {
		MINOR_DEBUFFS.put(Weakness.class, 2f);
		MINOR_DEBUFFS.put(Vulnerable.class, 2f);
		MINOR_DEBUFFS.put(Cripple.class, 1f);
		MINOR_DEBUFFS.put(Blindness.class, 1f);
		MINOR_DEBUFFS.put(Terror.class, 1f);

		MAJOR_DEBUFFS.put(Amok.class, 3f);
		MAJOR_DEBUFFS.put(Slow.class, 2f);
		MAJOR_DEBUFFS.put(Hex.class, 2f);
		MAJOR_DEBUFFS.put(Paralysis.class, 1f);
	}

	{
		image = EXItemSpriteSheet.DEMON_TAIL_WHIP;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1.1f;
		tier = TIER;
		RCH = BASE_RANGE;
	}

	@Override
	public int min(int level) {
		return 6 + level;
	}

	@Override
	public int max(int level) {
		return 30 + 6 * level;
	}

	public static int strengthRequirementForLevel(int level) {
		return STRReq(TIER, level);
	}

	public static int reachBonusForLevel(int level) {
		return Math.max(0, level) / 7;
	}

	static boolean debuffTriggers(float roll) {
		return roll < DEBUFF_CHANCE;
	}

	static DebuffTier preferredTier(float roll) {
		return roll < MINOR_TIER_CHANCE ? DebuffTier.MINOR : DebuffTier.MAJOR;
	}

	static float durationFor(DebuffTier tier) {
		return tier == DebuffTier.MINOR ? MINOR_DURATION : MAJOR_DURATION;
	}

	static LinkedHashMap<Class<? extends FlavourBuff>, Float> minorDebuffWeights() {
		return new LinkedHashMap<>(MINOR_DEBUFFS);
	}

	static LinkedHashMap<Class<? extends FlavourBuff>, Float> majorDebuffWeights() {
		return new LinkedHashMap<>(MAJOR_DEBUFFS);
	}

	static DebuffTier[] poolOrder(DebuffTier preferred) {
		return preferred == DebuffTier.MINOR
				? new DebuffTier[]{DebuffTier.MINOR, DebuffTier.MAJOR}
				: new DebuffTier[]{DebuffTier.MAJOR, DebuffTier.MINOR};
	}

	static LinkedHashMap<Class<? extends FlavourBuff>, Float> availableDebuffs(
			Char target, Map<Class<? extends FlavourBuff>, Float> source) {
		LinkedHashMap<Class<? extends FlavourBuff>, Float> available =
				new LinkedHashMap<>();
		for (Map.Entry<Class<? extends FlavourBuff>, Float> entry : source.entrySet()) {
			Class<? extends FlavourBuff> effect = entry.getKey();
			if (entry.getValue() > 0f && target.buff(effect) == null
					&& !target.isImmune(effect)) {
				available.put(effect, entry.getValue());
			}
		}
		return available;
	}

	@Override
	public int reachFactor(Char owner) {
		return super.reachFactor(owner) + reachBonusForLevel(buffedLvl());
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int result = super.proc(attacker, defender, damage);
		if (defender.isAlive() && debuffTriggers(Random.Float())) {
			tryApplyRandomDebuff(defender, preferredTier(Random.Float()));
		}
		return result;
	}

	static boolean tryApplyRandomDebuff(Char target, DebuffTier preferred) {
		for (DebuffTier tier : poolOrder(preferred)) {
			LinkedHashMap<Class<? extends FlavourBuff>, Float> available =
					availableDebuffs(target, tier == DebuffTier.MINOR
							? MINOR_DEBUFFS : MAJOR_DEBUFFS);
			while (!available.isEmpty()) {
				Class<? extends FlavourBuff> selected = Random.chances(available);
				FlavourBuff applied = Buff.append(
						target, selected, durationFor(tier));
				if (target.buff(selected) == applied) {
					return true;
				}
				available.remove(selected);
			}
		}
		return false;
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		Whip.WhipAbility(hero, target, 1f, 0, this);
	}

	@Override
	public String abilityInfo() {
		if (levelKnown) {
			return Messages.get(this, "ability_desc",
					augment.damageFactor(min()), augment.damageFactor(max()));
		} else {
			return Messages.get(this, "typical_ability_desc", min(0), max(0));
		}
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return augment.damageFactor(min(level)) + "-" + augment.damageFactor(max(level));
	}
}
