package com.shatteredpixel.shatteredpixeldungeon.scenes;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameSceneCheckpointTest {

	@Test
	public void checkpointRequiresRequestIdleActorAndReadyHero() throws Exception {
		Method method = GameScene.class.getDeclaredMethod(
				"canCommitCheckpoint", boolean.class, boolean.class, boolean.class);
		method.setAccessible(true);

		assertFalse((Boolean) method.invoke(null, false, true, true));
		assertFalse((Boolean) method.invoke(null, true, false, true));
		assertFalse((Boolean) method.invoke(null, true, true, false));
		assertTrue((Boolean) method.invoke(null, true, true, true));
	}

	@Test
	public void uiReadyAndTurnCheckpointRequestAreSeparate() throws Exception {
		java.nio.file.Path sourceRoot = Paths.get("src/main/java");
		if (!Files.isDirectory(sourceRoot)) sourceRoot = Paths.get("core/src/main/java");
		String gameScene = new String(Files.readAllBytes(sourceRoot.resolve(
				"com/shatteredpixel/shatteredpixeldungeon/scenes/GameScene.java")),
				StandardCharsets.UTF_8);
		String hero = new String(Files.readAllBytes(sourceRoot.resolve(
				"com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java")),
				StandardCharsets.UTF_8);

		int readyStart = gameScene.indexOf("public static void ready()");
		int readyEnd = gameScene.indexOf("public static void requestCheckpoint", readyStart);
		String readyMethod = gameScene.substring(readyStart, readyEnd);

		assertFalse(readyMethod.contains("checkpointRequested"));
		assertTrue(gameScene.contains("public static void requestCheckpoint()"));
		assertTrue(hero.contains("GameScene.ready();\n\t\tGameScene.requestCheckpoint();"));
	}
}
