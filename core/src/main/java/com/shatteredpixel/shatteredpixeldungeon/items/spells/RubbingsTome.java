package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Stylus;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndClericSpells;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndClericSpells1;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class RubbingsTome extends Item {
    {
        image = ItemSpriteSheet.RUBBINGSTOME;
        stackable = false;
        unique = true;
        bones = false;

        defaultAction = AC_CAST;
    }
    public int charge = 8;
    public static final String AC_CAST = "CAST";
    public boolean isIdentified() {
        return true;
    }
    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if ( !cursed
                && hero.buff(MagicImmune.class) == null) {
            actions.add(AC_CAST);
        }
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_CAST)) {

           if (cursed)       GLog.i( Messages.get(this, "cursed") );
            else {

                GameScene.show(new WndClericSpells1(this, hero, false));

            }

        }
    }

    //used to ensure tome has variable targeting logic for whatever spell is being case
    public ClericSpell targetingSpell = null;

    @Override
    public int targetingPos(Hero user, int dst) {
        if (targetingSpell == null || targetingSpell.targetingFlags() == -1) {
            return super.targetingPos(user, dst);
        } else {
            return new Ballistica( user.pos, dst, targetingSpell.targetingFlags() ).collisionPos;
        }
    }




    public boolean canCast( Hero hero, ClericSpell spell ){
        return hero.buff(MagicImmune.class) == null
                && charge >= spell.chargeUse(hero)
                && spell.canCast(hero);
    }
    public void spendCharge( float chargesSpent ){
        charge -= chargesSpent;
        if(charge<1){
            curItem.detach(curUser.belongings.backpack);
        }
        updateQuickslot();
    }
    @Override
    public String status() {
        return charge+"/"+8;

    }




    private static String Charge = "charge";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);

        bundle.put(Charge, charge);

    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        charge = bundle.getInt( Charge );
    }



    //lower values, as it's cheaper to make
    @Override
    public int value() {
        return 40 * quantity;
    }

    @Override
    public int energyVal() {
        return 0;
        //return Math.min(2 * charge * quantity,15);
    }

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

        @Override
        public boolean testIngredients(ArrayList<Item> ingredients) {
            boolean scroll1 = false;
            boolean scroll2 = false;
            boolean stylus = false;

            for (Item i : ingredients){
                if (i instanceof Stylus){
                    stylus = true;
                    //if it is a regular or exotic potion
                } else if (i instanceof Scroll && scroll1) {
                    scroll2 = true;
                }else if(i instanceof Scroll && !scroll1){
                    scroll1 = true;
                }
            }

            return scroll2 && stylus && scroll1;
        }

        @Override
        public int cost(ArrayList<Item> ingredients) {
            return 3;
        }

        @Override
        public Item brew(ArrayList<Item> ingredients) {
            if (!testIngredients(ingredients)) return null;

            for (Item ingredient : ingredients){
                if(ingredient instanceof Stylus){
                    continue;
                }
                ingredient.quantity(ingredient.quantity() - 1);
            }

            return sampleOutput(null);
        }

        @Override
        public Item sampleOutput(ArrayList<Item> ingredients) {
            return new RubbingsTome();
        }
    }
}
