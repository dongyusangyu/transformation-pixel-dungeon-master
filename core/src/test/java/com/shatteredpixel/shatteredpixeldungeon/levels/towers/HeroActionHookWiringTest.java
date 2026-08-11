package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeroActionHookWiringTest {

    @Test
    public void levelExposesNarrowDefaultHooks() throws Exception {
        assertEquals(void.class, Level.class
                .getMethod("onHeroTurnStarted", Hero.class).getReturnType());
        assertEquals(void.class, Level.class
                .getMethod("onHeroWaited", Hero.class).getReturnType());
        assertEquals(void.class, Level.class
                .getMethod("onHeroConsumableUsed", Hero.class, Item.class).getReturnType());
    }

    @Test
    public void heroNotifiesOncePerNewTurnAndEveryCommittedWait() throws Exception {
        String source = source("actors/hero/Hero.java");
        assertEquals(1, occurrences(source, "onHeroTurnStarted(this)"));
        assertEquals(2, occurrences(source, "onHeroWaited(this)"));

        int newTurn = source.indexOf("if (!ready) {");
        int turnHook = source.indexOf("onHeroTurnStarted(this)");
        assertTrue(newTurn >= 0 && turnHook > newTurn);

        int rest = source.indexOf("public void rest( boolean fullRest )");
        int restSpend = source.indexOf("spendAndNextConstant( TIME_TO_REST )", rest);
        int waitHook = source.indexOf("onHeroWaited(this)", rest);
        assertTrue(restSpend >= 0 && waitHook > restSpend);
    }

    @Test
    public void consumablesNotifyOnlyAfterTheirUseIsCommitted() throws Exception {
        String potion = source("items/potions/Potion.java");
        int potionDetach = potion.indexOf("detach( hero.belongings.backpack )");
        int potionHook = potion.indexOf("onHeroConsumableUsed(hero, this)");
        assertTrue(potionDetach >= 0 && potionHook > potionDetach);
        assertTrue(potion.substring(potionDetach, potionHook).contains("hero.spend( TIME_TO_DRINK )"));

        String food = source("items/food/Food.java");
        int foodDetach = food.indexOf("detach( hero.belongings.backpack )");
        int foodHook = food.indexOf("onHeroConsumableUsed(hero, this)");
        assertTrue(foodDetach >= 0 && foodHook > foodDetach);

        String scroll = source("items/scrolls/Scroll.java");
        int readAnimation = scroll.indexOf("public void readAnimation()");
        int readSpend = scroll.indexOf("curUser.spend( TIME_TO_READ )", readAnimation);
        int scrollHook = scroll.indexOf("onHeroConsumableUsed(curUser, this)", readAnimation);
        assertTrue(readAnimation >= 0 && readSpend > readAnimation && scrollHook > readSpend);
    }

    @Test
    public void towerBossLevelForwardsOnlyDuringActiveEncounter() throws Exception {
        String source = source("levels/towers/TowerBossLevel.java");
        assertTrue(source.contains("pestilenceArena != null && !encounter.bossEncounterDefeated()"));
        assertTrue(source.contains("pestilenceArena.onHeroTurnStarted(hero)"));
        assertTrue(source.contains("pestilenceArena.onHeroWaited(hero)"));
        assertTrue(source.contains("pestilenceArena.onHeroConsumableUsed(hero, item)"));
    }

    private static int occurrences(String value, String needle) {
        int count = 0;
        int from = 0;
        while ((from = value.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }

    private static String source(String relative) throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        if (Files.isDirectory(root.resolve("src/main/java"))) {
            return Files.readString(root.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
                    .resolve(relative), StandardCharsets.UTF_8);
        }
        return Files.readString(root.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon")
                .resolve(relative), StandardCharsets.UTF_8);
    }
}
