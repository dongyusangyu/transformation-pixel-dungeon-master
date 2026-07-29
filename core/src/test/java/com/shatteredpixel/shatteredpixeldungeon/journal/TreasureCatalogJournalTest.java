package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.items.treasures.AlMughiraPyxis;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.BlacasEwer;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.BookOfKells;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.CholaNataraja;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.DjenneTerracottaFigure;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.DojigiriYasutsuna;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.EthiopianProcessionalCross;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.GoryeoMaebyeong;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.GreatKhanPaiza;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.GreatZimbabweBird;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.HarbavilleTriptych;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.ImperialCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.IncaGoldenLlama;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.JavaneseGoldCup;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.LewisChessQueen;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.MuiscaGoldenRaft;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.PakalJadeMask;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.RuWareBowl;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.SuttonHooHelmet;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.TurquoiseSerpent;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TreasureCatalogJournalTest {

	private static final Class<?>[] APPROVED_TREASURES = {
			MuiscaGoldenRaft.class,
			ImperialCrown.class,
			PakalJadeMask.class,
			SuttonHooHelmet.class,
			BookOfKells.class,
			CholaNataraja.class,
			DojigiriYasutsuna.class,
			TurquoiseSerpent.class,
			RuWareBowl.class,
			IncaGoldenLlama.class,
			LewisChessQueen.class,
			HarbavilleTriptych.class,
			AlMughiraPyxis.class,
			BlacasEwer.class,
			GreatKhanPaiza.class,
			GoryeoMaebyeong.class,
			JavaneseGoldCup.class,
			EthiopianProcessionalCross.class,
			GreatZimbabweBird.class,
			DjenneTerracottaFigure.class
	};

	@Test
	public void treasureCatalogContainsApprovedClassesInSpriteOrder() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java");
		int start = source.indexOf("TREASURES.addItems(");
		int end = source.indexOf(");", start);

		assertTrue("missing TREASURES catalog declaration", start >= 0 && end > start);
		String declaration = source.substring(start, end);
		int previous = -1;
		for (Class<?> treasure : APPROVED_TREASURES) {
			int current = declaration.indexOf(treasure.getSimpleName() + ".class");
			assertTrue("missing treasure " + treasure.getSimpleName(), current >= 0);
			assertTrue("treasure order changed at " + treasure.getSimpleName(),
					current > previous);
			previous = current;
		}
	}

	@Test
	public void catalogPersistsTreasureAcquisitionCounts() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java");

		assertTrue(source.contains("bundle.put( CATALOG_USES, storeUses );"));
		assertTrue(source.contains("int[] uses = bundle.getIntArray(CATALOG_USES);"));
		assertTrue(source.contains("cat.useCount.put(classes[i], uses[i]);"));
	}

	@Test
	public void testModeDoesNotCountTreasureAcquisitions() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java");
		int start = source.indexOf("public static void countUses");
		int end = source.indexOf("private static final String CATALOG_CLASSES", start);
		String countMethod = source.substring(start, end);

		assertTrue(countMethod.contains("!Dungeon.isChallenged(Challenges.TEST_MODE)"));
	}

	@Test
	public void itemCollectCountsTreasuresOnSuccessfulNonStackingPath() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/Item.java");
		int nonStackingSuccess = source.indexOf("items.add( this );");
		int treasureCount = source.lastIndexOf(
				"Catalog.countUse(getClass())", nonStackingSuccess);

		assertTrue("treasure count must precede final insertion", treasureCount >= 0);
		assertTrue("treasure count must be on the final successful path",
				treasureCount < nonStackingSuccess);
		assertTrue(source.substring(
				Math.max(0, treasureCount - 160), treasureCount)
				.contains("this instanceof Treasures"));
	}

	@Test
	public void treasureCatalogTitleExistsInDefaultAndChineseMessages() throws IOException {
		String key = "journal.catalog.treasures.title";
		assertEquals("treasures", loadJournalMessages("journal.properties").getProperty(key));
		assertEquals("藏品", loadJournalMessages("journal_zh.properties").getProperty(key));
	}

	private static Properties loadJournalMessages(String fileName) throws IOException {
		Path source = coreDirectory()
				.resolve("src/main/assets/messages/journal")
				.resolve(fileName);
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path source = coreDirectory().resolve("src/main/java").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

	private static Path coreDirectory() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		return Files.isDirectory(coreDirectory) ? coreDirectory : workingDirectory;
	}
}
