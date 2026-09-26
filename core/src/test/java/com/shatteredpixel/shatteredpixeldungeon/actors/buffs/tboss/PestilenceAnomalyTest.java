package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PestilenceAnomalyTest {

    @Test
    public void everyAnomalyRollHasItsOwnTemporaryEffect() {
        assertEquals(Burning.class, PestilenceAnomaly.effectForRoll(0));
        assertEquals(Frost.class, PestilenceAnomaly.effectForRoll(1));
        assertEquals(MagicalSleep.class, PestilenceAnomaly.effectForRoll(2));
        assertEquals(PlagueFrailty.class, PestilenceAnomaly.effectForRoll(3));
    }
}
