package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs.CamouflageGnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.ElfWineCup;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElfIllusion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestGoo;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TowerMobPlacerTest {

    @Test
    public void toolExposesBossAndBossUnitPages() {
        assertEquals(Arrays.asList("test_bosses", "regional_boss_units",
                        "tower_bosses", "tower_boss_units"),
                TowerMobPlacer.pageKeysForTest());
        assertEquals(TestGoo.class, TowerMobPlacer.bossesOnPageForTest(0).get(0));
        assertEquals(new ArrayList<>(Bestiary.TOWER_BOSSES.entities()),
                new ArrayList<Class<?>>(TowerMobPlacer.bossesOnPageForTest(2)));
        assertFalse(TowerMobPlacer.bossesOnPageForTest(0).contains(CamouflageGnoll.class));
    }

    @Test
    public void bossUnitPagesExposeRegionalAndTowerComponents() {
        org.junit.Assert.assertTrue(TowerMobPlacer.bossesOnPageForTest(1)
                .contains(YogDzewa.Larva.class));
        org.junit.Assert.assertTrue(TowerMobPlacer.bossesOnPageForTest(1)
                .contains(YogFist.BurningFist.class));
        org.junit.Assert.assertTrue(TowerMobPlacer.bossesOnPageForTest(2)
                .contains(PestilenceKnight.class));
        org.junit.Assert.assertTrue(TowerMobPlacer.bossesOnPageForTest(3)
                .contains(ElfWineCup.class));
        org.junit.Assert.assertTrue(TowerMobPlacer.bossesOnPageForTest(3)
                .contains(GentlemanElfIllusion.class));
        assertFalse(TowerMobPlacer.bossesOnPageForTest(2).contains(CamouflageGnoll.class));
    }

    @Test
    public void oldTowerMobSelectionFallsBackToFirstTestBoss() {
        Bundle oldBundle = new Bundle();
        oldBundle.put("page", 2);
        oldBundle.put("mob_index", 17);
        oldBundle.put("elite_options", 1023);

        MobPlacementState restored = MobPlacementState.restoreBoss(oldBundle);
        restored.normalize(MobPlacementCatalog.bossPages());

        assertEquals("test_bosses", restored.categoryKey);
        assertEquals(TestGoo.class.getName(), restored.mobClassName);
        assertEquals(0, restored.eliteOptions);
    }
}
