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

package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GreatImp;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public abstract class Runestone extends Item {
	
	{
		stackable = true;
		defaultAction = AC_THROW;
	}
    public static final String AC_TRANS	= "TRANS";

	//anonymous stones don't count as consumed, do not drop, etc.
	//useful for stones which are only spawned for their effects
	protected boolean anonymous = false;
	public void anonymize(){
		image = ItemSpriteSheet.STONE_HOLDER;
		anonymous = true;
	}
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions( hero );
        if(hero.heroClass == HeroClass.PRINCESS && !(this instanceof StoneOfEnchantment)){
            actions.add( AC_TRANS );
        }
        return actions;
    }
    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_TRANS) && hero.buff(MagicImmune.class) == null){
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndOptions(new ItemSprite(Runestone.this),
							Messages.titleCase(Runestone.this.name()),
							Messages.get(Runestone.this, "transform_warm"),
							Messages.get(Runestone.this, "yes"),
							Messages.get(Runestone.this, "no") ) {
						@Override
						protected void onSelect( int index ) {
							int cost = 8;
							if(hero.hasTalent(Talent.RUNE_EXPERT)){
								cost -= 2*hero.pointsInTalent(Talent.RUNE_EXPERT);
							}
							switch (index) {
								case 0:
									if(Dungeon.energy>=cost){
										curItem.detach(curUser.belongings.backpack);

										Dungeon.energy-=cost;
										Dungeon.hero.spendAndNext(1f);
										Dungeon.hero.sprite.operate(Dungeon.hero.pos);
										Item result = new StoneOfEnchantment();
										if (!result.collect()) {
											Dungeon.level.drop(result, curUser.pos).sprite.drop();
										}
										Sample.INSTANCE.play( Assets.Sounds.READ );
										curUser.sprite.showStatus(CharSprite.POSITIVE, Messages.get(Runestone.this, "ac_trans"));
										GLog.p(Messages.get(Runestone.this, "transform"));
										curUser.sprite.emitter().start( Speck.factory( Speck.TRANSLIGHT ), 0.1f, 5 );
									}else{
										GLog.w(Messages.get(Runestone.this, "not_enough_energy"));
									}
								case 1:
									//do nothing
									break;
							}
						}
						public void onBackPressed() {}
					} );
				}
			});

        }
    }


	@Override
	protected void onThrow(int cell) {
		///inventory stones are thrown like normal items, other stones don't trigger when thrown into pits
		if (this instanceof InventoryStone ||
				Dungeon.hero.buff(MagicImmune.class) != null ||
				(Dungeon.level.pit[cell] && Actor.findChar(cell) == null)){
			if (!anonymous) super.onThrow( cell );
		} else {
			if (!anonymous) {
				Catalog.countUse(getClass());
				Talent.onRunestoneUsed(curUser, cell, getClass());
			}
			activate(cell);
			if (Actor.findChar(cell) == null) Dungeon.level.pressCell( cell );
			Invisibility.dispel();
		}
	}

	protected abstract void activate(int cell);
	
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
		return 15 * quantity;
	}

	@Override
	public int energyVal() {
		return 3 * quantity;
	}

	public static class PlaceHolder extends Runestone {
		
		{
			image = ItemSpriteSheet.STONE_HOLDER;
		}
		
		@Override
		protected void activate(int cell) {
			//does nothing
		}
		
		@Override
		public boolean isSimilar(Item item) {
			return item instanceof Runestone;
		}
		
		@Override
		public String info() {
			return "";
		}
	}
}
