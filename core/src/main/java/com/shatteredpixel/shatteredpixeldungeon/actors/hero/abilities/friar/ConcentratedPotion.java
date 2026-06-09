package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.friar;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SprayGun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class ConcentratedPotion extends ArmorAbility {
    {
        baseChargeUse = 35f;
    }

    @Override
    public int icon() {
        return HeroIcon.CONPOTION;
    }

    @Override
    public float chargeUse(Hero hero) {
        float chargeUse = super.chargeUse(hero);
        SprayGun sprayGun = hero == null ? null : hero.belongings.getItem(SprayGun.class);
        if (sprayGun != null && sprayGun.charges() > 6 && hero.hasTalent(Talent.REUSE_REAGENT)) {
            switch (hero.pointsInTalent(Talent.REUSE_REAGENT)) {
                case 1: default:
                    chargeUse *= 0.84f;
                    break;
                case 2:
                    chargeUse *= 0.70f;
                    break;
                case 3:
                    chargeUse *= 0.60f;
                    break;
                case 4:
                    chargeUse *= 0.50f;
                    break;
            }
        }
        return chargeUse;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        SprayGun sprayGun = hero.belongings.getItem(SprayGun.class);
        if (sprayGun == null) {
            GLog.w(Messages.get(this, "no_spraygun"));
            return;
        }

        float chargeUse = chargeUse(hero);
        sprayGun.loadConcentratedPotion(hero);
        armor.charge -= chargeUse;
        Talent.onArmorAbility(hero, chargeUse);
        armor.updateQuickslot();

        hero.spendAndNext(1f);
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.DRINK);
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.CONCENTRATED_ESSENCE, Talent.REAGENT_ENHANCEMENT, Talent.REUSE_REAGENT, Talent.HEROIC_ENERGY};
    }
}
