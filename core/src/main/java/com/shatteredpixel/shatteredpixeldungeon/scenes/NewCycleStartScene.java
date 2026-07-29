/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;

public class NewCycleStartScene extends StartScene {

	@Override
	protected boolean showsNewCycleSaves() {
		return true;
	}

	@Override
	protected Class<? extends PixelScene> cycleSavesScene() {
		return StartScene.class;
	}

	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchNoFade(StartScene.class);
	}
}
