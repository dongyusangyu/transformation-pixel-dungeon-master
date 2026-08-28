package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.utils.Bundle;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HungerKnightTalentSealTest {

    @Test
    public void sealedTalentReadsAsZeroWithoutChangingInvestedPoints() {
        Hero hero = TestHeroFactory.create();
        LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
        tier.put(Talent.HEARTY_MEAL, 2);
        hero.talents.add(tier);
        assertEquals(2, hero.pointsInTalent(Talent.HEARTY_MEAL));

        HungerKnightTalentSeal seal = new HungerKnightTalentSeal();
        seal.setOwnerId(71);
        seal.add(Talent.HEARTY_MEAL);
        assertTrue(seal.attachTo(hero));

        assertEquals(0, hero.pointsInTalent(Talent.HEARTY_MEAL));
        assertEquals(Integer.valueOf(2), tier.get(Talent.HEARTY_MEAL));
        assertFalse(HungerKnightTalentSeal.isSealedBy(hero, Talent.HEARTY_MEAL, 72));
        assertTrue(HungerKnightTalentSeal.isSealedBy(hero, Talent.HEARTY_MEAL, 71));
    }

    @Test
    public void ownerAndTalentNamesSurviveBundleRoundTrip() {
        HungerKnightTalentSeal source = new HungerKnightTalentSeal();
        source.setOwnerId(99);
        source.add(Talent.HEARTY_MEAL);
        source.add(Talent.PROVOKED_ANGER);
        Bundle bundle = new Bundle();
        source.storeInBundle(bundle);

        HungerKnightTalentSeal restored = new HungerKnightTalentSeal();
        restored.restoreFromBundle(bundle);
        assertEquals(99, restored.ownerId());
        assertTrue(restored.contains(Talent.HEARTY_MEAL));
        assertTrue(restored.contains(Talent.PROVOKED_ANGER));
        assertEquals(2, restored.size());
    }
}
