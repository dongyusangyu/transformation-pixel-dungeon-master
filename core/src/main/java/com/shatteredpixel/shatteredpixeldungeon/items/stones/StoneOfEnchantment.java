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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;

public class StoneOfEnchantment extends InventoryStone {
	
	{
		preferredBag = Belongings.Backpack.class;
		image = ItemSpriteSheet.STONE_ENCHANT;

		unique = true;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return ScrollOfEnchantment.enchantable(item);
	}
	
	@Override
	protected void onItemSelected(Item item) {
		if (!anonymous) {
			curItem.detach(curUser.belongings.backpack);
			Catalog.countUse(getClass());
			Talent.onRunestoneUsed(curUser, curUser.pos, getClass());
		}

		if (item instanceof Weapon) {
			if(hero.hasTalent(Talent.STR_RUNE)){
                final Weapon.Enchantment enchants[] = new Weapon.Enchantment[3];
                Class<? extends Weapon.Enchantment> existing = ((Weapon) item).enchantment != null ? ((Weapon) item).enchantment.getClass() : null;
                enchants[1] = Weapon.Enchantment.randomCommon( existing );
                enchants[2] = Weapon.Enchantment.randomUncommon( existing );
                enchants[0] = Weapon.Enchantment.random( existing, enchants[1].getClass(), enchants[2].getClass());

                GameScene.show(new WndEnchantSelect((Weapon) item, enchants[0], enchants[1], enchants[2]));
			} else {
				((Weapon)item).enchant();
                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                GLog.p(Messages.get(this, "weapon"));
                Enchanting.show( curUser, item );
			}
		}else if(item instanceof RingOfKing){
            ((RingOfKing)item).enchant(curItem);
        } else {
            if(hero.hasTalent(Talent.STR_RUNE)){
                final Armor.Glyph glyphs[] = new Armor.Glyph[3];
                Class<? extends Armor.Glyph> existing = ((Armor) item).glyph != null ? ((Armor) item).glyph.getClass() : null;
                glyphs[1] = Armor.Glyph.randomCommon( existing );
                glyphs[2] = Armor.Glyph.randomUncommon( existing );
                glyphs[0] = Armor.Glyph.random( existing, glyphs[1].getClass(), glyphs[2].getClass());
                GameScene.show(new WndGlyphSelect((Armor) item, glyphs[0], glyphs[1], glyphs[2]));
            } else {
                ((Armor)item).inscribe();
                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                Enchanting.show( curUser, item );
                GLog.p(Messages.get(this, "armor"));
            }
			
		}


		if(hero.pointsNegative(Talent.CURSEDMAN)>0){
			if (item instanceof Weapon) {
				Weapon w = (Weapon) item;
				w.enchant(Weapon.Enchantment.randomCurse());
			} else if (item instanceof Armor){
				Armor a = (Armor) item;
				a.inscribe(Armor.Glyph.randomCurse());
			}
			item.cursed=true;

		}
		
		useAnimation();
		
	}
	
	@Override
	public int value() {
		return 30 * quantity;
	}

	@Override
	public int energyVal() {
		return 5 * quantity;
	}

    public static class WndEnchantSelect extends WndOptions {

        private static Weapon wep;
        private static Weapon.Enchantment[] enchantments;

        //used in PixelScene.restoreWindows
        public WndEnchantSelect(){
            this(wep, enchantments[0], enchantments[1], enchantments[2]);
        }

        public WndEnchantSelect(Weapon wep, Weapon.Enchantment ench1,
                                Weapon.Enchantment ench2, Weapon.Enchantment ench3){
            super(new ItemSprite(new StoneOfEnchantment()),
                    Messages.titleCase(new StoneOfEnchantment().name()),
                    Messages.get(ScrollOfEnchantment.class, "weapon"),
                    ench1.name(),
                    ench2.name(),
                    ench3.name());
            this.wep = wep;
            enchantments = new Weapon.Enchantment[3];
            enchantments[0] = ench1;
            enchantments[1] = ench2;
            enchantments[2] = ench3;
        }

        @Override
        protected void onSelect(int index) {
            if (index < 3) {
                wep.enchant(enchantments[index]);
                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                Enchanting.show( curUser, wep );
                GLog.p(Messages.get(StoneOfEnchantment.class, "weapon"));
            }
        }
        @Override
        protected boolean enabled(int index) {
            if(hero.pointsInTalent(Talent.STR_RUNE)>2){
                return true;
            }else if(index==0){
                return true;
            }else if(hero.pointsInTalent(Talent.STR_RUNE)==index){
                return true;
            }else{
                return false;
            }
        }

        @Override
        protected boolean hasInfo(int index) {
            return index < 3;
        }

        @Override
        protected void onInfo( int index ) {
            GameScene.show(new WndTitledMessage(
                    Icons.get(Icons.INFO),
                    Messages.titleCase(enchantments[index].name()),
                    enchantments[index].desc()));
        }

        @Override
        public void onBackPressed() {
            //do nothing, reader has to cancel
        }

    }

    public static class WndGlyphSelect extends WndOptions {

        private static Armor arm;
        private static Armor.Glyph[] glyphs;

        //used in PixelScene.restoreWindows
        public WndGlyphSelect() {
            this(arm, glyphs[0], glyphs[1], glyphs[2]);
        }

        public WndGlyphSelect(Armor arm, Armor.Glyph glyph1,
                              Armor.Glyph glyph2, Armor.Glyph glyph3) {
            super(new ItemSprite(new StoneOfEnchantment()),
                    Messages.titleCase(new StoneOfEnchantment().name()),
                    Messages.get(ScrollOfEnchantment.class, "armor"),
                    glyph1.name(),
                    glyph2.name(),
                    glyph3.name());
            this.arm = arm;
            glyphs = new Armor.Glyph[3];
            glyphs[0] = glyph1;
            glyphs[1] = glyph2;
            glyphs[2] = glyph3;
        }

        @Override
        protected void onSelect(int index) {
            if (index < 3) {
                arm.inscribe(glyphs[index]);
                curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                Enchanting.show( curUser, arm );
                GLog.p(Messages.get(StoneOfEnchantment.class, "armor"));

            }
        }
        @Override
        protected boolean enabled(int index) {
            if(hero.pointsInTalent(Talent.STR_RUNE)>2){
                return true;
            }else if(index==0){
                return true;
            }else if(hero.pointsInTalent(Talent.STR_RUNE)==index){
                return true;
            }else{
                return false;
            }
        }

        @Override
        protected boolean hasInfo(int index) {
            return index < 3;
        }

        @Override
        protected void onInfo(int index) {
            GameScene.show(new WndTitledMessage(
                    Icons.get(Icons.INFO),
                    Messages.titleCase(glyphs[index].name()),
                    glyphs[index].desc()));
        }

        @Override
        public void onBackPressed() {
            //do nothing, reader has to cancel
        }

    }

}
