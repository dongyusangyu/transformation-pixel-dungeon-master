package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TowerShopRoomTest {

	@Test
	public void towerShopFiltersDartsFromItsFinalStock() throws Exception {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/levels/towers/TowerShopRoom.java");

		assertTrue(source.contains("import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;"));
		assertTrue(source.contains("items.removeIf(item -> item instanceof Dart);"));
		assertTrue(source.contains("TowerGenerationRules.guaranteedShopItem()"));
		assertTrue(source.contains("TowerGenerationRules.guaranteedShopMetamorphosis()"));
	}

	private static String readCoreSource(String relativePath) throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = Files.isDirectory(workingDirectory.resolve("core"))
				? workingDirectory.resolve("core") : workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve("src/main/java").resolve(relativePath)),
				StandardCharsets.UTF_8);
	}
}
