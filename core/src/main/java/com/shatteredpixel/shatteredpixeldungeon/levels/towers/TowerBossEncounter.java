package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.TowerBoss;
import com.watabou.utils.Bundle;

/** Headless encounter state machine used by {@link TowerBossLevel}. */
final class TowerBossEncounter {

    private static final String BOSS_SPAWNED = "boss_spawned";
    private static final String BOSS_DEFEATED = "boss_defeated";
    private static final String TOWER_BOSS_ID = "tower_boss_id";

    interface Host {
        TowerBoss createBoss(String id);
        int selectBossSpawnCell();
        boolean prepareArena(TowerBoss boss, int spawnCell);
        void sealArena();
        void launchBoss(TowerBoss boss);
        void cleanupArena(TowerBoss boss);
        void unsealArena();
    }

    private String selectedBossId;
    private boolean bossSpawned;
    private boolean bossDefeated;

    void ensureSelected(long dungeonSeed, int depth, int branch) {
        if (selectedBossId == null || selectedBossId.isEmpty()) {
            selectedBossId = TowerBossGenerator.selectId(dungeonSeed, depth, branch);
        }
    }

    TowerBoss start(Host host) {
        if (bossSpawned || bossDefeated) {
            throw new IllegalStateException("Tower boss encounter has already started");
        }
        if (selectedBossId == null || selectedBossId.isEmpty()) {
            throw new IllegalStateException("Tower boss encounter has no selected boss");
        }

        TowerBoss boss = host.createBoss(selectedBossId);
        if (boss == null) throw new IllegalStateException("Tower boss factory returned null");
        boss.pos = host.selectBossSpawnCell();
        if (!host.prepareArena(boss, boss.pos)) {
            throw new IllegalStateException("Unable to prepare arena for tower boss " + selectedBossId);
        }

        host.sealArena();
        host.launchBoss(boss);
        bossSpawned = true;
        return boss;
    }

    void onBossDefeated(TowerBoss boss, Host host) {
        if (bossDefeated) return;
        bossDefeated = true;
        host.cleanupArena(boss);
        host.unsealArena();
    }

    void storeInBundle(Bundle bundle) {
        bundle.put(TOWER_BOSS_ID, selectedBossId);
        bundle.put(BOSS_SPAWNED, bossSpawned);
        bundle.put(BOSS_DEFEATED, bossDefeated);
    }

    void restoreFromBundle(Bundle bundle, long dungeonSeed, int depth, int branch,
            boolean levelLocked, boolean restoredTowerBoss, boolean exitUnlocked) {
        selectedBossId = bundle.contains(TOWER_BOSS_ID)
                ? bundle.getString(TOWER_BOSS_ID) : null;
        ensureSelected(dungeonSeed, depth, branch);
        bossSpawned = bundle.contains(BOSS_SPAWNED)
                ? bundle.getBoolean(BOSS_SPAWNED)
                : levelLocked || restoredTowerBoss || exitUnlocked;
        bossDefeated = bundle.contains(BOSS_DEFEATED)
                ? bundle.getBoolean(BOSS_DEFEATED) : exitUnlocked;
    }

    String selectedBossId() {
        return selectedBossId;
    }

    boolean bossEncounterStarted() {
        return bossSpawned;
    }

    boolean bossEncounterDefeated() {
        return bossDefeated;
    }
}
