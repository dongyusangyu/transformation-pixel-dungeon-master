package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;


import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RotLasher;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndNinjaAbilities;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Ninja_Energy extends Buff implements ActionIndicator.Action {

    {
        type = Buff.buffType.POSITIVE;
        revivePersists = true;
    }
    public float energy=5f;
    public float partenergy; //currently unused, abilities had cooldowns prior to v2.5

    private static final float MAX_COOLDOWN = 20;

    @Override
    public int icon() {
        return BuffIndicator.THROW_SKILL;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0.3f, 0.3f, 0.3f);
    }

    @Override
    public boolean act() {
        partenergy+=0.025f;
        if(partenergy>0){
            if(energy<10){
                energy = Math.min(10,energy+(int)partenergy);
                partenergy-=(int)partenergy;
            }else{
                partenergy=0;
            }
        }
        ActionIndicator.setAction(this);
        spend(TICK);
        return true;
    }
    public static String ENERGY = "energy";
    public static String PARTENERGY = "partenergy";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ENERGY, energy);
        bundle.put(PARTENERGY, partenergy);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        energy = bundle.getFloat(ENERGY);
        partenergy = bundle.getInt(PARTENERGY);
        if (energy >= 0){
            ActionIndicator.setAction(this);
        }
    }

    @Override
    public String desc() {
        String desc = Messages.get(this, "desc", (int)energy);
        return desc;
    }

    public void gainEnergy(Mob enemy ){
        if (target == null) return;

        if (!Regeneration.regenOn()){
            return; //to prevent farming boss minions
        }
        float GainEnergy=0;
        if(hero.hasTalent(Talent.SOUL_HUNTING)){
            GainEnergy+=0.5f*hero.pointsInTalent(Talent.SOUL_HUNTING);
        }
        partenergy+=GainEnergy;
        if(partenergy>0){
            if(energy<10){
                energy = Math.min(10,energy+(int)partenergy);
                partenergy-=(int)partenergy;
            }else{
                partenergy=0;
            }
        }
        if (energy >= 1){
            ActionIndicator.setAction(this);
        }
        BuffIndicator.refreshHero();
    }

    public void abilityUsed( NinjaAbility abil ){
        energy -= abil.energyCost(this);

        if (energy < 1){
            ActionIndicator.clearAction(this);
        } else {
            ActionIndicator.refresh();
        }
        BuffIndicator.refreshHero();
    }
    @Override
    public String actionName() {
        return Messages.get(this, "action");
    }

    @Override
    public int actionIcon() {
        if(target==null){
            return HeroIcon.NINJA_NORMAL;
        }else{
            if(Dungeon.level.map[target.pos] == Terrain.GRASS || Dungeon.level.map[target.pos] == Terrain.EMBERS
            || Dungeon.level.map[target.pos] == Terrain.HIGH_GRASS || Dungeon.level.map[target.pos] == Terrain.FURROWED_GRASS){
                return HeroIcon.NINJA_GRASS;
            }else if(Dungeon.level.water[target.pos]){
                return HeroIcon.NINJA_WATER;
            }else{
                return HeroIcon.NINJA_NORMAL;
            }

        }

    }
    @Override
    public Visual secondaryVisual() {
        BitmapText txt = new BitmapText(PixelScene.pixelFont);
        txt.text( Integer.toString((int)energy) );
        txt.hardlight(CharSprite.POSITIVE);
        txt.measure();
        return txt;
    }
    @Override
    public int indicatorColor() {
        return 0xFFFFFF;
    }
    @Override
    public void doAction() {
        GameScene.show(new WndNinjaAbilities(this));
    }

    public static abstract class NinjaAbility {
        public static NinjaAbility[] abilities = new NinjaAbility[]{
                new Transported(),
                new Taijutsu(),
                new Throw()
        };
        public String name(Ninja_Energy buff){
            if(buff.target==null){
                return Messages.get(this, "name_normal");
            }else{
                if(Dungeon.level.map[buff.target.pos] == Terrain.GRASS || Dungeon.level.map[buff.target.pos] == Terrain.EMBERS
                        || Dungeon.level.map[buff.target.pos] == Terrain.HIGH_GRASS || Dungeon.level.map[buff.target.pos] == Terrain.FURROWED_GRASS){
                    return Messages.get(this, "name_grass");
                }else if(Dungeon.level.water[buff.target.pos]){
                    return Messages.get(this, "name_water");
                }else{
                    return Messages.get(this, "name_normal");
                }

            }
        }
        public String desc(Ninja_Energy buff){
            if(buff.target==null){
                return Messages.get(this, "desc_normal");
            }
            if(Dungeon.level.map[buff.target.pos] == Terrain.GRASS || Dungeon.level.map[buff.target.pos] == Terrain.EMBERS
                    || Dungeon.level.map[buff.target.pos] == Terrain.HIGH_GRASS || Dungeon.level.map[buff.target.pos] == Terrain.FURROWED_GRASS){
                return Messages.get(this, "desc_grass");
            }else if(Dungeon.level.water[buff.target.pos]){
                return Messages.get(this, "desc_water");
            }else{
                return Messages.get(this, "desc_normal");
            }
        }
        public abstract int energyCost(Ninja_Energy buff);

        public boolean usable(Ninja_Energy buff){
            return buff.energy >= energyCost(buff);
        }

        public String targetingPrompt(){
            return null; //return a string if uses targeting
        }

        public abstract void doAbility(Hero hero, Integer target );
        public static class Transported extends NinjaAbility {
            @Override
            public int energyCost(Ninja_Energy buff) {
                if(buff.target instanceof Hero){
                    if(((Hero)buff.target).pointsInTalent(Talent.MIND_WATER)>2){
                        return 1;
                    }
                }
                return 2;
            }
            @Override
            public boolean usable(Ninja_Energy buff) {
                return super.usable(buff);
            }
            @Override
            public String targetingPrompt() {
                if(Dungeon.level.map[hero.pos] == Terrain.GRASS || Dungeon.level.map[hero.pos] == Terrain.EMBERS
                        || Dungeon.level.map[hero.pos] == Terrain.HIGH_GRASS || Dungeon.level.map[hero.pos] == Terrain.FURROWED_GRASS){
                    return null;
                }else if(Dungeon.level.water[hero.pos]){
                    return null;
                }else{
                    return Messages.get(MeleeWeapon.class, "prompt");
                }

            }
            @Override
            public void doAbility(Hero hero, Integer target) {

                if(Dungeon.level.map[hero.pos] == Terrain.GRASS || Dungeon.level.map[hero.pos] == Terrain.EMBERS
                        || Dungeon.level.map[hero.pos] == Terrain.HIGH_GRASS || Dungeon.level.map[hero.pos] == Terrain.FURROWED_GRASS){
                    //草木复苏
                    ArrayList<Integer> respawnPoints = new ArrayList<>();
                    for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
                        int p = hero.pos + PathFinder.NEIGHBOURS9[i];
                        if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                            respawnPoints.add( p );
                        }
                    }
                    if(respawnPoints.isEmpty()){
                        GLog.w(Messages.get(this,"less"));
                        return;
                    }
                    int spawned = 0;
                    int maxSpawned=1;
                    if(Random.Int(10)<4){
                        maxSpawned++;
                        if(Random.Int(4)<1){
                            maxSpawned++;
                        }
                    }
                    while (spawned < maxSpawned && respawnPoints.size() > 0) {
                        int index = Random.index( respawnPoints );
                        RotLasher mob = new RotLasher();
                        Buff.affect( mob, ScrollOfSirensSong.Enthralled.class);
                        GameScene.add( mob );
                        ScrollOfTeleportation.appear( mob, respawnPoints.get( index ) );
                        respawnPoints.remove( index );
                        spawned++;
                    }
                    Buff.affect(hero,Ninja_Energy.class).abilityUsed(this);
                    if(hero.hasTalent(Talent.USE_ENVIRONMENT)){
                        Use_Environmet();
                    }
                }else if(Dungeon.level.water[hero.pos]){
                    Buff.affect(hero,WaterSmooth.class,8);
                    Buff.affect(hero,Ninja_Energy.class).abilityUsed(this);
                    if(hero.hasTalent(Talent.USE_ENVIRONMENT)){
                        Use_Environmet();
                    }
                }else{
                    if (target == null || target == -1){
                        return;
                    }
                    Ballistica route = new Ballistica(hero.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                    int cell = route.collisionPos;
                    if(cell==hero.pos || (Dungeon.level.distance(hero.pos,cell)<3 && Actor.findChar(cell)==null)){
                        hero.sprite.jump(hero.pos, cell, 0, 0.1f,new Callback() {
                            @Override
                            public void call() {
                                hero.move(cell);
                                Dungeon.level.occupyCell(hero);
                                Dungeon.observe();
                                GameScene.updateFog();
                                hero.spendAndNext(Actor.TICK);
                                Buff.affect(hero, Invisibility.class,5);
                                AttackIndicator.updateState();

                            }
                        });
                        Buff.affect(hero,Ninja_Energy.class).abilityUsed(this);
                        if(hero.hasTalent(Talent.USE_ENVIRONMENT)){
                            Use_Environmet();
                        }
                    }else{
                        GLog.w(Messages.get(this,"more_distance"));
                    }
                }
            }
        }
        public static void Throw_Water(int attacker,int defenser){
            int water = 0;
            for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
                int p = attacker + PathFinder.NEIGHBOURS9[i];
                if(Dungeon.level.water[p]){
                    water++;
                    Dungeon.level.set(p, Terrain.EMPTY);
                    //Dungeon.level.map[p]=Terrain.EMPTY;
                    GameScene.updateMap( p );
                    CellEmitter.get( p ).burst( Speck.factory( Speck.STEAM ), 10 );
                }
            }
            Ninja_Energy ninja_energy=hero.buff(Ninja_Energy.class);
            if(ninja_energy!=null){
                ActionIndicator.setAction(ninja_energy);
            }
            if(water>0){
                for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
                    int p = defenser + PathFinder.NEIGHBOURS9[i];
                    if(!Dungeon.level.water[p] && water>0){
                        water--;
                        Dungeon.level.setCellToWater(true, p);
                        GameScene.updateMap( p );
                    }
                }
            }
        }
        public static void Use_Environmet(){
            PathFinder.buildDistanceMap( hero.pos, BArray.not( Dungeon.level.solid, null ), 1 );
            ArrayList<Blob> blobs = new ArrayList<>();
            for (Class c :new BlobImmunity().immunities()){
            //for (Class c :PotionOfPurity.affectedBlobs){
                Blob b = Dungeon.level.blobs.get(c);
                if (b != null && b.volume > 0){
                    blobs.add(b);
                }
            }
            int fire = 0;
            int ele = 0;
            if(hero.pointsInTalent(Talent.USE_ENVIRONMENT)>1 && hero.buff(Gas_Storage.class)==null){
                Buff.affect(hero,Gas_Storage.class);
            }
            Gas_Storage gas = hero.buff(Gas_Storage.class);
            for (int i=0; i < Dungeon.level.length(); i++) {
                if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                    for (Blob blob : blobs) {
                        if(hero.pointsInTalent(Talent.USE_ENVIRONMENT)>0 && blob instanceof Fire && blob.get(i)){
                            blob.clear(i);
                            fire++;
                        }
                        if(hero.pointsInTalent(Talent.USE_ENVIRONMENT)>2 && blob instanceof Electricity && blob.get(i)){
                            blob.clear(i);
                            ele++;
                        }
                        if(gas!=null && !(blob instanceof Electricity) && !(blob instanceof Fire) && blob.get(i)){
                            blob.clear(i);
                            if(!gas.blobs.containsKey(blob.getClass())){
                                gas.blobs.put(blob.getClass(),Reflection.newInstance(blob.getClass()));
                            }

                        }
                    }
                }
            }
            if(fire>0){
                Buff.affect(hero, FireImbue.class).set(3);
            }
            if(ele>0){
                Buff.affect(hero, Recharging.class,ele);
                Buff.affect(hero, ArtifactRecharge.class).extend(ele);
            }
            if(gas!=null && gas.blobs.isEmpty()){
                gas.detach();
            }

        }

        public static class Taijutsu extends NinjaAbility {
            @Override
            public String targetingPrompt() {
                return Messages.get(MeleeWeapon.class, "prompt");
            }

            @Override
            public int energyCost(Ninja_Energy buff) {
                if(buff.target instanceof Hero){
                    if(((Hero)buff.target).pointsInTalent(Talent.MIND_WATER)>0){
                        return 1;
                    }
                }
                return 2;
            }
            @Override
            public boolean usable(Ninja_Energy buff) {
                return super.usable(buff);
            }
            @Override
            public void doAbility(Hero hero, Integer target) {
                if (target == null || target == -1){
                    return;
                }
                Char enemy = Actor.findChar(target);
                if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
                    GLog.w(Messages.get(MeleeWeapon.class, "ability_no_target"));
                    return;
                }

                if(Dungeon.level.map[hero.pos] == Terrain.GRASS || Dungeon.level.map[hero.pos] == Terrain.EMBERS
                        || Dungeon.level.map[hero.pos] == Terrain.HIGH_GRASS || Dungeon.level.map[hero.pos] == Terrain.FURROWED_GRASS){
                    if (!hero.canAttack(enemy)){
                        GLog.w(Messages.get(MeleeWeapon.class, "ability_target_range"));
                        return;
                    }
                    hero.sprite.attack(enemy.pos, new Callback() {
                        @Override
                        public void call() {
                            AttackIndicator.target(enemy);
                            hero.attack(enemy, 1.25f, 0, Char.INFINITE_ACCURACY);
                            Invisibility.dispel();
                            hero.next();
                            hero.spend(1f);
                        }
                    });
                    if(enemy.isAlive()){
                        Buff.affect(enemy, Roots.class,5);
                    }
                    Buff.affect(hero,Ninja_Energy.class).abilityUsed(this);
                }else if(Dungeon.level.water[hero.pos]){
                    if(hero.belongings.weapon==null){
                        if(Dungeon.level.distance(hero.pos,enemy.pos)>2){
                            GLog.w(Messages.get(MeleeWeapon.class, "ability_target_range"));
                            return;
                        }
                        hero.sprite.attack(enemy.pos, new Callback() {
                            @Override
                            public void call() {
                                AttackIndicator.target(enemy);
                                hero.attack(enemy, 1f, 0, Char.INFINITE_ACCURACY);
                                Invisibility.dispel();
                                hero.next();
                                hero.spend(1f);
                            }
                        });
                        if(enemy.isAlive()){
                            Buff.affect(enemy, Chill.class,5);
                        }
                    }else{
                        if(Dungeon.level.distance(hero.pos,enemy.pos)>hero.belongings.weapon.reachFactor(hero)+1){
                            GLog.w(Messages.get(MeleeWeapon.class, "ability_target_range"));
                            return;
                        }
                        hero.sprite.attack(enemy.pos, new Callback() {
                            @Override
                            public void call() {
                                AttackIndicator.target(enemy);
                                hero.attack(enemy, 1f, 0, Char.INFINITE_ACCURACY);
                                Invisibility.dispel();
                                hero.next();
                                hero.spend(1f);
                            }
                        });
                        if(enemy.isAlive()){
                            Buff.affect(enemy, Chill.class,5);
                        }
                    }
                    Buff.affect(hero,Ninja_Energy.class).abilityUsed(this);
                }else{
                    if (!hero.canAttack(enemy)){
                        GLog.w(Messages.get(MeleeWeapon.class, "ability_target_range"));
                        return;
                    }
                    hero.sprite.attack(enemy.pos, new Callback() {
                        @Override
                        public void call() {
                            AttackIndicator.target(enemy);
                            hero.attack(enemy, 0.8f, 0, Char.INFINITE_ACCURACY);
                            if (enemy.isAlive()){
                                hero.sprite.attack(enemy.pos, new Callback() {
                                    @Override
                                    public void call() {
                                        hero.attack(enemy, .8f, 0, Char.INFINITE_ACCURACY);
                                        Invisibility.dispel();
                                        hero.next();
                                    }
                                });
                            } else {
                                Invisibility.dispel();
                                hero.next();

                            }
                            hero.spend(1f);
                        }
                    });
                    if(enemy.isAlive()){
                        Buff.affect(enemy, Vertigo.class,5);
                    }
                    Buff.affect(hero, Ninja_Energy.class).abilityUsed(this);

                }
            }
        }

        public static class Throw extends NinjaAbility {


            @Override
            public int energyCost(Ninja_Energy buff) {
                if(buff.target instanceof Hero){
                    if(((Hero)buff.target).pointsInTalent(Talent.MIND_WATER)>1){
                        return 1;
                    }
                }
                return 2;
            }
            @Override
            public boolean usable(Ninja_Energy buff) {
                return super.usable(buff);
            }
            @Override
            public void doAbility(Hero hero, Integer target) {
                if(hero.buff(Throw_Skill.class)==null){
                    Buff.affect(hero,Throw_Skill.class);
                    Buff.affect(hero, Ninja_Energy.class).abilityUsed(this);
                }else{
                    GLog.w(Messages.get(this,"hive_throw"));
                }

            }
        }

    }

    public static class WaterSmooth extends FlavourBuff {
        public int icon() { return BuffIndicator.WATER_SMOOTH; }
        @Override
        public float iconFadePercent() {
            return Math.max(0, (10 - visualcooldown()) / 10);
        }
    };

    public static class Throw_Skill extends Buff {

        {
            type = buffType.POSITIVE;
        }
        @Override
        public int icon() {
            return BuffIndicator.THROW_SKILL;
        }
        @Override
        public String iconTextDisplay() {
            return Integer.toString(left);
        }
        @Override
        public String desc() {
            return Messages.get(this, "desc");
        }
        public int left;
        public void set(int shots){
            left = Math.max(left, shots);
        }
    }

    public static class Gas_Storage extends Buff {

        {
            type = buffType.POSITIVE;
        }
        public HashMap<Class<? extends Blob>,Blob> blobs=new HashMap<Class<? extends Blob>,Blob>();
        public void tintIcon(Image icon) { icon.hardlight(0f, 0.6f, 0f); }
        @Override
        public int icon() {
            return BuffIndicator.DAZE;
        }
        @Override
        public String iconTextDisplay() {
            return Integer.toString(left);
        }
        @Override
        public String desc() {
            return Messages.get(this, "desc");
        }
        public int left;
        public static String BLOBS = "blobs";
        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(BLOBS,blobs.values());
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            Collection<Bundlable> collection = bundle.getCollection( BLOBS );
            for (Bundlable b : collection) {
                Blob blob = (Blob)b;
                blobs.put( blob.getClass(), blob );
            }
        }
    }
}

