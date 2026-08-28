package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WandOfBlastWavePushSafetyTest {

	@Test
	public void expiredSpriteCancelsBeforeMovementIsCommitted() throws IOException {
		String throwBody = blockStartingAt(compactSource(), "privatestaticvoidthrowChar(");
		int spriteSnapshot = throwBody.indexOf("finalCharSpritepushingSprite=ch.sprite");
		int pushing = throwBody.indexOf("Pushingpushing=newPushing(");
		assertTrue("knockback must snapshot the sprite before starting its animation",
				spriteSnapshot >= 0 && spriteSnapshot < pushing);

		String pushingCallback = blockStartingAt(
				throwBody.substring(pushing), "publicvoidcall()");
		int invalidSprite = pushingCallback.indexOf("booleanpushSpriteInvalid=");
		int cancelled = pushingCallback.indexOf(
				"completeKnockback(callback,knockbackCallback,false,0)");
		int commitPosition = pushingCallback.indexOf("ch.pos=newPos");
		assertTrue("expired sprites must be checked in the completion callback",
				invalidSprite >= 0);
		assertTrue("an expired sprite must cancel before movement is committed",
				invalidSprite < cancelled && cancelled < commitPosition);
		assertTrue("the sprite identity and scene attachment must both be validated",
				pushingCallback.contains(
						"pushingSprite==null||ch.sprite!=pushingSprite||pushingSprite.parent==null"));
		assertFalse("completion must never dereference the mutable ch.sprite field",
				pushingCallback.contains("ch.sprite.place(ch.pos)"));
		assertTrue("the captured sprite must be rechecked after collision damage",
				pushingCallback.contains(
						"if(ch.sprite==pushingSprite&&pushingSprite.parent!=null)"
								+ "{pushingSprite.place(ch.pos);}"));
	}

	private static String compactSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/wands/"
						+ "WandOfBlastWave.java");
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8)
				.replaceAll("\\s+", "");
	}

	private static String blockStartingAt(String source, String marker) {
		int markerStart = source.indexOf(marker);
		assertTrue("missing source marker: " + marker, markerStart >= 0);
		int blockStart = source.indexOf('{', markerStart);
		assertTrue("missing block for source marker: " + marker, blockStart >= 0);
		int depth = 0;
		for (int i = blockStart; i < source.length(); i++) {
			char current = source.charAt(i);
			if (current == '{') depth++;
			if (current == '}' && --depth == 0) {
				return source.substring(markerStart, i + 1);
			}
		}
		throw new AssertionError("unterminated block for source marker: " + marker);
	}
}
