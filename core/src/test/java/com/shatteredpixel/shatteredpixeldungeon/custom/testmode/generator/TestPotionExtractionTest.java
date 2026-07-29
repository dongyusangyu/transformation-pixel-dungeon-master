package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.SealShard;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfExtraction;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPotionExtractionTest {

	@Test
	public void extractionScrollIsDeclaredLastInMiscConsumables() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java");
		int catalogStart = source.indexOf("MISC_CONSUMABLES.addItems(");
		int catalogEnd = source.indexOf(");", catalogStart);
		String declaration = source.substring(catalogStart, catalogEnd);

		assertTrue(declaration.trim().endsWith("ScrollOfExtraction.class"));
	}

	@Test
	public void testPotionClassifiesExtractionScrollAsGeneratableMisc() throws Exception {
		ArrayList<Class<? extends Item>> misc = new ArrayList<>();
		ArrayList<Class<? extends Item>> remains = new ArrayList<>();

		TestPotion.splitMiscCatalogItems(Arrays.asList(
				Item.class,
				SealShard.class,
				ScrollOfMetamorphosis.class,
				ScrollOfExtraction.class
		), misc, remains);

		assertEquals(ScrollOfExtraction.class, misc.get(misc.size() - 1));
		assertTrue(!remains.contains(ScrollOfExtraction.class));
		assertTrue(ScrollOfExtraction.class.getConstructor() != null);
	}

	@Test
	public void testPotionRendersMiscItemsThroughExAwareItemSprite() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestPotion.java");
		int createImageStart = source.indexOf("private void createImage()");
		int miscCaseStart = source.indexOf("case 11: default:", createImageStart);
		int miscCaseEnd = source.indexOf("case 12:", miscCaseStart);
		String miscCase = source.substring(miscCaseStart, miscCaseEnd);

		assertTrue(miscCase.contains("new ItemSprite("));
		assertTrue(!miscCase.contains("ItemSpriteSheet.film.get("));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/java").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
