package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Bracer extends MeleeWeapon {

    {
        image = ItemSpriteSheet.BRACER;

        tier = 1;
        bones = false;
    }

    @Override
    public int max(int lvl) {
        return  Math.round(3f*(tier+1)) +   //18 base, down from 20
                lvl*(tier);               //+3 per level, down from +6
    }

    @Override
    public int defenseFactor( Char owner ) {
        return DRMax();
    }

    public int DRMax(){
        return DRMax(buffedLvl());
    }

    //2 extra defence, plus 1 per level
    public int DRMax(int lvl){
        return 2 + lvl;
    }

    public String statsInfo(){
        if (isIdentified()){
            return Messages.get(this, "stats_desc", 2+1*buffedLvl());
        } else {
            return Messages.get(this, "typical_stats_desc", 2);
        }
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        RoundShield.guardAbility(hero, 7+buffedLvl(), this);
    }

    @Override
    public String abilityInfo() {
        if (levelKnown){
            return Messages.get(this, "ability_desc", 7+buffedLvl());
        } else {
            return Messages.get(this, "typical_ability_desc", 7);
        }
    }

    @Override
    public String upgradeAbilityStat(int level) {
        return Integer.toString(7 + level);
    }
}
