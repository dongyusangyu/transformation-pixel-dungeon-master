package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class SpiderJarTest {

    @Test
    public void heroTurnEndsOnlyAfterSpiderJarLandingCallback() throws Exception {
        String source = source();
        int callback = source.indexOf("public void call()");
        int landing = source.indexOf("b.onThrow(cell)", callback);
        int spend = source.indexOf("hero.spendAndNext(Actor.TICK)");

        assertTrue("the throw callback must exist", callback >= 0);
        assertTrue("the callback must place the jar", landing > callback);
        assertTrue("hero turn must end after the jar is placed", spend > landing);
        assertTrue("the ability must mark the hero busy before the animation", 
                source.indexOf("hero.busy()") >= 0 && source.indexOf("hero.busy()") < callback);
    }

    @Test
    public void landingCallbackIsOneShot() throws Exception {
        String source = source();
        int callback = source.indexOf("public void call()");
        int landing = source.indexOf("b.onThrow(cell)", callback);

        assertTrue("landing callback must guard repeated execution",
                source.substring(callback, landing).contains("resolved[0]"));
    }

    @Test
    public void fuseTriggerStopsBeforeAnyReschedule() throws Exception {
        String source = source();
        int trigger = source.indexOf("if (l >= maxl)");
        int fuseSpend = source.indexOf("spend(TICK)", trigger);

        assertTrue("fuse trigger must exist", trigger >= 0);
        assertTrue("a triggered fuse must return before rescheduling", 
                source.substring(trigger, fuseSpend).contains("return true"));
    }

    @Test
    public void explosionChecksMapBoundsBeforeReadingNeighborCells() throws Exception {
        String source = source();
        int firstNeighbor = source.indexOf("int i = cell + PathFinder.NEIGHBOURS9[p]");
        int firstMapRead = source.indexOf("Dungeon.level.map[i]", firstNeighbor);

        assertTrue("neighbor loop must exist", firstNeighbor >= 0);
        assertTrue("neighbor cell must be validated before map access",
                source.substring(firstNeighbor, firstMapRead).contains("i < 0"));
    }

    @Test
    public void explosionRejectsAnInvalidOriginBeforeUsingTheLevelMap() throws Exception {
        String source = source();
        int spiderExplode = source.indexOf("public void explode(int cell)");
        int firstLevelMapUse = source.indexOf("Dungeon.level", spiderExplode);

        assertTrue("spider explosion must validate its origin", spiderExplode >= 0);
        assertTrue("invalid origins must be rejected before level access",
                source.substring(spiderExplode, firstLevelMapUse).contains("!isValidCell(cell)"));
    }

    @Test
    public void restoredDroppedSpiderDoesNotCreateASecondFuse() throws Exception {
        String source = gameSceneSource();
        int rearm = source.indexOf("armAt(pos)");

        assertTrue("the cross-level restore path must exist", rearm >= 0);
        assertTrue("cross-level restore must be guarded by an empty fuse check",
                source.substring(Math.max(0, rearm - 180), rearm).contains("fuse == null"));
    }

    private static String source() throws Exception {
        Path working = Paths.get(System.getProperty("user.dir"));
        Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
        return Files.readString(core.resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/abilities/ninja/SpiderJar.java"),
                StandardCharsets.UTF_8);
    }

    private static String gameSceneSource() throws Exception {
        Path working = Paths.get(System.getProperty("user.dir"));
        Path core = Files.isDirectory(working.resolve("core")) ? working.resolve("core") : working;
        return Files.readString(core.resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/GameScene.java"),
                StandardCharsets.UTF_8);
    }
}
