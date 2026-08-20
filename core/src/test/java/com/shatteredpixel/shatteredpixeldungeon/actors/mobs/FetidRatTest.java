package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class FetidRatTest {

	@Test
	public void wanderingUsesTheValidDestinationWhenTheOtherIsMissing() throws Exception {
		int[] distance = new int[10];
		assertEquals(7, chooseCloserDestination(-1, 7, distance));
		assertEquals(7, chooseCloserDestination(7, -1, distance));
	}

	@Test
	public void wanderingKeepsNoDestinationWhenBothCandidatesAreMissing() throws Exception {
		assertEquals(-1, chooseCloserDestination(-1, -1, new int[0]));
	}

	private static int chooseCloserDestination(int first, int second, int[] distance)
			throws Exception {
		Method method = FetidRat.class.getDeclaredMethod(
				"chooseCloserDestination", int.class, int.class, int[].class);
		method.setAccessible(true);
		return (Integer) method.invoke(null, first, second, distance);
	}
}
