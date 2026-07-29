package com.shatteredpixel.shatteredpixeldungeon.items.treasures;

import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class TreasureCatalogTest {

	private static final Class<? extends Treasures>[] CLASSES = new Class[]{
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

	private static final int[] IMAGES = {
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
			EXItemSpriteSheet.DJENNE_TERRACOTTA_FIGURE
	};

	private static final Treasures.CollectionRarity[] COLLECTION_RARITIES = {
			Treasures.CollectionRarity.TOP,
			Treasures.CollectionRarity.RARE,
			Treasures.CollectionRarity.RARE,
			Treasures.CollectionRarity.RARE,
			Treasures.CollectionRarity.UNCOMMON,
			Treasures.CollectionRarity.UNCOMMON,
			Treasures.CollectionRarity.UNCOMMON,
			Treasures.CollectionRarity.UNCOMMON,
			Treasures.CollectionRarity.UNCOMMON,
			Treasures.CollectionRarity.UNCOMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON,
			Treasures.CollectionRarity.COMMON
	};

	private static final int[] VALUES = {
			5000,
			4500, 4000, 3500,
			2500, 2300, 2100, 1900, 1700, 1500,
			1400, 1300, 1200, 1100, 1000, 900, 800, 600, 500, 500
	};

	@Test
	public void catalogMetadataMatchesTheApprovedOrder() throws Exception {
		assertEquals(20, CLASSES.length);
		for (int i = 0; i < CLASSES.length; i++) {
			Treasures treasure = CLASSES[i].getDeclaredConstructor().newInstance();
			assertTrue(Treasures.class.isAssignableFrom(CLASSES[i]));
			assertEquals("image at index " + i, IMAGES[i], treasure.image);
			assertEquals("collection rarity at index " + i,
					COLLECTION_RARITIES[i], treasure.collectionRarity());
			treasure.setRarity(Treasures.Rarity.COMMON);
			assertEquals("value at index " + i, VALUES[i], treasure.value());
		}
	}

	@Test
	public void everyTreasureHasDefaultAndChineseNameAndDescription() throws Exception {
		Properties defaults = loadMessages("items.properties");
		Properties chinese = loadMessages("items_zh.properties");

		for (Class<? extends Treasures> treasureClass : CLASSES) {
			String itemKey = "items.treasures."
					+ treasureClass.getSimpleName().toLowerCase(Locale.ENGLISH);
			assertMessage(defaults, itemKey + ".name");
			assertMessage(defaults, itemKey + ".desc");
			assertMessage(chinese, itemKey + ".name");
			assertMessage(chinese, itemKey + ".desc");
		}
	}

	@Test
	public void treasureValueLabelExistsAndChineseFormatIsExact() throws Exception {
		Properties defaults = loadMessages("items.properties");
		Properties chinese = loadMessages("items_zh.properties");
		String key = "items.treasures.treasures.value";

		assertMessage(defaults, key);
		assertEquals("价值:%s", chinese.getProperty(key));
	}

	private static void assertMessage(Properties messages, String key) {
		String value = messages.getProperty(key);
		assertNotNull("missing message: " + key, value);
		assertTrue("blank message: " + key, !value.trim().isEmpty());
	}

	private static Properties loadMessages(String fileName) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/assets/messages/items").resolve(fileName);
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}
}
