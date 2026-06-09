package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.princess;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class KingBlade extends ArmorAbility {
    {
        baseChargeUse = 50f;
    }
    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    public int icon() {
        return HeroIcon.KINGBLADE;
    }
    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        if (target == null){
            return;
        }
        Char ch1 = Actor.findChar(target);
        if (ch1 == null || !hero.canInteract(ch1) || ch1==hero ){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }
        if(Dungeon.level.adjacent(hero.pos, target)){
            if(hero.hasTalent(Talent.KING_GAZE)){
                Buff.affect(hero, MindVision.class,2*hero.pointsInTalent(Talent.KING_GAZE));

                Dungeon.observe();
            }
            armor.charge -= chargeUse(hero);
            hero.chooseEnemy(ch1);
            hero.sprite.attack(ch1.pos, new Callback() {
                @Override
                public void call() {

                    hero.attack(ch1, 1.5f+0.125f*hero.pointsInTalent(Talent.KING_POWER), 0, Char.INFINITE_ACCURACY);
                    for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                        if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]) {
                            if(hero.hasTalent(Talent.KING_GAZE)){
                                Buff.affect(mob, Disarm.class, 4f);
                            }
                            if(!ch1.isAlive()){
                                Buff.affect(mob, Disarm.class, 4f);
                            }
                        }
                    }
                    if(!ch1.isAlive()){
                        armor.charge+=5f*hero.pointsInTalent(Talent.KING_MIGHT);
                    }else{
                        Buff.affect(ch1, Disarm.class, Disarm.DURATION);

                    }
                    Invisibility.dispel();
                    hero.next();
                }
            });
            hero.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "say1"));
            hero.spend(Actor.TICK);
            Talent.onArmorAbility(hero, chargeUse(hero));
            armor.updateQuickslot();
        }
    }
    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.KING_GAZE, Talent.KING_POWER, Talent.KING_MIGHT, Talent.HEROIC_ENERGY};
    }

    public static class Disarm extends FlavourBuff {

        {
            type = buffType.NEGATIVE;
        }

        public static final float DURATION = 8f;
        @Override
        public boolean attachTo(Char target) {
            if(target.buff(this.getClass())==null && target.sprite != null){
                target.sprite.showStatus(CharSprite.ORANGE, name());
            }
            return super.attachTo(target);
        }

        @Override
        public int icon() {
            return BuffIndicator.DISARM;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

    }

}
