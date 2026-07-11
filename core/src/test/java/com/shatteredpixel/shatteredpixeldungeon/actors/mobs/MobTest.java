package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MobTest {

	@Test
	public void pathTargetRejectsCellEqualToMapLength() {
		assertFalse(Mob.isValidPathTarget(1024, 1024));
	}

	@Test
	public void pathTargetRejectsNegativeCell() {
		assertFalse(Mob.isValidPathTarget(-1, 1024));
	}

	@Test
	public void pathTargetAcceptsLastMapCell() {
		assertTrue(Mob.isValidPathTarget(1023, 1024));
	}

	@Test
	public void armedUprisingAcceptsEveryAlliedMobOnly() {
		Mob ally = new Rat();
		ally.alignment = Char.Alignment.ALLY;
		assertTrue(Talent.isArmedUprisingAlly(ally));

		Mob enemy = new Rat();
		enemy.alignment = Char.Alignment.ENEMY;
		assertFalse(Talent.isArmedUprisingAlly(enemy));
		Char nonMobAlly = new Char() {
			@Override
			protected boolean act() {
				return false;
			}
		};
		nonMobAlly.alignment = Char.Alignment.ALLY;
		assertFalse(Talent.isArmedUprisingAlly(nonMobAlly));
	}
}
