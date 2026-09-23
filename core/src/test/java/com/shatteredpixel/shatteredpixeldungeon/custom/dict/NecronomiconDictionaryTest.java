package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NecronomiconDictionaryTest {

    @Test
    public void dictionaryUsesTheExtensionSpriteAndArtifactIsNotInGeneratorPool() throws IOException {
        String journal = new String(Files.readAllBytes(coreDirectory().resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java")),
                StandardCharsets.UTF_8);
        assertTrue(journal.contains("artifact_necronomicon"));
        assertTrue(journal.contains("EXItemSpriteSheet.NECRONOMICON"));

        String generator = new String(Files.readAllBytes(coreDirectory().resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java")),
                StandardCharsets.UTF_8);
        assertFalse(generator.contains("Necronomicon.class"));
    }

    @Test
    public void bilingualDetailsDescribeRecipeAndTwoCastingModes() throws IOException {
        Properties defaults = loadMessages("custom.properties");
        Properties chinese = loadMessages("custom_zh.properties");
        String d = defaults.getProperty("custom.dict.dict.artifact_necronomicon_d", "");
        String z = chinese.getProperty("custom.dict.dict.artifact_necronomicon_d", "");
        assertFalse(d.isEmpty());
        assertFalse(z.isEmpty());
        assertTrue(d.contains("Corpse Dust"));
        assertTrue(d.contains("Empty ground"));
        assertTrue(d.contains("soul"));
        assertTrue(z.contains("尸尘"));
        assertTrue(z.contains("空地"));
        assertTrue(z.contains("魂缚"));
    }

    @Test
    public void normalItemTextAndJournalEntryAreComplete() throws IOException {
        Properties defaults = loadItemMessages("items.properties");
        Properties chinese = loadItemMessages("items_zh.properties");
        String[] keys = {
                "items.artifacts.necronomicon.name",
                "items.artifacts.necronomicon.ac_cast",
                "items.artifacts.necronomicon.prompt",
                "items.artifacts.necronomicon.no_charge",
                "items.artifacts.necronomicon.invalid_target",
                "items.artifacts.necronomicon.no_space",
                "items.artifacts.necronomicon.desc",
                "items.artifacts.necronomicon.desc_equipped",
                "items.artifacts.necronomicon.desc_cursed",
                "items.artifacts.necronomicon$bookrecharge.levelup",
                "items.artifacts.necronomicon$soulbound.name",
                "items.artifacts.necronomicon$soulbound.desc"
        };
        for (String key : keys) {
            assertFalse(key, defaults.getProperty(key, "").isEmpty());
            assertFalse(key, chinese.getProperty(key, "").isEmpty());
        }
        String journal = new String(Files.readAllBytes(coreDirectory().resolve(
                "src/main/assets/messages/journal/journal.properties")), StandardCharsets.UTF_8);
        String journalZh = new String(Files.readAllBytes(coreDirectory().resolve(
                "src/main/assets/messages/journal/journal_zh.properties")), StandardCharsets.UTF_8);
        assertTrue(journal.contains("Necronomicon") && journal.contains("Corpse Dust")
                && journal.contains("10 alchemical energy"));
        assertTrue(journalZh.contains("死灵之书") && journalZh.contains("尸尘")
                && journalZh.contains("10点炼金能量"));
    }

    private static Properties loadMessages(String fileName) throws IOException {
        return loadProperties(coreDirectory().resolve("src/main/assets/messages/custom").resolve(fileName));
    }

    private static Properties loadItemMessages(String fileName) throws IOException {
        return loadProperties(coreDirectory().resolve("src/main/assets/messages/items").resolve(fileName));
    }

    private static Properties loadProperties(Path path) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static Path coreDirectory() {
        Path coreDirectory = Paths.get(System.getProperty("user.dir"));
        if (!coreDirectory.endsWith("core")) coreDirectory = coreDirectory.resolve("core");
        return coreDirectory;
    }
}
