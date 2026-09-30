package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharSpriteCursedBurningVisibilityTest {

    private Level previousLevel;
    private TestLevel level;

    @Before
    public void setUp() {
        previousLevel = Dungeon.level;
        level = new TestLevel();
        level.setSize(7, 7);
        Dungeon.level = level;
    }

    @After
    public void tearDown() {
        Dungeon.level = previousLevel;
    }

    @Test
    public void cursedBurningRequiresItsOwnerToBeInHeroFov() {
        Gnoll owner = new Gnoll();
        owner.pos = 24;

        level.heroFOV[owner.pos] = true;
        assertTrue(CharSprite.cursedBurningVisible(owner, true));

        level.heroFOV[owner.pos] = false;
        assertFalse(CharSprite.cursedBurningVisible(owner, true));
    }

    @Test
    public void cursedBurningCannotShowWhenSpriteIsHiddenOrPositionInvalid() {
        Gnoll owner = new Gnoll();
        owner.pos = 24;
        level.heroFOV[owner.pos] = true;
        assertFalse(CharSprite.cursedBurningVisible(owner, false));

        owner.pos = -1;
        assertFalse(CharSprite.cursedBurningVisible(owner, true));
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
