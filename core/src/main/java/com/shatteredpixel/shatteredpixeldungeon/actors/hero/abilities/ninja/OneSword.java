package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class OneSword extends ArmorAbility {

    {
        baseChargeUse = 50f;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {

        Buff.prolong(hero, OKU_OneSword.class, OKU_OneSword.DURATION);
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
        return HeroIcon.ONESWORD;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.OFFENSIVE, Talent.GLIMPSE, Talent.KILL_CONTINUE, Talent.HEROIC_ENERGY};
    }

    public static class OKU_OneSword extends FlavourBuff {

        {
            type = buffType.POSITIVE;
        }

        public static final float DURATION = 10f;

        @Override
        public int icon() {
            return BuffIndicator.ONESWORD;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

    }
    public static class Kill extends Buff {
        @Override
        public int icon() {
            return BuffIndicator.NONE;
        }
        @Override
        public void detach() {

            super.detach();
        }
    }
}