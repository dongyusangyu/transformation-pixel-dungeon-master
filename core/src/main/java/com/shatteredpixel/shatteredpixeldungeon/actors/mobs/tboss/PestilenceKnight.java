package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.IncubatingMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.OutbreakMiasma;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** Three-stage plague-doctor boss used by {@link TowerBossLevel}. */
public class PestilenceKnight extends TowerBoss {

    public enum Phase { INCUBATION, OUTBREAK, TERMINAL }
    public enum HarvestState { NONE, ARMED, CHANNELING }

    public static final int KNOCKBACK_DISTANCE = 1;
    public static final int FINAL_DAMAGE_CAP = 150;

    private static final String GROWTH = "growth";
    private static final String PHASE = "phase";
    private static final String PHASE_LOCKS = "phase_locks";
    private static final String HARVEST = "harvest";
    private static final String PENDING_SKILL = "pending_skill";
    private static final String COOLDOWNS = "cooldowns";
    private static final String PENDING_CELLS = "pending_cells";
    private static final String PRESCRIPTION_INDEX = "prescription_index";
    private static final String DIAGNOSIS = "diagnosis";
    private static final String LAST_HERO_POS = "last_hero_pos";
    private static final String REWARD_DROPPED = "reward_dropped";

    private int growth;
    private Phase phase = Phase.INCUBATION;
    private int phaseLocks;
    private HarvestState harvest = HarvestState.NONE;
    private String pendingSkill = "";
    private int[] cooldowns = new int[4];
    private int[] pendingCells = new int[0];
    private int prescriptionIndex;
    private int diagnosis;
    private int lastHeroPos = -1;
    private boolean rewardDropped;

    public PestilenceKnight() {
        this(Dungeon.depth);
    }

    PestilenceKnight(int depth) {
        growth = growthForDepth(depth);
        applyGrowthStats(true);
        EXP = 0;
        maxLvl = 30;
        loot = null;
        lootChance = 0f;
        properties.add(Property.BOSS);
        properties.add(Property.IMMOVABLE);
        properties.add(Property.UNSLEEP);
    }

    private static int growthForDepth(int depth) {
        return Math.min(6, Math.max(0, depth / 5 - 1));
    }

    private void applyGrowthStats(boolean refill) {
        int oldHT = HT;
        HT = Math.round(1500 * (1f + growth * 0.05f));
        if (refill || HP <= 0 || oldHT == 0) HP = HT;
        else HP = Math.min(HT, HP);
        defenseSkill = 30;
    }

    @Override
    public String towerBossId() {
        return TowerBossGenerator.PESTILENCE_KNIGHT_ID;
    }

    @Override
    public boolean prepareArena(TowerBossLevel level, int spawnCell) {
        return level.preparePestilenceArena(spawnCell);
    }

    @Override
    public int attackSkill(Char target) {
        return 50;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(damageRollMinForTest(), damageRollMaxForTest());
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(drRollMinForTest(), drRollMaxForTest());
    }

    @Override
    public float speed() {
        return 1f;
    }

    @Override
    public float attackDelay() {
        return 1f;
    }

    @Override
    public int attackProc(Char target, int damage, DamageTag... damageTags) {
        damage = super.attackProc(target, damage, damageTags);
        if (target != null && Dungeon.level != null && Dungeon.level.adjacent(pos, target.pos)) {
            Ballistica trajectory = new Ballistica(target.pos,
                    target.pos + (target.pos - pos), Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(target, trajectory, KNOCKBACK_DISTANCE,
                    false, false, this);
            if (target == Dungeon.hero) Dungeon.hero.interrupt();
        }
        return damage;
    }

    @Override
    protected int modifyFinalDamage(int damage, Object source, DamageTag... tags) {
        if (harvest != HarvestState.NONE) return 0;
        int capped = Math.min(FINAL_DAMAGE_CAP, super.modifyFinalDamage(damage, source, tags));
        int lock = nextUnfinishedLockHP();
        if (lock < 0) return capped;
        int untilLock = Math.max(0, HP - lock);
        if (capped >= untilLock) {
            harvest = HarvestState.ARMED;
            return untilLock;
        }
        return capped;
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return harvest != HarvestState.NONE || super.isInvulnerable(effect);
    }

    @Override
    protected boolean act() {
        if (harvest != HarvestState.NONE && paralysed <= 0 && state != SLEEPING) {
            advanceHarvest(currentHarvestBlobCells());
            spend(TICK);
            advanceArenaBossTurn();
            return true;
        }
        boolean completed = super.act();
        if (completed) advanceArenaBossTurn();
        return completed;
    }

    private int nextUnfinishedLockHP() {
        if ((phaseLocks & 1) == 0) return Math.round(HT * 0.70f);
        if ((phaseLocks & 2) == 0) return Math.round(HT * 0.35f);
        return -1;
    }

    private int currentHarvestBlobCells() {
        if (Dungeon.level == null) return 0;
        Class<? extends Blob> type = phase == Phase.INCUBATION
                ? IncubatingMiasma.class : OutbreakMiasma.class;
        Blob blob = Dungeon.level.blobs.get(type);
        if (blob == null || blob.cur == null) return 0;
        int count = 0;
        for (int value : blob.cur) if (value > 0) count++;
        return count;
    }

    private void advanceHarvest(int blobCells) {
        if (harvest == HarvestState.ARMED) {
            harvest = HarvestState.CHANNELING;
            return;
        }
        if (harvest != HarvestState.CHANNELING) return;
        heal(Math.min(200, Math.max(0, blobCells) * 8));
        clearHarvestMiasma();
        if ((phaseLocks & 1) == 0) {
            phaseLocks |= 1;
            phase = Phase.OUTBREAK;
        } else {
            phaseLocks |= 2;
            phase = Phase.TERMINAL;
            if (Dungeon.level instanceof TowerBossLevel
                    && ((TowerBossLevel) Dungeon.level).pestilenceArenaController() != null) {
                ((TowerBossLevel) Dungeon.level).pestilenceArenaController().resetBrazierCooldowns();
            }
        }
        harvest = HarvestState.NONE;
    }

    private void clearHarvestMiasma() {
        if (Dungeon.level == null) return;
        Class<? extends Blob> type = phase == Phase.INCUBATION
                ? IncubatingMiasma.class : OutbreakMiasma.class;
        Blob blob = Dungeon.level.blobs.get(type);
        if (blob != null) blob.fullyClear();
    }

    private void advanceArenaBossTurn() {
        if (Dungeon.level instanceof TowerBossLevel
                && ((TowerBossLevel) Dungeon.level).pestilenceArenaController() != null) {
            ((TowerBossLevel) Dungeon.level).pestilenceArenaController().advanceBossTurn();
        }
    }

    float activeDamageMultiplier() { return 1f + 0.03f * growth; }
    int damageRollMinForTest() { return Math.round(20 * activeDamageMultiplier()); }
    int damageRollMaxForTest() { return Math.round(30 * activeDamageMultiplier()); }
    int drRollMinForTest() { return 10 + growth / 2; }
    int drRollMaxForTest() { return 25 + growth / 2; }
    int capFinalDamageForTest(int damage) { return modifyFinalDamage(damage, null); }
    int growthForTest() { return growth; }
    Phase phaseForTest() { return phase; }
    HarvestState harvestForTest() { return harvest; }
    void armHarvestForTest() { harvest = HarvestState.ARMED; }
    void advanceHarvestForTest(int blobCells) { advanceHarvest(blobCells); }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(GROWTH, growth);
        bundle.put(PHASE, phase.ordinal());
        bundle.put(PHASE_LOCKS, phaseLocks);
        bundle.put(HARVEST, harvest.ordinal());
        bundle.put(PENDING_SKILL, pendingSkill);
        bundle.put(COOLDOWNS, cooldowns);
        bundle.put(PENDING_CELLS, pendingCells);
        bundle.put(PRESCRIPTION_INDEX, prescriptionIndex);
        bundle.put(DIAGNOSIS, diagnosis);
        bundle.put(LAST_HERO_POS, lastHeroPos);
        bundle.put(REWARD_DROPPED, rewardDropped);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        growth = Math.max(0, Math.min(6, bundle.getInt(GROWTH)));
        phase = enumAt(Phase.values(), bundle.getInt(PHASE), Phase.INCUBATION);
        phaseLocks = Math.max(0, Math.min(3, bundle.getInt(PHASE_LOCKS)));
        harvest = enumAt(HarvestState.values(), bundle.getInt(HARVEST), HarvestState.NONE);
        pendingSkill = bundle.getString(PENDING_SKILL);
        if (pendingSkill == null) pendingSkill = "";
        int[] savedCooldowns = bundle.getIntArray(COOLDOWNS);
        cooldowns = savedCooldowns.length == 4 ? savedCooldowns : new int[4];
        for (int i = 0; i < cooldowns.length; i++) cooldowns[i] = Math.max(0, cooldowns[i]);
        pendingCells = bundle.getIntArray(PENDING_CELLS);
        prescriptionIndex = Math.max(0, bundle.getInt(PRESCRIPTION_INDEX));
        diagnosis = Math.max(0, bundle.getInt(DIAGNOSIS));
        lastHeroPos = bundle.contains(LAST_HERO_POS) ? bundle.getInt(LAST_HERO_POS) : -1;
        rewardDropped = bundle.getBoolean(REWARD_DROPPED);
        applyGrowthStats(false);
    }

    private static <T> T enumAt(T[] values, int index, T fallback) {
        return index >= 0 && index < values.length ? values[index] : fallback;
    }
}
