package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

public class TestGoo extends Goo {
    {
        state = WANDERING;
    }

    @Override
    protected Char chooseEnemy() {
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }

    @Override
    public boolean act() {
        if (!TestBossUtil.hasVisibleAttackableEnemy(this)) {
            clearEnemy();
        }
        return super.act();
    }

    @Override
    public void damage(int dmg, Object src) {
        Char attacker = TestBossUtil.attackerToRetarget(this, src);
        if (attacker != null) {
            enemy = attacker;
            state = HUNTING;
            beckon(attacker.pos);
        }
        super.damage(dmg, src);
    }

    @Override
    public void notice() {
        super.notice();
        TestBossUtil.assignBoss(this);
    }
}
