package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CorrosiveSwarm;
import com.watabou.noosa.MovieClip;

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

public class CorrosiveSwarmSpriteAssetTest {

	private static final int FRAME_WIDTH = 16;
	private static final int FRAME_HEIGHT = 16;
	private static final int FRAME_COUNT = 17;

	@Test
	public void monsterUsesDedicatedTowerSprite() {
		CorrosiveSwarm swarm = new CorrosiveSwarm();

		assertEquals("sprites/corrosive_swarm.png", Assets.Sprites.CORROSIVE_SWARM);
		assertSame(CorrosiveSwarmSprite.class, swarm.spriteClass);
	}

	@Test
	public void burstUsesAnIndependentEffectClip() {
		boolean found = false;
		for (Class<?> nested : CorrosiveSwarmSprite.class.getDeclaredClasses()) {
			if (nested.getSimpleName().equals("BurstEffect")
					&& MovieClip.class.isAssignableFrom(nested)) {
				found = true;
			}
		}
		assertTrue(found);
	}

	@Test
	public void spriteSheetHasSeventeenHardEdgedSixteenBySixteenFrames() throws IOException {
		Path asset = coreDirectory().resolve("src/main/assets/sprites/corrosive_swarm.png");
		assertTrue(Files.isRegularFile(asset));

		BufferedImage image = ImageIO.read(asset.toFile());
		assertEquals(FRAME_WIDTH * FRAME_COUNT, image.getWidth());
		assertEquals(FRAME_HEIGHT, image.getHeight());

		Set<Integer> visibleColors = new HashSet<>();
		for (int frame = 0; frame < FRAME_COUNT; frame++) {
			boolean nonEmpty = false;
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int x = frame * FRAME_WIDTH; x < (frame + 1) * FRAME_WIDTH; x++) {
					int argb = image.getRGB(x, y);
					int alpha = argb >>> 24;
					assertTrue(alpha == 0 || alpha == 255);
					if (alpha == 255) {
						nonEmpty = true;
						visibleColors.add(argb & 0xFFFFFF);
					}
				}
			}
			assertTrue("frame " + frame + " must not be empty", nonEmpty);
		}
		assertTrue("sprite palette must stay at or below 12 colors", visibleColors.size() <= 12);
	}

	@Test
	public void gameplayFramesRetainOrdinarySwarmSilhouettes() throws IOException {
		Path sprites = coreDirectory().resolve("src/main/assets/sprites");
		BufferedImage ordinary = ImageIO.read(sprites.resolve("swarm.png").toFile());
		BufferedImage corrosive = ImageIO.read(sprites.resolve("corrosive_swarm.png").toFile());

		for (int frame = 0; frame < 15; frame++) {
			for (int y = 0; y < FRAME_HEIGHT; y++) {
				for (int localX = 0; localX < FRAME_WIDTH; localX++) {
					int x = frame * FRAME_WIDTH + localX;
					if ((ordinary.getRGB(x, y) >>> 24) > 0) {
						assertEquals(
								"frame " + frame + " must retain the ordinary Swarm silhouette",
								255,
								corrosive.getRGB(x, y) >>> 24);
					}
				}
			}
		}
	}

	@Test
	public void monsterMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadActorMessages("actors.properties");
		Properties chinese = loadActorMessages("actors_zh.properties");
		String key = "actors.mobs.tmobs.corrosiveswarm.";

		assertEquals("corrosive swarm", defaults.getProperty(key + "name"));
		assertFalse(defaults.getProperty(key + "desc", "").isEmpty());
		assertEquals("腐蚀蝇群", chinese.getProperty(key + "name"));
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
