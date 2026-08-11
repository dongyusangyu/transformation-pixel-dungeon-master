package com.shatteredpixel.shatteredpixeldungeon.scenes;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HeroHallSceneTest {

	@Test
	public void scrollPaneOwnsDragInputAndForwardsOnlyClicksToRecords() throws Exception {
		String source = heroHallSource();

		assertTrue(source.contains("new ScrollPane(content)"));
		assertTrue(source.contains("public void onClick(float x, float y)"));
		assertTrue(source.contains("row.inside(x, y)"));
		assertTrue(source.contains("new ScrollableRecord("));
		assertTrue(source.contains("hotArea.active = false;"));
		assertFalse(source.contains("new RankingsScene.Record(i, false, records.get(i), true)"));
	}

	private String heroHallSource() throws Exception {
		Path sourceRoot = Paths.get("src/main/java");
		if (!Files.isDirectory(sourceRoot)) {
			sourceRoot = Paths.get("core/src/main/java");
		}
		Path source = sourceRoot.resolve(
				"com/shatteredpixel/shatteredpixeldungeon/scenes/HeroHallScene.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
