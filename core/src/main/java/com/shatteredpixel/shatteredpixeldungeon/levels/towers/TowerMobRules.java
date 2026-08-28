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

package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.AlienatedPrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.ChainShadowThief;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Corpse;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CorrosiveSwarm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DeathButterfly;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.DarkMechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.EarthlySerpent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.HeavyCrabification;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MarshSlime;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MechanicalFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MyriadBlackShadow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MyriadEcho;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.MimicCrocodile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Obscura;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RoastLambWarlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.RuneSpinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.SoulCollector;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.TapirCrocodile;
import com.watabou.utils.Random;

public final class TowerMobRules {

	static final float BASE_DARK_MECHANICAL_FIST_CHANCE = 0.025f;
	static final float BASE_MYRIAD_BLACK_SHADOW_CHANCE = 0.025f;
	static final float BASE_TAPIR_CROCODILE_CHANCE = 0.025f;

	private TowerMobRules() {
	}

	static Selection select(float roll) {
		Selection[] selections = Selection.values();
		int index = (int) (roll * selections.length);
		index = Math.max(0, Math.min(index, selections.length - 1));
		return selections[index];
	}

	static Mob prepareNaturalSpawn(Mob mob) {
		mob.state = mob.WANDERING;
		return mob;
	}

	static float darkMechanicalFistChance(float exoticMultiplier) {
		return Math.min(1f, BASE_DARK_MECHANICAL_FIST_CHANCE * exoticMultiplier);
	}

	static boolean shouldSpawnDarkMechanicalFist(float roll, float exoticMultiplier) {
		return roll < darkMechanicalFistChance(exoticMultiplier);
	}

	static Mob createMechanicalFist(float roll, float exoticMultiplier) {
		return shouldSpawnDarkMechanicalFist(roll, exoticMultiplier)
				? new DarkMechanicalFist()
				: new MechanicalFist();
	}

	private static Mob createMechanicalFist() {
		return createMechanicalFist(Random.Float(), RatSkull.exoticChanceMultiplier());
	}

	static float myriadBlackShadowChance(float exoticMultiplier) {
		return Math.min(1f, BASE_MYRIAD_BLACK_SHADOW_CHANCE * exoticMultiplier);
	}

	static boolean shouldSpawnMyriadBlackShadow(float roll, float exoticMultiplier) {
		return roll < myriadBlackShadowChance(exoticMultiplier);
	}

	static Mob createAlienatedPrismaticGuard(float roll, float exoticMultiplier) {
		return shouldSpawnMyriadBlackShadow(roll, exoticMultiplier)
				? new MyriadBlackShadow()
				: new AlienatedPrismaticGuard();
	}

	private static Mob createAlienatedPrismaticGuard() {
		return createAlienatedPrismaticGuard(Random.Float(), RatSkull.exoticChanceMultiplier());
	}

	static float tapirCrocodileChance(float exoticMultiplier) {
		return Math.min(1f, BASE_TAPIR_CROCODILE_CHANCE * exoticMultiplier);
	}

	static boolean shouldSpawnTapirCrocodile(float roll, float exoticMultiplier) {
		return roll < tapirCrocodileChance(exoticMultiplier);
	}

	static Mob createMimicCrocodile(float roll, float exoticMultiplier) {
		return shouldSpawnTapirCrocodile(roll, exoticMultiplier)
				? new TapirCrocodile()
				: new MimicCrocodile();
	}

	private static Mob createMimicCrocodile() {
		return createMimicCrocodile(Random.Float(), RatSkull.exoticChanceMultiplier());
	}

	/** Creates one deterministic, non-recursive one-HP tower echo for a Myriad Black Shadow. */
	public static Mob createMyriadEcho() {
		return createMyriadEcho(Random.Float());
	}

	static Mob createMyriadEcho(float roll) {
		Mob mob;
		switch (MyriadEchoSelection.select(roll)) {
			case CAMOUFLAGE_GNOLL: mob = new CamouflageGnoll(); break;
			case CORPSE: mob = new Corpse(); break;
			case EARTHLY_SERPENT: mob = new EarthlySerpent(); break;
			case MECHANICAL_FIST: mob = new MechanicalFist(); break;
			case MIMIC_CROCODILE: mob = new MimicCrocodile(); break;
			case HEAVY_CRABIFICATION: mob = new HeavyCrabification(); break;
			case MARSH_SLIME: mob = new MarshSlime(); break;
			case RUNE_SPINNER: mob = new RuneSpinner(); break;
			case CHAIN_SHADOW_THIEF: mob = new ChainShadowThief(); break;
			case DEATH_BUTTERFLY: mob = new DeathButterfly(); break;
			default: throw new IllegalStateException("Unknown Myriad echo selection");
		}
		mob.HT = 1;
		mob.HP = 1;
		mob.EXP = 0;
		mob.alignment = com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY;
		mob.state = mob.HUNTING;
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(mob, MyriadEcho.class);
		return mob;
	}

	public static Mob createNaturalSpawn() {
		Mob mob;
		switch (select(Random.Float())) {
			case CAMOUFLAGE_GNOLL:
				mob = new CamouflageGnoll();
				break;
			case CORROSIVE_SWARM:
				mob = new CorrosiveSwarm();
				break;
			case CORPSE:
				mob = new Corpse();
				break;
			case EARTHLY_SERPENT:
				mob = new EarthlySerpent();
				break;
			case ROAST_LAMB_WARLOCK:
				mob = new RoastLambWarlock();
				break;
			case MECHANICAL_FIST:
				mob = createMechanicalFist();
				break;
			case MIMIC_CROCODILE:
				mob = createMimicCrocodile();
				break;
			case OBSCURA:
				mob = new Obscura();
				break;
			case ALIENATED_PRISMATIC_GUARD:
				mob = createAlienatedPrismaticGuard();
				break;
			case SOUL_COLLECTOR:
				mob = new SoulCollector();
				break;
			case HEAVY_CRABIFICATION:
				mob = new HeavyCrabification();
				break;
			case MARSH_SLIME:
				mob = new MarshSlime();
				break;
			case RUNE_SPINNER:
				mob = new RuneSpinner();
				break;
			case CHAIN_SHADOW_THIEF:
				mob = new ChainShadowThief();
				break;
			case DEATH_BUTTERFLY:
				mob = new DeathButterfly();
				break;
			default:
				throw new IllegalStateException("Unknown tower mob selection");
		}
		return prepareNaturalSpawn(mob);
	}

	enum Selection {
		CAMOUFLAGE_GNOLL,
		CORROSIVE_SWARM,
		CORPSE,
		EARTHLY_SERPENT,
		ROAST_LAMB_WARLOCK,
		MECHANICAL_FIST,
		MIMIC_CROCODILE,
		OBSCURA,
		ALIENATED_PRISMATIC_GUARD,
		SOUL_COLLECTOR,
		HEAVY_CRABIFICATION,
		MARSH_SLIME,
		RUNE_SPINNER,
		CHAIN_SHADOW_THIEF,
		DEATH_BUTTERFLY
	}

	private enum MyriadEchoSelection {
		CAMOUFLAGE_GNOLL,
		CORPSE,
		EARTHLY_SERPENT,
		MECHANICAL_FIST,
		MIMIC_CROCODILE,
		HEAVY_CRABIFICATION,
		MARSH_SLIME,
		RUNE_SPINNER,
		CHAIN_SHADOW_THIEF,
		DEATH_BUTTERFLY;

		private static MyriadEchoSelection select(float roll) {
			MyriadEchoSelection[] values = values();
			int index = (int) (roll * values.length);
			index = Math.max(0, Math.min(index, values.length - 1));
			return values[index];
		}
	}
}
