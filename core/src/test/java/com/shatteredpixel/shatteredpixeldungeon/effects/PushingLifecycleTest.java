package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.custom.buffs.DummyChar;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.PlatformSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PushingLifecycleTest {

	private final Field currentField = currentField();
	private Actor previousCurrent;
	private Thread schedulerThread;
	private boolean previousKeepActorThreadAlive;
	private Game previousGame;
	private PlatformSupport previousPlatform;

	@Before
	public void clearActors() throws IllegalAccessException {
		previousCurrent = (Actor) currentField.get(null);
		previousKeepActorThreadAlive = Actor.keepActorThreadAlive;
		Actor.clear();
		currentField.set(null, null);
	}

	@After
	public void restoreActors() throws IllegalAccessException {
		stopSchedulerThread();
		Actor.clear();
		currentField.set(null, previousCurrent);
		Actor.keepActorThreadAlive = previousKeepActorThreadAlive;
		Game.instance = previousGame;
		Game.platform = previousPlatform;
	}

	@Test
	public void simultaneousPushesStartTogetherAndHoldTheScheduler() throws IllegalAccessException {
		DummyChar target = new DummyChar();
		ControlledPushing first = new ControlledPushing(target, null, true);
		ControlledPushing second = new ControlledPushing(target, null, true);
		Actor.add(first);
		Actor.add(second);
		currentField.set(null, first);

		assertFalse(first.runActorTurn());
		assertEquals(1, first.startCount);
		assertEquals(1, second.startCount);
		assertTrue(Actor.all().contains(first));
		assertTrue(Actor.all().contains(second));
		assertTrue("the current pushing actor must keep the scheduler busy", Actor.processing());

		first.finishAnimation();
		assertFalse(Actor.all().contains(first));
		assertTrue(Actor.all().contains(second));
		assertFalse("the second push remains the next scheduler barrier", Actor.processing());

		currentField.set(null, second);
		assertFalse(second.runActorTurn());
		second.finishAnimation();
		assertFalse(Actor.processing());
	}

	@Test
	public void schedulerDoesNotRunAnotherActorUntilAllPushCallbacksFinish() throws Exception {
		previousGame = Game.instance;
		previousPlatform = Game.platform;
		Game testGame = new Game(null, null);
		Field requestedReset = Game.class.getDeclaredField("requestedReset");
		requestedReset.setAccessible(true);
		requestedReset.setBoolean(testGame, false);

		CountDownLatch otherActorRan = new CountDownLatch(1);
		DummyChar target = new DummyChar();
		ControlledPushing first = new ControlledPushing(target, null, true);
		ControlledPushing second = new ControlledPushing(target, null, true);
		Actor otherActor = new Actor() {
			@Override
			protected boolean act() {
				otherActorRan.countDown();
				return false;
			}
		};
		Actor.add(first);
		Actor.add(second);
		Actor.add(otherActor);
		Actor.keepActorThreadAlive = true;
		schedulerThread = new Thread(Actor::process, "PushingLifecycleTestActorThread");
		schedulerThread.start();

		awaitSchedulerOn(first);
		assertEquals(1, first.startCount);
		assertEquals(1, second.startCount);
		assertEquals("the other actor must remain queued during the first push", 1,
				otherActorRan.getCount());

		first.finishAnimation();
		wakeScheduler();
		awaitSchedulerOn(second);
		assertEquals("one completed push cannot release another active push", 1,
				otherActorRan.getCount());

		second.finishAnimation();
		wakeScheduler();
		assertTrue("the queued actor should run after the push callbacks", otherActorRan.await(2, TimeUnit.SECONDS));
	}

	@Test
	public void completionCallbackRunsBeforeSchedulerIsReleased() throws IllegalAccessException {
		AtomicBoolean callbackSawSchedulerBusy = new AtomicBoolean();
		DummyChar target = new DummyChar();
		ControlledPushing pushing = new ControlledPushing(target,
				() -> callbackSawSchedulerBusy.set(Actor.processing()), true);
		Actor.add(pushing);
		currentField.set(null, pushing);
		assertFalse(pushing.runActorTurn());

		pushing.finishAnimation();

		assertTrue("the target-resolution callback must finish before next() releases the actor thread",
				callbackSawSchedulerBusy.get());
		assertFalse(Actor.processing());
	}

	@Test
	public void immediateStartStillWaitsForTheSameSchedulerBarrier() throws IllegalAccessException {
		AtomicBoolean callbackSawSchedulerBusy = new AtomicBoolean();
		ControlledPushing pushing = new ControlledPushing(new DummyChar(),
				() -> callbackSawSchedulerBusy.set(Actor.processing()), true);
		pushing.startImmediately();
		currentField.set(null, pushing);

		assertFalse(pushing.runActorTurn());
		assertEquals("startImmediately must not launch its visual twice", 1, pushing.startCount);
		pushing.finishAnimation();

		assertTrue(callbackSawSchedulerBusy.get());
		assertFalse(Actor.processing());
	}

	@Test
	public void missingSpriteCompletesWithoutLeavingSchedulerBlocked() throws IllegalAccessException {
		AtomicInteger callbacks = new AtomicInteger();
		ControlledPushing pushing = new ControlledPushing(new DummyChar(), callbacks::incrementAndGet, false);
		Actor.add(pushing);
		currentField.set(null, pushing);

		assertTrue(pushing.runActorTurn());
		assertEquals(1, callbacks.get());
		assertFalse(Actor.all().contains(pushing));
		assertFalse(Actor.processing());
	}

	private static Field currentField() {
		try {
			Field field = Actor.class.getDeclaredField("current");
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException error) {
			throw new AssertionError(error);
		}
	}

	private void awaitSchedulerOn(Actor expected) throws Exception {
		long deadline = System.currentTimeMillis() + 2000;
		while (System.currentTimeMillis() < deadline) {
			if (Actor.threadIdle() && currentField.get(null) == expected) return;
			Thread.sleep(1);
		}
		throw new AssertionError("actor thread did not wait on the expected pushing actor");
	}

	private void wakeScheduler() {
		synchronized (schedulerThread) {
			schedulerThread.notifyAll();
		}
	}

	private void stopSchedulerThread() {
		if (schedulerThread != null && schedulerThread.isAlive()) {
			Actor.keepActorThreadAlive = false;
			schedulerThread.interrupt();
			synchronized (schedulerThread) {
				schedulerThread.notifyAll();
			}
			try {
				schedulerThread.join(2000);
			} catch (InterruptedException error) {
				Thread.currentThread().interrupt();
			}
		}
		schedulerThread = null;
	}

	private static class ControlledPushing extends Pushing {
		private int startCount;
		private final boolean hasVisual;

		private ControlledPushing(DummyChar target, Callback callback, boolean hasVisual) {
			super(target, 0, 1, callback);
			this.hasVisual = hasVisual;
		}

		@Override
		protected boolean startEffect() {
			startCount++;
			return hasVisual;
		}

		private boolean runActorTurn() {
			return act();
		}

		private void finishAnimation() {
			completeEffect();
		}
	}
}
