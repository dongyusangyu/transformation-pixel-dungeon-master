package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Pheromone;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.GreatImp;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.journal.RegionLorePage;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GreatDemonSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarlockSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GreatDemon extends Mob{

    {
        spriteClass = GreatDemonSprite.class;
        flying = true;

        HP = HT = 1500;
        baseSpeed = 2f;
        EXP = 50;

        //so that allies can attack it. States are never actually used.
        state = HUNTING;
        defenseSkill = 25;

        viewDistance = 60;

        properties.add(Property.BOSS);
        properties.add(Property.EVIL);
        properties.add(Property.DEMONIC);
        //properties.add(Property.STATIC);

    }
    @Override
    public float attackDelay() {
        return super.attackDelay()*0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 25, 70 );
    }
    @Override
    public int attackSkill( Char target ) {
        return 50;
    }
    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(5, 15);
    }
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );
        if(buffs(ThrowCooldown.class).isEmpty()){
            damage +=13;
            Buff c= enemy.buff(PotionOfCleansing.Cleanse.class);
            if(c!=null){
                c.detach();
            }
            Buff.affect(this, ThrowCooldown.class,50);
            Buff.affect(enemy, Weakness.class,5);
            yell(Messages.get(this, "throw"));
            spend(TICK);
        }else{
            Buff.affect(enemy, Vulnerable.class,2);
            yell(Messages.get(this, "melee"));
        }

        Buff.affect(this, Adrenaline.class,5);

        return damage;
    }
    @Override
    protected boolean canAttack( Char enemy ) {
        return  super.canAttack(enemy)
                || (new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos && buffs(ThrowCooldown.class).isEmpty());
    }


    @Override
    public void damage(int dmg, Object src) {
        if(!buffs(MeleesDefence.class).isEmpty() && src==hero && (hero.belongings.attackingWeapon() instanceof MeleeWeapon)){
            hero.damage((int)(dmg*0.5), new Electricity());
            dmg=(int)(dmg*0.8);
        }
        if(!buffs(MissilesDefence.class).isEmpty() && src==hero && (hero.belongings.attackingWeapon() instanceof MissileWeapon)){
            hero.damage((int)(dmg*0.5), new Electricity());
            dmg=0;
        }
        dmg=Math.min(150,dmg);
        if (Random.Int( 3 ) == 0 && dmg>5) {
            int i;
            do {
                i = Random.Int(Dungeon.level.length());
            } while (Dungeon.level.heroFOV[i]
                    || Dungeon.level.solid[i]
                    || Actor.findChar(i) != null
                    || PathFinder.getStep(i, Dungeon.level.exit(), Dungeon.level.passable) == -1);
            ScrollOfTeleportation.appear(this, i);
        }
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())){
           lock.addTime(dmg/5);
        }
        super.damage(dmg, src);
    }
    @Override
    protected boolean act() {


        if(buffs(SummonCooldown.class).isEmpty()){
            Buff.affect(this, SummonCooldown.class,30);
            yell(Messages.get(this, "summon"));
            ArrayList<Integer> respawnPoints = new ArrayList<>();
            for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
                int p = pos + PathFinder.NEIGHBOURS9[i];
                if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                    respawnPoints.add( p );
                }
            }

            int spawned = 0;
            while (spawned < 2 && respawnPoints.size() > 0) {
                int index1 = Random.index( respawnPoints );
                Mob mob=null;
                switch (Random.Int(4)){
                    case 0:
                        mob = new Succubus();
                        break;
                    case 1:
                        mob = new Scorpio();
                        break;
                    case 2:
                        mob = new Eye();
                    case 3:
                        mob = new RipperDemon();
                }
                if(mob!=null){
                    mob.state= mob.HUNTING;
                    GameScene.add( mob );
                    ScrollOfTeleportation.appear( mob, respawnPoints.get( index1 ) );
                    respawnPoints.remove( index1 );
                    spawned++;
                }
            }

        }


        if(buffs(MagicCooldown.class).isEmpty()){
            Buff.affect(this, MagicImmune.class,6);
            Buff.affect(this,MagicCooldown.class,12);
            yell(Messages.get(this, "magic"));
        }

        if(buffs(MeleesCooldown.class).isEmpty()){
            Buff.affect(this, MeleesDefence.class,6);
            Buff.affect(this,MeleesCooldown.class,12);
            yell(Messages.get(this, "defence"));
        }
        if(buffs(MissilesCooldown.class).isEmpty()){
            Buff.affect(this, MissilesDefence.class,6);
            Buff.affect(this,MissilesCooldown.class,12);
            yell(Messages.get(this, "missiles"));
        }
        if(buffs(HasteCooldown.class).isEmpty()){
            Buff.affect(this, Haste.class,3);
            Buff.affect(this,HasteCooldown.class,20);
            GLog.i(Messages.get(this, "haste"));
        }
        //GameScene.add(Blob.seed(pos, 50, Pheromone.class));
        if (hero.invisible <= 0 && state == WANDERING){
            beckon(hero.pos);
            state = HUNTING;
            enemy = hero;
        }

        return super.act();
    }
    public static class HasteCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(1f, 0.8f, 0f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
    };
    public static class SummonCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 30); }
    };



    public static class MagicCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(0f, 1f, 0f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 12); }
    };
    public static class MissilesCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(0f, 0f, 1f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 12); }
    };
    public static class MeleesCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(1f, 0f, 0f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 12); }
    };
    public static class ThrowCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(1f, 0f, 1f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
    };
    public  static class MeleesDefence extends FlavourBuff {

        @Override
        public int icon() {
            return BuffIndicator.MELEESDEFENCE;
        }
        

        @Override
        public float iconFadePercent() {
            return Math.max(0, (6 - visualcooldown()) / 6);
        }

    }
    public  static class MissilesDefence extends FlavourBuff {

        @Override
        public int icon() {
            return BuffIndicator.MISSILESDEFENCE;
        }


        @Override
        public float iconFadePercent() {
            return Math.max(0, (6 - visualcooldown()) / 6);
        }

    }

    @Override
    public void die(Object cause) {
        Statistics.bossScores[5] +=10000;
        yell(Messages.get(this, "die"));
        Dungeon.level.reunseal();
        GameScene.bossSlain();
        Badges.validateBack();
        //Dungeon.level.drop(new RegionLorePage.Backs(),pos).sprite.drop(pos);
        super.die(cause);
        /*
        GreatImp lastBoss =new  GreatImp();
        lastBoss.pos=pos;
        GameScene.add( lastBoss );

         */
    }
    @Override
    public void notice() {
        super.notice();



        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);


        }
    }
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                Music.INSTANCE.play(Assets.Music.BACKS1_2, true);
            }
        });
        BossHealthBar.assignBoss(this);

    }
}
