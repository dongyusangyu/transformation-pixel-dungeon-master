/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Gizmo;

public class AlignmentColorBlock extends Gizmo {

	private final CharSprite target;
	private final float r;
	private final float g;
	private final float b;

	public AlignmentColorBlock(CharSprite target, float r, float g, float b) {
		super();

		this.target = target;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	@Override
	public void update() {
		super.update();

		target.tint(r, g, b, 0.55f);
	}

	public void clear() {
		target.resetColor();
		killAndErase();
	}

	public static AlignmentColorBlock color(CharSprite sprite, float r, float g, float b) {
		AlignmentColorBlock colorBlock = new AlignmentColorBlock(sprite, r, g, b);
		if (sprite.parent != null) {
			sprite.parent.add(colorBlock);
		}
		return colorBlock;
	}
}
