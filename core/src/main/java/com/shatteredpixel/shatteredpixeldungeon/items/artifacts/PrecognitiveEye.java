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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.PalermoSword;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** A mechanical eye which converts successful dodges into a deliberately limited reserve of foresight. */
public class PrecognitiveEye extends Artifact {

	public static final String AC_ACTIVATE = "ACTIVATE";
	private static final int MAX_LEVEL = 10;
	private static final int HEAT_LIMIT = 10;
	private static final int HEAT_DECAY_TURNS = 10;
	private static final int OVERHEAT_TURNS = 100;

	{
		image = EXItemSpriteSheet.PRECOGNITIVE_EYE;
		exp = 0;
		levelCap = MAX_LEVEL;
		charge = 0;
		partialCharge = 0;
		chargeCap = 100;
		defaultAction = AC_ACTIVATE;
	}

	public static int minimumEvasion(int maximum, int level) {
		return Math.min(maximum, Math.round(5 + maximum * level * 0.03f));
	}

	public static int expToNextLevel(int level) {
		return 100 + 50 * level;
	}

	public static int momentaryForesightUses(int level) {
		return Math.max(1, (level + 1) / 2);
	}

	public static int trinityDodgeUses(int spiritFormPoints) {
		return 1 + spiritFormPoints;
	}

	public static float naturalChargePerTurn() {
		return 0.5f;
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
			if (!isEquipped(hero)) {
				GLog.i(Messages.get(Artifact.class, "need_to_equip"));
			} else if (!canActivate(hero)) {
				GLog.w(Messages.get(this, "cannot_activate"));
			} else {
				charge = 0;
				partialCharge = 0;
				Buff.affect(hero, MomentaryForesight.class).set(momentaryForesightUses(level()), true);
				Talent.onArtifactUsed(hero);
				hero.spendAndNext(Actor.TICK);
				updateQuickslot();
			}
		}
	}

	private boolean canActivate(Hero hero) {
		return level() > 0 && charge >= chargeCap && isEquipped(hero) && !cursed
				&& hero.buff(MagicImmune.class) == null && hero.buff(PrecognitiveOverheat.class) == null;
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new EyeRecharge();
	}

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		if (cursed && ch instanceof Hero) {
			MomentaryForesight moment = ch.buff(MomentaryForesight.class);
			if (moment != null) moment.detach();
		}
	}

	@Override
	public void charge(Hero target, float amount) {
		if (!canCharge(target)) return;
		gainCharge(artifactRechargePerTurn() * amount);
	}

	private boolean canCharge(Hero target) {
		return !cursed && target.buff(MagicImmune.class) == null
				&& target.buff(PrecognitiveOverheat.class) == null && charge < chargeCap;
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
				desc += "\n\n" + Messages.get(this, "desc_worn");
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
		onEnemyAttackDodged(attacker, defender, moment.countsAsEyeDodge());
		return true;
	}

	public static float rollEvasion(Char attacker, Char defender, float maximum) {
		PrecognitiveEye eye = equippedEye(attacker, defender);
		if (eye == null || eye.cursed || defender.buff(PrecognitiveOverheat.class) != null) {
			return Random.Float(maximum);
		}
		return Random.Float(minimumEvasion(Math.round(maximum), eye.level()), maximum);
	}

	public static void onEnemyAttackDodged(Char attacker, Char defender) {
		onEnemyAttackDodged(attacker, defender, true);
	}

	private static void onEnemyAttackDodged(Char attacker, Char defender, boolean tracksEye) {
		if (!tracksEye) return;
		PrecognitiveEye eye = equippedEye(attacker, defender);
		if (eye == null || eye.cursed || defender.buff(PrecognitiveOverheat.class) != null) return;

		PrecognitiveHeat heat = defender.buff(PrecognitiveHeat.class);
		if (heat == null) heat = Buff.affect(defender, PrecognitiveHeat.class);
		if (heat.addLayer() >= HEAT_LIMIT) {
			heat.detach();
			Buff.prolong(defender, PrecognitiveOverheat.class, OVERHEAT_TURNS);
			MomentaryForesight moment = defender.buff(MomentaryForesight.class);
			if (moment != null) moment.detach();
		}

		if (effectiveDepth() >= 2 * eye.level()) eye.gainExperience(10);
	}

	private void gainExperience(int amount) {
		if (level() >= levelCap) return;
		exp += amount;
		while (level() < levelCap && exp >= expToNextLevel(level())) {
			exp -= expToNextLevel(level());
			upgrade();
			Catalog.countUse(PrecognitiveEye.class);
			GLog.p(Messages.get(this, "levelup"));
		}
		updateQuickslot();
	}

	public static int effectiveDepth() {
		return Dungeon.level instanceof TowerLevel ? 30 : Dungeon.depth;
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

	public static class HeatState {
		private int layers;
		private int turnsToDecay;

		public HeatState(int layers, int turnsToDecay) {
			this.layers = layers;
			this.turnsToDecay = turnsToDecay;
		}

		public int addLayer() { return layers = Math.min(HEAT_LIMIT, layers + 1); }
		public int layers() { return layers; }
		public int turnsToDecay() { return turnsToDecay; }
		public boolean tick() {
			if (--turnsToDecay > 0) return false;
			layers = Math.max(0, layers - 1);
			turnsToDecay = HEAT_DECAY_TURNS;
			return true;
		}
	}

	public static class PrecognitiveHeat extends Buff {
		private static final String LAYERS = "layers";
		private static final String DECAY = "decay";
		private final HeatState state = new HeatState(0, HEAT_DECAY_TURNS);

		public int addLayer() { return state.addLayer(); }
		public int layers() { return state.layers(); }
		public int turnsToDecay() { return state.turnsToDecay(); }

		@Override public boolean act() {
			spend(TICK);
			if (state.tick() && state.layers() == 0) detach();
			return true;
		}

		@Override public int icon() { return BuffIndicator.PRECOGNITIVE_EYE; }
		@Override public String iconTextDisplay() { return Integer.toString(layers()); }
		@Override public float iconFadePercent() { return turnsToDecay() / (float) HEAT_DECAY_TURNS; }
		@Override public void tintIcon(com.watabou.noosa.Image icon) {
			icon.resetColor();
			if (layers() >= 9) icon.hardlight(0xFF4444);
			else if (layers() >= 6) icon.hardlight(0xFF9900);
			else if (layers() >= 4) icon.hardlight(0xFFFF33);
		}
		@Override public String desc() { return Messages.get(this, "desc", layers(), turnsToDecay()); }
		@Override public void storeInBundle(Bundle bundle) { super.storeInBundle(bundle); bundle.put(LAYERS, layers()); bundle.put(DECAY, turnsToDecay()); }
		@Override public void restoreFromBundle(Bundle bundle) { super.restoreFromBundle(bundle); state.layers = bundle.getInt(LAYERS); state.turnsToDecay = bundle.contains(DECAY) ? bundle.getInt(DECAY) : HEAT_DECAY_TURNS; }
	}

	public static class PrecognitiveOverheat extends FlavourBuff {
		@Override public int icon() { return BuffIndicator.PRECOGNITIVE_OVERHEAT; }
		@Override public String desc() { return Messages.get(this, "desc", dispTurns()); }
	}

	public static class MomentaryForesight extends Buff {
		private static final String USES = "uses";
		private static final String TRACKS_EYE = "tracks_eye";
		private int uses;
		private boolean tracksEye;

		public MomentaryForesight set(int uses, boolean tracksEye) {
			this.uses = Math.max(this.uses, uses);
			this.tracksEye = this.tracksEye || tracksEye;
			return this;
		}
		public MomentaryForesight set(int uses) { return set(uses, true); }
		public int uses() { return uses; }
		public boolean countsAsEyeDodge() { return tracksEye; }
		public boolean consumeDodge() { if (uses <= 0) return false; if (--uses == 0) detach(); return true; }
		@Override public int icon() { return BuffIndicator.MOMENTARY_FORESIGHT; }
		@Override public String iconTextDisplay() { return Integer.toString(uses); }
		@Override public String desc() { return Messages.get(this, "desc", uses); }
		@Override public void storeInBundle(Bundle bundle) { super.storeInBundle(bundle); bundle.put(USES, uses); bundle.put(TRACKS_EYE, tracksEye); }
		@Override public void restoreFromBundle(Bundle bundle) { super.restoreFromBundle(bundle); uses = bundle.getInt(USES); tracksEye = bundle.getBoolean(TRACKS_EYE); }
	}

	private class EyeRecharge extends ArtifactBuff {
		@Override public boolean act() {
			spend(TICK);
			if (canCharge((Hero) target)) gainCharge(naturalChargePerTurn() * RingOfEnergy.artifactChargeMultiplier(target));
			return true;
		}
	}
}
