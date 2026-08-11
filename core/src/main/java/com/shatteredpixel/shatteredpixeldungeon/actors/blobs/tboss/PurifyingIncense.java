package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;

import java.util.Arrays;

/** Short-lived barrier which miasmas cannot occupy or spread through. */
public class PurifyingIncense extends Blob {
    @Override
    protected void evolve() {
        Arrays.fill(off, 0);
        for (int cell = 0; cell < cur.length; cell++) {
            off[cell] = Math.max(0, cur[cell] - 1);
            volume += off[cell];
        }
    }
}
