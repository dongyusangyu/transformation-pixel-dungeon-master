package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GooSummonPlacementTest {

    private Level previousLevel;

    @Before
    public void setUp() {
        previousLevel = Dungeon.level;
        Actor.clear();
        Actor.resetNextID();
        Dungeon.level = openLevel(9, 9);
    }

    @After
    public void tearDown() {
        Actor.clear();
        Actor.resetNextID();
        Dungeon.level = previousLevel;
    }

    @Test
    public void summonCandidatesUseOnlyValidUnoccupiedAdjacentCells() {
        int gooPos = 40;
        int[] neighbours = {30, 31, 32, 39, 41, 48, 49, 50};
        Level level = Dungeon.level;
        level.solid[30] = true;
        level.passable[31] = false;
        level.pit[32] = true;

        Mob existingMob = mobAt(39);
        Hero hero = TestHeroFactory.create();
        hero.pos = 41;
        Actor.add(hero);

        Set<Integer> reserved = new HashSet<>();
        reserved.add(48);
        ArrayList<Integer> candidates = Goo.summonCandidates(level, gooPos, reserved);

        assertEquals(Arrays.asList(49, 50), candidates);
        assertFalse(candidates.contains(existingMob.pos));
        assertFalse(candidates.contains(hero.pos));
        assertFalse(candidates.contains(gooPos));
        for (int cell : candidates) {
            assertTrue(Arrays.stream(neighbours).anyMatch(neighbour -> neighbour == cell));
        }
    }

    @Test
    public void summonCandidateSearchSkipsOutOfBoundsNeighbours() {
        ArrayList<Integer> candidates = Goo.summonCandidates(Dungeon.level, 10, new HashSet<>());

        assertEquals(Arrays.asList(11, 19, 20), candidates);
        assertTrue(candidates.stream().noneMatch(cell -> cell < 0 || cell >= Dungeon.level.length()));
    }

    private static Mob mobAt(int pos) {
        Mob mob = new CausticSlime();
        mob.pos = pos;
        Actor.add(mob);
        Dungeon.level.mobs.add(mob);
        return mob;
    }

    private static Level openLevel(int width, int height) {
        TestLevel level = new TestLevel();
        level.setSize(width, height);
        Arrays.fill(level.passable, true);
        Arrays.fill(level.solid, false);
        Arrays.fill(level.pit, false);
        level.blobs = new HashMap<>();
        level.plants = new SparseArray<>();
        level.mobs = new HashSet<>();
        return level;
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
}
