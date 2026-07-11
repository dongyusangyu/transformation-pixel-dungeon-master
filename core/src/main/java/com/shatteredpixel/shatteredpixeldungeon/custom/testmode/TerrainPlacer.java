package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfAwareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfHealth;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WellWater;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SuperNovaTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestItem;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TrapPlacer;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SacrificeRoom;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class TerrainPlacer extends TestItem {
    {
        image = ItemSpriteSheet.PICKAXE;
        defaultAction = AC_PLACE;
    }

    public int chosen;
    public RenderedTextBlock name;
    private Class<? extends WellWater> chosenWellWater;
    private boolean chosenSacrificialFire;
    private static final String AC_PLACE = "place";
    private static final String AC_SET = "set";
    private static final int OPTION_COLS = 6;
    private static final int OPTION_SIZE = 18;
    private static final int WINDOW_WIDTH = 110;
    private static final int NAME_TOP = 148;

    private static final int[] TERRAIN_OPTIONS = new int[]{
            Terrain.CHASM, Terrain.EMPTY, Terrain.GRASS, Terrain.EMPTY_WELL,
            Terrain.WALL, Terrain.DOOR, Terrain.OPEN_DOOR, Terrain.ENTRANCE,
            Terrain.EXIT, Terrain.EMBERS, Terrain.LOCKED_DOOR, Terrain.PEDESTAL,
            Terrain.WALL_DECO, Terrain.BARRICADE, Terrain.EMPTY_SP, Terrain.HIGH_GRASS,
            Terrain.SECRET_DOOR, Terrain.SECRET_TRAP, Terrain.TRAP, Terrain.INACTIVE_TRAP,
            Terrain.EMPTY_DECO, Terrain.LOCKED_EXIT, Terrain.UNLOCKED_EXIT, Terrain.CUSTOM_DECO,
            Terrain.WELL, Terrain.STATUE, Terrain.STATUE_SP, Terrain.BOOKSHELF,
            Terrain.ALCHEMY, Terrain.WATER, Terrain.FURROWED_GRASS, Terrain.CRYSTAL_DOOR,
            Terrain.CUSTOM_DECO_EMPTY, Terrain.REGION_DECO, Terrain.REGION_DECO_ALT,
            Terrain.MINE_CRYSTAL, Terrain.MINE_BOULDER, Terrain.ENTRANCE_SP,
            Terrain.HERO_LKD_DR
    };

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_PLACE);
        actions.add(AC_SET);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_PLACE)) {
            GameScene.selectCell(new CellSelector.Listener() {
                @Override
                public void onSelect(final Integer cell) {
                    if (chosen == 17 || chosen == 18 || chosen == 19) {
//                        GLog.i("不能放置此类型的地形");
                    } else if (cell != null) {
                        placeTerrain(cell);
                    }
                    curUser.next();
                }
                @Override
                public String prompt() {
                    return M.L(TerrainPlacer.class,"prompt");
                }
            });
        }

        if (action.equals(AC_SET)) {
            GameScene.show(new SettingsWindow());
        }
    }

//    @Override
//    public void storeInBundle(Bundle bundle) {
//        super.storeInBundle(bundle);
//        bundle.put("chosen", chosen);
//    }
//
//    @Override
//    public void restoreFromBundle(Bundle bundle) {
//        super.restoreFromBundle(bundle);
//        chosen = bundle.getInt("chosen");
//    }

    private class SettingsWindow extends Window {

        public SettingsWindow() {
            TerrainButton terrainButton;
            int x;
            int y;

            for (int i = 0;i < TERRAIN_OPTIONS.length;i++) {
                terrainButton = new TerrainButton(TERRAIN_OPTIONS[i], null, false);
                x = i % OPTION_COLS * OPTION_SIZE + 2;
                y = i / OPTION_COLS * OPTION_SIZE + 2;
                terrainButton.setRect(x,y,16,16);
                add(terrainButton);
            }

            int offset = TERRAIN_OPTIONS.length;
            terrainButton = new TerrainButton(Terrain.WELL, WaterOfHealth.class, false);
            x = offset % OPTION_COLS * OPTION_SIZE + 2;
            y = offset / OPTION_COLS * OPTION_SIZE + 2;
            terrainButton.setRect(x,y,16,16);
            add(terrainButton);

            offset++;
            terrainButton = new TerrainButton(Terrain.WELL, WaterOfAwareness.class, false);
            x = offset % OPTION_COLS * OPTION_SIZE + 2;
            y = offset / OPTION_COLS * OPTION_SIZE + 2;
            terrainButton.setRect(x,y,16,16);
            add(terrainButton);

            offset++;
            terrainButton = new TerrainButton(Terrain.PEDESTAL, null, true);
            x = offset % OPTION_COLS * OPTION_SIZE + 2;
            y = offset / OPTION_COLS * OPTION_SIZE + 2;
            terrainButton.setRect(x,y,16,16);
            add(terrainButton);

            name = PixelScene.renderTextBlock("深渊",9);
            name.setPos((WINDOW_WIDTH - name.width()) / 2, NAME_TOP);
            add(name);

            resize(WINDOW_WIDTH, NAME_TOP + 2 + (int)name.height());
        }
    }

    private class TerrainButton extends IconButton {

        public int terrain = -1;
        public Class<? extends WellWater> wellWater;
        public boolean sacrificialFire;

        public TerrainButton(int terrain, Class<? extends WellWater> wellWater, boolean sacrificialFire) {
            this.terrain = terrain;
            this.wellWater = wellWater;
            this.sacrificialFire = sacrificialFire;
            switch (terrain) {
                case 17:
                case 18:
                case 19:
                case 23:
                    break;
                case 29:
                    icon(new Image(Dungeon.level.waterTex(),0, 0, DungeonTilemap.SIZE, DungeonTilemap.SIZE));
                    break;
                default:
                    if (wellWater != null || terrain >= Terrain.CUSTOM_DECO_EMPTY) {
                        icon(DungeonTerrainTilemap.tile(Dungeon.hero != null ? Dungeon.hero.pos : 0, terrain));
                    } else {
                        int x = X.get(terrain) % 16;
                        int y = X.get(terrain) / 16;
                        icon(new Image(Dungeon.level.tilesTex(),x * 16, y * 16, DungeonTilemap.SIZE, DungeonTilemap.SIZE));
                    }
                    break;
            }
        }

        @Override
        public void onClick() {
            chosen = terrain;
            chosenWellWater = wellWater;
            chosenSacrificialFire = sacrificialFire;
            if (wellWater == WaterOfHealth.class) {
                name.text(Messages.get(WaterOfHealth.class, "name"));
                name.setPos((WINDOW_WIDTH - name.width()) / 2, NAME_TOP);
                return;
            } else if (wellWater == WaterOfAwareness.class) {
                name.text(Messages.get(WaterOfAwareness.class, "name"));
                name.setPos((WINDOW_WIDTH - name.width()) / 2, NAME_TOP);
                return;
            } else if (sacrificialFire) {
                name.text(Messages.get(SacrificialFire.class, "name"));
                name.setPos((WINDOW_WIDTH - name.width()) / 2, NAME_TOP);
                return;
            }
            switch (terrain) {
                case 16:
                    name.text("隐藏门");
                    break;
                case 17:
                    name.text("隐藏陷阱");
                    break;
                case 18:
                    name.text("陷阱");
                    break;
                case 19:
                    name.text("已触发陷阱");
                    break;
                case 23:
                    name.text("被移除的告示牌");
                    break;
                default:
                    name.text(Dungeon.level.tileName(terrain));
                    break;
            }
            name.setPos((WINDOW_WIDTH - name.width()) / 2, NAME_TOP);
        }
    }

    private void placeTerrain(int cell) {
        if (chosenSacrificialFire) {
            placeSacrificialFire(cell);
            return;
        }
        clearSpecialBlobs(cell);
        Level.set(cell, chosen);
        if (chosenWellWater != null) {
            GameScene.add(Blob.seed(cell, 1, chosenWellWater));
        }
        GameScene.updateMap(cell);
    }

    private void placeSacrificialFire(int cell) {
        if (!Dungeon.level.insideMap(cell)) {
            return;
        }
        clearSacrificialFire();
        for (int offset : PathFinder.NEIGHBOURS9) {
            int target = cell + offset;
            if (!Dungeon.level.insideMap(target)) {
                continue;
            }
            clearSpecialBlobs(target);
            Level.set(target, Terrain.EMBERS);
            GameScene.updateMap(target);
        }

        Level.set(cell, Terrain.PEDESTAL);
        SacrificialFire fire = Blob.seed(cell, 6 + Dungeon.depth * 4, SacrificialFire.class);
        fire.setPrize(SacrificeRoom.prize(Dungeon.level));
        GameScene.add(fire);
        GameScene.updateMap(cell);
    }

    private void clearSpecialBlobs(int cell) {
        clearWellWater(cell);
        clearBlob(cell, SacrificialFire.class);
    }

    private void clearWellWater(int cell) {
        clearWellWater(cell, WaterOfHealth.class);
        clearWellWater(cell, WaterOfAwareness.class);
    }

    private void clearWellWater(int cell, Class<? extends WellWater> waterClass) {
        WellWater water = (WellWater)Dungeon.level.blobs.get(waterClass);
        if (water != null && water.volume > 0 && water.cur[cell] > 0) {
            water.clear(cell);
        }
    }

    private void clearSacrificialFire() {
        Blob fire = Dungeon.level.blobs.get(SacrificialFire.class);
        if (fire != null && fire.volume > 0) {
            for (int cell = 0; cell < fire.cur.length; cell++) {
                if (fire.cur[cell] > 0) {
                    fire.clear(cell);
                }
            }
        }
    }

    private void clearBlob(int cell, Class<? extends Blob> blobClass) {
        Blob blob = Dungeon.level.blobs.get(blobClass);
        if (blob != null && blob.volume > 0 && blob.cur[cell] > 0) {
            blob.clear(cell);
        }
    }

    private final ArrayList<Integer> X = new ArrayList<>(Arrays.asList(DungeonTileSheet.CHASM,DungeonTileSheet.FLOOR,DungeonTileSheet.GRASS,DungeonTileSheet.EMPTY_WELL,
            DungeonTileSheet.FLAT_WALL,DungeonTileSheet.FLAT_DOOR,DungeonTileSheet.FLAT_DOOR_OPEN,DungeonTileSheet.ENTRANCE,DungeonTileSheet.EXIT,
            DungeonTileSheet.EMBERS,DungeonTileSheet.FLAT_DOOR_LOCKED,DungeonTileSheet.PEDESTAL,DungeonTileSheet.FLAT_WALL_DECO,DungeonTileSheet.FLAT_BARRICADE,
            DungeonTileSheet.FLOOR_SP,DungeonTileSheet.FLAT_HIGH_GRASS,DungeonTileSheet.FLAT_DOOR,-1,-1,-1,DungeonTileSheet.FLOOR_DECO,DungeonTileSheet.LOCKED_EXIT,
            DungeonTileSheet.UNLOCKED_EXIT,-1,DungeonTileSheet.WELL,DungeonTileSheet.FLAT_STATUE,DungeonTileSheet.FLAT_STATUE_SP,
            DungeonTileSheet.FLAT_BOOKSHELF,DungeonTileSheet.FLAT_ALCHEMY_POT,DungeonTileSheet.WATER,DungeonTileSheet.FLAT_FURROWED_GRASS,DungeonTileSheet.FLAT_DOOR_CRYSTAL));
}
