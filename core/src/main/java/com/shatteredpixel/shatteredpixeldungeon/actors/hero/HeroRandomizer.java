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
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;

public class HeroRandomizer {

	private static final String RANDOM_MODE = "random_mode";
	private static final String RANDOM_TALENT_CLASS = "random_talent_class";
	private static final String RANDOM_CLASS_TALENTS = "random_class_talents";
	private static final String RANDOM_SUBCLASSES = "random_subclasses";
	private static final String RANDOM_ARMOR_ABILITIES = "random_armor_abilities";

	private static final long RANDOMIZER_SEED = 0x51A7E17D0L;

	public static void setupNewRun(Hero hero, HeroClass selectedClass) {
		if (selectedClass == null) {
			selectedClass = HeroClass.WARRIOR;
		}
		hero.randomMode = SPDSettings.randomMode() && selectedClass != HeroClass.RATKING;
		hero.randomTalentClass = null;
		hero.randomClassTalents = null;
		hero.randomSubClasses = null;
		hero.randomArmorAbilities = null;

		if (!hero.randomMode) {
			return;
		}

		java.util.Random rng = new java.util.Random(Dungeon.seed ^ RANDOMIZER_SEED ^ ((long)selectedClass.ordinal() << 32));

		hero.randomClassTalents = randomClassTalents(selectedClass, rng);

		ArrayList<HeroSubClass> subClasses = randomSubClassPool();
		Collections.shuffle(subClasses, rng);
		hero.randomSubClasses = subClasses.subList(0, Math.min(subClassChoiceCount(selectedClass), subClasses.size()))
				.toArray(new HeroSubClass[0]);

		ArrayList<ArmorAbility> abilities = randomArmorAbilityPool();
		Collections.shuffle(abilities, rng);
		int count = Math.min(armorAbilityChoiceCount(selectedClass), abilities.size());
		hero.randomArmorAbilities = new String[count];
		for (int i = 0; i < count; i++) {
			hero.randomArmorAbilities[i] = abilities.get(i).getClass().getName();
		}
	}

	public static boolean active(Hero hero) {
		return hero != null
				&& hero.randomMode
				&& hero.heroClass != HeroClass.RATKING
				&& (hasRandomClassTalents(hero) || hero.randomTalentClass != null)
				&& hero.randomSubClasses != null
				&& hero.randomSubClasses.length > 0
				&& hero.randomArmorAbilities != null
				&& hero.randomArmorAbilities.length > 0;
	}

	public static HeroClass talentClass(Hero hero) {
		if (hero == null) {
			return HeroClass.WARRIOR;
		}
		return active(hero) && hero.randomTalentClass != null ? hero.randomTalentClass : hero.heroClass;
	}

	public static boolean hasRandomClassTalents(Hero hero) {
		return hero != null
				&& hero.randomMode
				&& hero.randomClassTalents != null
				&& hero.randomClassTalents.length >= Talent.MAX_TALENT_TIERS;
	}

	public static ArrayList<Talent> classTalents(Hero hero, int tier) {
		ArrayList<Talent> talents = new ArrayList<>();
		if (!hasRandomClassTalents(hero) || tier < 1 || tier > hero.randomClassTalents.length) {
			return talents;
		}
		String values = hero.randomClassTalents[tier - 1];
		if (values == null || values.isEmpty()) {
			return talents;
		}
		for (String name : values.split(",")) {
			try {
				Talent talent = Talent.valueOf(name);
				if (isAllowedRandomClassTalent(talent, tier) && !talents.contains(talent)) {
					talents.add(talent);
				}
			} catch (Exception ignored) {
			}
		}
		return talents;
	}

	public static boolean randomInitialTalentNeedsMetaDesc(Hero hero, Talent talent) {
		return hasRandomClassTalents(hero)
				&& randomClassTalent(hero, talent)
				&& !nativeClassTalent(hero.heroClass, talent);
	}

	public static HeroSubClass[] subClasses(Hero hero) {
		if (hero == null) {
			return HeroClass.WARRIOR.subClasses();
		}
		return active(hero) ? hero.randomSubClasses : hero.heroClass.subClasses();
	}

	public static ArmorAbility[] armorAbilities(Hero hero) {
		if (hero == null) {
			return HeroClass.WARRIOR.armorAbilities();
		}
		if (!active(hero)) {
			return hero.heroClass.armorAbilities();
		}

		ArrayList<ArmorAbility> result = new ArrayList<>();
		for (String abilityName : hero.randomArmorAbilities) {
			ArmorAbility ability = abilityByName(abilityName);
			if (ability != null) {
				result.add(ability);
			}
		}
		if (result.isEmpty()) {
			return hero.heroClass.armorAbilities();
		}
		return result.toArray(new ArmorAbility[0]);
	}

	public static void storeInBundle(Bundle bundle, Hero hero) {
		bundle.put(RANDOM_MODE, hero.randomMode);
		if (hero.randomTalentClass != null) {
			bundle.put(RANDOM_TALENT_CLASS, hero.randomTalentClass.name());
		}
		if (hero.randomClassTalents != null) {
			bundle.put(RANDOM_CLASS_TALENTS, hero.randomClassTalents);
		}
		if (hero.randomSubClasses != null) {
			String[] subClasses = new String[hero.randomSubClasses.length];
			for (int i = 0; i < hero.randomSubClasses.length; i++) {
				subClasses[i] = hero.randomSubClasses[i].name();
			}
			bundle.put(RANDOM_SUBCLASSES, subClasses);
		}
		if (hero.randomArmorAbilities != null) {
			bundle.put(RANDOM_ARMOR_ABILITIES, hero.randomArmorAbilities);
		}
	}

	public static void restoreFromBundle(Bundle bundle, Hero hero) {
		hero.randomMode = bundle.contains(RANDOM_MODE) && bundle.getBoolean(RANDOM_MODE);
		hero.randomTalentClass = null;
		hero.randomClassTalents = null;
		hero.randomSubClasses = null;
		hero.randomArmorAbilities = null;

		if (!hero.randomMode) {
			return;
		}

		if (bundle.contains(RANDOM_TALENT_CLASS)) {
			try {
				HeroClass cls = HeroClass.valueOf(bundle.getString(RANDOM_TALENT_CLASS));
				if (cls != HeroClass.RATKING) {
					hero.randomTalentClass = cls;
				}
			} catch (Exception ignored) {
				hero.randomTalentClass = null;
			}
		}
		if (bundle.contains(RANDOM_CLASS_TALENTS)) {
			String[] values = bundle.getStringArray(RANDOM_CLASS_TALENTS);
			if (values != null) {
				hero.randomClassTalents = new String[Talent.MAX_TALENT_TIERS];
				for (int i = 0; i < hero.randomClassTalents.length && i < values.length; i++) {
					hero.randomClassTalents[i] = values[i];
				}
			}
		}

		if (bundle.contains(RANDOM_SUBCLASSES)) {
			ArrayList<HeroSubClass> subClasses = new ArrayList<>();
			for (String name : bundle.getStringArray(RANDOM_SUBCLASSES)) {
				try {
					HeroSubClass subClass = HeroSubClass.valueOf(name);
					if (isAllowedRandomSubClass(subClass)) {
						subClasses.add(subClass);
					}
				} catch (Exception ignored) {
				}
			}
			hero.randomSubClasses = subClasses.toArray(new HeroSubClass[0]);
		}

		if (bundle.contains(RANDOM_ARMOR_ABILITIES)) {
			ArrayList<String> abilities = new ArrayList<>();
			for (String name : bundle.getStringArray(RANDOM_ARMOR_ABILITIES)) {
				ArmorAbility ability = abilityByName(name);
				if (ability != null && isAllowedRandomArmorAbility(ability)) {
					abilities.add(name);
				}
			}
			hero.randomArmorAbilities = abilities.toArray(new String[0]);
		}

		if (!active(hero)) {
			hero.randomMode = false;
			hero.randomTalentClass = null;
			hero.randomClassTalents = null;
			hero.randomSubClasses = null;
			hero.randomArmorAbilities = null;
		}
	}

	private static ArrayList<HeroClass> randomClassPool() {
		ArrayList<HeroClass> classes = new ArrayList<>();
		for (HeroClass cls : HeroClass.values()) {
			if (cls != HeroClass.RATKING) {
				classes.add(cls);
			}
		}
		return classes;
	}

	private static String[] randomClassTalents(HeroClass selectedClass, java.util.Random rng) {
		String[] talents = new String[Talent.MAX_TALENT_TIERS];
		for (int tier = 1; tier <= Talent.MAX_TALENT_TIERS; tier++) {
			ArrayList<Talent> candidates = randomClassTalentPool(tier);
			Collections.shuffle(candidates, rng);
			int count = Math.min(classTalentSlotCount(selectedClass, tier), candidates.size());
			StringBuilder names = new StringBuilder();
			for (int i = 0; i < count; i++) {
				if (names.length() > 0) {
					names.append(',');
				}
				names.append(candidates.get(i).name());
			}
			talents[tier - 1] = names.toString();
		}
		return talents;
	}

	private static ArrayList<Talent> randomClassTalentPool(int tier) {
		ArrayList<Talent> pool = new ArrayList<>();
		for (Talent talent : Talent.values()) {
			if (isAllowedRandomClassTalent(talent, tier) && !pool.contains(talent)) {
				pool.add(talent);
			}
		}
		return pool;
	}

	private static int classTalentSlotCount(HeroClass selectedClass, int tier) {
		if (selectedClass == HeroClass.FREEMAN && tier == 3) {
			return 1;
		}
		ArrayList<java.util.LinkedHashMap<Talent, Integer>> classTalents = new ArrayList<>();
		Talent.initClassTalents(selectedClass, classTalents);
		int count = 0;
		for (Talent talent : classTalents.get(tier - 1).keySet()) {
			if (isAllowedRandomClassTalent(talent, tier)) {
				count++;
			}
		}
		return count;
	}

	private static boolean isAllowedRandomClassTalent(Talent talent, int tier) {
		return talent != null
				&& talent.tier() == tier
				&& talent.isCommonTalentType()
				&& !isRatKingTalent(talent);
	}

	private static boolean isRatKingTalent(Talent talent) {
		return talent == Talent.ROYAL_PRIVILEGE
				|| talent == Talent.ROYAL_INTUITION
				|| talent == Talent.KINGS_WISDOM
				|| talent == Talent.NOBLE_CAUSE
				|| talent == Talent.RK_ROYAL_MEAL
				|| talent == Talent.RESTORATION
				|| talent == Talent.POWER_WITHIN
				|| talent == Talent.KINGS_VISION
				|| talent == Talent.PURSUIT;
	}

	private static boolean randomClassTalent(Hero hero, Talent talent) {
		if (hero == null || talent == null) {
			return false;
		}
		for (int tier = 1; tier <= hero.randomClassTalents.length; tier++) {
			if (classTalents(hero, tier).contains(talent)) {
				return true;
			}
		}
		return false;
	}

	public static boolean nativeClassTalent(HeroClass heroClass, Talent talent) {
		if (heroClass == null || talent == null) {
			return false;
		}
		ArrayList<java.util.LinkedHashMap<Talent, Integer>> nativeTalents = new ArrayList<>();
		Talent.initClassTalents(heroClass, nativeTalents);
		for (java.util.LinkedHashMap<Talent, Integer> tier : nativeTalents) {
			if (tier.containsKey(talent)) {
				return true;
			}
		}
		return false;
	}

	private static ArrayList<HeroSubClass> randomSubClassPool() {
		ArrayList<HeroSubClass> subClasses = new ArrayList<>();
		for (HeroClass cls : randomClassPool()) {
			for (HeroSubClass subClass : cls.subClasses()) {
				if (isAllowedRandomSubClass(subClass) && !subClasses.contains(subClass)) {
					subClasses.add(subClass);
				}
			}
		}
		return subClasses;
	}

	private static ArrayList<ArmorAbility> randomArmorAbilityPool() {
		ArrayList<ArmorAbility> abilities = new ArrayList<>();
		for (HeroClass cls : randomClassPool()) {
			for (ArmorAbility ability : cls.armorAbilities()) {
				if (isAllowedRandomArmorAbility(ability)) {
					abilities.add(ability);
				}
			}
		}
		return abilities;
	}

	private static int subClassChoiceCount(HeroClass selectedClass) {
		int count = 0;
		for (HeroSubClass subClass : selectedClass.subClasses()) {
			if (isAllowedRandomSubClass(subClass)) {
				count++;
			}
		}
		return Math.max(2, count);
	}

	private static int armorAbilityChoiceCount(HeroClass selectedClass) {
		if (selectedClass == HeroClass.FREEMAN) {
			return 3;
		}
		return Math.max(2, selectedClass.armorAbilities().length);
	}

	private static boolean isAllowedRandomSubClass(HeroSubClass subClass) {
		return subClass != HeroSubClass.NONE
				&& subClass != HeroSubClass.KING
				&& subClass != HeroSubClass.FREEMAN;
	}

	private static boolean isAllowedRandomArmorAbility(ArmorAbility ability) {
		return !(ability instanceof Ratmogrify);
	}

	private static ArmorAbility abilityByName(String abilityName) {
		if (abilityName == null) {
			return null;
		}
		for (ArmorAbility ability : randomArmorAbilityPool()) {
			if (ability.getClass().getName().equals(abilityName)) {
				return ability;
			}
		}
		return null;
	}
}
