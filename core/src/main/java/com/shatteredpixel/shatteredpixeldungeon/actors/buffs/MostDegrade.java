package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class MostDegrade extends FlavourBuff {

    public static final float DURATION = 10f;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }
    @Override
    public boolean attachTo(Char target) {
        if (super.attachTo(target)){
            Item.updateQuickslot();
            if (target == Dungeon.hero) ((Hero) target).updateHT(false);
            return true;
        }
        return false;
    }
    @Override
    public void detach() {
        super.detach();
        if (target == Dungeon.hero) ((Hero) target).updateHT(false);
        Item.updateQuickslot();
    }

    @Override
    public int icon() {
        return BuffIndicator.DEGRADE;
    }

    @Override
    public float iconFadePercent() {
        return (DURATION - visualcooldown())/DURATION;
    }
}
