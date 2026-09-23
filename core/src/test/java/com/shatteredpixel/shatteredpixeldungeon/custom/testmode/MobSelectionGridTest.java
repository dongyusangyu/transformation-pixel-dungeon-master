package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MobSelectionGridTest {

    @Test
    public void contentHeightAndIndexSupportMoreThanOneViewport() {
        assertEquals(102f, MobSelectionGrid.contentHeight(20, 5, 24, 2), 0f);
        assertEquals(18, MobSelectionGrid.indexAt(101, 79, 20, 5, 24, 2, 140));
        assertEquals(-1, MobSelectionGrid.indexAt(134, 79, 20, 5, 24, 2, 140));
        assertEquals(17, MobSelectionGrid.indexAt(89, 79, 18, 5, 24, 2, 140));
        assertEquals(-1, MobSelectionGrid.indexAt(115, 79, 18, 5, 24, 2, 140));
    }

    @Test
    public void constructionDefersLayoutUntilAParentCameraExists() throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(
                "..", "core", "src", "main", "java", "com", "shatteredpixel",
                "shatteredpixeldungeon", "custom", "testmode", "MobSelectionGrid.java")),
                StandardCharsets.UTF_8);
        assertFalse(source.contains("setSize(width, viewportHeight)"));
        assertTrue(source.contains("camera() != null"));
    }

    @Test
    public void spriteScaleNeverEnlargesAndFitsLargeFrames() {
        assertEquals(1f, MobSelectionGrid.fitScale(16, 16, 20), 0f);
        assertEquals(0.625f, MobSelectionGrid.fitScale(32, 32, 20), 0f);
        assertEquals(0.5f, MobSelectionGrid.fitScale(40, 16, 20), 0f);
        assertEquals(1f, MobSelectionGrid.fitScale(0, 0, 20), 0f);
    }

    @Test
    public void viewportUsesOnlyVisibleRows() {
        assertEquals(24f, MobSelectionGrid.viewportHeightForEntries(1, 5, 3, 24f, 2f), 0f);
        assertEquals(50f, MobSelectionGrid.viewportHeightForEntries(10, 5, 3, 24f, 2f), 0f);
        assertEquals(76f, MobSelectionGrid.viewportHeightForEntries(15, 5, 3, 24f, 2f), 0f);
        assertEquals(76f, MobSelectionGrid.viewportHeightForEntries(20, 5, 3, 24f, 2f), 0f);
    }

    @Test
    public void scrollPaneKeepsGridComponentOffset() throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(
                "..", "core", "src", "main", "java", "com", "shatteredpixel",
                "shatteredpixeldungeon", "custom", "testmode", "MobSelectionGrid.java")),
                StandardCharsets.UTF_8);
        assertTrue(source.contains("pane.setRect(x, y, width(), height())"));
    }

    @Test
    public void eliteCheckboxesDoNotBlockParentScrollPane() throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(
                "..", "core", "src", "main", "java", "com", "shatteredpixel",
                "shatteredpixeldungeon", "custom", "testmode", "MobPlacer.java")),
                StandardCharsets.UTF_8);
        assertTrue(source.contains("PointerArea.NEVER_BLOCK"));
    }
}
