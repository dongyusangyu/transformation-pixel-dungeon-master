package com.shatteredpixel.shatteredpixeldungeon.custom.agentMin;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class AgentMinMetamorphPlanner {

	private AgentMinMetamorphPlanner() {
	}

	public static int maxUnlockedTalentTier(Hero hero) {
		if (hero == null) {
			return 1;
		}
		if (hero.lvl < 6) {
			return 1;
		}
		if (hero.lvl < 13) {
			return 2;
		}
		return Integer.MAX_VALUE;
	}

	public static boolean tierAllowed(Hero hero, int tier) {
		return tier > 0 && tier <= maxUnlockedTalentTier(hero);
	}

	public static Talent.TalentType typeFromOrdinal(int ordinal) {
		Talent.TalentType[] values = Talent.TalentType.values();
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
	}

	public static Choice choose(Hero hero, String preferredSourceName, Talent.TalentType preferredType, String preferredTargetName) {
		ArrayList<Choice> choices = choices(hero);
		if (choices.isEmpty()) {
			return null;
		}
		ArrayList<Choice> sourceChoices = filterBySource(choices, preferredSourceName);
		ArrayList<Choice> typeChoices = filterByType(sourceChoices, preferredType);
		ArrayList<Choice> finalChoices = typeChoices.isEmpty() ? sourceChoices : typeChoices;
		if (preferredTargetName != null && preferredTargetName.length() > 0) {
			for (Choice choice : finalChoices) {
				if (choice.newTalent.name().equals(preferredTargetName)) {
					return choice;
				}
			}
		}
		return finalChoices.isEmpty() ? choices.get(0) : finalChoices.get(0);
	}

	public static Choice choose(Hero hero, String preferredTargetName) {
		return choose(hero, null, null, preferredTargetName);
	}

	private static ArrayList<Choice> filterBySource(ArrayList<Choice> choices, String preferredSourceName) {
		if (preferredSourceName == null || preferredSourceName.length() == 0) {
			return choices;
		}
		ArrayList<Choice> filtered = new ArrayList<>();
		for (Choice choice : choices) {
			if (choice.oldTalent.name().equals(preferredSourceName)) {
				filtered.add(choice);
			}
		}
		return filtered.isEmpty() ? choices : filtered;
	}

	private static ArrayList<Choice> filterByType(ArrayList<Choice> choices, Talent.TalentType preferredType) {
		if (preferredType == null) {
			return choices;
		}
		ArrayList<Choice> filtered = new ArrayList<>();
		for (Choice choice : choices) {
			if (choice.targetType == preferredType) {
				filtered.add(choice);
			}
		}
		return filtered;
	}

	public static String[] targetNames(Hero hero) {
		ArrayList<Talent> targets = uniqueTargets(hero);
		String[] names = new String[targets.size()];
		for (int i = 0; i < targets.size(); i++) {
			names[i] = targets.get(i).name();
		}
		return names;
	}

	public static int[] targetIcons(Hero hero) {
		ArrayList<Talent> targets = uniqueTargets(hero);
		int[] icons = new int[targets.size()];
		for (int i = 0; i < targets.size(); i++) {
			icons[i] = Math.max(0, targets.get(i).icon());
		}
		return icons;
	}

	public static String[] sourceNames(Hero hero) {
		ArrayList<Talent> sources = uniqueSources(hero);
		String[] names = new String[sources.size()];
		for (int i = 0; i < sources.size(); i++) {
			names[i] = sources.get(i).name();
		}
		return names;
	}

	public static int[] sourceIcons(Hero hero) {
		ArrayList<Talent> sources = uniqueSources(hero);
		int[] icons = new int[sources.size()];
		for (int i = 0; i < sources.size(); i++) {
			icons[i] = Math.max(0, sources.get(i).icon());
		}
		return icons;
	}

	public static String[] choiceSourceNames(Hero hero) {
		ArrayList<Choice> choices = choices(hero);
		String[] names = new String[choices.size()];
		for (int i = 0; i < choices.size(); i++) {
			names[i] = choices.get(i).oldTalent.name();
		}
		return names;
	}

	public static String[] choiceTargetNames(Hero hero) {
		ArrayList<Choice> choices = choices(hero);
		String[] names = new String[choices.size()];
		for (int i = 0; i < choices.size(); i++) {
			names[i] = choices.get(i).newTalent.name();
		}
		return names;
	}

	public static int[] choiceTargetIcons(Hero hero) {
		ArrayList<Choice> choices = choices(hero);
		int[] icons = new int[choices.size()];
		for (int i = 0; i < choices.size(); i++) {
			icons[i] = Math.max(0, choices.get(i).newTalent.icon());
		}
		return icons;
	}

	public static int[] choiceTargetTypes(Hero hero) {
		ArrayList<Choice> choices = choices(hero);
		int[] types = new int[choices.size()];
		for (int i = 0; i < choices.size(); i++) {
			types[i] = choices.get(i).targetType.ordinal();
		}
		return types;
	}

	public static String[] upgradeCandidateNames(Hero hero) {
		ArrayList<Talent> talents = upgradeCandidates(hero);
		String[] names = new String[talents.size()];
		for (int i = 0; i < talents.size(); i++) {
			names[i] = talents.get(i).name();
		}
		return names;
	}

	public static int[] upgradeCandidateIcons(Hero hero) {
		ArrayList<Talent> talents = upgradeCandidates(hero);
		int[] icons = new int[talents.size()];
		for (int i = 0; i < talents.size(); i++) {
			icons[i] = Math.max(0, talents.get(i).icon());
		}
		return icons;
	}

	public static Talent chooseUpgrade(Hero hero, String preferredTalentName) {
		ArrayList<Talent> candidates = upgradeCandidates(hero);
		if (candidates.isEmpty()) {
			return null;
		}
		if (preferredTalentName != null && preferredTalentName.length() > 0) {
			for (Talent talent : candidates) {
				if (talent.name().equals(preferredTalentName)) {
					return talent;
				}
			}
		}
		return candidates.get(0);
	}

	public static String[] sublimationCandidateNames(Hero hero) {
		ArrayList<Talent> talents = sublimationCandidates(hero);
		String[] names = new String[talents.size()];
		for (int i = 0; i < talents.size(); i++) {
			names[i] = talents.get(i).name();
		}
		return names;
	}

	public static int[] sublimationCandidateIcons(Hero hero) {
		ArrayList<Talent> talents = sublimationCandidates(hero);
		int[] icons = new int[talents.size()];
		for (int i = 0; i < talents.size(); i++) {
			icons[i] = Math.max(0, talents.get(i).icon());
		}
		return icons;
	}

	public static Talent chooseSublimation(ScrollOfSublimation scroll, String preferredTalentName) {
		if (scroll == null) {
			return null;
		}
		List<Talent> targets = sublimationTargets(scroll.type());
		if (targets.isEmpty()) {
			return null;
		}
		if (preferredTalentName != null && preferredTalentName.length() > 0) {
			for (Talent talent : targets) {
				if (talent.name().equals(preferredTalentName)) {
					return talent;
				}
			}
		}
		return targets.get(0);
	}

	public static List<Talent> sublimationTargets(String type) {
		ArrayList<Talent> talents = new ArrayList<>();
		switch (type) {
			case "WARRIOR":
				talents.add(Talent.STRONGEST_SHIELD);
				talents.add(Talent.COMBO_PACKAGE);
				talents.add(Talent.BREAK_ENEMY_RANKS);
				break;
			case "TENGU":
				talents.add(Talent.SURPRISE_THROW);
				talents.add(Talent.SMOKE_MASK);
				talents.add(Talent.RUSH);
				break;
			case "ROGUE":
				talents.add(Talent.SHADOW_KILLER);
				talents.add(Talent.KILL_SPREE);
				talents.add(Talent.SEAOFPEOPLE);
				talents.add(Talent.PHANTOM_STEP);
				break;
			case "DM300":
				talents.add(Talent.FASTING);
				talents.add(Talent.THUNDER_STRIKE);
				talents.add(Talent.DIRECTIONAL_COLLAPSE);
				break;
			case "HUNTRESS":
				talents.add(Talent.HUNTING_TECHNIQUE);
				talents.add(Talent.NATURAL_CHILD);
				talents.add(Talent.FALCON_EYE);
				break;
			case "DWARFKING":
				talents.add(Talent.KING_PROTECT);
				talents.add(Talent.SUMMON_FOLLOWER);
				talents.add(Talent.WOLFISH_GAZE);
				talents.add(Talent.ENERGY_CONVERSION);
				break;
			case "YOG":
				talents.add(Talent.YOG_LARVA);
				talents.add(Talent.YOG_FIST);
				talents.add(Talent.YOG_RAY);
				break;
			case "GOO":
			default:
				talents.add(Talent.AQUATIC_RECOVER);
				talents.add(Talent.PUMP_ATTACK);
				talents.add(Talent.OOZE_ATTACK);
				break;
		}
		return talents;
	}

	public static int sublimationTier(String type) {
		switch (type) {
			case "DM300":
			case "HUNTRESS":
			case "DWARFKING":
				return 2;
			case "YOG":
				return 3;
			case "GOO":
			case "WARRIOR":
			case "TENGU":
			case "ROGUE":
			default:
				return 1;
		}
	}

	public static int sublimationIndex(String type) {
		switch (type) {
			case "TENGU":
			case "ROGUE":
			case "DM300":
			case "HUNTRESS":
				return 2;
			case "DWARFKING":
			case "YOG":
				return 3;
			case "GOO":
			case "WARRIOR":
			default:
				return 1;
		}
	}

	public static ArrayList<Choice> choices(Hero hero) {
		ArrayList<Choice> choices = new ArrayList<>();
		if (hero == null || hero.talents == null) {
			return choices;
		}
		int maxTier = Math.min(hero.talents.size(), maxUnlockedTalentTier(hero));
		for (int tierIndex = 0; tierIndex < maxTier; tierIndex++) {
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(tierIndex);
			if (tier == null) {
				continue;
			}
			for (Talent oldTalent : tier.keySet()) {
				Integer points = tier.get(oldTalent);
				if (points == null || oldTalent == null || oldTalent.maxPoints() <= 0 || Talent.excludedAsMetamorphSource(oldTalent)) {
					continue;
				}
				for (Talent.TalentType type : ScrollOfMetamorphosis.commonTypes()) {
					for (Talent newTalent : targetPoolForType(hero, tierIndex + 1, oldTalent, type)) {
						choices.add(new Choice(tierIndex + 1, oldTalent, newTalent, points, type));
					}
				}
			}
		}
		return choices;
	}

	private static ArrayList<Talent> uniqueTargets(Hero hero) {
		LinkedHashSet<Talent> targets = new LinkedHashSet<>();
		for (Choice choice : choices(hero)) {
			targets.add(choice.newTalent);
		}
		return new ArrayList<>(targets);
	}

	private static ArrayList<Talent> uniqueSources(Hero hero) {
		LinkedHashSet<Talent> sources = new LinkedHashSet<>();
		for (Choice choice : choices(hero)) {
			sources.add(choice.oldTalent);
		}
		return new ArrayList<>(sources);
	}

	private static ArrayList<Talent> targetPoolForType(Hero hero, int tier, Talent oldTalent, Talent.TalentType type) {
		ArrayList<Talent> targets = new ArrayList<>();
		if (hero == null || hero.talents == null || tier <= 0 || tier > hero.talents.size() || type == null) {
			return targets;
		}
		HashSet<Talent> excluded = new HashSet<>(hero.talents.get(tier - 1).keySet());
		for (Talent candidate : Talent.talentsByTierAndType(tier, type)) {
			if (candidate != null
					&& candidate != oldTalent
					&& !excluded.contains(candidate)
					&& !Talent.excludedFromMetamorphosis(candidate)) {
				targets.add(candidate);
			}
		}
		return targets;
	}

	private static ArrayList<Talent> upgradeCandidates(Hero hero) {
		ArrayList<Talent> candidates = new ArrayList<>();
		if (hero == null || hero.talents == null) {
			return candidates;
		}
		int maxTier = Math.min(hero.talents.size(), maxUnlockedTalentTier(hero));
		for (int tierIndex = 0; tierIndex < maxTier; tierIndex++) {
			int tier = tierIndex + 1;
			if (hero.talentPointsAvailable(tier) <= 0) {
				continue;
			}
			LinkedHashMap<Talent, Integer> talents = hero.talents.get(tierIndex);
			if (talents == null) {
				continue;
			}
			for (Talent talent : talents.keySet()) {
				if (talent != null && hero.canUpgradeTalent(talent)) {
					candidates.add(talent);
				}
			}
		}
		return candidates;
	}

	private static ArrayList<Talent> sublimationCandidates(Hero hero) {
		LinkedHashSet<Talent> talents = new LinkedHashSet<>();
		for (Item item : liveItems(hero)) {
			if (item instanceof ScrollOfSublimation) {
				int tier = sublimationTier(((ScrollOfSublimation)item).type());
				if (tierAllowed(hero, tier)) {
					talents.addAll(sublimationTargets(((ScrollOfSublimation)item).type()));
				}
			}
		}
		return new ArrayList<>(talents);
	}

	private static ArrayList<Item> liveItems(Hero hero) {
		ArrayList<Item> items = new ArrayList<>();
		if (hero == null || hero.belongings == null) {
			return items;
		}
		addItem(items, hero.belongings.weapon());
		addItem(items, hero.belongings.armor());
		addItem(items, hero.belongings.artifact());
		addItem(items, hero.belongings.misc());
		addItem(items, hero.belongings.ring());
		addItem(items, hero.belongings.secondWep());
		if (hero.belongings.backpack != null) {
			for (Item item : hero.belongings.backpack.items) {
				addItemTree(items, item);
			}
		}
		return items;
	}

	private static void addItemTree(ArrayList<Item> items, Item item) {
		addItem(items, item);
		if (item instanceof Bag) {
			for (Item child : ((Bag)item).items) {
				addItemTree(items, child);
			}
		}
	}

	private static void addItem(ArrayList<Item> items, Item item) {
		if (item != null) {
			items.add(item);
		}
	}

	public static class Choice {
		public final int tier;
		public final Talent oldTalent;
		public final Talent newTalent;
		public final int points;
		public final Talent.TalentType targetType;

		Choice(int tier, Talent oldTalent, Talent newTalent, int points, Talent.TalentType targetType) {
			this.tier = tier;
			this.oldTalent = oldTalent;
			this.newTalent = newTalent;
			this.points = points;
			this.targetType = targetType;
		}
	}
}
