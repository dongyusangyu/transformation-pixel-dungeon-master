package com.shatteredpixel.shatteredpixeldungeon.ui;

import java.util.ArrayList;
import java.util.List;

final class HighlightMarkup {

	enum Tone { NORMAL, TITLE, RED, ORANGE, GREEN }

	static final class Part {
		final String text;
		final Tone tone;

		Part(String text, Tone tone) {
			this.text = text;
			this.tone = tone;
		}
	}

	private HighlightMarkup() {}

	static List<Part> parse(String[] platformTokens, boolean titleEnabled,
			boolean redEnabled, boolean orangeEnabled, boolean greenEnabled) {
		List<String> tokens = new ArrayList<>();
		for (String token : platformTokens) splitMarkers(token, tokens);

		boolean[] enabled = {titleEnabled, redEnabled, orangeEnabled, greenEnabled};
		boolean[] paired = new boolean[tokens.size()];
		int[] opening = {-1, -1, -1, -1};
		for (int i = 0; i < tokens.size(); i++) {
			int marker = markerIndex(tokens.get(i));
			if (marker < 0 || !enabled[marker]) continue;
			if (opening[marker] < 0) {
				opening[marker] = i;
			} else {
				paired[opening[marker]] = paired[i] = true;
				opening[marker] = -1;
			}
		}

		boolean[] active = new boolean[enabled.length];
		List<Part> result = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			int marker = markerIndex(token);
			if (marker >= 0 && paired[i]) {
				active[marker] = !active[marker];
			} else {
				Tone tone = active[0] ? Tone.TITLE : active[1] ? Tone.RED
						: active[2] ? Tone.ORANGE : active[3] ? Tone.GREEN : Tone.NORMAL;
				result.add(new Part(token, tone));
			}
		}
		return result;
	}

	private static void splitMarkers(String token, List<String> result) {
		if (token == null || token.isEmpty()) return;
		int start = 0;
		for (int i = 0; i < token.length(); i++) {
			if (markerIndex(token.charAt(i)) >= 0) {
				if (start < i) result.add(token.substring(start, i));
				result.add(token.substring(i, i + 1));
				start = i + 1;
			}
		}
		if (start < token.length()) result.add(token.substring(start));
	}

	private static int markerIndex(String token) {
		return token.length() == 1 ? markerIndex(token.charAt(0)) : -1;
	}

	private static int markerIndex(char marker) {
		switch (marker) {
			case '_': return 0;
			case '￥': return 1;
			case '￡': return 2;
			case '€': return 3;
			default: return -1;
		}
	}
}
