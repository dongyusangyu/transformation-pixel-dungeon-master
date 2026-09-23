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
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.RubbingsTome;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

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

	public static int summonLimit(int level) {
		return 2 + Math.max(0, Math.min(MAX_LEVEL, level));
	}

	public static boolean isValidSummonCell(Level level, int cell) {
		return level != null && cell >= 0 && cell < level.length()
				&& !level.solid[cell] && Actor.findChar(cell) == null
				&& level.getTransition(cell) == null;
	}

	public static ArrayList<Integer> summonCandidates(Level level, int landingCell) {
		ArrayList<Integer> candidates = new ArrayList<>();
		if (level == null) return candidates;
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = landingCell + offset;
			if (isValidSummonCell(level, cell)) candidates.add(cell);
		}
		return candidates;
	}

	public static boolean isValidSoulBoundTarget(Mob target) {
		if (target == null || target instanceof Wraith || target.alignment == null
				|| !target.alignment.name().startsWith("ENEMY")) return false;
		return !Char.hasProp(target, Char.Property.BOSS)
				&& !Char.hasProp(target, Char.Property.MINIBOSS)
				&& !Char.hasProp(target, Char.Property.BOSS_MINION)
				&& !target.isImmune(Corruption.class);
	}

	public enum SoulBoundDeathResult {
		CONTINUE_DEATH,
		INTERCEPTED_DEATH
	}

	public static boolean hasSoulBound(Mob mob) {
		return mob != null && mob.buff(SoulBound.class) != null;
	}

	public static void clearSoulBound(Mob mob) {
		if (mob != null) {
			SoulBound marker = mob.buff(SoulBound.class);
			if (marker != null) marker.detach();
		}
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {
		static boolean isValidWandIngredient(boolean identified, boolean cursedKnown,
				boolean cursed, boolean equipped) {
			return identified && cursedKnown && !cursed && !equipped;
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients == null || ingredients.size() != 3) return false;
			boolean dust = false;
			boolean tome = false;
			boolean wand = false;
			for (Item item : ingredients) {
				if (item == null || item.quantity() < 1) return false;
				if (item instanceof CorpseDust && !dust) {
					dust = true;
				} else if (item instanceof RubbingsTome && !tome) {
					tome = true;
				} else if (item instanceof Wand && !wand
						&& isValidWandIngredient(item.isIdentified(), item.cursedKnown,
						item.cursed, item.isEquipped(Dungeon.hero))) {
					wand = true;
				} else {
					return false;
				}
			}
			return dust && tome && wand;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 10;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			for (Item ingredient : ingredients) ingredient.quantity(ingredient.quantity() - 1);
			CorpseDust.syncGhostSpawner(Dungeon.hero);
			return sampleOutput(ingredients);
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new Necronomicon().identify();
		}
	}

	/** Wand placeholder used by quick alchemy to select only recipe-valid wands. */
	public static class AlchemyWandPlaceholder extends Wand.PlaceHolder {
		@Override
		public boolean isSimilar(Item item) {
			if (!(item instanceof Wand)) return false;
			Wand wand = (Wand) item;
			return Recipe.isValidWandIngredient(wand.isIdentified(), wand.cursedKnown,
					wand.cursed, wand.isEquipped(Dungeon.hero));
		}
	}

	public static SoulBoundDeathResult resolveSoulBoundDeath(Mob mob, Object cause) {
		if (!hasSoulBound(mob)) return SoulBoundDeathResult.CONTINUE_DEATH;

		// Detaching first makes repeated die() calls idempotent, including callbacks
		// from Actor removal or a second damage source in the same tick.
		SoulBound marker = mob.buff(SoulBound.class);
		if (mob.alignment == null || !mob.alignment.name().startsWith("ENEMY")) {
			marker.detach();
			return SoulBoundDeathResult.CONTINUE_DEATH;
		}
		marker.detach();
		if (cause == Chasm.class) {
			spawnCorruptedWraithNear(mob.pos);
			return SoulBoundDeathResult.CONTINUE_DEATH;
		}

		mob.HP = mob.HT;
		Corruption.corruptionHeal(mob);
		boolean converted;
		if (Dungeon.hero != null) {
			converted = AllyBuff.affectAndLoot(mob, Dungeon.hero, Corruption.class);
		} else {
			converted = Buff.affect(mob, Corruption.class) != null;
		}
		if (!converted || mob.alignment != Char.Alignment.ALLY) {
			// If immunity was added between marking and death, keep the original
			// death path instead of leaving an unkillable, unconverted mob.
			mob.HP = 0;
			return SoulBoundDeathResult.CONTINUE_DEATH;
		}
		spawnCorruptedWraithNear(mob.pos);
		return SoulBoundDeathResult.INTERCEPTED_DEATH;
	}

	private static Wraith spawnCorruptedWraithNear(int origin) {
		if (Dungeon.level == null) return null;
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = origin + offset;
			if (cell >= 0 && cell < Dungeon.level.length()
					&& !Dungeon.level.solid[cell]
					&& Actor.findChar(cell) == null) {
				candidates.add(cell);
			}
		}
		if (candidates.isEmpty()) return null;
		Wraith wraith = Wraith.spawnAt(Random.element(candidates), Wraith.class);
		if (wraith != null) Buff.affect(wraith, Corruption.class);
		return wraith;
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
		partialCharge += 0.25f * amount;
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

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		if (ch instanceof Hero) CorpseDust.syncGhostSpawner((Hero) ch);
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)) {
			CorpseDust.syncGhostSpawner(hero);
			return true;
		}
		return false;
	}

	public final CellSelector.Listener caster = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target == null || curUser == null || Dungeon.level == null) return;
			if (target < 0 || target >= Dungeon.level.length()
					|| (!Dungeon.level.visited[target] && !Dungeon.level.mapped[target])) return;
			if (target == curUser.pos) {
				resolveCast((Hero) curUser, target, false);
				return;
			}

			final Ballistica shot = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
			final int cell = shot.collisionPos;
			curUser.sprite.zap(cell);
			MagicMissile.boltFromChar(curUser.sprite.parent, MagicMissile.SHADOW,
					curUser.sprite, cell, () -> resolveCast((Hero) curUser, cell, true));
			Sample.INSTANCE.play(Assets.Sounds.ZAP);
			curUser.busy();
		}

		@Override
		public String prompt() {
			return Messages.get(Necronomicon.class, "prompt");
		}
	};

	private boolean resolveCast(Hero hero, int cell, boolean projectileCallback) {
		if (hero == null || Dungeon.level == null || !canCast(hero)
				|| !isEquipped(hero)) {
			if (projectileCallback && hero != null) hero.next();
			return false;
		}

		if (cell != hero.pos) {
			Char target = Actor.findChar(cell);
			if (target instanceof Mob && isValidSoulBoundTarget((Mob) target)) {
				if (charge < 1) {
					if (projectileCallback) hero.next();
					return false;
				}
				Buff.affect(target, SoulBound.class);
				charge -= 1;
				Invisibility.dispel(hero);
				Talent.onArtifactUsed(hero);
				updateQuickslot();
				hero.spendAndNext(Actor.TICK);
				return true;
			}

			if (target != null) {
				if (projectileCallback) hero.next();
				return false;
			}
		}

		ArrayList<Integer> candidates = summonCandidates(Dungeon.level, cell);
		if (candidates.isEmpty() || charge < 2) {
			if (projectileCallback) hero.next();
			return false;
		}
		Random.shuffle(candidates);
		int amount = Math.min(summonLimit(level()), candidates.size());
		for (int i = 0; i < amount; i++) {
			Wraith wraith = Wraith.spawnAt(candidates.get(i), Wraith.class);
			if (wraith != null) Buff.affect(wraith, Corruption.class);
		}
		charge -= 2;
		Invisibility.dispel(hero);
		Talent.onArtifactUsed(hero);
		updateQuickslot();
		hero.spendAndNext(Actor.TICK);
		return true;
	}

	public class BookRecharge extends ArtifactBuff {
		@Override
		public boolean act() {
			if (target instanceof Hero) CorpseDust.syncGhostSpawner((Hero) target);
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

	public static class SoulBound extends Buff {
		{
			type = buffType.NEGATIVE;
			announced = true;
			revivePersists = true;
		}

		@Override
		public int icon() {
			return BuffIndicator.CORRUPT;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.65f, 0.65f, 0.65f);
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
