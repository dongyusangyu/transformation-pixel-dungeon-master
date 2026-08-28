package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.Overburden;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HungerKnightPressureBuffsTest {

    @Test
    public void hungerOverburdenAttenuatesHasteButNotArmorSpeedGlyphs() {
        Hero hero = TestHeroFactory.create();
        TestHeroFactory.addBackpackItem(hero);
        HungerKnightOverburden pressure = new HungerKnightOverburden();
        pressure.setOwnerId(8);
        assertTrue(pressure.attachTo(hero));

        assertEquals(2f, Overburden.attenuateEquipmentSpeed(hero, 2f), 0.001f);
        assertEquals(1f + 2f / 3f, Overburden.attenuateHasteRing(hero, 2f), 0.001f);
        assertFalse(HungerKnightOverburden.release(hero, 9));
        assertTrue(HungerKnightOverburden.release(hero, 8));
    }

    @Test
    public void magicLeaseUsesExactMagicImmuneAndRestoresPreexistingTime() {
        Hero hero = TestHeroFactory.create();
        MagicImmune original = Buff.affect(hero, MagicImmune.class, 8f);
        assertNotNull(original);

        HungerKnightMagicLease lease = HungerKnightMagicLease.acquire(hero, 23);
        assertNotNull(lease);
        MagicImmune encounterImmune = hero.buff(MagicImmune.class);
        assertNotNull(encounterImmune);
        assertEquals(MagicImmune.class, encounterImmune.getClass());
        assertTrue(encounterImmune.cooldown() >= 3f);

        assertFalse(HungerKnightMagicLease.release(hero, 24));
        assertTrue(HungerKnightMagicLease.release(hero, 23));
        assertNull(hero.buff(HungerKnightMagicLease.class));
        assertNotNull(hero.buff(MagicImmune.class));
        assertEquals(8f, hero.buff(MagicImmune.class).cooldown(), 0.001f);
    }

    @Test
    public void magicLeasePreservesDurationAddedByAnotherSourceDuringTheFight() {
        Hero hero = TestHeroFactory.create();
        HungerKnightMagicLease lease = HungerKnightMagicLease.acquire(hero, 23);
        assertNotNull(lease);

        Buff.affect(hero, MagicImmune.class, 6f);

        assertTrue(HungerKnightMagicLease.release(hero, 23));
        assertNotNull(hero.buff(MagicImmune.class));
        assertEquals(6f, hero.buff(MagicImmune.class).cooldown(), 0.001f);
    }

    @Test
    public void externalMagicImmunityContinuesToExpireFromItsActualGrantTime() throws Exception {
        Actor.clear();
        try {
            Hero hero = TestHeroFactory.create();
            assertNotNull(HungerKnightMagicLease.acquire(hero, 23));
            Buff.affect(hero, MagicImmune.class, 6f);

            setActorNow(2f);

            assertTrue(HungerKnightMagicLease.release(hero, 23));
            assertEquals(4f, hero.buff(MagicImmune.class).cooldown(), 0.001f);
        } finally {
            Actor.clear();
        }
    }

    @Test
    public void savingCapturesElapsedExternalDurationWithoutAbsoluteActorTime() throws Exception {
        Actor.clear();
        try {
            Hero hero = TestHeroFactory.create();
            HungerKnightMagicLease lease = HungerKnightMagicLease.acquire(hero, 23);
            assertNotNull(lease);
            Buff.affect(hero, MagicImmune.class, 6f);
            setActorNow(2f);

            Bundle bundle = new Bundle();
            lease.storeInBundle(bundle);

            assertEquals(4f, bundle.getFloat("external"), 0.001f);
            assertFalse(bundle.contains("last_observation"));
        } finally {
            Actor.clear();
        }
    }

    @Test
    public void aDifferentBossCannotReplaceTheActiveMagicLeaseOwner() {
        Hero hero = TestHeroFactory.create();
        assertNotNull(HungerKnightMagicLease.acquire(hero, 23));
        assertNull(HungerKnightMagicLease.acquire(hero, 24));
        assertFalse(HungerKnightMagicLease.release(hero, 24));
        assertNotNull(hero.buff(HungerKnightMagicLease.class));
        assertNotNull(hero.buff(MagicImmune.class));
        assertTrue(HungerKnightMagicLease.release(hero, 23));
    }

    @Test
    public void cleansingDoesNotRemoveTheEncounterLease() {
        Hero hero = TestHeroFactory.create();
        assertNotNull(HungerKnightMagicLease.acquire(hero, 23));

        for (Buff buff : hero.buffs()) {
            if (buff.type == Buff.buffType.NEGATIVE) buff.detach();
        }

        assertNotNull(hero.buff(HungerKnightMagicLease.class));
        assertNotNull(hero.buff(MagicImmune.class));
        assertTrue(HungerKnightMagicLease.release(hero, 23));
        assertNull(hero.buff(MagicImmune.class));
    }

    @Test
    public void releaseAllHandlesAnOwnerIdMismatch() {
        Hero hero = TestHeroFactory.create();
        assertNotNull(HungerKnightMagicLease.acquire(hero, 23));

        assertTrue(HungerKnightMagicLease.releaseAll(hero));
        assertNull(hero.buff(HungerKnightMagicLease.class));
        assertNull(hero.buff(MagicImmune.class));
    }

    @Test
    public void legacyOrphanedEncounterImmunityIsRemovedButNormalImmunityIsKept() {
        Hero hero = TestHeroFactory.create();
        MagicImmune legacy = Buff.affect(hero, MagicImmune.class);
        legacy.postponeUnadjusted(1000000f);

        HungerKnightMagicLease.cleanupLegacyOrphanedImmunity(hero);

        assertNull(hero.buff(MagicImmune.class));

        Buff.affect(hero, MagicImmune.class, 10f);
        HungerKnightMagicLease.cleanupLegacyOrphanedImmunity(hero);
        assertNotNull(hero.buff(MagicImmune.class));
    }

    private static void setActorNow(float value) throws Exception {
        Field now = Actor.class.getDeclaredField("now");
        now.setAccessible(true);
        now.setFloat(null, value);
    }
}
