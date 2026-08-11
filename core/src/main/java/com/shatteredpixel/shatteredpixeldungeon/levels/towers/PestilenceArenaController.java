package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PlagueBrazier;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Persistent state and deterministic fixture placement for the Pestilence arena. */
public class PestilenceArenaController implements Bundlable {

    private static final String PREPARED = "prepared";
    private static final String BRAZIER_CELLS = "brazier_cells";
    private static final String COOLDOWNS = "cooldowns";
    private static final String PREPARED_HERO_CELL = "prepared_hero_cell";

    private static final int BRAZIER_COUNT = 4;
    private static final int MIN_BRAZIER_DISTANCE = 6;
    private static final int MIN_BOSS_DISTANCE = 5;
    private static final int MIN_ANCHOR_DISTANCE = 4;
    private static final long PLACEMENT_SALT = 0x4252415A49455253L;

    public interface Arena {
        int length();
        int width();
        int terrain(int cell);
        boolean passable(int cell);
        boolean isArenaCell(int cell);
        boolean forbidden(int cell);
        int heroAnchor();
        int exitAnchor();
        void installBrazier(int cell);
    }

    private boolean prepared;
    private int[] brazierCells = new int[0];
    private int[] cooldowns = new int[BRAZIER_COUNT];
    private int preparedHeroCell = -1;

    public boolean prepare(TowerBossLevel level, int bossCell) {
        long seed = TowerBossGenerator.mix64(Dungeon.seed
                ^ ((long) Dungeon.depth << 32) ^ Dungeon.branch ^ PLACEMENT_SALT);
        return prepare(new LevelArena(level), bossCell, seed);
    }

    boolean prepare(Arena arena, int bossCell, long seed) {
        if (prepared) return brazierCells.length == BRAZIER_COUNT;

        ArrayList<Integer>[] quadrants = new ArrayList[BRAZIER_COUNT];
        ArrayList<Integer> allCandidates = new ArrayList<>();
        for (int i = 0; i < BRAZIER_COUNT; i++) quadrants[i] = new ArrayList<>();
        for (int cell = 0; cell < arena.length(); cell++) {
            if (!isCandidate(arena, cell, bossCell)) continue;
            quadrants[quadrant(arena, cell)].add(cell);
            allCandidates.add(cell);
        }

        Random random = new Random(seed);
        for (List<Integer> cells : quadrants) Collections.shuffle(cells, random);
        Collections.shuffle(allCandidates, random);

        ArrayList<Integer> chosen = new ArrayList<>(BRAZIER_COUNT);
        if (!chooseByQuadrant(arena, quadrants, 0, chosen)
                || !allConnected(arena, chosen, bossCell)) {
            chosen.clear();
            if (!chooseGlobally(arena, allCandidates, 0, chosen)
                    || !allConnected(arena, chosen, bossCell)) {
                return false;
            }
        }

        brazierCells = new int[BRAZIER_COUNT];
        for (int i = 0; i < BRAZIER_COUNT; i++) brazierCells[i] = chosen.get(i);
        for (int cell : brazierCells) arena.installBrazier(cell);
        cooldowns = new int[BRAZIER_COUNT];
        preparedHeroCell = -1;
        prepared = true;
        return true;
    }

    private static boolean isCandidate(Arena arena, int cell, int bossCell) {
        return arena.isArenaCell(cell)
                && arena.terrain(cell) == Terrain.EMPTY
                && arena.passable(cell)
                && !arena.forbidden(cell)
                && distance(arena, cell, bossCell) >= MIN_BOSS_DISTANCE
                && distance(arena, cell, arena.heroAnchor()) >= MIN_ANCHOR_DISTANCE
                && distance(arena, cell, arena.exitAnchor()) >= MIN_ANCHOR_DISTANCE;
    }

    private static boolean chooseByQuadrant(Arena arena, ArrayList<Integer>[] quadrants,
            int quadrant, ArrayList<Integer> chosen) {
        if (quadrant == BRAZIER_COUNT) return true;
        for (int cell : quadrants[quadrant]) {
            if (!farEnough(arena, cell, chosen)) continue;
            chosen.add(cell);
            if (chooseByQuadrant(arena, quadrants, quadrant + 1, chosen)) return true;
            chosen.remove(chosen.size() - 1);
        }
        return false;
    }

    private static boolean chooseGlobally(Arena arena, List<Integer> candidates,
            int start, ArrayList<Integer> chosen) {
        if (chosen.size() == BRAZIER_COUNT) return true;
        int remaining = BRAZIER_COUNT - chosen.size();
        for (int i = start; i <= candidates.size() - remaining; i++) {
            int cell = candidates.get(i);
            if (!farEnough(arena, cell, chosen)) continue;
            chosen.add(cell);
            if (chooseGlobally(arena, candidates, i + 1, chosen)) return true;
            chosen.remove(chosen.size() - 1);
        }
        return false;
    }

    private static boolean farEnough(Arena arena, int cell, List<Integer> chosen) {
        for (int other : chosen) {
            if (distance(arena, cell, other) < MIN_BRAZIER_DISTANCE) return false;
        }
        return true;
    }

    private static int quadrant(Arena arena, int cell) {
        int x = cell % arena.width();
        int y = cell / arena.width();
        int centerX = arena.width() / 2;
        int centerY = TowerBossLayout.HEIGHT / 2;
        return (y > centerY ? 2 : 0) + (x > centerX ? 1 : 0);
    }

    private static int distance(Arena arena, int a, int b) {
        int ax = a % arena.width();
        int ay = a / arena.width();
        int bx = b % arena.width();
        int by = b / arena.width();
        return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
    }

    private static boolean allConnected(Arena arena, List<Integer> chosen, int bossCell) {
        if (chosen.size() != BRAZIER_COUNT || !reachable(arena, arena.heroAnchor(), bossCell)
                || !reachable(arena, arena.heroAnchor(), arena.exitAnchor())) return false;
        for (int cell : chosen) {
            if (!reachable(arena, arena.heroAnchor(), cell)) return false;
        }
        return true;
    }

    private static boolean reachable(Arena arena, int start, int target) {
        if (start < 0 || target < 0 || start >= arena.length() || target >= arena.length()
                || !arena.passable(start) || !arena.passable(target)) return false;
        boolean[] seen = new boolean[arena.length()];
        int[] queue = new int[arena.length()];
        int head = 0;
        int tail = 0;
        seen[start] = true;
        queue[tail++] = start;
        while (head < tail) {
            int cell = queue[head++];
            if (cell == target) return true;
            int x = cell % arena.width();
            int[] neighbours = {cell - arena.width(), cell + 1, cell + arena.width(), cell - 1};
            for (int next : neighbours) {
                if (next < 0 || next >= arena.length() || seen[next] || !arena.passable(next)) continue;
                if (Math.abs(next % arena.width() - x) > 1) continue;
                seen[next] = true;
                queue[tail++] = next;
            }
        }
        return false;
    }

    public boolean prepared() {
        return prepared;
    }

    public int[] brazierCells() {
        return brazierCells.clone();
    }

    public int cooldownAt(int index) {
        return index >= 0 && index < cooldowns.length ? cooldowns[index] : 0;
    }

    public void putOnCooldown(int index, int turns) {
        if (index < 0 || index >= cooldowns.length) throw new IndexOutOfBoundsException();
        cooldowns[index] = Math.max(0, turns);
    }

    public void prepareHeroAt(int cell) {
        preparedHeroCell = cell;
    }

    public int preparedHeroCell() {
        return preparedHeroCell;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(PREPARED, prepared);
        bundle.put(BRAZIER_CELLS, brazierCells);
        bundle.put(COOLDOWNS, cooldowns);
        bundle.put(PREPARED_HERO_CELL, preparedHeroCell);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        int[] restoredCells = bundle.getIntArray(BRAZIER_CELLS);
        int[] restoredCooldowns = bundle.getIntArray(COOLDOWNS);
        if (bundle.getBoolean(PREPARED) && restoredCells.length == BRAZIER_COUNT) {
            prepared = true;
            brazierCells = restoredCells.clone();
            cooldowns = restoredCooldowns.length == BRAZIER_COUNT
                    ? restoredCooldowns.clone() : new int[BRAZIER_COUNT];
            for (int i = 0; i < cooldowns.length; i++) cooldowns[i] = Math.max(0, cooldowns[i]);
            preparedHeroCell = bundle.contains(PREPARED_HERO_CELL)
                    ? bundle.getInt(PREPARED_HERO_CELL) : -1;
        } else {
            prepared = false;
            brazierCells = new int[0];
            cooldowns = new int[BRAZIER_COUNT];
            preparedHeroCell = -1;
        }
    }

    private static final class LevelArena implements Arena {
        private final TowerBossLevel level;

        private LevelArena(TowerBossLevel level) {
            this.level = level;
        }

        @Override public int length() { return level.length(); }
        @Override public int width() { return level.width(); }
        @Override public int terrain(int cell) { return level.map[cell]; }
        @Override public boolean passable(int cell) { return level.passable[cell]; }
        @Override public boolean isArenaCell(int cell) { return TowerBossLayout.isArenaCell(cell); }
        @Override public int heroAnchor() { return TowerBossLayout.cell(14, 29); }
        @Override public int exitAnchor() { return TowerBossLayout.cell(14, 5); }

        @Override
        public boolean forbidden(int cell) {
            return level.getTransition(cell) != null
                    || level.heaps.get(cell) != null
                    || level.plants.get(cell) != null
                    || level.traps.get(cell) != null
                    || Actor.findChar(cell) != null;
        }

        @Override
        public void installBrazier(int cell) {
            PlagueBrazier brazier = new PlagueBrazier();
            brazier.set(cell);
            level.traps.put(cell, brazier);
            level.set(cell, Terrain.TRAP, level);
            GameScene.updateMap(cell);
        }
    }
}
