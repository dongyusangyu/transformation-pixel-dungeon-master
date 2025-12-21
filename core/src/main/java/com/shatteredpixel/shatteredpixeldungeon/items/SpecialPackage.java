package com.shatteredpixel.shatteredpixeldungeon.items;

import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genHighValueConsumable;
import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genLowValueConsumable;
import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genMidValueConsumable;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class SpecialPackage extends Item {
    {
        image = ItemSpriteSheet.SPECIAL_PACKAGE;
        //image = ItemSpriteSheet.GOLD;

        stackable = true;
        unique = true;

        defaultAction = AC_USE;

        bones = false;
    }
    private static final String AC_USE = "USE";
    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_USE );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals(AC_USE)) {

            /*
            switch (Random.Int(4)){
                case 0 : default:
                    item=new BrokenSeal();
                    break;
                case 1:
                    item=new MagesStaff(new WandOfMagicMissile());
                    break;
                case 2:
                    item=new CloakOfShadows();
                    break;
                case 3:
                    item=new SpiritBow();
                    break;
            }

             */
            GameScene.show(new WndOptions(new ItemSprite(this),
                    Messages.get(this, "prompt"),
                    Messages.get(this, "pack"),
                    Messages.get(new BrokenSeal(), "name"),
                    Messages.get(new MagesStaff(new WandOfMagicMissile()), "name"),
                    Messages.get(new CloakOfShadows(), "name"),
                    Messages.get(new SpiritBow(), "name"),
                    Messages.get(new HolyTome(), "name")){

                @Override
                protected void onSelect(int index) {
                    Item item=null;
                    if (index == 0){
                        item=new BrokenSeal();
                    } else if(index == 1){
                        item=new MagesStaff(new WandOfMagicMissile());
                    }else if(index == 2){
                        item=new CloakOfShadows();
                    }else if(index == 3){
                        item=new SpiritBow();
                    }else if(index == 4){
                        item=new HolyTome();
                    }
                    if(item!=null){
                        item.identify();
                        Dungeon.level.drop(item,hero.pos).sprite.drop(hero.pos);
                        curUser = hero;
                        curItem.detach(curUser.belongings.backpack);
                    }


                    updateQuickslot();
                }


                @Override
                public void onBackPressed() {
                    //do nothing
                }
            });

        }
    }
}
