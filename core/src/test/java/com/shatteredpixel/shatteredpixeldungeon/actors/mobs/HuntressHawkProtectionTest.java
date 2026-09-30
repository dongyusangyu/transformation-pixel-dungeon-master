package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.watabou.utils.Bundle;
import org.junit.Test;
import static org.junit.Assert.*;

public class HuntressHawkProtectionTest {
    private com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero previousHero;
    private com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites sprites;
    @org.junit.Before public void setUp() {
        sprites = new com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites();
        previousHero = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero =
                com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory.create();
    }
    @org.junit.After public void tearDown() {
        com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = previousHero;
        sprites.close();
    }
    @Test public void hawkStartsWithTwentyHealthAndIgnoresTwoPositiveHits() {
        HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
        assertEquals(20, hawk.HT);
        hawk.damage(100, this);
        assertEquals(20, hawk.HP);
        hawk.damage(100, this);
        assertEquals(20, hawk.HP);
        hawk.damage(3, this);
        assertEquals(17, hawk.HP);
        HuntressBoss.DistractingHawk other = new HuntressBoss.DistractingHawk();
        other.damage(100, this);
        assertEquals(20, other.HP);
    }

    @Test public void protectionPersistsAndZeroDamageDoesNotConsumeIt() {
        HuntressBoss.DistractingHawk hawk = new HuntressBoss.DistractingHawk();
        hawk.damage(0, this);
        hawk.damage(1, this);
        Bundle bundle = new Bundle();
        hawk.storeInBundle(bundle);
        assertEquals(1, bundle.getInt("protected_hits"));
        HuntressBoss.DistractingHawk restored = new HuntressBoss.DistractingHawk();
        restored.restoreFromBundle(bundle);
        restored.damage(100, this);
        assertEquals(20, restored.HP);
        Bundle saved = new Bundle();
        restored.storeInBundle(saved);
        assertEquals(0, saved.getInt("protected_hits"));
    }
}
