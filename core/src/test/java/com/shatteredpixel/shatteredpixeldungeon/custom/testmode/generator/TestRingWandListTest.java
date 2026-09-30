package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCursedFlame;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestRingWandListTest {

	@Test
	public void wandGeneratorIncludesCursedFlameAndUsesItemSpriteForIcons() throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/testmode/generator/TestRing.java");
		String source = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
		assertTrue(source.contains("WandOfCursedFlame.class"));
		assertTrue(source.contains("new ItemSprite("));
	}

	@Test
	public void lastWandEntryCreatesTheCursedFlameWand() throws Exception {
		java.lang.reflect.Method method = TestRing.class.getDeclaredMethod("idToWand", int.class);
		method.setAccessible(true);
		assertEquals(WandOfCursedFlame.class, method.invoke(new TestRing(), 13));
	}
}
