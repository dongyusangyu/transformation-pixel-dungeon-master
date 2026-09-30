package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessGameMessages;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;

public class TowerFeelingEntryTest {
    @Test public void towerFeelingsUseExistingChineseDescriptions() throws Exception {
        try (HeadlessGameMessages messages = new HeadlessGameMessages()) {
            assertEquals("这层高塔的房间如岛屿般悬于深渊之上，彼此之间没有任何连接。", Level.Feeling.SKY_ISLAND.desc());
            assertEquals("这层高塔的地面没有任何水源，也看不到草木生长的痕迹。", Level.Feeling.BARREN.desc());
        }
    }

    @Test public void bothFeelingsLogOnlyInsideTheExistingLevelTransitionGuard() throws Exception {
        Path core = Paths.get("core");
        if (!Files.isDirectory(core)) core = Paths.get("");
        String source = new String(Files.readAllBytes(core.resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/GameScene.java")), StandardCharsets.UTF_8);
        int guard = source.indexOf("if (InterlevelScene.mode != InterlevelScene.Mode.NONE)");
        int end = source.indexOf("InterlevelScene.mode = InterlevelScene.Mode.NONE", guard);
        assertTrue(guard >= 0 && end > guard);
        String entry = source.substring(guard, end);
        for (String feeling : new String[]{"SKY_ISLAND", "BARREN"}) {
            int start = entry.indexOf("case " + feeling + ":");
            assertTrue("missing feeling branch: " + feeling, start >= 0);
            int branchEnd = entry.indexOf("break;", start);
            assertTrue(entry.substring(start, branchEnd).contains("GLog.w(level.feeling.desc())"));
        }
        assertTrue(entry.contains("case CHAOS:"));
        assertTrue(entry.contains("Notes.add(Notes.Landmark.CHASM_FLOOR)"));
    }
}
