package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.KingSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class TestDwarfKing extends Mob {

    private int phase = 1;
    private int summonsMade = 0;
    private float summonCooldown = 0;

    private static final String PHASE = "phase";
    private static final String SUMMONS_MADE = "summons_made";
    private static final String SUMMON_CD = "summon_cd";

    {
        spriteClass = KingSprite.class;

        HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 450 : 300;
        EXP = 40;
        defenseSkill = 22;
        state = WANDERING;

        properties.add(Property.BOSS);
        properties.add(Property.UNDEAD);
    }

    @Override
    protected Char chooseEnemy() {
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(15, 25);
    }

    @Override
    public int attackSkill(Char target) {
        return 26;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 5);
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
            if (summonCooldown <= 0) {
                summonSubject(Random.Int(3) == 0 ? DwarfKing.DKMonk.class : DwarfKing.DKGhoul.class);
                summonsMade++;
                summonCooldown = Random.NormalIntRange(8, 12);
            } else {
                summonCooldown--;
            }
        } else if (phase == 2) {
            if (buff(Barrier.class) == null || buff(Barrier.class).shielding() <= 0) {
                phase = 3;
                properties.remove(Property.IMMOVABLE);
                BossHealthBar.bleed(true);
                yell(Messages.get(this, "defeated"));
            } else if (summonCooldown <= 0) {
                summonSubject(randomSubject(true));
                summonCooldown = 3;
            } else {
                summonCooldown--;
            }
        } else if (phase == 3 && summonCooldown <= 0) {
            summonSubject(randomSubject(false));
            summonCooldown = Random.NormalIntRange(4, 7);
        } else {
            summonCooldown--;
        }
        return super.act();
    }

    private void summonSubject(Class<? extends Mob> type) {
        Mob mob = TestBossUtil.summonNear(this, type, pos, 1, 5);

    }

    private Class<? extends Mob> randomSubject(boolean includeGolem) {
        switch (Random.Int(includeGolem ? 4 : 3)) {
            case 0:
            default:
                return DwarfKing.DKGhoul.class;
            case 1:
                return DwarfKing.DKMonk.class;
            case 2:
                return DwarfKing.DKWarlock.class;
            case 3:
                return DwarfKing.DKGolem.class;
        }
    }

    @Override
    public void damage(int dmg, Object src, DamageTag... damageTags) {
        Char attacker = TestBossUtil.attackerToRetarget(this, src);
        if (attacker != null) {
            enemy = attacker;
            state = HUNTING;
            beckon(attacker.pos);
        }
        int preHP = HP;
        super.damage(dmg, src, damageTags);
        int dmgTaken = preHP - HP;
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (dmgTaken > 0 && lock != null && !isImmune(src.getClass()) && !isInvulnerable(src.getClass())) {
            lock.addTime(dmgTaken);
        }
        if (phase == 1 && HP <= HT / 2 && isAlive()) {
            HP = HT / 2;
            phase = 2;
            summonsMade = 0;
            summonCooldown = 0;
            properties.add(Property.IMMOVABLE);
            Buff.affect(this, Barrier.class).setShield(HT);
            if (sprite != null) {
                sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "shield"));
            }
            yell(Messages.get(this, "summon"));
        }
    }

    @Override
    public boolean isInvulnerable(Class effect) {
        return phase == 2 || super.isInvulnerable(effect);
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() || phase != 3;
    }

    @Override
    public void die(Object cause) {
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
        bundle.put(SUMMONS_MADE, summonsMade);
        bundle.put(SUMMON_CD, summonCooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase = bundle.getInt(PHASE);
        summonsMade = bundle.getInt(SUMMONS_MADE);
        summonCooldown = bundle.getFloat(SUMMON_CD);
        BossHealthBar.assignBoss(this);
        if (phase == 2) {
            properties.add(Property.IMMOVABLE);
        }
        if (phase == 3) {
            BossHealthBar.bleed(true);
        }
    }
}
