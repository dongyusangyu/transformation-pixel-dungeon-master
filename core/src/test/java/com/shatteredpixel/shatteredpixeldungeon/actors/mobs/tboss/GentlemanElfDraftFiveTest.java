package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.HeavyInjury;
import com.watabou.utils.Bundle;
import org.junit.Test;

import static org.junit.Assert.*;

public class GentlemanElfDraftFiveTest {
	@Test public void thirdPhaseOnlyTakesMechanicDamageAndNeverReturnsToMirrors() {
		GentlemanElf boss = new GentlemanElf();
		boss.setPhaseForTest(GentlemanElf.Phase.FINAL_DUEL, 2);
		boss.HP = 600;
		assertEquals(0, boss.capFinalDamageForTest(999));
		assertEquals(GentlemanElf.Phase.FINAL_DUEL, boss.phase());
	}

	@Test public void pounceSelfDamageDependsOnHeavyInjuryBeforeTheHit() {
		GentlemanElf boss = new GentlemanElf();
		boss.setPhaseForTest(GentlemanElf.Phase.FINAL_DUEL, 2);
		assertEquals(50, boss.pounceSelfDamage(false));
		assertEquals(100, boss.pounceSelfDamage(true));
		Bundle bundle = new Bundle();
		boss.storeInBundle(bundle);
		GentlemanElf restored = new GentlemanElf();
		restored.restoreFromBundle(bundle);
		assertEquals(50, restored.pounceSelfDamage(false));
		assertEquals(100, restored.pounceSelfDamage(true));
	}

	@Test public void heavyInjuryRefreshesToTenAndBlocksHealingUntilItExpires() {
		DeathKnight target = new DeathKnight();
		HeavyInjury injury = HeavyInjury.apply(target);
		assertTrue(injury.blocksIncomingHealing());
		for (int i = 0; i < 4; i++) injury.act();
		assertEquals(6, injury.stacks());
		assertSame(injury, HeavyInjury.apply(target));
		assertEquals(10, injury.stacks());
		Bundle bundle = new Bundle();
		injury.storeInBundle(bundle);
		HeavyInjury restored = new HeavyInjury();
		restored.restoreFromBundle(bundle);
		assertEquals(10, restored.stacks());
		for (int i = 0; i < 10; i++) injury.act();
		assertNull(target.buff(HeavyInjury.class));
	}

	@Test public void toastWarningSurvivesPounceStateAndBundleRoundTrip() {
		GentlemanElf boss = new GentlemanElf();
		boss.stagePendingForTest(GentlemanElf.Skill.DEVOUR, new int[]{1, 2}, 2,
				GentlemanElf.WineState.DRUNKENNESS);
		boss.stageWineWarningForTest(8, new int[]{7, 8, 9}, GentlemanElf.WineState.EXHILARATION, 1f);
		assertEquals(GentlemanElf.Skill.DEVOUR, boss.pendingSkill());
		Bundle bundle = new Bundle();
		boss.storeInBundle(bundle);
		GentlemanElf restored = new GentlemanElf();
		restored.restoreFromBundle(bundle);
		assertEquals(GentlemanElf.Skill.DEVOUR, restored.pendingSkill());
		assertArrayEquals(new int[]{7, 8, 9}, restored.wineWarningCellsForTest());
		assertEquals(GentlemanElf.WineState.EXHILARATION, restored.wineWarningStateForTest());
	}
}
