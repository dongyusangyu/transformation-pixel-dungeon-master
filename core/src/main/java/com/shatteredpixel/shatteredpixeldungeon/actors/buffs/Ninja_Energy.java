package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;


import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RipperDemon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RotLasher;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMonkAbilities;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Ninja_Energy extends Buff implements ActionIndicator.Action {

    {
        type = Buff.buffType.POSITIVE;
        revivePersists = true;
    }
    public float energy;
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
        partenergy+=0.05f;
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
                    int spawned = 0;
                    int maxSpawned=Random.Int(1,4);
                    while (spawned < maxSpawned && respawnPoints.size() > 0) {
                        int index = Random.index( respawnPoints );
                        RotLasher mob = new RotLasher();
                        AllyBuff.affectAndLoot(mob, hero, ScrollOfSirensSong.Enthralled.class);
                        GameScene.add( mob );
                        ScrollOfTeleportation.appear( mob, respawnPoints.get( index ) );
                        respawnPoints.remove( index );
                        spawned++;
                    }
                }else if(Dungeon.level.water[hero.pos]){

                }else{

                }
            }


        }




    }

    public static class WaterSmooth extends FlavourBuff {
        public int icon() { return BuffIndicator.WATER_SMOOTH; }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 5); }
    };





}

