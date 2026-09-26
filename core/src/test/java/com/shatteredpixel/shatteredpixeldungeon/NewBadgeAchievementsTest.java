package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnight;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NewBadgeAchievementsTest {

	@After
	public void tearDown() {
		Statistics.reset();
	}

	@Test
	public void badgesUseTheirNewBackgroundTierCells() {
		assertBadge(Badges.Badge.CHAIN_MACE_SIX_TARGETS, 112);
		assertBadge(Badges.Badge.ENTER_TOWER, 137);
		assertBadge(Badges.Badge.TOWER_FLOOR_10, 138);
		assertBadge(Badges.Badge.CORPSES_SLAIN_ONE_FLOOR, 139);
		assertBadge(Badges.Badge.DEATH_FROM_ALIENATED_PRISMATIC_GUARD, 140);
		assertBadge(Badges.Badge.DEATH_KNIGHT_BLESSED_ANKH, 141);
		assertBadge(Badges.Badge.TOWER_FLOOR_25, 152);
		assertBadge(Badges.Badge.TOWER_FLOOR_50, 153);
		assertEquals(154, Badges.Badge.WAR_KNIGHT_SLAIN.image);
		assertEquals(Badges.BadgeType.HIDDEN, Badges.Badge.WAR_KNIGHT_SLAIN.type);
		assertBadge(Badges.Badge.HUNGER_KNIGHT_SLAIN, 155);
		assertBadge(Badges.Badge.PESTILENCE_KNIGHT_SLAIN, 156);
		assertBadge(Badges.Badge.DEATH_KNIGHT_SLAIN, 157);
		assertBadge(Badges.Badge.GENTLEMAN_ELF_SLAIN, 158);
		assertBadge(Badges.Badge.TOWER_FLOOR_100, 169);
		assertEquals(170, Badges.Badge.WAR_KNIGHT_DUELIST.image);
		assertEquals(Badges.BadgeType.HIDDEN, Badges.Badge.WAR_KNIGHT_DUELIST.type);
		assertBadge(Badges.Badge.HUNGER_KNIGHT_WARRIOR, 171);
		assertBadge(Badges.Badge.PESTILENCE_KNIGHT_CLERIC, 172);
		assertBadge(Badges.Badge.DEATH_KNIGHT_FRIAR, 173);
		assertBadge(Badges.Badge.GENTLEMAN_ELF_FREEMAN, 174);
	}

	@Test
	public void backpackAndTowerDeathsAreIncludedInAggregateBadgeConditions() throws IOException {
		String source = source("Badges.java");
		assertTrue(source.contains("HikingBackpack.class"));
		assertTrue(source.contains("DEATH_FROM_ALIENATED_PRISMATIC_GUARD"));
		assertTrue(source.contains("DEATH_KNIGHT_BLESSED_ANKH"));
	}

	@Test
	public void aggregateBadgesRequireTheNewBackpackAndTowerDeaths() {
		List<Badges.Badge> bags = Arrays.asList(Badges.Badge.BAG_BOUGHT_VELVET_POUCH,
				Badges.Badge.BAG_BOUGHT_SCROLL_HOLDER,
				Badges.Badge.BAG_BOUGHT_POTION_BANDOLIER,
				Badges.Badge.BAG_BOUGHT_MAGICAL_HOLSTER);
		assertFalse(Badges.qualifiesForAllBags(bags, false));
		assertTrue(Badges.qualifiesForAllBags(bags, true));

		List<Badges.Badge> deaths = Arrays.asList(Badges.Badge.DEATH_FROM_FIRE,
				Badges.Badge.DEATH_FROM_POISON, Badges.Badge.DEATH_FROM_GAS,
				Badges.Badge.DEATH_FROM_HUNGER, Badges.Badge.DEATH_FROM_FALLING,
				Badges.Badge.DEATH_FROM_ENEMY_MAGIC, Badges.Badge.DEATH_FROM_FRIENDLY_MAGIC,
				Badges.Badge.DEATH_FROM_SACRIFICE, Badges.Badge.DEATH_FROM_GRIM_TRAP,
				Badges.Badge.DEATH_FROM_ALIENATED_PRISMATIC_GUARD);
		assertFalse(Badges.qualifiesForDeathFromAll(deaths));
		deaths = new java.util.ArrayList<>(deaths);
		deaths.add(Badges.Badge.DEATH_KNIGHT_BLESSED_ANKH);
		assertTrue(Badges.qualifiesForDeathFromAll(deaths));
	}

	@Test
	public void towerProgressBadgesOnlyComeFromTowerFloors() {
		assertEquals(Collections.emptyList(), Badges.towerProgressBadges(1, 0));
		assertEquals(Collections.emptyList(), Badges.towerProgressBadges(0, TowerLevel.BRANCH));
		assertEquals(Collections.singletonList(Badges.Badge.ENTER_TOWER),
				Badges.towerProgressBadges(1, TowerLevel.BRANCH));
		assertEquals(Collections.singletonList(Badges.Badge.ENTER_TOWER),
				Badges.towerProgressBadges(9, TowerLevel.BRANCH));
		assertEquals(Arrays.asList(Badges.Badge.ENTER_TOWER, Badges.Badge.TOWER_FLOOR_10),
				Badges.towerProgressBadges(10, TowerLevel.BRANCH));
		assertEquals(Arrays.asList(Badges.Badge.ENTER_TOWER, Badges.Badge.TOWER_FLOOR_10),
				Badges.towerProgressBadges(24, TowerLevel.BRANCH));
		assertEquals(Arrays.asList(Badges.Badge.ENTER_TOWER, Badges.Badge.TOWER_FLOOR_10,
				Badges.Badge.TOWER_FLOOR_25), Badges.towerProgressBadges(25, TowerLevel.BRANCH));
		assertEquals(Arrays.asList(Badges.Badge.ENTER_TOWER, Badges.Badge.TOWER_FLOOR_10,
				Badges.Badge.TOWER_FLOOR_25), Badges.towerProgressBadges(49, TowerLevel.BRANCH));
		assertEquals(Arrays.asList(Badges.Badge.ENTER_TOWER, Badges.Badge.TOWER_FLOOR_10,
				Badges.Badge.TOWER_FLOOR_25, Badges.Badge.TOWER_FLOOR_50),
				Badges.towerProgressBadges(50, TowerLevel.BRANCH));
		assertEquals(Arrays.asList(Badges.Badge.ENTER_TOWER, Badges.Badge.TOWER_FLOOR_10,
				Badges.Badge.TOWER_FLOOR_25, Badges.Badge.TOWER_FLOOR_50),
				Badges.towerProgressBadges(99, TowerLevel.BRANCH));
		assertEquals(Arrays.asList(Badges.Badge.ENTER_TOWER, Badges.Badge.TOWER_FLOOR_10,
				Badges.Badge.TOWER_FLOOR_25, Badges.Badge.TOWER_FLOOR_50,
				Badges.Badge.TOWER_FLOOR_100),
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
		assertEquals(Arrays.asList(Badges.Badge.DEATH_KNIGHT_SLAIN,
				Badges.Badge.DEATH_KNIGHT_FRIAR),
				Badges.towerBossBadges(TowerBossGenerator.DEATH_KNIGHT_ID, HeroClass.FRIAR));
		assertEquals(Collections.emptyList(),
				Badges.towerBossBadges("war_knight", HeroClass.DUELIST));
		assertEquals(Collections.emptyList(), Badges.towerBossBadges("unknown", HeroClass.WARRIOR));
	}

	@Test
	public void blessedAnkhRequiresDeathKnightAsActualFatalSource() {
		assertTrue(Badges.qualifiesForDeathKnightBlessedAnkh(new DeathKnight(), true));
		assertFalse(Badges.qualifiesForDeathKnightBlessedAnkh(new DeathKnight(), false));
		assertFalse(Badges.qualifiesForDeathKnightBlessedAnkh(new Object(), true));
	}

	@Test
	public void renamedAndMovedBadgesRestoreUsingTheirExistingEnumNames() {
		HashSet<Badges.Badge> saved = new HashSet<>(Arrays.asList(
				Badges.Badge.CHAIN_MACE_SIX_TARGETS,
				Badges.Badge.DEATH_KNIGHT_BLESSED_ANKH,
				Badges.Badge.TOWER_FLOOR_100));
		Bundle bundle = new Bundle();
		Badges.store(bundle, saved);
		assertEquals(saved, Badges.restore(bundle));
	}

	@Test
	public void targetBadgeCellsAreDrawnAndFormerCellsAreEmpty() throws Exception {
		File file = new File("src/main/assets/interfaces/badges.png");
		if (!file.isFile()) file = new File("core/src/main/assets/interfaces/badges.png");
		BufferedImage sheet = ImageIO.read(file);
		assertEquals(128, sheet.getWidth());
		assertEquals(352, sheet.getHeight());
		for (int index : new int[]{112, 137, 138, 139, 140, 141, 152, 153, 154,
				155, 156, 157, 158, 169, 170, 171, 172, 173, 174}) {
			assertTrue("badge cell " + index + " is empty", hasPixels(sheet, index));
		}
		for (int index : new int[]{31, 32, 33, 34, 66, 67, 68, 69, 70, 71, 72, 73, 74}) {
			assertFalse("former badge cell " + index + " still has art", hasPixels(sheet, index));
		}
	}

	private static boolean hasPixels(BufferedImage sheet, int index) {
		int x = index % 8 * 16;
		int y = index / 8 * 16;
		for (int row = y; row < y + 16; row++) {
			for (int col = x; col < x + 16; col++) {
				if ((sheet.getRGB(col, row) >>> 24) != 0) return true;
			}
		}
		return false;
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
		assertSourceContains("actors/hero/Hero.java", "Badges.validateDeathKnightBlessedAnkh(cause)");
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
		assertSourceContains("Badges.java", "validateBossSlain();");
	}

	@Test
	public void badgeNotificationRetriesUntilTheBadgeIsGloballyUnlocked() {
		assertTrue(Badges.shouldDisplayAward(true, true));
		assertTrue(Badges.shouldDisplayAward(true, false));
		assertTrue(Badges.shouldDisplayAward(false, false));
		assertFalse(Badges.shouldDisplayAward(false, true));
	}

	@Test
	public void transformedBossesMapToTheirFloorBossBadges() {
		assertEquals(Badges.Badge.BOSS_SLAIN_1, Badges.bossSlainBadge(5));
		assertEquals(Badges.Badge.BOSS_SLAIN_2, Badges.bossSlainBadge(10));
		assertEquals(Badges.Badge.BOSS_SLAIN_3, Badges.bossSlainBadge(15));
		assertEquals(Badges.Badge.BOSS_SLAIN_4, Badges.bossSlainBadge(20));
		assertEquals(Badges.Badge.HEROBOSS_SLAIN_1, Badges.heroBossBadge(5));
		assertEquals(Badges.Badge.HEROBOSS_SLAIN_2, Badges.heroBossBadge(10));
		assertEquals(Badges.Badge.HEROBOSS_SLAIN_3, Badges.heroBossBadge(15));
		assertEquals(null, Badges.heroBossBadge(20));
	}

	@Test
	public void hiddenPrerequisitesDoNotBlockAchievementProgress() {
		assertPrerequisiteHidden(Badges.Badge.BOSS_SLAIN_1, Badges.Badge.HEROBOSS_SLAIN_1);
		assertPrerequisiteHidden(Badges.Badge.BOSS_SLAIN_1, Badges.Badge.HEROBOSS_COUNTER_1);
		assertPrerequisiteHidden(Badges.Badge.BOSS_SLAIN_2, Badges.Badge.HEROBOSS_COUNTER_2);
		assertPrerequisiteHidden(Badges.Badge.BOSS_SLAIN_3, Badges.Badge.HEROBOSS_COUNTER_3);
		assertPrerequisiteHidden(Badges.Badge.BOSS_SLAIN_4, Badges.Badge.HEROBOSS_COUNTER_4);
		assertPrerequisiteHidden(Badges.Badge.VICTORY, Badges.Badge.BACK1);
		assertPrerequisiteHidden(Badges.Badge.VICTORY, Badges.Badge.BACK2);
		assertPrerequisiteHidden(Badges.Badge.CHAMPION_3, Badges.Badge.CHAMPION_4);
		assertPrerequisiteHidden(Badges.Badge.ENTER_TOWER, Badges.Badge.GENTLEMAN_ELF_SLAIN);
		assertPrerequisiteHidden(Badges.Badge.HUNGER_KNIGHT_SLAIN,
				Badges.Badge.HUNGER_KNIGHT_WARRIOR);
		assertPrerequisiteHidden(Badges.Badge.BETTER_TALENT, Badges.Badge.TALENT2025);
		assertPrerequisiteHidden(Badges.Badge.BOSS_CHALLENGE_1,
				Badges.Badge.BOSS_CHALLENGE_2);

		List<Badges.Badge> alreadyUnlockedPrerequisite = new java.util.ArrayList<>(Collections.singletonList(
				Badges.Badge.HEROBOSS_SLAIN_1));
		Badges.filterBadgesWithoutPrerequisites(alreadyUnlockedPrerequisite);
		assertEquals(Collections.singletonList(Badges.Badge.HEROBOSS_SLAIN_1),
				alreadyUnlockedPrerequisite);
	}

	private static void assertPrerequisiteHidden(Badges.Badge prerequisite,
			Badges.Badge dependent) {
		List<Badges.Badge> locked = new java.util.ArrayList<>(Arrays.asList(prerequisite, dependent));
		Badges.filterBadgesWithoutPrerequisites(locked);
		assertEquals(Collections.singletonList(prerequisite), locked);
	}

	@Test
	public void towerProgressReplacesThroughFloor50ButKeepsFloor100AlongsideIt() {
		List<Badges.Badge> unlocked = new java.util.ArrayList<>(Arrays.asList(
				Badges.Badge.TOWER_FLOOR_10,
				Badges.Badge.TOWER_FLOOR_25,
				Badges.Badge.TOWER_FLOOR_50,
				Badges.Badge.TOWER_FLOOR_100));
		Badges.filterReplacedBadges(unlocked);
		assertEquals(Arrays.asList(Badges.Badge.TOWER_FLOOR_50,
				Badges.Badge.TOWER_FLOOR_100), unlocked);

		List<Badges.Badge> locked = new java.util.ArrayList<>(Arrays.asList(
				Badges.Badge.TOWER_FLOOR_10,
				Badges.Badge.TOWER_FLOOR_25,
				Badges.Badge.TOWER_FLOOR_50,
				Badges.Badge.TOWER_FLOOR_100));
		Badges.filterBadgesWithoutPrerequisites(locked);
		assertEquals(Collections.singletonList(Badges.Badge.TOWER_FLOOR_10), locked);
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
				Badges.Badge.HUNGER_KNIGHT_SLAIN,
				Badges.Badge.TOWER_FLOOR_10,
				Badges.Badge.TOWER_FLOOR_25,
				Badges.Badge.TOWER_FLOOR_50,
				Badges.Badge.WAR_KNIGHT_SLAIN,
				Badges.Badge.WAR_KNIGHT_DUELIST,
				Badges.Badge.DEATH_KNIGHT_FRIAR);
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
