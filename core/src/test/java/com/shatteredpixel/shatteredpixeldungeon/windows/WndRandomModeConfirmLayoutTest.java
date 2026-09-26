package com.shatteredpixel.shatteredpixeldungeon.windows;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WndRandomModeConfirmLayoutTest {

	@Test
	public void confirmationButtonsArePositionedByBottomLayout() throws Exception {
		String source = readConfirmWindowSource();

		assertTrue(source.contains("addToBottom(4, 0, enable, cancel);"));
		assertFalse(source.contains("Component footer"));
	}

	private static String readConfirmWindowSource() throws Exception {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core"))
				? working.resolve("core") : working;
		Path source = core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndRandomModeConfirm.java");
		return Files.readString(source, StandardCharsets.UTF_8);
	}
}
