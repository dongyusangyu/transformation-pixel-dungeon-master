package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnight;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import org.junit.Test;
import static org.junit.Assert.*;

public class GentlemanWineStateTest {
    @Test public void statesAreMutuallyExclusiveAndUseReservedIcons() {
        DeathKnight target = new DeathKnight();
        Drunkenness drunk = Drunkenness.affect(target);
        assertSame(drunk, target.buff(Drunkenness.class));
        assertNull(target.buff(Exhilaration.class));
        assertEquals(0.8f, drunk.outgoingDamageMultiplier(null, DamageTag.PHYSICAL), 0.001f);
        assertEquals(0.8f, drunk.incomingDamageMultiplier(null, DamageTag.PHYSICAL), 0.001f);
        Exhilaration high = Exhilaration.affect(target);
        assertNull(target.buff(Drunkenness.class));
        assertSame(high, target.buff(Exhilaration.class));
        assertEquals(BuffIndicator.DRUNKENNESS, drunk.icon());
        assertEquals(BuffIndicator.EXHILARATION, high.icon());
        assertEquals(127, BuffIndicator.NONE);
    }
}
