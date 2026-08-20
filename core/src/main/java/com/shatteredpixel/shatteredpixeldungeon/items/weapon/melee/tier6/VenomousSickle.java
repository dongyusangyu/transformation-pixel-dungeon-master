package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** A tier-six war scythe with a deliberately inaccurate, venom-coated blade. */
public class VenomousSickle extends MeleeWeapon {

	public static final int TIER = 6;

	enum AttackMode { NORMAL, PURSUIT_FREE, FORCED_AMBUSH_FREE, BONUS_FREE }
	enum ToxicCategory { POISON, CORROSION, OOZE, CORRUPTION_DEBUFF }

	private static final LinkedHashMap<Class<? extends FlavourBuff>, Float> CORRUPTION_DEBUFFS =
			new LinkedHashMap<>();

	static {
		CORRUPTION_DEBUFFS.put(Weakness.class, 2f);
		CORRUPTION_DEBUFFS.put(Vulnerable.class, 2f);
		CORRUPTION_DEBUFFS.put(Cripple.class, 1f);
		CORRUPTION_DEBUFFS.put(Blindness.class, 1f);
		CORRUPTION_DEBUFFS.put(Terror.class, 1f);
		CORRUPTION_DEBUFFS.put(Amok.class, 3f);
		CORRUPTION_DEBUFFS.put(Slow.class, 2f);
		CORRUPTION_DEBUFFS.put(Hex.class, 2f);
		CORRUPTION_DEBUFFS.put(Paralysis.class, 1f);
	}

	private int trackedTargetId = -1;
	private int originPos = -1;
	private boolean movedSinceHit;
	private boolean pursuitReady;
	private boolean cancelWindow;
	private boolean forcedAmbushArmed;
	private transient AttackMode activeAttackMode = AttackMode.NORMAL;
	private transient boolean freeDelayPending;

	{
		image = EXItemSpriteSheet.VENOMOUS_SICKLE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.9f;
		tier = TIER;
		ACC = 0.8f;
		DLY = 1f;
		RCH = 1;
	}

	@Override
	public int min(int level) {
		return 6 + level;
	}

	@Override
	public int max(int level) {
		return 47 + 7 * level;
	}

	@Override
	public int STRReq(int level) {
		return STRReq(TIER, level);
	}

	int tierForTest() {
		return tier;
	}

	float accuracyForTest() {
		return ACC;
	}

	float delayForTest() {
		return DLY;
	}

	int rangeForTest() {
		return RCH;
	}

	static float procChanceForLevel(int level) {
		return Math.min(1f, 0.20f + 0.05f * Math.max(0, level));
	}

	static int poisonDuration(int level) {
		return 10 + Math.max(0, level);
	}

	static int corrosionDamage(int level) {
		return 5 + Math.max(0, level);
	}

	static int corrosionDuration(int level) {
		return 3 + Math.max(0, level);
	}

	static int oozeDuration(int level) {
		return 10 + Math.max(0, level);
	}

	static int corruptionDebuffDuration(int level) {
		return 6 + 3 * Math.max(0, level);
	}

	static LinkedHashMap<Class<? extends FlavourBuff>, Float> corruptionDebuffWeights() {
		return new LinkedHashMap<>(CORRUPTION_DEBUFFS);
	}

	@Override
	public void beforeHeroAttack(Hero hero, Char target) {
		if (!hero.isResolvingAttackAction()) {
			activeAttackMode = AttackMode.BONUS_FREE;
			return;
		}
		if (forcedAmbushArmed) {
			forcedAmbushArmed = false;
			activeAttackMode = AttackMode.FORCED_AMBUSH_FREE;
			freeDelayPending = true;
			return;
		}
		if (isTrackedTargetValid(hero, target) && pursuitReady) {
			clearTracking();
			activeAttackMode = AttackMode.PURSUIT_FREE;
			freeDelayPending = true;
			return;
		}
		cancelWindow = false;
		clearTracking();
		activeAttackMode = AttackMode.NORMAL;
	}

	@Override
	public void afterHeroAttack(Hero hero, Char target, boolean hit) {
		AttackMode completed = activeAttackMode;
		activeAttackMode = AttackMode.NORMAL;
		if (completed == AttackMode.NORMAL) {
			if (hit && target != null && target.isAlive()) {
				trackedTargetId = target.id();
				originPos = hero.pos;
				movedSinceHit = false;
				pursuitReady = false;
			}
		} else if (completed == AttackMode.PURSUIT_FREE) {
			clearTracking();
			cancelWindow = true;
		} else if (completed == AttackMode.FORCED_AMBUSH_FREE) {
			clearTracking();
			cancelWindow = false;
		}
	}

	@Override
	public float delayFactor(Char owner) {
		return freeDelayPending ? 0f : super.delayFactor(owner);
	}

	@Override
	public void afterHeroAttackDelayResolved(Hero hero) {
		freeDelayPending = false;
	}

	@Override
	public void onHeroStep(Hero hero, int from, int to) {
		if (trackedTargetId == -1 || from == to) return;
		Actor actor = Actor.findById(trackedTargetId);
		Char target = actor instanceof Char ? (Char) actor : null;
		if (!isTrackedTargetPresent(target)) {
			clearTracking();
			return;
		}
		movedSinceHit |= to != originPos;
		pursuitReady = movedSinceHit && Dungeon.level.adjacent(hero.pos, target.pos);
	}

	@Override
	public boolean forcesSurpriseAttack(Hero hero, Char target) {
		return activeAttackMode == AttackMode.FORCED_AMBUSH_FREE;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int result = super.proc(attacker, defender, damage);
		if (activeAttackMode == AttackMode.NORMAL
				&& attacker instanceof Hero
				&& ((Hero) attacker).belongings.attackingWeapon() == this
				&& defender.isAlive()
				&& Random.Float() < procChanceForLevel(buffedLvl())) {
			tryApplyToxicEffect(defender, buffedLvl());
		}
		return result;
	}

	static boolean tryApplyToxicEffect(Char target, int level) {
		ArrayList<ToxicCategory> remaining = new ArrayList<>();
		for (ToxicCategory category : ToxicCategory.values()) remaining.add(category);
		while (!remaining.isEmpty()) {
			ToxicCategory selected = remaining.remove(Random.Int(remaining.size()));
			if (tryApplyCategory(target, selected, level)) return true;
		}
		return false;
	}

	private static boolean tryApplyCategory(Char target, ToxicCategory category, int level) {
		switch (category) {
			case POISON:
				Poison poison = Buff.affect(target, Poison.class);
				poison.set(poisonDuration(level));
				return target.buff(Poison.class) == poison;
			case CORROSION:
				Corrosion corrosion = Buff.affect(target, Corrosion.class);
				corrosion.set(corrosionDuration(level), corrosionDamage(level), VenomousSickle.class);
				return target.buff(Corrosion.class) == corrosion;
			case OOZE:
				Ooze ooze = Buff.affect(target, Ooze.class);
				ooze.set(oozeDuration(level));
				return target.buff(Ooze.class) == ooze;
			case CORRUPTION_DEBUFF:
				return tryApplyCorruptionDebuff(target, level);
			default:
				return false;
		}
	}

	private static boolean tryApplyCorruptionDebuff(Char target, int level) {
		LinkedHashMap<Class<? extends FlavourBuff>, Float> candidates = new LinkedHashMap<>();
		for (Map.Entry<Class<? extends FlavourBuff>, Float> entry : CORRUPTION_DEBUFFS.entrySet()) {
			if (target.buff(entry.getKey()) == null && !target.isImmune(entry.getKey())) {
				candidates.put(entry.getKey(), entry.getValue());
			}
		}
		while (!candidates.isEmpty()) {
			Class<? extends FlavourBuff> selected = Random.chances(candidates);
			FlavourBuff applied = Buff.append(target, selected, corruptionDebuffDuration(level));
			if (target.buff(selected) == applied) return true;
			candidates.remove(selected);
		}
		return false;
	}

	@Override
	public boolean canUseWeaponAbilityAction(Hero hero) {
		return cancelWindow && super.canUseWeaponAbilityAction(hero);
	}

	@Override
	public String defaultAction() {
		return canUseWeaponAbilityAction(Dungeon.hero) ? AC_ABILITY : defaultAction;
	}

	@Override
	public String targetingPrompt() {
		return null;
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (!canUseWeaponAbilityAction(hero)) return;
		beforeAbilityUsed(hero, null);
		cancelWindow = false;
		forcedAmbushArmed = true;
		afterAbilityUsed(hero);
		updateQuickslot();
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, "ability_desc");
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		clearCombatState();
		return super.doUnequip(hero, collect, single);
	}

	@Override
	public void onThrow(int cell) {
		clearCombatState();
		super.onThrow(cell);
	}

	private boolean isTrackedTargetPresent(Char target) {
		return target != null && target.id() == trackedTargetId && target.isAlive()
				&& Actor.findById(trackedTargetId) == target;
	}

	private boolean isTrackedTargetValid(Hero hero, Char target) {
		return isTrackedTargetPresent(target) && Dungeon.level != null
				&& Dungeon.level.adjacent(hero.pos, target.pos);
	}

	private void clearTracking() {
		trackedTargetId = -1;
		originPos = -1;
		movedSinceHit = false;
		pursuitReady = false;
	}

	private void clearCombatState() {
		clearTracking();
		cancelWindow = false;
		forcedAmbushArmed = false;
		activeAttackMode = AttackMode.NORMAL;
		freeDelayPending = false;
	}
}
