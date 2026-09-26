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

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Mace;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scimitar;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.princess.KingBlade;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.watabou.utils.Callback;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Random;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class OracleTerminal extends MeleeWeapon {

	public static final int TIER = 6;
	public static final float FORM_DURATION = 10f;
	private static final String BLUNT_WEIGHT = "oracle_blunt_weight";
	private static final String SLASH_WEIGHT = "oracle_slash_weight";
	private static final String THRUST_WEIGHT = "oracle_thrust_weight";
	private static final String SCYTHE_WEIGHT = "oracle_scythe_weight";
	private int bluntWeight = 30, slashWeight = 30, thrustWeight = 30, scytheWeight = 10;

	public enum Form {
		BLUNT,
		SLASH,
		THRUST,
		SCYTHE,
		SPOON
	}

	{
		image = EXItemSpriteSheet.ORACLE_TERMINAL;
		tier = TIER;
		ACC = 1f;
		DLY = 1f;
		RCH = 1;
	}

	@Override
	public int min(int lvl) {
		return 6 + Math.max(0, lvl);
	}

	@Override
	public int max(int lvl) {
		return 30 + 7 * Math.max(0, lvl);
	}

	static Form formForRoll(int roll) {
		int normalized = Math.max(0, Math.min(99, roll));
		if (normalized < 30) return Form.BLUNT;
		if (normalized < 60) return Form.SLASH;
		if (normalized < 90) return Form.THRUST;
		return Form.SCYTHE;
	}

	static Form formForWeightedRoll(int roll, int blunt, int slash, int thrust, int scythe) {
		int total = Math.max(0, blunt) + Math.max(0, slash)
				+ Math.max(0, thrust) + Math.max(0, scythe);
		if (total <= 0) return Form.BLUNT;
		int value = Math.max(0, Math.min(total - 1, roll));
		if (value < blunt) return Form.BLUNT;
		value -= blunt;
		if (value < slash) return Form.SLASH;
		value -= slash;
		if (value < thrust) return Form.THRUST;
		return Form.SCYTHE;
	}

	private static int weightTotal(int blunt, int slash, int thrust, int scythe) {
		return Math.max(0, blunt) + Math.max(0, slash)
				+ Math.max(0, thrust) + Math.max(0, scythe);
	}

	private void rollNextForm(Hero hero) {
		Form next;
		if (spoonThresholdReached(hero.HP, hero.HT)) {
			next = Form.SPOON;
		} else {
			if (currentForm(hero) instanceof SpoonForm
					&& resetWeightsAfterRecovery(hero.HP, hero.HT)) resetWeights();
			next = formForWeightedRoll(Random.Int(weightTotal(
					bluntWeight, slashWeight, thrustWeight, scytheWeight)),
					bluntWeight, slashWeight, thrustWeight, scytheWeight);
			if (next == Form.SCYTHE) resetWeights();
			else {
				switch (next) {
					case BLUNT: bluntWeight = Math.max(0, bluntWeight - 5); break;
					case SLASH: slashWeight = Math.max(0, slashWeight - 5); break;
					case THRUST: thrustWeight = Math.max(0, thrustWeight - 5); break;
				}
				scytheWeight += 5;
			}
		}
		applyForm(hero, next);
	}

	static boolean spoonThresholdReached(int hp, int maxHealth) {
		return maxHealth > 0 && Math.max(0, hp) * 10 < maxHealth;
    }

	static boolean resetWeightsAfterRecovery(int hp, int maxHealth) {
		return maxHealth > 0 && Math.max(0, hp) * 10 >= maxHealth;
    }

	private void resetWeights() {
		bluntWeight = slashWeight = thrustWeight = 30;
		scytheWeight = 10;
	}

	private static OracleFormBuff applyForm(Char target, Form selected) {
		clearForms(target);
		Class<? extends OracleFormBuff> cls;
		switch (selected) {
			case BLUNT: cls = BluntForm.class; break;
			case SLASH: cls = SlashForm.class; break;
			case THRUST: cls = ThrustForm.class; break;
			case SCYTHE: cls = ScytheForm.class; break;
			default: cls = SpoonForm.class; break;
		}
		OracleFormBuff form = Buff.affect(target, cls);
		if (target instanceof Hero) {
			OracleTerminal weapon = equippedOracle((Hero) target);
			if (weapon != null) weapon.hitSound = soundForForm(selected);
		}
		form.startCycle();
		BuffIndicator.refreshHero();
		return form;
	}

	private static OracleTerminal equippedOracle(Hero hero) {
		if (hero.belongings.weapon() instanceof OracleTerminal) return (OracleTerminal) hero.belongings.weapon();
		if (hero.belongings.secondWep() instanceof OracleTerminal) return (OracleTerminal) hero.belongings.secondWep();
		return null;
	}

	static String soundForForm(Form form) {
		switch (form) {
			case BLUNT: return com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_CRUSH;
			case SLASH: return com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_SLASH;
			case THRUST: return com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
			case SCYTHE: return com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_SLASH;
			default: return com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
		}
	}

	static int thrustAbilityExtraRange(int level) {
		return 2 + Math.max(0, level) / 7;
	}

	static int swordDanceDurationForLevel(int level) {
		return 2 + Math.max(0, level);
	}

	static int thrustBleedForDamage(int damageDealt) {
		return Math.max(0, Math.round(Math.max(0, damageDealt) * 0.3f));
	}

	static int scytheDamage(int damage) {
		return Math.round(Math.max(0, damage) * 1.5f);
	}

	static boolean bluntTriggers(float roll) {
		return roll < 1f / 3f;
	}

	static boolean slashTriggers(float roll) {
		return roll < 1f / 3f;
	}

	static boolean requiresLivingTarget(Form form) {
		return form != Form.SLASH;
	}

	static int heavyBlowBonus(int level) {
		return 5 + Math.round(1.5f * Math.max(0, level));
	}

	public static boolean hasEquippedOracle(Hero hero) {
		return hero != null && hasEquippedOracle(
				hero.belongings.weapon(), hero.belongings.secondWep());
	}

	static boolean hasEquippedOracle(KindOfWeapon primary, KindOfWeapon secondary) {
		return primary instanceof OracleTerminal || secondary instanceof OracleTerminal;
	}

	static OracleFormBuff currentForm(Char target) {
		if (target == null) return null;
		OracleFormBuff form = target.buff(BluntForm.class);
		if (form == null) form = target.buff(SlashForm.class);
		if (form == null) form = target.buff(ThrustForm.class);
		if (form == null) form = target.buff(ScytheForm.class);
		if (form == null) form = target.buff(SpoonForm.class);
		return form;
	}

	static void clearForms(Char target) {
		if (target == null) return;
		Buff.detach(target, BluntForm.class);
		Buff.detach(target, SlashForm.class);
		Buff.detach(target, ThrustForm.class);
		Buff.detach(target, ScytheForm.class);
		Buff.detach(target, SpoonForm.class);
	}

	static OracleFormBuff applyFormForRoll(Char target, int roll) {
		return applyForm(target, formForRoll(roll));
	}

	public static Form currentFormType(Char owner) {
		OracleFormBuff form = currentForm(owner);
		return form == null ? null : form.form();
	}

	public static boolean hasForm(Char owner, Form form) {
		return currentFormType(owner) == form;
	}

	public static boolean spoonAbility(Hero hero) {
		return hasForm(hero, Form.SPOON);
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		float factor = super.accuracyFactor(owner, target);
		return hasForm(owner, Form.BLUNT) && owner instanceof Hero
				&& ((Hero) owner).belongings.attackingWeapon() == this ? factor * 1.2f : factor;
	}

	@Override
	public float delayFactor(Char owner) {
		float delay = super.delayFactor(owner);
		return hasForm(owner, Form.SLASH) && owner instanceof Hero
				&& ((Hero) owner).belongings.attackingWeapon() == this ? delay / 1.2f : delay;
	}

	@Override
	public int damageRoll(Char owner) {
		int damage = super.damageRoll(owner);
		return hasForm(owner, Form.SPOON) && owner instanceof Hero
				&& ((Hero) owner).belongings.attackingWeapon() == this
				? Math.round(damage * 0.5f) : damage;
	}

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		if (ch instanceof Hero && currentForm((Hero) ch) == null) {
			rollNextForm((Hero) ch);
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		boolean result = super.doUnequip(hero, collect, single);
		if (result && !hasEquippedOracle(hero)) clearForms(hero);
		return result;
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (spoonAbility(hero)) {
			Charger charge = hero.buff(Charger.class);
			if (charge == null || charge.charges < 1) return;
			beforeAbilityUsed(hero, null);
			charge.charges = 0;
			charge.partialCharge = 0;
			updateQuickslot();
			hero.heal(Math.max(1, Math.round(hero.HT * 0.1f)));
			if (hero.sprite != null) hero.sprite.operate(hero.pos);
			hero.next();
			afterAbilityUsed(hero);
			return;
		}
		if (hasForm(hero, Form.SCYTHE)) {
			Char enemy = target == null ? null : Actor.findChar(target);
			if (!isValidEnemy(hero, enemy)) {
				GLog.w(Messages.get(this, "ability_no_target"));
				return;
			}
			beforeAbilityUsed(hero, enemy);
			int bleed = 25 + 4 * Math.max(0, buffedLvl());
			if (enemy.isImmune(Bleeding.class)) enemy.damage(bleed, hero, DamageTag.PHYSICAL);
			else Buff.affect(enemy, Bleeding.class).set(bleed);
			hero.spendAndNext(Actor.TICK);
			afterAbilityUsed(hero);
			return;
		}
		if (hasForm(hero, Form.SLASH)) {
			beforeAbilityUsed(hero, null);
			Buff.prolong(hero, Scimitar.SwordDance.class, swordDanceDurationForLevel(buffedLvl()));
			if (hero.sprite != null) hero.sprite.operate(hero.pos);
			hero.next();
			afterAbilityUsed(hero);
			return;
		}
		if (hasForm(hero, Form.THRUST)) {
			piercingAbility(hero, target);
			return;
		}
		int damageBonus = augment.damageFactor(heavyBlowBonus(buffedLvl()));
		Mace.heavyBlowAbility(hero, target, 1f, damageBonus, this);
	}

	private void piercingAbility(Hero hero, Integer targetCell) {
		Char enemy = targetCell == null ? null : Actor.findChar(targetCell);
		int reach = reachFactor(hero) + thrustAbilityExtraRange(buffedLvl());
		if (!isValidEnemy(hero, enemy) || Dungeon.level == null || Dungeon.level.heroFOV == null
				|| enemy.pos < 0 || enemy.pos >= Dungeon.level.heroFOV.length
				|| !Dungeon.level.heroFOV[enemy.pos]
				|| Dungeon.level.distance(hero.pos, enemy.pos) > reach
				|| new Ballistica(hero.pos, enemy.pos, Ballistica.PROJECTILE).collisionPos != enemy.pos) {
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}

		hero.belongings.abilityWeapon = this;
		hero.chooseEnemy(enemy);
		hero.busy();
		Callback attack = new Callback() {
			@Override
			public void call() {
				if (!isValidEnemy(hero, enemy) || enemy.pos != targetCell) {
					hero.belongings.abilityWeapon = null;
					hero.next();
					return;
				}
				beforeAbilityUsed(hero, enemy);
				AttackIndicator.target(enemy);
				Invisibility.dispel();
				boolean hit;
				try {
					hit = hero.attack(enemy, 1f, 0f, Float.POSITIVE_INFINITY,
						DamageTag.PHYSICAL, DamageTag.MELEE, DamageTag.NO_ARMOR);
				} finally {
					hero.belongings.abilityWeapon = null;
				}
				if (hit && !enemy.isAlive()) onAbilityKill(hero, enemy);
				hero.spendAndNext(hero.attackDelay());
				afterAbilityUsed(hero);
			}
		};
		if (hero.sprite == null) attack.call();
		else hero.sprite.attack(enemy.pos, attack);
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		if (spoonAbility(hero)) {
			Charger charger = hero.buff(Charger.class);
			return charger == null ? 1 : Math.max(1, charger.charges);
		}
		return 1;
	}

	@Override
	public String targetingPrompt() {
		if (spoonAbility(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero)) return null;
		return Messages.get(this, "prompt");
	}

	@Override
	protected String abilityName() {
		if (spoonAbility(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero)) {
			return Messages.titleCase(Messages.get(this, "spoon_ability_name"));
		}
		if (hasForm(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero, Form.SCYTHE)) {
			return Messages.titleCase(Messages.get(this, "scythe_ability_name"));
		}
		if (hasForm(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero, Form.SLASH)) {
			return Messages.titleCase(Messages.get(this, "slash_ability_name"));
		}
		if (hasForm(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero, Form.THRUST)) {
			return Messages.titleCase(Messages.get(this, "thrust_ability_name"));
		}
		return super.abilityName();
	}

	@Override
	public String abilityInfo() {
		Hero hero = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
		if (!hasEquippedOracle(hero)) {
			return Messages.get(this, levelKnown ? "unequipped_ability_desc"
					: "typical_unequipped_ability_desc");
		}
		if (spoonAbility(hero)) {
			return Messages.get(this, levelKnown ? "spoon_ability_desc" : "typical_spoon_ability_desc");
		}
		int level = levelKnown ? buffedLvl() : 0;
		if (hasForm(hero, Form.SCYTHE)) {
			int amount = 25 + 4 * Math.max(0, level);
			return Messages.get(this, levelKnown ? "scythe_ability_desc" : "typical_scythe_ability_desc", amount);
		}
		if (hasForm(hero, Form.SLASH)) {
			return Messages.get(this, levelKnown ? "slash_ability_desc" : "typical_slash_ability_desc",
					swordDanceDurationForLevel(level));
		}
		if (hasForm(hero, Form.THRUST)) {
			return Messages.get(this, levelKnown ? "thrust_ability_desc" : "typical_thrust_ability_desc",
					thrustAbilityExtraRange(level));
		}
		int damageBonus = heavyBlowBonus(level);
		if (levelKnown) {
			return Messages.get(this, "ability_desc",
					augment.damageFactor(min(level)) + augment.damageFactor(damageBonus),
					augment.damageFactor(max(level)) + augment.damageFactor(damageBonus));
		}
		return Messages.get(this, "typical_ability_desc",
				min(0) + damageBonus, max(0) + damageBonus);
	}

	@Override
	public String upgradeAbilityStat(int level) {
		Hero hero = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
		if (hasForm(hero, Form.SLASH)) return Integer.toString(swordDanceDurationForLevel(level));
		if (hasForm(hero, Form.THRUST)) return Integer.toString(thrustAbilityExtraRange(level));
		if (spoonAbility(hero)) return "10%";
		if (hasForm(hero, Form.SCYTHE)) {
			return Integer.toString(25 + 4 * Math.max(0, level));
		}
		int damageBonus = heavyBlowBonus(level);
		return augment.damageFactor(min(level) + damageBonus) + "-"
				+ augment.damageFactor(max(level) + damageBonus);
	}

	@Override
	public java.util.ArrayList<UpgradeAbilityStat> upgradeAbilityStats(int level) {
		java.util.ArrayList<UpgradeAbilityStat> result = new java.util.ArrayList<>();
		Hero hero = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
		if (!isEquippedOracle(hero, this)) return result;
		Form form = currentFormType(hero);
		if (form == null || form == Form.SPOON) return result;
		if (form == Form.SLASH) {
			result.add(abilityStat(UpgradeAbilityStatType.DURATION, upgradeAbilityStat(level)));
		} else if (form == Form.THRUST) {
			result.add(abilityStat(UpgradeAbilityStatType.EXTRA_ATTACK_RANGE, upgradeAbilityStat(level)));
		} else {
			result.add(abilityStat(UpgradeAbilityStatType.DAMAGE, upgradeAbilityStat(level)));
		}
		return result;
	}

	private static boolean isEquippedOracle(Hero hero, OracleTerminal weapon) {
		return hero != null && hero.belongings != null
				&& (hero.belongings.weapon() == weapon || hero.belongings.secondWep() == weapon);
	}

	public String catalogDesc() {
		return Messages.get(this, "catalog_desc");
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int result = super.proc(attacker, defender, damage);
        if(attacker instanceof Hero){
            onAttackResolved((Hero) attacker, defender, true, result, DamageTag.PHYSICAL);
        }

		return applyFormDamage(attacker, result);
	}

	static int applyFormDamage(Char attacker, int damage) {
		OracleFormBuff form = currentForm(attacker);
		if (form != null && form.form() == Form.SCYTHE) {
			return scytheDamage(damage);
		}

		return damage;
	}

	@Override
	public void onAttackResolved(Hero hero, Char target, boolean hit,
	                             int damageDealt, DamageTag... damageTags) {
		if (!hit || !isResolvedEnemy(hero, target)) return;
		OracleFormBuff form = currentForm(hero);
		if (form == null) return;
		if (requiresLivingTarget(form.form()) && !isValidEnemy(hero, target)) return;
		switch (form.form()) {
			case BLUNT:
				if (bluntTriggers(Random.Float())) {
					Buff.affect(target, Paralysis.class, 1f);
				}
				break;
			case SLASH:
				if (slashTriggers(Random.Float())) {
					Buff.affect(target, KingBlade.Disarm.class, 1f);
				}
				break;
			case THRUST:
				if (Random.Float() < 1f / 3f) {
					Buff.affect(target, Bleeding.class).set(thrustBleedForDamage(damageDealt));
				}
				break;
			case SCYTHE:
				if (target.buff(ScytheExecutionMark.class) == null) {
					Buff.affect(target, ScytheExecutionMark.class);
					Buff.prolong(target, Vulnerable.class, 5f);
					knockBackScytheTarget(hero, target);
				}
				break;
			case SPOON:
				break;
		}


	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BLUNT_WEIGHT, bluntWeight);
		bundle.put(SLASH_WEIGHT, slashWeight);
		bundle.put(THRUST_WEIGHT, thrustWeight);
		bundle.put(SCYTHE_WEIGHT, scytheWeight);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		bluntWeight = bundle.contains(BLUNT_WEIGHT) ? bundle.getInt(BLUNT_WEIGHT) : 30;
		slashWeight = bundle.contains(SLASH_WEIGHT) ? bundle.getInt(SLASH_WEIGHT) : 30;
		thrustWeight = bundle.contains(THRUST_WEIGHT) ? bundle.getInt(THRUST_WEIGHT) : 30;
		scytheWeight = bundle.contains(SCYTHE_WEIGHT) ? bundle.getInt(SCYTHE_WEIGHT) : 10;
	}

	private void knockBackScytheTarget(Hero hero, Char target) {
		Ballistica trajectory = new Ballistica(hero.pos, target.pos, Ballistica.STOP_TARGET);
		trajectory = new Ballistica(trajectory.collisionPos,
				trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
		WandOfBlastWave.throwCharImmediately(target, trajectory, 1,
				true, false, this, null);
	}

	private boolean isValidEnemy(Hero hero, Char target) {
		return isResolvedEnemy(hero, target) && target.isAlive()
				&& Actor.findById(target.id()) == target;
	}

	private boolean isResolvedEnemy(Hero hero, Char target) {
		return target != null && target != hero
				&& target.alignment == Char.Alignment.ENEMY
				&& !hero.isCharmedBy(target);
	}

	public abstract static class OracleFormBuff extends FlavourBuff {

		{
			type = buffType.POSITIVE;
			announced = true;
		}

		public abstract Form form();

		private void startCycle() {
			spend(FORM_DURATION);
		}

		@Override
		public boolean act() {
			if (!(target instanceof Hero) || !hasEquippedOracle((Hero) target)) {
				detach();
			} else {
				Hero hero = (Hero) target;
				KindOfWeapon equipped = hero.belongings.weapon() instanceof OracleTerminal
						? hero.belongings.weapon() : hero.belongings.secondWep();
				((OracleTerminal) equipped).rollNextForm(hero);
			}
			return true;
		}
	}

	public static class BluntForm extends OracleFormBuff {
		@Override
		public Form form() {
			return Form.BLUNT;
		}

		@Override
		public int icon() {
			return BuffIndicator.ORACLE_BLUNT;
		}

		@Override
		public String name() {
			return Messages.get(OracleTerminal.class, "blunt_name");
		}

		@Override
		public String desc() {
			return Messages.get(OracleTerminal.class, "blunt_desc", dispTurns());
		}
	}

	public static class SlashForm extends OracleFormBuff {
		@Override
		public Form form() {
			return Form.SLASH;
		}

		@Override
		public int icon() {
			return BuffIndicator.ORACLE_SLASH;
		}

		@Override
		public String name() {
			return Messages.get(OracleTerminal.class, "slash_name");
		}

		@Override
		public String desc() {
			return Messages.get(OracleTerminal.class, "slash_desc", dispTurns());
		}
	}

	public static class ThrustForm extends OracleFormBuff {
		@Override
		public Form form() {
			return Form.THRUST;
		}

		@Override
		public int icon() {
			return BuffIndicator.ORACLE_THRUST;
		}

		@Override
		public String name() {
			return Messages.get(OracleTerminal.class, "thrust_name");
		}

		@Override
		public String desc() {
			return Messages.get(OracleTerminal.class, "thrust_desc", dispTurns());
		}
	}

	public static class ScytheForm extends OracleFormBuff {
		@Override
		public Form form() {
			return Form.SCYTHE;
		}

		@Override
		public int icon() {
			return BuffIndicator.ORACLE_SCYTHE;
		}

		@Override
		public String name() {
			return Messages.get(OracleTerminal.class, "scythe_name");
		}

		@Override
		public String desc() {
			return Messages.get(OracleTerminal.class, "scythe_desc", dispTurns());
		}
	}

	public static class SpoonForm extends OracleFormBuff {
		@Override public Form form() { return Form.SPOON; }
		@Override public int icon() { return BuffIndicator.SERPENT_STAFF_SPOON; }
		@Override public String name() { return Messages.get(OracleTerminal.class, "spoon_name"); }
		@Override public String desc() { return Messages.get(OracleTerminal.class, "spoon_desc", dispTurns()); }
	}

	public static class ScytheExecutionMark extends Buff {
	}
}
