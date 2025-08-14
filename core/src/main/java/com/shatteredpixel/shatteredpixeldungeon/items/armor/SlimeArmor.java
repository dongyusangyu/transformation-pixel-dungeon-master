package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class SlimeArmor extends Armor {

    {
        image = ItemSpriteSheet.ARMOR_SLIME;
        //image = ItemSpriteSheet.ARMOR_CLOTH;

        bones = false; //Finding them in bones would be semi-frequent and disappointing.
        unique = false;
    }

    public SlimeArmor() {
        super(1);
    }


    @Override
    public int level() {
        if(hero != null){
            return super.level() + Math.min((hero.lvl+3)/6,5);
        }else{
            return super.level();
        }

    }
    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public int STRReq(int lvl){
        /*
        int req = STRReq(tier, lvl);
        if (masteryPotionBonus){
            req -= 2;
        }
        return req -1;

         */
        if(hero !=null && hero.heroClass== HeroClass.SLIMEGIRL){
            return hero.STR;
        }else{
            int req = STRReq(tier, lvl);
            if (masteryPotionBonus){
                req -= 2;
            }
            return req;
        }
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        return false;
    }

    private static final String TIER = "tier";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(TIER, tier);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        tier = bundle.getInt(TIER);
    }
    @Override
    public int value() {
        return 0;
    }
}