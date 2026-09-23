package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class SpiritBowStrengthRequirementTest {

	@Test
	public void spiritArrowDelegatesBothStrengthRequirementOverloadsToItsBow() throws Exception {
		String source = Files.readString(spiritBowSource());

		assertTrue(source.contains("public int STRReq() {\n"
				+ "\t\t\treturn SpiritBow.this.STRReq();\n"
				+ "\t\t}"));
		assertTrue(source.contains("public int STRReq(int lvl) {\n"
				+ "\t\t\treturn SpiritBow.this.STRReq(lvl);\n"
				+ "\t\t}"));
	}

	private static Path spiritBowSource() {
		Path projectRoot = Path.of("core/src/main/java");
		if (!Files.isDirectory(projectRoot)) projectRoot = Path.of("src/main/java");
		return projectRoot.resolve("com/shatteredpixel/shatteredpixeldungeon/items/weapon/SpiritBow.java");
	}
}
