package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PlagueGuard;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Signal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class PestilencePurifierEntryTest {
    private Application previousApp;
    private Files previousFiles;
    private String previousVersion;
    private HeadlessItemSprites sprites;
    private TowerBossLevel level;
    private Hero hero;
    private final List<String> logs = new ArrayList<>();
    private final Signal.Listener<String> listener = text -> {
        logs.add(text);
        return false;
    };

    @Before public void setUp() {
        previousApp = Gdx.app;
        previousFiles = Gdx.files;
        previousVersion = Game.version;
        Game.version = "test";
        Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
                new Class<?>[]{Application.class}, (proxy, method, args) -> {
                    if (method.getReturnType() == Preferences.class) {
                        return Proxy.newProxyInstance(Preferences.class.getClassLoader(),
                                new Class<?>[]{Preferences.class}, (p, m, a) -> {
                                    if (a != null && a.length == 2) return a[1];
                                    if (m.getReturnType() == boolean.class) return false;
                                    return null;
                                });
                    }
                    return null;
                });
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
                new Class<?>[]{Files.class}, (proxy, method, args) -> {
                    if (method.getReturnType() == FileHandle.class) {
                        File root = new File("src/main/assets");
                        if (!root.isDirectory()) root = new File("core/src/main/assets");
                        return new FileHandle(new File(root, (String) args[0]));
                    }
                    return null;
                });
        sprites = new HeadlessItemSprites();
        level = new TowerBossLevel();
        level.setSize(7, 7);
        level.mobs = new HashSet<>();
        level.mobs.add(new PlagueGuard());
        hero = TestHeroFactory.create();
        GLog.update.add(listener);
    }

    @After public void tearDown() {
        GLog.update.remove(listener);
        sprites.close();
        Gdx.app = previousApp;
        Gdx.files = previousFiles;
        Game.version = previousVersion;
    }

    @Test public void ordinaryLandingCellsStaySilentWithActiveGuards() {
        PestilenceArenaController controller = restoredController(true, 24, 0);
        for (int cell : new int[]{16, 17, 18, 25}) {
            hero.pos = cell;
            assertEquals(PestilenceArenaController.ActivationResult.NONE,
                    controller.onHeroEntered(level, hero, true));
        }
        assertTrue(logs.isEmpty());
        assertEquals(24, controller.purifierCell());
        assertEquals(0, cooldown(controller));
    }

    @Test public void steppingOnReadyPurifierReportsGuardBlockOnceWithoutActivation() {
        PestilenceArenaController controller = restoredController(true, 24, 0);
        hero.pos = 24;
        assertEquals(PestilenceArenaController.ActivationResult.NONE,
                controller.onHeroEntered(level, hero, true));
        assertEquals(Collections.singletonList(GLog.WARNING
                + Messages.get(PestilenceArenaController.class, "guard_blocked")), logs);
        assertEquals(24, controller.purifierCell());
        assertEquals(0, cooldown(controller));
    }

    @Test public void cooldownMessageTakesPriorityOnlyOnPurifier() {
        PestilenceArenaController controller = restoredController(true, 24, 5);
        hero.pos = 23;
        controller.onHeroEntered(level, hero, true);
        assertTrue(logs.isEmpty());
        hero.pos = 24;
        assertEquals(PestilenceArenaController.ActivationResult.NONE,
                controller.onHeroEntered(level, hero, true));
        assertEquals(Collections.singletonList(GLog.WARNING
                + Messages.get(PestilenceArenaController.class, "recharging", 5)), logs);
        assertEquals(5, cooldown(controller));
    }

    @Test public void unpreparedAndOutOfMapPurifiersStaySilent() {
        hero.pos = 24;
        PestilenceArenaController unprepared = restoredController(false, 24, 0);
        assertEquals(PestilenceArenaController.ActivationResult.NONE,
                unprepared.onHeroEntered(level, hero, true));
        hero.pos = level.length();
        PestilenceArenaController invalid = restoredController(true, hero.pos, 0);
        assertEquals(PestilenceArenaController.ActivationResult.NONE,
                invalid.onHeroEntered(level, hero, true));
        assertTrue(logs.isEmpty());
    }

    @Test public void missingHeroOrLevelDoesNotTriggerPurifier() {
        PestilenceArenaController controller = restoredController(true, 24, 0);
        hero.pos = 24;
        assertEquals(PestilenceArenaController.ActivationResult.NONE,
                controller.onHeroEntered(level, null, true));
        assertEquals(PestilenceArenaController.ActivationResult.NONE,
                controller.onHeroEntered(null, hero, true));
        assertTrue(logs.isEmpty());
    }

    private static PestilenceArenaController restoredController(boolean prepared, int cell, int cooldown) {
        Bundle saved = new Bundle();
        saved.put("prepared", prepared);
        saved.put("prelude_started", true);
        saved.put("purifier_cell", cell);
        saved.put("purifier_cooldown", cooldown);
        PestilenceArenaController controller = new PestilenceArenaController();
        controller.restoreFromBundle(saved);
        return controller;
    }

    private static int cooldown(PestilenceArenaController controller) {
        Bundle saved = new Bundle();
        controller.storeInBundle(saved);
        return saved.getInt("purifier_cooldown");
    }
}
