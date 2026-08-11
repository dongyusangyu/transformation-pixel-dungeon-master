package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetamorphosisPrismTest {

	@Test
	public void candidatesOnlyContainOtherTalentsFromSameBossSlot() {
		List<Talent> candidates = MetamorphosisPrism.eligibleBossTalents(Talent.AQUATIC_RECOVER);

		assertFalse(candidates.contains(Talent.AQUATIC_RECOVER));
		assertTrue(candidates.contains(Talent.PUMP_ATTACK));
		assertTrue(candidates.contains(Talent.STRONGEST_SHIELD));
		assertFalse(candidates.contains(Talent.SURPRISE_THROW));
		for (Talent candidate : candidates) {
			assertEquals(Talent.BOSS_TALENT_SLOT_1, Talent.bossTalentSlot(candidate));
		}
	}

	@Test
	public void smallBossSlotsReturnTheirActualCandidateCount() {
		List<Talent> candidates = MetamorphosisPrism.chooseBossCandidates(Talent.FASTING, 4);

		assertEquals(2, candidates.size());
		assertTrue(candidates.contains(Talent.THUNDER_STRIKE));
		assertTrue(candidates.contains(Talent.DIRECTIONAL_COLLAPSE));
	}

	@Test
	public void replacementPreservesInvestedPointsAndOrder() {
		LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
		tier.put(Talent.IRON_STOMACH, 1);
		tier.put(Talent.AQUATIC_RECOVER, 2);
		tier.put(Talent.IRON_WILL, 1);

		LinkedHashMap<Talent, Integer> replaced = MetamorphosisPrism.replaceInTier(
				tier, Talent.AQUATIC_RECOVER, Talent.PUMP_ATTACK);

		assertEquals(3, replaced.size());
		assertEquals(Integer.valueOf(2), replaced.get(Talent.PUMP_ATTACK));
		assertFalse(replaced.containsKey(Talent.AQUATIC_RECOVER));
		assertEquals(
				"[IRON_STOMACH, PUMP_ATTACK, IRON_WILL]",
				replaced.keySet().toString());
	}

	@Test
	public void warriorSubclassesHaveIndependentExpandedTalentPools() {
		List<Talent> berserkerPool = Talent.subclassTalentPool(HeroSubClass.BERSERKER);
		List<Talent> gladiatorPool = Talent.subclassTalentPool(HeroSubClass.GLADIATOR);

		assertEquals(6, berserkerPool.size());
		assertTrue(berserkerPool.containsAll(Arrays.asList(
				Talent.ENDLESS_RAGE, Talent.DEATHLESS_FURY, Talent.ENRAGED_CATALYST,
				Talent.CEASELESS_RAGE, Talent.MIRRORED_REVENGE, Talent.BLOODTHIRSTY_BERSERK)));
		assertEquals(berserkerPool, Talent.subclassTalentPool(Talent.CEASELESS_RAGE));
		assertFalse(berserkerPool.contains(Talent.CLEAVE));
		assertEquals(6, gladiatorPool.size());
		assertTrue(gladiatorPool.containsAll(Arrays.asList(
				Talent.CLEAVE, Talent.LETHAL_DEFENSE, Talent.ENHANCED_COMBO,
				Talent.COMBO_FOCUS, Talent.RELENTLESS_COMBAT, Talent.COMBO_MASTERY)));
		assertFalse(gladiatorPool.contains(Talent.ENDLESS_RAGE));
	}

	@Test
	public void allTwentyTwoNormalSubclassesHaveIndependentPools() {
		int poolCount = 0;
		for (HeroSubClass subClass : HeroSubClass.values()) {
			if (subClass == HeroSubClass.NONE
					|| subClass == HeroSubClass.FREEMAN
					|| subClass == HeroSubClass.KING) {
				continue;
			}
			int expectedSize = subClass == HeroSubClass.BERSERKER
					|| subClass == HeroSubClass.GLADIATOR
					|| subClass == HeroSubClass.BATTLEMAGE
					|| subClass == HeroSubClass.WARLOCK
					|| subClass == HeroSubClass.ASSASSIN
					|| subClass == HeroSubClass.FREERUNNER
					|| subClass == HeroSubClass.CHAMPION
					|| subClass == HeroSubClass.MONK ? 6 : 3;
			assertEquals(subClass.name(), expectedSize, Talent.subclassTalentPool(subClass).size());
			poolCount++;
		}
		assertEquals(22, poolCount);
	}

	@Test
	public void threeTalentSubclassPoolsCannotBeMetamorphosed() {
		HashSet<Talent> owned = new HashSet<>(Arrays.asList(Talent.FARSIGHT));

		assertFalse(MetamorphosisPrism.subclassPoolSupportsMetamorphosis(
				Talent.subclassTalentPool(HeroSubClass.SNIPER)));
		assertTrue(MetamorphosisPrism.eligibleSubclassTalents(
				Talent.FARSIGHT, owned).isEmpty());
		assertTrue(MetamorphosisPrism.chooseSubclassCandidates(
				Talent.FARSIGHT, owned, 3).isEmpty());
	}

	@Test
	public void expandedSubclassPoolsAutomaticallyEnableMetamorphosis() {
		HashSet<Talent> owned = new HashSet<>(Arrays.asList(
				Talent.ENDLESS_RAGE, Talent.DEATHLESS_FURY, Talent.ENRAGED_CATALYST));
		assertTrue(MetamorphosisPrism.subclassPoolSupportsMetamorphosis(
				Talent.subclassTalentPool(HeroSubClass.BERSERKER)));
		assertEquals(3, MetamorphosisPrism.eligibleSubclassTalents(
				Talent.ENDLESS_RAGE, owned).size());
	}

	@Test
	public void subclassInitializationAppliesStoredMetamorphosis() {
		ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
		LinkedHashMap<Talent, Talent> replacements = new LinkedHashMap<>();
		replacements.put(Talent.ENDLESS_RAGE, Talent.CLEAVE);

		Talent.initSubclassTalents(HeroSubClass.BERSERKER, talents, replacements);

		assertFalse(talents.get(2).containsKey(Talent.ENDLESS_RAGE));
		assertTrue(talents.get(2).containsKey(Talent.CLEAVE));
		assertTrue(talents.get(2).containsKey(Talent.DEATHLESS_FURY));
	}

	@Test
	public void repeatedSubclassMetamorphosisKeepsOneLevelSaveMapping() {
		LinkedHashMap<Talent, Talent> replacements = new LinkedHashMap<>();
		replacements.put(Talent.ENDLESS_RAGE, Talent.CLEAVE);

		MetamorphosisPrism.recordMetamorphosis(
				replacements, Talent.CLEAVE, Talent.LETHAL_DEFENSE);

		assertEquals(1, replacements.size());
		assertEquals(Talent.LETHAL_DEFENSE, replacements.get(Talent.ENDLESS_RAGE));

		MetamorphosisPrism.recordMetamorphosis(
				replacements, Talent.LETHAL_DEFENSE, Talent.ENDLESS_RAGE);
		assertTrue(replacements.isEmpty());
	}

	@Test
	public void recipeUsesThreeScrollsAndThreeEnergy() throws Exception {
		MetamorphosisPrism.Recipe recipe = new MetamorphosisPrism.Recipe();

		assertEquals(ScrollOfMetamorphosis.class,
				((Class<?>[]) readField(recipe, "inputs"))[0]);
		assertEquals(3, ((int[]) readField(recipe, "inQuantity"))[0]);
		assertEquals(3, readField(recipe, "cost"));
		assertEquals(MetamorphosisPrism.class, readField(recipe, "output"));
		assertEquals(1, readField(recipe, "outQuantity"));
	}

	private static Object readField(Object target, String name) throws Exception {
		Field field = Recipe.SimpleRecipe.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}
}
