package com.shatteredpixel.shatteredpixeldungeon.items;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class DewdropTest {

	@Test
	public void militaryWaterskinBranchChecksForMissingWaterskin() throws Exception {
		Path source = Paths.get(System.getProperty("user.dir"), "core", "src", "main", "java",
				"com", "shatteredpixel", "shatteredpixeldungeon", "items", "Dewdrop.java");
		if (!Files.exists(source)) {
			source = Paths.get(System.getProperty("user.dir"), "src", "main", "java",
					"com", "shatteredpixel", "shatteredpixeldungeon", "items", "Dewdrop.java");
		}

		String contents = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
		assertTrue(contents.contains(
				"} else if(flask != null && hero.hasTalent(Talent.MILITARY_WATERSKIN)"));
	}
}
