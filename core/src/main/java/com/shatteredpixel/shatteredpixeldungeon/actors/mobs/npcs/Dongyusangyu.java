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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DongyusangyuSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTreasureHuntMenu;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

public class Dongyusangyu extends NPC {

	{
		spriteClass = DongyusangyuSprite.class;
		properties.add(Property.IMMOVABLE);
	}

	@Override
	public int defenseSkill(Char enemy) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage(int dmg, Object src, DamageTag... damageTags) {
		// Dongyusangyu cannot be damaged.
	}

	@Override
	public boolean add(Buff buff) {
		return false;
	}

	@Override
	public boolean interact(Char c) {
		if (sprite != null) {
			sprite.turnTo(pos, c.pos);
		}
		if (c == Dungeon.hero) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndTreasureHuntMenu());
				}
			});
		}
		return true;
	}
}
