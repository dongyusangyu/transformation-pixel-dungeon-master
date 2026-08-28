/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MagicalRangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDeepSleep;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.TapirCrocodileSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.List;

public class TapirCrocodile extends MimicCrocodile implements MagicalRangedAttack {

	private static final float TIME_TO_ZAP = 1f;
	private static final int AMBUSH_DAMAGE = 60;
	private static final int ZAP_DAMAGE_MIN = 20;
	private static final int ZAP_DAMAGE_MAX = 30;
	private static final float NORMAL_SLEEP_CHANCE = 0.5f;
	private static final float DEEP_SLEEP_STONE_CHANCE = 1f / 2f;
	private static final float SHOCKING_BREW_CHANCE = 1f / 12f;

	{
		spriteClass = TapirCrocodileSprite.class;
		loot = null;
		lootChance = 0f;
	}

	protected void onNoValidTarget() {
		// Lurking is an opening state: once revealed, this mob never hides again.
	}

	protected boolean isSleepingTarget(Char target) {
		return target != null && (target.buff(Sleep.class) != null
				|| target.buff(MagicalSleep.class) != null
				|| target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
				&& ((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target).state
				== ((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target).SLEEPING);
	}

	@Override
	protected boolean canAttack(Char target) {
		if (isSleepingTarget(target)) {
			return Dungeon.level != null && Dungeon.level.adjacent(pos, target.pos);
		}
		return super.canAttack(target);
	}

	@Override
	protected boolean doAttack(Char target) {
		if (isLurking() && !canRangedAttack(target)) {
			beginAmbushAttack();
			finishAmbushAttack();
			return performAttack(target);
		}
		return super.doAttack(target);
	}

	@Override
	public boolean doRangedAttack(final Char target) {
		final boolean ambush = isLurking() || isAmbushAttackPending();
		if (isLurking()) {
			beginAmbushAttack();
		}
		spend(TIME_TO_ZAP);

		if (sprite != null && target != null && target.sprite != null
				&& (sprite.visible || target.sprite.visible)) {
			sprite.zap(target.pos, new Callback() {
				@Override
				public void call() {
					launchWardingMissile(target, ambush);
				}
			});
			return false;
		}

		resolveRangedAttack(target, ambush);
		return true;
	}

	protected void launchWardingMissile(final Char target, final boolean ambush) {
		if (sprite == null || sprite.parent == null || target == null) {
			resolveRangedAttack(target, ambush);
			next();
			return;
		}
		MagicMissile.boltFromChar(sprite.parent, missileType(ambush), sprite,
				target.pos, new Callback() {
					@Override
					public void call() {
						resolveRangedAttack(target, ambush);
						next();
					}
				});
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	protected int missileType(boolean ambush) {
		return MagicMissile.WARD;
	}

	protected void resolveRangedAttack(Char target, boolean ambush) {
		if (isSleepingTarget(target)) {
			finishAmbushAttack();
			return;
		}
		if (target != null && target.isAlive() && rangedHit(target)) {
			resolveZapHit(target, ambush, ambush || normalZapSleeps(Random.Float()));
		} else if (target != null) {
			showRangedMiss(target);
		}
		finishAmbushAttack();
	}

	protected void resolveZapHit(Char target, boolean ambush, boolean sleepRoll) {
		if (target == null || !target.isAlive() || isSleepingTarget(target)) {
			return;
		}
		int damage = ambush ? AMBUSH_DAMAGE : normalZapDamage(Random.Float());
		target.damage(damage, new MentalShock(), DamageTag.MAGICAL);
		if (target.isAlive() && sleepRoll) {
			Buff.affect(target, MagicalSleep.class);
		}
	}

	protected int normalZapDamage(float roll) {
		int value = ZAP_DAMAGE_MIN + (int) (Math.max(0f, Math.min(0.999999f, roll))
				* (ZAP_DAMAGE_MAX - ZAP_DAMAGE_MIN + 1));
		return Math.min(ZAP_DAMAGE_MAX, value);
	}

	protected boolean normalZapSleeps(float roll) {
		return roll < NORMAL_SLEEP_CHANCE;
	}

	protected float baseCombinedLootChance() {
		return DEEP_SLEEP_STONE_CHANCE + SHOCKING_BREW_CHANCE
				- DEEP_SLEEP_STONE_CHANCE * SHOCKING_BREW_CHANCE;
	}

	@Override
	public float lootChance() {
		return adjustedLootChance(baseCombinedLootChance());
	}

	protected float lootOutcomeRoll() {
		return Random.Float();
	}

	protected List<Item> createLootOutcome(float roll) {
		ArrayList<Item> result = new ArrayList<>(2);
		for (Class<? extends Item> lootClass : lootClassesForRoll(roll)) {
			result.add(Reflection.newInstance(lootClass));
		}
		return result;
	}

	protected List<Class<? extends Item>> lootClassesForRoll(float roll) {
		ArrayList<Class<? extends Item>> result = new ArrayList<>(2);
		if (roll < 11f / 13f) {
			result.add(StoneOfDeepSleep.class);
		} else if (roll < 12f / 13f) {
			result.add(ShockingBrew.class);
		} else {
			result.add(StoneOfDeepSleep.class);
			result.add(ShockingBrew.class);
		}
		return result;
	}

	@Override
	public Item createLoot() {
		List<Item> outcome = createLootOutcome(lootOutcomeRoll());
		return outcome.isEmpty() ? null : outcome.get(0);
	}

	@Override
	protected ArrayList<Item> createLootDrops() {
		return new ArrayList<>(createLootOutcome(lootOutcomeRoll()));
	}

	protected void dropAdditionalLoot(Item item) {
		if (Dungeon.level != null && item != null) {
			Dungeon.level.drop(item, pos).sprite.drop();
		}
	}

	public static class MentalShock {
	}
}
