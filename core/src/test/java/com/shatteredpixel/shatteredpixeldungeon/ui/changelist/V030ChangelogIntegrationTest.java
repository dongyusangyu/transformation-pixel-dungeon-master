package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class V030ChangelogIntegrationTest {

    private static final int BUTTON_COUNT = 5;

    @Test
    public void releasePageUsesOneStableVersionAndFiveLocalizedSections() throws Exception {
        String source = read("src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/changelist/v0_3_X.java");
        String english = read("src/main/assets/messages/ui/ui.properties");
        String chinese = read("src/main/assets/messages/ui/ui_zh.properties");

        assertTrue(source.contains("new ChangeInfo(\"v0.3.0\", true, \"\")"));
        assertFalse(source.contains("Alpha"));
        assertFalse(source.contains("alpha"));

        for (int i = 1; i <= BUTTON_COUNT; i++) {
            String stem = "ui.changelist.v0_3_x.v0_3_0.button_" + i;
            assertTrue(stem, english.contains(stem + ".title="));
            assertTrue(stem, english.contains(stem + ".text="));
            assertTrue(stem, chinese.contains(stem + ".title="));
            assertTrue(stem, chinese.contains(stem + ".text="));
            assertTrue(stem, source.contains("v0_3_0.button_" + i + ".title"));
            assertTrue(stem, source.contains("v0_3_0.button_" + i + ".text"));
        }

        String versionMessages = versionMessages(english) + versionMessages(chinese);
        assertFalse(versionMessages.contains("v0_3_0alpha"));
    }

    @Test
    public void chineseReleaseCopyEmphasizesImportantNewContent() throws Exception {
        String chinese = versionMessages(read("src/main/assets/messages/ui/ui_zh.properties"));

        assertContains(chinese,
                "_新周目_",
                "_0层小镇_",
                "_高塔_",
                "_六阶武器_",
                "_神器_",
                "_提取卷轴_",
                "_蜕变棱晶_",
                "_女猎手？_",
                "_英雄殿堂_",
                "_幻影射手_",
                "_千发投掷_");
    }

    @Test
    public void changesSceneRoutesVersionTabsNewestFirst() throws Exception {
        String source = read("src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/ChangesScene.java");

        assertTrue(source.contains("import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.v0_3_X;"));
        assertTrue(source.contains("public static int changesSelected = 0;"));
        assertInOrder(source,
                "v0_3_X.addAllChanges(changeInfos)",
                "v0_2_X.addAllChanges(changeInfos)",
                "v0_1_X.addAllChanges(changeInfos)");
        assertInOrder(source,
                "GREY_BUTTON_TR, \"0.3\"",
                "GREY_BUTTON_TR, \"0.2\"",
                "GREY_BUTTON_TR, \"0.1\"");
    }

    private static void assertInOrder(String text, String... needles) {
        int previous = -1;
        for (String needle : needles) {
            int current = text.indexOf(needle);
            assertTrue(needle, current > previous);
            previous = current;
        }
    }

    private static void assertContains(String text, String... needles) {
        for (String needle : needles) {
            assertTrue(needle, text.contains(needle));
        }
    }

    private static String versionMessages(String messages) {
        StringBuilder result = new StringBuilder();
        for (String line : messages.split("\\R")) {
            if (line.startsWith("ui.changelist.v0_3_x.")) {
                result.append(line).append('\n');
            }
        }
        return result.toString();
    }

    private static String read(String relativePath) throws Exception {
        Path core = Paths.get(System.getProperty("user.dir"));
        if (!core.endsWith("core")) core = core.resolve("core");
        return new String(Files.readAllBytes(core.resolve(relativePath)), StandardCharsets.UTF_8);
    }
}
