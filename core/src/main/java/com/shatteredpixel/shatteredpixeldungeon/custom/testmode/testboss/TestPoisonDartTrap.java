package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.testboss;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class TestPoisonDartTrap extends PoisonDartTrap {

    @Override
    public void activate() {
        if (Dungeon.level.heroFOV[pos]) {
            Sample.INSTANCE.play(Assets.Sounds.TRAP);
        }

        Char target = Actor.findChar(pos);
        if (target != null) {
            int dmg = Random.NormalIntRange(4, 8) - target.drRoll();
            if (dmg < 0) dmg = 0;
            target.damage(dmg, this);
            Buff.affect(target, Poison.class).set(poisonAmount());
            Sample.INSTANCE.play(Assets.Sounds.HIT, 1, 1, Random.Float(0.8f, 1.25f));
            if (target.sprite != null) {
                target.sprite.bloodBurstA(target.sprite.center(), dmg);
                target.sprite.flash();
            }
        }
    }
}
