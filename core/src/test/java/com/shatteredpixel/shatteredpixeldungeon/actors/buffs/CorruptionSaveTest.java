package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.watabou.utils.Bundle;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class CorruptionSaveTest {

	@Test
	public void fractionalDamageProgressSurvivesSaveAndLoad() throws Exception {
		Corruption original = new Corruption();
		Field progress = Corruption.class.getDeclaredField("buildToDamage");
		progress.setAccessible(true);
		progress.setFloat(original, 0.75f);

		Bundle saved = new Bundle();
		original.storeInBundle(saved);
		Corruption restored = new Corruption();
		restored.restoreFromBundle(saved);

		assertEquals(0.75f, progress.getFloat(restored), 0.0001f);
	}

	@Test
	public void olderSavesWithoutProgressStartAtZero() throws Exception {
		Corruption restored = new Corruption();
		restored.restoreFromBundle(new Bundle());

		Field progress = Corruption.class.getDeclaredField("buildToDamage");
		progress.setAccessible(true);
		assertEquals(0f, progress.getFloat(restored), 0.0001f);
	}
}
