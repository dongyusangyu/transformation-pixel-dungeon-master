package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnightBombardment.Band;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.RectF;

import java.util.ArrayList;

/** A visual-only sword wave drawn on the committed bombardment cells. */
public class DeathKnightSlash extends Image {

    private static final int FRAME_COUNT = 4;
    private static final int FRAMES_PER_SECOND = 12;
    private static final float MAX_DELAY = 0.12f;
    private static final float DURATION = MAX_DELAY + (float) FRAME_COUNT / FRAMES_PER_SECOND;

    private final Level level;
    private final Entry[] entries;
    private final RectF[] frames = new RectF[FRAME_COUNT];
    private final int originX;
    private final int originY;
    private float elapsed;

    static final class Entry {
        final int cell;
        final float delay;

        Entry(int cell, float delay) {
            this.cell = cell;
            this.delay = delay;
        }
    }

    private DeathKnightSlash(Level level, int from, Entry[] entries) {
        this.level = level;
        this.entries = entries;
        originX = from % level.width();
        originY = from / level.width();

        texture(Assets.Effects.DEATH_KNIGHT_SLASH);
        TextureFilm film = new TextureFilm(texture, 16, 16);
        for (int i = 0; i < FRAME_COUNT; i++) frames[i] = film.get(i);
        frame(frames[0]);
    }

    public static void show(Group parent, int from, int[] affectedCells, Band[] bands) {
        Level level = Dungeon.level;
        if (parent == null || level == null || !SPDSettings.charAnimations()) return;
        Entry[] entries = plan(level.width(), level.length(), from, affectedCells, bands);
        if (entries.length > 0) parent.add(new DeathKnightSlash(level, from, entries));
    }

    static Entry[] plan(int width, int length, int from, int[] affectedCells, Band[] bands) {
        if (width <= 0 || length <= 0 || from < 0 || from >= length || affectedCells == null) {
            return new Entry[0];
        }
        boolean[] seen = new boolean[length];
        ArrayList<Entry> result = new ArrayList<>();
        int fromX = from % width;
        int fromY = from / width;
        for (int i = 0; i < affectedCells.length; i++) {
            int cell = affectedCells[i];
            if (cell < 0 || cell >= length || cell == from || seen[cell]) continue;
            seen[cell] = true;
            Band band = bands != null && i < bands.length ? bands[i] : Band.NONE;
            float delay;
            if (band == Band.CORE) delay = 0f;
            else if (band == Band.INNER) delay = 0.04f;
            else if (band == Band.OUTER) delay = 0.08f;
            else {
                int distance = Math.max(Math.abs(cell % width - fromX),
                        Math.abs(cell / width - fromY));
                delay = Math.min(MAX_DELAY, distance * 0.02f);
            }
            result.add(new Entry(cell, delay));
        }
        return result.toArray(new Entry[0]);
    }

    static int frameIndex(float elapsed, float delay) {
        float localTime = elapsed - delay;
        if (localTime < 0 || localTime >= (float) FRAME_COUNT / FRAMES_PER_SECOND) return -1;
        return (int) (localTime * FRAMES_PER_SECOND);
    }

    @Override
    public void update() {
        super.update();
        elapsed += Game.elapsed;
        if (Dungeon.level != level || elapsed >= DURATION) killAndErase();
    }

    @Override
    public void draw() {
        if (Dungeon.level != level || level.heroFOV == null) return;
        int lastFrame = -1;
        boolean lastFlipH = false;
        boolean lastFlipV = false;
        for (Entry entry : entries) {
            if (!level.heroFOV[entry.cell]) continue;
            int index = frameIndex(elapsed, entry.delay);
            if (index < 0) continue;
            int cellX = entry.cell % level.width();
            int cellY = entry.cell / level.width();
            boolean flipH = cellX < originX;
            boolean flipV = cellY < originY;
            if (index != lastFrame || flipH != lastFlipH || flipV != lastFlipV) {
                flipHorizontal = flipH;
                flipVertical = flipV;
                frame(frames[index]);
                lastFrame = index;
                lastFlipH = flipH;
                lastFlipV = flipV;
            }
            x = cellX * DungeonTilemap.SIZE;
            y = cellY * DungeonTilemap.SIZE;
            super.draw();
        }
    }
}
