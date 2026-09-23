package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TargetedClericSpellActionLifecycleTest {

    @Test
    public void targetedSpellAcceptsOnlyOneActiveTargetRequest() throws Exception {
        String source = source("TargetedClericSpell.java");

        assertTrue("targeted spells must reject a reentrant target request",
                source.contains("if (targetSelectionActive) return;"));
        assertTrue("each target request must receive a unique token",
                source.contains("final int request = ++targetSelectionToken;"));
        assertTrue("stale target callbacks must be ignored",
                source.contains("if (!isCurrentTargetSelection(request)) return;"));
    }

    @Test
    public void targetSelectionCancellationReleasesOnlyItsOwnRequest() throws Exception {
        String source = source("TargetedClericSpell.java");

        assertTrue("target selection must finish through one guarded path",
                source.contains("finishTargetSelection(request, cell);"));
        assertTrue("a cancelled request must not invoke the spell",
                source.contains("if (cell != null) onTargetSelected(tome, hero, cell);"));
    }

    @Test
    public void tomeActionHandlesASecondClickWhileTheSelectedSpellIsTargeting() throws Exception {
        String source = sourceAt("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/HolyTome.java");

        assertTrue("the tome action must handle an active target request",
                source.contains("quickSpell.isTargeting()")
                        && source.contains("GameScene.handleCell"));
        assertTrue("the tome action must auto-aim the active spell",
                source.contains("QuickSlotButton.autoAim"));
    }

    @Test
    public void actionIndicatorKeepsAnInProgressActionAndLetsItHandleARepeatClick() throws Exception {
        String source = sourceAt("src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/ActionIndicator.java");

        assertTrue("actions must opt in to handling a repeat click",
                source.contains("canHandleClickWhileInProgress()"));
        assertTrue("the action button must dispatch only allowed repeat clicks",
                source.contains("!action.isActionInProgress() || action.canHandleClickWhileInProgress()"));
        assertTrue("actions must expose their in-progress state",
                source.contains("default boolean isActionInProgress() { return false; }"));
    }

    private static String source(String fileName) throws Exception {
        Path working = Paths.get(System.getProperty("user.dir"));
        Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
        return Files.readString(core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/spells/").resolve(fileName),
                StandardCharsets.UTF_8);
    }

    private static String sourceAt(String fileName) throws Exception {
        Path working = Paths.get(System.getProperty("user.dir"));
        Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
        return Files.readString(core.resolve(fileName), StandardCharsets.UTF_8);
    }
}
