package com.shatteredpixel.shatteredpixeldungeon.ui;

import org.junit.Test;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class HighlightMarkupTest {

	@Test
	public void desktopTokensDoNotLeakChallengeColors() {
		List<HighlightMarkup.Part> parts = HighlightMarkup.parse(new String[]{
				"￡地牢原住民们变得更强了！", "(等级2)￡", "\n",
				"￥这些怪物只会不断变强", "，当心！(等级3)￥", "\n", "-普通敌人"
		}, true, true, true, true);
		assertEquals(HighlightMarkup.Tone.ORANGE, toneOf(parts, "地牢原住民们变得更强了！"));
		assertEquals(HighlightMarkup.Tone.ORANGE, toneOf(parts, "(等级2)"));
		assertEquals(HighlightMarkup.Tone.RED, toneOf(parts, "这些怪物只会不断变强"));
		assertEquals(HighlightMarkup.Tone.NORMAL, toneOf(parts, "-普通敌人"));
	}

	@Test
	public void markersAreRecognizedBesideAsciiAndMixedStylesKeepPriority() {
		List<HighlightMarkup.Part> parts = HighlightMarkup.parse(new String[]{
				"before￥red_amber_red￥after €green€"
		}, true, true, true, true);
		assertEquals(HighlightMarkup.Tone.NORMAL, toneOf(parts, "before"));
		assertEquals(HighlightMarkup.Tone.RED, toneOf(parts, "red"));
		assertEquals(HighlightMarkup.Tone.TITLE, toneOf(parts, "amber"));
		assertEquals(HighlightMarkup.Tone.NORMAL, toneOf(parts, "after "));
		assertEquals(HighlightMarkup.Tone.GREEN, toneOf(parts, "green"));
	}

	@Test
	public void unmatchedMarkerRemainsLiteralWithoutColoringFollowingLines() {
		List<HighlightMarkup.Part> parts = HighlightMarkup.parse(new String[]{"￥", "danger", "\n", "normal"},
				true, true, true, true);
		assertEquals(HighlightMarkup.Tone.NORMAL, toneOf(parts, "￥"));
		assertEquals(HighlightMarkup.Tone.NORMAL, toneOf(parts, "danger"));
		assertEquals(HighlightMarkup.Tone.NORMAL, toneOf(parts, "normal"));
	}

	@Test
	public void localizedChampionDescriptionRestoresOrdinaryTextColor() throws Exception {
		Path root = Paths.get(System.getProperty("user.dir"));
		if (!Files.isDirectory(root.resolve("src/main/assets"))) root = root.resolve("core");
		Properties messages = new Properties();
		try (Reader reader = Files.newBufferedReader(
				root.resolve("src/main/assets/messages/misc/misc_zh.properties"), StandardCharsets.UTF_8)) {
			messages.load(reader);
		}
		String description = messages.getProperty("challenges.champion_enemies_desc");
		List<HighlightMarkup.Part> parts = HighlightMarkup.parse(new String[]{description},
				true, true, true, true);
		assertEquals(HighlightMarkup.Tone.NORMAL, toneContaining(parts, "-普通敌人生成时"));
		assertEquals(HighlightMarkup.Tone.NORMAL, toneContaining(parts, "-精英敌人免疫腐化效果"));
		assertEquals(HighlightMarkup.Tone.RED, toneContaining(parts, "英雄不再能感知到"));
	}

	private static HighlightMarkup.Tone toneOf(List<HighlightMarkup.Part> parts, String text) {
		for (HighlightMarkup.Part part : parts) {
			if (part.text.equals(text)) return part.tone;
		}
		throw new AssertionError("Missing part: " + text);
	}

	private static HighlightMarkup.Tone toneContaining(List<HighlightMarkup.Part> parts, String text) {
		for (HighlightMarkup.Part part : parts) {
			if (part.text.contains(text)) return part.tone;
		}
		throw new AssertionError("Missing text: " + text);
	}
}
