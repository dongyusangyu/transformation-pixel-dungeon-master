package com.shatteredpixel.shatteredpixeldungeon.desktop;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class DesktopTextSplitTest {

	@Test
	public void fullwidthHighlightMarkersAreSeparateTokensInBothModes() {
		DesktopPlatformSupport desktop = new DesktopPlatformSupport();
		String text = "￡地牢原住民们变得更强了！(等级2)￡\n"
				+ "￥这些怪物只会不断变强，当心！(等级3)￥\n-普通敌人";
		for (boolean multiline : new boolean[]{false, true}) {
			String[] tokens = desktop.splitforTextBlock(text, multiline);
			assertTrue(Arrays.asList(tokens).contains("￡"));
			assertTrue(Arrays.asList(tokens).contains("￥"));
		}
	}
}
