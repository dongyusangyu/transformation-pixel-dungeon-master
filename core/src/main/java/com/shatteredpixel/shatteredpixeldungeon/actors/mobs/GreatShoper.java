package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.level;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GreatShoperSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;


public class GreatShoper extends Mob {
    {
        spriteClass = GreatShoperSprite.class;

        HP = HT = 1500;
        EXP = 50;

        //so that allies can attack it. States are never actually used.
        state = HUNTING;
        defenseSkill = 25;

        viewDistance = 12;

        properties.add(Property.BOSS);
        properties.add(Property.GOLD);
    }

    private int phase = 1;

    private float summonCooldown = 10;

    private static final String PHASE = "phase";

    private static final String SUMMON_CD = "summon_cd";

    private static final float TIME_TO_ZAP	= 1f;

    private GoldBoss findGold(){
        for ( Char c : Actor.chars() ){
            if (c instanceof GoldBoss){
                return (GoldBoss) c;
            }
        }
        return null;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 15, 30 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 75;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(5, 15);
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        return (super.canAttack(enemy)
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos) && phase == 1;
    }

    protected boolean doAttack( Char enemy ) {

        if (Dungeon.level.adjacent( pos, enemy.pos )
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

            return super.doAttack( enemy );

        } else {

            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                level.drop( new Gold().quantity(200), enemy.pos ).sprite.drop();
                return false;
            } else {
                zap();
                level.drop( new Gold().quantity(200), enemy.pos );
                return true;
            }
        }
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return phase == 2 || super.isInvulnerable(effect) || phase == 3 ;
    }



    @Override
    public void damage(int dmg, Object src) {
        /*
        if(phase == 2 || phase ==3){
            yell(Messages.get(this, "no_damage"));
            return ;
        }

         */
        if (isInvulnerable(src.getClass())){
            super.damage(dmg, src);
            return;
        }
        dmg=Math.min(150,dmg);
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())) {
            lock.addTime(dmg / 10);
        }
        super.damage(dmg, src);
        if(phase == 1 && HP < 500){
            HP = 500;
            phase = 2;
            yell(Messages.get(this, "summon"));
            sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "no_damage"));
            Buff.affect(this,GoldBarrier.class).incShield(10);
            ScrollOfTeleportation.appear(this, CityBossLevel.throne);
            properties.add(Property.IMMOVABLE);
            sprite.idle();
            GoldBoss m1 = new GoldBoss.GoldElemental();
            m1.pos = CityBossLevel.pedestals[0];
            m1.state= m1.HUNTING;
            GameScene.add( m1 );
            ScrollOfTeleportation.appear( m1, CityBossLevel.pedestals[0] );
            GoldBoss m2 = new GoldBoss.MonkMaster();
            m2.pos = CityBossLevel.pedestals[3];
            m2.state= m2.HUNTING;
            GameScene.add( m2 );
            ScrollOfTeleportation.appear( m2, CityBossLevel.pedestals[3] );

            return ;
        }
    }

    public void Summon(){
        Mob mob = null;
        switch (Random.Int(4)){
            case 0:
                mob = new DwarfKing.DKMonk();
                break;
            case 1:
                mob = new DwarfKing.DKGhoul();
                break;
            case 2:
                mob = new DwarfKing.DKGolem();
                break;
            case 3:
                mob = new DwarfKing.DKWarlock();
                break;
            default:
                mob = new DwarfKing.DKMonk();
                break;
        }
        int pos1 =((CityBossLevel)Dungeon.level).getSummoningPos1();
        if(pos1 == -1){
            return;
        }
        if(Actor.findChar( pos1 ) == null){
            mob.pos = pos1;
            mob.state= mob.HUNTING;
            GameScene.add( mob );
        }else if((Actor.findChar( pos1 ) instanceof Hero)
                || (Actor.findChar( pos1 ) instanceof GoldBoss )
                || (Actor.findChar( pos1 ) instanceof GreatShoper )){

        }else{

            Char c =  Actor.findChar( pos1 );
            c.die(this);
            mob.pos = pos1;
            mob.state= mob.HUNTING;
            GameScene.add( mob );
        }
        Dungeon.observe();

        return;
    }


    @Override
    protected boolean act() {
        if(phase == 1){
            if(summonCooldown >= 10){
                Summon();
                summonCooldown = 0;
            }else{
                summonCooldown ++;
            }
        }else if(phase == 2){
            if(findGold() == null){
                phase = 3;
                yell(Messages.get(this, "summon1"));
                GoldBoss m1 = new GoldBoss.GoldGolem();
                m1.pos = CityBossLevel.pedestals[1];
                m1.state= m1.HUNTING;
                GameScene.add( m1 );
                ScrollOfTeleportation.appear( m1, CityBossLevel.pedestals[1] );

                GoldBoss m2 = new GoldBoss.Thymor();
                m2.pos = CityBossLevel.pedestals[3];
                m2.state= m2.HUNTING;
                GameScene.add( m2 );
                ScrollOfTeleportation.appear( m2, CityBossLevel.pedestals[3] );
            }
        }else if(phase == 3){
            if(findGold() == null){
                phase = 4;
                yell(Messages.get(this, "phase4"));
                properties.remove(Property.IMMOVABLE);
            }
        }
        if (phase == 2 || phase == 3){
            GoldBarrier b = this.buff(GoldBarrier.class);
            if(b == null || b.shielding() < 750){
                for(Heap h: level.heaps.valueList()){
                    for(Item g: h.items){
                        if(g instanceof Gold){
                            sprite.parent.add(new Beam.HealthRay(sprite.destinationCenter(),h.sprite.center()));
                            Buff.affect(this,GoldBarrier.class).incShield(10);
                        }
                    }
                }
            }
        }
        if(phase == 4){
            if(enemy == null){
                enemy = hero;
            }
            dropGold( enemy);
            spend(2 * TICK);
            damage(25,this);
        }

        return super.act();
    }

    protected void zap() {
        spend( TIME_TO_ZAP );

        Invisibility.dispel(this);
        Char enemy = this.enemy;

        if (hit( this, enemy, true )) {
            //TODO would be nice for this to work on ghost/statues too
            if (enemy == hero && Random.Int( 2 ) == 0) {
                Sample.INSTANCE.play( Assets.Sounds.GOLD );
            }
            int dmg = Random.NormalIntRange( 15, 30 );
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));

            //logic for DK taking 1/2 damage from aggression stoned minions
            if ( enemy.buff(StoneOfAggression.Aggression.class) != null
                    && enemy.alignment == alignment
                    && (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))){
                dmg *= 0.5f;
            }
            enemy.damage( dmg, this );

            if (enemy == hero && !enemy.isAlive()) {
                Badges.validateDeathFromEnemyMagic();
                Dungeon.fail( this );
                GLog.n( Messages.get(this, "glod_kill") );
            }
        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }
    }

    public void onZapComplete() {
        zap();
        next();
    }



    public static class GoldBarrier extends Barrier {

        @Override
        public boolean act() {
            incShield();
            return super.act();
        }
        @Override
        public void fx(boolean on) {
            if (on) target.sprite.add(CharSprite.State.SHIELDED);
            else target.sprite.remove(CharSprite.State.SHIELDED);
        }

        @Override
        public int icon() {
            return BuffIndicator.NONE;
        }
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
        bundle.put( PHASE, phase );
        bundle.put( SUMMON_CD, summonCooldown );
        super.storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        BossHealthBar.assignBoss(this);
        phase = bundle.getInt( PHASE );
        summonCooldown = bundle.getFloat( SUMMON_CD );
        if (phase == 2 || phase == 3) properties.add(Property.IMMOVABLE);
        BossHealthBar.assignBoss(this);
        if (phase == 4) BossHealthBar.bleed(true);
    }

    public void dropGold( Char target ) {

        Dungeon.hero.interrupt();
        final int rockCenter;

        //knock back 2 tiles if adjacent
        if (Dungeon.level.adjacent(pos, target.pos)){
            int oppositeAdjacent = target.pos + (target.pos - pos);
            Ballistica trajectory = new Ballistica(target.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(target, trajectory, 2, false, false, this);
            if (target == Dungeon.hero){
                Dungeon.hero.interrupt();
            }
            rockCenter = trajectory.path.get(Math.min(trajectory.dist, 2));

            //knock back 1 tile if there's 1 tile of space
        } else if (Dungeon.level.distance(pos, target.pos) == 2) {
            int oppositeAdjacent = target.pos + (target.pos - pos);
            Ballistica trajectory = new Ballistica(target.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(target, trajectory, 1, false, false, this);
            if (target == Dungeon.hero){
                Dungeon.hero.interrupt();
            }
            rockCenter = trajectory.path.get(Math.min(trajectory.dist, 1));

            //otherwise no knockback
        } else {
            rockCenter = target.pos;
        }

        int safeCell;
        do {
            safeCell = rockCenter + PathFinder.NEIGHBOURS8[Random.Int(8)];
        } while (safeCell == pos
                || (Dungeon.level.solid[safeCell] && Random.Int(2) == 0)
                && Random.Int(2) == 0);

        ArrayList<Integer> rockCells = new ArrayList<>();

        int start = rockCenter - Dungeon.level.width() * 3 - 3;
        int pos;
        for (int y = 0; y < 7; y++) {
            pos = start + Dungeon.level.width() * y;
            for (int x = 0; x < 7; x++) {
                if (!Dungeon.level.insideMap(pos)) {
                    pos++;
                    continue;
                }
                //add rock cell to pos, if it is not solid, and isn't the safecell
                if (!Dungeon.level.solid[pos] && pos != safeCell && Random.Int(Dungeon.level.distance(rockCenter, pos)) == 0) {
                    rockCells.add(pos);
                }
                pos++;
            }
        }
        for (int i : rockCells){
            sprite.parent.add(new TargetedCell(i, 0xFFD700));
        }
        Buff.append(this, FallingGoldBuff.class, 2 * TICK).setRockPositions(rockCells);

    }

    @Override
    public void die(Object cause) {
        Statistics.bossScores[6] +=10000;
        yell(Messages.get(this, "die"));
        Dungeon.level.reunseal();
        GameScene.bossSlain();
        Badges.validateBack();
        Buff b = hero.buff(GoldCurse.class);
        if(b != null){
            b.detach();
        }
        Dungeon.level.drop(new Gold().quantity(114514),pos).sprite.drop(pos);
        //Dungeon.level.drop(new RegionLorePage.Backs(),pos).sprite.drop(pos);
        super.die(cause);
        /*
        GreatImp lastBoss =new  GreatImp();
        lastBoss.pos=pos;
        GameScene.add( lastBoss );

         */
    }

    public static class FallingGoldBuff extends DelayedRockFall {

        @Override
        public void affectChar(Char ch) {
            if (!(ch instanceof GreatShoper)){
                ch.damage(Random.Int(20),new Viscosity.DeferedDamage());
                Buff.prolong(ch, Paralysis.class, 2);
                if (ch == hero && !ch.isAlive()) {
                    Badges.validateDeathFromEnemyMagic();
                    GLog.n( Messages.get(this, "gold_kill") );
                }
            }
        }
        @Override
        public void affectCell( int cell ){
            level.drop( new Gold().quantity(Random.Int(200)), cell ).sprite.drop();
        }

    }

    public  static class GoldCurse extends Buff {
        {
            revivePersists = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.GOLDCURSE;
        }

        @Override
        public boolean act() {
            if(target == hero){
                if(Dungeon.gold>100){
                    Dungeon.gold -= 10;
                }else{
                    target.damage(5,Viscosity.ViscosityTracker.class);
                }
            }else{
                target.damage(5,Viscosity.ViscosityTracker.class);
            }
            spend( TICK );
            return true;
        }
    }

}
