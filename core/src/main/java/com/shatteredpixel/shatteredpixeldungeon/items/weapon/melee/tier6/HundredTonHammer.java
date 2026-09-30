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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class HundredTonHammer extends MeleeWeapon {

	public static final int TIER = 6;
	public static final float ACCURACY = 1.18f;
	public static final int RANGE = 1;
	public static final float DELAY = 1f;
	private transient boolean abilityKnockback;

	{
		image = EXItemSpriteSheet.HUNDRED_TON_HAMMER;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 0.8f;
		tier = TIER;
		ACC = ACCURACY;
		RCH = RANGE;
		DLY = DELAY;
	}

	@Override
	public int min(int lvl) {
		return minForLevel(lvl);
	}

	@Override
	public int max(int lvl) {
		return maxForLevel(lvl);
	}



	public static int minForLevel(int level) {
		return 6 + Math.max(0, level);
	}

	public static int maxForLevel(int level) {
		return 21 + 7 * Math.max(0, level);
	}

	public static int strengthRequirementForLevel(int level) {
		return STRReq(TIER, level);
	}

	public static int normalKnockbackDistance(int level) {
		return 2 + Math.max(0, level) / 4;
	}

	public static int ambushDamageBonus(int level) {
		return (int) Math.ceil(7 + 1.5f * Math.max(0, level));
	}

	static boolean shouldKnockback(boolean abilityForced, boolean surprised, float roll) {
		return abilityForced || surprised || roll < 1f / 3f;
	}

	@Override
	public java.util.ArrayList<UpgradeAbilityStat> upgradeFeatureStats(int level) {
		java.util.ArrayList<UpgradeAbilityStat> result = new java.util.ArrayList<>();
		result.add(abilityStat(UpgradeAbilityStatType.KNOCKBACK_DISTANCE,
				Integer.toString(normalKnockbackDistance(level))));
		return result;
	}

	public static int bouncePower(int level) {
		return normalKnockbackDistance(level);
	}

	public static int bounceDamage(int level, int actualDistance) {
		return Math.max(0, actualDistance) / 3;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public String abilityInfo() {
		int level = levelKnown ? buffedLvl() : 0;
		return Messages.get(this, levelKnown ? "ability_desc" : "typical_ability_desc",
				ambushAbilityDamage(min(level), level), ambushAbilityDamage(max(level), level));
	}

	private int ambushAbilityDamage(int normalDamage, int level) {
		return augment.damageFactor(normalDamage) + augment.damageFactor(ambushDamageBonus(level));
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(normalKnockbackDistance(level));
	}

	@Override
	public java.util.ArrayList<UpgradeAbilityStat> upgradeAbilityStats(int level) {
		java.util.ArrayList<UpgradeAbilityStat> result = new java.util.ArrayList<>();
		result.add(abilityStat(UpgradeAbilityStatType.AMBUSH_DAMAGE,
				ambushAbilityDamage(min(level), level) + "-" + ambushAbilityDamage(max(level), level)));
		return result;
	}

	@Override
	protected void duelistAbility(final Hero hero, Integer targetCell) {
		if (targetCell == null) {
			hero.belongings.abilityWeapon = null;
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}

		final Char target = Actor.findChar(targetCell);
		hero.belongings.abilityWeapon = this;
		if (target == null) {
			hero.belongings.abilityWeapon = null;
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}
		final int expectedTargetPos = target.pos;
		if (!isValidAbilityTarget(hero, target, expectedTargetPos, true)) {
			hero.belongings.abilityWeapon = null;
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}

		hero.busy();
		hero.chooseEnemy(target);
		Callback attack = new Callback() {
			@Override
			public void call() {
				if (!isValidAbilityTarget(hero, target, expectedTargetPos, false)) {
					hero.belongings.abilityWeapon = null;
					hero.next();
					return;
				}
				beforeAbilityUsed(hero, target);
				AttackIndicator.target(target);
				Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				Invisibility.dispel();
				boolean surprised = target instanceof Mob && ((Mob) target).surprisedBy(hero);
				int bonus = surprised ? augment.damageFactor(ambushDamageBonus(buffedLvl())) : 0;
				int beforeDamage = target.HP + target.shielding();
				abilityKnockback = true;
				boolean hit;
				try {
					hit = hero.attack(target, 1f, bonus, Float.POSITIVE_INFINITY,
						DamageTag.PHYSICAL, DamageTag.MELEE);
				} finally {
					abilityKnockback = false;
				}
				int directDamage = Math.max(0, beforeDamage - target.HP - target.shielding());
				if (!hit || !target.isAlive()) finishAbility(hero);
				else pushAndResolve(hero, target, directDamage);
			}
		};
		if (hero.sprite == null) {
			attack.call();
		} else {
			hero.sprite.attack(target.pos, attack);
		}
	}

	private boolean isValidAbilityTarget(Hero hero, Char target, int expectedTargetPos,
	                                     boolean requireVisible) {
		return isBounceDamageTarget(hero, target)
				&& target.pos == expectedTargetPos
				&& expectedTargetPos >= 0 && expectedTargetPos < Dungeon.level.length()
				&& (!requireVisible || Dungeon.level.heroFOV[expectedTargetPos])
				&& Dungeon.level.distance(hero.pos, target.pos) <= 1
				&& hero.canAttack(target);
	}

	private void pushAndResolve(final Hero hero, final Char target, final int directDamage) {
		Ballistica trajectory = new Ballistica(hero.pos, target.pos, Ballistica.STOP_TARGET);
		trajectory = new Ballistica(trajectory.collisionPos,
				trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
		WandOfBlastWave.throwCharImmediatelyWithResult(target, trajectory, bouncePower(buffedLvl()),
				true, true, this, new WandOfBlastWave.KnockbackCallback() {
			@Override
			public void call(boolean resolvedByThisPush, int reportedDistance) {
				resolveBounceDamage(hero, target, directDamage / 3);
			}
		});
	}

	private void resolveBounceDamage(Hero hero, Char target, int damage) {
		final int finalPos = target.pos;
		ArrayList<Char> splashTargets = new ArrayList<>();
		if (isBounceDamageTarget(hero, target)) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(target,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze.class, 5f);
		}
		if (damage > 0) {
			for (int offset : PathFinder.NEIGHBOURS8) {
				int cell = finalPos + offset;
				if (cell < 0 || cell >= Dungeon.level.length()
						|| Dungeon.level.distance(finalPos, cell) > 1) {
					continue;
				}
				Char splashTarget = Actor.findChar(cell);
				if (splashTarget != target && isBounceDamageTarget(hero, splashTarget)
						&& !splashTargets.contains(splashTarget)) {
					splashTargets.add(splashTarget);
				}
			}

			for (Char splashTarget : splashTargets) {
				if (!isBounceDamageTarget(hero, splashTarget)) continue;
				splashTarget.damage(damage, hero, DamageTag.PHYSICAL);
				if (!splashTarget.isAlive()) onAbilityKill(hero, splashTarget);
			}
		}
		finishAbility(hero);
	}

	private boolean isBounceDamageTarget(Hero hero, Char target) {
		return target != null && target != hero && target.isAlive()
				&& target.alignment == Char.Alignment.ENEMY && !hero.isCharmedBy(target);
	}

	private void finishAbility(Hero hero) {
		hero.spendAndNext(hero.attackDelay());
		afterAbilityUsed(hero);
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int result = super.proc(attacker, defender, damage);
		if (!defender.isAlive() || defender.alignment != Char.Alignment.ENEMY) return result;
		boolean surprised = attacker instanceof Hero && defender instanceof Mob
				&& ((Mob) defender).surprisedBy(attacker);
		if (!abilityKnockback && shouldKnockback(false, surprised, Random.Float())) {
            Ballistica trajectory = new Ballistica(attacker.pos, defender.pos,
                    Ballistica.STOP_TARGET);
            trajectory = new Ballistica(trajectory.collisionPos,
                    trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
            // Start innate knockback before any Elastic pushes queued by super.proc.
            WandOfBlastWave.throwCharImmediately(defender, trajectory,
                    normalKnockbackDistance(buffedLvl()), true, true, this, null);
        }

		return result;
	}
}
