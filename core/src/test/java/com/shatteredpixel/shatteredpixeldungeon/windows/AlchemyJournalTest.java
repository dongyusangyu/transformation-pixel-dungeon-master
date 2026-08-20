package com.shatteredpixel.shatteredpixeldungeon.windows;

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

public class AlchemyJournalTest {

	@Test
	public void weaponCraftingIsRegisteredAsTheTenthAlchemyGuidePage()
			throws IOException {
		String document = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/journal/Document.java");
		String journal = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");

		assertTrue(document.contains(
				"ALCHEMY_GUIDE.pagesStates.put(\"Weapon_Crafting\""));
		assertTrue(journal.contains("NUM_BUTTONS = 10"));
	}

	@Test
	public void everyAlchemyPageKeepsAnIndependentScrollPosition()
			throws IOException {
		String journal = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");

		assertTrue(journal.contains("scrollPositions = new float[NUM_BUTTONS]"));
		assertTrue(journal.contains("scrollPositions[currentPageIdx]"));
		assertTrue(journal.contains("list.scrollTo(0, scrollPositions[currentPageIdx])"));
	}

	@Test
	public void weaponCraftingPageUsesRegisteredRecipesWithoutQuickFill()
			throws IOException {
		String quickRecipe = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/ui/QuickRecipe.java");
		String recipes = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/Recipe.java");
		String alchemyScene = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/scenes/AlchemyScene.java");

		assertTrue(quickRecipe.contains("case 9:"));
		assertTrue(quickRecipe.contains("Recipe.weaponRecipes()"));
		assertTrue(quickRecipe.contains("quickAlchemyEnabled = false"));
		assertTrue(recipes.contains("GreatGreatGreatsword.class"));
		assertTrue(recipes.contains("Gungnir.class"));
		assertTrue(recipes.contains("public static ArrayList<WeaponRecipe> weaponRecipes()"));
		assertTrue(alchemyScene.contains("shouldDetachWholeStack(item)"));
		assertTrue(alchemyScene.contains("shouldDetachWholeStack(finding)"));
	}

	@Test
	public void weaponCraftingMessagesExistInDefaultAndChinese() throws IOException {
		Properties defaults = loadJournalMessages("journal.properties");
		Properties chinese = loadJournalMessages("journal_zh.properties");
		String prefix = "journal.document.alchemy_guide.weapon_crafting.";

		assertEquals("Weapon Crafting", defaults.getProperty(prefix + "title"));
		assertEquals("武器合成", chinese.getProperty(prefix + "title"));
		assertTrue(defaults.getProperty(prefix + "body").contains("weapons"));
		assertTrue(chinese.getProperty(prefix + "body").contains("武器"));
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
