package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GentlemanElfPreludeIntegrationTest {

	@Test
	public void levelOwnsTheChoiceAndBossNoLongerOpensItsOwnWindow() throws Exception {
		String level = read("src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerBossLevel.java");
		String boss = read("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tboss/GentlemanElf.java");

		assertTrue(level.contains("private GentlemanElfPrelude gentlemanElfPrelude"));
		assertTrue(level.contains("GentlemanElfPrelude.Action.SHOW_WINDOW"));
		assertTrue(level.contains("GentlemanElfPrelude.Action.START_ENCOUNTER"));
		assertTrue(level.contains("new GentlemanElfSprite()"));
		assertFalse(boss.contains("requestIntroWindow()"));
		assertFalse(boss.contains("GameScene.show(new WndOptions"));
	}

	private static String read(String relativePath) throws Exception {
		Path core = Paths.get(System.getProperty("user.dir"));
		if (!core.endsWith("core")) core = core.resolve("core");
		return new String(Files.readAllBytes(core.resolve(relativePath)), StandardCharsets.UTF_8);
	}
}
