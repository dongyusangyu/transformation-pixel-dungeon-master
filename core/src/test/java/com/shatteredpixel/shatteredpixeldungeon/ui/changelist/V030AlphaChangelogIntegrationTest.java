package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class V030AlphaChangelogIntegrationTest {

    private static final int BUTTON_COUNT = 17;

    @Test
    public void alphaPageUsesThreeSectionsAndAllLocalizedButtons() throws Exception {
        String source = read("src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/changelist/v0_3_X.java");
        String english = read("src/main/assets/messages/ui/ui.properties");
        String chinese = read("src/main/assets/messages/ui/ui_zh.properties");

        assertTrue(source.contains("new ChangeInfo(\"v0.3.0Alpha\", true, \"\")"));
        int newSection = source.indexOf("Messages.get(ChangesScene.class, \"new\")");
        int changeSection = source.indexOf("Messages.get(ChangesScene.class, \"changes\")");
        int bugfixSection = source.indexOf("Messages.get(ChangesScene.class, \"bugfixes\")");
        assertTrue(newSection >= 0 && newSection < changeSection && changeSection < bugfixSection);

        for (int i = 1; i <= BUTTON_COUNT; i++) {
            String stem = "ui.changelist.v0_3_x.v0_3_0alpha.button_" + i;
            assertTrue(stem, english.contains(stem + ".title="));
            assertTrue(stem, english.contains(stem + ".text="));
            assertTrue(stem, chinese.contains(stem + ".title="));
            assertTrue(stem, chinese.contains(stem + ".text="));
            assertTrue(stem, source.contains("v0_3_0alpha.button_" + i + ".title"));
            assertTrue(stem, source.contains("v0_3_0alpha.button_" + i + ".text"));
        }
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

    @Test
    public void excludedTopicsAreAbsentFromAlphaCopy() throws Exception {
        String english = alphaMessages(read("src/main/assets/messages/ui/ui.properties"));
        String chinese = alphaMessages(read("src/main/assets/messages/ui/ui_zh.properties"));

        assertAbsent(chinese,
                "天赋系统重构",
                "战士纹章与提取卷轴",
                "安卓非全屏模式",
                "系统栏遮挡",
                "输入法闪退",
                "邪眼蓄力被打断",
                "快捷栏中的升级卷轴",
                "力量药剂、蜕变卷轴、面具和皇冠");
        assertAbsent(english.toLowerCase(),
                "talent system refactor",
                "warrior seal and extraction scroll",
                "android non-fullscreen",
                "input method",
                "evil eye charging",
                "quickslot");
    }

    private static void assertInOrder(String text, String... needles) {
        int previous = -1;
        for (String needle : needles) {
            int current = text.indexOf(needle);
            assertTrue(needle, current > previous);
            previous = current;
        }
    }

    private static void assertAbsent(String text, String... needles) {
        for (String needle : needles) {
            assertFalse(needle, text.contains(needle));
        }
    }

    private static String alphaMessages(String messages) {
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
