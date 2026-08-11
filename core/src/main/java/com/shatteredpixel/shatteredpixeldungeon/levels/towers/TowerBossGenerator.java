package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/** Stable-id registry and deterministic selector for tower bosses. */
public final class TowerBossGenerator {

    public static final String PESTILENCE_KNIGHT_ID = "pestilence_knight";
    private static final long SELECTION_SALT = 0x50455354494C454EL;

    private static final List<Entry> ENTRIES;
    private static final Map<String, Entry> BY_ID;

    static {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(PESTILENCE_KNIGHT_ID, 1, 1, Integer.MAX_VALUE,
                PestilenceKnight::new));
        ENTRIES = Collections.unmodifiableList(entries);

        LinkedHashMap<String, Entry> byId = new LinkedHashMap<>();
        for (Entry entry : ENTRIES) {
            if (byId.put(entry.id, entry) != null) {
                throw new IllegalStateException("Duplicate tower boss id: " + entry.id);
            }
        }
        BY_ID = Collections.unmodifiableMap(byId);
    }

    private TowerBossGenerator() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static String selectId(long dungeonSeed, int depth, int branch) {
        int bossIndex = Math.max(1, depth / TowerBossLevel.FLOORS_PER_BOSS);
        ArrayList<Entry> eligible = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            if (entry.eligibleAt(bossIndex)) eligible.add(entry);
        }
        long seed = mix64(dungeonSeed ^ ((long) depth << 32) ^ branch ^ SELECTION_SALT);
        return selectId(eligible, seed);
    }

    static String selectId(List<Entry> eligible, long seed) {
        if (eligible == null || eligible.isEmpty()) {
            throw new IllegalStateException("No eligible tower bosses are registered");
        }
        long totalWeight = 0;
        for (Entry entry : eligible) totalWeight += entry.weight;
        if (totalWeight <= 0 || totalWeight > Integer.MAX_VALUE) {
            throw new IllegalStateException("Eligible tower boss weights are invalid");
        }

        int roll = new Random(seed).nextInt((int) totalWeight);
        for (Entry entry : eligible) {
            roll -= entry.weight;
            if (roll < 0) return entry.id;
        }
        throw new IllegalStateException("Tower boss selection did not resolve");
    }

    public static TowerBoss create(String id) {
        Entry entry = BY_ID.get(id);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown tower boss id: " + id);
        }
        return entry.factory.get();
    }

    static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    public static final class Entry {
        private final String id;
        private final int weight;
        private final int minBossIndex;
        private final int maxBossIndex;
        private final Supplier<? extends TowerBoss> factory;

        Entry(String id, int weight, int minBossIndex, int maxBossIndex,
                Supplier<? extends TowerBoss> factory) {
            if (id == null || id.isEmpty()) throw new IllegalArgumentException("Boss id is required");
            if (weight <= 0) throw new IllegalArgumentException("Boss weight must be positive");
            if (minBossIndex < 1 || maxBossIndex < minBossIndex) {
                throw new IllegalArgumentException("Boss index range is invalid");
            }
            if (factory == null) throw new IllegalArgumentException("Boss factory is required");
            this.id = id;
            this.weight = weight;
            this.minBossIndex = minBossIndex;
            this.maxBossIndex = maxBossIndex;
            this.factory = factory;
        }

        boolean eligibleAt(int bossIndex) {
            return bossIndex >= minBossIndex && bossIndex <= maxBossIndex;
        }

        public String id() {
            return id;
        }

        public int weight() {
            return weight;
        }

        public int minBossIndex() {
            return minBossIndex;
        }

        public int maxBossIndex() {
            return maxBossIndex;
        }
    }
}
