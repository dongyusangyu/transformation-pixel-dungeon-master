package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TenguSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TestTengu extends Mob {

    private int phase = 1;
    private int abilityCooldown = 2;
    private int lastAbility = -1;

    private static final String PHASE = "phase";
    private static final String ABILITY_COOLDOWN = "ability_cooldown";
    private static final String LAST_ABILITY = "last_ability";

    {
        spriteClass = TenguSprite.class;

        HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 250 : 200;
        EXP = 20;
        defenseSkill = 15;

        viewDistance = 12;
        state = WANDERING;
        properties.add(Property.BOSS);
    }

    @Override
    protected Char chooseEnemy() {
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(6, 12);
    }

    @Override
    public int attackSkill(Char target) {
        return Dungeon.level.adjacent(pos, target.pos) ? 10 : 20;
    }

    @Override
    public float attackDelay() {
        return Dungeon.isChallenged(Challenges.STRONGER_BOSSES)
                && Dungeon.isChallenged(Challenges.HARSH_ENVIRONMENT)
                ? super.attackDelay() * 2 / 3
                : super.attackDelay();
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 5);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return super.canAttack(enemy)
                || new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
    }

    @Override
    protected boolean act() {
        if (!BossHealthBar.isAssigned()) {
            notice();
        }
        if (!TestBossUtil.hasVisibleAttackableEnemy(this)) {
            clearEnemy();
            return super.act();
        }
        if (enemy != null && enemy.isAlive() && state == HUNTING) {
            abilityCooldown--;
            if (abilityCooldown <= 0) {
                useTestAbility(enemy);
                abilityCooldown = phase == 1 ? 4 : 3;
                spend(TICK);
                return true;
            }
        }
        return super.act();
    }

    private void useTestAbility(Char target) {
        int ability = Random.Int(4);
        if (ability == lastAbility) {
            ability = (ability + 1) % 4;
        }
        lastAbility = ability;
        switch (ability) {
            case 0:
                Tengu.throwBomb(this, target);
                break;
            case 1:
                Tengu.throwFire(this, target);
                break;
            case 2:
                Tengu.throwShocker(this, target);
                break;
            default:
                placeVisiblePoisonTrap(target);
                break;
        }
    }

    private void placeVisiblePoisonTrap(Char target) {
        int trapPos = -1;
        for (int i = 0; i < 40; i++) {
            int cell = target.pos + PathFinder.NEIGHBOURS8[Random.Int(PathFinder.NEIGHBOURS8.length)];
            if (TestBossUtil.canUseCell(cell)) {
                trapPos = cell;
                break;
            }
        }
        if (trapPos == -1) {
            trapPos = target.pos;
        }
        Dungeon.level.setTrap(new PoisonDartTrap().reveal(), trapPos);
        CellEmitter.get(trapPos).burst(Speck.factory(Speck.STEAM), 4);
    }

    @Override
    public void damage(int dmg, Object src) {
        Char attacker = TestBossUtil.attackerToRetarget(this, src);
        if (attacker != null) {
            enemy = attacker;
            state = HUNTING;
            beckon(attacker.pos);
        }
        if (!BossHealthBar.isAssigned()) {
            notice();
        }
        int preHP = HP;
        super.damage(dmg, src);
        int dmgTaken = preHP - HP;
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (dmgTaken > 0 && lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())) {
            lock.addTime(dmgTaken);
        }
        if (phase == 1 && HP <= HT / 2 && isAlive()) {
            HP = Math.max(HP, HT / 2);
            phase = 2;
            yell(Messages.get(this, "phase"));
            jumpNearTarget();
            BossHealthBar.bleed(true);
        }
    }

    private void jumpNearTarget() {
        Char target = TestBossUtil.firstEnemy(this);
        int center = target == null ? pos : target.pos;
        int newPos = TestBossUtil.randomSpawnCellNear(center, 2, 6);
        if (newPos != -1) {
            if (sprite != null) {
                sprite.move(pos, newPos);
            }
            move(newPos);
            Dungeon.level.occupyCell(this);
        }
    }

    @Override
    public void die(Object cause) {
        for (Blob blob : Dungeon.level.blobs.values()) {
            if (blob instanceof Fire) {
                blob.clear(0);
            }
        }
        ArrayList<Item> items = Dungeon.level.getItemsToPreserveFromSealedResurrect();

        for (Item i : items.toArray(new Item[0])){
            if (i instanceof Tengu.BombAbility.BombItem || i instanceof Tengu.ShockerAbility.ShockerItem){
                items.remove(i);
            }
        }
        TestBossUtil.bossSlain(this);
        super.die(cause);
    }

    @Override
    public void notice() {
        super.notice();
        TestBossUtil.assignBoss(this);
        yell(Messages.get(this, "notice"));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, phase);
        bundle.put(ABILITY_COOLDOWN, abilityCooldown);
        bundle.put(LAST_ABILITY, lastAbility);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase = bundle.getInt(PHASE);
        abilityCooldown = bundle.getInt(ABILITY_COOLDOWN);
        lastAbility = bundle.getInt(LAST_ABILITY);
        BossHealthBar.assignBoss(this);
        if (phase >= 2) {
            BossHealthBar.bleed(true);
        }
    }
}
