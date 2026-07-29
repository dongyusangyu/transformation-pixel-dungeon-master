package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EXItemSpriteSheetTest {

	@Test
	public void exSealUsesAlternateTextureAndNormalSealDoesNot() {
		assertFalse(EXItemSpriteSheet.isEX(ItemSpriteSheet.SEAL));
		assertEquals(Assets.Sprites.ITEMS,
				EXItemSpriteSheet.textureFor(ItemSpriteSheet.SEAL));
		assertEquals(ItemSpriteSheet.SEAL,
				EXItemSpriteSheet.frameFor(ItemSpriteSheet.SEAL));

		assertTrue(EXItemSpriteSheet.isEX(EXItemSpriteSheet.SEAL));
		assertEquals(Assets.Sprites.EX_ITEMS,
				EXItemSpriteSheet.textureFor(EXItemSpriteSheet.SEAL));
		assertEquals(ItemSpriteSheet.SEAL,
				EXItemSpriteSheet.frameFor(EXItemSpriteSheet.SEAL));
	}

	@Test
	public void invalidNegativeIndexIsNotTreatedAsEX() {
		assertFalse(EXItemSpriteSheet.isEX(-1));
		assertEquals(-1, EXItemSpriteSheet.frameFor(-1));
		assertEquals(Assets.Sprites.ITEMS, EXItemSpriteSheet.textureFor(-1));
	}

	@Test
	public void treasureFramesCoverCellsZeroThroughNineteen() {
		assertEquals(0, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.MUISCA_GOLDEN_RAFT));
		assertEquals(1, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.IMPERIAL_CROWN));
		assertEquals(9, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.INCA_GOLDEN_LLAMA));
		assertEquals(10, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.LEWIS_CHESS_QUEEN));
		assertEquals(19, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.DJENNE_TERRACOTTA_FIGURE));
	}

	@Test
	public void exFramesUseEncodedCellGeometry() {
		assertEquals(0, EXItemSpriteSheet.frameX(EXItemSpriteSheet.MUISCA_GOLDEN_RAFT));
		assertEquals(0, EXItemSpriteSheet.frameY(EXItemSpriteSheet.MUISCA_GOLDEN_RAFT));
		assertEquals(14, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.MUISCA_GOLDEN_RAFT));
		assertEquals(14, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.MUISCA_GOLDEN_RAFT));
		assertEquals(48,
				EXItemSpriteSheet.frameX(EXItemSpriteSheet.DJENNE_TERRACOTTA_FIGURE));
		assertEquals(16,
				EXItemSpriteSheet.frameY(EXItemSpriteSheet.DJENNE_TERRACOTTA_FIGURE));
	}

	@Test
	public void metaInfuseKeepsItsIndexAndUsesTenByFifteenPixels() {
		assertEquals(48, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.META_INFUSE));
		assertEquals(10, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.META_INFUSE));
		assertEquals(15, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.META_INFUSE));
	}

	@Test
	public void everyExtendedItemUsesItsMeasuredDimensions() {
		int[] frames = {
				EXItemSpriteSheet.MUISCA_GOLDEN_RAFT,
				EXItemSpriteSheet.IMPERIAL_CROWN,
				EXItemSpriteSheet.PAKAL_JADE_MASK,
				EXItemSpriteSheet.SUTTON_HOO_HELMET,
				EXItemSpriteSheet.BOOK_OF_KELLS,
				EXItemSpriteSheet.CHOLA_NATARAJA,
				EXItemSpriteSheet.DOJIGIRI_YASUTSUNA,
				EXItemSpriteSheet.TURQUOISE_SERPENT,
				EXItemSpriteSheet.RU_WARE_BOWL,
				EXItemSpriteSheet.INCA_GOLDEN_LLAMA,
				EXItemSpriteSheet.LEWIS_CHESS_QUEEN,
				EXItemSpriteSheet.HARBAVILLE_TRIPTYCH,
				EXItemSpriteSheet.AL_MUGHIRA_PYXIS,
				EXItemSpriteSheet.BLACAS_EWER,
				EXItemSpriteSheet.GREAT_KHAN_PAIZA,
				EXItemSpriteSheet.GORYEO_MAEBYEONG,
				EXItemSpriteSheet.JAVANESE_GOLD_CUP,
				EXItemSpriteSheet.ETHIOPIAN_PROCESSIONAL_CROSS,
				EXItemSpriteSheet.GREAT_ZIMBABWE_BIRD,
				EXItemSpriteSheet.DJENNE_TERRACOTTA_FIGURE,
				EXItemSpriteSheet.SCROLL_EXTRACTION,
				EXItemSpriteSheet.META_INFUSE,
				EXItemSpriteSheet.SEAL
		};
		int[] expectedIndices = {
				0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
				10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
				32, 48, ItemSpriteSheet.SEAL
		};
		int[][] expectedSizes = {
				{14, 14}, {12, 14}, {15, 16}, {15, 16}, {14, 16},
				{14, 16}, {15, 16}, {16, 12}, {16, 13}, {14, 15},
				{13, 16}, {15, 16}, {11, 16}, {16, 16}, {11, 16},
				{11, 16}, {15, 14}, {16, 16}, {11, 16}, {11, 16},
				{15, 14}, {10, 15}, {16, 16}
		};

		assertEquals(expectedIndices.length, frames.length);
		assertEquals(expectedSizes.length, frames.length);
		for (int i = 0; i < frames.length; i++) {
			assertEquals("index " + i,
					expectedIndices[i], EXItemSpriteSheet.frameFor(frames[i]));
			assertEquals("width " + i,
					expectedSizes[i][0], EXItemSpriteSheet.frameWidth(frames[i]));
			assertEquals("height " + i,
					expectedSizes[i][1], EXItemSpriteSheet.frameHeight(frames[i]));
		}
	}

	@Test
	public void extractionScrollStartsASeparateThirdRow() {
		assertEquals(32,
				EXItemSpriteSheet.frameFor(EXItemSpriteSheet.SCROLL_EXTRACTION));
		assertEquals(0,
				EXItemSpriteSheet.frameX(EXItemSpriteSheet.SCROLL_EXTRACTION));
		assertEquals(32,
				EXItemSpriteSheet.frameY(EXItemSpriteSheet.SCROLL_EXTRACTION));
	}

	@Test
	public void itemSpriteUsesEncodedWidthAndHeightForExtendedFrames() throws IOException {
		String source = itemSpriteSource();
		int start = source.indexOf("public void frame( int image )");
		int end = source.indexOf("public static int pick", start);
		String frameMethod = source.substring(start, end);

		assertTrue(frameMethod.contains("EXItemSpriteSheet.frameWidth(image)"));
		assertTrue(frameMethod.contains("EXItemSpriteSheet.frameHeight(image)"));
		assertTrue(frameMethod.contains(
				"targetTexture.uvRect(left, top, left + width, top + height)"));
		assertFalse(frameMethod.contains("EXItemSpriteSheet.frameSize()"));
	}

	@Test
	public void missingBaseFramesFallBackBeforeCallingImageFrame() throws IOException {
		String source = itemSpriteSource();
		int start = source.indexOf("public void frame( int image )");
		int end = source.indexOf("public static int pick", start);
		String frameMethod = source.substring(start, end);

		assertTrue(frameMethod.contains("if (itemFrame == null)"));
		assertTrue(frameMethod.contains("image = ItemSpriteSheet.SOMETHING"));
		assertTrue(frameMethod.contains("frame(itemFrame)"));
	}

	private static String itemSpriteSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSprite.java");
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
	}
}
