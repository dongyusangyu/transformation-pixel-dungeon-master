package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PlagueBrazier;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.noosa.Game;
import com.watabou.utils.SparseArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlagueBrazierWaterTest {

    private Files previousFiles;
    private String previousVersion;
    private Level previousLevel;

    @Before
    public void setUp() {
        previousFiles = Gdx.files;
        previousVersion = Game.version;
        previousLevel = Dungeon.level;
        Game.version = "test";
        GdxNativesLoader.load();
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
                new Class<?>[]{Files.class}, (proxy, method, args) -> {
                    if (method.getReturnType() == FileHandle.class) {
                        File assets = new File("core/src/main/assets");
                        if (!assets.isDirectory()) assets = new File("src/main/assets");
                        return new FileHandle(new File(assets, (String) args[0]));
                    }
                    return null;
                });
    }

    @After
    public void tearDown() {
        Gdx.files = previousFiles;
        Game.version = previousVersion;
        Dungeon.level = previousLevel;
    }

    @Test
    public void onlyTowerBossPurifierSurvivesWaterTerrainChange() throws Exception {
        Level tower = allocate(TowerBossLevel.class);
        Level regular = allocate(SewerLevel.class);
        Trap purifier = new PlagueBrazier();
        assertTrue(Level.preserveTrapOnTerrainChange(tower, purifier, Terrain.WATER));
        assertFalse(Level.preserveTrapOnTerrainChange(regular, purifier, Terrain.WATER));
        assertFalse(Level.preserveTrapOnTerrainChange(tower, purifier, Terrain.EMPTY));
    }

    @Test
    public void waterEffectsCannotCoverTowerPurifierButStillRemoveNormalTraps() throws Exception {
        TowerBossLevel tower = allocate(TowerBossLevel.class);
        tower.setSize(7, 7);
        tower.traps = new SparseArray<>();
        tower.customTiles = new ArrayList<>();
        PlagueBrazier purifier = new PlagueBrazier();
        tower.traps.put(24, purifier);
        Level.set(24, Terrain.TRAP, tower);
        Dungeon.level = tower;
        assertFalse(tower.setCellToWater(true, 24));
        assertFalse(tower.water[24]);
        assertTrue(tower.map[24] == Terrain.TRAP);
        assertTrue(tower.traps.get(24) == purifier);

        Level.set(24, Terrain.WATER, tower);
        assertFalse(tower.water[24]);
        assertTrue(tower.map[24] == Terrain.TRAP);
        assertTrue(tower.traps.get(24) == purifier);

        SewerLevel regular = allocate(SewerLevel.class);
        regular.setSize(7, 7);
        regular.traps = new SparseArray<>();
        regular.traps.put(24, new PlagueBrazier());
        Level.set(24, Terrain.WATER, regular);
        assertTrue(regular.water[24]);
        assertTrue(regular.traps.get(24) == null);
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (T) ((Unsafe) field.get(null)).allocateInstance(type);
    }
}
