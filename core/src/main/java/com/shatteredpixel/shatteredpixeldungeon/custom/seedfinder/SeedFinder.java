package com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder;

import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.getNegativeTalent;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SeedFinder {
    enum Condition {ANY, ALL}
    enum FINDING {STOP,CONTINUE}
    public enum Algorithm {SEQUENTIAL, RANDOM, GENETIC, PARTICLE_SWARM, SIMULATED_ANNEALING, HILL_CLIMB}

    public static FINDING findingStatus = FINDING.STOP;
    public static final Object DUNGEON_LOCK = new Object();

    public static class Options {
        public static int floors;
        public static Condition condition;
        public static long seed;
    }

    public interface ProgressListener {
        void onProgress(SearchState state);
    }

    public static class SearchState {
        public long searched;
        public long qualified;
        public long elapsedMillis;
        public int bestScore;
        public String currentSeed;
        public String bestSeed;
        public String bestCode;
        public String algorithm;

        public String summary() {
            StringBuilder builder = new StringBuilder();
            builder.append(Messages.get(SeedFinder.class, "searching")).append("\n");
            builder.append(Messages.get(SeedFinder.class, "current_seed")).append(currentSeed == null ? "-" : currentSeed).append("\n");
            builder.append(Messages.get(SeedFinder.class, "algorithm")).append(algorithm == null ? "-" : Messages.get(SeedFinder.class, "algorithm_" + algorithm.toLowerCase())).append("\n");
            builder.append(Messages.get(SeedFinder.class, "searched")).append(searched).append("\n");
            builder.append(Messages.get(SeedFinder.class, "elapsed")).append(elapsedMillis / 1000f).append(Messages.get(SeedFinder.class, "seconds")).append("\n");
            builder.append(Messages.get(SeedFinder.class, "qualified")).append(qualified).append("\n");
            builder.append(Messages.get(SeedFinder.class, "best_score")).append(bestScore).append("%\n");
            if (bestSeed != null) {
                builder.append(Messages.get(SeedFinder.class, "candidate")).append(bestCode).append(" (").append(bestSeed).append(")");
            } else {
                builder.append(Messages.get(SeedFinder.class, "candidate")).append("-");
            }
            return builder.toString();
        }
    }

    private static class MatchResult {
        final String seed;
        final int matched;
        final int total;
        final int score;

        MatchResult(String seed, int matched, int total) {
            this.seed = seed;
            this.matched = matched;
            this.total = total;
            this.score = total <= 0 ? 100 : Math.min(100, Math.round(100f * matched / total));
        }
    }

    private static class SearchMemory {
        final Algorithm algorithm;
        final PriorityQueue<MatchResult> elites = new PriorityQueue<>(Comparator.comparingInt(a -> a.score));
        final Object elitesLock = new Object();
        final AtomicLong sequential = new AtomicLong(Random.Long(DungeonSeed.TOTAL_SEEDS));
        final long[] currentSeed;
        final int[] currentScore;
        final long[] localBestSeed;
        final int[] localBestScore;
        final long[] velocity;
        final int[] failedSteps;
        final int[] hillRadius;
        final float[] temperature;

        SearchMemory(Algorithm algorithm, int threads) {
            this.algorithm = algorithm;
            currentSeed = new long[threads];
            currentScore = new int[threads];
            localBestSeed = new long[threads];
            localBestScore = new int[threads];
            velocity = new long[threads];
            failedSteps = new int[threads];
            hillRadius = new int[threads];
            temperature = new float[threads];
            for (int i = 0; i < threads; i++) {
                currentSeed[i] = Random.Long(DungeonSeed.TOTAL_SEEDS);
                localBestSeed[i] = currentSeed[i];
                localBestScore[i] = -1;
                velocity[i] = Random.IntRange(-65536, 65536);
                hillRadius[i] = 1 << 18;
                temperature[i] = 40f;
            }
        }
    }

    static class HeapItem {
        public Item item;
        public Heap heap;

        public HeapItem(Item item, Heap heap) {
            this.item = item;
            this.heap = heap;
        }
    }

    List<Class<? extends Item>> blacklist;
    ArrayList<String> itemList;
    ArrayList<String> itemLists;
    ArrayList<String> talentLists;

    private void addTextItems(String caption, ArrayList<HeapItem> items, StringBuilder builder) {
        if (!items.isEmpty()) {
            builder.append(caption).append(":\n");

            for (HeapItem item : items) {
                Item i = item.item;
                Heap h = item.heap;

                if (((i instanceof Armor && ((Armor) i).hasGoodGlyph()) ||
                        (i instanceof Weapon && ((Weapon) i).hasGoodEnchant()) ||
                        (i instanceof Ring) || (i instanceof Wand)) && i.cursed)
                    builder.append("- " + Messages.get(this, "cursed")).append(i.title().toLowerCase());

                else
                    builder.append("- ").append(i.title().toLowerCase());

                if (h.type != Type.HEAP)
                    builder.append(" (").append(h.title().toLowerCase()).append(")");

                builder.append("\n");
            }

            builder.append("\n");
        }
    }
    private void addTextTalent( ArrayList<Talent> talents, StringBuilder builder) {
        if (!talents.isEmpty()) {

            for (Talent talent : talents) {

                builder.append(talent.title());

                builder.append("\n");
            }

            builder.append("\n");
        }
    }

    private void addTextQuest(String caption, ArrayList<Item> items, StringBuilder builder) {
        if (!items.isEmpty()) {
            builder.append(caption).append(":\n");

            for (Item i : items) {
                if (i.cursed)
                    builder.append("- " + Messages.get(this, "cursed")).append(i.title().toLowerCase()).append("\n");

                else
                    builder.append("- ").append(i.title().toLowerCase()).append("\n");
            }

            builder.append("\n");
        }
    }



    public void findSeed(boolean stop){
        if(!stop){
            findingStatus = FINDING.STOP;
        }
    }

    public String findSeed(String[] wanted, int floor) {
        return findSeed(wanted, floor, null);
    }

    public String findSeed(String[] wanted, int floor, ProgressListener listener) {
        itemLists = new ArrayList<>(Arrays.asList(wanted));
        itemList = new ArrayList<>();
        talentLists = new ArrayList<>();
        int cnt=0;
        for(String i:itemLists){
            if(cnt==0){
                if(i.contains("Negative Talent")){
                    cnt=1;
                    //continue;
                }else{itemList.add(i);}
            }else{
                talentLists.add(i);
            }
        }
        findingStatus = FINDING.CONTINUE;
        Options.condition = Condition.ALL;
        MatchResult result = searchSeeds(floor, listener);
        if (result == null) {
            return "NONE";
        }
        return logSeedItems(result.seed, floor, result.score);
    }

    private MatchResult searchSeeds(int floor, ProgressListener listener) {
        final long start = System.currentTimeMillis();
        final long timeLimit = Math.max(1, SPDSettings.seedFinderTimeLimit()) * 1000L;
        final int threshold = SPDSettings.seedFinderThreshold();
        final int threads = SPDSettings.seedFinderThreads() ? Math.max(1, Runtime.getRuntime().availableProcessors()) : 1;
        final Algorithm algorithm = Algorithm.values()[SPDSettings.seedFinderAlgorithm()];
        final AtomicBoolean done = new AtomicBoolean(false);
        final AtomicLong searched = new AtomicLong();
        final AtomicLong qualified = new AtomicLong();
        final AtomicLong lastUpdate = new AtomicLong();
        final AtomicReference<MatchResult> best = new AtomicReference<>(new MatchResult(null, 0, Math.max(1, itemList.size() + talentLists.size())));
        final AtomicReference<MatchResult> exact = new AtomicReference<>();
        final Set<Long> visited = Collections.synchronizedSet(new HashSet<Long>());
        final SearchMemory memory = new SearchMemory(algorithm, threads);

        ArrayList<Thread> workers = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            final int worker = t;
            Thread thread = new Thread(() -> {
                while (!done.get() && findingStatus == FINDING.CONTINUE && System.currentTimeMillis() - start < timeLimit) {
                    if (Thread.currentThread().isInterrupted()) {
                        done.set(true);
                        break;
                    }
                    long seed = nextSeed(memory, worker, visited, best);
                    MatchResult result = evaluateSeed(Long.toString(seed), floor);
                    long searchedNow = searched.incrementAndGet();
                    if (result.score >= threshold) {
                        qualified.incrementAndGet();
                    }
                    updateBest(best, result);
                    updateSearchMemory(memory, worker, seed, result, best.get());
                    if (result.score >= 100) {
                        exact.set(result);
                        done.set(true);
                    }
                    long now = System.currentTimeMillis();
                    if (listener != null && now - lastUpdate.get() > 250 && lastUpdate.compareAndSet(lastUpdate.get(), now)) {
                        listener.onProgress(buildState(searchedNow, qualified.get(), now - start, seed, best.get(), algorithm));
                    }
                }
            }, "seed-finder-" + worker);
            workers.add(thread);
            thread.start();
        }

        for (Thread thread : workers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                done.set(true);
                Thread.currentThread().interrupt();
                break;
            }
        }

        MatchResult result = exact.get();
        if (result == null) {
            result = best.get();
        }
        if (listener != null) {
            listener.onProgress(buildState(searched.get(), qualified.get(), System.currentTimeMillis() - start,
                    result == null || result.seed == null ? -1 : Long.parseLong(result.seed), result, algorithm));
        }
        return result == null || result.seed == null ? null : result;
    }

    private long nextSeed(SearchMemory memory, int worker, Set<Long> visited, AtomicReference<MatchResult> best) {
        long seed;
        int attempts = 0;
        while (true) {
            switch (memory.algorithm) {
                case SEQUENTIAL:
                    seed = Math.floorMod(memory.sequential.getAndIncrement(), DungeonSeed.TOTAL_SEEDS);
                    break;
                case GENETIC:
                    seed = geneticSeed(memory);
                    break;
                case PARTICLE_SWARM:
                    seed = particleSeed(memory, worker, best.get());
                    break;
                case SIMULATED_ANNEALING:
                    seed = annealSeed(memory, worker);
                    break;
                case HILL_CLIMB:
                    seed = climbSeed(memory, worker);
                    break;
                case RANDOM:
                default:
                    seed = Random.Long(DungeonSeed.TOTAL_SEEDS);
                    break;
            }
            if (visited.add(seed)) {
                return seed;
            }
            if (++attempts > 32) {
                do {
                    seed = Random.Long(DungeonSeed.TOTAL_SEEDS);
                } while (!visited.add(seed));
                return seed;
            }
        }
    }

    private long geneticSeed(SearchMemory memory) {
        ArrayList<MatchResult> parents = elites(memory);
        if (parents.size() < 2) {
            return Random.Long(DungeonSeed.TOTAL_SEEDS);
        }
        MatchResult parentA = tournament(parents);
        MatchResult parentB = tournament(parents);
        long a = Long.parseLong(parentA.seed);
        long b = Long.parseLong(parentB.seed);
        int split = Random.IntRange(8, 41);
        long lowMask = (1L << split) - 1L;
        long child = (a & lowMask) | (b & ~lowMask);

        int mutations = 1 + Random.Int(3);
        for (int i = 0; i < mutations; i++) {
            child ^= 1L << Random.Int(43);
        }
        if (Random.Int(3) == 0) {
            child += Random.IntRange(-65536, 65536);
        }
        return Math.floorMod(child, DungeonSeed.TOTAL_SEEDS);
    }

    private ArrayList<MatchResult> elites(SearchMemory memory) {
        synchronized (memory.elitesLock) {
            return new ArrayList<>(memory.elites);
        }
    }

    private MatchResult tournament(ArrayList<MatchResult> parents) {
        MatchResult best = Random.element(parents);
        for (int i = 0; i < 2; i++) {
            MatchResult candidate = Random.element(parents);
            if (candidate.score > best.score) {
                best = candidate;
            }
        }
        return best;
    }

    private long particleSeed(SearchMemory memory, int worker, MatchResult globalBest) {
        long position = memory.currentSeed[worker];
        long personalBest = memory.localBestSeed[worker];
        long socialBest = globalBest != null && globalBest.seed != null ? Long.parseLong(globalBest.seed) : personalBest;

        long cognitive = clampStep(personalBest - position) / 2L;
        long social = clampStep(socialBest - position) / 2L;
        long inertia = memory.velocity[worker] * 3L / 5L;
        memory.velocity[worker] = clampStep(inertia + cognitive + social + Random.IntRange(-32768, 32768));

        long seed = position + memory.velocity[worker];
        if (Random.Int(5) == 0) {
            seed ^= 1L << Random.Int(43);
        }
        return Math.floorMod(seed, DungeonSeed.TOTAL_SEEDS);
    }

    private long annealSeed(SearchMemory memory, int worker) {
        long seed = memory.currentSeed[worker];
        int radius = Math.max(128, Math.round(memory.temperature[worker] * 4096f));
        seed += Random.IntRange(-radius, radius);
        if (Random.Int(3) == 0) {
            seed ^= 1L << Random.Int(43);
        }
        return Math.floorMod(seed, DungeonSeed.TOTAL_SEEDS);
    }

    private long climbSeed(SearchMemory memory, int worker) {
        if (memory.failedSteps[worker] > 8) {
            memory.failedSteps[worker] = 0;
            memory.hillRadius[worker] = 1 << 18;
            return Random.Long(DungeonSeed.TOTAL_SEEDS);
        }
        long seed = memory.localBestSeed[worker];
        int radius = Math.max(64, memory.hillRadius[worker]);
        seed += Random.IntRange(-radius, radius);
        seed ^= 1L << Random.Int(Math.min(43, Math.max(6, 32 - memory.failedSteps[worker])));
        return Math.floorMod(seed, DungeonSeed.TOTAL_SEEDS);
    }

    private long clampStep(long value) {
        long max = 1L << 24;
        if (value > max) {
            return max;
        } else if (value < -max) {
            return -max;
        }
        return value;
    }

    private void updateSearchMemory(SearchMemory memory, int worker, long seed, MatchResult result, MatchResult globalBest) {
        if (result.score > memory.localBestScore[worker]) {
            memory.localBestScore[worker] = result.score;
            memory.localBestSeed[worker] = seed;
            memory.failedSteps[worker] = 0;
            memory.hillRadius[worker] = Math.max(64, memory.hillRadius[worker] / 2);
        } else {
            memory.failedSteps[worker]++;
            if (memory.algorithm == Algorithm.HILL_CLIMB && memory.failedSteps[worker] % 3 == 0) {
                memory.hillRadius[worker] = Math.max(64, memory.hillRadius[worker] / 2);
            }
        }

        if (memory.algorithm == Algorithm.SIMULATED_ANNEALING) {
            int delta = result.score - memory.currentScore[worker];
            boolean accept = delta >= 0 || Random.Float() < Math.exp(delta / Math.max(1f, memory.temperature[worker]));
            if (accept) {
                memory.currentSeed[worker] = seed;
                memory.currentScore[worker] = result.score;
            }
            memory.temperature[worker] = Math.max(1f, memory.temperature[worker] * 0.985f);
            if (memory.failedSteps[worker] > 12) {
                memory.currentSeed[worker] = globalBest != null && globalBest.seed != null ? Long.parseLong(globalBest.seed) : Random.Long(DungeonSeed.TOTAL_SEEDS);
                memory.currentScore[worker] = globalBest != null ? globalBest.score : 0;
                memory.temperature[worker] = 25f;
                memory.failedSteps[worker] = 0;
            }
        } else {
            memory.currentSeed[worker] = seed;
            memory.currentScore[worker] = result.score;
        }

        rememberElite(memory, result);
    }

    private void rememberElite(SearchMemory memory, MatchResult result) {
        synchronized (memory.elitesLock) {
            if (memory.elites.size() < 32) {
                memory.elites.add(result);
            } else if (memory.elites.peek().score < result.score) {
                memory.elites.poll();
                memory.elites.add(result);
            }
        }
    }

    private void updateBest(AtomicReference<MatchResult> best, MatchResult result) {
        while (true) {
            MatchResult current = best.get();
            if (current != null && current.score >= result.score) {
                return;
            }
            if (best.compareAndSet(current, result)) {
                return;
            }
        }
    }

    private SearchState buildState(long searched, long qualified, long elapsed, long currentSeed, MatchResult best, Algorithm algorithm) {
        SearchState state = new SearchState();
        state.searched = searched;
        state.qualified = qualified;
        state.elapsedMillis = elapsed;
        state.algorithm = algorithm.name();
        state.currentSeed = currentSeed >= 0 ? Long.toString(currentSeed) : null;
        if (best != null && best.seed != null) {
            state.bestScore = best.score;
            state.bestSeed = best.seed;
            state.bestCode = DungeonSeed.convertToCode(Long.parseLong(best.seed));
        }
        return state;
    }

    private ArrayList<Heap> getMobDrops(Level l) {
        ArrayList<Heap> heaps = new ArrayList<>();

        for (Mob m : l.mobs) {
            if (m instanceof Statue && !(m instanceof ArmoredStatue)) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((Statue) m).weapon().identify());
                h.type = Type.HEAP;
                heaps.add(h);
            }

            else if (m instanceof ArmoredStatue) {
                Heap h = new Heap();
                h.items = new LinkedList<>();
                h.items.add(((ArmoredStatue) m).armor().identify());
                h.items.add(((ArmoredStatue) m).weapon().identify());
                h.type = Type.HEAP;
                heaps.add(h);
            }

            else if (m instanceof Mimic) {
                Heap h = new Heap();
                h.items = new LinkedList<>();

                for (Item item : ((Mimic) m).items)
                    h.items.add(item.identify());

                if (m instanceof GoldenMimic) h.type = Type.HEAP;
                else if (m instanceof CrystalMimic) h.type = Type.HEAP;
                else h.type = Type.HEAP;
                heaps.add(h);
            }
        }

        return heaps;
    }

    private boolean testSeed(String seed, int floors) {
        SPDSettings.customSeed(seed);
        GamesInProgress.selectedClass = HeroClass.WARRIOR;
        Dungeon.init();

        boolean[] itemsFound = new boolean[itemList.size()];

        for (int i = 0; i < floors; i++) {
            Level l = Dungeon.newLevel();

            ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
            heaps.addAll(getMobDrops(l));

            if(Ghost.Quest.armor != null){
                for (int j = 0; j < itemList.size(); j++) {
                    if (Ghost.Quest.armor.identify().title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ",""))) {
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }
            if(Wandmaker.Quest.wand1 != null){
                for (int j = 0; j < itemList.size(); j++) {
                    if (Wandmaker.Quest.wand1.identify().title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ","")) || Wandmaker.Quest.wand2.identify().title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ",""))) {
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                    if(Wandmaker.Quest.type() == 1 && Messages.get(this, "corpsedust").contains(itemList.get(j).replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }else if(Wandmaker.Quest.type() == 2 && Messages.get(this, "embers").contains(itemList.get(j).replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }else if(Wandmaker.Quest.type() == 3 && Messages.get(this, "rotberry").contains(itemList.get(j).replaceAll(" ",""))){
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }
            if(Imp.Quest.reward != null){
                for (int j = 0; j < itemList.size(); j++) {
                    if (Imp.Quest.reward.identify().title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ",""))) {
                        if (itemsFound[j] == false) {
                            itemsFound[j] = true;
                            break;
                        }
                    }
                }
            }

            for (Heap h : heaps) {
                for (Item item : h.items) {
                    item.identify();

                    for (int j = 0; j < itemList.size(); j++) {
                        if (item.title().toLowerCase().replaceAll(" ","").contains(itemList.get(j).replaceAll(" ",""))) {
                            if (itemsFound[j] == false) {
                                itemsFound[j] = true;
                                break;
                            }
                        }
                    }
                }
            }

            Dungeon.depth++;
        }

        if (Options.condition == Condition.ANY) {
            for (int i = 0; i < itemList.size(); i++) {
                if (itemsFound[i] == true)
                    return true;
            }

            return false;
        }

        else {
            for (int i = 0; i < itemList.size(); i++) {
                if (itemsFound[i] == false)
                    return false;
            }

            return true;
        }
    }

    private boolean testSeedALL(String seed, int floors) {
        return evaluateSeed(seed, floors).score >= 100;
    }

    private MatchResult evaluateSeed(String seed, int floors) {
        synchronized (DUNGEON_LOCK) {
            return evaluateSeedLocked(seed, floors);
        }
    }

    private MatchResult evaluateSeedLocked(String seed, int floors) {
        String oldSeed = SPDSettings.customSeed();
        int a = SPDSettings.challenges();
        int matched = 0;
        int total = itemList.size() + talentLists.size();
        try {
            SPDSettings.customSeed(seed);
            SPDSettings.challenges( Challenges.TEST_MODE);
            Dungeon.hero = null;
            Dungeon.daily = Dungeon.dailyReplay = false;
            Dungeon.initSeed();

            boolean[] talentFound = new boolean[talentLists.size()];
            if(!talentLists.isEmpty()){
                ArrayList<Talent> getNegativeTalent=getNegativeTalent();
                ArrayList<String> getNegativeTalents=new ArrayList<String>();
                for(int cnt=0;cnt<8;cnt++){
                    getNegativeTalents.add(getNegativeTalent.get(cnt).title().toLowerCase());
                }
                String passTalent="**";
                if(talentLists.size()==4){
                    for(int cnt=0;cnt<4;cnt++){
                        String wantingTalent = talentLists.get(cnt).toLowerCase();
                        if(wantingTalent.contains(passTalent) || wantingTalent.contains(getNegativeTalents.get(cnt*2+1)) || wantingTalent.contains(getNegativeTalents.get(cnt*2))){
                            talentFound[cnt]=true;
                        }
                    }
                }else if(talentLists.size()==1){
                    String wantingTalent = talentLists.get(0).toLowerCase();
                    if(wantingTalent.contains(passTalent) || wantingTalent.contains(getNegativeTalents.get(1)) || wantingTalent.contains(getNegativeTalents.get(0))){
                        talentFound[0]=true;
                    }
                }else{
                    for(int cnt=0;cnt<talentLists.size();cnt++){
                        String wantingTalent = talentLists.get(cnt).toLowerCase();
                        if(getNegativeTalents.contains(wantingTalent)){
                            talentFound[cnt]=true;
                        }
                    }
                }
            }
            for (boolean found : talentFound) {
                if (found) matched++;
            }
            final int matchedTalents = matched;

            if(itemList.isEmpty()){
                return new MatchResult(seed, matched, total);
            }

            GamesInProgress.selectedClass = HeroClass.WARRIOR;

            Dungeon.init();

            boolean[] itemsFound = new boolean[itemList.size()];
            Arrays.fill(itemsFound, false);


            for (int i = 0; i < floors; i++) {
                Level l = Dungeon.newLevel();

                ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
                heaps.addAll(getMobDrops(l));

                if(Ghost.Quest.armor != null){
                    for (int j = 0; j < itemList.size(); j++) {
                        String wantingItem = itemList.get(j);
                        boolean precise = wantingItem.startsWith("\"")&&wantingItem.endsWith("\"");
                        if(precise){
                            wantingItem = wantingItem.replaceAll(" ", "");
                        }else{
                            wantingItem = wantingItem.replaceAll("\"","");
                        }
                        if (!precise&&Ghost.Quest.armor.identify().title().toLowerCase().replaceAll(" ","").contains(wantingItem) || precise&& Ghost.Quest.armor.identify().title().toLowerCase().equals(wantingItem)) {
                            itemsFound[j] = true;
                        }
                    }
                }
                if(Wandmaker.Quest.wand1 != null){
                    for (int j = 0; j < itemList.size(); j++) {
                        String wantingItem = itemList.get(j);
                        String wand1 = Wandmaker.Quest.wand1.identify().title().toLowerCase();
                        String wand2 = Wandmaker.Quest.wand2.identify().title().toLowerCase();
                        boolean precise = wantingItem.startsWith("\"")&&wantingItem.endsWith("\"");
                        if(precise){
                            wantingItem = wantingItem.replaceAll("\"","");
                            if (wand1.equals(wantingItem) || wand2.equals(wantingItem)) {
                                itemsFound[j] = true;
                            }
                        }else{
                            wantingItem = wantingItem.replaceAll(" ", "");
                            wand1 = wand1.replaceAll(" ","");
                            wand2 = wand2.replaceAll(" ","");
                            if (wand1.contains(wantingItem) || wand2.contains(wantingItem)) {
                                itemsFound[j] = true;
                            }
                        }
                        if(Wandmaker.Quest.type() == 1 && Messages.get(this, "corpsedust").contains(wantingItem.replaceAll(" ",""))){
                            itemsFound[j] = true;
                        }else if(Wandmaker.Quest.type() == 2 && Messages.get(this, "embers").contains(wantingItem.replaceAll(" ",""))){
                            itemsFound[j] = true;
                        }else if(Wandmaker.Quest.type() == 3 && Messages.get(this, "rotberry").contains(wantingItem.replaceAll(" ",""))){
                            itemsFound[j] = true;
                        }
                    }
                }
                if(Imp.Quest.reward != null){
                    for (int j = 0; j < itemList.size(); j++) {
                        String wantingItem = itemList.get(j);
                        boolean precise = wantingItem.startsWith("\"")&&wantingItem.endsWith("\"");
                        String ring = Imp.Quest.reward.identify().title().toLowerCase();
                        if (!precise&&ring.replaceAll(" ","").contains(wantingItem.replaceAll(" ",""))
                                ||
                                precise&& ring.equals(wantingItem)) {
                            itemsFound[j] = true;
                        }
                    }
                }

                for (Heap h : heaps) {
                    for (Item item : h.items) {
                        item.identify();
                        String itemName = item.title().toLowerCase();

                        for (int j = 0; j < itemList.size(); j++) {
                            String wantingItem = itemList.get(j);
                            boolean precise = wantingItem.startsWith("\"")&&wantingItem.endsWith("\"");
                            if (!precise&&itemName.replaceAll(" ","").contains(wantingItem.replaceAll(" ",""))
                                    || precise&& itemName.equals(wantingItem.replaceAll("\"", ""))) {
                                itemsFound[j] = true;
                            }
                        }
                    }
                }
                Dungeon.depth++;
                if (areAllTrue(itemsFound) || findingStatus == FINDING.STOP) {
                    return new MatchResult(seed, matchedTalents + countFound(itemsFound), total);
                }
            }
            return new MatchResult(seed, matchedTalents + countFound(itemsFound), total);
        } finally {
            SPDSettings.challenges(a);
            SPDSettings.customSeed(oldSeed);
        }
    }

    private static int countFound(boolean[] array) {
        int count = 0;
        for (boolean found : array) {
            if (found) count++;
        }
        return count;
    }

    private static boolean areAllTrue(boolean[] array)
    {
        for(boolean b : array) if(!b) return false;
        return true;
    }

    public String logSeedItems(String seed, int floors) {
        return logSeedItems(seed, floors, -1);
    }

    public String logSeedItems(String seed, int floors, int matchScore) {
        synchronized (DUNGEON_LOCK) {
            return logSeedItemsLocked(seed, floors, matchScore);
        }
    }

    private String logSeedItemsLocked(String seed, int floors, int matchScore) {
        String oldSeed = SPDSettings.customSeed();
        int a = SPDSettings.challenges();
        try {
            SPDSettings.challenges( Challenges.TEST_MODE);
            SPDSettings.customSeed(seed);
            Dungeon.initSeed();
            GamesInProgress.selectedClass = HeroClass.WARRIOR;
            Dungeon.init();
            StringBuilder result = new StringBuilder(Messages.get(this, "seed") + DungeonSeed.convertToCode(Dungeon.seed) + " \n(" + Dungeon.seed + ") " + Messages.get(this, "items") + ":\n");
            if (matchScore >= 0) {
                result.append(Messages.get(this, "match_score")).append(matchScore).append("%\n");
            }
            result.append("\n");

            blacklist = Arrays.asList(Gold.class, Dewdrop.class, IronKey.class, GoldenKey.class, CrystalKey.class, EnergyCrystal.class,
                    CorpseDust.class, Embers.class, CeremonialCandle.class, Pickaxe.class);
            result.append("\n_----- ").append(Messages.get(this, "talent") + " -----_\n\n");

            ArrayList<Talent> getNegativeTalent=getNegativeTalent();
            ArrayList<Talent> getNegativeTalents=new ArrayList<Talent>();
            for(int i=0;i<8;i++){
                getNegativeTalents.add(getNegativeTalent.get(i));
            }
            StringBuilder talentBuilder = new StringBuilder();
            addTextTalent(getNegativeTalents, talentBuilder);
            result.append("\n").append(talentBuilder);

            for (int i = 0; i < floors; i++) {
                result.append("\n_----- ").append(Long.toString(Dungeon.depth)).append(" ").append(Messages.get(this, "floor") + " -----_\n\n");

                Level l = Dungeon.newLevel();
                ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
                StringBuilder builder = new StringBuilder();
                ArrayList<HeapItem> scrolls = new ArrayList<>();
                ArrayList<HeapItem> potions = new ArrayList<>();
                ArrayList<HeapItem> equipment = new ArrayList<>();
                ArrayList<HeapItem> rings = new ArrayList<>();
                ArrayList<HeapItem> artifacts = new ArrayList<>();
                ArrayList<HeapItem> wands = new ArrayList<>();
                ArrayList<HeapItem> others = new ArrayList<>();
                ArrayList<HeapItem> forSales = new ArrayList<>();

            // list quest rewards
            if (Ghost.Quest.armor != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Ghost.Quest.armor.identify());
                rewards.add(Ghost.Quest.weapon.identify());
                Ghost.Quest.complete();

                addTextQuest("[ " + Messages.get(this, "sad_ghost_reward") + " ]", rewards, builder);
            }

            if (Wandmaker.Quest.wand1 != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Wandmaker.Quest.wand1.identify());
                rewards.add(Wandmaker.Quest.wand2.identify());
                Wandmaker.Quest.complete();

                builder.append("[ " + Messages.get(this, "wandmaker_need") +" ]:\n ");


                switch (Wandmaker.Quest.type()) {
                    case 1: default:
                        builder.append(Messages.get(this, "corpsedust") + "\n\n");
                        break;
                    case 2:
                        builder.append(Messages.get(this, "embers") + "\n\n");
                        break;
                    case 3:
                        builder.append(Messages.get(this, "rotberry") + "\n\n");
                }

                addTextQuest("[ "+ Messages.get(this, "wandmaker_reward") +" ]", rewards, builder);
            }

            if (Imp.Quest.reward != null) {
                ArrayList<Item> rewards = new ArrayList<>();
                rewards.add(Imp.Quest.reward.identify());
                Imp.Quest.complete();

                addTextQuest("[ "+ Messages.get(this, "imp_reward") +" ]", rewards, builder);
            }

            heaps.addAll(getMobDrops(l));

            // list items
            for (Heap h : heaps) {
                for (Item item : h.items) {
                    item.identify();

                    if (h.type == Type.FOR_SALE) forSales.add(new HeapItem(item, h));
                    else if (blacklist.contains(item.getClass())) continue;
                    else if (item instanceof Scroll) scrolls.add(new HeapItem(item, h));
                    else if (item instanceof Potion) potions.add(new HeapItem(item, h));
                    else if (item instanceof MeleeWeapon || item instanceof Armor) equipment.add(new HeapItem(item, h));
                    else if (item instanceof Ring) rings.add(new HeapItem(item, h));
                    else if (item instanceof Artifact) artifacts.add(new HeapItem(item, h));
                    else if (item instanceof Wand) wands.add(new HeapItem(item, h));
                    else others.add(new HeapItem(item, h));
                }
            }

            addTextItems("[ "+ Messages.get(this, "scrolls") +" ]", scrolls, builder);
            addTextItems("[ "+ Messages.get(this, "potions") +" ]", potions, builder);
            addTextItems("[ "+ Messages.get(this, "equipment") +" ]", equipment, builder);
            addTextItems("[ "+ Messages.get(this, "rings") +" ]", rings, builder);
            addTextItems("[ "+ Messages.get(this, "artifacts") +" ]", artifacts, builder);
            addTextItems("[ "+ Messages.get(this, "wands") +" ]", wands, builder);
            addTextItems("[ "+ Messages.get(this, "for_sales") +" ]", forSales, builder);
            addTextItems("[ "+ Messages.get(this, "others") +" ]", others, builder);

            result.append("\n").append(builder);

                Dungeon.depth++;
            }
            SPDSettings.customSeed(seed);
            return result.toString();
        } finally {
            SPDSettings.challenges(a);
            SPDSettings.customSeed(seed);
            //SPDSettings.customSeed(oldSeed);
        }
    }

}
