package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PestilenceArenaControllerTest {

    @Test
    public void generatedArenasReceiveFourSeparatedReachableBraziers() {
        for (long seed = 1; seed <= 32; seed++) {
            FakeArena arena = new FakeArena(generateMap(seed));
            PestilenceArenaController controller = new PestilenceArenaController();
            int bossCell = TowerBossLayout.cell(14, 10);

            assertTrue("seed=" + seed, controller.prepare(arena, bossCell, seed));
            int[] cells = controller.brazierCells();
            assertEquals(4, cells.length);
            assertEquals(4, arena.installed.size());

            boolean[] quadrants = new boolean[4];
            for (int i = 0; i < cells.length; i++) {
                int cell = cells[i];
                assertEquals(Terrain.EMPTY, arena.original[cell]);
                assertTrue(arena.isArenaCell(cell));
                assertFalse(arena.forbidden(cell));
                assertTrue(arena.reachable(arena.heroAnchor(), cell));
                assertTrue(arena.reachable(cell, arena.exitAnchor()));
                assertTrue(arena.distance(cell, bossCell) >= 5);
                quadrants[quadrant(cell)] = true;
                for (int j = i + 1; j < cells.length; j++) {
                    assertTrue(arena.distance(cell, cells[j]) >= 6);
                }
            }
            assertArrayEquals(new boolean[]{true, true, true, true}, quadrants);
        }
    }

    @Test
    public void placementUsesOnlyEmptyUnoccupiedCells() {
        int[] map = new int[TowerBossLayout.WIDTH * TowerBossLayout.HEIGHT];
        Arrays.fill(map, Terrain.WALL);
        for (int y = 5; y <= 29; y++) {
            for (int x = 2; x <= 26; x++) {
                map[TowerBossLayout.cell(x, y)] = Terrain.WATER;
            }
        }
        int[] valid = {
                TowerBossLayout.cell(6, 8), TowerBossLayout.cell(22, 8),
                TowerBossLayout.cell(6, 25), TowerBossLayout.cell(22, 25)
        };
        for (int cell : valid) map[cell] = Terrain.EMPTY;
        int occupiedEmpty = TowerBossLayout.cell(4, 6);
        map[occupiedEmpty] = Terrain.EMPTY;

        FakeArena arena = new FakeArena(map);
        arena.forbidden.add(occupiedEmpty);
        PestilenceArenaController controller = new PestilenceArenaController();

        assertTrue(controller.prepare(arena, TowerBossLayout.cell(14, 17), 7L));
        assertArrayEquals(sorted(valid), sorted(controller.brazierCells()));
        assertFalse(arena.installed.contains(occupiedEmpty));
    }

    @Test
    public void failedPlacementDoesNotMutateArena() {
        int[] map = new int[TowerBossLayout.WIDTH * TowerBossLayout.HEIGHT];
        Arrays.fill(map, Terrain.WALL);
        FakeArena arena = new FakeArena(map);
        int[] before = map.clone();
        PestilenceArenaController controller = new PestilenceArenaController();

        assertFalse(controller.prepare(arena, TowerBossLayout.cell(14, 10), 3L));
        assertArrayEquals(before, arena.map);
        assertTrue(arena.installed.isEmpty());
        assertFalse(controller.prepared());
    }

    @Test
    public void controllerStateSurvivesBundleWithoutReinstallingFixtures() {
        FakeArena arena = new FakeArena(generateMap(11L));
        PestilenceArenaController controller = new PestilenceArenaController();
        assertTrue(controller.prepare(arena, TowerBossLayout.cell(14, 10), 11L));
        controller.putOnCooldown(2, 12);
        controller.prepareHeroAt(controller.brazierCells()[1]);

        Bundle bundle = new Bundle();
        controller.storeInBundle(bundle);
        PestilenceArenaController restored = new PestilenceArenaController();
        restored.restoreFromBundle(bundle);

        assertArrayEquals(controller.brazierCells(), restored.brazierCells());
        assertEquals(12, restored.cooldownAt(2));
        assertEquals(controller.brazierCells()[1], restored.preparedHeroCell());
        assertTrue(restored.prepared());
        assertEquals(4, arena.installed.size());
    }

    @Test
    public void brazierCooldownUsesBossTurnsAndCanBeResetAfterHarvest() {
        PestilenceArenaController controller = new PestilenceArenaController();
        controller.putOnCooldown(1, 12);

        controller.onHeroTurnStarted(null);
        assertEquals(12, controller.cooldownAt(1));
        controller.advanceBossTurn();
        assertEquals(11, controller.cooldownAt(1));

        controller.resetBrazierCooldowns();
        assertEquals(0, controller.cooldownAt(1));
    }

    @Test
    public void nearestReadyBrazierSkipsFixturesOnCooldown() {
        FakeArena arena = new FakeArena(generateMap(19L));
        PestilenceArenaController controller = new PestilenceArenaController();
        assertTrue(controller.prepare(arena, TowerBossLayout.cell(14, 10), 19L));
        int[] cells = controller.brazierCells();
        for (int i = 0; i < cells.length; i++) controller.putOnCooldown(i, 12);
        controller.putOnCooldown(2, 0);

        assertEquals(cells[2], controller.nearestReadyBrazier(TowerBossLayout.cell(14, 29)));
        assertTrue(controller.isBrazierCell(cells[2]));
        assertFalse(controller.isBrazierCell(TowerBossLayout.cell(14, 17)));
    }

    private static int[] generateMap(long seed) {
        Random.pushGenerator(seed);
        try {
            return TowerBossLayout.generateMap();
        } finally {
            Random.popGenerator();
        }
    }

    private static int quadrant(int cell) {
        int x = cell % TowerBossLayout.WIDTH;
        int y = cell / TowerBossLayout.WIDTH;
        return (y > 17 ? 2 : 0) + (x > 14 ? 1 : 0);
    }

    private static int[] sorted(int[] values) {
        int[] copy = values.clone();
        Arrays.sort(copy);
        return copy;
    }

    private static final class FakeArena implements PestilenceArenaController.Arena {
        private final int[] original;
        private final int[] map;
        private final Set<Integer> forbidden = new HashSet<>();
        private final List<Integer> installed = new ArrayList<>();

        private FakeArena(int[] map) {
            this.original = map.clone();
            this.map = map.clone();
        }

        @Override public int length() { return map.length; }
        @Override public int width() { return TowerBossLayout.WIDTH; }
        @Override public int terrain(int cell) { return map[cell]; }
        @Override public boolean passable(int cell) {
            return (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0;
        }
        @Override public boolean isArenaCell(int cell) { return TowerBossLayout.isArenaCell(cell); }
        @Override public boolean forbidden(int cell) { return forbidden.contains(cell); }
        @Override public int heroAnchor() { return TowerBossLayout.cell(14, 29); }
        @Override public int exitAnchor() { return TowerBossLayout.cell(14, 5); }
        @Override public void installBrazier(int cell) {
            installed.add(cell);
            map[cell] = Terrain.TRAP;
        }

        int distance(int a, int b) {
            int ax = a % width();
            int ay = a / width();
            int bx = b % width();
            int by = b / width();
            return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
        }

        boolean reachable(int start, int target) {
            boolean[] seen = new boolean[map.length];
            ArrayList<Integer> queue = new ArrayList<>();
            seen[start] = true;
            queue.add(start);
            for (int at = 0; at < queue.size(); at++) {
                int cell = queue.get(at);
                if (cell == target) return true;
                int x = cell % width();
                int[] next = {cell - width(), cell + 1, cell + width(), cell - 1};
                for (int value : next) {
                    // Validate the route chosen before the fixtures were installed. A visible trap
                    // uses Terrain.AVOID rather than Terrain.PASSABLE after installation, but that
                    // does not make its cell physically impassable to actors.
                    if (value < 0 || value >= map.length || seen[value]
                            || (Terrain.flags[original[value]] & Terrain.PASSABLE) == 0) continue;
                    if (Math.abs(value % width() - x) > 1) continue;
                    seen[value] = true;
                    queue.add(value);
                }
            }
            return false;
        }
    }
}
