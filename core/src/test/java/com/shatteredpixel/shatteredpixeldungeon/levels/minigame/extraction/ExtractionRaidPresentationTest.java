package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong.Enthralled;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.utils.Bundle;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class ExtractionRaidPresentationTest {
	private HeadlessItemSprites sheets;
	@Before public void setUp() { sheets = new HeadlessItemSprites(); }
	@After public void tearDown() { sheets.close(); }
	@Test public void raidUsesItsOwnEnvironment() {
		ExtractionRaidLevel level = new ExtractionRaidLevel();
		assertEquals("environment/tiles_UES.png", level.tilesTex());
		assertEquals("environment/water_UES.png", level.waterTex());
	}

	@Test public void allRaidDronesAreFlyingInorganicMinibosses() {
		for (Mob mob : drones()) {
			assertTrue(mob.getClass().getSimpleName(), mob.flying);
			assertTrue(Char.hasProp(mob, Char.Property.MINIBOSS));
			assertTrue(Char.hasProp(mob, Char.Property.INORGANIC));
			assertFalse(Char.hasProp(mob, Char.Property.DEMONIC));
			assertFalse(Char.hasProp(mob, Char.Property.BOSS));
			assertTrue(mob.isImmune(AllyBuff.class));
			assertTrue(mob.isImmune(Dread.class));
		}
	}

	@Test public void legacyAlliedDronesReturnToEnemyAlignmentWithoutLosingHealth() {
		for (Mob mob : drones()) {
			mob.HP = 17;
			mob.alignment = Char.Alignment.ALLY;
			mob.state = mob.FLEEING;
			Bundle bundle = new Bundle();
			mob.storeInBundle(bundle);
			mob.restoreFromBundle(bundle);
			assertEquals(Char.Alignment.ENEMY, mob.alignment);
			assertEquals(17, mob.HP);
			assertSame(mob.WANDERING, mob.state);
			assertTrue(mob.flying);
		}
	}

	@Test public void conversionIsRejectedButOrdinaryStatusEffectsAreNotBlanketBlocked() {
		for (Mob mob : drones()) {
			assertFalse(new Corruption().attachTo(mob));
			assertFalse(new Enthralled().attachTo(mob));
			assertFalse(new Dread().attachTo(mob));
			assertEquals(Char.Alignment.ENEMY, mob.alignment);
			assertTrue(mob.isImmune(Poison.class));
			assertTrue(mob.isImmune(Bleeding.class));
			assertFalse(mob.isImmune(Paralysis.class));
		}
		assertFalse(Char.hasProp(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio(), Char.Property.MINIBOSS));
		assertTrue(Char.hasProp(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus(), Char.Property.DEMONIC));
	}

	@Test public void legacyTemporaryLevitationCannotOverrideIntrinsicDroneFlight() {
		for (Mob mob : drones()) {
			mob.HP = 17;
			Bundle bundle = new Bundle();
			mob.storeInBundle(bundle);
			// Old non-flying raid enemies could acquire temporary levitation.
			bundle.put("buffs", java.util.Collections.singletonList(new Levitation()));
			mob.restoreFromBundle(bundle);
			assertNull(mob.getClass().getSimpleName(), mob.buff(Levitation.class));
			assertTrue(mob.flying);
			assertEquals(17, mob.HP);
			assertFalse(new Levitation().attachTo(mob));
		}
	}

	@Test public void blackLoadingScreenIsLimitedToRaidAndSurfaceDestinations() throws Exception {
		Method black = InterlevelScene.class.getDeclaredMethod("usesBlackBackground", int.class, int.class);
		black.setAccessible(true);
		assertEquals(true, black.invoke(null, 31, 1));
		assertEquals(true, black.invoke(null, 0, 0));
		assertEquals(false, black.invoke(null, 31, 0));
		assertEquals(false, black.invoke(null, 0, 1));
		assertEquals(false, black.invoke(null, -5, 0));
		for (int depth = 1; depth <= 26; depth++) assertEquals(false, black.invoke(null, depth, 0));
	}

	private Mob[] drones() {
		return new Mob[]{new VaultArmoredStatue(), new VeilbreakerEye(),
				new ChronoSuccubus(), new DeferredScorpio()};
	}
}
