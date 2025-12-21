package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Pheromone;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MostDegrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ElementalSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GoldElementalSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GoldGolemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GolemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MonkMasterSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MonkSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ScorpioBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ThymorSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarlockSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;


public abstract class GoldBoss extends Mob {
    {

        HP = HT = 500;

        EXP = 0;

        //so that allies can attack it. States are never actually used.
        state = HUNTING;
        defenseSkill = 20;

        viewDistance = 30;

        properties.add(Property.BOSS);
        properties.add(Property.GOLD);

    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(15, 30);
    }

    @Override
    public void die(Object src) {
        super.die(src);
    }

    @Override
    public int attackSkill(Char target) {
        return 50;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(5, 25);
    }

    @Override
    public void damage(int dmg, Object src) {
        dmg = Math.min(100, dmg);
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())) {
            lock.addTime(dmg / 10);
        }
        super.damage(dmg, src);
    }


    public  static class MonkMaster extends GoldBoss {

        {
            spriteClass = MonkMasterSprite.class;
            properties.add(Property.UNDEAD);

        }

        public int damageRoll() {
            return Random.NormalIntRange( 10, 25 );
        }
        @Override
        public int drRoll() {
            return 0;
        }

        @Override
        public float attackDelay() {
            return super.attackDelay()*0.5f;
        }


        @Override
        public void damage(int dmg, Object src) {
            if(src==hero && ((hero.belongings.attackingWeapon() instanceof MissileWeapon) || (hero.belongings.attackingWeapon() instanceof SpiritBow))){
                yell(Messages.get(this, "missileweapon"));
                Buff.affect(this, Adrenaline.class,5);
                //Buff.affect(this, Haste.class,2);
                dmg=0;
            }
            super.damage(dmg, src);
        }

        @Override
        public float speed() {
            float speed=super.speed();
            if(hero != null && hero.speed()>1){
                speed*=(hero.speed()+1);
            }

            return speed * AscensionChallenge.enemySpeedModifier(this);
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            damage = super.attackProc( enemy, damage );
            if(hero != null){
                damage *= (Talent.onAttackProcMult(hero, enemy, 1 )+1);
                damage += Talent.onAttackProcBonus(hero, enemy);
            }
            return damage;
        }
    }

    public  static class GoldElemental extends GoldBoss {
        {
            spriteClass = GoldElementalSprite.class;
            flying = true;
        }

        @Override
        protected boolean canAttack( Char enemy ) {
            if (super.canAttack(enemy)){
                return true;
            } else {
                return new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos == enemy.pos;
            }
        }
        protected boolean doAttack( Char enemy ) {

            if (Dungeon.level.adjacent( pos, enemy.pos )
                    || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos != enemy.pos) {
                return super.doAttack( enemy );

            } else {

                if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                    sprite.zap( enemy.pos );
                    return false;
                } else {
                    zap();
                    return true;
                }
            }
        }
        protected void zap() {
            spend( 1f );
            Invisibility.dispel(this);
            Char enemy = this.enemy;
            if (hit( this, enemy, true )) {
                attackProc( enemy ,damageRoll());
            } else {
                enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
            }

        }
        public void onZapComplete() {
            zap();
            next();
        }
        @Override
        public int attackProc( Char enemy, int damage ) {

            if(Random.Int(4)==0){
                Buff.affect(enemy, Paralysis.class,1);
            }
            if(enemy==hero){
                if(Dungeon.gold>300){
                    Dungeon.gold-=300;
                    Buff.affect(this, Barrier.class).incShield(20);
                    GLog.i(Messages.get(this,"steal"));
                }else{
                    Dungeon.gold+=400;
                    GLog.i(Messages.get(this,"give"));
                }
            }
            damage = super.attackProc( enemy, damage );
            return damage;
        }
        @Override
        protected boolean act() {
            if(Random.Int(20)==1){
                ArrayList<Integer> respawnPoints = new ArrayList<>();
                for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
                    int p = pos + PathFinder.NEIGHBOURS9[i];
                    if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                        respawnPoints.add( p );
                    }
                }

                int spawned = 0;
                int a = respawnPoints.size();
                while (spawned < 4 && respawnPoints.size() > 0) {
                    int index1 = Random.index( respawnPoints );
                    Mob mob =  new Elemental.ShockElemental();
                    switch (Random.Int(3)){
                        case 0:
                            mob = new Elemental.ShockElemental();
                            break;
                        case 1:
                            mob = new Elemental.FireElemental();
                            break;
                        case 2:
                            mob = new Elemental.FrostElemental();
                            break;
                    }
                    mob.state= mob.HUNTING;
                    GameScene.add( mob );
                    ScrollOfTeleportation.appear( mob, respawnPoints.get( index1 ) );
                    respawnPoints.remove( index1 );
                    spawned++;
                }
                super.act();
            }

            if (hero.invisible <= 0 && state == WANDERING){
                beckon(hero.pos);
                state = HUNTING;
                enemy = hero;
            }
            return super.act();
        }
    }


    public  static class GoldGolem extends GoldBoss {
        {
            spriteClass = GoldGolemSprite.class;
            properties.add(Property.INORGANIC);
            properties.add(Property.LARGE);

        }
        @Override
        public int damageRoll() {
            return Random.NormalIntRange(25, 45);
        }

        @Override
        public void damage(int dmg, Object src) {

            super.damage(dmg, src);
        }
        @Override
        public int drRoll() {
            return super.drRoll() + Random.NormalIntRange(5, 10);
        }
        @Override
        protected boolean act() {
            if(this.buffs(ElectricityCooldown.class).isEmpty()){
                for( int i : PathFinder.NEIGHBOURS9) {
                    if (!Dungeon.level.solid[pos + i]) {
                        GameScene.add(Blob.seed(pos + i, 10, Electricity.class));
                    }
                }
                yell(Messages.get(this, "electricity"));
                Buff.affect(this,ElectricityCooldown.class,10);
            }
            for (int i : PathFinder.NEIGHBOURS9){
                Char ch = Actor.findChar( pos+i );
                if(ch instanceof Sheep){
                    ch.die(this);
                }
            }
            return super.act();
        }
    }

    public  static class Thymor extends GoldBoss {
        {
            spriteClass = ThymorSprite.class;
            properties.add(Property.UNDEAD);

        }

        private int curseUp = 0;

        protected boolean doAttack( Char enemy ) {

            if (Dungeon.level.adjacent( pos, enemy.pos )
                    || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

                return super.doAttack( enemy );

            } else {

                if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                    sprite.zap( enemy.pos );
                    return false;
                } else {
                    zap();
                    return true;
                }
            }
        }

        @Override
        protected boolean canAttack( Char enemy ) {
            return super.canAttack(enemy)
                    || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
        }

        protected void zap() {
            spend( 0.5f );

            Invisibility.dispel(this);
            Char enemy = this.enemy;
            if (hit( this, enemy, true )) {
                //TODO would be nice for this to work on ghost/statues too
                if (enemy == hero){
                    Buff.affect(enemy, MostDegrade.class,10 );
                    Sample.INSTANCE.play( Assets.Sounds.DEGRADE );
                }
                //int dmg = Random.NormalIntRange( 20, 30 );
                int dmg = Random.NormalIntRange( 1, 1 );
                dmg = Math.round(dmg * AscensionChallenge.statModifier(this));

                //logic for DK taking 1/2 damage from aggression stoned minions
                if ( enemy.buff(StoneOfAggression.Aggression.class) != null
                        && enemy.alignment == alignment
                        && (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))){
                    dmg *= 0.5f;
                }
                if(hero.hasTalent(Talent.NO_VIEWRAPE)){
                    if(enemy==hero && distance(enemy)>1){
                        Buff.affect(this, Blindness.class,hero.pointsInTalent(Talent.NO_VIEWRAPE));
                    }
                }
                enemy.damage( dmg, new Warlock.DarkBolt() );

                if (enemy == hero && !enemy.isAlive()) {
                    Badges.validateDeathFromEnemyMagic();
                    Dungeon.fail( this );
                    GLog.n( Messages.get(this, "bolt_kill") );
                }
            } else {
                enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
            }
        }

        public void onZapComplete() {
            zap();
            next();
        }

        public void call() {
            next();
        }

        @Override
        protected boolean act() {
            if(this.buffs(SheepCurseCooldown.class).isEmpty() && curseUp<2){
                if(curseUp==0){
                    yell(Messages.get(this, "curse"));
                }
                curseUp++;
                PathFinder.buildDistanceMap(pos, BArray.not( Dungeon.level.solid, null ), 2 );
                for (int i = 0; i < PathFinder.distance.length; i++) {
                    if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                        sprite.parent.addToBack(new TargetedCell(i, 0xFF0000));
                    }
                }
                spend( 1f );
                return true;
            }
            if(curseUp==2){
                Buff.affect(this, SheepCurseCooldown.class,20);
                curseUp=0;
                yell(Messages.get(this, "cursedo"));
                PathFinder.buildDistanceMap(pos, BArray.not( Dungeon.level.solid, null ), 2 );
                for (int i = 0; i < PathFinder.distance.length; i++) {
                    if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                        Char ch = Actor.findChar(i);
                        if(ch==null){

                        }else if(ch==hero && ch.isAlive()){
                            Buff.affect(ch, Hex.class,5);
                            Buff.affect(ch, Vulnerable.class,5);
                            Buff.affect(ch, Blindness.class,5);
                            Buff.affect(ch, Charm.class,5).object=this.id();
                            Buff.affect(ch, Burning.class).reignite(ch,5);
                            Buff.affect(ch, Degrade.class,5);
                            Buff.affect(ch, Amok.class,5);
                            Buff.affect(ch, Weakness.class,5);
                            Buff.affect(ch, Chill.class,5);
                            Buff.affect(ch, Slow.class,5);
                        }else if(ch.alignment != Char.Alignment.ENEMY){
                            ch.sprite.showStatus( CharSprite.WARNING, "die" );
                            ch.die(this);
                        }
                    }
                }
            }
            return super.act();
        }
        private final String CURSEUP = "curseup";
        @Override
        public void storeInBundle( Bundle bundle ) {

            super.storeInBundle( bundle );

            bundle.put( CURSEUP , curseUp );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {

            super.restoreFromBundle( bundle );

            curseUp = bundle.getInt( CURSEUP );
        }
    }

    public static class ElectricityCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(1f, 1f, 0f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 10); }
    };

    public static class SheepCurseCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(0.2f, 0.2f, 0.2f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
    };
    
}