/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS;
import static com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS_MINION;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Resurrection;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonNewBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.LaboratoryRoom;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RogueBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class RogueBoss extends Mob {

    {
        HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 250 : 200;
        EXP = 20;
        defenseSkill = 15;//待改进：此处数值仍待设计
        spriteClass = RogueBossSprite.class;

        properties.add(Property.BOSS);
        properties.add(Property.DEMONIC);
        properties.add(Property.ACIDIC);
    }

    private int phase = 0;
    private int InvisibilityCoolDown = 0;

    @Override
    public int damageRoll() {
        //待改进：此处伤害仍待设计
        int min = 6;
        int max = (HP*2 <= HT) ? 10 : 12;
        int dmg = Random.NormalIntRange( min, max );
        if(!this.buffs(Invisibility.class).isEmpty()){
            dmg *= 2;
        }
        if(this.buff(MomentumTime.class)!=null && this.buff(MomentumTime.class).left>0){
            //若有逸动加30%伤害
            dmg = (int) (dmg*1.3f);
        }
        return dmg;
    }

    @Override
    public int attackSkill( Char target ) {
        //待改进：此处命中数值仍待设计
        int attack = 10;
        if (HP*2 <= HT) attack = 15;
        if (this.buff(MomentumTime.class)!=null && this.buff(MomentumTime.class).left>0){
            //若有逸动加5命中
            attack += 5;
        }
        return attack;
    }

    @Override
    public int defenseSkill(Char enemy) {
        //待改进：此处数值仍待设计
        int defense = super.defenseSkill(enemy);
        if (HP*2 <= HT) {
            defense = (int) (defense * 1.5);
        }
        if (this.buff(MomentumTime.class)!=null && this.buff(MomentumTime.class).left>0){
            //若有逸动加5闪避
            defense += 5;
        }
        return defense;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 2);
    }

    @Override
    public boolean act() {
        if (!BossHealthBar.isAssigned()){
            BossHealthBar.assignBoss( this );
        }
        //GLog.p("隐身冷却"+InvisibilityCoolDown);
        if (state != HUNTING){
            InvisibilityCoolDown = 0;
            sprite.idle();
        }
        if(enemy != null && state!= SLEEPING){
            if (this.buffs(AbsoluteInvisibility.class).isEmpty() && this.buffs(Paralysis.class).isEmpty()) {
                InvisibilityCoolDown++;
            } else if (!this.buffs(AbsoluteInvisibility.class).isEmpty() && HP > HT/2) {
                this.buff(AbsoluteInvisibility.class).InvisibilityAttack++;
            }
        }
        if(InvisibilityCoolDown >= 8){
            //8回合隐身16回合
            Buff.affect(this, AbsoluteInvisibility.class, 16f);
            if(TargetHealthIndicator.instance !=null){
                TargetHealthIndicator.instance.target(null);
            }
            InvisibilityCoolDown = 0;
            yell( Messages.get(this, "invisibility") );
            for (int i : PathFinder.NEIGHBOURS8) {
                if (Dungeon.level.map[pos + i] == Terrain.EMPTY && Dungeon.level.passable[pos + i]
                        && Actor.findChar(pos + i) == null) {
                    move( pos + i, true );
                    break;
                }
            }
        }
        if (!this.buffs(AbsoluteInvisibility.class).isEmpty() && this.buff(AbsoluteInvisibility.class).InvisibilityAttack >= 10){
            //隐身10回合斩杀玩家
            if (enemy!=null) {
                for (int i : PathFinder.NEIGHBOURS8) {
                    //寻找玩家身边安全位置
                    if (Dungeon.level.map[pos + i] == Terrain.EMPTY && Dungeon.level.passable[pos + i]
                            && Actor.findChar(pos + i) == null) {
                        execute(enemy.pos + i);
                        break;
                    }
                }
            }
            return true;
        }



        if (state != SLEEPING){
            Dungeon.level.seal();
        }

        return super.act();
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        if (HP > HT/2) {
            if (!this.buffs(AbsoluteInvisibility.class).isEmpty()) {
                return false;
            }
            return super.canAttack(enemy);
        }else {
            if (distance(enemy) <=2){
                return false;
            }else{
                return super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
            }
        }
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );
        if (this.buff(MomentumTime.class)!=null && this.buff(MomentumTime.class).left<=0){
            damage = (int) (damage*1.33f);
        }
        return damage;
    }

    @Override
    public int defenseProc( Char enemy, int damage ) {
        if (!this.buffs(AbsoluteInvisibility.class).isEmpty()){
            //隐身受击破隐并麻痹
            AbsoluteInvisibility.dispel(this);
            Buff.affect(this, Paralysis.class, 3f);
        }
        MomentumTime momentum = this.buff(MomentumTime.class);
        if (momentum!=null){
            momentum.minus(5);
            if (momentum.left<=0 && momentum.recovery==10){
                yell( Messages.get(this, "momentum-recovery") );
            }
        }
        return super.defenseProc(enemy, damage);
    }

    @Override
    protected boolean getCloser( int target ) {
        if(HP <= HT/2){
            return super.getFurther( target );
        }else if (!this.buffs(AbsoluteInvisibility.class).isEmpty()){
            return super.getFurther( target );
        }
        return super.getCloser( target );
    }

    @Override
    public void damage(int dmg, Object src) {
        if (!BossHealthBar.isAssigned()){
            BossHealthBar.assignBoss( this );
            Dungeon.level.seal();
        }

        boolean bleeding = (HP*2 <= HT);

        dmg = Math.min(25,dmg);
        super.damage(dmg, src);

        if (phase == 0 && HP<100) {
            //转阶段锁血
            HP = 100;
            phase = 1;
        }
        if ((HP <= HT/2) && !bleeding){
            BossHealthBar.bleed(true);
            sprite.showStatus(CharSprite.WARNING, Messages.get(this, "enraged"));
            yell( Messages.get(this, "angered") );
            Buff.affect(this, MomentumTime.class);
            yell( Messages.get(this, "momentum") );
        }
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())){
            if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(dmg);
            else                                                    lock.addTime(dmg*1.5f);
        }

        //phase 2 of the fight is over
        if (HP == 0) {
            //let full attack action complete first
            Actor.add(new Actor() {

                {
                    actPriority = VFX_PRIO;
                }

                @Override
                protected boolean act() {
                    Actor.remove(this);
                    ((PrisonNewBossLevel)Dungeon.level).onWon();
                    return true;
                }
            });
            return;
        }
    }

    @Override
    public float speed() {
        float speed=super.speed();
        if (this.buff(MomentumTime.class)!=null && this.buff(MomentumTime.class).left>0) speed*=2;
        return speed;
    }

    @Override
    public void die( Object cause ) {

        if(((PrisonNewBossLevel)Dungeon.level).state==PrisonNewBossLevel.State.WON){
            if(!Statistics.subLimation[1]){
                Statistics.subLimation[1] = true;
                Dungeon.level.drop( new ScrollOfSublimation().type("TENGU"), pos ).sprite.drop();
            }
        }

        if(hero.buff(BLiness.class)!=null){
            hero.buff(BLiness.class).detach();
        }

        super.die( cause );

        Dungeon.level.drop( new TengusMask(), pos).sprite.drop();
        //Dungeon.level.drop( new IronKey(10), pos).sprite.drop();
        Dungeon.level.unseal();

        GameScene.bossSlain();

        Badges.validateBossSlain();
        if (Statistics.qualifiedForBossChallengeBadge){
            Badges.validateBossChallengeCompleted();
        }
        Statistics.bossScores[1] += 2000;

        yell( Messages.get(this, "defeated") );
    }

    @Override
    public void notice() {
        super.notice();
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            Dungeon.level.seal();
            yell(Messages.get(this, "notice"));
            for (Char ch : Actor.chars()){
                if (ch instanceof DriedRose.GhostHero){
                    ((DriedRose.GhostHero) ch).sayBoss();
                }
            }
        }
    }

    public void execute(Integer target) {
        //传送至玩家身边
        ScrollOfTeleportation.appear(this, target);
        int damage = (int) (enemy.HT*0.9);
        enemy.damage(damage, RogueBoss.this);
        yell( Messages.get(this, "execute") );
        if (enemy == hero && !enemy.isAlive()) {
            Dungeon.fail( this );
            if (Dungeon.hero.heroClass == HeroClass.NINJA){
                GLog.n( Messages.capitalize(Messages.get(RogueBoss.class, "executeninja")) );
            }
            GLog.n( Messages.get(RogueBoss.this, "spkill") );
        }
        if(enemy == hero){
            //何意味的代码
            Statistics.bossScores[0] -= 100;
        }
        RogueBoss.this.sprite.idle();
        PixelScene.shake(2, 0.5f);
        AbsoluteInvisibility.dispel(this);
    }
    private final String PHASE = "phrase";

    private final String INVIS_CD = "invisibilitycooldown";

    @Override
    public void storeInBundle( Bundle bundle ) {

        super.storeInBundle( bundle );
        bundle.put( INVIS_CD, InvisibilityCoolDown );
        bundle.put( PHASE, phase );

    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {

        super.restoreFromBundle( bundle );
        if (state != SLEEPING) BossHealthBar.assignBoss(this);
        if ((HP*2 <= HT)) BossHealthBar.bleed(true);

        InvisibilityCoolDown = bundle.getInt(INVIS_CD);
        phase = bundle.getInt(PHASE);


    }

    public static class AbsoluteInvisibility extends Invisibility{

        public static final float DURATION = 16f;
        public float InvisibilityAttack = 1;


        public static void dispel() {
            if (Dungeon.hero == null) return;
            dispel(Dungeon.hero);
        }

        @Override
        public int icon() {
            if (target.HP > target.HT/2) {
                return BuffIndicator.PREPARATION;
            }else{
                return BuffIndicator.INVISIBLE;
            }
        }

        @Override
        public void tintIcon(Image icon) {
            if (target.HP > target.HT/2) {
                if (InvisibilityAttack<=2)      icon.hardlight(0f, 1f, 0f);
                else if (InvisibilityAttack<=4) icon.hardlight(1f, 1f, 0f);
                else if (InvisibilityAttack<=8) icon.hardlight(1f, 0.6f, 0f);
                else if (InvisibilityAttack<=9) icon.hardlight(1f, 0f, 0f);
            } else {
                icon.brightness(0.6f);
            }
        }

        @Override
        public float iconFadePercent() {
            if (target.HP > target.HT/2) {
                return 0f;
            } else {
                return super.iconFadePercent();
            }
        }

        @Override
        public String name() {
            if (target.HP > target.HT/2) {
                return Messages.get(this, "spname");
            } else {
                return Messages.get(this, "name");
            }
        }

        @Override
        public String desc() {
            if (target.HP > target.HT/2) {
                return Messages.get(this, "spdesc", InvisibilityAttack, InvisibilityAttack);
            } else {
                return Messages.get(this, "desc", visualcooldown());
            }
        }

        private final String INVIS_ATTACK = "invisibilityattack";

        @Override
        public void storeInBundle( Bundle bundle ) {

            super.storeInBundle( bundle );
            bundle.put( INVIS_ATTACK, InvisibilityAttack );

        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {

            super.restoreFromBundle( bundle );
            InvisibilityAttack = bundle.getInt(INVIS_ATTACK);

        }
    }

    public static class MomentumTime extends Buff {

        private static final float STEP = 1f;
        public float left = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 30 : 20;
        public float recovery = 10;

        public static final float DURATION	= Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 30f : 20f;

        @Override
        public boolean act() {
            if (left<=0){
                recovery--;
                if (recovery == 0){
                    left = 20f;
                    recovery = 10f;
                }
            }
            if (target.buffs(AbsoluteInvisibility.class).isEmpty() && left>0){
                left--;
            }
            spend( STEP );
            return true;
        }
        public void minus(int time) {
            left = Math.max(0, left-time);
        }

        @Override
        public int icon() {
            return BuffIndicator.MOMENTUM;
        }

        @Override
        public void tintIcon(Image icon) {
            if (left>0){
                icon.hardlight(1,1,0);
            } else {
                icon.hardlight(0.5f,0.5f,1);
            }
        }

        @Override
        public float iconFadePercent() {
            if (left>0){
                return ((DURATION-left) / DURATION);
            } else {
                return super.iconFadePercent();
            }
        }

        @Override
        public String name() {
            if (left>0){
                return Messages.get(this, "name");
            }else{
                return Messages.get(this, "spname");
            }
        }
        @Override
        public String desc() {
            if (left>0){
                return Messages.get(this, "desc", left);
            }else{
                return Messages.get(this, "spdesc", recovery);
            }
        }

        private static final String LEFT = "left";
        private static final String RECOVERY = "recovery";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( LEFT, left );
            bundle.put( RECOVERY, recovery );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            left = bundle.getInt( LEFT );
            recovery = bundle.getInt( RECOVERY );

        }
    }

    public static class BLiness extends Buff {
        @Override
        public int icon() {
            return BuffIndicator.NONE;
        }

    }



}
