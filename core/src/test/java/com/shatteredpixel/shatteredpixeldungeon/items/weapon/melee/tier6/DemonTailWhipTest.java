package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DemonTailWhipTest {

	@Test
	public void tierSixStatsAndReachGrowthMatchSpecification() {
		TestableDemonTailWhip weapon = new TestableDemonTailWhip();

		assertEquals(6, DemonTailWhip.TIER);
		assertEquals(20, DemonTailWhip.strengthRequirementForLevel(0));
		assertEquals(6, weapon.min(0));
		assertEquals(30, weapon.max(0));
		assertEquals(13, weapon.min(7));
		assertEquals(72, weapon.max(7));
		assertEquals(0, DemonTailWhip.reachBonusForLevel(6));
		assertEquals(1, DemonTailWhip.reachBonusForLevel(7));
		assertEquals(1, DemonTailWhip.reachBonusForLevel(13));
		assertEquals(2, DemonTailWhip.reachBonusForLevel(14));
		assertEquals(0, DemonTailWhip.reachBonusForLevel(-7));
		assertEquals(3, weapon.baseRange());
	}

	@Test
	public void debuffTriggerAndTierBoundariesMatchSpecification() {
		assertTrue(DemonTailWhip.debuffTriggers(0f));
		assertTrue(DemonTailWhip.debuffTriggers(0.4999f));
		assertFalse(DemonTailWhip.debuffTriggers(0.5f));
		assertEquals(DemonTailWhip.DebuffTier.MINOR,
				DemonTailWhip.preferredTier(0.7499f));
		assertEquals(DemonTailWhip.DebuffTier.MAJOR,
				DemonTailWhip.preferredTier(0.75f));
	}

	@Test
	public void debuffPoolsReuseCorruptionWandWeightsAndFixedDurations() {
		Map<Class<? extends FlavourBuff>, Float> minor =
				DemonTailWhip.minorDebuffWeights();
		Map<Class<? extends FlavourBuff>, Float> major =
				DemonTailWhip.majorDebuffWeights();

		assertEquals(5, minor.size());
		assertEquals(2f, minor.get(Weakness.class), 0f);
		assertEquals(2f, minor.get(Vulnerable.class), 0f);
		assertEquals(1f, minor.get(Cripple.class), 0f);
		assertEquals(1f, minor.get(Blindness.class), 0f);
		assertEquals(1f, minor.get(Terror.class), 0f);
		assertEquals(4, major.size());
		assertEquals(3f, major.get(Amok.class), 0f);
		assertEquals(2f, major.get(Slow.class), 0f);
		assertEquals(2f, major.get(Hex.class), 0f);
		assertEquals(1f, major.get(Paralysis.class), 0f);
		assertEquals(4f,
				DemonTailWhip.durationFor(DemonTailWhip.DebuffTier.MINOR), 0f);
		assertEquals(2f,
				DemonTailWhip.durationFor(DemonTailWhip.DebuffTier.MAJOR), 0f);
	}

	@Test
	public void availableDebuffsExcludeExistingAndImmuneEffects() {
		TestChar target = new TestChar();
		Buff.affect(target, Weakness.class);
		target.immuneTo.add(Vulnerable.class);

		Map<Class<? extends FlavourBuff>, Float> available =
				DemonTailWhip.availableDebuffs(target,
						DemonTailWhip.minorDebuffWeights());

		assertFalse(available.containsKey(Weakness.class));
		assertFalse(available.containsKey(Vulnerable.class));
		assertEquals(3, available.size());
	}

	@Test
	public void fallbackOrderTriesEachTierOnce() {
		assertArrayEquals(new DemonTailWhip.DebuffTier[]{
				DemonTailWhip.DebuffTier.MINOR,
				DemonTailWhip.DebuffTier.MAJOR
		}, DemonTailWhip.poolOrder(DemonTailWhip.DebuffTier.MINOR));
		assertArrayEquals(new DemonTailWhip.DebuffTier[]{
				DemonTailWhip.DebuffTier.MAJOR,
				DemonTailWhip.DebuffTier.MINOR
		}, DemonTailWhip.poolOrder(DemonTailWhip.DebuffTier.MAJOR));
	}

	@Test
	public void failedAttachmentsExhaustPreferredPoolThenFallback() {
		RejectingChar target = new RejectingChar();
		target.rejected.addAll(DemonTailWhip.minorDebuffWeights().keySet());

		assertTrue(DemonTailWhip.tryApplyRandomDebuff(
				target, DemonTailWhip.DebuffTier.MINOR));

		boolean hasMajorDebuff = false;
		for (Class<? extends FlavourBuff> effect
				: DemonTailWhip.majorDebuffWeights().keySet()) {
			hasMajorDebuff |= target.buff(effect) != null;
		}
		assertTrue(hasMajorDebuff);
	}

	@Test
	public void generatorIncludesDemonTailWhipWithMatchingWeights() {
		int weaponIndex = Arrays.asList(Generator.Category.WEP_T6.classes)
				.indexOf(DemonTailWhip.class);

		assertTrue(weaponIndex >= 0);
		assertEquals(Generator.Category.WEP_T6.classes.length,
				Generator.Category.WEP_T6.defaultProbs.length);
		float expectedWeight = Generator.Category.WEP_T6.defaultProbs[0];
		for (float weight : Generator.Category.WEP_T6.defaultProbs) {
			assertEquals(expectedWeight, weight, 0f);
		}
		assertEquals(expectedWeight,
				Generator.Category.WEP_T6.defaultProbs[weaponIndex], 0f);
	}

	@Test
	public void weaponDelegatesLashAbilityAndDefinesBothLocales() throws IOException {
		String compactSource = readMainSource(
				"items/weapon/melee/tier6/DemonTailWhip.java").replaceAll("\\s+", "");
		assertTrue(compactSource.contains(
				"Whip.WhipAbility(hero,target,1f,0,this);"));

		String[] keys = {
				"items.weapon.melee.tier6.demontailwhip.name=",
				"items.weapon.melee.tier6.demontailwhip.ability_name=",
				"items.weapon.melee.tier6.demontailwhip.ability_no_target=",
				"items.weapon.melee.tier6.demontailwhip.typical_ability_desc=",
				"items.weapon.melee.tier6.demontailwhip.ability_desc=",
				"items.weapon.melee.tier6.demontailwhip.desc=",
				"items.weapon.melee.tier6.demontailwhip.stats_desc="
		};
		String english = readCoreFile("src/main/assets/messages/items/items.properties");
		String chinese = readCoreFile("src/main/assets/messages/items/items_zh.properties");
		for (String key : keys) {
			assertEquals("English key count for " + key, 1, countOccurrences(english, key));
			assertEquals("Chinese key count for " + key, 1, countOccurrences(chinese, key));
		}
	}

	private static int countOccurrences(String source, String needle) {
		int count = 0;
		int from = 0;
		while ((from = source.indexOf(needle, from)) >= 0) {
			count++;
			from += needle.length();
		}
		return count;
	}

	private static String readMainSource(String relativePath) throws IOException {
		return readCoreFile("src/main/java/com/shatteredpixel/shatteredpixeldungeon/"
				+ relativePath);
	}

	private static String readCoreFile(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)),
				StandardCharsets.UTF_8);
	}

	private static class TestableDemonTailWhip extends DemonTailWhip {
		int baseRange() {
			return RCH;
		}
	}

	private static class TestChar extends Char {
		private final Set<Class<?>> immuneTo = new HashSet<>();

		@Override
		public boolean isImmune(Class effect) {
			return immuneTo.contains(effect) || super.isImmune(effect);
		}

		@Override
		public float resist(Class effect) {
			return 1f;
		}

		@Override
		protected boolean act() {
			return true;
		}
	}

	private static class RejectingChar extends TestChar {

		final Set<Class<? extends Buff>> rejected = new HashSet<>();

		@Override
		public synchronized boolean add(Buff buff) {
			if (rejected.contains(buff.getClass())) {
				return false;
			}
			return super.add(buff);
		}
	}
}
