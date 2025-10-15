package com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DronesSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class AttackDrone  extends InstructionTool.Drone {

    public AttackDrone(){
        super();
        if(hero!=null){
            int hpBonus = (int)(hero.HT*0.1f);
            if (hpBonus > 0){
                HT += hpBonus;
                HP += hpBonus;
            }
            defenseSkill = hero.lvl+5;
        }

    }

    private int dodgesUsed = 4;

    @Override
    public int defenseSkill(Char enemy) {
        if (Dungeon.hero.hasTalent(Talent.FLY_DRONE) &&
                dodgesUsed > 3-Dungeon.hero.pointsInTalent(Talent.FLY_DRONE)) {
            dodgesUsed--;
            return Char.INFINITE_EVASION;
        }
        return super.defenseSkill(enemy);
    }

    private static final String DODGES_USED     = "dodges_used";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(DODGES_USED, dodgesUsed);

    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        dodgesUsed = bundle.getInt(DODGES_USED);
    }

    public static class FlashDrone extends AttackDrone {
        {
            spriteClass = DronesSprite.FlashDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
        }
        public FlashDrone(){
            super();
            if(hero!=null){
                int hpBonus = (int)(hero.HT*0.2f);
                if (hpBonus > 0){
                    HT += hpBonus;
                    HP += hpBonus;
                }
                defenseSkill = hero.lvl+5;
            }

        }

        @Override
        protected boolean canAttack( Char enemy ) {
            return Dungeon.level.distance( pos, enemy.pos )<4
                    && (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
        }
        @Override
        public int damageRoll() {
            int lvl = InstructionTool.Drone.getTool();
            int damage = Random.NormalIntRange(lvl, lvl+5);
            return damage;
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            //enemy.damage(damageRoll(),new WandOfMagicMissile());
            damage = super.attackProc( enemy, damage );
            if(enemy.buff(InstructionTool.InstructionMark.class)!=null){
                Buff.affect(enemy, Blindness.class,3);
                if (Char.hasProp(enemy, Char.Property.UNDEAD) || Char.hasProp(enemy, Char.Property.DEMONIC)){
                    enemy.damage(5, new WandOfMagicMissile());
                }
            }
            return damage;
        }

    }


    public static class LaserDrone extends AttackDrone {
        {
            spriteClass = DronesSprite.LaserDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
        }
        public LaserDrone(){
            super();
            if(hero!=null){
                int hpBonus = (int)(hero.HT*0.1f);
                if (hpBonus > 0){
                    HT += hpBonus;
                    HP += hpBonus;
                }
                defenseSkill = hero.lvl+5;
            }

        }
        @Override
        public int attackSkill(Char target) {
            return 2*super.attackSkill(this);
        }

        @Override
        protected boolean canAttack( Char enemy ) {
            return (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
        }
        @Override
        public int damageRoll() {
            int lvl = InstructionTool.Drone.getTool();
            int damage = Random.NormalIntRange(lvl, lvl+2);
            return damage;
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            enemy.damage(damageRoll(),new WandOfMagicMissile());
            if(enemy.buff(InstructionTool.InstructionMark.class)!=null && Random.Int(2)==0){
                Buff.affect(enemy, Burning.class).extend(3);
            }
            return -1;
        }

    }

    public static class AnesthesiaDrone extends AttackDrone {
        {
            spriteClass = DronesSprite.AnesthesiaDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
        }
        public AnesthesiaDrone(){
            super();
            if(hero!=null){
                int hpBonus = (int)(hero.HT*0.33f);
                if (hpBonus > 0){
                    HT += hpBonus;
                    HP += hpBonus;
                }
                defenseSkill = hero.lvl+5;
            }

        }
        @Override
        public int damageRoll() {
            int lvl = InstructionTool.Drone.getTool();
            int damage = Random.NormalIntRange(1+lvl, 2*lvl);
            return damage;
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            damage = super.attackProc( enemy, damage );
            if(enemy.buff(InstructionTool.InstructionMark.class)!=null && Random.Int(3)==0){
                Buff.affect(enemy, Vertigo.class,3);
                if(Random.Int(2)==1){
                    Ballistica trajectory = new Ballistica(this.pos, enemy.pos, Ballistica.STOP_TARGET);
                    //trim it to just be the part that goes past them
                    trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
                    //knock them back along that ballistica
                    WandOfBlastWave.throwChar(enemy,
                            trajectory,
                            1,
                            false,
                            true,
                            this);
                }
            }
            return damage;
        }

    }

    public static class RaidDrone extends AttackDrone {
        {
            spriteClass = DronesSprite.RaidDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
            baseSpeed=2f;
        }
        public RaidDrone(){
            super();
            if(hero!=null){
                int hpBonus = (int)(hero.HT*0.1f);
                if (hpBonus > 0){
                    HT += hpBonus;
                    HP += hpBonus;
                }
                defenseSkill = hero.lvl+5;
            }

        }



        @Override
        public int damageRoll() {
            int lvl = InstructionTool.Drone.getTool();
            int damage = Random.NormalIntRange(lvl, 5+lvl);
            return damage;
        }

        @Override
        protected boolean canAttack( Char enemy ) {
            return Dungeon.level.distance( pos, enemy.pos )<4
                    && (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            damage = super.attackProc( enemy, damage );
            if(enemy.buff(InstructionTool.InstructionMark.class)!=null && Random.Int(6)==0){
                Buff.affect(enemy, Amok.class,3);
            }else if(Random.Int(3)==0){
                Buff.affect(enemy, InstructionTool.InstructionMark.class).reset(3);
            }
            return damage;
        }

    }

    public static class ShockDrone extends AttackDrone {
        {
            spriteClass = DronesSprite.ShockDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
        }
        public ShockDrone(){
            super();
            if(hero!=null){
                int hpBonus = (int)(hero.HT*0.1f);
                if (hpBonus > 0){
                    HT += hpBonus;
                    HP += hpBonus;
                }
                defenseSkill = hero.lvl+5;
            }

        }
        @Override
        public int damageRoll() {
            int lvl = InstructionTool.Drone.getTool();
            int damage = Random.NormalIntRange(lvl, 5+lvl);
            return damage;
        }

        @Override
        protected boolean canAttack( Char enemy ) {
            return (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            damage = super.attackProc( enemy, damage );
            if(enemy.buff(InstructionTool.InstructionMark.class)!=null && Random.Int(3)==0){
                Buff.affect(enemy, Terror.class,8).object = id();;
            }else if(Random.Int(6)==0){
                Buff.affect(enemy, InstructionTool.InstructionMark.class).reset(3);
            }
            return damage;
        }

    }

}
