package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.watabou.noosa.audio.Music;
import org.junit.Test;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class ExtractionRaidAssetTest {
	@Test public void allGrassVariantsAndExtensionsUseSewerPixels() throws Exception {
		BufferedImage raid = ImageIO.read(asset(Assets.Environment.TILES_UES));
		BufferedImage sewers = ImageIO.read(asset(Assets.Environment.TILES_SEWERS));
		int[] grass = {DungeonTileSheet.GRASS, DungeonTileSheet.GRASS_ALT,
				DungeonTileSheet.FLAT_HIGH_GRASS, DungeonTileSheet.FLAT_HIGH_GRASS_ALT,
				DungeonTileSheet.FLAT_FURROWED_GRASS, DungeonTileSheet.FLAT_FURROWED_ALT,
				DungeonTileSheet.RAISED_HIGH_GRASS, DungeonTileSheet.RAISED_HIGH_GRASS_ALT,
				DungeonTileSheet.RAISED_FURROWED_GRASS, DungeonTileSheet.RAISED_FURROWED_ALT,
				DungeonTileSheet.HIGH_GRASS_OVERHANG, DungeonTileSheet.HIGH_GRASS_OVERHANG_ALT,
				DungeonTileSheet.FURROWED_OVERHANG, DungeonTileSheet.FURROWED_OVERHANG_ALT,
				DungeonTileSheet.HIGH_GRASS_UNDERHANG, DungeonTileSheet.HIGH_GRASS_UNDERHANG_ALT,
				DungeonTileSheet.FURROWED_UNDERHANG, DungeonTileSheet.FURROWED_UNDERHANG_ALT};
		for (int index : grass) {
			int left = index % 16 * 16, top = index / 16 * 16;
			for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++)
				assertEquals("grass atlas index " + index + " pixel " + x + ":" + y,
						sewers.getRGB(left + x, top + y), raid.getRGB(left + x, top + y));
		}
		BufferedImage preview = new BufferedImage(6 * 96, 3 * 120, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = preview.createGraphics();
		graphics.setColor(new Color(40, 40, 40));
		graphics.fillRect(0, 0, preview.getWidth(), preview.getHeight());
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		for (int i = 0; i < grass.length; i++) {
			int index = grass[i], x = i % 6 * 96, y = i / 6 * 120;
			graphics.drawImage(raid, x, y, x + 96, y + 96,
					index % 16 * 16, index / 16 * 16,
					index % 16 * 16 + 16, index / 16 * 16 + 16, null);
			graphics.setColor(Color.WHITE);
			graphics.drawString("index " + index, x + 4, y + 112);
		}
		graphics.dispose();
		File output = new File("build/test-artifacts/raid-sewer-grass-cells.png");
		assertTrue(output.getParentFile().isDirectory() || output.getParentFile().mkdirs());
		assertTrue(ImageIO.write(preview, "png", output));
		assertEquals(Assets.Environment.TILES_UES, new ExtractionRaidLevel().tilesTex());
	}
	@Test public void environmentSheetsRetainTerrainAtlasDimensions() throws Exception {
		for (String[] pair : new String[][]{
				{Assets.Environment.TILES_UES, Assets.Environment.TILES_HALLS},
				{Assets.Environment.WATER_UES, Assets.Environment.WATER_HALLS}}) {
			BufferedImage replacement = ImageIO.read(asset(pair[0]));
			BufferedImage original = ImageIO.read(asset(pair[1]));
			assertEquals(original.getWidth(), replacement.getWidth());
			assertEquals(original.getHeight(), replacement.getHeight());
		}
	}

	@Test public void droneSheetHasFourCompleteRowsOfLiveAnimationFrames() throws Exception {
		BufferedImage sheet = ImageIO.read(asset(Assets.Sprites.UES_DRONES));
		assertEquals(288, sheet.getWidth());
		assertEquals(64, sheet.getHeight());
		BufferedImage preview = new BufferedImage(18 * 80, 4 * 104 + 28, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = preview.createGraphics();
		g.setColor(new Color(30, 30, 30)); g.fillRect(0, 0, preview.getWidth(), preview.getHeight());
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		for (int row = 0; row < 4; row++) {
			g.setColor(Color.WHITE);
			g.drawString(new String[]{"Guard", "Eye", "Siren", "Gunner"}[row], 4, row * 104 + 20);
			for (int column = 0; column < 18; column++) {
				if (column < 8 || column >= 13) {
					boolean opaque = false;
					for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++)
						opaque |= (sheet.getRGB(column * 16 + x, row * 16 + y) >>> 24) != 0;
					assertTrue("empty live frame " + row + ":" + column, opaque);
				}
				g.drawImage(sheet, column * 80, row * 104 + 28, column * 80 + 80, row * 104 + 108,
						column * 16, row * 16, column * 16 + 16, row * 16 + 16, null);
			}
		}
		g.dispose();
		File output = new File("build/test-artifacts/raid-drone-frames.png");
		assertTrue(output.getParentFile().isDirectory() || output.getParentFile().mkdirs());
		assertTrue(ImageIO.write(preview, "png", output));
	}

	@Test public void raidMusicLoopsTheUESAssetWithAndWithoutTheAmulet() throws Exception {
		Map<Field, Object> state = new HashMap<>();
		for (Field field : Music.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;
			field.setAccessible(true); state.put(field, field.get(Music.INSTANCE));
		}
		boolean oldAmulet = Statistics.amuletObtained;
		try {
			Field enabled = Music.class.getDeclaredField("enabled"); enabled.setAccessible(true); enabled.set(Music.INSTANCE, false);
			Field player = Music.class.getDeclaredField("player"); player.setAccessible(true); player.set(Music.INSTANCE, null);
			Field lastPlayed = Music.class.getDeclaredField("lastPlayed"); lastPlayed.setAccessible(true);
			Field looping = Music.class.getDeclaredField("looping"); looping.setAccessible(true);
			for (boolean amulet : new boolean[]{true, false}) {
				Statistics.amuletObtained = amulet;
				new ExtractionRaidLevel().playLevelMusic();
				assertEquals(Assets.Music.UES, lastPlayed.get(Music.INSTANCE));
				assertEquals(true, looping.get(Music.INSTANCE));
			}
		} finally {
			for (Map.Entry<Field, Object> entry : state.entrySet()) entry.getKey().set(Music.INSTANCE, entry.getValue());
			Statistics.amuletObtained = oldAmulet;
		}
	}

	@Test public void UESMusicAndDroneDeathSoundAreOggVorbisAssets() throws Exception {
		for (String name : new String[]{Assets.Music.UES, Assets.Sounds.DRONEDIED}) {
			byte[] data = Files.readAllBytes(asset(name).toPath());
			ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
			assertEquals("OggS", new String(data, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
			int firstPacket = 27 + (data[26] & 255);
			assertEquals(1, data[firstPacket]);
			assertEquals("vorbis", new String(data, firstPacket + 1, 6, java.nio.charset.StandardCharsets.US_ASCII));
			assertTrue(data[firstPacket + 11] > 0);
			assertTrue(buffer.getInt(firstPacket + 12) > 0);
			int offset = 0;
			long finalGranule = 0;
			while (offset < data.length) {
				assertEquals("OggS", new String(data, offset, 4, java.nio.charset.StandardCharsets.US_ASCII));
				finalGranule = Math.max(finalGranule, buffer.getLong(offset + 6));
				int segments = data[offset + 26] & 255;
				int payload = 0;
				for (int i = 0; i < segments; i++) payload += data[offset + 27 + i] & 255;
				offset += 27 + segments + payload;
			}
			assertEquals(data.length, offset);
			assertTrue(finalGranule > 0);
		}
	}

	private File asset(String name) {
		File file = new File("src/main/assets", name);
		return file.exists() ? file : new File("core/src/main/assets", name);
	}
}
