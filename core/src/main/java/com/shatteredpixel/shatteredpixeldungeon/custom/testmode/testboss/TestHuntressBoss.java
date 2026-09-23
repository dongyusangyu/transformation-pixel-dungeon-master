package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class TestHuntressBoss extends HuntressBoss {

	private static final String ENCOUNTER_SUPPORT_SPAWNED = "encounter_support_spawned";
	private static final String WARDEN_SUPPORT_SPAWNED = "warden_support_spawned";

	private boolean encounterSupportSpawned;
	private boolean wardenSupportSpawned;

	{
		state = WANDERING;
	}

	@Override
	protected boolean act() {
		if (Dungeon.level != null) {
			TestBossUtil.assignBoss(this);
		}
		return super.act();
	}

	@Override
	public void damage(int dmg, Object src, DamageTag... damageTags) {
		Char attacker = TestBossUtil.attackerToRetarget(this, src);
		if (attacker != null) {
			aggro(attacker);
			beckon(attacker.pos);
		}
		super.damage(dmg, src, damageTags);
	}

	@Override
	public void notice() {
		super.notice();
		TestBossUtil.assignBoss(this);
		if (!encounterSupportSpawned && Dungeon.level != null) {
			encounterSupportSpawned = true;
			spawnSupport(DistractingHawk.class, 1);
		}
	}

	@Override
	protected void onWardenPhaseStarted() {
		if (wardenSupportSpawned || Dungeon.level == null) {
			return;
		}
		wardenSupportSpawned = true;
		spawnWardenPlants();
		spawnSupport(DistractingHawk.class, 1);
		spawnSupport(HuntressTentacle.class, 3);
	}

	@Override
	protected boolean tryTriggerFadeleafBoon() {
		Char target = TestBossUtil.firstEnemy(this);
		return target != null && target.isAlive()
				&& teleportBossAndTargetApart(target);
	}

	@Override
	protected boolean teleportBossAndTargetApart(Char target) {
		if (Dungeon.level == null || target == null || !target.isAlive()
				|| !Dungeon.level.insideMap(pos) || !Dungeon.level.insideMap(target.pos)) {
			return false;
		}
		int oldDistance = Dungeon.level.distance(pos, target.pos);
		for (int tries = 0; tries < 12; tries++) {
			int bossDestination = TestBossUtil.randomSpawnCellNearStrict(target.pos, 3, 10);
			if (bossDestination == -1) {
				return false;
			}
			int targetDestination = TestBossUtil.randomSpawnCellNearStrict(
					bossDestination, Math.max(4, oldDistance + 1), 50);
			if (targetDestination == -1 || targetDestination == bossDestination) {
				continue;
			}
			int oldBossPosition = pos;
			int oldTargetPosition = target.pos;
			ScrollOfTeleportation.appear(this, bossDestination);
			ScrollOfTeleportation.appear(target, targetDestination);
			if (pos == bossDestination && target.pos == targetDestination
					&& Dungeon.level.distance(pos, target.pos) > oldDistance) {
				return true;
			}
			ScrollOfTeleportation.appear(this, oldBossPosition);
			ScrollOfTeleportation.appear(target, oldTargetPosition);
		}
		return false;
	}

	@Override
	protected boolean performBossOnlyEscape(Char target) {
		if (Dungeon.level == null || target == null || !target.isAlive()) {
			return false;
		}
		int oldDistance = Dungeon.level.distance(pos, target.pos);
		int destination = TestBossUtil.randomSpawnCellNearStrict(
				target.pos, Math.max(3, oldDistance + 1), 50);
		if (destination == -1) {
			return false;
		}
		ScrollOfTeleportation.appear(this, destination);
		return pos == destination;
	}

	@Override
	protected void summonChallengeHawk() {
		spawnSupport(DistractingHawk.class, 1);
	}

	protected void spawnSupport(Class<? extends Mob> type, int count) {
		Char target = TestBossUtil.firstEnemy(this);
		for (int i = 0; i < count; i++) {
			Mob support = TestBossUtil.summonNear(this, type, pos, 1, 6);
			if (support != null && target != null) {
				support.aggro(target);
			}
		}
	}

	protected void spawnWardenPlants() {
		for (int i = 0; i < HuntressBoss.wardenSeedClassCount(); i++) {
			int cell = randomPlantCell();
			if (cell == -1) {
				return;
			}
			Plant.Seed seed = Reflection.newInstance(
					HuntressBoss.wardenSeedClassForIndex(i));
			Plant plant = seed.couch(cell, Dungeon.level);
			Dungeon.level.plants.put(cell, plant);
			Dungeon.level.set(cell, Terrain.GRASS);
			GameScene.add(plant);
			GameScene.updateMap(cell);
		}
	}

	private int randomPlantCell() {
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int cell = 0; cell < Dungeon.level.length(); cell++) {
			int terrain = Dungeon.level.map[cell];
			if (Dungeon.level.passable[cell]
					&& (terrain == Terrain.EMPTY || terrain == Terrain.EMPTY_DECO
					|| terrain == Terrain.GRASS || terrain == Terrain.HIGH_GRASS
					|| terrain == Terrain.FURROWED_GRASS || terrain == Terrain.EMBERS)
					&& Actor.findChar(cell) == null
					&& Dungeon.level.plants.get(cell) == null
					&& Dungeon.level.heaps.get(cell) == null
					&& Dungeon.level.distance(pos, cell) >= 2) {
				candidates.add(cell);
			}
		}
		return candidates.isEmpty() ? -1 : Random.element(candidates);
	}

	@Override
	protected boolean usesCampaignDeathEffects() {
		return false;
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		if (Dungeon.level != null) {
			TestBossUtil.bossSlain(this);
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ENCOUNTER_SUPPORT_SPAWNED, encounterSupportSpawned);
		bundle.put(WARDEN_SUPPORT_SPAWNED, wardenSupportSpawned);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		encounterSupportSpawned = bundle.getBoolean(ENCOUNTER_SUPPORT_SPAWNED);
		wardenSupportSpawned = bundle.getBoolean(WARDEN_SUPPORT_SPAWNED);
	}
}
