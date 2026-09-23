package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pylon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.ElfWineCup;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Infection;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.OutbreakMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss.TestGoo;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MobPlacementServiceTest {

    @Test
    public void bossComponentsCanBePlacedWhileABossHealthBarIsAssigned() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            Actor.clear();
            Dungeon.level = openLevel(3, 3);
            Dungeon.hero = TestHeroFactory.create();
            TestGoo boss = new TestGoo();
            boss.pos = 0;
            Dungeon.level.mobs.add(boss);
            BossHealthBar.assignBoss(boss);

            assertSame(MobPlacementService.Failure.NONE,
                    MobPlacementService.validateTarget(4,
                            MobPlacementCatalog.SpawnMode.BOSS_COMPONENT));
        } finally {
            BossHealthBar.assignBoss(null);
            Actor.clear();
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void preparedBossPylonIsActiveOutsideItsArena() {
        Pylon pylon = (Pylon) MobPlacementService.createPrepared(Pylon.class,
                MobPlacementCatalog.SpawnMode.BOSS_COMPONENT);

        assertSame(Mob.Alignment.ENEMY, pylon.alignment);
        assertSame(pylon.HUNTING, pylon.state);
    }

    @Test
    public void preparedCombatComponentStartsHunting() {
        YogDzewa.Larva larva = (YogDzewa.Larva) MobPlacementService.createPrepared(
                YogDzewa.Larva.class, MobPlacementCatalog.SpawnMode.BOSS_COMPONENT);

        assertSame(larva.HUNTING, larva.state);
    }

    @Test
    public void preparedObjectiveComponentKeepsItsNeutralPassiveRole() {
        ElfWineCup cup = (ElfWineCup) MobPlacementService.createPrepared(
                ElfWineCup.class, MobPlacementCatalog.SpawnMode.BOSS_COMPONENT);

        assertSame(Mob.Alignment.NEUTRAL, cup.alignment);
        assertSame(cup.PASSIVE, cup.state);
    }

    @Test
    public void bossPylonCanDieOnAGenericTestLevel() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            Actor.clear();
            Dungeon.level = openLevel(3, 3);
            Dungeon.hero = TestHeroFactory.create();
            Pylon pylon = new Pylon();
            pylon.pos = 4;
            Dungeon.level.mobs.add(pylon);

            pylon.die(null);
        } finally {
            Actor.clear();
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void towerMobsUseSleepingInitialState() {
        Mob mob = MobPlacementService.createPrepared(TestGoo.class,
                MobPlacementCatalog.SpawnMode.TOWER_MOB);
        assertSame(mob.SLEEPING, mob.state);
    }

    @Test
    public void towerBossesAreActiveWhenPreparedByTheBossPlacer() {
        Hero previousHero = Dungeon.hero;
        try {
            Hero hero = TestHeroFactory.create();
            hero.HP = hero.HT = 10;
            Dungeon.hero = hero;

            GentlemanElf gentleman = (GentlemanElf) MobPlacementService.createPrepared(
                    GentlemanElf.class, MobPlacementCatalog.SpawnMode.TOWER_BOSS);
            DeathKnight deathKnight = (DeathKnight) MobPlacementService.createPrepared(
                    DeathKnight.class, MobPlacementCatalog.SpawnMode.TOWER_BOSS);

            assertTrue(gentleman.introResolved());
            assertFalse(gentleman.isInvulnerable(null));
            assertSame(gentleman.HUNTING, gentleman.state);
            assertSame(deathKnight.HUNTING, deathKnight.state);
            assertTrue(deathKnight.isTargeting(hero));
        } finally {
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void plagueGasInfectsHeroOnAPlacedPestilenceBoss() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            Actor.clear();
            TestLevel level = openLevel(3, 3);
            level.blobs = new HashMap<>();
            Dungeon.level = level;
            Hero hero = TestHeroFactory.create();
            hero.pos = 4;
            Dungeon.hero = hero;
            PestilenceKnight boss = (PestilenceKnight) MobPlacementService.createPrepared(
                    PestilenceKnight.class, MobPlacementCatalog.SpawnMode.TOWER_BOSS);
            boss.pos = 0;
            level.mobs.add(boss);
            OutbreakMiasma miasma = com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob.seed(
                    4, 1, OutbreakMiasma.class, level);

            level.onHeroTurnStarted(hero);
            level.onHeroTurnStarted(hero);

            assertTrue(Infection.stacks(hero) > 0);
        } finally {
            Actor.clear();
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void dynamicMobNamesUseTheMobNameImplementation() throws Exception {
        String serviceSource = Files.readString(sourcePath("MobPlacementService.java"),
                StandardCharsets.UTF_8);
        String placerSource = Files.readString(sourcePath("MobPlacer.java"),
                StandardCharsets.UTF_8);
        String towerPlacerSource = Files.readString(sourcePath("TowerMobPlacer.java"),
                StandardCharsets.UTF_8);

        assertTrue(serviceSource.contains("return mob.name();"));
        assertFalse(placerSource.contains("M.L(selectedClass(), \"name\")"));
        assertFalse(towerPlacerSource.contains("M.L(selectedMobClass(), \"name\")"));
    }

    @Test
    public void generatedBeeIsInitializedAsAnActiveStandaloneMob() {
        Bee bee = (Bee) MobPlacementService.createPrepared(Bee.class,
                MobPlacementCatalog.SpawnMode.STANDARD);

        assertTrue(bee.HT > 0);
        assertEquals(bee.HT, bee.HP);
        assertEquals(-1, bee.potPos());
        assertEquals(-1, bee.potHolderID());
    }

    @Test
    public void generatedMirrorImageIsBoundBeforeItsFirstAction() {
        Hero previousHero = Dungeon.hero;
        try {
            Hero owner = TestHeroFactory.create();
            Dungeon.hero = owner;

            TestMirrorImage mirror = (TestMirrorImage) MobPlacementService.createPrepared(
                    TestMirrorImage.class, MobPlacementCatalog.SpawnMode.STANDARD);

            assertTrue(mirror.bound);
        } finally {
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void generatedPrismaticImageIsBoundBeforeItsFirstAction() {
        Hero previousHero = Dungeon.hero;
        try {
            Hero owner = TestHeroFactory.create();
            Dungeon.hero = owner;

            TestPrismaticImage prismatic = (TestPrismaticImage) MobPlacementService.createPrepared(
                    TestPrismaticImage.class, MobPlacementCatalog.SpawnMode.STANDARD);

            assertTrue(prismatic.bound);
        } finally {
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void heroBoundImageCreationRequiresAnActiveHero() {
        Hero previousHero = Dungeon.hero;
        try {
            Dungeon.hero = null;

            try {
                MobPlacementService.createPrepared(MirrorImage.class,
                        MobPlacementCatalog.SpawnMode.STANDARD);
                fail("hero-bound mobs must not be created without an active hero");
            } catch (MobPlacementService.PlacementException exception) {
                assertSame(MobPlacementService.Failure.CREATE_FAILED, exception.failure());
            }
        } finally {
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void unboundMirrorImageCanDieWithoutOwner() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            Actor.clear();
            Dungeon.level = openLevel(3, 3);
            Dungeon.hero = TestHeroFactory.create();
            MirrorImage mirror = new MirrorImage();
            mirror.pos = 0;
            Dungeon.level.mobs.add(mirror);

            try {
                mirror.die(null);
            } catch (NullPointerException exception) {
                fail("an orphaned mirror must be cleaned up without querying a missing owner");
            }
        } finally {
            Actor.clear();
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void unboundPrismaticImageCanDieWithoutOwner() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            Actor.clear();
            Dungeon.level = openLevel(3, 3);
            Dungeon.hero = TestHeroFactory.create();
            PrismaticImage prismatic = new PrismaticImage();
            prismatic.pos = 0;
            Dungeon.level.mobs.add(prismatic);

            try {
                prismatic.die(com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.class);
            } catch (NullPointerException exception) {
                fail("an orphaned prismatic image must die without querying a missing owner");
            }
        } finally {
            Actor.clear();
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void testPlacedWraithCanBeSurprisedBeforeItsFirstTurn() {
        TestWraith wraith = new TestWraith();
        wraith.adjustStats(10, false);
        assertFalse(wraith.enemySeenForTest());
    }

    @Test
    public void normalWraithStatAdjustmentCanRemainAlert() {
        TestWraith wraith = new TestWraith();
        wraith.adjustStats(10, true);
        assertTrue(wraith.enemySeenForTest());
    }

    private static class TestWraith extends Wraith {
        boolean enemySeenForTest() {
            return enemySeen;
        }
    }

    private static TestLevel openLevel(int width, int height) {
        TestLevel level = new TestLevel();
        level.setSize(width, height);
        Arrays.fill(level.passable, true);
        Arrays.fill(level.solid, false);
        level.mobs = new HashSet<>();
        return level;
    }

    private static Path sourcePath(String fileName) {
        Path root = Paths.get("").toAbsolutePath();
        Path core = Files.isDirectory(root.resolve("src/main/java"))
                ? root
                : root.resolve("core");
        return core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode")
                .resolve(fileName);
    }

    private static class TestLevel extends Level {
        @Override
        protected boolean build() {
            return true;
        }

        @Override
        protected void createMobs() {
        }

        @Override
        protected void createItems() {
        }
    }

    public static class TestMirrorImage extends MirrorImage {
        boolean bound;

        @Override
        public void duplicate(Hero hero) {
            bound = true;
        }
    }

    public static class TestPrismaticImage extends PrismaticImage {
        boolean bound;

        @Override
        public void duplicate(Hero hero, int HP) {
            bound = true;
        }
    }
}
