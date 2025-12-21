package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class WalkStick extends MeleeWeapon{
    {
        image = ItemSpriteSheet.WALK_STICK;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 1.1f;

        tier = 1;

        bones = false;
    }
    @Override
    public int max(int lvl) {
        return  3*(tier+1) + 1 +  //7 base, down from 10
                lvl*(tier+1);   //scaling unchanged
    }
    @Override
    public int proc(Char attacker, Char defender, int damage ) {
        if(Random.Int(2)==1){
            Buff.affect(attacker, Barrier.class).incShield(1);
        }

        return super.proc(attacker, defender, damage );
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        beforeAbilityUsed(hero, null);
        //1 turn less as using the ability is instant
        Buff.prolong(hero, Quarterstaff.DefensiveStance.class, 4 + buffedLvl());
        hero.sprite.operate(hero.pos);
        hero.next();
        afterAbilityUsed(hero);
    }

    @Override
    public String abilityInfo() {
        if (levelKnown){
            return Messages.get(this, "ability_desc", 5+buffedLvl());
        } else {
            return Messages.get(this, "typical_ability_desc", 5);
        }
    }

    @Override
    public String upgradeAbilityStat(int level) {
        return Integer.toString(+5+level);
    }

    public static class DefensiveStance extends FlavourBuff {

        {
            announced = true;
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.DUEL_EVASIVE;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (4 - visualcooldown()) / 4);
        }
    }

}
