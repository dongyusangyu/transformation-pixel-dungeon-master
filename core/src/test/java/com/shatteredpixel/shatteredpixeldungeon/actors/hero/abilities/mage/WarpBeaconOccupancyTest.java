package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class WarpBeaconOccupancyTest {
	@Before public void setUp() { Actor.clear(); }
	@After public void tearDown() { Actor.clear(); }

	@Test public void movingDamageTargetIsNoLongerTreatedAsMarkerOccupant() throws Exception {
		Hero caster = actorAt(17);
		Hero damagedTarget = actorAt(24);
		damagedTarget.pos = 25;

		assertNull(markerOccupant(24, caster));
	}

	@Test public void replacementOnMarkerIsUsedAfterDamageMovesOriginalTarget() throws Exception {
		Hero caster = actorAt(17);
		Hero damagedTarget = actorAt(24);
		damagedTarget.pos = 25;
		Hero replacement = actorAt(24);

		assertSame(replacement, markerOccupant(24, caster));
	}

	@Test public void targetStillOnMarkerRemainsTheOccupant() throws Exception {
		Hero caster = actorAt(17);
		Hero target = actorAt(24);
		assertSame(target, markerOccupant(24, caster));
	}

	@Test public void casterIsNotPushedIfAnEffectMovedThemOntoMarker() throws Exception {
		Hero caster = actorAt(24);
		assertNull(markerOccupant(24, caster));
	}

	private static Hero actorAt(int cell) {
		Hero actor = TestHeroFactory.create();
		actor.pos = cell;
		Actor.add(actor);
		return actor;
	}

	private static Char markerOccupant(int cell, Hero caster) throws Exception {
		Method method = WarpBeacon.class.getDeclaredMethod("markerOccupant", int.class, Hero.class);
		method.setAccessible(true);
		return (Char) method.invoke(null, cell, caster);
	}
}
