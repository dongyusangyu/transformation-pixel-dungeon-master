package com.shatteredpixel.shatteredpixeldungeon.levels;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SurfaceTownLevelTest {

	@Test
	public void surfaceShopContainsOneSublimationScrollPerAllowedBoss() throws Exception {
		Path sourcePath = Paths.get("src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/SurfaceTownLevel.java");
		String source = Files.readString(sourcePath);

		assertTrue(source.contains("import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"GOO\")"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"TENGU\")"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"DM300\")"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"DWARFKING\")"));
		assertEquals(1, occurrences(source, "new ScrollOfSublimation().type(\"YOG\")"));
		assertEquals(3, occurrences(source, "items.add(new Torch());"));
	}

	private static int occurrences(String source, String token) {
		int count = 0;
		int offset = 0;
		while ((offset = source.indexOf(token, offset)) >= 0) {
			count++;
			offset += token.length();
		}
		return count;
	}
}
