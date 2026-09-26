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

public class CursedFlameContentTextTest {

    @Test
    public void wandAndTrinketHaveBilingualItemTextAndDetailedDictionaryEntries() throws IOException {
        Properties items = load("items", "items.properties");
        Properties itemsZh = load("items", "items_zh.properties");
        Properties actors = load("actors", "actors.properties");
        Properties actorsZh = load("actors", "actors_zh.properties");
        Properties custom = load("custom", "custom.properties");
        Properties customZh = load("custom", "custom_zh.properties");

        assertNonEmpty(items, "items.wands.wandofcursedflame.name");
        assertNonEmpty(itemsZh, "items.wands.wandofcursedflame.name");
        assertTrue(items.getProperty("items.wands.wandofcursedflame.desc", "").contains("Wand of Fireblast"));
        assertTrue(itemsZh.getProperty("items.wands.wandofcursedflame.desc", "").contains("灵魂深处"));
        assertNonEmpty(items, "items.trinkets.twindemoneyes.desc");
        assertNonEmpty(itemsZh, "items.trinkets.twindemoneyes.desc");
        assertNonEmpty(actors, "actors.blobs.cursedflame.name");
        assertNonEmpty(actors, "actors.blobs.cursedflame.desc");
        assertNonEmpty(actorsZh, "actors.blobs.cursedflame.name");
        assertNonEmpty(actorsZh, "actors.blobs.cursedflame.desc");

        assertTrue(custom.getProperty("custom.dict.dict.wand_cursedflame_d", "").contains("5 alchemical energy"));
        assertTrue(custom.getProperty("custom.dict.dict.wand_cursedflame_d", "").contains("Battlemage"));
        assertTrue(customZh.getProperty("custom.dict.dict.wand_cursedflame_d", "").contains("5点炼金能量"));
        assertTrue(customZh.getProperty("custom.dict.dict.wand_cursedflame_d", "").contains("元素风暴"));
        assertNonEmpty(custom, "custom.dict.dict.wand_cursedflame");
        assertNonEmpty(customZh, "custom.dict.dict.wand_cursedflame");
        assertNonEmpty(custom, "custom.dict.dict.trinket_twin_demon_eyes");
        assertNonEmpty(customZh, "custom.dict.dict.trinket_twin_demon_eyes");
        assertTrue(custom.getProperty("custom.dict.dict.trinket_twin_demon_eyes_d", "").contains("4+2×trinket level"));
        assertTrue(custom.getProperty("custom.dict.dict.trinket_twin_demon_eyes_d", "").contains("12-tile"));
        assertTrue(customZh.getProperty("custom.dict.dict.trinket_twin_demon_eyes_d", "").contains("魔焰眼"));
        assertTrue(customZh.getProperty("custom.dict.dict.trinket_twin_demon_eyes_d", "").contains("激光眼"));
    }

    @Test
    public void twinEyeBuffDescriptionsAreSingleValidPropertiesAndKeepFormatArguments() throws IOException {
        Properties items = load("items", "items.properties");
        Properties itemsZh = load("items", "items_zh.properties");
        for (String mode : new String[]{"flame", "laser"}) {
            String prefix = "items.trinkets.twindemoneyes$eyelock.";
            String key = prefix + "desc_" + mode;
            String english = items.getProperty(key, "");
            String chinese = itemsZh.getProperty(key, "");
            assertFalse("missing " + key, english.isEmpty());
            assertFalse("missing " + key, chinese.isEmpty());
            assertTrue("expected escaped paragraph breaks in " + key,
                    english.contains("\n\n") && chinese.contains("\n\n"));
            assertTrue("expected both format args in " + key,
                    english.contains("%1$d") && english.contains("%2$d")
                            && chinese.contains("%1$d") && chinese.contains("%2$d"));
            assertFalse("obsolete namespace should not shadow the runtime class", load("actors", "actors.properties")
                    .containsKey("actors.buffs.twindemoneyes$eyelock." + "desc_" + mode));
            assertFalse("obsolete namespace should not shadow the runtime class", load("actors", "actors_zh.properties")
                    .containsKey("actors.buffs.twindemoneyes$eyelock." + "desc_" + mode));
        }
    }

    @Test
    public void journalRegistersBothAssets() throws IOException {
        String journal = new String(Files.readAllBytes(coreDirectory().resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java")),
                StandardCharsets.UTF_8);
        assertTrue(journal.contains("WANDS.d.put(\"wand_cursedflame\"")
                && journal.contains("EXItemSpriteSheet.WAND_CURSED_FLAME"));
        assertTrue(journal.contains("TRINKETS.d.put(\"trinket_twin_demon_eyes\"")
                && journal.contains("EXItemSpriteSheet.TWIN_DEMON_EYES"));
    }

    private static void assertNonEmpty(Properties properties, String key) {
        assertFalse("missing " + key, properties.getProperty(key, "").isEmpty());
    }

    private static Properties load(String folder, String file) throws IOException {
        Path path = coreDirectory().resolve("src/main/assets/messages").resolve(folder).resolve(file);
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static Path coreDirectory() {
        Path core = Paths.get(System.getProperty("user.dir"));
        return core.endsWith("core") ? core : core.resolve("core");
    }
}
