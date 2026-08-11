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
 * any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.EarthlySerpentSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class EarthlySerpent extends Mob implements RangedAttack {

	private static final int BASE_HT = 240;
	private static final int MAX_HT = BASE_HT * 2;
	private static final int MAX_GROWTH_DAMAGE = 120;
	private static final float BASE_ATTACK_DELAY = 0.5f;
	private static final float REGENERATION_INTERVAL = 1f;

	private int meleeDamageDealt;
	private float regenerationProgress;
	private float lastRegenerationTime = Float.NaN;
	private boolean pullUsed;
	private int pendingExplosionCell = -1;

	{
		HP = HT = BASE_HT;
		defenseSkill = 20;
		spriteClass = EarthlySerpentSprite.class;

		EXP = 13;
		maxLvl = 30;

		loot = Generator.Category.SEED;
		lootChance = 1f;

		immunities.add(CorrosiveGas.class);
		immunities.add(Corrosion.class);
	}

	@Override
	public int damageRoll() {
		return scaleMeleeDamage(Random.NormalIntRange(24, 36));
	}

	@Override
	public int attackSkill(Char target) {
		return 40;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(8, 16);
	}

	@Override
	public float attackDelay() {
		return super.attackDelay() * BASE_ATTACK_DELAY / growthMultiplier();
	}

	@Override
	public float speed() {
		return super.speed() * growthMultiplier();
	}

	@Override
	public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti,
			DamageTag... damageTags) {
		boolean melee = canMeleeAttack(enemy);
		int healthBefore = enemy == null ? 0 : enemy.HP;
		boolean hit = super.attack(enemy, dmgMulti, dmgBonus, accMulti, damageTags);
		if (melee && enemy != null) {
			recordMeleeDamage(Math.max(0, healthBefore - enemy.HP));
		}
		return hit;
	}

	@Override
	public void damage(int damage, Object source, DamageTag... damageTags) {
		if (source instanceof SerpentBomb) {
			damage = normalizeBombDamage(damage, ((SerpentBomb) source).ownerId());
		}
        Char target = null;
        if(source instanceof Char){
            target = (Char)source;
        }
        int healthBefore = HP;
        boolean retaliates = isQualifyingMeleeSource(source, target);
		super.damage(damage, source, damageTags);
        if(target != null){
            int healthLost = Math.max(0, healthBefore - HP);
            if (retaliates && healthLost > 0 && target != null && target.isAlive()) {
                Buff.affect(target, Poison.class).extend(healthLost*0.1f);
            }
        }




	}

	@Override
	public boolean canMeleeAttack(Char target) {
		if (Dungeon.level == null || target == null
				|| Dungeon.level.distance(pos, target.pos) > 2) {
			return false;
		}
		Ballistica path = new Ballistica(pos, target.pos, Ballistica.PROJECTILE);
		return path.collisionPos == target.pos;
	}

	@Override
	public Type rangedAttackType() {
		return Type.RANGED_MAGIC;
	}

	@Override
	public int rangedAttackBallisticaMode() {
		return Ballistica.PROJECTILE;
	}

	@Override
	public boolean canRangedAttack(Char target) {
		return Dungeon.level != null
				&& target != null
				&& Dungeon.level.distance(pos, target.pos) > 2
				&& new Ballistica(pos, target.pos, rangedAttackBallisticaMode()).collisionPos == target.pos;
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return canMeleeAttack(enemy) || canRangedAttack(enemy);
	}

	@Override
	protected boolean doAttack(Char enemy) {
		if (canPull(enemy) && performPull(enemy)) {
			spend(attackDelay());
			return true;
		}
		return super.doAttack(enemy);
	}

	@Override
	public boolean doRangedAttack(Char target) {
		spitAt(target.pos);
		if (sprite != null) {
			sprite.zap(target.pos);
		}
		spend(attackDelay());
		return true;
	}

	protected float growthMultiplier() {
		return 1f + Math.min(meleeDamageDealt, 100) / 100f;
	}

	protected int scaleMeleeDamage(int damage) {
		return Math.round(damage * growthMultiplier());
	}

	protected void recordMeleeDamage(int damage) {
		if (damage <= 0 || meleeDamageDealt >= MAX_GROWTH_DAMAGE) {
			return;
		}
		meleeDamageDealt = Math.min(MAX_GROWTH_DAMAGE, meleeDamageDealt + damage);
		HT = Math.min(MAX_HT, BASE_HT + meleeDamageDealt * 2);
	}

	protected void advanceRegeneration(float elapsed) {
		if (elapsed <= 0) {
			return;
		}
		regenerationProgress += elapsed;
		while (regenerationProgress >= REGENERATION_INTERVAL) {
			regenerationProgress -= REGENERATION_INTERVAL;
			heal((int) Math.ceil(HT * 0.1f), false);
		}
	}

	protected void emitAura() {
		if (Dungeon.level == null || Dungeon.level.blobs == null) {
			return;
		}
		seedCorrosiveGas(pos, 5, 1, CorrosiveGas.class);
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = pos + offset;
			if (validOpenCell(cell)) {
				seedCorrosiveGas(cell, 2, 1, CorrosiveGas.class);
			}
		}
	}

	protected void spitAt(int target) {
		if (Dungeon.level == null || Dungeon.level.blobs == null) {
			return;
		}
		if (validOpenCell(target)) {
			seedCorrosiveGas(target, 15, 8, EarthlySerpentSpitGas.class);
		}
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = target + offset;
			if (validOpenCell(cell)) {
				seedCorrosiveGas(cell, 5, 8, EarthlySerpentSpitGas.class);
			}
		}
	}

	private <T extends CorrosiveGas> void seedCorrosiveGas(
			int cell, int volume, int strength, Class<T> gasClass) {
		Blob.seed(cell, volume, gasClass)
				.setStrength(strength, EarthlySerpent.class);
	}

	private boolean validOpenCell(int cell) {
		return Dungeon.level.insideMap(cell)
				&& !Dungeon.level.solid[cell]
				&& (Dungeon.level.passable[cell] || Dungeon.level.avoid[cell]);
	}

	protected boolean canPull(Char target) {
		if (pullUsed || Dungeon.level == null || target == null
				|| target.properties().contains(Property.IMMOVABLE)
				|| (Dungeon.hero != null && target != Dungeon.hero)) {
			return false;
		}
		int distance = Dungeon.level.distance(pos, target.pos);
		if (distance < 3 || distance > 4) {
			return false;
		}
		Ballistica path = new Ballistica(pos, target.pos, Ballistica.PROJECTILE);
		return path.collisionPos == target.pos && findPullLanding(target) != -1;
	}

	protected boolean performPull(Char target) {
		if (!canPull(target)) {
			return false;
		}
		int landing = findPullLanding(target);
		if (landing == -1) {
			return false;
		}

		int from = target.pos;
		pullUsed = true;
		pendingExplosionCell = landing;
		target.pos = landing;
		if (Dungeon.hero != null) {
			Dungeon.level.occupyCell(target);
		}

		if (target instanceof Hero) {
			((Hero) target).interrupt();
		}
		if (target.sprite != null) {
			Actor.add(new Pushing(target, from, landing));
		}
		if (sprite instanceof EarthlySerpentSprite) {
			((EarthlySerpentSprite) sprite).pull(from);
		}
		if (Dungeon.hero != null) {
			Dungeon.observe();
			GameScene.updateFog();
		}
		return true;
	}

	private int findPullLanding(Char target) {
		int best = -1;
		float bestDistance = Float.MAX_VALUE;
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = pos + offset;
			if (!validOpenCell(cell) || Actor.findChar(cell) != null) {
				continue;
			}
			float distance = Dungeon.level.trueDistance(cell, target.pos);
			if (distance < bestDistance) {
				best = cell;
				bestDistance = distance;
			}
		}
		return best;
	}

	protected boolean resolvePendingExplosion() {
		if (pendingExplosionCell == -1) {
			return false;
		}
		int cell = pendingExplosionCell;
		pendingExplosionCell = -1;
		explodeAt(cell);
		return true;
	}

	protected void explodeAt(int cell) {
		if (sprite instanceof EarthlySerpentSprite) {
			((EarthlySerpentSprite) sprite).warning(false);
		}
		new SerpentBomb(id()).explode(cell);
	}

	protected boolean pullUsed() {
		return pullUsed;
	}

	protected int pendingExplosionCell() {
		return pendingExplosionCell;
	}



	protected boolean isQualifyingMeleeSource(Object source, Char target) {
		if (target instanceof Hero && source == target) {
			KindOfWeapon weapon = ((Hero) target).belongings.attackingWeapon();
			return isMeleeWeaponType(weapon == null ? null : weapon.getClass());
		}
		return source instanceof Mob
				&& source != this
				&& Dungeon.level != null
				&& Dungeon.level.adjacent(pos, ((Mob) source).pos);
	}

	protected boolean isMeleeWeaponType(Class<?> weaponType) {
		return weaponType != null && MeleeWeapon.class.isAssignableFrom(weaponType);
	}

	protected int normalizeBombDamage(int damage, int ownerId) {
		return ownerId == id() ? 0 : damage;
	}

	@Override
	protected boolean act() {
		float now = Actor.now();
		if (Float.isNaN(lastRegenerationTime)) {
			lastRegenerationTime = now;
		} else {
			advanceRegeneration(Math.max(0f, now - lastRegenerationTime));
			lastRegenerationTime = now;
		}
		emitAura();
		if (resolvePendingExplosion()) {
			spend(attackDelay());
			return true;
		}
		return super.act();
	}

	private static final String MELEE_DAMAGE_DEALT = "melee_damage_dealt";
	private static final String REGENERATION_PROGRESS = "regeneration_progress";
	private static final String PULL_USED = "pull_used";
	private static final String PENDING_EXPLOSION_CELL = "pending_explosion_cell";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(MELEE_DAMAGE_DEALT, meleeDamageDealt);
		bundle.put(REGENERATION_PROGRESS, regenerationProgress);
		bundle.put(PULL_USED, pullUsed);
		bundle.put(PENDING_EXPLOSION_CELL, pendingExplosionCell);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		meleeDamageDealt = Math.min(MAX_GROWTH_DAMAGE, bundle.getInt(MELEE_DAMAGE_DEALT));
		regenerationProgress = Math.max(0f, bundle.getFloat(REGENERATION_PROGRESS));
		pullUsed = bundle.getBoolean(PULL_USED);
		pendingExplosionCell = bundle.contains(PENDING_EXPLOSION_CELL)
				? bundle.getInt(PENDING_EXPLOSION_CELL)
				: -1;
		HT = Math.min(MAX_HT, BASE_HT + meleeDamageDealt * 2);
		HP = Math.min(HP, HT);
		lastRegenerationTime = Float.NaN;
	}

	@Override
	public void die(Object cause) {
		pendingExplosionCell = -1;
		super.die(cause);
	}

	static final class SerpentBomb extends Bomb {

		private final int ownerId;

		SerpentBomb(int ownerId) {
			this.ownerId = ownerId;
		}

		int ownerId() {
			return ownerId;
		}
	}

	public static class EarthlySerpentSpitGas extends CorrosiveGas {

		@Override
		public String tileDesc() {
			return Messages.get(CorrosiveGas.class, "desc");
		}
	}
}
