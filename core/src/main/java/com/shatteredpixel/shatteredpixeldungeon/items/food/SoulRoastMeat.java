package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EtherealBody;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class SoulRoastMeat extends Food {

    {
        image = EXItemSpriteSheet.SOUL_ROAST_MEAT;
        energy = 150f;
    }

    @Override
    protected void satisfy(Hero hero) {
        super.satisfy(hero);
        Buff.detach(hero, CursedBurning.class);
        Buff.affect(hero, EtherealBody.class, EtherealBody.DURATION);
        GLog.i(Messages.get(this, "ethereal"));
    }

    @Override public int value() { return 10 * quantity; }

    public static SoulRoastMeat cook(Item rawMeat, int count) {
        SoulRoastMeat result = new SoulRoastMeat();
        result.quantity(count);
        return result;
    }
}
