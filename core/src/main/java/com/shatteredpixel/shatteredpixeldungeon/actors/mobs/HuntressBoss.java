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

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
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
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.BowFragment;
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
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HuntressBoss extends Mob implements PhysicalRangedAttack {

	public static final String SUBLIMATION_TYPE = "HUNTRESS";

	public static final float FIRE_IMBUE_DURATION = FireImbue.DURATION * 0.3f;
	public static final float FIRE_ABSORPTION_COOLDOWN = 20f;
	public static final int BASE_NORMAL_SHOT_RANGE = 6;
	public static final int HAWK_RELAY_RADIUS = 4;
	public static final int GALE_INTERVAL = 5;
	public static final int GALE_RECOVERY_TURNS = 1;
	public static final int ESCAPE_COOLDOWN = 4;
	public static final int PLANT_HUNT_GRACE = 3;
	public static final int PLANT_HUNT_DURATION = 5;
	public static final float NATURE_HUNT_MOVE_MULTIPLIER = 2f;
	public static final float NATURE_HUNT_ATTACK_DELAY_MULTIPLIER = 0.75f;
	public static final int NATURE_HUNT_PLANT_CHANCE_DENOMINATOR = 4;
	public static final int MAX_FADELEAF_ESCAPES = 3;

	public enum Phase {
		SNIPER,
		WARDEN
	}

	public enum CombatStep {
		SHOOT,
		MOVE
	}

	public enum GaleState {
		HUNTING,
		READY,
		AIMED,
		RECOVERING
	}

	public enum PlantHuntState {
		GRACE,
		SELECTED,
		BOON_ACTIVE,
		LOST_TRACK,
		NO_PLANTS
	}

	public enum TacticalAction {
		CHASE,
		MELEE,
		SHOOT,
		SEEK_COVER,
		SEEK_PLANT
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
	private WardenBoon activeBoon;
	private int boonTurns;
	private int fadeleafEscapesUsed;
	private CombatStep combatStep = CombatStep.SHOOT;
	private GaleState galeState = GaleState.HUNTING;
	private PlantHuntState plantHuntState = PlantHuntState.GRACE;
	private int galeAimDx;
	private int galeAimDy;
	private int galeAimOrigin = -1;
	private int galeAimTarget = -1;
	private int galeRecoveryTurns;
	private boolean contactArmed;
	private int contactTargetId = -1;
	private int escapeCooldown;
	private transient boolean escapeCooldownStartedThisAction;
	private int markedPlantCell = -1;
	private int plantHuntTurns;
	private int plantGraceTurns = PLANT_HUNT_GRACE;
	private boolean restoredPlantValidationPending;
	private boolean meleeAttack;
	private transient boolean ordinaryActionStarted;
	private transient boolean claimedPlantThisAction;

	private static final String PHASE = "phase";
	private static final String ENCOUNTER_DELAY = "encounter_delay";
	static final String GALE_TURNS_REMAINING = "gale_turns_remaining";
	private static final String GALE_TARGET = "gale_target";
	private static final String ACTIVE_BOON = "active_boon";
	private static final String BOON_TURNS = "boon_turns";
	private static final String FADELEAF_ESCAPES_USED = "fadeleaf_escapes_used";
	private static final String COMBAT_STEP = "combat_step";
	private static final String GALE_STATE = "gale_state";
	private static final String PLANT_HUNT_STATE = "plant_hunt_state";
	private static final String GALE_AIM_DX = "gale_aim_dx";
	private static final String GALE_AIM_DY = "gale_aim_dy";
	private static final String GALE_AIM_ORIGIN = "gale_aim_origin";
	private static final String GALE_AIM_TARGET = "gale_aim_target";
	private static final String GALE_RECOVERY_TURNS_KEY = "gale_recovery_turns";
	private static final String CONTACT_ARMED = "contact_armed";
	private static final String CONTACT_TARGET_ID = "contact_target_id";
	private static final String ESCAPE_COOLDOWN_KEY = "escape_cooldown";
	private static final String MARKED_PLANT_CELL = "marked_plant_cell";
	private static final String PLANT_HUNT_TURNS = "plant_hunt_turns";
	private static final String PLANT_GRACE_TURNS = "plant_grace_turns";

	{
		HUNTING = new Hunting();
		spriteClass = HuntressBossSprite.class;
		HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 400 : 300;
		EXP = 30;
		defenseSkill = 15;
		viewDistance = 15;
		properties.add(Property.BOSS);
		properties.add(Property.DEMONIC);
	}

	public Phase phase() {
		return phase;
	}

	public int galeTurnsRemaining() {
		return galeTurnsRemaining;
	}

	public int fadeleafEscapesUsed() {
		return fadeleafEscapesUsed;
	}

	CombatStep combatStep() {
		return combatStep;
	}

	GaleState galeState() {
		return galeState;
	}

	PlantHuntState plantHuntState() {
		return plantHuntState;
	}

	boolean contactArmed() {
		return contactArmed;
	}

	int escapeCooldown() {
		return escapeCooldown;
	}

	int markedPlantCell() {
		return markedPlantCell;
	}

	int plantHuntTurns() {
		return plantHuntTurns;
	}

	int plantGraceTurns() {
		return plantGraceTurns;
	}

	int galeAimOrigin() {
		return galeAimOrigin;
	}

	int galeAimTarget() {
		return galeAimTarget;
	}

	int galeAimDx() {
		return galeAimDx;
	}

	int galeAimDy() {
		return galeAimDy;
	}

	int galeRecoveryTurns() {
		return galeRecoveryTurns;
	}

	int contactTargetId() {
		return contactTargetId;
	}

	WardenBoon activeBoon() {
		return activeBoon;
	}

	public void startEncounter() {
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
		meleeAttack = false;
		galeShot = false;
		combatStep = CombatStep.SHOOT;
		clearGaleRhythm(GaleState.HUNTING);
		contactArmed = false;
		contactTargetId = -1;
		plantHuntState = PlantHuntState.GRACE;
		markedPlantCell = -1;
		plantHuntTurns = 0;
		plantGraceTurns = PLANT_HUNT_GRACE;
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

	static TacticalAction tacticalAction(CombatStep step, boolean enemyInBossFOV,
			boolean hawkRelayed, boolean clearLine, int distance, int shotRange,
			boolean natureHunt) {
		if (distance <= 1) {
			return TacticalAction.MELEE;
		}
		if (natureHunt && step == CombatStep.MOVE) {
			return TacticalAction.SEEK_PLANT;
		}
		if (!normalShotEligible(enemyInBossFOV, hawkRelayed,
				clearLine, distance, shotRange)) {
			return TacticalAction.CHASE;
		}
		if (step == CombatStep.MOVE && distance >= 2 && distance <= 4) {
			return TacticalAction.SEEK_COVER;
		}
		return TacticalAction.SHOOT;
	}

	static CombatStep stepAfterCompletedAction(CombatStep current,
			TacticalAction action, boolean completed) {
		if (!completed) {
			return current;
		}
		if (action == TacticalAction.SHOOT) {
			return CombatStep.MOVE;
		}
		if (action == TacticalAction.SEEK_COVER) {
			return CombatStep.SHOOT;
		}
		return current;
	}

	static boolean canUseHawkRelay(boolean bossBlinded, boolean hawkBlinded,
			boolean hawkAlive, boolean withinRelayRadius, boolean clearProjectile) {
		return !bossBlinded && !hawkBlinded && hawkAlive
				&& withinRelayRadius && clearProjectile;
	}

	static boolean canAcquireGaleTarget(boolean blinded) {
		return !blinded;
	}

	protected int normalShotRange() {
		return BASE_NORMAL_SHOT_RANGE;
	}

	private boolean natureHuntActive() {
		return phase == Phase.WARDEN && plantHuntState == PlantHuntState.SELECTED
				&& !restoredPlantValidationPending;
	}

	@Override
	public float speed() {
		return natureHuntSpeed(super.speed(), natureHuntActive());
	}

	@Override
	public float attackDelay() {
		return natureHuntAttackDelay(super.attackDelay(), natureHuntActive(),
				meleeAttack, galeShot);
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

	static boolean shouldDeferCustomActions(int paralysed, boolean sleeping,
			boolean fleeing, boolean confused) {
		return paralysed > 0 || sleeping || fleeing || confused;
	}

	static int advanceGaleCountdown(int remaining, boolean actionable) {
		return actionable ? Math.max(0, remaining - 1) : remaining;
	}

	static int galeEndpointForVector(int origin, int dx, int dy, int width, int height) {
		if (width <= 0 || height <= 0 || origin < 0 || origin >= width * height
				|| dx == 0 && dy == 0) {
			return -1;
		}
		int originX = origin % width;
		int originY = origin / width;
		int targetX = clamp(originX + dx, 0, width - 1);
		int targetY = clamp(originY + dy, 0, height - 1);
		return targetX + targetY * width;
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

	static int restoredFadeleafEscapes(Bundle bundle) {
		return bundle.contains(FADELEAF_ESCAPES_USED)
				? Math.max(0, Math.min(MAX_FADELEAF_ESCAPES,
				bundle.getInt(FADELEAF_ESCAPES_USED))) : 0;
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}

	private static <E extends Enum<E>> E restoredEnum(Bundle bundle, String key,
			Class<E> type, E fallback) {
		if (!bundle.contains(key)) {
			return fallback;
		}
		try {
			return Enum.valueOf(type, bundle.getString(key));
		} catch (IllegalArgumentException exception) {
			return fallback;
		}
	}

	private static boolean validLevelCell(int cell) {
		return Dungeon.level != null && cell >= 0 && cell < Dungeon.level.length();
	}

	private void clearGaleRhythm(GaleState nextState) {
		galeState = nextState;
		galeAimDx = 0;
		galeAimDy = 0;
		galeAimOrigin = -1;
		galeAimTarget = -1;
		galeRecoveryTurns = 0;
		galeTarget = -1;
	}

	private boolean restoreLegacyGaleTarget(int legacyTarget) {
		if (phase != Phase.SNIPER || !validLevelCell(pos)
				|| !validLevelCell(legacyTarget) || pos == legacyTarget) {
			return false;
		}
		int width = Dungeon.level.width();
		galeAimDx = legacyTarget % width - pos % width;
		galeAimDy = legacyTarget / width - pos / width;
		if (galeAimDx == 0 && galeAimDy == 0) {
			return false;
		}
		galeAimOrigin = pos;
		galeAimTarget = legacyTarget;
		galeTarget = legacyTarget;
		galeRecoveryTurns = 0;
		galeState = GaleState.AIMED;
		return true;
	}

	private void normalizeRestoredRhythm(boolean hadPlantState) {
		escapeCooldownStartedThisAction = false;
		if (phase == Phase.WARDEN) {
			clearGaleRhythm(GaleState.HUNTING);
			if (!hadPlantState) {
				plantHuntState = activeBoon == null
						? PlantHuntState.GRACE : PlantHuntState.BOON_ACTIVE;
			}
			if (activeBoon != null) {
				plantHuntState = PlantHuntState.BOON_ACTIVE;
			}
			switch (plantHuntState) {
			case SELECTED:
				plantGraceTurns = 0;
				HuntressBossLevel level = huntressLevel();
				if (Dungeon.level == null) {
					restoredPlantValidationPending = true;
				} else if (level == null
						|| !level.isMarkedPlantReachable(this, markedPlantCell)) {
					plantHuntState = PlantHuntState.LOST_TRACK;
					markedPlantCell = -1;
					plantHuntTurns = 0;
				}
				break;
			case BOON_ACTIVE:
				markedPlantCell = -1;
				plantHuntTurns = 0;
				plantGraceTurns = 0;
				if (activeBoon == null) {
					plantHuntState = PlantHuntState.GRACE;
					plantGraceTurns = PLANT_HUNT_GRACE;
				}
				break;
			case LOST_TRACK:
			case NO_PLANTS:
				markedPlantCell = -1;
				plantHuntTurns = 0;
				plantGraceTurns = 0;
				break;
			case GRACE:
			default:
				markedPlantCell = -1;
				plantHuntTurns = 0;
				break;
			}
		} else {
			activeBoon = null;
			boonTurns = 0;
			plantHuntState = PlantHuntState.GRACE;
			markedPlantCell = -1;
			plantHuntTurns = 0;
			plantGraceTurns = PLANT_HUNT_GRACE;
			if (galeState == GaleState.AIMED) {
				if (!validLevelCell(galeAimOrigin) || !validLevelCell(galeAimTarget)
						|| galeAimOrigin == galeAimTarget
						|| galeAimDx == 0 && galeAimDy == 0) {
					clearGaleRhythm(GaleState.READY);
				} else {
					galeTarget = galeAimTarget;
					galeRecoveryTurns = 0;
				}
			} else {
				galeAimDx = 0;
				galeAimDy = 0;
				galeAimOrigin = -1;
				galeAimTarget = -1;
				galeTarget = -1;
				if (galeState != GaleState.RECOVERING) {
					galeRecoveryTurns = 0;
				}
			}
		}
	}

	static boolean shouldAbsorbBurning(int roll) {
		return roll == 0;
	}

	static float natureHuntSpeed(float base, boolean active) {
		return active ? base * NATURE_HUNT_MOVE_MULTIPLIER : base;
	}

	static float natureHuntAttackDelay(float base, boolean active,
			boolean melee, boolean gale) {
		return active && !melee && !gale
				? base * NATURE_HUNT_ATTACK_DELAY_MULTIPLIER : base;
	}

	static boolean shouldProcNatureWrath(boolean melee, boolean gale, int roll) {
		return !melee && !gale && roll == 0;
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
		if (activeBoon != null) {
			bundle.put(ACTIVE_BOON, activeBoon);
		}
		bundle.put(BOON_TURNS, boonTurns);
		bundle.put(FADELEAF_ESCAPES_USED, fadeleafEscapesUsed);
		bundle.put(COMBAT_STEP, combatStep);
		bundle.put(GALE_STATE, galeState);
		bundle.put(PLANT_HUNT_STATE, plantHuntState);
		bundle.put(GALE_AIM_DX, galeAimDx);
		bundle.put(GALE_AIM_DY, galeAimDy);
		bundle.put(GALE_AIM_ORIGIN, galeAimOrigin);
		bundle.put(GALE_AIM_TARGET, galeAimTarget);
		bundle.put(GALE_RECOVERY_TURNS_KEY, galeRecoveryTurns);
		bundle.put(CONTACT_ARMED, contactArmed);
		bundle.put(CONTACT_TARGET_ID, contactTargetId);
		bundle.put(ESCAPE_COOLDOWN_KEY, escapeCooldown);
		bundle.put(MARKED_PLANT_CELL, markedPlantCell);
		bundle.put(PLANT_HUNT_TURNS, plantHuntTurns);
		bundle.put(PLANT_GRACE_TURNS, plantGraceTurns);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		phase = restoredEnum(bundle, PHASE, Phase.class, Phase.SNIPER);
		encounterDelay = bundle.getBoolean(ENCOUNTER_DELAY);
		galeTurnsRemaining = restoredGaleTurns(bundle);
		int legacyGaleTarget = bundle.contains(GALE_TARGET)
				? bundle.getInt(GALE_TARGET) : -1;
		galeTarget = legacyGaleTarget;
		activeBoon = restoredEnum(bundle, ACTIVE_BOON, WardenBoon.class, null);
		boonTurns = bundle.getInt(BOON_TURNS);
		fadeleafEscapesUsed = restoredFadeleafEscapes(bundle);
		combatStep = restoredEnum(bundle, COMBAT_STEP, CombatStep.class, CombatStep.SHOOT);
		galeState = restoredEnum(bundle, GALE_STATE, GaleState.class, GaleState.HUNTING);
		boolean hadPlantState = bundle.contains(PLANT_HUNT_STATE);
		plantHuntState = restoredEnum(bundle, PLANT_HUNT_STATE,
				PlantHuntState.class, PlantHuntState.GRACE);
		galeAimDx = bundle.contains(GALE_AIM_DX) ? bundle.getInt(GALE_AIM_DX) : 0;
		galeAimDy = bundle.contains(GALE_AIM_DY) ? bundle.getInt(GALE_AIM_DY) : 0;
		galeAimOrigin = bundle.contains(GALE_AIM_ORIGIN)
				? bundle.getInt(GALE_AIM_ORIGIN) : -1;
		galeAimTarget = bundle.contains(GALE_AIM_TARGET)
				? bundle.getInt(GALE_AIM_TARGET) : -1;
		galeRecoveryTurns = clamp(bundle.contains(GALE_RECOVERY_TURNS_KEY)
				? bundle.getInt(GALE_RECOVERY_TURNS_KEY) : 0,
				0, GALE_RECOVERY_TURNS);
		contactArmed = bundle.contains(CONTACT_ARMED) && bundle.getBoolean(CONTACT_ARMED);
		contactTargetId = bundle.contains(CONTACT_TARGET_ID)
				? Math.max(-1, bundle.getInt(CONTACT_TARGET_ID)) : -1;
		escapeCooldown = clamp(bundle.contains(ESCAPE_COOLDOWN_KEY)
				? bundle.getInt(ESCAPE_COOLDOWN_KEY) : 0, 0, ESCAPE_COOLDOWN);
		markedPlantCell = bundle.contains(MARKED_PLANT_CELL)
				? bundle.getInt(MARKED_PLANT_CELL) : -1;
		if (Dungeon.level != null && !validLevelCell(markedPlantCell)) {
			markedPlantCell = -1;
		}
		plantHuntTurns = clamp(bundle.contains(PLANT_HUNT_TURNS)
				? bundle.getInt(PLANT_HUNT_TURNS) : 0, 0, PLANT_HUNT_DURATION);
		plantGraceTurns = clamp(bundle.contains(PLANT_GRACE_TURNS)
				? bundle.getInt(PLANT_GRACE_TURNS) : PLANT_HUNT_GRACE,
				0, PLANT_HUNT_GRACE);
		if (legacyGaleTarget >= 0 && galeAimTarget < 0) {
			if (!restoreLegacyGaleTarget(legacyGaleTarget)) {
				clearGaleRhythm(GaleState.READY);
			}
		}
		normalizeRestoredRhythm(hadPlantState);
		BossHealthBar.assignBoss(this);
		BossHealthBar.bleed(phase == Phase.WARDEN);
	}

	@Override
	protected boolean act() {
		if (shouldDeferCustomActions(paralysed, state == SLEEPING, state == FLEEING,
				isConfusedForCustomActionPriority())) {
			return super.act();
		}
		validateRestoredPlantHunt(huntressLevel());

		if (encounterDelay) {
			encounterDelay = false;
			spend(TICK);
			return true;
		}

		if (phase == Phase.SNIPER) {
			switch (galeState) {
				case AIMED:
					return finishSpecialAction(fireGale());
				case RECOVERING:
					return finishSpecialAction(recoverFromGale());
				default:
					break;
			}
		}

		Char closeTarget = closestAdjacentHostile();
		if (closeTarget == null) {
			clearContactWarning();
		}

		if (hasPriorityPlantRhythmAction() && actWardenRhythm()) {
			return finishSpecialAction(true);
		}

		if (closeTarget != null && contactArmed && escapeCooldown == 0) {
			if (tryContactEscape(closeTarget)) {
				spend(TICK);
				onEffectiveActionCompleted(false);
				return true;
			}
		}

		if (phase == Phase.WARDEN && actWardenRhythm()) {
			return finishSpecialAction(true);
		}

		if (closeTarget == null && shouldSeekMarkedPlant()) {
			int oldPos = pos;
			boolean moved = getCloser(markedPlantCell);
			if (moved) {
				spend(1 / speed());
				combatStep = CombatStep.SHOOT;
				onEffectiveActionCompleted(true);
				return moveSprite(oldPos, pos);
			}
		}

		if (closeTarget != null) {
			enemy = closeTarget;
			target = closeTarget.pos;
			state = HUNTING;
			ordinaryActionStarted = false;
			boolean result = doAttack(closeTarget);
			if (ordinaryActionStarted) {
				if (!contactArmed) {
					contactArmed = true;
					announceContactWarning();
				}
				contactTargetId = closeTarget.id();
				onEffectiveActionCompleted(true);
			}
			return result;
		}

		if (phase == Phase.SNIPER) {
			switch (galeState) {
				case HUNTING:
					if (galeTurnsRemaining <= 0) {
						if (tryAimGale()) {
							return finishSpecialAction(true);
						}
						galeState = GaleState.READY;
					}
					break;
				case READY:
					if (tryAimGale()) {
						return finishSpecialAction(true);
					}
					break;
				default:
					break;
			}
		}

		ordinaryActionStarted = false;
		boolean result = super.act();
		if (ordinaryActionStarted) {
			onEffectiveActionCompleted(true);
		}
		return result;
	}

	public void finishLevelRestore(HuntressBossLevel level) {
		validateRestoredPlantHunt(level);
	}

	private void validateRestoredPlantHunt(HuntressBossLevel level) {
		if (!restoredPlantValidationPending) {
			return;
		}
		restoredPlantValidationPending = false;
		if (phase == Phase.WARDEN && plantHuntState == PlantHuntState.SELECTED
				&& level != null && level.isMarkedPlantReachable(this, markedPlantCell)) {
			return;
		}
		plantHuntState = PlantHuntState.LOST_TRACK;
		markedPlantCell = -1;
		plantHuntTurns = 0;
	}

	private boolean hasPriorityPlantRhythmAction() {
		return phase == Phase.WARDEN && plantHuntState == PlantHuntState.LOST_TRACK;
	}

	private boolean shouldSeekMarkedPlant() {
		return natureHuntActive() && combatStep == CombatStep.MOVE
				&& levelHasMarkedPlant();
	}

	protected boolean isConfusedForCustomActionPriority() {
		return buff(Amok.class) != null
				|| buff(Feint.AfterImage.FeintConfusion.class) != null;
	}

	private boolean tryAimGale() {
		ArrayList<Char> visibleTargets = visibleGaleTargets();
		if (visibleTargets.isEmpty()) {
			return false;
		}
		Char target = pickGaleTarget(visibleTargets, Random.Int(visibleTargets.size()));
		return target != null && aimGaleAt(target);
	}

	private void onEffectiveActionCompleted(boolean ordinary) {
		if (ordinary && phase == Phase.SNIPER && galeState == GaleState.HUNTING
				&& galeTurnsRemaining > 0) {
			galeTurnsRemaining = advanceGaleCountdown(galeTurnsRemaining, true);
		}
		if (escapeCooldownStartedThisAction) {
			escapeCooldownStartedThisAction = false;
		} else if (escapeCooldown > 0) {
			escapeCooldown--;
		}
		if (!ordinary || phase != Phase.WARDEN) {
			return;
		}
		if (claimedPlantThisAction) {
			claimedPlantThisAction = false;
			return;
		}
		switch (plantHuntState) {
			case GRACE:
				if (plantGraceTurns > 0) {
					plantGraceTurns--;
				}
				break;
			case SELECTED:
				if (plantHuntTurns > 0) {
					plantHuntTurns--;
				}
				break;
			case BOON_ACTIVE:
				advanceBoon();
				if (activeBoon == null) {
					beginPlantGrace();
				}
				break;
			default:
				break;
		}
	}

	private boolean actWardenRhythm() {
		switch (plantHuntState) {
			case LOST_TRACK:
				beginPlantGrace();
				spend(TICK);
				return true;
			case GRACE:
				return plantGraceTurns <= 0 && markNextPlant();
			case SELECTED:
				if (!levelHasMarkedPlant() || plantHuntTurns <= 0) {
					loseMarkedPlant();
					return actWardenRhythm();
				}
				return false;
			case BOON_ACTIVE:
				if (activeBoon == null) {
					beginPlantGrace();
				}
				return false;
			case NO_PLANTS:
				if (hasReachablePlant()) {
					beginPlantGrace();
				}
				return false;
			default:
				return false;
		}
	}

	private HuntressBossLevel huntressLevel() {
		return Dungeon.level instanceof HuntressBossLevel
				? (HuntressBossLevel) Dungeon.level : null;
	}

	private boolean levelHasMarkedPlant() {
		HuntressBossLevel level = huntressLevel();
		return level != null && markedPlantCell >= 0
				&& level.isMarkedPlantReachable(this, markedPlantCell);
	}

	private boolean hasReachablePlant() {
		HuntressBossLevel level = huntressLevel();
		return level != null && level.hasReachablePlant(this);
	}

	private boolean markNextPlant() {
		if (isBlindedForPlantMark()) {
			return false;
		}
		HuntressBossLevel level = huntressLevel();
		markedPlantCell = level == null ? -1 : level.selectMarkedPlant(this);
		if (markedPlantCell < 0) {
			plantHuntState = PlantHuntState.NO_PLANTS;
			return false;
		}
		plantHuntState = PlantHuntState.SELECTED;
		plantHuntTurns = PLANT_HUNT_DURATION;
		plantGraceTurns = 0;
		announcePlantMarked(markedPlantCell);
		announceNatureHunt();
		spend(TICK);
		return true;
	}

	protected boolean isBlindedForPlantMark() {
		return buff(Blindness.class) != null;
	}

	private void loseMarkedPlant() {
		if (plantHuntState == PlantHuntState.LOST_TRACK) {
			return;
		}
		markedPlantCell = -1;
		plantHuntTurns = 0;
		plantHuntState = PlantHuntState.LOST_TRACK;
		announceLostTrack();
	}

	private void beginPlantGrace() {
		markedPlantCell = -1;
		plantHuntTurns = 0;
		plantGraceTurns = PLANT_HUNT_GRACE;
		plantHuntState = PlantHuntState.GRACE;
	}

	protected void announcePlantMarked(int cell) {
		if (sprite != null && sprite.parent != null) {
			sprite.parent.addToBack(new TargetedCell(cell, 0x00CC44));
		}
		yell(Messages.get(this, "plant_marked"));
	}

	protected void announceNatureHunt() {
		Emitter emitter = sprite == null ? null : sprite.emitter();
		if (emitter != null) {
			emitter.burst(LeafParticle.GENERAL, 10);
		}
		if (Gdx.app != null && Gdx.files != null) {
			yell(Messages.get(this, "nature_hunt"));
		}
	}

	protected void announceLostTrack() {
		yell(Messages.get(this, "lost_track"));
	}

	public void onPlantRemoved(int cell) {
		if (phase == Phase.WARDEN && plantHuntState == PlantHuntState.SELECTED
				&& cell == markedPlantCell) {
			loseMarkedPlant();
		}
	}

	public void onPlantClaimed(int cell, WardenBoon boon) {
		if (phase != Phase.WARDEN || boon == null) {
			return;
		}
		markedPlantCell = -1;
		plantHuntTurns = 0;
		claimedPlantThisAction = true;
		grantBoon(boon);
		if (activeBoon == null) {
			beginPlantGrace();
		} else {
			plantGraceTurns = 0;
			plantHuntState = PlantHuntState.BOON_ACTIVE;
		}
	}

	private boolean finishSpecialAction(boolean result) {
		onEffectiveActionCompleted(false);
		return result;
	}

	private boolean recoverFromGale() {
		announceGaleRecovery();
		spend(TICK);
		if (--galeRecoveryTurns <= 0) {
			clearGaleRhythm(GaleState.HUNTING);
			galeTurnsRemaining = GALE_INTERVAL;
		}
		return true;
	}

	protected boolean teleportBossAndTargetApart(Char target) {
		return Dungeon.level instanceof HuntressBossLevel
				&& ((HuntressBossLevel) Dungeon.level)
				.teleportBossAndTargetApart(this, target);
	}

	private Char closestAdjacentHostile() {
		if (Dungeon.level == null) {
			return null;
		}
		Char closest = null;
		int closestDistance = Integer.MAX_VALUE;
		for (Char candidate : Actor.chars()) {
			if (candidate == null || candidate == this || !candidate.isAlive()
					|| candidate.invisible > 0
					|| Actor.findCharById(candidate.id()) != candidate
					|| !isHostileOrAggressed(this, candidate) || isCharmedBy(candidate)
					|| isProtectedHuntressSummon(candidate)) {
				continue;
			}
			int distance = Dungeon.level.distance(pos, candidate.pos);
			if (distance <= 1 && (distance < closestDistance
					|| distance == closestDistance && closest != null
					&& candidate.id() < closest.id())) {
				closest = candidate;
				closestDistance = distance;
			}
		}
		return closest;
	}

	private void clearContactWarning() {
		contactArmed = false;
		contactTargetId = -1;
	}

	protected void announceContactWarning() {
		yell(Messages.get(this, "contact_warning"));
	}

	protected void announceGaleRecovery() {
		yell(Messages.get(this, "gale_recover"));
	}

	private boolean tryContactEscape(Char closeTarget) {
		boolean escaped = false;
		if (phase == Phase.WARDEN && fadeleafEscapesUsed < MAX_FADELEAF_ESCAPES) {
			escaped = teleportBossAndTargetApart(closeTarget);
			if (escaped) {
				fadeleafEscapesUsed++;
			}
		}
		if (!escaped) {
			escaped = performBossOnlyEscape(closeTarget);
		}
		if (escaped) {
			startEscapeCooldown();
			clearContactWarning();
		}
		return escaped;
	}

	protected boolean performBossOnlyEscape(Char target) {
		if (!(Dungeon.level instanceof HuntressBossLevel)) {
			return false;
		}
		HuntressBossLevel level = (HuntressBossLevel) Dungeon.level;
		int destination = level.selectHuntressEscapeCell(this, target);
		return destination >= 0 && level.moveHuntressToEscapeCell(this, destination);
	}

	private void startEscapeCooldown() {
		escapeCooldown = ESCAPE_COOLDOWN;
		escapeCooldownStartedThisAction = true;
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
		if (isProtectedHuntressSummon(chosen)) {
			chosen = null;
		}
		if (!canUseHawkRelay) {
			return chosen;
		}
		if (bossCanSee(chosen) || hasHawkRelay(chosen)) {
			return chosen;
		}
		if (!isProtectedHuntressSummon(enemy)
				&& (bossCanSee(enemy) || hasHawkRelay(enemy))) {
			return enemy;
		}
		ArrayList<Char> relayTargets = hawkRelayTargets();
		if (!relayTargets.isEmpty()) {
			state = HUNTING;
			return Random.element(relayTargets);
		}
		return chosen;
	}

	@Override
	public void aggro(Char ch) {
		if (isProtectedHuntressSummon(ch)) {
			if (enemy == ch) {
				clearEnemy();
			}
			return;
		}
		super.aggro(ch);
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
		boolean bossBlinded = isBlindedForNormalShot();
		boolean clearProjectile = hasClearNormalShotLine(target);
		for (Char candidate : Actor.chars()) {
			if (candidate instanceof DistractingHawk
					&& candidate.alignment == alignment && candidate.pos >= 0
					&& canUseHawkRelay(bossBlinded,
						candidate.buff(Blindness.class) != null, candidate.isAlive(),
						withinHawkRelayRadius(Dungeon.level.distance(
							candidate.pos, target.pos), HAWK_RELAY_RADIUS),
						clearProjectile)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isBlindedForNormalShot() {
		return buff(Blindness.class) != null;
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
				&& isHostileOrAggressed(this, target) && !isCharmedBy(target)
				&& !isProtectedHuntressSummon(target);
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
		return target != null && target != this && !isProtectedHuntressSummon(target)
				&& isGaleTargetEligible(
				target.isAlive(), isHostileOrAggressed(this, target) && !isCharmedBy(target),
				inBossFOV, target.invisible > 0);
	}

	private ArrayList<Char> visibleGaleTargets() {
		ArrayList<Char> targets = new ArrayList<>();
		if (Dungeon.level == null || !canAcquireGaleTarget(isBlindedForGaleAim())) {
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

	protected boolean isBlindedForGaleAim() {
		return buff(Blindness.class) != null;
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
		if (isProtectedHuntressSummon(enemy)) {
			return false;
		}
		if (galeState == GaleState.AIMED) {
			return true;
		}
		TacticalAction action = tacticalActionFor(enemy);
		return action == TacticalAction.MELEE || action == TacticalAction.SHOOT;
	}

	@Override
	public boolean canRangedAttack(Char enemy) {
		return !isProtectedHuntressSummon(enemy)
				&& galeState != GaleState.AIMED
				&& tacticalActionFor(enemy) == TacticalAction.SHOOT;
	}

	@Override
	public boolean canMeleeAttack(Char enemy) {
		return !isProtectedHuntressSummon(enemy)
				&& tacticalActionFor(enemy) == TacticalAction.MELEE;
	}

	@Override
	protected boolean doAttack(final Char enemy) {
		if (isProtectedHuntressSummon(enemy)) {
			return false;
		}
		TacticalAction action = tacticalActionFor(enemy);
		if (action == TacticalAction.MELEE) {
			ordinaryActionStarted = true;
			meleeAttack = true;
			boolean awaitingAnimation = false;
			try {
				boolean completed = super.doAttack(enemy);
				awaitingAnimation = !completed;
				return completed;
			} finally {
				if (!awaitingAnimation) {
					meleeAttack = false;
				}
			}
		}
		return action == TacticalAction.SHOOT && doRangedAttack(enemy);
	}

	@Override
	public boolean doRangedAttack(final Char enemy) {
		if (isProtectedHuntressSummon(enemy)) {
			return false;
		}
		ordinaryActionStarted = true;
		combatStep = stepAfterCompletedAction(combatStep,
				TacticalAction.SHOOT, true);
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
		if (target == null || Dungeon.level == null) {
			return false;
		}
		int width = Dungeon.level.width();
		galeAimOrigin = pos;
		galeAimTarget = target.pos;
		galeAimDx = target.pos % width - pos % width;
		galeAimDy = target.pos / width - pos / width;
		int endpoint = galeEndpointForVector(pos, galeAimDx, galeAimDy,
				width, Dungeon.level.height());
		if (endpoint < 0) {
			return false;
		}
		galeState = GaleState.AIMED;
		galeRecoveryTurns = 0;
		galeTarget = -1;
		Ballistica aim = new Ballistica(pos, endpoint, Ballistica.STOP_SOLID);
		if (sprite != null && sprite.parent != null) {
			for (int cell : aim.subPath(1, aim.dist)) {
				sprite.parent.addToBack(new TargetedCell(cell, 0xFF0000));
			}
		}
		announceGaleAim();
		spend(TICK);
		return true;
	}

	private void resolveNormalShot(Char shotTarget) {
		meleeAttack = false;
		try {
			galeShot = false;
			if (shotTarget != null && shotTarget.isAlive()
					&& !isProtectedHuntressSummon(shotTarget)
					&& new Ballistica(pos, shotTarget.pos, Ballistica.PROJECTILE).collisionPos == shotTarget.pos) {
				beginPhysicalRangedAttack();
				completePhysicalAttack(shotTarget, attack(shotTarget));
			}
			Invisibility.dispel(this);
		} finally {
			galeShot = false;
		}
	}

	private boolean fireGale() {
		if (Dungeon.level == null) {
			return recoverFromInvalidGale();
		}
		int endpoint = galeEndpointForVector(pos, galeAimDx, galeAimDy,
				Dungeon.level.width(), Dungeon.level.height());
		if (endpoint < 0) {
			return recoverFromInvalidGale();
		}
		final Ballistica gale = new Ballistica(pos, endpoint, Ballistica.STOP_SOLID);
		final int destination = gale.collisionPos;
		galeState = GaleState.RECOVERING;
		galeRecoveryTurns = GALE_RECOVERY_TURNS;
		galeAimDx = 0;
		galeAimDy = 0;
		galeAimOrigin = -1;
		galeAimTarget = -1;
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

	private boolean recoverFromInvalidGale() {
		galeState = GaleState.RECOVERING;
		galeRecoveryTurns = GALE_RECOVERY_TURNS;
		galeAimDx = 0;
		galeAimDy = 0;
		galeAimOrigin = -1;
		galeAimTarget = -1;
		galeTarget = -1;
		spend(TICK);
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
				if (ch != null && ch != this && !isProtectedHuntressSummon(ch)
					&& galeCanDamage(alignment, ch)) {
					beginPhysicalRangedAttack();
					completePhysicalAttack(ch, attack(ch));
					Buff.prolong(ch, Cripple.class, 2f);
				}
			}
			Invisibility.dispel(this);
		} finally {
			galeShot = false;
		}
	}

	static boolean galeCanDamage(Alignment attacker, Char target) {
		return target != null && (attacker != target.alignment
				|| target.buff(StoneOfAggression.Aggression.class) != null);
	}

	private static boolean isHostileOrAggressed(Char attacker, Char target) {
		return Actor.isHostile(attacker, target)
				|| target != null
				&& target.buff(StoneOfAggression.Aggression.class) != null;
	}

	private boolean isProtectedHuntressSummon(Char target) {
		return target != null && target.alignment == alignment
				&& (target instanceof DistractingHawk
				|| target instanceof HuntressTentacle)
				&& target.buff(StoneOfAggression.Aggression.class) == null;
	}

	@Override
	public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
		if (isProtectedHuntressSummon(enemy)) {
			return 0;
		}
		damage = baseAttackProc(enemy, damage, damageTags);
		boolean ordinaryRemote = phase == Phase.WARDEN && !meleeAttack && !galeShot;
		if (ordinaryRemote && plantHuntState == PlantHuntState.SELECTED) {
			if (shouldProcNatureWrath(meleeAttack, galeShot, natureWrathRoll())) {
				activateNatureWrathPlant(enemy, natureWrathPlantIndex());
			}
		} else if (ordinaryRemote && activeBoon != null) {
			applyActiveBoonOnRemoteHit(enemy, activeBoon);
		}
		return damage;
	}

	protected void applyActiveBoonOnRemoteHit(Char enemy, WardenBoon boon) {
		switch (boon) {
				case BLINDWEED:
					Buff.prolong(enemy, Blindness.class, 3f);
					break;
				case FIREBLOOM:
					Buff.affect(enemy, Burning.class).reignite(enemy, 4f);
					break;
				case ICECAP:
					Buff.prolong(enemy, Chill.class, 4f);
					break;
				case SORROWMOSS:
					Buff.affect(enemy, Poison.class).set(5f);
					break;
				case STORMVINE:
					Buff.prolong(enemy, Vertigo.class, 4f);
					break;
				default:
					break;
		}
	}

	protected int baseAttackProc(Char enemy, int damage, DamageTag... damageTags) {
		return super.attackProc(enemy, damage, damageTags);
	}

	protected int natureWrathRoll() {
		return Random.Int(NATURE_HUNT_PLANT_CHANCE_DENOMINATOR);
	}

	protected int natureWrathPlantIndex() {
		return Random.Int(SpiritBow.harmfulPlantPool().length);
	}

	protected void activateNatureWrathPlant(Char enemy, int index) {
		SpiritBow.activateHarmfulPlant(enemy, index);
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
				if (tryTriggerFadeleafBoon()) {
					startEscapeCooldown();
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
			announceBoon(boon);
		}
	}

	protected void announceBoon(WardenBoon boon) {
		yell(Messages.get(this, "boon_" + boon.name().toLowerCase()));
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
			announceWardenTransition();
			onWardenPhaseStarted();
		}
	}

	protected void announceWardenTransition() {
		BossHealthBar.bleed(true);
		yell(Messages.get(this, "warden"));
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

	protected boolean tryTriggerFadeleafBoon() {
		if (Dungeon.level instanceof HuntressBossLevel) {
			return ((HuntressBossLevel) Dungeon.level).teleportBossAndHero(this);
		}
		triggerFadeleafBoon();
		return false;
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
		TacticalAction action = state == HUNTING && enemy != null
				&& this.target == enemy.pos && target == this.target
				? tacticalActionFor(enemy) : null;
		if (action == TacticalAction.SEEK_COVER) {
			int coverCell = nearestCoverCell();
			if (coverCell == -1) {
				coverCell = nearestFirePositionByCover(enemy);
			}
			if (coverCell != -1) {
				boolean moved;
				if (Dungeon.level.adjacent(pos, coverCell)) {
					move(coverCell);
					moved = true;
				} else {
					moved = super.getCloser(coverCell);
				}
				moved = markOrdinaryMovement(moved);
				combatStep = stepAfterCompletedAction(combatStep, action, moved);
				return moved;
			}
		}
		if (action == TacticalAction.SEEK_PLANT && levelHasMarkedPlant()) {
			boolean moved = markOrdinaryMovement(super.getCloser(markedPlantCell));
			if (moved) {
				combatStep = CombatStep.SHOOT;
			}
			return moved;
		}
		return markOrdinaryMovement(super.getCloser(target));
	}

	private boolean markOrdinaryMovement(boolean moved) {
		if (moved) {
			ordinaryActionStarted = true;
		}
		return moved;
	}

	private TacticalAction tacticalActionFor(Char enemy) {
		boolean enemyInBossFOV = bossCanSee(enemy);
		boolean hawkRelayed = hasHawkRelay(enemy);
		boolean clearLine = hasClearNormalShotLine(enemy);
		int distance = enemy == null || Dungeon.level == null
				? Integer.MAX_VALUE : Dungeon.level.distance(pos, enemy.pos);
		TacticalAction action = tacticalAction(combatStep, enemyInBossFOV,
				hawkRelayed, clearLine, distance, normalShotRange(), natureHuntActive());
		return action;
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

	private int nearestFirePositionByCover(Char target) {
		if (!(Dungeon.level instanceof HuntressBossLevel) || target == null) {
			return -1;
		}
		List<Set<Integer>> clusters = ((HuntressBossLevel) Dungeon.level).coverClusters();
		if (clusters.isEmpty()) {
			return -1;
		}
		int bestCell = -1;
		int bestCoverDistance = Integer.MAX_VALUE;
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = pos + offset;
			if (!Dungeon.level.insideMap(cell)
					|| !Dungeon.level.adjacent(pos, cell)
					|| !Dungeon.level.passable[cell]
					|| Actor.findChar(cell) != null
					|| Char.hasProp(this, Char.Property.LARGE)
					&& !Dungeon.level.openSpace[cell]
					|| new Ballistica(cell, target.pos, Ballistica.PROJECTILE)
					.collisionPos.intValue() != target.pos) {
				continue;
			}
			int coverDistance = distanceToCover(cell, clusters);
			if (coverDistance < bestCoverDistance) {
				bestCell = cell;
				bestCoverDistance = coverDistance;
			}
		}
		return bestCell;
	}

	private int distanceToCover(int cell, List<Set<Integer>> clusters) {
		int best = Integer.MAX_VALUE;
		for (Set<Integer> cluster : clusters) {
			for (int cover : cluster) {
				best = Math.min(best, Dungeon.level.distance(cell, cover));
			}
		}
		return best;
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
            properties.add(Property.UNSLEEP);
			HUNTING = new Hunting();
		}

		public DistractingHawk() {
			this(Alignment.ENEMY);
		}

		public DistractingHawk(Alignment huntressAlignment) {
			alignment = huntressAlignment;
		}

		private boolean isHuntressFaction(Char target) {
			return target != null && target.alignment == alignment
					&& (target instanceof HuntressBoss
					|| target instanceof DistractingHawk
					|| target instanceof HuntressTentacle)
					&& target.buff(StoneOfAggression.Aggression.class) == null;
		}

		@Override
		protected Char chooseEnemy() {
			Char chosen = super.chooseEnemy();
			if (isHuntressFaction(chosen)) {
				clearEnemy();
				return null;
			}
			return chosen;
		}

		@Override
		protected boolean canAttack(Char enemy) {
			return !isHuntressFaction(enemy) && super.canAttack(enemy);
		}

		@Override
		public void aggro(Char ch) {
			if (isHuntressFaction(ch)) {
				if (enemy == ch) {
					clearEnemy();
				}
				return;
			}
			super.aggro(ch);
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
			if (isHuntressFaction(enemy)) {
				return 0;
			}
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
					&& !isHuntressFaction(candidate)
					&& candidate.pos >= 0
					&& fieldOfView != null
					&& candidate.pos < fieldOfView.length
					&& eligibleTarget(candidate.isAlive(),
							isHostileOrAggressed(this, candidate),
							fieldOfView[candidate.pos], candidate.invisible > 0
									&& candidate.buff(StoneOfAggression.Aggression.class) == null,
						candidate.buff(Cripple.class) != null
								&& candidate.buff(StoneOfAggression.Aggression.class) == null)
					&& !isCharmedBy(candidate);
		}

		private Char nearestEligibleTarget() {
			Char best = null;
			int bestDistance = Integer.MAX_VALUE;
			int bestId = -1;
			boolean bestIsAggressed = false;
			for (Char candidate : Actor.chars()) {
				if (!isEligibleTarget(candidate)) {
					continue;
				}
				boolean candidateIsAggressed = candidate.buff(
						StoneOfAggression.Aggression.class) != null;
				int distance = Dungeon.level.distance(pos, candidate.pos);
				int id = candidate.id();
				if (candidateIsAggressed && !bestIsAggressed
						|| candidateIsAggressed == bestIsAggressed
						&& preferTarget(distance, id, bestDistance, bestId)) {
					best = candidate;
					bestDistance = distance;
					bestId = id;
					bestIsAggressed = candidateIsAggressed;
				}
			}
			return best;
		}

		private Char nearestVisibleHostileReference() {
			Char best = null;
			int bestDistance = Integer.MAX_VALUE;
			int bestId = -1;
			for (Char candidate : Actor.chars()) {
				if (candidate == this || isHuntressFaction(candidate) || !candidate.isAlive()
						|| !isHostileOrAggressed(this, candidate)
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
			return target != null && !isHuntressFaction(target)
					&& target.isAlive() && Actor.chars().contains(target)
					&& isHostileOrAggressed(this, target) ? target : null;
		}

		private boolean actSeeking(boolean justAlerted) {
			if (isBlindedForSearch()) {
				return actBlindedWaiting();
			}
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
			if (isBlindedForSearch()) {
				return actBlindedWaiting();
			}
			Char next = nearestEligibleTarget();
			hawkMode = modeAfterRetreat(next != null);
			if (next != null) {
				return actSeeking(justAlerted);
			}
			return actWaiting(justAlerted);
		}

		private boolean actWaiting(boolean justAlerted) {
			if (isBlindedForSearch()) {
				return actBlindedWaiting();
			}
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

		private boolean isBlindedForSearch() {
			return buff(Blindness.class) != null;
		}

		private boolean actBlindedWaiting() {
			hawkMode = HawkMode.WAITING;
			retreatTargetId = -1;
			enemy = null;
			enemySeen = false;
			state = WANDERING;
			target = ((Mob.Wandering) WANDERING).randomDestination();
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

		public HuntressTentacle() {
			this(Alignment.ENEMY);
		}

		public HuntressTentacle(Alignment huntressAlignment) {
			alignment = huntressAlignment;
		}

		private boolean isHuntressFaction(Char target) {
			return target != null && target.alignment == alignment
					&& (target instanceof HuntressBoss
					|| target instanceof DistractingHawk
					|| target instanceof HuntressTentacle)
					&& target.buff(StoneOfAggression.Aggression.class) == null;
		}

		@Override
		protected Char chooseEnemy() {
			Char chosen = super.chooseEnemy();
			if (isHuntressFaction(chosen)) {
				clearEnemy();
				return null;
			}
			return chosen;
		}

		@Override
		protected boolean canAttack(Char enemy) {
			return !isHuntressFaction(enemy) && super.canAttack(enemy);
		}

		@Override
		public void aggro(Char ch) {
			if (isHuntressFaction(ch)) {
				if (enemy == ch) {
					clearEnemy();
				}
				return;
			}
			super.aggro(ch);
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
			if (isHuntressFaction(enemy)) {
				return 0;
			}
			Buff.prolong(enemy, Cripple.class, 2f);
			return damage;
		}

		@Override
		public int drRoll() {
			return 0;
		}
	}
}
