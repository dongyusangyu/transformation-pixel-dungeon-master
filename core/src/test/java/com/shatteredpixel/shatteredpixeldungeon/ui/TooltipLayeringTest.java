package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.watabou.noosa.Camera;
import com.watabou.noosa.Group;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TooltipLayeringTest {

	@Test
	public void nestedButtonsPromoteTooltipsToTheirSharedCameraLayer() throws Exception {
		Camera windowCamera = new Camera(0, 0, 100, 100, 1);
		Camera scrollCamera = new Camera(0, 0, 100, 40, 1);
		Group window = new Group();
		window.camera = windowCamera;
		Group scrollContent = new Group();
		scrollContent.camera = scrollCamera;
		Group compositeButton = new Group();
		TestButton button = new TestButton();

		window.add(scrollContent);
		scrollContent.add(compositeButton);
		compositeButton.add(button);

		assertSame(scrollContent, button.tooltipHost());
		assertTrue(buttonSource().contains("tooltipParent.addToFront(hoverTip);"));
	}

	@Test
	public void tooltipsUseTheCurrentCameraViewportForVerticalPlacement() throws Exception {
		String source = buttonSource();

		assertTrue(source.contains("float viewportTop = cam.scroll.y;"));
		assertTrue(source.contains("float viewportBottom = viewportTop + cam.height;"));
		assertTrue(source.contains("if (tip.top() < viewportTop)"));
	}

	private String buttonSource() throws Exception {
		Path sourceRoot = Paths.get("src/main/java");
		if (!Files.isDirectory(sourceRoot)) {
			sourceRoot = Paths.get("core/src/main/java");
		}
		Path source = sourceRoot.resolve(
				"com/shatteredpixel/shatteredpixeldungeon/ui/Button.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

	private static class TestButton extends Button {
		Group tooltipHost() {
			return tooltipParent();
		}
	}
}
