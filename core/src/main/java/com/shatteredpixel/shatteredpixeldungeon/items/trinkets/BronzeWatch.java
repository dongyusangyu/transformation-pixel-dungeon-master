package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
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
            return Messages.get(this, "stats_desc", watchGainMultiplier(buffedLvl()));
        } else {
            return Messages.get(this, "typical_stats_desc", 2);
        }
    }

    public static float watchMultiplier(){
        return watchGainMultiplier(trinketLevel(BronzeWatch.class));
    }

    public static float adjustDuration( Char target, float duration ){
        if (target instanceof Hero && duration > 1 && ((Hero)target).belongings.getItem(BronzeWatch.class) != null){
            return Math.max(1, duration - watchMultiplier());
        } else {
            return duration;
        }
    }

    public static int adjustDuration( Char target, int duration ){
        return Math.round(adjustDuration(target, (float)duration));
    }

    public static int watchGainMultiplier( int level ){
        if (level < 0){
            return 2;
        } else {
            return 2*(level+1);
        }
    }

}
