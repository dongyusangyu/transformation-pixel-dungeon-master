package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM300Sprite;
import com.watabou.utils.Bundle;

public class TestDM300 extends DM300 {

    private int superchargeTurns = 0;
    private static final String SUPERCHARGE_TURNS = "supercharge_turns";

    {
        state = WANDERING;
    }

    @Override
    protected Char chooseEnemy() {
        return TestBossUtil.visibleEnemyOrNull(this, super.chooseEnemy());
    }

    @Override
    protected boolean act() {
        if (!TestBossUtil.hasVisibleAttackableEnemy(this)) {
            clearEnemy();
        }
        if (supercharged) {
            superchargeTurns--;
            if (superchargeTurns <= 0) {
                loseSupercharge();
            }
        }
        return super.act();
    }

    @Override
    public void supercharge() {
        supercharged = true;
        pylonsActivated++;
        superchargeTurns = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 12 : 8;

        spend(Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 2f : 3f);
        yell(Messages.get(this, "charging"));
        if (sprite != null) {
            sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
            ((DM300Sprite)sprite).updateChargeState(true);
            ((DM300Sprite)sprite).charge();
        }
        chargeAnnounced = false;
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

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SUPERCHARGE_TURNS, superchargeTurns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        superchargeTurns = bundle.getInt(SUPERCHARGE_TURNS);
    }
}
