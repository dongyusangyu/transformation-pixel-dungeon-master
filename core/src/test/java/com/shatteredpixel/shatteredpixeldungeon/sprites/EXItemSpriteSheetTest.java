package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EXItemSpriteSheetTest {

	@Test
	public void extendedFramesSelectTheirOwnAtlas() {
		assertFalse(EXItemSpriteSheet.isEX(ItemSpriteSheet.SEAL));
		assertFalse(EXItemSpriteSheet.isEX(-1));
		assertEquals(Assets.Sprites.ITEMS, EXItemSpriteSheet.textureFor(ItemSpriteSheet.SEAL));
		assertEquals(Assets.Sprites.EX_ITEMS, EXItemSpriteSheet.textureFor(EXItemSpriteSheet.SEAL));
		assertEquals(ItemSpriteSheet.SEAL, EXItemSpriteSheet.frameFor(EXItemSpriteSheet.SEAL));
		assertEquals(-1, EXItemSpriteSheet.frameFor(-1));
	}

	@Test
	public void establishedItemIndicesDoNotShiftWhenArtChanges() {
		assertIndex(EXItemSpriteSheet.MUISCA_GOLDEN_RAFT, 0);
		assertIndex(EXItemSpriteSheet.DJENNE_TERRACOTTA_FIGURE, 19);
		assertIndex(EXItemSpriteSheet.ELF_WINE, 20);
		assertIndex(EXItemSpriteSheet.SCROLL_EXTRACTION, 32);
		assertIndex(EXItemSpriteSheet.RAID_ACCESS_CARD, 33);
		assertIndex(EXItemSpriteSheet.META_INFUSE, 48);
		assertIndex(EXItemSpriteSheet.SACRED_BLADE_BOOMERANG, 96);
		assertIndex(EXItemSpriteSheet.DEATH_KNIGHT_SLASH, 97);
		assertIndex(EXItemSpriteSheet.GREAT_GREAT_GREATSWORD, 144);
		assertIndex(EXItemSpriteSheet.LAKE_SWORD_SHEATHED, 159);
		assertIndex(EXItemSpriteSheet.MOUNTAIN_GUARD, 160);
		assertIndex(EXItemSpriteSheet.GUNGNIR, 208);
		assertIndex(EXItemSpriteSheet.PORTABLE_BLACK_HOLE, 209);
		assertIndex(EXItemSpriteSheet.PRECOGNITIVE_EYE, 272);
		assertIndex(EXItemSpriteSheet.NECRONOMICON, 273);
		assertIndex(EXItemSpriteSheet.LAKE_SWORD_SCABBARD, 288);
		assertIndex(EXItemSpriteSheet.HIKING_BACKPACK, 480);
	}

	@Test
	public void insetItemUsesItsTrueStartingPixel() {
		assertEquals(66, EXItemSpriteSheet.frameX(EXItemSpriteSheet.ELF_WINE));
		assertEquals(16, EXItemSpriteSheet.frameY(EXItemSpriteSheet.ELF_WINE));
		assertEquals(12, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.ELF_WINE));
		assertEquals(15, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.ELF_WINE));
		assertEquals(0, EXItemSpriteSheet.frameX(EXItemSpriteSheet.SCROLL_EXTRACTION));
		assertEquals(32, EXItemSpriteSheet.frameY(EXItemSpriteSheet.SCROLL_EXTRACTION));
		assertEquals(16, EXItemSpriteSheet.frameX(EXItemSpriteSheet.RAID_ACCESS_CARD));
		assertEquals(32, EXItemSpriteSheet.frameY(EXItemSpriteSheet.RAID_ACCESS_CARD));
		assertEquals(16, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.RAID_ACCESS_CARD));
		assertEquals(11, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.RAID_ACCESS_CARD));
		assertEquals(0, EXItemSpriteSheet.frameX(EXItemSpriteSheet.SACRED_BLADE_BOOMERANG));
		assertEquals(96, EXItemSpriteSheet.frameY(EXItemSpriteSheet.SACRED_BLADE_BOOMERANG));
		assertEquals(14, EXItemSpriteSheet.frameWidth(EXItemSpriteSheet.SACRED_BLADE_BOOMERANG));
		assertEquals(14, EXItemSpriteSheet.frameHeight(EXItemSpriteSheet.SACRED_BLADE_BOOMERANG));
	}

	@Test
	public void sacredBladeProjectileKeepsItsSpinWithTheNewFrame() throws Exception {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
		Path java = core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon");
		String spell = new String(Files.readAllBytes(java.resolve("actors/hero/spells/Sacred_Blade.java")),
				StandardCharsets.UTF_8);
		String missile = new String(Files.readAllBytes(java.resolve("sprites/MissileSprite.java")),
				StandardCharsets.UTF_8);
		assertTrue(spell.contains("image = EXItemSpriteSheet.SACRED_BLADE_BOOMERANG"));
		assertTrue(missile.contains("ANGULAR_SPEEDS.put(Sacred_Blade.HolBladeVFX.class,   1440)"));
	}

	@Test
	public void itemSpriteUsesEncodedGeometryAndKeepsBaseFallback() throws Exception {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
		String source = new String(Files.readAllBytes(core.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSprite.java")),
				StandardCharsets.UTF_8);
		int start = source.indexOf("public void frame( int image )");
		int end = source.indexOf("public static int pick", start);
		String method = source.substring(start, end);
		assertTrue(method.contains("EXItemSpriteSheet.frameX(image)"));
		assertTrue(method.contains("EXItemSpriteSheet.frameY(image)"));
		assertTrue(method.contains("EXItemSpriteSheet.frameWidth(image)"));
		assertTrue(method.contains("EXItemSpriteSheet.frameHeight(image)"));
		assertTrue(method.contains("targetTexture.uvRect(left, top, left + width, top + height)"));
		assertTrue(method.contains("if (itemFrame == null)"));
		assertTrue(method.contains("image = ItemSpriteSheet.SOMETHING"));
	}

	private static void assertIndex(int sprite, int index) {
		assertTrue(EXItemSpriteSheet.isEX(sprite));
		assertEquals(index, EXItemSpriteSheet.frameFor(sprite));
	}
}
