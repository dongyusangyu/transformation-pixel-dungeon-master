package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpack;

import org.junit.Test;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EXItemAtlasBoundsTest {

	@Test
	public void everyNamedItemFrameMatchesItsOpaqueBounds() throws Exception {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
		BufferedImage sheet = ImageIO.read(core.resolve("src/main/assets/sprites/ex_items.png").toFile());
		assertEquals(256, sheet.getWidth());
		assertEquals(512, sheet.getHeight());

		Set<Integer> checked = new HashSet<>();
		for (Field field : EXItemSpriteSheet.class.getFields()) {
			if (!Modifier.isStatic(field.getModifiers()) || field.getType() != int.class
					|| field.getName().equals("SEAL")) continue;
			int frame = field.getInt(null);
			if (!checked.add(EXItemSpriteSheet.frameFor(frame))) continue;
			int cellX = EXItemSpriteSheet.frameFor(frame) % 16 * 16;
			int cellY = EXItemSpriteSheet.frameFor(frame) / 16 * 16;
			Rectangle bounds = opaqueBounds(sheet, cellX, cellY);
			assertNotNull(field.getName() + " is blank", bounds);
			assertEquals(field.getName() + " x", bounds.x, EXItemSpriteSheet.frameX(frame));
			assertEquals(field.getName() + " y", bounds.y, EXItemSpriteSheet.frameY(frame));
			assertEquals(field.getName() + " width", bounds.width, EXItemSpriteSheet.frameWidth(frame));
			assertEquals(field.getName() + " height", bounds.height, EXItemSpriteSheet.frameHeight(frame));
		}
	}

	@Test
	public void hikingBackpackUsesThePurpleParcelInThePenultimateRow() throws Exception {
		int frame = EXItemSpriteSheet.class.getField("HIKING_BACKPACK").getInt(null);
		assertEquals(480, EXItemSpriteSheet.frameFor(frame));
		assertEquals(frame, new HikingBackpack().image);
	}

	@Test
	public void customItemGeneratorUsesTheHikingBackpackSprite() throws Exception {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
		String source = new String(Files.readAllBytes(core.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestPotion.java")),
				java.nio.charset.StandardCharsets.UTF_8);
		org.junit.Assert.assertTrue(source.contains("case 14: return EXItemSpriteSheet.HIKING_BACKPACK;"));
	}

	private static Rectangle opaqueBounds(BufferedImage sheet, int cellX, int cellY) {
		int minX = 16, minY = 16, maxX = -1, maxY = -1;
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				if ((sheet.getRGB(cellX + x, cellY + y) >>> 24) == 0) continue;
				minX = Math.min(minX, x);
				minY = Math.min(minY, y);
				maxX = Math.max(maxX, x);
				maxY = Math.max(maxY, y);
			}
		}
		return maxX < 0 ? null
				: new Rectangle(cellX + minX, cellY + minY, maxX - minX + 1, maxY - minY + 1);
	}
}
