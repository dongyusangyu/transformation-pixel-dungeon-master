package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
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

public class Suffering extends FlavourBuff {
    {

        revivePersists = false;
        announced = false;
    }

    public static final float DURATION	= 300f;
    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - visualcooldown()) / DURATION);
    }
    @Override
    public int icon() {
        return BuffIndicator.SUFFERING;
    }
    @Override
    public boolean attachTo( Char target ) {
        GLog.n(Messages.get(Suffering.class,"desc",this.name()));
        return super.attachTo(target);
    }

    public static void rollGiveSuffer(Char m, float Duration){
        Reason r = m.buff(Reason.class);
        if(r!=null){
            r.reason=100;
            ActionIndicator.refresh();
            if(r.target.sprite!=null){
                new Flare( 5, 32 ).color( 0xFF0000, true ).show( m.sprite, 2f );
                SpellSprite.show( m, SpellSprite.SUFFER );
            }
        }
        Class<?extends Suffering> buffCls;
        if (m instanceof Hero && ((Hero)m).subClass.is(HeroSubClass.PIOUS)){
            buffCls = Suffering.Ecstasy.class;
        } else switch (Random.Int(3)){
            case 0: default:    buffCls = Suffering.Fear.class;      break;
            case 1:             buffCls = Suffering.Despair.class;   break;
            case 2:             buffCls = Suffering.Paranoia.class;    break;
        }
        Buff.affect(m, buffCls,Duration);
        Sample.INSTANCE.play( Assets.Sounds.SUFFER, 1, 1, Random.Float( 0.9f, 1.1f ) );
    }

    public static class Fear extends Suffering {


    }

    public static class Despair extends Suffering {

    }

    public static class Paranoia extends Suffering {


    }

    public static class Ecstasy extends Suffering {

    }
}
