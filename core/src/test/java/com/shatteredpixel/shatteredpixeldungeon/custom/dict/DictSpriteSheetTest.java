package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DictSpriteSheetTest {

	@Test
	public void allTierSixWeaponIconsUseTheItemSpritePath() {
		int[] tierSixWeapons = {
				EXItemSpriteSheet.GREAT_GREAT_GREATSWORD,
				EXItemSpriteSheet.SAKURA_BLOSSOM,
				EXItemSpriteSheet.CHAIN_MACE,
				EXItemSpriteSheet.TWO_HANDED_GREATSWORD,
				EXItemSpriteSheet.PALERMO_SWORD,
				EXItemSpriteSheet.HUNDRED_TON_HAMMER,
				EXItemSpriteSheet.ORACLE_TERMINAL,
				EXItemSpriteSheet.DEMON_TAIL_WHIP,
				EXItemSpriteSheet.MERCURY_BLADE,
				EXItemSpriteSheet.AUXILIARY_CORE,
				EXItemSpriteSheet.GUNGNIR
		};

		for (int image : tierSixWeapons) {
			assertTrue(String.valueOf(image), DictSpriteSheet.isItemSprite(image));
		}
	}

	@Test
	public void existingDictionaryIconRangesKeepTheirOriginalRouting() {
		assertTrue(DictSpriteSheet.isItemSprite(0));
		assertFalse(DictSpriteSheet.isItemSprite(DictSpriteSheet.RANDOM_MODE));
		assertFalse(DictSpriteSheet.isItemSprite(DictSpriteSheet.AREA_TOWER));
		assertFalse(DictSpriteSheet.isItemSprite(DictSpriteSheet.BUFF_POSITIVE));
	}
}
