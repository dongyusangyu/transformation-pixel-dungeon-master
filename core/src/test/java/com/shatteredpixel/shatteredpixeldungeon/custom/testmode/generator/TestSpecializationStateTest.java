package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestSpecializationStateTest {

	@Test
	public void selectableClassesAreFirstTwelveAndExcludeRatKing() {
		List<HeroClass> classes = TestSpecializationState.selectableClasses();

		assertEquals(12, classes.size());
		assertEquals(HeroClass.WARRIOR, classes.get(0));
		assertEquals(HeroClass.FRIAR, classes.get(11));
		assertFalse(classes.contains(HeroClass.RATKING));
	}

	@Test
	public void armorAbilityPoolExcludesRatKingButKeepsFreemanRatmogrify() {
		List<TestSpecializationState.ArmorAbilityChoice> choices =
				TestSpecializationState.armorAbilityChoices();

		assertEquals(35, choices.size());
		assertFalse(choices.stream().anyMatch(choice -> choice.heroClass == HeroClass.RATKING));
		assertTrue(choices.stream().anyMatch(choice ->
				choice.heroClass == HeroClass.FREEMAN && choice.ability instanceof Ratmogrify));
	}

	@Test
	public void armorAbilitiesAreFilteredBySelectedClass() {
		assertEquals(3, TestSpecializationState.armorAbilitiesForClass(
				HeroClass.WARRIOR).size());
		assertTrue(TestSpecializationState.armorAbilitiesForClass(
				HeroClass.WARRIOR).stream().anyMatch(ability -> ability instanceof HeroicLeap));
		assertFalse(TestSpecializationState.armorAbilitiesForClass(
				HeroClass.MAGE).stream().anyMatch(ability -> ability instanceof HeroicLeap));
		assertTrue(TestSpecializationState.armorAbilitiesForClass(
				HeroClass.RATKING).isEmpty());
	}

	@Test
	public void replacingTalentKeysPreservesUnrelatedTalentsAndPoints() {
		LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
		tier.put(Talent.ENDLESS_RAGE, 2);
		tier.put(Talent.CLEAVE, 1);

		TestSpecializationState.replaceTalentKeys(
				tier,
				Arrays.asList(Talent.ENDLESS_RAGE),
				Arrays.asList(Talent.EMPOWERED_STRIKE, Talent.MYSTICAL_CHARGE));

		assertFalse(tier.containsKey(Talent.ENDLESS_RAGE));
		assertEquals(Integer.valueOf(1), tier.get(Talent.CLEAVE));
		assertEquals(Integer.valueOf(0), tier.get(Talent.EMPOWERED_STRIKE));
		assertEquals(Integer.valueOf(0), tier.get(Talent.MYSTICAL_CHARGE));
	}

	@Test
	public void subclassReplacementUsesOnlyOldAndNewSubclassTalents() {
		LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
		tier.put(Talent.CLEAVE, 2);
		tier.put(Talent.ENDLESS_RAGE, 1);

		TestSpecializationState.replaceTalentKeys(
				tier,
				TestSpecializationState.subclassTalents(HeroSubClass.BERSERKER),
				TestSpecializationState.subclassTalents(HeroSubClass.BATTLEMAGE));

		assertFalse(tier.containsKey(Talent.ENDLESS_RAGE));
		assertEquals(Integer.valueOf(2), tier.get(Talent.CLEAVE));
		assertTrue(tier.containsKey(Talent.EMPOWERED_STRIKE));
	}

	@Test
	public void armorReplacementUsesOnlyOldAndNewAbilityTalents() {
		LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
		HeroicLeap oldAbility = new HeroicLeap();
		Shockwave newAbility = new Shockwave();
		Talent shared = Talent.HEROIC_ENERGY;
		tier.put(oldAbility.talents()[0], 3);
		tier.put(shared, 1);

		TestSpecializationState.replaceTalentKeys(
				tier,
				TestSpecializationState.armorTalents(oldAbility),
				TestSpecializationState.armorTalents(newAbility));

		assertFalse(tier.containsKey(oldAbility.talents()[0]));
		assertEquals(Integer.valueOf(1), tier.get(shared));
		assertTrue(tier.containsKey(newAbility.talents()[0]));
	}

	@Test
	public void mappedOldTalentsAreRemovedWithTheirSlotMappings() {
		LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
		LinkedHashMap<Talent, Talent> metamorphed = new LinkedHashMap<>();
		LinkedHashMap<Talent, String> sublimated = new LinkedHashMap<>();
		tier.put(Talent.SOUL_EATER, 2);
		tier.put(Talent.MYSTICAL_CHARGE, 1);
		metamorphed.put(Talent.ENDLESS_RAGE, Talent.SOUL_EATER);
		sublimated.put(Talent.DEATHLESS_FURY, Talent.MYSTICAL_CHARGE.name());

		TestSpecializationState.replaceMappedTalentKeys(
				tier, metamorphed, sublimated,
				TestSpecializationState.subclassTalents(HeroSubClass.BERSERKER),
				TestSpecializationState.subclassTalents(HeroSubClass.BATTLEMAGE));

		assertFalse(tier.containsKey(Talent.SOUL_EATER));
		assertFalse(metamorphed.containsKey(Talent.ENDLESS_RAGE));
		assertFalse(sublimated.containsKey(Talent.DEATHLESS_FURY));
		assertTrue(tier.containsKey(Talent.EMPOWERED_STRIKE));
	}

}
