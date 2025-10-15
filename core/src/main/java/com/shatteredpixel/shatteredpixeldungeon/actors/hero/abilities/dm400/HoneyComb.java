package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.dm400;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class HoneyComb extends ArmorAbility {
    {
        baseChargeUse = 90f;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {

        Buff.prolong(hero, HoneyCombBuff.class, 20);
        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        armor.updateQuickslot();
        Invisibility.dispel();
        hero.spendAndNext(1f);
    }

    @Override
    public int icon() {
        return HeroIcon.HONEYCOMB;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.PROCESS_EXTEND,Talent.GLORIOUS_DEAD,Talent.UPON_WAVE, Talent.HEROIC_ENERGY};
    }

    public static class HoneyCombBuff extends FlavourBuff {

        {
            type = buffType.POSITIVE;
        }

        public static final float DURATION = 20f;

        @Override
        public int icon() {
            return BuffIndicator.HONEYCOMB;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

    }
}
