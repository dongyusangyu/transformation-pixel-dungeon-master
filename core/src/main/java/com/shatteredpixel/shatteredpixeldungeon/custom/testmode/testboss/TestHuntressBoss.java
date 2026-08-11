package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.HuntressBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.watabou.utils.Bundle;

public class TestHuntressBoss extends HuntressBoss {

	private static final String ENCOUNTER_SUPPORT_SPAWNED = "encounter_support_spawned";
	private static final String WARDEN_SUPPORT_SPAWNED = "warden_support_spawned";

	private boolean encounterSupportSpawned;
	private boolean wardenSupportSpawned;

	{
		state = WANDERING;
	}

	@Override
	protected Char chooseEnemy() {
		return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
	}

	@Override
	protected boolean act() {
		if (Dungeon.level != null) {
			TestBossUtil.assignBoss(this);
			if (!TestBossUtil.hasVisibleAttackableEnemy(this)) {
				clearEnemy();
			}
		}
		return super.act();
	}

	@Override
	public void damage(int dmg, Object src, DamageTag... damageTags) {
		Char attacker = TestBossUtil.attackerToRetarget(this, src);
		if (attacker != null) {
			enemy = attacker;
			state = HUNTING;
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
		spawnSupport(DistractingHawk.class, 1);
		spawnSupport(HuntressTentacle.class, 3);
	}

	@Override
	protected void triggerFadeleafBoon() {
		if (Dungeon.level == null) {
			return;
		}
		Char target = TestBossUtil.firstEnemy(this);
		teleportOnCurrentFloor(this);
		if (target != null && target.isAlive()) {
			teleportOnCurrentFloor(target);
		}
	}

	private void teleportOnCurrentFloor(Char target) {
		if (!Dungeon.level.insideMap(target.pos)) {
			return;
		}
		int destination = TestBossUtil.randomSpawnCellNear(target.pos, 3, 8);
		if (destination != -1) {
			ScrollOfTeleportation.appear(target, destination);
		}
	}

	private void spawnSupport(Class<? extends Mob> type, int count) {
		Char target = TestBossUtil.firstEnemy(this);
		for (int i = 0; i < count; i++) {
			Mob support = TestBossUtil.summonNear(this, type, pos, 1, 6);
			if (support != null && target != null) {
				support.aggro(target);
			}
		}
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
