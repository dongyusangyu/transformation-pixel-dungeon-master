package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MobEnemyReferenceTest {

	@Before
	public void resetActors() {
		Actor.clear();
		Actor.resetNextID();
	}

	@After
	public void clearActors() {
		Actor.clear();
		Actor.resetNextID();
	}

	@Test
	public void duplicateActorIdCannotReplaceExistingCharacter() {
		TestMob source = new TestMob();
		Actor.add(source);
		int sourceId = source.id();

		Bundle staleIdState = new Bundle();
		staleIdState.put("nextid", sourceId);
		Actor.restoreNextID(staleIdState);

		Regeneration regeneration = new Regeneration();
		Actor.add(regeneration);

		assertSame(source, Actor.findById(sourceId));
		assertNotEquals(sourceId, regeneration.id());
	}

	@Test
	public void characterKeepsStoredIdWhenConflictingBuffWasRegisteredFirst() {
		Regeneration regeneration = new Regeneration();
		Actor.add(regeneration);
		int storedCharacterId = regeneration.id();

		Bundle staleIdState = new Bundle();
		staleIdState.put("nextid", storedCharacterId);
		Actor.restoreNextID(staleIdState);

		TestMob source = new TestMob();
		Actor.add(source);

		assertSame(source, Actor.findById(storedCharacterId));
		assertNotEquals(storedCharacterId, regeneration.id());
	}

	@Test
	public void charmIgnoresSourceIdThatResolvesToBuff() {
		TestMob victim = new TestMob();
		Actor.add(victim);

		Regeneration regeneration = new Regeneration();
		Actor.add(regeneration);

		Charm charm = new Charm();
		assertTrue(charm.attachTo(victim));
		charm.object = regeneration.id();

		assertNull(victim.chooseEnemyForTest());
	}

	private static class TestMob extends Mob {
		private TestMob() {
			alignment = Char.Alignment.NEUTRAL;
		}

		private Char chooseEnemyForTest() {
			return chooseEnemy();
		}
	}
}
