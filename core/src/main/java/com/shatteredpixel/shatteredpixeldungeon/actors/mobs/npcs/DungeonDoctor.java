/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WandmakerSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDungeonDoctor;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.Bundle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DungeonDoctor extends NPC {

	private static final String TALENT_RESET_USED = "talent_reset_used";

	private boolean talentResetUsed;

	{
		spriteClass = WandmakerSprite.class;
		properties.add(Property.IMMOVABLE);
	}

	@Override
	public int defenseSkill(Char enemy) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage(int dmg, Object src, DamageTag... damageTags) {
		// The Dungeon Doctor cannot be damaged.
	}

	@Override
	public boolean add(Buff buff) {
		return false;
	}

	public boolean canResetTalentPoints() {
		return !talentResetUsed;
	}

	public static int investedTalentPoints(Hero hero) {
		return hero == null ? 0 : investedTalentPoints(hero.talents);
	}

	static int investedTalentPoints(List<LinkedHashMap<Talent, Integer>> talents) {
		int total = 0;
		for (LinkedHashMap<Talent, Integer> tier : talents) {
			for (int points : tier.values()) {
				total += Math.max(0, points);
			}
		}
		return total;
	}

	public int resetTalentPoints(Hero hero) {
		if (hero == null) {
			return 0;
		}
		int resetPoints = resetTalentPoints(hero.talents);
		if (resetPoints > 0) {
			Talent.onTalentUpgraded(hero, null);
		}
		return resetPoints;
	}

	int resetTalentPoints(List<LinkedHashMap<Talent, Integer>> talents) {
		if (talentResetUsed) {
			return 0;
		}
		int resetPoints = investedTalentPoints(talents);
		if (resetPoints == 0) {
			return 0;
		}

		for (LinkedHashMap<Talent, Integer> tier : talents) {
			for (Map.Entry<Talent, Integer> talent : tier.entrySet()) {
				talent.setValue(0);
			}
		}
		talentResetUsed = true;
		return resetPoints;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TALENT_RESET_USED, talentResetUsed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		talentResetUsed = bundle.getBoolean(TALENT_RESET_USED);
	}

	@Override
	public boolean interact(Char c) {
		if (sprite != null) {
			sprite.turnTo(pos, c.pos);
		}
		if (c == Dungeon.hero) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndDungeonDoctor(DungeonDoctor.this));
				}
			});
		}
		return true;
	}
}
