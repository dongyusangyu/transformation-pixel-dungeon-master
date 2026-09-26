package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;

public class TowerBossSlashMarksTest {

    @Test
    public void marksPersistAcrossBuffTicksAndExposeTheirStackCount() {
        TestChar target = new TestChar();
        TowerBossSlashMarks.add(target, 11);
        TowerBossSlashMarks.add(target, 11);

        TowerBossSlashMarks marks = target.buff(TowerBossSlashMarks.class);
        marks.act();

        assertNotNull(target.buff(TowerBossSlashMarks.class));
        assertTrue(marks.cooldown() < Float.MAX_VALUE);
        assertEquals("2", marks.iconTextDisplay());
        assertTrue(marks.icon() != BuffIndicator.NONE);
    }

    @Test
    public void marksCapAtThreeAndAreConsumedOnlyByTheirOwner() {
        TestChar target = new TestChar();
        for (int i = 0; i < 5; i++) TowerBossSlashMarks.add(target, 11);
        TowerBossSlashMarks.add(target, 12);
        assertEquals(3, TowerBossSlashMarks.consume(target, 11));
        assertEquals(0, TowerBossSlashMarks.consume(target, 11));
        assertEquals(1, TowerBossSlashMarks.consume(target, 12));
    }

    @Test
    public void marksSurviveBundleRoundTrip() {
        TestChar target = new TestChar();
        TowerBossSlashMarks.add(target, 11);
        TowerBossSlashMarks.add(target, 11);
        Bundle bundle = new Bundle();
        target.buff(TowerBossSlashMarks.class).storeInBundle(bundle);
        TowerBossSlashMarks restored = new TowerBossSlashMarks();
        restored.restoreFromBundle(bundle);
        assertEquals(2, restored.consume(11));
    }

    @Test
    public void onlyBasicMeleeConsumesMarks() {
        assertEquals(true, TowerBoss.consumesMarks(false, DamageTag.MELEE, DamageTag.PHYSICAL));
        assertEquals(false, TowerBoss.consumesMarks(true, DamageTag.MELEE, DamageTag.PHYSICAL));
        assertEquals(false, TowerBoss.consumesMarks(false, DamageTag.RANGED, DamageTag.PHYSICAL));
    }

    @Test
    public void physicalRangedDeliveryDoesNotCountAsBasicMelee() {
        TestBoss boss = new TestBoss();
        assertTrue(boss.isBasicMeleeAttack(DamageTag.PHYSICAL));
        boss.usePhysicalRangedDelivery();
        assertEquals(false, boss.isBasicMeleeAttack(DamageTag.PHYSICAL));
        assertEquals(false, boss.isBasicMeleeAttack(DamageTag.MAGICAL));
    }

    @Test
    public void skillMarksOnlyApplyDuringTowerRulesAndClearPerBoss() {
        int oldBranch = Dungeon.branch;
        int oldDepth = Dungeon.depth;
        try {
            Dungeon.depth = 5;
            TestBoss boss = new TestBoss();
            TestChar target = new TestChar();
            Dungeon.branch = TowerLevel.BRANCH;
            boss.skillHit(target);
            assertEquals(1, TowerBossSlashMarks.stacks(target, boss.id()));

            TowerBossSlashMarks.add(target, 99);
            TowerBossSlashMarks.clearOwner(target, boss.id());
            assertEquals(0, TowerBossSlashMarks.stacks(target, boss.id()));
            assertEquals(1, TowerBossSlashMarks.stacks(target, 99));

            Dungeon.branch = 0;
            boss.skillHit(target);
            assertEquals(0, TowerBossSlashMarks.stacks(target, boss.id()));
        } finally {
            Dungeon.branch = oldBranch;
            Dungeon.depth = oldDepth;
        }
    }

    @Test
    public void sharedBossRulesAreTowerOnly() {
        int oldBranch = Dungeon.branch;
        int oldDepth = Dungeon.depth;
        try {
            Dungeon.branch = 0;
            Dungeon.depth = 10;
            assertEquals(false, TowerBoss.towerRulesActive());
            Dungeon.branch = TowerLevel.BRANCH;
            Dungeon.depth = 1;
            assertEquals(true, TowerBoss.towerRulesActive());
        } finally {
            Dungeon.branch = oldBranch;
            Dungeon.depth = oldDepth;
        }
    }

    private static class TestChar extends Char {
        TestChar() {
            HT = HP = 1;
        }

        @Override public int attackSkill(Char target) { return 0; }
        @Override public int defenseSkill(Char enemy) { return 0; }
        @Override public int drRoll() { return 0; }
    }

    private static class TestBoss extends TowerBoss {
        @Override public String towerBossId() { return "test"; }
        void usePhysicalRangedDelivery() { beginPhysicalRangedAttack(); }
        void skillHit(Char target) { markSkillHit(target); }
    }
}
