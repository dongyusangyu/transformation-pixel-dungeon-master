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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.PhantomMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.MimicCrocodileSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class MimicCrocodile extends Mob {

	public static final float LURKING_ALPHA = 0.2f;
	private static final int NORMAL_DAMAGE_MIN = 20;
	private static final int NORMAL_DAMAGE_MAX = 30;
	private static final int AMBUSH_DAMAGE = 60;
	private static final float CRIPPLE_DURATION = 3f;
	private static final float MYSTERY_MEAT_DROP_CHANCE = 1f / 4f;
	private static final float PHANTOM_MEAT_DROP_CHANCE = 1f / 100f;

	private static final String LURKING = "lurking";
	private static final String AMBUSH_ATTACK = "ambush_attack";

	private boolean lurking = true;
	private boolean ambushAttack;

	{
		spriteClass = MimicCrocodileSprite.class;

		HP = HT = 100;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = null;
		lootChance = 0f;
		state = WANDERING;
	}

	@Override
	public int damageRoll() {
		return ambushAttack
				? AMBUSH_DAMAGE
				: Random.NormalIntRange(normalDamageMin(), normalDamageMax());
	}

	protected int normalDamageMin() {
		return NORMAL_DAMAGE_MIN;
	}

	protected int normalDamageMax() {
		return NORMAL_DAMAGE_MAX;
	}

	@Override
	public int attackSkill(Char target) {
		return ambushAttack ? INFINITE_ACCURACY : 40;
	}

	@Override
	public int drRoll() {
		return armorRoll();
	}

	protected int armorRoll() {
		return Random.NormalIntRange(0, 10);
	}

	@Override
	public float lootChance() {
		return adjustedLootChance(
				MYSTERY_MEAT_DROP_CHANCE + PHANTOM_MEAT_DROP_CHANCE);
	}

	protected float lootSelectionRoll() {
		return Random.Float();
	}

	@Override
	public Item createLoot() {
		float totalChance = MYSTERY_MEAT_DROP_CHANCE + PHANTOM_MEAT_DROP_CHANCE;
		return lootSelectionRoll() < PHANTOM_MEAT_DROP_CHANCE / totalChance
				? createPhantomMeatLoot()
				: createMysteryMeatLoot();
	}

	protected Item createPhantomMeatLoot() {
		return new PhantomMeat();
	}

	protected Item createMysteryMeatLoot() {
		return new MysteryMeat();
	}

	@Override
	protected boolean doAttack(Char enemy) {
		beginAmbushAttack();
		boolean completed = performAttack(enemy);
		if (completed) {
			finishAmbushAttack();
		}
		return completed;
	}

	protected boolean performAttack(Char enemy) {
		return super.doAttack(enemy);
	}

	@Override
	public void onAttackComplete() {
		super.onAttackComplete();
		finishAmbushAttack();
	}

	protected void beginAmbushAttack() {
		if (lurking) {
			lurking = false;
			ambushAttack = true;
			if (sprite != null) {
				sprite.idle();
			}
		}
	}

	protected void finishAmbushAttack() {
		ambushAttack = false;
	}

	protected boolean isAmbushAttackPending() {
		return ambushAttack;
	}

	protected boolean isPlayerTarget(Char target) {
		return target == Dungeon.hero;
	}

	@Override
	protected void onAttackResolved(
			Char target, boolean hit, int damageDealt, DamageTag... damageTags) {
		super.onAttackResolved(target, hit, damageDealt, damageTags);
		if (hit && target != null && target.isAlive()) {
			Buff.affect(target, Cripple.class, CRIPPLE_DURATION);
			Bleeding bleeding = Buff.affect(target, Bleeding.class);
			bleeding.set(Math.max(1, Math.round(damageDealt * 0.5f)));
			if (ambushAttack && isPlayerTarget(target)) {
				bleeding.extend(20f);
			}
		}
		finishAmbushAttack();
	}

	public boolean isLurking() {
		return lurking;
	}

	@Override
	public boolean isVisibleEnemyForHero() {
		return !lurking;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LURKING, lurking);
		bundle.put(AMBUSH_ATTACK, ambushAttack);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lurking = bundle.getBoolean(LURKING);
		ambushAttack = bundle.getBoolean(AMBUSH_ATTACK);
	}
}
