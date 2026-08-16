package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class V029ChangelogIntegrationTest {

	private static final String[] BASE_NEW_BUTTONS = {
			"v0_2_9.button_1.title",
			"v0_2_9.button_2.title",
			"v0_2_9.button_3.title",
			"v0_2_9.button_4.title"
	};
	private static final String[] BASE_NEW_ICONS = {
			"Icons.get(Icons.TALENT)",
			"HeroSprite.avatar(HeroClass.FRIAR, 6)",
			"Icons.get(Icons.CATALOG)",
			"Icons.get(Icons.CHALLENGE_COLOR)"
	};

	private static final String[] BASE_CHANGE_BUTTONS = {
			"v0_2_9.button_5.title",
			"v0_2_9.button_6.title"
	};
	private static final String[] BASE_CHANGE_ICONS = {
			"Icons.get(Icons.PREFS)",
			"Assets.Sprites.SPINNER"
	};

	private static final String[] FIX_NEW_BUTTONS = {
			"v0_2_9fix.button_1.title",
			"v0_2_9fix2.button_1.title",
			"v0_2_9fix3.button_1.title",
			"v0_2_9fix3.button_2.title",
			"v0_2_9fix4.button_1.title"
	};
	private static final String[] FIX_NEW_ICONS = {
			"Icons.get(Icons.TALENT)",
			"ItemSpriteSheet.ARMOR_CLOTH",
			"Icons.get(Icons.CHALLENGE_COLOR)",
			"Icons.get(Icons.CATALOG)",
			"Icons.get(Icons.CATALOG)"
	};

	private static final String[] FIX_CHANGE_BUTTONS = {
			"v0_2_9fix.button_2.title",
			"v0_2_9fix.button_3.title",
			"v0_2_9fix.button_4.title",
			"v0_2_9fix.button_5.title",
			"v0_2_9fix2.button_2.title",
			"v0_2_9fix2.button_3.title",
			"v0_2_9fix3.button_3.title",
			"v0_2_9fix4.button_2.title",
			"v0_2_9fix4.button_3.title",
			"v0_2_9fix4.button_4.title"
	};
	private static final String[] FIX_CHANGE_ICONS = {
			"ItemSpriteSheet.RITUAL_DAGGER",
			"ItemSpriteSheet.BRONZE_WATCH",
			"new TenguSprite()",
			"Assets.Sprites.SPINNER",
			"Icons.get(Icons.PREFS)",
			"Assets.Sprites.SPINNER",
			"Icons.get(Icons.BUFFS)",
			"Icons.get(Icons.PREFS)",
			"Assets.Sprites.SPINNER",
			"Icons.get(Icons.DISPLAY)"
	};

	@Test
	public void fixReleasesAreMergedIntoTheSingleV029Entry() throws Exception {
		String source = source();
		String method = method(source, "add_v0_2_9Changes", "add_v0_2_0Changes");
		int changesSection = method.indexOf(
				"new ChangeInfo(Messages.get(ChangesScene.class, \"changes\")");

		assertFalse(source.contains("add_v0_2_9fixChanges("));
		assertFalse(source.contains("new ChangeInfo(\"v0.2.9fix\""));
		assertFalse(source.contains("new ChangeInfo(\"fix1\""));
		assertFalse(source.contains("new ChangeInfo(\"fix2\""));
		assertFalse(source.contains("new ChangeInfo(\"fix3\""));
		assertFalse(source.contains("new ChangeInfo(\"fix4\""));
		assertTrue(changesSection > 0);

		String newSection = method.substring(0, changesSection);
		String changeSection = method.substring(changesSection);
		assertButtonsInOrder(newSection, BASE_NEW_BUTTONS, BASE_NEW_ICONS);
		assertButtonsInOrder(newSection, FIX_NEW_BUTTONS, FIX_NEW_ICONS);
		assertButtonsInOrder(changeSection, BASE_CHANGE_BUTTONS, BASE_CHANGE_ICONS);
		assertButtonsInOrder(changeSection, FIX_CHANGE_BUTTONS, FIX_CHANGE_ICONS);
		assertTrue(newSection.indexOf(BASE_NEW_BUTTONS[BASE_NEW_BUTTONS.length - 1])
				< newSection.indexOf(FIX_NEW_BUTTONS[0]));
		assertTrue(changeSection.indexOf(BASE_CHANGE_BUTTONS[BASE_CHANGE_BUTTONS.length - 1])
				< changeSection.indexOf(FIX_CHANGE_BUTTONS[0]));
	}

	private static void assertButtonsInOrder(String section, String[] keys, String[] icons) {
		assertEquals(keys.length, icons.length);
		int previous = -1;
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			int position = section.indexOf(key);
			assertTrue(key, position > previous);
			assertEquals(key, 1, occurrences(section, key));
			String call = buttonCall(section, key);
			assertTrue(key + " icon", call.contains(icons[i]));
			assertMessageArguments(call, key);
			previous = position;
		}
	}

	private static String buttonCall(String section, String titleKey) {
		int title = section.indexOf(titleKey);
		int start = section.lastIndexOf("changes.addButton", title);
		int end = section.indexOf("));", title);
		assertTrue(titleKey, start >= 0 && end > title);
		return section.substring(start, end + 3);
	}

	private static void assertMessageArguments(String call, String titleKey) {
		String stem = titleKey.substring(0, titleKey.length() - ".title".length());
		if (stem.equals("v0_2_9.button_6")
				|| stem.equals("v0_2_9fix.button_5")
				|| stem.equals("v0_2_9fix2.button_3")
				|| stem.equals("v0_2_9fix4.button_2")) {
			assertTrue(stem, call.contains(stem + ".text_1"));
			assertTrue(stem, call.contains(stem + ".text_2"));
		} else {
			assertTrue(stem, call.contains(stem + ".text"));
		}
	}

	private static int occurrences(String text, String needle) {
		int count = 0;
		int position = 0;
		while ((position = text.indexOf(needle, position)) >= 0) {
			count++;
			position += needle.length();
		}
		return count;
	}

	private static String method(String source, String name, String nextName) {
		int start = source.indexOf("public static void " + name);
		int end = source.indexOf("public static void " + nextName, start);
		assertTrue(name, start >= 0 && end > start);
		return source.substring(start, end);
	}

	private static String source() throws Exception {
		Path core = Paths.get(System.getProperty("user.dir"));
		if (!core.endsWith("core")) core = core.resolve("core");
		return new String(Files.readAllBytes(core.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/changelist/v0_2_X.java")),
				StandardCharsets.UTF_8);
	}
}
