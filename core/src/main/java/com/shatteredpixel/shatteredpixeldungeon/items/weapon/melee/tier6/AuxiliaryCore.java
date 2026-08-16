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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class AuxiliaryCore extends MeleeWeapon {

	public static final int TIER = 6;
	public static final int RANGE = 1;
	public static final float DELAY = 1f;
	private static final int BASE_MAGIC_DAMAGE = 10;
	private static final int BOOSTED_MAGIC_DAMAGE = 20;
	private static final int ABILITY_CHARGE_COST = 1;
	private static final float BOOST_DURATION = 2f;

	public static final String AC_INFUSE = "INFUSE";

	{
		image = EXItemSpriteSheet.AUXILIARY_CORE;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1.1f;

		tier = TIER;
		RCH = RANGE;
		DLY = DELAY;
	}

	@Override
	public int min(int lvl) {
		return minForLevel(lvl);
	}

	@Override
	public int max(int lvl) {
		return maxForLevel(lvl);
	}

	@Override
	public int STRReq(int lvl) {
		int requirement = strengthRequirementForLevel(lvl);
		if (masteryPotionBonus) requirement -= 2;
		return requirement;
	}

	public static int minForLevel(int level) {
		return 6 + Math.max(0, level);
	}

	public static int maxForLevel(int level) {
		return 25 + 7 * Math.max(0, level);
	}

	public static int strengthRequirementForLevel(int level) {
		return STRReq(TIER, level);
	}

	public static int magicDamage(boolean boosted) {
		return boosted ? BOOSTED_MAGIC_DAMAGE : BASE_MAGIC_DAMAGE;
	}

	public static int abilityChargeCost() {
		return ABILITY_CHARGE_COST;
	}

	public static float boostDuration() {
		return BOOST_DURATION;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int result = super.proc(attacker, defender, damage);
		if (defender.isAlive() && defender.alignment != Char.Alignment.ALLY) {
			defender.damage(magicDamage(attacker.buff(MagicPowerBoost.class) != null),
					this, DamageTag.MAGICAL);
		}
		return result;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_INFUSE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (AC_INFUSE.equals(action)) {
			curUser = hero;
			GameScene.selectItem(infusionSelector);
		}
	}

	public boolean canInfuse(Item item) {
		if (item == null || item == this || !(item instanceof Weapon)) return false;
		int targetTier;
		if (item instanceof MeleeWeapon) {
			targetTier = ((MeleeWeapon) item).tier;
		} else if (item instanceof MissileWeapon) {
			targetTier = ((MissileWeapon) item).tier;
		} else {
			return false;
		}
		return targetTier >= 1 && targetTier < TIER;
	}

	public void infuseWeapon(Weapon target) {
		target.augment = Weapon.Augment.MAGIC;
		target.updateQuickslot();
	}

	private final WndBag.ItemSelector infusionSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(AuxiliaryCore.this, "prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return canInfuse(item);
		}

		@Override
		public void onSelect(Item item) {
			if (item instanceof Weapon && canInfuse(item)) {
				applyInfusion(curUser, (Weapon) item);
			}
		}
	};

	private void applyInfusion(Hero hero, Weapon target) {
		infuseWeapon(target);
		consumeCore(hero);
		Sample.INSTANCE.play(Assets.Sounds.EVOKE);
		if (hero.sprite != null) hero.sprite.operate(hero.pos);
		GLog.p(Messages.get(this, "infused"), target.name());
		hero.spendAndNext(Actor.TICK);
	}

	private void consumeCore(Hero hero) {
		if (hero.belongings.weapon == this) {
			hero.belongings.weapon = null;
		} else if (hero.belongings.secondWep == this) {
			hero.belongings.secondWep = null;
		} else {
			detach(hero.belongings.backpack);
		}
		updateQuickslot();
	}

	@Override
	protected void duelistAbility(Hero hero, Integer ignored) {
		beforeAbilityUsed(hero, null);
		MagicPowerBoost existing = hero.buff(MagicPowerBoost.class);
		if (existing != null) existing.detach();
		Buff.affect(hero, MagicPowerBoost.class, BOOST_DURATION);
		if (hero.sprite != null) hero.sprite.operate(hero.pos);
		hero.next();
		afterAbilityUsed(hero);
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return ABILITY_CHARGE_COST;
	}

	@Override
	public String statsInfo() {
		return Messages.get(this, "stats_desc");
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, "ability_desc", (int) BOOST_DURATION+1,
				BASE_MAGIC_DAMAGE, BOOSTED_MAGIC_DAMAGE);
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(BOOSTED_MAGIC_DAMAGE);
	}

	public static class MagicPowerBoost extends FlavourBuff {
		{
			announced = true;
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.UPGRADE;
		}
	}
}
