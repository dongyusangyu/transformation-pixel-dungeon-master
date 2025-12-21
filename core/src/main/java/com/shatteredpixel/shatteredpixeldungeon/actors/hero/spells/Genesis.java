package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class Genesis extends ClericSpell {
    public static Genesis INSTANCE = new Genesis();

    @Override
    public int icon() {
        return HeroIcon.GENESIS;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 8;
    }

    public String desc(){
        return Messages.get(this, "desc") +"\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.GENESIS);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {

        int  Tier = 2;
        if(hero.pointsInTalent(Talent.GENESIS)>1){
             Tier = 4;
        }
        for (int tier=0;tier<Tier;tier++){
            for(Talent talent: hero.talents.get(tier).keySet()){
                hero.talents.get(tier).put(talent,0);
            }
        }
        hero.spend( 1f );
        hero.busy();
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.READ);
        new Flare( 6, 32 ).color(0xFFFF00, true).show( hero.sprite, 2f );

        onSpellCast(tome, hero);

    }
}
