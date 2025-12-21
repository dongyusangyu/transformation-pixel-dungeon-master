package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blizzard;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Pheromone;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EyeBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EyeSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FistSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RipperBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ScorpioBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ScorpioSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SuccbossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.YogSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public abstract class ChaosDisciples extends Mob{
    {
        //spriteClass = SuccbossSprite.class;

        HP = HT = 500;

        EXP = 50;

        //so that allies can attack it. States are never actually used.
        state = HUNTING;
        defenseSkill = 20;

        viewDistance = 60;

        properties.add(Property.BOSS);
        properties.add(Property.EVIL);
        properties.add(Property.DEMONIC);
        //properties.add(Property.STATIC);

    }
    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 25, 50 );
    }
    @Override
    public int attackSkill( Char target ) {
        return 50;
    }
    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(5, 25);
    }
    @Override
    public void damage(int dmg, Object src) {
        dmg=Math.min(100,dmg);
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())){
            lock.addTime(dmg/10);
        }
        super.damage(dmg, src);
    }



    public  static class EyeBoss extends ChaosDisciples {
        private static final float TIME_TO_ZAP	= 1f;

        {
            //spriteClass = YogSprite.class;
            spriteClass = EyeBossSprite.class;
            flying = true;

        }
        private float abilityCooldown=2;
        private static final int MIN_ABILITY_CD = 10;
        private static final int MAX_ABILITY_CD = 15;
        private static final String ABILITY_CD = "ability_cd";
        public int damageRoll() {
            return Random.NormalIntRange( 5, 20 );
        }
        @Override
        public int drRoll() {
            return super.drRoll() + Random.NormalIntRange(5,10);
        }

        @Override
        public void damage(int dmg, Object src) {
            if (Random.Int( 3 ) == 0) {
                int i;
                do {
                    i = Random.Int(Dungeon.level.length());
                } while (Dungeon.level.heroFOV[i]
                        || Dungeon.level.solid[i]
                        || Actor.findChar(i) != null
                        || PathFinder.getStep(i, Dungeon.level.exit(), Dungeon.level.passable) == -1);
                ScrollOfTeleportation.appear(this, i);
            }
            super.damage(dmg, src);
        }
        @Override
        protected boolean canAttack( Char enemy ) {
            return super.canAttack(enemy)
                    || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
        }
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
        public static class DarkBolt{}




        protected void zap() {
            spend( TIME_TO_ZAP );

            Invisibility.dispel(this);
            Char enemy = this.enemy;
            if (hit( this, enemy, true )) {
                Buff c= enemy.buff(PotionOfCleansing.Cleanse.class);
                if(c!=null){
                    c.detach();
                }
                switch (Random.Int(5)){
                    case 0:
                        Buff.affect(enemy, Hex.class,5);
                        Buff.affect(enemy, Vulnerable.class,5);
                        break;
                    case 1:
                        Buff.affect(enemy, Blindness.class,5);
                        Buff.affect(enemy, Charm.class,5);
                        break;
                    case 2:
                        Buff.affect(enemy, Burning.class);
                        Buff.affect(enemy, Degrade.class,5);
                        break;
                    case 3:
                        Buff.affect(enemy, Amok.class,5);
                        Buff.affect(enemy, Weakness.class,5);
                        break;
                    case 5:
                        Buff.affect(enemy, Chill.class,5);
                        Buff.affect(enemy, Slow.class,5);
                        break;
                }

                Sample.INSTANCE.play( Assets.Sounds.DEGRADE );
                int dmg = Random.NormalIntRange( 30, 40 );
                dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
                //logic for DK taking 1/2 damage from aggression stoned minions
                if ( enemy.buff(StoneOfAggression.Aggression.class) != null
                        && enemy.alignment == alignment
                        && (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))){
                    dmg *= 0.5f;
                }
                if(hero.hasTalent(Talent.NO_VIEWRAPE)) {
                    if (enemy == hero && distance(enemy) > 1) {
                        Buff.affect(this, Blindness.class, hero.pointsInTalent(Talent.NO_VIEWRAPE));
                    }
                }
                enemy.damage( dmg, new Warlock.DarkBolt() );

            } else {
                enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
            }
        }
        public void onZapComplete() {
            zap();
            next();
        }

        private ArrayList<Integer> targetedCells = new ArrayList<>();

        @Override
        protected boolean act() {
            boolean terrainAffected = false;
            HashSet<Char> affected = new HashSet<>();
            if (!Dungeon.hero.rooted) {
                for (int i : targetedCells) {
                    Ballistica b = new Ballistica(pos, i, Ballistica.WONT_STOP);
                    //shoot beams
                    sprite.parent.add(new Beam.DeathRay(sprite.center(), DungeonTilemap.raisedTileCenterToWorld(b.collisionPos)));
                    for (int p : b.path) {
                        Char ch = Actor.findChar(p);
                        if (ch != null && (ch.alignment != alignment || ch instanceof Bee)) {
                            affected.add(ch);
                        }
                        if (Dungeon.level.flamable[p]) {
                            Dungeon.level.destroy(p);
                            GameScene.updateMap(p);
                            terrainAffected = true;
                        }
                    }
                }
                if (terrainAffected) {
                    Dungeon.observe();
                }
                Invisibility.dispel(this);
                for (Char ch : affected) {
                    if (hit( this, ch, true )) {
                        ch.damage(Random.NormalIntRange(40, 50), new Eye.DeathGaze());
                        if (Dungeon.level.heroFOV[pos]) {
                            ch.sprite.flash();
                            CellEmitter.center(pos).burst(PurpleParticle.BURST, Random.IntRange(1, 2));
                        }
                    } else {
                        ch.sprite.showStatus( CharSprite.NEUTRAL,  ch.defenseVerb() );
                    }
                }
                targetedCells.clear();
            }
            if (abilityCooldown <= 0){
                int beams = 2;
                HashSet<Integer> affectedCells = new HashSet<>();
                for (int i = 0; i < beams; i++){

                    int targetPos = Dungeon.hero.pos;
                    if (i != 0){
                        do {
                            targetPos = Dungeon.hero.pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
                        } while (Dungeon.level.trueDistance(pos, Dungeon.hero.pos)
                                > Dungeon.level.trueDistance(pos, targetPos));
                    }
                    targetedCells.add(targetPos);
                    Ballistica b = new Ballistica(pos, targetPos, Ballistica.WONT_STOP);
                    affectedCells.addAll(b.path);
                }
                //remove one beam if multiple shots would cause every cell next to the hero to be targeted
                boolean allAdjTargeted = true;
                for (int i : PathFinder.NEIGHBOURS9){
                    if (!affectedCells.contains(Dungeon.hero.pos + i) && Dungeon.level.passable[Dungeon.hero.pos + i]){
                        allAdjTargeted = false;
                        break;
                    }
                }
                if (allAdjTargeted){
                    targetedCells.remove(targetedCells.size()-1);
                }
                for (int i : targetedCells){
                    Ballistica b = new Ballistica(pos, i, Ballistica.WONT_STOP);
                    for (int p : b.path){
                        sprite.parent.add(new TargetedCell(p, 0xFF0000));
                        affectedCells.add(p);
                    }
                }
                //don't want to overly punish players with slow move or attack speed
                spend(GameMath.gate(TICK, (int)Math.ceil(Dungeon.hero.cooldown()), 3*TICK));
                Dungeon.hero.interrupt();
                abilityCooldown += Random.NormalFloat(MIN_ABILITY_CD, MAX_ABILITY_CD);
                abilityCooldown -= 1;
                spend(TICK);
            }else {
                spend(TICK);
            }
            if (hero.invisible <= 0 && state == WANDERING){
                beckon(hero.pos);
                state = HUNTING;
                enemy = hero;
            }
            abilityCooldown -= 1;

            return super.act();
        }
        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(ABILITY_CD, abilityCooldown);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            abilityCooldown = bundle.getFloat(ABILITY_CD);
        }
    }

    public  static class RipperBoss extends ChaosDisciples {

        {
            spriteClass = RipperBossSprite.class;
            baseSpeed = 2f;


        }
        private int combo=0;
        private float combotime=0;
        public int damageRoll() {
            return Random.NormalIntRange( 50, 75 );
        }
        @Override
        public float attackDelay() {
            return super.attackDelay()*0.5f;
        }

        @Override
        public void damage(int dmg, Object src) {
            Buff.affect(this, Adrenaline.class,5);
            super.damage(dmg, src);
        }
        public String info(){
            String desc = description();

            for (Buff b : buffs(ChampionEnemy.class)){
                desc += "\n\n_" + Messages.titleCase(b.name()) + "_\n" + b.desc();
            }
            desc +="\n\n_"+ Messages.get(this, "combo",combo)+Messages.get(this, "combotime",combotime);

            return desc;
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            damage = super.attackProc( enemy, damage );
            combo++;
            combotime+=5;
            damage=(int)(damage*(1+0.1f*combo));
            Buff.affect(this, Adrenaline.class,5);

            return damage;
        }

        @Override
        protected boolean act() {
            if(combotime>0){
                combotime--;
            }else{
                combotime=0;
                combo=0;
            }
            if (hero.invisible <= 0 && state == WANDERING){
                beckon(hero.pos);
                state = HUNTING;
                enemy = hero;
            }

            return super.act();
        }
        private static final String COMBO = "count";
        private static final String COMBOTIME  = "combotime";
        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(COMBO,combo);
            bundle.put(COMBOTIME, combotime);

        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            combo = bundle.getInt( COMBO );
            combotime = bundle.getFloat( COMBOTIME );}
    }


    public  static class ScorpioBoss extends ChaosDisciples {

        {
            spriteClass = ScorpioBossSprite.class;

        }

        public int damageRoll() {
            return Random.NormalIntRange( 30, 50 );
        }
        @Override
        public int drRoll() {
            return super.drRoll() + Random.NormalIntRange(25, 50);
        }
        @Override
        public void damage(int dmg, Object src) {
            if ( AntiMagic.RESISTS.contains(src.getClass())){
                dmg =(int)(dmg*0.3f);
            }
            if(src.getClass() == Burning.class){
                dmg*=5;
                yell(Messages.get(this, "fire"));
            }
            dmg=Math.min(50,dmg);
            if(src==hero && Dungeon.level.distance(hero.pos,pos)>1 && dmg>10){
                hero.damage((int)(dmg/2),this);
            }
            super.damage(dmg, src);
        }


        @Override
        protected boolean canAttack( Char enemy ) {
            return  super.canAttack(enemy)
                    || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
        }
        @Override
        public int attackProc( Char enemy, int damage ) {
            damage = super.attackProc( enemy, damage );
            if (Random.Int( 2 ) == 0) {
                Buff.prolong( enemy, Cripple.class, Cripple.DURATION );
                Buff.affect( enemy, Ooze.class).set(10);
            }

            return damage;
        }

        @Override
        protected boolean act() {
            GameScene.add(Blob.seed(pos, 50, ParalyticGas.class));
            GameScene.add(Blob.seed(pos, 50, ToxicGas.class));
            GameScene.add(Blob.seed(pos, 50, CorrosiveGas.class));
            if (hero.invisible <= 0 && state == WANDERING){
                beckon(hero.pos);
                state = HUNTING;
                enemy = hero;
            }

            return super.act();
        }
    }



    public static class SuccBoss extends ChaosDisciples {

        {
            spriteClass = SuccbossSprite.class;
        }


        @Override
        protected boolean act() {

            if(buffs(SummonCooldown.class).isEmpty()){
                Buff.affect(this,SummonCooldown.class,30);
                ArrayList<Integer> respawnPoints = new ArrayList<>();
                for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
                    int p = pos + PathFinder.NEIGHBOURS9[i];
                    if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                        respawnPoints.add( p );
                    }
                }

                int spawned = 0;
                while (spawned < 4 && respawnPoints.size() > 0) {
                    int index1 = Random.index( respawnPoints );
                    Succubus mob = new Succubus();
                    mob.state= mob.HUNTING;
                    GameScene.add( mob );
                    ScrollOfTeleportation.appear( mob, respawnPoints.get( index1 ) );
                    respawnPoints.remove( index1 );
                    spawned++;
                }
            }


            GameScene.add(Blob.seed(pos, 50, Pheromone.class));
            if (hero.invisible <= 0 && state == WANDERING){
                beckon(hero.pos);
                state = HUNTING;
                enemy = hero;
            }

            return super.act();
        }

    }
    public static class SummonCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(1f, 0f, 0.8f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 30); }
    };
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                Music.INSTANCE.play(Assets.Music.BACKS1_1, true);
            }
        });


    }






}
