package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blooming;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Chilling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.CorrosionEnchanted;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Corrupting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Sweeping;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Unstable;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vampiric;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class RuneMark extends Buff {
    public HashMap<Class<? extends Weapon.Enchantment>, Weapon.Enchantment> enchantments=new HashMap<Class<? extends Weapon.Enchantment>, Weapon.Enchantment>();
    public static String ENCHANTMENTS = "enchantments";
    public static String TIMES = "times";
    @Override
    public boolean attachTo( Char target ) {
        return super.attachTo( target );

    }
    public Wand lastWand;
    public int times=2;
    @Override
    public int icon() {
        return BuffIndicator.RUNEMARK;
    }
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ENCHANTMENTS,enchantments.values());
        bundle.put(TIMES,times);

    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        Collection<Bundlable> collection = bundle.getCollection( ENCHANTMENTS );
        for (Bundlable e : collection) {
            Weapon.Enchantment blob = (Weapon.Enchantment)e;
            enchantments.put( blob.getClass(), blob );
        }
        times = bundle.getInt(TIMES);

    }

    @Override
    public String desc() {
        String d=Messages.get(this, "desc");
        for (Weapon.Enchantment e : enchantments.values()){
            d+="\n_"+e.name()+"_";
        }
        return d;
    }

    public void addEnchantment(Weapon.Enchantment enchantment){
        if(!enchantment.curse()){
            enchantments.put(enchantment.getClass(), enchantment);
        }
    }
    //to do 将英雄的武器附魔添加到这个标记上
    public void addEnchantments(Hero attacker){

    }

    public void explore(Wand wand){
        int explosionRange=1;
        if(hero.pointsInTalent(Talent.RUNE_BLAST)>2){
            explosionRange=2;
        }
        int cell = target.pos;
        int num = enchantments.size();
        int s=0;
        ArrayList<Integer> affectedCells = new ArrayList<>();
        ArrayList<Char> affectedChars = new ArrayList<>();

        if (ShatteredPixelDungeon.scene() instanceof GameScene && Dungeon.level.heroFOV[cell] ) {
            CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
        }

        boolean terrainAffected = false;
        boolean[] explodable = new boolean[Dungeon.level.length()];
        BArray.not( Dungeon.level.solid, explodable);
        BArray.or( Dungeon.level.flamable, explodable, explodable);
        PathFinder.buildDistanceMap( cell, explodable, explosionRange );
        int unable=-1;
        if (enchantments.containsKey(Unstable.class)){
            unable= Random.Int(14);
        }
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] != Integer.MAX_VALUE) {
                affectedCells.add(i);
                Char ch = Actor.findChar(i);
                if (ch != null && ch.alignment != Char.Alignment.ALLY) {
                    affectedChars.add(ch);
                }
            }
        }

        for (int i : affectedCells){
            Char ch = Actor.findChar(i);
            if(enchantments.containsKey(Shocking.class) && ch !=null && ch.alignment != Char.Alignment.ALLY ){
                GameScene.add( Blob.seed( i, 3, Electricity.class ) );
            }
            if(unable==0){
                GameScene.add( Blob.seed( i, 3, Electricity.class ) );
            }
        }
        int sheild=0;
        for (Char ch : affectedChars){
            if(enchantments.containsKey(Blazing.class) && ch.alignment != Char.Alignment.ALLY ){
                Buff.affect(ch, Burning.class).reignite(ch, 8f);
            }
            if(unable==1 && ch.alignment != Char.Alignment.ALLY ){
                Buff.affect(ch, Burning.class).reignite(ch, 8f);
            }
            if(enchantments.containsKey(Blocking.class) && ch.alignment != Char.Alignment.ALLY ){
                sheild+=6;
            }
            if(unable==2 && ch.alignment != Char.Alignment.ALLY ){
                sheild+=6;
            }
            if(enchantments.containsKey(Blooming.class) && ch.alignment != Char.Alignment.ALLY ){
                Buff.affect(ch, Roots.class,3);
            }
            if(unable==3&& ch.alignment != Char.Alignment.ALLY ){
                Buff.affect(ch, Roots.class,3);
            }
            if(enchantments.containsKey(Chilling.class) && ch.alignment != Char.Alignment.ALLY ){
                Buff.affect(ch, Chill.class,10);
            }
            if(unable==4 && ch.alignment != Char.Alignment.ALLY ){
                Buff.affect(ch, Chill.class,10);
            }
            if(enchantments.containsKey(Corrupting.class) && ch.alignment != Char.Alignment.ALLY && num>Random.Int(20)){
                Buff.affect(ch, Doom.class);
            }
            if(unable==6 && num>Random.Int(20) && ch.alignment != Char.Alignment.ALLY ){
                Buff.affect(ch, Doom.class);
            }

            if(enchantments.containsKey(Elastic.class) && ch.alignment != Char.Alignment.ALLY){
                int dis =2;
                if(ch.properties().contains(Char.Property.BOSS)){
                    dis = 1;
                }
                Ballistica trajectory = new Ballistica(cell, ch.pos, Ballistica.STOP_TARGET);
                //trim it to just be the part that goes past them
                trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
                //knock them back along that ballistica
                WandOfBlastWave.throwChar(ch,
                        trajectory,
                        dis,
                        false,
                        true,
                        this);
            }
            if(unable==7 && ch.alignment != Char.Alignment.ALLY ){
                int dis =2;
                if(ch.properties().contains(Char.Property.BOSS)){
                    dis = 1;
                }
                Ballistica trajectory = new Ballistica(cell, ch.pos, Ballistica.STOP_TARGET);
                //trim it to just be the part that goes past them
                trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
                //knock them back along that ballistica
                WandOfBlastWave.throwChar(ch,
                        trajectory,
                        dis,
                        false,
                        true,
                        this);
            }
            if(enchantments.containsKey(Lucky.class) && ch.alignment != Char.Alignment.ALLY && num>Random.Int(10)){
                Buff.affect(ch, Lucky.LuckProc.class).ringLevel = num;
            }
            if(unable==9 && ch.alignment != Char.Alignment.ALLY && num>Random.Int(10)){
                Buff.affect(ch, Lucky.LuckProc.class).ringLevel = num;
            }
            if(enchantments.containsKey(Projecting.class) && ch.alignment != Char.Alignment.ALLY){
                Buff.append(hero, TalismanOfForesight.CharAwareness.class, 10).charID = ch.id();
            }
            if(unable==10 && ch.alignment != Char.Alignment.ALLY){
                Buff.append(hero, TalismanOfForesight.CharAwareness.class, 10).charID = ch.id();
            }
            if(enchantments.containsKey(Vampiric.class) && ch.alignment != Char.Alignment.ALLY && num>Random.Int(10)){
                hero.heal(6);
            }
            if(unable==11 && ch.alignment != Char.Alignment.ALLY && num>Random.Int(10)){
                hero.heal(6);
            }
            if(enchantments.containsKey(Sweeping.class) && ch.alignment != Char.Alignment.ALLY){
                ch.damage(num+5,hero);
            }
            if(unable==12 && ch.alignment != Char.Alignment.ALLY){
                ch.damage(num+5,hero);
            }
            if(enchantments.containsKey(CorrosionEnchanted.class) && ch.alignment != Char.Alignment.ALLY){
                Buff.affect(ch, Corrosion.class).set(num*2f, num, null);
            }
            if(unable==13 && ch.alignment != Char.Alignment.ALLY){
                Buff.affect(ch, Corrosion.class).set(num*2f, num, null);
            }
            if(enchantments.containsKey(Grim.class) && ch.alignment != Char.Alignment.ALLY){
                Buff.affect(ch, Grim.GrimTracker.class).maxChance = (0.5f+0.1f*num);
            }
            if(unable==8 && ch.alignment != Char.Alignment.ALLY){
                Buff.affect(ch, Grim.GrimTracker.class).maxChance = (0.5f+0.1f*num);
            }
            int dmg = num*5;
            if(hero.pointsInTalent(Talent.RUNE_BLAST)>1 && lastWand!=null && wand!=lastWand){
                dmg += 10;
            }
            ch.damage(dmg, HolyWeapon.INSTANCE);

            if(enchantments.containsKey(Kinetic.class) && !ch.isAlive()){
                s+=6;
            }
            if(unable==5 && !ch.isAlive()){
                s+=6;
            }
        }
        if(sheild>0){
            Buff.affect(hero,Barrier.class).setShield(sheild);
        }
        if(s>0){
            Buff.affect(hero,Kinetic.ConservedDamage.class).setBonus(s);
        }
        if(hero.pointsInTalent(Talent.RUNE_BLAST)>0){
            int wands = hero.belongings.charge( 0.25f*num );
            if (wands > 0) {
                hero.sprite.centerEmitter().burst(EnergyParticle.FACTORY, 10);
            }
        }
        if(hero.hasTalent(Talent.RUNE_SURGE)){
            Buff.affect(hero, RuneSurge.class, 5+5*hero.pointsInTalent(Talent.RUNE_SURGE));
        }
        if(hero.pointsInTalent(Talent.RUNE_BLAST)>1){
            times--;
            if(times<=0){
                detach();
            }
            lastWand = wand;
        }else{
            detach();
        }

    }

    @Override
    public void fx(boolean on) {
        if (on) {
            target.sprite.add(CharSprite.State.RUNEMARK);
        } else{
            target.sprite.remove(CharSprite.State.RUNEMARK);
        }
    }

    public static class RuneSurge extends FlavourBuff {
        public static final float DURATION	= 5f;
        @Override
        public int icon() {
            return BuffIndicator.UPGRADE;
        }
        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.537f, 0.0f, 1.0f);
        }
        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }
    }



}
