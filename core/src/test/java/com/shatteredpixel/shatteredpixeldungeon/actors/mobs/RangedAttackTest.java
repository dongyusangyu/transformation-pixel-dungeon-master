package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SoulMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AttackDrone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AuxiliaryDrone;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.cave.BruteH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.cave.ShamanH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.city.ElementalH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.city.WarlockH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.hall.EyeH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.hall.EyeImage;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.hall.ScorpioH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.prison.DM100H;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.sewer.GnollH;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.VeilbreakerEye;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangedAttackTest {

	@Test
	public void projectileMobsUsePhysicalRangedProtocol() {
		assertPhysical(GnollTrickster.class);
		assertPhysical(Scorpio.class);
		assertPhysical(Acidic.class);
		assertPhysical(FungalSentry.class);
		assertPhysical(GnollGuard.class);
		assertPhysical(Goo.class);
		assertPhysical(GreatDemon.class);
		assertPhysical(RogueBoss.class);
		assertPhysical(Tengu.class);
		assertPhysical(HuntressBoss.class);
		assertPhysical(ChaosDisciples.ScorpioBoss.class);
		assertPhysical(GnollH.class);
		assertPhysical(BruteH.class);
		assertPhysical(ScorpioH.class);
	}

	@Test
	public void spellMobsUseMagicalRangedProtocol() {
		assertMagical(DM100.class);
		assertMagical(Shaman.RedShaman.class);
		assertMagical(Warlock.class);
		assertMagical(CrystalWisp.class);
		assertMagical(Eye.class);
		assertMagical(Elemental.class);
		assertMagical(YogFist.class);
		assertMagical(GreatShoper.class);
		assertMagical(ChaosDisciples.EyeBoss.class);
		assertMagical(GoldBoss.GoldElemental.class);
		assertMagical(GoldBoss.Thymor.class);
		assertMagical(YogDzewa.class);
		assertMagical(ShamanH.class);
		assertMagical(ElementalH.class);
		assertMagical(WarlockH.class);
		assertMagical(EyeH.class);
		assertMagical(DM100H.class);
	}

	@Test
	public void droneAttackProtocolsMatchTheirRoles() {
		assertPhysical(AttackDrone.FlashDrone.class);
		assertMagical(AttackDrone.LaserDrone.class);
		assertPhysical(AttackDrone.ShockDrone.class);

		assertNotRanged(InstructionTool.Drone.class);
		assertNotRanged(AttackDrone.AnesthesiaDrone.class);
		assertNotRanged(AttackDrone.RaidDrone.class);

		assertNotRanged(AuxiliaryDrone.ScoutDrone.class);
		assertNotRanged(AuxiliaryDrone.MirrorDrone.class);
		assertNotRanged(AuxiliaryDrone.ProtectDrone.class);
		assertNotRanged(AuxiliaryDrone.EscortDrone.class);
		assertNotRanged(AuxiliaryDrone.BombDrone.class);
		assertNotRanged(AuxiliaryDrone.ChaosDrone.class);
	}

	@Test
	public void raidDroneUsesThreeTileMeleeReach() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = new TestLevel();
			level.setSize(9, 9);
			for (int cell = 0; cell < level.length(); cell++) {
				level.passable[cell] = true;
				level.openSpace[cell] = true;
			}
			Dungeon.level = level;

			TestRaidDrone drone = new TestRaidDrone();
			drone.pos = 40;
			TestTarget target = new TestTarget();
			target.pos = 43;
			assertTrue(drone.canAttackTarget(target));

			target.pos = 44;
			assertFalse(drone.canAttackTarget(target));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void shockDroneHasNoRangeCapButRespectsProjectileBallistics() {
		Level previousLevel = Dungeon.level;
		try {
			TestLevel level = new TestLevel();
			level.setSize(11, 11);
			for (int cell = 0; cell < level.length(); cell++) {
				level.passable[cell] = true;
				level.openSpace[cell] = true;
			}
			Dungeon.level = level;

			AttackDrone.ShockDrone drone = new AttackDrone.ShockDrone();
			drone.pos = 12;
			TestTarget target = new TestTarget();
			target.pos = 108;
			assertTrue(drone.canRangedAttack(target));

			level.solid[60] = true;
			assertFalse(drone.canRangedAttack(target));
		} finally {
			Dungeon.level = previousLevel;
		}
	}

	@Test
	public void eyeDeathGazeHonorsTheSharedRangedAttackBlock() {
		assertFalse(Eye.deathGazeAllowedWhenRangedBlocked(true));
		assertTrue(Eye.deathGazeAllowedWhenRangedBlocked(false));
	}

	@Test
	public void chargedRangedAttackIsInterruptedOnlyWhenBlocked() {
		assertTrue(RangedAttack.shouldInterruptCharge(true, false));
		assertFalse(RangedAttack.shouldInterruptCharge(true, true));
		assertFalse(RangedAttack.shouldInterruptCharge(false, false));
	}

	@Test
	public void firingChargeRestoresAsReadyWithoutCooldown() {
		assertTrue(RangedAttack.restoredCharge(false, true));
		assertEquals(0, RangedAttack.restoredChargeCooldown(5, true));
		assertTrue(RangedAttack.restoredCharge(true, false));
		assertEquals(5, RangedAttack.restoredChargeCooldown(5, false));
	}

	@Test
	public void deathGazeFixCoversBothEyeHierarchies() {
		assertTrue(Eye.class.isAssignableFrom(VeilbreakerEye.class));
		assertTrue(Eye.class.isAssignableFrom(YogDzewa.YogEye.class));
		assertTrue(EyeH.class.isAssignableFrom(EyeImage.class));
	}

	@Test
	public void blockedDeathGazeValidationClearsActualChargeState() {
		assertBlockedChargeCleared(new TestEye());
		assertBlockedChargeCleared(new TestEyeH());
	}

	@Test
	public void allowedDeathGazeValidationPreservesActualChargeState() {
		assertAllowedChargePreserved(new TestEye());
		assertAllowedChargePreserved(new TestEyeH());
	}

	@Test
	public void eyesOnlyExposeAReadyDeathGazeToTheRangedProtocol() throws Exception {
		Level previousLevel = Dungeon.level;
		Actor.clear();
		try {
			TestLevel level = new TestLevel();
			level.setSize(7, 7);
			for (int cell = 0; cell < level.length(); cell++) {
				level.passable[cell] = true;
				level.openSpace[cell] = true;
			}
			Dungeon.level = level;

			TestTarget target = new TestTarget();
			target.pos = 12;
			Actor.add(target);

			assertDeathGazeProtocolState(new TestEye(), target);
			assertDeathGazeProtocolState(new TestEyeH(), target);
		} finally {
			Actor.clear();
			Dungeon.level = previousLevel;
		}
	}

	private static void assertDeathGazeProtocolState(
			DeathGazeProtocolHarness eye, Char target) throws Exception {
		eye.configureProtocolState(target, true, false, 5);
		assertFalse(eye.protocolAllowsRangedAttack(target));
		assertFalse(eye.canAttackTarget(target));
		target.pos = 9;
		assertTrue(eye.canAttackTarget(target));
		target.pos = 12;

		eye.configureProtocolState(target, false, false, 0);
		assertFalse(eye.protocolAllowsRangedAttack(target));
		assertFalse(eye.canAttackTarget(target));

		eye.configureProtocolState(target, true, true, 0);
		assertTrue(eye.protocolAllowsRangedAttack(target));
	}

	private static void assertBlockedChargeCleared(DeathGazeChargeHarness eye) {
		assertFalse(eye.validateCharge(false));
		assertFalse(eye.isChargeActive());
		assertEquals(-1, eye.aimCell());
	}

	private static void assertAllowedChargePreserved(DeathGazeChargeHarness eye) {
		assertTrue(eye.validateCharge(true));
		assertTrue(eye.isChargeActive());
		assertEquals(7, eye.aimCell());
	}

	private interface DeathGazeChargeHarness {
		boolean validateCharge(boolean allowed);
		boolean isChargeActive();
		int aimCell();
	}

	private interface DeathGazeProtocolHarness extends DeathGazeChargeHarness {
		void configureProtocolState(Char target, boolean allowed,
				boolean charged, int cooldown) throws Exception;
		boolean protocolAllowsRangedAttack(Char target);
		boolean canAttackTarget(Char target);
	}

	private static class TestEye extends Eye implements DeathGazeProtocolHarness {
		private boolean deathGazeAllowed;

		TestEye() {
			super(false);
		}

		@Override
		protected boolean canUseDeathGazeAgainst(
				com.shatteredpixel.shatteredpixeldungeon.actors.Char target) {
			return deathGazeAllowed;
		}

		@Override
		public boolean validateCharge(boolean allowed) {
			deathGazeAllowed = allowed;
			beamCharged = true;
			beamTarget = 7;
			return validateDeathGazeCharge(null);
		}

		@Override
		public boolean isChargeActive() {
			return beamCharged;
		}

		@Override
		public int aimCell() {
			return beamTarget;
		}

		@Override
		public void configureProtocolState(Char target, boolean allowed,
				boolean charged, int cooldown) throws Exception {
			pos = 8;
			deathGazeAllowed = allowed;
			beamCharged = charged;
			beam = charged ? new Ballistica(pos, target.pos, Ballistica.STOP_SOLID) : null;
			beamTarget = charged ? target.pos : -1;
			java.lang.reflect.Field field = Eye.class.getDeclaredField("beamCooldown");
			field.setAccessible(true);
			field.setInt(this, cooldown);
		}

		@Override
		public boolean protocolAllowsRangedAttack(Char target) {
			return canRangedAttack(target);
		}

		@Override
		public boolean canAttackTarget(Char target) {
			return canAttack(target);
		}
	}

	private static class TestEyeH extends EyeH implements DeathGazeProtocolHarness {
		private boolean deathGazeAllowed;

		TestEyeH() {
			super(false);
		}

		@Override
		protected boolean canUseDeathGazeAgainst(
				com.shatteredpixel.shatteredpixeldungeon.actors.Char target) {
			return deathGazeAllowed;
		}

		@Override
		public boolean validateCharge(boolean allowed) {
			deathGazeAllowed = allowed;
			beamCharged = true;
			beamTarget = 7;
			return validateDeathGazeCharge(null);
		}

		@Override
		public boolean isChargeActive() {
			return beamCharged;
		}

		@Override
		public int aimCell() {
			return beamTarget;
		}

		@Override
		public void configureProtocolState(Char target, boolean allowed,
				boolean charged, int cooldown) {
			pos = 8;
			deathGazeAllowed = allowed;
			beamCharged = charged;
			beam = charged ? new Ballistica(pos, target.pos, Ballistica.STOP_SOLID) : null;
			beamTarget = charged ? target.pos : -1;
			beamCooldown = cooldown;
		}

		@Override
		public boolean protocolAllowsRangedAttack(Char target) {
			return canRangedAttack(target);
		}

		@Override
		public boolean canAttackTarget(Char target) {
			return canAttack(target);
		}
	}

	private static class TestTarget extends Char {
		@Override
		protected boolean act() {
			return false;
		}
	}

	private static class TestRaidDrone extends AttackDrone.RaidDrone {
		boolean canAttackTarget(Char target) {
			return canAttack(target);
		}
	}

	private static class TestLevel extends Level {
		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}

	@Test
	public void mindImprisonmentOnlyBlocksMarkedNonBossesTargetingTheHero() {
		assertTrue(SoulMark.blocksRangedAttack(true, false, true, 1));
		assertFalse(SoulMark.blocksRangedAttack(false, false, true, 1));
		assertFalse(SoulMark.blocksRangedAttack(true, true, true, 1));
		assertFalse(SoulMark.blocksRangedAttack(true, false, false, 1));
		assertFalse(SoulMark.blocksRangedAttack(true, false, true, 0));
	}

	private static void assertPhysical(Class<?> attacker) {
		assertTrue(PhysicalRangedAttack.class.isAssignableFrom(attacker));
		assertEquals(RangedAttack.Type.RANGED_PHYSICAL,
				new PhysicalRangedAttack() {
				}.rangedAttackType());
	}

	private static void assertMagical(Class<?> attacker) {
		assertTrue(MagicalRangedAttack.class.isAssignableFrom(attacker));
		assertEquals(RangedAttack.Type.RANGED_MAGIC,
				new MagicalRangedAttack() {
					@Override
					public boolean doRangedAttack(
							com.shatteredpixel.shatteredpixeldungeon.actors.Char target) {
						return false;
					}
				}.rangedAttackType());
	}

	private static void assertNotRanged(Class<?> attacker) {
		assertFalse(RangedAttack.class.isAssignableFrom(attacker));
	}
}
