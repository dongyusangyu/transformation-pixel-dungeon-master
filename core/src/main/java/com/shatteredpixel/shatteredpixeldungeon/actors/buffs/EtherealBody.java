package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/** A visible, semi-transparent body; it does not change targeting or invisibility. */
public class EtherealBody extends FlavourBuff {
    public static final float DURATION = 10f;

    {
        type = buffType.POSITIVE;
        announced = true;
        immunities.add(CursedBurning.class);
        immunities.add(CursedFlameDamage.class);
        immunities.add(CursedFlame.class);
    }

    @Override public boolean attachTo(Char target) {
        if (!super.attachTo(target)) return false;
        Buff.detach(target, CursedBurning.class);
        return true;
    }

    @Override public int icon() { return BuffIndicator.XIA; }

    @Override public void tintIcon(Image icon) {
        icon.hardlight(0.376f, 0.973f, 0.008f);
    }

    @Override public float iconFadePercent() {
        return Math.max(0, (DURATION - visualcooldown()) / DURATION);
    }

    @Override public void fx(boolean on) {
        if (on) target.sprite.add(CharSprite.State.ETHEREAL);
        else target.sprite.remove(CharSprite.State.ETHEREAL);
    }
}
