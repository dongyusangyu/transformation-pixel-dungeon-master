package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedFlameDamage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;

import static org.junit.Assert.*;

public class WandOfCursedFlameCastTest {
    private static HeadlessItemSprites sprites;
    private Level oldLevel;
    private Hero oldHero;
    private TestLevel level;
    private Hero hero;

    @BeforeClass public static void installSheets() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void restoreSheets() { sprites.close(); }

    @Before public void setUp() {
        oldLevel = Dungeon.level;
        oldHero = Dungeon.hero;
        level = new TestLevel();
        level.setSize(7, 7);
        level.blobs = new HashMap<>();
        level.heaps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.traps = new SparseArray<>();
        level.customTiles = new ArrayList<>();
        for (int i = 0; i < level.length(); i++) level.passable[i] = true;
        Dungeon.level = level;
        hero = TestHeroFactory.create();
        hero.pos = 17;
        Dungeon.hero = hero;
    }

    @After public void tearDown() {
        Dungeon.level = oldLevel;
        Dungeon.hero = oldHero;
    }

    @Test public void directHitDamagesOnlyCenterButProcessesEachAreaTargetOnce() {
        RecordingGnoll center = new RecordingGnoll(24);
        RecordingGnoll beside = new RecordingGnoll(25);
        Actor.add(center);
        Actor.add(beside);
        try {
            RecordingWand wand = new RecordingWand();
            Ballistica bolt = new Ballistica(hero.pos, center.pos, wand.collisionProperties(center.pos));
            assertEquals(center.pos, bolt.collisionPos.intValue());
            wand.onZap(bolt);
            assertEquals(1, center.hits);
            assertEquals(4, center.damage);
            assertEquals(0, beside.hits);
            assertEquals(1, wand.procs.get(center).intValue());
            assertEquals(1, wand.procs.get(beside).intValue());
        } finally {
            Actor.remove(center);
            Actor.remove(beside);
        }
    }

    @Test public void emptyPitCastSeedsThreeByThreeAndRepeatCastAddsDuration() {
        level.pit[24] = true;
        RecordingWand wand = new RecordingWand();
        Ballistica bolt = new Ballistica(hero.pos, 24, wand.collisionProperties(24));
        assertEquals(24, bolt.collisionPos.intValue());
        wand.onZap(bolt);
        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertNotNull(flame);
        assertEquals(2, flame.cur[24]);
        assertEquals(2, flame.cur[25]);
        assertEquals(0, flame.cur[0]);
        wand.onZap(bolt);
        assertEquals(4, flame.cur[24]);
        assertEquals(4, flame.cur[25]);
    }

    @Test public void wallStopsTargetedShotBeforeSolidCell() {
        level.solid[31] = true;
        level.passable[31] = false;
        RecordingWand wand = new RecordingWand();
        Ballistica bolt = new Ballistica(hero.pos, 38, wand.collisionProperties(38));
        assertEquals(24, bolt.collisionPos.intValue());
        wand.onZap(bolt);
        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertEquals(2, flame.cur[24]);
    }

    @Test public void blastDoesNotSeedNonflammableWallsButCanSeedCombustibleObstacles() {
        level.solid[23] = true;
        level.passable[23] = false;
        level.solid[25] = true;
        level.flamable[25] = true;
        level.passable[25] = false;

        RecordingWand wand = new RecordingWand();
        wand.onZap(new Ballistica(hero.pos, 24, wand.collisionProperties(24)));

        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertNotNull(flame);
        assertEquals(0, flame.cur[23]);
        assertEquals(2, flame.cur[24]);
        assertEquals(2, flame.cur[25]);
    }

    @Test public void anInterveningEnemyInterceptsTheTargetedBolt() {
        RecordingGnoll blocker = new RecordingGnoll(24);
        Actor.add(blocker);
        try {
            RecordingWand wand = new RecordingWand();
            Ballistica bolt = new Ballistica(hero.pos, 31, wand.collisionProperties(31));
            assertEquals("targeting follows the Blast Wave projectile rule", blocker.pos,
                    bolt.collisionPos.intValue());
        } finally {
            Actor.remove(blocker);
        }
    }

    @Test public void movingProcTargetCannotReceiveSecondProcInSameBlast() {
        RecordingGnoll moving = new RecordingGnoll(24);
        Actor.add(moving);
        try {
            RecordingWand wand = new RecordingWand() {
                @Override protected void wandProc(Char target, int chargesUsed) {
                    super.wandProc(target, chargesUsed);
                    target.pos = 25;
                }
            };
            wand.onZap(new Ballistica(hero.pos, 24, wand.collisionProperties(24)));
            assertEquals(1, wand.procs.get(moving).intValue());
        } finally {
            Actor.remove(moving);
        }
    }

    @Test public void directMagicHitUsesSharedCursedFireResistanceAndBothTags() {
        RecordingGnoll fiery = new RecordingGnoll(24) {
            @Override public float resist(Class effect) {
                return effect == CursedFlameDamage.class ? 0.5f : 1f;
            }
        };
        fiery.addProperties(Char.Property.FIERY);
        Actor.add(fiery);
        try {
            RecordingWand wand = new RecordingWand() {
                @Override public int damageRoll(Char target) { return 20; }
            };
            wand.onZap(new Ballistica(hero.pos, fiery.pos, wand.collisionProperties(fiery.pos)));
            assertEquals(8, fiery.damage);
            assertTrue(DamageTag.of(fiery.tags).contains(DamageTag.MAGICAL));
            assertTrue(DamageTag.of(fiery.tags).contains(DamageTag.FIRE));
            assertFalse(DamageTag.of(fiery.tags).contains(DamageTag.PHYSICAL));
        } finally {
            Actor.remove(fiery);
        }
    }

    @Test public void cancelledSelectionDoesNotSpendChargeOrCreateFlame() {
        RecordingWand wand = new RecordingWand();
        int before = wand.curCharges;
        Wand.zapper.onSelect(null);
        assertEquals(before, wand.curCharges);
        assertNull(level.blobs.get(CursedFlame.class));
    }

    private static class RecordingWand extends WandOfCursedFlame {
        final IdentityHashMap<Char, Integer> procs = new IdentityHashMap<>();
        @Override protected void wandProc(Char target, int chargesUsed) {
            procs.put(target, procs.getOrDefault(target, 0) + 1);
        }
        @Override public int damageRoll(Char target) { return 4; }
    }

    private static class RecordingGnoll extends Gnoll {
        int hits;
        int damage;
        DamageTag[] tags;
        RecordingGnoll(int cell) { pos = cell; HT = HP = 1000; }
        @Override public void damage(int amount, Object source, DamageTag... tags) {
            hits++;
            damage += amount;
            this.tags = tags;
        }
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
