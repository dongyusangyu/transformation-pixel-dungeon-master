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
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class Gungnir extends MissileWeapon {

	public static final int TIER = 6;

	private boolean wasBleedingBeforeHit;
	private int bleedingTargetID;

	{
		image = EXItemSpriteSheet.GUNGNIR;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.9f;

		tier = TIER;
		sticky = false;
	}

	public static boolean canThrowWithCurrentHP(int hp) {
		return hp > 1;
	}

	public static int lifeCostForCurrentHP(int hp) {
		return Math.max(1, hp / 10);
	}

	public static int bleedForDamage(int damage) {
		return Math.max(1, Math.round(damage * 0.5f));
	}

	public static int healingForMaxHP(int maxHP) {
		return Math.max(1, maxHP / 10);
	}

	public static boolean qualifiesForBleedingKillHealing(boolean wasBleeding, boolean killed) {
		return wasBleeding && killed;
	}

	@Override
	public int defaultQuantity() {
		return 3;
	}

	@Override
	public void execute(Hero hero, String action) {
		if (AC_THROW.equals(action)) {
			if (!canThrowWithCurrentHP(hero.HP)) {
				usesTargeting = false;
				GLog.w(Messages.get(this, "not_enough_health"));
				QuickSlotButton.cancel();
				InventoryPane.cancelTargeting();
				return;
			}
			usesTargeting = true;
		}
		super.execute(hero, action);
	}

	@Override
	public void doThrow(Hero hero) {
		if (!canThrowWithCurrentHP(hero.HP)) {
			usesTargeting = false;
			GLog.w(Messages.get(this, "not_enough_health"));
			QuickSlotButton.cancel();
			InventoryPane.cancelTargeting();
			return;
		}
		super.doThrow(hero);
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		return Char.INFINITE_ACCURACY;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (attacker instanceof Hero) {
			Hero hero = (Hero) attacker;
			int lifeCost = lifeCostForCurrentHP(hero.HP);
			hero.HP -= lifeCost;
			hero.sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(lifeCost));
		}
		wasBleedingBeforeHit = defender.buff(Bleeding.class) != null;
		bleedingTargetID = wasBleedingBeforeHit ? defender.id() : 0;
		Buff.affect(defender, Bleeding.class).set(bleedForDamage(damage));
		return super.proc(attacker, defender, damage);
	}

	@Override
	protected void rangedHit(Char enemy, int cell) {
		decrementDurability();
		if (durability > 0) {
			recoverToHero();
		}
		clearBleedingKillTracking();
	}

	@Override
	protected void rangedMiss(int cell) {
		clearBleedingKillTracking();
		recoverToHero();
	}

	public void onDirectKill(Hero hero, Char enemy) {
		boolean killedBleedingTarget = wasBleedingBeforeHit && bleedingTargetID == enemy.id();
		clearBleedingKillTracking();
		if (qualifiesForBleedingKillHealing(killedBleedingTarget, !enemy.isAlive())) {
			hero.heal(healingForMaxHP(hero.HT));
		}
	}

	private void recoverToHero() {
		Hero hero = curUser instanceof Hero ? (Hero) curUser : Dungeon.hero;
		parent = null;
		if (!spawnedForEffect && hero != null && hero.belongings != null
				&& collect(hero.belongings.backpack)) {
			updateQuickslot();
			return;
		}

		if (!spawnedForEffect && hero != null) {
			Dungeon.level.drop(this, hero.pos).sprite.drop();
		}
	}

	private void clearBleedingKillTracking() {
		wasBleedingBeforeHit = false;
		bleedingTargetID = 0;
	}
}
