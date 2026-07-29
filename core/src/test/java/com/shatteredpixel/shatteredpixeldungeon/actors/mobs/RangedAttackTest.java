package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AttackDrone;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.cave.BruteH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.cave.ShamanH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.city.ElementalH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.city.WarlockH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.hall.EyeH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.hall.ScorpioH;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.prison.DM100H;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.mob.sewer.GnollH;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
	public void dronesUsePhysicalRangedProtocol() {
		assertPhysical(InstructionTool.Drone.class);
		assertPhysical(AttackDrone.FlashDrone.class);
		assertPhysical(AttackDrone.LaserDrone.class);
		assertPhysical(AttackDrone.AnesthesiaDrone.class);
		assertPhysical(AttackDrone.RaidDrone.class);
		assertPhysical(AttackDrone.ShockDrone.class);
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
}
