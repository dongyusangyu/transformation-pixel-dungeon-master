package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class CursedFlameElementalBlastTest {
    private Level oldLevel;
    private TestLevel level;

    @Before public void setUp() {
        oldLevel = Dungeon.level;
        level = new TestLevel();
        level.setSize(7, 7);
        level.blobs = new HashMap<>();
        level.heaps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.traps = new SparseArray<>();
        level.customTiles = new ArrayList<>();
        for (int i = 0; i < level.length(); i++) {
            level.map[i] = Terrain.EMPTY;
            level.passable[i] = true;
        }
        Dungeon.level = level;
    }

    @After public void tearDown() { Dungeon.level = oldLevel; }

    @SuppressWarnings("unchecked")
    @Test public void craftedWandHasGreenConeEffectAndSixtySevenPercentStormDamage() throws Exception {
        Field effectsField = ElementalBlast.class.getDeclaredField("effectTypes");
        Field factorsField = ElementalBlast.class.getDeclaredField("damageFactors");
        effectsField.setAccessible(true);
        factorsField.setAccessible(true);
        Map<Class<?>, Integer> effects = (Map<Class<?>, Integer>) effectsField.get(null);
        Map<Class<?>, Float> factors = (Map<Class<?>, Float>) factorsField.get(null);
        Integer effect = effects.get(WandOfCursedFlame.class);
        assertNotNull("missing effect type would unbox null during blast animation", effect);
        assertEquals("cursed flame storm should emit the dedicated green cone",
                MagicMissile.CURSED_FLAME_CONE, effect.intValue());
        Float factor = factors.get(WandOfCursedFlame.class);
        assertNotNull("missing damage factor would unbox null during damage calculation", factor);
        assertEquals(0.67f, factor, 0.0001f);
    }

    @Test public void stormLastsFourTurnsOrUpToEightWithElementalPower() throws Exception {
        assertEquals(4, duration(0));
        assertEquals(6, duration(2));
        assertEquals(8, duration(4));
        assertEquals(8, duration(8));
    }

    @Test public void stormIgnitesWaterAndPitButNotHeroesTile() throws Exception {
        int heroCell = 24;
        level.water[25] = true;
        level.pit[31] = true;
        seed(heroCell, heroCell, 4);
        seed(25, heroCell, 4);
        seed(31, heroCell, 4);
        CursedFlame flame = (CursedFlame) level.blobs.get(CursedFlame.class);
        assertNotNull(flame);
        assertEquals(0, flame.cur[heroCell]);
        assertEquals(4, flame.cur[25]);
        assertEquals(4, flame.cur[31]);
    }

    @Test public void stormOpensDoorsAndIgnitesTheirTile() throws Exception {
        int door = 25;
        level.map[door] = Terrain.DOOR;
        level.flamable[door] = true;
        seed(door, 24, 4);
        assertEquals(Terrain.OPEN_DOOR, level.map[door]);
        assertEquals(4, ((CursedFlame) level.blobs.get(CursedFlame.class)).cur[door]);
    }

    @Test public void stormDoesNotSeedNonflammableWall() throws Exception {
        int wall = 25;
        level.map[wall] = Terrain.WALL;
        level.solid[wall] = true;
        level.passable[wall] = false;

        seed(wall, 24, 4);

        assertNull(level.blobs.get(CursedFlame.class));
    }

    private static int duration(int elementalPowerPoints) throws Exception {
        Method method = method("cursedFlameDuration", int.class);
        return (Integer) method.invoke(null, elementalPowerPoints);
    }

    private static void seed(int cell, int heroCell, int duration) throws Exception {
        Method method = method("seedCursedFlame", int.class, int.class, int.class);
        method.invoke(null, cell, heroCell, duration);
    }

    private static Method method(String name, Class<?>... args) throws Exception {
        Method method;
        try {
            method = ElementalBlast.class.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException missing) {
            fail("ElementalBlast must provide the real storm-cell behavior: " + name);
            return null;
        }
        method.setAccessible(true);
        return method;
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }
}
