package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnightBombardment.Band;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeathKnightSlashTest {

    @Test
    public void onlyCommittedCellsBecomeSwordWaveEffects() {
        DeathKnightSlash.Entry[] entries = DeathKnightSlash.plan(5, 25, 12,
                new int[]{7, 8, 8, 12, -1, 26, 17}, null);

        assertEquals(3, entries.length);
        assertEquals(7, entries[0].cell);
        assertEquals(8, entries[1].cell);
        assertEquals(17, entries[2].cell);
    }

    @Test
    public void swordWaveStartsAtTheBossAndThenMovesAcrossTheAffectedCells() {
        DeathKnightSlash.Entry[] entries = DeathKnightSlash.plan(5, 25, 12,
                new int[]{13, 14}, null);

        assertEquals(0.02f, entries[0].delay, 0.0001f);
        assertEquals(0.04f, entries[1].delay, 0.0001f);
    }

    @Test
    public void executionUsesItsCommittedDamageBandsForEffectTiming() {
        DeathKnightSlash.Entry[] entries = DeathKnightSlash.plan(5, 25, 12,
                new int[]{2, 3, 4}, new Band[]{Band.CORE, Band.INNER, Band.OUTER});

        assertEquals(0f, entries[0].delay, 0.0001f);
        assertEquals(0.04f, entries[1].delay, 0.0001f);
        assertEquals(0.08f, entries[2].delay, 0.0001f);
    }

    @Test
    public void eachSwordWavePlaysExactlyFourFramesOnce() {
        assertEquals(-1, DeathKnightSlash.frameIndex(0.019f, 0.02f));
        assertEquals(0, DeathKnightSlash.frameIndex(0.02f, 0.02f));
        assertEquals(1, DeathKnightSlash.frameIndex(0.11f, 0.02f));
        assertEquals(3, DeathKnightSlash.frameIndex(0.28f, 0.02f));
        assertEquals(-1, DeathKnightSlash.frameIndex(0.36f, 0.02f));
    }
}
