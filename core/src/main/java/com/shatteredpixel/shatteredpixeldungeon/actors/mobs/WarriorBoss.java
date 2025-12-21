package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS;
import static com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS_MINION;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Resurrection;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.SealShard;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarriorBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WarriorBoss extends Mob {
    {
        HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 120 : 80;
        EXP = 10;
        defenseSkill = 8;
        spriteClass = WarriorBossSprite.class;
        properties.add(Property.BOSS);
        SLEEPING     = new Sleeping();
        state = SLEEPING;
    }
    public int healInc = 6;
    private float leapCooldown = 5;
    private int leapPos = -1;
    private int lastEnemyPos = -1;

    private static final String LAST_ENEMY_POS = "last_enemy_pos";
    private static final String LEAP_POS = "leap_pos";
    private static final String LEAP_CD = "leap_cd";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LAST_ENEMY_POS, lastEnemyPos);
        bundle.put(LEAP_POS, leapPos);
        bundle.put(LEAP_CD, leapCooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        lastEnemyPos = bundle.getInt(LAST_ENEMY_POS);
        leapPos = bundle.getInt(LEAP_POS);
        leapCooldown = bundle.getFloat(LEAP_CD);
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 2);
    }

    @Override
    public int attackSkill( Char target ) {
        int attack = 15;
        return attack;
    }

    @Override
    public int damageRoll() {
        int min = 1;
        int max = 10;
        int mult = 1;
        if(!buffs(Berserk.class).isEmpty()){
            mult *= buff(Berserk.class).damageFactor(1f);
        }
        return Random.NormalIntRange( min, max )* mult;
    }

    @Override
    public void damage(int dmg, Object src) {
        if (dmg > 0 ){
            Berserk berserk = Buff.affect(this, Berserk.class);
            berserk.damage(dmg);
        }
        if (!BossHealthBar.isAssigned()){
            BossHealthBar.assignBoss( this );
            Dungeon.level.seal();
        }

        boolean bleeding = (HP*5 <= HT);
        super.damage(dmg, src);
        if ((HP*5 <= HT) && !bleeding){
            BossHealthBar.bleed(true);
        }
        LockedFloor lock = hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())){
            if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(dmg);
            else                                                    lock.addTime(dmg*1.5f);
        }
    }



    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );
        Combo combo = Buff.affect(this, Combo.class);
        combo.hit(enemy);
        return damage;
    }
    @Override
    public int defenseSkill( Char enemy ) {
        if (!buffs(ParryTracker.class).isEmpty()){
            buff(ParryTracker.class).detach();
            yell(Messages.get(this,"def"));
            return INFINITE_EVASION;
        }
        return super.defenseSkill(enemy );
    }

    @Override
    public boolean act() {
        if(buffs(Paralysis.class).isEmpty()){
            Heap h = Dungeon.level.heaps.get(pos);
            if(h != null  && h.type!= Heap.Type.CHEST && h.type!= Heap.Type.SKELETON){
                for (Item item : h.items) {
                    if(item instanceof Food){
                        h.items.remove(item);
                        sprite.operate( pos );
                        SpellSprite.show( this, SpellSprite.FOOD );
                        Sample.INSTANCE.play( Assets.Sounds.EAT );
                        int heal = 0;
                        heal = Math.min(HT-HP,healInc);
                        HP += heal;
                        if (Dungeon.level.heroFOV[pos] ){
                            sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING );
                        }
                        yell(Messages.get(this,"eat"));
                        if (h.isEmpty()) {
                            h.destroy();
                        } else if (h.sprite != null) {
                            h.sprite.view(h).place( h.pos );
                        }
                        if(Dungeon.isChallenged(Challenges.STRONGER_BOSSES) && Dungeon.isChallenged(Challenges.EXTREME_ENVIRONMENT)){
                            Buff.affect(this, Invulnerability.class,1);
                            spend(TICK);
                        }else{
                            spend(3*TICK);
                        }
                        return true;
                    }
                }
            }
        }
        Combo b = buff(Combo.class);
        int combo = 3;
        int shield = HT/8;
        if(Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
            combo -= 1;
            if(Dungeon.isChallenged(Challenges.EXTREME_ENVIRONMENT)){
                shield += 5;
            }
        }
        if(b != null && b.count>combo){
            b.detach();
            Buff.affect(this, ParryTracker.class,3);
            Buff.affect(this, Barrier.class).incShield(shield);
        }
        if(enemy != null && state!= SLEEPING){
            leapCooldown++;
        }
        if(leapCooldown > 8 && enemy !=null && lastEnemyPos ==-1 && (HP<20 || Dungeon.level.distance(pos,enemy.pos)>1) && state!= SLEEPING){
            lastEnemyPos = enemy.pos;
            yell(Messages.get(this,"wantjump"));
            sprite.parent.add(new TargetedCell(enemy.pos, 0xFF0000));
            spend(TICK);
            return true;
        }
        if(leapCooldown > 9 && lastEnemyPos !=-1 && enemy!=null && state!= SLEEPING){
            //检查视野里是否有食物
            lastEnemyPos = enemy.pos;
            boolean food = false;
            for(int cell=0;cell<fieldOfView.length;cell++){
                if(fieldOfView[cell]){
                    Heap h = Dungeon.level.heaps.get(cell);
                    if(h != null && h.type!= Heap.Type.CHEST && h.type!= Heap.Type.SKELETON){
                        for (Item item : h.items) {
                            if(item instanceof Food){
                                //改变跳跃目标
                                leapPos = cell;
                                food = true;
                                break;
                            }
                        }
                    }
                }
                if(food){
                    break;
                }
            }
            if(food){
                yell(Messages.get(this,"wantfood"));
            }else{
                int wantleap = -1;
                for (int i : PathFinder.NEIGHBOURS8){
                    int p = i+lastEnemyPos;
                    if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                        wantleap=p;
                        break;
                    }
                }
                if(wantleap != -1){
                    leapPos = wantleap;
                }else{
                    leapPos = lastEnemyPos;
                }
                yell(Messages.get(this,"leap"));
            }
            leap(leapPos);
            leapPos = -1;
            lastEnemyPos =-1;
            leapCooldown = 0;
            return true;
        }else if(leapCooldown > 9 && lastEnemyPos !=-1){
            leapPos = -1;
            lastEnemyPos =-1;
        }
        return super.act();
    }
    @Override
    public void die( Object cause ) {


        super.die( cause );
        if(!Statistics.subLimation[0]){
            Dungeon.level.drop( new ScrollOfSublimation().type("WARRIOR"), pos ).sprite.drop();
            Statistics.subLimation[0] = true;
        }
        Dungeon.level.unseal();

        GameScene.bossSlain();
        Dungeon.level.drop( new SkeletonKey( Dungeon.depth ), pos ).sprite.drop();

        Dungeon.level.drop( new SealShard(), pos ).sprite.drop( pos );
        int blobs = Random.chances(new float[]{0, 6, 3, 1});
        for (int i = 0; i < blobs; i++){
            int ofs;
            do {
                ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
            } while (!Dungeon.level.passable[pos + ofs]);
            Dungeon.level.drop( new Food(), pos + ofs ).sprite.drop( pos );
        }

        Badges.validateHeroBossSlain();
        /*
        if (Statistics.qualifiedForBossChallengeBadge){
            Badges.validateBossChallengeCompleted();
        }

         */
        Statistics.bossScores[0] += 1000;

        yell( Messages.get(this, "defeated") );
    }
    @Override
    public void notice() {
        super.notice();
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            yell(Messages.get(this, "wake"));
            Dungeon.level.seal();
            for (Char ch : Actor.chars()){
                if (ch instanceof DriedRose.GhostHero){
                    ((DriedRose.GhostHero) ch).sayBoss();
                }
            }
        }
    }

    public void leap(Integer target ) {
        if (target != null) {

            if (this.rooted){
                return;
            }

            Ballistica route = new Ballistica(this.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
            int cell = route.collisionPos;

            //can't occupy the same cell as another char, so move back one.
            int backTrace = route.dist-1;
            while (Actor.findChar( cell ) != null && cell != this.pos) {
                cell = route.path.get(backTrace);
                backTrace--;
            }

            final int dest = cell;
            sprite.jump(pos, dest, new Callback() {
                @Override
                public void call() {

                    WarriorBoss.this.move(dest, false );
                    WarriorBoss.this.sprite.interruptMotion();
                    Dungeon.observe();
                    for (int i : PathFinder.NEIGHBOURS8) {
                        Char mob = Actor.findChar(WarriorBoss.this.pos + i);
                        if (mob != null && mob != WarriorBoss.this && mob.alignment != Alignment.ENEMY) {
                            int damage = 5;
                            damage += WarriorBoss.this.damageRoll()*2;
                            damage -= mob.drRoll();
                            mob.damage(damage, WarriorBoss.this);
                            if (mob == hero && !mob.isAlive()) {
                                Dungeon.fail( this );
                                GLog.n( Messages.get(WarriorBoss.this, "kill") );
                            }
                            if(mob == hero){
                                Statistics.bossScores[0] -= 100;
                            }
                            if (mob.pos == WarriorBoss.this.pos + i){
                                Ballistica trajectory = new Ballistica(mob.pos, mob.pos + i, Ballistica.MAGIC_BOLT);
                                int strength = 5;
                                WandOfBlastWave.throwChar(mob, trajectory, strength, true, true, WarriorBoss.this);
                                if (mob == Dungeon.hero){
                                    Dungeon.hero.interrupt();
                                }
                            }

                        }
                    }
                    WarriorBoss.this.sprite.idle();
                    next();
                    Buff.affect(WarriorBoss.this, Paralysis.class,3);
                    WandOfBlastWave.BlastWave.blast(dest);
                    PixelScene.shake(2, 0.5f);

                    Invisibility.dispel();

                }
            });
        }
    }





    public static class ParryTracker extends FlavourBuff {
        { actPriority = HERO_PRIO+1;}

        public boolean parried;

        @Override
        public void detach() {
            if (!parried && target.buff(Combo.class) != null) target.buff(Combo.class).detach();
            super.detach();
        }
    }


    public static class Berserk extends Buff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }
        public float power = 0;

        public float damageFactor(float dmg){
            return dmg * Math.min(2f, 1f + power);
        }

        @Override
        public int icon() {
            return BuffIndicator.BERSERK;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(1f, 0f, 0f);
        }

        public void damage(int damage){
            float maxPower = 1f;
            power = Math.min(maxPower, power + (damage/(float)target.HT)*2f );
        }

        @Override
        public String desc() {
            float dispDamage = ((int)damageFactor(10000) / 100f) - 100f;
            return Messages.get(this, "angered_desc", Math.floor(power * 100f), dispDamage);

        }
        @Override
        public boolean act() {
            float powerLast = Math.max(0.02f,power/10);
            if(Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
                if(Dungeon.isChallenged(Challenges.HARSH_ENVIRONMENT)){
                    powerLast /= 2f;
                }else{
                    powerLast *= 0.75f;
                }
            }
            power -= powerLast;
            if (power <= 0) {
                detach();
            }
            spend(TICK);
            return true;
        }

        private static final String POWER = "power";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(POWER, power);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            power = bundle.getFloat(POWER);
        }
    }

    public static class Combo extends Buff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }
        public int count = 0;
        private float comboTime = 0f;
        private float initialComboTime = 5f;

        @Override
        public int icon() {
            return BuffIndicator.COMBO;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (initialComboTime - comboTime)/ initialComboTime);
        }
        public void hit( Char enemy ) {
            count++;
            comboTime = 5f;
            initialComboTime = comboTime;

        }
        @Override
        public boolean act() {
            comboTime-=TICK;
            spend(TICK);
            if (comboTime <= 0) {
                detach();
            }
            return true;
        }
        @Override
        public String desc() {
            return Messages.get(this, "desc", count, dispTurns(comboTime));
        }

        private static final String COMBO = "combo";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(COMBO, count);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            count = bundle.getInt(COMBO);
        }
    }








}
