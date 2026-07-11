package com.shatteredpixel.shatteredpixeldungeon.plants;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SwiftthistleTest {

	@Test
	public void detachingForTargetingDoesNotAdvanceTheCharacter() {
		TrackingChar target = new TrackingChar();
		Swiftthistle.TimeBubble bubble = new Swiftthistle.TimeBubble() {
			@Override
			public void triggerPresses() {
				// Delayed terrain effects are unrelated to this lifecycle assertion.
			}
		};
		bubble.attachTo(target);

		bubble.detachForTargeting();

		assertEquals(0, target.nextCalls);
	}

	private static class TrackingChar extends Char {
		private int nextCalls;

		@Override
		public void next() {
			nextCalls++;
		}
	}
}
