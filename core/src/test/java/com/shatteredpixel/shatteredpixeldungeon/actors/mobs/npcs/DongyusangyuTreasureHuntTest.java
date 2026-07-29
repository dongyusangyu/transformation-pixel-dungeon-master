/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DongyusangyuTreasureHuntTest {

	private static final String[] CHOICE_KEYS = {
			"mossy_path",
			"ancient_runes",
			"broken_bridge",
			"lost_ghost",
			"rat_ambush",
			"toxic_thicket",
			"falling_rocks",
			"stone_guardian",
			"dark_riddle",
			"cursed_altar",
			"mimic_chest",
			"abyss_shortcut",
			"ember_camp",
			"moon_well",
			"ember_pact"
	};

	@Test
	public void interactionOpensTreasureHuntMenuInsteadOfOnlyYelling() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Dongyusangyu.java");

		assertTrue(source.contains("new WndTreasureHuntMenu()"));
		assertTrue(source.contains("Game.runOnRenderThread"));
		assertFalse(source.contains("yell("));
	}

	@Test
	public void dungeonLifecycleStoresAndRestoresTreasureHuntRecords() throws IOException {
		String source = readCoreSource("com/shatteredpixel/shatteredpixeldungeon/Dungeon.java");

		assertTrue(source.contains("TreasureHuntRecords.reset()"));
		assertTrue(source.contains("TreasureHuntRecords.storeInBundle( bundle )"));
		assertTrue(source.contains("TreasureHuntRecords.restoreFromBundle( bundle )"));
	}

	@Test
	public void treasureHuntChoicesUseShortFuzzyLabelsAndRevealResultsAfterSelection()
			throws IOException {
		String english = readCoreFile("src/main/assets/messages/windows/windows.properties");
		String chinese = readCoreFile("src/main/assets/messages/windows/windows_zh.properties");

		for (String choiceKey : CHOICE_KEYS) {
			String propertyKey = "windows.wndtreasurehunt.choice_" + choiceKey;
			String englishLabel = propertyValue(english, propertyKey);
			String chineseLabel = propertyValue(chinese, propertyKey);
			String englishNarrative = propertyValue(english,
					"windows.wndtreasurehunt.narrative_" + choiceKey);
			String chineseNarrative = propertyValue(chinese,
					"windows.wndtreasurehunt.narrative_" + choiceKey);

			assertFalse(englishLabel.contains("%"));
			assertFalse(chineseLabel.contains("%"));
			assertTrue(englishLabel.length() <= 22);
			assertTrue(chineseLabel.length() <= 10);
			assertFalse(englishNarrative.trim().isEmpty());
			assertFalse(chineseNarrative.trim().isEmpty());
		}

		String windowSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHunt.java")
				.replaceAll("\\s+", " ");
		String menuSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndTreasureHuntMenu.java")
				.replaceAll("\\s+", " ");
		assertFalse(windowSource.contains(
				"choiceKey(choice.encounter()), choice.scoreGain()"));
		assertTrue(windowSource.contains("result.afterglowChange()"));
		assertTrue(windowSource.contains("encounterNarrative(result.encounter())"));
		assertTrue(windowSource.contains("settle(turnResult(result))"));
		assertTrue(windowSource.contains(
				"TreasureHuntRecords.updateTenStepProgress( "
						+ "game.score(), game.completedTenSteps())"));
		assertTrue(windowSource.contains(
				"Dungeon.hero, game.score(), game.completedTenSteps()"));
		assertTrue(menuSource.contains(
				"Dungeon.hero, TreasureHuntRecords.tenStepScore(), "
						+ "TreasureHuntRecords.tenStepCompleted()"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		return readCoreFile("src/main/java/" + relativePath);
	}

	private static String readCoreFile(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

	private static String propertyValue(String properties, String key) {
		String prefix = key + "=";
		for (String line : properties.split("\\R")) {
			if (line.startsWith(prefix)) {
				return line.substring(prefix.length());
			}
		}
		throw new AssertionError("Missing property: " + key);
	}
}
