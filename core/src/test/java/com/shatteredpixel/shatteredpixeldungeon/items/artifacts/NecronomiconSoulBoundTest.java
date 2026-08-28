package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NecronomiconSoulBoundTest {

    @Test
    public void unmarkedMobContinuesNormalDeath() {
        Mob enemy = new Rat();
        enemy.alignment = Char.Alignment.ENEMY;
        assertEquals(Necronomicon.SoulBoundDeathResult.CONTINUE_DEATH,
                Necronomicon.resolveSoulBoundDeath(enemy, null));
    }

    @Test
    public void soulBoundMarkerIsRecognizedExactlyOnce() {
        Mob enemy = new Rat();
        Buff.affect(enemy, Necronomicon.SoulBound.class);
        assertTrue(Necronomicon.hasSoulBound(enemy));
    }
}
