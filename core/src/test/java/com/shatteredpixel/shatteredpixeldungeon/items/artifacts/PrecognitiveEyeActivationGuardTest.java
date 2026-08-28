package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class PrecognitiveEyeActivationGuardTest {

	@Test
	public void activationUsesTheCommonArtifactActionGuard() throws IOException {
		String source = sourceFile();
		String execute = source.substring(source.indexOf("public void execute"),
				source.indexOf("private boolean canActivate"));

		assertTrue(execute.contains("canUseActiveAction(hero)"));
	}

	private static String sourceFile() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/PrecognitiveEye.java")),
				StandardCharsets.UTF_8);
	}
}
