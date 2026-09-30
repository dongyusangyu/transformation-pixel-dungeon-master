package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.audio.OggInputStream;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import org.junit.Test;
import java.io.File;
import java.io.FileInputStream;
import static org.junit.Assert.*;

public class ExtractionRaidAudioTest {
	@Test public void raidMusicAndDeathSoundDecodeThroughTheDesktopVorbisPlayer() throws Exception {
		for (String name : new String[]{Assets.Music.UES, Assets.Sounds.DRONEDIED}) {
			File root = new File("../core/src/main/assets");
			if (!root.isDirectory()) root = new File("core/src/main/assets");
			try (OggInputStream stream = new OggInputStream(new FileInputStream(new File(root, name)))) {
				int channels = stream.getChannels();
				int sampleRate = stream.getSampleRate();
				assertTrue(channels >= 1 && channels <= 2);
				assertTrue(sampleRate > 0);
				byte[] buffer = new byte[32768];
				long total = 0;
				boolean audible = false;
				int read;
				while ((read = stream.read(buffer)) > 0) {
					total += read;
					for (int i = 0; i < read; i++) audible |= buffer[i] != 0;
				}
				assertTrue(name, total > sampleRate * channels * 2L / 2);
				assertTrue(name + " is silent", audible);
				System.out.println(name + ": decoded " + total + " PCM bytes, "
						+ channels + " channels, " + sampleRate + " Hz, "
						+ total / (2.0 * channels * sampleRate) + " seconds");
			}
		}
	}
}
