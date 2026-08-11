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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TwoHandedGreatsword extends Greatsword {

	private boolean forceEnchantmentProc;

	{
		image = EXItemSpriteSheet.TWO_HANDED_GREATSWORD;
		tier = 6;
		RCH = 1;
		DLY = 0.5f;
	}

	public static boolean isEquippedBy(Hero hero) {
		return hero != null && hero.belongings != null && extraActionDelay(
				hero.belongings.weapon instanceof TwoHandedGreatsword,
				hero.belongings.secondWep instanceof TwoHandedGreatsword) > 0f;
	}

	public static float extraActionDelay(Hero hero) {
		return isEquippedBy(hero) ? 1f : 0f;
	}

	static float extraActionDelay(boolean inPrimaryWeaponSlot, boolean inSecondaryWeaponSlot) {
		return inPrimaryWeaponSlot || inSecondaryWeaponSlot ? 1f : 0f;
	}

	@Override
	protected void duelistAbility(final Hero hero, Integer ignored) {
		final ArrayList<Char> targets = trainSlashTargets(hero);
		if (targets.isEmpty()) {
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}

		final Char firstTarget = targets.get(0);
		hero.sprite.attack(hero.pos, new Callback() {
			@Override
			public void call() {
				beforeAbilityUsed(hero, firstTarget);
				forceEnchantmentProc = true;
				try {
					for (Char target : targets) {
						hero.chooseEnemy(target);
						hero.attack(target, 1f, 0f, Char.INFINITE_ACCURACY);
						if (!target.isAlive()) {
							onAbilityKill(hero, target);
						}
					}
				} finally {
					forceEnchantmentProc = false;
				}
				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
				afterAbilityUsed(hero);
			}
		});
	}

	private ArrayList<Char> trainSlashTargets(Hero hero) {
		ArrayList<Char> targets = new ArrayList<>();
		boolean projecting = hasEnchant(Projecting.class, hero);
		for (Char target : Actor.chars()) {
			if (target == hero || !target.isAlive() || hero.isCharmedBy(target)
					|| target.pos < 0 || target.pos >= Dungeon.level.length()) {
				continue;
			}
			int collisionPos = new Ballistica(hero.pos, target.pos, Ballistica.PROJECTILE).collisionPos;
			if (trainSlashTargetAllowed(Dungeon.level.heroFOV[target.pos],
					target.alignment == Char.Alignment.ENEMY, projecting,
					Dungeon.level.distance(hero.pos, target.pos), target.pos, collisionPos)) {
				targets.add(target);
			}
		}
		Collections.sort(targets, new Comparator<Char>() {
			@Override
			public int compare(Char left, Char right) {
				int distance = Integer.compare(Dungeon.level.distance(hero.pos, left.pos),
						Dungeon.level.distance(hero.pos, right.pos));
				return distance != 0 ? distance : Integer.compare(left.pos, right.pos);
			}
		});
		return targets;
	}

	static boolean trainSlashTargetAllowed(boolean visible, boolean enemy, boolean projecting,
			int distance, int targetCell, int collisionCell) {
		return visible && enemy && distance <= 2 && (projecting || targetCell == collisionCell);
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (!forceEnchantmentProc || enchantment == null) {
			return super.proc(attacker, defender, damage);
		}

		Weapon.Enchantment.forceProc(enchantment);
		try {
			return super.proc(attacker, defender, damage);
		} finally {
			Weapon.Enchantment.clearForcedProc(enchantment);
		}
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, levelKnown ? "ability_desc" : "typical_ability_desc");
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Messages.get(this, "upgrade_ability_stat");
	}
}
