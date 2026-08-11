package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TalentCatalogSubclassPoolTest {

    @Test
    public void subclassCatalogIncludesExpandedMetamorphosisPools() {
        List<Talent> catalog = TalentCatalog.SUBCLASS.entities2();

        assertTrue(catalog.contains(Talent.CEASELESS_RAGE));
        assertTrue(catalog.contains(Talent.MIRRORED_REVENGE));
        assertTrue(catalog.contains(Talent.BLOODTHIRSTY_BERSERK));
        assertTrue(catalog.contains(Talent.COMBO_FOCUS));
        assertTrue(catalog.contains(Talent.RELENTLESS_COMBAT));
        assertTrue(catalog.contains(Talent.COMBO_MASTERY));
        assertTrue(catalog.contains(Talent.LONG_ARM));
        assertTrue(catalog.contains(Talent.ARCANE_CONFLUENCE));
        assertTrue(catalog.contains(Talent.FOCUSED_CASTING));
        assertTrue(catalog.contains(Talent.FINE_TASTING));
        assertTrue(catalog.contains(Talent.BONE_DEEP));
        assertTrue(catalog.contains(Talent.MIND_IMPRISONMENT));
        assertTrue(catalog.contains(Talent.UNEXPECTED_STRIKE));
        assertTrue(catalog.contains(Talent.CLOSING_STAGE));
        assertTrue(catalog.contains(Talent.PERFECT_FINALE));
        assertTrue(catalog.contains(Talent.FREERUNNER_AFTERIMAGE));
        assertTrue(catalog.contains(Talent.MOMENTUM_RESERVE));
        assertTrue(catalog.contains(Talent.WARMUP_PREPARATION));
		assertTrue(catalog.contains(Talent.WEAPON_ABILITY_MASTER));
		assertTrue(catalog.contains(Talent.SKILLED_PARRY));
		assertTrue(catalog.contains(Talent.ALTERNATING_WEAPONS));
		assertTrue(catalog.contains(Talent.NATURAL_WAY));
		assertTrue(catalog.contains(Talent.INNER_PEACE));
		assertTrue(catalog.contains(Talent.YIN_YANG_BALANCE));
    }

    @Test
    public void subclassCatalogFollowsClassSubclassAndPoolOrder() {
        LinkedHashSet<Talent> expected = new LinkedHashSet<>();
        for (HeroClass heroClass : HeroClass.values()) {
            if (heroClass == HeroClass.RATKING) {
                continue;
            }
            for (HeroSubClass subClass : heroClass.subClasses()) {
                expected.addAll(Talent.subclassTalentPool(subClass));
            }
        }

        assertEquals(new ArrayList<>(expected), TalentCatalog.SUBCLASS.entities2());
    }

    @Test
    public void expandedSubclassTalentsRecordMetamorphosisStatistics() {
        int previousUses = TalentCatalog.useCount(Talent.CEASELESS_RAGE);
        int previousAppearances = TalentCatalog.appearanceCount(Talent.CEASELESS_RAGE);
        try {
            TalentCatalog.countTrackedUses(Talent.CEASELESS_RAGE, 1);
            TalentCatalog.countTrackedAppearances(Talent.CEASELESS_RAGE, 1);

            assertEquals(previousUses + 1, TalentCatalog.useCount(Talent.CEASELESS_RAGE));
            assertEquals(previousAppearances + 1,
                    TalentCatalog.appearanceCount(Talent.CEASELESS_RAGE));
        } finally {
            TalentCatalog.countTrackedUses(Talent.CEASELESS_RAGE, -1);
            TalentCatalog.countTrackedAppearances(Talent.CEASELESS_RAGE, -1);
        }
    }
}
