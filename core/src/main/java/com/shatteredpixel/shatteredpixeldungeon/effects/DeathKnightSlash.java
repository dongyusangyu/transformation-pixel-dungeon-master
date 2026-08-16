package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;

import java.util.ArrayList;
import java.util.Comparator;

/** Non-blocking sword-wave projectile used to visualize committed bombardment lanes. */
public class DeathKnightSlash extends MovieClip {

    private final float travelTime;
    private float elapsedTime;

    private DeathKnightSlash(int from, int to) {
        texture(Assets.Effects.DEATH_KNIGHT_SLASH);
        TextureFilm film = new TextureFilm(texture, 32, 32);
        Animation animation = new Animation(24, true);
        animation.frames(film, 0, 1, 2, 3);

        PointF start = DungeonTilemap.tileCenterToWorld(from);
        PointF end = DungeonTilemap.tileCenterToWorld(to);
        x = start.x - width / 2f;
        y = start.y - height / 2f;
        origin.set(width / 2f, height / 2f);
        angle = (float) Math.toDegrees(Math.atan2(end.y - start.y, end.x - start.x));
        float distance = PointF.distance(start, end);
        travelTime = Math.max(0.12f, Math.min(0.42f, distance / 180f));
        speed.set((end.x - start.x) / travelTime, (end.y - start.y) / travelTime);
        play(animation);
    }

    /**
     * Selects angularly distributed far cells so large zones read as moving sword rays
     * without creating one visual per affected tile.
     */
    public static void showVolley(Group parent, int from, int[] affectedCells, int rayCount) {
        if (parent == null || Dungeon.level == null || affectedCells == null
                || from < 0 || from >= Dungeon.level.length() || rayCount <= 0) return;
        ArrayList<Endpoint> endpoints = new ArrayList<>();
        int width = Dungeon.level.width();
        int fromX = from % width;
        int fromY = from / width;
        for (int cell : affectedCells) {
            if (cell < 0 || cell >= Dungeon.level.length() || cell == from) continue;
            int dx = cell % width - fromX;
            int dy = cell / width - fromY;
            endpoints.add(new Endpoint(cell, Math.atan2(dy, dx), dx * dx + dy * dy));
        }
        if (endpoints.isEmpty()) return;
        endpoints.sort(Comparator.comparingDouble(endpoint -> endpoint.angle));

        int count = Math.min(rayCount, endpoints.size());
        int previousCell = -1;
        for (int i = 0; i < count; i++) {
            int center = count == 1 ? endpoints.size() / 2
                    : Math.round(i * (endpoints.size() - 1f) / (count - 1f));
            int radius = Math.max(1, endpoints.size() / Math.max(2, count * 2));
            Endpoint selected = endpoints.get(center);
            for (int index = Math.max(0, center - radius);
                 index <= Math.min(endpoints.size() - 1, center + radius); index++) {
                Endpoint candidate = endpoints.get(index);
                if (candidate.distanceSquared > selected.distanceSquared) selected = candidate;
            }
            if (selected.cell != previousCell) {
                parent.add(new DeathKnightSlash(from, selected.cell));
                previousCell = selected.cell;
            }
        }
    }

    @Override
    public void update() {
        super.update();
        elapsedTime += Game.elapsed;
        if (elapsedTime >= travelTime) killAndErase();
    }

    private static final class Endpoint {
        final int cell;
        final double angle;
        final int distanceSquared;

        Endpoint(int cell, double angle, int distanceSquared) {
            this.cell = cell;
            this.angle = angle;
            this.distanceSquared = distanceSquared;
        }
    }
}
