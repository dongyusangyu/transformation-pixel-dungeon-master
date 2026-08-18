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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

/**
 * A swift tier-six sword whose successful hits build two single-use benefits.
 * Its duelist ability safely lunges into a three-strike sequence without
 * treating movement as a normal step, matching the Rapier's lunge behaviour.
 */
public class PalermoSword extends MeleeWeapon {

	public static final int TIER = 6;
	public static final float ACCURACY = 1.2f;
	public static final float DELAY = 0.5f;
	public static final int RANGE = 1;
	private static final int HITS_PER_REWARD = 4;
	private static final int XIEXIANG_STRIKES = 3;
	private static final int XIEXIANG_CHARGE_COST = 2;
	private static final int XIEXIANG_EXTRA_TARGET_RANGE = 1;
	private static final int XIEXIANG_MOVEMENT_RANGE = 1;
	private static final String HIT_COUNT = "palermo_hit_count";

	private final HitState hitState = new HitState();
	// Set during a confirmed hit, then consumed by that attack's time settlement.
	private transient boolean skipNextAttackDelay;

	{
		// Reserved 16x16 cell directly after the two-handed greatsword.
		image = EXItemSpriteSheet.PALERMO_SWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.1f;
		tier = TIER;
		ACC = ACCURACY;
		DLY = DELAY;
		RCH = RANGE;
		usesTargeting = true;
	}

	@Override
	public int min(int lvl) {
		return minForLevel(lvl);
	}

	@Override
	public int max(int lvl) {
		return maxForLevel(lvl);
	}

	@Override
	public int STRReq(int lvl) {
		int requirement = strengthRequirementForLevel(lvl);
		if (masteryPotionBonus) requirement -= 2;
		return requirement;
	}

	public static int minForLevel(int level) {
		return 6 + Math.max(0, level) * 2;
	}

	public static int maxForLevel(int level) {
		return 18 + Math.max(0, level) * 4;
	}

	public static int strengthRequirementForLevel(int level) {
		return STRReq(TIER, level);
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return XIEXIANG_CHARGE_COST;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(final Hero hero, Integer targetCell) {
		if (targetCell == null) return;
		Char target = Actor.findChar(targetCell);
		if (!isXiexiangTarget(hero, target)) {
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}
		if (!xiexiangCanContinue(hero.isAlive(), hero.paralysed, hero.rooted)) {
			GLog.w(Messages.get(this, "ability_target_range"));
			PixelScene.shake(1, 1f);
			return;
		}
		if (safeLungeCell(hero, target) == -1) {
			GLog.w(Messages.get(this, "ability_target_range"));
			return;
		}

		hero.busy();
		xiexiangStep(hero, target, 0);
	}

	private void xiexiangStep(final Hero hero, final Char target, final int strikeIndex) {
		if (!isXiexiangTarget(hero, target)
				|| !xiexiangCanContinue(hero.isAlive(), hero.paralysed, hero.rooted)) {
			finishXiexiang(hero, strikeIndex > 0);
			return;
		}
		final int destination = safeLungeCell(hero, target);
		if (destination == -1 || !xiexiangMayStrikeAtStep(strikeIndex, destination != hero.pos)) {
			finishXiexiang(hero, strikeIndex > 0);
			return;
		}

		Callback strike = new Callback() {
			@Override
			public void call() {
				strikeXiexiangTarget(hero, target, strikeIndex);
			}
		};
		if (destination == hero.pos || hero.sprite == null) {
			strike.call();
			return;
		}

		Sample.INSTANCE.play(Assets.Sounds.MISS);
		hero.sprite.jump(hero.pos, destination, 0, 0.1f, new Callback() {
			@Override
			public void call() {
				moveHeroWithoutTriggeringTerrain(hero, destination);
				strike.call();
			}
		});
	}

	private void strikeXiexiangTarget(final Hero hero, final Char target, final int strikeIndex) {
		hero.belongings.abilityWeapon = this;
		if (!xiexiangCanContinue(hero.isAlive(), hero.paralysed, hero.rooted)
				|| !isXiexiangTarget(hero, target) || !hero.canAttack(target)) {
			finishXiexiang(hero, strikeIndex > 0);
			return;
		}

		hero.chooseEnemy(target);
		hero.sprite.attack(target.pos, new Callback() {
			@Override
			public void call() {
				if (strikeIndex == 0) {
					beforeAbilityUsed(hero, target);
					armFutureAccelerationForAbility(hero.buff(FutureAcceleration.class));
				}
				AttackIndicator.target(target);
				boolean hit = hero.attack(target, 1f, xiexiangDamageBoost(buffedLvl()), Char.INFINITE_ACCURACY);
				if (hit) {
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
					if (!target.isAlive()) onAbilityKill(hero, target);
					Invisibility.dispel();
					knockBack(target, hero.pos, new Callback() {
						@Override
						public void call() {
							if (strikeIndex + 1 < maxXiexiangStrikes() && target.isAlive()) {
								xiexiangStep(hero, target, strikeIndex + 1);
							} else {
								finishXiexiang(hero, true);
							}
						}
					});
				} else {
					Invisibility.dispel();
					finishXiexiang(hero, true);
				}
			}
		});
	}

	private void finishXiexiang(Hero hero, boolean abilityWasUsed) {
		if (abilityWasUsed) {
			hero.spendAndNext(hero.attackDelay());
			afterAbilityUsed(hero);
		} else {
			hero.belongings.abilityWeapon = null;
			hero.next();
		}
	}

	private void moveHeroWithoutTriggeringTerrain(Hero hero, int destination) {
		if (Dungeon.level.map[hero.pos] == Terrain.OPEN_DOOR) Door.leave(hero.pos);
		hero.pos = destination;
		Dungeon.level.occupyCell(hero);
		Dungeon.observe();
	}

	private int safeLungeCell(Hero hero, Char target) {
		if (Dungeon.level.distance(hero.pos, target.pos) <= 1) return hero.pos;
		int attackReach = xiexiangAttackReach(hero);
		int lungeCell = -1;
		for (int offset : PathFinder.NEIGHBOURS8) {
			int candidate = hero.pos + offset;
			if (candidate < 0 || candidate >= Dungeon.level.length()
					|| Dungeon.level.distance(hero.pos, candidate) > XIEXIANG_MOVEMENT_RANGE
					|| Actor.findChar(candidate) != null
					|| !(Dungeon.level.passable[candidate]
							|| (Dungeon.level.avoid[candidate] && hero.flying))
					|| Dungeon.level.distance(candidate, target.pos) > attackReach) {
				continue;
			}
			if (lungeCell == -1 || Dungeon.level.trueDistance(candidate, target.pos)
					< Dungeon.level.trueDistance(lungeCell, target.pos)) {
				lungeCell = candidate;
			}
		}
		return lungeCell;
	}

	private boolean isXiexiangTarget(Hero hero, Char target) {
		return target != null && target != hero && target.isAlive()
				&& target.alignment == Char.Alignment.ENEMY && !hero.isCharmedBy(target)
				&& target.pos >= 0 && target.pos < Dungeon.level.length()
				&& Dungeon.level.heroFOV[target.pos]
				&& xiexiangTargetAllowed(true, Dungeon.level.distance(hero.pos, target.pos),
						xiexiangAttackReach(hero));
	}

	private int xiexiangAttackReach(Hero hero) {
		int reach = reachFactor(hero);
		Berserk berserk = hero.buff(Berserk.class);
		if (berserk != null && berserk.isBerserking()
				&& hero.hasTalent(Talent.BLOODTHIRSTY_BERSERK)) {
			reach++;
		}
		return reach;
	}

	private void knockBack(Char target, int source, Callback callback) {
		Ballistica trajectory = new Ballistica(source, target.pos, Ballistica.STOP_TARGET);
		trajectory = new Ballistica(trajectory.collisionPos,
				trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
		WandOfBlastWave.throwCharImmediately(target, trajectory, 1, true, false, this, callback);
	}

	@Override
	public float delayFactor(Char owner) {
		FutureAcceleration future = owner.buff(FutureAcceleration.class);
		boolean currentAttackWeapon = owner instanceof Hero
				&& ((Hero) owner).belongings.attackingWeapon() == this;
		if (future != null && currentAttackWeapon && !future.appliesToThisAttack()) {
			future.armForNextAttack();
		}
		if (currentAttackWeapon && skipNextAttackDelay) {
			skipNextAttackDelay = false;
			BuffIndicator.refreshHero();
			return 0f;
		}
		return super.delayFactor(owner);
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		boolean heroUsesThis = attacker == Dungeon.hero && attacker instanceof Hero
				&& ((Hero) attacker).belongings.attackingWeapon() == this
				&& defender.alignment == Char.Alignment.ENEMY;
		FutureAcceleration future = heroUsesThis ? attacker.buff(FutureAcceleration.class) : null;
		damage = super.proc(attacker, defender, damage);
		if (!heroUsesThis) {
			return damage;
		}

		if (future != null && future.appliesToThisAttack()) {
			future.detach();
			skipNextAttackDelay = true;
		}
		Breathing breathing = attacker.buff(Breathing.class);
		if (breathing != null) {
			breathing.detach();
			BuffIndicator.refreshHero();
		}
		if (hitState.recordEnemyHit()) {
			Buff.affect(attacker, FutureAcceleration.class).refreshForNextAttack();
			Buff.affect(attacker, Breathing.class);
			BuffIndicator.refreshHero();
		}
		return damage;
	}

	@Override
	public int damageRoll(Char owner) {
		if (owner.buff(Breathing.class) == null) return super.damageRoll(owner);

		// Replace only this weapon's random base roll. Strength and the hero's
		// ordinary post-roll bonuses still flow through the standard attack pipeline.
		int damage = augment.damageFactor(breathDamageForMaximum(max()));
		if (owner instanceof Hero) {
			int excessStrength = ((Hero) owner).STR() - STRReq();
			if (excessStrength > 0) damage += Hero.heroDamageIntRange(0, excessStrength);
		}
		return damage;
	}

	public static int breathDamageForMaximum(int maximumDamage) {
		return (int) Math.ceil(maximumDamage * 1.2f);
	}

	public static int breathDamageWithStrength(int maximumDamage, int strengthDamage) {
		return breathDamageForMaximum(maximumDamage) + Math.max(0, strengthDamage);
	}

	public static float delayAfterFutureAcceleration(float normalDelay, boolean accelerated) {
		return accelerated ? 0f : normalDelay;
	}

	public static int xiexiangTargetRange(int attackReach) {
		return Math.max(1, attackReach) + XIEXIANG_EXTRA_TARGET_RANGE;
	}

	public static int xiexiangMovementRange() {
		return XIEXIANG_MOVEMENT_RANGE;
	}

	public static boolean xiexiangTargetAllowed(boolean enemyVisible, int distance, int attackReach) {
		return enemyVisible && distance >= 1 && distance <= xiexiangTargetRange(attackReach);
	}

	static boolean armFutureAccelerationForAbility(FutureAcceleration future) {
		if (future == null) return false;
		if (!future.appliesToThisAttack()) future.armForNextAttack();
		return true;
	}

	public static int maxXiexiangStrikes() {
		return XIEXIANG_STRIKES;
	}

	public static int xiexiangChargeCost() {
		return XIEXIANG_CHARGE_COST;
	}

	public static int xiexiangDamageBoost(int weaponLevel) {
		return 4 + Math.max(0, weaponLevel);
	}

	public static boolean xiexiangMayStrikeAtStep(int strikeIndex, boolean lungeChangesPosition) {
		// A blocked/immune knockback leaves the target adjacent. Follow-up strikes
		// are still valid and simply play in place instead of ending the chain.
		return strikeIndex >= 0;
	}

	public static boolean xiexiangCanContinue(boolean heroAlive, int paralysis, boolean rooted) {
		return heroAlive && paralysis <= 0 && !rooted;
	}

	@Override
	public String abilityInfo() {
		int level = levelKnown ? buffedLvl() : 0;
		int damageBoost = xiexiangDamageBoost(level);
		if (levelKnown) {
			return Messages.get(this, "ability_desc",
					augment.damageFactor(min()) + damageBoost,
					augment.damageFactor(max()) + damageBoost);
		}
		return Messages.get(this, "typical_ability_desc",
				min(0) + damageBoost, max(0) + damageBoost);
	}

	@Override
	public String upgradeAbilityStat(int level) {
		int damageBoost = xiexiangDamageBoost(level);
		return augment.damageFactor(min(level)) + damageBoost + "-"
				+ (augment.damageFactor(max(level)) + damageBoost);
	}

	@Override
	public String statsInfo() {
		return Messages.get(this, "stats_desc");
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(HIT_COUNT, hitState.hitsSinceReward());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		hitState.restore(bundle.getInt(HIT_COUNT));
	}

	public static class HitState {
		private int hitsSinceReward;

		public boolean recordEnemyHit() {
			hitsSinceReward = (hitsSinceReward + 1) % HITS_PER_REWARD;
			return hitsSinceReward == 0;
		}

		public int hitsSinceReward() {
			return hitsSinceReward;
		}

		void restore(int storedHits) {
			hitsSinceReward = Math.max(0, storedHits) % HITS_PER_REWARD;
		}
	}

	public static class FutureAcceleration extends Buff {
		private static final String WAITING_FOR_CURRENT_ATTACK = "waiting_for_current_attack";
		private boolean waitingForCurrentAttack = true;

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.JIASUWEILAI;
		}


		@Override
		public String name() {
			return Messages.get(PalermoSword.class, "future_name");
		}

		@Override
		public String desc() {
			return Messages.get(PalermoSword.class, "future_desc");
		}

		void refreshForNextAttack() {
			waitingForCurrentAttack = true;
		}

		boolean appliesToThisAttack() {
			return !waitingForCurrentAttack;
		}

		void armForNextAttack() {
			waitingForCurrentAttack = false;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(WAITING_FOR_CURRENT_ATTACK, waitingForCurrentAttack);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			waitingForCurrentAttack = bundle.getBoolean(WAITING_FOR_CURRENT_ATTACK);
		}
	}

	public static class Breathing extends Buff {
		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.HUXIFA;
		}

		@Override
		public String name() {
			return Messages.get(PalermoSword.class, "breathing_name");
		}

		@Override
		public String desc() {
			return Messages.get(PalermoSword.class, "breathing_desc");
		}
	}
}
