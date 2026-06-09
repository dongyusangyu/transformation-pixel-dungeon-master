package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldBoss;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GreatShoper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DelayedRockFall;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GreatShoperSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TestGreatShoper extends Mob {

    private int phase = 1;
    private float summonCooldown = 10;

    private static final String PHASE = "phase";
    private static final String SUMMON_CD = "summon_cd";
    private static final float TIME_TO_ZAP = 1f;

    {
        spriteClass = GreatShoperSprite.class;

        HP = HT = 1500;
        EXP = 50;
        state = WANDERING;
        defenseSkill = 25;
        viewDistance = 12;

        properties.add(Property.BOSS);
        properties.add(Property.GOLD);
    }

    @Override
    protected Char chooseEnemy() {
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(15, 30);
    }

    @Override
    public int attackSkill(Char target) {
        return 75;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(5, 15);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return phase == 1 && (super.canAttack(enemy)
                || new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos);
    }

    @Override
    protected boolean doAttack(Char enemy) {
        if (Dungeon.level.adjacent(pos, enemy.pos)
                || new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {
            return super.doAttack(enemy);
        } else if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
            sprite.zap(enemy.pos);
            Dungeon.level.drop(new Gold().quantity(200), enemy.pos).sprite.drop();
            return false;
        } else {
            zap();
            Dungeon.level.drop(new Gold().quantity(200), enemy.pos);
            return true;
        }
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return phase == 2 || phase == 3 || super.isInvulnerable(effect);
    }

    @Override
    public void damage(int dmg, Object src) {
        Char attacker = TestBossUtil.attackerToRetarget(this, src);
        if (attacker != null) {
            enemy = attacker;
            state = HUNTING;
            beckon(attacker.pos);
        }
        if (isInvulnerable(src.getClass())) {
            super.damage(dmg, src);
            return;
        }
        dmg = Math.min(150, dmg);
        int preHP = HP;
        super.damage(dmg, src);
        int dmgTaken = preHP - HP;
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (dmgTaken > 0 && lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())) {
            lock.addTime(dmgTaken * 0.33f);
        }
        if (phase == 1 && HP < 500 && isAlive()) {
            HP = 500;
            phase = 2;
            yell(Messages.get(this, "summon"));
            if (sprite != null) {
                sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "no_damage"));
            }
            Buff.affect(this, GreatShoper.GoldBarrier.class).incShield(10);
            properties.add(Property.IMMOVABLE);
            summonGoldBoss(GoldBoss.GoldElemental.class);
            summonGoldBoss(GoldBoss.MonkMaster.class);
        }
    }

    @Override
    protected boolean act() {
        TestBossUtil.assignBoss(this);
        if (!TestBossUtil.hasVisibleAttackableEnemy(this)) {
            clearEnemy();
            if (Char.hasProp(this, Property.IMMOVABLE)) {
                spend(TICK);
                return true;
            }
            return super.act();
        }
        if (phase == 1) {
            if (summonCooldown >= 10) {
                summonSubject();
                summonCooldown = 0;
            } else {
                summonCooldown++;
            }
        } else if (phase == 2) {
            if (findGold() == null) {
                phase = 3;
                yell(Messages.get(this, "summon1"));
                summonGoldBoss(GoldBoss.GoldGolem.class);
                summonGoldBoss(GoldBoss.Thymor.class);
            }
        } else if (phase == 3) {
            if (findGold() == null) {
                phase = 4;
                yell(Messages.get(this, "phase4"));
                properties.remove(Property.IMMOVABLE);
                BossHealthBar.bleed(true);
            }
        }

        if (phase == 2 || phase == 3) {
            GreatShoper.GoldBarrier b = buff(GreatShoper.GoldBarrier.class);
            if (sprite != null && (b == null || b.shielding() < 750)) {
                for (Heap h : Dungeon.level.heaps.valueList()) {
                    for (Item g : h.items) {
                        if (g instanceof Gold) {
                            sprite.parent.add(new Beam.HealthRay(sprite.destinationCenter(), h.sprite.center()));
                            Buff.affect(this, GreatShoper.GoldBarrier.class).incShield(10);
                        }
                    }
                }
            }
        }
        if (phase == 4) {
            Char target = TestBossUtil.firstEnemy(this);
            if (target != null) {
                dropGold(target);
            }
            spend(2 * TICK);
            damage(25, this);
            return true;
        }
        return super.act();
    }

    private GoldBoss findGold() {
        for (Char c : Actor.chars()) {
            if (c instanceof GoldBoss && c.alignment == alignment && c.isAlive()) {
                return (GoldBoss)c;
            }
        }
        return null;
    }

    private void summonGoldBoss(Class<? extends GoldBoss> type) {
        Mob mob = TestBossUtil.summonNear(this, type, pos, 2, 6);
        if (mob != null) {
            ScrollOfTeleportation.appear(mob, mob.pos);
        }
    }

    private void summonSubject() {
        TestBossUtil.summonNear(this, randomSubject(), pos, 1, 5);
        Dungeon.observe();
    }

    private Class<? extends Mob> randomSubject() {
        switch (Random.Int(4)) {
            case 0:
            default:
                return DwarfKing.DKMonk.class;
            case 1:
                return DwarfKing.DKGhoul.class;
            case 2:
                return DwarfKing.DKGolem.class;
            case 3:
                return DwarfKing.DKWarlock.class;
        }
    }

    protected void zap() {
        spend(TIME_TO_ZAP);
        Invisibility.dispel(this);
        Char enemy = this.enemy;
        if (enemy == null) {
            return;
        }
        if (hit(this, enemy, true)) {
            if (enemy == Dungeon.hero && Random.Int(2) == 0) {
                Sample.INSTANCE.play(Assets.Sounds.GOLD);
            }
            int dmg = Random.NormalIntRange(15, 30);
            if (enemy.buff(StoneOfAggression.Aggression.class) != null
                    && enemy.alignment == alignment
                    && (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))) {
                dmg *= 0.5f;
            }
            enemy.damage(dmg, this);
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
        }
    }

    public void onZapComplete() {
        zap();
        next();
    }

    public void dropGold(Char target) {
        final int rockCenter;
        if (Dungeon.level.adjacent(pos, target.pos)) {
            int oppositeAdjacent = target.pos + (target.pos - pos);
            Ballistica trajectory = new Ballistica(target.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(target, trajectory, 2, false, false, this);
            rockCenter = trajectory.path.get(Math.min(trajectory.dist, 2));
        } else if (Dungeon.level.distance(pos, target.pos) == 2) {
            int oppositeAdjacent = target.pos + (target.pos - pos);
            Ballistica trajectory = new Ballistica(target.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(target, trajectory, 1, false, false, this);
            rockCenter = trajectory.path.get(Math.min(trajectory.dist, 1));
        } else {
            rockCenter = target.pos;
        }

        int safeCell;
        do {
            safeCell = rockCenter + PathFinder.NEIGHBOURS8[Random.Int(8)];
        } while (!Dungeon.level.insideMap(safeCell)
                || safeCell == pos
                || (Dungeon.level.solid[safeCell] && Random.Int(2) == 0));

        ArrayList<Integer> rockCells = new ArrayList<>();
        int start = rockCenter - Dungeon.level.width() * 3 - 3;
        int cell;
        for (int y = 0; y < 7; y++) {
            cell = start + Dungeon.level.width() * y;
            for (int x = 0; x < 7; x++) {
                if (Dungeon.level.insideMap(cell)
                        && !Dungeon.level.solid[cell]
                        && cell != safeCell
                        && Random.Int(Math.max(1, Dungeon.level.distance(rockCenter, cell))) == 0) {
                    rockCells.add(cell);
                }
                cell++;
            }
        }
        for (int i : rockCells) {
            sprite.parent.add(new TargetedCell(i, 0xFFD700));
        }
        Buff.append(this, FallingGoldBuff.class, 2 * TICK).setRockPositions(rockCells);
    }

    @Override
    public void die(Object cause) {
        TestBossUtil.bossSlain(this);
        Buff b = Dungeon.hero.buff(GreatShoper.GoldCurse.class);
        if (b != null) {
            b.detach();
        }
        Dungeon.level.drop(new Gold().quantity(114514), pos).sprite.drop(pos);
        super.die(cause);
    }

    @Override
    public void notice() {
        super.notice();
        TestBossUtil.assignBoss(this);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(PHASE, phase);
        bundle.put(SUMMON_CD, summonCooldown);
        super.storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase = bundle.getInt(PHASE);
        summonCooldown = bundle.getFloat(SUMMON_CD);
        if (phase == 2 || phase == 3) {
            properties.add(Property.IMMOVABLE);
        }
        BossHealthBar.assignBoss(this);
        if (phase == 4) {
            BossHealthBar.bleed(true);
        }
    }

    public static class FallingGoldBuff extends DelayedRockFall {
        @Override
        public void affectChar(Char ch) {
            if (!(ch instanceof TestGreatShoper) && !(ch instanceof GreatShoper)) {
                ch.damage(Random.Int(20), new Viscosity.DeferedDamage());
                Buff.prolong(ch, Paralysis.class, 2);
            }
        }

        @Override
        public void affectCell(int cell) {
            Dungeon.level.drop(new Gold().quantity(Random.Int(200)), cell).sprite.drop();
        }
    }
}
