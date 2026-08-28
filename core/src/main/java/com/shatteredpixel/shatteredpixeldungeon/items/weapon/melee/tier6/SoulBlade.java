/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class SoulBlade extends MeleeWeapon {

	public static final int TIER = 6;
	private static final float DUSK_ATTACK_SPEED = 1.5f;
	private static final int DUSK_ATTACKS = 3;
	private static final String DUSK_REMAINING = "dusk_remaining";
	private static final String SHIELD_READY_AT = "shield_ready_at";

	private transient boolean duskAttackPending;
	private transient Char pendingTarget;
	private transient int pendingSoulDamage;
	private transient boolean pendingShield;
	private float shieldReadyAt;

	{
		image = EXItemSpriteSheet.SOUL_BLADE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.05f;
		tier = TIER;
		ACC = 1f;
		DLY = 1f;
		RCH = 1;
	}

	@Override
	public int min(int level) {
		return 6 + level;
	}

	@Override
	public int max(int level) {
		return 24 + 6 * level;
	}

	@Override
	public int STRReq(int level) {
		return STRReq(tier, level);
	}

	@Override
	public int defenseFactor(Char owner) {
		return DRMax();
	}

	public int DRMax() {
		return maxBlockForLevel(buffedLvl());
	}

	public int DRMax(int level) {
		return maxBlockForLevel(level);
	}

	public static int maxBlockForLevel(int level) {
		return 6 + 2 * level;
	}

	public static float soulProcChance(int level) {
		return Math.max(0f, Math.min(1f, (level + 5) / 25f));
	}

	public static int shieldAmountForMaxHealth(int maxHealth, int level) {
		int effectiveLevel = Math.max(0, level);
		return Math.max(1, 15 + (int) (Math.max(0, maxHealth) * effectiveLevel / 100f));
	}

	public static int shieldCooldown(int level) {
		return Math.max(1, 25 - Math.max(0, level));
	}

	private static float gameTime() {
		return Statistics.duration + Actor.now();
	}

	public int shieldCooldownRemaining() {
		return Math.max(0, (int) Math.ceil(shieldReadyAt - gameTime()));
	}

	private boolean shieldReady() {
		return shieldReadyAt <= gameTime() + 0.0001f;
	}

	public static int soulDamageForTarget(int targetMaxHealth, boolean bossLike) {
		int damage = Math.max(1, (int) Math.floor(targetMaxHealth * 0.10f));
		if (bossLike) damage = Math.max(1, damage / 3);
		return damage;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int result = super.proc(attacker, defender, damage);
		pendingTarget = null;
		pendingSoulDamage = 0;
		pendingShield = false;

		if (attacker instanceof Hero
				&& ((Hero) attacker).belongings.attackingWeapon() == this
				&& defender.alignment == Char.Alignment.ENEMY
				&& defender.isAlive()) {
			int level = buffedLvl();
			if (Random.Float() < soulProcChance(level)) {
				boolean bossLike = defender.properties().contains(Char.Property.MINIBOSS)
						|| defender.properties().contains(Char.Property.BOSS);
				pendingSoulDamage = soulDamageForTarget(defender.HT, bossLike);
			}
			pendingShield = shieldReady();
			if (pendingSoulDamage > 0 || pendingShield) {
				pendingTarget = defender;
			}
		}
		return result;
	}

	@Override
	public void afterHeroAttack(Hero hero, Char target, boolean hit) {
		if (hit && target == pendingTarget) {
			if (pendingSoulDamage > 0 && target.isAlive()) {
				target.damage(pendingSoulDamage, this, DamageTag.MAGICAL);
			}
			if (pendingShield) {
				Buff.affect(hero, Barrier.class).setShield(
						shieldAmountForMaxHealth(hero.HT, buffedLvl()));
				shieldReadyAt = gameTime() + shieldCooldown(buffedLvl());
			}
		}
		pendingTarget = null;
		pendingSoulDamage = 0;
		pendingShield = false;
	}

	@Override
	public float delayFactor(Char owner) {
		float delay = super.delayFactor(owner);
		if (duskAttackPending && owner instanceof Hero
				&& ((Hero) owner).belongings.attackingWeapon() == this) {
			return delay / DUSK_ATTACK_SPEED;
		}
		return delay;
	}

	@Override
	public int reachFactor(Char owner) {
		int reach = super.reachFactor(owner);
		if (owner instanceof Hero
				&& ((Hero) owner).belongings.attackingWeapon() == this
				&& ((Hero) owner).buff(DuskBuff.class) != null
				&& ((Hero) owner).buff(DuskBuff.class).remainingAttacks() > 0) {
			reach++;
		}
		return reach;
	}

	@Override
	public void beforeHeroAttack(Hero hero, Char target) {
		DuskBuff dusk = hero.buff(DuskBuff.class);
		duskAttackPending = dusk != null && dusk.remainingAttacks() > 0
				&& hero.belongings.attackingWeapon() == this;
	}

	@Override
	public void afterHeroAttackDelayResolved(Hero hero) {
		if (duskAttackPending) {
			DuskBuff dusk = hero.buff(DuskBuff.class);
			if (dusk != null) dusk.consumeAttack();
		}
		duskAttackPending = false;
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		beforeAbilityUsed(hero, null);
		Buff.affect(hero, DuskBuff.class).refreshAttacks();
		hero.sprite.operate(hero.pos);
		hero.next();
		afterAbilityUsed(hero);
	}

	@Override
	public String statsInfo() {
		String cooldown = shieldCooldownRemaining() > 0
				? Messages.get(this, "shield_cooldown", shieldCooldownRemaining())
				: Messages.get(this, "shield_ready");
		return Messages.get(this, "stats_desc", cooldown);
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, "ability_desc");
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SHIELD_READY_AT, shieldReadyAt);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		shieldReadyAt = Math.max(0f, bundle.getFloat(SHIELD_READY_AT));
	}

	public static class DuskBuff extends Buff {

		{
			type = buffType.POSITIVE;
			announced = true;
		}

		private int remainingAttacks;

		public void refreshAttacks() {
			remainingAttacks = DUSK_ATTACKS;
			BuffIndicator.refreshHero();
		}

		public boolean consumeAttack() {
			if (remainingAttacks <= 0) return false;
			remainingAttacks--;
			if (remainingAttacks <= 0 && target != null) detach();
			BuffIndicator.refreshHero();
			return true;
		}

		public int remainingAttacks() {
			return remainingAttacks;
		}

		@Override
		public int icon() {
			return BuffIndicator.HASTE;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.55f, 0.75f, 1f);
		}

		@Override
		public String name() {
			return Messages.get(SoulBlade.class, "dusk_name");
		}

		@Override
		public String desc() {
			return Messages.get(SoulBlade.class, "dusk_desc", remainingAttacks);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(remainingAttacks);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(DUSK_REMAINING, remainingAttacks);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			remainingAttacks = Math.max(0, Math.min(DUSK_ATTACKS, bundle.getInt(DUSK_REMAINING)));
		}
	}
}
