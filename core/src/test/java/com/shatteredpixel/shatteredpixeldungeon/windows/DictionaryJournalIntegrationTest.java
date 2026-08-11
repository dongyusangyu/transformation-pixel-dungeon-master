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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DictionaryJournalIntegrationTest {

	@Test
	public void dictionaryIsAReusableLazyLoadedJournalTab() throws IOException {
		String journal = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");
		String dictionary = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictBook.java");

		assertTrue(journal.contains("class DictionaryTab extends DictBook.DictTab"));
		assertTrue(journal.contains("private DictionaryTab dictionaryTab"));
		assertTrue(journal.contains("ensureDictionaryLoaded()"));
		assertTrue(journal.contains("ItemSpriteSheet.GUIDE_PAGE"));
		assertTrue(dictionary.contains("public void updateList()"));
		assertTrue(dictionary.contains("itemButtons[i].setRect(x +"));
		assertTrue(dictionary.contains("list.setRect(x,"));
	}

	@Test
	public void oldDictionaryEntrancesOpenTheJournalDictionaryTab() throws IOException {
		String journal = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");
		String gameWindow = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/windows/WndGame.java");
		String dictionary = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictBook.java");

		assertTrue(journal.contains("public static final int DICTIONARY_TAB_INDEX = 5"));
		assertTrue(journal.contains("public static WndJournal dictionaryPage()"));
		assertTrue(gameWindow.contains("GameScene.show(WndJournal.dictionaryPage())"));
		assertTrue(dictionary.contains("GameScene.show(WndJournal.dictionaryPage())"));
		assertFalse(gameWindow.contains("new DictBook.WndDict()"));
	}

	@Test
	public void dictionaryJournalTabHasDefaultAndChineseTitles() throws IOException {
		Properties defaults = loadWindowMessages("windows.properties");
		Properties chinese = loadWindowMessages("windows_zh.properties");
		String key = "windows.wndjournal$dictionarytab.title";

		assertEquals("Reference", defaults.getProperty(key));
		assertEquals("资料", chinese.getProperty(key));
	}

	@Test
	public void titleJournalSceneHasAnIndependentDictionarySection() throws IOException {
		String journalScene = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/scenes/JournalScene.java");

		assertTrue(journalScene.contains("private static final int TAB_COUNT = 5"));
		assertTrue(journalScene.contains("case 4:"));
		assertTrue(journalScene.contains("WndJournal.DictionaryTab dictionary"));
		assertTrue(journalScene.contains("dictionary.updateList()"));
		assertTrue(journalScene.contains("Messages.get(WndJournal.DictionaryTab.class, \"title\")"));
		assertTrue(journalScene.contains("ItemSpriteSheet.GUIDE_PAGE"));
	}

	@Test
	public void dictionaryDetailsOpenInGameAndTitleScenes() throws IOException {
		String dictionary = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictBook.java");

		assertTrue(dictionary.contains("ShatteredPixelDungeon.scene() instanceof GameScene"));
		assertTrue(dictionary.contains("ShatteredPixelDungeon.scene().addToFront(window)"));
		assertTrue(dictionary.contains("showWindow(new WndScrollTitledMessage"));
		assertFalse(dictionary.contains("GameScene.show(new WndScrollTitledMessage"));
	}

	private static Properties loadWindowMessages(String fileName) throws IOException {
		Path source = coreDirectory()
				.resolve("src/main/assets/messages/windows")
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
