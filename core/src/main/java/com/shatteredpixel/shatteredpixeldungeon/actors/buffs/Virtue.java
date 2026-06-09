package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Virtue extends FlavourBuff {
    {
        type = buffType.POSITIVE;
        announced = false;
    }

    public static final float DURATION	= 150f;
    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - visualcooldown()) / DURATION);
    }
    @Override
    public int icon() {
        return BuffIndicator.VIRTUE;
    }
    public static void rollGiveVirtue(Char m,float Duration){
        Reason r = m.buff(Reason.class);
        Class<?extends Virtue> buffCls;
        switch (Random.Int(3)){
            case 0: default:    buffCls = Virtue.Firm.class;      break;
            case 1:             buffCls = Virtue.Inspire.class;   break;
            case 2:             buffCls = Virtue.Fearless.class;    break;
        }
        Buff.affect(m, buffCls,Duration);
        if(r!=null){
            if(buffCls==Virtue.Inspire.class){
                r.reason=60;
            }else{
                r.reason=50;
            }
            if(buffCls==Virtue.Firm.class){
                Buff.affect(m,VirtueBarrier.class).incShield(m.HT/4);
            }
            if(buffCls==Virtue.Fearless.class && m.buff(Panic.class)!=null){
                m.buff(Panic.class).detach();
            }

            if(r.target.sprite!=null){
                new Flare( 5, 32 ).color( 0xFFFF00, true ).show( m.sprite, 2f );
                SpellSprite.show( m, SpellSprite.VIRTUE );
            }
        }
        ActionIndicator.refresh();
        Sample.INSTANCE.play( Assets.Sounds.VIRTUE, 1, 1, Random.Float( 0.9f, 1.1f ) );
    }
    @Override
    public boolean attachTo( Char target ) {
        GLog.p(Messages.get(Suffering.class,"desc",this.name()));
        return super.attachTo(target);
    }

    public static class Firm extends Virtue {
    }

    public static class Inspire extends Virtue {
    }

    public static class Fearless extends Virtue {
    }
    public static class VirtueBarrier extends ShieldBuff {
        @Override
        public void incShield(int amt) {
            amt=Math.min(target.HT/4-this.shielding(),amt);
            if (amt > 0 && target.sprite!=null) {
                target.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(amt), FloatingText.SHIELDING );
                super.incShield(amt);
            }

        }

    }


}
