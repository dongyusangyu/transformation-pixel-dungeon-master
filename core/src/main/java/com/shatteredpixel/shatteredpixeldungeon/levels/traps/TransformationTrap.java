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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransformationTrap extends Trap {

	private static final float AMOK_DURATION = 50f;

	{
		color = VIOLET;
		shape = DIAMOND;
	}

	@Override
	public void activate() {
		if (Dungeon.level == null) return;

		TalentChange heroChange = null;
		boolean heroInRange = false;
		for (int offset : PathFinder.NEIGHBOURS9) {
			int cell = pos + offset;
			if (!Dungeon.level.insideMap(cell)) continue;

			Char target = Actor.findChar(cell);
			if (target instanceof Hero) {
				heroInRange = true;
				heroChange = transformRandomCommonTalent((Hero) target);
			} else if (target != null && shouldAmok(target.alignment)) {
				applyAmok(target);
			}
		}

		boolean visible = Dungeon.level.heroFOV != null
				&& pos >= 0
				&& pos < Dungeon.level.heroFOV.length
				&& Dungeon.level.heroFOV[pos];
		if (visible) {
			Sample.INSTANCE.play(Assets.Sounds.CURSED);
		}

		if (heroChange != null) {
			Hero hero = Dungeon.hero;
			if (hero != null) {
				Badges.validateTalent(heroChange.replacement);
				Talent.onTalentUpgraded(hero,
						hero.pointsInTalent(heroChange.replacement) > 0
								? heroChange.replacement : null);
				if (SPDSettings.charAnimations() && hero.sprite != null) {
					Transmuting.show(hero, heroChange.replacing, heroChange.replacement);
				}
			}
			GLog.n(Messages.get(this, "changed",
					heroChange.replacing.title(), heroChange.replacement.title()));
		} else if (heroInRange) {
			GLog.n(Messages.get(this, "unchanged"));
		}
	}

	static boolean shouldAmok(Char.Alignment alignment) {
		return alignment != null && alignment != Char.Alignment.NEUTRAL;
	}

	static void applyAmok(Char target) {
		if (target != null && !(target instanceof Hero) && shouldAmok(target.alignment)) {
			Buff.prolong(target, Amok.class, AMOK_DURATION);
		}
	}

	static List<Talent> replacementCandidates(Hero hero, int tier, Talent replacing) {
		ArrayList<Talent> candidates = new ArrayList<>();
		if (hero == null || hero.talents == null || replacing == null
				|| tier < 1 || replacing.tier() != tier || !replacing.isCommonTalentType()
				|| Talent.forbiddenInCatalogOrMetamorphosis(replacing)) {
			return candidates;
		}

		HashSet<Talent> owned = new HashSet<>();
		for (LinkedHashMap<Talent, Integer> talentTier : hero.talents) {
			owned.addAll(talentTier.keySet());
		}
		for (Talent candidate : Talent.commonTalentsByTier(tier)) {
			if (candidate != replacing
					&& !owned.contains(candidate)
					&& !Talent.excludedFromMetamorphosis(candidate)) {
				candidates.add(candidate);
			}
		}
		return candidates;
	}

	static boolean replaceCommonTalent(Hero hero, int tier,
			Talent replacing, Talent replacement) {
		if (hero == null || hero.talents == null || tier < 1 || tier > hero.talents.size()
				|| replacing == null || replacement == null
				|| replacing.tier() != tier || replacement.tier() != tier
				|| !replacing.isCommonTalentType() || !replacement.isCommonTalentType()) {
			return false;
		}

		LinkedHashMap<Talent, Integer> currentTier = hero.talents.get(tier - 1);
		if (!currentTier.containsKey(replacing)
				|| !replacementCandidates(hero, tier, replacing).contains(replacement)) {
			return false;
		}

		LinkedHashMap<Talent, Integer> replacedTier = new LinkedHashMap<>();
		for (Map.Entry<Talent, Integer> entry : currentTier.entrySet()) {
			replacedTier.put(entry.getKey() == replacing ? replacement : entry.getKey(),
					entry.getValue());
		}
		hero.talents.set(tier - 1, replacedTier);
		recordMetamorphosis(hero, replacing, replacement);
		return true;
	}

	static TalentChange transformRandomCommonTalent(Hero hero) {
		if (hero == null || hero.talents == null) return null;

		ArrayList<TalentSlot> sources = new ArrayList<>();
		for (int i = 0; i < hero.talents.size(); i++) {
			int tier = i + 1;
			for (Talent talent : hero.talents.get(i).keySet()) {
				if (talent != null && talent.isCommonTalentType()
						&& !replacementCandidates(hero, tier, talent).isEmpty()) {
					sources.add(new TalentSlot(tier, talent));
				}
			}
		}
		if (sources.isEmpty()) return null;

		TalentSlot source = Random.element(sources);
		List<Talent> candidates = replacementCandidates(hero, source.tier, source.talent);
		Talent replacement = Random.element(candidates);
		return replaceCommonTalent(hero, source.tier, source.talent, replacement)
				? new TalentChange(source.talent, replacement) : null;
	}

	private static void recordMetamorphosis(Hero hero, Talent replacing, Talent replacement) {
		if (hero.metamorphedTalents == null) {
			hero.metamorphedTalents = new LinkedHashMap<>();
		}
		if (!hero.metamorphedTalents.containsValue(replacing)) {
			hero.metamorphedTalents.put(replacing, replacement);
		} else if (hero.metamorphedTalents.get(replacement) == replacing) {
			hero.metamorphedTalents.remove(replacement);
		} else {
			for (Talent original : new ArrayList<>(hero.metamorphedTalents.keySet())) {
				if (hero.metamorphedTalents.get(original) == replacing) {
					hero.metamorphedTalents.put(original, replacement);
					break;
				}
			}
		}
	}

	static class TalentChange {
		final Talent replacing;
		final Talent replacement;

		TalentChange(Talent replacing, Talent replacement) {
			this.replacing = replacing;
			this.replacement = replacement;
		}
	}

	private static class TalentSlot {
		final int tier;
		final Talent talent;

		TalentSlot(int tier, Talent talent) {
			this.tier = tier;
			this.talent = talent;
		}
	}
}
