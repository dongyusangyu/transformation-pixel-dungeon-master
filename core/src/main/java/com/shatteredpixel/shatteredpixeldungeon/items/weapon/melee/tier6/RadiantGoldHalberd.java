package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
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
import com.watabou.utils.Random;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/** The ponderous tier-six radiant gold halberd. */
public class RadiantGoldHalberd extends MeleeWeapon {

	public static final int TIER = 6;
	public static final float ACCURACY = 1f;
	public static final float DELAY = 2f;
	public static final int RANGE = 3;
	private static final int ABILITY_CHARGE_COST = 2;

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
		return 48 + 9 * l + 2 * (l / 3) + 2 * Math.max(0, l - 11);
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

	public static float controlChance(boolean surprised) {
		return surprised ? 0.75f : 0.25f;
	}

	public static float dazeChance(boolean surprised) {
		return surprised ? 0.45f : 0.15f;
	}

	public static boolean triggers(float roll, float chance) {
		return roll < chance;
	}

	public static int bleedingAmountForLevel(int level) {
		return 3 * effectiveLevel(level);
	}

	public static int falsehoodPowerReduction(boolean hasTalent) {
		return hasTalent ? 2 : 0;
	}

	@Override
	protected int falsehoodPowerEncumbranceReduction(Hero owner) {
		return falsehoodPowerReduction(owner.hasTalent(Talent.FALSEHOOD_POWER));
	}

	public static void applyPassiveEffects(Char defender, int level, boolean surprised,
			float controlRoll, float dazeRoll) {
		int l = effectiveLevel(level);
		if (triggers(controlRoll, controlChance(surprised))) {
			Buff.prolong(defender, Cripple.class, 3f);
			if (l > 0) {
				Buff.affect(defender, Bleeding.class).set(bleedingAmountForLevel(l), RadiantGoldHalberd.class);
			}
		}
		if (triggers(dazeRoll, dazeChance(surprised))) {
			Buff.prolong(defender, Daze.class, 3f);
		}
	}

	private transient boolean abilityResolving;
	private transient boolean abilityDamageActive;
	private transient int currentAbilityTargetId = -1;
	private transient HashSet<Integer> abilitySurprisedTargetIds = new HashSet<>();

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

	private boolean isSurprisedHit(Char attacker, Char defender) {
		if (abilityDamageActive && defender.id() == currentAbilityTargetId) {
			return abilitySurprisedTargetIds.contains(defender.id());
		}
		return defender instanceof Mob && ((Mob) defender).surprisedBy(attacker);
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
		abilitySurprisedTargetIds.clear();
		for (LineTarget target : line.targets) {
			if (target.surprised) abilitySurprisedTargetIds.add(target.actorId);
		}
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
			boolean surprised = target instanceof Mob
					&& ((Mob) target).surprisedBy(hero);
			targets.add(new LineTarget(target.id(), target.pos, pathIndex, nextCell,
					surprised, wallCollision(boundary, obstacle)));
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
		abilitySurprisedTargetIds.clear();
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
		final boolean surprised;
		final boolean wallCollision;

		LineTarget(int actorId, int expectedCell, int pathIndex, int nextCell,
				boolean surprised, boolean wallCollision) {
			this.actorId = actorId;
			this.expectedCell = expectedCell;
			this.pathIndex = pathIndex;
			this.nextCell = nextCell;
			this.surprised = surprised;
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
		if (!defender.isAlive() || defender.alignment != Char.Alignment.ENEMY) {
			return result;
		}
		boolean surprised = isSurprisedHit(attacker, defender);
		applyPassiveEffects(defender, buffedLvl(), surprised,
				Random.Float(), Random.Float());
		return result;
	}

}
