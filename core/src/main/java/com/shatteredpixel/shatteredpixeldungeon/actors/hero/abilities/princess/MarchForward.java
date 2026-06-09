package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.princess;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;

import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class MarchForward extends ArmorAbility {
    {
        baseChargeUse = 50f;
    }
    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {

        Buff.affect(hero, Forward.class).set(1+(hero.pointsInTalent(Talent.DELAY_TACTIC)+1)/2);
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.HEALTH_WARN);
        hero.sprite.emitter().burst(BloodParticle.BURST, 10);
        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        armor.updateQuickslot();
        Invisibility.dispel();
        hero.spendAndNext(Actor.TICK);
    }
    @Override
    public int icon() {
        return HeroIcon.MARCHFORWARD;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.SPEEDUP, Talent.RUN_ATTACK, Talent.DELAY_TACTIC, Talent.HEROIC_ENERGY};
    }
    public static class Forward extends Buff {
        {
            type = buffType.POSITIVE;
        }
        @Override
        public int icon() {
            return BuffIndicator.FORWARD;
        }
        public void set( int duration ) {
            this.speed=0.5f+0.125f*hero.pointsInTalent(Talent.SPEEDUP);
            this.left = duration;
        }
        public int left=1;
        public float speed=1.5f;
        private static final String LEFT	= "left";
        private static final String SPEED	= "speed";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( LEFT, left );
            bundle.put( SPEED, speed );
        }
        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            left = bundle.getInt( LEFT );
            speed = bundle.getFloat( SPEED );
        }

        @Override
        public String desc() {
            float limSpeed=1.0f+0.125f*hero.pointsInTalent(Talent.SPEEDUP);
            return Messages.get(this, "desc", Messages.decimalFormat("#.##", 100f * speed),
                    Messages.decimalFormat("#.##", 100f * limSpeed),left);
        }
    }
}
