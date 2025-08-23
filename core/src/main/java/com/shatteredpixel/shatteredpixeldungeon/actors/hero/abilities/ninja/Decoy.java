package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RotLasher;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Decoy extends ArmorAbility {
    @Override
    public boolean useTargeting(){
        return false;
    }

    {
        baseChargeUse = 50f;
    }

    @Override
    public String targetingPrompt() {
        if (getDecoymanAlly() == null) {
            return super.targetingPrompt();
        } else {
            return Messages.get(this, "prompt");
        }
    }
    @Override
    public float chargeUse(Hero hero) {
        if (getDecoymanAlly() == null) {
            return super.chargeUse(hero);
        } else {
            return 0;
        }
    }
    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        ArrayList<Decoyman> ally = getDecoymanAlly();

        if (ally != null){
            if (target == null){
                return;
            } else {
                for(Decoyman d:ally){
                    d.directTocell(target);
                }
            }
        } else {
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
            armor.charge -= chargeUse(hero);
            Talent.onArmorAbility(hero, chargeUse(hero));
            armor.updateQuickslot();
            int spawned = 0;
            int maxSpawned=1;
            if(hero.hasTalent(Talent.DEVERSION)){
                maxSpawned+=(int)(hero.pointsInTalent(Talent.DEVERSION)/2);
            }
            while (spawned < maxSpawned && respawnPoints.size() > 0) {
                int index = Random.index( respawnPoints );
                Decoyman mob = new Decoyman();
                GameScene.add(mob);
                Decoyman.appear( mob, respawnPoints.get( index ) );
                respawnPoints.remove( index );
                spawned++;
            }
            Invisibility.dispel();
            hero.spendAndNext(Actor.TICK);
        }

    }

    @Override
    public int icon() {
        return HeroIcon.DECOY;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.DEVERSION, Talent.SHINKAGE, Talent.ALLHUNTING, Talent.HEROIC_ENERGY};
    }
    public static ArrayList<Decoyman> getDecoymanAlly(){
        ArrayList<Decoyman> decoymen = new ArrayList<Decoyman>();
        for (Char ch : Actor.chars()){
            if(ch instanceof Decoyman){
                decoymen.add((Decoyman) ch);
            }
        }
        if(decoymen.isEmpty()){
            return null;
        }else{
            return decoymen;
        }
    }


    public static class Decoyman extends DirectableAlly {

        {
            spriteClass = DecoymanSprite.class;

            HP = HT = 40;

            immunities.add(AllyBuff.class);

            properties.add(Property.INORGANIC);
        }

        public Decoyman( ){
            super();
            if(hero!=null){
                int hpBonus = 10*((int)((1+hero.pointsInTalent(Talent.DEVERSION))/2));
                if (hpBonus > 0){
                    HT += hpBonus;
                    HP += hpBonus;
                }
            }
            defenseSkill = 30; //equal to base hero defense skill
        }

        @Override
        protected boolean act() {
            int oldPos = pos;
            boolean result = super.act();
            //partially simulates how the hero switches to idle animation
            if ((pos == target || oldPos == pos) && sprite.looping()){
                sprite.idle();
            }
            return result;
        }

        @Override
        public void defendPos(int cell) {
            yell(Messages.get(this, "direct_defend"));
            super.defendPos(cell);
        }

        @Override
        public void followHero() {
            yell(Messages.get(this, "direct_follow"));
            super.followHero();
        }

        @Override
        public void targetChar(Char ch) {
            yell(Messages.get(this, "direct_attack"));
            super.targetChar(ch);
        }

        @Override
        public int attackSkill(Char target) {
            return defenseSkill+5; //equal to base hero attack skill
        }

        @Override
        public int damageRoll() {
            int damage = Random.NormalIntRange(10, 15);
            damage += 10*((int)((1+hero.pointsInTalent(Talent.DEVERSION))/2));
            return damage;
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            damage = super.attackProc( enemy, damage );
            if(hero.hasTalent(Talent.ALLHUNTING)){
                Buff.affect(enemy, ShadowMark.class,2*hero.pointsInTalent(Talent.ALLHUNTING));
            }
            if(enemy.buff(ShadowMark.class)!=null && hero.hasTalent(Talent.ALLHUNTING)){
                damage=(int)(damage*(1+0.15*hero.pointsInTalent(Talent.ALLHUNTING)));
            }
            return damage;
        }
        @Override
        public void die(Object src) {
            switch (Random.Int(3)){
                case 0: default:
                    yell(Messages.get(this, "die1"));
                    break;
                case 1:
                    yell(Messages.get(this, "die2"));
                    break;
                case 2:
                    yell(Messages.get(this, "die3"));
                    break;
                case 3:
                    yell(Messages.get(this, "die4"));
                    break;
            }
            super.die(src);
        }


        @Override
        public int drRoll() {
            int dr = super.drRoll();
            dr += Random.NormalIntRange(1, 5)*((int)((1+hero.pointsInTalent(Talent.DEVERSION))/2));
            return dr;
        }

        @Override
        public boolean isImmune(Class effect) {
            if (effect == Burning.class
                    && Random.Int(4) < hero.pointsInTalent(Talent.CLONED_ARMOR)
                    && hero.belongings.armor() != null
                    && hero.belongings.armor().hasGlyph(Brimstone.class, this)){
                return true;
            }
            return super.isImmune(effect);
        }

        @Override
        public int defenseProc(Char enemy, int damage) {
            damage = super.defenseProc(enemy, damage);
            if(hero.pointsInTalent(Talent.SHINKAGE)>1){
                Buff.affect(hero, Adrenaline.class,0.5f+0.5f*(int)(hero.pointsInTalent(Talent.SHINKAGE)/2));
            }
            return damage;
        }

        @Override
        public void damage(int dmg, Object src) {

            //TODO improve this when I have proper damage source logic


            super.damage(dmg, src);
        }

        @Override
        public float speed() {
            float speed = super.speed();

            //moves 2 tiles at a time when returning to the hero
            if (state == WANDERING
                    && defendingPos == -1
                    && Dungeon.level.distance(pos, hero.pos) > 1){
                speed *= 2;
            }

            return speed;
        }

        @Override
        public boolean canInteract(Char c) {
            return super.canInteract(c);
        }

        @Override
        public boolean interact(Char c) {
            return super.interact(c);
        }

        public static void appear( Char ch, int pos ) {

            ch.sprite.interruptMotion();

            if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[ch.pos]){
                Sample.INSTANCE.play(Assets.Sounds.PUFF);
            }

            ch.move( pos );
            if (ch.pos == pos) ch.sprite.place( pos );

            if (Dungeon.level.heroFOV[pos] || ch == hero ) {
                ch.sprite.emitter().burst(SmokeParticle.FACTORY, 10);
            }
        }

        private static final String DEF_SKILL = "def_skill";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(DEF_SKILL, defenseSkill);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            defenseSkill = bundle.getInt(DEF_SKILL);
        }
    }

    public static class DecoymanSprite extends MobSprite {

        public  DecoymanSprite() {
            super();

            texture( Assets.Sprites.DECOY );

            TextureFilm film = new TextureFilm( texture, 12, 15 );

            idle = new Animation( 1, true );
            idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

            run = new Animation( 12, true );
            run.frames( film, 2, 3, 4, 5, 6, 7 );

            die = new Animation( 20, false );
            die.frames( film, 0,11,12,13,14 );

            attack = new Animation( 15, false );
            attack.frames( film, 8, 9,  10 );

            zap = attack.clone();
            play( idle );
        }
        @Override
        public void link( Char ch ) {
            super.link( ch );
            renderShadow = false;

        }
    }


    public static class ShadowMark extends FlavourBuff {
        public int icon() { return BuffIndicator.DECOY; }
    };

}
