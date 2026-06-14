package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Panic;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class Tincture extends Food {
    {
        image = ItemSpriteSheet.TINCTURE;
        energy = Hunger.HUNGRY/3f; //100 food value
    }
    @Override
    protected void satisfy(Hero hero) {
        if(hero!=null && hero.heroClass== HeroClass.FRIAR){
            Reason.gainReason(hero,50);
            if(hero.buff(Panic.class)!=null) hero.buff(Panic.class).detach();
        }
        if(hero.hasTalent(Talent.VEGETARIANISM)){
            energy*=2f;
            Buff.affect(hero, WellFed.class).reset();
            if (hero.pointsInTalent(Talent.VEGETARIANISM)==2){
                Buff.affect(hero, Healing.class).setHeal((int)(hero.HT/4),0.25f,0);
            }
        }
        super.satisfy( hero );

        //GLog.i(Messages.get(this,"eat_msg"));
    }
    @Override
    protected float eatingTime(){

        if (Dungeon.hero.hasTalent(Talent.IRON_STOMACH)
                || Dungeon.hero.hasTalent(Talent.ENERGIZING_MEAL)
                || Dungeon.hero.hasTalent(Talent.MYSTICAL_MEAL)
                || Dungeon.hero.hasTalent(Talent.INVIGORATING_MEAL)
                || Dungeon.hero.hasTalent(Talent.FOCUSED_MEAL)
                || Dungeon.hero.hasTalent(Talent.ENLIGHTENING_MEAL)
                || Dungeon.hero.hasTalent(Talent.BLESS_MEAL)
                || Dungeon.hero.hasTalent(Talent.YUNYING_MEAL)
                || Dungeon.hero.hasTalent(Talent.ROYAL_MEAL)
                || Dungeon.hero.hasTalent(Talent.ICE_MEAL)
                || Dungeon.hero.hasTalent(Talent.OVER_MEAL)
                || Dungeon.hero.hasTalent(Talent.WHISPERING_MEAL)){
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int value() {
        return 5 * quantity;
    }
}
