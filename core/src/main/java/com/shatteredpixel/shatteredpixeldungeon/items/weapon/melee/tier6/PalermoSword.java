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
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

/**
 * A swift tier-six sword whose successful hits build two single-use benefits.
 * Its duelist ability safely repositions through a four-strike sequence
 * without treating movement as a normal step.
 */
public class PalermoSword extends MeleeWeapon {

	public static final int TIER = 6;
	public static final float ACCURACY = 1f;
	public static final float DELAY = 0.5f;
	public static final int RANGE = 1;
	private static final int HITS_PER_REWARD = 4;
	private static final int XIEXIANG_STRIKES = 4;
	private static final int XIEXIANG_CHARGE_COST = 2;
	private static final int XIEXIANG_EXTRA_TARGET_RANGE = 1;
	private static final String HIT_COUNT = "palermo_hit_count";
	private static final String PRECISION_TARGET = "palermo_precision_target";
	private static final String PRECISION_COUNT = "palermo_precision_count";
	private static final String PRECISION_TIME = "palermo_precision_time";
	private static final String NEXT_STAB = "palermo_next_stab";

	private final HitState hitState = new HitState();
	private final PrecisionState precisionState = new PrecisionState();
	private boolean nextStab;
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



	public static int minForLevel(int level) {
		return 6 + Math.max(0, level);
	}

	public static int maxForLevel(int level) {
		return 18 + Math.max(0, level) * 4;
	}

	public static int strengthRequirementForLevel(int level) {
		return STRReq(TIER, level);
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return chargeCostForFuture(hero.buff(FutureAcceleration.class) != null);
	}

	static int chargeCostForFuture(boolean hasFuture) {
		return hasFuture ? 1 : XIEXIANG_CHARGE_COST;
	}

	private static float gameTime() {
		return Statistics.duration;
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		float factor = super.accuracyFactor(owner, target);
		if (owner instanceof Hero && target != null
				&& ((Hero) owner).belongings.attackingWeapon() == this) {
			factor *= precisionState.factorFor(target.id(), gameTime());
		}
		return factor;
	}

	@Override
	public void afterHeroAttack(Hero hero, Char target, boolean hit) {
		if (target != null && target.alignment == Char.Alignment.ENEMY
				&& hero.belongings.attackingWeapon() == this) {
			precisionState.recordAttack(target.id(), gameTime());
			if (hitState.recordEnemyHit()) {
				Buff.affect(hero, FutureAcceleration.class).refreshForNextAttack();
				Buff.affect(hero, Breathing.class);
				BuffIndicator.refreshHero();
			}
		}
	}

	@Override
	public void hitSound(float pitch) {
		Sample.INSTANCE.play(nextStab ? Assets.Sounds.HIT_STAB : Assets.Sounds.HIT_SLASH,
				1f, pitch * hitSoundPitch);
		nextStab = !nextStab;
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
		int landingCell = safeXiexiangLandingCell(hero, target);
		if (landingCell == -1) {
			GLog.w(Messages.get(this, "ability_target_range"));
			return;
		}

		hero.busy();
		beforeAbilityUsed(hero, target);
		armFutureAccelerationForAbility(hero.buff(FutureAcceleration.class));
		XiexiangContext context = new XiexiangContext(hero, target);
		moveForXiexiang(context, landingCell, true);
	}

	private void resolveXiexiangStrike(final XiexiangContext context) {
		if (context.finished) return;
		Hero hero = context.hero;
		if (!xiexiangCanContinue(hero.isAlive(), hero.paralysed, hero.rooted)
				|| context.strikes >= maxXiexiangStrikes()) {
			finishXiexiang(context);
			return;
		}

		Char target = findXiexiangTarget(hero, context.target);
		if (target == null) {
			target = findXiexiangTarget(hero, null);
			if (target == null) {
				finishXiexiang(context);
				return;
			}
			context.target = target;
			context.targetStrikes = 0;
			context.firstStrikeCell = -1;
			context.secondStrikeCell = -1;
			context.lockedInPlace = false;
			int landingCell = safeXiexiangLandingCell(hero, target);
			if (landingCell == -1) {
				finishXiexiang(context);
				return;
			}
			moveForXiexiang(context, landingCell, true);
			return;
		}
		context.target = target;

		if (context.targetStrikes > 0 && !context.lockedInPlace) {
			int[] candidates = xiexiangFollowupCells(hero, target);
			int destination = chooseXiexiangFollowupCell(context.targetStrikes, hero.pos,
					context.firstStrikeCell, context.secondStrikeCell, candidates);
			if (destination != hero.pos && !isXiexiangLegalCell(hero, destination)) {
				destination = hero.pos;
			}
			if (context.targetStrikes == 1 && candidates.length == 0) context.lockedInPlace = true;
			if (destination != hero.pos) {
				moveForXiexiang(context, destination, false);
				return;
			}
		}
		strikeXiexiangTarget(context, target);
	}

	private int[] xiexiangFollowupCells(Hero hero, Char target) {
		ArrayList<Integer> cells = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int candidate = hero.pos + offset;
			if (isXiexiangLegalCell(hero, candidate)
					&& xiexiangCanAttackFrom(hero, target, candidate)) cells.add(candidate);
		}
		int[] result = new int[cells.size()];
		for (int i = 0; i < cells.size(); i++) result[i] = cells.get(i);
		return result;
	}

	private void moveForXiexiang(final XiexiangContext context, final int destination,
			final boolean firstStrike) {
		Hero hero = context.hero;
		if (context.finished || context.movementPending) return;
		if (destination == hero.pos || hero.sprite == null) {
			if (destination != hero.pos) moveHeroWithoutTriggeringTerrain(hero, destination);
			if (firstStrike) strikeXiexiangTarget(context, context.target);
			else strikeXiexiangTarget(context, context.target);
			return;
		}

		context.movementPending = true;
		Sample.INSTANCE.play(Assets.Sounds.MISS);
			hero.sprite.jump(hero.pos, destination, 0, 0.1f, new Callback() {
			@Override
			public void call() {
				context.movementPending = false;
				if (context.finished) return;
				if (!isXiexiangLegalCell(context.hero, destination)) {
					resolveXiexiangStrike(context);
					return;
				}
				moveHeroWithoutTriggeringTerrain(context.hero, destination);
				if (firstStrike) strikeXiexiangTarget(context, context.target);
				else strikeXiexiangTarget(context, context.target);
			}
		});
	}

	private Char findXiexiangTarget(Hero hero, Char preferred) {
		if (isXiexiangTarget(hero, preferred)) return preferred;

		Char replacement = null;
		int replacementDistance = Integer.MAX_VALUE;
		for (Char candidate : new ArrayList<>(Actor.chars())) {
			if (!isXiexiangTarget(hero, candidate)) continue;
			int distance = Dungeon.level.distance(hero.pos, candidate.pos);
			if (replacement == null || distance < replacementDistance
					|| distance == replacementDistance && candidate.id() < replacement.id()) {
				replacement = candidate;
				replacementDistance = distance;
			}
		}
		return replacement;
	}

	private void strikeXiexiangTarget(final XiexiangContext context, final Char target) {
		final Hero hero = context.hero;
		if (context.finished) return;
		hero.belongings.abilityWeapon = this;
		if (!xiexiangCanContinue(hero.isAlive(), hero.paralysed, hero.rooted)) {
			finishXiexiang(context);
			return;
		}
		if (!isXiexiangTarget(hero, target)) {
			context.target = null;
			resolveXiexiangStrike(context);
			return;
		}
		if (!hero.canAttack(target)) {
			finishXiexiang(context);
			return;
		}

		hero.chooseEnemy(target);
		final int strikeCell = hero.pos;
		Callback attack = new Callback() {
			@Override
			public void call() {
				context.strikes++;
				context.targetStrikes++;
				if (context.targetStrikes == 1) context.firstStrikeCell = strikeCell;
				else if (context.targetStrikes == 2) context.secondStrikeCell = strikeCell;
				AttackIndicator.target(target);
				boolean hit = hero.attack(target, 1f, xiexiangDamageBoost(buffedLvl()), Char.INFINITE_ACCURACY);
				if (hit) {
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
					if (!target.isAlive()) onAbilityKill(hero, target);
					Invisibility.dispel();
				} else {
					Invisibility.dispel();
				}
				if (context.strikes < maxXiexiangStrikes()) resolveXiexiangStrike(context);
				else finishXiexiang(context);
			}
		};
		if (context.strikes == 0 && hero.sprite != null) hero.sprite.attack(target.pos, attack);
		else attack.call();
	}

	private void finishXiexiang(XiexiangContext context) {
		if (context.finished) return;
		context.finished = true;
		context.hero.spendAndNext(context.hero.attackDelay());
		afterAbilityUsed(context.hero);
	}

	private void moveHeroWithoutTriggeringTerrain(Hero hero, int destination) {
		if (Dungeon.level.map[hero.pos] == Terrain.OPEN_DOOR) Door.leave(hero.pos);
		hero.pos = destination;
		Dungeon.level.occupyCell(hero);
		Dungeon.observe();
		GameScene.updateFog();
	}

	private int safeXiexiangLandingCell(Hero hero, Char target) {
		if (Dungeon.level.distance(hero.pos, target.pos) <= 1
				&& isXiexiangLegalCell(hero, hero.pos)) return hero.pos;
		int landingCell = -1;
		for (int offset : PathFinder.NEIGHBOURS8) {
			int candidate = target.pos + offset;
			if (!isXiexiangLegalCell(hero, candidate) || !clearXiexiangPath(hero.pos, candidate)
					|| !xiexiangCanAttackFrom(hero, target, candidate)) continue;
			if (landingCell == -1 || Dungeon.level.trueDistance(hero.pos, candidate)
					< Dungeon.level.trueDistance(hero.pos, landingCell)) landingCell = candidate;
		}
		return landingCell;
	}

	private boolean isXiexiangLegalCell(Hero hero, int cell) {
		return Dungeon.level.insideMap(cell) && Dungeon.level.map[cell] != Terrain.CHASM
				&& !Dungeon.level.solid[cell] && (cell == hero.pos || Actor.findChar(cell) == null);
	}

	private boolean xiexiangCanAttackFrom(Hero hero, Char target, int cell) {
		return Dungeon.level.distance(cell, target.pos) <= xiexiangAttackReach(hero)
				&& new Ballistica(cell, target.pos, Ballistica.PROJECTILE).collisionPos == target.pos;
	}

	private boolean clearXiexiangPath(int from, int to) {
		return from == to || new Ballistica(from, to, Ballistica.PROJECTILE).collisionPos == to;
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

	private static final class XiexiangContext {
		final Hero hero;
		Char target;
		int strikes;
		int targetStrikes;
		int firstStrikeCell = -1;
		int secondStrikeCell = -1;
		boolean lockedInPlace;
		boolean movementPending;
		boolean finished;

		XiexiangContext(Hero hero, Char target) {
			this.hero = hero;
			this.target = target;
		}
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
		return 6 + Math.max(0, weaponLevel);
	}

	static int chooseXiexiangFollowupCell(int strikesCompleted, int currentCell,
			int firstStrikeCell, int secondStrikeCell, int[] legalCandidates) {
		int excludedCell = strikesCompleted == 2 ? firstStrikeCell
				: strikesCompleted == 3 ? secondStrikeCell : -1;
		for (int candidate : legalCandidates) {
			if (candidate != excludedCell) return candidate;
		}
		return xiexiangFallbackCell(strikesCompleted, currentCell,
				firstStrikeCell, secondStrikeCell);
	}

	static int xiexiangFallbackCell(int strikesCompleted, int currentCell,
			int firstStrikeCell, int secondStrikeCell) {
		if (strikesCompleted == 2 && firstStrikeCell >= 0) return firstStrikeCell;
		if (strikesCompleted == 3 && secondStrikeCell >= 0) return secondStrikeCell;
		return currentCell;
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
	public java.util.ArrayList<UpgradeAbilityStat> upgradeAbilityStats(int level) {
		java.util.ArrayList<UpgradeAbilityStat> result = new java.util.ArrayList<>();
		result.add(abilityStat(UpgradeAbilityStatType.DAMAGE, upgradeAbilityStat(level)));
		return result;
	}

	@Override
	public String statsInfo() {
		return Messages.get(this, "stats_desc");
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(HIT_COUNT, hitState.hitsSinceReward());
		bundle.put(PRECISION_TARGET, precisionState.targetId);
		bundle.put(PRECISION_COUNT, precisionState.attackCount);
		bundle.put(PRECISION_TIME, precisionState.lastAttackAt);
		bundle.put(NEXT_STAB, nextStab);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		hitState.restore(bundle.getInt(HIT_COUNT));
		precisionState.restore(bundle.getInt(PRECISION_TARGET),
				bundle.getInt(PRECISION_COUNT), bundle.getFloat(PRECISION_TIME));
		nextStab = bundle.getBoolean(NEXT_STAB);
	}

	static class PrecisionState {
		private int targetId = -1;
		private int attackCount;
		private float lastAttackAt;

		float factorFor(int target, float now) {
			if (attackCount == 0 || target != targetId
					|| now < lastAttackAt || now - lastAttackAt > 3f) return 1f;
			return 1f + 0.2f * attackCount;
		}

		void recordAttack(int target, float now) {
			attackCount = factorFor(target, now) == 1f ? 1 : attackCount + 1;
			targetId = target;
			lastAttackAt = now;
		}

		void restore(int target, int count, float time) {
			targetId = target;
			attackCount = Math.max(0, count);
			lastAttackAt = time;
		}
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
