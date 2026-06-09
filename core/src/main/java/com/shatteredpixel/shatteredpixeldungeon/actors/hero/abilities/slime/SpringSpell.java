package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.slime;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;


public class SpringSpell extends ArmorAbility {

    {
        baseChargeUse = 50f;
    }


    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        int heal=25 + 5*hero.pointsInTalent(Talent.POTENT_HEALING);
        if(hero.hasTalent(Talent.ORIGINAL_MONSTER)){
            heal+=heal/2;
        }
        Buff.affect(hero, Sungrass.Health.class).boost(heal);
        Char finalch = null;
        int finalchHP = 1000000000;
        float distance = 0;
        switch (hero.pointsInTalent((Talent.MASS_HEALING))){
            case 0:
                distance = 0;
                break;
            case 1:
                distance = 1.5f;
                break;
            case 2:
                distance = 2.5f;
                break;
            case 3:
                distance = 2.9f;
                break;
            case 4:
                distance = 3.2f;
                break;
        }

        for(Char ch: Actor.chars()){
            if(ch.alignment == Char.Alignment.ALLY && ch.HP < finalchHP && Dungeon.level.trueDistance(hero.pos, ch.pos) < distance && ch != hero){
                finalch = ch;
                finalchHP = ch.HP;
            }
        }

        if (finalch != null){

            Buff.affect(finalch, Sungrass.Health.class).boost(heal);
            hero.sprite.parent.add(new Beam.HealthRay(hero.sprite.destinationCenter(),finalch.sprite.center()));
        }

        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        hero.spendAndNext(Actor.TICK);
        armor.updateQuickslot();


    }

    @Override
    public int icon() {
        return HeroIcon.SPRING_SPELL;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.POTENT_HEALING, Talent.EFFICIENT_HEALING, Talent.MASS_HEALING, Talent.HEROIC_ENERGY};
    }


}
