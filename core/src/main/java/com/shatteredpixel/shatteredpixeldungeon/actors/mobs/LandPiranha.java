package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.LandPiranhaSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PiranhaSprite;
import com.watabou.utils.Random;

public class LandPiranha extends Mob{
    {
        spriteClass = LandPiranhaSprite.class;

        baseSpeed = 2f;

        EXP = 0;

        loot = MysteryMeat.class;
        lootChance = 1f;


        state = SLEEPING;

    }
    public LandPiranha() {
        super();

        HP = HT = 10 + Dungeon.depth * 5;
        defenseSkill = 10 + Dungeon.depth * 2;
    }
    @Override
    public float speed() {
        float speed=super.speed();
        if(hero.pointsNegative(Talent.LAND_SWIMMING)<2 ){
            speed*=0.5f;
        }
        return speed * AscensionChallenge.enemySpeedModifier(this);
    }

    @Override
    protected boolean act() {
        if ((!Dungeon.level.water[pos] || flying) && hero.pointsNegative(Talent.LAND_SWIMMING)==0){
            dieOnLand();
            return true;
        } else {
            return super.act();
        }
    }
    @Override
    public int damageRoll() {
        return Random.NormalIntRange( Dungeon.depth, 4 + Dungeon.depth * 2 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 20 + Dungeon.depth * 2;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, Dungeon.depth);
    }
    @Override
    public void die( Object cause ) {
        super.die( cause );

        Statistics.piranhasKilled++;
        Badges.validatePiranhasKilled();
    }

    @Override
    public void damage(int dmg, Object src) {
        if(hero.hasTalent(Talent.FISHING_TIME)){
            dmg*=1.3;
        }
        super.damage(dmg, src);
    }
    public void dieOnLand(){
        die( null );
    }
}
