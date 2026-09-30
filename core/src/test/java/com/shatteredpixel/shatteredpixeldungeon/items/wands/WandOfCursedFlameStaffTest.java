package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.noosa.Game;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/** Exercises the real battlemage wand hook, not just the cooldown storage format. */
public class WandOfCursedFlameStaffTest {

    private static HeadlessItemSprites sprites;
    private Hero oldHero;
    private int oldDepth;
    private float oldNow;
    private float oldDuration;
    private Hero hero;

    @BeforeClass public static void installSheets() { sprites = new HeadlessItemSprites(); }
    @AfterClass public static void restoreSheets() { sprites.close(); }

    @Before public void setUp() throws Exception {
        oldHero = Dungeon.hero;
        oldDepth = Dungeon.depth;
        oldNow = Actor.now();
        oldDuration = Statistics.duration;
        hero = TestHeroFactory.create();
        hero.HT = hero.HP = 100;
        Dungeon.hero = hero;
        Dungeon.depth = 1;
        Statistics.duration = 0;
        setNow(100);
    }

    @After public void tearDown() throws Exception {
        Dungeon.hero = oldHero;
        Dungeon.depth = oldDepth;
        Statistics.duration = oldDuration;
        setNow(oldNow);
    }

    @Test public void unburnedTargetDoesNotProc() {
        RecordingGnoll target = new RecordingGnoll();
        WandOfCursedFlame wand = new WandOfCursedFlame();
        wand.onHit(null, hero, target, 8);
        assertTrue(target.wandHits.isEmpty());
        assertNull(target.buff(CursedBurning.class));
    }

    @Test public void procConsumesExistingBurnAndRefreshesFourTurnsWithoutAnExtraStatusHit() {
        RecordingGnoll target = burnedTarget();
        WandOfCursedFlame wand = new WandOfCursedFlame();
        wand.level(3);
        wand.onHit(null, hero, target, 8);
        assertEquals(1, target.wandHits.size());
        assertTrue("(1+4)*U[2,6] at level 3", target.wandHits.get(0) >= 10);
        assertTrue(target.wandHits.get(0) <= 30);
        assertNotNull(target.buff(CursedBurning.class));
		assertEquals(4f, target.buff(CursedBurning.class).remaining(), 0.001f);
        assertTrue("the staff proc is cursed burning rather than a magic zap",
                target.lastWandTags.contains(DamageTag.PHYSICAL));
        assertFalse(target.lastWandTags.contains(DamageTag.MAGICAL));
        assertTrue(target.lastWandTags.contains(DamageTag.FIRE));
    }

    @Test public void cooldownIsPerTargetSurvivesReloadAndExpiresAfterNineTurns() throws Exception {
        RecordingGnoll first = burnedTarget();
        RecordingGnoll second = burnedTarget();
        WandOfCursedFlame wand = new WandOfCursedFlame();
        wand.onHit(null, hero, first, 8);
        wand.onHit(null, hero, first, 8);
        wand.onHit(null, hero, second, 8);
        assertEquals(1, first.wandHits.size());
        assertEquals(1, second.wandHits.size());

        Bundle bundle = new Bundle();
        wand.storeInBundle(bundle);
        WandOfCursedFlame restored = new WandOfCursedFlame();
        restored.restoreFromBundle(bundle);
        setNow(108.99f);
        restored.onHit(null, hero, first, 8);
        assertEquals("loading cannot reset the first target's cooldown", 1, first.wandHits.size());
        setNow(109f);
        restored.onHit(null, hero, first, 8);
        assertEquals(2, first.wandHits.size());
    }

    @Test public void oldFloorCooldownCannotBlockAReusedTargetId() {
        RecordingGnoll target = burnedTarget();
        WandOfCursedFlame wand = new WandOfCursedFlame();
        wand.onHit(null, hero, target, 8);
        Dungeon.depth = 2;
        wand.onHit(null, hero, target, 8);
        assertEquals(2, target.wandHits.size());
    }

    @Test public void savedCooldownKeepsElapsedTimeWhenActorClockIsRebased() throws Exception {
        Level previousLevel = Dungeon.level;
        Bundle previousActorState = new Bundle();
        Actor.storeNextID(previousActorState);
        try {
            Actor.clear();
            Actor.resetNextID();
            setNow(100f);
            Dungeon.level = new PlainLevel();
            Actor.add(hero);
            RecordingGnoll target = burnedTarget();
            Actor.add(target);
            WandOfCursedFlame wand = new WandOfCursedFlame();
            wand.onHit(null, hero, target, 8);
            Actor.fixTime();
            assertEquals(100f, Statistics.duration, 0.001f);
            Bundle actorState = new Bundle();
            Actor.storeNextID(actorState);
            Bundle wandState = new Bundle();
            wand.storeInBundle(wandState);

            Actor.clear();
            Actor.restoreNextID(actorState);
            Actor.add(hero);
            Actor.add(target);
            WandOfCursedFlame restored = new WandOfCursedFlame();
            restored.restoreFromBundle(wandState);
            setNow(8.99f);
            restored.onHit(null, hero, target, 8);
            assertEquals(1, target.wandHits.size());
            setNow(9f);
            restored.onHit(null, hero, target, 8);
            assertEquals(2, target.wandHits.size());
        } finally {
            Actor.clear();
            Actor.restoreNextID(previousActorState);
            Dungeon.level = previousLevel;
        }
    }

    @Test public void vaultRebasePreservesNineTurnCooldownWithoutChangingStatisticsDuration() throws Exception {
        Level previousLevel = Dungeon.level;
        String previousVersion = Game.version;
        Bundle previousActorState = new Bundle();
        Actor.storeNextID(previousActorState);
        try {
            Actor.clear();
            Actor.resetNextID();
            setNow(100f);
            Game.version = "0.3.0-test";
            Dungeon.level = new VaultLevel();
            Actor.add(hero);
            RecordingGnoll target = burnedTarget();
            Actor.add(target);
            WandOfCursedFlame wand = new WandOfCursedFlame();
            wand.onHit(null, hero, target, 8);
            assertEquals(1, target.wandHits.size());

            Actor.fixTime();
            assertEquals("VaultLevel must not add to the displayed run duration", 0f,
                    Statistics.duration, 0.001f);
            Bundle actorState = new Bundle();
            Actor.storeNextID(actorState);
            Bundle wandState = new Bundle();
            wand.storeInBundle(wandState);
            Actor.clear();
            Actor.restoreNextID(actorState);
            Actor.add(hero);
            Actor.add(target);
            WandOfCursedFlame restored = new WandOfCursedFlame();
            restored.restoreFromBundle(wandState);

            setNow(8.99f);
            restored.onHit(null, hero, target, 8);
            assertEquals(1, target.wandHits.size());
            setNow(9f);
            restored.onHit(null, hero, target, 8);
            assertEquals("rebase must not turn a 9-turn cooldown into 109 turns",
                    2, target.wandHits.size());
        } finally {
            Actor.clear();
            Actor.restoreNextID(previousActorState);
            Dungeon.level = previousLevel;
            Game.version = previousVersion;
        }
    }

    @Test public void clearingActorsBetweenFloorsKeepsUnusedClockTime() throws Exception {
        Level previousLevel = Dungeon.level;
        String previousVersion = Game.version;
        Bundle previousActorState = new Bundle();
        Actor.storeNextID(previousActorState);
        try {
            Actor.clear();
            Actor.resetNextID();
            setNow(100f);
            Game.version = "0.3.0-test";
            Dungeon.level = new VaultLevel();
            Actor.add(hero);
            RecordingGnoll target = burnedTarget();
            Actor.add(target);
            WandOfCursedFlame wand = new WandOfCursedFlame();
            wand.onHit(null, hero, target, 8);
            assertEquals(1, target.wandHits.size());

            // Five turns pass after the proc; fixTime consumes only the actors' 100-turn base.
            setNow(105f);
            Actor.fixTime();
            assertEquals(105f, Actor.absoluteTime(), 0.001f);
            Actor.clear();
            assertEquals("floor transition must preserve the five unrebased turns",
                    105f, Actor.absoluteTime(), 0.001f);

            Dungeon.depth = 2;
            Dungeon.depth = 1; // returning to the old floor and the original target ID
            Actor.add(hero);
            Actor.add(target);
            setNow(3.99f);
            wand.onHit(null, hero, target, 8);
            assertEquals(1, target.wandHits.size());
            setNow(4f);
            wand.onHit(null, hero, target, 8);
            assertEquals(2, target.wandHits.size());

            Actor.clear();
            Actor.resetNextID();
            assertEquals("a new game still starts at time zero", 0f,
                    Actor.absoluteTime(), 0.001f);
        } finally {
            Actor.clear();
            Actor.restoreNextID(previousActorState);
            Dungeon.level = previousLevel;
            Game.version = previousVersion;
        }
    }

    @Test public void empoweredStrikeBoostsOnlyImmediateDamageNotRefreshedBurn() {
        RecordingGnoll baseline = burnedTarget();
        RecordingGnoll empowered = burnedTarget();
        WandOfCursedFlame plain = new WandOfCursedFlame();
        WandOfCursedFlame boosted = new WandOfCursedFlame();
        LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
        tier.put(Talent.EMPOWERED_STRIKE, 2);
        hero.talents.add(tier);

        Random.pushGenerator(421);
        try { plain.onHit(null, hero, baseline, 8); }
        finally { Random.popGenerator(); }
        Buff.affect(hero, Talent.EmpoweredStrikeTracker.class, 10f);
        Random.pushGenerator(421);
        try { boosted.onHit(null, hero, empowered, 8); }
        finally { Random.popGenerator(); }

        assertEquals(1, baseline.wandHits.size());
        assertEquals(1, empowered.wandHits.size());
        assertEquals(baseline.wandHits.get(0) * 2, empowered.wandHits.get(0).intValue());
		assertEquals(4f, empowered.buff(CursedBurning.class).remaining(), 0.001f);
    }

    private static RecordingGnoll burnedTarget() {
        RecordingGnoll target = new RecordingGnoll();
        CursedBurning.apply(target, 4f);
        target.wandHits.clear();
        return target;
    }

    private static void setNow(float now) throws Exception {
        Field field = Actor.class.getDeclaredField("now");
        field.setAccessible(true);
        field.setFloat(null, now);
    }

    private static class RecordingGnoll extends Gnoll {
        final List<Integer> wandHits = new ArrayList<>();
        List<DamageTag> lastWandTags = new ArrayList<>();
        RecordingGnoll() { HT = HP = 1000; }
        @Override public void damage(int damage, Object source, DamageTag... tags) {
            if (source instanceof WandOfCursedFlame) {
                wandHits.add(damage);
                lastWandTags = java.util.Arrays.asList(tags);
            }
        }
    }

    private static class PlainLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
