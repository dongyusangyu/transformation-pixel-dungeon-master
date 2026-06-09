package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.princess;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RuneMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TabooMagic extends ArmorAbility {
    {
        baseChargeUse = 35f;
    }
    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }
    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        if (target == null){
            return;
        }
        Char ch1 = Actor.findChar(target);
        if (ch1 == null || !Dungeon.level.heroFOV[target] || ch1.alignment==Char.Alignment.ALLY){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }
        ArrayList<Char> affectedChars = new ArrayList<>();
        affectedChars.add(ch1);
        if(hero.pointsInTalent(Talent.SPREAD_PAIN)>0 && armor.charge>=100f){
            int dst = hero.pointsInTalent(Talent.SPREAD_PAIN);
            boolean[] explodable = new boolean[Dungeon.level.length()];
            BArray.not( Dungeon.level.solid, explodable);
            BArray.or( Dungeon.level.flamable, explodable, explodable);
            PathFinder.buildDistanceMap( target, explodable, dst);
            for (int i = 0; i < PathFinder.distance.length; i++) {
                if (PathFinder.distance[i] != Integer.MAX_VALUE) {
                   Char ch = Actor.findChar(i);
                    if (ch != null && ch.alignment != Char.Alignment.ALLY && ch!=hero && !affectedChars.contains(ch)) {
                        affectedChars.add(ch);
                    }
                }
            }
        }
        for (Char ch : affectedChars){
            if(ch.buff(RuneMark.class)!=null){
                ch.buff(RuneMark.class).explore(null);
            }
            Buff.affect(ch, Weakness.class, 10);
            Buff.affect(ch, Vulnerable.class, 10);
            Buff.affect(ch, Hex.class, 10);
            if(hero.pointsInTalent(Talent.ELEMENT_CURSE)>0){
                switch (Random.Int(2)){
                    case 0:
                        Buff.affect(ch, Burning.class).reignite(ch, 4*(((1+hero.pointsInTalent(Talent.ELEMENT_CURSE))/2)));
                        break;
                    case 1:
                        Buff.affect(ch, Chill.class,8*(((1+hero.pointsInTalent(Talent.ELEMENT_CURSE))/2)));
                        break;
                }
            }
            if(hero.pointsInTalent(Talent.ELEMENT_CURSE)>1){
                Buff.affect(ch, Roots.class,5*(hero.pointsInTalent(Talent.ELEMENT_CURSE)/2));
            }
            if(hero.pointsInTalent(Talent.AFFLICTED_ILLNESS)>0){
                Buff.affect(ch, Poison.class).set(4*(1+hero.pointsInTalent(Talent.AFFLICTED_ILLNESS))/2);
                Buff.affect(ch, Bleeding.class).set(4*((1+hero.pointsInTalent(Talent.AFFLICTED_ILLNESS))/2));
            }
            if(hero.pointsInTalent(Talent.AFFLICTED_ILLNESS)>1){
                Buff.affect(ch, Cripple.class,5*(hero.pointsInTalent(Talent.AFFLICTED_ILLNESS)/2));
            }
        }
        MagicMissile.boltFromChar( hero.sprite.parent,
                MagicMissile.SHADOW,
                hero.sprite,
                target,
                new Callback() {
                    @Override
                    public void call() {
                        hero.sprite.operate(target);
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
        switch (Random.Int(4)){
            case 0: default:
                GLog.w(Messages.get(this, "say"));
                break;
            case 1:
                GLog.w(Messages.get(this, "say1"));
                break;
            case 2:
                GLog.w(Messages.get(this, "say2"));
                break;
            case 3:
                GLog.w(Messages.get(this, "say3"));
                break;
        }

        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        armor.updateQuickslot();
        Invisibility.dispel();
        hero.spendAndNext(Actor.TICK);
    }
    @Override
    public int icon() {
        return HeroIcon.TABOOMAGIC;
    }
    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.ELEMENT_CURSE, Talent.AFFLICTED_ILLNESS, Talent.SPREAD_PAIN, Talent.HEROIC_ENERGY};
    }
}
