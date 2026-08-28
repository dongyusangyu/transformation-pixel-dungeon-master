/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.PalermoSword;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** A mechanical eye which stores charged, guaranteed dodges and learns from ordinary evasion. */
public class PrecognitiveEye extends Artifact {

	public static final String AC_ACTIVATE = "ACTIVATE";
	private static final int MAX_LEVEL = 5;
	private static final int MOMENTARY_FORESIGHT_COST = 20;
	private static final int DODGE_EXPERIENCE = 10;

	{
		image = EXItemSpriteSheet.PRECOGNITIVE_EYE;
		exp = 0;
		levelCap = MAX_LEVEL;
		charge = 0;
		partialCharge = 0;
		chargeCap = 100;
		defaultAction = AC_ACTIVATE;
	}

	public static int expToNextLevel(int level) {
		return 100 + 100 * level;
	}

	public static int momentaryForesightChargeCost(int level) {
		return MOMENTARY_FORESIGHT_COST;
	}

	public static int momentaryForesightCapacity(int level) {
		return 1 + Math.max(0, Math.min(MAX_LEVEL, level));
	}

	public static int trinityDodgeUses(int spiritFormPoints) {
		return 1 + spiritFormPoints;
	}

	public static float naturalChargePerTurn(int level) {
		return 0.5f + 0.1f * Math.max(0, Math.min(MAX_LEVEL, level));
	}

	public static float artifactRechargePerTurn() {
		return 3f;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (canActivate(hero)) {
			actions.add(AC_ACTIVATE);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (AC_ACTIVATE.equals(action)) {
			if (!canUseActiveAction(hero)) {
				return;
			} else if (!storeMomentaryForesight(hero)) {
				GLog.w(Messages.get(this, "cannot_activate"));
			} else {
				Talent.onArtifactUsed(hero);
				hero.next();
			}
		}
	}

	private boolean canActivate(Hero hero) {
		if (charge < momentaryForesightChargeCost(level()) || !isEquipped(hero) || cursed
				|| hero.buff(MagicImmune.class) != null) {
			return false;
		}
		MomentaryForesight foresight = hero.buff(MomentaryForesight.class);
		return foresight == null || foresight.uses() < momentaryForesightCapacity(level());
	}

	boolean storeMomentaryForesight(Hero hero) {
		if (!canActivate(hero)) return false;
		charge -= momentaryForesightChargeCost(level());
		Buff.affect(hero, MomentaryForesight.class)
				.addDodge(momentaryForesightCapacity(level()));
		updateQuickslot();
		return true;
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new EyeRecharge();
	}

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		PrecognitiveHeat legacyHeat = ch.buff(PrecognitiveHeat.class);
		if (legacyHeat != null) legacyHeat.detach();
		PrecognitiveOverheat legacyOverheat = ch.buff(PrecognitiveOverheat.class);
		if (legacyOverheat != null) legacyOverheat.detach();

		MomentaryForesight moment = ch.buff(MomentaryForesight.class);
		if (cursed && ch instanceof Hero) {
			if (moment != null) moment.detach();
		} else if (moment != null) {
			moment.cap(momentaryForesightCapacity(level()));
		}
	}

	@Override
	public void charge(Hero target, float amount) {
		if (!canCharge(target)) return;
		gainCharge(artifactRechargePerTurn() * amount);
	}

	private boolean canCharge(Hero target) {
		return !cursed && target.buff(MagicImmune.class) == null
				&& charge < chargeCap;
	}

	private void gainCharge(float amount) {
		if (charge >= chargeCap) return;
		partialCharge += amount;
		while (partialCharge >= 1f && charge < chargeCap) {
			partialCharge--;
			charge++;
		}
		if (charge >= chargeCap) {
			charge = chargeCap;
			partialCharge = 0;
			GLog.p(Messages.get(this, "full_charge"));
		}
		updateQuickslot();
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (isEquipped(Dungeon.hero)) {
			if (cursed) {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			} else {
				desc += "\n\n" + Messages.get(this, "desc_worn", momentaryForesightCapacity(level()));
				if (Dungeon.hero.belongings.weapon() instanceof PalermoSword
						|| Dungeon.hero.belongings.secondWep() instanceof PalermoSword) {
					desc += "\n\n" + Messages.get(this, "desc_palermo");
				}
			}
		}
		return desc;
	}

	public static boolean forcesEnemyHit(Char attacker, Char defender) {
		PrecognitiveEye eye = equippedEye(attacker, defender);
		return eye != null && eye.cursed;
	}

	public static boolean consumeMomentaryForesight(Char attacker, Char defender) {
		if (!isEnemyAttackOnHero(attacker, defender)) return false;
		MomentaryForesight moment = defender.buff(MomentaryForesight.class);
		if (moment == null || !moment.consumeDodge()) return false;
		return true;
	}

	public static float rollEvasion(Char attacker, Char defender, float maximum) {
		return Random.Float(maximum);
	}

	public static void onEnemyAttackDodged(Char attacker, Char defender) {
		PrecognitiveEye eye = equippedEye(attacker, defender);
		if (eye == null || eye.cursed || defender.buff(MagicImmune.class) != null) return;
		eye.gainExperience(DODGE_EXPERIENCE);
	}

	@Override
	public void onHeroGainExp(float levelPercent, Hero hero) {
		if (levelPercent > 0 && isEquipped(hero) && !cursed
				&& hero.buff(MagicImmune.class) == null) {
			gainExperience(Math.round(100 * levelPercent));
		}
	}

	private void gainExperience(int amount) {
		if (level() >= levelCap) return;
		exp += amount;
		while (level() < levelCap && exp >= expToNextLevel(level())) {
			exp -= expToNextLevel(level());
			upgrade();
			Catalog.countUses(PrecognitiveEye.class, 2);
			GLog.p(Messages.get(this, "levelup"));
		}
		updateQuickslot();
	}

	private static boolean isEnemyAttackOnHero(Char attacker, Char defender) {
		return attacker != null && defender instanceof Hero && attacker.alignment != null
				&& attacker.alignment.name().startsWith("ENEMY");
	}

	private static PrecognitiveEye equippedEye(Char attacker, Char defender) {
		if (!isEnemyAttackOnHero(attacker, defender)) return null;
		PrecognitiveEye eye = ((Hero) defender).belongings.getItem(PrecognitiveEye.class);
		return eye != null && eye.isEquipped((Hero) defender) ? eye : null;
	}

	/** Kept so saves containing the removed heat buff can still be loaded safely. */
	@Deprecated
	public static class PrecognitiveHeat extends Buff {
		@Override public boolean act() { detach(); return true; }
		@Override public int icon() { return BuffIndicator.NONE; }
	}

	/** Kept so saves containing the removed overheat buff can still be loaded safely. */
	@Deprecated
	public static class PrecognitiveOverheat extends Buff {
		@Override public boolean act() { detach(); return true; }
		@Override public int icon() { return BuffIndicator.NONE; }
	}

	public static class MomentaryForesight extends Buff {
		private static final String USES = "uses";
		private int uses;

		public MomentaryForesight set(int uses) {
			this.uses = Math.max(this.uses, uses);
			return this;
		}
		public boolean addDodge(int capacity) {
			int boundedCapacity = Math.max(1, capacity);
			if (uses >= boundedCapacity) return false;
			uses++;
			return true;
		}
		public void cap(int capacity) { uses = Math.min(uses, Math.max(1, capacity)); }
		public int uses() { return uses; }
		public boolean consumeDodge() { if (uses <= 0) return false; if (--uses == 0) detach(); return true; }
		@Override public int icon() { return BuffIndicator.MOMENTARY_FORESIGHT; }
		@Override public String iconTextDisplay() { return Integer.toString(uses); }
		@Override public String desc() { return Messages.get(this, "desc", uses); }
		@Override public void storeInBundle(Bundle bundle) { super.storeInBundle(bundle); bundle.put(USES, uses); }
		@Override public void restoreFromBundle(Bundle bundle) { super.restoreFromBundle(bundle); uses = bundle.getInt(USES); }
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (level() > levelCap) level(levelCap);
		if (level() >= levelCap) exp = 0;
	}

	private class EyeRecharge extends ArtifactBuff {
		@Override public boolean act() {
			spend(TICK);
			if (canCharge((Hero) target) && Regeneration.regenOn()) {
				gainCharge(naturalChargePerTurn(level()) * RingOfEnergy.artifactChargeMultiplier(target));
			}
			return true;
		}
	}
}
