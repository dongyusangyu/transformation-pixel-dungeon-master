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

public class HuntressBoss extends Mob implements PhysicalRangedAttack {

	public static final String SUBLIMATION_TYPE = "HUNTRESS";

	public static final int WARDEN_BOON_INTERVAL = 10;
	public static final int CLOSE_QUARTERS_KNOCKBACK_DISTANCE = 3;
	public static final float FIRE_IMBUE_DURATION = FireImbue.DURATION * 0.3f;

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
	private int normalShots;
	private int galeTarget = -1;
	private int boonCooldown = WARDEN_BOON_INTERVAL;
	private WardenBoon activeBoon;
	private int boonTurns;
	private final TacticalDecision cachedTacticalDecision = new TacticalDecision();
	private boolean meleeAttack;

	private static final String PHASE = "phase";
	private static final String ENCOUNTER_DELAY = "encounter_delay";
	private static final String NORMAL_SHOTS = "normal_shots";
	private static final String GALE_TARGET = "gale_target";
	private static final String BOON_COOLDOWN = "boon_cooldown";
	private static final String ACTIVE_BOON = "active_boon";
	private static final String BOON_TURNS = "boon_turns";

	{
		spriteClass = HuntressBossSprite.class;
		HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 400 : 300;
		EXP = 30;
		defenseSkill = 15;
		viewDistance = 15;
		properties.add(Property.BOSS);
	}

	public Phase phase() {
		return phase;
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
		normalShots = 0;
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

	static TacticalAction tacticalAction(boolean enemyInBossFOV, boolean clearLine,
			int distance, int roll) {
		if (!enemyInBossFOV || !clearLine) {
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

	static int armorForAttack(Phase phase, boolean meleeAttack, boolean galeShot, int armor) {
		return galeShot || (phase == Phase.SNIPER && !meleeAttack) ? 0 : armor;
	}

	@Override
	protected int modifyEnemyArmor(Char enemy, int armor) {
		return armorForAttack(phase, meleeAttack, galeShot, armor);
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
		bundle.put(NORMAL_SHOTS, normalShots);
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
		normalShots = bundle.getInt(NORMAL_SHOTS);
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

		if (galeTarget != -1 && phase == Phase.SNIPER && state == HUNTING) {
			return fireGale();
		}
		return super.act();
	}

	@Override
	public boolean add(Buff buff) {
		if (buff instanceof Burning) {
			if (!super.add(buff)) {
				return false;
			}
			super.remove(buff);
			if (shouldAbsorbBurning(fireAbsorptionRoll())) {
				Buff.affect(this, FireImbue.class).set(FIRE_IMBUE_DURATION);
			}
			return false;
		}
		return super.add(buff);
	}

	protected int fireAbsorptionRoll() {
		return Random.Int(2);
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
		if (phase == Phase.SNIPER && normalShots >= 3) {
			normalShots = 0;
			galeTarget = enemy.pos;
			Ballistica aim = new Ballistica(pos, galeTarget, Ballistica.STOP_SOLID);
			if (sprite != null && sprite.parent != null) {
				for (int cell : aim.subPath(1, aim.dist)) {
					sprite.parent.addToBack(new TargetedCell(cell, 0xFF0000));
				}
			}
			yell(Messages.get(this, "gale_aim"));
			spend(TICK);
			clearTacticalDecision();
			return true;
		}

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

	private void resolveNormalShot(Char shotTarget) {
		meleeAttack = false;
		try {
			galeShot = false;
			if (shotTarget != null && shotTarget.isAlive()
					&& new Ballistica(pos, shotTarget.pos, Ballistica.PROJECTILE).collisionPos == shotTarget.pos) {
				beginPhysicalRangedAttack();
				completePhysicalAttack(shotTarget, attack(shotTarget));
			}
			normalShots++;
			Invisibility.dispel(this);
		} finally {
			galeShot = false;
			clearTacticalDecision();
		}
	}

	private boolean fireGale() {
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
	public int attackProc(Char enemy, int damage) {
		damage = super.attackProc(enemy, damage);

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
				if (Dungeon.level instanceof HuntressBossLevel) {
					((HuntressBossLevel) Dungeon.level).teleportBossAndHero(this);
				}
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
		if (galeShot) {
			return Random.NormalIntRange(20, 28);
		}
		if (phase == Phase.WARDEN) {
			return Random.NormalIntRange(12, 20);
		}
		return Random.NormalIntRange(10, 16);
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
	public void damage(int dmg, Object src) {
		int preHP = HP;
		super.damage(cappedIncomingDamage(dmg), src);
		int damageTaken = preHP - HP;
		LockedFloor lock = Dungeon.hero == null ? null : Dungeon.hero.buff(LockedFloor.class);
		if (damageTaken > 0 && lock != null) {
			lock.addTime(Dungeon.isChallenged(Challenges.STRONGER_BOSSES)
					? damageTaken / 2f : damageTaken);
		}
		if (isAlive() && HP * 2 <= HT && enterWardenPhase()) {
			BossHealthBar.bleed(true);
			yell(Messages.get(this, "warden"));
			if (Dungeon.level instanceof HuntressBossLevel) {
				((HuntressBossLevel) Dungeon.level).onWardenPhase(this);
			}
		}
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
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
			boolean enemyInBossFOV = enemy != null && enemy.invisible == 0
					&& fieldOfView != null && enemy.pos >= 0
					&& enemy.pos < fieldOfView.length && fieldOfView[enemy.pos];
			boolean clearLine = enemyInBossFOV
					&& new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
			int distance = enemy == null || Dungeon.level == null
					? Integer.MAX_VALUE : Dungeon.level.distance(pos, enemy.pos);
			int roll = enemyInBossFOV && clearLine && distance >= 2 && distance <= 4
					? Random.Int(2) : 0;
			action = tacticalAction(enemyInBossFOV, clearLine, distance, roll);
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
		public int attackProc(Char enemy, int damage) {
			damage = super.attackProc(enemy, damage);
			applyDistraction(enemy);
			return damageAfterArmor(damage);
		}

		int damageAfterArmor(int damage) {
			return 1;
		}

		void applyDistraction(Char enemy) {
			Buff.prolong(enemy, Blindness.class, DISTRACTION_DURATION);
			Buff.prolong(enemy, Cripple.class, DISTRACTION_DURATION);
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
		public int attackProc(Char enemy, int damage) {
			Buff.prolong(enemy, Cripple.class, 2f);
			return damage;
		}

		@Override
		public int drRoll() {
			return 0;
		}
	}
}
