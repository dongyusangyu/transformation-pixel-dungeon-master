package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

import org.junit.Test;

import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Test
    public void sameCellBallisticaDoesNotWalkAwayFromItsSource() {
        Level previous = Dungeon.level;
        try {
            TestLevel level = new TestLevel();
            level.setSize(9, 9);
            Arrays.fill(level.passable, true);
            Arrays.fill(level.solid, false);
            Dungeon.level = level;

            Ballistica shot = new Ballistica(40, 40, Ballistica.MAGIC_BOLT);

            assertEquals(Integer.valueOf(40), shot.collisionPos);
            assertEquals(1, shot.path.size());
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void projectileTrajectoryStopsAtTargetAndSolidTerrain() {
        Level previous = Dungeon.level;
        try {
            TestLevel level = new TestLevel();
            level.setSize(9, 9);
            Arrays.fill(level.passable, true);
            Arrays.fill(level.solid, false);
            Dungeon.level = level;

            Ballistica clearShot = new Ballistica(40, 43, Ballistica.PROJECTILE);
            assertEquals(Integer.valueOf(43), clearShot.collisionPos);

            level.solid[42] = true;
            Ballistica blockedShot = new Ballistica(40, 43, Ballistica.PROJECTILE);
            assertEquals(Integer.valueOf(42), blockedShot.collisionPos);
        } finally {
            Dungeon.level = previous;
        }
    }

    @Test
    public void necronomiconUsesProjectileCollisionRules() throws Exception {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        Path coreDirectory = Files.isDirectory(workingDirectory.resolve("core"))
                ? workingDirectory.resolve("core") : workingDirectory;
        Path sourcePath = coreDirectory.resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/Necronomicon.java");
        String source = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);

        assertTrue(source.contains(
                "new Ballistica(curUser.pos, target, Ballistica.PROJECTILE)"));
    }

    private static class TestLevel extends Level {
        @Override
        protected boolean build() {
            return true;
        }

        @Override
        protected void createMobs() {
        }

        @Override
        protected void createItems() {
        }
    }
}
