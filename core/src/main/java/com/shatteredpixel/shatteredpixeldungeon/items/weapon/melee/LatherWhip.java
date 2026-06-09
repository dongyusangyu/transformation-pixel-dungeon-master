package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class LatherWhip extends MeleeWeapon {
    {
        image = ItemSpriteSheet.LWHIP;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        bones = false;

        tier = 1;
        RCH = 3;    //lots of extra reach
    }

    @Override
    public int max(int lvl) {
        return  7*(tier) +      //7 base, down from 20
                lvl*(tier);     //+3 per level, down from +4
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        Whip.WhipAbility(hero, target, 1, 0, this);
    }

    @Override
    public String abilityInfo() {
        if (levelKnown){
            return Messages.get(this, "ability_desc", augment.damageFactor(min()), augment.damageFactor(max()));
        } else {
            return Messages.get(this, "typical_ability_desc", min(0), max(0));
        }
    }

    public String upgradeAbilityStat(int level){
        return augment.damageFactor(min(level)) + "-" + augment.damageFactor(max(level));
    }
}
