package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class GoldIngot extends Trinket{

    {
        image = ItemSpriteSheet.GOLD_INGOT;
    }

    @Override
    protected int upgradeEnergyCost() {
        //6 -> 20(26) -> 25(51) -> 30(81)
        return 10+5*level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc",
                    Messages.decimalFormat("#.##", (buffedLvl() * 7.5)+7.5),
                    Messages.decimalFormat("#.##", (buffedLvl() * 7.5)+7.5));
        } else {
            return Messages.get(this, "typical_stats_desc",
                    Messages.decimalFormat("#.##", 7.5),
                    Messages.decimalFormat("#.##", 7.5));
        }
    }
}
