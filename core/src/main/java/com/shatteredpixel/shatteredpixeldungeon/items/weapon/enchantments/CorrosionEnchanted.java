package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;



import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class CorrosionEnchanted extends Weapon.Enchantment {

    private static ItemSprite.Glowing AIDORANGE = new ItemSprite.Glowing( 0xFFAA33 );

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage ) {
        int level = Math.max( 0, weapon.buffedLvl() );

        // lvl 0 - 33%
        // lvl 1 - 50%
        // lvl 2 - 60%
        float procChance = (level+1f)/(level+3f) * procChanceMultiplier(attacker);
        if (Random.Float() < procChance) {

            float powerMulti = Math.max(1f, procChance);

            if (defender.buff(Corrosion.class) == null){
                Buff.affect(defender, Corrosion.class).set(3f, (int)((1+level/2)*powerMulti), null);
            }
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return AIDORANGE;
    }
}

