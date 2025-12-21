package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.dm400;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja.OneSword;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class Routine extends ArmorAbility {
    {
        baseChargeUse = 50f;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {

        Buff.prolong(hero, OverLoad.class, 13+(int)(10/4*hero.pointsInTalent(Talent.OVER_EXTEND)));
        Buff.prolong(hero, preOverLoad.class, 3);
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
        hero.sprite.emitter().burst(BloodParticle.BURST, 10);
        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        armor.updateQuickslot();
        Invisibility.dispel();
        hero.spendAndNext(4f);
        if(hero.hasTalent(Talent.STRONG_PERSERVE)){
            Buff.affect(hero, Barrier.class).setShield((int)(hero.HT*(0.05+0.025*hero.pointsInTalent(Talent.STRONG_PERSERVE))));
            ArrayList<InstructionTool.Drone> drones = InstructionTool.getDroneAlly();
            if(drones!=null){
                for(InstructionTool.Drone d:drones){
                    Buff.affect(d, Barrier.class).setShield((int)(d.HT*(0.05+0.025*hero.pointsInTalent(Talent.STRONG_PERSERVE))));
                }
            }

        }
    }

    @Override
    public int icon() {
        return HeroIcon.ROUTINE;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.OVER_EXTEND,Talent.TERROR_MACH,Talent.STRONG_PERSERVE, Talent.HEROIC_ENERGY};
    }

    public static class OverLoad extends FlavourBuff {

        {
            type = buffType.POSITIVE;
        }

        public static final float DURATION = 10f;

        @Override
        public int icon() {
            return BuffIndicator.OVERLOAD;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

    }
    public static class preOverLoad extends FlavourBuff {

        {
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.NONE;
        }

    }

    public static class TerrorCd extends Buff {
        @Override
        public int icon() {
            return BuffIndicator.NONE;
        }

    }

}
