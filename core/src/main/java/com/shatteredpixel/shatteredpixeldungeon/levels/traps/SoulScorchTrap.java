package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.noosa.audio.Sample;

/** A large trap that blankets a five by five area in cursed flame. */
public class SoulScorchTrap extends Trap {

    public static final int FLAME_DURATION = 5;
    public static final int AREA_SIZE = 5;

    {
        color = GREEN;
        shape = LARGE_DOT;
    }

    @Override public void activate() {
        CursedFlameTrap.seedArea(Dungeon.level, pos, AREA_SIZE, FLAME_DURATION);
        Sample.INSTANCE.play(Assets.Sounds.BURNING);
    }
}
