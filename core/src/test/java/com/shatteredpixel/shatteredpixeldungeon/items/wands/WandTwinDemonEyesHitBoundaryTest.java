package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TwinDemonEyes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class WandTwinDemonEyesHitBoundaryTest {

    private Hero oldHero;
    private Level oldLevel;

    @Before public void setUp() {
        oldHero = Dungeon.hero;
        oldLevel = Dungeon.level;
        Actor.clear();
        Actor.resetNextID();
        Dungeon.hero = TestHeroFactory.create();
        Dungeon.hero.pos = 40;
        Dungeon.level = new TestLevel();
        Dungeon.level.setSize(9, 9);
    }

    @After public void tearDown() {
        Actor.clear();
        Actor.resetNextID();
        Dungeon.hero = oldHero;
        Dungeon.level = oldLevel;
    }

    @Test
    public void nearestSurvivingHostileInActualAreaIsSelectedEvenWhenAimedCellMissed() {
        Rat farther = new Rat();
        farther.pos = 44;
        Actor.add(farther);
        Rat nearer = new Rat();
        nearer.pos = 42;
        Actor.add(nearer);
        Set<Char> affected = new LinkedHashSet<>();
        affected.add(farther);
        affected.add(nearer);

        assertSame(nearer, TwinDemonEyes.nearestZapTarget(affected, Dungeon.hero));
    }

    @Test
    public void deadFriendlyAndHeroTargetsAreIgnored() {
        Rat dead = new Rat();
        dead.pos = 41;
        dead.HP = 0;
        Actor.add(dead);
        Rat friendly = new Rat();
        friendly.pos = 42;
        friendly.alignment = Char.Alignment.ALLY;
        Actor.add(friendly);
        Set<Char> affected = new LinkedHashSet<>();
        affected.add(dead);
        affected.add(friendly);
        affected.add(Dungeon.hero);

        assertNull(TwinDemonEyes.nearestZapTarget(affected, Dungeon.hero));
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
