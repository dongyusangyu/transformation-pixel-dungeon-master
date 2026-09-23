package com.shatteredpixel.shatteredpixeldungeon.plants;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SungrassHealthMovementTest {

    @Test
    public void playersOriginalMonsterTalentDoesNotProtectMovingMob() {
        Hero previousHero = Dungeon.hero;
        try {
            Dungeon.hero = heroWithOriginalMonster(3);
            Gnoll mob = new Gnoll();
            mob.HP = mob.HT = 20;
            mob.pos = 0;
            Sungrass.Health health = Buff.affect(mob, Sungrass.Health.class);
            health.boost(10);

            mob.pos = 1;
            health.act();

            assertNull(mob.buff(Sungrass.Health.class));
        } finally {
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void originalMonsterPlusThreeProtectsMovingPlayer() {
        Hero previousHero = Dungeon.hero;
        try {
            Hero player = heroWithOriginalMonster(3);
            Dungeon.hero = player;
            player.HP = 10;
            player.HT = 20;
            player.pos = 0;
            Sungrass.Health health = Buff.affect(player, Sungrass.Health.class);
            health.boost(10);

            player.pos = 1;
            health.act();

            assertSame(health, player.buff(Sungrass.Health.class));
        } finally {
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void movingMobWithoutPlayerDoesNotCrashAndLosesHealth() {
        Hero previousHero = Dungeon.hero;
        try {
            Dungeon.hero = null;
            Gnoll mob = new Gnoll();
            mob.HP = mob.HT = 20;
            mob.pos = 0;
            Sungrass.Health health = Buff.affect(mob, Sungrass.Health.class);
            health.boost(10);

            mob.pos = 1;
            health.act();

            assertNull(mob.buff(Sungrass.Health.class));
        } finally {
            Dungeon.hero = previousHero;
        }
    }

    private static Hero heroWithOriginalMonster(int points) {
        Hero hero = TestHeroFactory.create();
        LinkedHashMap<Talent, Integer> talents = new LinkedHashMap<>();
        talents.put(Talent.ORIGINAL_MONSTER, points);
        hero.talents.add(talents);
        return hero;
    }
}
