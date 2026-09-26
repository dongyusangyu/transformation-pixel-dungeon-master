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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WeaponSpecialAction;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.SlimeBall;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MercuryBlade extends MeleeWeapon implements WeaponSpecialAction {

	public static final String AC_SHOOT = "SHOOT";
	public static final int TIER = 6;
	public static final float DELAY = 0.8f;
	public static final int RANGE = 1;
	private static final int SOLIDIFICATION_DISPLAY_BASE = 2;

	{
		image = EXItemSpriteSheet.MERCURY_BLADE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.2f;

		tier = TIER;
		DLY = DELAY;
		RCH = RANGE;
		usesTargeting = false;
	}

	@Override
	public int min(int lvl) {
		return minForLevel(lvl);
	}

	@Override
	public int max(int lvl) {
		return maxForLevel(lvl);
	}

	public static int minForLevel(int lvl) {
		return 6 + Math.max(0, lvl);
	}

	public static int maxForLevel(int lvl) {
		return 28 + 7 * Math.max(0, lvl);
	}

	public static int strengthRequirementForLevel(int lvl) {
		return STRReq(TIER, lvl);
	}

	public static int throwMinForLevel(int lvl) {
		return 8 + 2 * Math.max(0, lvl);
	}

	public static int throwMaxForLevel(int lvl) {
		return 20 + 4 * Math.max(0, lvl);
	}

	public static int solidificationDurationForLevel(int lvl) {
		return SOLIDIFICATION_DISPLAY_BASE + Math.max(0, lvl);
	}

	private static float solidificationBuffDurationForLevel(int lvl) {
		return Math.max(1f, solidificationDurationForLevel(lvl) - 1f);
	}

	public static int liquidMetalCost(boolean solidified) {
		return solidified ? 0 : 1;
	}

	public static float oozeDuration(boolean solidified) {
		return solidified ? 3f : 2f;
	}

	public static float solidificationDuration() {
		return solidificationDurationForLevel(0);
	}

	public static int abilityChargeCost() {
		return 1;
	}

	public static boolean actionUsesTargeting(String action) {
		return AC_SHOOT.equals(action);
	}

	public static boolean canShootAt(int userPos, int targetPos) {
		return userPos != targetPos;
	}

	@Override
	public String specialActionId() {
		return AC_SHOOT;
	}

	@Override
	public int indicatorColor() {
		return 0x95F1F5;
	}

	@Override
	public void execute(final Hero hero, String action) {
		usesTargeting = actionUsesTargeting(action);
		super.execute(hero, action);

		if (AC_SHOOT.equals(action) && isEquipped(hero)) {
			GameScene.selectCell(new CellSelector.Listener() {
				@Override
				public void onSelect(Integer cell) {
					if (cell != null) shoot(hero, cell);
				}

				@Override
				public String prompt() {
					return Messages.get(MercuryBlade.this, "prompt");
				}
			});
		}
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		if (isEquipped(user)) return new MercuryProjectile().targetingPos(user, dst);
		return super.targetingPos(user, dst);
	}

	private void shoot(Hero user, int dst) {
		if (!canShootAt(user.pos, dst)) {
			GLog.w(Messages.get(this, "self_target"));
			return;
		}
		if (!tryConsumeLiquidMetal(user)) {
			GLog.w(Messages.get(this, "no_liquid_metal"));
			return;
		}

		new MercuryProjectile().cast(user, dst);
		updateQuickslot();
	}

	boolean tryConsumeLiquidMetal(Hero user) {
		if (user.buff(MercurySolidification.class) != null) return true;
		LiquidMetal metal = user.belongings.getItem(LiquidMetal.class);
		if (metal == null || metal.quantity() <= 0) return false;
		metal.detach(user.belongings.backpack);
		return true;
	}

	@Override
	protected void duelistAbility(Hero hero, Integer ignored) {
		beforeAbilityUsed(hero, null);

		MercurySolidification existing = hero.buff(MercurySolidification.class);
		if (existing != null) existing.detach();
		Buff.affect(hero, MercurySolidification.class,
				solidificationBuffDurationForLevel(buffedLvl()));
		hero.buff(MercurySolidification.class).captureInitialDuration();

		hero.sprite.operate(hero.pos);
		hero.next();
		afterAbilityUsed(hero);
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return abilityChargeCost();
	}

	@Override
	public String statsInfo() {
		int level = levelKnown ? buffedLvl() : 0;
		String key = levelKnown ? "stats_desc" : "typical_stats_desc";
		return Messages.get(this, key,
				augment.damageFactor(throwMinForLevel(level)),
				augment.damageFactor(throwMaxForLevel(level)));
	}

	@Override
	public String abilityInfo() {
		int level = levelKnown ? buffedLvl() : 0;
		return Messages.get(this, levelKnown ? "ability_desc" : "typical_ability_desc",
				solidificationDurationForLevel(level));
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(solidificationDurationForLevel(level));
	}

	@Override
	public java.util.ArrayList<UpgradeAbilityStat> upgradeAbilityStats(int level) {
		java.util.ArrayList<UpgradeAbilityStat> result = new java.util.ArrayList<>();
		result.add(abilityStat(UpgradeAbilityStatType.DURATION, upgradeAbilityStat(level)));
		return result;
	}

	@Override
	public java.util.ArrayList<UpgradeAbilityStat> upgradeFeatureStats(int level) {
		java.util.ArrayList<UpgradeAbilityStat> result = new java.util.ArrayList<>();
		result.add(abilityStat(UpgradeAbilityStatType.BLADE_SHADOW_DAMAGE,
				augment.damageFactor(throwMinForLevel(level)) + "-"
						+ augment.damageFactor(throwMaxForLevel(level))));
		return result;
	}

	public class MercuryProjectile extends MissileWeapon implements MissileWeapon.QianfaRepeatProjectile {

		{
			image = MercuryBlade.this.image;
			tier = 5;
			augment = MercuryBlade.this.augment;
			hitSound = Assets.Sounds.HIT_SLASH;
			hitSoundPitch = 1.2f;
			setID = 0;
			spawnedForEffect = true;
		}

		@Override
		public int defaultQuantity() {
			return 1;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}

		@Override
		public int min(int lvl) {
			return throwMinForLevel(lvl);
		}

		@Override
		public int max(int lvl) {
			return throwMaxForLevel(lvl);
		}

		@Override
		public int buffedLvl() {
			return MercuryBlade.this.buffedLvl();
		}

		@Override
		public boolean benefitsFromSharpshooting() {
			return false;
		}

		@Override
		public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
			return MercuryBlade.this.hasEnchant(type, owner);
		}

		@Override
		public Enchantment getEnchant() {
			return MercuryBlade.this.getEnchant();
		}

		@Override
		public int proc(Char attacker, Char defender, int damage) {
			int result = MercuryBlade.this.proc(attacker, defender, damage);
			boolean solidified = attacker.buff(MercurySolidification.class) != null;
			Buff.affect(defender, SlimeBall.SlimeOoze.class, oozeDuration(solidified));
			if (defender.sprite != null) {
				defender.sprite.showStatus(CharSprite.WARNING,
						Messages.get(SlimeBall.SlimeOoze.class, "name"));
			}
			return result;
		}

		@Override
		public float delayFactor(Char user) {
			return MercuryBlade.this.delayFactor(user);
		}

		@Override
		public int STRReq(int lvl) {
			return MercuryBlade.this.STRReq();
		}

		@Override
		protected MissileWeapon createPhantomProjectile() {
			return markAsPhantom(MercuryBlade.this.new MercuryProjectile());
		}

		@Override
		protected void onThrow(int cell) {
			super.onThrow(cell);
			if (Actor.findChar(cell) == null) Splash.at(cell, 0xCCB9FFFF, 2);
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play(Assets.Sounds.ATK_SPIRITBOW, 1f,
					Random.Float(0.92f, 1.08f));
		}
	}

	public static class MercurySolidification extends FlavourBuff {
		private static final String INITIAL_DURATION = "initial_duration";
		private float initialDuration;

		{
			announced = false;
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.MERCURY_SOLIDIFICATION;
		}

		public void captureInitialDuration() {
			initialDuration = Math.max(1f, visualcooldown());
		}

		@Override
		public float iconFadePercent() {
		return initialDuration <= 0f ? 0f : Math.max(0f, Math.min(1f,
				(initialDuration - visualcooldown()) / initialDuration));
		}

		@Override
		public void storeInBundle(com.watabou.utils.Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(INITIAL_DURATION, initialDuration);
		}

		@Override
		public void restoreFromBundle(com.watabou.utils.Bundle bundle) {
			super.restoreFromBundle(bundle);
			initialDuration = bundle.getFloat(INITIAL_DURATION);
			if (initialDuration <= 0f) captureInitialDuration();
		}
	}
}
