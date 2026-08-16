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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.RuneWeb;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.RuneSpinnerSprite;
import com.watabou.utils.Random;

public class RuneSpinner extends Spinner {

	private static final float ARMOR_CURSE_CHANCE = 0.01f;

	{
		spriteClass = RuneSpinnerSprite.class;

		HP = HT = 120;
		defenseSkill = 20;

		EXP = 13;
		maxLvl = 30;

		loot = null;
		lootChance = 0f;

		immunities.add(RuneWeb.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 35);
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 10);
	}

	@Override
	protected void applyWebToCell(int cell) {
		GameScene.add(Blob.seed(cell, RuneWeb.STRENGTH, RuneWeb.class));
	}

	@Override
	protected int modifyEnemyArmor(Char enemy, int armor) {
		prepareTargetArmorCurse(enemy);
		return super.modifyEnemyArmor(enemy, armor);
	}

	protected boolean prepareTargetArmorCurse(Char defender) {
		if (!(defender instanceof Hero) || defender != Dungeon.hero) {
			return false;
		}
		Armor armor = ((Hero) defender).belongings.armor();
		return prepareArmorCurse(armor, defender);
	}

	protected boolean prepareArmorCurse(Armor armor, Char defender) {
		if (armor == null) {
			return false;
		}

		if ((armor.glyph == null || !armor.glyph.curse()) && rollArmorCurse()) {
			Armor.Glyph curse = randomArmorCurse();
			if (curse != null) {
				armor.inscribe(curse);
				armor.cursed = true;
			}
		}

		if (armor.glyph != null && armor.glyph.curse()) {
			Armor.Glyph.forceNextCurseProc(defender, armor.glyph);
			return true;
		}
		return false;
	}

	protected boolean rollArmorCurse() {
		return curseRollSucceeds(Random.Float());
	}

	static boolean curseRollSucceeds(float roll) {
		return roll < ARMOR_CURSE_CHANCE;
	}

	protected Armor.Glyph randomArmorCurse() {
		return Armor.Glyph.randomCurse();
	}

	@Override
	public int attackProc(Char enemy, int damage, DamageTag... damageTags) {
		try {
			return super.attackProc(enemy, damage, damageTags);
		} finally {
			Armor.Glyph.clearForcedCurseProc(enemy);
		}
	}
}
