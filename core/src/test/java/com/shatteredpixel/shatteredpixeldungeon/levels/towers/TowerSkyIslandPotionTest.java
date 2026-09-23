package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.watabou.utils.SparseArray;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TowerSkyIslandPotionTest {

    @Test
    public void selectsOnePotionCellForEveryOrdinaryStandardRoom() {
        TestLevel level = new TestLevel();
        level.feeling = Level.Feeling.SKY_ISLAND;
        level.setSize(16, 10);
        level.transitions = new ArrayList<>();
        level.heaps = new SparseArray<>();
        level.traps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.mobs = new HashSet<>();

        TestStandardRoom first = new TestStandardRoom();
        first.set(1, 1, 5, 5);
        TestStandardRoom second = new TestStandardRoom();
        second.set(7, 1, 11, 5);
        EntranceStandardRoom entrance = new EntranceStandardRoom();
        entrance.set(1, 6, 5, 9);

        makePassable(level, first);
        makePassable(level, second);
        makePassable(level, entrance);

        ArrayList<Integer> cells = TowerGenerationRules.skyIslandPotionCells(
                level, Arrays.asList(first, second, entrance));

        assertEquals(2, cells.size());
        assertTrue(first.inside(level.cellToPoint(cells.get(0)))
                || first.inside(level.cellToPoint(cells.get(1))));
        assertTrue(second.inside(level.cellToPoint(cells.get(0)))
                || second.inside(level.cellToPoint(cells.get(1))));
    }

    private static void makePassable(Level level, Room room) {
        for (Point point : room.getPoints()) {
            int cell = level.pointToCell(point);
            level.map[cell] = Terrain.EMPTY;
            level.passable[cell] = true;
            level.pit[cell] = false;
        }
    }

    private static class TestStandardRoom extends StandardRoom {
        @Override
        public void paint(Level level) {
        }
    }

    private static class EntranceStandardRoom extends TestStandardRoom {
        @Override
        public boolean isEntrance() {
            return true;
        }
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
