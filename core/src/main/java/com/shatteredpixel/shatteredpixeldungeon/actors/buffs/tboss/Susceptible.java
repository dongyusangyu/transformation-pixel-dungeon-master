package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.PestilenceKnight;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/** Temporary vulnerability to plague effects after contact with sewage. */
public class Susceptible extends FlavourBuff {

    public static final float DURATION = 10f;

    public static Susceptible apply(Char target) {
        if (target == null || target instanceof PestilenceKnight
                || target.isImmune(Susceptible.class)) return null;

        boolean newlyApplied = target.buff(Susceptible.class) == null;
        Susceptible susceptible = Buff.prolong(target, Susceptible.class, DURATION);
        if (newlyApplied) Infection.extendDecayForSusceptibility(target);
        return susceptible;
    }

    public static boolean active(Char target) {
        return target != null && target.buff(Susceptible.class) != null;
    }

    public static float plagueDuration(Char target, float duration) {
        return active(target) ? duration * 2f : duration;
    }

    @Override
    public int icon() {
        return BuffIndicator.VULNERABLE;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0x587B32);
    }

    @Override
    public boolean act() {
        Char owner = target;
        super.act();
        Infection.resumeNormalDecay(owner);
        return true;
    }
}
