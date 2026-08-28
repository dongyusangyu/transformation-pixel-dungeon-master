package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NewBadgeAchievementsTest {

	@After
	public void tearDown() {
		Statistics.reset();
	}

	@Test
	public void newBadgesUseTheReservedCopperAndSilverCells() {
		assertBadge(Badges.Badge.DEATH_FROM_ALIENATED_PRISMATIC_GUARD, Badges.copper + 31);
		assertBadge(Badges.Badge.CHAIN_MACE_SIX_TARGETS, Badges.copper + 32);
		assertBadge(Badges.Badge.ENTER_TOWER, Badges.copper + 33);
		assertBadge(Badges.Badge.CORPSES_SLAIN_ONE_FLOOR, Badges.copper + 34);

		assertBadge(Badges.Badge.DEATH_KNIGHT_BLESSED_ANKH, Badges.silver + 26);
		assertBadge(Badges.Badge.TOWER_FLOOR_100, Badges.silver + 27);
		assertBadge(Badges.Badge.HUNGER_KNIGHT_WARRIOR, Badges.silver + 28);
		assertBadge(Badges.Badge.PESTILENCE_KNIGHT_SLAIN, Badges.silver + 29);
		assertBadge(Badges.Badge.PESTILENCE_KNIGHT_CLERIC, Badges.silver + 30);
		assertBadge(Badges.Badge.GENTLEMAN_ELF_SLAIN, Badges.silver + 31);
		assertBadge(Badges.Badge.GENTLEMAN_ELF_FREEMAN, Badges.silver + 32);
		assertBadge(Badges.Badge.DEATH_KNIGHT_SLAIN, Badges.silver + 33);
		assertBadge(Badges.Badge.HUNGER_KNIGHT_SLAIN, Badges.silver + 34);
	}

	@Test
	public void towerProgressBadgesOnlyComeFromTowerFloors() {
		assertEquals(Collections.emptyList(), Badges.towerProgressBadges(1, 0));
		assertEquals(Collections.singletonList(Badges.Badge.ENTER_TOWER),
				Badges.towerProgressBadges(1, TowerLevel.BRANCH));
		assertEquals(Collections.singletonList(Badges.Badge.ENTER_TOWER),
				Badges.towerProgressBadges(99, TowerLevel.BRANCH));
		assertEquals(Arrays.asList(Badges.Badge.ENTER_TOWER, Badges.Badge.TOWER_FLOOR_100),
				Badges.towerProgressBadges(100, TowerLevel.BRANCH));
	}

	@Test
	public void towerBossBadgesIncludeBaseAndMatchingClassBadge() {
		assertEquals(Arrays.asList(Badges.Badge.HUNGER_KNIGHT_SLAIN,
				Badges.Badge.HUNGER_KNIGHT_WARRIOR),
				Badges.towerBossBadges(TowerBossGenerator.HUNGER_KNIGHT_ID, HeroClass.WARRIOR));
		assertEquals(Collections.singletonList(Badges.Badge.HUNGER_KNIGHT_SLAIN),
				Badges.towerBossBadges(TowerBossGenerator.HUNGER_KNIGHT_ID, HeroClass.MAGE));
		assertEquals(Arrays.asList(Badges.Badge.PESTILENCE_KNIGHT_SLAIN,
				Badges.Badge.PESTILENCE_KNIGHT_CLERIC),
				Badges.towerBossBadges(TowerBossGenerator.PESTILENCE_KNIGHT_ID, HeroClass.CLERIC));
		assertEquals(Arrays.asList(Badges.Badge.GENTLEMAN_ELF_SLAIN,
				Badges.Badge.GENTLEMAN_ELF_FREEMAN),
				Badges.towerBossBadges(TowerBossGenerator.GENTLEMAN_ELF_ID, HeroClass.FREEMAN));
		assertEquals(Collections.singletonList(Badges.Badge.DEATH_KNIGHT_SLAIN),
				Badges.towerBossBadges(TowerBossGenerator.DEATH_KNIGHT_ID, HeroClass.ROGUE));
		assertEquals(Collections.emptyList(), Badges.towerBossBadges("unknown", HeroClass.WARRIOR));
	}

	@Test
	public void corpseKillsAreSeparatedByFloorAndBranchAndSurviveSaveRestore() {
		Statistics.reset();
		for (int i = 1; i <= 4; i++) {
			assertEquals(i, Statistics.recordCorpseSlain(3, 0));
		}
		assertEquals(1, Statistics.recordCorpseSlain(3, TowerLevel.BRANCH));
		assertEquals(1, Statistics.recordCorpseSlain(4, 0));

		Bundle bundle = new Bundle();
		Statistics.storeInBundle(bundle);
		Statistics.reset();
		Statistics.restoreFromBundle(bundle);

		assertEquals(5, Statistics.recordCorpseSlain(3, 0));
		assertEquals(2, Statistics.recordCorpseSlain(3, TowerLevel.BRANCH));
		assertEquals(2, Statistics.recordCorpseSlain(4, 0));
	}

	@Test
	public void everyNewBadgeHasBaseAndChineseText() throws IOException {
		String base = asset("messages/misc/misc.properties");
		String chinese = asset("messages/misc/misc_zh.properties");
		for (Badges.Badge badge : newBadges()) {
			String key = "badges$badge." + badge.name().toLowerCase(Locale.ENGLISH);
			for (String messages : Arrays.asList(base, chinese)) {
				org.junit.Assert.assertTrue(messages.contains(key + ".title="));
				org.junit.Assert.assertTrue(messages.contains(key + ".desc="));
			}
		}
	}

	@Test
	public void runtimeEventsContainAllNewBadgeHooks() throws IOException {
		assertSourceContains("Dungeon.java", "Badges.validateTowerProgress(depth, branch)");
		assertSourceContains("actors/hero/Hero.java", "Badges.validateDeathKnightBlessedAnkh()");
		assertSourceContains("actors/mobs/tboss/TowerBoss.java", "Badges.validateTowerBossSlain");
		assertSourceContains("actors/mobs/tmobs/Corpse.java", "Badges.validateCorpseSlain()");
		assertSourceContains("actors/mobs/tmobs/AlienatedPrismaticGuard.java",
				"implements Hero.Doom");
		assertSourceContains("actors/mobs/tmobs/AlienatedPrismaticGuard.java",
				"Badges.validateDeathFromAlienatedPrismaticGuard()");
		assertSourceContains("items/weapon/melee/tier6/ChainMace.java",
				"Badges.validateChainMaceSixTargets()");
		assertSourceContains("levels/towers/TowerBossLevel.java",
				"public boolean isActiveTowerBoss(String bossId)");
	}

	@Test
	public void badgeNotificationRetriesUntilTheBadgeIsGloballyUnlocked() {
		assertTrue(Badges.shouldDisplayAward(true, true));
		assertTrue(Badges.shouldDisplayAward(true, false));
		assertTrue(Badges.shouldDisplayAward(false, false));
		assertFalse(Badges.shouldDisplayAward(false, true));
	}

	private static void assertBadge(Badges.Badge badge, int image) {
		assertEquals(image, badge.image);
		assertEquals(Badges.BadgeType.LOCAL, badge.type);
	}

	private static List<Badges.Badge> newBadges() {
		return Arrays.asList(
				Badges.Badge.DEATH_FROM_ALIENATED_PRISMATIC_GUARD,
				Badges.Badge.CHAIN_MACE_SIX_TARGETS,
				Badges.Badge.ENTER_TOWER,
				Badges.Badge.CORPSES_SLAIN_ONE_FLOOR,
				Badges.Badge.DEATH_KNIGHT_BLESSED_ANKH,
				Badges.Badge.TOWER_FLOOR_100,
				Badges.Badge.HUNGER_KNIGHT_WARRIOR,
				Badges.Badge.PESTILENCE_KNIGHT_SLAIN,
				Badges.Badge.PESTILENCE_KNIGHT_CLERIC,
				Badges.Badge.GENTLEMAN_ELF_SLAIN,
				Badges.Badge.GENTLEMAN_ELF_FREEMAN,
				Badges.Badge.DEATH_KNIGHT_SLAIN,
				Badges.Badge.HUNGER_KNIGHT_SLAIN);
	}

	private static void assertSourceContains(String relativePath, String expected)
			throws IOException {
		org.junit.Assert.assertTrue(source(relativePath).contains(expected));
	}

	private static String source(String relativePath) throws IOException {
		return readCorePath("src/main/java/com/shatteredpixel/shatteredpixeldungeon/" + relativePath);
	}

	private static String asset(String relativePath) throws IOException {
		return readCorePath("src/main/assets/" + relativePath);
	}

	private static String readCorePath(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)),
				StandardCharsets.UTF_8);
	}
}
