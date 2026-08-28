package com.watabou.noosa.audio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MusicFadeEnvelopeTest {

	@Test
	public void fadeInRampsFromSilenceToFullVolume() {
		assertEquals(0f, Music.fadeInMultiplier(0f, 2f), 0f);
		assertEquals(0.5f, Music.fadeInMultiplier(1f, 2f), 0f);
		assertEquals(1f, Music.fadeInMultiplier(2f, 2f), 0f);
	}

	@Test
	public void fadeOutRampsFromFullVolumeToSilence() {
		assertEquals(1f, Music.fadeOutMultiplier(0f, 2f), 0f);
		assertEquals(0.5f, Music.fadeOutMultiplier(1f, 2f), 0f);
		assertEquals(0f, Music.fadeOutMultiplier(2f, 2f), 0f);
	}
}
