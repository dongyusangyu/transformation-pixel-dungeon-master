package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;

import static org.junit.Assert.*;

public class DeathKnightSlashVisibilityTest {
    private Level oldLevel;
    private float oldElapsed;
    private HeadlessItemSprites sprites;
    private Level level;

    @Before public void setUp() {
        oldLevel = Dungeon.level;
        oldElapsed = Game.elapsed;
        sprites = new HeadlessItemSprites();
        sprites.addSheet(Assets.Effects.DEATH_KNIGHT_SLASH, 64, 16);
        level = new Level() {
            @Override protected boolean build() { return true; }
            @Override protected void createMobs() { }
            @Override protected void createItems() { }
        };
        level.setSize(64, 64);
        Dungeon.level = level;
    }

    @After public void tearDown() {
        Dungeon.level = oldLevel;
        Game.elapsed = oldElapsed;
        sprites.close();
    }

    @Test public void cameraFarFromMapOriginStillSeesTheFirstSwordWaveFrame() throws Exception {
        int cell = 30 * 64 + 31;
        level.heroFOV[cell] = true;
        DeathKnightSlash effect = effect(cell);
        assertTrue("batch must be visible before draw assigns individual tile positions", effect.isVisible());
    }

    @Test public void anOffscreenLastTileCannotHideOtherVisibleTiles() throws Exception {
        int visibleCell = 30 * 64 + 31;
        int offscreenCell = 50 * 64 + 50;
        level.heroFOV[visibleCell] = level.heroFOV[offscreenCell] = true;
        DeathKnightSlash effect = effect(visibleCell, offscreenCell);
        effect.x = effect.y = 800;
        assertTrue(effect.isVisible());
    }

    @Test public void hiddenOrOffscreenCellsDoNotMakeTheBatchVisible() throws Exception {
        DeathKnightSlash hidden = effect(30 * 64 + 31);
        assertFalse(hidden.isVisible());
        int offscreen = 50 * 64 + 50;
        level.heroFOV[offscreen] = true;
        assertFalse(effect(offscreen).isVisible());
    }

    @Test public void explicitVisibilityAndLevelChangesAreRespected() throws Exception {
        int cell = 30 * 64 + 31;
        level.heroFOV[cell] = true;
        DeathKnightSlash effect = effect(cell);
        effect.visible = false;
        assertFalse(effect.isVisible());
        effect.visible = true;
        Dungeon.level = oldLevel;
        assertFalse(effect.isVisible());
        effect.update();
        assertFalse(effect.exists);
    }

    @Test public void playbackEndsWithoutWaitingForAnyActorCallback() throws Exception {
        DeathKnightSlash effect = effect(30 * 64 + 31);
        Group group = new Group();
        group.add(effect);
        Game.elapsed = 0.5f;
        group.update();
        assertFalse(effect.exists);
        assertFalse(group.members.contains(effect));
    }

    @Test public void allFourSixteenPixelFramesContainVisiblePixels() throws Exception {
        File file = new File("src/main/assets/effects/death_knight_slash.png");
        if (!file.exists()) file = new File("core", file.getPath());
        BufferedImage sheet = ImageIO.read(file);
        assertEquals(64, sheet.getWidth());
        assertEquals(16, sheet.getHeight());
        for (int frame = 0; frame < 4; frame++) {
            int opaque = 0;
            for (int y = 0; y < 16; y++) for (int x = frame * 16; x < (frame + 1) * 16; x++) {
                if ((sheet.getRGB(x, y) >>> 24) != 0) opaque++;
            }
            assertTrue("frame " + frame + " must not be blank", opaque > 0);
        }
    }

    private DeathKnightSlash effect(int... cells) throws Exception {
        Constructor<DeathKnightSlash> constructor = DeathKnightSlash.class.getDeclaredConstructor(
                Level.class, int.class, DeathKnightSlash.Entry[].class);
        constructor.setAccessible(true);
        DeathKnightSlash result = constructor.newInstance(level, 30 * 64 + 30,
                DeathKnightSlash.plan(64, 4096, 30 * 64 + 30, cells, null));
        result.camera = new Camera(0, 0, 80, 80, 1);
        result.camera.scroll.set(480, 480);
        return result;
    }
}
