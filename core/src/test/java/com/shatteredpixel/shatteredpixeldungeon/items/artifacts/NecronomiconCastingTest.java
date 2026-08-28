package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NecronomiconCastingTest {

    @Test
    public void summonLimitScalesWithArtifactLevel() {
        assertEquals(2, Necronomicon.summonLimit(0));
        assertEquals(7, Necronomicon.summonLimit(5));
        assertEquals(7, Necronomicon.summonLimit(99));
    }

    @Test
    public void soulBoundAcceptsOnlyOrdinaryEnemies() {
        Mob enemy = new Rat();
        enemy.alignment = Char.Alignment.ENEMY;
        assertTrue(Necronomicon.isValidSoulBoundTarget(enemy));

        Mob wraith = new Wraith();
        wraith.alignment = Char.Alignment.ENEMY;
        assertFalse(Necronomicon.isValidSoulBoundTarget(wraith));
    }
}
