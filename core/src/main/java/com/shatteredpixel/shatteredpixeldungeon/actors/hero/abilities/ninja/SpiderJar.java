package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import static jdk.javadoc.internal.doclets.toolkit.util.DocPath.parent;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Noisemaker;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GreatShoperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpiderJar extends ArmorAbility {

    {
        baseChargeUse = 35f;
    }
    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }
    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        if (target == null) {
            return;
        }
        Ballistica route = new Ballistica(hero.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
        int cell = route.collisionPos;
        Spider b = new Spider();
        ((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).
                reset(hero.sprite,
                        target,
                        b,
                        new Callback() {
                            @Override
                            public void call() {
                                b.onThrow(cell);
                            }
                        });
        armor.charge -= chargeUse(hero);
        armor.updateQuickslot();
        Invisibility.dispel();

    }

    @Override
    public int icon() {
        return HeroIcon.SPIDERJAR;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.POWER_GUNPOWDER, Talent.TEA_STAINS, Talent.FIREWORK, Talent.HEROIC_ENERGY};
    }

    public static class Spider extends Bomb {
        {
            image = ItemSpriteSheet.SPIDER;
            usesTargeting = true;
            stackable = false;
        }
        private static boolean lightingFuse = true;
        public SpiderFuse createFuse(){

            return new SpiderFuse();
        }
        @Override
        public boolean doPickUp(Hero hero, int pos) {
            return false;
        }
        @Override
        public void onThrow( int cell ) {
            if (!Dungeon.level.pit[ cell ] && lightingFuse) {
                fuse = createFuse().ignite(this);
                Actor.add(fuse);
                Dungeon.level.drop(this, cell);
            }else{
                super.onThrow( cell );
            }

        }
        @Override
        protected int explosionRange(){
            if(hero.hasTalent(Talent.POWER_GUNPOWDER)){
                return 1+(int)(hero.pointsInTalent(Talent.POWER_GUNPOWDER)/2);
            }else{
                return 1;
            }
        }
        public void explode(int cell){
            if(Actor.all().contains(this.fuse)){
                Actor.remove(this.fuse);
            }

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
                    if(!ch.isAlive()){
                        continue;
                    }
                    int m=15;
                    int M=30;
                    if(hero.hasTalent(Talent.POWER_GUNPOWDER)){
                        m+=(int)((1+hero.pointsInTalent(Talent.POWER_GUNPOWDER))/2)*10;
                        M+=(int)((1+hero.pointsInTalent(Talent.POWER_GUNPOWDER))/2)*10;
                    }
                    int dmg = Random.NormalIntRange(m, M);
                    if(hero.hasTalent(Talent.BOMB_MANIAC)){
                        dmg*=1+hero.pointsInTalent(Talent.BOMB_MANIAC)*0.25;
                    }
                    if(ch instanceof Hero){
                        dmg*=1.5f;
                    }
                    dmg -= ch.drRoll();

                    if (dmg > 0) {
                        ch.damage(dmg, this);
                    }
                    if(hero.hasTalent(Talent.TEA_STAINS) && ch!=hero && ch.isAlive()){
                        int r=Math.min(hero.pointsInTalent(Talent.TEA_STAINS),2);
                        int t=5;
                        if(hero.pointsInTalent(Talent.TEA_STAINS)>3){
                            t+=3;
                        }
                        if(Random.Int(r)==0){
                            Buff.affect(ch, Burning.class).extend(t);
                        }else{
                            Buff.affect(ch, Chill.class,t);
                        }
                        if(hero.pointsInTalent(Talent.TEA_STAINS)>2){
                            Buff.affect(ch, Paralysis.class,5);
                        }
                    }
                    if(hero.hasTalent(Talent.SHOCK_BOMB) && ch!=hero && ch.isAlive()){
                        Buff.affect(ch, Paralysis.class,hero.pointsInTalent(Talent.SHOCK_BOMB));
                    }

                    if (ch == hero && !ch.isAlive()) {
                        GLog.n(Messages.get(this, "ondeath"));
                        Dungeon.fail(this);
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
                if(hero.hasTalent(Talent.FIREWORK)){
                    SmallSpider b = new SmallSpider();
                    b.onThrow(cell);
                }
            }
        }

    }

    public static class SmallSpider extends Spider {
        {
            image = ItemSpriteSheet.SMALLSPIDER;
            usesTargeting = true;
            stackable = false;
        }
        private static boolean lightingFuse = true;

        @Override
        public void onThrow( int cell ) {
            if (!Dungeon.level.pit[ cell ] && lightingFuse) {

                fuse = createFuse().ignite(this);
                Actor.add(fuse);
                Dungeon.level.drop(this, cell);
            }else{
                super.onThrow( cell );
            }
        }


        @Override
        protected int explosionRange(){
            return 1;
        }
        public void explode(int cell){
            if(Actor.all().contains(this.fuse)){
                Actor.remove(this.fuse);
            }
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
                    if(!ch.isAlive()){
                        continue;
                    }
                    int m=0;
                    int M=0;
                    if(hero.hasTalent(Talent.FIREWORK)){
                        if(hero.pointsInTalent(Talent.FIREWORK)>1){
                            M=10;
                        }
                        m+=(int)((hero.pointsInTalent(Talent.FIREWORK))/2)*5;
                        M+=(int)((hero.pointsInTalent(Talent.FIREWORK))/2)*5;
                    }
                    int dmg = Random.NormalIntRange(m, M);
                    if(hero.hasTalent(Talent.BOMB_MANIAC)){
                        dmg*=1+hero.pointsInTalent(Talent.BOMB_MANIAC)*0.25;
                    }
                    if(ch instanceof Hero){
                        dmg*=1.5f;
                    }
                    dmg -= ch.drRoll();

                    if (dmg > 0) {
                        ch.damage(dmg, this);
                    }
                    if(hero.hasTalent(Talent.SHOCK_BOMB) && ch!=hero && ch.isAlive()){
                        Buff.affect(ch, Paralysis.class,hero.pointsInTalent(Talent.SHOCK_BOMB));
                    }

                    if (ch == hero && !ch.isAlive()) {
                        GLog.n(Messages.get(SpiderJar.Spider.class, "ondeath"));
                        Dungeon.fail(SpiderJar.Spider.class);
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

    }


    public static class SpiderFuse extends Noisemaker.NoisemakerFuse {
        public int l=0;
        public Bomb.Fuse ignite(Bomb bomb){
            this.bomb = bomb;
            return this;
        }

        @Override
        protected boolean act() {
            if (bomb.fuse != this){
                Actor.remove( this );
                return true;
            }

            for (Heap heap : Dungeon.level.heaps.valueList()) {
                if (heap.items.contains(bomb)) {
                    //active noisemakers cannot be snuffed out, blow it up!
                    int maxl=3;
                    if(this.bomb instanceof SmallSpider){
                        maxl = 10;
                        if(hero.pointsInTalent(Talent.FIREWORK)>2){
                            maxl=15;
                        }
                        switch (Random.Int(5)){
                            default:
                                break; //do nothing
                            case 1:
                                new Flare(6, 20).color(0x00FF00, true).show(heap.sprite,3);
                                break;
                            case 2:
                                new Flare(6, 24).color(0x00AAFF, true).show(heap.sprite,3);
                                break;
                            case 3:
                                new Flare(6, 28).color(0xAA00FF, true).show(heap.sprite,3);
                                break;
                            case 4:
                                new Flare(6, 32).color(0xFFAA00, true).show(heap.sprite,3);
                                break;
                        }
                        if(Math.floorMod(l,3)==1) {
                            CellEmitter.center(heap.pos).start(Speck.factory(Speck.SCREAM), 0.3f, 3);
                        }

                    }
                    if(Math.floorMod(l,2)==1) {
                        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                            if (mob.state != mob.SLEEPING) {
                                mob.beckon(heap.pos);
                            }
                        }


                    }
                    if(!(this.bomb instanceof SmallSpider)){
                        new Flare(6, 32).color(0xFFB7C5, true).show(heap.sprite,3);
                    }
                    l+=1;
                    if (l >= maxl){
                        trigger(heap);
                    }
                    spend(TICK);
                    return true;
                }
            }
            bomb.fuse = null;
            Actor.remove( this );
            return true;
        }

        @Override
        //first trigger sets the alarm mechanism, second explodes
        protected void trigger(Heap heap) {
            heap.remove(bomb);
            bomb.explode(heap.pos);
            Actor.remove(this);
        }

        private static final String L = "l";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(L, l);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            l = bundle.getInt(L);
        }
        @Override
        public boolean freeze(){
            return false;
        }
    }

}
