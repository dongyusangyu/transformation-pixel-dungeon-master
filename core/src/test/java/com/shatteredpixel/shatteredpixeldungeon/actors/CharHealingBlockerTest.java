package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CharHealingBlockerTest {

    @Test
    public void healingBlockerPreventsAllHealingAndReleasesNormally() {
        TestChar target = new TestChar();
        target.HT = 100;
        target.HP = 25;
        BlockingBuff blocker = new BlockingBuff();
        assertTrue(blocker.attachTo(target));

        assertEquals(0, target.heal(40, false));
        assertEquals(25, target.HP);

        blocker.detach();
        assertEquals(40, target.heal(40, false));
        assertEquals(65, target.HP);
    }

    private static class BlockingBuff extends Buff implements Char.HealingBlocker {
        @Override public boolean blocksIncomingHealing() { return true; }
    }

    private static class TestChar extends Char {
        @Override public int attackSkill(Char target) { return 0; }
        @Override public int defenseSkill(Char enemy) { return 0; }
        @Override public int drRoll() { return 0; }
    }
}
