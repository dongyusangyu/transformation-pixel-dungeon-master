/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * A tier-six heavy weapon whose iron ball is also represented by a following ally.
 * All gameplay and rendering code for the follower intentionally lives in this file.
 */
public class ChainMace extends MeleeWeapon {

	private static final String FOLLOWER_ID = "follower_id";

	private BallFollower follower;
	private int followerID;
	private final CommandState commandState = new CommandState();
	private transient Level commandLevel;
	private transient Level followerLevel;
	private transient int nextCommandToken;
	private transient int activeCommandToken;

	{
		image = EXItemSpriteSheet.CHAIN_MACE;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 0.9f;
		tier = 6;
		RCH = 1;
		usesTargeting = true;
	}

	@Override
	public int min(int lvl) {
		return 6 + lvl;
	}

	@Override
	public int max(int lvl) {
		return 50 + 7 * lvl;
	}

	@Override
	public int STRReq(int lvl) {
		int req = STRReq(tier + 1, lvl);
		if (masteryPotionBonus) req -= 2;
		return req;
	}

	@Override
	public String statsInfo() {
		return Messages.get(this, "stats_desc");
	}

	@Override
	public String defaultAction() {
		if (canUseWeaponAbility(Dungeon.hero)) return AC_ABILITY;
		return AC_THROW;
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, levelKnown ? "ability_desc" : "typical_ability_desc");
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Messages.get(this, "upgrade_ability_stat");
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);
		if (Dungeon.hero != null && attacker == Dungeon.hero
				&& Dungeon.hero.belongings.attackingWeapon() == this
				&& Dungeon.hero.belongings.thrownWeapon != this
				&& Dungeon.hero.belongings.abilityWeapon != this) {
			playMeleeHitFeedback();
		}
		return damage;
	}

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		if (ch instanceof Hero) {
			BallFollower ball = ensureFollower((Hero) ch);
			if (ball != null && commandState.isReturning()) {
				ball.returning = true;
				ball.followHero();
			}
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (!super.doUnequip(hero, collect, single)) return false;
		cancelActiveCommand(hero);
		removeFollower();
		commandState.finishReturn();
		commandLevel = null;
		return true;
	}

	/**
	 * Throwing an equipped chain mace commands its follower instead of unequipping and
	 * dropping the weapon. Throwing an unequipped copy keeps the standard item behavior.
	 */
	@Override
	public void cast(Hero user, int dst) {
		if (!isEquipped(user)) {
			super.cast(user, dst);
			return;
		}

		BallFollower ball = ensureFollower(user);
		Char target = validEnemyAt(user, dst);
		if (ball == null || !ball.isAlive()) {
			GLog.w(Messages.get(this, "no_follower"));
		} else if (!commandState.canDispatch()) {
			GLog.w(Messages.get(this, "follower_busy"));
		} else if (target == null) {
			GLog.w(Messages.get(this, "invalid_target"));
		} else {
			dispatchSingleAttack(user, ball, target);
		}
	}

	@Override
	protected void duelistAbility(Hero hero, Integer ignored) {
		BallFollower ball = ensureFollower(hero);
		if (ball == null || !ball.isAlive()) {
			GLog.w(Messages.get(this, "no_follower"));
			return;
		}
		if (!commandState.canDispatch()) {
			GLog.w(Messages.get(this, "follower_busy"));
			return;
		}

		int[] targets = visibleEnemyOrder(hero);
		if (targets.length == 0) {
			GLog.w(Messages.get(this, "no_enemies"));
			return;
		}

		beforeAbilityUsed(hero, null);
		commandState.beginAbility();
		commandLevel = Dungeon.level;
		int commandToken = beginCommand();
		hero.busy();
		playLaunchFeedback();
		dispatchAbilityStep(hero, ball, targets, 0, ball.pos, commandToken);
	}

	private void dispatchSingleAttack(final Hero hero, final BallFollower ball,
			final Char target) {
		commandState.beginSingleAttack();
		commandLevel = Dungeon.level;
		final int commandToken = beginCommand();
		hero.busy();
		playLaunchFeedback();
		final int from = ball.pos;
		jumpRapidly(ball, from, target.pos, new Callback() {
			@Override
			public void call() {
				if (!isCommandValid(hero, ball, commandToken)) {
					abortCommand(hero, ball, false, commandToken);
					return;
				}
				landFollower(ball, target.pos);
				KindOfWeapon previous = hero.belongings.thrownWeapon;
				boolean hit = false;
				float attackDelay;
				try {
					hero.belongings.thrownWeapon = ChainMace.this;
					if (isVisibleEnemy(hero, target)) {
						hit = hero.attack(target);
					}
					attackDelay = hero.attackDelay();
				} finally {
					hero.belongings.thrownWeapon = previous;
				}
				Invisibility.dispel();
				if (hit) {
					Sample.INSTANCE.play(Assets.Sounds.HIT_CRUSH, 1f, 0.9f);
					playHitFeedback();
				}
				commandState.beginReturn();
				ball.returning = true;
				ball.followHero();
				commandLevel = null;
				activeCommandToken = 0;
				hero.spendAndNext(attackDelay);
			}
		});
	}

	private void dispatchAbilityStep(final Hero hero, final BallFollower ball,
			final int[] targetIDs, final int index, final int visualFrom,
			final int commandToken) {
		if (!isCommandValid(hero, ball, commandToken)) {
			abortCommand(hero, ball, true, commandToken);
			return;
		}
		if (index >= targetIDs.length) {
			returnFollower(hero, ball, visualFrom, commandToken);
			return;
		}

		Actor actor = Actor.findById(targetIDs[index]);
		if (!(actor instanceof Char) || !isVisibleEnemy(hero, (Char) actor)) {
			dispatchAbilityStep(hero, ball, targetIDs, index + 1, visualFrom, commandToken);
			return;
		}

		final Char target = (Char) actor;
		jumpRapidly(ball, visualFrom, target.pos, new Callback() {
			@Override
			public void call() {
				if (!isCommandValid(hero, ball, commandToken)) {
					abortCommand(hero, ball, true, commandToken);
					return;
				}
				landFollower(ball, target.pos);
				if (isVisibleEnemy(hero, target)) {
					if (hero.attack(target, 1f, 0f, Char.INFINITE_ACCURACY)) {
						Sample.INSTANCE.play(Assets.Sounds.HIT_CRUSH, 1f, 0.9f);
						playHitFeedback();
					}
				}
				dispatchAbilityStep(hero, ball, targetIDs, index + 1, target.pos, commandToken);
			}
		});
	}

	private void returnFollower(final Hero hero, final BallFollower ball, int visualFrom,
			final int commandToken) {
		final int returnCell = safeReturnCell(hero, ball);
		Callback finish = new Callback() {
			@Override
			public void call() {
				if (!isCommandValid(hero, ball, commandToken)) {
					abortCommand(hero, ball, true, commandToken);
					return;
				}
				landFollower(ball, returnCell);
				commandState.finishReturn();
				ball.returning = false;
				commandLevel = null;
				activeCommandToken = 0;
				Invisibility.dispel();
				afterAbilityUsed(hero);
				hero.spendAndNext(Actor.TICK);
			}
		};

		if (ball.sprite == null || visualFrom == returnCell) {
			finish.call();
		} else {
			jumpRapidly(ball, visualFrom, returnCell, finish);
		}
	}

	private void landFollower(BallFollower ball, int cell) {
		ball.pos = cell;
		Dungeon.level.occupyCell(ball);
		if (ball.sprite != null) ball.sprite.place(cell);
	}

	private int safeReturnCell(Hero hero, BallFollower ball) {
		for (int direction : PathFinder.NEIGHBOURS8) {
			int cell = hero.pos + direction;
			if (cell >= 0 && cell < Dungeon.level.length()
					&& (Dungeon.level.passable[cell] || Dungeon.level.avoid[cell])) {
				Char occupant = Actor.findChar(cell);
				if (occupant == null || occupant == ball) return cell;
			}
		}
		return hero.pos;
	}

	private int beginCommand() {
		if (++nextCommandToken == 0) nextCommandToken = 1;
		return activeCommandToken = nextCommandToken;
	}

	private boolean isCommandValid(Hero hero, BallFollower ball, int commandToken) {
		return commandToken != 0 && commandToken == activeCommandToken
				&& commandLevel != null && Dungeon.level == commandLevel
				&& Dungeon.hero == hero && hero.isAlive() && isEquipped(hero)
				&& ball != null && ball.isAlive() && getFollower() == ball
				&& ball.linkedWeapon() == this;
	}

	private void abortCommand(Hero hero, BallFollower ball, boolean ability, int commandToken) {
		if (commandToken == 0 || commandToken != activeCommandToken) return;
		Level startedLevel = commandLevel;
		activeCommandToken = 0;
		if (ability && hero.belongings.abilityWeapon == this) {
			hero.belongings.abilityWeapon = null;
		}
		if (ball != null && ball.isAlive() && ball.linkedWeapon() == this
				&& Dungeon.level == startedLevel) {
			commandState.beginReturn();
			ball.returning = true;
			ball.followHero();
		} else {
			commandState.finishReturn();
		}
		commandLevel = null;
		if (Dungeon.hero == hero && Dungeon.level == startedLevel && hero.isAlive()) {
			hero.spendAndNext(ability ? Actor.TICK : hero.attackDelay());
		}
	}

	private void cancelActiveCommand(Hero hero) {
		if (activeCommandToken == 0) return;
		activeCommandToken = 0;
		commandLevel = null;
		commandState.finishReturn();
		if (hero.belongings.abilityWeapon == this) hero.belongings.abilityWeapon = null;
	}

	private void jumpRapidly(BallFollower ball, int from, int to, Callback callback) {
		if (ball.sprite == null || from == to) {
			callback.call();
			return;
		}
		float distance = Math.max(1f, Dungeon.level.trueDistance(from, to));
		ball.sprite.jump(from, to, distance * 2f, Math.max(0.04f, distance * 0.03f), callback);
	}

	private void playLaunchFeedback() {
		Sample.INSTANCE.play(Assets.Sounds.CHAINS);
	}

	private void playHitFeedback() {
		PixelScene.shake(2, 0.33f);
	}

	private void playMeleeHitFeedback() {
		PixelScene.shake(2, 0.25f);
	}

	private Char validEnemyAt(Hero hero, int cell) {
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()
				|| !Dungeon.level.heroFOV[cell]) return null;
		Char target = Actor.findChar(cell);
		return isCommandTarget(hero, target) ? target : null;
	}

	private boolean isVisibleEnemy(Hero hero, Char target) {
		return Dungeon.level != null && target != null && target != hero && target.isAlive()
				&& target.alignment == Char.Alignment.ENEMY
				&& target.pos >= 0 && target.pos < Dungeon.level.length()
				&& Dungeon.level.heroFOV[target.pos];
	}

	static boolean commandTargetAllowed(boolean visible, boolean enemy,
			boolean projecting, int targetCell, int collisionCell) {
		return visible && enemy && (projecting || targetCell == collisionCell);
	}

	private boolean isCommandTarget(Hero hero, Char target) {
		boolean visibleEnemy = isVisibleEnemy(hero, target);
		if (!visibleEnemy) return false;
		boolean projecting = hasEnchant(Projecting.class, hero);
		int collisionCell = target.pos;
		if (!projecting) {
			collisionCell = new Ballistica(hero.pos, target.pos, Ballistica.PROJECTILE).collisionPos;
		}
		return commandTargetAllowed(true, target.alignment == Char.Alignment.ENEMY,
				projecting, target.pos, collisionCell);
	}

	private int[] visibleEnemyOrder(Hero hero) {
		ArrayList<Char> enemies = new ArrayList<>();
		for (Char ch : Actor.chars()) {
			if (isCommandTarget(hero, ch)) enemies.add(ch);
		}
		int[] ids = new int[enemies.size()];
		int[] cells = new int[enemies.size()];
		for (int i = 0; i < enemies.size(); i++) {
			ids[i] = enemies.get(i).id();
			cells[i] = enemies.get(i).pos;
		}
		return stableNearestTargetOrder(hero.pos, Dungeon.level.width(), ids, cells);
	}

	static int[] stableNearestTargetOrder(int origin, int width, int[] ids, int[] cells) {
		if (ids.length != cells.length) throw new IllegalArgumentException("ids/cells length mismatch");
		int[] ordered = new int[ids.length];
		boolean[] used = new boolean[ids.length];
		int current = origin;
		for (int out = 0; out < ordered.length; out++) {
			int best = -1;
			int bestDistance = Integer.MAX_VALUE;
			int bestID = Integer.MAX_VALUE;
			for (int i = 0; i < ids.length; i++) {
				if (used[i]) continue;
				int distance = gridDistance(current, cells[i], width);
				if (distance < bestDistance || distance == bestDistance && ids[i] < bestID) {
					best = i;
					bestDistance = distance;
					bestID = ids[i];
				}
			}
			used[best] = true;
			ordered[out] = ids[best];
			current = cells[best];
		}
		return ordered;
	}

	private static int gridDistance(int first, int second, int width) {
		int dx = Math.abs(first % width - second % width);
		int dy = Math.abs(first / width - second / width);
		return Math.max(dx, dy);
	}

	private BallFollower getFollower() {
		if (follower == null && followerID != 0) {
			Actor actor = Actor.findById(followerID);
			if (actor instanceof BallFollower) {
				follower = (BallFollower) actor;
			} else if (Dungeon.level != null) {
				// Equipped items activate before restored actors are registered during loading.
				follower = findFollowerInLevel(followerID, Dungeon.level.mobs);
			}
			if (follower == null && actor != null) {
				followerID = 0;
			}
		}
		return follower;
	}

	static BallFollower findFollowerInLevel(int id, Iterable<? extends Char> characters) {
		if (id == 0 || characters == null) return null;
		for (Char character : characters) {
			if (character instanceof BallFollower && character.id() == id) {
				return (BallFollower) character;
			}
		}
		return null;
	}

	private BallFollower ensureFollower(Hero hero) {
		if (hero == null || !isEquipped(hero)) return null;
		BallFollower existing = getFollower();
		if (existing != null && existing.isAlive() && Dungeon.level != null) {
			existing.owner = hero;
			if (followerLevel != null && followerLevel != Dungeon.level) {
				if (Dungeon.level.mobs.contains(existing)) {
					reconcileFollowerLevel(existing);
					return existing;
				}
				discardStaleFollower(existing, hero);
				existing = null;
			}
			if (existing != null && Dungeon.level.mobs.contains(existing)) {
				reconcileFollowerLevel(existing);
				return existing;
			}
		}
		follower = null;
		if (Dungeon.level == null || !hero.isAlive()) return null;
		followerID = 0;

		ArrayList<Integer> spawnPoints = new ArrayList<>();
		for (int direction : PathFinder.NEIGHBOURS8) {
			int cell = hero.pos + direction;
			if (cell >= 0 && cell < Dungeon.level.length()
					&& Actor.findChar(cell) == null
					&& (Dungeon.level.passable[cell] || Dungeon.level.avoid[cell])) {
				spawnPoints.add(cell);
			}
		}
		int spawnCell = hero.pos;
		if (!spawnPoints.isEmpty()) spawnCell = Random.element(spawnPoints);
		follower = new BallFollower();
		follower.owner = hero;
		follower.pos = spawnCell;
		follower.returning = commandState.isReturning();
		follower.followHero();
		followerID = follower.id();
		followerLevel = Dungeon.level;
		GameScene.add(follower, 1f);
		Dungeon.level.occupyCell(follower);
		return follower;
	}

	private void reconcileFollowerLevel(BallFollower ball) {
		Level currentLevel = Dungeon.level;
		if (currentLevel == null) return;
		if (followerLevel == null) {
			followerLevel = currentLevel;
			return;
		}
		if (followerLevel == currentLevel) return;

		Level previousLevel = followerLevel;
		previousLevel.mobs.remove(ball);
		followerLevel = currentLevel;
		Hero hero = Dungeon.hero;
		if (hero != null && (ball.pos < 0 || ball.pos >= currentLevel.length()
				|| currentLevel.distance(ball.pos, hero.pos) > 1)) {
			landFollower(ball, safeReturnCell(hero, ball));
		}

		clearCommandAfterLevelChange(hero);
		ball.returning = false;
	}

	private void discardStaleFollower(BallFollower ball, Hero hero) {
		Level previousLevel = followerLevel;
		if (previousLevel != null) previousLevel.mobs.remove(ball);
		if (ball.sprite != null) ball.sprite.killAndErase();
		ball.destroy();
		follower = null;
		followerID = 0;
		followerLevel = null;
		clearCommandAfterLevelChange(hero);
	}

	private void clearCommandAfterLevelChange(Hero hero) {
		activeCommandToken = 0;
		commandLevel = null;
		commandState.finishReturn();
		if (hero != null && hero.belongings.abilityWeapon == this) {
			hero.belongings.abilityWeapon = null;
		}
	}

	private void onFollowerReturned(BallFollower ball) {
		if (ball == getFollower() && ball.id() == followerID) {
			ball.returning = false;
			commandState.finishReturn();
			updateQuickslot();
		}
	}

	private void removeFollower() {
		BallFollower ball = getFollower();
		if (ball != null) {
			if (ball.sprite != null) ball.sprite.killAndErase();
			ball.destroy();
		}
		follower = null;
		followerID = 0;
		commandLevel = null;
		followerLevel = null;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		BallFollower ball = getFollower();
		if (ball != null) followerID = ball.id();
		bundle.put(FOLLOWER_ID, followerID);
		commandState.storeInBundle(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		follower = null;
		followerID = bundle.getInt(FOLLOWER_ID);
		commandLevel = null;
		followerLevel = null;
		activeCommandToken = 0;
		commandState.restoreFromBundle(bundle);
	}

	static class CommandState {

		private static final String COMMAND_MODE = "chain_mace_command_mode";
		private static final int IDLE = 0;
		private static final int SINGLE_ATTACK = 1;
		private static final int ABILITY = 2;
		private static final int RETURNING = 3;

		private int mode = IDLE;

		boolean canDispatch() {
			return mode == IDLE;
		}

		void beginSingleAttack() {
			mode = SINGLE_ATTACK;
		}

		void beginAbility() {
			mode = ABILITY;
		}

		void beginReturn() {
			mode = RETURNING;
		}

		boolean isReturning() {
			return mode == RETURNING;
		}

		void finishReturn() {
			mode = IDLE;
		}

		void storeInBundle(Bundle bundle) {
			bundle.put(COMMAND_MODE, mode);
		}

		void restoreFromBundle(Bundle bundle) {
			int restored = bundle.getInt(COMMAND_MODE);
			mode = restored == IDLE ? IDLE : RETURNING;
		}
	}

	public static class BallFollower extends DirectableAlly {

		private static final String OWNER_ID = "chain_mace_owner_id";
		private static final String RETURNING = "chain_mace_returning";
		private Hero owner;
		private boolean returning;

		{
			spriteClass = BallFollowerSprite.class;
			alignment = Alignment.NEUTRAL;
			HP = HT = 999666;
			EXP = 0;
			maxLvl = -2;
			flying = true;
			defenseSkill = 999;
			attacksAutomatically = false;
			immunities.add(AllyBuff.class);
			immunities.add(Amok.class);
			immunities.add(Charm.class);
			immunities.add(StoneOfAggression.Aggression.class);
		}

		@Override
		public float speed() {
			return 4f;
		}

		@Override
		protected boolean act() {
			if (owner == null) owner = Dungeon.hero;
			ChainMace weapon = linkedWeapon();
			if (owner == null || !owner.isAlive() || weapon == null) {
				if (sprite != null) sprite.killAndErase();
				destroy();
				return true;
			}
			weapon.reconcileFollowerLevel(this);

			if (overlapsAnotherCharacter() && moveToSafeAdjacent()) {
				spend(TICK);
				if (returning && Dungeon.level.distance(pos, owner.pos) <= 1) {
					weapon.onFollowerReturned(this);
				}
				return true;
			}

			if (returning) {
				if (Dungeon.level.distance(pos, owner.pos) <= 1) {
					weapon.onFollowerReturned(this);
					spend(TICK);
					return true;
				}
				if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
					fieldOfView = new boolean[Dungeon.level.length()];
				}
				Dungeon.level.updateFieldOfView(this, fieldOfView);
				int oldPos = pos;
				if (getCloser(owner.pos)) {
					spend(TICK / speed());
					boolean result = moveSprite(oldPos, pos);
					if (Dungeon.level.distance(pos, owner.pos) <= 1) {
						weapon.onFollowerReturned(this);
					}
					return result;
				}
				spend(TICK);
				return true;
			}

			followHero();
			return super.act();
		}

		private ChainMace linkedWeapon() {
			if (owner == null) return null;
			KindOfWeapon primary = owner.belongings.weapon;
			if (primary instanceof ChainMace
					&& ((ChainMace) primary).followerID == id()) return (ChainMace) primary;
			KindOfWeapon secondary = owner.belongings.secondWep;
			if (secondary instanceof ChainMace
					&& ((ChainMace) secondary).followerID == id()) return (ChainMace) secondary;
			return null;
		}

		private boolean overlapsAnotherCharacter() {
			for (Char ch : Actor.chars()) {
				if (ch != this && ch.pos == pos) return true;
			}
			return false;
		}

		private boolean moveToSafeAdjacent() {
			for (int direction : PathFinder.NEIGHBOURS8) {
				int cell = pos + direction;
				if (cell >= 0 && cell < Dungeon.level.length()
						&& Actor.findChar(cell) == null
						&& (Dungeon.level.passable[cell] || Dungeon.level.avoid[cell])) {
					int oldPos = pos;
					move(cell);
					if (sprite != null) sprite.move(oldPos, pos);
					return true;
				}
			}
			return false;
		}

		@Override
		public void directTocell(int cell) {
			followHero();
		}

		@Override
		protected boolean canAttack(Char enemy) {
			return false;
		}

		@Override
		protected Char chooseEnemy() {
			return null;
		}

		@Override
		public boolean add(Buff buff) {
			return false;
		}

		@Override
		public boolean isInvulnerable(Class effect) {
			return true;
		}

		@Override
		public boolean blocksBallistica() {
			return false;
		}

		@Override
		public float spawningWeight() {
			return 0f;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			if (owner != null) bundle.put(OWNER_ID, owner.id());
			bundle.put(RETURNING, returning);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			alignment = Alignment.NEUTRAL;
			Actor actor = Actor.findById(bundle.getInt(OWNER_ID));
			if (actor instanceof Hero) owner = (Hero) actor;
			returning = bundle.getBoolean(RETURNING);
		}
	}

	public static class BallFollowerSprite extends MobSprite {

		public BallFollowerSprite() {
			texture("sprites/ball.png");
			TextureFilm frames = new TextureFilm(texture, 16, 16);

			idle = new Animation(1, true);
			idle.frames(frames, 0);
			run = idle.clone();
			attack = idle.clone();
			zap = idle.clone();
			die = idle.clone();

			play(idle);
		}
	}
}
