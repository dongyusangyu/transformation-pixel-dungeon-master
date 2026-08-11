package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CharAttackResolvedTest {

	@Test
	public void resolvedHookReceivesDamageAfterTargetDamagePipeline() {
		RecordingAttacker attacker = new RecordingAttacker();
		HalfDamageTarget target = new HalfDamageTarget();

		attacker.dealAndResolveForTest(target, 20);

		assertTrue(attacker.resolvedHit);
		assertEquals(10, attacker.resolvedDamage);
		assertEquals(90, target.HP);
	}

	private static final class RecordingAttacker extends Gnoll {

		private boolean resolvedHit;
		private int resolvedDamage = -1;

		private void dealAndResolveForTest(Char target, int damage) {
			int healthBefore = target.HP + target.shielding();
			target.damage(damage, this, DamageTag.PHYSICAL, DamageTag.MELEE);
			onAttackResolved(
					target,
					true,
					resolvedAttackDamage(target, healthBefore),
					DamageTag.PHYSICAL,
					DamageTag.MELEE);
		}

		@Override
		protected void onAttackResolved(
				Char target, boolean hit, int damageDealt, DamageTag... damageTags) {
			resolvedHit = hit;
			resolvedDamage = damageDealt;
		}
	}

	private static final class HalfDamageTarget extends Gnoll {

		private HalfDamageTarget() {
			HP = HT = 100;
		}

		@Override
		public int defenseSkill(Char enemy) {
			return 0;
		}

		@Override
		public int drRoll() {
			return 0;
		}

		@Override
		public void damage(int damage, Object source, DamageTag... damageTags) {
			HP -= damage / 2;
		}
	}

}
