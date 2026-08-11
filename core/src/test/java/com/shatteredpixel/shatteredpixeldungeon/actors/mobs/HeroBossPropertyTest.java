package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HeroBossPropertyTest {

	@Test
	public void heroBossesAreBossesAndDemons() {
		assertBossAndDemon(new WarriorBoss());
		assertBossAndDemon(new RogueBoss());
		assertBossAndDemon(new HuntressBoss());
	}

	private static void assertBossAndDemon(Mob boss) {
		assertTrue(boss.properties().contains(Char.Property.BOSS));
		assertTrue(boss.properties().contains(Char.Property.DEMONIC));
	}
}
