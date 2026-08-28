package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/** The ponderous tier-six radiant gold halberd. */
public class RadiantGoldHalberd extends MeleeWeapon {

	public static final int TIER = 6;
	public static final float ACCURACY = 1f;
	public static final float DELAY = 2f;
	public static final int RANGE = 3;
	private static final int ABILITY_CHARGE_COST = 2;
	private static final int MAX_SPLASH_TARGETS = 2;

	{
		image = EXItemSpriteSheet.RADIANT_GOLD_HALBERD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.8f;
		tier = TIER;
		ACC = ACCURACY;
		DLY = DELAY;
		RCH = RANGE;
	}

	static int effectiveLevel(int level) {
		return Math.max(0, level);
	}

	public static int minForLevel(int level) {
		int l = effectiveLevel(level);
		return 6 + l + l / 3 + Math.max(0, l - 11);
	}

	public static int maxForLevel(int level) {
		int l = effectiveLevel(level);
		return 48 + 9 * l + 2 * (l / 3) + 4 * Math.max(0, l - 11);
	}

	public static int strengthRequirementForLevel(int level) {
		int l = effectiveLevel(level);
		return 22 - (l >= 9 ? 1 : 0) - (l >= 15 ? 1 : 0);
	}

	@Override
	public int min(int level) {
		return minForLevel(level);
	}

	@Override
	public int max(int level) {
		return maxForLevel(level);
	}

	@Override
	public int STRReq(int level) {
		int requirement = strengthRequirementForLevel(level);
		return masteryPotionBonus ? requirement - 2 : requirement;
	}

	public static int abilityMin(int level) {
		return 7 + effectiveLevel(level);
	}

	public static int abilityMax(int level) {
		return 7 + 11 * effectiveLevel(level);
	}

	public static boolean abilityTargetAllowed(boolean alive, Char.Alignment alignment,
			boolean charmed, boolean onPath) {
		return alive && alignment == Char.Alignment.ENEMY && !charmed && onPath;
	}

	public static boolean wallCollision(boolean boundary, boolean terrainObstacle) {
		return boundary || terrainObstacle;
	}

	public static float wallDamageMultiplier(boolean wallCollision) {
		return wallCollision ? 1.2f : 1f;
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return ABILITY_CHARGE_COST;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public String abilityInfo() {
		int level = levelKnown ? buffedLvl() : 0;
		return Messages.get(this, levelKnown ? "ability_desc" : "typical_ability_desc",
				abilityMin(level), abilityMax(level));
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return abilityMin(level) + "-" + abilityMax(level);
	}

	static float splashDamageMultiplier(int targetCount) {
		if (targetCount <= 1) return 1.25f;
		if (targetCount == 2) return 1.12f;
		return 1f;
	}

	static boolean canStartSplash(boolean abilityDamageActive, boolean splashResolving,
			boolean heroAttacker) {
		return !abilityDamageActive && !splashResolving && heroAttacker;
	}

	static boolean splashTargetAllowed(boolean alive, Char.Alignment alignment,
			boolean charmed, boolean primaryTarget, boolean attacker) {
		return alive && alignment == Char.Alignment.ENEMY && !charmed
				&& !primaryTarget && !attacker;
	}

	public static int falsehoodPowerReduction(boolean hasTalent) {
		return hasTalent ? 2 : 0;
	}

	@Override
	protected int falsehoodPowerEncumbranceReduction(Hero owner) {
		return falsehoodPowerReduction(owner.hasTalent(Talent.FALSEHOOD_POWER));
	}

	private transient boolean abilityResolving;
	private transient boolean abilityDamageActive;
	private transient int currentAbilityTargetId = -1;
	private transient boolean splashResolving;
	private transient float activeSplashMultiplier = 1f;
	private transient SplashAttack pendingSplash;

	@Override
	public int damageRoll(Char owner) {
		if (!abilityDamageActive) return super.damageRoll(owner);
		int damage = augment.damageFactor(Random.NormalIntRange(
				abilityMin(buffedLvl()), abilityMax(buffedLvl())));
		if (owner instanceof Hero) {
			int excessStrength = ((Hero) owner).STR() - STRReq();
			if (excessStrength > 0) {
				damage += Hero.heroDamageIntRange(0, excessStrength);
			}
		}
		return damage;
	}

	ArrayList<Char> collectSplashTargets(Char attacker, Char primaryTarget) {
		ArrayList<Char> targets = new ArrayList<>();
		if (attacker == null || primaryTarget == null || Dungeon.level == null) return targets;

		for (int offset : PathFinder.NEIGHBOURS4) {
			int cell = primaryTarget.pos + offset;
			if (!isCardinalNeighbor(primaryTarget.pos, cell)) continue;

			Char candidate = Actor.findChar(cell);
			if (candidate != null && splashTargetAllowed(candidate.isAlive(), candidate.alignment,
					attacker.isCharmedBy(candidate), candidate == primaryTarget, candidate == attacker)) {
				targets.add(candidate);
			}
		}

		Random.shuffle(targets);
		while (targets.size() > MAX_SPLASH_TARGETS) {
			targets.remove(targets.size() - 1);
		}
		return targets;
	}

	private boolean isCardinalNeighbor(int center, int cell) {
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()
				|| Dungeon.level.distance(center, cell) != 1) {
			return false;
		}
		for (int offset : PathFinder.NEIGHBOURS4) {
			if (center + offset == cell) return true;
		}
		return false;
	}

	@Override
	public void beforeHeroAttack(Hero hero, Char target) {
		if (!splashResolving) pendingSplash = null;
	}

	@Override
	public void afterHeroAttack(Hero hero, Char target, boolean hit) {
		if (splashResolving) return;

		SplashAttack attack = pendingSplash;
		pendingSplash = null;
		if (!hit || attack == null || target == null || target.id() != attack.primaryTargetId) {
			return;
		}

		splashResolving = true;
		activeSplashMultiplier = attack.damageMultiplier;
		try {
			for (int targetId : attack.targetIds) {
				Char splashTarget = Actor.findCharById(targetId);
				if (splashTarget == null || !isCardinalNeighbor(attack.centerCell, splashTarget.pos)
						|| !splashTargetAllowed(splashTarget.isAlive(), splashTarget.alignment,
						hero.isCharmedBy(splashTarget), splashTarget == target,
						splashTarget == hero)) {
					continue;
				}
				hero.chooseEnemy(splashTarget);
				hero.attack(splashTarget, 1f, 0f, Char.INFINITE_ACCURACY);
			}
		} finally {
			hero.chooseEnemy(target);
			activeSplashMultiplier = 1f;
			splashResolving = false;
		}
	}

	@Override
	protected void duelistAbility(final Hero hero, Integer selectedCell) {
		AbilityLine line = buildLine(hero, selectedCell);
		if (line == null) {
			GLog.w(Messages.get(this, "ability_no_path"));
			return;
		}
		beforeAbilityUsed(hero, null);
		hero.busy();
		abilityResolving = true;
		abilityDamageActive = false;
		Sample.INSTANCE.play(Assets.Sounds.MISS);
		launchProjection(hero, line.endCell,
				() -> resolveLineTarget(hero, line, 0));
	}

	private void launchProjection(Hero hero, int endCell, Callback callback) {
		if (hero.sprite == null || hero.sprite.parent == null) {
			callback.call();
			return;
		}
		((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class))
				.reset(hero.sprite, endCell, this, callback);
	}

	private AbilityLine buildLine(Hero hero, Integer selectedCell) {
		if (selectedCell == null || Dungeon.level == null
				|| !validDirectionCell(hero.pos, selectedCell, Dungeon.level.length())) {
			return null;
		}
		Ballistica trajectory = new Ballistica(hero.pos, selectedCell,
				Ballistica.STOP_SOLID);
		int travelEnd = 0;
		for (int i = 1; i <= trajectory.dist && i < trajectory.path.size(); i++) {
			int cell = trajectory.path.get(i);
			if (terrainObstacle(cell)) break;
			travelEnd = i;
		}
		if (travelEnd == 0) return null;

		HashMap<Integer, Integer> pathIndices = new HashMap<>();
		for (int i = 1; i <= travelEnd; i++) {
			pathIndices.put(trajectory.path.get(i), i);
		}
		ArrayList<LineTarget> targets = collectLineTargets(hero, trajectory, pathIndices);
		return new AbilityLine(trajectory.path.get(travelEnd), targets);
	}

	private boolean terrainObstacle(int cell) {
		return cell < 0 || cell >= Dungeon.level.length()
				|| Dungeon.level.solid[cell]
				|| (!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]);
	}

	private ArrayList<LineTarget> collectLineTargets(Hero hero,
			Ballistica trajectory, HashMap<Integer, Integer> pathIndices) {
		ArrayList<LineTarget> targets = new ArrayList<>();
		for (Char target : Actor.chars()) {
			Integer pathIndex = pathIndices.get(target.pos);
			if (!abilityTargetAllowed(target.isAlive(), target.alignment,
					hero.isCharmedBy(target), pathIndex != null)) continue;
			int nextIndex = pathIndex + 1;
			int nextCell = nextIndex < trajectory.path.size()
					? trajectory.path.get(nextIndex) : -1;
			boolean boundary = nextCell < 0 || !Dungeon.level.insideMap(nextCell);
			boolean obstacle = !boundary && terrainObstacle(nextCell);
			targets.add(new LineTarget(target.id(), target.pos, pathIndex, nextCell,
					wallCollision(boundary, obstacle)));
		}
		Collections.sort(targets, (left, right) ->
				Integer.compare(right.pathIndex, left.pathIndex));
		return targets;
	}

	private void resolveLineTarget(final Hero hero, final AbilityLine line, int index) {
		if (!abilityResolving || index >= line.targets.size()) {
			finishAbility(hero);
			return;
		}
		LineTarget record = line.targets.get(index);
		Char target = Actor.findCharById(record.actorId);
		if (!validRecordedTarget(hero, target, record)) {
			resolveLineTarget(hero, line, index + 1);
			return;
		}

		abilityDamageActive = true;
		currentAbilityTargetId = target.id();
		boolean hit;
		try {
			hit = hero.attack(target, wallDamageMultiplier(record.wallCollision),
					0f, Char.INFINITE_ACCURACY);
		} finally {
			abilityDamageActive = false;
			currentAbilityTargetId = -1;
		}

		if (hit && !target.isAlive()) onAbilityKill(hero, target);
		if (!hit || !target.isAlive() || record.wallCollision
				|| record.nextCell < 0 || target.pos != record.expectedCell
				|| Pushing.pushingExistsForChar(target)) {
			resolveLineTarget(hero, line, index + 1);
			return;
		}

		Ballistica push = new Ballistica(target.pos, record.nextCell,
				Ballistica.PROJECTILE);
		WandOfBlastWave.throwCharImmediately(target, push, 1,
				true, false, this,
				() -> resolveLineTarget(hero, line, index + 1));
	}

	private boolean validRecordedTarget(Hero hero, Char target, LineTarget record) {
		return target != null && target.id() == record.actorId
				&& target.pos == record.expectedCell
				&& abilityTargetAllowed(target.isAlive(), target.alignment,
						hero.isCharmedBy(target), true);
	}

	private void finishAbility(Hero hero) {
		if (!abilityResolving) return;
		abilityResolving = false;
		abilityDamageActive = false;
		currentAbilityTargetId = -1;
		Invisibility.dispel();
		hero.spendAndNext(hero.attackDelay());
		afterAbilityUsed(hero);
	}

	static boolean validDirectionCell(int heroCell, int selectedCell, int levelLength) {
		return selectedCell >= 0 && selectedCell < levelLength && selectedCell != heroCell;
	}

	private static final class LineTarget {
		final int actorId;
		final int expectedCell;
		final int pathIndex;
		final int nextCell;
		final boolean wallCollision;

		LineTarget(int actorId, int expectedCell, int pathIndex, int nextCell,
				boolean wallCollision) {
			this.actorId = actorId;
			this.expectedCell = expectedCell;
			this.pathIndex = pathIndex;
			this.nextCell = nextCell;
			this.wallCollision = wallCollision;
		}
	}

	private static final class AbilityLine {
		final int endCell;
		final ArrayList<LineTarget> targets;

		AbilityLine(int endCell, ArrayList<LineTarget> targets) {
			this.endCell = endCell;
			this.targets = targets;
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int result = super.proc(attacker, defender, damage);
		if (splashResolving) {
			return Math.round(result * activeSplashMultiplier);
		}
		if (!canStartSplash(abilityDamageActive, splashResolving, attacker instanceof Hero)
				|| !defender.isAlive() || defender.alignment != Char.Alignment.ENEMY) {
			return result;
		}

		Hero hero = (Hero) attacker;
		if (hero.isCharmedBy(defender)) return result;
		ArrayList<Char> splashTargets = collectSplashTargets(hero, defender);
		float multiplier = splashDamageMultiplier(1 + splashTargets.size());
		pendingSplash = new SplashAttack(defender.id(), defender.pos, multiplier, splashTargets);
		return Math.round(result * multiplier);
	}

	private static final class SplashAttack {
		final int primaryTargetId;
		final int centerCell;
		final float damageMultiplier;
		final int[] targetIds;

		SplashAttack(int primaryTargetId, int centerCell, float damageMultiplier,
				ArrayList<Char> targets) {
			this.primaryTargetId = primaryTargetId;
			this.centerCell = centerCell;
			this.damageMultiplier = damageMultiplier;
			targetIds = new int[targets.size()];
			for (int i = 0; i < targets.size(); i++) {
				targetIds[i] = targets.get(i).id();
			}
		}
	}

}
