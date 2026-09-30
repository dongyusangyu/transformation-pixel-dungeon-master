package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.Game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CursedFlameParticleVisibilityTest {

    private Level previousLevel;
    private TestLevel level;
    private HeadlessItemSprites sprites;

    @Before
    public void setUp() {
        previousLevel = Dungeon.level;
        sprites = new HeadlessItemSprites();
        level = new TestLevel();
        level.setSize(7, 7);
        Dungeon.level = level;
    }

    @After
    public void tearDown() {
        Dungeon.level = previousLevel;
        sprites.close();
    }

    @Test
    public void flameParticleTracksVisibilityOfItsSourceCell() {
        int sourceCell = 24;
        level.heroFOV[sourceCell] = true;
        assertTrue(CursedFlameParticle.isSourceCellVisible(sourceCell));

        level.heroFOV[sourceCell] = false;
        assertFalse(CursedFlameParticle.isSourceCellVisible(sourceCell));

        level.heroFOV[sourceCell] = true;
        assertTrue(CursedFlameParticle.isSourceCellVisible(sourceCell));
    }

    @Test
    public void particleWithInvalidSourceCellIsNeverVisible() {
        assertFalse(CursedFlameParticle.isSourceCellVisible(-1));
    }

    @Test
    public void projectileFactoryEmitsVisibleParticlesAndTracksTheirCurrentCell() {
        Emitter emitter = new Emitter();
        level.heroFOV[24] = true;
        CursedFlameParticle.FACTORY.emit(emitter, 0, 56, 56);
        CursedFlameParticle particle = (CursedFlameParticle) emitter.members.get(0);
        assertTrue(particle.visible);
        particle.x = 72;
        particle.update();
        assertFalse(particle.visible);
        level.heroFOV[25] = true;
        particle.update();
        assertTrue(particle.visible);
    }

    @Test
    public void groundFactoryKeepsItsSourceVisibilityWhileParticlesDrift() {
        Emitter emitter = new Emitter();
        level.heroFOV[24] = true;
        CursedFlameParticle.FACTORY.emit(emitter, 0, 56, 56, 24);
        CursedFlameParticle particle = (CursedFlameParticle) emitter.members.get(0);
        particle.x = 72;
        particle.update();
        assertTrue(particle.visible);
        level.heroFOV[24] = false;
        particle.update();
        assertFalse(particle.visible);
    }

    @Test public void characterParticleRecycledAsProjectileDoesNotKeepCharacterVisibility() {
        Emitter emitter = new Emitter();
        CursedFlameParticle.CHARACTER_FACTORY.emit(emitter, 0, 56, 56);
        CursedFlameParticle particle = (CursedFlameParticle) emitter.members.get(0);
        assertTrue(particle.visible);
        particle.kill();
        CursedFlameParticle.FACTORY.emit(emitter, 0, 56, 56);
        assertFalse(particle.visible);
        level.heroFOV[24] = true;
        particle.update();
        assertTrue(particle.visible);
    }

    @Test public void expiredParticlesRemainHiddenEvenOnVisibleCells() {
        float previousElapsed = Game.elapsed;
        try {
            Emitter emitter = new Emitter();
            level.heroFOV[24] = true;
            CursedFlameParticle.FACTORY.emit(emitter, 0, 56, 56, 24);
            CursedFlameParticle particle = (CursedFlameParticle) emitter.members.get(0);
            Game.elapsed = 1f;
            particle.update();
            assertFalse(particle.alive);
            assertFalse(particle.visible);
        } finally { Game.elapsed = previousElapsed; }
    }

    @Test public void actualCursedFlameMissileAndConeEmitVisibleParticles() {
        float previousElapsed = Game.elapsed;
        try {
            java.util.Arrays.fill(level.heroFOV, true);
            Game.elapsed = 0.02f;
            for (int type : new int[]{
                    com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile.CURSED_FLAME,
                    com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile.CURSED_FLAME_CONE}) {
                com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile missile =
                        new com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile();
                missile.reset(type, new com.watabou.utils.PointF(56, 56),
                        new com.watabou.utils.PointF(88, 56), () -> {});
                missile.update();
                assertTrue(missile.members.stream().anyMatch(p -> p instanceof CursedFlameParticle && p.visible));
                java.util.Arrays.fill(level.heroFOV, false);
                missile.update();
                assertFalse(missile.members.stream().anyMatch(p -> p.visible));
                java.util.Arrays.fill(level.heroFOV, true);
            }
        } finally { Game.elapsed = previousElapsed; }
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
