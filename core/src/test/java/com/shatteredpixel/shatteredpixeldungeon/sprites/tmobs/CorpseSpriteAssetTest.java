package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.Corpse;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CorpseSpriteAssetTest {

	private static final int FRAME_WIDTH = 12;
	private static final int FRAME_HEIGHT = 15;
	private static final int FRAME_COUNT = 16;

	@Test
	public void monsterUsesDedicatedTowerSprite() {
		Corpse corpse = new Corpse();

		assertEquals("sprites/corpse.png", Assets.Sprites.CORPSE);
		assertSame(CorpseSprite.class, corpse.spriteClass);
	}

	@Test
	public void spriteSheetHasSixteenHardEdgedTwelveByFifteenFrames() throws IOException {
		Path asset = coreDirectory().resolve("src/main/assets/sprites/corpse.png");
		assertTrue(Files.isRegularFile(asset));

		BufferedImage image = ImageIO.read(asset.toFile());
		assertEquals(FRAME_WIDTH * FRAME_COUNT, image.getWidth());
		assertEquals(FRAME_HEIGHT, image.getHeight());

		Set<Integer> visibleColors = new HashSet<>();
		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int argb = image.getRGB(frame * FRAME_WIDTH + localX, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 0) {
						assertEquals(0, argb);
					} else {
						nonEmpty = true;
						visibleColors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertTrue("frame " + frame + " must not be empty", nonEmpty);
		}
		assertTrue("sprite palette must stay at or below 12 colors",
				visibleColors.size() <= 12);
	}

	@Test
	public void allGameplayFramesRetainWarriorClothArmorSilhouettes() throws IOException {
		Path sprites = coreDirectory().resolve("src/main/assets/sprites");
		BufferedImage warrior = ImageIO.read(sprites.resolve("warrior.png").toFile());
		BufferedImage corpse = ImageIO.read(sprites.resolve("corpse.png").toFile());

		assertEquals(256, warrior.getWidth());
		assertEquals(128, warrior.getHeight());

		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int sourceX = frame * FRAME_WIDTH + localX;
					if ((warrior.getRGB(sourceX, y) >>> 24) > 0) {
						assertEquals(
								"frame " + frame + " must retain the cloth-armor silhouette",
								255,
								corpse.getRGB(sourceX, y) >>> 24);
					}
				}
			}
		}
	}

	@Test
	public void monsterMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String key = "actors.mobs.tmobs.corpse.";

		assertEquals("corpse", defaults.getProperty(key + "name"));
		assertFalse(defaults.getProperty(key + "desc", "").isEmpty());
		assertEquals("死尸", chinese.getProperty(key + "name"));
		assertFalse(chinese.getProperty(key + "desc", "").isEmpty());
	}

	private static Properties loadActorMessages(String fileName) throws IOException {
		Path source = coreDirectory()
				.resolve("src/main/assets/messages/actors")
				.resolve(fileName);
		Properties messages = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			messages.load(reader);
		}
		return messages;
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
