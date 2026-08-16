package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.IncubatingMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.OutbreakMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.PaleMiasma;
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
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Persistent state and deterministic purifier placement for the Pestilence arena. */
public class PestilenceArenaController implements Bundlable {

    static final int PRELUDE_MIASMA_AMOUNT = 20_000;
    static final int PURIFIER_BOSS_DAMAGE = 100;
    static final int PURIFIER_COOLDOWN = 10;
    static final int MIN_RELOCATION_DISTANCE = 6;

    private static final String PREPARED = "prepared";
    private static final String PRELUDE_STARTED = "prelude_started";
    private static final String PURIFIER_CELL = "purifier_cell";
    private static final String PURIFIER_COOLDOWN_KEY = "purifier_cooldown";
    private static final String SAFE_ROUTE = "safe_route";
    private static final String BOSS_CELL = "boss_cell";
    private static final String RELOCATION_RNG_STATE = "relocation_rng_state";
    private static final String LEGACY_BRAZIER_CELLS = "brazier_cells";
    private static final String LEGACY_COOLDOWNS = "cooldowns";
    private static final String WATER_CELL = "water_cell";
    private static final String WATER_TURNS = "water_turns";

    private static final int MIN_BOSS_DISTANCE = 5;
    private static final int MIN_ANCHOR_DISTANCE = 4;
    private static final long PLACEMENT_SALT = 0x4252415A49455253L;
    private static final long RELOCATION_SALT = 0x5055524946494552L;

    public interface Arena {
        int length();
        int width();
        int terrain(int cell);
        boolean passable(int cell);
        boolean isArenaCell(int cell);
        boolean forbidden(int cell);
        int heroAnchor();
        int exitAnchor();
        void installPurifier(int cell);
        void relocatePurifier(int from, int to);
        void setPurifierReady(int cell, boolean ready);
    }

    private boolean prepared;
    private boolean preludeStarted;
    private int purifierCell = -1;
    private int purifierCooldown;
    private int[] safeRoute = new int[0];
    private int bossCell = -1;
    private long relocationRngState;
    private int waterCell = -1;
    private int waterTurns;

    public boolean prepare(TowerBossLevel level, int bossCell) {
        long seed = TowerBossGenerator.mix64(Dungeon.seed
                ^ ((long) Dungeon.depth << 32) ^ Dungeon.branch ^ PLACEMENT_SALT);
        return prepare(new LevelArena(level), bossCell, seed);
    }

    boolean beginPrelude(Arena arena, int bossCell, long seed) {
        if (preludeStarted) return prepared && purifierCell >= 0;
        if (!prepare(arena, bossCell, seed)) return false;
        preludeStarted = true;
        return true;
    }

    boolean beginPrelude(TowerBossLevel level, int bossCell) {
        long seed = TowerBossGenerator.mix64(Dungeon.seed
                ^ ((long) Dungeon.depth << 32) ^ Dungeon.branch ^ PLACEMENT_SALT);
        LevelArena arena = new LevelArena(level);
        if (!beginPrelude(arena, bossCell, seed)) return false;

        PaleMiasma miasma = null;
        for (int cell : preludeMiasmaCells(arena)) {
            miasma = Blob.seed(cell, PRELUDE_MIASMA_AMOUNT, PaleMiasma.class, level);
        }
        if (miasma != null) GameScene.add(miasma);
        GLog.w(Messages.get(PestilenceArenaController.class, "prelude"));
        return true;
    }

    boolean prepare(Arena arena, int bossCell, long seed) {
        if (prepared) return purifierCell >= 0;

        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < arena.length(); cell++) {
            if (isInitialCandidate(arena, cell, bossCell)) candidates.add(cell);
        }
        Collections.shuffle(candidates, new Random(seed));
        candidates.sort(Comparator.comparingInt(cell -> distance(arena, arena.heroAnchor(), cell)));

        int chosen = -1;
        int[] route = new int[0];
        for (int candidate : candidates) {
            int[] candidateRoute = shortestRoute(arena, arena.heroAnchor(), candidate);
            if (candidateRoute.length == 0) continue;
            chosen = candidate;
            route = candidateRoute;
            break;
        }
        if (chosen < 0) return false;

        purifierCell = chosen;
        safeRoute = route;
        this.bossCell = bossCell;
        purifierCooldown = 0;
        relocationRngState = TowerBossGenerator.mix64(seed ^ RELOCATION_SALT);
        arena.installPurifier(chosen);
        prepared = true;
        return true;
    }

    private static boolean isInitialCandidate(Arena arena, int cell, int bossCell) {
        int x = cell % arena.width();
        int y = cell / arena.width();
        return arena.isArenaCell(cell)
                && x < arena.width() / 2
                && y > TowerBossLayout.HEIGHT / 2
                && arena.terrain(cell) == Terrain.EMPTY
                && arena.passable(cell)
                && !arena.forbidden(cell)
                && distance(arena, cell, bossCell) >= MIN_BOSS_DISTANCE
                && distance(arena, cell, arena.heroAnchor()) >= MIN_ANCHOR_DISTANCE
                && distance(arena, cell, arena.exitAnchor()) >= MIN_ANCHOR_DISTANCE;
    }

    /** Activates and relocates the purifier. World effects are applied by the live-level wrapper. */
    boolean activate(Arena arena, int heroCell) {
        if (!prepared || heroCell != purifierCell || purifierCooldown > 0) return false;

        int oldCell = purifierCell;
        int nextCell = selectRelocation(arena, heroCell);
        purifierCooldown = PURIFIER_COOLDOWN;
        if (nextCell >= 0 && nextCell != oldCell) {
            arena.relocatePurifier(oldCell, nextCell);
            purifierCell = nextCell;
        }
        arena.setPurifierReady(purifierCell, false);
        return true;
    }

    enum ActivationResult {
        NONE, START_BOSS, DAMAGE_BOSS
    }

    ActivationResult activateForEncounter(Arena arena, int heroCell, boolean bossStarted) {
        if (!activate(arena, heroCell)) return ActivationResult.NONE;
        return preludeStarted && !bossStarted
                ? ActivationResult.START_BOSS : ActivationResult.DAMAGE_BOSS;
    }

    ActivationResult onHeroEntered(TowerBossLevel level, Hero hero, boolean bossStarted) {
        if (hero == null) return ActivationResult.NONE;
        if (prepared && hero.pos == purifierCell && purifierCooldown > 0) {
            GLog.w(Messages.get(PestilenceArenaController.class,
                    "recharging", purifierCooldown));
            return ActivationResult.NONE;
        }
        ActivationResult result = activateForEncounter(new LevelArena(level), hero.pos, bossStarted);
        if (result == ActivationResult.NONE) return result;

        level.beginPurifierMiasmaClear();
        try {
            clearAllMiasma(level);
        } finally {
            level.endPurifierMiasmaClear();
        }
        Infection.clear(hero);
        Dungeon.observe();
        if (result == ActivationResult.START_BOSS) {
            GLog.p(Messages.get(PestilenceArenaController.class, "purifier_start"));
        } else {
            GLog.p(Messages.get(PestilenceArenaController.class,
                    "purifier_strike", PURIFIER_BOSS_DAMAGE));
        }
        return result;
    }

    private static void clearAllMiasma(TowerBossLevel level) {
        clearBlob(level, IncubatingMiasma.class);
        clearBlob(level, OutbreakMiasma.class);
        clearBlob(level, PaleMiasma.class);
    }

    private static void clearBlob(TowerBossLevel level, Class<? extends Blob> type) {
        Blob blob = level.blobs.get(type);
        if (blob != null) blob.fullyClear();
    }

    int[] preludeMiasmaCells(Arena arena) {
        if (!prepared) return new int[0];
        boolean[] safe = new boolean[arena.length()];
        for (int cell : safeRoute) {
            if (cell >= 0 && cell < safe.length) safe[cell] = true;
        }
        ArrayList<Integer> cells = new ArrayList<>();
        for (int cell = 0; cell < arena.length(); cell++) {
            if (arena.isArenaCell(cell) && arena.passable(cell)
                    && cell != purifierCell && !safe[cell]) {
                cells.add(cell);
            }
        }
        int[] result = new int[cells.size()];
        for (int i = 0; i < result.length; i++) result[i] = cells.get(i);
        return result;
    }

    private int selectRelocation(Arena arena, int heroCell) {
        ArrayList<Integer> reachable = new ArrayList<>();
        ArrayList<Integer> distant = new ArrayList<>();
        int farthest = -1;
        for (int cell = 0; cell < arena.length(); cell++) {
            if (!isRelocationCandidate(arena, cell) || shortestRoute(arena, heroCell, cell).length == 0) {
                continue;
            }
            reachable.add(cell);
            int cellDistance = distance(arena, heroCell, cell);
            if (cellDistance >= MIN_RELOCATION_DISTANCE) distant.add(cell);
            farthest = Math.max(farthest, cellDistance);
        }

        if (!distant.isEmpty()) return distant.get(nextInt(distant.size()));
        if (reachable.isEmpty()) return -1;

        ArrayList<Integer> farthestCells = new ArrayList<>();
        for (int cell : reachable) {
            if (distance(arena, heroCell, cell) == farthest) farthestCells.add(cell);
        }
        return farthestCells.get(nextInt(farthestCells.size()));
    }

    private boolean isRelocationCandidate(Arena arena, int cell) {
        return cell != purifierCell
                && arena.isArenaCell(cell)
                && arena.terrain(cell) == Terrain.EMPTY
                && arena.passable(cell)
                && !arena.forbidden(cell)
                && (bossCell < 0 || distance(arena, cell, bossCell) >= MIN_BOSS_DISTANCE);
    }

    private int nextInt(int bound) {
        relocationRngState += 0x9E3779B97F4A7C15L;
        long z = relocationRngState;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (int) Long.remainderUnsigned(z, bound);
    }

    private static int[] shortestRoute(Arena arena, int start, int target) {
        if (start < 0 || target < 0 || start >= arena.length() || target >= arena.length()
                || !arena.passable(target)) return new int[0];
        boolean[] seen = new boolean[arena.length()];
        int[] previous = new int[arena.length()];
        int[] queue = new int[arena.length()];
        java.util.Arrays.fill(previous, -1);
        int head = 0;
        int tail = 0;
        seen[start] = true;
        queue[tail++] = start;
        while (head < tail) {
            int cell = queue[head++];
            if (cell == target) break;
            int x = cell % arena.width();
            int[] neighbours = {cell - arena.width(), cell + 1, cell + arena.width(), cell - 1};
            for (int next : neighbours) {
                if (next < 0 || next >= arena.length() || seen[next] || !arena.passable(next)) continue;
                if (Math.abs(next % arena.width() - x) > 1) continue;
                seen[next] = true;
                previous[next] = cell;
                queue[tail++] = next;
            }
        }
        if (!seen[target]) return new int[0];

        int length = 1;
        for (int cell = target; cell != start; cell = previous[cell]) length++;
        int[] route = new int[length];
        int cell = target;
        for (int i = length - 1; i >= 0; i--) {
            route[i] = cell;
            if (cell != start) cell = previous[cell];
        }
        return route;
    }

    private static int distance(Arena arena, int a, int b) {
        int ax = a % arena.width();
        int ay = a / arena.width();
        int bx = b % arena.width();
        int by = b / arena.width();
        return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
    }

    public boolean prepared() {
        return prepared;
    }

    public boolean preludeStarted() {
        return preludeStarted;
    }

    public int purifierCell() {
        return purifierCell;
    }

    public int[] brazierCells() {
        return purifierCell < 0 ? new int[0] : new int[]{purifierCell};
    }

    public int[] safeRoute() {
        return safeRoute.clone();
    }

    public int bossCell() {
        return bossCell;
    }

    public int cooldownAt(int index) {
        return index == 0 ? purifierCooldown : 0;
    }

    public void putOnCooldown(int index, int turns) {
        if (index != 0) throw new IndexOutOfBoundsException();
        purifierCooldown = Math.max(0, Math.min(PURIFIER_COOLDOWN, turns));
    }

    public void prepareHeroAt(int cell) {
        // Retained for old integrations; purifier activation is now immediate on entry.
    }

    public int preparedHeroCell() {
        return -1;
    }

    public void onHeroTurnStarted(Hero hero) {
        if (Dungeon.level instanceof TowerBossLevel) {
            advanceHeroTurn(new LevelArena((TowerBossLevel) Dungeon.level));
        }
        if (hero == null || Dungeon.level == null) return;
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
        // Waiting is no longer part of purifier activation.
    }

    public void onHeroConsumableUsed(Hero hero, Item item) {
        if (hero == null || item == null || Dungeon.level == null) return;
        if ((item instanceof Potion || item instanceof Food || item instanceof Scroll)
                && Blob.volumeAt(hero.pos, OutbreakMiasma.class) > 0) {
            Infection.addStacks(hero, 1);
        }
    }

    public int nearestReadyBrazier(int origin) {
        return prepared && purifierCooldown == 0 ? purifierCell : -1;
    }

    public boolean isBrazierCell(int cell) {
        return prepared && purifierCell == cell;
    }

    public int randomArenaCell() {
        if (!(Dungeon.level instanceof TowerBossLevel)) return -1;
        Arena arena = new LevelArena((TowerBossLevel) Dungeon.level);
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < arena.length(); cell++) {
            if (arena.isArenaCell(cell) && arena.passable(cell)
                    && cell != purifierCell && !arena.forbidden(cell)) {
                candidates.add(cell);
            }
        }
        return candidates.isEmpty() ? -1
                : candidates.get(com.watabou.utils.Random.Int(candidates.size()));
    }

    static boolean shouldStartFromExternalPaleClear(boolean preludeStarted,
            boolean bossStarted, boolean bossDefeated, boolean purifierClearing,
            int removedVolume) {
        return preludeStarted && !bossStarted && !bossDefeated
                && !purifierClearing && removedVolume > 0;
    }

    public void advanceBossTurn() {
        // Retained for compatibility: purifier recharge is driven only by hero turns.
    }

    void advanceHeroTurn(Arena arena) {
        if (!prepared || purifierCooldown <= 0) return;
        purifierCooldown--;
        if (purifierCooldown == 0) arena.setPurifierReady(purifierCell, true);
    }

    void syncPurifierVisual(TowerBossLevel level) {
        if (prepared && purifierCell >= 0) {
            new LevelArena(level).setPurifierReady(purifierCell, purifierCooldown == 0);
        }
    }

    public void finishEncounter() {
        waterCell = -1;
        waterTurns = 0;
        purifierCooldown = 0;
    }

    private static Class<? extends Blob> activeMiasmaAt(int cell) {
        if (Blob.volumeAt(cell, PaleMiasma.class) > 0) return PaleMiasma.class;
        if (Blob.volumeAt(cell, OutbreakMiasma.class) > 0) return OutbreakMiasma.class;
        if (Blob.volumeAt(cell, IncubatingMiasma.class) > 0) return IncubatingMiasma.class;
        return null;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(PREPARED, prepared);
        bundle.put(PRELUDE_STARTED, preludeStarted);
        bundle.put(PURIFIER_CELL, purifierCell);
        bundle.put(PURIFIER_COOLDOWN_KEY, purifierCooldown);
        bundle.put(SAFE_ROUTE, safeRoute);
        bundle.put(BOSS_CELL, bossCell);
        bundle.put(RELOCATION_RNG_STATE, relocationRngState);
        bundle.put(WATER_CELL, waterCell);
        bundle.put(WATER_TURNS, waterTurns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        int restoredCell = bundle.contains(PURIFIER_CELL) ? bundle.getInt(PURIFIER_CELL) : -1;
        int restoredCooldown = bundle.contains(PURIFIER_COOLDOWN_KEY)
                ? bundle.getInt(PURIFIER_COOLDOWN_KEY) : 0;
        if (restoredCell < 0) {
            int[] legacyCells = bundle.getIntArray(LEGACY_BRAZIER_CELLS);
            int[] legacyCooldowns = bundle.getIntArray(LEGACY_COOLDOWNS);
            if (legacyCells.length > 0) restoredCell = legacyCells[0];
            if (legacyCooldowns.length > 0) restoredCooldown = legacyCooldowns[0];
        }

        if (bundle.getBoolean(PREPARED) && restoredCell >= 0) {
            prepared = true;
            preludeStarted = bundle.contains(PRELUDE_STARTED)
                    && bundle.getBoolean(PRELUDE_STARTED);
            purifierCell = restoredCell;
            purifierCooldown = Math.max(0, Math.min(PURIFIER_COOLDOWN, restoredCooldown));
            safeRoute = bundle.contains(SAFE_ROUTE) ? bundle.getIntArray(SAFE_ROUTE) : new int[0];
            bossCell = bundle.contains(BOSS_CELL) ? bundle.getInt(BOSS_CELL) : -1;
            relocationRngState = bundle.contains(RELOCATION_RNG_STATE)
                    ? bundle.getLong(RELOCATION_RNG_STATE)
                    : TowerBossGenerator.mix64(((long) restoredCell << 32) ^ RELOCATION_SALT);
            waterCell = bundle.contains(WATER_CELL) ? bundle.getInt(WATER_CELL) : -1;
            waterTurns = bundle.contains(WATER_TURNS) ? Math.max(0, bundle.getInt(WATER_TURNS)) : 0;
        } else {
            prepared = false;
            preludeStarted = false;
            purifierCell = -1;
            purifierCooldown = 0;
            safeRoute = new int[0];
            bossCell = -1;
            relocationRngState = 0L;
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
        public void installPurifier(int cell) {
            PlagueBrazier purifier = new PlagueBrazier();
            purifier.set(cell);
            level.traps.put(cell, purifier);
            level.set(cell, Terrain.TRAP, level);
            GameScene.updateMap(cell);
        }

        @Override
        public void relocatePurifier(int from, int to) {
            Trap purifier = level.traps.remove(from);
            level.set(from, Terrain.EMPTY, level);
            if (!(purifier instanceof PlagueBrazier)) purifier = new PlagueBrazier();
            purifier.set(to);
            level.traps.put(to, purifier);
            level.set(to, Terrain.TRAP, level);
            GameScene.updateMap(from);
            GameScene.updateMap(to);
        }

        @Override
        public void setPurifierReady(int cell, boolean ready) {
            Trap trap = level.traps.get(cell);
            if (trap instanceof PlagueBrazier) {
                trap.active = ready;
                GameScene.updateMap(cell);
            }
        }
    }
}
