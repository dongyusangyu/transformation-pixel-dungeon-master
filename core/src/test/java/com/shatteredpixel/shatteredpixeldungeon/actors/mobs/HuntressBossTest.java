package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Badges.Badge;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.levels.HuntressBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HuntressBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Group;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HuntressBossTest {
	private PathFinderState originalPathFinderState;

	@Before
	public void rememberPathFinderState() {
		originalPathFinderState = PathFinderState.capture();
	}

	@After
	public void restorePathFinderState() {
		if (originalPathFinderState != null) {
			originalPathFinderState.restore();
		}
	}

	@Test
	public void sublimationTypeUsesIndependentHuntressMarker() {
		assertEquals("HUNTRESS", HuntressBoss.SUBLIMATION_TYPE);
	}

	@Test
	public void huntressSlainAwardsItsDedicatedHeroBossBadgeInsteadOfThirdBossBadges()
			throws ReflectiveOperationException {
		int previousDepth = Dungeon.depth;
		String previousCustomSeed = Dungeon.customSeedText;
		Field globalBadges = Badges.class.getDeclaredField("global");
		globalBadges.setAccessible(true);
		Object previousGlobalBadges = globalBadges.get(null);
		Bundle previousLocalBadges = new Bundle();
		Badges.saveLocal(previousLocalBadges);
		try {
			Dungeon.depth = 15;
			Dungeon.customSeedText = "huntress-badge-test";
			Badges.loadLocal(new Bundle());
			globalBadges.set(null, new HashSet<Badge>());

			Badges.validateHeroBossSlain();

			Bundle savedBadges = new Bundle();
			Badges.saveLocal(savedBadges);
			Set<Badge> awarded = Badges.restore(savedBadges);
			assertTrue(awarded.contains(Badge.HEROBOSS_SLAIN_3));
			assertEquals(Badges.gold + 26, Badge.HEROBOSS_SLAIN_3.image);
			assertFalse(awarded.contains(Badge.BOSS_SLAIN_3));
			assertFalse(awarded.contains(Badge.BOSS_CHALLENGE_3));
		} finally {
			Badges.loadLocal(previousLocalBadges);
			globalBadges.set(null, previousGlobalBadges);
			Dungeon.customSeedText = previousCustomSeed;
			Dungeon.depth = previousDepth;
		}
	}

	@Test
	public void huntressHeroBossBadgeHasLocalizedNameAndDescription() throws IOException {
		String key = "badges$badge.heroboss_slain_3";
		Properties english = loadMessages("misc", "misc.properties");
		assertEquals("End of the Hunt", english.getProperty(key + ".title"));
		assertEquals("Defeat Huntress?", english.getProperty(key + ".desc"));

		Properties chinese = loadMessages("misc", "misc_zh.properties");
		assertEquals("猎风终结", chinese.getProperty(key + ".title"));
		assertEquals("击败女猎手？", chinese.getProperty(key + ".desc"));
	}

	@Test
	public void huntRhythmMessagesExistInEnglishAndChinese() throws IOException {
		String prefix = "actors.mobs.huntressboss.";
		String[] keys = {"gale_recover", "contact_warning", "plant_marked",
				"nature_hunt", "lost_track"};
		for (String fileName : new String[]{"actors.properties", "actors_zh.properties"}) {
			Properties messages = loadActorMessages(fileName);
			for (String key : keys) {
				String value = messages.getProperty(prefix + key);
				assertNotNull(fileName + " missing " + key, value);
				assertFalse(fileName + " has empty " + key, value.trim().isEmpty());
			}
		}
	}

	@Test
	public void huntressDescriptionsExplainTheCurrentRhythmInsteadOfRetiredRules()
			throws IOException {
		String prefix = "actors.mobs.huntressboss.desc";
		String english = loadActorMessages("actors.properties").getProperty(prefix);
		assertTrue(english.contains("five effective ordinary actions"));
		assertTrue(english.contains("continued contact"));
		assertTrue(english.contains("five natural-hunt actions"));
		assertTrue(english.contains("moves at triple speed"));
		assertFalse(english.contains("moves at double speed"));
		assertTrue(english.contains("25% chance"));
		assertFalse(english.contains("75%%"));
		assertFalse(english.contains("25%%"));
		assertFalse(english.contains("every third shot"));
		assertFalse(english.contains("random shove"));

		String chinese = loadActorMessages("actors_zh.properties").getProperty(prefix);
		assertTrue(chinese.contains("五个有效普通行动"));
		assertTrue(chinese.contains("确定性地翻越脱离"));
		assertTrue(chinese.contains("五个自然狩猎行动"));
		assertTrue(chinese.contains("移动速度提升至三倍"));
		assertFalse(chinese.contains("移动速度翻倍"));
		assertTrue(chinese.contains("25%概率"));
		assertFalse(chinese.contains("75%%"));
		assertFalse(chinese.contains("25%%"));
		assertTrue(chinese.contains("与“自然之怒”相同的五种有害植物效果之一"));
		assertFalse(chinese.contains("与自然之力相同"));
		assertFalse(chinese.contains("三次普通射击"));
		assertFalse(chinese.contains("随机击退"));
	}

	@Test
	public void strongerBossDescriptionDocumentsAllHuntressChallengeTiers()
			throws IOException {
		String key = "challenges.stronger_bosses_desc";
		String english = loadMessages("misc", "misc.properties").getProperty(key);
		assertTrue(english.contains("_Huntress?:_"));
		assertTrue(english.contains("range increases from 6 to 8"));
		assertTrue(english.contains("damage +20%"));
		assertTrue(english.contains("￡"));
		assertTrue(english.contains("every 75 health lost"));
		assertTrue(english.contains("removes her gale recovery action"));
		assertTrue(english.contains("￥"));
		assertTrue(english.contains("range increases to 10"));
		assertTrue(english.contains("Plant-hunt movement speed increases to 5x"));

		String chinese = loadMessages("misc", "misc_zh.properties").getProperty(key);
		assertTrue(chinese.contains("_女猎手？：_"));
		assertTrue(chinese.contains("射击距离由6格提升至8格"));
		assertTrue(chinese.contains("各类伤害+20%"));
		assertTrue(chinese.contains("￡每损失75点生命就会召唤1只飞鹰￡"));
		assertTrue(chinese.contains("￡贯风箭不再需要收弓行动￡"));
		assertTrue(chinese.contains("￥射击距离提升至10格￥"));
		assertTrue(chinese.contains("￥植物争夺时的移动速度提升至5倍￥"));
	}

	@Test
	public void incomingDamageIsCappedAtThirty() {
		assertEquals(0, HuntressBoss.cappedIncomingDamage(0));
		assertEquals(30, HuntressBoss.cappedIncomingDamage(30));
		assertEquals(30, HuntressBoss.cappedIncomingDamage(31));
		assertEquals(30, HuntressBoss.cappedIncomingDamage(100));
	}

	@Test
	public void challengeTiersAdjustShotRangeDamageAndNatureHuntSpeed() {
		assertEquals(6, HuntressBoss.challengeNormalShotRange(false, false));
		assertEquals(6, HuntressBoss.challengeNormalShotRange(false, true));
		assertEquals(8, HuntressBoss.challengeNormalShotRange(true, false));
		assertEquals(10, HuntressBoss.challengeNormalShotRange(true, true));

		for (int[] pair : new int[][]{
				{6, 7}, {14, 17}, {10, 12}, {16, 19},
				{20, 24}, {28, 34}, {12, 14}}) {
			assertEquals(pair[0], HuntressBoss.challengeAdjustedDamage(pair[0], false));
			assertEquals(pair[1], HuntressBoss.challengeAdjustedDamage(pair[0], true));
		}

		assertEquals(3f, HuntressBoss.natureHuntSpeed(
				1f, true, false, false), 0f);
		assertEquals(3f, HuntressBoss.natureHuntSpeed(
				1f, true, false, true), 0f);
		assertEquals(3f, HuntressBoss.natureHuntSpeed(
				1f, true, true, false), 0f);
		assertEquals(5f, HuntressBoss.natureHuntSpeed(
				1f, true, true, true), 0f);
		assertEquals(1f, HuntressBoss.natureHuntSpeed(
				1f, false, true, true), 0f);
	}

	@Test
	public void liveChallengeMaskDrivesShotRangeAndHarshGaleRecovery() {
		int previousChallenges = Dungeon.challenges;
		Level previousLevel = Dungeon.level;
		try {
			TestHuntress boss = new TestHuntress();

			Dungeon.challenges = 0;
			assertEquals(6, boss.normalShotRangeForTest());
			Dungeon.challenges = Challenges.EXTREME_ENVIRONMENT;
			assertEquals(6, boss.normalShotRangeForTest());
			Dungeon.challenges = Challenges.STRONGER_BOSSES;
			assertEquals(8, boss.normalShotRangeForTest());
			Dungeon.challenges = Challenges.STRONGER_BOSSES
					| Challenges.EXTREME_ENVIRONMENT;
			assertEquals(10, boss.normalShotRangeForTest());

			Dungeon.level = testLevel(9, 5);
			boss.pos = 20;
			boss.prepareAimedGale(24);
			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());
			assertEquals(HuntressBoss.GALE_INTERVAL, boss.galeTurnsRemaining());
			assertEquals(0, boss.galeRecoveryTurns());
			assertEquals(0, boss.galeRecoveryAnnouncements);
		} finally {
			Dungeon.challenges = previousChallenges;
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void harshStrongerBossSummonsOneHawkPerSeventyFiveHealthLostAndPersistsProgress() {
		int previousChallenges = Dungeon.challenges;
		try {
			Dungeon.challenges = Challenges.STRONGER_BOSSES
					| Challenges.HARSH_ENVIRONMENT;
			ChallengeHawkHuntress boss = new ChallengeHawkHuntress();
			for (int i = 0; i < 5; i++) {
				boss.takeThirtyDamage();
			}
			assertEquals(250, boss.HP);
			assertEquals(2, boss.challengeHawkSpawnRequests);

			Bundle saved = new Bundle();
			boss.storeInBundle(saved);
			ChallengeHawkHuntress restored = new ChallengeHawkHuntress();
			restored.restoreFromBundle(saved);
			for (int i = 0; i < 3; i++) {
				restored.takeThirtyDamage();
			}
			assertEquals(160, restored.HP);
			assertEquals(1, restored.challengeHawkSpawnRequests);

			Dungeon.challenges = Challenges.HARSH_ENVIRONMENT;
			ChallengeHawkHuntress noStrongerBoss = new ChallengeHawkHuntress();
			for (int i = 0; i < 3; i++) {
				noStrongerBoss.takeThirtyDamage();
			}
			assertEquals(0, noStrongerBoss.challengeHawkSpawnRequests);
		} finally {
			Dungeon.challenges = previousChallenges;
		}
	}

	@Test
	public void galeCountdownAdvancesOnlyAfterOrdinaryEffectiveActions() {
		assertEquals(5, HuntressBoss.GALE_INTERVAL);
		assertEquals(4, HuntressBoss.advanceGaleCountdown(5, true));
		assertEquals(1, HuntressBoss.advanceGaleCountdown(2, true));
		assertEquals(0, HuntressBoss.advanceGaleCountdown(1, true));
		assertEquals(0, HuntressBoss.advanceGaleCountdown(0, true));
		assertEquals(3, HuntressBoss.advanceGaleCountdown(3, false));

		assertTrue(HuntressBoss.shouldAimGale(HuntressBoss.Phase.SNIPER, 0, true));
		assertFalse(HuntressBoss.shouldAimGale(HuntressBoss.Phase.SNIPER, 1, true));
		assertFalse(HuntressBoss.shouldAimGale(HuntressBoss.Phase.SNIPER, 0, false));
		assertFalse(HuntressBoss.shouldAimGale(HuntressBoss.Phase.WARDEN, 0, true));
	}

	@Test
	public void galeEndpointUsesTwoDimensionalClampingWithoutRowWrapping() {
		assertEquals(14, HuntressBoss.galeEndpointForVector(12, 10, 0, 5, 5));
		assertEquals(10, HuntressBoss.galeEndpointForVector(12, -10, 0, 5, 5));
		assertEquals(22, HuntressBoss.galeEndpointForVector(12, 0, 10, 5, 5));
		assertEquals(2, HuntressBoss.galeEndpointForVector(12, 0, -10, 5, 5));
		assertEquals(9, HuntressBoss.galeEndpointForVector(9, 1, 0, 5, 5));
		assertEquals(9, HuntressBoss.galeEndpointForVector(12, 10, -1, 5, 5));
		assertEquals(-1, HuntressBoss.galeEndpointForVector(12, 0, 0, 5, 5));
		assertEquals(-1, HuntressBoss.galeEndpointForVector(-1, 1, 0, 5, 5));
		assertEquals(-1, HuntressBoss.galeEndpointForVector(25, 1, 0, 5, 5));
		assertEquals(-1, HuntressBoss.galeEndpointForVector(0, 1, 0, 0, 5));
	}

	@Test
	public void galeTargetEligibilityRequiresEveryCombatCondition() {
		assertTrue(HuntressBoss.isGaleTargetEligible(true, true, true, false));
		assertFalse(HuntressBoss.isGaleTargetEligible(false, true, true, false));
		assertFalse(HuntressBoss.isGaleTargetEligible(true, false, true, false));
		assertFalse(HuntressBoss.isGaleTargetEligible(true, true, false, false));
		assertFalse(HuntressBoss.isGaleTargetEligible(true, true, true, true));
	}

	@Test
	public void galeTargetPickerUsesRequestedIndexAndHandlesNoTargets() {
		TestTarget first = new TestTarget();
		TestTarget second = new TestTarget();
		ArrayList<Char> targets = new ArrayList<>(Arrays.asList(first, second));

		assertSame(first, HuntressBoss.pickGaleTarget(targets, 0));
		assertSame(second, HuntressBoss.pickGaleTarget(targets, 1));
		assertNull(HuntressBoss.pickGaleTarget(Collections.<Char>emptyList(), 0));
	}

	@Test
	public void galeCountdownPersistenceRoundTripsAndLegacyBundlesDefaultToInterval() {
		TestHuntress boss = new TestHuntress();
		boss.setGaleTurnsRemainingForTest(2);
		Bundle saved = new Bundle();
		boss.storeInBundle(saved);
		TestHuntress restored = new TestHuntress();
		restored.restoreFromBundle(saved);

		assertEquals(2, HuntressBoss.restoredGaleTurns(saved));
		assertEquals(2, restored.galeTurnsRemaining());
		assertEquals(5, HuntressBoss.restoredGaleTurns(new Bundle()));
		Bundle legacy = new Bundle();
		legacy.put("normal_shots", 3);
		assertEquals(5, HuntressBoss.restoredGaleTurns(legacy));
		TestHuntress restoredLegacy = new TestHuntress();
		restoredLegacy.restoreFromBundle(legacy);
		assertEquals(HuntressBoss.GALE_INTERVAL, restoredLegacy.galeTurnsRemaining());
		assertEquals(2, boss.galeTurnsRemaining());
	}

	@Test
	public void sniperRhythmStateRoundTripsThroughAFreshBoss() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = testLevel(9, 5);
			TestHuntress boss = new TestHuntress();
			boss.pos = 20;
			boss.setRhythmStateForTest(
					HuntressBoss.CombatStep.MOVE,
					HuntressBoss.GaleState.AIMED,
					HuntressBoss.PlantHuntState.GRACE,
					20, 24, 1, true, 77, 3, -1, 0,
					HuntressBoss.PLANT_HUNT_GRACE);
			Bundle saved = new Bundle();
			boss.storeInBundle(saved);

			TestHuntress restored = new TestHuntress();
			restored.restoreFromBundle(saved);

			assertEquals(HuntressBoss.CombatStep.MOVE, restored.combatStep());
			assertEquals(HuntressBoss.GaleState.AIMED, restored.galeState());
			assertEquals(HuntressBoss.PlantHuntState.GRACE, restored.plantHuntState());
			assertEquals(20, restored.galeAimOrigin());
			assertEquals(24, restored.galeAimTarget());
			assertEquals(4, restored.galeAimDx());
			assertEquals(0, restored.galeAimDy());
			assertEquals(0, restored.galeRecoveryTurns());
			assertTrue(restored.contactArmed());
			assertEquals(77, restored.contactTargetId());
			assertEquals(3, restored.escapeCooldown());
			assertEquals(-1, restored.markedPlantCell());
			assertEquals(0, restored.plantHuntTurns());
			assertEquals(HuntressBoss.PLANT_HUNT_GRACE, restored.plantGraceTurns());

			TestHuntress recovering = new TestHuntress();
			recovering.pos = 20;
			recovering.setRhythmStateForTest(
					HuntressBoss.CombatStep.SHOOT,
					HuntressBoss.GaleState.RECOVERING,
					HuntressBoss.PlantHuntState.GRACE,
					-1, -1, HuntressBoss.GALE_RECOVERY_TURNS,
					false, -1, 0, -1, 0, HuntressBoss.PLANT_HUNT_GRACE);
			Bundle recoveringSaved = new Bundle();
			recovering.storeInBundle(recoveringSaved);
			TestHuntress recoveringRestored = new TestHuntress();
			recoveringRestored.restoreFromBundle(recoveringSaved);
			assertEquals(HuntressBoss.GaleState.RECOVERING,
					recoveringRestored.galeState());
			assertEquals(HuntressBoss.GALE_RECOVERY_TURNS,
					recoveringRestored.galeRecoveryTurns());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void wardenPlantHuntStateRoundTripsThroughAFreshBoss() {
		Level previousLevel = Dungeon.level;
		try {
			RhythmLevel level = rhythmLevel(9, 5);
			Dungeon.level = level;
			putPlant(level, 24, new Firebloom());
			TestHuntress boss = new TestHuntress();
			boss.pos = 20;
			assertTrue(boss.enterWardenPhase());
			boss.setRhythmStateForTest(
					HuntressBoss.CombatStep.MOVE,
					HuntressBoss.GaleState.HUNTING,
					HuntressBoss.PlantHuntState.SELECTED,
					-1, -1, 0, true, 77, 2, 24, 4, 0);
			Bundle saved = new Bundle();
			boss.storeInBundle(saved);

			TestHuntress restored = new TestHuntress();
			restored.restoreFromBundle(saved);

			assertEquals(HuntressBoss.Phase.WARDEN, restored.phase());
			assertEquals(HuntressBoss.CombatStep.MOVE, restored.combatStep());
			assertEquals(HuntressBoss.GaleState.HUNTING, restored.galeState());
			assertEquals(HuntressBoss.PlantHuntState.SELECTED,
					restored.plantHuntState());
			assertEquals(24, restored.markedPlantCell());
			assertEquals(4, restored.plantHuntTurns());
			assertEquals(0, restored.plantGraceTurns());
			assertEquals(2, restored.escapeCooldown());
			assertTrue(restored.contactArmed());
			assertEquals(77, restored.contactTargetId());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void legalWardenActiveBoonRoundTripsAsBoonActivePlantState() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = testLevel(9, 5);
			TestHuntress boss = new TestHuntress();
			boss.pos = 20;
			assertTrue(boss.enterWardenPhase());
			boss.setActiveBoonForTest(HuntressBoss.WardenBoon.FIREBLOOM, 5);
			Bundle saved = new Bundle();
			boss.storeInBundle(saved);

			TestHuntress restored = new TestHuntress();
			restored.restoreFromBundle(saved);

			assertEquals(HuntressBoss.WardenBoon.FIREBLOOM, restored.activeBoon());
			assertEquals(HuntressBoss.PlantHuntState.BOON_ACTIVE,
					restored.plantHuntState());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void emptyLegacyBundleUsesSafeRhythmDefaults() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = null;
			TestHuntress restored = new TestHuntress();

			restored.restoreFromBundle(new Bundle());

			assertEquals(HuntressBoss.Phase.SNIPER, restored.phase());
			assertEquals(HuntressBoss.CombatStep.SHOOT, restored.combatStep());
			assertEquals(HuntressBoss.GaleState.HUNTING, restored.galeState());
			assertEquals(HuntressBoss.PlantHuntState.GRACE,
					restored.plantHuntState());
			assertFalse(restored.contactArmed());
			assertEquals(0, restored.escapeCooldown());
			assertEquals(-1, restored.galeAimOrigin());
			assertEquals(-1, restored.galeAimTarget());
			assertEquals(-1, restored.markedPlantCell());
			assertEquals(0, restored.plantHuntTurns());
			assertEquals(HuntressBoss.PLANT_HUNT_GRACE, restored.plantGraceTurns());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void legacyGaleTargetMigratesOnlyForAValidSniperRay() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = testLevel(9, 5);
			Bundle valid = new Bundle();
			valid.put("pos", 20);
			valid.put("gale_target", 24);
			TestHuntress migrated = new TestHuntress();
			migrated.restoreFromBundle(valid);
			assertEquals(HuntressBoss.GaleState.AIMED, migrated.galeState());
			assertEquals(20, migrated.galeAimOrigin());
			assertEquals(24, migrated.galeAimTarget());

			Bundle zeroVector = new Bundle();
			zeroVector.put("pos", 20);
			zeroVector.put("gale_target", 20);
			TestHuntress rejectedZeroVector = new TestHuntress();
			rejectedZeroVector.restoreFromBundle(zeroVector);
			assertEquals(HuntressBoss.GaleState.READY, rejectedZeroVector.galeState());
			assertEquals(-1, rejectedZeroVector.galeAimTarget());

			Bundle outOfBounds = new Bundle();
			outOfBounds.put("pos", 20);
			outOfBounds.put("gale_target", 99);
			TestHuntress rejectedOutOfBounds = new TestHuntress();
			rejectedOutOfBounds.restoreFromBundle(outOfBounds);
			assertEquals(HuntressBoss.GaleState.READY, rejectedOutOfBounds.galeState());
			assertEquals(-1, rejectedOutOfBounds.galeAimTarget());

			Dungeon.level = null;
			TestHuntress rejectedWithoutLevel = new TestHuntress();
			rejectedWithoutLevel.restoreFromBundle(valid);
			assertEquals(HuntressBoss.GaleState.READY, rejectedWithoutLevel.galeState());
			assertEquals(-1, rejectedWithoutLevel.galeAimTarget());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void restoreClampsCorruptRhythmValuesAndNormalizesThePhase() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = testLevel(9, 5);
			Bundle corruptSniper = new Bundle();
			corruptSniper.put("pos", 20);
			corruptSniper.put("combat_step", "BROKEN");
			corruptSniper.put("gale_state", HuntressBoss.GaleState.RECOVERING);
			corruptSniper.put("gale_recovery_turns", 99);
			corruptSniper.put("plant_hunt_state", "BROKEN");
			corruptSniper.put("escape_cooldown", 99);
			corruptSniper.put("marked_plant_cell", 99);
			corruptSniper.put("plant_hunt_turns", 99);
			corruptSniper.put("plant_grace_turns", -7);
			TestHuntress sniper = new TestHuntress();
			sniper.restoreFromBundle(corruptSniper);

			assertEquals(HuntressBoss.CombatStep.SHOOT, sniper.combatStep());
			assertEquals(HuntressBoss.GaleState.RECOVERING, sniper.galeState());
			assertEquals(4, sniper.escapeCooldown());
			assertEquals(HuntressBoss.PlantHuntState.GRACE, sniper.plantHuntState());
			assertEquals(-1, sniper.markedPlantCell());
			assertEquals(0, sniper.plantHuntTurns());
			assertEquals(HuntressBoss.PLANT_HUNT_GRACE, sniper.plantGraceTurns());

			Bundle warden = new Bundle();
			warden.put("pos", 20);
			warden.put("phase", HuntressBoss.Phase.WARDEN);
			warden.put("gale_state", HuntressBoss.GaleState.AIMED);
			warden.put("gale_aim_origin", 20);
			warden.put("gale_aim_target", 24);
			warden.put("contact_armed", true);
			warden.put("contact_target_id", 31);
			warden.put("plant_hunt_state", HuntressBoss.PlantHuntState.SELECTED);
			warden.put("marked_plant_cell", 24);
			warden.put("plant_hunt_turns", 99);
			warden.put("plant_grace_turns", 99);
			TestHuntress restoredWarden = new TestHuntress();
			restoredWarden.restoreFromBundle(warden);

			assertEquals(HuntressBoss.GaleState.HUNTING, restoredWarden.galeState());
			assertEquals(-1, restoredWarden.galeAimOrigin());
			assertEquals(-1, restoredWarden.galeAimTarget());
			assertTrue(restoredWarden.contactArmed());
			assertEquals(31, restoredWarden.contactTargetId());
			assertEquals(HuntressBoss.PlantHuntState.LOST_TRACK,
					restoredWarden.plantHuntState());
			assertEquals(-1, restoredWarden.markedPlantCell());
			assertEquals(0, restoredWarden.plantHuntTurns());
			assertEquals(0, restoredWarden.plantGraceTurns());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void restoreKeepsSelectedOnlyForARealReachablePlant() {
		Level previousLevel = Dungeon.level;
		try {
			RhythmLevel level = rhythmLevel(11, 7);
			Dungeon.level = level;
			int bossCell = 34;
			int plantCell = 18;
			Bundle selected = selectedPlantBundle(bossCell, plantCell);

			TestHuntress missing = new TestHuntress();
			missing.restoreFromBundle(selected);
			assertRestoredPlantLost(missing);

			putPlant(level, plantCell, new Firebloom());
			Arrays.fill(level.passable, false);
			level.passable[bossCell] = true;
			level.passable[plantCell] = true;
			TestHuntress unreachable = new TestHuntress();
			unreachable.restoreFromBundle(selected);
			assertRestoredPlantLost(unreachable);

			level.buildFlagMaps();
			Arrays.fill(level.map, Terrain.EMPTY);
			level.buildFlagMaps();
			TestHuntress reachable = new TestHuntress();
			reachable.restoreFromBundle(selected);
			assertEquals(HuntressBoss.PlantHuntState.SELECTED,
					reachable.plantHuntState());
			assertEquals(plantCell, reachable.markedPlantCell());
			assertEquals(4, reachable.plantHuntTurns());
			assertEquals(3f, reachable.speed(), 0f);
			assertEquals(0.75f, reachable.attackDelay(), 0f);
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	private static Bundle selectedPlantBundle(int bossCell, int plantCell) {
		Bundle bundle = new Bundle();
		bundle.put("pos", bossCell);
		bundle.put("phase", HuntressBoss.Phase.WARDEN);
		bundle.put("plant_hunt_state", HuntressBoss.PlantHuntState.SELECTED);
		bundle.put("marked_plant_cell", plantCell);
		bundle.put("plant_hunt_turns", 4);
		bundle.put("plant_grace_turns", 0);
		return bundle;
	}

	private static void assertRestoredPlantLost(TestHuntress boss) {
		assertEquals(HuntressBoss.PlantHuntState.LOST_TRACK, boss.plantHuntState());
		assertEquals(-1, boss.markedPlantCell());
		assertEquals(0, boss.plantHuntTurns());
		assertEquals(1f, boss.speed(), 0f);
		assertEquals(1f, boss.attackDelay(), 0f);
	}

	@Test
	public void enteringWardenPhaseResetsAllCrossPhaseRhythmState() {
		TestHuntress boss = new TestHuntress();
		boss.setRhythmStateForTest(
				HuntressBoss.CombatStep.MOVE,
				HuntressBoss.GaleState.AIMED,
				HuntressBoss.PlantHuntState.SELECTED,
				20, 24, 1, true, 77, 3, 24, 4, 0);

		assertTrue(boss.enterWardenPhase());

		assertEquals(HuntressBoss.CombatStep.SHOOT, boss.combatStep());
		assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());
		assertEquals(-1, boss.galeAimOrigin());
		assertEquals(-1, boss.galeAimTarget());
		assertFalse(boss.contactArmed());
		assertEquals(HuntressBoss.PlantHuntState.GRACE, boss.plantHuntState());
		assertEquals(-1, boss.markedPlantCell());
		assertEquals(0, boss.plantHuntTurns());
		assertEquals(HuntressBoss.PLANT_HUNT_GRACE, boss.plantGraceTurns());
	}

	@Test
	public void halfHealthTransitionRequestsExactlyOneWardenHawkWithOrWithoutFirstHawk() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			WardenTransitionHuntress withFirstHawk = new WardenTransitionHuntress();
			withFirstHawk.pos = 20;
			HuntressBoss.DistractingHawk firstHawk = new HuntressBoss.DistractingHawk();
			firstHawk.pos = 21;
			registerActor(withFirstHawk, registeredActors);
			registerActor(firstHawk, registeredActors);
			Dungeon.level.mobs.add(firstHawk);

			withFirstHawk.crossHalfHealthWithRealDamageForTest();

			assertEquals(HuntressBoss.Phase.WARDEN, withFirstHawk.phase());
			assertEquals(1, withFirstHawk.wardenHawkSpawnRequests);
			withFirstHawk.damageAgainForTest();
			assertEquals(1, withFirstHawk.wardenHawkSpawnRequests);
			Actor.remove(firstHawk);
			registeredActors.remove(firstHawk);
			Dungeon.level.mobs.remove(firstHawk);

			WardenTransitionHuntress withoutFirstHawk = new WardenTransitionHuntress();
			withoutFirstHawk.pos = 22;
			registerActor(withoutFirstHawk, registeredActors);

			withoutFirstHawk.crossHalfHealthWithRealDamageForTest();

			assertEquals(HuntressBoss.Phase.WARDEN, withoutFirstHawk.phase());
			assertEquals(1, withoutFirstHawk.wardenHawkSpawnRequests);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void firstAdjacentActionMeleesAndTheSecondEscapesWithoutAttacking() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = contactBossAt(20);
			TestTarget target = hostileTargetAt(21);
			boss.bossOnlyEscapeSucceeds = true;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);

			assertTrue(boss.actForTest());
			assertTrue(boss.contactArmed());
			assertEquals(1, boss.contactWarningAnnouncements);
			assertEquals(1, boss.attackedTargets.size());

			assertTrue(boss.actForTest());
			assertEquals(1, boss.contactWarningAnnouncements);
			assertEquals(1, boss.bossOnlyEscapes);
			assertEquals(1, boss.attackedTargets.size());
			assertEquals(HuntressBoss.ESCAPE_COOLDOWN, boss.escapeCooldown());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void duePlantSpecialActionsDoNotArmContactBeforeARealMelee() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			for (HuntressBoss.PlantHuntState dueState
					: new HuntressBoss.PlantHuntState[]{
					HuntressBoss.PlantHuntState.LOST_TRACK,
					HuntressBoss.PlantHuntState.GRACE,
					HuntressBoss.PlantHuntState.SELECTED}) {
				RhythmLevel level = rhythmLevel(11, 7);
				Dungeon.level = level;
				Dungeon.hero = null;
				int plantCell = 18;
				putPlant(level, plantCell, new Firebloom());
				TestHuntress boss = contactBossAt(34);
				assertTrue(boss.enterWardenPhase());
				TestTarget target = hostileTargetAt(35);
				registerActor(boss, registeredActors);
				registerActor(target, registeredActors);
				boss.followTargetForTest(target);
				boss.bossOnlyEscapeSucceeds = true;
				boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
						HuntressBoss.GaleState.HUNTING, dueState,
						-1, -1, 0, false, -1, 0,
						dueState == HuntressBoss.PlantHuntState.SELECTED ? plantCell : -1,
						0, 0);

				assertTrue(boss.actForTest());
				assertFalse(boss.contactArmed());
				assertEquals(0, boss.contactWarningAnnouncements);
				assertTrue(boss.attackedTargets.isEmpty());

				assertTrue(boss.actForTest());
				assertTrue(boss.contactArmed());
				assertEquals(1, boss.contactWarningAnnouncements);
				assertEquals(1, boss.attackedTargets.size());
				assertEquals(0, boss.bossOnlyEscapes);

				assertTrue(boss.actForTest());
				assertEquals(1, boss.bossOnlyEscapes);
				assertEquals(1, boss.attackedTargets.size());
				removeRegisteredActors(registeredActors);
			}
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void adjacentTargetPreemptsAFarEnemyAndANewGaleAim() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = contactBossAt(20);
			boss.setGaleTurnsRemainingForTest(0);
			boss.bossOnlyEscapeSucceeds = true;
			TestTarget farEnemy = hostileTargetAt(24);
			TestTarget adjacentEnemy = hostileTargetAt(21);
			registerActor(boss, registeredActors);
			registerActor(farEnemy, registeredActors);
			registerActor(adjacentEnemy, registeredActors);
			boss.followTargetForTest(farEnemy);

			assertTrue(boss.actForTest());

			assertTrue(boss.contactArmed());
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());
			assertEquals(0, boss.galeAims);
			assertEquals(1, boss.attackedTargets.size());
			assertSame(adjacentEnemy, boss.attackedTargets.get(0));
			assertSame(adjacentEnemy, boss.enemyForTest());

			assertTrue(boss.actForTest());
			assertEquals(1, boss.bossOnlyEscapes);
			assertSame(adjacentEnemy, boss.lastEscapeTarget);
			assertEquals(1, boss.attackedTargets.size());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void fadeleafEscapeUsesRoundTripAndLegacyBundlesDefaultToZero() {
		TestHuntress boss = new TestHuntress();
		boss.setFadeleafEscapesUsedForTest(2);
		Bundle saved = new Bundle();
		boss.storeInBundle(saved);
		TestHuntress restored = new TestHuntress();
		restored.restoreFromBundle(saved);

		assertEquals(2, HuntressBoss.restoredFadeleafEscapes(saved));
		assertEquals(2, restored.fadeleafEscapesUsed());
		assertEquals(0, HuntressBoss.restoredFadeleafEscapes(new Bundle()));
		Bundle negative = new Bundle();
		negative.put("fadeleaf_escapes_used", -1);
		assertEquals(0, HuntressBoss.restoredFadeleafEscapes(negative));
		Bundle excessive = new Bundle();
		excessive.put("fadeleaf_escapes_used", 99);
		assertEquals(3, HuntressBoss.restoredFadeleafEscapes(excessive));
		assertEquals(2, boss.fadeleafEscapesUsed());
	}

	@Test
	public void lockedGaleVectorSurvivesBundleRoundTripAndUsesTheCurrentOrigin() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		NoResistanceTarget lockedRayTarget = null;
		try {
			Dungeon.level = testLevel(25, 7);
			Dungeon.hero = null;
			TestHuntress savedBoss = new TestHuntress();
			savedBoss.pos = 76;
			savedBoss.alignment = Char.Alignment.ENEMY1;
			savedBoss.prepareAimedGale(80);
			Bundle saved = new Bundle();
			savedBoss.storeInBundle(saved);

			TestHuntress restored = new TestHuntress();
			restored.restoreFromBundle(saved);
			restored.sprite = new HeadlessCharSprite();
			assertEquals(HuntressBoss.GaleState.AIMED, restored.galeState());
			assertEquals(76, restored.galeAimOrigin());
			assertEquals(80, restored.galeAimTarget());
			assertEquals(4, restored.galeAimDx());
			assertEquals(0, restored.galeAimDy());
			restored.pos = 101;
			registerActor(restored, registeredActors);
			lockedRayTarget = new NoResistanceTarget();
			lockedRayTarget.pos = 105;
			lockedRayTarget.alignment = Char.Alignment.ENEMY2;
			registerActor(lockedRayTarget, registeredActors);

			assertTrue(restored.actForTest());
			assertTrue(restored.attackedTargets.contains(lockedRayTarget));
			assertEquals(HuntressBoss.GaleState.RECOVERING, restored.galeState());
			assertEquals(1, restored.galeRecoveryTurns());
		} finally {
			if (lockedRayTarget != null && lockedRayTarget.buff(Cripple.class) != null) {
				lockedRayTarget.buff(Cripple.class).detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void changingAdjacentTargetsDoesNotResetTheArmedEscape() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = contactBossAt(20);
			TestTarget first = hostileTargetAt(21);
			TestTarget second = hostileTargetAt(29);
			boss.bossOnlyEscapeSucceeds = true;
			registerActor(boss, registeredActors);
			registerActor(first, registeredActors);
			registerActor(second, registeredActors);
			boss.followTargetForTest(first);

			assertTrue(boss.actForTest());
			first.pos = 40;
			second.pos = 21;
			boss.followTargetForTest(second);
			assertTrue(boss.actForTest());

			assertEquals(1, boss.attackedTargets.size());
			assertEquals(1, boss.bossOnlyEscapes);
			assertSame(second, boss.lastEscapeTarget);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void leavingContactClearsTheWarningAndAFailedEscapeFallsBackToMelee() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = contactBossAt(20);
			TestTarget target = hostileTargetAt(21);
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);

			assertTrue(boss.actForTest());
			target.pos = 23;
			boss.followTargetForTest(target);
			assertTrue(boss.actForTest());
			assertFalse(boss.contactArmed());

			target.pos = 21;
			boss.followTargetForTest(target);
			assertTrue(boss.actForTest());
			assertTrue(boss.actForTest());

			assertEquals(1, boss.bossOnlyEscapes);
			assertEquals(4, boss.attackedTargets.size());
			assertTrue(boss.contactArmed());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void successfulEscapeCooldownTicksOnFourEffectiveActionsButNotControl() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = contactBossAt(20);
			TestTarget target = hostileTargetAt(21);
			boss.bossOnlyEscapeSucceeds = true;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);

			assertTrue(boss.actForTest());
			assertTrue(boss.actForTest());
			assertEquals(4, boss.escapeCooldown());

			boss.paralysed = 1;
			assertTrue(boss.actForTest());
			assertEquals(4, boss.escapeCooldown());
			boss.paralysed = 0;

			for (int expected = 3; expected >= 0; expected--) {
				assertTrue(boss.actForTest());
				assertEquals(expected, boss.escapeCooldown());
			}
			assertEquals(1, boss.bossOnlyEscapes);
			assertTrue(boss.actForTest());
			assertEquals(2, boss.bossOnlyEscapes);
			assertEquals(4, boss.escapeCooldown());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void wardenUsesTransactionalFadeleafThenFallsBackToTheBossVault() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestTarget target = hostileTargetAt(21);

			TestHuntress dualSuccess = contactBossAt(20);
			dualSuccess.enterWardenPhase();
			dualSuccess.fadeleafTeleportSucceeds = true;
			dualSuccess.bossOnlyEscapeSucceeds = true;
			registerActor(dualSuccess, registeredActors);
			registerActor(target, registeredActors);
			dualSuccess.followTargetForTest(target);
			assertTrue(dualSuccess.actForTest());
			assertTrue(dualSuccess.actForTest());
			assertEquals(1, dualSuccess.fadeleafTeleports);
			assertEquals(1, dualSuccess.fadeleafEscapesUsed());
			assertEquals(0, dualSuccess.bossOnlyEscapes);

			Actor.remove(dualSuccess);
			registeredActors.remove(dualSuccess);
			TestHuntress fallback = contactBossAt(20);
			fallback.enterWardenPhase();
			fallback.fadeleafTeleportSucceeds = false;
			fallback.bossOnlyEscapeSucceeds = true;
			registerActor(fallback, registeredActors);
			fallback.followTargetForTest(target);
			assertTrue(fallback.actForTest());
			fallback.resetSpendTracking();
			assertTrue(fallback.actForTest());
			assertEquals(1, fallback.fadeleafTeleports);
			assertEquals(0, fallback.fadeleafEscapesUsed());
			assertEquals(1, fallback.bossOnlyEscapes);
			assertEquals(1, fallback.spendCalls);
			assertEquals(HuntressBoss.TICK, fallback.spentTime, 0f);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void plantFadeleafOnlyStartsTheSharedCooldownAfterACompleteTransaction() {
		TestHuntress boss = new TestHuntress();
		boss.fadeleafBoonSucceeds = false;
		boss.grantBoon(HuntressBoss.WardenBoon.FADELEAF);
		assertEquals(0, boss.escapeCooldown());

		boss.fadeleafBoonSucceeds = true;
		boss.grantBoon(HuntressBoss.WardenBoon.FADELEAF);
		assertEquals(HuntressBoss.ESCAPE_COOLDOWN, boss.escapeCooldown());
		assertEquals(0, boss.fadeleafEscapesUsed());
	}

	@Test
	public void claimingAPlantEndsSelectionAndGrantsItsBoonExactlyOnce() {
		TestHuntress boss = new TestHuntress();
		assertTrue(boss.enterWardenPhase());
		boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
				HuntressBoss.GaleState.HUNTING,
				HuntressBoss.PlantHuntState.SELECTED,
				-1, -1, 0, false, -1, 0, 17, 5, 0);

		boss.onPlantClaimed(17, HuntressBoss.WardenBoon.FIREBLOOM);

		assertEquals(HuntressBoss.PlantHuntState.BOON_ACTIVE,
				boss.plantHuntState());
		assertEquals(HuntressBoss.WardenBoon.FIREBLOOM, boss.activeBoon());
		assertEquals(-1, boss.markedPlantCell());
		assertEquals(1, boss.boonAnnouncements);
	}

	@Test
	public void claimedFadeleafAlwaysReturnsToGraceButOnlySuccessStartsCooldown() {
		for (boolean succeeds : new boolean[]{false, true}) {
			TestHuntress boss = new TestHuntress();
			assertTrue(boss.enterWardenPhase());
			boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
					HuntressBoss.GaleState.HUNTING,
					HuntressBoss.PlantHuntState.SELECTED,
					-1, -1, 0, false, -1, 0, 17, 5, 0);
			boss.fadeleafBoonSucceeds = succeeds;

			boss.onPlantClaimed(17, HuntressBoss.WardenBoon.FADELEAF);

			assertEquals(HuntressBoss.PlantHuntState.GRACE,
					boss.plantHuntState());
			assertEquals(HuntressBoss.PLANT_HUNT_GRACE, boss.plantGraceTurns());
			assertNull(boss.activeBoon());
			assertEquals(succeeds ? HuntressBoss.ESCAPE_COOLDOWN : 0,
					boss.escapeCooldown());
			assertEquals(0, boss.fadeleafEscapesUsed());
		}
	}

	@Test
	public void activeBoonAndSelectedNatureWrathAreExclusiveOnOrdinaryRemoteHits() {
		TestHuntress boss = new TestHuntress();
		assertTrue(boss.enterWardenPhase());
		boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
				HuntressBoss.GaleState.HUNTING,
				HuntressBoss.PlantHuntState.SELECTED,
				-1, -1, 0, false, -1, 0, 17, 5, 0);
		boss.onPlantClaimed(17, HuntressBoss.WardenBoon.FIREBLOOM);
		boss.natureWrathRoll = 0;
		TestTarget target = new TestTarget();

		boss.attackProc(target, 1);
		assertEquals(1, boss.activeBoonHitApplications);
		assertEquals(0, boss.natureWrathActivations);

		boss.setPrivateBoolean("meleeAttack", true);
		boss.attackProc(target, 1);
		boss.setPrivateBoolean("meleeAttack", false);
		boss.setPrivateBoolean("galeShot", true);
		boss.attackProc(target, 1);
		assertEquals(1, boss.activeBoonHitApplications);
	}

	@Test
	public void normalShotRangeAndHawkRelayBoundariesMatchDesign() {
		assertEquals(6, HuntressBoss.BASE_NORMAL_SHOT_RANGE);
		assertEquals(4, HuntressBoss.HAWK_RELAY_RADIUS);
		assertEquals(HuntressBoss.BASE_NORMAL_SHOT_RANGE,
				new HuntressBoss().normalShotRange());

		assertTrue(HuntressBoss.normalShotEligible(true, false, true, 6,
				HuntressBoss.BASE_NORMAL_SHOT_RANGE));
		assertFalse(HuntressBoss.normalShotEligible(true, false, true, 7,
				HuntressBoss.BASE_NORMAL_SHOT_RANGE));
		assertTrue(HuntressBoss.withinHawkRelayRadius(4,
				HuntressBoss.HAWK_RELAY_RADIUS));
		assertFalse(HuntressBoss.withinHawkRelayRadius(5,
				HuntressBoss.HAWK_RELAY_RADIUS));
		assertFalse(HuntressBoss.normalShotEligible(false, true, false, 9,
				HuntressBoss.BASE_NORMAL_SHOT_RANGE));
	}

	@Test
	public void tacticalDecisionUsesThePersistentCombatStepWithoutRandomRolls() {
		assertEquals(HuntressBoss.TacticalAction.MELEE,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.SHOOT,
						false, false, false, 1, 6, false));
		assertEquals(HuntressBoss.TacticalAction.MELEE,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
						true, false, true, 1, 6, true));

		assertEquals(HuntressBoss.TacticalAction.CHASE,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.SHOOT,
						false, false, true, 3, 6, false));
		assertEquals(HuntressBoss.TacticalAction.CHASE,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.SHOOT,
						true, false, false, 3, 6, false));

		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.SHOOT,
						true, false, true, 2, 6, false));
		assertEquals(HuntressBoss.TacticalAction.SEEK_COVER,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
						true, false, true, 2, 6, false));
		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.SHOOT,
						true, false, true, 4, 6, false));
		assertEquals(HuntressBoss.TacticalAction.SEEK_COVER,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
						true, false, true, 4, 6, false));

		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
						true, false, true, 5, 6, false));
		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
						true, false, true, 6, 6, false));
		assertEquals(HuntressBoss.TacticalAction.CHASE,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.SHOOT,
						true, false, true, 7, 6, false));
		assertEquals(HuntressBoss.TacticalAction.SHOOT,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.SHOOT,
						false, true, true, 12, 6, false));

		assertEquals(HuntressBoss.TacticalAction.SEEK_PLANT,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
						true, false, true, 3, 6, true));
		assertEquals(HuntressBoss.TacticalAction.SEEK_PLANT,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
						true, false, true, 6, 6, true));
		assertEquals(HuntressBoss.TacticalAction.SEEK_PLANT,
				HuntressBoss.tacticalAction(HuntressBoss.CombatStep.MOVE,
						false, false, false, 20, 6, true));
	}

	@Test
	public void combatStepChangesOnlyAfterItsMatchingActionReallyCompletes() {
		assertEquals(HuntressBoss.CombatStep.MOVE,
				HuntressBoss.stepAfterCompletedAction(HuntressBoss.CombatStep.SHOOT,
						HuntressBoss.TacticalAction.SHOOT, true));
		assertEquals(HuntressBoss.CombatStep.SHOOT,
				HuntressBoss.stepAfterCompletedAction(HuntressBoss.CombatStep.MOVE,
						HuntressBoss.TacticalAction.SEEK_COVER, true));
		assertEquals(HuntressBoss.CombatStep.MOVE,
				HuntressBoss.stepAfterCompletedAction(HuntressBoss.CombatStep.MOVE,
						HuntressBoss.TacticalAction.SEEK_COVER, false));
		assertEquals(HuntressBoss.CombatStep.SHOOT,
				HuntressBoss.stepAfterCompletedAction(HuntressBoss.CombatStep.SHOOT,
						HuntressBoss.TacticalAction.CHASE, true));
		assertEquals(HuntressBoss.CombatStep.MOVE,
				HuntressBoss.stepAfterCompletedAction(HuntressBoss.CombatStep.MOVE,
						HuntressBoss.TacticalAction.MELEE, true));
	}

	@Test
	public void blindnessAndProjectileLineGateHawkRelayAndNewGaleAims() {
		assertTrue(HuntressBoss.canUseHawkRelay(false, false, true, true, true));
		assertFalse(HuntressBoss.canUseHawkRelay(true, false, true, true, true));
		assertFalse(HuntressBoss.canUseHawkRelay(false, true, true, true, true));
		assertFalse(HuntressBoss.canUseHawkRelay(false, false, false, true, true));
		assertFalse(HuntressBoss.canUseHawkRelay(false, false, true, false, true));
		assertFalse(HuntressBoss.canUseHawkRelay(false, false, true, true, false));
		assertTrue(HuntressBoss.canAcquireGaleTarget(false));
		assertFalse(HuntressBoss.canAcquireGaleTarget(true));
	}

	@Test
	public void hawkRelayMakesTargetSeenForHuntingWithoutChangingBossFov() {
		assertTrue(HuntressBoss.enemySeenForHunting(false, true));
		assertFalse(HuntressBoss.enemySeenForHunting(false, false));
	}

	@Test
	public void hawkCannotRelayAnEnemyThroughTheBossProjectileLine() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		TestLevel level = null;
		NoResistanceTarget target = null;
		try {
			level = testLevel(9, 7);
			int bossCell = 28;
			int wallCell = 29;
			int hawkCell = 31;
			int targetCell = 32;
			level.map[wallCell] = Terrain.WALL;
			level.passable[wallCell] = false;
			level.solid[wallCell] = true;
			Dungeon.level = level;

			TestHuntress boss = new TestHuntress();
			boss.pos = bossCell;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.state = boss.WANDERING;
			boss.resetFieldOfView();
			HuntressBoss.DistractingHawk hawk =
					new HuntressBoss.DistractingHawk(boss.alignment);
			hawk.pos = hawkCell;
			target = new NoResistanceTarget();
			target.pos = targetCell;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);
			level.mobs.add(target);

			assertFalse(boss.isHawkRelayedForTest(target));
			assertFalse(new Ballistica(boss.pos, target.pos,
					Ballistica.PROJECTILE).collisionPos == target.pos);
			assertFalse(boss.canRangedAttack(target));
		} finally {
			if (level != null && target != null) {
				level.mobs.remove(target);
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void hawkRelayExtendsNormalShotPastSixOnlyWithAnUnblindedClearLink() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		Blindness bossBlindness = null;
		Blindness hawkBlindness = null;
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 40;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.resetFieldOfView();
			HuntressBoss.DistractingHawk hawk =
					new HuntressBoss.DistractingHawk(boss.alignment);
			hawk.pos = 31;
			TestTarget target = new TestTarget();
			target.pos = 48;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);

			assertEquals(8, Dungeon.level.distance(boss.pos, target.pos));
			assertEquals(4, Dungeon.level.distance(hawk.pos, target.pos));
			assertTrue(boss.isHawkRelayedForTest(target));
			assertTrue(boss.canRangedAttack(target));

			bossBlindness = new Blindness();
			assertTrue(bossBlindness.attachTo(boss));
			assertFalse(boss.isHawkRelayedForTest(target));
			assertFalse(boss.canRangedAttack(target));
			bossBlindness.detach();
			bossBlindness = null;

			hawkBlindness = new Blindness();
			assertTrue(hawkBlindness.attachTo(hawk));
			assertFalse(boss.isHawkRelayedForTest(target));
			assertFalse(boss.canRangedAttack(target));
		} finally {
			if (bossBlindness != null && bossBlindness.target != null) {
				bossBlindness.detach();
			}
			if (hawkBlindness != null && hawkBlindness.target != null) {
				hawkBlindness.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void foreignFactionHawkCannotRelayEvenWhenEveryOtherConditionIsTrue() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 40;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.resetFieldOfView();
			HuntressBoss.DistractingHawk foreignHawk =
					new HuntressBoss.DistractingHawk(Char.Alignment.ENEMY2);
			foreignHawk.pos = 31;
			TestTarget target = new TestTarget();
			target.pos = 48;
			target.alignment = Char.Alignment.ALLY;
			registerActor(boss, registeredActors);
			registerActor(foreignHawk, registeredActors);
			registerActor(target, registeredActors);

			assertEquals(4, Dungeon.level.distance(foreignHawk.pos, target.pos));
			assertTrue(new Ballistica(boss.pos, target.pos,
					Ballistica.PROJECTILE).collisionPos == target.pos);
			assertFalse(boss.isHawkRelayedForTest(target));
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void realShotAndSuccessfulCoverMoveAlternateTheCombatStep() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 30;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.sprite = new HeadlessCharSprite();
			boss.useHeadlessMovementForTest();
			boss.setCombatStepForTest(HuntressBoss.CombatStep.MOVE);
			TestTarget target = new TestTarget();
			target.pos = 33;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);

			assertTrue(boss.doRangedAttack(target));
			assertEquals(HuntressBoss.CombatStep.MOVE, boss.combatStep());

			boss.resetFieldOfView();
			boss.setVisibleForTest(target.pos);
			assertFalse(boss.canRangedAttack(target));
			int oldPos = boss.pos;
			assertTrue(boss.getCloserViaBossForTest(target.pos));
			assertFalse(oldPos == boss.pos);
			assertEquals(HuntressBoss.CombatStep.SHOOT, boss.combatStep());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void failedCoverMoveLeavesTheMoveStepPending() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 30;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.setCombatStepForTest(HuntressBoss.CombatStep.MOVE);
			TestTarget target = new TestTarget();
			target.pos = 33;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);
			boss.resetFieldOfView();
			boss.setVisibleForTest(target.pos);
			assertFalse(boss.canRangedAttack(target));
			Arrays.fill(Dungeon.level.passable, false);

			assertFalse(boss.getCloserViaBossForTest(target.pos));
			assertEquals(30, boss.pos);
			assertEquals(HuntressBoss.CombatStep.MOVE, boss.combatStep());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void attackEntryRecomputesRelayAfterBlindnessHawkAndBallisticChanges() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 40;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.resetFieldOfView();
			HuntressBoss.DistractingHawk hawk =
					new HuntressBoss.DistractingHawk(boss.alignment);
			hawk.pos = 31;
			TestTarget target = new TestTarget();
			target.pos = 48;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);

			assertTrue(boss.canAttackForTest(target));
			boss.plantMarkBlinded = true;
			assertFalse(boss.doAttackViaBossForTest(target));
			assertTrue(boss.attackedTargets.isEmpty());
			assertEquals(HuntressBoss.CombatStep.SHOOT, boss.combatStep());
			boss.plantMarkBlinded = false;

			assertTrue(boss.canAttackForTest(target));
			hawk.HP = 0;
			assertFalse(boss.doAttackViaBossForTest(target));
			assertTrue(boss.attackedTargets.isEmpty());
			hawk.HP = hawk.HT;

			assertTrue(boss.canAttackForTest(target));
			hawk.pos = 28;
			assertFalse(boss.doAttackViaBossForTest(target));
			assertTrue(boss.attackedTargets.isEmpty());
			hawk.pos = 31;

			assertTrue(boss.canAttackForTest(target));
			Dungeon.level.map[44] = Terrain.WALL;
			Dungeon.level.buildFlagMaps();
			assertFalse(boss.doAttackViaBossForTest(target));
			assertTrue(boss.attackedTargets.isEmpty());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void movementEntryRecomputesAfterBossAndTargetPositionsChange() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 40;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.useHeadlessMovementForTest();
			boss.setCombatStepForTest(HuntressBoss.CombatStep.MOVE);
			TestTarget target = new TestTarget();
			target.pos = 43;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);
			boss.resetFieldOfView();
			boss.setVisibleForTest(target.pos);

			assertFalse(boss.canAttackForTest(target));
			target.pos = 48;
			boss.followTargetForTest(target);
			assertTrue(boss.getCloserViaBossForTest(target.pos));
			assertEquals(HuntressBoss.CombatStep.MOVE, boss.combatStep());

			boss.pos = 40;
			target.pos = 43;
			boss.setCombatStepForTest(HuntressBoss.CombatStep.MOVE);
			boss.followTargetForTest(target);
			boss.resetFieldOfView();
			boss.setVisibleForTest(target.pos);
			assertFalse(boss.canAttackForTest(target));
			boss.pos = 42;
			assertFalse(boss.getCloserViaBossForTest(target.pos));
			assertEquals(HuntressBoss.CombatStep.MOVE, boss.combatStep());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void moveStepApproachesRealCoverWhenNoOutOfSightDestinationExists() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			RhythmLevel level = rhythmLevel(13, 7);
			Dungeon.level = level;
			Dungeon.hero = null;
			int cover = 14;
			Level.set(cover, Terrain.WALL, level);
			level.buildFlagMaps();
			level.setCoverClustersForTest(Collections.singletonList(
					new HashSet<>(Collections.singletonList(cover))));
			Arrays.fill(level.heroFOV, true);

			TestHuntress boss = new TestHuntress();
			boss.pos = 40;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.useBossMovementForTest();
			boss.useHeadlessMovementForTest();
			boss.setCombatStepForTest(HuntressBoss.CombatStep.MOVE);
			TestTarget target = hostileTargetAt(44);
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);
			boss.resetFieldOfView();
			boss.setVisibleForTest(target.pos);

			int chosenCoverStep = boss.nearestFirePositionByCoverForTest(target);
			assertEquals(27, chosenCoverStep);
			assertTrue(boss.getCloserViaBossForTest(target.pos));

			assertEquals(chosenCoverStep, boss.pos);
			assertEquals(target.pos, new Ballistica(boss.pos, target.pos,
					Ballistica.PROJECTILE).collisionPos.intValue());
			assertTrue(boss.pos != 41);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void asynchronousNormalShotSwitchesOnLaunchButNeverAgainOnResolution() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 30;
			boss.alignment = Char.Alignment.ENEMY1;
			DeferredZapSprite sprite = new DeferredZapSprite();
			boss.sprite = sprite;
			boss.setCombatStepForTest(HuntressBoss.CombatStep.MOVE);
			TestTarget target = new TestTarget();
			target.pos = 33;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);

			assertFalse(boss.doRangedAttack(target));
			assertEquals(HuntressBoss.CombatStep.MOVE, boss.combatStep());

			boss.setCombatStepForTest(HuntressBoss.CombatStep.SHOOT);
			target.HP = 0;
			sprite.completeZapWithoutScene();

			assertEquals(HuntressBoss.CombatStep.SHOOT, boss.combatStep());
			assertTrue(boss.attackedTargets.isEmpty());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void relayOnlyTargetDoesNotTriggerGaleWhenCountdownIsReady() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(25, 7);
			TestHuntress boss = new TestHuntress();
			boss.pos = 76;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(0);
			HuntressBoss.DistractingHawk hawk =
					new HuntressBoss.DistractingHawk(boss.alignment);
			hawk.pos = 70;
			TestTarget target = new TestTarget();
			target.pos = 96;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);

			assertTrue(boss.isHawkRelayedForTest(target));
			assertFalse(boss.bossCanSeeForTest(target));
			assertTrue(new Ballistica(boss.pos, target.pos,
					Ballistica.PROJECTILE).collisionPos == target.pos);

			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.READY, boss.galeState());
			assertEquals(-1, boss.galeAimTarget());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(0, boss.galeAims);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void fiveOrdinaryActionsThenAimFireAndRecoverConsumeSeparateTurns() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		TestTarget target = null;
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 40;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(HuntressBoss.GALE_INTERVAL);
			target = new NoResistanceTarget();
			target.pos = 45;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			Dungeon.level.mobs.add(target);

			for (int remaining = 4; remaining >= 0; remaining--) {
				assertTrue(boss.actForTest());
				assertEquals(remaining, boss.galeTurnsRemaining());
				assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());
				assertEquals(-1, boss.galeAimTarget());
				assertEquals(0, boss.galeAims);
				assertEquals(5 - remaining, boss.attackedTargets.size());
				assertEquals(0, boss.closerAttempts);
			}

			int attacksBeforeAim = boss.attackedTargets.size();
			int movesBeforeAim = boss.closerAttempts;
			assertTrue(attacksBeforeAim + movesBeforeAim > 0);
			int positionBeforeAim = boss.pos;
			assertTrue(boss.actForTest());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(HuntressBoss.GaleState.AIMED, boss.galeState());
			assertEquals(target.pos, boss.galeAimTarget());
			assertEquals(1, boss.galeAims);
			assertEquals(attacksBeforeAim, boss.attackedTargets.size());
			assertEquals(movesBeforeAim, boss.closerAttempts);
			assertEquals(positionBeforeAim, boss.pos);

			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.RECOVERING, boss.galeState());
			assertEquals(1, boss.galeRecoveryTurns());
			assertEquals(0, boss.galeRecoveryAnnouncements);
			assertEquals(1, boss.galeAims);
			assertEquals(attacksBeforeAim + 1, boss.attackedTargets.size());
			assertEquals(movesBeforeAim, boss.closerAttempts);
			assertEquals(positionBeforeAim, boss.pos);

			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());
			assertEquals(HuntressBoss.GALE_INTERVAL, boss.galeTurnsRemaining());
			assertEquals(0, boss.galeRecoveryTurns());
			assertEquals(1, boss.galeRecoveryAnnouncements);
			assertEquals(attacksBeforeAim + 1, boss.attackedTargets.size());
			assertEquals(movesBeforeAim, boss.closerAttempts);
			assertEquals(positionBeforeAim, boss.pos);
		} finally {
			if (Dungeon.level != null && target != null) {
				Dungeon.level.mobs.remove(target);
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void failedMovementDoesNotAdvanceTheOrdinaryActionCounter() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		TestTarget target = null;
		try {
			Dungeon.level = testLevel(25, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 76;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(3);
			target = new TestTarget();
			target.pos = 96;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			Dungeon.level.mobs.add(target);
			boss.followTargetForTest(target);

			assertTrue(boss.actForTest());

			assertEquals(1, boss.closerAttempts);
			assertTrue(boss.attackedTargets.isEmpty());
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());
			assertEquals(3, boss.galeTurnsRemaining());
		} finally {
			if (Dungeon.level != null && target != null) {
				Dungeon.level.mobs.remove(target);
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void noEnemyWaitingDoesNotAdvanceTheOrdinaryActionCounter() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 31;
			boss.prepareForGaleAct(3);
			boss.skipBaseEnemySelection = true;
			registerActor(boss, registeredActors);

			assertTrue(boss.actForTest());

			assertTrue(boss.attackedTargets.isEmpty());
			assertEquals(0, boss.closerAttempts);
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());
			assertEquals(3, boss.galeTurnsRemaining());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void fleeingBaseActsPreserveHuntingAimedAndRecoveringGaleState() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			for (HuntressBoss.GaleState stage : new HuntressBoss.GaleState[]{
					HuntressBoss.GaleState.HUNTING,
					HuntressBoss.GaleState.AIMED,
					HuntressBoss.GaleState.RECOVERING}) {
				TestHuntress boss = new TestHuntress();
				boss.pos = 40;
				boss.prepareGaleStageForTest(stage, 45);
				boss.skipBaseEnemySelection = true;
				boss.state = boss.FLEEING;
				registerActor(boss, registeredActors);
				GaleSnapshot before = new GaleSnapshot(boss);

				assertTrue(boss.actForTest());

				before.assertUnchanged(boss);
			}
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void amokBaseTargetingPreservesHuntingAimedAndRecoveringGaleState() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		TestTarget sameAlignment = null;
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			sameAlignment = new TestTarget();
			sameAlignment.pos = 41;
			sameAlignment.alignment = Char.Alignment.ENEMY;
			registerActor(sameAlignment, registeredActors);
			Dungeon.level.mobs.add(sameAlignment);

			for (HuntressBoss.GaleState stage : new HuntressBoss.GaleState[]{
					HuntressBoss.GaleState.HUNTING,
					HuntressBoss.GaleState.AIMED,
					HuntressBoss.GaleState.RECOVERING}) {
				TestHuntress boss = new TestHuntress();
				assertTrue(new Amok().attachTo(boss));
				boss.pos = 40;
				boss.prepareGaleStageForTest(stage, 45);
				boss.immediateBaseAttack = true;
				registerActor(boss, registeredActors);
				GaleSnapshot before = new GaleSnapshot(boss);

				assertTrue(boss.actForTest());

				assertSame(sameAlignment, boss.enemyForTest());
				before.assertUnchanged(boss);
				boss.buff(Amok.class).detach();
			}
		} finally {
			if (Dungeon.level != null && sameAlignment != null) {
				Dungeon.level.mobs.remove(sameAlignment);
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void paralysisFreezeAndBothSleepFormsPauseEveryGaleStage() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 31;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(3);
			registerActor(boss, registeredActors);

			boss.paralysed = 1;
			assertTrue(boss.actForTest());
			assertEquals(3, boss.galeTurnsRemaining());
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());

			// Frozen shares the engine's paralysed gate and must pause identically.
			assertTrue(boss.actForTest());
			assertEquals(3, boss.galeTurnsRemaining());
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());

			boss.paralysed = 0;
			boss.state = boss.SLEEPING;
			assertTrue(boss.actForTest());
			assertEquals(3, boss.galeTurnsRemaining());
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());

			// Magical sleep shares the SLEEPING state and must also pause identically.
			boss.state = boss.SLEEPING;
			assertTrue(boss.actForTest());
			assertEquals(3, boss.galeTurnsRemaining());
			assertEquals(HuntressBoss.GaleState.HUNTING, boss.galeState());

			boss.state = boss.HUNTING;
			boss.prepareAimedGale(35);
			boss.paralysed = 1;
			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.AIMED, boss.galeState());
			assertEquals(35, boss.galeAimTarget());
			assertTrue(boss.attackedTargets.isEmpty());

			boss.paralysed = 0;
			boss.state = boss.SLEEPING;
			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.AIMED, boss.galeState());
			assertEquals(35, boss.galeAimTarget());

			boss.state = boss.HUNTING;
			boss.prepareRecoveringGale();
			boss.paralysed = 1;
			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.RECOVERING, boss.galeState());
			assertEquals(1, boss.galeRecoveryTurns());

			boss.paralysed = 0;
			boss.state = boss.SLEEPING;
			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.RECOVERING, boss.galeState());
			assertEquals(1, boss.galeRecoveryTurns());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void readyBossKeepsTrackingUntilARealVisibleTargetCanBeAimed() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(25, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 76;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(0);
			TestTarget target = new TestTarget();
			target.pos = 96;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			Dungeon.level.mobs.add(target);
			boss.followTargetForTest(target);

			assertTrue(boss.actForTest());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(HuntressBoss.GaleState.READY, boss.galeState());
			assertEquals(-1, boss.galeAimTarget());
			assertTrue(boss.closerAttempts > 0);

			target.pos = 80;
			assertTrue(boss.actForTest());
			assertEquals(0, boss.galeTurnsRemaining());
			assertEquals(HuntressBoss.GaleState.AIMED, boss.galeState());
			assertEquals(target.pos, boss.galeAimTarget());
			assertEquals(1, boss.galeAims);
		} finally {
			if (Dungeon.level != null) {
				Dungeon.level.mobs.removeIf(mob -> mob instanceof TestTarget);
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void blindnessPreventsNewAimButDoesNotCancelAnExistingLock() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 40;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(0);
			TestTarget target = new NoResistanceTarget();
			target.pos = 44;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);

			boss.galeAimBlinded = true;
			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.READY, boss.galeState());
			assertEquals(0, boss.galeAims);

			boss.galeAimBlinded = false;
			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.AIMED, boss.galeState());
			assertEquals(1, boss.galeAims);

			boss.galeAimBlinded = true;
			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.RECOVERING, boss.galeState());
			assertTrue(boss.attackedTargets.contains(target));
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void animatedGaleFireAdvancesTheEscapeCooldownWhenTheActionStarts() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(25, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 76;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareAimedGale(80);
			boss.setEscapeCooldownForTest(3);
			DeferredZapSprite sprite = new DeferredZapSprite();
			boss.sprite = sprite;
			registerActor(boss, registeredActors);

			assertFalse(boss.actForTest());
			assertEquals(2, boss.escapeCooldown());
			sprite.completeZapWithoutScene();
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void pendingGaleFiresAtLockedCellAfterTargetMoves() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		NoResistanceTarget lockedRayTarget = null;
		try {
			Dungeon.level = testLevel(25, 7);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 76;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareForGaleAct(0);
			TestTarget target = new TestTarget();
			target.pos = 80;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);

			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.AIMED, boss.galeState());
			assertEquals(target.pos, boss.galeAimTarget());
			target.pos = 121;
			for (int cell = 77; cell < 80; cell++) {
				assertNull("unexpected actor on locked gale path at " + cell,
						Actor.findChar(cell));
			}
			lockedRayTarget = new NoResistanceTarget();
			lockedRayTarget.pos = 80;
			lockedRayTarget.alignment = Char.Alignment.ENEMY2;
			registerActor(lockedRayTarget, registeredActors);

			assertTrue(boss.actForTest());
			assertEquals(HuntressBoss.GaleState.RECOVERING, boss.galeState());
			assertEquals(0, boss.galeTurnsRemaining());
			assertTrue(boss.attackedTargets.contains(lockedRayTarget));
			assertFalse(boss.attackedTargets.contains(target));
		} finally {
			if (lockedRayTarget != null && lockedRayTarget.buff(Cripple.class) != null) {
				lockedRayTarget.buff(Cripple.class).detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void galeSkipsSameAlignmentCreaturesAlongItsLockedPath() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		NoResistanceTarget enemy = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 10;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.prepareAimedGale(14);
			HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
			hawk.pos = 12;
			hawk.alignment = Char.Alignment.ENEMY1;
			enemy = new NoResistanceTarget();
			enemy.pos = 14;
			enemy.alignment = Char.Alignment.ENEMY2;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(enemy, registeredActors);
			assertTrue(boss.actForTest());

			assertFalse(boss.attackedTargets.contains(hawk));
			assertNull(hawk.buff(Cripple.class));
			assertTrue(boss.attackedTargets.contains(enemy));
			assertNotNull(enemy.buff(Cripple.class));
		} finally {
			if (enemy != null && enemy.buff(Cripple.class) != null) {
				enemy.buff(Cripple.class).detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void huntressFactionCannotTargetOrDamageItsOwnSummonsEvenWhileAmoked() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 10;
			boss.alignment = Char.Alignment.ENEMY1;
			boss.sprite = new HeadlessCharSprite();
			boss.setCombatStepForTest(HuntressBoss.CombatStep.SHOOT);
			HuntressBoss.DistractingHawk hawk =
					new HuntressBoss.DistractingHawk(boss.alignment);
			hawk.pos = 12;
			HuntressBoss.HuntressTentacle tentacle =
					new HuntressBoss.HuntressTentacle(boss.alignment);
			tentacle.pos = 12;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(tentacle, registeredActors);

			assertFalse(boss.canAttackForTest(hawk));
			assertFalse(boss.canRangedAttack(hawk));
			assertFalse(boss.doRangedAttack(hawk));
			assertFalse(boss.attackedTargets.contains(hawk));
			tentacle.pos = boss.pos + 1;
			assertFalse(boss.canMeleeAttack(tentacle));

			assertTrue(boss.enterWardenPhase());
			boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
					HuntressBoss.GaleState.HUNTING,
					HuntressBoss.PlantHuntState.SELECTED,
					-1, -1, 0, false, -1, 0, 20, 4, 0);
			boss.natureWrathRoll = 0;
			boss.attackProc(hawk, 1);
			assertEquals(0, boss.natureWrathActivations);

			boss.confusedForCustomActions = true;
			assertFalse(boss.canAttackForTest(tentacle));
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void summonsNeverAcquireHuntressFactionTargetsEvenWhileAmoked() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 10;
			boss.alignment = Char.Alignment.ENEMY1;
			TestHawk hawk = new TestHawk(boss.alignment);
			hawk.pos = 11;
			TestTentacle tentacle = new TestTentacle(boss.alignment);
			tentacle.pos = 12;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(tentacle, registeredActors);
			Dungeon.level.mobs.add(boss);
			Dungeon.level.mobs.add(hawk);
			Dungeon.level.mobs.add(tentacle);
			hawk.setAllVisibleForTest();
			tentacle.setAllVisibleForTest();
			hawk.aggro(boss);
			tentacle.aggro(hawk);
			assertNull(hawk.enemyForTest());
			assertNull(tentacle.enemyForTest());
			assertFalse(hawk.canAttackForTest(boss));
			assertFalse(tentacle.canAttackForTest(hawk));
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void aggressionMakesOnlyTheMarkedHuntressFactionMemberAttackable() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression targetAggression = null;
		StoneOfAggression.Aggression attackerAggression = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 10;
			boss.alignment = Char.Alignment.ENEMY;
			boss.state = boss.HUNTING;
			boss.setCombatStepForTest(HuntressBoss.CombatStep.SHOOT);
			boss.resetFieldOfView();
			TestHawk hawk = new TestHawk(boss.alignment);
			hawk.pos = 13;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			Dungeon.level.mobs.add(boss);
			Dungeon.level.mobs.add(hawk);
			boss.setVisibleForTest(hawk.pos);

			assertFalse(boss.canAttackForTest(hawk));
			attackerAggression = new StoneOfAggression.Aggression();
			assertTrue(attackerAggression.attachTo(boss));
			assertFalse("an aggressed attacker must not attack an unmarked ally",
					boss.canAttackForTest(hawk));
			attackerAggression.detach();
			attackerAggression = null;

			targetAggression = new StoneOfAggression.Aggression();
			assertTrue(targetAggression.attachTo(hawk));
			boss.followTargetForTest(hawk);
			assertSame(hawk, boss.chooseEnemyForTest());
			assertTrue(boss.canAttackForTest(hawk));
			assertTrue(boss.canRangedAttack(hawk));
			assertTrue(boss.doRangedAttack(hawk));
			assertTrue(boss.attackedTargets.contains(hawk));
			assertTrue(boss.enterWardenPhase());
			boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
					HuntressBoss.GaleState.HUNTING,
					HuntressBoss.PlantHuntState.SELECTED,
					-1, -1, 0, false, -1, 0, 20, 4, 0);
			boss.natureWrathRoll = 0;
			boss.attackProc(hawk, 1);
			assertEquals(1, boss.natureWrathActivations);

			targetAggression.detach();
			targetAggression = null;
			assertFalse(boss.canAttackForTest(hawk));
			assertFalse(boss.canRangedAttack(hawk));
			boss.attackProc(hawk, 1);
			assertEquals(1, boss.natureWrathActivations);
		} finally {
			if (targetAggression != null && targetAggression.target != null) {
				targetAggression.detach();
			}
			if (attackerAggression != null && attackerAggression.target != null) {
				attackerAggression.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void summonsSelectAndAggroOnlyAggressedHuntressFactionMembers() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression bossAggression = null;
		StoneOfAggression.Aggression hawkAggression = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 10;
			boss.alignment = Char.Alignment.ENEMY;
			TestHawk hawk = new TestHawk(boss.alignment);
			hawk.pos = 11;
			TestTentacle tentacle = new TestTentacle(boss.alignment);
			tentacle.pos = 19;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(tentacle, registeredActors);
			Dungeon.level.mobs.add(boss);
			Dungeon.level.mobs.add(hawk);
			Dungeon.level.mobs.add(tentacle);
			hawk.setAllVisibleForTest();
			tentacle.setAllVisibleForTest();
			hawk.state = hawk.HUNTING;
			tentacle.state = tentacle.HUNTING;

			bossAggression = new StoneOfAggression.Aggression();
			assertTrue(bossAggression.attachTo(boss));
			hawk.setEnemyForTest(boss);
			assertSame(boss, hawk.chooseEnemyForTest());
			assertTrue(hawk.canAttackForTest(boss));
			hawk.aggro(boss);
			assertSame(boss, hawk.enemyForTest());
			tentacle.setEnemyForTest(boss);
			assertSame(boss, tentacle.chooseEnemyForTest());
			assertTrue(tentacle.canAttackForTest(boss));
			tentacle.aggro(boss);
			assertSame(boss, tentacle.enemyForTest());
			assertEquals(7, tentacle.attackProcForTest(boss, 7));
			assertNotNull(boss.buff(Cripple.class));
			boss.buff(Cripple.class).detach();

			hawkAggression = new StoneOfAggression.Aggression();
			assertTrue(hawkAggression.attachTo(hawk));
			tentacle.setEnemyForTest(hawk);
			assertSame(hawk, tentacle.chooseEnemyForTest());
			assertTrue(tentacle.canAttackForTest(hawk));
			tentacle.aggro(hawk);
			assertSame(hawk, tentacle.enemyForTest());

			bossAggression.detach();
			bossAggression = null;
			hawkAggression.detach();
			hawkAggression = null;
			hawk.aggro(boss);
			tentacle.aggro(hawk);
			assertNull(hawk.enemyForTest());
			assertNull(tentacle.enemyForTest());
		} finally {
			if (bossAggression != null && bossAggression.target != null) {
				bossAggression.detach();
			}
			if (hawkAggression != null && hawkAggression.target != null) {
				hawkAggression.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void bossAggroRejectsProtectedSummonsButAcceptsAggressedOnes() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		TestHuntress boss = new TestHuntress();
		boss.alignment = Char.Alignment.ENEMY;
		TestHawk hawk = new TestHawk(boss.alignment);
		StoneOfAggression.Aggression aggression = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			boss.followTargetForTest(hawk);
			boss.aggro(hawk);
			assertNull(boss.enemyForTest());

			aggression = new StoneOfAggression.Aggression();
			assertTrue(aggression.attachTo(hawk));
			boss.aggro(hawk);
			assertSame(hawk, boss.enemyForTest());

			aggression.detach();
			aggression = null;
			boss.aggro(hawk);
			assertNull(boss.enemyForTest());
		} finally {
			if (aggression != null && aggression.target != null) {
				aggression.detach();
			}
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void summonAggroClearsOnlyTheCurrentTargetWhenAggressionExpires() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		TestHuntress boss = new TestHuntress();
		boss.alignment = Char.Alignment.ENEMY;
		TestHawk hawk = new TestHawk(boss.alignment);
		TestTentacle tentacle = new TestTentacle(boss.alignment);
		TestTarget legalEnemy = new TestTarget();
		legalEnemy.alignment = Char.Alignment.ALLY;
		StoneOfAggression.Aggression bossAggression = null;
		StoneOfAggression.Aggression hawkAggression = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			bossAggression = new StoneOfAggression.Aggression();
			assertTrue(bossAggression.attachTo(boss));
			hawk.aggro(boss);
			assertSame(boss, hawk.enemyForTest());

			bossAggression.detach();
			bossAggression = null;
			hawk.aggro(boss);
			assertNull(hawk.enemyForTest());
			hawk.setEnemyForTest(legalEnemy);
			hawk.aggro(boss);
			assertSame(legalEnemy, hawk.enemyForTest());

			hawkAggression = new StoneOfAggression.Aggression();
			assertTrue(hawkAggression.attachTo(hawk));
			tentacle.aggro(hawk);
			assertSame(hawk, tentacle.enemyForTest());

			hawkAggression.detach();
			hawkAggression = null;
			tentacle.aggro(hawk);
			assertNull(tentacle.enemyForTest());
			tentacle.setEnemyForTest(legalEnemy);
			tentacle.aggro(hawk);
			assertSame(legalEnemy, tentacle.enemyForTest());
		} finally {
			if (bossAggression != null && bossAggression.target != null) {
				bossAggression.detach();
			}
			if (hawkAggression != null && hawkAggression.target != null) {
				hawkAggression.detach();
			}
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void aggressedTentacleCompletesTheFactionTargetMatrix() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression aggression = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 10;
			boss.alignment = Char.Alignment.ENEMY;
			boss.state = boss.HUNTING;
			boss.setCombatStepForTest(HuntressBoss.CombatStep.MOVE);
			boss.resetFieldOfView();
			TestHawk hawk = new TestHawk(boss.alignment);
			hawk.pos = 12;
			TestTentacle tentacle = new TestTentacle(boss.alignment);
			tentacle.pos = 11;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			registerActor(tentacle, registeredActors);
			Dungeon.level.mobs.add(boss);
			Dungeon.level.mobs.add(hawk);
			Dungeon.level.mobs.add(tentacle);
			boss.setVisibleForTest(tentacle.pos);
			hawk.setAllVisibleForTest();
			aggression = new StoneOfAggression.Aggression();
			assertTrue(aggression.attachTo(tentacle));

			boss.followTargetForTest(tentacle);
			assertSame(tentacle, boss.chooseEnemyForTest());
			assertTrue(boss.canAttackForTest(tentacle));
			boss.aggro(tentacle);
			assertSame(tentacle, boss.enemyForTest());
			assertTrue(boss.doAttackViaBossForTest(tentacle));
			assertTrue(boss.attackedTargets.contains(tentacle));
			hawk.setEnemyForTest(tentacle);
			assertSame(tentacle, hawk.chooseEnemyForTest());
			assertTrue(hawk.canAttackForTest(tentacle));
			hawk.aggro(tentacle);
			assertSame(tentacle, hawk.enemyForTest());
			assertEquals(1, hawk.attackProcForTest(tentacle, 7));
			assertNotNull(tentacle.buff(Blindness.class));
			assertNotNull(tentacle.buff(Cripple.class));
		} finally {
			if (aggression != null && aggression.target != null) {
				aggression.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void hawkStillSelectsAnAggressedTargetThatIsAlreadyCrippled() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression aggression = null;
		Cripple cripple = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHawk hawk = new TestHawk(Char.Alignment.ENEMY);
			hawk.pos = 10;
			TestTentacle tentacle = new TestTentacle(hawk.alignment);
			tentacle.pos = 11;
			registerActor(hawk, registeredActors);
			registerActor(tentacle, registeredActors);
			Dungeon.level.mobs.add(hawk);
			Dungeon.level.mobs.add(tentacle);
			hawk.setAllVisibleForTest();
			hawk.state = hawk.HUNTING;
			aggression = new StoneOfAggression.Aggression();
			assertTrue(aggression.attachTo(tentacle));
			cripple = new Cripple();
			assertTrue(cripple.attachTo(tentacle));

			hawk.chooseEnemyAndHuntForTest();

			assertSame(tentacle, hawk.attackedTarget);
		} finally {
			if (aggression != null && aggression.target != null) {
				aggression.detach();
			}
			if (cripple != null && cripple.target != null) {
				cripple.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void hawkPrioritizesAggressedTargetsBeforeCloserOrdinaryTargets() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression aggression = null;
		try {
			Dungeon.level = testLevel(11, 5);
			Dungeon.hero = null;
			TestHawk hawk = new TestHawk(Char.Alignment.ENEMY);
			hawk.pos = 12;
			TestTarget closeOrdinary = new TestTarget();
			closeOrdinary.pos = 13;
			closeOrdinary.alignment = Char.Alignment.ALLY;
			TestTentacle farMarked = new TestTentacle(hawk.alignment);
			farMarked.pos = 16;
			registerActor(hawk, registeredActors);
			registerActor(closeOrdinary, registeredActors);
			registerActor(farMarked, registeredActors);
			Dungeon.level.mobs.add(hawk);
			Dungeon.level.mobs.add(closeOrdinary);
			Dungeon.level.mobs.add(farMarked);
			hawk.setAllVisibleForTest();
			hawk.state = hawk.HUNTING;
			aggression = new StoneOfAggression.Aggression();
			assertTrue(aggression.attachTo(farMarked));

			hawk.chooseEnemyAndHuntForTest();

			assertSame(farMarked, hawk.enemyForTest());
		} finally {
			if (aggression != null && aggression.target != null) {
				aggression.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void hawkUsesDistanceThenIdWithinTheAggressedPriorityGroup() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression farAggression = null;
		StoneOfAggression.Aggression firstNearAggression = null;
		StoneOfAggression.Aggression secondNearAggression = null;
		try {
			Dungeon.level = testLevel(11, 5);
			Dungeon.hero = null;
			TestHawk hawk = new TestHawk(Char.Alignment.ENEMY);
			hawk.pos = 23;
			TestTentacle lowerIdNear = new TestTentacle(hawk.alignment);
			lowerIdNear.pos = 22;
			TestTentacle higherIdNear = new TestTentacle(hawk.alignment);
			higherIdNear.pos = 24;
			TestTentacle far = new TestTentacle(hawk.alignment);
			far.pos = 29;
			registerActor(hawk, registeredActors);
			registerActor(lowerIdNear, registeredActors);
			registerActor(higherIdNear, registeredActors);
			registerActor(far, registeredActors);
			Dungeon.level.mobs.add(hawk);
			Dungeon.level.mobs.add(lowerIdNear);
			Dungeon.level.mobs.add(higherIdNear);
			Dungeon.level.mobs.add(far);
			hawk.setAllVisibleForTest();
			hawk.state = hawk.HUNTING;
			firstNearAggression = new StoneOfAggression.Aggression();
			secondNearAggression = new StoneOfAggression.Aggression();
			farAggression = new StoneOfAggression.Aggression();
			assertTrue(firstNearAggression.attachTo(lowerIdNear));
			assertTrue(secondNearAggression.attachTo(higherIdNear));
			assertTrue(farAggression.attachTo(far));

			hawk.chooseEnemyAndHuntForTest();

			assertSame(lowerIdNear, hawk.attackedTarget);
		} finally {
			if (farAggression != null && farAggression.target != null) farAggression.detach();
			if (firstNearAggression != null && firstNearAggression.target != null) firstNearAggression.detach();
			if (secondNearAggression != null && secondNearAggression.target != null) secondNearAggression.detach();
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void hawkAggressionPriorityIgnoresInvisibilityButOrdinarySearchDoesNot() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression aggression = null;
		try {
			Dungeon.level = testLevel(11, 5);
			Dungeon.hero = null;
			TestHawk hawk = new TestHawk(Char.Alignment.ENEMY);
			hawk.pos = 12;
			TestTarget closeOrdinary = new TestTarget();
			closeOrdinary.pos = 13;
			closeOrdinary.alignment = Char.Alignment.ALLY;
			TestTarget invisibleOrdinary = new TestTarget();
			invisibleOrdinary.pos = 14;
			invisibleOrdinary.alignment = Char.Alignment.ALLY;
			invisibleOrdinary.invisible = 2;
			TestTentacle invisibleMarked = new TestTentacle(hawk.alignment);
			invisibleMarked.pos = 16;
			invisibleMarked.invisible = 2;
			registerActor(hawk, registeredActors);
			registerActor(closeOrdinary, registeredActors);
			registerActor(invisibleOrdinary, registeredActors);
			registerActor(invisibleMarked, registeredActors);
			Dungeon.level.mobs.add(hawk);
			Dungeon.level.mobs.add(closeOrdinary);
			Dungeon.level.mobs.add(invisibleOrdinary);
			Dungeon.level.mobs.add(invisibleMarked);
			hawk.setAllVisibleForTest();

			assertSame(closeOrdinary, hawk.nearestEligibleTargetForTest());

			aggression = new StoneOfAggression.Aggression();
			assertTrue(aggression.attachTo(invisibleMarked));
			assertSame(invisibleMarked, hawk.nearestEligibleTargetForTest());
		} finally {
			if (aggression != null && aggression.target != null) {
				aggression.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void summonsRecheckAggressionBeforeApplyingDamageOrDebuffs() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		TestHawk hawk = new TestHawk(Char.Alignment.ENEMY);
		TestTentacle tentacle = new TestTentacle(hawk.alignment);
		StoneOfAggression.Aggression tentacleAggression = null;
		StoneOfAggression.Aggression hawkAggression = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			tentacleAggression = new StoneOfAggression.Aggression();
			assertTrue(tentacleAggression.attachTo(tentacle));
			assertTrue(hawk.canAttackForTest(tentacle));
			tentacleAggression.detach();
			tentacleAggression = null;

			assertEquals(0, hawk.attackProcForTest(tentacle, 7));
			assertNull(tentacle.buff(Blindness.class));
			assertNull(tentacle.buff(Cripple.class));

			hawkAggression = new StoneOfAggression.Aggression();
			assertTrue(hawkAggression.attachTo(hawk));
			assertTrue(tentacle.canAttackForTest(hawk));
			hawkAggression.detach();
			hawkAggression = null;

			assertEquals(0, tentacle.attackProcForTest(hawk, 7));
			assertNull(hawk.buff(Cripple.class));
		} finally {
			if (tentacleAggression != null && tentacleAggression.target != null) {
				tentacleAggression.detach();
			}
			if (hawkAggression != null && hawkAggression.target != null) {
				hawkAggression.detach();
			}
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void normalShotRechecksAggressionBeforeResolvingFriendlyDamage() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression aggression = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 10;
			boss.alignment = Char.Alignment.ENEMY;
			DeferredZapSprite sprite = new DeferredZapSprite();
			sprite.visible = true;
			boss.sprite = sprite;
			boss.setCombatStepForTest(HuntressBoss.CombatStep.SHOOT);
			TestHawk hawk = new TestHawk(boss.alignment);
			hawk.pos = 13;
			registerActor(boss, registeredActors);
			registerActor(hawk, registeredActors);
			aggression = new StoneOfAggression.Aggression();
			assertTrue(aggression.attachTo(hawk));

			assertFalse(boss.doRangedAttack(hawk));
			assertTrue("an aggressed ally should launch a deferred friendly shot",
					sprite.hasPendingCallback());
			aggression.detach();
			aggression = null;
			sprite.completeZapWithoutScene();

			assertFalse(boss.attackedTargets.contains(hawk));
		} finally {
			if (aggression != null && aggression.target != null) {
				aggression.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void galeDamagesAndCripplesOnlyAggressedFactionMembersOnItsPath() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		StoneOfAggression.Aggression aggression = null;
		try {
			Dungeon.level = testLevel(9, 5);
			Dungeon.hero = null;
			TestHuntress boss = new TestHuntress();
			boss.pos = 10;
			boss.alignment = Char.Alignment.ENEMY;
			boss.prepareAimedGale(14);
			TestHawk markedHawk = new TestHawk(boss.alignment);
			markedHawk.pos = 11;
			TestTentacle unmarkedTentacle = new TestTentacle(boss.alignment);
			unmarkedTentacle.pos = 12;
			NoResistanceTarget enemy = new NoResistanceTarget();
			enemy.pos = 14;
			enemy.alignment = Char.Alignment.ALLY;
			registerActor(boss, registeredActors);
			registerActor(markedHawk, registeredActors);
			registerActor(unmarkedTentacle, registeredActors);
			registerActor(enemy, registeredActors);
			aggression = new StoneOfAggression.Aggression();
			assertTrue(aggression.attachTo(markedHawk));

			assertTrue(boss.actForTest());

			assertTrue(boss.attackedTargets.contains(markedHawk));
			assertNotNull(markedHawk.buff(Cripple.class));
			assertFalse(boss.attackedTargets.contains(unmarkedTentacle));
			assertNull(unmarkedTentacle.buff(Cripple.class));
			assertTrue(boss.attackedTargets.contains(enemy));
		} finally {
			if (aggression != null && aggression.target != null) {
				aggression.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void damageRangesDistinguishBothPhaseShotsMeleeAndGale() {
		assertArrayEquals(new int[]{6, 14}, HuntressBoss.damageRange(
				HuntressBoss.Phase.SNIPER, false, false));
		assertArrayEquals(new int[]{10, 16}, HuntressBoss.damageRange(
				HuntressBoss.Phase.SNIPER, true, false));
		assertArrayEquals(new int[]{20, 28}, HuntressBoss.damageRange(
				HuntressBoss.Phase.SNIPER, false, true));
		assertArrayEquals(new int[]{10, 16}, HuntressBoss.damageRange(
				HuntressBoss.Phase.WARDEN, false, false));
		assertArrayEquals(new int[]{12, 20}, HuntressBoss.damageRange(
				HuntressBoss.Phase.WARDEN, true, false));
	}

	@Test
	public void armorModesDistinguishSniperShotsMeleeWardenAndGale() {
		assertTrue(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.SNIPER, false, false));
		assertFalse(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.SNIPER, true, false));
		assertTrue(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.SNIPER, true, true));
		assertFalse(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.WARDEN, false, false));
		assertFalse(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.WARDEN, true, false));
		assertTrue(HuntressBoss.ignoresArmorForAttack(
				HuntressBoss.Phase.WARDEN, false, true));

		assertEquals(0, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.SNIPER, false, false, 20));
		assertEquals(20, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.SNIPER, true, false, 20));
		assertEquals(20, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.WARDEN, false, false, 20));
		assertEquals(20, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.WARDEN, true, false, 20));
		assertEquals(0, HuntressBoss.armorForAttack(
				HuntressBoss.Phase.SNIPER, true, true, 20));
	}

	@Test
	public void natureHuntAppliesExactMovementAttackAndPlantProcRules() {
		assertEquals(3f, HuntressBoss.natureHuntSpeed(1f, true), 0f);
		assertEquals(1f, HuntressBoss.natureHuntSpeed(1f, false), 0f);
		assertEquals(0.75f,
				HuntressBoss.natureHuntAttackDelay(1f, true, false, false), 0f);
		assertEquals(1f,
				HuntressBoss.natureHuntAttackDelay(1f, true, true, false), 0f);
		assertEquals(1f,
				HuntressBoss.natureHuntAttackDelay(1f, true, false, true), 0f);
		assertTrue(HuntressBoss.shouldProcNatureWrath(false, false, 0));
		assertFalse(HuntressBoss.shouldProcNatureWrath(true, false, 0));
		assertFalse(HuntressBoss.shouldProcNatureWrath(false, true, 0));
		assertFalse(HuntressBoss.shouldProcNatureWrath(false, false, 1));
	}

	@Test
	public void wardenUsesThreeOrdinaryActionsThenMarksAndFiveHuntActionsThenLosesTrack() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			RhythmLevel level = rhythmLevel(11, 7);
			Dungeon.level = level;
			Dungeon.hero = null;
			TestHuntress boss = contactBossAt(34);
			assertTrue(boss.enterWardenPhase());
			TestTarget target = hostileTargetAt(38);
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);
			boss.resetFieldOfView();
			boss.setVisibleForTest(target.pos);
			putPlant(level, 18, new Firebloom());

			for (int remaining = 2; remaining >= 0; remaining--) {
				assertTrue(boss.actForTest());
				assertEquals(HuntressBoss.PlantHuntState.GRACE, boss.plantHuntState());
				assertEquals(remaining, boss.plantGraceTurns());
			}

			boss.plantMarkBlinded = true;
			int attacksBeforeBlindTurn = boss.attackedTargets.size();
			assertTrue(boss.actForTest());
			assertEquals(attacksBeforeBlindTurn + 1, boss.attackedTargets.size());
			assertEquals(HuntressBoss.PlantHuntState.GRACE, boss.plantHuntState());
			assertEquals(0, boss.plantGraceTurns());
			boss.plantMarkBlinded = false;

			int attacksBeforeMark = boss.attackedTargets.size();
			assertTrue(boss.actForTest());
			assertEquals(attacksBeforeMark, boss.attackedTargets.size());
			assertEquals(HuntressBoss.PlantHuntState.SELECTED, boss.plantHuntState());
			assertEquals(5, boss.plantHuntTurns());
			assertEquals(18, boss.markedPlantCell());
			assertEquals(1, boss.plantMarkedAnnouncements);
			assertEquals(1, boss.natureHuntAnnouncements);

			for (int remaining = 4; remaining >= 0; remaining--) {
				assertTrue(boss.actForTest());
				assertEquals(HuntressBoss.PlantHuntState.SELECTED, boss.plantHuntState());
				assertEquals(remaining, boss.plantHuntTurns());
			}

			int attacksBeforeLostTrack = boss.attackedTargets.size();
			assertTrue(boss.actForTest());
			assertEquals(attacksBeforeLostTrack, boss.attackedTargets.size());
			assertEquals(HuntressBoss.PlantHuntState.GRACE, boss.plantHuntState());
			assertEquals(3, boss.plantGraceTurns());
			assertEquals(-1, boss.markedPlantCell());
			assertEquals(1, boss.lostTrackAnnouncements);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void restoringVisibleRhythmStatesDoesNotReplayTransitionAnnouncements() {
		Bundle recovering = new Bundle();
		recovering.put("phase", HuntressBoss.Phase.SNIPER);
		recovering.put("gale_state", HuntressBoss.GaleState.RECOVERING);
		recovering.put("gale_recovery_turns", 1);
		TestHuntress restoredRecovery = new TestHuntress();
		restoredRecovery.restoreFromBundle(recovering);
		assertEquals(0, restoredRecovery.galeRecoveryAnnouncements);

		Bundle selected = new Bundle();
		selected.put("phase", HuntressBoss.Phase.WARDEN);
		selected.put("plant_hunt_state", HuntressBoss.PlantHuntState.SELECTED);
		selected.put("marked_plant_cell", 17);
		selected.put("plant_hunt_turns", 4);
		TestHuntress restoredSelected = new TestHuntress();
		restoredSelected.restoreFromBundle(selected);
		assertEquals(0, restoredSelected.plantMarkedAnnouncements);
		assertEquals(0, restoredSelected.natureHuntAnnouncements);
		assertEquals(0, restoredSelected.lostTrackAnnouncements);
	}

	@Test
	public void defaultNatureHuntAnnouncementIsSafeWithoutASceneEmitter() {
		DefaultAnnouncementHuntress boss = new DefaultAnnouncementHuntress();
		boss.sprite = new HeadlessCharSprite();
		boss.announceNatureHuntForTest();
	}

	@Test
	public void selectedPlantMovementUsesRealPathAtDoubleSpeedAndOnlySuccessChangesStep() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			RhythmLevel level = rhythmLevel(11, 7);
			Dungeon.level = level;
			Dungeon.hero = null;
			TestHuntress boss = contactBossAt(34);
			assertTrue(boss.enterWardenPhase());
			boss.useBossMovementForTest();
			boss.useHeadlessMovementForTest();
			TestTarget target = hostileTargetAt(38);
			registerActor(boss, registeredActors);
			registerActor(target, registeredActors);
			boss.followTargetForTest(target);
			boss.resetFieldOfView();
			boss.setVisibleForTest(target.pos);
			int plantCell = 14;
			putPlant(level, plantCell, new Firebloom());
			boss.setRhythmStateForTest(HuntressBoss.CombatStep.MOVE,
					HuntressBoss.GaleState.HUNTING,
					HuntressBoss.PlantHuntState.SELECTED,
					-1, -1, 0, false, -1, 0, plantCell, 5, 0);
			PathFinder.buildDistanceMap(plantCell, level.passable);
			int before = PathFinder.distance[boss.pos];

			assertEquals(3f, boss.speed(), 0f);
			assertTrue(boss.actForTest());

			PathFinder.buildDistanceMap(plantCell, level.passable);
			assertTrue(PathFinder.distance[boss.pos] < before);
			assertEquals(HuntressBoss.CombatStep.SHOOT, boss.combatStep());
			assertEquals(4, boss.plantHuntTurns());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void selectedPlantMovementDoesNotDependOnAnEnemyOrHuntingState() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			for (boolean wandering : new boolean[]{false, true}) {
				RhythmLevel level = rhythmLevel(11, 7);
				Dungeon.level = level;
				Dungeon.hero = null;
				TestHuntress boss = contactBossAt(34);
				assertTrue(boss.enterWardenPhase());
				boss.useBossMovementForTest();
				boss.useHeadlessMovementForTest();
				registerActor(boss, registeredActors);
				int plantCell = 14;
				putPlant(level, plantCell, new Firebloom());
				boss.setRhythmStateForTest(HuntressBoss.CombatStep.MOVE,
						HuntressBoss.GaleState.HUNTING,
						HuntressBoss.PlantHuntState.SELECTED,
						-1, -1, 0, false, -1, 0, plantCell, 5, 0);
				boss.resetFieldOfView();
				boss.clearEnemyForTest(wandering);
				PathFinder.buildDistanceMap(plantCell, level.passable);
				int before = PathFinder.distance[boss.pos];

				assertTrue(boss.actForTest());

				PathFinder.buildDistanceMap(plantCell, level.passable);
				assertTrue(PathFinder.distance[boss.pos] < before);
				assertEquals(HuntressBoss.CombatStep.SHOOT, boss.combatStep());
				assertEquals(4, boss.plantHuntTurns());
				Actor.remove(boss);
				registeredActors.remove(boss);
			}
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void adjacentEnemyKeepsSelectedMoveOnMeleeForWarningAndCooldown() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			for (int escapeCooldown : new int[]{0, 2}) {
				RhythmLevel level = rhythmLevel(11, 7);
				Dungeon.level = level;
				Dungeon.hero = null;
				TestHuntress boss = contactBossAt(34);
				assertTrue(boss.enterWardenPhase());
				boss.setPrivateBoolean("encounterDelay", false);
				boss.useBossMovementForTest();
				boss.useHeadlessMovementForTest();
				TestTarget adjacent = hostileTargetAt(35);
				registerActor(boss, registeredActors);
				registerActor(adjacent, registeredActors);
				boss.followTargetForTest(adjacent);
				int plantCell = 14;
				putPlant(level, plantCell, new Firebloom());
				boss.setRhythmStateForTest(HuntressBoss.CombatStep.MOVE,
						HuntressBoss.GaleState.HUNTING,
						HuntressBoss.PlantHuntState.SELECTED,
						-1, -1, 0, escapeCooldown > 0, adjacent.id(),
						escapeCooldown, plantCell, 5, 0);
				boss.resetFieldOfView();
				int originalPos = boss.pos;

				assertTrue(boss.actForTest());

				assertEquals(originalPos, boss.pos);
				assertEquals(HuntressBoss.CombatStep.MOVE, boss.combatStep());
				assertEquals(1, boss.attackedTargets.size());
				assertSame(adjacent, boss.attackedTargets.get(0));
				assertTrue(boss.contactArmed());
				Actor.remove(adjacent);
				Actor.remove(boss);
				registeredActors.remove(adjacent);
				registeredActors.remove(boss);
			}
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void adjacentInvisibleEnemyCannotBeMeleeAttacked() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = rhythmLevel(11, 7);
			Dungeon.hero = null;
			TestHuntress boss = contactBossAt(34);
			TestTarget adjacent = hostileTargetAt(35);
			adjacent.invisible = 1;
			registerActor(boss, registeredActors);
			registerActor(adjacent, registeredActors);
			boss.followTargetForTest(adjacent);
			boss.resetFieldOfView();

			assertTrue(boss.actForTest());

			assertTrue(boss.attackedTargets.isEmpty());
			assertFalse(boss.contactArmed());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void selectedOrdinaryRemoteHitUsesSharedNatureWrathSeamOnlyAtRollZero() {
		TestHuntress boss = new TestHuntress();
		assertTrue(boss.enterWardenPhase());
		boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
				HuntressBoss.GaleState.HUNTING,
				HuntressBoss.PlantHuntState.SELECTED,
				-1, -1, 0, false, -1, 0, 7, 5, 0);
		TestTarget target = new TestTarget();
		boss.natureWrathRoll = 0;

		boss.attackProc(target, 1);
		assertEquals(1, boss.natureWrathActivations);
		assertEquals(2, boss.lastNatureWrathIndex);

		boss.natureWrathRoll = 1;
		boss.attackProc(target, 1);
		assertEquals(1, boss.natureWrathActivations);
		boss.setPrivateBoolean("meleeAttack", true);
		boss.natureWrathRoll = 0;
		boss.attackProc(target, 1);
		assertEquals(1, boss.natureWrathActivations);
		boss.setPrivateBoolean("meleeAttack", false);
		boss.setPrivateBoolean("galeShot", true);
		boss.attackProc(target, 1);
		assertEquals(1, boss.natureWrathActivations);
	}

	@Test
	public void removingTheMarkedPlantSchedulesExactlyOneLostTrackAction() {
		Level previousLevel = Dungeon.level;
		try {
			Dungeon.level = testLevel(9, 5);
			TestHuntress boss = new TestHuntress();
			boss.pos = 20;
			assertTrue(boss.enterWardenPhase());
			boss.wakeForTest();
			boss.setPrivateBoolean("encounterDelay", false);
			boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
					HuntressBoss.GaleState.HUNTING,
					HuntressBoss.PlantHuntState.SELECTED,
					-1, -1, 0, false, -1, 0, 17, 4, 0);

			boss.onPlantRemoved(17);
			boss.onPlantRemoved(17);
			assertEquals(HuntressBoss.PlantHuntState.LOST_TRACK,
					boss.plantHuntState());
			assertEquals(1, boss.lostTrackAnnouncements);
			int spendsBefore = boss.spendCalls;

			assertTrue(boss.actForTest());
			assertEquals(spendsBefore + 1, boss.spendCalls);
			assertEquals(HuntressBoss.PlantHuntState.GRACE,
					boss.plantHuntState());
			assertEquals(HuntressBoss.PLANT_HUNT_GRACE, boss.plantGraceTurns());
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void adjacentBlockedEscapeCannotStarveDuePlantRhythmActions() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			for (boolean cooldownBlocked : new boolean[]{false, true}) {
				for (HuntressBoss.PlantHuntState plantState
						: new HuntressBoss.PlantHuntState[]{
						HuntressBoss.PlantHuntState.LOST_TRACK,
						HuntressBoss.PlantHuntState.GRACE,
						HuntressBoss.PlantHuntState.SELECTED}) {
					RhythmLevel level = rhythmLevel(11, 7);
					Dungeon.level = level;
					Dungeon.hero = null;
					putPlant(level, 18, new Firebloom());
					TestHuntress boss = contactBossAt(34);
					assertTrue(boss.enterWardenPhase());
					boss.setPrivateBoolean("encounterDelay", false);
					TestTarget target = hostileTargetAt(35);
					registerActor(boss, registeredActors);
					registerActor(target, registeredActors);
					boss.followTargetForTest(target);
					boss.fadeleafTeleportSucceeds = false;
					boss.bossOnlyEscapeSucceeds = false;
					boss.setRhythmStateForTest(HuntressBoss.CombatStep.SHOOT,
							HuntressBoss.GaleState.HUNTING, plantState,
							-1, -1, 0, true, target.id(), cooldownBlocked ? 2 : 0,
							plantState == HuntressBoss.PlantHuntState.SELECTED ? 18 : -1,
							0, 0);

					assertTrue(boss.actForTest());
					assertTrue(boss.attackedTargets.isEmpty());
					if (plantState == HuntressBoss.PlantHuntState.GRACE) {
						assertEquals(HuntressBoss.PlantHuntState.SELECTED,
								boss.plantHuntState());
						assertEquals(HuntressBoss.PLANT_HUNT_DURATION,
								boss.plantHuntTurns());
					} else {
						assertEquals(HuntressBoss.PlantHuntState.GRACE,
								boss.plantHuntState());
						assertEquals(HuntressBoss.PLANT_HUNT_GRACE,
								boss.plantGraceTurns());
					}
					assertEquals(cooldownBlocked
							|| plantState == HuntressBoss.PlantHuntState.LOST_TRACK
							? 0 : 1, boss.bossOnlyEscapes);

					removeRegisteredActors(registeredActors);
				}
			}
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void realControlActsPauseEveryPlantCounterAndPreserveCombatState() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			TestTarget sameAlignment = hostileTargetAt(41);
			sameAlignment.alignment = Char.Alignment.ENEMY;
			registerActor(sameAlignment, registeredActors);
			Dungeon.level.mobs.add(sameAlignment);

			for (int control = 0; control < 4; control++) {
				TestHuntress boss = contactBossAt(40);
				boss.alignment = Char.Alignment.ENEMY;
				boss.headlessMovement = true;
				assertTrue(boss.enterWardenPhase());
				boss.setPrivateBoolean("encounterDelay", false);
				boss.followTargetForTest(sameAlignment);
				boss.setRhythmStateForTest(HuntressBoss.CombatStep.MOVE,
						HuntressBoss.GaleState.AIMED,
						HuntressBoss.PlantHuntState.SELECTED,
						40, 45, 0, true, sameAlignment.id(), 3, 17, 4, 0);
				registerActor(boss, registeredActors);
				if (control == 0) {
					boss.paralysed = 1;
				} else if (control == 1) {
					boss.state = boss.SLEEPING;
				} else if (control == 2) {
					boss.state = boss.FLEEING;
				} else {
					boss.confusedForCustomActions = true;
				}

				assertTrue(boss.actForTest());
				assertEquals(HuntressBoss.PlantHuntState.SELECTED,
						boss.plantHuntState());
				assertEquals(4, boss.plantHuntTurns());
				assertEquals(17, boss.markedPlantCell());
				assertEquals(HuntressBoss.GaleState.AIMED, boss.galeState());
				assertEquals(40, boss.galeAimOrigin());
				assertEquals(45, boss.galeAimTarget());
				assertEquals(3, boss.escapeCooldown());
				assertTrue(boss.contactArmed());
			}
		} finally {
			if (Dungeon.level != null) {
				Dungeon.level.mobs.clear();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void realControlBuffMatrixPausesPlantHuntEscapeAndAimedLock() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(13, 7);
			Dungeon.hero = null;
			for (HuntressBoss.PlantHuntState plantState
					: new HuntressBoss.PlantHuntState[]{
					HuntressBoss.PlantHuntState.GRACE,
					HuntressBoss.PlantHuntState.SELECTED}) {
				for (int control = 0; control < 7; control++) {
					TestHuntress boss = contactBossAt(40);
					boss.sprite = null;
					assertTrue(boss.enterWardenPhase());
					boss.setPrivateBoolean("encounterDelay", false);
					boss.skipBaseEnemySelection = true;
					boss.setRhythmStateForTest(HuntressBoss.CombatStep.MOVE,
							HuntressBoss.GaleState.AIMED, plantState,
							40, 45, 0, true, Integer.MAX_VALUE, 3,
							plantState == HuntressBoss.PlantHuntState.SELECTED ? 17 : -1,
							plantState == HuntressBoss.PlantHuntState.SELECTED ? 4 : 0,
							plantState == HuntressBoss.PlantHuntState.GRACE ? 2 : 0);
					registerActor(boss, registeredActors);

					if (control == 0) {
						assertTrue(new Paralysis().attachTo(boss));
					} else if (control == 1) {
						assertTrue(new Frost().attachTo(boss));
					} else if (control == 2) {
						assertTrue(new Sleep().attachTo(boss));
					} else if (control == 3) {
						assertTrue(new MagicalSleep().attachTo(boss));
					} else if (control == 4) {
						boss.state = boss.FLEEING;
					} else if (control == 5) {
						assertTrue(new Amok().attachTo(boss));
					} else {
						assertTrue(new Feint.AfterImage.FeintConfusion().attachTo(boss));
					}
					boss.sprite = new HeadlessCharSprite();

					assertTrue(boss.actForTest());
					assertEquals(HuntressBoss.GaleState.AIMED, boss.galeState());
					assertEquals(40, boss.galeAimOrigin());
					assertEquals(45, boss.galeAimTarget());
					assertEquals(3, boss.escapeCooldown());
					assertTrue(boss.contactArmed());
					assertEquals(plantState, boss.plantHuntState());
					assertEquals(plantState == HuntressBoss.PlantHuntState.SELECTED ? 4 : 0,
							boss.plantHuntTurns());
					assertEquals(plantState == HuntressBoss.PlantHuntState.GRACE ? 2 : 0,
							boss.plantGraceTurns());
					Actor.remove(boss);
					registeredActors.remove(boss);
				}
			}
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void phaseTransitionUpdatesBossPresentation() {
		TestHuntress boss = new TestHuntress();

		assertEquals(HuntressBoss.Phase.SNIPER, boss.phase());
		assertEquals(15, boss.viewDistance);
		assertEquals(HuntressBossSprite.class, boss.spriteClass);

		boss.setGaleTurnsRemainingForTest(2);
		boss.setGaleTargetForTest(17);
		assertTrue(boss.enterWardenPhase());

		assertEquals(HuntressBoss.Phase.WARDEN, boss.phase());
		assertEquals(HuntressBoss.GALE_INTERVAL, boss.galeTurnsRemaining());
		assertEquals(-1, boss.galeTargetForTest());
		assertFalse(boss.enterWardenPhase());
	}

	@Test
	public void normalAndGaleProjectilesUseNonSpinningItemTypes() {
		assertEquals(SpiritBow.SpiritArrow.class, HuntressBoss.projectileClassFor(false));
		assertEquals(Dart.class, HuntressBoss.projectileClassFor(true));
	}

	@Test
	public void distractingHawkOnlyDealsOneDamageAndIsBossMinion() {
		HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();

		assertEquals(1, hawk.damageRoll());
		assertEquals(1, hawk.damageAfterArmor(0));
		assertEquals(1, hawk.damageAfterArmor(7));
		assertEquals(0, hawk.EXP);
		assertEquals(Char.Alignment.ENEMY, hawk.alignment);
		assertTrue(hawk.properties().contains(Char.Property.BOSS_MINION));
	}

	@Test
	public void distractingHawkBlindsAndCripplesOnHit() {
		assertEquals(2f, HuntressBoss.DistractingHawk.DISTRACTION_DURATION, 0f);
		assertEquals(Blindness.class, HuntressBoss.DistractingHawk.distractionEffects()[0]);
		assertEquals(Cripple.class, HuntressBoss.DistractingHawk.distractionEffects()[1]);
	}

	@Test
	public void distractingHawkAttackProcAppliesItsFullHitContract() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		NoResistanceTarget target = null;
		Blindness blindness = null;
		Cripple cripple = null;
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			target = new NoResistanceTarget();
			target.pos = 32;
			blindness = new Blindness();
			cripple = new Cripple();
			assertTrue(blindness.attachTo(target));
			assertTrue(cripple.attachTo(target));
			blindness.clearTime();
			cripple.clearTime();
			assertEquals(0f, blindness.cooldown(), 0f);
			assertEquals(0f, cripple.cooldown(), 0f);
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);

			int damage = hawk.attackProcForTest(target, 17);

			assertEquals(1, damage);
			assertSame(blindness, target.buff(Blindness.class));
			assertSame(cripple, target.buff(Cripple.class));
			assertEquals(HuntressBoss.DistractingHawk.DISTRACTION_DURATION,
					blindness.cooldown(), 0f);
			assertEquals(HuntressBoss.DistractingHawk.DISTRACTION_DURATION,
					cripple.cooldown(), 0f);
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
					hawk.hawkMode());
			assertEquals(target.id(), hawk.retreatTargetId());
		} finally {
			Dungeon.hero = null;
			try {
				try {
					if (blindness != null && target != null
							&& target.buff(Blindness.class) == blindness) {
						blindness.detach();
					}
				} finally {
					try {
						if (cripple != null && target != null
								&& target.buff(Cripple.class) == cripple) {
							cripple.detach();
						}
					} finally {
						removeRegisteredActors(registeredActors);
					}
				}
			} finally {
				Dungeon.level = previousLevel;
				Dungeon.hero = previousHero;
			}
		}
	}

	@Test
	public void distractingHawkTargetEligibilityRejectsEveryInvalidCondition() {
		assertTrue(HuntressBoss.DistractingHawk.eligibleTarget(
				true, true, true, false, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				false, true, true, false, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				true, false, true, false, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				true, true, false, false, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				true, true, true, true, false));
		assertFalse(HuntressBoss.DistractingHawk.eligibleTarget(
				true, true, true, false, true));
	}

	@Test
	public void distractingHawkTargetPreferenceIsNearestThenLowestActorId() {
		assertTrue(HuntressBoss.DistractingHawk.preferTarget(2, 20, 3, 10));
		assertFalse(HuntressBoss.DistractingHawk.preferTarget(4, 1, 3, 10));
		assertTrue(HuntressBoss.DistractingHawk.preferTarget(3, 9, 3, 10));
		assertFalse(HuntressBoss.DistractingHawk.preferTarget(3, 11, 3, 10));
	}

	@Test
	public void distractingHawkModeTransitionsAndRetreatBoundariesMatchDesign() {
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
				HuntressBoss.DistractingHawk.modeAfterHit());
		assertTrue(HuntressBoss.DistractingHawk.shouldRetreat(true, 1));
		assertFalse(HuntressBoss.DistractingHawk.shouldRetreat(false, 1));
		assertFalse(HuntressBoss.DistractingHawk.shouldRetreat(true, 2));
		assertFalse(HuntressBoss.DistractingHawk.inRetreatBand(1));
		assertTrue(HuntressBoss.DistractingHawk.inRetreatBand(2));
		assertTrue(HuntressBoss.DistractingHawk.inRetreatBand(4));
		assertFalse(HuntressBoss.DistractingHawk.inRetreatBand(5));
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.SEEKING,
				HuntressBoss.DistractingHawk.modeAfterRetreat(true));
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
				HuntressBoss.DistractingHawk.modeAfterRetreat(false));
	}

	@Test
	public void distractingHawkRetreatStateRoundTripsAndOldBundlesUseDefaults() {
		HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
		TestTarget target = new TestTarget();
		hawk.beginRetreatFrom(target);
		Bundle saved = new Bundle();
		hawk.storeInBundle(saved);

		HuntressBoss.DistractingHawk restored = new HuntressBoss.DistractingHawk();
		restored.restoreFromBundle(saved);
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
				restored.hawkMode());
		assertEquals(target.id(), restored.retreatTargetId());

		HuntressBoss.DistractingHawk legacy = new HuntressBoss.DistractingHawk();
		legacy.restoreFromBundle(new Bundle());
		assertEquals(HuntressBoss.DistractingHawk.HawkMode.SEEKING,
				legacy.hawkMode());
		assertEquals(-1, legacy.retreatTargetId());
	}

	@Test
	public void distractingHawkDropsAnUnregisteredRestoredRetreatTarget() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			HuntressBoss.DistractingHawk saved = new HuntressBoss.DistractingHawk();
			saved.beginRetreatFrom(new TestTarget());
			Bundle bundle = new Bundle();
			saved.storeInBundle(bundle);
			bundle.put("retreat_target_id", Integer.MAX_VALUE);
			assertNull(Actor.findCharById(Integer.MAX_VALUE));

			TestHawk restored = new TestHawk();
			restored.restoreFromBundle(bundle);
			restored.pos = 31;
			restored.setAllVisibleForTest();
			Arrays.fill(Dungeon.level.passable, false);
			Dungeon.level.passable[32] = true;
			registerActor(restored, registeredActors);
			restored.state = restored.HUNTING;

			restored.HUNTING.act(false, false);

			assertEquals(-1, restored.retreatTargetId());
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
					restored.hawkMode());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void distractingHawkSelectsLowestIdWhenEligibleTargetsAreEquidistant() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			hawk.alignment = Char.Alignment.ENEMY1;
			TestTarget lowerId = new TestTarget();
			lowerId.pos = 21;
			lowerId.alignment = Char.Alignment.ENEMY2;
			TestTarget higherId = new TestTarget();
			higherId.pos = 23;
			higherId.alignment = Char.Alignment.ENEMY2;
			registerActor(hawk, registeredActors);
			registerActor(lowerId, registeredActors);
			registerActor(higherId, registeredActors);
			hawk.setVisibleForTest(lowerId.pos, higherId.pos);
			Arrays.fill(Dungeon.level.passable, false);
			hawk.state = hawk.HUNTING;

			hawk.HUNTING.act(false, false);

			assertSame(lowerId, hawk.enemyForTest());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void distractingHawkWaitsInsteadOfAttackingItsCrippledRetreatTarget() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			hawk.alignment = Char.Alignment.ENEMY1;
			TestTarget target = new TestTarget();
			target.pos = 33;
			target.alignment = Char.Alignment.ENEMY2;
			assertTrue(new Cripple().attachTo(target));
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);
			hawk.setVisibleForTest(target.pos);
			Arrays.fill(Dungeon.level.passable, false);
			hawk.beginRetreatFrom(target);
			hawk.state = hawk.HUNTING;

			hawk.HUNTING.act(true, false);

			assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
					hawk.hawkMode());
			assertEquals(0, hawk.attacks);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void distractingHawkRetreatsOneStepAtDistanceOneThenStopsAtTwo() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			hawk.alignment = Char.Alignment.ENEMY1;
			TestTarget target = new TestTarget();
			target.pos = 32;
			target.alignment = Char.Alignment.ENEMY2;
			assertTrue(new Cripple().attachTo(target));
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);
			hawk.setAllVisibleForTest();
			hawk.beginRetreatFrom(target);
			hawk.state = hawk.HUNTING;

			hawk.HUNTING.act(true, false);

			assertEquals(2, Dungeon.level.distance(hawk.pos, target.pos));
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
					hawk.hawkMode());

			hawk.HUNTING.act(true, false);

			assertTrue(HuntressBoss.DistractingHawk.inRetreatBand(
					Dungeon.level.distance(hawk.pos, target.pos)));
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
					hawk.hawkMode());
			assertEquals(0, hawk.attacks);
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void blindedHawkCannotAcquireANewTargetWhileSeekingOrWaiting() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		Blindness blindness = null;
		try {
			Dungeon.level = testLevel(9, 7);
			Dungeon.hero = null;
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			hawk.alignment = Char.Alignment.ENEMY1;
			TestTarget target = new TestTarget();
			target.pos = 33;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);
			hawk.setAllVisibleForTest();
			blindness = new Blindness();
			assertTrue(blindness.attachTo(hawk));
			hawk.state = hawk.HUNTING;

			hawk.HUNTING.act(true, false);

			assertNull(hawk.enemyForTest());
			assertEquals(0, hawk.attacks);
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
					hawk.hawkMode());

			hawk.state = hawk.HUNTING;
			hawk.HUNTING.act(true, false);

			assertNull(hawk.enemyForTest());
			assertEquals(0, hawk.attacks);
		} finally {
			if (blindness != null && blindness.target != null) {
				blindness.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void blindedHawkFinishesAnAlreadyStartedRetreatBeforeWaiting() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		Blindness blindness = null;
		try {
			Dungeon.level = testLevel(9, 7);
			Dungeon.hero = null;
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			hawk.alignment = Char.Alignment.ENEMY1;
			TestTarget target = new TestTarget();
			target.pos = 32;
			target.alignment = Char.Alignment.ENEMY2;
			registerActor(hawk, registeredActors);
			registerActor(target, registeredActors);
			hawk.setAllVisibleForTest();
			hawk.beginRetreatFrom(target);
			blindness = new Blindness();
			assertTrue(blindness.attachTo(hawk));
			hawk.state = hawk.HUNTING;

			hawk.HUNTING.act(true, false);

			assertEquals(2, Dungeon.level.distance(hawk.pos, target.pos));
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
					hawk.hawkMode());

			hawk.HUNTING.act(true, false);

			assertEquals(HuntressBoss.DistractingHawk.HawkMode.WAITING,
					hawk.hawkMode());
			assertEquals(0, hawk.attacks);
		} finally {
			if (blindness != null && blindness.target != null) {
				blindness.detach();
			}
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void distractingHawkCustomAiLivesOnlyInsideBaseHuntingState() {
		HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
		assertEquals(Mob.Hunting.class, hawk.HUNTING.getClass().getSuperclass());
		try {
			HuntressBoss.DistractingHawk.class.getDeclaredMethod("act");
			throw new AssertionError("DistractingHawk must leave Mob.act control priority intact");
		} catch (NoSuchMethodException expected) {
			// Expected: paralysis, fear, confusion and sleep remain owned by Mob.act/state.
		}
	}

	@Test
	public void distractingHawkAmokUsesBaseTargetingWithoutAdvancingHawkMode() {
		Level previousLevel = Dungeon.level;
		Hero previousHero = Dungeon.hero;
		ArrayList<Actor> registeredActors = new ArrayList<>();
		try {
			Dungeon.level = testLevel(9, 7);
			TestHawk hawk = new TestHawk();
			hawk.pos = 31;
			TestTarget sameAlignment = new TestTarget();
			sameAlignment.pos = 32;
			sameAlignment.alignment = Char.Alignment.ENEMY;
			TestTarget normallyHostile = new TestTarget();
			normallyHostile.pos = 33;
			normallyHostile.alignment = Char.Alignment.ALLY;
			hawk.beginRetreatFrom(normallyHostile);
			int retreatId = hawk.retreatTargetId();
			assertTrue(new Amok().attachTo(hawk));
			registerActor(hawk, registeredActors);
			registerActor(sameAlignment, registeredActors);
			registerActor(normallyHostile, registeredActors);
			Dungeon.level.mobs.add(sameAlignment);
			Dungeon.level.mobs.add(normallyHostile);
			hawk.setAllVisibleForTest();
			Arrays.fill(Dungeon.level.passable, false);
			hawk.state = hawk.HUNTING;

			assertSame(sameAlignment, hawk.chooseEnemyForTest());
			hawk.chooseEnemyAndHuntForTest();

			assertSame(sameAlignment, hawk.attackedTarget);
			assertEquals(HuntressBoss.DistractingHawk.HawkMode.RETREATING,
					hawk.hawkMode());
			assertEquals(retreatId, hawk.retreatTargetId());
		} finally {
			removeRegisteredActors(registeredActors);
			Dungeon.level = previousLevel;
			Dungeon.hero = previousHero;
		}
	}

	@Test
	public void fadeleafMapsToAdjustedTeleportBoon() {
		assertEquals(HuntressBoss.WardenBoon.FADELEAF,
				HuntressBoss.boonForPlant(new Fadeleaf()));
	}

	@Test
	public void oneTimeHawkSpawnsRemainIndependentOfPlantBoons() {
		assertEquals(1, HuntressBoss.hawksSpawnedAtFightStart());
		assertEquals(1, HuntressBoss.hawksSpawnedAtWardenTransition());
	}

	@Test
	public void baseMobControlStatesPreemptHuntressCustomActions() {
		assertTrue(HuntressBoss.shouldDeferCustomActions(1, false, false, false));
		assertTrue(HuntressBoss.shouldDeferCustomActions(0, true, false, false));
		assertTrue(HuntressBoss.shouldDeferCustomActions(0, false, true, false));
		assertTrue(HuntressBoss.shouldDeferCustomActions(0, false, false, true));
		assertFalse(HuntressBoss.shouldDeferCustomActions(0, false, false, false));
	}

	@Test
	public void successfulFireAbsorptionStartsCooldownAndKeepsFiveTurnImbue() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);

		assertFalse(new Burning().attachTo(boss));

		assertNull(boss.buff(Burning.class));
		assertNotNull(boss.buff(FireImbue.class));
		Bundle fireImbueState = new Bundle();
		boss.buff(FireImbue.class).storeInBundle(fireImbueState);
		assertEquals(5f, fireImbueState.getFloat("left"), 0f);
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		assertNotNull(cooldown);
		assertEquals(HuntressBoss.FIRE_ABSORPTION_COOLDOWN, cooldown.cooldown(), 0f);
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void failedFireAbsorptionStillRemovesBurningAndStartsCooldown() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(1);

		assertFalse(new Burning().attachTo(boss));

		assertNull(boss.buff(Burning.class));
		assertNull(boss.buff(FireImbue.class));
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		assertNotNull(cooldown);
		assertEquals(20f, cooldown.cooldown(), 0f);
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void burningDuringAbsorptionCooldownAttachesNormallyWithoutRefreshingIt() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(1);
		assertFalse(new Burning().attachTo(boss));
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		float remaining = cooldown.cooldown();

		Burning secondBurning = new Burning();
		assertTrue(secondBurning.attachTo(boss));

		assertSame(secondBurning, boss.buff(Burning.class));
		assertSame(cooldown, boss.buff(HuntressBoss.FireAbsorptionCooldown.class));
		assertEquals(remaining, cooldown.cooldown(), 0f);
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void cooldownAllowsBurningDespiteTemporaryFireImbueImmunity() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);
		assertFalse(new Burning().attachTo(boss));
		assertNotNull(boss.buff(FireImbue.class));
		assertNotNull(boss.buff(HuntressBoss.FireAbsorptionCooldown.class));

		Burning secondBurning = new Burning();
		assertTrue(secondBurning.attachTo(boss));

		assertSame(secondBurning, boss.buff(Burning.class));
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void cooldownPreservesOtherBurningImmunitySources() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);
		assertFalse(new Burning().attachTo(boss));
		assertNotNull(boss.buff(FireImbue.class));
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		float remaining = cooldown.cooldown();
		boss.addProperties(Char.Property.FIERY);

		assertFalse(new Burning().attachTo(boss));

		assertNull(boss.buff(Burning.class));
		assertTrue(boss.isImmune(Burning.class));
		assertSame(cooldown, boss.buff(HuntressBoss.FireAbsorptionCooldown.class));
		assertEquals(remaining, cooldown.cooldown(), 0f);
		assertEquals(1, boss.absorptionRolls());
	}

	@Test
	public void fireAbsorptionCooldownUsesIndependentFlavourBuffWorldTime() {
		assertEquals(20f, HuntressBoss.FIRE_ABSORPTION_COOLDOWN, 0f);
		assertEquals(FlavourBuff.class,
				HuntressBoss.FireAbsorptionCooldown.class.getSuperclass());
		HuntressBoss.FireAbsorptionCooldown marker =
				new HuntressBoss.FireAbsorptionCooldown();
		assertEquals(Buff.buffType.NEUTRAL, marker.type);
		assertFalse(marker.announced);

		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(1);
		assertFalse(new Burning().attachTo(boss));
		HuntressBoss.FireAbsorptionCooldown cooldown =
				boss.buff(HuntressBoss.FireAbsorptionCooldown.class);
		boss.setControlledForTest();

		assertTrue(cooldown.act());

		assertNull(boss.buff(HuntressBoss.FireAbsorptionCooldown.class));
	}

	@Test
	public void nonBurningFireBuffDoesNotStartAbsorptionCooldown() {
		DeterministicFireAbsorptionHuntress boss =
				new DeterministicFireAbsorptionHuntress(0);

		assertTrue(new FireImbue().attachTo(boss));

		assertNotNull(boss.buff(FireImbue.class));
		assertNull(boss.buff(HuntressBoss.FireAbsorptionCooldown.class));
		assertEquals(0, boss.absorptionRolls());
	}

	private static class DeterministicFireAbsorptionHuntress extends HuntressBoss {

		private final int roll;
		private int absorptionRolls;

		private DeterministicFireAbsorptionHuntress(int roll) {
			this.roll = roll;
		}

		@Override
		protected int fireAbsorptionRoll() {
			absorptionRolls++;
			return roll;
		}

		private int absorptionRolls() {
			return absorptionRolls;
		}

		private void setControlledForTest() {
			paralysed = 1;
			state = SLEEPING;
		}
	}

	private static class WardenTransitionHuntress extends HuntressBoss {

		private int wardenHawkSpawnRequests;

		private void crossHalfHealthWithRealDamageForTest() {
			HT = 100;
			HP = 51;
			damage(1, this);
		}

		private void damageAgainForTest() {
			damage(1, this);
		}

		@Override
		public float resist(Class effect) {
			return 1f;
		}

		@Override
		protected void announceWardenTransition() {
			// Headless tests verify transition dispatch, not UI presentation.
		}

		@Override
		protected void onWardenPhaseStarted() {
			wardenHawkSpawnRequests++;
		}
	}

	private static class ChallengeHawkHuntress extends HuntressBoss {

		private int challengeHawkSpawnRequests;

		private void takeThirtyDamage() {
			damage(30, this);
		}

		@Override
		public float resist(Class effect) {
			return 1f;
		}

		@Override
		protected void summonChallengeHawk() {
			challengeHawkSpawnRequests++;
		}

		@Override
		protected void announceWardenTransition() {
			// Headless tests verify challenge dispatch, not UI presentation.
		}

		@Override
		protected void onWardenPhaseStarted() {
			// The fixed phase-transition hawk is independent of threshold hawks.
		}
	}

	private static TestLevel testLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.map, Terrain.EMPTY);
		level.buildFlagMaps();
		return level;
	}

	private static RhythmLevel rhythmLevel(int width, int height) {
		RhythmLevel level = new RhythmLevel();
		level.setSize(width, height);
		Arrays.fill(level.map, Terrain.EMPTY);
		level.buildFlagMaps();
		return level;
	}

	private static void putPlant(RhythmLevel level, int cell, Plant plant) {
		plant.pos = cell;
		level.plants.put(cell, plant);
	}

	private static TestHuntress contactBossAt(int cell) {
		TestHuntress boss = new TestHuntress();
		boss.pos = cell;
		boss.alignment = Char.Alignment.ENEMY1;
		boss.state = boss.HUNTING;
		boss.sprite = new HeadlessCharSprite();
		boss.immediateBaseAttack = true;
		boss.setPrivateBoolean("encounterDelay", false);
		boss.setGaleTurnsRemainingForTest(HuntressBoss.GALE_INTERVAL);
		return boss;
	}

	private static TestTarget hostileTargetAt(int cell) {
		TestTarget target = new TestTarget();
		target.pos = cell;
		target.alignment = Char.Alignment.ENEMY2;
		return target;
	}

	private static <T extends Actor> T registerActor(T actor,
			ArrayList<Actor> registeredActors) {
		Actor.add(actor);
		registeredActors.add(actor);
		return actor;
	}

	private static void removeRegisteredActors(ArrayList<Actor> registeredActors) {
		for (int i = registeredActors.size() - 1; i >= 0; i--) {
			Actor.remove(registeredActors.get(i));
		}
	}

	private static final class PathFinderState {
		private final ArrayList<Field> fields;
		private final ArrayList<Object> values;

		private PathFinderState(ArrayList<Field> fields, ArrayList<Object> values) {
			this.fields = fields;
			this.values = values;
		}

		static PathFinderState capture() {
			ArrayList<Field> fields = new ArrayList<>();
			ArrayList<Object> values = new ArrayList<>();
			try {
				for (Field field : PathFinder.class.getDeclaredFields()) {
					int modifiers = field.getModifiers();
					if (!Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
						continue;
					}
					field.setAccessible(true);
					fields.add(field);
					values.add(field.get(null));
				}
				return new PathFinderState(fields, values);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		void restore() {
			try {
				for (int i = 0; i < fields.size(); i++) {
					fields.get(i).set(null, values.get(i));
				}
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}
	}

	private static final class GaleSnapshot {
		private final HuntressBoss.GaleState state;
		private final int remaining;
		private final int aimOrigin;
		private final int aimTarget;
		private final int aimDx;
		private final int aimDy;
		private final int recoveryTurns;

		private GaleSnapshot(TestHuntress boss) {
			state = boss.galeState();
			remaining = boss.galeTurnsRemaining();
			aimOrigin = boss.galeAimOrigin();
			aimTarget = boss.galeAimTarget();
			aimDx = boss.galeAimDx();
			aimDy = boss.galeAimDy();
			recoveryTurns = boss.galeRecoveryTurns();
		}

		private void assertUnchanged(TestHuntress boss) {
			assertEquals(state, boss.galeState());
			assertEquals(remaining, boss.galeTurnsRemaining());
			assertEquals(aimOrigin, boss.galeAimOrigin());
			assertEquals(aimTarget, boss.galeAimTarget());
			assertEquals(aimDx, boss.galeAimDx());
			assertEquals(aimDy, boss.galeAimDy());
			assertEquals(recoveryTurns, boss.galeRecoveryTurns());
		}
	}

	private static class TestHuntress extends HuntressBoss {

		private int galeAims;
		private int galeRecoveryAnnouncements;
		private int contactWarningAnnouncements;
		private int plantMarkedAnnouncements;
		private int natureHuntAnnouncements;
		private int closerAttempts;
		private final ArrayList<Char> attackedTargets = new ArrayList<>();
		private int fadeleafTeleports;
		private boolean fadeleafTeleportSucceeds;
		private boolean skipBaseEnemySelection;
		private boolean galeAimBlinded;
		private boolean immediateBaseAttack;
		private boolean headlessMovement;
		private boolean bossOnlyEscapeSucceeds;
		private int bossOnlyEscapes;
		private Char lastEscapeTarget;
		private boolean fadeleafBoonSucceeds;
		private int spendCalls;
		private float spentTime;
		private boolean bossMovement;
		private int natureWrathRoll;
		private int natureWrathActivations;
		private int lastNatureWrathIndex = -1;
		private boolean plantMarkBlinded;
		private int boonAnnouncements;
		private int activeBoonHitApplications;
		private int lostTrackAnnouncements;
		private boolean confusedForCustomActions;

		@Override
		protected Char chooseEnemy() {
			return skipBaseEnemySelection ? null : super.chooseEnemy();
		}

		private void prepareForGaleAct(int remaining) {
			state = HUNTING;
			sprite = new HeadlessCharSprite();
			setPrivateBoolean("encounterDelay", false);
			setGaleTurnsRemainingForTest(remaining);
			setPrivateObject("galeState", HuntressBoss.GaleState.HUNTING);
			setPrivateInt("galeAimDx", 0);
			setPrivateInt("galeAimDy", 0);
			setPrivateInt("galeAimOrigin", -1);
			setPrivateInt("galeAimTarget", -1);
			setPrivateInt("galeRecoveryTurns", 0);
			setGaleTargetForTest(-1);
		}

		private int normalShotRangeForTest() {
			return normalShotRange();
		}

		private void prepareAimedGale(int targetCell) {
			prepareForGaleAct(0);
			int width = Dungeon.level.width();
			setPrivateObject("galeState", HuntressBoss.GaleState.AIMED);
			setPrivateInt("galeAimOrigin", pos);
			setPrivateInt("galeAimTarget", targetCell);
			setPrivateInt("galeAimDx", targetCell % width - pos % width);
			setPrivateInt("galeAimDy", targetCell / width - pos / width);
		}

		private void prepareRecoveringGale() {
			prepareForGaleAct(0);
			setPrivateObject("galeState", HuntressBoss.GaleState.RECOVERING);
			setPrivateInt("galeRecoveryTurns", HuntressBoss.GALE_RECOVERY_TURNS);
		}

		private void prepareGaleStageForTest(HuntressBoss.GaleState stage,
				int targetCell) {
			switch (stage) {
				case AIMED:
					prepareAimedGale(targetCell);
					break;
				case RECOVERING:
					prepareRecoveringGale();
					break;
				case HUNTING:
				default:
					prepareForGaleAct(3);
					break;
			}
		}

		private void followTargetForTest(Char target) {
			enemy = target;
			this.target = target.pos;
			state = HUNTING;
		}

		private boolean actForTest() {
			return act();
		}

		private void wakeForTest() {
			state = WANDERING;
		}

		private void clearEnemyForTest(boolean wandering) {
			enemy = null;
			target = pos;
			state = wandering ? WANDERING : HUNTING;
		}

		private void resetSpendTracking() {
			spendCalls = 0;
			spentTime = 0f;
		}

		private void resetFieldOfView() {
			fieldOfView = new boolean[Dungeon.level.length()];
		}

		private void setVisibleForTest(int cell) {
			fieldOfView[cell] = true;
		}

		private boolean bossCanSeeForTest(Char target) {
			return bossCanSee(target);
		}

		private boolean isHawkRelayedForTest(Char target) {
			return isHawkRelayed(target);
		}

		private Char chooseEnemyForTest() {
			return chooseEnemy();
		}

		private Char enemyForTest() {
			return enemy;
		}

		private void setEnemyForTest(Char target) {
			enemy = target;
		}

		private void setGaleTurnsRemainingForTest(int remaining) {
			setPrivateInt("galeTurnsRemaining", remaining);
		}

		private void setCombatStepForTest(HuntressBoss.CombatStep step) {
			setPrivateObject("combatStep", step);
		}

		private boolean getCloserViaBossForTest(int targetCell) {
			return super.getCloser(targetCell);
		}

		private int nearestFirePositionByCoverForTest(Char target) {
			try {
				java.lang.reflect.Method method = HuntressBoss.class.getDeclaredMethod(
						"nearestFirePositionByCover", Char.class);
				method.setAccessible(true);
				return (Integer) method.invoke(this, target);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		private boolean canAttackForTest(Char target) {
			return super.canAttack(target);
		}

		private boolean doAttackViaBossForTest(Char target) {
			return super.doAttack(target);
		}

		private void useHeadlessMovementForTest() {
			headlessMovement = true;
		}

		private void useBossMovementForTest() {
			bossMovement = true;
		}

		private void setGaleTargetForTest(int cell) {
			setPrivateInt("galeTarget", cell);
		}

		private void setFadeleafEscapesUsedForTest(int uses) {
			setPrivateInt("fadeleafEscapesUsed", uses);
		}

		private void setEscapeCooldownForTest(int turns) {
			setPrivateInt("escapeCooldown", turns);
		}

		private void setActiveBoonForTest(HuntressBoss.WardenBoon boon, int turns) {
			setPrivateObject("activeBoon", boon);
			setPrivateInt("boonTurns", turns);
		}

		private void setRhythmStateForTest(HuntressBoss.CombatStep combatStep,
				HuntressBoss.GaleState galeState,
				HuntressBoss.PlantHuntState plantHuntState,
				int aimOrigin, int aimTarget, int recoveryTurns,
				boolean contactArmed, int contactTargetId, int escapeCooldown,
				int markedPlantCell, int plantHuntTurns, int plantGraceTurns) {
			setPrivateObject("combatStep", combatStep);
			setPrivateObject("galeState", galeState);
			setPrivateObject("plantHuntState", plantHuntState);
			int width = Dungeon.level == null ? 1 : Dungeon.level.width();
			setPrivateInt("galeAimDx", aimOrigin < 0 || aimTarget < 0
					? 0 : aimTarget % width - aimOrigin % width);
			setPrivateInt("galeAimDy", aimOrigin < 0 || aimTarget < 0
					? 0 : aimTarget / width - aimOrigin / width);
			setPrivateInt("galeAimOrigin", aimOrigin);
			setPrivateInt("galeAimTarget", aimTarget);
			setPrivateInt("galeRecoveryTurns", recoveryTurns);
			setPrivateBoolean("contactArmed", contactArmed);
			setPrivateInt("contactTargetId", contactTargetId);
			setPrivateInt("escapeCooldown", escapeCooldown);
			setPrivateInt("markedPlantCell", markedPlantCell);
			setPrivateInt("plantHuntTurns", plantHuntTurns);
			setPrivateInt("plantGraceTurns", plantGraceTurns);
		}

		private int galeTargetForTest() {
			return getPrivateInt("galeTarget");
		}

		private void setPrivateInt(String name, int value) {
			try {
				Field field = HuntressBoss.class.getDeclaredField(name);
				field.setAccessible(true);
				field.setInt(this, value);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		private int getPrivateInt(String name) {
			try {
				Field field = HuntressBoss.class.getDeclaredField(name);
				field.setAccessible(true);
				return field.getInt(this);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		private void setPrivateBoolean(String name, boolean value) {
			try {
				Field field = HuntressBoss.class.getDeclaredField(name);
				field.setAccessible(true);
				field.setBoolean(this, value);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		private void setPrivateObject(String name, Object value) {
			try {
				Field field = HuntressBoss.class.getDeclaredField(name);
				field.setAccessible(true);
				field.set(this, value);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti,
				DamageTag... damageTags) {
			attackedTargets.add(enemy);
			return false;
		}

		@Override
		protected void spend(float time) {
			spendCalls++;
			spentTime += time;
			super.spend(time);
		}

		@Override
		protected boolean getCloser(int target) {
			closerAttempts++;
			return bossMovement && super.getCloser(target);
		}

		@Override
		public void move(int step, boolean travelling) {
			if (headlessMovement) {
				pos = step;
			} else {
				super.move(step, travelling);
			}
		}

		@Override
		protected boolean moveSprite(int from, int to) {
			return headlessMovement || super.moveSprite(from, to);
		}

		@Override
		protected boolean doAttack(Char enemy) {
			if (immediateBaseAttack) {
				setPrivateBoolean("ordinaryActionStarted", true);
				attackedTargets.add(enemy);
				spend(attackDelay());
				return true;
			}
			if (phase() == Phase.WARDEN) {
				setPrivateBoolean("ordinaryActionStarted", true);
				attackedTargets.add(enemy);
				spend(attackDelay());
				return true;
			}
			return super.doAttack(enemy);
		}

		@Override
		protected void announceGaleAim() {
			galeAims++;
		}

		@Override
		protected void announceContactWarning() {
			contactWarningAnnouncements++;
		}

		@Override
		protected void announceGaleRecovery() {
			galeRecoveryAnnouncements++;
		}

		@Override
		protected void announcePlantMarked(int cell) {
			plantMarkedAnnouncements++;
		}

		@Override
		protected void announceNatureHunt() {
			natureHuntAnnouncements++;
		}

		@Override
		protected boolean isBlindedForGaleAim() {
			return galeAimBlinded || super.isBlindedForGaleAim();
		}

		@Override
		protected boolean isBlindedForPlantMark() {
			return plantMarkBlinded || super.isBlindedForPlantMark();
		}

		@Override
		protected boolean isBlindedForNormalShot() {
			return plantMarkBlinded || super.isBlindedForNormalShot();
		}

		@Override
		protected boolean isConfusedForCustomActionPriority() {
			return confusedForCustomActions
					|| super.isConfusedForCustomActionPriority();
		}

		@Override
		protected int baseAttackProc(Char enemy, int damage, DamageTag... damageTags) {
			return damage;
		}

		@Override
		public float resist(Class effect) {
			return 1f;
		}

		@Override
		protected boolean teleportBossAndTargetApart(Char target) {
			fadeleafTeleports++;
			return fadeleafTeleportSucceeds;
		}

		@Override
		protected boolean performBossOnlyEscape(Char target) {
			bossOnlyEscapes++;
			lastEscapeTarget = target;
			return bossOnlyEscapeSucceeds;
		}

		@Override
		protected boolean tryTriggerFadeleafBoon() {
			return fadeleafBoonSucceeds;
		}

		@Override
		protected int natureWrathRoll() {
			return natureWrathRoll;
		}

		@Override
		protected int natureWrathPlantIndex() {
			return 2;
		}

		@Override
		protected void activateNatureWrathPlant(Char enemy, int index) {
			natureWrathActivations++;
			lastNatureWrathIndex = index;
		}

		@Override
		protected void applyActiveBoonOnRemoteHit(Char enemy, WardenBoon boon) {
			activeBoonHitApplications++;
		}

		@Override
		protected void announceBoon(WardenBoon boon) {
			boonAnnouncements++;
		}

		@Override
		protected void announceLostTrack() {
			lostTrackAnnouncements++;
		}
	}

	private static Properties loadActorMessages(String fileName) throws IOException {
		return loadMessages("actors", fileName);
	}

	private static Properties loadMessages(String folder, String fileName)
			throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = Files.isDirectory(workingDirectory.resolve("core"))
				? workingDirectory.resolve("core") : workingDirectory;
		Path source = coreDirectory.resolve("src/main/assets/messages").resolve(folder)
				.resolve(fileName);
		Properties messages = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			messages.load(reader);
		}
		return messages;
	}

	private static class HeadlessCharSprite extends CharSprite {

		private HeadlessCharSprite() {
			visible = true;
		}

		@Override
		public void showLost() {
			// No scene exists in headless actor-cycle tests.
		}
	}

	private static class DefaultAnnouncementHuntress extends HuntressBoss {
		private void announceNatureHuntForTest() {
			announceNatureHunt();
		}

		@Override
		public void yell(String str) {
			// Isolate the default particle seam from the headless log backend.
		}
	}

	private static class DeferredZapSprite extends HeadlessCharSprite {

		private Callback callback;

		private DeferredZapSprite() {
			parent = new Group();
		}

		@Override
		public synchronized void zap(int cell, Callback callback) {
			this.callback = callback;
		}

		private void completeZapWithoutScene() {
			Callback pending = callback;
			callback = null;
			parent = null;
			assertNotNull(pending);
			pending.call();
		}

		private boolean hasPendingCallback() {
			return callback != null;
		}
	}

	private static class TestTarget extends Mob {

		{
			HP = HT = 20;
		}

		@Override
		public int damageRoll() {
			return 0;
		}

		@Override
		public int attackSkill(Char target) {
			return 0;
		}
	}

	private static class NoResistanceTarget extends TestTarget {

		@Override
		public float resist(Class effect) {
			return 1f;
		}
	}

	private static class TestHawk extends HuntressBoss.DistractingHawk {

		private int attacks;
		private Char attackedTarget;

		private TestHawk() {
			super();
		}

		private TestHawk(Char.Alignment alignment) {
			super(alignment);
		}

		private void setVisibleForTest(int... cells) {
			fieldOfView = new boolean[Dungeon.level.length()];
			for (int cell : cells) {
				fieldOfView[cell] = true;
			}
		}

		private void setAllVisibleForTest() {
			fieldOfView = new boolean[Dungeon.level.length()];
			Arrays.fill(fieldOfView, true);
		}

		private Char enemyForTest() {
			return enemy;
		}

		private void setEnemyForTest(Char target) {
			enemy = target;
		}

		private Char nearestEligibleTargetForTest() {
			try {
				java.lang.reflect.Method method = HuntressBoss.DistractingHawk.class
						.getDeclaredMethod("nearestEligibleTarget");
				method.setAccessible(true);
				return (Char) method.invoke(this);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}

		private Char chooseEnemyForTest() {
			return chooseEnemy();
		}

		private boolean canAttackForTest(Char target) {
			return canAttack(target);
		}

		private void chooseEnemyAndHuntForTest() {
			enemy = chooseEnemy();
			boolean enemyInFOV = enemy != null && fieldOfView[enemy.pos]
					&& enemy.invisible <= 0;
			HUNTING.act(enemyInFOV, false);
		}

		private int attackProcForTest(Char target, int incomingDamage) {
			return super.attackProc(target, incomingDamage);
		}

		@Override
		protected int baseAttackProc(Char enemy, int damage, DamageTag... damageTags) {
			return damage;
		}

		@Override
		public float resist(Class effect) {
			return 1f;
		}

		@Override
		public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti,
				DamageTag... damageTags) {
			attacks++;
			return true;
		}

		@Override
		protected boolean doAttack(Char enemy) {
			attacks++;
			attackedTarget = enemy;
			beginRetreatFrom(enemy);
			return true;
		}

		@Override
		protected boolean moveSprite(int from, int to) {
			return true;
		}

		@Override
		public void move(int step, boolean travelling) {
			pos = step;
		}

	}

	private static class TestTentacle extends HuntressBoss.HuntressTentacle {

		private TestTentacle(Char.Alignment alignment) {
			super(alignment);
		}

		private void setAllVisibleForTest() {
			fieldOfView = new boolean[Dungeon.level.length()];
			Arrays.fill(fieldOfView, true);
		}

		private Char chooseEnemyForTest() {
			return chooseEnemy();
		}

		private Char enemyForTest() {
			return enemy;
		}

		private void setEnemyForTest(Char target) {
			enemy = target;
		}

		private boolean canAttackForTest(Char target) {
			return canAttack(target);
		}

		private int attackProcForTest(Char target, int incomingDamage) {
			return super.attackProc(target, incomingDamage);
		}

		@Override
		public float resist(Class effect) {
			return 1f;
		}
	}

	private static class TestLevel extends Level {

		private TestLevel() {
			blobs = new HashMap<>();
			mobs = new HashSet<>();
		}

		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}

	private static class RhythmLevel extends HuntressBossLevel {

		private RhythmLevel() {
			blobs = new HashMap<>();
			mobs = new HashSet<>();
			heaps = new SparseArray<>();
			plants = new SparseArray<>();
			traps = new SparseArray<>();
		}

		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}

		private void setCoverClustersForTest(List<Set<Integer>> clusters) {
			try {
				Field field = HuntressBossLevel.class.getDeclaredField("coverClusters");
				field.setAccessible(true);
				field.set(this, clusters);
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}
	}
}
