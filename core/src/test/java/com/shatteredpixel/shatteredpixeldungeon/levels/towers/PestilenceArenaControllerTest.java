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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PestilenceArenaControllerTest {

    @Test
    public void generatedArenasReceiveOneLeftReachablePurifierAndSafeRoute() {
        for (long seed = 1; seed <= 32; seed++) {
            FakeArena arena = new FakeArena(generateMap(seed));
            PestilenceArenaController controller = new PestilenceArenaController();

            assertTrue("seed=" + seed,
                    controller.prepare(arena, TowerBossLayout.cell(14, 10), seed));
            int purifier = controller.purifierCell();
            assertEquals(1, arena.installed.size());
            assertEquals(purifier, (int) arena.installed.iterator().next());
            assertTrue(purifier % arena.width() < arena.width() / 2);
            assertTrue(arena.reachable(arena.heroAnchor(), purifier));

            int[] route = controller.safeRoute();
            assertTrue(route.length > 1);
            assertEquals(arena.heroAnchor(), route[0]);
            assertEquals(purifier, route[route.length - 1]);
            for (int i = 0; i < route.length; i++) {
                assertTrue(arena.passableBeforeFixture(route[i]));
                if (i > 0) assertEquals(1, cardinalDistance(arena, route[i - 1], route[i]));
            }
        }
    }

    @Test
    public void purifierPlacementUsesOnlyEmptyUnoccupiedCells() {
        int[] map = new int[TowerBossLayout.WIDTH * TowerBossLayout.HEIGHT];
        Arrays.fill(map, Terrain.WALL);
        carve(map, 14, 29, 6, 25);
        int valid = TowerBossLayout.cell(6, 25);
        int occupied = TowerBossLayout.cell(5, 25);
        map[occupied] = Terrain.EMPTY;

        FakeArena arena = new FakeArena(map);
        for (int cell = 0; cell < map.length; cell++) {
            if (map[cell] == Terrain.EMPTY && cell != valid) arena.forbidden.add(cell);
        }
        arena.forbidden.add(occupied);
        PestilenceArenaController controller = new PestilenceArenaController();

        assertTrue(controller.prepare(arena, TowerBossLayout.cell(14, 10), 7L));
        assertEquals(valid, controller.purifierCell());
        assertFalse(arena.installed.contains(occupied));
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
    public void activationIsImmediateThenRelocatesAwayAndStartsCooldown() {
        FakeArena arena = new FakeArena(generateMap(19L));
        PestilenceArenaController controller = new PestilenceArenaController();
        assertTrue(controller.prepare(arena, TowerBossLayout.cell(14, 10), 19L));
        int old = controller.purifierCell();

        assertTrue(controller.activate(arena, old));
        int moved = controller.purifierCell();
        assertNotEquals(old, moved);
        assertTrue(arena.distance(old, moved) >= PestilenceArenaController.MIN_RELOCATION_DISTANCE);
        assertEquals(PestilenceArenaController.PURIFIER_COOLDOWN, controller.cooldownAt(0));
        assertEquals(Set.of(moved), arena.installed);

        assertFalse(controller.activate(arena, moved));
        assertEquals(moved, controller.purifierCell());
    }

    @Test
    public void relocationIsDeterministicAndDoesNotNeedGlobalRandom() {
        FakeArena firstArena = new FakeArena(generateMap(23L));
        FakeArena secondArena = new FakeArena(generateMap(23L));
        PestilenceArenaController first = new PestilenceArenaController();
        PestilenceArenaController second = new PestilenceArenaController();
        assertTrue(first.prepare(firstArena, TowerBossLayout.cell(14, 10), 23L));
        assertTrue(second.prepare(secondArena, TowerBossLayout.cell(14, 10), 23L));

        assertTrue(first.activate(firstArena, first.purifierCell()));
        assertTrue(second.activate(secondArena, second.purifierCell()));
        assertEquals(first.purifierCell(), second.purifierCell());
    }

    @Test
    public void controllerStateSurvivesBundleWithoutReinstallingFixture() {
        FakeArena arena = new FakeArena(generateMap(11L));
        PestilenceArenaController controller = new PestilenceArenaController();
        assertTrue(controller.prepare(arena, TowerBossLayout.cell(14, 10), 11L));
        assertTrue(controller.activate(arena, controller.purifierCell()));
        controller.advanceBossTurn();

        Bundle bundle = new Bundle();
        controller.storeInBundle(bundle);
        PestilenceArenaController restored = new PestilenceArenaController();
        restored.restoreFromBundle(bundle);

        assertEquals(controller.purifierCell(), restored.purifierCell());
        assertEquals(11, restored.cooldownAt(0));
        assertTrue(restored.prepared());
        assertEquals(1, arena.installed.size());

        Bundle roundTrip = new Bundle();
        restored.storeInBundle(roundTrip);
        assertEquals(bundle.getLong("relocation_rng_state"),
                roundTrip.getLong("relocation_rng_state"));
    }

    @Test
    public void oldFourBrazierSaveRestoresOnePurifier() {
        Bundle legacy = new Bundle();
        int[] cells = {101, 202, 303, 404};
        legacy.put("prepared", true);
        legacy.put("brazier_cells", cells);
        legacy.put("cooldowns", new int[]{3, 4, 5, 6});

        PestilenceArenaController restored = new PestilenceArenaController();
        restored.restoreFromBundle(legacy);

        assertTrue(restored.prepared());
        assertEquals(cells[0], restored.purifierCell());
        assertEquals(3, restored.cooldownAt(0));
    }

    @Test
    public void purifierCooldownUsesOnlyBossTurns() {
        PestilenceArenaController controller = new PestilenceArenaController();
        controller.putOnCooldown(0, PestilenceArenaController.PURIFIER_COOLDOWN);

        controller.onHeroTurnStarted(null);
        assertEquals(12, controller.cooldownAt(0));
        controller.advanceBossTurn();
        assertEquals(11, controller.cooldownAt(0));
        controller.resetBrazierCooldowns();
        assertEquals(0, controller.cooldownAt(0));
    }

    private static int[] generateMap(long seed) {
        Random.pushGenerator(seed);
        try {
            return TowerBossLayout.generateMap();
        } finally {
            Random.popGenerator();
        }
    }

    private static void carve(int[] map, int fromX, int fromY, int toX, int toY) {
        int x = fromX;
        int y = fromY;
        map[TowerBossLayout.cell(x, y)] = Terrain.EMPTY;
        while (x != toX) {
            x += Integer.compare(toX, x);
            map[TowerBossLayout.cell(x, y)] = Terrain.EMPTY;
        }
        while (y != toY) {
            y += Integer.compare(toY, y);
            map[TowerBossLayout.cell(x, y)] = Terrain.EMPTY;
        }
    }

    private static int cardinalDistance(FakeArena arena, int a, int b) {
        return Math.abs(a % arena.width() - b % arena.width())
                + Math.abs(a / arena.width() - b / arena.width());
    }

    private static final class FakeArena implements PestilenceArenaController.Arena {
        private final int[] original;
        private final int[] map;
        private final Set<Integer> forbidden = new HashSet<>();
        private final Set<Integer> installed = new HashSet<>();

        private FakeArena(int[] map) {
            this.original = map.clone();
            this.map = map.clone();
        }

        @Override public int length() { return map.length; }
        @Override public int width() { return TowerBossLayout.WIDTH; }
        @Override public int terrain(int cell) { return map[cell]; }
        @Override public boolean passable(int cell) { return passableBeforeFixture(cell); }
        @Override public boolean isArenaCell(int cell) { return TowerBossLayout.isArenaCell(cell); }
        @Override public boolean forbidden(int cell) { return forbidden.contains(cell); }
        @Override public int heroAnchor() { return TowerBossLayout.cell(14, 29); }
        @Override public int exitAnchor() { return TowerBossLayout.cell(14, 5); }

        @Override
        public void installPurifier(int cell) {
            installed.add(cell);
            map[cell] = Terrain.TRAP;
        }

        @Override
        public void relocatePurifier(int from, int to) {
            installed.remove(from);
            map[from] = Terrain.EMPTY;
            installed.add(to);
            map[to] = Terrain.TRAP;
        }

        boolean passableBeforeFixture(int cell) {
            return cell >= 0 && cell < original.length
                    && (Terrain.flags[original[cell]] & Terrain.PASSABLE) != 0;
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
            List<Integer> queue = new ArrayList<>();
            seen[start] = true;
            queue.add(start);
            for (int at = 0; at < queue.size(); at++) {
                int cell = queue.get(at);
                if (cell == target) return true;
                int x = cell % width();
                int[] next = {cell - width(), cell + 1, cell + width(), cell - 1};
                for (int value : next) {
                    if (value < 0 || value >= map.length || seen[value]
                            || !passableBeforeFixture(value)) continue;
                    if (Math.abs(value % width() - x) > 1) continue;
                    seen[value] = true;
                    queue.add(value);
                }
            }
            return false;
        }
    }
}
