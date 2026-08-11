package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.IncubatingMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.OutbreakMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.PaleMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.PurifyingIncense;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Infection;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
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
    private static final String WATER_CELL = "water_cell";
    private static final String WATER_TURNS = "water_turns";

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
    private int waterCell = -1;
    private int waterTurns;

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

    public void onHeroTurnStarted(Hero hero) {
        if (hero == null || Dungeon.level == null) return;
        int brazier = brazierIndex(hero.pos);
        preparedHeroCell = brazier >= 0 && cooldowns[brazier] == 0 ? hero.pos : -1;

        Class<? extends Blob> miasma = activeMiasmaAt(hero.pos);
        if (miasma == null || hero.isImmune(miasma)) {
            waterCell = -1;
            waterTurns = 0;
            return;
        }
        if (Dungeon.level.water[hero.pos]) {
            if (waterCell != hero.pos) {
                waterCell = hero.pos;
                waterTurns = 1;
            } else {
                waterTurns++;
            }
            if ((waterTurns & 1) != 0) return;
        } else {
            waterCell = -1;
            waterTurns = 0;
        }
        Infection.addStacks(hero, 1);
        if (miasma == OutbreakMiasma.class) {
            Buff.affect(hero, Poison.class).set(3f);
        } else if (miasma == PaleMiasma.class) {
            Buff.prolong(hero, Slow.class, 2f);
        }
    }

    public void onHeroWaited(Hero hero) {
        if (hero == null || hero.pos != preparedHeroCell) return;
        int index = brazierIndex(hero.pos);
        if (index < 0 || cooldowns[index] > 0) return;
        activateBrazier(hero, index);
    }

    public void onHeroConsumableUsed(Hero hero, Item item) {
        if (hero == null || item == null || Dungeon.level == null) return;
        if ((item instanceof Potion || item instanceof Food || item instanceof Scroll)
                && Blob.volumeAt(hero.pos, OutbreakMiasma.class) > 0) {
            Infection.addStacks(hero, 1);
        }
    }

    /** Returns the cell of the nearest ready brazier, or -1 when none is available. */
    public int nearestReadyBrazier(int origin) {
        int bestCell = -1;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < brazierCells.length; i++) {
            if (cooldowns[i] > 0) continue;
            int distance = Dungeon.level == null
                    ? Math.abs(brazierCells[i] - origin)
                    : Dungeon.level.distance(origin, brazierCells[i]);
            if (distance < bestDistance || distance == bestDistance && brazierCells[i] < bestCell) {
                bestDistance = distance;
                bestCell = brazierCells[i];
            }
        }
        return bestCell;
    }

    public boolean isBrazierCell(int cell) {
        return brazierIndex(cell) >= 0;
    }

    public void advanceBossTurn() {
        for (int i = 0; i < cooldowns.length; i++) {
            if (cooldowns[i] > 0) cooldowns[i]--;
        }
    }

    public void resetBrazierCooldowns() {
        Arrays.fill(cooldowns, 0);
    }

    /** Clears transient encounter state while keeping the installed fixtures inert and reusable. */
    public void finishEncounter() {
        preparedHeroCell = -1;
        waterCell = -1;
        waterTurns = 0;
        Arrays.fill(cooldowns, 0);
    }

    private int brazierIndex(int cell) {
        for (int i = 0; i < brazierCells.length; i++) if (brazierCells[i] == cell) return i;
        return -1;
    }

    private static Class<? extends Blob> activeMiasmaAt(int cell) {
        if (Blob.volumeAt(cell, PaleMiasma.class) > 0) return PaleMiasma.class;
        if (Blob.volumeAt(cell, OutbreakMiasma.class) > 0) return OutbreakMiasma.class;
        if (Blob.volumeAt(cell, IncubatingMiasma.class) > 0) return IncubatingMiasma.class;
        return null;
    }

    private void activateBrazier(Hero hero, int index) {
        int center = brazierCells[index];
        int width = Dungeon.level.width();
        for (int cell = 0; cell < Dungeon.level.length(); cell++) {
            int dx = Math.abs(cell % width - center % width);
            int dy = Math.abs(cell / width - center / width);
            if (Math.max(dx, dy) > 3) continue;
            clearAt(cell, IncubatingMiasma.class);
            clearAt(cell, OutbreakMiasma.class);
            clearAt(cell, PaleMiasma.class);
            if (!Dungeon.level.solid[cell]) Blob.seed(cell, 3, PurifyingIncense.class);
        }
        Infection.set(hero, Math.max(0, Infection.stacks(hero) - 2));
        cooldowns[index] = 12;
        preparedHeroCell = -1;
    }

    private static void clearAt(int cell, Class<? extends Blob> type) {
        Blob blob = Dungeon.level.blobs.get(type);
        if (blob != null) blob.clear(cell);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(PREPARED, prepared);
        bundle.put(BRAZIER_CELLS, brazierCells);
        bundle.put(COOLDOWNS, cooldowns);
        bundle.put(PREPARED_HERO_CELL, preparedHeroCell);
        bundle.put(WATER_CELL, waterCell);
        bundle.put(WATER_TURNS, waterTurns);
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
            waterCell = bundle.contains(WATER_CELL) ? bundle.getInt(WATER_CELL) : -1;
            waterTurns = Math.max(0, bundle.getInt(WATER_TURNS));
        } else {
            prepared = false;
            brazierCells = new int[0];
            cooldowns = new int[BRAZIER_COUNT];
            preparedHeroCell = -1;
            waterCell = -1;
            waterTurns = 0;
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
