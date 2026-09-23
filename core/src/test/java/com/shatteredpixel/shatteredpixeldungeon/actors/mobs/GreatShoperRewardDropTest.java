package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class GreatShoperRewardDropTest {

    @Test
    public void rewardUsesTheOriginalCellWhenItIsValid() {
        TestLevel level = openLevel(5, 5);

        assertEquals(12, GreatShoper.rewardDropCell(level, 12));
    }

    @Test
    public void rewardMovesOffAnImpassableThroneCell() {
        TestLevel level = openLevel(5, 5);
        level.passable[12] = false;
        level.solid[12] = true;

        int result = GreatShoper.rewardDropCell(level, 12);

        assertNotEquals(12, result);
        assertTrue(level.passable[result]);
    }

    private static TestLevel openLevel(int width, int height) {
        TestLevel level = new TestLevel();
        level.setSize(width, height);
        Arrays.fill(level.passable, true);
        Arrays.fill(level.solid, false);
        level.mobs = new HashSet<>();
        level.transitions = new ArrayList<>();
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
