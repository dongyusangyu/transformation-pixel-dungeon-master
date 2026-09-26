package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Bundle;

/** Common marker and lifecycle surface for bosses selected by a tower boss level. */
public abstract class TowerBoss extends Mob {

    static final Class<ScrollOfMetamorphosis> GUARANTEED_TOWER_REWARD = ScrollOfMetamorphosis.class;
    private boolean towerDeathNotified;
    private final TowerBossHitLimit hitLimit = new TowerBossHitLimit();
    private transient boolean resolvingMechanismDamage;
    private transient boolean resolvingMarkedAttackSequence;

    /** Marker for private boss state buffs which must not be treated as external debuffs. */
    public interface InternalState {
    }

    public abstract String towerBossId();

    public static boolean towerRulesActive() {
        return Dungeon.branch == TowerLevel.BRANCH && Dungeon.depth >= 1;
    }

    /** Initializes a boss placed by the standalone test tool. */
    public void prepareForStandalonePlacement(Char initialTarget) {
        if (initialTarget != null) {
            enemy = initialTarget;
            target = initialTarget.pos;
        }
        state = HUNTING;
    }

    public boolean prepareArena(TowerBossLevel level, int spawnCell) {
        return true;
    }

    public void cleanupArena(TowerBossLevel level) {
        TowerBossSlashMarks.clearOwnerFromAll(id());
    }

    protected void markSkillHit(Char target) {
        if (towerRulesActive() && target != null && target.isAlive()) {
            TowerBossSlashMarks.add(target, id());
        }
    }

    protected boolean isSkillAttack() {
        return false;
    }

	@Override
	protected boolean attackAlwaysHits(Char enemy, DamageTag... damageTags) {
		return isSkillAttack();
	}

    protected final boolean isResolvingMarkedAttackSequence() {
        return resolvingMarkedAttackSequence;
    }

    protected void onMarkedAttackSequenceComplete(Char target) {
    }

    protected final void mechanismDamage(int amount, Object source, DamageTag... tags) {
        boolean previous = resolvingMechanismDamage;
        resolvingMechanismDamage = true;
        try {
            damage(amount, source, tags);
        } finally {
            resolvingMechanismDamage = previous;
        }
    }

    protected final boolean isMechanismDamage() {
        return resolvingMechanismDamage;
    }

    protected int damageCap(Object source, DamageTag... tags) {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int modifyPreShieldDamage(int damage, Object source, DamageTag... tags) {
        return resolvingMechanismDamage || !towerRulesActive()
                ? damage : Math.min(damage, Math.max(0, damageCap(source, tags)));
    }

    static boolean consumesMarks(boolean skill, DamageTag... tags) {
        if (skill) return false;
        if (tags != null) {
            for (DamageTag tag : tags) {
                if (tag == DamageTag.RANGED || tag == DamageTag.MAGICAL) return false;
            }
        }
        return true;
    }

    boolean isBasicMeleeAttack(DamageTag... tags) {
        if (!consumesMarks(isSkillAttack(), tags)) return false;
        for (DamageTag tag : tags) {
            if (tag == DamageTag.MELEE) return true;
        }
        return physicalAttackDeliveryTag() == DamageTag.MELEE;
    }

    @Override
    public boolean attack(Char enemy, float damageMultiplier, float damageBonus,
                          float accuracyMultiplier, DamageTag... tags) {
        int extraHits = towerRulesActive() && enemy != null && isBasicMeleeAttack(tags)
                && canAttack(enemy) ? TowerBossSlashMarks.consume(enemy, id()) : 0;
        resolvingMarkedAttackSequence = extraHits > 0;
        try {
            boolean firstHit = super.attack(enemy, damageMultiplier, damageBonus, accuracyMultiplier, tags);
            for (int i = 0; i < extraHits && enemy != null && enemy.isAlive(); i++) {
                super.attack(enemy, damageMultiplier, damageBonus, accuracyMultiplier, tags);
            }
            return firstHit;
        } finally {
            if (resolvingMarkedAttackSequence) {
                resolvingMarkedAttackSequence = false;
                onMarkedAttackSequenceComplete(enemy);
            }
        }
    }

    protected boolean isInternalState(Buff buff) {
        return buff instanceof InternalState;
    }

    static boolean isPeriodicDamageSource(Object source) {
        return source instanceof Blob || source instanceof Burning
                || source instanceof Corrosion || source instanceof Ooze
                || source instanceof Poison || source instanceof Bleeding
                || source instanceof Corruption || source instanceof Hunger
                || source instanceof AscensionChallenge;
    }

    @Override
    public void damage(int damage, Object source, DamageTag... damageTags) {
        boolean periodic = isPeriodicDamageSource(source);
        float time = Statistics.duration + Actor.now();
        if (towerRulesActive() && damage > 0 && !resolvingMechanismDamage
                && !hitLimit.allows(time, periodic)) return;
        int hpBefore = HP;
        int shieldingBefore = shielding();
        super.damage(damage, source, damageTags);

        int damageTaken = Math.max(0, hpBefore - HP);
        if (towerRulesActive() && !periodic && !resolvingMechanismDamage
                && (damageTaken > 0 || shielding() < shieldingBefore)) {
            hitLimit.record(time);
        }
        LockedFloor lock = Dungeon.hero == null ? null : Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && damageTaken > 0) {
            lock.addTime(recoveryTimeForDamage(
                    damageTaken, Dungeon.isChallenged(Challenges.STRONGER_BOSSES)));
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        hitLimit.store(bundle, Statistics.duration + Actor.now());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        hitLimit.restore(bundle);
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
        TowerBossSlashMarks.clearOwnerFromAll(id());
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
