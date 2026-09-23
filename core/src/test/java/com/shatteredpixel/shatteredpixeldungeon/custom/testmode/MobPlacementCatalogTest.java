package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ChaosDisciples;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pylon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RogueBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.ElfWineCup;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElfIllusion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MobPlacementCatalogTest {

    @Test
    public void ordinaryPagesFollowBestiaryAndExcludeBosses() {
        assertEquals(Arrays.asList("regional", "universal", "rare", "quest",
                "neutral", "ally", "tower_mobs"), pageKeys(MobPlacementCatalog.mobPages()));
        assertEquals(mobClasses(Bestiary.REGIONAL), MobPlacementCatalog.mobPages().get(0).entries());
        assertFalse(flatten(MobPlacementCatalog.mobPages()).contains(PestilenceKnight.class));
        assertFalse(flatten(MobPlacementCatalog.mobPages()).contains(TestGoo.class));
        assertTrue(flatten(MobPlacementCatalog.mobPages()).contains(CamouflageGnoll.class));
    }

    @Test
    public void bossPagesContainTopLevelBossesAndTheirPlaceableComponents() {
        List<MobPlacementCatalog.Page> pages = MobPlacementCatalog.bossPages();
        assertEquals(Arrays.asList("test_bosses", "regional_boss_units",
                "tower_bosses", "tower_boss_units"), pageKeys(pages));
        assertEquals(Arrays.asList(TestGoo.class, TestTengu.class, TestDM300.class,
                TestDwarfKing.class, TestYogDzewa.class, TestWarriorBoss.class,
                TestRogueBoss.class, TestHuntressBoss.class, TestGreatDemon.class,
                TestGreatShoper.class), pages.get(0).entries());
        assertEquals(Arrays.asList(
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
        ), pages.get(1).entries());
        assertEquals(mobClasses(Bestiary.TOWER_BOSSES), pages.get(2).entries());
        assertEquals(Arrays.asList(ElfWineCup.class, GentlemanElfIllusion.class),
                pages.get(3).entries());
        assertEquals(MobPlacementCatalog.SpawnMode.BOSS_COMPONENT,
                pages.get(1).spawnMode());
        assertEquals(MobPlacementCatalog.SpawnMode.BOSS_COMPONENT,
                pages.get(3).spawnMode());
    }

    @Test
    public void pagesAreNonEmptyAndHaveNoDuplicateClasses() {
        for (MobPlacementCatalog.Page page : MobPlacementCatalog.mobPages()) {
            assertFalse(page.entries().isEmpty());
        }
        for (MobPlacementCatalog.Page page : MobPlacementCatalog.bossPages()) {
            assertFalse(page.entries().isEmpty());
        }
        assertFalse(MobPlacementCatalog.containsDuplicateClasses(MobPlacementCatalog.mobPages()));
        assertFalse(MobPlacementCatalog.containsDuplicateClasses(MobPlacementCatalog.bossPages()));
    }

    @Test
    public void everyBossComponentHasANoArgConstructor() throws Exception {
        for (MobPlacementCatalog.Page page : MobPlacementCatalog.bossPages()) {
            if (page.spawnMode() != MobPlacementCatalog.SpawnMode.BOSS_COMPONENT) continue;
            for (Class<? extends Mob> type : page.entries()) {
                assertNotNull(type.getName(), type.getDeclaredConstructor());
            }
        }
    }

    @Test
    public void invalidSelectionFallsBackToFirstEntry() {
        MobPlacementCatalog.Page page = MobPlacementCatalog.findPage(
                MobPlacementCatalog.mobPages(), "does_not_exist");
        assertNotNull(page);
        assertEquals(CamouflageGnoll.class,
                MobPlacementCatalog.resolveClass(MobPlacementCatalog.mobPages().get(6), "missing"));
    }

    private static List<String> pageKeys(List<MobPlacementCatalog.Page> pages) {
        ArrayList<String> result = new ArrayList<>();
        for (MobPlacementCatalog.Page page : pages) result.add(page.key());
        return result;
    }

    private static List<Class<? extends Mob>> mobClasses(Bestiary bestiary) {
        ArrayList<Class<? extends Mob>> result = new ArrayList<>();
        for (Class<?> entity : bestiary.entities()) {
            if (Mob.class.isAssignableFrom(entity)) result.add(entity.asSubclass(Mob.class));
        }
        return result;
    }

    private static List<Class<? extends Mob>> flatten(List<MobPlacementCatalog.Page> pages) {
        ArrayList<Class<? extends Mob>> result = new ArrayList<>();
        for (MobPlacementCatalog.Page page : pages) result.addAll(page.entries());
        return result;
    }
}
