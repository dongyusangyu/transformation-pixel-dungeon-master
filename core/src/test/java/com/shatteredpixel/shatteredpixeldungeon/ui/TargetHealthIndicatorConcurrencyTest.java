package com.shatteredpixel.shatteredpixeldungeon.ui;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TargetHealthIndicatorConcurrencyTest {

	@Test
	public void updateUsesOneTargetSnapshotAcrossLivenessAndSpriteChecks() throws IOException {
		String source = readTargetHealthIndicatorSource();
		int updateStart = source.indexOf("public void update() {");
		int updateEnd = source.indexOf("\n\tpublic void target(", updateStart);
		String update = source.substring(updateStart, updateEnd);

		assertTrue(source.contains("volatile Char target;"));
		assertTrue(update.contains("Char currentTarget = target;"));
		assertTrue(update.contains("currentTarget.sprite"));
		assertTrue(update.contains("level(currentTarget);"));
	}

	private static String readTargetHealthIndicatorSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
				.resolve("ui/TargetHealthIndicator.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
