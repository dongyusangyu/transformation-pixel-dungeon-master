package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class RuneString extends MissileWeapon {
    {
        image = ItemSpriteSheet.RUNESTRING;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1f;

        bones = false;

        tier = 1;
        baseUses = 1000;
        sticky = false;
        //all darts share a set ID
        setID = 1L;

    }
    public static final String AC_ENCHANT = "ENCHANT";
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_ENCHANT );
        return actions;
    }

    @Override
    public int value() {
        return Math.round(super.value()/2f); //half normal value
    }
    @Override
    public boolean isUpgradable() {
        return false;
    }
    @Override
    public int min(int lvl) {
        return  2+lvl;
    }

    @Override
    public int max(int lvl) {
        return  4 * tier +                      //base
                (tier+1)*lvl;                       //level scaling
    }
    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_ENCHANT)){
            GameScene.selectItem(itemSelector);
        }
    }
    private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(RuneString.class, "inv_title");
        }

        @Override
        public boolean itemSelectable(Item item) {
            return ScrollOfEnchantment.enchantable(item);
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return null;
        }

        @Override
        public void onSelect( Item item) {
            if (item != null) {
                curItem.detach(curUser.belongings.backpack);

                RuneString1 r = new RuneString1();
                r.quantity(1).identify();
                if (!r.collect()){
                    Dungeon.level.drop(r, hero.pos).sprite.drop();
                }
                Catalog.countUse(getClass());
                if (item instanceof Weapon) {

                    if(hero.hasTalent(Talent.STR_RUNE)){
                        final Weapon.Enchantment enchants[] = new Weapon.Enchantment[3];
                        Class<? extends Weapon.Enchantment> existing = ((Weapon) item).enchantment != null ? ((Weapon) item).enchantment.getClass() : null;
                        enchants[1] = Weapon.Enchantment.randomCommon( existing );
                        enchants[2] = Weapon.Enchantment.randomUncommon( existing );
                        enchants[0] = Weapon.Enchantment.random( existing, enchants[1].getClass(), enchants[2].getClass());
                        GameScene.show(new StoneOfEnchantment.WndEnchantSelect((Weapon) item, enchants[0], enchants[1], enchants[2]));
                    } else {
                        ((Weapon)item).enchant();
                        curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                        GLog.p(Messages.get(StoneOfEnchantment.class, "weapon"));
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
                        GameScene.show(new StoneOfEnchantment.WndGlyphSelect((Armor) item, glyphs[0], glyphs[1], glyphs[2]));
                    } else {
                        ((Armor)item).inscribe();
                        curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
                        Enchanting.show( curUser, item );
                        GLog.p(Messages.get(StoneOfEnchantment.class, "armor"));
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
                curUser.spend( 1f );
                curUser.busy();
                curUser.sprite.operate(curUser.pos);
                Sample.INSTANCE.play( Assets.Sounds.READ );
                Invisibility.dispel();
            }


        }
    };
    @Override
    public int defaultQuantity() {
        return 1;
    }

    public static  class RuneString1 extends MissileWeapon {
        {
            image = ItemSpriteSheet.RUNESTRING1;
            hitSound = Assets.Sounds.HIT;
            hitSoundPitch = 1f;

            bones = false;

            tier = 1;
            baseUses = 15;
            sticky = false;
        }
        @Override
        public int min(int lvl) {
            return  2+lvl;
        }

        @Override
        public int max(int lvl) {
            return  4 * tier +                      //base
                    (tier+1)*lvl;                       //level scaling
        }
    }

}
