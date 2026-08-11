/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.BowFragment;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Mageroyal;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HuntressBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class HuntressBoss extends Mob implements PhysicalRangedAttack {

	public static final String SUBLIMATION_TYPE = "HUNTRESS";

	public static final int WARDEN_BOON_INTERVAL = 10;
	public static final int CLOSE_QUARTERS_KNOCKBACK_DISTANCE = 3;
	public static final float FIRE_IMBUE_DURATION = FireImbue.DURATION * 0.3f;
	public static final float FIRE_ABSORPTION_COOLDOWN = 20f;
	public static final int BASE_NORMAL_SHOT_RANGE = 6;
	public static final int HAWK_RELAY_RADIUS = 4;
	public static final int GALE_INTERVAL = 5;

	@SuppressWarnings("unchecked")
	private static final Class<? extends Plant>[] HARMFUL_PLANTS = new Class[]{
			Blindweed.class,
			Firebloom.class,
			Icecap.class,
			Sorrowmoss.class,
			Stormvine.class
	};

	public enum Phase {
		SNIPER,
		WARDEN
	}

	public enum TacticalAction {
		CHASE,
		MELEE,
		SHOOT,
		SEEK_COVER
	}

	static final class TacticalDecision {

		private Char target;
		private TacticalAction action;

		boolean matches(Char candidate) {
			return target != null && target == candidate;
		}

		TacticalAction actionFor(Char candidate) {
			return matches(candidate) ? action : null;
		}

		void set(Char target, TacticalAction action) {
			this.target = target;
			this.action = action;
		}

		void clear() {
			target = null;
			action = null;
		}
	}

	public enum WardenBoon {
		BLINDWEED,
		EARTHROOT,
		FADELEAF,
		FIREBLOOM,
		ICECAP,
		MAGEROYAL,
		SORROWMOSS,
		STARFLOWER,
		STORMVINE,
		SUNGRASS,
		SWIFTTHISTLE
	}

	private Phase phase = Phase.SNIPER;
	private boolean galeShot;
	private boolean encounterDelay = true;
	private int galeTurnsRemaining = GALE_INTERVAL;
	private int galeTarget = -1;
	private int boonCooldown = WARDEN_BOON_INTERVAL;
	private WardenBoon activeBoon;
	private int boonTurns;
	private final TacticalDecision cachedTacticalDecision = new TacticalDecision();
	private boolean meleeAttack;

	private static final String PHASE = "phase";
	private static final String ENCOUNTER_DELAY = "encounter_delay";
	static final String GALE_TURNS_REMAINING = "gale_turns_remaining";
	private static final String GALE_TARGET = "gale_target";
	private static final String BOON_COOLDOWN = "boon_cooldown";
	private static final String ACTIVE_BOON = "active_boon";
	private static final String BOON_TURNS = "boon_turns";

	{
		HUNTING = new Hunting();
		spriteClass = HuntressBossSprite.class;
		HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 400 : 300;
		EXP = 30;
		defenseSkill = 15;
		viewDistance = 15;
		properties.add(Property.BOSS);
		properties.add(Property.DEMONIC);
        properties.add(Property.UNSLEEP);
	}

	public Phase phase() {
		return phase;
	}

	public int galeTurnsRemaining() {
		return galeTurnsRemaining;
	}

	public void startEncounter() {
		clearTacticalDecision();
		encounterDelay = true;
		state = HUNTING;
		enemy = Dungeon.hero;
		target = Dungeon.hero.pos;
		BossHealthBar.assignBoss(this);
	}

	public boolean enterWardenPhase() {
		if (phase == Phase.WARDEN) {
			return false;
		}
		phase = Phase.WARDEN;
		galeTurnsRemaining = GALE_INTERVAL;
		galeTarget = -1;
		boonCooldown = WARDEN_BOON_INTERVAL;
		meleeAttack = false;
		galeShot = false;
		clearTacticalDecision();
		return true;
	}

	static int cappedIncomingDamage(int damage) {
		return Math.max(0, Math.min(30, damage));
	}

	static boolean withinHawkRelayRadius(int distance, int radius) {
		return distance <= radius;
	}

	static boolean normalShotEligible(boolean enemyInBossFOV, boolean hawkRelayed,
			boolean clearLine, int distance, int shotRange) {
		return clearLine && (hawkRelayed || (enemyInBossFOV && distance <= shotRange));
	}

	static boolean enemySeenForHunting(boolean enemyInBossFOV, boolean relayEligible) {
		return enemyInBossFOV || relayEligible;
	}

	static TacticalAction tacticalAction(boolean enemyInBossFOV, boolean hawkRelayed,
			boolean clearLine, int distance, int shotRange, int roll) {
		if (!enemySeenForHunting(enemyInBossFOV, hawkRelayed)
				|| !normalShotEligible(enemyInBossFOV, hawkRelayed,
				clearLine, distance, shotRange)) {
			return TacticalAction.CHASE;
		}
		if (distance <= 1) {
			return TacticalAction.MELEE;
		}
		if (distance <= 4) {
			return roll == 0 ? TacticalAction.SHOOT : TacticalAction.SEEK_COVER;
		}
		return TacticalAction.SHOOT;
	}

	protected int normalShotRange() {
		return BASE_NORMAL_SHOT_RANGE;
	}

	static int armorForAttack(Phase phase, boolean meleeAttack, boolean galeShot, int armor) {
		return ignoresArmorForAttack(phase, meleeAttack, galeShot) ? 0 : armor;
	}

	static boolean ignoresArmorForAttack(Phase phase, boolean meleeAttack, boolean galeShot) {
		return galeShot || phase == Phase.SNIPER && !meleeAttack;
	}

	@Override
	protected int modifyEnemyArmor(Char enemy, int armor) {
		return armorForAttack(phase, meleeAttack, galeShot, armor);
	}

	@Override
	protected boolean attackIgnoresArmor(Char enemy) {
		return super.attackIgnoresArmor(enemy)
				|| ignoresArmorForAttack(phase, meleeAttack, galeShot);
	}

	static Class<? extends Item> projectileClassFor(boolean gale) {
		return gale ? Dart.class : SpiritBow.SpiritArrow.class;
	}

	static Item projectileFor(boolean gale) {
		if (gale) {
			return new Dart();
		}
		SpiritBow bow = new SpiritBow();
		return bow.new SpiritArrow();
	}

	static boolean shouldKnockBackCloseTarget(boolean adjacent, boolean heroOrAlly, int roll) {
		return adjacent && heroOrAlly && roll == 0;
	}

	static boolean shouldApplyHarmfulPlantEffect(Phase phase, boolean meleeAttack,
			boolean galeShot, int roll) {
		return phase == Phase.WARDEN && !meleeAttack && !galeShot && roll == 0;
	}

	static boolean shouldDeferCustomActions(int paralysed, boolean sleeping,
			boolean fleeing, boolean confused) {
		return paralysed > 0 || sleeping || fleeing || confused;
	}

	static int advanceGaleCountdown(int remaining, boolean actionable) {
		return actionable ? Math.max(0, remaining - 1) : remaining;
	}

	static boolean shouldAimGale(Phase phase, int remaining, boolean hasTarget) {
		return phase == Phase.SNIPER && remaining == 0 && hasTarget;
	}

	static boolean isGaleTargetEligible(boolean alive, boolean hostile,
			boolean inBossFOV, boolean invisible) {
		return alive && hostile && inBossFOV && !invisible;
	}

	static Char pickGaleTarget(List<Char> targets, int index) {
		if (targets == null || index < 0 || index >= targets.size()) {
			return null;
		}
		return targets.get(index);
	}

	static int restoredGaleTurns(Bundle bundle) {
		return bundle.contains(GALE_TURNS_REMAINING)
				? bundle.getInt(GALE_TURNS_REMAINING) : GALE_INTERVAL;
	}

	static boolean shouldAbsorbBurning(int roll) {
		return roll == 0;
	}

	public static WardenBoon boonForPlant(Plant plant) {
		if (plant instanceof Blindweed) return WardenBoon.BLINDWEED;
		if (plant instanceof Earthroot) return WardenBoon.EARTHROOT;
		if (plant instanceof Fadeleaf) return WardenBoon.FADELEAF;
		if (plant instanceof Firebloom) return WardenBoon.FIREBLOOM;
		if (plant instanceof Icecap) return WardenBoon.ICECAP;
		if (plant instanceof Mageroyal) return WardenBoon.MAGEROYAL;
		if (plant instanceof Sorrowmoss) return WardenBoon.SORROWMOSS;
		if (plant instanceof Starflower) return WardenBoon.STARFLOWER;
		if (plant instanceof Stormvine) return WardenBoon.STORMVINE;
		if (plant instanceof Sungrass) return WardenBoon.SUNGRASS;
		if (plant instanceof Swiftthistle) return WardenBoon.SWIFTTHISTLE;
		return null;
	}

	static Class<? extends Plant>[] harmfulPlantPool() {
		return HARMFUL_PLANTS.clone();
	}

	public static int hawksSpawnedAtFightStart() {
		return 1;
	}

	public static int hawksSpawnedAtWardenTransition() {
		return 1;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PHASE, phase);
		bundle.put(ENCOUNTER_DELAY, encounterDelay);
		bundle.put(GALE_TURNS_REMAINING, galeTurnsRemaining);
		bundle.put(GALE_TARGET, galeTarget);
		bundle.put(BOON_COOLDOWN, boonCooldown);
		if (activeBoon != null) {
			bundle.put(ACTIVE_BOON, activeBoon);
		}
		bundle.put(BOON_TURNS, boonTurns);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(PHASE)) {
			phase = bundle.getEnum(PHASE, Phase.class);
		}
		encounterDelay = bundle.getBoolean(ENCOUNTER_DELAY);
		galeTurnsRemaining = restoredGaleTurns(bundle);
		galeTarget = bundle.contains(GALE_TARGET) ? bundle.getInt(GALE_TARGET) : -1;
		boonCooldown = bundle.contains(BOON_COOLDOWN)
				? bundle.getInt(BOON_COOLDOWN) : WARDEN_BOON_INTERVAL;
		if (bundle.contains(ACTIVE_BOON)) {
			activeBoon = bundle.getEnum(ACTIVE_BOON, WardenBoon.class);
		}
		boonTurns = bundle.getInt(BOON_TURNS);
		BossHealthBar.assignBoss(this);
		BossHealthBar.bleed(phase == Phase.WARDEN);
	}

	@Override
	protected boolean act() {
		if (shouldDeferCustomActions(paralysed, state == SLEEPING, state == FLEEING,
				buff(Feint.AfterImage.FeintConfusion.class) != null)) {
			clearTacticalDecision();
			return super.act();
		}

		if (encounterDelay) {
			encounterDelay = false;
			spend(TICK);
			return true;
		}

		if (phase == Phase.WARDEN) {
			advanceBoon();
			if (--boonCooldown <= 0) {
				WardenBoon timedBoon = Random.element(WardenBoon.values());
				grantBoon(timedBoon);
				boonCooldown = activeBoon == WardenBoon.STARFLOWER
						? WARDEN_BOON_INTERVAL - 2 : WARDEN_BOON_INTERVAL;
				if (timedBoon == WardenBoon.FADELEAF) {
					spend(TICK);
					return true;
				}
			}
		}

		if (phase == Phase.SNIPER) {
			if (galeTarget != -1) {
				refreshGaleFieldOfView();
				Char target = Actor.findChar(galeTarget);
				if (isEligibleGaleTarget(target)) {
					return fireGale();
				}
				galeTarget = -1;
			}

			galeTurnsRemaining = advanceGaleCountdown(galeTurnsRemaining, true);
			ArrayList<Char> visibleTargets = visibleGaleTargets();
			if (shouldAimGale(phase, galeTurnsRemaining, !visibleTargets.isEmpty())) {
				Char target = pickGaleTarget(visibleTargets, Random.Int(visibleTargets.size()));
				return aimGaleAt(target);
			}
		}
		return super.act();
	}

	@Override
	public boolean add(Buff buff) {
		if (buff instanceof Burning && buff(FireAbsorptionCooldown.class) == null) {
			if (!super.add(buff)) {
				return false;
			}
			super.remove(buff);
			Buff.affect(this, FireAbsorptionCooldown.class)
					.set(FIRE_ABSORPTION_COOLDOWN);
			if (shouldAbsorbBurning(fireAbsorptionRoll())) {
				Buff.affect(this, FireImbue.class).set(5);
			}
			return false;
		}
		return super.add(buff);
	}

	protected int fireAbsorptionRoll() {
		return Random.Int(2);
	}

	public static class FireAbsorptionCooldown extends FlavourBuff {

		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		public void set(float duration) {
			spend(duration);
		}
	}

	@Override
	public boolean isImmune(Class effect) {
		// FireImbue normally rejects Burning before it can reach add().
		FireImbue absorbedFireImbue = buff(FireImbue.class);
		if (Burning.class.isAssignableFrom(effect)
				&& buff(FireAbsorptionCooldown.class) != null
				&& absorbedFireImbue != null) {
			return hasBurningImmunityOtherThan(effect, absorbedFireImbue);
		}
		return super.isImmune(effect);
	}

	private boolean hasBurningImmunityOtherThan(Class effect, Buff ignoredImmunitySource) {
		if (hasAssignableImmunity(immunities, effect)) {
			return true;
		}
		for (Property property : properties()) {
			if (hasAssignableImmunity(property.immunities(), effect)) {
				return true;
			}
		}
		for (Buff buff : buffs()) {
			if (buff != ignoredImmunitySource
					&& hasAssignableImmunity(buff.immunities(), effect)) {
				return true;
			}
		}
		return glyphLevel(Brimstone.class) >= 0;
	}

	private static boolean hasAssignableImmunity(Iterable<Class> immunities, Class effect) {
		for (Class immunity : immunities) {
			if (immunity.isAssignableFrom(effect)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Char chooseEnemy() {
		boolean canUseHawkRelay = state == HUNTING || state == WANDERING;
		Char chosen = super.chooseEnemy();
		if (!canUseHawkRelay) {
			return chosen;
		}
		if (bossCanSee(chosen) || hasHawkRelay(chosen)) {
			return chosen;
		}
		if (bossCanSee(enemy) || hasHawkRelay(enemy)) {
			return enemy;
		}
		ArrayList<Char> relayTargets = hawkRelayTargets();
		if (!relayTargets.isEmpty()) {
			state = HUNTING;
			return Random.element(relayTargets);
		}
		return chosen;
	}

	protected boolean bossCanSee(Char target) {
		return target != null && target.isAlive() && target.invisible <= 0
				&& fieldOfView != null && target.pos >= 0
				&& target.pos < fieldOfView.length && fieldOfView[target.pos];
	}

	protected boolean isHawkRelayed(Char target) {
		if (target == null || Dungeon.level == null || target.pos < 0) {
			return false;
		}
		for (Char candidate : Actor.chars()) {
			if (candidate instanceof DistractingHawk && candidate.isAlive()
					&& candidate.pos >= 0
					&& withinHawkRelayRadius(
						Dungeon.level.distance(candidate.pos, target.pos), HAWK_RELAY_RADIUS)) {
				return true;
			}
		}
		return false;
	}

	protected boolean hasHawkRelay(Char target) {
		return isEligibleRelayTarget(target) && isHawkRelayed(target);
	}

	protected ArrayList<Char> hawkRelayTargets() {
		ArrayList<Char> targets = new ArrayList<>();
		for (Char candidate : Actor.chars()) {
			if (hasHawkRelay(candidate)) {
				targets.add(candidate);
			}
		}
		return targets;
	}

	private boolean isEligibleRelayTarget(Char target) {
		return target != null && target != this && target.isAlive() && target.isActive()
				&& target.invisible <= 0 && Actor.chars().contains(target)
				&& Actor.isHostile(this, target) && !isCharmedBy(target);
	}

	private boolean hasClearNormalShotLine(Char target) {
		return target != null && Dungeon.level != null && target.pos >= 0
				&& new Ballistica(pos, target.pos,
				Ballistica.PROJECTILE).collisionPos == target.pos;
	}

	private void refreshGaleFieldOfView() {
		if (Dungeon.level == null) {
			return;
		}
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView(this, fieldOfView);
	}

	private boolean isEligibleGaleTarget(Char target) {
		boolean inBossFOV = target != null && fieldOfView != null && target.pos >= 0
				&& target.pos < fieldOfView.length && fieldOfView[target.pos];
		return target != null && target != this && isGaleTargetEligible(
				target.isAlive(), Actor.isHostile(this, target) && !isCharmedBy(target),
				inBossFOV, target.invisible > 0);
	}

	private ArrayList<Char> visibleGaleTargets() {
		ArrayList<Char> targets = new ArrayList<>();
		if (Dungeon.level == null) {
			return targets;
		}
		refreshGaleFieldOfView();
		for (Char candidate : Actor.chars()) {
			if (isEligibleGaleTarget(candidate)) {
				targets.add(candidate);
			}
		}
		return targets;
	}

	protected class Hunting extends Mob.Hunting {

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			return super.act(enemySeenForHunting(
					enemyInFOV, hasHawkRelay(enemy)), justAlerted);
		}
	}

	@Override
	protected boolean canAttack(Char enemy) {
		if (galeTarget != -1) {
			return true;
		}
		TacticalAction action = tacticalActionFor(enemy);
		return action == TacticalAction.MELEE || action == TacticalAction.SHOOT;
	}

	@Override
	public boolean canRangedAttack(Char enemy) {
		return galeTarget == -1 && tacticalActionFor(enemy) == TacticalAction.SHOOT;
	}

	@Override
	public boolean canMeleeAttack(Char enemy) {
		return tacticalActionFor(enemy) == TacticalAction.MELEE;
	}

	@Override
	protected boolean doAttack(final Char enemy) {
		TacticalAction action = cachedTacticalDecision.actionFor(enemy);
		if (action == null) {
			action = tacticalActionFor(enemy);
		}
		if (action == TacticalAction.MELEE) {
			meleeAttack = true;
			boolean awaitingAnimation = false;
			try {
				boolean completed = super.doAttack(enemy);
				awaitingAnimation = !completed;
				return completed;
			} finally {
				if (!awaitingAnimation) {
					meleeAttack = false;
					clearTacticalDecision();
				}
			}
		}

		return doRangedAttack(enemy);
	}

	@Override
	public boolean doRangedAttack(final Char enemy) {
		final int targetCell = enemy.pos;
		spend(attackDelay());
		if (sprite != null && sprite.parent != null
				&& (sprite.visible || enemy.sprite.visible)) {
			sprite.zap(targetCell, new Callback() {
				@Override
				public void call() {
					launchProjectile(targetCell, false, new Callback() {
						@Override
						public void call() {
							resolveNormalShot(enemy);
							next();
						}
					});
				}
			});
			return false;
		}

		resolveNormalShot(enemy);
		return true;
	}

	protected void announceGaleAim() {
		yell(Messages.get(this, "gale_aim"));
	}

	private boolean aimGaleAt(Char target) {
		galeTarget = target.pos;
		Ballistica aim = new Ballistica(pos, galeTarget, Ballistica.STOP_SOLID);
		if (sprite != null && sprite.parent != null) {
			for (int cell : aim.subPath(1, aim.dist)) {
				sprite.parent.addToBack(new TargetedCell(cell, 0xFF0000));
			}
		}
		announceGaleAim();
		spend(TICK);
		clearTacticalDecision();
		return true;
	}

	private void resolveNormalShot(Char shotTarget) {
		meleeAttack = false;
		try {
			galeShot = false;
			if (shotTarget != null && shotTarget.isAlive()
					&& new Ballistica(pos, shotTarget.pos, Ballistica.PROJECTILE).collisionPos == shotTarget.pos) {
				beginPhysicalRangedAttack();
				completePhysicalAttack(shotTarget, attack(shotTarget));
			}
			Invisibility.dispel(this);
		} finally {
			galeShot = false;
			clearTacticalDecision();
		}
	}

	private boolean fireGale() {
		galeTurnsRemaining = GALE_INTERVAL;
		final Ballistica gale = new Ballistica(pos, galeTarget, Ballistica.STOP_SOLID);
		final int destination = gale.collisionPos;
		galeTarget = -1;
		spend(attackDelay());

		if (sprite != null && sprite.parent != null) {
			sprite.zap(destination, new Callback() {
				@Override
				public void call() {
					launchProjectile(destination, true, new Callback() {
						@Override
						public void call() {
							resolveGale(gale);
							next();
						}
					});
				}
			});
			return false;
		}

		resolveGale(gale);
		return true;
	}

	private void launchProjectile(int destination, boolean gale, Callback callback) {
		if (sprite == null || sprite.parent == null) {
			callback.call();
			return;
		}
		((MissileSprite) sprite.parent.recycle(MissileSprite.class))
				.reset(sprite, destination, projectileFor(gale), callback);
	}

	private void resolveGale(Ballistica gale) {
		meleeAttack = false;
		galeShot = true;
		try {
			for (int cell : gale.subPath(1, gale.dist)) {
				Char ch = Actor.findChar(cell);
				if (ch != null && ch != this && ch.alignment != alignment) {
					beginPhysicalRangedAttack();
					completePhysicalAttack(ch, attack(ch));
					Buff.prolong(ch, Cripple.class, 2f);
				}
			}
			Invisibility.dispel(this);
		} finally {
			galeShot = false;
			clearTacticalDecision();
		}
	}

	@Override
	public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
		damage = super.attackProc(enemy, damage, damageTags);

		boolean adjacent = Dungeon.level != null && Dungeon.level.adjacent(pos, enemy.pos);
		boolean heroOrAlly = enemy == Dungeon.hero || enemy.alignment == Alignment.ALLY;
		if (adjacent && heroOrAlly) {
			if (shouldKnockBackCloseTarget(true, true, Random.Int(3))) {
				int oppositeAdjacent = enemy.pos + (enemy.pos - pos);
				Ballistica trajectory = new Ballistica(enemy.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
				WandOfBlastWave.throwChar(enemy, trajectory, CLOSE_QUARTERS_KNOCKBACK_DISTANCE,
						false, false, this);
				if (enemy == Dungeon.hero) {
					Dungeon.hero.interrupt();
				}
			}
		}

		if (phase == Phase.WARDEN && !meleeAttack && !galeShot) {
			int plantEffectRoll = Random.Int(3);
			if (shouldApplyHarmfulPlantEffect(
					phase, meleeAttack, galeShot, plantEffectRoll)) {
				applyHarmfulPlantEffect(enemy, Random.Int(HARMFUL_PLANTS.length));
			}
		}

		if (phase == Phase.WARDEN && activeBoon != null) {
			switch (activeBoon) {
				case FIREBLOOM:
					Buff.affect(enemy, Burning.class).reignite(enemy, 4f);
					break;
				case ICECAP:
					Buff.prolong(enemy, Chill.class, 4f);
					break;
				case SORROWMOSS:
					Buff.affect(enemy, Poison.class).set(5f);
					break;
				default:
					break;
			}
		}
		return damage;
	}

	private void applyHarmfulPlantEffect(Char enemy, int effect) {
		switch (effect) {
			case 0:
				Buff.prolong(enemy, Blindness.class, 3f);
				Buff.prolong(enemy, Cripple.class, 2f);
				break;
			case 1:
				Buff.affect(enemy, Burning.class).reignite(enemy, 4f);
				break;
			case 2:
				Buff.prolong(enemy, Chill.class, 4f);
				break;
			case 3:
				Buff.affect(enemy, Poison.class).set(5f);
				break;
			default:
				Buff.prolong(enemy, Vertigo.class, 4f);
				break;
		}
	}

	public void grantBoon(WardenBoon boon) {
		if (boon == null) {
			return;
		}
		clearActiveBoon();
		activeBoon = boon;
		boonTurns = 5;
		switch (boon) {
			case BLINDWEED:
				boonTurns = 6;
				Buff.affect(this, Invisibility.class, 6f);
				clearEnemy();
				break;
			case FADELEAF:
				boonTurns = 0;
				triggerFadeleafBoon();
				activeBoon = null;
				break;
			case MAGEROYAL:
				for (Buff buff : new ArrayList<>(buffs())) {
					if (buff.type == Buff.buffType.NEGATIVE) {
						buff.detach();
					}
				}
				Buff.affect(this, BlobImmunity.class, 5f);
				break;
			case STARFLOWER:
				boonTurns = 6;
				Buff.affect(this, Bless.class, 6f);
				boonCooldown = Math.max(1, boonCooldown - 2);
				break;
			case STORMVINE:
				Buff.affect(this, Levitation.class, 5f);
				break;
			case SWIFTTHISTLE:
				boonTurns = 2;
				Buff.affect(this, Haste.class, 2f);
				break;
			default:
				break;
		}
		if (activeBoon != null) {
			yell(Messages.get(this, "boon_" + boon.name().toLowerCase()));
		}
	}

	private void advanceBoon() {
		if (activeBoon == null || boonTurns <= 0) {
			return;
		}
		if (activeBoon == WardenBoon.SUNGRASS && HP < HT) {
			heal(Math.max(1, Math.round(HT * 0.02f)));
		}
		if (--boonTurns <= 0) {
			clearActiveBoon();
		}
	}

	private void clearActiveBoon() {
		if (activeBoon == null) {
			return;
		}
		Class<? extends Buff> buffClass = null;
		switch (activeBoon) {
			case BLINDWEED: buffClass = Invisibility.class; break;
			case MAGEROYAL: buffClass = BlobImmunity.class; break;
			case STARFLOWER: buffClass = Bless.class; break;
			case STORMVINE: buffClass = Levitation.class; break;
			case SWIFTTHISTLE: buffClass = Haste.class; break;
			default: break;
		}
		if (buffClass != null && buff(buffClass) != null) {
			buff(buffClass).detach();
		}
		activeBoon = null;
		boonTurns = 0;
	}

	@Override
	public int damageRoll() {
		int[] range = damageRange(phase, meleeAttack, galeShot);
		return Random.NormalIntRange(range[0], range[1]);
	}

	static int[] damageRange(Phase phase, boolean meleeAttack, boolean galeShot) {
		if (galeShot) {
			return new int[]{20, 28};
		}
		if (phase == Phase.SNIPER) {
			return meleeAttack ? new int[]{10, 16} : new int[]{6, 14};
		}
		return meleeAttack ? new int[]{12, 20} : new int[]{10, 16};
	}

	@Override
	public int attackSkill(Char target) {
		return 25;
	}

	@Override
	public int drRoll() {
		int dr = super.drRoll() + Random.NormalIntRange(0, 6);
		if (phase == Phase.WARDEN && activeBoon == WardenBoon.EARTHROOT) {
			dr += Random.NormalIntRange(6, 10);
		}
		return dr;
	}

	@Override
	public void damage(int dmg, Object src, DamageTag... damageTags) {
		int preHP = HP;
		super.damage(cappedIncomingDamage(dmg), src, damageTags);
		int damageTaken = preHP - HP;
		LockedFloor lock = Dungeon.hero == null ? null : Dungeon.hero.buff(LockedFloor.class);
		if (damageTaken > 0 && lock != null) {
			lock.addTime(Dungeon.isChallenged(Challenges.STRONGER_BOSSES)
					? damageTaken / 2f : damageTaken);
		}
		if (isAlive() && HP * 2 <= HT && enterWardenPhase()) {
			BossHealthBar.bleed(true);
			yell(Messages.get(this, "warden"));
			onWardenPhaseStarted();
		}
	}

	protected void onWardenPhaseStarted() {
		if (Dungeon.level instanceof HuntressBossLevel) {
			((HuntressBossLevel) Dungeon.level).onWardenPhase(this);
		}
	}

	protected void triggerFadeleafBoon() {
		if (Dungeon.level instanceof HuntressBossLevel) {
			((HuntressBossLevel) Dungeon.level).teleportBossAndHero(this);
		}
	}

	protected boolean usesCampaignDeathEffects() {
		return true;
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		if (!usesCampaignDeathEffects()) {
			return;
		}
		GameScene.bossSlain();
		if (Dungeon.level instanceof HuntressBossLevel) {
			((HuntressBossLevel) Dungeon.level).onBossDefeated();
		} else {
			Dungeon.level.unseal();
		}

		Dungeon.level.drop(new BowFragment(), pos).sprite.drop();
		int shards = Random.chances(new float[]{0, 0, 6, 3, 1});
		for (int i = 0; i < shards; i++) {
			int dropPos = pos;
			for (int tries = 0; tries < 8; tries++) {
				int candidate = pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
				if (Dungeon.level.insideMap(candidate) && Dungeon.level.passable[candidate]) {
					dropPos = candidate;
					break;
				}
			}
			Dungeon.level.drop(Generator.random(Generator.Category.SEED), dropPos).sprite.drop(pos);
		}
		if (!Statistics.subLimation[2]) {
			Dungeon.level.drop(sublimationDrop(), pos).sprite.drop();
			Statistics.subLimation[2] = true;
		}

		Badges.validateBossSlain();
		if (Statistics.qualifiedForBossChallengeBadge) {
			Badges.validateBossChallengeCompleted();
		}
		Statistics.bossScores[2] += 3000;
		LloydsBeacon beacon = Dungeon.hero.belongings.getItem(LloydsBeacon.class);
		if (beacon != null) {
			beacon.upgrade();
		}
		yell(Messages.get(this, "defeated"));
	}

	static ScrollOfSublimation sublimationDrop() {
		return new ScrollOfSublimation().type(SUBLIMATION_TYPE);
	}

	@Override
	protected boolean getCloser(int target) {
		try {
			TacticalAction action = state == HUNTING && enemy != null
					&& this.target == enemy.pos && target == this.target
					? cachedTacticalDecision.actionFor(enemy) : null;
			if (action == TacticalAction.SEEK_COVER) {
				int coverCell = nearestCoverCell();
				if (coverCell != -1 && super.getCloser(coverCell)) {
					return true;
				}
			}
			return super.getCloser(target);
		} finally {
			clearTacticalDecision();
		}
	}

	private TacticalAction tacticalActionFor(Char enemy) {
		TacticalAction action = cachedTacticalDecision.actionFor(enemy);
		if (action == null) {
			boolean enemyInBossFOV = bossCanSee(enemy);
			boolean hawkRelayed = hasHawkRelay(enemy);
			boolean clearLine = hasClearNormalShotLine(enemy);
			int distance = enemy == null || Dungeon.level == null
					? Integer.MAX_VALUE : Dungeon.level.distance(pos, enemy.pos);
			int shotRange = normalShotRange();
			boolean shotEligible = normalShotEligible(enemyInBossFOV, hawkRelayed,
					clearLine, distance, shotRange);
			int roll = shotEligible && distance >= 2 && distance <= 4 ? Random.Int(2) : 0;
			action = tacticalAction(enemyInBossFOV, hawkRelayed, clearLine,
					distance, shotRange, roll);
			cachedTacticalDecision.set(enemy, action);
		}
		return action;
	}

	private void clearTacticalDecision() {
		cachedTacticalDecision.clear();
	}

	private int nearestCoverCell() {
		if (Dungeon.level == null || Dungeon.level.heroFOV == null) {
			return -1;
		}
		int bestCell = -1;
		int bestPathLength = Integer.MAX_VALUE;
		PathFinder.buildDistanceMap(pos, Dungeon.findPassable(
				this, Dungeon.level.passable, fieldOfView, true));
		for (int cell = 0; cell < Dungeon.level.length(); cell++) {
			if (cell == pos || Dungeon.level.heroFOV[cell]
					|| !Dungeon.level.insideMap(cell)
					|| !Dungeon.level.passable[cell]
					|| Actor.findChar(cell) != null
					|| Char.hasProp(this, Char.Property.LARGE)
					&& !Dungeon.level.openSpace[cell]) {
				continue;
			}
			if (PathFinder.distance[cell] < bestPathLength) {
				bestCell = cell;
				bestPathLength = PathFinder.distance[cell];
			}
		}
		return bestCell;
	}

	@Override
	public void onAttackComplete() {
		if (!meleeAttack) {
			super.onAttackComplete();
			return;
		}
		try {
			super.onAttackComplete();
		} finally {
			meleeAttack = false;
			clearTacticalDecision();
		}
	}

	@Override
	public boolean reset() {
		return false;
	}

	public static class DistractingHawk extends Mob {

		static final float DISTRACTION_DURATION = 2f;
		static final int RETREAT_MIN_DISTANCE = 2;
		static final int RETREAT_MAX_DISTANCE = 4;

		private static final String HAWK_MODE = "hawk_mode";
		private static final String RETREAT_TARGET_ID = "retreat_target_id";

		public enum HawkMode {
			SEEKING,
			RETREATING,
			WAITING
		}

		private HawkMode hawkMode = HawkMode.SEEKING;
		private int retreatTargetId = -1;
		private final Mob.Hunting baseHunting = new Mob.Hunting();

		static boolean eligibleTarget(boolean alive, boolean hostile, boolean visible,
				boolean invisible, boolean crippled) {
			return alive && hostile && visible && !invisible && !crippled;
		}

		static boolean preferTarget(int distance, int id, int bestDistance, int bestId) {
			return distance < bestDistance
					|| distance == bestDistance && (bestId < 0 || id < bestId);
		}

		static HawkMode modeAfterHit() {
			return HawkMode.RETREATING;
		}

		static boolean shouldRetreat(boolean validTarget, int distance) {
			return validTarget && distance < RETREAT_MIN_DISTANCE;
		}

		static boolean inRetreatBand(int distance) {
			return distance >= RETREAT_MIN_DISTANCE && distance <= RETREAT_MAX_DISTANCE;
		}

		static HawkMode modeAfterRetreat(boolean hasEligibleTarget) {
			return hasEligibleTarget ? HawkMode.SEEKING : HawkMode.WAITING;
		}

		@SuppressWarnings("unchecked")
		static Class<? extends Buff>[] distractionEffects() {
			return new Class[]{Blindness.class, Cripple.class};
		}

		{
			spriteClass = SpiritHawk.HawkSprite.class;
			HP = HT = 8;
			EXP = 0;
			defenseSkill = 18;
			viewDistance = 8;
			baseSpeed = 1.5f;
			flying = true;
			alignment = Alignment.ENEMY;
			properties.add(Property.BOSS_MINION);
			HUNTING = new Hunting();
		}

		public HawkMode hawkMode() {
			return hawkMode;
		}

		public int retreatTargetId() {
			return retreatTargetId;
		}

		public void beginRetreatFrom(Char target) {
			if (shouldDeferHawkModeLogic()) {
				return;
			}
			retreatTargetId = target == null ? -1 : target.id();
			hawkMode = target == null ? HawkMode.SEEKING : modeAfterHit();
		}

		private boolean shouldDeferHawkModeLogic() {
			return buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok.class)
					!= null;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(HAWK_MODE, hawkMode);
			bundle.put(RETREAT_TARGET_ID, retreatTargetId);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			hawkMode = bundle.contains(HAWK_MODE)
					? bundle.getEnum(HAWK_MODE, HawkMode.class) : HawkMode.SEEKING;
			retreatTargetId = bundle.contains(RETREAT_TARGET_ID)
					? bundle.getInt(RETREAT_TARGET_ID) : -1;
		}

		@Override
		public int damageRoll() {
			return 1;
		}

		@Override
		public int attackSkill(Char target) {
			return 18;
		}

		@Override
		public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
			damage = baseAttackProc(enemy, damage, damageTags);
			applyDistraction(enemy);
			beginRetreatFrom(enemy);
			return damageAfterArmor(damage);
		}

		protected int baseAttackProc(Char enemy, int damage, DamageTag... damageTags) {
			return super.attackProc(enemy, damage, damageTags);
		}

		int damageAfterArmor(int damage) {
			return 1;
		}

		void applyDistraction(Char enemy) {
			Buff.prolong(enemy, Blindness.class, DISTRACTION_DURATION);
			Buff.prolong(enemy, Cripple.class, DISTRACTION_DURATION);
		}

		private boolean isEligibleTarget(Char candidate) {
			return candidate != null
					&& candidate != this
					&& candidate.pos >= 0
					&& fieldOfView != null
					&& candidate.pos < fieldOfView.length
					&& eligibleTarget(candidate.isAlive(), Actor.isHostile(this, candidate),
							fieldOfView[candidate.pos], candidate.invisible > 0,
							candidate.buff(Cripple.class) != null)
					&& !isCharmedBy(candidate);
		}

		private Char nearestEligibleTarget() {
			Char best = null;
			int bestDistance = Integer.MAX_VALUE;
			int bestId = -1;
			for (Char candidate : Actor.chars()) {
				if (!isEligibleTarget(candidate)) {
					continue;
				}
				int distance = Dungeon.level.distance(pos, candidate.pos);
				int id = candidate.id();
				if (preferTarget(distance, id, bestDistance, bestId)) {
					best = candidate;
					bestDistance = distance;
					bestId = id;
				}
			}
			return best;
		}

		private Char nearestVisibleHostileReference() {
			Char best = null;
			int bestDistance = Integer.MAX_VALUE;
			int bestId = -1;
			for (Char candidate : Actor.chars()) {
				if (candidate == this || !candidate.isAlive()
						|| !Actor.isHostile(this, candidate)
						|| candidate.pos < 0 || fieldOfView == null
						|| candidate.pos >= fieldOfView.length
						|| !fieldOfView[candidate.pos] || candidate.invisible > 0
						|| isCharmedBy(candidate)) {
					continue;
				}
				int distance = Dungeon.level.distance(pos, candidate.pos);
				int id = candidate.id();
				if (preferTarget(distance, id, bestDistance, bestId)) {
					best = candidate;
					bestDistance = distance;
					bestId = id;
				}
			}
			return best;
		}

		private Char validRetreatTarget() {
			Char target = Actor.findCharById(retreatTargetId);
			return target != null && target.isAlive() && Actor.chars().contains(target)
					&& Actor.isHostile(this, target) ? target : null;
		}

		private boolean actSeeking(boolean justAlerted) {
			Char next = nearestEligibleTarget();
			if (next == null) {
				hawkMode = HawkMode.WAITING;
				return actWaiting(justAlerted);
			}
			enemy = next;
			target = next.pos;
			recentlyAttackedBy.clear();
			return baseHunting.act(true, justAlerted);
		}

		private boolean actRetreating(boolean justAlerted) {
			Char retreatTarget = validRetreatTarget();
			boolean valid = retreatTarget != null;
			int distance = valid ? Dungeon.level.distance(pos, retreatTarget.pos)
					: Integer.MAX_VALUE;
			if (shouldRetreat(valid, distance)) {
				if (distance == 1) {
					int oldPos = pos;
					if (getFurther(retreatTarget.pos)) {
						spend(1 / speed());
						return moveSprite(oldPos, pos);
					}
				}
				spend(TICK);
				return true;
			}

			retreatTargetId = -1;
			Char next = nearestEligibleTarget();
			hawkMode = modeAfterRetreat(next != null);
			if (next != null) {
				return actSeeking(justAlerted);
			}
			return actWaiting(justAlerted);
		}

		private boolean actWaiting(boolean justAlerted) {
			Char next = nearestEligibleTarget();
			if (next != null) {
				hawkMode = HawkMode.SEEKING;
				return actSeeking(justAlerted);
			}

			Char reference = nearestVisibleHostileReference();
			if (reference == null) {
				enemy = null;
				enemySeen = false;
				state = WANDERING;
				target = ((Mob.Wandering) WANDERING).randomDestination();
				spend(TICK);
				return true;
			}

			enemy = reference;
			enemySeen = true;
			target = reference.pos;
			int orbitCell = chooseWaitingOrbitCell(reference);
			if (orbitCell != -1) {
				int oldPos = pos;
				move(orbitCell);
				spend(1 / speed());
				return moveSprite(oldPos, pos);
			}
			spend(TICK);
			return true;
		}

		private int chooseWaitingOrbitCell(Char reference) {
			int bestCell = -1;
			int bestDistanceFromThree = Integer.MAX_VALUE;
			for (int offset : PathFinder.NEIGHBOURS8) {
				int cell = pos + offset;
				if (!Dungeon.level.insideMap(cell) || !Dungeon.level.passable[cell]
						|| Actor.findChar(cell) != null) {
					continue;
				}
				int distance = Dungeon.level.distance(cell, reference.pos);
				if (!inRetreatBand(distance)) {
					continue;
				}
				int distanceFromThree = Math.abs(distance - 3);
				if (distanceFromThree < bestDistanceFromThree
						|| distanceFromThree == bestDistanceFromThree
						&& (bestCell == -1 || cell < bestCell)) {
					bestCell = cell;
					bestDistanceFromThree = distanceFromThree;
				}
			}
			return bestCell;
		}

		private class Hunting extends Mob.Hunting {

			@Override
			public boolean act(boolean enemyInFOV, boolean justAlerted) {
				if (shouldDeferHawkModeLogic()) {
					return baseHunting.act(enemyInFOV, justAlerted);
				}
				if (hawkMode == HawkMode.RETREATING) {
					return actRetreating(justAlerted);
				}
				if (hawkMode == HawkMode.WAITING) {
					return actWaiting(justAlerted);
				}
				return actSeeking(justAlerted);
			}
		}

		@Override
		public int drRoll() {
			return 0;
		}
	}

	public static class HuntressTentacle extends RotLasher {

		{
			HP = HT = 20;
			EXP = 0;
			defenseSkill = 0;
			loot = null;
			lootChance = 0;
			properties.add(Property.BOSS_MINION);
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange(1, 3);
		}

		@Override
		public int attackSkill(Char target) {
			return 15;
		}

		@Override
		public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
			Buff.prolong(enemy, Cripple.class, 2f);
			return damage;
		}

		@Override
		public int drRoll() {
			return 0;
		}
	}
}
