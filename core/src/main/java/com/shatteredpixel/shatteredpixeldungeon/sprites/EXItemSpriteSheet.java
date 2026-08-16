/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

public final class EXItemSpriteSheet {

	private static final int INDEX_MASK = 0xFFFF;
	private static final int EX_SHEET_FLAG = 1 << 16;
	private static final int DIMENSION_MASK = 0x1F;
	private static final int WIDTH_SHIFT = 17;
	private static final int HEIGHT_SHIFT = 22;
	private static final int SHEET_COLUMNS = 16;

	public static final int MUISCA_GOLDEN_RAFT           = encode(0, 14, 14);
	public static final int IMPERIAL_CROWN               = encode(1, 12, 14);
	public static final int PAKAL_JADE_MASK              = encode(2, 15, 16);
	public static final int SUTTON_HOO_HELMET            = encode(3, 15, 16);
	public static final int BOOK_OF_KELLS                = encode(4, 14, 16);
	public static final int CHOLA_NATARAJA               = encode(5, 14, 16);
	public static final int DOJIGIRI_YASUTSUNA           = encode(6, 15, 16);
	public static final int TURQUOISE_SERPENT            = encode(7, 16, 12);
	public static final int RU_WARE_BOWL                 = encode(8, 16, 13);
	public static final int INCA_GOLDEN_LLAMA            = encode(9, 14, 15);
	public static final int LEWIS_CHESS_QUEEN            = encode(10, 13, 16);
	public static final int HARBAVILLE_TRIPTYCH          = encode(11, 15, 16);
	public static final int AL_MUGHIRA_PYXIS             = encode(12, 11, 16);
	public static final int BLACAS_EWER                   = encode(13, 16, 16);
	public static final int GREAT_KHAN_PAIZA             = encode(14, 11, 16);
	public static final int GORYEO_MAEBYEONG             = encode(15, 11, 16);
	public static final int JAVANESE_GOLD_CUP            = encode(16, 15, 14);
	public static final int ETHIOPIAN_PROCESSIONAL_CROSS = encode(17, 16, 16);
	public static final int GREAT_ZIMBABWE_BIRD          = encode(18, 11, 16);
	public static final int DJENNE_TERRACOTTA_FIGURE     = encode(19, 11, 16);

	public static final int SCROLL_EXTRACTION = encode(32, 15, 14);
	public static final int META_INFUSE = encode(48, 10, 15);

	// Row 7 (indices 96-111): new tier 1-5 weapons.
	public static final int WEAPON_PLACEHOLDER = encode(96, 16, 16);

	// Row 10 (indices 144-159): new tier 6 weapons.
	public static final int GREAT_GREAT_GREATSWORD = encode(144, 16, 16);
	public static final int SAKURA_BLOSSOM = encode(145, 16, 16);
	public static final int BLOOD_SAKURA = encode(146, 16, 16);
	public static final int CHAIN_MACE = encode(147, 16, 16);
	public static final int TWO_HANDED_GREATSWORD = encode(148, 16, 16);
	public static final int PALERMO_SWORD = encode(149, 16, 16);
	public static final int MERCURY_BLADE = encode(150, 15, 16);
	public static final int AUXILIARY_CORE = encode(151, 14, 15);
	public static final int HUNDRED_TON_HAMMER = encode(152, 16, 16);
	public static final int ORACLE_TERMINAL = encode(153, 16, 16);
	public static final int DEMON_TAIL_WHIP = encode(154, 16, 16);

	// Rows 11-13 (indices 160-207) are intentionally reserved as blank spacers.

	// Row 14 (indices 208-223): new tier 6 missile weapons.
	public static final int GUNGNIR = encode(208, 16, 16);
	public static final int TIER6_MISSILE_WEAPON_PLACEHOLDER = GUNGNIR;

	public static final int SEAL = encode(ItemSpriteSheet.SEAL, 16, 16);

	private EXItemSpriteSheet() {
	}

	private static int encode(int image, int width, int height) {
		if (image < 0 || image > INDEX_MASK
				|| width < 1 || width > ItemSpriteSheet.SIZE
				|| height < 1 || height > ItemSpriteSheet.SIZE) {
			throw new IllegalArgumentException("invalid EX item sprite frame");
		}
		return image
				| EX_SHEET_FLAG
				| (width << WIDTH_SHIFT)
				| (height << HEIGHT_SHIFT);
	}

	public static boolean isEX(int image) {
		return image >= 0 && (image & EX_SHEET_FLAG) != 0;
	}

	static int frameFor(int image) {
		return isEX(image) ? image & INDEX_MASK : image;
	}

	static String textureFor(int image) {
		return isEX(image) ? Assets.Sprites.EX_ITEMS : Assets.Sprites.ITEMS;
	}

	static int frameX(int image) {
		return frameFor(image) % SHEET_COLUMNS * ItemSpriteSheet.SIZE;
	}

	static int frameY(int image) {
		return frameFor(image) / SHEET_COLUMNS * ItemSpriteSheet.SIZE;
	}

	static int frameWidth(int image) {
		return isEX(image)
				? image >> WIDTH_SHIFT & DIMENSION_MASK
				: (int) ItemSpriteSheet.film.width(image);
	}

	static int frameHeight(int image) {
		return isEX(image)
				? image >> HEIGHT_SHIFT & DIMENSION_MASK
				: (int) ItemSpriteSheet.film.height(image);
	}

}
