package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Drunkenness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Exhilaration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HeavyInjury;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.SourWineAroma;
import com.shatteredpixel.shatteredpixeldungeon.items.food.GreenGlowFruit;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArena;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tboss.GentlemanElfSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** Three-stage tower boss built around a readable toast, cup and illusion duel. */
public class GentlemanElf extends TowerBoss implements ElfWineCup.Listener {
	public enum Phase { TOAST_GAME, CUP_CONTEST, MIRROR_TEST, BERSERK, FINAL_DUEL }
	public enum Skill { NONE, NORMAL, TOAST, DEVOUR, CUP_DEVOUR, CUP_DASH, TABLE_SHOCK, BANQUET, ILLUSION }
	public enum WineState { DRUNKENNESS, EXHILARATION }

	public static final int FINAL_DAMAGE_CAP = 50;
	public static final int FIRST_LOCK_HP = 1200;
	public static final int SECOND_LOCK_HP = 600;
	private static final int TOAST_BLUE = 0x4F8DFF;
	private static final int TOAST_YELLOW = 0xF2D34F;
	private static final int TABLE_WARNING = 0x9D6CFF;
	private static final int DASH_WARNING = 0x6FEA65;

	private static final String PHASE = "gentleman_phase", LOCKS = "gentleman_locks";
	private static final String ACTIONS = "gentleman_actions", PENDING = "gentleman_pending";
	private static final String PENDING_CELLS = "gentleman_pending_cells";
	private static final String PENDING_LANDING = "gentleman_pending_landing";
	private static final String PENDING_TARGET = "gentleman_pending_target";
	private static final String PENDING_ENTITY = "gentleman_pending_entity";
	private static final String PENDING_WINE = "gentleman_pending_wine", NEXT_WINE = "gentleman_next_wine";
	private static final String DASH_CD = "gentleman_dash_cd", SHOCK_CD = "gentleman_shock_cd";
	private static final String CUP_DEVOUR_CD = "gentleman_cup_devour_cd";
	private static final String INTRO = "gentleman_intro";
	private static final String REWARD = "gentleman_reward", ILLUSIONS = "gentleman_illusions";
	private static final String BERSERK_BANQUET = "gentleman_berserk_banquet";
	private static final String PENDING_ADVANCES = "gentleman_pending_advances";
	private static final String WINE_TARGET = "gentleman_wine_target", WINE_CELLS = "gentleman_wine_cells";
	private static final String WINE_STATE = "gentleman_wine_state", WINE_TIME = "gentleman_wine_time";
	private static final String CUP_BOOST = "gentleman_cup_boost";
	private static final String LAST_POUNCE = "gentleman_last_pounce", POUNCE_COUNT = "gentleman_pounce_count";
	private static final String OWNED_WINE = "gentleman_owned_wine";

	private Phase phase = Phase.TOAST_GAME;
	private int phaseLocks, phaseActions;
	private Skill pendingSkill = Skill.NONE;
	private int[] pendingCells = new int[0];
	private int pendingLandingCell = -1, pendingTargetCell = -1, pendingEntityId = -1;
	private WineState pendingWineState = WineState.DRUNKENNESS;
	private WineState nextWineState = WineState.DRUNKENNESS;
	private int cupDashCooldown, tableShockCooldown, cupDevourCooldown;
	private boolean waitingForNormalAttack, introResolved, rewardDropped, berserkBanquetWindow;
	private boolean pendingAdvancesRhythm = true;
	private int activeIllusions;
	private int wineTargetCell = -1;
	private int[] wineWarningCells = new int[0];
	private WineState wineWarningState = WineState.DRUNKENNESS;
	private float wineWarningTime, cupBoostTime, lastPounceTime;
	private int successfulPounces;
	private boolean ownedHeroWine;
	private transient GentlemanElfArena arena;
	private transient boolean restoreGrace;

	public GentlemanElf() {
		HT = HP = 1500;
		defenseSkill = 25;
		viewDistance = 6;
		EXP = 0;
		maxLvl = 30;
		loot = GreenGlowFruit.class;
		lootChance = 1f;
		spriteClass = GentlemanElfSprite.class;
		properties.add(Property.BOSS);
        //properties.add(Property.STATIC);
        properties.add(Property.UNSLEEP);
	}

	@Override public String towerBossId() { return TowerBossGenerator.GENTLEMAN_ELF_ID; }
	@Override public void prepareForStandalonePlacement(Char initialTarget) {
		introResolved = true;
		initializeEncounterTarget(initialTarget);
		if (state != HUNTING) state = HUNTING;
	}
	@Override public boolean prepareArena(TowerBossLevel level, int spawnCell) {
		arena = level.prepareGentlemanElfArena(this);
		return true;
	}
	@Override public void cleanupArena(TowerBossLevel level) {
		if (arena != null) { arena.cleanup(); Actor.remove(arena); }
	}
	public void bindArena(GentlemanElfArena value) { arena = value; }

	@Override public int attackSkill(Char target) { return 50; }
	@Override public int damageRoll() { return Random.NormalIntRange(20, 40); }
	@Override public int drRoll() { return Random.NormalIntRange(0, 20); }
	@Override public float speed() { return (phase == Phase.MIRROR_TEST || phase == Phase.BERSERK ? 1.6f : 0.8f)
			* (cupBoostTime > 0 ? 2.5f : 1f); }
	@Override public float attackDelay() { return (phase == Phase.MIRROR_TEST || phase == Phase.BERSERK ? 0.5f : 1f)
			/ (cupBoostTime > 0 ? 2.5f : 1f); }
	@Override public int heal(int amount, boolean visual) {
		int before = HP;
		int healed = super.heal(amount, visual);
		int ceiling = phase == Phase.TOAST_GAME ? HT
				: phase == Phase.CUP_CONTEST ? FIRST_LOCK_HP : SECOND_LOCK_HP;
		if (HP > ceiling) HP = ceiling;
		return Math.min(healed, Math.max(0, HP - before));
	}

	/**
	 * Starts the encounter with a valid last-known target before the first AI tick.
	 * The target position is important when the boss starts outside its view range.
	 */
	public void beginEncounter(Char initialTarget) {
		initializeEncounterTarget(initialTarget);
		notice();
	}

	private void initializeEncounterTarget(Char initialTarget) {
		if (initialTarget != null && initialTarget.isAlive()) {
			enemy = initialTarget;
			target = initialTarget.pos;
			state = HUNTING;
		}
	}

	@Override protected int damageCap(Object source, DamageTag... tags) {
		if (phase == Phase.FINAL_DUEL || phase == Phase.CUP_CONTEST && activeCup() != null) return 0;
		int cap = Dungeon.hero != null && Dungeon.hero.buff(Exhilaration.class) != null ? 80 : FINAL_DAMAGE_CAP;
		if (cupBoostTime > 0) cap = Math.min(cap, 20);
		return mirrorsProtectingBody() ? Math.min(cap, 1) : cap;
	}

	@Override protected int modifyFinalDamage(int damage, Object source, DamageTag... tags) {
		if (phase == Phase.FINAL_DUEL || phase == Phase.CUP_CONTEST && activeCup() != null) return 0;
		int cap = Dungeon.hero != null && Dungeon.hero.buff(Exhilaration.class) != null ? 80 : FINAL_DAMAGE_CAP;
		if (cupBoostTime > 0) cap = Math.min(cap, 20);
		int resolved = Math.min(cap, Math.max(0, super.modifyFinalDamage(damage, source, tags)));
		if (mirrorsProtectingBody() && resolved > 0) return 1;
		int lock = phase == Phase.TOAST_GAME ? FIRST_LOCK_HP : phase == Phase.CUP_CONTEST ? SECOND_LOCK_HP : -1;
		int requiredLocks = phase == Phase.TOAST_GAME ? 1 : 2;
		if (lock >= 0 && phaseLocks < requiredLocks && HP - resolved <= lock) {
			resolved = Math.max(0, HP - lock);
			phaseLocks = requiredLocks;
		}
		return resolved;
	}

	@Override public boolean isInvulnerable(Class effect) {
		return !introResolved
				|| phase == Phase.FINAL_DUEL
				|| phase == Phase.CUP_CONTEST && activeCup() != null
				|| (phase == Phase.TOAST_GAME && phaseLocks >= 1)
				|| (phase == Phase.CUP_CONTEST && phaseLocks >= 2)
				|| super.isInvulnerable(effect);
	}

	@Override protected boolean act() {
		if (!introResolved) {
			spend(TICK);
			return true;
		}
		refreshPerception();
		if (phase == Phase.FINAL_DUEL) ensureFinalDrunkenness();
		if (arena == null) tickEncounterClock(TICK);
		ElfWineCup cup = phase == Phase.CUP_CONTEST ? activeCup() : null;
		if (cup != null) lockCupObjective(cup);
		refreshEnemySeen();
		if (phase == Phase.CUP_CONTEST && cup != null
				&& (paralysed > 0 || buff(Sleep.class) != null || buff(MagicalSleep.class) != null)) {
			showPendingTelegraph();
			spend(TICK);
			return true;
		}
		if (paralysed > 0 || buff(Sleep.class) != null || buff(MagicalSleep.class) != null) {
			showPendingTelegraph();
			return super.act();
		}
		if (needsTransition()) return transitionPhase();
		if (pendingSkill != Skill.NONE && restoreGrace) {
			restoreGrace = false;
			showPendingTelegraph();
			spend(TICK);
			return true;
		}
		if (pendingSkill != Skill.NONE) return resolvePendingSkill();
		if (phase == Phase.TOAST_GAME) return actToastGame();
		if (phase == Phase.CUP_CONTEST) return actCupContest();
		if (phase == Phase.MIRROR_TEST) return actWithLowestPriorityBanquet();
		if (phase == Phase.FINAL_DUEL) return telegraphDevour(false, Skill.DEVOUR);
		return actBerserk();
	}
	private ElfWineCup activeCup() { return arena == null ? null : arena.cup(); }
	private void lockCupObjective(ElfWineCup cup) {
		enemy = cup;
		target = cup.pos;
		state = HUNTING;
		recentlyAttackedBy.clear();
		refreshEnemySeen();
	}

	private boolean needsTransition() {
		return phase == Phase.TOAST_GAME && phaseLocks >= 1 && HP <= FIRST_LOCK_HP
				|| phase == Phase.CUP_CONTEST && phaseLocks >= 2 && HP <= SECOND_LOCK_HP;
	}
	private boolean transitionPhase() {
		clearPendingSkill();
		phaseActions = 0;
		if (phase == Phase.TOAST_GAME) {
			phase = Phase.CUP_CONTEST;
			if (arena != null) arena.spawnCupNow();
		} else {
			phase = Phase.FINAL_DUEL;
			if (arena != null) {
				arena.cancelCup();
				arena.clearIllusions();
			}
			activeIllusions = 0;
			cancelWineWarning();
			ensureFinalDrunkenness();
			if (Dungeon.hero != null && Dungeon.hero.isAlive()) initializeEncounterTarget(Dungeon.hero);
		}
		announce("phase");
		spend(TICK);
		return true;
	}

	private boolean actToastGame() {
		Skill next = firstPhaseSkillAt(phaseActions);
		if (next == Skill.TOAST) {
			if (toastTarget() != null && wineWarningTime <= 0) stageWineWarning(toastTarget());
			phaseActions = (phaseActions + 1) % 6;
			return actWithLowestPriorityBanquet(false);
		}
		if (next == Skill.DEVOUR && canPerceive(enemy)) return telegraphDevour(false, Skill.DEVOUR);
		return actWithLowestPriorityBanquet();
	}
	private boolean actCupContest() {
		if (phaseActions == 2 && wineWarningTime <= 0 && toastTarget() != null)
			stageWineWarning(toastTarget());
		ElfWineCup cup = activeCup();
		if (cup == null || Dungeon.level == null) return actCupContestWithoutCup();
		lockCupObjective(cup);
		Hero hero = Dungeon.hero;
		if (tableShockCooldown <= 0 && canPerceive(hero)
				&& Dungeon.level.distance(hero.pos, cup.pos) <= 2) return telegraphTableShock(cup);
		if (cupDashCooldown <= 0 && cupDashEligible(Dungeon.level.distance(pos, cup.pos))) return telegraphCupDash(cup);
		if (arena != null && arena.banquetReady()) return telegraphBanquet();
		if (canAttack(cup)) {
			enemy = cup;
			boolean complete = super.doAttack(cup);
			if (complete) finishBossAction(); else waitingForNormalAttack = true;
			return complete;
		}
		int oldPos = pos;
		if (getCloser(cup.pos)) {
			spend(1f / speed());
			finishBossAction();
			return moveSprite(oldPos, pos);
		}
		return waitForCupObjective(cup);
	}
	private boolean actCupContestWithoutCup() {
		if (cupDevourCooldown <= 0) {
			Char target = chooseVisibleEnemy();
			if (target != null) {
				enemy = target;
				refreshEnemySeen();
				return telegraphDevour(false, Skill.CUP_DEVOUR);
			}
		}
		return actWithLowestPriorityBanquet();
	}
	private boolean waitForCupObjective(ElfWineCup cup) {
		lockCupObjective(cup);
		spend(TICK);
		finishBossAction();
		return true;
	}
	private boolean actWithLowestPriorityBanquet() { return actWithLowestPriorityBanquet(true); }
	private boolean actWithLowestPriorityBanquet(boolean advanceRhythm) {
		if (arena != null && arena.banquetReady()) return telegraphBanquet(advanceRhythm);
		return performNormalAction(advanceRhythm);
	}
	private boolean actBerserk() {
		if (berserkBanquetWindow && arena != null && arena.banquetReady()) {
			berserkBanquetWindow = false;
			return telegraphBanquet();
		}
		berserkBanquetWindow = false;
		return telegraphDevour(true, Skill.DEVOUR);
	}
	private boolean performNormalAction() {
		return performNormalAction(true);
	}
	private boolean performNormalAction(boolean advanceRhythm) {
		boolean complete = super.act();
		if (complete) finishBossAction(advanceRhythm); else waitingForNormalAttack = true;
		return complete;
	}
	@Override public void onAttackComplete() {
		super.onAttackComplete();
		if (waitingForNormalAttack) { waitingForNormalAttack = false; finishBossAction(); }
	}
	private void finishBossAction() { finishBossAction(true); }
	private void finishBossAction(boolean advanceRhythm) {
		if (advanceRhythm && (phase == Phase.TOAST_GAME || phase == Phase.CUP_CONTEST))
			phaseActions = (phaseActions + 1) % 6;
		tickCooldowns();
	}
	private void tickCooldowns() {
		if (cupDashCooldown > 0) cupDashCooldown--;
		if (tableShockCooldown > 0) tableShockCooldown--;
		if (cupDevourCooldown > 0) cupDevourCooldown--;
	}

	private boolean telegraphToast() {
		Char target = toastTarget();
		if (target == null || Dungeon.level == null) return performNormalAction(false);
		pendingSkill = Skill.TOAST;
		pendingEntityId = target.id();
		pendingTargetCell = target.pos;
		pendingCells = GentlemanElfTelegraph.square(currentGrid(), pendingTargetCell, 1);
		pendingWineState = nextWineState;
		advanceWineSequence();
		announce("toast"); showPendingTelegraph();
		if (sprite instanceof GentlemanElfSprite) ((GentlemanElfSprite) sprite).toast();
		spend(TICK);
		return true;
	}
	private void stageWineWarning(Char target) {
		if (target == null || Dungeon.level == null || phase == Phase.FINAL_DUEL) return;
		stageWineWarningForTest(target.pos,
				GentlemanElfTelegraph.square(currentGrid(), target.pos, 1), nextWineState, TICK);
		advanceWineSequence();
		announce("toast");
		showWineWarning();
	}
	public void tickEncounterClock(float elapsed) {
		if (cupBoostTime > 0) cupBoostTime = Math.max(0, cupBoostTime - elapsed);
		if (wineWarningTime > 0) {
			wineWarningTime -= elapsed;
			if (wineWarningTime <= 0) {
				if (phase == Phase.TOAST_GAME || phase == Phase.CUP_CONTEST)
					resolveToastImpact(wineTargetCell, wineWarningCells.clone(), wineWarningState);
				cancelWineWarning();
			}
		}
	}
	private void cancelWineWarning() {
		wineWarningTime = 0;
		wineTargetCell = -1;
		wineWarningCells = new int[0];
	}
	private void showWineWarning() {
		if (wineWarningTime <= 0 || sprite == null || sprite.parent == null) return;
		int color = wineWarningState == WineState.DRUNKENNESS ? TOAST_BLUE : TOAST_YELLOW;
		for (int cell : wineWarningCells) if (Dungeon.level != null && cell >= 0 && cell < Dungeon.level.length())
			sprite.parent.addToBack(new TargetedCell(cell, color));
	}
	private boolean telegraphDevour(boolean wide, Skill skill) {
		Char target = canPerceive(enemy) ? enemy : null;
		if (!validEnemy(target) || Dungeon.level == null) return performNormalAction();
		pendingSkill = skill;
		pendingEntityId = target.id();
		pendingTargetCell = target.pos;
		if (wide) {
			int endpoint = endpointToBoundary(pos, pendingTargetCell);
			pendingCells = GentlemanElfTelegraph.corridor(currentGrid(), pos, endpoint, 3);
			pendingLandingCell = furthestLegalOnAxis(pos, endpoint, Integer.MAX_VALUE);
		} else {
			pendingCells = GentlemanElfTelegraph.line(dashGrid(), pos, pendingTargetCell);
			pendingLandingCell = furthestLegalOnAxis(pos, pendingTargetCell, 4);
		}
		if (pendingCells.length == 0) { clearPendingSkill(); return performNormalAction(); }
		announce("devour"); showPendingTelegraph();
		if (sprite instanceof GentlemanElfSprite) ((GentlemanElfSprite) sprite).devour();
		spend(TICK);
		return true;
	}
	private boolean telegraphCupDash(ElfWineCup cup) {
		int landing = cupDashLanding(cup);
		if (landing < 0 || landing == pos) return waitForCupObjective(cup);
		pendingSkill = Skill.CUP_DASH;
		pendingEntityId = cup.id(); pendingTargetCell = cup.pos; pendingLandingCell = landing;
		pendingCells = GentlemanElfTelegraph.line(dashGrid(), pos, landing);
		announce("cup_dash"); showPendingTelegraph();
		if (sprite instanceof GentlemanElfSprite) ((GentlemanElfSprite) sprite).dash();
		tickCooldowns();
		spend(TICK);
		return true;
	}
	private boolean telegraphBanquet() { return telegraphBanquet(true); }
	private boolean telegraphBanquet(boolean advanceRhythm) {
		if (arena == null || !arena.banquetReady()) return performNormalAction();
		pendingSkill = Skill.BANQUET;
		pendingAdvancesRhythm = advanceRhythm;
		arena.warnBanquetNow();
		spend(TICK);
		return true;
	}
	private boolean telegraphTableShock(ElfWineCup cup) {
		pendingSkill = Skill.TABLE_SHOCK;
		pendingEntityId = cup.id(); pendingTargetCell = cup.pos;
		pendingCells = GentlemanElfTelegraph.square(currentGrid(), cup.pos, 2);
		announce("table_shock"); showPendingTelegraph();
		tickCooldowns();
		spend(TICK);
		return true;
	}

	private boolean resolvePendingSkill() {
		Skill skill = pendingSkill;
		boolean advanceRhythm = pendingAdvancesRhythm;
		if ((skill == Skill.CUP_DASH || skill == Skill.TABLE_SHOCK)
				&& !(Actor.findById(pendingEntityId) instanceof ElfWineCup)) {
			clearPendingSkill();
			if (phase == Phase.CUP_CONTEST && activeCup() == null && cupDevourCooldown <= 0) {
				finishBossAction();
				return actCupContest();
			}
			finishBossAction(); spend(TICK); return true;
		}
		if (skill == Skill.TOAST) return resolveToast(advanceRhythm);
		else if (skill == Skill.DEVOUR || skill == Skill.CUP_DEVOUR) resolveDevour();
		else if (skill == Skill.CUP_DASH) resolveCupDash();
		else if (skill == Skill.TABLE_SHOCK) resolveTableShock();
		else if (skill == Skill.BANQUET) resolveBanquet();
		clearPendingSkill();
		finishBossAction(advanceRhythm);
		if (skill == Skill.CUP_DASH) cupDashCooldown = 5;
		else if (skill == Skill.TABLE_SHOCK) tableShockCooldown = 6;
		else if (skill == Skill.CUP_DEVOUR) cupDevourCooldown = 10;
		else if (skill == Skill.BANQUET && arena != null) arena.banquetResolved();
		if (skill == Skill.DEVOUR && phase == Phase.BERSERK && arena != null && arena.banquetReady())
			berserkBanquetWindow = true;
		spend(TICK);
		return true;
	}
	private boolean resolveToast(final boolean advanceRhythm) {
		final int targetCell = pendingTargetCell;
		final int[] affectedCells = pendingCells.clone();
		final WineState wineState = pendingWineState;
		final boolean[] completed = {false};
		Callback impact = new Callback() {
			@Override public void call() {
				if (completed[0]) return;
				completed[0] = true;
				resolveToastImpact(targetCell, affectedCells, wineState);
				clearPendingSkill();
				finishBossAction(advanceRhythm);
				spend(TICK);
				next();
			}
		};
		if (launchWineProjectile(targetCell, wineState, impact)) {
			spend(TICK);
			return false;
		}
		impact.call();
		return false;
	}
	private void resolveToastImpact(int targetCell, int[] affectedCells, WineState wineState) {
		if (Dungeon.level != null) Splash.at(targetCell,
				wineState == WineState.DRUNKENNESS ? TOAST_BLUE : TOAST_YELLOW, 8);
		for (Char target : characterSnapshot()) {
			if (target == this || target instanceof GentlemanElfIllusion
					|| !contains(affectedCells, target.pos)) continue;
			target.damage(5, this, DamageTag.MAGICAL);
			if (target.isAlive()) {
				markSkillHit(target);
				if (target instanceof Hero) {
					ownedHeroWine = true;
					if (wineState == WineState.DRUNKENNESS) Drunkenness.affect(target);
					else Exhilaration.affect(target);
				}
			}
		}
	}
	private void resolveDevour() {
		int jumpOrigin = pos;
		Char designated = pendingEntityId < 0 ? null : (Actor.findById(pendingEntityId) instanceof Char
				? (Char) Actor.findById(pendingEntityId) : null);
		boolean validStrike = validEnemy(designated) && contains(pendingCells, designated.pos)
				&& (phase == Phase.FINAL_DUEL
				|| new Ballistica(pos, designated.pos, Ballistica.PROJECTILE).collisionPos == designated.pos);
		boolean hit = false;
		for (Char target : characterSnapshot()) {
			if (!validStrike || !validEnemy(target) || !contains(pendingCells, target.pos)) continue;
			if (phase == Phase.FINAL_DUEL) {
				hit = true;
				int stacks = target.buff(HeavyInjury.class) == null ? 0 : target.buff(HeavyInjury.class).stacks();
				int damage = Math.round(Random.NormalIntRange(20, 30) * (1f + stacks * 0.1f));
				target.damage(damage, this, DamageTag.PHYSICAL);
				if (target.isAlive()) markSkillHit(target);
				if (target instanceof Hero) {
					HeavyInjury.apply(target);
					if (hasDrunkenness(target)) applyPounceSelfDamage(Actor.now());
				}
			} else {
				hit = true;
				target.damage(Random.NormalIntRange(40, 60), this, DamageTag.PHYSICAL);
				if (target.isAlive()) markSkillHit(target);
				pushAway(target, pos, 2);
			}
		}
		if (!isAlive()) return;
		destroyFlammableObstaclesBetween(jumpOrigin, pendingTargetCell);
		int landing = hit ? pendingLandingCell : geometricJumpLanding(pos, pendingTargetCell, 4);
		if (!isLegalLanding(landing)) {
			landing = furthestLegalOnAxis(pos, landing >= 0 ? landing : pendingTargetCell, Integer.MAX_VALUE);
		}
		moveBossTo(landing, true);
	}
	private void destroyFlammableObstaclesBetween(int from, int to) {
		if (Dungeon.level == null) return;
		boolean destroyed = false;
		for (int cell : GentlemanElfTelegraph.intermediateSegment(currentGrid(), from, to)) {
			if (!Dungeon.level.solid[cell]) continue;
			if (Dungeon.level instanceof TowerBossLevel) {
				if (!((TowerBossLevel) Dungeon.level).isDestructibleBossCover(cell)) break;
				((TowerBossLevel) Dungeon.level).destroyBossCover(cell);
			} else {
				if (!isFlammableObstacle(Dungeon.level.map[cell])) break;
				Dungeon.level.destroy(cell);
				GameScene.updateMap(cell);
			}
			destroyed = true;
		}
		if (destroyed) Dungeon.observe();
	}
	static boolean isFlammableObstacle(int terrain) {
		return terrain >= 0 && terrain < Terrain.flags.length
				&& (Terrain.flags[terrain] & Terrain.FLAMABLE) != 0
				&& (Terrain.flags[terrain] & Terrain.SOLID) != 0;
	}
	private void resolveBanquet() { if (arena != null) arena.resolveBanquetNow(); }
	private void resolveCupDash() {
		for (Char target : characterSnapshot()) {
			if (target == this || target instanceof ElfWineCup || !contains(pendingCells, target.pos)) continue;
			target.damage(Random.NormalIntRange(20, 30), this, DamageTag.PHYSICAL);
			if (target.isAlive()) markSkillHit(target);
			pushAway(target, pos, 1);
		}
		destroyFlammableObstaclesBetween(pos, pendingLandingCell);
		if (isDestructibleCover(pendingLandingCell))
			((TowerBossLevel) Dungeon.level).destroyBossCover(pendingLandingCell);
		int landing = isLegalLanding(pendingLandingCell) ? pendingLandingCell
				: furthestLegalOnAxis(pos, pendingLandingCell, Integer.MAX_VALUE);
		moveBossTo(landing, true);
	}
	private void resolveTableShock() {
		for (Char target : characterSnapshot()) {
			if (target == this || target instanceof ElfWineCup || !contains(pendingCells, target.pos)) continue;
			target.damage(Random.NormalIntRange(15, 25), this, DamageTag.MAGICAL);
			if (target.isAlive()) markSkillHit(target);
			if (target.isAlive()) { pushAway(target, pendingTargetCell, 2); Buff.affect(target, Slow.class, 3f); }
		}
	}
	public void markBanquetHit(Char target) { markSkillHit(target); }
	private boolean launchWineProjectile(int target, WineState wineState, Callback callback) {
		if (Dungeon.level == null || target < 0 || target >= Dungeon.level.length()
				|| sprite == null || sprite.parent == null) return false;
		Item wine = new Item();
		wine.image = wineState == WineState.DRUNKENNESS
				? ItemSpriteSheet.POTION_AZURE : ItemSpriteSheet.POTION_GOLDEN;
		((MissileSprite) sprite.parent.recycle(MissileSprite.class)).reset(sprite, target, wine, callback);
		return true;
	}

	private boolean mirrorsProtectingBody() {
		return arena != null ? arena.hasIllusions() : activeIllusions > 0;
	}
	private void enterBerserkIfIllusionsGone() {
		if (phase != Phase.MIRROR_TEST) return;
		if (arena != null) { arena.pruneIllusions(); activeIllusions = arena.livingIllusionCount(); }
		else if (activeIllusions > 0) activeIllusions--;
		if (activeIllusions <= 0) { activeIllusions = 0; phase = Phase.BERSERK; clearPendingSkill(); }
	}

	@Override public void onCupDestroyed(ElfWineCup cup, Char lastHit) {
		if (arena != null) arena.onCupDestroyed(cup, lastHit);
		else onArenaCupDestroyed(lastHit);
	}
	public void onArenaCupDestroyed(Char lastHit) {
		cupDevourCooldown = 0;
		applyCupReward(lastHit);
	}
	public void applyCupReward(Char lastHit) {
		if (lastHit == this) {
			cupBoostTime = 5f;
			return;
		}
		if (!(lastHit instanceof Hero) || !lastHit.isAlive()) return;
		ownedHeroWine = true;
		Exhilaration.affect(lastHit);
		if (lastHit instanceof Hero) {
			lastHit.heal(Math.max(1, Math.round(lastHit.HT * 0.05f)), true);
			Buff.affect(lastHit, Haste.class, 5f);
		}
	}
	int heroCupHealedHpForTest(int hp, int ht) { return Math.min(ht, hp + Math.max(1, Math.round(ht * 0.05f))); }
	float heroCupHasteDurationForTest() { return 5f; }
	public GentlemanElfIllusion[] createIllusionsForTest() {
		activeIllusions = 2;
		return new GentlemanElfIllusion[]{new GentlemanElfIllusion(this), new GentlemanElfIllusion(this)};
	}
	public int activeIllusions() { return activeIllusions; }
	public void syncActiveIllusions(int count) {
		activeIllusions = Math.max(0, Math.min(2, count));
		if (phase == Phase.MIRROR_TEST && activeIllusions == 0) phase = Phase.BERSERK;
	}
	void illusionDied() { enterBerserkIfIllusionsGone(); }

	@Override public void die(Object cause) {
		announce("defeated");
		clearPendingSkill();
		cancelWineWarning();
		if (arena != null) arena.cleanup();
		for (Char ch : characterSnapshot()) if (ch instanceof GentlemanElfIllusion
				&& ((GentlemanElfIllusion) ch).ownerId() == id()) ((GentlemanElfIllusion) ch).dismiss();
		super.die(cause);
	}

	private void clearPendingSkill() {
		pendingSkill = Skill.NONE; pendingCells = new int[0];
		pendingLandingCell = pendingTargetCell = pendingEntityId = -1;
		pendingAdvancesRhythm = true;
		restoreGrace = false;
	}
	private void showPendingTelegraph() {
		showWineWarning();
		if (sprite == null || sprite.parent == null) return;
		if (pendingSkill == Skill.BANQUET) {
			if (arena != null) arena.warnBanquetNow();
			return;
		}
		int color = pendingSkill == Skill.TOAST
				? pendingWineState == WineState.DRUNKENNESS ? TOAST_BLUE : TOAST_YELLOW
				: pendingSkill == Skill.TABLE_SHOCK ? TABLE_WARNING : DASH_WARNING;
		for (int cell : pendingCells) if (Dungeon.level != null && cell >= 0 && cell < Dungeon.level.length())
			sprite.parent.addToBack(new TargetedCell(cell, color));
		if (Dungeon.level != null && (pendingSkill == Skill.DEVOUR || pendingSkill == Skill.CUP_DEVOUR)
				&& pendingLandingCell >= 0 && pendingLandingCell < Dungeon.level.length())
			sprite.parent.addToBack(new TargetedCell(pendingLandingCell, 0xFFFFFF));
	}
	private GentlemanElfTelegraph.Grid currentGrid() {
		if (Dungeon.level == null) return null;
		return new GentlemanElfTelegraph.Grid() {
			public int width() { return Dungeon.level.width(); }
			public int height() { return Dungeon.level.height(); }
			public boolean passable(int cell) { return cell >= 0 && cell < Dungeon.level.length()
					&& Dungeon.level.passable[cell] && !Dungeon.level.solid[cell]; }
			public boolean occupied(int cell) { Char ch = Actor.findChar(cell); return ch != null && ch != GentlemanElf.this; }
		};
	}
	private int endpointToBoundary(int from, int toward) {
		return GentlemanElfTelegraph.endpointAtBoundary(currentGrid(), from, toward);
	}
	private int furthestLegalOnAxis(int from, int toward, int maxSteps) {
		if (Dungeon.level == null || from < 0 || toward < 0) return -1;
		int last = from, steps = 0;
		for (int cell : GentlemanElfTelegraph.line(currentGrid(), from, toward)) {
			if (cell == from) continue;
			if (steps++ >= maxSteps) break;
			if (!isLegalLanding(cell)) break;
			last = cell;
		}
		return last;
	}
	private int geometricJumpLanding(int from, int toward, int maxSteps) {
		if (Dungeon.level == null || from < 0 || toward < 0) return -1;
		int width = Dungeon.level.width();
		int x = from % width, y = from / width;
		int tx = toward % width, ty = toward / width;
		int dx = Integer.compare(tx, x), dy = Integer.compare(ty, y), last = from;
		for (int step = 1; step <= maxSteps; step++) {
			int nx = x + dx * step, ny = y + dy * step;
			if (nx < 0 || ny < 0 || nx >= width || ny >= Dungeon.level.height()) break;
			int cell = nx + ny * width;
			if (isLegalLanding(cell)) last = cell;
		}
		return last;
	}
	private boolean isLegalLanding(int cell) {
		return Dungeon.level != null && cell >= 0 && cell < Dungeon.level.length()
				&& isInsideActiveBossArena(cell)
				&& Dungeon.level.passable[cell] && !Dungeon.level.solid[cell]
				&& (Actor.findChar(cell) == null || Actor.findChar(cell) == this);
	}
	private boolean isInsideActiveBossArena(int cell) {
		return !(Dungeon.level instanceof TowerBossLevel)
				|| ((TowerBossLevel) Dungeon.level).isBossArenaCell(cell);
	}
	private int cupDashLanding(ElfWineCup cup) {
		if (cup == null || Dungeon.level == null || !cupDashEligible(Dungeon.level.distance(pos, cup.pos))) return -1;
		int[] path = GentlemanElfTelegraph.line(dashGrid(), pos, cup.pos);
		if (path.length == 0 || path[path.length - 1] != cup.pos) return -1;
		int preferred = Math.max(1, path.length - 4);
		for (int index = preferred; index > 0; index--) {
			int cell = path[index];
			if (isLegalLanding(cell) || isDestructibleCover(cell)) return cell;
		}
		return -1;
	}
	private GentlemanElfTelegraph.Grid dashGrid() {
		return new GentlemanElfTelegraph.Grid() {
			public int width() { return Dungeon.level.width(); }
			public int height() { return Dungeon.level.height(); }
			public boolean occupied(int cell) { return Actor.findChar(cell) != null; }
			public boolean passable(int cell) {
				return isInsideActiveBossArena(cell)
						&& (Dungeon.level.passable[cell] && !Dungeon.level.solid[cell]
						|| isDestructibleCover(cell));
			}
		};
	}
	private boolean isDestructibleCover(int cell) {
		return Dungeon.level instanceof TowerBossLevel
				&& ((TowerBossLevel) Dungeon.level).isDestructibleBossCover(cell);
	}
	private int nearestFreeNeighbor(int center) {
		if (Dungeon.level == null) return -1;
		int best = -1, bestDistance = Integer.MAX_VALUE;
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = center + offset;
			if (cell < 0 || cell >= Dungeon.level.length() || !Dungeon.level.adjacent(center, cell)
					|| !Dungeon.level.passable[cell] || Dungeon.level.solid[cell] || Actor.findChar(cell) != null) continue;
			int distance = Dungeon.level.distance(pos, cell);
			if (distance < bestDistance || distance == bestDistance && (best < 0 || cell < best)) {
				best = cell; bestDistance = distance;
			}
		}
		return best;
	}
	private void moveBossTo(int cell, boolean jump) {
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length() || cell == pos) return;
		if (!isLegalLanding(cell)) return;
		Char occupant = Actor.findChar(cell);
		if (occupant != null && occupant != this) return;
		int oldPos = pos;
		pos = cell;
		Dungeon.level.occupyCell(this);
		if (sprite != null) {
			if (jump && sprite.parent != null) {
				sprite.place(oldPos);
				sprite.jump(oldPos, cell, 4f, 0.25f,
						new Callback() { @Override public void call() { if (sprite != null) sprite.place(pos); } });
			} else sprite.place(cell);
		}
	}
	private void pushAway(Char target, int center, int distance) {
		if (Dungeon.level == null || target == null || distance <= 0) return;
		int width = Dungeon.level.width();
		int dx = Integer.compare(target.pos % width, center % width), dy = Integer.compare(target.pos / width, center / width);
		int x = target.pos % width + dx, y = target.pos / width + dy;
		if (x < 0 || x >= width || y < 0 || y >= Dungeon.level.height()) return;
		WandOfBlastWave.throwChar(target, new Ballistica(target.pos, x + y * width, Ballistica.MAGIC_BOLT),
				distance, false, false, this);
	}
	@Override public void aggro(Char ch) {
		ElfWineCup cup = phase == Phase.CUP_CONTEST ? activeCup() : null;
		if (cup != null) {
			lockCupObjective(cup);
			return;
		}
		super.aggro(ch);
	}
	private Char chooseVisibleEnemy() {
		Char selected = chooseEnemy();
		return canPerceive(selected) ? selected : null;
	}
	private static ArrayList<Char> characterSnapshot() { return new ArrayList<>(Actor.chars()); }
	private boolean validEnemy(Char target) { return target != null && target != this && target.isAlive()
			&& (target == Dungeon.hero || Actor.isHostile(this, target)); }
	private boolean canPerceive(Char target) {
		return validEnemy(target) && target.invisible <= 0 && fieldOfView != null
				&& target.pos >= 0 && target.pos < fieldOfView.length && fieldOfView[target.pos];
	}
	private void refreshPerception() {
		if (Dungeon.level == null) return;
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView(this, fieldOfView);
	}
	private void refreshEnemySeen() {
		enemySeen = enemy != null && enemy.isAlive() && fieldOfView != null
				&& enemy.pos >= 0 && enemy.pos < fieldOfView.length
				&& enemy.invisible <= 0 && fieldOfView[enemy.pos];
	}
	private Char toastTarget() { return canPerceive(enemy) ? enemy : null; }
	static boolean cupDashEligible(int distance) { return distance >= 2 && distance <= 8; }
	private static boolean contains(int[] cells, int cell) { if (cells != null) for (int value : cells) if (value == cell) return true; return false; }
	private void announce(String key) { if (sprite != null) yell(Messages.get(this, key)); }
	private static Skill firstPhaseSkillAt(int action) {
		int beat = Math.floorMod(action, 6);
		return beat == 2 ? Skill.TOAST : beat == 5 ? Skill.DEVOUR : Skill.NORMAL;
	}

	public WineState nextWineState() { return nextWineState; }
	public void advanceWineSequence() { nextWineState = nextWineState == WineState.DRUNKENNESS ? WineState.EXHILARATION : WineState.DRUNKENNESS; }
	public static boolean isEncounterDrunk(Hero hero) {
		if (hero == null || Dungeon.level == null) return false;
		for (Char ch : Actor.chars()) if (ch instanceof GentlemanElf && ch.isAlive()) {
			GentlemanElf boss = (GentlemanElf) ch;
			if (boss.phase == Phase.FINAL_DUEL || hero.buff(Drunkenness.class) != null) return true;
		}
		return false;
	}
	static boolean hasDrunkenness(Char target) {
		return target != null && target.buff(Drunkenness.class) != null;
	}
	public static boolean isEncounterActive() {
		if (Dungeon.level == null) return false;
		for (Char ch : Actor.chars()) if (ch instanceof GentlemanElf && ch.isAlive()
				&& ((GentlemanElf) ch).introResolved) return true;
		return false;
	}
	public static boolean isFinalDuelDrunk(Char target) {
		if (!(target instanceof Hero) || Dungeon.level == null) return false;
		for (Char ch : Actor.chars()) if (ch instanceof GentlemanElf && ch.isAlive()
				&& ((GentlemanElf) ch).phase == Phase.FINAL_DUEL) return true;
		return false;
	}
	private void ensureFinalDrunkenness() {
		if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			ownedHeroWine = true;
			Drunkenness.affect(Dungeon.hero);
		}
	}
	public void clearEncounterWine() {
		if (!ownedHeroWine || Dungeon.hero == null) return;
		Buff.detach(Dungeon.hero, Drunkenness.class);
		Buff.detach(Dungeon.hero, Exhilaration.class);
		Buff.detach(Dungeon.hero, HeavyInjury.class);
		ownedHeroWine = false;
	}
	int pounceSelfDamageAt(float now) {
		if (successfulPounces == 0) return 50;
		int missed = Math.max(0, Math.min(10, (int) Math.floor(now - lastPounceTime) - 1));
		return 100 - 5 * missed;
	}
	void recordPounceHitAt(float now) {
		successfulPounces++;
		lastPounceTime = now;
	}
	private void applyPounceSelfDamage(float now) {
		int loss = pounceSelfDamageAt(now);
		recordPounceHitAt(now);
		HP = Math.max(0, HP - loss);
		if (sprite != null) sprite.showStatus(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.NEGATIVE, Integer.toString(loss));
		if (HP <= 0) die(this);
	}
	void stageWineWarningForTest(int target, int[] cells, WineState state, float delay) {
		wineTargetCell = target;
		wineWarningCells = cells == null ? new int[0] : cells.clone();
		wineWarningState = state == null ? WineState.DRUNKENNESS : state;
		wineWarningTime = Math.max(0, delay);
	}
	int[] wineWarningCellsForTest() { return wineWarningCells.clone(); }
	WineState wineWarningStateForTest() { return wineWarningState; }
	float cupBoostTimeForTest() { return cupBoostTime; }
	void finishBossActionForTest() { finishBossAction(); }
	void stagePendingForTest(Skill skill, int[] cells, int landing, WineState wine) {
		pendingSkill = skill == null ? Skill.NONE : skill;
		pendingCells = cells == null ? new int[0] : cells.clone();
		pendingLandingCell = landing;
		pendingWineState = wine == null ? WineState.DRUNKENNESS : wine;
	}
	int[] pendingCellsForTest() { return pendingCells.clone(); }
	int pendingLandingForTest() { return pendingLandingCell; }
	WineState pendingWineForTest() { return pendingWineState; }
	public Char devourTargetForTest(Char currentEnemy, Char hero) {
		return canPerceive(currentEnemy) ? currentEnemy : null;
	}
	Char toastTargetForTest(Char currentEnemy) {
		return canPerceive(currentEnemy) ? currentEnemy : null;
	}
	public static boolean cupDashEligibleForTest(int distance) { return cupDashEligible(distance); }
	public static int cupDashLandingDistanceForTest() { return 3; }
	public int warningColorForTest(Skill skill) { return skill == Skill.TOAST && nextWineState == WineState.DRUNKENNESS ? TOAST_BLUE : TOAST_YELLOW; }
	public Skill nextSkillForTest() { return phase == Phase.TOAST_GAME ? firstPhaseSkillAt(phaseActions)
			: phase == Phase.CUP_CONTEST ? activeCup() != null ? Skill.CUP_DASH
					: cupDevourCooldown <= 0 ? Skill.DEVOUR : Skill.NORMAL
			: phase == Phase.MIRROR_TEST ? Skill.NORMAL : Skill.DEVOUR; }
	public int devourJumpDistanceForTest() { return 4; }
	public Phase phase() { return phase; }
	public int phaseLocks() { return phaseLocks; }
	public Skill pendingSkill() { return pendingSkill; }
	public int phaseActions() { return phaseActions; }
	public void setPendingSkillForTest(Skill skill) { pendingSkill = skill == null ? Skill.NONE : skill; }
	public void setPhaseForTest(Phase value, int locks) { phase = value; phaseLocks = Math.max(0, Math.min(2, locks)); }
	public int capFinalDamageForTest(int damage) { return modifyFinalDamage(damage, null); }
	public Skill[] nextSixActionsForTest() { return new Skill[]{Skill.NORMAL, Skill.NORMAL, Skill.TOAST, Skill.NORMAL, Skill.NORMAL, Skill.DEVOUR}; }
	public int damageRollMinForTest() { return 20; }
	public int damageRollMaxForTest() { return 40; }
	public int drRollMinForTest() { return 0; }
	public int drRollMaxForTest() { return 20; }
	public GentlemanElfArena arena() { return arena; }
	public void arenaForTest(GentlemanElfArena value) { arena = value; }
	public boolean introResolved() { return introResolved; }
	boolean introNeedsPresentationForTest() { return !introResolved; }
	void initializeEncounterTargetForTest(Char target) { initializeEncounterTarget(target); }
	int targetForTest() { return target; }
	boolean restoreGraceForTest() { return restoreGrace; }
	public void setCupDevourCooldownForTest(int value) { cupDevourCooldown = Math.max(0, Math.min(10, value)); }
	public void armCupDevourForTest() { cupDevourCooldown = 0; }
	public void resolveCupDevourForTest() { cupDevourCooldown = 10; }
	public int cupDevourCooldownForTest() { return cupDevourCooldown; }
	public Char enemyForTest() { return enemy; }
	public void resolveIntro(boolean drink) {
		if (introResolved) return;
		introResolved = true;
		if (Dungeon.hero != null) {
			ownedHeroWine = true;
			if (drink) Drunkenness.affect(Dungeon.hero); else Exhilaration.affect(Dungeon.hero);
		}
	}

	@Override public com.shatteredpixel.shatteredpixeldungeon.items.Item createLoot() { return createLootForRoll(Random.Float()); }
	public com.shatteredpixel.shatteredpixeldungeon.items.Item createLootForRoll(float roll) {
		return roll < 0.5f ? new GreenGlowFruit().quantity(3) : new SourWineAroma();
	}

	@Override public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PHASE, phase.name()); bundle.put(LOCKS, phaseLocks); bundle.put(ACTIONS, phaseActions);
		bundle.put(PENDING, pendingSkill.name()); bundle.put(PENDING_CELLS, pendingCells);
		bundle.put(PENDING_LANDING, pendingLandingCell); bundle.put(PENDING_TARGET, pendingTargetCell);
		bundle.put(PENDING_ENTITY, pendingEntityId); bundle.put(PENDING_WINE, pendingWineState.name());
		bundle.put(NEXT_WINE, nextWineState.name()); bundle.put(DASH_CD, cupDashCooldown);
		bundle.put(SHOCK_CD, tableShockCooldown); bundle.put(CUP_DEVOUR_CD, cupDevourCooldown);
		bundle.put(INTRO, introResolved); bundle.put(REWARD, rewardDropped); bundle.put(ILLUSIONS, activeIllusions);
		bundle.put(BERSERK_BANQUET, berserkBanquetWindow);
		bundle.put(PENDING_ADVANCES, pendingAdvancesRhythm);
		bundle.put(WINE_TARGET, wineTargetCell); bundle.put(WINE_CELLS, wineWarningCells);
		bundle.put(WINE_STATE, wineWarningState.name()); bundle.put(WINE_TIME, wineWarningTime);
		bundle.put(CUP_BOOST, cupBoostTime);
		bundle.put(LAST_POUNCE, lastPounceTime); bundle.put(POUNCE_COUNT, successfulPounces);
		bundle.put(OWNED_WINE, ownedHeroWine);
	}
	@Override public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		phase = enumValue(Phase.class, bundle.getString(PHASE), Phase.TOAST_GAME);
		phaseLocks = Math.max(0, Math.min(2, bundle.getInt(LOCKS)));
		phaseActions = Math.floorMod(bundle.getInt(ACTIONS), 6);
		pendingSkill = enumValue(Skill.class, bundle.getString(PENDING), Skill.NONE);
		pendingCells = bundle.getIntArray(PENDING_CELLS); if (pendingCells == null) pendingCells = new int[0];
		pendingLandingCell = bundle.contains(PENDING_LANDING) ? bundle.getInt(PENDING_LANDING) : -1;
		pendingTargetCell = bundle.contains(PENDING_TARGET) ? bundle.getInt(PENDING_TARGET) : -1;
		pendingEntityId = bundle.contains(PENDING_ENTITY) ? bundle.getInt(PENDING_ENTITY) : -1;
		pendingWineState = enumValue(WineState.class, bundle.getString(PENDING_WINE), WineState.DRUNKENNESS);
		nextWineState = enumValue(WineState.class, bundle.getString(NEXT_WINE), WineState.DRUNKENNESS);
		cupDashCooldown = Math.max(0, Math.min(5, bundle.getInt(DASH_CD)));
		tableShockCooldown = Math.max(0, Math.min(6, bundle.getInt(SHOCK_CD)));
		cupDevourCooldown = bundle.contains(CUP_DEVOUR_CD)
				? Math.max(0, Math.min(10, bundle.getInt(CUP_DEVOUR_CD))) : 0;
		introResolved = bundle.getBoolean(INTRO); rewardDropped = bundle.getBoolean(REWARD);
		activeIllusions = Math.max(0, Math.min(2, bundle.getInt(ILLUSIONS)));
		berserkBanquetWindow = bundle.getBoolean(BERSERK_BANQUET);
		pendingAdvancesRhythm = !bundle.contains(PENDING_ADVANCES) || bundle.getBoolean(PENDING_ADVANCES);
		wineTargetCell = bundle.contains(WINE_TARGET) ? bundle.getInt(WINE_TARGET) : -1;
		wineWarningCells = bundle.getIntArray(WINE_CELLS);
		if (wineWarningCells == null) wineWarningCells = new int[0];
		wineWarningState = enumValue(WineState.class, bundle.getString(WINE_STATE), WineState.DRUNKENNESS);
		wineWarningTime = Math.max(0, bundle.getFloat(WINE_TIME));
		cupBoostTime = Math.max(0, bundle.getFloat(CUP_BOOST));
		lastPounceTime = bundle.getFloat(LAST_POUNCE);
		successfulPounces = Math.max(0, bundle.getInt(POUNCE_COUNT));
		ownedHeroWine = bundle.getBoolean(OWNED_WINE);
		if (phase == Phase.FINAL_DUEL) { cancelWineWarning(); activeIllusions = 0; }
		if (pendingSkill != Skill.NONE && pendingSkill != Skill.BANQUET && pendingCells.length == 0) clearPendingSkill();
		restoreGrace = pendingSkill != Skill.NONE;
	}
	private static <T extends Enum<T>> T enumValue(Class<T> type, String value, T fallback) {
		try { return Enum.valueOf(type, value); } catch (Exception ignored) { return fallback; }
	}
}
