package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HungerKnightPressureTest {

    @Test
    public void yellowAndRedClocksUseWorldTicksAndResetOnBandChange() {
        HungerKnightPressure pressure = new HungerKnightPressure();
        for (int i = 0; i < 29; i++) assertEquals(0, pressure.advance(300));
        assertEquals(1, pressure.advance(300));
        for (int i = 0; i < 4; i++) assertEquals(0, pressure.advance(450));
        assertEquals(2, pressure.advance(450));
        assertEquals(0, pressure.advance(299));
        for (int i = 0; i < 29; i++) assertEquals(0, pressure.advance(300));
        assertEquals(1, pressure.advance(300));
    }
}
