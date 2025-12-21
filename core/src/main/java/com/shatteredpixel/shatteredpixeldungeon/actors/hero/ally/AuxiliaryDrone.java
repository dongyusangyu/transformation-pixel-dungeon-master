package com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.newLevel;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.InstructionTool;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DronesSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class AuxiliaryDrone extends InstructionTool.Drone {
    public AuxiliaryDrone(){
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
    protected boolean getCloser( int target ) {
        if (enemy!=null && enemySeen && !(this instanceof  BombDrone)) {
            return enemySeen && getFurther( target );
        } else {
            return super.getCloser( target );
        }
    }

    private int shieldCooldown = 50;
    private int fastCooldown = 80;
    @Override
    protected boolean canAttack( Char enemy ) {
        return false;
    }

    @Override
    protected boolean act() {
        if(buff(DroneFast.class)==null){
            Buff.affect(this, DroneShield.class);
            Buff.affect(this, DroneFast.class);
        }

        boolean result = super.act();
        return result;
    }

    private static final String SHIELD     = "shield";
    private static final String FAST     = "fast";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SHIELD, shieldCooldown);
        bundle.put(FAST, fastCooldown);

    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        shieldCooldown = bundle.getInt(SHIELD);
        fastCooldown = bundle.getInt(FAST);
    }

    public static class ScoutDrone extends AuxiliaryDrone {
        {
            spriteClass = DronesSprite.ScoutDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
            if (Dungeon.hero != null) {
                int lvl = InstructionTool.Drone.getTool();
                viewDistance = 3+lvl/3;
            } else {
                viewDistance = 3;
            }
        }
        public ScoutDrone(){
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
        protected boolean act() {
            boolean result = super.act();
            if(result){
                for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                    if (mob.alignment != Char.Alignment.ALLY && fieldOfView[mob.pos]) {
                        Buff.affect(mob,WeaknessMark.class,1);
                    }
                }
                Dungeon.level.updateFieldOfView( this, fieldOfView );
                GameScene.updateFog(pos, viewDistance+(int)Math.ceil(speed()));
            }
            return result;
        }
    }

    public static class MirrorDrone extends AuxiliaryDrone {
        {
            spriteClass = DronesSprite.MirrorDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);

        }
        private int mirrorCooldown = 0;
        public MirrorDrone(){
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
        protected boolean act() {
            boolean result = super.act();
            if(result){
                int lvl = InstructionTool.Drone.getTool();
                mirrorCooldown++;
                if(mirrorCooldown > 41-lvl){
                    mirrorCooldown=0;
                    if(hero!=null){
                        ScrollOfMirrorImage.spawnImages(hero,pos,1);
                    }
                }
            }
            return result;
        }
        private static final String MIRROR     = "fast";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(MIRROR, mirrorCooldown);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            mirrorCooldown = bundle.getInt(MIRROR);
        }
    }


    public static class ProtectDrone extends AuxiliaryDrone {
        {
            spriteClass = DronesSprite.ProtectDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
        }
        public ProtectDrone(){
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
        public void die( Object cause ) {
            if(cause instanceof Mob){
                ScrollOfTeleportation.teleportChar((Mob)cause);
            }
            super.die(cause);
        }
    }

    public static class EscortDrone extends AuxiliaryDrone {
        {
            spriteClass = DronesSprite.EscortDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
        }
        public EscortDrone(){
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
        protected boolean act() {
            if(buff(DroneEscort.class)==null){
                Buff.affect(this,DroneEscort.class);
            }

            boolean result = super.act();
            return result;
        }
    }

    public static class BombDrone extends AuxiliaryDrone {
        {
            spriteClass = DronesSprite.BombDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
            baseSpeed=2f;
        }
        public BombDrone(){
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
        protected boolean act() {
            boolean result = super.act();
            if(enemy!=null && distance(enemy)<2){
                damage(3,this);
            }
            return result;
        }
        @Override
        public void die( Object cause ) {
            new DroneBomb().explode(this.pos);
            super.die(cause);
        }


    }

    public static class ChaosDrone extends InstructionTool.Drone {
        {
            spriteClass = DronesSprite.ChaosDroneSprite.class;
            HP = HT = 3;
            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;
            properties.add(Property.INORGANIC);
        }

        @Override
        protected boolean canAttack( Char enemy ) {
            return false;
        }
        public ChaosDrone(){
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
        protected boolean getCloser( int target ) {
            if (enemy!=null && enemySeen) {
                return enemySeen && getFurther( target );
            } else {
                return super.getCloser( target );
            }
        }
        @Override
        protected boolean act() {
            boolean result = super.act();
            int lvl = InstructionTool.Drone.getTool();
            left++;
            if(left>21-lvl){
                int p = pos;
                RollDrone(p);
                sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
                destroy();
                sprite.killAndErase();
                Sample.INSTANCE.play(Assets.Sounds.PUFF);
                return true;
            }
            return result;
        }
        public void RollDrone(int pos){
            Mob d = null;
            if(hero!=null && hero.subClass==HeroSubClass.AU400){
                switch (Random.Int(8)){
                    case 0: default:
                        d = new AttackDrone.FlashDrone();
                        break;
                    case 1:
                        d = new AttackDrone.LaserDrone();
                        break;
                    case 2:
                        d = new AttackDrone.AnesthesiaDrone();
                        break;
                    case 3:
                        d = new AttackDrone.RaidDrone();
                        break;
                    case 4:
                        d = new AttackDrone.ShockDrone();
                        break;
                    case 5:
                        d = new Sheep();
                        ((Sheep)d).initialize(50);
                        break;
                    case 6:
                        d = new MirrorImage();
                        ((MirrorImage)d).duplicate( hero );
                        break;
                    case 7:
                        switch (Random.Int(2)){
                            case 0:
                                d = new InstructionTool.Drone();
                                break;
                            case 1:
                                d = Dungeon.level.createMob();
                                break;
                        }
                }
            }else if(hero!=null && hero.subClass==HeroSubClass.AT400){
                switch (Random.Int(8)){
                    case 0: default:
                        d = new AuxiliaryDrone.ScoutDrone();
                        break;
                    case 1:
                        d = new AuxiliaryDrone.ProtectDrone();
                        break;
                    case 2:
                        d = new AuxiliaryDrone.EscortDrone();
                        break;
                    case 3:
                        d = new AuxiliaryDrone.BombDrone();
                        break;
                    case 4:
                        d = new AuxiliaryDrone.MirrorDrone();
                        break;
                    case 5:
                        d = new Sheep();
                        ((Sheep)d).initialize(50);
                        break;
                    case 6:
                        d = new MirrorImage();
                        ((MirrorImage)d).duplicate( hero );
                        break;
                    case 7:
                        switch (Random.Int(2)){
                            case 0:
                                d = new InstructionTool.Drone();
                                break;
                            case 1:
                                d = Dungeon.level.createMob();
                                break;
                        }
                }
            }else{
                d = new InstructionTool.Drone();
            }
            if(d!=null){
                GameScene.add(d);
                InstructionTool.Drone.appear( d, pos );
            }
        }
        public int left = 0;
        private static final String LEFT     = "left";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(LEFT, left);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            left = bundle.getInt(LEFT);
        }

    }


    public static class DroneBomb extends Bomb{
        @Override
        public void explode(int cell){
            //We're blowing up, so no need for a fuse anymore.
            this.fuse = null;

            Sample.INSTANCE.play( Assets.Sounds.BLAST );

            if (explodesDestructively()) {

                ArrayList<Char> affected = new ArrayList<>();

                if (Dungeon.level.heroFOV[cell]) {
                    CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
                }

                boolean terrainAffected = false;
                boolean[] explodable = new boolean[Dungeon.level.length()];
                BArray.not( Dungeon.level.solid, explodable);
                BArray.or( Dungeon.level.flamable, explodable, explodable);
                PathFinder.buildDistanceMap( cell, explodable, explosionRange() );
                for (int i = 0; i < PathFinder.distance.length; i++) {
                    if (PathFinder.distance[i] != Integer.MAX_VALUE) {
                        if (Dungeon.level.heroFOV[i]) {
                            CellEmitter.get(i).burst(SmokeParticle.FACTORY, 4);
                        }

                        if (Dungeon.level.flamable[i]) {
                            Dungeon.level.destroy(i);
                            GameScene.updateMap(i);
                            terrainAffected = true;
                        }


                        //destroys items / triggers bombs caught in the blast.
                        Heap heap = Dungeon.level.heaps.get(i);
                        if (heap != null) {
                            heap.explode();
                        }

                        Char ch = Actor.findChar(i);
                        if (ch != null) {
                            affected.add(ch);
                        }
                    }
                }
                for (Char ch : affected){
                    //if they have already been killed by another bomb
                    if(!ch.isAlive() && ((ch instanceof  Hero) || (ch instanceof InstructionTool.Drone))){
                        continue;
                    }
                    int dmg = Random.NormalIntRange(4 + Dungeon.scalingDepth(), 12 + 3*Dungeon.scalingDepth())*3/2;                    if(hero.hasTalent(Talent.BOMB_MANIAC)){
                        dmg*=1+hero.pointsInTalent(Talent.BOMB_MANIAC)*0.25f;
                    }
                    dmg -= ch.drRoll();
                    if (dmg > 0 && !((ch instanceof  Hero) || (ch instanceof InstructionTool.Drone))) {
                        ch.damage(dmg, this);
                    }
                    if(hero.hasTalent(Talent.SHOCK_BOMB) && ch!=hero){
                        Buff.affect(ch, Paralysis.class,hero.pointsInTalent(Talent.SHOCK_BOMB));
                    }

                }


                for (int p = 0; p < PathFinder.NEIGHBOURS9.length; p++) {
                    int i = cell + PathFinder.NEIGHBOURS9[p];
                    if ((Dungeon.level.map[i] == Terrain.REGION_DECO
                            || Dungeon.level.map[i] == Terrain.REGION_DECO_ALT)
                            && !(Dungeon.level instanceof SewerLevel)){
                        Splash.at(i, 0x555555, 10);
                        Sample.INSTANCE.play( Assets.Sounds.MINE, 0.6f );
                        Level.set( i, Terrain.EMPTY_DECO );
                        GameScene.updateMap(i);
                        terrainAffected = true;
                    }
                }
                if (terrainAffected) {
                    Dungeon.observe();
                }
            }
        }
    };


    public static class WeaknessMark extends FlavourBuff {
        public int icon() { return BuffIndicator.WEAKMARK; }
    };

    public static class DroneShield extends Buff {
        public int icon() { return BuffIndicator.NONE; }
        @Override
        public boolean act() {

            if(target.isAlive()){
                if(hero!=null && hero.pointsInTalent(Talent.ASSIST_UPGRADE)>0){
                    left1++;
                    //
                }
                if(hero!=null && left1>60-hero.pointsInTalent(Talent.ASSIST_UPGRADE)*10 && hero.shielding()<hero.HT/2){
                    Buff.affect(hero, Barrier.class).incShield(4);
                    left1=0;
                }
            }
            spend(TICK);
            return true;
        }
        public int left1 = 0;
        private static final String LEFT1     = "left1";
        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(LEFT1, left1);
        }
        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            left1 = bundle.getInt(LEFT1);
        }
    };

    public static class DroneFast extends Buff {
        public int icon() { return BuffIndicator.NONE; }

        @Override
        public boolean act() {

            if(target.isAlive()){
                if(hero!=null && hero.pointsInTalent(Talent.FAST_CRUISE)>0){
                    left1++;
                    //GLog.w("1");
                }
                if(hero!=null && left1>90-hero.pointsInTalent(Talent.FAST_CRUISE)*10){
                    Buff.affect(hero, Haste.class,2);
                    left1=0;
                }
            }
            spend(TICK);
            return true;
        }
        public int left1= 0;
        private static final String LEFT1     = "left1";
        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(LEFT1, left1);
        }
        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            left1 = bundle.getInt(LEFT1);
        }
    };

    public static class DroneEscort extends Buff {
        public int icon() { return BuffIndicator.NONE; }

        @Override
        public boolean act() {
            left++;
            if(left>50){
                if(hero!=null){
                    Buff.affect(hero, Invisibility.class,4);
                }else{
                    Buff.affect(target, Invisibility.class,4);
                }
                left=0;
            }
            spend( TICK );
            return true;
        }
        public int left = 0;
        private static final String LEFT     = "left";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(LEFT, left);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            left = bundle.getInt(LEFT);
        }
    };


}
