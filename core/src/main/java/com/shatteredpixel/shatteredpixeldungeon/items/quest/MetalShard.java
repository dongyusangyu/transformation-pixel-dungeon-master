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

package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class MetalShard extends Item {
	
	{
		image = ItemSpriteSheet.SHARD;
		stackable = true;
	}
	public static final String AC_EAT	= "eat";
	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if(hero.heroClass == HeroClass.SLIMEGIRL){
			actions.add( AC_EAT );
		}
		return actions;
	}
	@Override
	public void execute( Hero hero, String action ) {

		super.execute (hero, action );

		if (action.equals( AC_EAT )) {

			detach( hero.belongings.backpack );
			if(hero.belongings.armor !=null){
				Armor a = (Armor) hero.belongings.armor;
				if (a.glyph != null){
					//if we are freshly applying curse infusion, don't replace an existing curse
					if (a.hasGoodGlyph() || a.curseInfusionBonus) {
						a.inscribe(Armor.Glyph.randomCurse(a.glyph.getClass()));
					}
				} else {
					a.inscribe(Armor.Glyph.randomCurse());
				}
				a.curseInfusionBonus = true;
				a.cursed = true;
			}
			hero.sprite.operate( hero.pos );
			hero.busy();
			SpellSprite.show( hero, SpellSprite.FOOD );
			Sample.INSTANCE.play( Assets.Sounds.EAT );
			GLog.i(Messages.get(this,"eat"));
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int value() {
		return quantity * 50;
	}

	@Override
	public int energyVal() {
		return quantity * 3;
	}
}
