package com.shatteredpixel.shatteredpixeldungeon.effects;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/** Verifies that a lightning animation owns its arc collection. */
public class LightningArcOwnershipTest {

	@Test
	@SuppressWarnings("unchecked")
	public void animationKeepsSnapshotWhenSourceListChanges() throws Exception {
		List<Lightning.Arc> source = new ArrayList<>();
		Lightning lightning = new Lightning(source, null);

		source.add(null);

		Field arcsField = Lightning.class.getDeclaredField("arcs");
		arcsField.setAccessible(true);
		List<Lightning.Arc> owned = (List<Lightning.Arc>) arcsField.get(lightning);

		assertNotSame("animation must not retain the caller's mutable list", source, owned);
		assertTrue("animation snapshot must not change with the caller's list", owned.isEmpty());
	}
}
