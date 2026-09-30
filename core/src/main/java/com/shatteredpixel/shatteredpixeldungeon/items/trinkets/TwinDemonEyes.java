package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedFlameDamage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.CursedFlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.Set;

/** A trinket which keeps one remote target locked for a limited number of turns. */
public class TwinDemonEyes extends Trinket {

    public enum Mode {
        FLAME_EYE,
        LASER_EYE
    }

    public enum FinishReason {
        TURN_LIMIT,
        TARGET_LOST,
        ITEM_REMOVED,
        FLOOR_CHANGED
    }

    private static final String MODE = "twin_demon_eyes_mode";
    private static final String LOCK_ACTIVE = "twin_demon_eyes_lock_active";
    private static final String LOCK_TARGET_ID = "twin_demon_eyes_lock_target_id";
    private static final String LOCK_CELL = "twin_demon_eyes_lock_cell";
    private static final String LOCK_TURNS = "twin_demon_eyes_lock_turns";
    private static final String LOCK_DEPTH = "twin_demon_eyes_lock_depth";
    private static final String LOCK_BRANCH = "twin_demon_eyes_lock_branch";

    {
        image = EXItemSpriteSheet.TWIN_DEMON_EYES;
    }

    private Mode mode = randomMode();
    private boolean lockActive;
    private boolean finishingLock;
    private int lockTargetId;
    private int lastTargetCell = -1;
    private int elapsedLockTurns;
    private int lockDepth = -1;
    private int lockBranch = -1;

    private static Mode randomMode() {
        return Random.Int(2) == 0 ? Mode.FLAME_EYE : Mode.LASER_EYE;
    }

    @Override
    protected int upgradeEnergyCost() {
        return 20 + 5 * level();
    }

    @Override
    public String statsDesc() {
        if (!isIdentified()) {
            return Messages.get(this, "typical_stats_desc", lockDuration());
        }
        if (mode == Mode.FLAME_EYE) {
            return Messages.get(this, "stats_desc_flame", lockDuration());
        }
        return Messages.get(this, "stats_desc_laser", lockDuration());
    }

    public Mode mode() {
        return mode;
    }

    public int lockDuration() {
        return 4 + 2 * Math.max(0, Math.min(3, buffedLvl()));
    }

    static boolean isFlameShotTurn(int lockTurn) {
        return lockTurn > 0 && (lockTurn & 1) == 1;
    }

    public boolean isLocked() {
        return lockActive;
    }

    public int lockedTargetId() {
        return lockTargetId;
    }

    public int lastTargetCell() {
        return lastTargetCell;
    }

    public int elapsedLockTurns() {
        return elapsedLockTurns;
    }

    /**
     * Called only after a ranged attack's hit resolution has completed. Misses,
     * deaths, friendly fire and item ownership are checked at this boundary.
     */
    public boolean tryBeginLock(Hero owner, Char survivor) {
        if (owner == null || owner != Dungeon.hero || owner.belongings == null
                || owner.belongings.backpack == null
                || !owner.belongings.backpack.contains(this)
                || lockActive || survivor == null || survivor == owner
                || survivor.alignment != Char.Alignment.ENEMY || !survivor.isAlive()) {
            return false;
        }

        lockActive = true;
        lockTargetId = survivor.id();
        lastTargetCell = survivor.pos;
        elapsedLockTurns = 0;
        lockDepth = Dungeon.depth;
        lockBranch = Dungeon.branch;
        Buff.affect(owner, EyeLock.class);
        refreshAwareness(owner, lockTargetId);
        applyLockBlindness(owner);
        return true;
    }

    /** Central subscriber for completed missile and wand hits. */
    public static boolean onSuccessfulRangedHit(Hero owner, Char survivor) {
        if (owner == null || owner.belongings == null || owner.belongings.backpack == null) {
            return false;
        }
        TwinDemonEyes eyes = owner.belongings.getItem(TwinDemonEyes.class);
        return eyes != null && owner.belongings.backpack.contains(eyes)
                && eyes.tryBeginLock(owner, survivor);
    }

    /** Prevents a secondary area target from being mistaken for the aimed wand target. */
    public static Char nearestZapTarget(Set<Char> reportedTargets, Hero owner) {
        if (reportedTargets == null || owner == null || Dungeon.level == null
                || owner.pos < 0 || owner.pos >= Dungeon.level.length()) return null;
        Char nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (Char candidate : reportedTargets) {
            if (candidate == null || candidate == owner || !candidate.isAlive()
                    || candidate.alignment != Char.Alignment.ENEMY
                    || candidate.pos < 0 || candidate.pos >= Dungeon.level.length()
                    || Actor.findById(candidate.id()) != candidate) continue;
            int distance = Dungeon.level.distance(owner.pos, candidate.pos);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private void applyLockBlindness(Hero owner) {
        float cap = lockDuration();
        Blindness existing = owner.buff(Blindness.class);
        float remaining = existing == null ? 0f : Math.max(0f, existing.cooldown());
        if (remaining < cap) {
            Buff.affect(owner, Blindness.class, cap - remaining);
        }
    }

    private static void refreshAwareness(Hero owner, int targetId) {
        if (owner == null) return;
        TwinEyeAwareness awareness = Buff.affect(owner, TwinEyeAwareness.class);
        awareness.charID = targetId;
        awareness.refresh(2 * Actor.TICK);
    }

    private static void clearAwareness(Hero owner) {
        if (owner != null) Buff.detach(owner, TwinEyeAwareness.class);
    }

    /** Returns a current cell when the target is still present, otherwise the saved last cell. */
    public int targetCell() {
        if (!lockActive) return -1;
        Actor target = Actor.findById(lockTargetId);
        if (target instanceof Char && ((Char) target).isAlive()) {
            lastTargetCell = ((Char) target).pos;
        }
        return lastTargetCell;
    }

    public boolean isTargetAware(Char candidate) {
        return lockActive && candidate != null && candidate.id() == lockTargetId;
    }

    /** Advances the single serialized lock state before resolving this turn's mode effect. */
    public void recordLockTurn(int targetCell) {
        if (!lockActive) return;
        if (targetCell >= 0) lastTargetCell = targetCell;
        elapsedLockTurns++;
    }

    /** Ends the active lock and switches modes exactly once. */
    public boolean finishLock() {
        return finishLock(FinishReason.TURN_LIMIT);
    }

    public boolean finishLock(FinishReason reason) {
        if (!lockActive || finishingLock) return false;
        // Damage can remove the item or target while its final laser is resolving.
        finishingLock = true;
        try {
            onLockFinish(reason);
            clearAwareness(Dungeon.hero);
            clearLockState();
            mode = mode == Mode.FLAME_EYE ? Mode.LASER_EYE : Mode.FLAME_EYE;
            if (com.badlogic.gdx.Gdx.app != null) {
                GLog.w(Messages.get(this, mode == Mode.LASER_EYE
                        ? "mode_changed_laser" : "mode_changed_flame"));
            }
            return true;
        } finally {
            finishingLock = false;
        }
    }

    @Override
    protected void onDetach() {
        if (finishLock(FinishReason.ITEM_REMOVED) && Dungeon.hero != null) {
            Buff.detach(Dungeon.hero, EyeLock.class);
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MODE, mode.ordinal());
        bundle.put(LOCK_ACTIVE, lockActive);
        if (lockActive) {
            bundle.put(LOCK_TARGET_ID, lockTargetId);
            bundle.put(LOCK_CELL, lastTargetCell);
            bundle.put(LOCK_TURNS, elapsedLockTurns);
            bundle.put(LOCK_DEPTH, lockDepth);
            bundle.put(LOCK_BRANCH, lockBranch);
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        int storedMode = bundle.contains(MODE) ? bundle.getInt(MODE) : -1;
        mode = storedMode == Mode.LASER_EYE.ordinal() ? Mode.LASER_EYE
                : storedMode == Mode.FLAME_EYE.ordinal() ? Mode.FLAME_EYE : randomMode();
        lockActive = bundle.getBoolean(LOCK_ACTIVE);
        if (lockActive && bundle.contains(LOCK_TARGET_ID)) {
            lockTargetId = bundle.getInt(LOCK_TARGET_ID);
            lastTargetCell = bundle.getInt(LOCK_CELL);
            elapsedLockTurns = Math.max(0, bundle.getInt(LOCK_TURNS));
            lockDepth = bundle.contains(LOCK_DEPTH) ? bundle.getInt(LOCK_DEPTH) : Dungeon.depth;
            lockBranch = bundle.contains(LOCK_BRANCH) ? bundle.getInt(LOCK_BRANCH) : Dungeon.branch;
        } else {
            clearLockState();
        }
    }

    private void clearLockState() {
        lockActive = false;
        lockTargetId = 0;
        lastTargetCell = -1;
        elapsedLockTurns = 0;
        lockDepth = -1;
        lockBranch = -1;
    }

    /** Hero-attached heartbeat keeps target awareness alive and safely closes stale locks. */
    public static class EyeLock extends Buff {

        private TwinDemonEyes eyes() {
            if (!(target instanceof Hero)) return null;
            Hero hero = (Hero) target;
            TwinDemonEyes eyes = hero.belongings == null || hero.belongings.backpack == null
                    ? null : hero.belongings.getItem(TwinDemonEyes.class);
            return eyes != null && hero.belongings.backpack.contains(eyes) ? eyes : null;
        }

        @Override public String name() {
            TwinDemonEyes eyes = eyes();
            return Messages.get(this, eyes == null || eyes.mode == Mode.FLAME_EYE
                    ? "name_flame" : "name_laser");
        }

        @Override public String desc() {
            TwinDemonEyes eyes = eyes();
            if (eyes == null || eyes.mode == Mode.FLAME_EYE) {
                int damage = eyes == null ? 1 : 1 + 2 * eyes.buffedLvl();
                int turns = eyes == null ? 0 : Math.max(0, eyes.lockDuration() - eyes.elapsedLockTurns);
                int level = eyes == null ? 0 : Math.max(0, eyes.buffedLvl());
                return Messages.get(this, "desc_flame", damage, 4, 1 + level, turns);
            }
            int level = eyes.buffedLvl();
            int minDamage = 5 + level;
            int chargedTurns = Math.max(1, eyes.elapsedLockTurns);
            int maxDamage = minDamage * chargedTurns;
            int turns = Math.max(0, eyes.lockDuration() - eyes.elapsedLockTurns);
            return Messages.get(this, "desc_laser", minDamage, maxDamage, turns);
        }

        @Override
        public boolean act() {
            if (!(target instanceof Hero)) {
                detach();
                return true;
            }
            Hero owner = (Hero) target;
            TwinDemonEyes eyes = owner.belongings == null || owner.belongings.backpack == null
                    ? null : owner.belongings.getItem(TwinDemonEyes.class);
            if (eyes != null && !owner.belongings.backpack.contains(eyes)) eyes = null;
            if (eyes == null || !eyes.lockActive) {
                clearAwareness(owner);
                detach();
                return true;
            }
            if (eyes.lockDepth != Dungeon.depth || eyes.lockBranch != Dungeon.branch) {
                eyes.finishLock(FinishReason.FLOOR_CHANGED);
                detach();
                return true;
            }
            Actor targetActor = Actor.findById(eyes.lockTargetId);
            if (!(targetActor instanceof Char) || !((Char) targetActor).isAlive()) {
                eyes.finishLock(FinishReason.TARGET_LOST);
                detach();
                return true;
            }

            eyes.lastTargetCell = ((Char) targetActor).pos;
            refreshAwareness(owner, eyes.lockTargetId);
            eyes.recordLockTurn(eyes.lastTargetCell);
            eyes.onLockRound((Char) targetActor);
            if (eyes.elapsedLockTurns >= eyes.lockDuration()) {
                eyes.finishLock(FinishReason.TURN_LIMIT);
                detach();
            } else {
                spend(Actor.TICK);
            }
            return true;
        }

        @Override
        public int icon() {
            return BuffIndicator.PRECISE_LOCK;
        }
    }

    /** Separate class preserves other sources which use Talisman's awareness buff. */
    public static class TwinEyeAwareness extends TalismanOfForesight.CharAwareness {

        private void refresh(float duration) {
            spend(-cooldown());
            spend(duration);
        }
    }

    protected void onLockRound(Char target) {
        if (!lockActive || mode != Mode.FLAME_EYE || Dungeon.hero == null || Dungeon.level == null) return;
        if (!isFlameShotTurn(elapsedLockTurns)) return;
        Hero owner = Dungeon.hero;
        int aimCell = targetCell();
        if (aimCell < 0) return;

        final Level expectedLevel = Dungeon.level;
        final int expectedDepth = Dungeon.depth;
        final int expectedBranch = Dungeon.branch;
        final int itemLevel = buffedLvl();
        final int burnTurns = elapsedLockTurns == 1 ? 4 : 1 + itemLevel;
        final int sourceCell = owner.pos;
        Ballistica shot = new Ballistica(sourceCell, aimCell, Ballistica.PROJECTILE);
        int collisionCell = shot.collisionPos;
        int previousFreeCell = shot.path.get(Math.max(0, shot.dist - 1));
        if (collisionCell != aimCell && Actor.findChar(collisionCell) == null
                && shot.dist + 1 < shot.path.size()) {
            int obstacleCell = shot.path.get(shot.dist + 1);
            if (expectedLevel.solid[obstacleCell]
                    || (!expectedLevel.passable[obstacleCell] && !expectedLevel.avoid[obstacleCell])) {
                collisionCell = obstacleCell;
                previousFreeCell = shot.collisionPos;
            }
        }
        final int impactCell = collisionCell;
        final int freeCell = previousFreeCell;

        if (owner.sprite != null && owner.sprite.parent != null) {
            Sample.INSTANCE.play(Assets.Sounds.ZAP, 1f, Random.Float(0.9f, 1.1f));
            MagicMissile missile = (MagicMissile) owner.sprite.parent.recycle(MagicMissile.class);
            missile.reset(MagicMissile.CURSED_FLAME, sourceCell, impactCell, null);
        }
        // Resolve on the actor thread before another actor can leave the impact cell.
        resolveFlameImpact(expectedLevel, owner, expectedDepth, expectedBranch,
                impactCell, freeCell, itemLevel, burnTurns);
    }

    protected void onLockFinish(FinishReason reason) {
        if (mode != Mode.LASER_EYE || elapsedLockTurns <= 0 || reason == FinishReason.FLOOR_CHANGED
                || Dungeon.hero == null || Dungeon.level == null
                || lockDepth != Dungeon.depth || lockBranch != Dungeon.branch) return;

        Hero owner = Dungeon.hero;
        int destination = targetCell();
        if (destination < 0) return;
        Level expectedLevel = Dungeon.level;
        int expectedDepth = Dungeon.depth;
        int expectedBranch = Dungeon.branch;
        boolean allowRemovedItem = reason == FinishReason.ITEM_REMOVED;
        fireLaser(expectedLevel, owner, expectedDepth, expectedBranch, destination,
                buffedLvl(), elapsedLockTurns, allowRemovedItem);
    }

    private void resolveFlameImpact(Level expectedLevel, Hero owner, int expectedDepth, int expectedBranch,
                                   int collisionCell, int previousFreeCell, int itemLevel, int turns) {
        if (!isCurrentContext(expectedLevel, owner, expectedDepth, expectedBranch, false)
                || !expectedLevel.insideMap(collisionCell)) return;

        Char collision = Actor.findChar(collisionCell);
        if (collision != null && collision != owner && collision.isAlive()) {
            CursedFlameDamage.apply(collision, 1 + 2 * itemLevel, this);
            if (owner.sprite != null) {
                Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1f, Random.Float(0.9f, 1.1f));
            }
            if (collision.isAlive()) {
                CursedBurning.apply(collision, turns);
            }
        }

        if (expectedLevel.flamable[collisionCell]
                && CursedFlame.canIgnite(expectedLevel, collisionCell)) {
            GameScene.add(Blob.seed(collisionCell, 2, CursedFlame.class, expectedLevel));
            if (owner.sprite != null) CellEmitter.get(collisionCell).burst(CursedFlameParticle.FACTORY, 6);
        } else if (expectedLevel.solid[collisionCell]
                && CursedFlame.canIgnite(expectedLevel, previousFreeCell)) {
            GameScene.add(Blob.seed(previousFreeCell, 2, CursedFlame.class, expectedLevel));
            if (owner.sprite != null) CellEmitter.get(previousFreeCell).burst(CursedFlameParticle.FACTORY, 6);
        }
    }

    private void fireLaser(Level expectedLevel, Hero owner, int expectedDepth, int expectedBranch,
                           int destination, int itemLevel, int chargedTurns, boolean allowRemovedItem) {
        if (!isCurrentContext(expectedLevel, owner, expectedDepth, expectedBranch, allowRemovedItem)) return;
        int sourceCell = owner.pos;
        // Clip the original ray by length; clamping an extrapolated endpoint changes its direction.
        Ballistica ray = new Ballistica(sourceCell, destination, Ballistica.WONT_STOP);
        int endIndex = Math.min(12, ray.path.size() - 1);
        int endCell = ray.path.get(Math.max(0, endIndex));
        resolveLaserPath(expectedLevel, owner, ray.path, endIndex, itemLevel, chargedTurns);

        if (owner.sprite != null && owner.sprite.parent != null) {
            Beam beam;
            if (chargedTurns < 8) {
                beam = new Beam.DeathRay(DungeonTilemap.raisedTileCenterToWorld(sourceCell),
                        DungeonTilemap.raisedTileCenterToWorld(endCell));
            } else {
                beam = new Beam.LightRay(DungeonTilemap.raisedTileCenterToWorld(sourceCell),
                        DungeonTilemap.raisedTileCenterToWorld(endCell));
                beam.tint(1f, 0.16f, 0.16f, 1f);
            }
            owner.sprite.parent.add(beam);
        }
    }

    void resolveLaserPath(Level expectedLevel, Hero owner, java.util.List<Integer> path,
                          int endIndex, int itemLevel, int chargedTurns) {
        if (expectedLevel == null || Dungeon.level != expectedLevel || owner == null || Dungeon.hero != owner) return;
        java.util.HashSet<Char> hit = new java.util.HashSet<>();
        int minDamage = 5 + itemLevel;
        int maxDamage = minDamage * Math.max(1, chargedTurns);
        for (int i = 1; i <= endIndex && i < path.size(); i++) {
            Char ch = Actor.findChar(path.get(i));
            if (ch != null && ch != owner && ch.isAlive() && hit.add(ch)) {
                ch.damage(Hero.heroDamageIntRange(minDamage, maxDamage), this, DamageTag.MAGICAL);
            }
        }
    }

    private boolean isCurrentContext(Level expectedLevel, Hero owner, int expectedDepth,
                                     int expectedBranch, boolean allowRemovedItem) {
        return owner == Dungeon.hero && Dungeon.level == expectedLevel
                && Dungeon.depth == expectedDepth && Dungeon.branch == expectedBranch
                && owner.belongings != null && owner.belongings.backpack != null
                && (allowRemovedItem || owner.belongings.backpack.contains(this));
    }
}
