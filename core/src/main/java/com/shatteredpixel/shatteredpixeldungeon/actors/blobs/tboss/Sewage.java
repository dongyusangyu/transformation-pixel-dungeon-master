package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Susceptible;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.particles.Emitter;

/** Polluted water left by the Pestilence Knight's terminal-phase flask. */
public class Sewage extends Blob {

    public static final int INITIAL_VOLUME = 12;
    private static final int COLOR = 0x385B24;

    @Override
    protected void evolve() {
        if (Dungeon.level == null) return;
        for (int cell = 0; cell < off.length; cell++) off[cell] = 0;

        int width = Dungeon.level.width();
        int purifierCell = purifierCell();
        for (int cell = 0; cell < cur.length; cell++) {
            int intensity = cur[cell];
            if (intensity <= 0 || isPurifierCell(cell, purifierCell) || disinfected(cell)) continue;

            boolean waterCell = Dungeon.level.water[cell]
                    || Dungeon.level.setCellToWater(false, cell);
            if (!waterCell) continue;

            Char occupant = Actor.findChar(cell);
            if (occupant != null && occupant.alignment == Char.Alignment.ALLY) {
                Susceptible.apply(occupant);
            }

            int remaining = intensity - 1;
            merge(cell, remaining);
            if (remaining <= 1) continue;

            int[] neighbours = {cell - width, cell + 1, cell + width, cell - 1};
            for (int i = 0; i < neighbours.length; i++) {
                int next = neighbours[i];
                if (next < 0 || next >= cur.length
                        || !canSpreadToWater(Dungeon.level.water[next], Dungeon.level.solid[next],
                        Dungeon.level.getTransition(next) != null)
                        || Math.abs(next % width - cell % width) > 1
                        || disinfected(next)) continue;
                merge(next, remaining - 1);
            }
        }
    }

    private boolean disinfected(int cell) {
        return Blob.volumeAt(cell, Fire.class) > 0 || Blob.volumeAt(cell, CursedFlame.class) > 0;
    }

    private void merge(int cell, int value) {
        if (value <= off[cell]) return;
        volume += value - off[cell];
        off[cell] = value;
        area.union(cell % Dungeon.level.width(), cell / Dungeon.level.width());
    }

    static boolean canSpreadToWater(boolean water, boolean solid, boolean hasTransition) {
        return water && !solid && !hasTransition;
    }

    static boolean canSpreadToWaterForTest(boolean water, boolean solid, boolean hasTransition) {
        return canSpreadToWater(water, solid, hasTransition);
    }

    private int purifierCell() {
        if (!(Dungeon.level instanceof TowerBossLevel)) return -1;
        TowerBossLevel level = (TowerBossLevel) Dungeon.level;
        return level.pestilenceArenaController() == null
                ? -1 : level.pestilenceArenaController().purifierCell();
    }

    static boolean isPurifierCell(int cell, int purifierCell) {
        return purifierCell >= 0 && cell == purifierCell;
    }

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        emitter.pour(new Emitter.Factory() {
            @Override
            public void emit(Emitter source, int index, float x, float y) {
                Speck particle = (Speck) source.recycle(Speck.class);
                particle.reset(index, x, y, Speck.TOXIC);
                particle.hardlight(COLOR);
            }
        }, 0.25f);
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }
}
