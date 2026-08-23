package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnight;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
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

    @Test public void temporaryDrunkennessExpiresAndShowsCountdown() {
        DeathKnight target = new DeathKnight();
        Drunkenness drunk = Drunkenness.affectTemporary(target, 50f);

        assertEquals("50", drunk.iconTextDisplay());
        assertEquals(0f, drunk.iconFadePercent(), 0.001f);
        for (int i = 0; i < 25; i++) drunk.act();
        assertEquals("25", drunk.iconTextDisplay());
        assertEquals(0.5f, drunk.iconFadePercent(), 0.001f);
        for (int i = 0; i < 25; i++) drunk.act();
        assertNull(target.buff(Drunkenness.class));
    }

    @Test public void ordinaryDrunkennessPromotesTemporaryDrunkennessToPermanent() {
        DeathKnight target = new DeathKnight();
        Drunkenness timed = Drunkenness.affectTemporary(target, 50f);
        Drunkenness permanent = Drunkenness.affect(target);

        assertSame(timed, permanent);
        assertEquals("", permanent.iconTextDisplay());
        for (int i = 0; i < 60; i++) permanent.act();
        assertSame(permanent, target.buff(Drunkenness.class));
    }

    @Test public void permanentDrunkennessIsNotDowngradedByFruitDrunkenness() {
        DeathKnight target = new DeathKnight();
        Drunkenness permanent = Drunkenness.affect(target);

        assertSame(permanent, Drunkenness.affectTemporary(target, 50f));
        assertEquals("", permanent.iconTextDisplay());
    }

    @Test public void temporaryDrunkennessRefreshesWhenFruitIsConsumedAgain() {
        DeathKnight target = new DeathKnight();
        Drunkenness timed = Drunkenness.affectTemporary(target, 50f);
        for (int i = 0; i < 10; i++) timed.act();

        assertSame(timed, Drunkenness.affectTemporary(target, 50f));
        assertEquals("50", timed.iconTextDisplay());
    }

    @Test public void temporaryDrunkennessPersistsItsModeAndRemainingTurns() {
        DeathKnight target = new DeathKnight();
        Drunkenness timed = Drunkenness.affectTemporary(target, 50f);
        for (int i = 0; i < 7; i++) timed.act();
        Bundle bundle = new Bundle();
        timed.storeInBundle(bundle);

        Drunkenness restored = new Drunkenness();
        restored.restoreFromBundle(bundle);

        assertEquals("43", restored.iconTextDisplay());
    }
}
