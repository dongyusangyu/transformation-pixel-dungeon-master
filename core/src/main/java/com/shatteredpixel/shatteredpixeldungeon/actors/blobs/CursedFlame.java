package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedFlameDamage;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.CursedFlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/** A persistent green fire, independent from ordinary Fire and Burning. */
public class CursedFlame extends Blob {

    /** Cursed flame may occupy open terrain and combustible obstacles, but not inert solid terrain. */
    public static boolean canIgnite(Level level, int cell) {
        return level != null && level.insideMap(cell)
                && (!level.solid[cell] || level.flamable[cell]);
    }

    @Override protected void evolve() {
        Freezing freeze = (Freezing) Dungeon.level.blobs.get(Freezing.class);
        boolean observe = false;
        int width = Dungeon.level.width();
        for (int x = area.left - 1; x <= area.right; x++) {
            for (int y = area.top - 1; y <= area.bottom; y++) {
                int cell = x + y * width;
                if (!Dungeon.level.insideMap(cell)) continue;

                if (!canIgnite(Dungeon.level, cell)) {
                    off[cell] = 0;
                    continue;
                }

                boolean freezing = freeze != null && freeze.volume > 0 && freeze.cur != null
                        && freeze.cur[cell] > 0;
                int remaining;
                if (cur[cell] > 0) {
                    if (freezing) {
                        freeze.clear(cell);
                        cur[cell] = off[cell] = 0;
                        continue;
                    }
                    burn(cell);
                    remaining = cur[cell] - 1;
                    if (remaining == 0 && Dungeon.level.flamable[cell]) {
                        Dungeon.level.destroy(cell);
                        GameScene.updateMap(cell);
                        observe = true;
                    }
                } else if (!freezing && Dungeon.level.flamable[cell]
                        && (hasFlame(x - 1, y) || hasFlame(x + 1, y)
                        || hasFlame(x, y - 1) || hasFlame(x, y + 1))) {
                    remaining = 4;
                    burn(cell);
                    area.union(x, y);
                } else {
                    remaining = 0;
                }
                off[cell] = remaining;
                volume += remaining;
            }
        }
        if (observe) Dungeon.observe();
    }

    private boolean hasFlame(int x, int y) {
        int cell = x + y * Dungeon.level.width();
        return Dungeon.level.insideMap(cell) && cur[cell] > 0;
    }

    private void burn(int cell) {
        Char target = Actor.findChar(cell);
        if (target != null && !target.isImmune(CursedFlame.class)) {
            CursedFlameDamage.apply(target, CursedFlameDamage.roll(target), this);
            if (target.isAlive()) CursedBurning.apply(target, CursedBurning.DURATION);
        }
        Heap heap = Dungeon.level.heaps.get(cell);
        if (heap != null) heap.burnWithCursedFlame();
        Plant plant = Dungeon.level.plants.get(cell);
        if (plant != null) plant.wither();
    }

    @Override public void use(BlobEmitter emitter) {
        super.use(emitter);
        emitter.pour(CursedFlameParticle.FACTORY, 0.03f);
    }

    @Override public String tileDesc() { return Messages.get(this, "desc"); }
}
