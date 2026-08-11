package com.shatteredpixel.shatteredpixeldungeon.actors;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CharDeathListenerTest {

	private final TestDeathListener listener = new TestDeathListener();
	private final Char victim = new Char() { };

	@After
	public void cleanActors() {
		Actor.remove(listener);
		Actor.remove(victim);
	}

	@Test
	public void finalDeathNotifiesRegisteredListenersExactlyOnce() {
		listener.HP = listener.HT = 1;
		victim.HP = victim.HT = 1;
		Actor.add(listener);
		Actor.add(victim);

		victim.HP = 0;
		victim.die(null);
		victim.die(null);

		assertEquals(1, listener.calls);
		assertSame(victim, listener.lastDeath);
	}

	private static class TestDeathListener extends Char implements Char.DeathListener {
		int calls;
		Char lastDeath;

		@Override
		public void onCharDied(Char deceased) {
			calls++;
			lastDeath = deceased;
		}
	}
}
