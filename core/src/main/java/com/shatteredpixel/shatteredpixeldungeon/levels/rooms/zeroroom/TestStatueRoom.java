package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.zeroroom;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.RatKing;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;

public abstract class TestStatueRoom  extends StandardRoom {
    @Override
    public int maxHeight() { return 7; }
    public int maxWidth() { return 7; }
    public void paint(Level level ) {

        Painter.fill( level, this, Terrain.WALL );
        Painter.fill( level, this, 1, Terrain.EMPTY_SP );
        RatKing king = new RatKing();
        king.pos = level.pointToCell(random( 2 ));
        level.mobs.add( king );
        for (Door door : connected.values()) {
            door.set( Door.Type.REGULAR );
        }
    }
}
