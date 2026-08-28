package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
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
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpiderJar extends ArmorAbility {

    {
        baseChargeUse = 25f;
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
        final boolean[] resolved = {false};
        hero.busy();
        ((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).
                reset(hero.sprite,
                        cell,
                        b,
                        new Callback() {
                            @Override
                            public void call() {
                                if (resolved[0]) return;
                                resolved[0] = true;
                                b.onThrow(cell);
                                armor.charge -= chargeUse(hero);
                                Talent.onArmorAbility(hero, chargeUse(hero));
                                armor.updateQuickslot();
                                Invisibility.dispel();
                                hero.spendAndNext(Actor.TICK);
                            }
                        });
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
            if (!isValidCell(cell)) {
                return;
            }
            if (!Dungeon.level.pit[ cell ] && lightingFuse) {
                armAt(cell);
            }else{
                super.onThrow( cell );
            }

        }
        public void armAt(int cell) {
            if (!isValidCell(cell)) return;
            if (fuse != null && Actor.all().contains(fuse)) return;
            fuse = createFuse().ignite(this, cell);
            Actor.add(fuse);
            Dungeon.level.drop(this, cell);
        }
        @Override
        protected int explosionRange(){
            Hero hero = Dungeon.hero;
            if(hero != null && hero.hasTalent(Talent.POWER_GUNPOWDER)){
                return 1+(int)(hero.pointsInTalent(Talent.POWER_GUNPOWDER)/2);
            }else{
                return 1;
            }
        }
        public void explode(int cell){
            if (!isValidCell(cell)) {
                fuse = null;
                return;
            }
            Hero hero = Dungeon.hero;
            if(this.fuse != null && Actor.all().contains(this.fuse)){
                Actor.remove(this.fuse);
            }

            //We're blowing up, so no need for a fuse anymore.
            this.fuse = null;

            Sample.INSTANCE.play( Assets.Sounds.BLAST );
            ArrayList<Integer> affectedCells = new ArrayList<>();
            if (explodesDestructively()) {

                ArrayList<Char> affected = new ArrayList<>();

                if (isVisibleCell(cell)) {
                    CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
                }

                boolean terrainAffected = false;
                boolean[] explodable = new boolean[Dungeon.level.length()];
                BArray.not( Dungeon.level.solid, explodable);
                BArray.or( Dungeon.level.flamable, explodable, explodable);
                PathFinder.buildDistanceMap( cell, explodable, explosionRange() );
                for (int i = 0; i < PathFinder.distance.length; i++) {
                    if (PathFinder.distance[i] != Integer.MAX_VALUE) {
                        affectedCells.add(i);
                        Char ch = Actor.findChar(i);
                        if (ch != null) {
                            affected.add(ch);
                        }
                    }
                }
                for (int i : affectedCells){
                    if (isVisibleCell(i)) {
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
                }

                for (Char ch : affected){
                    //if they have already been killed by another bomb
                    if(!ch.isAlive()){
                        continue;
                    }
                    int m=20;
                    int M=40;
                    if(hero != null && hero.hasTalent(Talent.POWER_GUNPOWDER)){
                        m+=(int)((1+hero.pointsInTalent(Talent.POWER_GUNPOWDER))/2)*5;
                        M+=(int)((1+hero.pointsInTalent(Talent.POWER_GUNPOWDER))/2)*15;
                    }
                    int dmg = Random.NormalIntRange(m, M);
                    dmg = Bomb.damageWithBombTalents(hero, dmg);
                    if(ch instanceof Hero){
                        dmg*=1.5f;
                    }
                    dmg -= ch.drRoll();

                    if (dmg > 0) {
                        ch.damage(dmg, this, DamageTag.PHYSICAL);
                    }
                    if(hero != null && hero.hasTalent(Talent.TEA_STAINS) && ch.isAlive()){
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
                    if (ch.isAlive()) Bomb.applyShockBomb(hero, ch);

                    if (ch == hero && !ch.isAlive()) {
                        GLog.n(Messages.get(this, "ondeath"));
                        Dungeon.fail(this);
                    }
                }
                for (int p = 0; p < PathFinder.NEIGHBOURS9.length; p++) {
                    int i = cell + PathFinder.NEIGHBOURS9[p];
                    if (i < 0 || i >= Dungeon.level.map.length) continue;
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
                if(hero != null && hero.hasTalent(Talent.FIREWORK)){
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
            if (!isValidCell(cell)) {
                return;
            }
            if (!Dungeon.level.pit[ cell ] && lightingFuse) {
                armAt(cell);
            }else{
                super.onThrow( cell );
            }
        }


        @Override
        protected int explosionRange(){
            return 1;
        }
        public void explode(int cell){
            if (!isValidCell(cell)) {
                fuse = null;
                return;
            }
            Hero hero = Dungeon.hero;
            if(Actor.all().contains(this.fuse)){
                Actor.remove(this.fuse);
            }
            //We're blowing up, so no need for a fuse anymore.
            this.fuse = null;
            Sample.INSTANCE.play( Assets.Sounds.BLAST );
            if (explodesDestructively()) {
                ArrayList<Char> affected = new ArrayList<>();

                if (isVisibleCell(cell)) {
                    CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
                }

                boolean terrainAffected = false;
                boolean[] explodable = new boolean[Dungeon.level.length()];
                BArray.not( Dungeon.level.solid, explodable);
                BArray.or( Dungeon.level.flamable, explodable, explodable);
                PathFinder.buildDistanceMap( cell, explodable, explosionRange() );
                for (int i = 0; i < PathFinder.distance.length; i++) {
                    if (PathFinder.distance[i] != Integer.MAX_VALUE) {
                        if (isVisibleCell(i)) {
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
                    if(hero != null && hero.hasTalent(Talent.FIREWORK)){
                        if(hero.pointsInTalent(Talent.FIREWORK)>1){
                            M=10;
                        }
                        m+=5+(int)((hero.pointsInTalent(Talent.FIREWORK))/2)*5;
                        M+=(int)((hero.pointsInTalent(Talent.FIREWORK))/2)*10;
                    }
                    int dmg = Random.NormalIntRange(m, M);
                    dmg = Bomb.damageWithBombTalents(hero, dmg);
                    if(ch instanceof Hero){
                        dmg*=1.5f;
                    }
                    dmg -= ch.drRoll();

                    if (dmg > 0) {
                        ch.damage(dmg, this, DamageTag.PHYSICAL);
                    }
                    if (ch.isAlive()) Bomb.applyShockBomb(hero, ch);

                    if (ch == hero && !ch.isAlive()) {
                        GLog.n(Messages.get(SpiderJar.Spider.class, "ondeath"));
                        Dungeon.fail(SpiderJar.Spider.class);
                    }
                }
                for (int p = 0; p < PathFinder.NEIGHBOURS9.length; p++) {
                    int i = cell + PathFinder.NEIGHBOURS9[p];
                    if (i < 0 || i >= Dungeon.level.map.length) continue;
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


    private static boolean isValidCell(int cell) {
        return Dungeon.level != null && cell >= 0 && cell < Dungeon.level.map.length;
    }

    private static boolean isVisibleCell(int cell) {
        return isValidCell(cell)
                && ShatteredPixelDungeon.scene() instanceof GameScene
                && Dungeon.level.heroFOV[cell];
    }

    public static class SpiderFuse extends Bomb.Fuse {
        public int l=0;
        private int cell = -1;
        private static final String CELL = "cell";

        @Override
        public SpiderFuse ignite(Bomb bomb){
            super.ignite(bomb);
            return this;
        }

        public SpiderFuse ignite(Bomb bomb, int cell){
            super.ignite(bomb);
            this.cell = cell;
            return this;
        }

        @Override
        protected boolean act() {
            if (bomb == null || bomb.fuse != this){
                snuff();
                return true;
            }

            Heap heap = findHeap();
            if (heap != null) {
                int maxl=3;
                Hero hero = Dungeon.hero;
                if(this.bomb instanceof SmallSpider){
                    maxl = 10;
                    if(hero != null && hero.pointsInTalent(Talent.FIREWORK)>2){
                        maxl=15;
                    }
                    if (heap.sprite != null && heap.sprite.parent != null) {
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
                    }
                    if(Math.floorMod(l,3)==1 && isVisibleCell(heap.pos)) {
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
                if(!(this.bomb instanceof SmallSpider)
                        && heap.sprite != null && heap.sprite.parent != null){
                    new Flare(6, 32).color(0xFFB7C5, true).show(heap.sprite,3);
                }
                l+=1;
                if (l >= maxl){
                    trigger(heap);
                    return true;
                }
                spend(TICK);
                return true;
            }
            bomb.fuse = null;
            snuff();
            return true;
        }

        private Heap findHeap() {
            if (Dungeon.level == null) return null;
            if (isValidCell(cell)) {
                Heap heap = Dungeon.level.heaps.get(cell);
                if (heap != null && heap.items.contains(bomb)) return heap;
            }
            for (Heap heap : Dungeon.level.heaps.valueList()) {
                if (heap.items.contains(bomb)) {
                    cell = heap.pos;
                    return heap;
                }
            }
            return null;
        }

        @Override
        //first trigger sets the alarm mechanism, second explodes
        protected void trigger(Heap heap) {
            if (bomb == null) {
                snuff();
                return;
            }
            int explosionCell = heap.pos;
            heap.remove(bomb);
            bomb.fuse = null;
            snuff();
            bomb.explode(explosionCell);
        }

        private static final String L = "l";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(L, l);
            bundle.put(CELL, cell);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            l = bundle.getInt(L);
            if (bundle.contains(CELL)) cell = bundle.getInt(CELL);
        }
        @Override
        public boolean freeze(){
            return false;
        }
    }

}
