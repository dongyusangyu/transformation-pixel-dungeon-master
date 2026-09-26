package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EtherealBody;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.slime.RapidWaterfall;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.food.ChargrilledMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SoulRoastMeat;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.watabou.utils.Bundle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CursedFlameTest {
    private static HeadlessItemSprites sprites;
    @BeforeClass public static void installSheets() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void restoreSheets() { sprites.close(); }
    private Level previous;
    private TestLevel level;
    private static final int CENTER = 24;

    @Before public void setUp() {
        previous = Dungeon.level;
        level = new TestLevel();
        level.setSize(7, 7);
        level.blobs = new HashMap<>();
        level.heaps = new com.watabou.utils.SparseArray<>();
        level.plants = new com.watabou.utils.SparseArray<>();
        level.traps = new com.watabou.utils.SparseArray<>();
        level.customTiles = new ArrayList<>();
        Dungeon.level = level;
    }

    @After public void tearDown() { Dungeon.level = previous; }

    @Test public void seededLifetimeIsExactEvenOnWaterOrPit() {
        // Removing decrement or adding automatic water/pit extinguishing must fail.
        for (int duration : new int[]{2, 4, 5, 8}) {
            for (int terrain = 0; terrain < 3; terrain++) {
                level.water[CENTER] = terrain == 1;
                level.pit[CENTER] = terrain == 2;
                CursedFlame flame = Blob.seed(CENTER, duration, CursedFlame.class);
                for (int tick = 1; tick <= duration; tick++) {
                    flame.act();
                    assertEquals("duration=" + duration + ", tick=" + tick, duration - tick, flame.cur[CENTER]);
                }
                level.blobs.clear();
            }
        }
    }

    @Test public void overlappingSeedsAddTheirRemainingLifetime() {
        CursedFlame flame = Blob.seed(CENTER, 2, CursedFlame.class);
        flame.act();
        assertSame(flame, Blob.seed(CENTER, 4, CursedFlame.class));
        assertEquals(5, flame.cur[CENTER]);
        for (int i = 0; i < 5; i++) flame.act();
        assertEquals(0, flame.cur[CENTER]);
    }

    @Test public void bundleRoundTripRetainsBothSourceContributions() {
        CursedFlame flame = Blob.seed(CENTER, 2, CursedFlame.class);
        Blob.seed(CENTER, 4, CursedFlame.class);
        flame.act();
        Bundle saved = new Bundle();
        flame.storeInBundle(saved);
        CursedFlame restored = new CursedFlame();
        restored.restoreFromBundle(saved);
        assertEquals(5, restored.cur[CENTER]);
        assertEquals(5, restored.volume);
        for (int i = 0; i < 5; i++) restored.act();
        assertEquals(0, restored.cur[CENTER]);
    }

    @Test public void cursedFlameOnlyChangesRawMeatConversion() {
        Heap cursed = new Heap();
        cursed.pos = CENTER;
        cursed.items.add(new MysteryMeat());
        cursed.items.add(FrozenCarpaccio.cook(new MysteryMeat()));
        level.heaps.put(CENTER, cursed);
        cursed.burnWithCursedFlame();
        assertTrue(cursed.items.stream().anyMatch(i -> i instanceof SoulRoastMeat));
        assertFalse(cursed.items.stream().anyMatch(i -> i instanceof ChargrilledMeat));

        Heap ordinary = new Heap();
        ordinary.pos = CENTER + 1;
        ordinary.items.add(new MysteryMeat());
        level.heaps.put(ordinary.pos, ordinary);
        ordinary.burn();
        assertTrue(ordinary.items.getFirst() instanceof ChargrilledMeat);
    }

    @Test public void evolvingAtInteriorEdgeDoesNotReadPastArray() {
        CursedFlame flame = Blob.seed(8, 2, CursedFlame.class);
        flame.act();
        assertEquals(1, flame.cur[8]);
    }

    @Test public void flammableNeighbourCatchesThenBurnsOutWithoutTouchingOrdinaryFire() {
        level.flamable[CENTER + 1] = true;
        CursedFlame flame = Blob.seed(CENTER, 2, CursedFlame.class);
        flame.act();
        assertEquals(4, flame.cur[CENTER + 1]);
        assertNull(level.blobs.get(Fire.class));
        for (int i = 0; i < 4; i++) flame.act();
        assertEquals(0, flame.cur[CENTER + 1]);
        assertEquals(1, level.destroyed);
    }

    @Test public void existingFlameOnNonflammableSolidTerrainIsRemovedWithoutBurning() {
        level.solid[CENTER] = true;
        level.passable[CENTER] = false;
        CursedFlame flame = Blob.seed(CENTER, 2, CursedFlame.class);

        flame.act();

        assertEquals(0, flame.cur[CENTER]);
        assertEquals(0, flame.volume);
    }

    @Test public void existingFlameOnFlammableSolidTerrainRemainsActive() {
        level.solid[CENTER] = true;
        level.flamable[CENTER] = true;
        level.passable[CENTER] = false;
        CursedFlame flame = Blob.seed(CENTER, 2, CursedFlame.class);

        flame.act();

        assertEquals(1, flame.cur[CENTER]);
    }

    @Test public void occupiedCellDealsEnvironmentDamageAndAppliesBurnOncePerTick() {
        RecordingGnoll target = new RecordingGnoll();
        target.pos = CENTER;
        Actor.add(target);
        try {
            CursedFlame flame = Blob.seed(CENTER, 2, CursedFlame.class);
            flame.act();
            assertEquals(2, target.hits); // environment + immediate new burn
            assertNotNull(target.buff(CursedBurning.class));
            flame.act();
            assertEquals(4, target.hits); // exactly one environment and one refresh
        } finally { Actor.remove(target); }
    }

    @Test public void cleansingStopsBurnButNotEnvironmentHit() {
        RecordingGnoll target = new RecordingGnoll();
        target.pos = CENTER;
        Actor.add(target);
        try {
            Buff.affect(target, PotionOfCleansing.Cleanse.class);
            Blob.seed(CENTER, 2, CursedFlame.class).act();
            assertEquals(1, target.hits);
            assertNull(target.buff(CursedBurning.class));
        } finally { Actor.remove(target); }
    }

    @Test public void etherealBodyMakesCursedFlameHarmless() {
        RecordingGnoll target = new RecordingGnoll();
        target.pos = CENTER;
        Actor.add(target);
        try {
            Buff.affect(target, EtherealBody.class, EtherealBody.DURATION);
            assertTrue(target.isImmune(CursedFlame.class));
            Blob.seed(CENTER, 2, CursedFlame.class).act();
            assertEquals(0, target.hits);
            assertNull(target.buff(CursedBurning.class));
        } finally { Actor.remove(target); }
    }

    @Test public void freezingAffectClearsCursedFlameRatherThanWaterAlone() {
        CursedFlame flame = Blob.seed(CENTER, 4, CursedFlame.class);
        level.water[CENTER] = true;
        flame.act();
        assertEquals(3, flame.cur[CENTER]);
        Freezing.affect(CENTER);
        assertEquals(0, flame.cur[CENTER]);
    }

    @Test public void activeFreezingBlobExtinguishesCursedFlame() {
        CursedFlame flame = Blob.seed(CENTER, 4, CursedFlame.class);
        Blob.seed(CENTER, 2, Freezing.class).act();
        assertEquals(0, flame.cur[CENTER]);
    }

    @Test public void geyserWaterBurstClearsCursedFlameButPlainWaterDoesNot() {
        CursedFlame flame = Blob.seed(CENTER, 4, CursedFlame.class);
        level.water[CENTER] = true;
        flame.act();
        assertEquals(3, flame.cur[CENTER]);
        GeyserTrap geyser = new GeyserTrap();
        geyser.pos = CENTER;
        geyser.activate();
        assertEquals(0, flame.cur[CENTER]);
    }

    @Test public void thrownPotionSplashClearsCursedFlameWithoutOrdinaryFire() {
        CursedFlame flame = Blob.seed(CENTER, 4, CursedFlame.class);
        assertNull(level.blobs.get(Fire.class));
        new SplashTestPotion().splashAt(CENTER);
        assertEquals(0, flame.cur[CENTER]);
    }

    @Test public void rapidWaterfallClearsCursedFlameWithoutOrdinaryFire() {
        CursedFlame flame = Blob.seed(CENTER, 4, CursedFlame.class);
        Blob.seed(CENTER, 2, RapidWaterfall.RapidWater.class).act();
        assertEquals(0, flame.cur[CENTER]);
    }

    private static class SplashTestPotion extends Potion {
        void splashAt(int cell) { splash(cell); }
    }

    private static class RecordingGnoll extends Gnoll {
        int hits;
        RecordingGnoll() { HT = HP = 1000; }
        @Override public float resist(Class effect) { return 1f; }
        @Override public void damage(int amount, Object source, DamageTag... tags) { hits++; }
    }

    private static class TestLevel extends Level {
        int destroyed;
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
        @Override public void destroy(int cell) { destroyed++; flamable[cell] = false; }
    }
}
