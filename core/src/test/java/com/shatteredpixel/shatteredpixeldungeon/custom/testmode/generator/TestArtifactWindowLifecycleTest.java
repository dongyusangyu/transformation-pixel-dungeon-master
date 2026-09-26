package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TestArtifactWindowLifecycleTest {

	@Test
	public void successfulGenerationReleasesTheModalWindow() throws IOException {
		String source = readCoreSource(
				"custom/testmode/generator/TestArtifact.java");
		assertTrue(source.contains("SettingsWindow.this.hide()"));
	}

	@Test
	public void bagTabsUseOffsetAwareLayoutWhenReopenedAfterGeneration() throws IOException {
		String source = readCoreSource("windows/WndTabbed.java");
		assertTrue(source.contains("centeredCameraOrigin(insets.left"));
		assertTrue(source.contains("centeredCameraOrigin(insets.top"));
		assertTrue(source.contains("xOffset, camera.zoom"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
				.resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
