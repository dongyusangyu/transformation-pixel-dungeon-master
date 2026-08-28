package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;

/** Common marker and lifecycle surface for bosses selected by a tower boss level. */
public abstract class TowerBoss extends Mob {

    static final Class<ScrollOfMetamorphosis> GUARANTEED_TOWER_REWARD = ScrollOfMetamorphosis.class;
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
    public void damage(int damage, Object source, DamageTag... damageTags) {
        int hpBefore = HP;
        super.damage(damage, source, damageTags);

        int damageTaken = Math.max(0, hpBefore - HP);
        LockedFloor lock = Dungeon.hero == null ? null : Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && damageTaken > 0) {
            lock.addTime(recoveryTimeForDamage(
                    damageTaken, Dungeon.isChallenged(Challenges.STRONGER_BOSSES)));
        }
    }

    static float recoveryTimeForDamage(int damageTaken, boolean strongerBosses) {
        if (damageTaken <= 0) return 0f;
        return damageTaken / (strongerBosses ? 3f : 2f);
    }

    static ScrollOfMetamorphosis guaranteedTowerReward() {
        return new ScrollOfMetamorphosis();
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        if (!towerDeathNotified && Dungeon.level instanceof TowerBossLevel) {
            towerDeathNotified = true;
            Badges.validateTowerBossSlain(towerBossId(),
                    Dungeon.hero == null ? null : Dungeon.hero.heroClass);
            Dungeon.level.drop(guaranteedTowerReward(), pos).sprite.drop(pos);
            ((TowerBossLevel) Dungeon.level).onTowerBossDefeated(this);
        }
    }
}
