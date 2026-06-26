package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.friar;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Panic;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class ReasonDose  extends ArmorAbility {
    {
        baseChargeUse = 50f;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        float chargeUse = chargeUse(hero);

        Reason.gainReason(hero, 50);
        Buff.detach(hero, Panic.class);

        if (hero.hasTalent(Talent.HEART_DOSE)) {
            int heal = Math.round(hero.HT * (5 + 5 * hero.pointsInTalent(Talent.HEART_DOSE)) / 100f);
            if (heal > 0) {
                hero.heal(heal);
            }
        }

        if (hero.hasTalent(Talent.SHIELD_DOSE)) {
            Buff.affect(hero, Barrier.class).incShield(10 + 10 * hero.pointsInTalent(Talent.SHIELD_DOSE));
        }

        if (hero.hasTalent(Talent.CALM_DOSE)) {
            PotionOfCleansing.cleanse(hero, calmDoseDuration(hero)-1, false);
        }

        armor.charge -= chargeUse;
        Talent.onArmorAbility(hero, chargeUse);
        armor.updateQuickslot();

        GLog.p(Messages.get(this, "cast"));
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.DRINK);
    }

    @Override
    public int icon() {
        return HeroIcon.REASONDOSE;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.HEART_DOSE, Talent.SHIELD_DOSE, Talent.CALM_DOSE, Talent.HEROIC_ENERGY};
    }

    private float calmDoseDuration(Hero hero) {
        switch (hero.pointsInTalent(Talent.CALM_DOSE)) {
            case 1: default:
                return 3f;
            case 2:
                return 5f;
            case 3:
                return 8f;
            case 4:
                return 10f;
        }
    }
}
