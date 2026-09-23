package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DungeonRunStateResetTest {

    @Test
    public void resetRunLimitedCountersClearsEatingTalentUses() {
        Dungeon.eat_item = 100;

        Dungeon.resetRunLimitedCounters();

        assertEquals(0, Dungeon.eat_item);
    }
}
