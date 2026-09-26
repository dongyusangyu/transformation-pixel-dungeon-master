package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExhilarationMovementTest {

    @Test
    public void movementBonusRequiresBothBuffAndActiveEncounter() {
        assertEquals(1.5f, Exhilaration.movementMultiplier(true, true), 0f);
        assertEquals(1f, Exhilaration.movementMultiplier(true, false), 0f);
        assertEquals(1f, Exhilaration.movementMultiplier(false, true), 0f);
        assertEquals(1f, Exhilaration.movementMultiplier(false, false), 0f);
    }
}
