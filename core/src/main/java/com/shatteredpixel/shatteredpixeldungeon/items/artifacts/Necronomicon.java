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
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/** A forbidden tome that binds the dead and calls corrupted wraiths. */
public class Necronomicon extends Artifact {

	public static final String AC_CAST = "CAST";
	private static final int MAX_LEVEL = 5;

	{
		image = EXItemSpriteSheet.NECRONOMICON;
		levelCap = MAX_LEVEL;
		exp = 0;
		charge = 5;
		chargeCap = chargeCapForLevel(0);
		partialCharge = 0;
		defaultAction = AC_CAST;
		usesTargeting = true;
	}

	public static int chargeCapForLevel(int level) {
		return 5 + Math.max(0, Math.min(MAX_LEVEL, level));
	}

	public static int expThresholdForLevel(int level) {
		return 100 * (Math.max(0, Math.min(MAX_LEVEL - 1, level)) + 1);
	}

	public static float naturalChargePerTurn(int level, int charge, float multiplier) {
		int cap = chargeCapForLevel(level);
		if (charge >= cap) return 0f;
		return multiplier / (120f - 5f * (cap - Math.max(0, charge)));
	}

	public static int artifactExpFromHeroProgress(float levelPortion) {
		return Math.round(levelPortion * 100f);
	}

	public static float expChargeFromHeroProgress(float levelPortion) {
		return levelPortion * 6f;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (canCast(hero)) actions.add(AC_CAST);
		return actions;
	}

	private boolean canCast(Hero hero) {
		return hero != null && isEquipped(hero) && !cursed
				&& hero.buff(MagicImmune.class) == null && charge >= 1;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!AC_CAST.equals(action)) return;
		if (!canCast(hero)) {
			if (!isEquipped(hero)) GLog.i(Messages.get(Artifact.class, "need_to_equip"));
			else if (hero.buff(MagicImmune.class) != null) GLog.w(Messages.get(Artifact.class, "no_magic"));
			else if (cursed) GLog.w(Messages.get(this, "cursed"));
			else GLog.i(Messages.get(this, "no_charge"));
			usesTargeting = false;
			return;
		}
		curUser = hero;
		usesTargeting = true;
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.selectCell(caster);
	}

	@Override
	public void charge(Hero target, float amount) {
		if (!canCharge(target)) return;
		partialCharge += 0.5f * amount;
		convertPartialCharge();
	}

	private boolean canCharge(Hero target) {
		return target != null && isEquipped(target) && !cursed
				&& target.buff(MagicImmune.class) == null && charge < chargeCap;
	}

	private void convertPartialCharge() {
		while (partialCharge >= 1f && charge < chargeCap) {
			partialCharge -= 1f;
			charge++;
		}
		if (charge >= chargeCap) {
			charge = chargeCap;
			partialCharge = 0f;
		}
		updateQuickslot();
	}

	@Override
	public void onHeroGainExp(float levelPercent, Hero hero) {
		BookRecharge recharge = hero == null ? null : hero.buff(BookRecharge.class);
		if (recharge != null && isEquipped(hero)) recharge.gainExp(levelPercent);
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new BookRecharge();
	}

	public final CellSelector.Listener caster = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			// Active targeting is implemented in the casting task; cancellation is
			// intentionally side-effect free.
		}

		@Override
		public String prompt() {
			return Messages.get(Necronomicon.class, "prompt");
		}
	};

	public class BookRecharge extends ArtifactBuff {
		@Override
		public boolean act() {
			if (target instanceof Hero && canCharge((Hero) target)
					&& Regeneration.regenOn()) {
				partialCharge += naturalChargePerTurn(level(), charge,
						RingOfEnergy.artifactChargeMultiplier(target));
				convertPartialCharge();
			}
			updateQuickslot();
			spend(TICK);
			return true;
		}

		public void gainExp(float levelPortion) {
			if (levelPortion <= 0f || target == null || cursed
					|| target.buff(MagicImmune.class) != null) return;
			exp += artifactExpFromHeroProgress(levelPortion);
			partialCharge += expChargeFromHeroProgress(levelPortion);
			while (level() < levelCap && exp >= expThresholdForLevel(level())) {
				exp -= expThresholdForLevel(level());
				upgrade();
				GLog.p(Messages.get(this, "levelup"));
			}
			convertPartialCharge();
		}
	}

	@Override
	public Item upgrade() {
		Item result = super.upgrade();
		chargeCap = chargeCapForLevel(level());
		if (charge > chargeCap) charge = chargeCap;
		if (charge == chargeCap) partialCharge = 0f;
		return result;
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (level() > levelCap) level(levelCap);
		chargeCap = chargeCapForLevel(level());
		charge = Math.max(0, Math.min(chargeCap, charge));
		if (charge == chargeCap) partialCharge = 0f;
		if (level() >= levelCap) exp = 0;
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (isEquipped(Dungeon.hero)) {
			desc += "\n\n" + Messages.get(this, cursed ? "desc_cursed" : "desc_equipped");
		}
		return desc;
	}
}
