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
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.SlimeBall;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MercuryBlade extends MeleeWeapon {

	public static final String AC_SHOOT = "SHOOT";
	public static final int TIER = 6;
	public static final float DELAY = 0.8f;
	public static final int RANGE = 1;
	private static final float SOLIDIFICATION_DURATION = 4f;

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
		return 10 + 2 * Math.max(0, lvl);
	}

	public static int throwMaxForLevel(int lvl) {
		return 25 + 5 * Math.max(0, lvl);
	}

	public static int liquidMetalCost(boolean solidified) {
		return solidified ? 0 : 1;
	}

	public static float oozeDuration(boolean solidified) {
		return solidified ? 3f : 2f;
	}

	public static float solidificationDuration() {
		return SOLIDIFICATION_DURATION;
	}

	public static int abilityChargeCost() {
		return 1;
	}

	public static boolean defaultActionPrefersAbility(boolean canUseAbility,
			boolean solidified) {
		return canUseAbility && !solidified;
	}

	public static String equippedDefaultAction(boolean canUseAbility,
			boolean solidified) {
		return defaultActionPrefersAbility(canUseAbility, solidified)
				? AC_ABILITY : AC_SHOOT;
	}

	public static boolean actionUsesTargeting(String action) {
		return AC_SHOOT.equals(action);
	}

	public static boolean canShootAt(int userPos, int targetPos) {
		return userPos != targetPos;
	}

	@Override
	public String defaultAction() {
		Hero hero = Dungeon.hero;
		if (hero != null && isEquipped(hero)) {
			boolean solidified = hero.buff(MercurySolidification.class) != null;
			return equippedDefaultAction(canUseWeaponAbility(hero), solidified);
		}
		return super.defaultAction();
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) actions.add(AC_SHOOT);
		return actions;
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
		Buff.affect(hero, MercurySolidification.class, SOLIDIFICATION_DURATION);

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
		return Messages.get(this, "stats_desc");
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, "ability_desc", (int) SOLIDIFICATION_DURATION);
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString((int) SOLIDIFICATION_DURATION);
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

		{
			announced = true;
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.IMBUE;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.55f, 0.9f, 1f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0f,
					(SOLIDIFICATION_DURATION - visualcooldown()) / SOLIDIFICATION_DURATION);
		}
	}
}
