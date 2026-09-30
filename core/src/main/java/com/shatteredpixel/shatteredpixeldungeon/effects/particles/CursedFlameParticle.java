package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.noosa.particles.Emitter;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;

/** Green variant with a separate recycle pool, so ordinary fire keeps its colour. */
public class CursedFlameParticle extends FlameParticle {

    public static final int COLOR = 0x60F802;

    private int sourceCell = -1;
    private boolean followsCharacter;

    // Character emitters are gated by CharSprite, not by the drifting particle's cell.
    public static final Emitter.Factory CHARACTER_FACTORY = new Emitter.Factory() {
        @Override public void emit(Emitter emitter, int index, float x, float y) {
            CursedFlameParticle particle = (CursedFlameParticle) emitter.recycle(CursedFlameParticle.class);
            particle.resetForSource(x, y, -1);
            particle.followsCharacter = true;
            particle.visible = true;
        }
        @Override public boolean lightMode() { return true; }
    };

    public static final Emitter.Factory FACTORY = new Emitter.Factory() {
        @Override public void emit(Emitter emitter, int index, float x, float y) {
            ((CursedFlameParticle) emitter.recycle(CursedFlameParticle.class)).resetForSource(x, y, -1);
        }
        @Override public void emit(Emitter emitter, int index, float x, float y, int cell) {
            ((CursedFlameParticle) emitter.recycle(CursedFlameParticle.class)).resetForSource(x, y, cell);
        }
        @Override public boolean lightMode() { return true; }
    };

    public CursedFlameParticle() {
        super();
        color(COLOR);
    }

    void resetForSource(float x, float y, int cell) {
        super.reset(x, y);
        sourceCell = cell;
        followsCharacter = false;
        visible = cell >= 0 ? isSourceCellVisible(cell) : isPositionVisible(x, y);
    }

    public static boolean isPositionVisible(float x, float y) {
        if (Dungeon.level == null || !Float.isFinite(x) || !Float.isFinite(y)) return false;
        int tileX = (int) Math.floor(x / DungeonTilemap.SIZE);
        int tileY = (int) Math.floor(y / DungeonTilemap.SIZE);
        return tileX >= 0 && tileX < Dungeon.level.width()
                && tileY >= 0 && tileY < Dungeon.level.height()
                && isSourceCellVisible(tileX + tileY * Dungeon.level.width());
    }

    static boolean isSourceCellVisible(int cell) {
        return Dungeon.level != null && Dungeon.level.heroFOV != null
                && cell >= 0 && cell < Dungeon.level.heroFOV.length
                && Dungeon.level.heroFOV[cell];
    }

    @Override
    public void update() {
        super.update();
        visible = alive && (followsCharacter || (sourceCell >= 0
                ? isSourceCellVisible(sourceCell) : isPositionVisible(x, y)));
    }
}
