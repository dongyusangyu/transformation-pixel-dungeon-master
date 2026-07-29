package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestPotionTreasureTest {

	private static String source() throws Exception {
		Path path = Paths.get("src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestPotion.java");
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	@Test
	public void treasureIsTheSixteenthCategory() throws Exception {
		String source = source();
		assertTrue(source.contains("private static final int TREASURE_CATEGORY = 15;"));
		assertTrue(source.contains("return TREASURE_CATEGORY;"));
		assertTrue(source.contains("case TREASURE_CATEGORY: return treasureList.get(id);"));
		assertTrue(source.contains("case TREASURE_CATEGORY: return treasureList.toArray().length-1;"));
	}

	@Test
	public void treasureCategoryUsesCatalogOrderAndExtendedSheetIcon() throws Exception {
		String source = source();
		assertTrue(source.contains("ArrayList<Class<? extends Treasures>> treasureList"));
		assertTrue(source.contains("Catalog.TREASURES.items()"));
		assertTrue(source.contains("case TREASURE_CATEGORY: return EXItemSpriteSheet.MUISCA_GOLDEN_RAFT;"));
	}

	@Test
	public void categoryAndTreasureIconsAreRenderedThroughItemSprite() throws Exception {
		String source = source();
		assertTrue(source.contains("Image im = new ItemSprite(idToCategoryImage(i));"));
		assertTrue(source.contains("new ItemSprite(Objects.requireNonNull(Reflection.newInstance(treasureList.get(i))))"));
		assertFalse(source.contains("im.frame(ItemSpriteSheet.film.get(idToCategoryImage(i)))"));
	}

	@Test
	public void generatedTreasureKeepsConstructorRandomQuality() throws Exception {
		String source = source();
		int start = source.indexOf("private void createItem()");
		int end = source.indexOf("\n    }", start);
		String createItem = source.substring(start, end);
		assertTrue(createItem.contains("Reflection.newInstance(idToItem(selected))"));
		assertFalse(createItem.contains("setRarity"));
	}
}
