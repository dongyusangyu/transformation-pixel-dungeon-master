package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

/** A small trap that ignites a three by three area with cursed flame. */
public class CursedFlameTrap extends Trap {

    public static final int FLAME_DURATION = 2;
    public static final int AREA_SIZE = 3;

    {
        color = GREEN;
        shape = STARS;
    }

    @Override public void activate() {
        seedArea(Dungeon.level, pos, AREA_SIZE, FLAME_DURATION);
        Sample.INSTANCE.play(Assets.Sounds.BURNING);
    }

    static void seedArea(Level level, int center, int size, int duration) {
        for (int cell : affectedCells(level, center, size)) {
            GameScene.add(Blob.seed(cell, duration, CursedFlame.class));
            if (Actor.findChar(cell) instanceof Mob) {
                Buff.prolong(Actor.findChar(cell), Trap.HazardAssistTracker.class,
                        Trap.HazardAssistTracker.DURATION);
            }
        }
    }

    static int[] affectedCells(Level level, int center, int size) {
        ArrayList<Integer> cells = new ArrayList<>();
        if (level == null || center < 0 || center >= level.length() || size <= 0) return new int[0];
        int width = level.width();
        int radius = size / 2;
        int centerX = center % width;
        int centerY = center / width;
        for (int y = centerY - radius; y <= centerY + radius; y++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                if (x < 0 || y < 0 || x >= width || y >= level.height()) continue;
                int cell = x + y * width;
                if (CursedFlame.canIgnite(level, cell)) cells.add(cell);
            }
        }
        int[] result = new int[cells.size()];
        for (int i = 0; i < cells.size(); i++) result[i] = cells.get(i);
        return result;
    }
}
