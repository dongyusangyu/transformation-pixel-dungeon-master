package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.DeathKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Drunkenness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Exhilaration;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArena;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CharDamageModifierTest {
    @Test
    public void multiplierInterfaceIsAppliedToOutgoingAndIncomingDamage() {
        TestChar attacker = new TestChar();
        TestChar defender = new TestChar();
        Buff.affect(attacker, Outgoing.class);
        Buff.affect(defender, Incoming.class);
        assertEquals(96, defender.finalDamage(100, attacker, DamageTag.PHYSICAL));
    }

    @Test
    public void globalBanquetSourceIgnoresWineDamageModifiers() {
        TestChar defender = new TestChar();
        Buff.affect(defender, Drunkenness.class);
        assertEquals(10, defender.finalDamage(10, new GentlemanElfArena(), DamageTag.MAGICAL));
        Exhilaration.affect(defender);
        assertEquals(12, defender.finalDamage(10, new TestChar(), DamageTag.MAGICAL));
    }

    public static class Outgoing extends Buff implements Char.DamageMultiplier {
        @Override public float outgoingDamageMultiplier(Object source, DamageTag... tags) { return 0.8f; }
    }
    public static class Incoming extends Buff implements Char.DamageMultiplier {
        @Override public float incomingDamageMultiplier(Object source, DamageTag... tags) { return 1.2f; }
    }
    public static class TestChar extends DeathKnight {
        int finalDamage(int damage, Object source, DamageTag... tags) {
            return modifyFinalDamage(damage, source, tags);
        }
    }
}
