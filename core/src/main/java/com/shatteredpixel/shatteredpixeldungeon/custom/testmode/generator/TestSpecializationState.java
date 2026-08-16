package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DarkHook;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FightStance;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SprayGun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Tatteki;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RitualDagger;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public final class TestSpecializationState {

	private static final int SELECTABLE_CLASS_COUNT = 12;

	private TestSpecializationState() {
	}

	public static List<HeroClass> selectableClasses() {
		HeroClass[] classes = HeroClass.values();
		return Collections.unmodifiableList(
				Arrays.asList(Arrays.copyOf(classes, SELECTABLE_CLASS_COUNT)));
	}

	public static List<ArmorAbilityChoice> armorAbilityChoices() {
		ArrayList<ArmorAbilityChoice> choices = new ArrayList<>();
		for (HeroClass heroClass : selectableClasses()) {
			for (ArmorAbility ability : armorAbilitiesForClass(heroClass)) {
				choices.add(new ArmorAbilityChoice(heroClass, ability));
			}
		}
		return choices;
	}

	public static List<ArmorAbility> armorAbilitiesForClass(HeroClass heroClass) {
		if (!selectableClasses().contains(heroClass)) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(
				new ArrayList<>(Arrays.asList(heroClass.armorAbilities())));
	}

	public static List<Talent> subclassTalents(HeroSubClass subClass) {
		ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
		Talent.initSubclassTalents(subClass, talents);
		if (talents.size() <= 2) {
			return Collections.emptyList();
		}
		return new ArrayList<>(talents.get(2).keySet());
	}

	public static List<Talent> armorTalents(ArmorAbility ability) {
		if (ability == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(ability.talents());
	}

	static void replaceTalentKeys(LinkedHashMap<Talent, Integer> tier,
			Collection<Talent> oldTalents, Collection<Talent> newTalents) {
		for (Talent talent : oldTalents) {
			if (!newTalents.contains(talent)) {
				tier.remove(talent);
			}
		}
		for (Talent talent : newTalents) {
			if (!tier.containsKey(talent)) {
				tier.put(talent, 0);
			}
		}
	}

	static void replaceMappedTalentKeys(LinkedHashMap<Talent, Integer> tier,
			LinkedHashMap<Talent, Talent> metamorphedTalents,
			LinkedHashMap<Talent, String> sublimationTalents,
			Collection<Talent> oldTalents, Collection<Talent> newTalents) {
		for (Talent oldTalent : oldTalents) {
			if (newTalents.contains(oldTalent)) {
				continue;
			}
			tier.remove(oldTalent);

			Talent metamorphed = metamorphedTalents.remove(oldTalent);
			if (metamorphed != null) {
				tier.remove(metamorphed);
			}

			String sublimated = sublimationTalents.remove(oldTalent);
			if (sublimated != null) {
				try {
					tier.remove(Talent.valueOf(sublimated));
				} catch (IllegalArgumentException ignored) {
					// Removed talents can remain in old save mappings.
				}
			}
		}
		for (Talent newTalent : newTalents) {
			if (!tier.containsKey(newTalent)) {
				tier.put(newTalent, 0);
			}
		}
	}

	public static boolean chooseSubclass(Hero hero, HeroSubClass subClass) {
		if (hero == null || subClass == null || !isSelectableSubclass(subClass)) {
			return false;
		}
		ensureTalentTiers(hero);

		HeroSubClass oldSubClass = hero.subClass == null ? HeroSubClass.NONE : hero.subClass;
		if (oldSubClass != subClass) {
			replaceMappedTalentKeys(hero.talents.get(2),
					hero.metamorphedTalents, hero.sublimationTalents,
					subclassTalents(oldSubClass), subclassTalents(subClass));
			removeSubclassBuffs(hero, oldSubClass);
			hero.subClass = subClass;
		}

		grantSubclassKit(hero, subClass);
		applySubclassBuffs(hero);
		MeleeWeapon.syncCharger(hero);
		refreshHero(hero);
		return true;
	}

	public static boolean chooseArmorAbility(Hero hero, ArmorAbility ability) {
		if (hero == null || ability == null || hero.belongings.armor() == null) {
			return false;
		}
		ensureTalentTiers(hero);

		replaceMappedTalentKeys(hero.talents.get(3),
				hero.metamorphedTalents, hero.sublimationTalents,
				armorTalents(hero.armorAbility), armorTalents(ability));

		Armor armor = hero.belongings.armor();
		if (!(armor instanceof ClassArmor)) {
			ClassArmor classArmor = ClassArmor.upgrade(hero, armor);
			hero.belongings.armor = classArmor;
			classArmor.activate(hero);
			if (hero.sprite instanceof HeroSprite) {
				((HeroSprite) hero.sprite).updateArmor();
			}
		}

		hero.armorAbility = ability;
		refreshHero(hero);
		return true;
	}

	private static boolean isSelectableSubclass(HeroSubClass subClass) {
		for (HeroClass heroClass : selectableClasses()) {
			for (HeroSubClass candidate : heroClass.subClasses()) {
				if (candidate == subClass) {
					return true;
				}
			}
		}
		return false;
	}

	private static void ensureTalentTiers(Hero hero) {
		while (hero.talents.size() < Talent.MAX_TALENT_TIERS) {
			hero.talents.add(new LinkedHashMap<>());
		}
	}

	private static void removeSubclassBuffs(Hero hero, HeroSubClass oldSubClass) {
		detach(hero, Preparation.class);
		detach(hero, DarkHook.class);
		detach(hero, Ninja_Energy.class);
		detach(hero, MonkEnergy.class);
		detach(hero, FightStance.class);
		if (oldSubClass == HeroSubClass.PIOUS && hero.heroClass != HeroClass.FRIAR) {
			detach(hero, Reason.class);
		}
	}

	private static void applySubclassBuffs(Hero hero) {
		hero.ensureSubclassBuffs();
	}

	private static void grantSubclassKit(Hero hero, HeroSubClass subClass) {
		if ((subClass == HeroSubClass.BERSERKER || subClass == HeroSubClass.GLADIATOR)
				&& !hasBrokenSeal(hero)) {
			collectOrDrop(hero, new BrokenSeal());
		}
		if (subClass == HeroSubClass.BATTLEMAGE
				&& hero.belongings.getItem(MagesStaff.class) == null) {
			collectOrDrop(hero, new MagesStaff((Wand) Generator.random(Generator.Category.WAND)));
		}
		if (subClass == HeroSubClass.ASSASSIN
				&& hero.belongings.getItem(CloakOfShadows.class) == null) {
			collectOrDrop(hero, new CloakOfShadows());
		}
		if (subClass == HeroSubClass.SNIPER
				&& hero.belongings.getItem(SpiritBow.class) == null) {
			collectOrDrop(hero, new SpiritBow());
		}
		if ((subClass == HeroSubClass.PRIEST || subClass == HeroSubClass.PALADIN)
				&& hero.belongings.getItem(HolyTome.class) == null) {
			collectOrDrop(hero, new HolyTome());
		}
		if ((subClass == HeroSubClass.AT400 || subClass == HeroSubClass.AU400)
				&& hero.belongings.getItem(InstructionTool.class) == null) {
			collectOrDrop(hero, new InstructionTool());
		}
		if (subClass == HeroSubClass.ALCHEMIST
				&& hero.belongings.getItem(SprayGun.class) == null) {
			collectOrDrop(hero, new SprayGun());
		}
		if (subClass == HeroSubClass.TATTEKI_NINJA
				&& hero.belongings.getItem(Tatteki.class) == null) {
			collectOrDrop(hero, new Tatteki());
		}
		if (subClass == HeroSubClass.PIOUS
				&& hero.belongings.getItem(RitualDagger.class) == null) {
			RitualDagger dagger = new RitualDagger();
			collectOrDrop(hero, dagger);
			dagger.resetRitual(hero);
		}
	}

	private static boolean hasBrokenSeal(Hero hero) {
		Armor armor = hero.belongings.armor();
		return hero.belongings.getItem(BrokenSeal.class) != null
				|| armor != null && armor.checkSeal() != null;
	}

	private static void collectOrDrop(Hero hero, Item item) {
		if (item instanceof Artifact
				&& !Generator.claimArtifact(((Artifact) item).getClass())) {
			return;
		}
		item.identify();
		if (!item.collect(hero.belongings.backpack) && Dungeon.level != null) {
			Dungeon.level.drop(item, hero.pos);
		}
	}

	private static <T extends Buff> void detach(Hero hero, Class<T> buffClass) {
		T buff = hero.buff(buffClass);
		if (buff != null) {
			buff.detach();
		}
	}

	private static void refreshHero(Hero hero) {
		hero.updateHT(true);
		Item.updateQuickslot();
		ActionIndicator.refresh();
		BuffIndicator.refreshHero();
	}

	public static final class ArmorAbilityChoice {
		public final HeroClass heroClass;
		public final ArmorAbility ability;

		private ArmorAbilityChoice(HeroClass heroClass, ArmorAbility ability) {
			this.heroClass = heroClass;
			this.ability = ability;
		}
	}
}
