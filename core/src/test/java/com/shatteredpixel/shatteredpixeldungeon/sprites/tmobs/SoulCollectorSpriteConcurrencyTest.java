package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertTrue;

public class SoulCollectorSpriteConcurrencyTest {

	@Test
	public void soulMarkersUseACollectionSafeForActorAndRenderThreads() throws Exception {
		Field markers = SoulCollectorSprite.class.getDeclaredField("soulMarkers");

		assertTrue(ConcurrentMap.class.isAssignableFrom(markers.getType()));
	}
}
