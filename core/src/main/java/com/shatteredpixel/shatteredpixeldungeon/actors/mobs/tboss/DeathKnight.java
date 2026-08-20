package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.DeathKnightSlash;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossRewardGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.DeathKnightSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** Three-stage martial boss used by tower boss levels. */
public class DeathKnight extends TowerBoss {

    public enum Phase { QUESTION, BREAK_FORMATION, DEATH_DUEL }
    enum Skill { NONE, LINE, CONE, CROSS, RING, SOUL_LINE, EXECUTION }
    enum PhaseTransition { NONE, ARMED }

    public static final int FINAL_DAMAGE_CAP = 150;
    public static final int FIRST_LOCK_HP = 1050;
    public static final int SECOND_LOCK_HP = 525;

    private static final String PHASE = "phase";
    private static final String PHASE_LOCKS = "phase_locks";
    private static final String TRANSITION = "transition";
    private static final String PENDING_SKILL = "pending_skill";
    private static final String PENDING_CELLS = "pending_cells";
    private static final String PENDING_TURNS = "pending_turns";
    private static final String SKILL_INDEX = "skill_index";
    private static final String NORMAL_ACTIONS = "normal_actions";
    private static final String NORMAL_ACTIONS_REQUIRED = "normal_actions_required";
    private static final String MAIN_BOMBARDMENTS = "main_bombardments";
    private static final String LAST_EXECUTION = "last_execution";
    private static final String PENDING_BANDS = "pending_bands";
    private static final String PENDING_LANDING_CELL = "pending_landing_cell";
    private static final String PENDING_TARGET_CELL = "pending_target_cell";
    private static final String PENDING_PAUSED = "pending_paused";
    private static final String REWARD_DROPPED = "reward_dropped";
    private static final String NOTICE_ANNOUNCED = "notice_announced";
    private static final String LANDED_MELEE_ATTACKS = "landed_melee_attacks";
    private static final String COVER_BREAK_NOTICE = "cover_break_notice";

    private Phase phase = Phase.QUESTION;
    private int phaseLocks;
    private PhaseTransition transition = PhaseTransition.NONE;
    private Skill pendingSkill = Skill.NONE;
    private int[] pendingCells = new int[0];
    private DeathKnightBombardment.Band[] pendingBands = new DeathKnightBombardment.Band[0];
    private int pendingLandingCell = -1;
    private int pendingTargetCell = -1;
    private int pendingTurns;
    private int skillIndex;
    private int normalActions;
    private int normalActionsRequired;
    private int mainBombardments;
    private boolean lastExecution;
    private boolean pendingPaused;
    private transient boolean restoreGrace;
    private transient boolean waitingForAttackCompletion;
    private transient boolean waitingForLeapPush;
    private transient boolean leapCompletionHandled;
    private int landedMeleeAttacks;
    private boolean rewardDropped;
    private boolean noticeAnnounced;
    private boolean coverBreakNoticeAnnounced;

    private static final Skill[] QUESTION_SEQUENCE = {
            Skill.LINE, Skill.CONE
    };
    private static final Skill[] BREAK_SEQUENCE = {
            Skill.LINE, Skill.CROSS, Skill.CONE, Skill.RING,
            Skill.LINE, Skill.SOUL_LINE, Skill.CONE
    };
    private static final Skill[] DUEL_SEQUENCE = {
            Skill.LINE, Skill.CROSS, Skill.CONE, Skill.SOUL_LINE,
            Skill.RING, Skill.LINE, Skill.CONE
    };

    public DeathKnight() {
        HT = HP = 1500;
        defenseSkill = 30;
        EXP = 0;
        maxLvl = 30;
        loot = null;
        lootChance = 0f;
        spriteClass = DeathKnightSprite.class;
        properties.add(Property.BOSS);
        //properties.add(Property.IMMOVABLE);
        properties.add(Property.UNSLEEP);
    }

    @Override
    public String towerBossId() {
        return TowerBossGenerator.DEATH_KNIGHT_ID;
    }

    @Override
    public int attackSkill(Char target) {
        return 50;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(28, 42);
    }

    @Override
    public int drRoll() {
        return phase == Phase.DEATH_DUEL ? 0 : Random.NormalIntRange(15, 30);
    }

    @Override
    public float speed() {
        float phaseSpeed = phase == Phase.DEATH_DUEL ? 3f : 2f;
        return Math.max(1f, super.speed() * phaseSpeed);
    }

    @Override
    public float attackDelay() {
        return super.attackDelay();
    }

    @Override
    public float resist(Class effect) {
        float result = baseResist(effect);
        if (phase != Phase.QUESTION && isHalfDurationControl(effect)) result *= 0.5f;
        return result;
    }

    protected float baseResist(Class effect) {
        return super.resist(effect);
    }

    @Override
    public boolean isImmune(Class effect) {
        if (phase == Phase.DEATH_DUEL
                && (Paralysis.class.isAssignableFrom(effect)
                || Frost.class.isAssignableFrom(effect)
                || Sleep.class.isAssignableFrom(effect)
                || MagicalSleep.class.isAssignableFrom(effect))) {
            return true;
        }
        return super.isImmune(effect);
    }

    private static boolean isHalfDurationControl(Class effect) {
        return Paralysis.class.isAssignableFrom(effect)
                || Frost.class.isAssignableFrom(effect)
                || Sleep.class.isAssignableFrom(effect)
                || MagicalSleep.class.isAssignableFrom(effect)
                || Terror.class.isAssignableFrom(effect)
                || Charm.class.isAssignableFrom(effect)
                || Blindness.class.isAssignableFrom(effect);
    }

    @Override
    protected int modifyFinalDamage(int damage, Object source, DamageTag... tags) {
        if (transition != PhaseTransition.NONE) return 0;
        int capped = Math.min(FINAL_DAMAGE_CAP,
                Math.max(0, super.modifyFinalDamage(damage, source, tags)));
        int lock = nextUnfinishedLockHP();
        if (lock < 0) return capped;
        int untilLock = Math.max(0, HP - lock);
        if (capped >= untilLock) {
            transition = PhaseTransition.ARMED;
            return untilLock;
        }
        return capped;
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return transition != PhaseTransition.NONE || super.isInvulnerable(effect);
    }

    @Override
    protected boolean act() {
        if (waitingForLeapPush) return false;
        if (pendingSkill != Skill.NONE && (paralysed > 0 || state == SLEEPING)) {
            pendingPaused = true;
            showPendingTelegraph();
            return super.act();
        }
        if (bombardmentTimeFrozen()) return super.act();
        if (transition != PhaseTransition.NONE && paralysed <= 0 && state != SLEEPING) {
            return advanceTransition();
        }
        if (restoreGrace) return consumeRestoreGrace();
        if (pendingPaused) return rearmPending();
        if (pendingSkill != Skill.NONE) return advancePending();
        Char bombardmentTarget = bombardmentTarget();
        if (canUseBombardment(bombardmentTarget)) {
            Skill skill = nextSkill();
            DeathKnightBombardment.Plan plan = createPlan(skill, bombardmentTarget);
            if (plan != null && plan.cells.length > 0) {
                return telegraph(skill, plan.cells, plan.bands, plan.landingCell,
                        bombardmentTarget.pos);
            }
        }
        boolean completed = super.act();
        if (completed) finishNormalAction();
        else waitingForAttackCompletion = true;
        return completed;
    }

    @Override
    public void onAttackComplete() {
        super.onAttackComplete();
        if (waitingForAttackCompletion) {
            waitingForAttackCompletion = false;
            finishNormalAction();
        }
    }

    @Override
    protected void onAttackResolved(Char enemy, boolean hit, int damageDealt,
                                    DamageTag... damageTags) {
        super.onAttackResolved(enemy, hit, damageDealt, damageTags);
        if (!hit || enemy == null || !containsTag(damageTags, DamageTag.MELEE)
                || containsTag(damageTags, DamageTag.RANGED)) return;
        landedMeleeAttacks++;
        if (landedMeleeAttacks % 2 == 0) pushNormalMeleeTarget(enemy);
    }

    private int nextUnfinishedLockHP() {
        if ((phaseLocks & 1) == 0) return FIRST_LOCK_HP;
        if ((phaseLocks & 2) == 0) return SECOND_LOCK_HP;
        return -1;
    }

    private boolean advanceTransition() {
        if (transition == PhaseTransition.NONE) return false;
        clearPendingSkill();
        if ((phaseLocks & 1) == 0) {
            phaseLocks |= 1;
            phase = Phase.BREAK_FORMATION;
        } else if ((phaseLocks & 2) == 0) {
            phaseLocks |= 2;
            phase = Phase.DEATH_DUEL;
        }
        skillIndex = 0;
        mainBombardments = 0;
        lastExecution = false;
        normalActions = 0;
        normalActionsRequired = phase == Phase.BREAK_FORMATION ? 2 : 1;
        transition = PhaseTransition.NONE;
        if (sprite instanceof DeathKnightSprite) {
            ((DeathKnightSprite) sprite).phaseTransition();
        }
        if (sprite != null) {
            yell(Messages.get(this, phase == Phase.BREAK_FORMATION
                    ? "phase_break" : "phase_duel"));
        }
        spend(TICK);
        return true;
    }

    private void clearPendingSkill() {
        pendingSkill = Skill.NONE;
        pendingCells = new int[0];
        pendingBands = new DeathKnightBombardment.Band[0];
        pendingLandingCell = -1;
        pendingTurns = 0;
    }

    private Skill nextSkill() {
        int executionPeriod = phase == Phase.BREAK_FORMATION ? 4
                : phase == Phase.DEATH_DUEL ? 3 : Integer.MAX_VALUE;
        if (!lastExecution && mainBombardments > 0
                && mainBombardments % executionPeriod == executionPeriod - 1) {
            return Skill.EXECUTION;
        }
        Skill[] sequence = phase == Phase.QUESTION ? QUESTION_SEQUENCE
                : phase == Phase.BREAK_FORMATION ? BREAK_SEQUENCE : DUEL_SEQUENCE;
        return sequence[Math.floorMod(skillIndex, sequence.length)];
    }

    private void finishBombardment(Skill skill) {
        if (skill != Skill.EXECUTION) skillIndex++;
        mainBombardments++;
        lastExecution = skill == Skill.EXECUTION;
        normalActions = 0;
        normalActionsRequired = skill == Skill.EXECUTION ? 2
                : phase == Phase.QUESTION ? 3
                : phase == Phase.BREAK_FORMATION ? 2 : 1;
        clearPendingSkill();
    }

    private void finishNormalAction() {
        if (normalActions < normalActionsRequired) normalActions++;
    }

    private boolean bombardmentReady() {
        return pendingSkill == Skill.NONE && normalActions >= normalActionsRequired;
    }

    private Char bombardmentTarget() {
        if (isValidBombardmentTarget(enemy)) return enemy;
        Char fallback = bombardmentFallbackTarget();
        return isValidBombardmentTarget(fallback) ? fallback : null;
    }

    protected Char bombardmentFallbackTarget() {
        return Dungeon.hero;
    }

    private boolean isValidBombardmentTarget(Char target) {
        return target != null && target != this && target.isAlive()
                && Dungeon.level != null && target.pos >= 0
                && target.pos < Dungeon.level.length();
    }

    private boolean canUseBombardment(Char target) {
        return !bombardmentTimeFrozen()
                && bombardmentReady()
                && isValidBombardmentTarget(target);
    }

    private boolean bombardmentTimeFrozen() {
        return paralysed > 0;
    }

    private DeathKnightBombardment.Plan createPlan(Skill skill, Char target) {
        DeathKnightBombardment.Grid grid = currentGrid();
        if (grid == null || !isValidBombardmentTarget(target)) return null;
        int targetCell = target.pos;
        switch (skill) {
            case EXECUTION:
                return DeathKnightBombardment.execution(grid, pos, targetCell);
            case CROSS:
                return DeathKnightBombardment.firstSafePlan(grid,
                        DeathKnightBombardment.SkillShape.CROSS, pos, targetCell, 3, 0, 8);
            case RING:
                return DeathKnightBombardment.firstSafePlan(grid,
                        DeathKnightBombardment.SkillShape.RING, pos, targetCell, 0, 0, 4);
            case CONE:
                return DeathKnightBombardment.firstSafePlan(grid,
                        DeathKnightBombardment.SkillShape.CONE, pos, targetCell, 0,
                        phase == Phase.DEATH_DUEL ? 90 : 60, 8);
            case SOUL_LINE:
                return DeathKnightBombardment.firstSafePlan(grid,
                        DeathKnightBombardment.SkillShape.LINE, pos, targetCell,
                        phase == Phase.DEATH_DUEL ? 5 : 3, 0, 10);
            case LINE:
            default:
                DeathKnightBombardment.Plan line = DeathKnightBombardment.firstSafePlan(grid,
                        DeathKnightBombardment.SkillShape.LINE, pos, targetCell,
                        phase == Phase.DEATH_DUEL ? 5 : 3, 0, 10);
                return phase == Phase.DEATH_DUEL ? line
                        : DeathKnightBombardment.withLanding(line, -1);
        }
    }

    private DeathKnightBombardment.Grid currentGrid() {
        if (Dungeon.level == null) return null;
        boolean[] occupied = new boolean[Dungeon.level.length()];
        for (Char ch : com.shatteredpixel.shatteredpixeldungeon.actors.Actor.chars()) {
            if (ch != this && ch.pos >= 0 && ch.pos < occupied.length) occupied[ch.pos] = true;
        }
        boolean[] arena = new boolean[Dungeon.level.length()];
        boolean[] covers = new boolean[Dungeon.level.length()];
        TowerBossLevel bossLevel = Dungeon.level instanceof TowerBossLevel
                ? (TowerBossLevel) Dungeon.level : null;
        for (int cell = 0; cell < arena.length; cell++) {
            arena[cell] = bossLevel != null && bossLevel.isBossArenaCell(cell);
            covers[cell] = bossLevel != null && bossLevel.isDestructibleBossCover(cell);
        }
        return new DeathKnightBombardment.Grid(Dungeon.level.width(), Dungeon.level.height(),
                Dungeon.level.passable.clone(), Dungeon.level.solid.clone(), occupied,
                arena, covers);
    }

    private boolean telegraph(Skill skill, int[] cells,
                              DeathKnightBombardment.Band[] bands, int landingCell) {
        return telegraph(skill, cells, bands, landingCell, -1);
    }

    private boolean telegraph(Skill skill, int[] cells,
                              DeathKnightBombardment.Band[] bands, int landingCell,
                              int targetCell) {
        if (skill == null || skill == Skill.NONE || cells == null || cells.length == 0) return false;
        pendingSkill = skill;
        pendingCells = cells.clone();
        pendingBands = normalizedBands(bands, pendingCells.length);
        pendingLandingCell = landingCell;
        pendingTargetCell = targetCell;
        pendingTurns = telegraphTurns(skill);
        pendingPaused = false;
        if (sprite instanceof DeathKnightSprite) ((DeathKnightSprite) sprite).charge();
        announceSkill(skill);
        showPendingTelegraph();
        spend(TICK);
        return true;
    }

    private boolean advancePending() {
        if (pendingSkill == Skill.NONE) return false;
        if (pendingTurns > 1) {
            pendingTurns--;
            showPendingTelegraph();
            spend(TICK);
            return true;
        }
        boolean completed = resolvePendingSkill();
        spend(TICK);
        return completed;
    }

    protected boolean resolvePendingSkill() {
        Skill skill = pendingSkill;
        int landingCell = pendingLandingCell;
        showBombardmentFx();
        for (Char target : com.shatteredpixel.shatteredpixeldungeon.actors.Actor.chars()) {
            if (target == this || !contains(pendingCells, target.pos)) continue;
            int[] range = damageRange(skill);
            int raw = Random.NormalIntRange(range[0], range[1]);
            DeathKnightBombardment.Band band = bandAt(target.pos);
            if (skill == Skill.EXECUTION) raw = DeathKnightBombardment.scaledDamage(raw, band);
            damageWithBombardment(target, raw);
            applySkillAftermath(skill, target, band);
        }
        breakNearbyCoverAfterResolution(pendingTargetCell);
        finishBombardment(skill);
        if ((skill == Skill.SOUL_LINE
                || (skill == Skill.LINE && phase == Phase.DEATH_DUEL))
                && landingCell >= 0) {
            return beginLeapResolution(landingCell);
        }
        return true;
    }

    protected boolean breakNearbyCoverAfterResolution(int fallbackTargetCell) {
        if (!(Dungeon.level instanceof TowerBossLevel)) return false;
        TowerBossLevel level = (TowerBossLevel) Dungeon.level;
        int targetCell = Dungeon.hero != null && Dungeon.hero.isAlive()
                ? Dungeon.hero.pos : fallbackTargetCell;
        int[] candidates = DeathKnightBombardment.nearbyCoverCandidates(
                currentGrid(), pendingCells, targetCell);
        if (candidates.length == 0) return false;
        int cell = candidates[Random.Int(candidates.length)];
        if (!level.destroyBossCover(cell)) return false;
        showCoverBreak(cell);
        if (!coverBreakNoticeAnnounced) {
            coverBreakNoticeAnnounced = true;
            yell(Messages.get(this, "cover_break"));
        }
        return true;
    }

    private void showCoverBreak(int cell) {
    }

    private void showBombardmentFx() {
        if (sprite == null || sprite.parent == null || pendingCells.length == 0) return;
        DeathKnightSlash.showVolley(sprite.parent, pos, pendingCells,
                volleyRayCount(pendingSkill));
    }

    private static int volleyRayCount(Skill skill) {
        switch (skill) {
            case LINE:
            case SOUL_LINE:
                return 3;
            case CONE:
                return 5;
            case CROSS:
                return 4;
            case RING:
            case EXECUTION:
                return 8;
            default:
                return 1;
        }
    }

    private boolean beginLeapResolution(int whiteCell) {
        if (Dungeon.level == null || whiteCell < 0 || whiteCell >= Dungeon.level.length()) {
            return true;
        }
        final int originalCell = pos;
        if (sprite instanceof DeathKnightSprite) ((DeathKnightSprite) sprite).leap();
        Char occupant = charAt(whiteCell);
        if (occupant == null || occupant == this) {
            moveBossTo(whiteCell);
            return true;
        }

        int pushAim = pushAimBeyond(whiteCell, originalCell, 2);
        if (pushAim == whiteCell) {
            moveBossTo(fallbackLanding(originalCell, whiteCell));
            return true;
        }
        waitingForLeapPush = true;
        leapCompletionHandled = false;
        Ballistica trajectory = new Ballistica(whiteCell, pushAim, Ballistica.MAGIC_BOLT);
        startLeapPush(occupant, trajectory, 4, false, false,
                (resolvedByThisPush, actualDistance) ->
                        finishLeapAfterPush(originalCell, whiteCell));
        return false;
    }

    private void finishLeapAfterPush(int originalCell, int whiteCell) {
        if (leapCompletionHandled) return;
        leapCompletionHandled = true;
        int destination = charAt(whiteCell) == null
                ? whiteCell : fallbackLanding(originalCell, whiteCell);
        moveBossTo(destination);
        waitingForLeapPush = false;
        next();
    }

    private int pushAimBeyond(int whiteCell, int originalCell, int distance) {
        int width = Dungeon.level.width();
        int dx = Integer.compare(whiteCell % width, originalCell % width);
        int dy = Integer.compare(whiteCell / width, originalCell / width);
        int aim = whiteCell;
        for (int i = 0; i < distance; i++) {
            int x = aim % width + dx;
            int y = aim / width + dy;
            if (x < 0 || x >= width || y < 0 || y >= Dungeon.level.height()) break;
            aim = x + y * width;
        }
        return aim;
    }

    private int fallbackLanding(int originalCell, int whiteCell) {
        Ballistica route = new Ballistica(originalCell, whiteCell,
                Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
        for (int i = Math.min(route.dist - 1, route.path.size() - 1); i >= 1; i--) {
            int cell = route.path.get(i);
            if (!Dungeon.level.passable[cell]
                    || Dungeon.level.solid[cell] || charAt(cell) != null) continue;
            return cell;
        }
        return originalCell;
    }

    private void moveBossTo(int cell) {
        if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()
                || cell == pos) return;
        pos = cell;
        occupyBossCell();
        if (sprite != null) sprite.place(pos);
    }

    protected void occupyBossCell() {
        Dungeon.level.occupyCell(this);
    }

    protected Char charAt(int cell) {
        return Actor.findChar(cell);
    }

    protected void startLeapPush(Char target, Ballistica trajectory, int power,
                                 boolean closeDoors, boolean collisionDamage,
                                 WandOfBlastWave.KnockbackCallback callback) {
        WandOfBlastWave.throwCharImmediatelyWithResult(target, trajectory, power,
                closeDoors, collisionDamage, this, callback);
    }

    protected void pushNormalMeleeTarget(Char target) {
        pushOneCell(target);
    }

    protected void pushBombardmentTarget(Char target) {
        pushOneCell(target);
    }

    @Override
    protected boolean getCloser(int target) {
        if (Dungeon.hero != null && target == Dungeon.hero.pos) {
            target = pressureTargetForHero(target);
        }
        return super.getCloser(target);
    }

    private int pressureTargetForHero(int heroCell) {
        if (Dungeon.level == null || heroCell < 0
                || heroCell >= Dungeon.level.length()) return heroCell;
        int best = heroCell;
        int bestWalls = -1;
        int bestDistance = Integer.MAX_VALUE;
        for (int offset : PathFinder.NEIGHBOURS8) {
            int candidate = heroCell + offset;
            if (candidate < 0 || candidate >= Dungeon.level.length()
                    || !Dungeon.level.adjacent(heroCell, candidate)
                    || !Dungeon.level.passable[candidate]
                    || Dungeon.level.solid[candidate]
                    || Actor.findChar(candidate) != null) continue;
            int walls = adjacentWallCount(candidate);
            int distance = Dungeon.level.distance(pos, candidate);
            if (walls > bestWalls || (walls == bestWalls && distance < bestDistance)
                    || (walls == bestWalls && distance == bestDistance && candidate < best)) {
                best = candidate;
                bestWalls = walls;
                bestDistance = distance;
            }
        }
        return best;
    }

    private int adjacentWallCount(int cell) {
        int walls = 0;
        for (int offset : PathFinder.NEIGHBOURS8) {
            int neighbor = cell + offset;
            if (neighbor < 0 || neighbor >= Dungeon.level.length()
                    || !Dungeon.level.adjacent(cell, neighbor)
                    || Dungeon.level.solid[neighbor]) walls++;
        }
        return walls;
    }

    private void pushOneCell(Char target) {
        if (Dungeon.level == null || target == null) return;
        int width = Dungeon.level.width();
        int dx = Integer.compare(target.pos % width, pos % width);
        int dy = Integer.compare(target.pos / width, pos / width);
        int x = target.pos % width + dx;
        int y = target.pos / width + dy;
        if (x < 0 || x >= width || y < 0 || y >= Dungeon.level.height()) return;
        WandOfBlastWave.throwChar(target,
                new Ballistica(target.pos, x + y * width, Ballistica.MAGIC_BOLT),
                1, false, false, this);
    }

    private static boolean containsTag(DamageTag[] tags, DamageTag expected) {
        if (tags == null) return false;
        for (DamageTag tag : tags) if (tag == expected) return true;
        return false;
    }

    @Override
    public void die(Object cause) {
        if (!rewardDropped) {
            if (sprite != null) yell(Messages.get(this, "defeated"));
            rewardDropped = true;
            dropBossRewards();
        }
        clearPendingSkill();
        waitingForLeapPush = false;
        super.die(cause);
    }

    private void dropBossRewards() {
        if (Dungeon.level == null) return;
        Dungeon.level.drop(new StoneOfEnchantment(), pos).sprite.drop(pos);
        Dungeon.level.drop(new StoneOfAugmentation(), pos).sprite.drop(pos);
        Dungeon.level.drop(TowerBossRewardGenerator.createReward(
                Dungeon.seed, Dungeon.depth, towerBossId()), pos).sprite.drop(pos);
    }

    private void damageWithBombardment(Char target, int raw) {
        target.damage(raw, this,
                DamageTag.PHYSICAL, DamageTag.RANGED, DamageTag.NO_ARMOR);
    }

    protected void applySkillAftermath(Skill skill, Char target,
                                     DeathKnightBombardment.Band band) {
        if (skill == Skill.LINE) pushBombardmentTarget(target);
        if (skill == Skill.CONE) {
            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
                    target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class, 2f);
        } else if (skill == Skill.RING
                || (skill == Skill.EXECUTION && band == DeathKnightBombardment.Band.CORE)) {
            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
                    target, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding.class).set(8f);
        }
    }

    @Override
    public void notice() {
        super.notice();
        if (!noticeAnnounced) {
            noticeAnnounced = true;
            yell(Messages.get(this, "notice"));
        }
    }

    private void pausePending() {
        if (pendingSkill != Skill.NONE) pendingPaused = true;
    }

    private boolean rearmPending() {
        if (!pendingPaused) return false;
        pendingPaused = false;
        showPendingTelegraph();
        if (sprite != null) yell(Messages.get(this, "rearm"));
        spend(TICK);
        return true;
    }

    private boolean consumeRestoreGrace() {
        if (!restoreGrace) return false;
        restoreGrace = false;
        showPendingTelegraph();
        spend(TICK);
        return true;
    }

    private void announceSkill(Skill skill) {
        if (sprite != null) yell(Messages.get(this, skill.name().toLowerCase()));
    }

    private void showPendingTelegraph() {
        if (sprite == null || sprite.parent == null || Dungeon.level == null) return;
        for (int i = 0; i < pendingCells.length; i++) {
            int cell = pendingCells[i];
            if (cell >= 0 && cell < Dungeon.level.length()) {
                DeathKnightBombardment.Band band = i < pendingBands.length
                        ? pendingBands[i] : DeathKnightBombardment.Band.NONE;
                sprite.parent.addToBack(new TargetedCell(cell,
                        telegraphColor(pendingSkill, band)));
            }
        }
        if (pendingLandingCell >= 0 && pendingLandingCell < Dungeon.level.length()) {
            sprite.parent.addToBack(new TargetedCell(pendingLandingCell, 0xE8E8E8));
        }
    }

    private static int telegraphColor(Skill skill, DeathKnightBombardment.Band band) {
        if (skill == Skill.EXECUTION) {
            switch (band) {
                case OUTER:
                    return 0xE8C84A;
                case INNER:
                    return 0xE98232;
                case CORE:
                    return 0xD83C32;
                default:
                    break;
            }
        }
        return 0xD34B3F;
    }

    private DeathKnightBombardment.Band bandAt(int cell) {
        for (int i = 0; i < pendingCells.length; i++) {
            if (pendingCells[i] == cell) return pendingBands[i];
        }
        return DeathKnightBombardment.Band.NONE;
    }

    private static boolean contains(int[] cells, int cell) {
        for (int value : cells) if (value == cell) return true;
        return false;
    }

    private static DeathKnightBombardment.Band[] normalizedBands(
            DeathKnightBombardment.Band[] source, int length) {
        DeathKnightBombardment.Band[] result = new DeathKnightBombardment.Band[length];
        for (int i = 0; i < length; i++) {
            result[i] = source != null && i < source.length && source[i] != null
                    ? source[i] : DeathKnightBombardment.Band.NONE;
        }
        return result;
    }

    private int[] damageRange(Skill skill) {
        switch (skill) {
            case LINE:
                return phase == Phase.QUESTION ? new int[]{45, 60}
                        : phase == Phase.BREAK_FORMATION ? new int[]{55, 70}
                        : new int[]{70, 90};
            case CONE:
                return phase == Phase.QUESTION ? new int[]{40, 55}
                        : phase == Phase.BREAK_FORMATION ? new int[]{50, 65}
                        : new int[]{65, 85};
            case CROSS:
                return phase == Phase.DEATH_DUEL ? new int[]{65, 85} : new int[]{55, 75};
            case RING:
                return phase == Phase.DEATH_DUEL ? new int[]{60, 80} : new int[]{50, 70};
            case SOUL_LINE:
                return phase == Phase.DEATH_DUEL ? new int[]{75, 95} : new int[]{60, 80};
            case EXECUTION:
                int base = phase == Phase.DEATH_DUEL ? 90 : 70;
                return new int[]{base, base};
            default:
                return new int[]{0, 0};
        }
    }

    private static int telegraphTurns(Skill skill) {
        return skill == Skill.EXECUTION ? 2 : 1;
    }

    public Phase phase() {
        return phase;
    }

    int damageRollMinForTest() {
        return 28;
    }

    int damageRollMaxForTest() {
        return 42;
    }

    int drRollMinForTest() {
        return phase == Phase.DEATH_DUEL ? 0 : 15;
    }

    int drRollMaxForTest() {
        return phase == Phase.DEATH_DUEL ? 0 : 30;
    }

    int capFinalDamageForTest(int damage) {
        return modifyFinalDamage(damage, null);
    }

    PhaseTransition transitionForTest() {
        return transition;
    }

    void armTransitionForTest() {
        transition = PhaseTransition.ARMED;
    }

    boolean advanceTransitionForTest() {
        return advanceTransition();
    }

    void setPendingForTest(Skill skill, int[] cells, int turns) {
        pendingSkill = skill;
        pendingCells = cells == null ? new int[0] : cells.clone();
        pendingBands = new DeathKnightBombardment.Band[pendingCells.length];
        for (int i = 0; i < pendingBands.length; i++) {
            pendingBands[i] = DeathKnightBombardment.Band.NONE;
        }
        pendingTurns = turns;
    }

    Skill pendingSkillForTest() {
        return pendingSkill;
    }

    float cooldownForTest() {
        return cooldown();
    }

    int phaseLocksForTest() {
        return phaseLocks;
    }

    Skill nextSkillForTest() {
        return nextSkill();
    }

    void commitAndResolveForTest(Skill skill) {
        pendingSkill = skill;
        pendingTurns = telegraphTurns(skill);
        finishBombardment(skill);
    }

    boolean bombardmentReadyForTest() {
        return bombardmentReady();
    }

    boolean actForTest() {
        return act();
    }

    int normalActionsForTest() {
        return normalActions;
    }

    void finishNormalActionForTest() {
        finishNormalAction();
    }

    void setMainBombardmentsForTest(int value) {
        mainBombardments = Math.max(0, value);
        lastExecution = false;
    }

    int mainBombardmentsForTest() {
        return mainBombardments;
    }

    int skillIndexForTest() {
        return skillIndex;
    }

    void forcePhaseForTest(Phase value, int locks) {
        phase = value;
        phaseLocks = locks;
    }

    int[] damageRangeForTest(Skill skill) {
        return damageRange(skill);
    }

    void damageWithBombardmentForTest(Char target, int raw) {
        damageWithBombardment(target, raw);
    }

    int telegraphTurnsForTest(Skill skill) {
        return telegraphTurns(skill);
    }

    void setEnemyForTest(Char target) {
        enemy = target;
    }

    Char bombardmentTargetForTest() {
        return bombardmentTarget();
    }

    boolean canUseBombardmentForTest(Char target) {
        return canUseBombardment(target);
    }

    DeathKnightBombardment.Plan createPlanForTest(Skill skill, Char target) {
        return createPlan(skill, target);
    }

    boolean telegraphForTest(Skill skill, int[] cells,
                             DeathKnightBombardment.Band[] bands, int landingCell) {
        return telegraph(skill, cells, bands, landingCell);
    }

    boolean advancePendingForTest() {
        return advancePending();
    }

    int pendingTurnsForTest() {
        return pendingTurns;
    }

    int[] pendingCellsForTest() {
        return pendingCells.clone();
    }

    void pausePendingForTest() {
        pausePending();
    }

    boolean pendingPausedForTest() {
        return pendingPaused;
    }

    boolean rearmPendingForTest() {
        return rearmPending();
    }

    boolean restoreGraceForTest() {
        return restoreGrace;
    }

    boolean consumeRestoreGraceForTest() {
        return consumeRestoreGrace();
    }

    boolean beginLeapForTest(int whiteCell) {
        return beginLeapResolution(whiteCell);
    }

    void normalMeleeResolvedForTest(Char target, boolean hit, DamageTag... tags) {
        onAttackResolved(target, hit, 0, tags);
    }

    void applySkillAftermathForTest(Skill skill, Char target,
                                    DeathKnightBombardment.Band band) {
        applySkillAftermath(skill, target, band);
    }

    int pressureTargetForTest(int heroCell) {
        return pressureTargetForHero(heroCell);
    }

    int telegraphColorForTest(Skill skill, DeathKnightBombardment.Band band) {
        return telegraphColor(skill, band);
    }

    boolean rewardDroppedForTest() {
        return rewardDropped;
    }

    void markRewardDroppedForTest() {
        rewardDropped = true;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, phase.ordinal());
        bundle.put(PHASE_LOCKS, phaseLocks);
        bundle.put(TRANSITION, transition.ordinal());
        bundle.put(PENDING_SKILL, pendingSkill.ordinal());
        bundle.put(PENDING_CELLS, pendingCells);
        bundle.put(PENDING_TURNS, pendingTurns);
        bundle.put(SKILL_INDEX, skillIndex);
        bundle.put(NORMAL_ACTIONS, normalActions);
        bundle.put(NORMAL_ACTIONS_REQUIRED, normalActionsRequired);
        bundle.put(MAIN_BOMBARDMENTS, mainBombardments);
        bundle.put(LAST_EXECUTION, lastExecution);
        int[] bandOrdinals = new int[pendingBands.length];
        for (int i = 0; i < pendingBands.length; i++) bandOrdinals[i] = pendingBands[i].ordinal();
        bundle.put(PENDING_BANDS, bandOrdinals);
        bundle.put(PENDING_LANDING_CELL, pendingLandingCell);
        bundle.put(PENDING_PAUSED, pendingPaused);
        bundle.put(REWARD_DROPPED, rewardDropped);
        bundle.put(NOTICE_ANNOUNCED, noticeAnnounced);
        bundle.put(LANDED_MELEE_ATTACKS, landedMeleeAttacks);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase = enumAt(Phase.values(), bundle.getInt(PHASE), Phase.QUESTION);
        phaseLocks = Math.max(0, Math.min(3, bundle.getInt(PHASE_LOCKS)));
        transition = enumAt(PhaseTransition.values(), bundle.getInt(TRANSITION),
                PhaseTransition.NONE);
        pendingSkill = enumAt(Skill.values(), bundle.getInt(PENDING_SKILL), Skill.NONE);
        pendingCells = bundle.getIntArray(PENDING_CELLS);
        if (pendingCells == null) pendingCells = new int[0];
        pendingTurns = pendingSkill == Skill.NONE ? 0
                : Math.max(1, Math.min(telegraphTurns(pendingSkill), bundle.getInt(PENDING_TURNS)));
        skillIndex = Math.max(0, bundle.getInt(SKILL_INDEX));
        normalActions = Math.max(0, bundle.getInt(NORMAL_ACTIONS));
        normalActionsRequired = Math.max(0, Math.min(3,
                bundle.getInt(NORMAL_ACTIONS_REQUIRED)));
        normalActions = Math.min(normalActions, normalActionsRequired);
        mainBombardments = Math.max(0, bundle.getInt(MAIN_BOMBARDMENTS));
        lastExecution = bundle.getBoolean(LAST_EXECUTION);
        int[] bandOrdinals = bundle.getIntArray(PENDING_BANDS);
        ArrayList<Integer> validCells = new ArrayList<>();
        ArrayList<DeathKnightBombardment.Band> validBands = new ArrayList<>();
        for (int i = 0; i < pendingCells.length; i++) {
            int cell = pendingCells[i];
            if (cell < 0 || (Dungeon.level != null && cell >= Dungeon.level.length())) continue;
            validCells.add(cell);
            int ordinal = i < bandOrdinals.length ? bandOrdinals[i] : 0;
            validBands.add(enumAt(DeathKnightBombardment.Band.values(), ordinal,
                    DeathKnightBombardment.Band.NONE));
        }
        pendingCells = new int[validCells.size()];
        pendingBands = new DeathKnightBombardment.Band[validBands.size()];
        for (int i = 0; i < validCells.size(); i++) {
            pendingCells[i] = validCells.get(i);
            pendingBands[i] = validBands.get(i);
        }
        pendingLandingCell = bundle.contains(PENDING_LANDING_CELL)
                ? bundle.getInt(PENDING_LANDING_CELL) : -1;
        if (pendingLandingCell < -1 || (Dungeon.level != null
                && pendingLandingCell >= Dungeon.level.length())) pendingLandingCell = -1;
        pendingPaused = bundle.getBoolean(PENDING_PAUSED);
        rewardDropped = bundle.getBoolean(REWARD_DROPPED);
        noticeAnnounced = bundle.getBoolean(NOTICE_ANNOUNCED);
        landedMeleeAttacks = Math.floorMod(bundle.getInt(LANDED_MELEE_ATTACKS), 2);
        if (pendingSkill == Skill.NONE || pendingCells.length == 0) clearPendingSkill();
        else restoreGrace = true;
    }

    private static <T> T enumAt(T[] values, int index, T fallback) {
        return index >= 0 && index < values.length ? values[index] : fallback;
    }
}
