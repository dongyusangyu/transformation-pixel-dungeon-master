package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExtractionRaidMobsTest {

	@Test
	public void customMobsKeepTheirOriginalCombatKits() throws Exception {
		assertTrue(ArmoredStatue.class.isAssignableFrom(type("VaultArmoredStatue")));
		assertTrue(Eye.class.isAssignableFrom(type("VeilbreakerEye")));
		assertTrue(Succubus.class.isAssignableFrom(type("ChronoSuccubus")));
		assertTrue(Scorpio.class.isAssignableFrom(type("DeferredScorpio")));
	}

	@Test
	public void veilbreakerChanceUsesDistancePlusOne() throws Exception {
		Method chance = type("VeilbreakerEye").getMethod("revealChance", int.class);
		assertEquals(0.5f, (Float) chance.invoke(null, 1), 0.0001f);
		assertEquals(0.25f, (Float) chance.invoke(null, 3), 0.0001f);
		assertEquals(0.125f, (Float) chance.invoke(null, 7), 0.0001f);
	}

	@Test
	public void veilbreakerConeCoversSixtyDegreesAndRespectsItsRange() throws Exception {
		Method contains = type("VeilbreakerEye").getMethod("coneContains",
				float.class, float.class, float.class, float.class,
				float.class, float.class, float.class);

		assertTrue((Boolean) contains.invoke(null, 0f, 0f, 5f, 0f, 3f, 0f, 5f));
		assertTrue((Boolean) contains.invoke(null, 0f, 0f, 5f, 0f, 3f, 1f, 5f));
		assertTrue((Boolean) contains.invoke(null, 0f, 0f, 5f, 0f, 3f, -1f, 5f));
		assertFalse((Boolean) contains.invoke(null, 0f, 0f, 5f, 0f, 1f, 3f, 5f));
		assertFalse((Boolean) contains.invoke(null, 0f, 0f, 5f, 0f, 6f, 0f, 5f));
	}

	@Test
	public void chronoSuccubusChanceUsesRemainingTurnsReciprocal() throws Exception {
		Method chance = type("ChronoSuccubus").getMethod("breakChance", float.class);
		assertEquals(1f, (Float) chance.invoke(null, 1f), 0.0001f);
		assertEquals(1f / 3f, (Float) chance.invoke(null, 3f), 0.0001f);
		assertEquals(0.1f, (Float) chance.invoke(null, 9.2f), 0.0001f);
	}

	@Test
	public void deferredScorpioUsesSixLowDamageStrikesPerTurn() throws Exception {
		Class<?> type = type("DeferredScorpio");
		assertEquals(6, type.getField("ATTACKS_PER_TURN").getInt(null));
		assertEquals(0.2f, type.getField("ARMOR_PIERCE_CHANCE").getFloat(null), 0.0001f);
		Method pierceDamage = type.getMethod("armorPierceDamage", int.class, int.class, float.class);
		assertEquals(11, pierceDamage.invoke(null, 4, 7, 0.19f));
		assertEquals(4, pierceDamage.invoke(null, 4, 7, 0.2f));

		Scorpio scorpio = (Scorpio) type.getDeclaredConstructor().newInstance();
		assertEquals(new Scorpio().attackDelay() / 6f, scorpio.attackDelay(), 0.0001f);
		for (int i = 0; i < 128; i++) {
			int damage = scorpio.damageRoll();
			assertTrue(damage >= 2 && damage <= 8);
		}
	}

	@Test
	public void allRaidMobsGainTenAttackSkill() throws Exception {
		assertEquals(new Succubus().attackSkill(null) + 10,
				((Succubus) type("ChronoSuccubus").getDeclaredConstructor().newInstance()).attackSkill(null));
		assertEquals(int.class, type("VeilbreakerEye")
				.getDeclaredMethod("attackSkill", com.shatteredpixel.shatteredpixeldungeon.actors.Char.class)
				.getReturnType());
		assertEquals(new Scorpio().attackSkill(null) + 10,
				((Scorpio) type("DeferredScorpio").getDeclaredConstructor().newInstance()).attackSkill(null));
		assertEquals(int.class, type("VaultArmoredStatue")
				.getDeclaredMethod("attackSkill", com.shatteredpixel.shatteredpixeldungeon.actors.Char.class)
				.getReturnType());
	}

	@Test
	public void vaultArmoredStatueStartsActivated() throws Exception {
		Object statue = type("VaultArmoredStatue").getDeclaredConstructor().newInstance();
		assertTrue((Boolean) type("VaultArmoredStatue")
				.getMethod("isRaidActive").invoke(statue));
	}

	private static Class<?> type(String simpleName) throws ClassNotFoundException {
		return Class.forName(
				"com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs."
						+ simpleName);
	}
}
