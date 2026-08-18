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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Mace;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class OracleTerminal extends MeleeWeapon {

	public static final int TIER = 6;
	public static final float FORM_DURATION = 10f;

	public enum Form {
		BLUNT,
		SLASH,
		THRUST,
		SCYTHE
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

	static int slashDamage(int damageDealt) {
		return Math.max(0, damageDealt) / 4;
	}

	static int bleedLevel(int level) {
		return 4 + Math.max(0, level);
	}

	static int scytheDamage(int damage) {
		return Math.round(Math.max(0, damage) * 1.5f);
	}

	static boolean bluntTriggers(float roll) {
		return roll < 0.25f;
	}

	static boolean requiresLivingTarget(Form form) {
		return form != Form.SLASH;
	}

	static int heavyBlowBonus(int level) {
		return 5 + Math.round(1.5f * Math.max(0, level));
	}

	static boolean hasEquippedOracle(Hero hero) {
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
		return form;
	}

	static void clearForms(Char target) {
		if (target == null) return;
		Buff.detach(target, BluntForm.class);
		Buff.detach(target, SlashForm.class);
		Buff.detach(target, ThrustForm.class);
		Buff.detach(target, ScytheForm.class);
	}

	static OracleFormBuff applyFormForRoll(Char target, int roll) {
		clearForms(target);
		Class<? extends OracleFormBuff> formClass;
		switch (formForRoll(roll)) {
			case BLUNT:
				formClass = BluntForm.class;
				break;
			case SLASH:
				formClass = SlashForm.class;
				break;
			case THRUST:
				formClass = ThrustForm.class;
				break;
			case SCYTHE:
			default:
				formClass = ScytheForm.class;
				break;
		}
		OracleFormBuff form = Buff.affect(target, formClass);
		form.startCycle();
		BuffIndicator.refreshHero();
		return form;
	}

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		if (ch instanceof Hero && currentForm((Hero) ch) == null) {
			applyFormForRoll((Hero) ch, Random.Int(100));
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
		int damageBonus = augment.damageFactor(heavyBlowBonus(buffedLvl()));
		Mace.heavyBlowAbility(hero, target, 1f, damageBonus, this);
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public String abilityInfo() {
		int damageBonus = levelKnown ? heavyBlowBonus(buffedLvl()) : heavyBlowBonus(0);
		if (levelKnown) {
			return Messages.get(this, "ability_desc",
					augment.damageFactor(min() + damageBonus),
					augment.damageFactor(max() + damageBonus));
		}
		return Messages.get(this, "typical_ability_desc",
				min(0) + damageBonus, max(0) + damageBonus);
	}

	@Override
	public String upgradeAbilityStat(int level) {
		int damageBonus = heavyBlowBonus(level);
		return augment.damageFactor(min(level) + damageBonus) + "-"
				+ augment.damageFactor(max(level) + damageBonus);
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
        /*
        if(hero.buff(BluntForm.class)!=null && bluntTriggers(Random.Float())){
            Buff.affect(target, Paralysis.class, 1f);
            Buff.affect(target, Daze.class, 1f);
        }else if(hero.buff(SlashForm.class)!=null){
            applySlash(hero, target, damageDealt);
        }else if(hero.buff(ThrustForm.class)!=null){
            Buff.affect(target, Bleeding.class).set(bleedLevel(buffedLvl()));
        } else if (hero.buff(ScytheForm.class) != null && target.buff(ScytheExecutionMark.class) == null) {
            Buff.affect(target, ScytheExecutionMark.class);
            Buff.affect(target, Vulnerable.class, 2f);
            knockBackScytheTarget(hero, targe

         */

		switch (form.form()) {
			case BLUNT:
				if (bluntTriggers(Random.Float())) {
					Buff.affect(target, Paralysis.class, 1f);
					Buff.affect(target, Daze.class, 1f);
				}
				break;
			case SLASH:
				applySlash(hero, target, damageDealt);
				break;
			case THRUST:
				Buff.affect(target, Bleeding.class).set(bleedLevel(buffedLvl()));
				break;
			case SCYTHE:
				if (target.buff(ScytheExecutionMark.class) == null) {
					Buff.affect(target, ScytheExecutionMark.class);
					Buff.affect(target, Vulnerable.class, 2f);
					knockBackScytheTarget(hero, target);
				}
				break;
		}


	}

	private void applySlash(Hero hero, Char primaryTarget, int damageDealt) {
		int splashDamage = slashDamage(damageDealt);
		if (splashDamage <= 0) return;

		ArrayList<Char> slashTargets = new ArrayList<>();
		for (Char candidate : Actor.chars()) {
			if (candidate != primaryTarget && isValidEnemy(hero, candidate)
					&& canReach(hero, candidate.pos)
					&& !slashTargets.contains(candidate)) {
				slashTargets.add(candidate);
			}
		}

		for (Char candidate : slashTargets) {
			if (isValidEnemy(hero, candidate)) {
				candidate.damage(splashDamage, hero, DamageTag.PHYSICAL);
			}
		}
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
				applyFormForRoll((Hero) target, Random.Int(100));
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

	public static class ScytheExecutionMark extends Buff {
	}
}
