package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;

/** Common marker and lifecycle surface for bosses selected by a tower boss level. */
public abstract class TowerBoss extends Mob {

    private boolean towerDeathNotified;

    /** Marker for private boss state buffs which must not be treated as external debuffs. */
    public interface InternalState {
    }

    public abstract String towerBossId();

    public boolean prepareArena(TowerBossLevel level, int spawnCell) {
        return true;
    }

    public void cleanupArena(TowerBossLevel level) {
    }

    protected boolean isInternalState(Buff buff) {
        return buff instanceof InternalState;
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        if (!towerDeathNotified && Dungeon.level instanceof TowerBossLevel) {
            towerDeathNotified = true;
            ((TowerBossLevel) Dungeon.level).onTowerBossDefeated(this);
        }
    }
}
