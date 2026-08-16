package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.IncubatingMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.OutbreakMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.PaleMiasma;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss.PurifyingIncense;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Infection;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.PostPlagueFatigue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.TerminalHealingPenalty;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MagicalRangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.PestilenceArenaController;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossRewardGenerator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.PestilenceKnightSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** Three-stage plague-doctor boss used by {@link TowerBossLevel}. */
public class PestilenceKnight extends TowerBoss implements MagicalRangedAttack {

    public enum Phase { INCUBATION, OUTBREAK, TERMINAL }
    public enum HarvestState { NONE, ARMED, CHANNELING }

    public static final int KNOCKBACK_DISTANCE = 1;
    public static final int FINAL_DAMAGE_CAP = 150;
    private static final int PLAGUE_FLASK_IMPACT_COLOR = 0x9EAD48;
    private static final int OUTBREAK_MIASMA_IMPACT_COLOR = 0x63D13F;
    private static final int PALE_MIASMA_IMPACT_COLOR = 0xC8C3E8;
    private static final int PURPLE_PRESCRIPTION_COLOR = 0x9A55D6;

    static final String PLAGUE_FLASK = "plague_flask";
    static final String QUARANTINE = "quarantine";
    static final String PALE_CHARGE = "pale_charge";
    static final String DOOM_PROCESSION = "doom_procession";

    private static final int FLASK_CD = 0;
    private static final int QUARANTINE_CD = 1;
    private static final int PRESCRIPTION_CD = 2;
    private static final int PALE_CHARGE_CD = 3;
    private static final int BRAZIER_TUTORIAL_FLAG = 1 << 8;

    private static final String GROWTH = "growth";
    private static final String PHASE = "phase";
    private static final String PHASE_LOCKS = "phase_locks";
    private static final String HARVEST = "harvest";
    private static final String PENDING_SKILL = "pending_skill";
    private static final String PENDING_TURNS = "pending_turns";
    private static final String PENDING_PROJECTILE_TARGET = "pending_projectile_target";
    private static final String COOLDOWNS = "cooldowns";
    private static final String PENDING_CELLS = "pending_cells";
    private static final String PRESCRIPTION_INDEX = "prescription_index";
    private static final String DIAGNOSIS = "diagnosis";
    private static final String LAST_HERO_POS = "last_hero_pos";
    private static final String REWARD_DROPPED = "reward_dropped";
    private static final String PROCESSION_STEPS = "procession_steps";
    private static final String TERMINAL_ENTERED = "terminal_entered";

    private int growth;
    private Phase phase = Phase.INCUBATION;
    private int phaseLocks;
    private HarvestState harvest = HarvestState.NONE;
    private String pendingSkill = "";
    private int pendingTurns;
    private int pendingProjectileTarget = -1;
    private int[] cooldowns = new int[4];
    private int[] pendingCells = new int[0];
    private int prescriptionIndex;
    private int diagnosis;
    private int lastHeroPos = -1;
    private boolean rewardDropped;
    private int processionSteps;
    private boolean terminalEntered;
    private transient boolean waitingForAttackCompletion;
    private transient boolean restoringFromBundle;
    private transient Boolean forcedTenacityRoll;
    private transient int plagueFlaskVisualIndex;

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
        spriteClass = PestilenceKnightSprite.class;
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
    public int rangedAttackBallisticaMode() {
        return Ballistica.PROJECTILE;
    }

    @Override
    public boolean canRangedAttack(Char target) {
        return target != null && Dungeon.level != null
                && phase == Phase.OUTBREAK && cooldowns[PRESCRIPTION_CD] == 0
                && target.invisible <= 0
                && Dungeon.level.distance(pos, target.pos) <= viewDistance
                && MagicalRangedAttack.super.canRangedAttack(target);
    }

    @Override
    public boolean doRangedAttack(final Char target) {
        final int prescription = prescriptionIndex++ % 3;
        final int potionImage = prescriptionPotionImage(Random.Int(12));
        announceSkill(prescriptionAnnouncementKey(prescription));
        enemy = target;

        if (sprite instanceof PestilenceKnightSprite && sprite.parent != null
                && (sprite.visible || target.sprite != null && target.sprite.visible)) {
            ((PestilenceKnightSprite) sprite).throwPrescription(target.pos, potionImage,
                    new Callback() {
                        @Override
                        public void call() {
                            resolvePrescriptionAttack(target, prescription);
                            next();
                        }
                    });
            return false;
        }

        resolvePrescriptionAttack(target, prescription);
        return true;
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
        return phase == Phase.TERMINAL ? 2f : 1f;
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
        int adjusted = super.modifyFinalDamage(damage, source, tags);
        boolean standingInOutbreak = Dungeon.level != null && pos >= 0
                && Blob.volumeAt(pos, OutbreakMiasma.class) > 0;
        adjusted = applyOutbreakReduction(adjusted, source, standingInOutbreak);
        int capped = Math.min(FINAL_DAMAGE_CAP, adjusted);
        int lock = nextUnfinishedLockHP();
        if (lock < 0) return capped;
        int untilLock = Math.max(0, HP - lock);
        if (capped >= untilLock) {
            harvest = HarvestState.ARMED;
            return untilLock;
        }
        return capped;
    }

    private int applyOutbreakReduction(int damage, Object source, boolean standingInOutbreak) {
        if (phase == Phase.OUTBREAK && standingInOutbreak
                && !(source instanceof PestilenceArenaController)) {
            return Math.round(damage * 0.8f);
        }
        return damage;
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return harvest != HarvestState.NONE || super.isInvulnerable(effect);
    }

    @Override
    protected boolean act() {
        if (state == FLEEING && !pendingSkill.isEmpty()) cancelPendingSkillForFleeing();
        if (harvest != HarvestState.NONE && paralysed <= 0 && state != SLEEPING) {
            advanceHarvest(currentHarvestBlobCells());
            spend(TICK);
            finishBossAction();
            return true;
        }
        if (paralysed <= 0 && state != SLEEPING) {
            if (shouldUsePhaseAI(!pendingSkill.isEmpty(), canUsePhaseSkills())) return actPhaseAI();
            resetDiagnosisStreak();
        }
        boolean completed = super.act();
        if (completed) finishBossAction();
        else waitingForAttackCompletion = true;
        return completed;
    }

    @Override
    public void onAttackComplete() {
        super.onAttackComplete();
        if (waitingForAttackCompletion) {
            waitingForAttackCompletion = false;
            finishBossAction();
        }
    }

    private boolean canUsePhaseSkills() {
        if (!phaseSkillsAllowedForState(state == FLEEING)
                || Dungeon.level == null || Dungeon.hero == null || !Dungeon.hero.isAlive()
                || Dungeon.level.distance(pos, Dungeon.hero.pos) > viewDistance) return false;
        if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
            fieldOfView = new boolean[Dungeon.level.length()];
        }
        Dungeon.level.updateFieldOfView(this, fieldOfView);
        return fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible <= 0;
    }

    private static boolean phaseSkillsAllowedForState(boolean fleeing) {
        return !fleeing;
    }

    private static boolean shouldUsePhaseAI(boolean hasPendingSkill, boolean canUsePhaseSkills) {
        return hasPendingSkill || canUsePhaseSkills;
    }

    private void cancelPendingSkillForFleeing() {
        pendingSkill = "";
        pendingTurns = 0;
        pendingProjectileTarget = -1;
        pendingCells = new int[0];
        processionSteps = 0;
    }

    private boolean actPhaseAI() {
        if (!pendingSkill.isEmpty() && advancePendingCountdown()) {
            showTelegraph(pendingCells, telegraphColor(pendingSkill));
            spend(TICK);
            finishBossAction();
            return true;
        }
        boolean diagnosedBeforeAction = hasFlaskDiagnosisBonus();
        updateDiagnosis();
        if (!pendingSkill.isEmpty()) return resolvePendingSkill(diagnosedBeforeAction);
        switch (phase) {
            case INCUBATION:
                if (cooldowns[FLASK_CD] == 0) {
                    int target = selectPlagueFlaskTarget();
                    return telegraphSkill(PLAGUE_FLASK, squareAround(target), target);
                }
                return maintainRangeOrMelee(4, 6);
            case OUTBREAK:
                PestilenceArenaControllerRef arena = arenaRef();
                if (cooldowns[QUARANTINE_CD] == 0 && arena.readyBrazier(Dungeon.hero.pos) >= 0) {
                    int[] band = quarantineBand(Dungeon.hero.pos, arena.readyBrazier(Dungeon.hero.pos));
                    if (band.length > 0) return telegraphSkill(QUARANTINE, band);
                }
                if (canCastPrescription(Dungeon.hero)) return doRangedAttack(Dungeon.hero);
                return maintainRangeOrMelee(3, 5);
            case TERMINAL:
                ensureTerminalEntered();
                if (cooldowns[PALE_CHARGE_CD] == 0) {
                    return telegraphSkill(PALE_CHARGE, chargeBand(Dungeon.hero.pos));
                }
                if (cooldowns[QUARANTINE_CD] == 0) {
                    processionSteps = 1;
                    return telegraphSkill(DOOM_PROCESSION, processionCells(processionSteps));
                }
                if (cooldowns[PRESCRIPTION_CD] == 0) return castTerminalDiagnosis();
                return maintainRangeOrMelee(2, 4);
            default:
                return completeBaseAction();
        }
    }

    private boolean maintainRangeOrMelee(int min, int max) {
        Hero hero = Dungeon.hero;
        enemy = hero;
        int distance = Dungeon.level.distance(pos, hero.pos);
        int oldPos = pos;
        if (distance < min && distance > 1 && moveTowardPurifierOrFurther(hero.pos)) {
            spend(1f / speed());
            moveSprite(oldPos, pos);
            finishBossAction();
            return true;
        }
        oldPos = pos;
        if (distance > max && getCloser(hero.pos)) {
            spend(1f / speed());
            moveSprite(oldPos, pos);
            finishBossAction();
            return true;
        }
        return completeBaseAction();
    }

    private int selectPlagueFlaskTarget() {
        int heroTarget = predictedHeroCell();
        int purifierTarget = arenaRef().brazier();
        return choosePlagueFlaskTarget(heroTarget, hasProjectileLine(heroTarget),
                purifierTarget, hasProjectileLine(purifierTarget), randomArenaTarget());
    }

    private boolean hasProjectileLine(int target) {
        return Dungeon.level != null && target >= 0 && target < Dungeon.level.length()
                && new Ballistica(pos, target, Ballistica.PROJECTILE).collisionPos == target;
    }

    private int randomArenaTarget() {
        int target = arenaRef().randomTarget();
        return target >= 0 ? target : pos;
    }

    private static int choosePlagueFlaskTarget(int heroTarget, boolean heroBallistic,
            int purifierTarget, boolean purifierBallistic, int randomTarget) {
        if (heroBallistic) return heroTarget;
        if (purifierBallistic) return purifierTarget;
        return randomTarget;
    }

    private boolean moveTowardPurifierOrFurther(int fallbackTarget) {
        int guard = purifierGuardCell();
        if (guard >= 0 && guard != pos && super.getCloser(guard)) return true;
        if (guard == pos) return false;
        return super.getFurther(fallbackTarget);
    }

    @Override
    protected boolean getCloser(int target) {
        if (state == WANDERING) {
            target = preferredMovementTarget(target, purifierGuardCell());
        }
        return super.getCloser(target);
    }

    private static int preferredMovementTarget(int fallbackTarget, int purifierGuard) {
        return purifierGuard >= 0 ? purifierGuard : fallbackTarget;
    }

    @Override
    protected boolean getFurther(int target) {
        if (state == FLEEING) return moveTowardPurifierOrFurther(target);
        return super.getFurther(target);
    }

    private int purifierGuardCell() {
        int purifier = arenaRef().brazier();
        if (Dungeon.level == null || purifier < 0 || purifier >= Dungeon.level.length()) return -1;
        int best = -1;
        int bestDistance = Integer.MAX_VALUE;
        int purifierX = purifier % Dungeon.level.width();
        for (int offset : PathFinder.NEIGHBOURS8) {
            int cell = purifier + offset;
            if (cell < 0 || cell >= Dungeon.level.length()
                    || Math.abs(cell % Dungeon.level.width() - purifierX) > 1
                    || !Dungeon.level.passable[cell]
                    || Actor.findChar(cell) != null && cell != pos) continue;
            int distance = Dungeon.level.distance(pos, cell);
            if (distance < bestDistance) {
                best = cell;
                bestDistance = distance;
            }
        }
        return best;
    }

    private boolean completeBaseAction() {
        boolean completed = super.act();
        if (completed) finishBossAction();
        else waitingForAttackCompletion = true;
        return completed;
    }

    private boolean telegraphSkill(String skill, int[] cells) {
        return telegraphSkill(skill, cells, miasmaProjectileTarget(cells));
    }

    private boolean telegraphSkill(String skill, int[] cells, int projectileTarget) {
        if (cells == null || cells.length == 0) return completeBaseAction();
        pendingSkill = skill;
        pendingCells = cells;
        pendingProjectileTarget = projectileTarget;
        pendingTurns = PLAGUE_FLASK.equals(skill)
                ? plagueFlaskTelegraphTurns(Dungeon.hero == null ? 1f : Dungeon.hero.speed()) : 1;
        announceSkill(skill);
        showTelegraph(cells, telegraphColor(skill));
        if (sprite instanceof PestilenceKnightSprite) ((PestilenceKnightSprite) sprite).cast();
        if (!PLAGUE_FLASK.equals(skill)) launchMiasmaProjectile(skill, projectileTarget);
        spend(TICK);
        finishBossAction();
        return true;
    }

    private void launchMiasmaProjectile(final String skill, final int target) {
        if (Dungeon.level == null || target < 0 || target >= Dungeon.level.length()
                || sprite == null || sprite.parent == null) return;
        int image = miasmaProjectileImage(skill);
        final int impactColor = miasmaProjectileImpactColor(skill);
        if (PLAGUE_FLASK.equals(skill)) image = plagueFlaskImage(plagueFlaskVisualIndex++);
        if (image < 0 || impactColor < 0) return;
        final Level level = Dungeon.level;
        Item flask = new Item();
        flask.image = image;
        ((MissileSprite) sprite.parent.recycle(MissileSprite.class)).reset(sprite, target, flask,
                new Callback() {
                    @Override
                    public void call() {
                        if (Dungeon.level == level) {
                            Splash.at(target, impactColor, 6);
                        }
                    }
                });
    }

    private static int plagueFlaskImage(int index) {
        switch (plagueFlaskPaletteIndex(index)) {
            case 0: return ItemSpriteSheet.POTION_JADE;
            case 1: return ItemSpriteSheet.POTION_GOLDEN;
            default: return ItemSpriteSheet.POTION_BISTRE;
        }
    }

    private static int plagueFlaskPaletteIndex(int index) {
        return Math.floorMod(index, 3);
    }

    private static int prescriptionPotionImage(int index) {
        return ItemSpriteSheet.POTION_CRIMSON + prescriptionPotionPaletteIndex(index);
    }

    private static int prescriptionPotionPaletteIndex(int index) {
        return Math.floorMod(index, 12);
    }

    private static int miasmaProjectileImage(String skill) {
        if (QUARANTINE.equals(skill)) return ItemSpriteSheet.POTION_JADE;
        if (PALE_CHARGE.equals(skill) || DOOM_PROCESSION.equals(skill)) {
            return ItemSpriteSheet.POTION_SILVER;
        }
        return -1;
    }

    private static int miasmaProjectileImpactColor(String skill) {
        if (PLAGUE_FLASK.equals(skill)) return PLAGUE_FLASK_IMPACT_COLOR;
        if (QUARANTINE.equals(skill)) return OUTBREAK_MIASMA_IMPACT_COLOR;
        if (PALE_CHARGE.equals(skill) || DOOM_PROCESSION.equals(skill)) {
            return PALE_MIASMA_IMPACT_COLOR;
        }
        return -1;
    }

    private static int miasmaProjectileTarget(int[] cells) {
        if (cells == null || cells.length == 0) return -1;
        int middle = cells.length / 2;
        for (int offset = 0; offset < cells.length; offset++) {
            int right = middle + offset;
            if (right < cells.length && cells[right] >= 0) return cells[right];
            int left = middle - offset - 1;
            if (left >= 0 && cells[left] >= 0) return cells[left];
        }
        return -1;
    }

    private boolean resolvePendingSkill(boolean diagnosedBeforeAction) {
        String skill = pendingSkill;
        int[] cells = pendingCells;
        int projectileTarget = pendingProjectileTarget;
        pendingSkill = "";
        pendingTurns = 0;
        pendingProjectileTarget = -1;
        pendingCells = new int[0];
        if (PLAGUE_FLASK.equals(skill)) {
            launchMiasmaProjectile(skill, projectileTarget);
            hitHeroInCells(cells, 12, 20, diagnosedBeforeAction);
            seedCells(cells, miasmaAmountForSkill(skill), IncubatingMiasma.class, false);
            cooldowns[FLASK_CD] = plagueFlaskCooldown(
                    Dungeon.hero == null ? 1f : Dungeon.hero.speed());
        } else if (QUARANTINE.equals(skill)) {
            seedCells(cells, miasmaAmountForSkill(skill), OutbreakMiasma.class, false);
            cooldowns[QUARANTINE_CD] = 7;
        } else if (PALE_CHARGE.equals(skill)) {
            hitHeroInCells(cells, 30, 45, false);
            knockHeroIfInCells(cells, 2);
            seedCells(cells, miasmaAmountForSkill(skill), PaleMiasma.class, true);
            cooldowns[PALE_CHARGE_CD] = 5;
        } else if (DOOM_PROCESSION.equals(skill)) {
            seedCells(cells, miasmaAmountForSkill(skill), PaleMiasma.class, false);
            if (processionSteps < 3) {
                processionSteps++;
                pendingSkill = DOOM_PROCESSION;
                pendingTurns = 1;
                pendingCells = processionCells(processionSteps);
                pendingProjectileTarget = miasmaProjectileTarget(pendingCells);
                showTelegraph(pendingCells, 0x6B557C);
                launchMiasmaProjectile(DOOM_PROCESSION,
                        pendingProjectileTarget);
            } else {
                processionSteps = 0;
                cooldowns[QUARANTINE_CD] = 8;
            }
        }
        spend(TICK);
        finishBossAction();
        return true;
    }

    static int miasmaAmountForSkill(String skill) {
        if (PLAGUE_FLASK.equals(skill)) return 60;
        if (QUARANTINE.equals(skill) || PALE_CHARGE.equals(skill)) return 80;
        if (DOOM_PROCESSION.equals(skill)) return 70;
        return 0;
    }

    private static int plagueFlaskCooldown(float heroSpeed) {
        if (!(heroSpeed > 1f)) return 5;
        if (heroSpeed >= 10f) return 1;
        return 5 - (int) Math.floor((heroSpeed - 1f) * 4f / 9f);
    }

    private static int plagueFlaskTelegraphTurns(float heroSpeed) {
        return heroSpeed > 2f ? 1 : 2;
    }

    private boolean advancePendingCountdown() {
        if (pendingTurns <= 1) return false;
        pendingTurns--;
        return true;
    }

    private static int telegraphColor(String skill) {
        if (PALE_CHARGE.equals(skill)) return 0xD8D8D8;
        if (DOOM_PROCESSION.equals(skill)) return 0x6B557C;
        return 0x88AA33;
    }

    private boolean canCastPrescription(Char target) {
        return !soulMarkBlocksRangedAttack(target) && canRangedAttack(target);
    }

    private void resolvePrescriptionAttack(Char target, int prescription) {
        spend(TICK);
        Invisibility.dispel(this);
        if (target != null && target.isAlive()) {
            if (rangedHit(target)) {
                applyPrescription(target, prescription);
            } else {
                showRangedMiss(target);
            }
        }
        cooldowns[PRESCRIPTION_CD] = 5;
        finishBossAction();
    }

    private void applyPrescription(Char target, int index) {
        switch (index % 3) {
            case 0:
                dealPrescriptionDamage(target, 10, 16);
                Buff.prolong(target, Weakness.class, 4f);
                Infection.addStacks(target, 1);
                break;
            case 1:
                dealPrescriptionDamage(target, 10, 16);
                Buff.affect(target, Bleeding.class).set(6f);
                break;
            default:
                showPurplePrescription(target);
                dealPrescriptionDamage(target, 8, 14);
                Buff.prolong(target, Vertigo.class, 3f);
                break;
        }
    }

    private void showPurplePrescription(Char target) {
        if (target == null) return;
        if (target.sprite != null && target.sprite.visible) {
            Splash.at(target.pos, PURPLE_PRESCRIPTION_COLOR, 10);
            target.sprite.emitter().burst(ShadowParticle.CURSE, 6);
        }
    }

    private void dealPrescriptionDamage(Char target, int min, int max) {
        target.damage(Random.NormalIntRange(
                Math.round(min * activeDamageMultiplier()),
                Math.round(max * activeDamageMultiplier())), this,
                DamageTag.MAGICAL, DamageTag.RANGED);
    }

    private boolean castTerminalDiagnosis() {
        int stacks = Infection.stacks(Dungeon.hero);
        announceSkill(diagnosisAnnouncementKey(stacks));
        applyTerminalDiagnosis(Dungeon.hero, stacks);
        cooldowns[PRESCRIPTION_CD] = 6;
        spend(TICK);
        finishBossAction();
        return true;
    }

    private void applyTerminalDiagnosis(Char target, int stacks) {
        if (stacks <= 1) {
            Buff.affect(target, Poison.class).set(4f);
        } else if (stacks <= 3) {
            Buff.prolong(target, Weakness.class, 4f);
            Buff.prolong(target, Vulnerable.class, 4f);
        } else {
            dealSkillDamage(target, 25, 40);
            TerminalHealingPenalty.set(target, 0.25f, 3f);
        }
    }

    private void dealSkillDamage(Char target, int min, int max) {
        target.damage(Random.NormalIntRange(
                Math.round(min * activeDamageMultiplier()),
                Math.round(max * activeDamageMultiplier())), this);
    }

    private void hitHeroInCells(int[] cells, int min, int max, boolean extraInfection) {
        if (Dungeon.hero == null || !contains(cells, Dungeon.hero.pos)) return;
        Dungeon.hero.damage(Random.NormalIntRange(
                Math.round(min * activeDamageMultiplier()),
                Math.round(max * activeDamageMultiplier())), this);
        Infection.addStacks(Dungeon.hero, extraInfection ? 2 : 1);
    }

    private void knockHeroIfInCells(int[] cells, int distance) {
        if (Dungeon.hero == null || !contains(cells, Dungeon.hero.pos)) return;
        Ballistica trajectory = new Ballistica(pos, Dungeon.hero.pos, Ballistica.MAGIC_BOLT);
        WandOfBlastWave.throwChar(Dungeon.hero, trajectory, distance, false, false, this);
        Dungeon.hero.interrupt();
    }

    private static boolean contains(int[] cells, int cell) {
        for (int value : cells) if (value == cell) return true;
        return false;
    }

    private void seedCells(int[] cells, int amount, Class<? extends Blob> type, boolean skipCenters) {
        if (Dungeon.level == null) return;
        for (int i = 0; i < cells.length; i++) {
            if (skipCenters && i % 3 == 0) continue;
            int cell = cells[i];
            if (!validEffectCell(cell) || Blob.volumeAt(cell, PurifyingIncense.class) > 0) continue;
            GameScene.add(Blob.seed(cell, amount, type));
        }
    }

    private boolean validEffectCell(int cell) {
        return cell >= 0 && cell < Dungeon.level.length()
                && !Dungeon.level.solid[cell]
                && Dungeon.level.getTransition(cell) == null
                && !arenaRef().isBrazier(cell);
    }

    private int predictedHeroCell() {
        int current = Dungeon.hero.pos;
        if (!hasFlaskDiagnosisBonus() || lastHeroPos < 0 || Dungeon.level == null) return current;
        int width = Dungeon.level.width();
        int dx = Integer.compare(current % width, lastHeroPos % width);
        int dy = Integer.compare(current / width, lastHeroPos / width);
        int predicted = current + dx + dy * width;
        return validEffectCell(predicted) ? predicted : current;
    }

    private int[] squareAround(int center) {
        ArrayList<Integer> result = new ArrayList<>(9);
        for (int offset : PathFinder.NEIGHBOURS9) {
            int cell = center + offset;
            if (validEffectCell(cell)) result.add(cell);
        }
        return toArray(result);
    }

    private int[] quarantineBand(int from, int to) {
        PathFinder.Path path = PathFinder.find(from, to, Dungeon.level.passable);
        if (path == null || path.isEmpty()) return new int[0];
        ArrayList<Integer> cells = new ArrayList<>();
        int width = Dungeon.level.width();
        int previous = from;
        for (int center : path) {
            int delta = center - previous;
            int side = Math.abs(delta) == width ? 1 : width;
            addIfValid(cells, center);
            addIfValid(cells, center - side);
            addIfValid(cells, center + side);
            previous = center;
        }
        return toArray(cells);
    }

    private int[] chargeBand(int target) {
        Ballistica line = new Ballistica(pos, target, Ballistica.STOP_TARGET);
        ArrayList<Integer> cells = new ArrayList<>();
        int width = Dungeon.level.width();
        int previous = pos;
        for (int center : line.subPath(1, line.dist)) {
            int delta = center - previous;
            int side = Math.abs(delta) == width ? 1 : width;
            // The center is first in every group so it can be excluded from Pale Miasma.
            cells.add(center);
            cells.add(chargeSideCell(validEffectCell(center - side), center - side));
            cells.add(chargeSideCell(validEffectCell(center + side), center + side));
            previous = center;
        }
        return toArray(cells);
    }

    private int[] processionCells(int step) {
        ArrayList<Integer> cells = new ArrayList<>();
        int width = Dungeon.level.width();
        int y = Dungeon.hero.pos / width;
        for (int row = Math.max(1, y - 1); row <= Math.min(Dungeon.level.height() - 2, y + 1); row++) {
            addIfValid(cells, row * width + step);
            addIfValid(cells, row * width + width - 1 - step);
        }
        return toArray(cells);
    }

    private void addIfValid(ArrayList<Integer> cells, int cell) {
        if (validEffectCell(cell) && !Dungeon.level.water[cell]
                && Blob.volumeAt(cell, PurifyingIncense.class) == 0 && !cells.contains(cell)) {
            cells.add(cell);
        }
    }

    private static int[] toArray(ArrayList<Integer> cells) {
        int[] result = new int[cells.size()];
        for (int i = 0; i < cells.size(); i++) result[i] = cells.get(i);
        return result;
    }

    private void showTelegraph(int[] cells, int color) {
        if (sprite == null || sprite.parent == null) return;
        for (int cell : cells) {
            if (Dungeon.level != null && cell >= 0 && cell < Dungeon.level.length()) {
                sprite.parent.addToBack(new TargetedCell(cell, color));
            }
        }
    }

    private void updateDiagnosis() {
        Hero hero = Dungeon.hero;
        if (hero == null) return;
        int count = diagnosis & 0xFF;
        count = Math.min(2, count + 1);
        diagnosis = (diagnosis & ~0xFF) | count;
        if ((diagnosis & BRAZIER_TUTORIAL_FLAG) == 0) {
            int brazier = arenaRef().readyBrazier(hero.pos);
            if (brazier >= 0) {
                diagnosis |= BRAZIER_TUTORIAL_FLAG;
                showTelegraph(new int[]{brazier}, 0xFFD35A);
            }
        }
    }

    private void resetDiagnosisStreak() {
        diagnosis &= ~0xFF;
    }

    private void finishBossAction() {
        for (int i = 0; i < cooldowns.length; i++) if (cooldowns[i] > 0) cooldowns[i]--;
        if (Dungeon.hero != null) lastHeroPos = Dungeon.hero.pos;
    }

    private PestilenceArenaControllerRef arenaRef() {
        return new PestilenceArenaControllerRef();
    }

    private final class PestilenceArenaControllerRef {
        private com.shatteredpixel.shatteredpixeldungeon.levels.towers.PestilenceArenaController controller() {
            return Dungeon.level instanceof TowerBossLevel
                    ? ((TowerBossLevel) Dungeon.level).pestilenceArenaController() : null;
        }
        int readyBrazier(int origin) {
            com.shatteredpixel.shatteredpixeldungeon.levels.towers.PestilenceArenaController c = controller();
            return c == null ? -1 : c.nearestReadyBrazier(origin);
        }
        int brazier() {
            com.shatteredpixel.shatteredpixeldungeon.levels.towers.PestilenceArenaController c = controller();
            return c == null ? -1 : c.purifierCell();
        }
        int randomTarget() {
            com.shatteredpixel.shatteredpixeldungeon.levels.towers.PestilenceArenaController c = controller();
            return c == null ? -1 : c.randomArenaCell();
        }
        boolean isBrazier(int cell) {
            com.shatteredpixel.shatteredpixeldungeon.levels.towers.PestilenceArenaController c = controller();
            return c != null && c.isBrazierCell(cell);
        }
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
            announceSkill("harvest");
            if (sprite instanceof PestilenceKnightSprite) {
                ((PestilenceKnightSprite) sprite).harvest();
            }
            return;
        }
        if (harvest != HarvestState.CHANNELING) return;
        int oldHP = HP;
        heal(Math.min(200, Math.max(0, blobCells) * 8));
        int restored = HP - oldHP;
        clearHarvestMiasma();
        if ((phaseLocks & 1) == 0) {
            phaseLocks |= 1;
            phase = Phase.OUTBREAK;
            announceSkill("phase_outbreak", restored);
        } else {
            phaseLocks |= 2;
            phase = Phase.TERMINAL;
            announceSkill("phase_terminal", restored);
            ensureTerminalEntered();
        }
        harvest = HarvestState.NONE;
    }

    private void announceSkill(String key, Object... args) {
        if (sprite != null) yell(Messages.get(this, key, args));
    }

    private static String prescriptionAnnouncementKey(int index) {
        switch (Math.floorMod(index, 3)) {
            case 0: return "prescription_yellow";
            case 1: return "prescription_red";
            default: return "prescription_purple";
        }
    }

    private static String diagnosisAnnouncementKey(int stacks) {
        if (stacks <= 1) return "diagnosis_mild";
        if (stacks <= 3) return "diagnosis_severe";
        return "diagnosis_critical";
    }

    private void clearHarvestMiasma() {
        if (Dungeon.level == null) return;
        Class<? extends Blob> type = phase == Phase.INCUBATION
                ? IncubatingMiasma.class : OutbreakMiasma.class;
        Blob blob = Dungeon.level.blobs.get(type);
        if (blob != null) blob.fullyClear();
    }

    @Override
    public void die(Object cause) {
        if (!rewardDropped) {
            // Commit before constructing or dropping anything so re-entrant death callbacks cannot duplicate loot.
            rewardDropped = true;
            dropBossRewards();
        }
        cleanupPestilenceEncounter();
        super.die(cause);
    }

    private void dropBossRewards() {
        if (Dungeon.level == null) return;
        PotionOfCleansing cleansing = new PotionOfCleansing();
        cleansing.identify(false);
        Dungeon.level.drop(cleansing, pos).sprite.drop(pos);
        Dungeon.level.drop(TowerBossRewardGenerator.createReward(
                Dungeon.seed, Dungeon.depth, towerBossId()), pos).sprite.drop(pos);
    }

    private void cleanupPestilenceEncounter() {
        harvest = HarvestState.NONE;
        pendingSkill = "";
        pendingCells = new int[0];
        processionSteps = 0;
        if (Dungeon.level != null) {
            clearBlob(IncubatingMiasma.class);
            clearBlob(OutbreakMiasma.class);
            clearBlob(PaleMiasma.class);
            clearBlob(PurifyingIncense.class);
        }
        if (Dungeon.hero != null) {
            int stacks = Infection.stacks(Dungeon.hero);
            if (stacks > 0) {
                Buff.prolong(Dungeon.hero, PostPlagueFatigue.class, stacks * 10f);
                Infection.clear(Dungeon.hero);
            }
        }
        if (Dungeon.level instanceof TowerBossLevel) {
            com.shatteredpixel.shatteredpixeldungeon.levels.towers.PestilenceArenaController arena =
                    ((TowerBossLevel) Dungeon.level).pestilenceArenaController();
            if (arena != null) arena.finishEncounter();
        }
    }

    private static void clearBlob(Class<? extends Blob> type) {
        Blob blob = Dungeon.level.blobs.get(type);
        if (blob != null) blob.fullyClear();
    }

    float activeDamageMultiplier() { return 1f + 0.03f * growth; }
    int damageRollMinForTest() { return Math.round(20 * activeDamageMultiplier()); }
    int damageRollMaxForTest() { return Math.round(30 * activeDamageMultiplier()); }
    int drRollMinForTest() { return (phase == Phase.TERMINAL ? 5 : 10) + growth / 2; }
    int drRollMaxForTest() { return (phase == Phase.TERMINAL ? 18 : 25) + growth / 2; }
    int capFinalDamageForTest(int damage) { return modifyFinalDamage(damage, null); }
    int growthForTest() { return growth; }
    Phase phaseForTest() { return phase; }
    public Phase phase() { return phase; }
    public String pendingSkill() { return pendingSkill; }
    HarvestState harvestForTest() { return harvest; }
    void armHarvestForTest() { harvest = HarvestState.ARMED; }
    void advanceHarvestForTest(int blobCells) { advanceHarvest(blobCells); }
    boolean telegraphSkillForTest(String skill, int[] cells) {
        pendingSkill = skill;
        pendingTurns = 1;
        pendingProjectileTarget = miasmaProjectileTarget(cells);
        pendingCells = cells.clone();
        return true;
    }
    boolean telegraphSkillForTest(String skill, int[] cells, float heroSpeed) {
        telegraphSkillForTest(skill, cells);
        pendingTurns = PLAGUE_FLASK.equals(skill) ? plagueFlaskTelegraphTurns(heroSpeed) : 1;
        return true;
    }
    boolean telegraphSkillForTest(String skill, int[] cells, float heroSpeed, int projectileTarget) {
        telegraphSkillForTest(skill, cells, heroSpeed);
        pendingProjectileTarget = projectileTarget;
        return true;
    }
    void resolveSkillForTest(float heroSpeed) {
        if (PLAGUE_FLASK.equals(pendingSkill)) {
            cooldowns[FLASK_CD] = plagueFlaskCooldown(heroSpeed);
        }
        pendingSkill = "";
        pendingTurns = 0;
        pendingProjectileTarget = -1;
        pendingCells = new int[0];
        finishBossAction();
    }
    String pendingSkillForTest() { return pendingSkill; }
    int pendingTurnsForTest() { return pendingTurns; }
    int pendingProjectileTargetForTest() { return pendingProjectileTarget; }
    boolean advancePendingCountdownForTest() { return advancePendingCountdown(); }
    int[] pendingCellsForTest() { return pendingCells.clone(); }
    int skillCooldownForTest(int index) { return cooldowns[index]; }
    void setSkillCooldownForTest(int index, int value) { cooldowns[index] = value; }
    void finishBossActionForTest() { finishBossAction(); }
    void setPhaseForTest(Phase value) { phase = value; if (value == Phase.TERMINAL) ensureTerminalEntered(); }
    int outbreakReductionForTest(int value) { return Math.round(value * 0.8f); }
    int applyOutbreakReductionForTest(int value, Object source, boolean standingInOutbreak) {
        return applyOutbreakReduction(value, source, standingInOutbreak);
    }
    void forceTenacityRollForTest(boolean value) { forcedTenacityRoll = value; }
    boolean acceptNegativeForTest() { return !rollTenacity(); }
    boolean rewardDroppedForTest() { return rewardDropped; }
    void markRewardDroppedForTest() { rewardDropped = true; }
    void setDiagnosisForTest(int value) { diagnosis = value; }
    boolean flaskDiagnosisBonusForTest() { return hasFlaskDiagnosisBonus(); }
    static int chargeSideCellForTest(boolean valid, int candidate) {
        return chargeSideCell(valid, candidate);
    }
    void resetDiagnosisStreakForTest() { resetDiagnosisStreak(); }
    int diagnosisForTest() { return diagnosis; }
    void applyPrescriptionForTest(Char target, int index) { applyPrescription(target, index); }
    void applyTerminalDiagnosisForTest(Char target, int stacks) {
        applyTerminalDiagnosis(target, stacks);
    }
    static String prescriptionAnnouncementKeyForTest(int index) {
        return prescriptionAnnouncementKey(index);
    }
    static String diagnosisAnnouncementKeyForTest(int stacks) {
        return diagnosisAnnouncementKey(stacks);
    }
    static int plagueFlaskPaletteIndexForTest(int index) { return plagueFlaskPaletteIndex(index); }
    static int prescriptionPotionPaletteIndexForTest(int index) {
        return prescriptionPotionPaletteIndex(index);
    }
    static int plagueFlaskCooldownForTest(float heroSpeed) {
        return plagueFlaskCooldown(heroSpeed);
    }
    static int plagueFlaskTelegraphTurnsForTest(float heroSpeed) {
        return plagueFlaskTelegraphTurns(heroSpeed);
    }
    static boolean shouldUsePhaseAIForTest(boolean hasPendingSkill, boolean canUsePhaseSkills) {
        return shouldUsePhaseAI(hasPendingSkill, canUsePhaseSkills);
    }
    static int plagueFlaskImpactColorForTest() { return PLAGUE_FLASK_IMPACT_COLOR; }
    static int miasmaProjectileColorForTest(String skill) {
        return miasmaProjectileImpactColor(skill);
    }
    static int miasmaProjectileTargetForTest(int[] cells) { return miasmaProjectileTarget(cells); }
    static int choosePlagueFlaskTargetForTest(int heroTarget, boolean heroBallistic,
            int purifierTarget, boolean purifierBallistic, int randomTarget) {
        return choosePlagueFlaskTarget(heroTarget, heroBallistic,
                purifierTarget, purifierBallistic, randomTarget);
    }
    static int preferredMovementTargetForTest(int fallbackTarget, int purifierGuard) {
        return preferredMovementTarget(fallbackTarget, purifierGuard);
    }
    static boolean phaseSkillsAllowedForStateForTest(boolean fleeing) {
        return phaseSkillsAllowedForState(fleeing);
    }
    void cancelPendingSkillForFleeingForTest() { cancelPendingSkillForFleeing(); }

    private void ensureTerminalEntered() {
        if (terminalEntered) return;
        terminalEntered = true;
        for (Buff buff : buffs().toArray(new Buff[0])) {
            if (buff.type == Buff.buffType.NEGATIVE && !isInternalState(buff)) buff.detach();
        }
    }

    protected boolean rollTenacity() {
        return forcedTenacityRoll != null ? forcedTenacityRoll : Random.Int(2) == 0;
    }

    private boolean hasFlaskDiagnosisBonus() {
        return (diagnosis & 0xFF) >= 2;
    }

    private static int chargeSideCell(boolean valid, int candidate) {
        return valid ? candidate : -1;
    }

    @Override
    public synchronized boolean add(Buff buff) {
        if (!restoringFromBundle && phase == Phase.TERMINAL
                && buff.type == Buff.buffType.NEGATIVE && !isInternalState(buff)
                && rollTenacity()) {
            if (sprite instanceof PestilenceKnightSprite) {
                ((PestilenceKnightSprite) sprite).tenacity();
            }
            return false;
        }
        return super.add(buff);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(GROWTH, growth);
        bundle.put(PHASE, phase.ordinal());
        bundle.put(PHASE_LOCKS, phaseLocks);
        bundle.put(HARVEST, harvest.ordinal());
        bundle.put(PENDING_SKILL, pendingSkill);
        bundle.put(PENDING_TURNS, pendingTurns);
        bundle.put(PENDING_PROJECTILE_TARGET, pendingProjectileTarget);
        bundle.put(COOLDOWNS, cooldowns);
        bundle.put(PENDING_CELLS, pendingCells);
        bundle.put(PRESCRIPTION_INDEX, prescriptionIndex);
        bundle.put(DIAGNOSIS, diagnosis);
        bundle.put(LAST_HERO_POS, lastHeroPos);
        bundle.put(REWARD_DROPPED, rewardDropped);
        bundle.put(PROCESSION_STEPS, processionSteps);
        bundle.put(TERMINAL_ENTERED, terminalEntered);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        restoringFromBundle = true;
        try {
            super.restoreFromBundle(bundle);
        } finally {
            restoringFromBundle = false;
        }
        growth = Math.max(0, Math.min(6, bundle.getInt(GROWTH)));
        phase = enumAt(Phase.values(), bundle.getInt(PHASE), Phase.INCUBATION);
        phaseLocks = Math.max(0, Math.min(3, bundle.getInt(PHASE_LOCKS)));
        harvest = enumAt(HarvestState.values(), bundle.getInt(HARVEST), HarvestState.NONE);
        pendingSkill = bundle.getString(PENDING_SKILL);
        if (pendingSkill == null) pendingSkill = "";
        pendingTurns = pendingSkill.isEmpty() ? 0
                : bundle.contains(PENDING_TURNS) ? Math.max(1, bundle.getInt(PENDING_TURNS)) : 1;
        pendingCells = bundle.getIntArray(PENDING_CELLS);
        pendingProjectileTarget = pendingSkill.isEmpty() ? -1
                : bundle.contains(PENDING_PROJECTILE_TARGET)
                ? bundle.getInt(PENDING_PROJECTILE_TARGET) : miasmaProjectileTarget(pendingCells);
        int[] savedCooldowns = bundle.getIntArray(COOLDOWNS);
        cooldowns = savedCooldowns.length == 4 ? savedCooldowns : new int[4];
        for (int i = 0; i < cooldowns.length; i++) cooldowns[i] = Math.max(0, cooldowns[i]);
        prescriptionIndex = Math.max(0, bundle.getInt(PRESCRIPTION_INDEX));
        int savedDiagnosis = Math.max(0, bundle.getInt(DIAGNOSIS));
        diagnosis = (savedDiagnosis & BRAZIER_TUTORIAL_FLAG)
                | Math.min(2, savedDiagnosis & 0xFF);
        lastHeroPos = bundle.contains(LAST_HERO_POS) ? bundle.getInt(LAST_HERO_POS) : -1;
        rewardDropped = bundle.getBoolean(REWARD_DROPPED);
        processionSteps = Math.max(0, Math.min(3, bundle.getInt(PROCESSION_STEPS)));
        terminalEntered = bundle.getBoolean(TERMINAL_ENTERED) || phase == Phase.TERMINAL;
        applyGrowthStats(false);
    }

    private static <T> T enumAt(T[] values, int index, T fallback) {
        return index >= 0 && index < values.length ? values[index] : fallback;
    }
}
