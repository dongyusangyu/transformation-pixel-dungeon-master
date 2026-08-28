package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

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
	public void everyDrawnExtendedItemUsesItsMeasuredDimensions() {
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
				EXItemSpriteSheet.SAKURA_BLOSSOM,
				EXItemSpriteSheet.BLOOD_SAKURA,
				EXItemSpriteSheet.CHAIN_MACE,
				EXItemSpriteSheet.MERCURY_BLADE,
				EXItemSpriteSheet.AUXILIARY_CORE,
				EXItemSpriteSheet.HUNDRED_TON_HAMMER,
				EXItemSpriteSheet.ORACLE_TERMINAL,
				EXItemSpriteSheet.DEMON_TAIL_WHIP,
				EXItemSpriteSheet.VENOMOUS_SICKLE,
				EXItemSpriteSheet.RADIANT_GOLD_HALBERD,
				EXItemSpriteSheet.SOUL_BLADE,
				EXItemSpriteSheet.MOUNTAIN_GUARD,
				EXItemSpriteSheet.SEAL
		};
		int[] expectedIndices = {
				0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
				10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
				32, 48, 145, 146, 147, 150, 151, 152, 153, 154, 155, 156, 157, 160, ItemSpriteSheet.SEAL
		};
		int[][] expectedSizes = {
				{14, 14}, {12, 14}, {15, 16}, {15, 16}, {14, 16},
				{14, 16}, {15, 16}, {16, 12}, {16, 13}, {14, 15},
				{13, 16}, {15, 16}, {11, 16}, {16, 16}, {11, 16},
				{11, 16}, {15, 14}, {16, 16}, {11, 16}, {11, 16},
				{15, 14}, {10, 15}, {15, 16}, {15, 16}, {16, 16},
				{15, 16}, {14, 15}, {16, 16}, {16, 16}, {14, 14}, {16, 16}, {16, 16}, {16, 16}, {16, 16}
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
	public void weaponPlaceholderRowsUseTheReservedCells() {
		assertEquals(96,
				EXItemSpriteSheet.frameFor(EXItemSpriteSheet.WEAPON_PLACEHOLDER));
		assertEquals(0,
				EXItemSpriteSheet.frameX(EXItemSpriteSheet.WEAPON_PLACEHOLDER));
		assertEquals(96,
				EXItemSpriteSheet.frameY(EXItemSpriteSheet.WEAPON_PLACEHOLDER));
		assertEquals(16,
				EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.WEAPON_PLACEHOLDER));
		assertEquals(16,
				EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.WEAPON_PLACEHOLDER));

		assertEquals(144,
				EXItemSpriteSheet.frameFor(EXItemSpriteSheet.GREAT_GREAT_GREATSWORD));
		assertEquals(0,
				EXItemSpriteSheet.frameX(EXItemSpriteSheet.GREAT_GREAT_GREATSWORD));
		assertEquals(144,
				EXItemSpriteSheet.frameY(EXItemSpriteSheet.GREAT_GREAT_GREATSWORD));
		assertEquals(16,
				EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.GREAT_GREAT_GREATSWORD));
		assertEquals(16,
				EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.GREAT_GREAT_GREATSWORD));

		assertEquals(145, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.SAKURA_BLOSSOM));
		assertEquals(16, EXItemSpriteSheet.frameX(EXItemSpriteSheet.SAKURA_BLOSSOM));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.SAKURA_BLOSSOM));
		assertEquals(15, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.SAKURA_BLOSSOM));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.SAKURA_BLOSSOM));

		assertEquals(146, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.BLOOD_SAKURA));
		assertEquals(32, EXItemSpriteSheet.frameX(EXItemSpriteSheet.BLOOD_SAKURA));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.BLOOD_SAKURA));
		assertEquals(15, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.BLOOD_SAKURA));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.BLOOD_SAKURA));

		assertEquals(147, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.CHAIN_MACE));
		assertEquals(48, EXItemSpriteSheet.frameX(EXItemSpriteSheet.CHAIN_MACE));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.CHAIN_MACE));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.CHAIN_MACE));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.CHAIN_MACE));

		assertEquals(148, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.TWO_HANDED_GREATSWORD));
		assertEquals(64, EXItemSpriteSheet.frameX(EXItemSpriteSheet.TWO_HANDED_GREATSWORD));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.TWO_HANDED_GREATSWORD));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.TWO_HANDED_GREATSWORD));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.TWO_HANDED_GREATSWORD));

		assertEquals(149, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.PALERMO_SWORD));
		assertEquals(80, EXItemSpriteSheet.frameX(EXItemSpriteSheet.PALERMO_SWORD));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.PALERMO_SWORD));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.PALERMO_SWORD));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.PALERMO_SWORD));

		assertEquals(150, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.MERCURY_BLADE));
		assertEquals(96, EXItemSpriteSheet.frameX(EXItemSpriteSheet.MERCURY_BLADE));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.MERCURY_BLADE));
		assertEquals(15, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.MERCURY_BLADE));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.MERCURY_BLADE));

		assertEquals(151, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.AUXILIARY_CORE));
		assertEquals(112, EXItemSpriteSheet.frameX(EXItemSpriteSheet.AUXILIARY_CORE));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.AUXILIARY_CORE));
		assertEquals(14, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.AUXILIARY_CORE));
		assertEquals(15, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.AUXILIARY_CORE));

		assertEquals(152, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.HUNDRED_TON_HAMMER));
		assertEquals(128, EXItemSpriteSheet.frameX(EXItemSpriteSheet.HUNDRED_TON_HAMMER));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.HUNDRED_TON_HAMMER));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.HUNDRED_TON_HAMMER));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.HUNDRED_TON_HAMMER));

		assertEquals(153, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.ORACLE_TERMINAL));
		assertEquals(144, EXItemSpriteSheet.frameX(EXItemSpriteSheet.ORACLE_TERMINAL));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.ORACLE_TERMINAL));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.ORACLE_TERMINAL));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.ORACLE_TERMINAL));

		assertEquals(154, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(155, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(176, EXItemSpriteSheet.frameX(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(160, EXItemSpriteSheet.frameX(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(14, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(14, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.DEMON_TAIL_WHIP));
		assertEquals(156, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
		assertEquals(192, EXItemSpriteSheet.frameX(EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
		assertEquals(157, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.SOUL_BLADE));
		assertEquals(208, EXItemSpriteSheet.frameX(EXItemSpriteSheet.SOUL_BLADE));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.SOUL_BLADE));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.SOUL_BLADE));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.SOUL_BLADE));
	}

	@Test
	public void tierSixWeaponSpritesAreCrispAndLeaveRemainingCellsEmpty()
			throws IOException {
		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		assertCrispSprite(sheet, 144, 1, 0, 15, 15, 8);
		assertCrispSprite(sheet, 145, 0, 0, 14, 15, 8);
		assertCrispSprite(sheet, 146, 0, 0, 14, 15, 8);
		assertCrispSprite(sheet, 149, 0, 0, 15, 15, 6);
		assertCrispSprite(sheet, 150, 0, 0, 14, 15, 8);
		assertCrispSprite(sheet, 151, 0, 1, 13, 14, 8);
		assertCrispSprite(sheet, 152, 0, 0, 15, 15, 7);
		assertCrispSprite(sheet, 153, 0, 0, 15, 15, 9);
		assertCrispSprite(sheet, 154, 0, 0, 13, 13, 8);
		assertCrispSprite(sheet, 155, 0, 1, 14, 15, 10);
		assertCrispSprite(sheet, 156, 0, 0, 15, 15, 10);
		assertCrispSprite(sheet, 157, 0, 0, 15, 15, 6);
		for (int index = 161; index < 208; index++) assertTransparentCell(sheet, index);
	}

	@Test
	public void oracleTerminalUsesACrispCaduceusCell() throws IOException {
		assertEquals(153, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.ORACLE_TERMINAL));
		assertEquals(144, EXItemSpriteSheet.frameX(EXItemSpriteSheet.ORACLE_TERMINAL));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.ORACLE_TERMINAL));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.ORACLE_TERMINAL));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.ORACLE_TERMINAL));
		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		assertCrispSprite(sheet, 153, 0, 0, 15, 15, 9);
	}

	@Test
	public void venomousSickleUsesTheDedicatedCrispTierSixCell() throws IOException {
		assertEquals(155, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(176, EXItemSpriteSheet.frameX(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.VENOMOUS_SICKLE));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.VENOMOUS_SICKLE));
		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		assertCrispSprite(sheet, 155, 0, 1, 14, 15, 10);
		assertCrispSprite(sheet, 156, 0, 0, 15, 15, 10);
		assertCrispSprite(sheet, 157, 0, 0, 15, 15, 6);
		for (int index = 161; index < 208; index++) assertTransparentCell(sheet, index);
	}

	@Test
	public void radiantGoldHalberdUsesItsDedicatedTierSixCell() throws IOException {
		assertEquals(156, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
		assertEquals(192, EXItemSpriteSheet.frameX(EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
		assertEquals(144, EXItemSpriteSheet.frameY(EXItemSpriteSheet.RADIANT_GOLD_HALBERD));
		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		assertCrispSprite(sheet, 156, 0, 0, 15, 15, 10);
		assertCrispSprite(sheet, 157, 0, 0, 15, 15, 6);
		for (int index = 161; index < 208; index++) assertTransparentCell(sheet, index);
	}

	@Test
	public void tierSixMissileWeaponRowFollowsThreeReservedBlankRows() throws IOException {
		assertEquals(208,
				EXItemSpriteSheet.frameFor(EXItemSpriteSheet.GUNGNIR));
		assertEquals(0,
				EXItemSpriteSheet.frameX(EXItemSpriteSheet.GUNGNIR));
		assertEquals(208,
				EXItemSpriteSheet.frameY(EXItemSpriteSheet.GUNGNIR));
		assertEquals(16,
				EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.GUNGNIR));
		assertEquals(16,
				EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.GUNGNIR));

		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		for (int index = 161; index < 208; index++) assertTransparentCell(sheet, index);
		assertPixelArtCell(sheet, 208, 0, 0, 15, 15, 8);
		assertEquals(209,
				EXItemSpriteSheet.frameFor(EXItemSpriteSheet.PORTABLE_BLACK_HOLE));
		assertEquals(16,
				EXItemSpriteSheet.frameX(EXItemSpriteSheet.PORTABLE_BLACK_HOLE));
		assertEquals(208,
				EXItemSpriteSheet.frameY(EXItemSpriteSheet.PORTABLE_BLACK_HOLE));
		assertEquals(15,
				EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.PORTABLE_BLACK_HOLE));
		assertEquals(15,
				EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.PORTABLE_BLACK_HOLE));
		assertPortableBlackHoleCell(sheet);
		for (int index = 210; index < 224; index++) assertTransparentCell(sheet, index);
	}

	private static void assertPortableBlackHoleCell(BufferedImage sheet) {
		Set<String> rgba = new HashSet<>();
		int[] alphaHistogram = new int[256];
		int visible = 0;
		int minX = 16, minY = 16, maxX = -1, maxY = -1;
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (Exception e) {
			throw new AssertionError(e);
		}

		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int argb = sheet.getRGB(16 + x, 208 + y);
				int alpha = (argb >>> 24) & 0xFF;
				int red = (argb >>> 16) & 0xFF;
				int green = (argb >>> 8) & 0xFF;
				int blue = argb & 0xFF;
				alphaHistogram[alpha]++;
				digest.update((byte) alpha);
				digest.update((byte) red);
				digest.update((byte) green);
				digest.update((byte) blue);
				if (alpha > 0) {
					visible++;
					rgba.add(alpha + "," + red + "," + green + "," + blue);
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}

		assertEquals(153, visible);
		assertEquals(11, rgba.size());
		assertEquals(0, minX);
		assertEquals(0, minY);
		assertEquals(14, maxX);
		assertEquals(14, maxY);
		assertEquals(103, alphaHistogram[0]);
		assertEquals(68, alphaHistogram[102]);
		assertEquals(48, alphaHistogram[179]);
		assertEquals(8, alphaHistogram[204]);
		assertEquals(8, alphaHistogram[224]);
		assertEquals(8, alphaHistogram[236]);
		assertEquals(3, alphaHistogram[244]);
		assertEquals(1, alphaHistogram[248]);
		assertEquals(9, alphaHistogram[255]);
		StringBuilder hex = new StringBuilder();
		for (byte value : digest.digest()) hex.append(String.format("%02X", value));
		assertEquals("E3940B0AF8BA6A1F107E9A04DEE2AACB5748E0ABF6B9A40631B932DD7E27BC47",
				hex.toString());
	}

	@Test
	public void tierSixWeaponCellsUseCrispPixels()
			throws IOException {
		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		assertCrispSprite(sheet, 148, 0, 1, 14, 15, 8);
		assertCrispSprite(sheet, 156, 0, 0, 15, 15, 10);
		assertCrispSprite(sheet, 157, 0, 0, 15, 15, 6);
		for (int index = 161; index < 208; index++) assertTransparentCell(sheet, index);
	}

	@Test
	public void lakeSwordCellsPreserveSourceGeometryAndAlpha() throws Exception {
		assertEquals(158, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.LAKE_SWORD));
		assertEquals(159, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.LAKE_SWORD_SHEATHED));
		assertEquals(288, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.LAKE_SWORD_SCABBARD));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.LAKE_SWORD));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.LAKE_SWORD_SHEATHED));
		assertEquals(14, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.LAKE_SWORD_SCABBARD));
		assertEquals(14, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.LAKE_SWORD_SCABBARD));

		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		assertCellDigest(sheet, 158, "7470907c7dd0280afc4d65a60ddac72ebffd522f1a5572f820f4a55c0674a571");
		assertCellDigest(sheet, 159, "bfc1fafb1730ce971887857d53eda47f9c2b637712900d527110afa7e3e4c96a");
		assertCellDigest(sheet, 288, "c1b29389a6a62414adfb4253b31286b55b178f8225e8d4b34359715577da2398");
		assertEquals(74, alphaCount(sheet, 158, 102));
		assertEquals(51, alphaCount(sheet, 159, 102));
		assertEquals(31, alphaCount(sheet, 288, 102));
		for (int index = 161; index < 208; index++) assertTransparentCell(sheet, index);
	}

	@Test
	public void mountainGuardUsesFirstCellOfTierSixMeleeExpansionArea()
			throws IOException {
		assertEquals(160, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.MOUNTAIN_GUARD));
		assertEquals(0, EXItemSpriteSheet.frameX(EXItemSpriteSheet.MOUNTAIN_GUARD));
		assertEquals(160, EXItemSpriteSheet.frameY(EXItemSpriteSheet.MOUNTAIN_GUARD));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.MOUNTAIN_GUARD));
		assertEquals(16, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.MOUNTAIN_GUARD));

		BufferedImage sheet = ImageIO.read(exItemSpritePath().toFile());
		assertPixelArtCell(sheet, 160, 0, 0, 15, 15, 6);
		String[] expected = {
				".....OOOOOO.....", "...OOOMMMMOOO...", "..OOMMMMMMMMOO..",
				".OOMMLLSSLLMMOO.", ".OMMLLSSSSLLMMO.", "OMMMLLMMMMLLMMMO",
				"OMMMMMMCCMMMMMMO", "OMMMMCCCCCCMMMMO", "OMMCCMMCCMMCCMMO",
				".OMCMMMMMMMMCMO.", ".OMMCMDDDDMCMMO.", "..OMMCDDDDCMMO..",
				"..OOMMCCCCMMOO..", "...OOMMMMMMOO...", "....OOMMMMOO....",
				"......OOOO......"
		};
		int[] palette = {0x00000000, 0xFF2B2731, 0xFF3B444B, 0xFF5B6970,
				0xFF849497, 0xFFDAE8E5, 0xFF59D3DC};
		char[] symbols = {'.', 'O', 'D', 'M', 'L', 'S', 'C'};
		for (int y = 0; y < 16; y++) {
			StringBuilder actual = new StringBuilder();
			for (int x = 0; x < 16; x++) {
				int argb = sheet.getRGB(x, 160 + y);
				int found = -1;
				for (int i = 0; i < palette.length; i++) if (palette[i] == argb) found = i;
				assertTrue("unknown mountain guard color at " + x + "," + y, found >= 0);
				actual.append(symbols[found]);
			}
			assertEquals("mountain guard row " + y, expected[y], actual.toString());
		}
		for (int index = 161; index < 208; index++) assertTransparentCell(sheet, index);
	}

	private static void assertTransparentCell(BufferedImage sheet, int index) {
		int cellX = index % 16 * 16;
		int cellY = index / 16 * 16;
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				assertEquals("reserved tier-six cell changed at " + (cellX + x) + "," + (cellY + y),
						0, sheet.getRGB(cellX + x, cellY + y) >>> 24);
			}
		}
	}

	private static void assertCellDigest(BufferedImage sheet, int index, String expected)
			throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		int cellX = index % 16 * 16;
		int cellY = index / 16 * 16;
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int argb = sheet.getRGB(cellX + x, cellY + y);
				digest.update((byte) ((argb >>> 16) & 0xFF));
				digest.update((byte) ((argb >>> 8) & 0xFF));
				digest.update((byte) (argb & 0xFF));
				digest.update((byte) ((argb >>> 24) & 0xFF));
			}
		}
		StringBuilder actual = new StringBuilder();
		for (byte value : digest.digest()) actual.append(String.format("%02x", value));
		assertEquals(expected, actual.toString());
	}

	private static int alphaCount(BufferedImage sheet, int index, int expectedAlpha) {
		int count = 0;
		int cellX = index % 16 * 16;
		int cellY = index / 16 * 16;
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				if (((sheet.getRGB(cellX + x, cellY + y) >>> 24) & 0xFF) == expectedAlpha) count++;
			}
		}
		return count;
	}

	private static void assertPixelArtCell(BufferedImage sheet, int index,
			int expectedMinX, int expectedMinY, int expectedMaxX, int expectedMaxY,
			int maxColors) {
		Set<Integer> colors = new HashSet<>();
		int minX = 16;
		int minY = 16;
		int maxX = -1;
		int maxY = -1;
		int cellX = index % 16 * 16;
		int cellY = index / 16 * 16;

		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int argb = sheet.getRGB(cellX + x, cellY + y);
				int alpha = argb >>> 24;
				assertTrue("pixel art must not use partial alpha at " + x + "," + y,
						alpha == 0 || alpha == 255);
				if (alpha == 255) {
					colors.add(argb);
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}

		assertFalse("item cell must contain a sprite", colors.isEmpty());
		assertEquals(expectedMinX, minX);
		assertEquals(expectedMinY, minY);
		assertEquals(expectedMaxX, maxX);
		assertEquals(expectedMaxY, maxY);
		assertTrue("small item sprite palette", colors.size() <= maxColors);
	}

	private static void assertCrispSprite(BufferedImage sheet, int index,
			int expectedMinX, int expectedMinY, int expectedMaxX, int expectedMaxY,
			int maxColors) {
		Set<Integer> colors = new HashSet<>();
		int minX = 16;
		int minY = 16;
		int maxX = -1;
		int maxY = -1;
		int cellX = index % 16 * 16;
		int cellY = index / 16 * 16;

		// Only encoded pixels are rendered. Padding outside the measured frame is
		// intentionally excluded from crispness and palette checks.
		for (int y = 0; y <= expectedMaxY; y++) {
			for (int x = 0; x <= expectedMaxX; x++) {
				int argb = sheet.getRGB(cellX + x, cellY + y);
				int alpha = argb >>> 24;
				assertTrue("partial alpha in index " + index + " at " + x + "," + y,
						alpha == 0 || alpha == 255);
				if (alpha == 255) {
					colors.add(argb);
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}

		assertFalse("index " + index + " must contain a sprite", colors.isEmpty());
		assertEquals(expectedMinX, minX);
		assertEquals(expectedMinY, minY);
		assertEquals(expectedMaxX, maxX);
		assertEquals(expectedMaxY, maxY);
		assertTrue("small item sprite palette for index " + index, colors.size() <= maxColors);
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

	private static Path exItemSpritePath() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		return coreDirectory.resolve("src/main/assets/sprites/ex_items.png");
	}
}
