package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class BronzeWatch extends Trinket {
    {
        image = ItemSpriteSheet.BRONZE_WATCH;
    }
    @Override
    protected int upgradeEnergyCost() {
        //6 -> 8(14) -> 10(24) -> 12(36)
        return 6+2*level();
    }
    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", (int)(watchMultiplier()));
        } else {
            return Messages.get(this, "typical_stats_desc", 2);
        }
    }

    public static float watchMultiplier(){
        return watchGainMultiplier(trinketLevel(BronzeWatch.class));
    }

    public static float watchGainMultiplier( int level ){
        if (level == -1){
            return 0;
        } else {
            return 2*(level+1);
        }
    }

}
