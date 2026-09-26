package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.watabou.utils.RectF;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeroSelectSceneTest {

	@Test
	public void bottomSafeOffsetOnlyAppliesWhenNavigationInsetExists() {
		assertEquals(0f, HeroSelectScene.bottomSafeOffset(new RectF(0, 0, 0, 0)), 0f);
		assertEquals(26f, HeroSelectScene.bottomSafeOffset(new RectF(0, 0, 0, 24)), 0f);
	}

	@Test
	public void enablingRandomModeRequiresConfirmationAndUsesScrollableWindow() throws Exception {
		String scene = readFile("src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/HeroSelectScene.java");
		String window = readFile("src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndRandomModeConfirm.java");
		String english = readFile("src/main/assets/messages/scenes/scenes.properties");
		String chinese = readFile("src/main/assets/messages/scenes/scenes_zh.properties");

		assertTrue(scene.contains("if (SPDSettings.randomMode())"));
		assertTrue(scene.contains("new WndRandomModeConfirm(() ->"));
		assertTrue(window.contains("extends WndTitledMessage"));
		assertTrue(window.contains("Icons.SHUFFLE_SLIVER"));
		assertTrue(window.contains("random_mode_enable"));
		assertTrue(window.contains("random_mode_cancel"));
		assertTrue(english.contains("scenes.heroselectscene.random_mode_desc="));
		assertTrue(chinese.contains("scenes.heroselectscene.random_mode_desc="));
		assertTrue(chinese.contains("_随机模式会正常获得徽章"));
	}

	private String readFile(String relativePath) throws Exception {
		Path root = Paths.get("core");
		if (!Files.isDirectory(root)) root = Paths.get(".");
		return new String(Files.readAllBytes(root.resolve(relativePath)), StandardCharsets.UTF_8);
	}
}
