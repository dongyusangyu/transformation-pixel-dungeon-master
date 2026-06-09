/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Sweeping extends Weapon.Enchantment {

    private static ItemSprite.Glowing DEEPBLUE = new ItemSprite.Glowing( 0x00BFFF );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        int level = Math.max( 0, weapon.buffedLvl() );

        // lvl 0 - 20%
        // lvl 1 ~ 23%
        // lvl 2 ~ 26%
        //float procChance = (level+5f)/(level+25f) * procChanceMultiplier(attacker);
        float procChance = 1;
        ArrayList<Char> targets = new ArrayList<>();
        Char closest = null;
        for (Char ch : Actor.chars()) {
            if (ch.alignment == Char.Alignment.ENEMY//属于敌人（似乎不包括宝箱怪）
                    && !attacker.isCharmedBy(ch)        //未被敌人魅惑
                    && attacker.distance(ch)<=weapon.RCH*procChanceMultiplier(attacker)          //能够攻击到
                    && ch != defender
                    && ch !=attacker) {            //被攻击的敌人不会受到横扫
                targets.add(ch);
                if (closest == null || Dungeon.level.trueDistance(attacker.pos, closest.pos) > Dungeon.level.trueDistance(attacker.pos, ch.pos)) {
                    closest = ch;
                }
            }
        }

        for (Char ch : targets) {
            //横扫攻击
            int sweepingdmg = (int) Math.ceil(damage * (1f+level) / ( 10f+level));
            ch.damage(sweepingdmg, attacker);
        }

        return damage;

    }

    @Override
    public Glowing glowing() {
        return DEEPBLUE;
    }
}
