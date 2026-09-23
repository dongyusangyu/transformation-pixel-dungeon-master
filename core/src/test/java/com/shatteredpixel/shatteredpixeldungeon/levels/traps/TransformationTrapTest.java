package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TransformationTrapTest {

	@After
	public void tearDown() {
		Actor.clear();
		Dungeon.level = null;
		Dungeon.hero = null;
	}

	@Test
	public void usesVioletDiamondSpriteAndNormalTrapRules() {
		TransformationTrap trap = new TransformationTrap();

		assertEquals(Trap.VIOLET, trap.color);
		assertEquals(Trap.DIAMOND, trap.shape);
		assertFalse(trap.preservesTerrain());
		assertFalse(trap.triggersOnEntry());
		assertTrue(trap.canBeHidden);
		assertTrue(trap.canBeSearched);
	}

	@Test
	public void affectsEveryHostileAlignmentAndAlliesButNotNeutralCharacters() {
		assertTrue(TransformationTrap.shouldAmok(Char.Alignment.ENEMY));
		assertTrue(TransformationTrap.shouldAmok(Char.Alignment.ENEMY1));
		assertTrue(TransformationTrap.shouldAmok(Char.Alignment.ENEMY2));
		assertTrue(TransformationTrap.shouldAmok(Char.Alignment.ENEMY3));
		assertTrue(TransformationTrap.shouldAmok(Char.Alignment.ENEMY4));
		assertTrue(TransformationTrap.shouldAmok(Char.Alignment.ALLY));
		assertFalse(TransformationTrap.shouldAmok(Char.Alignment.NEUTRAL));
		assertFalse(TransformationTrap.shouldAmok(null));
	}

	@Test
	public void prolongsAmokToAtLeastFiftyTurnsWithoutAddingAnotherFifty() {
		Rat newlyAffected = new TestCombatant();
		TransformationTrap.applyAmok(newlyAffected);
		assertEquals(50f, newlyAffected.buff(Amok.class).cooldown(), 0.001f);

		Rat alreadyAffected = new TestCombatant();
		Buff.prolong(alreadyAffected, Amok.class, 70f);
		TransformationTrap.applyAmok(alreadyAffected);
		assertEquals(70f, alreadyAffected.buff(Amok.class).cooldown(), 0.001f);
	}

	@Test
	public void activationOnlyAffectsCombatantsInsideTheThreeByThreeArea() {
		TestLevel level = new TestLevel();
		level.setSize(5, 5);
		level.heroFOV = new boolean[level.length()];
		level.traps = new SparseArray<>();
		level.plants = new SparseArray<>();
		level.heaps = new SparseArray<>();
		level.blobs = new HashMap<>();
		Dungeon.level = level;

		Rat enemy = ratAt(11, Char.Alignment.ENEMY2);
		Rat ally = ratAt(13, Char.Alignment.ALLY);
		Rat neutral = ratAt(7, Char.Alignment.NEUTRAL);
		Rat distant = ratAt(0, Char.Alignment.ENEMY);
		Actor.add(enemy);
		Actor.add(ally);
		Actor.add(neutral);
		Actor.add(distant);

		new TransformationTrap().set(12).activate();

		assertTrue(enemy.buff(Amok.class) != null);
		assertTrue(ally.buff(Amok.class) != null);
		assertNull(neutral.buff(Amok.class));
		assertNull(distant.buff(Amok.class));
	}

	@Test
	public void replacesACommonTalentInPlaceAndPreservesItsPoints() {
		Hero hero = heroWithTalents();

		assertTrue(TransformationTrap.replaceCommonTalent(
				hero, 1, Talent.HEARTY_MEAL, Talent.SUCKER_PUNCH));

		LinkedHashMap<Talent, Integer> tier = hero.talents.get(0);
		assertEquals(Arrays.asList(Talent.SUCKER_PUNCH, Talent.VETERANS_INTUITION),
				new ArrayList<>(tier.keySet()));
		assertEquals(Integer.valueOf(2), tier.get(Talent.SUCKER_PUNCH));
		assertFalse(tier.containsKey(Talent.HEARTY_MEAL));
		assertEquals(Talent.SUCKER_PUNCH,
				hero.metamorphedTalents.get(Talent.HEARTY_MEAL));
	}

	@Test
	public void rejectsWrongTierDuplicateAndNonCommonTalentReplacement() {
		Hero hero = heroWithTalents();

		assertFalse(TransformationTrap.replaceCommonTalent(
				hero, 1, Talent.HEARTY_MEAL, Talent.ENERGIZING_MEAL));
		assertFalse(TransformationTrap.replaceCommonTalent(
				hero, 1, Talent.HEARTY_MEAL, Talent.VETERANS_INTUITION));
		assertFalse(TransformationTrap.replaceCommonTalent(
				hero, 3, Talent.ENDLESS_RAGE, Talent.HOLD_FAST));
		assertTrue(hero.talents.get(0).containsKey(Talent.HEARTY_MEAL));
	}

	@Test
	public void rejectsInternalPlaceholderTalentsAsTransformationSources() {
		Hero hero = emptyHero();
		hero.talents.get(0).put(Talent.POTENTIAL_1, 2);

		assertFalse(TransformationTrap.replaceCommonTalent(
				hero, 1, Talent.POTENTIAL_1, Talent.SUCKER_PUNCH));
		assertTrue(hero.talents.get(0).containsKey(Talent.POTENTIAL_1));
	}

	@Test
	public void chainedTransformationsKeepTheOriginalTalentMapping() {
		Hero hero = heroWithTalents();
		assertTrue(TransformationTrap.replaceCommonTalent(
				hero, 1, Talent.HEARTY_MEAL, Talent.SUCKER_PUNCH));
		assertTrue(TransformationTrap.replaceCommonTalent(
				hero, 1, Talent.SUCKER_PUNCH, Talent.PROVOKED_ANGER));

		assertEquals(1, hero.metamorphedTalents.size());
		assertEquals(Talent.PROVOKED_ANGER,
				hero.metamorphedTalents.get(Talent.HEARTY_MEAL));
	}

	@Test
	public void candidatePoolUsesOnlyUnownedCommonTalentsFromTheSameTier() {
		Hero hero = heroWithTalents();

		List<Talent> candidates = TransformationTrap.replacementCandidates(
				hero, 1, Talent.HEARTY_MEAL);

		assertTrue(candidates.contains(Talent.SUCKER_PUNCH));
		assertTrue(candidates.contains(Talent.PROVOKED_ANGER));
		assertFalse(candidates.contains(Talent.VETERANS_INTUITION));
		for (Talent candidate : candidates) {
			assertEquals(1, candidate.tier());
			assertTrue(candidate.isCommonTalentType());
			assertFalse(Talent.excludedFromMetamorphosis(candidate));
		}
	}

	@Test
	public void randomTransformationReturnsNullWhenNoLegalReplacementExists() {
		Hero hero = emptyHero();
		LinkedHashMap<Talent, Integer> tier = hero.talents.get(0);
		for (Talent talent : Talent.commonTalentsByTier(1)) {
			if (!Talent.excludedFromMetamorphosis(talent)) {
				tier.put(talent, 0);
			}
		}

		assertNull(TransformationTrap.transformRandomCommonTalent(hero));
		assertEquals(tier.size(), hero.talents.get(0).size());
	}

	private static Hero heroWithTalents() {
		Hero hero = emptyHero();
		hero.talents.get(0).put(Talent.HEARTY_MEAL, 2);
		hero.talents.get(0).put(Talent.VETERANS_INTUITION, 0);
		hero.talents.get(2).put(Talent.ENDLESS_RAGE, 3);
		hero.talents.get(3).put(Talent.BODY_SLAM, 4);
		return hero;
	}

	private static Hero emptyHero() {
		Hero hero = TestHeroFactory.create();
		hero.metamorphedTalents = new LinkedHashMap<>();
		for (int i = 0; i < 4; i++) {
			hero.talents.add(new LinkedHashMap<>());
		}
		return hero;
	}

	private static Rat ratAt(int pos, Char.Alignment alignment) {
		Rat rat = new TestCombatant();
		rat.pos = pos;
		rat.alignment = alignment;
		return rat;
	}

	private static class TestCombatant extends Rat {
		@Override public boolean isImmune(Class effect) { return false; }
		@Override public float resist(Class effect) { return 1f; }
	}

	private static class TestLevel extends Level {
		@Override protected boolean build() { return true; }
		@Override protected void createMobs() {}
		@Override protected void createItems() {}
	}
}
