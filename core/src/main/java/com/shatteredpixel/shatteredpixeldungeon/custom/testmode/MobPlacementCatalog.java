package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ChaosDisciples;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pylon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RogueBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElfIllusion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.ElfWineCup;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestDM300;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestDwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestGoo;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestGreatDemon;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestGreatShoper;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestHuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestRogueBoss;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestTengu;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestWarriorBoss;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestYogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class MobPlacementCatalog {

    enum SpawnMode {
        STANDARD,
        TOWER_MOB,
        TEST_BOSS,
        TOWER_BOSS,
        BOSS_COMPONENT
    }

    static final class Page {
        private final String key;
        private final SpawnMode spawnMode;
        private final List<Class<? extends Mob>> entries;

        private Page(String key, SpawnMode spawnMode, List<Class<? extends Mob>> entries) {
            this.key = key;
            this.spawnMode = spawnMode;
            this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        }

        String key() {
            return key;
        }

        SpawnMode spawnMode() {
            return spawnMode;
        }

        List<Class<? extends Mob>> entries() {
            return entries;
        }
    }

    private static final List<Page> MOB_PAGES = Collections.unmodifiableList(Arrays.asList(
            bestiaryPage("regional", Bestiary.REGIONAL, SpawnMode.STANDARD),
            bestiaryPage("universal", Bestiary.UNIVERSAL, SpawnMode.STANDARD),
            bestiaryPage("rare", Bestiary.RARE, SpawnMode.STANDARD),
            bestiaryPage("quest", Bestiary.QUEST, SpawnMode.STANDARD),
            bestiaryPage("neutral", Bestiary.NEUTRAL, SpawnMode.STANDARD),
            bestiaryPage("ally", Bestiary.ALLY, SpawnMode.STANDARD),
            bestiaryPage("tower_mobs", Bestiary.TOWER_MOBS, SpawnMode.TOWER_MOB)
    ));

    private static final List<Page> BOSS_PAGES = Collections.unmodifiableList(Arrays.asList(
            new Page("test_bosses", SpawnMode.TEST_BOSS, Arrays.asList(
                    TestGoo.class,
                    TestTengu.class,
                    TestDM300.class,
                    TestDwarfKing.class,
                    TestYogDzewa.class,
                    TestWarriorBoss.class,
                    TestRogueBoss.class,
                    TestHuntressBoss.class,
                    TestGreatDemon.class,
                    TestGreatShoper.class
            )),
            new Page("regional_boss_units", SpawnMode.BOSS_COMPONENT, Arrays.asList(
                    RogueBoss.ShadowRogue.class,
                    Pylon.class,
                    HuntressBoss.DistractingHawk.class,
                    HuntressBoss.HuntressTentacle.class,
                    DwarfKing.DKGhoul.class,
                    DwarfKing.DKMonk.class,
                    DwarfKing.DKWarlock.class,
                    DwarfKing.DKGolem.class,
                    YogDzewa.Larva.class,
                    YogDzewa.YogRipper.class,
                    YogDzewa.YogEye.class,
                    YogDzewa.YogScorpio.class,
                    YogFist.BurningFist.class,
                    YogFist.SoiledFist.class,
                    YogFist.RottingFist.class,
                    YogFist.RustedFist.class,
                    YogFist.BrightFist.class,
                    YogFist.DarkFist.class,
                    ChaosDisciples.ScorpioBoss.class,
                    ChaosDisciples.EyeBoss.class,
                    ChaosDisciples.RipperBoss.class,
                    ChaosDisciples.SuccBoss.class,
                    GoldBoss.GoldElemental.class,
                    GoldBoss.Thymor.class,
                    GoldBoss.MonkMaster.class,
                    GoldBoss.GoldGolem.class
            )),
            bestiaryPage("tower_bosses", Bestiary.TOWER_BOSSES, SpawnMode.TOWER_BOSS),
            new Page("tower_boss_units", SpawnMode.BOSS_COMPONENT, Arrays.asList(
                    ElfWineCup.class,
                    GentlemanElfIllusion.class
            ))
    ));

    private MobPlacementCatalog() {
    }

    static List<Page> mobPages() {
        return MOB_PAGES;
    }

    static List<Page> bossPages() {
        return BOSS_PAGES;
    }

    static Page findPage(List<Page> pages, String key) {
        if (pages == null || pages.isEmpty()) return null;
        if (key != null) {
            for (Page page : pages) {
                if (key.equals(page.key())) return page;
            }
        }
        return pages.get(0);
    }

    static Class<? extends Mob> resolveClass(Page page, String className) {
        if (page == null || page.entries().isEmpty()) return null;
        if (className != null) {
            for (Class<? extends Mob> entry : page.entries()) {
                if (className.equals(entry.getName())) return entry;
            }
        }
        return page.entries().get(0);
    }

    private static Page bestiaryPage(String key, Bestiary bestiary, SpawnMode mode) {
        ArrayList<Class<? extends Mob>> mobs = new ArrayList<>();
        for (Class<?> entity : bestiary.entities()) {
            if (Mob.class.isAssignableFrom(entity)) {
                mobs.add(entity.asSubclass(Mob.class));
            }
        }
        return new Page(key, mode, mobs);
    }

    static boolean containsDuplicateClasses(List<Page> pages) {
        Set<Class<? extends Mob>> seen = new HashSet<>();
        for (Page page : pages) {
            for (Class<? extends Mob> entry : page.entries()) {
                if (!seen.add(entry)) return true;
            }
        }
        return false;
    }

}
