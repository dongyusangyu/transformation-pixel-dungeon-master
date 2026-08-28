package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TowerMusicPolicyTest {

	@Test
	public void rawStateUsesVisibleEnemyBandsAndCriticalPriority() {
		assertEquals(TowerMusicPolicy.State.CALM,
				TowerMusicPolicy.rawState(100, 100, 0));
		assertEquals(TowerMusicPolicy.State.ALERT,
				TowerMusicPolicy.rawState(100, 100, 1));
		assertEquals(TowerMusicPolicy.State.ALERT,
				TowerMusicPolicy.rawState(100, 100, 4));
		assertEquals(TowerMusicPolicy.State.COMBAT,
				TowerMusicPolicy.rawState(100, 100, 5));
		assertEquals(TowerMusicPolicy.State.COMBAT,
				TowerMusicPolicy.rawState(100, 100, 7));
		assertEquals(TowerMusicPolicy.State.CRITICAL,
				TowerMusicPolicy.rawState(100, 100, 8));
		assertEquals(TowerMusicPolicy.State.CRITICAL,
				TowerMusicPolicy.rawState(33, 100, 0));
		assertEquals(TowerMusicPolicy.State.CALM,
				TowerMusicPolicy.rawState(34, 100, 0));
	}

	@Test
	public void hysteresisPreventsBoundaryFlapping() {
		assertEquals(TowerMusicPolicy.State.CRITICAL,
				TowerMusicPolicy.stateWithHysteresis(
						TowerMusicPolicy.State.CRITICAL, 40, 100, 0));
		assertEquals(TowerMusicPolicy.State.COMBAT,
				TowerMusicPolicy.stateWithHysteresis(
						TowerMusicPolicy.State.CRITICAL, 41, 100, 6));
		assertEquals(TowerMusicPolicy.State.COMBAT,
				TowerMusicPolicy.stateWithHysteresis(
						TowerMusicPolicy.State.COMBAT, 100, 100, 4));
		assertEquals(TowerMusicPolicy.State.ALERT,
				TowerMusicPolicy.stateWithHysteresis(
						TowerMusicPolicy.State.COMBAT, 100, 100, 3));
	}

	@Test
	public void transitionsEscalateQuicklyAndRelaxSlowly() {
		assertEquals(0f, TowerMusicPolicy.transitionDelay(
				TowerMusicPolicy.State.COMBAT, TowerMusicPolicy.State.CRITICAL, true), 0f);
		assertEquals(0.5f, TowerMusicPolicy.transitionDelay(
				TowerMusicPolicy.State.CALM, TowerMusicPolicy.State.ALERT, false), 0f);
		assertEquals(3f, TowerMusicPolicy.transitionDelay(
				TowerMusicPolicy.State.COMBAT, TowerMusicPolicy.State.ALERT, false), 0f);
	}

	@Test
	public void towerBossLevelOwnsItsMusicUpdateBoundary() throws Exception {
		assertEquals(TowerBossLevel.class, TowerBossLevel.class
				.getMethod("updateLevelMusic", float.class).getDeclaringClass());
	}
}
