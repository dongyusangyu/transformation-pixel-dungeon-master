package com.shatteredpixel.shatteredpixeldungeon.actors.blobs.tboss;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SewageTest {

    @Test
    public void contaminationSpreadsOnlyAcrossOpenWaterCells() {
        assertTrue(Sewage.canSpreadToWaterForTest(true, false, false));
        assertFalse(Sewage.canSpreadToWaterForTest(false, false, false));
        assertFalse(Sewage.canSpreadToWaterForTest(true, true, false));
        assertFalse(Sewage.canSpreadToWaterForTest(true, false, true));
    }

    @Test
    public void sewageNeverOccupiesThePurifierCell() {
        assertTrue(Sewage.isPurifierCell(42, 42));
        assertFalse(Sewage.isPurifierCell(41, 42));
        assertFalse(Sewage.isPurifierCell(42, -1));
    }
}
