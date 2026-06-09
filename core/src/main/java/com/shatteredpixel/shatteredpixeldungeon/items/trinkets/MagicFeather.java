package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
public class MagicFeather extends Trinket{

    {
        image = ItemSpriteSheet.MAGIC_FEATHER;
    }

    @Override
    protected int upgradeEnergyCost() {
        //6 -> 10(16) -> 15(31) -> 20(51)
        return 10+5*level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc",
                    Messages.decimalFormat("#.##", (buffedLvl() * 7.5)+7.5),
                    Messages.decimalFormat("#.##", (buffedLvl() * 15)+15));
        } else {
            return Messages.get(this, "typical_stats_desc",
                    Messages.decimalFormat("#.##", 7.5),
                    Messages.decimalFormat("#.##", 15));
        }
    }
}
