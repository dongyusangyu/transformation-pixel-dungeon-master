package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ChampionEnemyTargetingTest {

    private Hero previousHero;
    private int previousChallenges;

    @Before
    public void setUp() {
        previousHero = Dungeon.hero;
        previousChallenges = Dungeon.challenges;
        Actor.clear();
        Actor.resetNextID();
    }

    @After
    public void tearDown() {
        Actor.clear();
        Actor.resetNextID();
        Dungeon.hero = previousHero;
        Dungeon.challenges = previousChallenges;
    }

    @Test
    public void normalChampionForcesLockWheneverHarshEnvironmentIsEnabled() {
        assertTargetingForChallenges(0, false, ChampionEnemy.Blazing.class);
        assertTargetingForChallenges(Challenges.CHAMPION_ENEMIES, false,
                ChampionEnemy.Blazing.class);
        assertTargetingForChallenges(Challenges.HARSH_ENVIRONMENT, true,
                ChampionEnemy.Blazing.class);
        assertTargetingForChallenges(Challenges.EXTREME_ENVIRONMENT, false,
                ChampionEnemy.Blazing.class);
        assertTargetingForChallenges(Challenges.HARSH_ENVIRONMENT
                | Challenges.EXTREME_ENVIRONMENT, true, ChampionEnemy.Blazing.class);
        assertTargetingForChallenges(Challenges.CHAMPION_ENEMIES
                | Challenges.HARSH_ENVIRONMENT | Challenges.EXTREME_ENVIRONMENT,
                true, ChampionEnemy.Blazing.class);
    }

    @Test
    public void randomMiniBossForcesLockWithHarshEnvironmentAlone() {
        assertTargetingForChallenges(0, false, ChampionEnemy.RandomMiniBoss.class);
        assertTargetingForChallenges(Challenges.HARSH_ENVIRONMENT, true,
                ChampionEnemy.RandomMiniBoss.class);
        assertTargetingForChallenges(Challenges.HARSH_ENVIRONMENT
                | Challenges.EXTREME_ENVIRONMENT, true,
                ChampionEnemy.RandomMiniBoss.class);
    }

    private void assertTargetingForChallenges(int challenges, boolean shouldLock,
                                             Class<? extends ChampionEnemy> buffType) {
        Dungeon.challenges = challenges;
        Hero hero = TestHeroFactory.create();
        hero.pos = 70;
        hero.HP = hero.HT = 20;
        Dungeon.hero = hero;

        TestMob mob = new TestMob();
        mob.pos = 10;
        mob.HP = mob.HT = 10;
        mob.state = mob.SLEEPING;
        int originalTarget = mob.targetCell();

        ChampionEnemy buff = Buff.affect(mob, buffType);
        assertNotNull(buff);
        buff.act();

        if (shouldLock) {
            assertEquals(mob.WANDERING, mob.state);
            assertEquals(hero.pos, mob.targetCell());
        } else {
            assertEquals(mob.SLEEPING, mob.state);
            assertEquals(originalTarget, mob.targetCell());
        }
    }

    private static class TestMob extends Mob {
        private int targetCell() {
            return target;
        }

        @Override
        public void notice() {
        }
    }
}
