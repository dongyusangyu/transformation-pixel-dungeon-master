package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MyriadEcho;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MyriadBlackShadowTest {

	@Test
	public void keepsGuardEquipmentReplicationAndGuaranteedTransmutationDrop() {
		TestShadow shadow = new TestShadow();
		assertEquals(100, shadow.HT);
		assertEquals(1f, shadow.lootChance(), 0f);
		assertSame(ScrollOfTransmutation.class, shadow.lootClassForTest());
	}

	@Test
	public void splitFactoryProducesMarkedOneHpEnemyEcho() {
		TestShadow shadow = new TestShadow();
		Mob echo = shadow.splitForTest();
		assertTrue(echo instanceof Mob);
		assertEquals(1, echo.HP);
		assertEquals(1, echo.HT);
		assertEquals(0, echo.EXP);
		assertTrue(MyriadEcho.isMarked(echo));
	}

	private static final class TestShadow extends MyriadBlackShadow {
		private Object lootClassForTest() {
			return loot;
		}

		private Mob splitForTest() {
			return createSplitOffspring();
		}
	}
}
