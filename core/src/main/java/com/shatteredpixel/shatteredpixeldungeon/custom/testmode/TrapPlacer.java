package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrapPlacer extends TestItem {
    {
        image = ItemSpriteSheet.RECLAIM_TRAP;
        defaultAction = AC_PLACE;
    }

    private static final String AC_PLACE = "place";
    private static final String AC_SET = "set";
    private static final String TRAP_CLASS = "trap_class";
    private static final int LEGACY_COLS = 8;
    private static final int LEGACY_ROWS = 7;

    //The coordinates are retained only to migrate old test-tool saves and to
    //keep the optional invalid-sprite entries available.
    private int row = 0;
    private int column = 0;
    private int selectedEntry = 0;
    private Class<? extends Trap> selectedTrapClass;
    protected Trap trap = null;
    private boolean triggerWhenPut = false;
    private boolean enableInvalidImage = false;
    //encoding for traps, column = color, row = shape;
    /*
    //trap colors
    public static final int RED     = 0;
    public static final int ORANGE  = 1;
    public static final int YELLOW  = 2;
    public static final int GREEN   = 3;
    public static final int TEAL    = 4;
    public static final int VIOLET  = 5;
    public static final int WHITE   = 6;
    public static final int GREY    = 7;
    public static final int BLACK   = 8;

    //trap shapes
    public static final int DOTS        = 0;
    public static final int WAVES       = 1;
    public static final int GRILL       = 2;
    public static final int STARS       = 3;
    public static final int DIAMOND     = 4;
    public static final int CROSSHAIR   = 5;
    public static final int LARGE_DOT   = 6;
     */

    static List<Class<? extends Trap>> trapClasses() {
        ArrayList<Class<? extends Trap>> result = new ArrayList<>();
        for (Class<?> type : Bestiary.TRAP.entities()) {
            if (Trap.class.isAssignableFrom(type)) {
                @SuppressWarnings("unchecked")
                Class<? extends Trap> trapType = (Class<? extends Trap>) type;
                result.add(trapType);
            }
        }
        return result;
    }

    private static ArrayList<TrapEntry> trapEntries(boolean includeEmptySlots) {
        ArrayList<TrapEntry> result = new ArrayList<>();
        Set<Integer> usedSprites = new HashSet<>();
        for (Class<? extends Trap> type : trapClasses()) {
            Trap instance = Reflection.newInstance(type);
            int sprite = spriteIndex(instance);
            result.add(new TrapEntry(type, sprite));
            if (sprite >= 0) usedSprites.add(sprite);
        }
        if (includeEmptySlots) {
            for (int sprite = 0; sprite < LEGACY_ROWS * LEGACY_COLS; sprite++) {
                if (!usedSprites.contains(sprite)) {
                    result.add(new TrapEntry(null, sprite));
                }
            }
        }
        return result;
    }

    static List<Class<? extends Trap>> trapClassesForDisplay(boolean includeEmptySlots) {
        ArrayList<Class<? extends Trap>> result = new ArrayList<>();
        for (TrapEntry entry : trapEntries(includeEmptySlots)) result.add(entry.type);
        return result;
    }

    private static int spriteIndex(Trap trap) {
        return trap.shape >= 0 && trap.shape < LEGACY_ROWS
                && trap.color >= 0 && trap.color < LEGACY_COLS
                ? trap.shape * LEGACY_COLS + trap.color : -1;
    }

    static float gridContentHeight(int itemCount, int columns, float rowHeight) {
        if (itemCount <= 0 || columns <= 0 || rowHeight <= 0) return 0;
        return ((itemCount + columns - 1) / columns) * rowHeight;
    }

    static int gridIndexAt(float x, float y, int itemCount, int columns,
                           float cellWidth, float cellHeight) {
        if (x < 0 || y < 0 || itemCount <= 0 || columns <= 0
                || cellWidth <= 0 || cellHeight <= 0 || x >= columns * cellWidth) {
            return -1;
        }
        int index = (int) (y / cellHeight) * columns + (int) (x / cellWidth);
        return index < itemCount ? index : -1;
    }

    private static int clampSelection(int selection, int itemCount) {
        return itemCount <= 0 ? 0 : Math.max(0, Math.min(selection, itemCount - 1));
    }

    private static final class TrapEntry {
        private final Class<? extends Trap> type;
        private final int legacySprite;

        private TrapEntry(Class<? extends Trap> type, int legacySprite) {
            this.type = type;
            this.legacySprite = legacySprite;
        }
    }

    static Class<? extends Trap> trapClassFromBundle(Bundle bundle) {
        String className = bundle.contains(TRAP_CLASS) ? bundle.getString(TRAP_CLASS) : null;
        if (className != null && !className.isEmpty()) {
            for (Class<? extends Trap> type : trapClasses()) {
                if (type.getName().equals(className)) return type;
            }
        }

        int legacySprite = bundle.getInt("row") * LEGACY_COLS + bundle.getInt("column");
        for (TrapEntry entry : trapEntries(false)) {
            if (entry.type != null && entry.legacySprite == legacySprite) return entry.type;
        }
        return null;
    }

    static PaneBounds trapPaneBounds(float windowWidth) {
        return new PaneBounds(2f, windowWidth - 4f);
    }

    static final class PaneBounds {
        final float left;
        final float width;

        private PaneBounds(float left, float width) {
            this.left = left;
            this.width = width;
        }
    }

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
            if (trap == null) {
                GLog.w(M.L(this, "null_trap"));
                return;
            }
            GameScene.selectCell(new CellSelector.Listener() {
                @Override
                public void onSelect(final Integer cell) {
                    if (cell != null) {
                        ((TrapPlacer) curItem).setTrap(cell);
                    }
                    curUser.next();
                }
                @Override
                public String prompt() {
                    return M.L(TrapPlacer.class, "prompt");
                }
            });

        } else if (action.equals(AC_SET)) {
            GameScene.show(new SettingsWindow());
        }
    }

    public void setTrap(Integer cell) {
        if (trap == null) {
            GLog.w(M.L(this, "null_trap"));
            return;
        }

        if (!canPlaceTrap(cell)) {
            {
                GLog.w(M.L(this, "invalid_tile"));
                return;
            }
        }

        Trap trapToCreate = trap;
        //Create a fresh instance so placing a trap never reuses its editor state.
        TrapEntry entry = trapEntries(enableInvalidImage).get(selectedEntry);
        if (entry.type == null) {
            trap = new EmptyTrap();
            trap.color = enableInvalidImage ? color() : Trap.BLACK;
            trap.shape = shape();
            trapToCreate = trap;
        }else{
            trap = Reflection.newInstance(entry.type);
            trapToCreate = trap;
        }
        Dungeon.level.setTrap(trapToCreate.reveal(), cell);
        //logic for deciding if triggering trap
        //Dungeon.level.map[cell] = Terrain.TRAP;
        if (!trapToCreate.preservesTerrain()) {
            Level.set(cell, Terrain.TRAP, Dungeon.level);
        }
        GameScene.updateMap(cell);
        //avoid new traps
        //Dungeon.level.avoid[cell] = true;
        //Dungeon.level.passable[cell] = false;

        if(triggerWhenPut) trapToCreate.trigger();
    }

    private boolean canPlaceTrap(int cell) {

        if (Dungeon.level == null || trap == null || cell < 0 || cell >= Dungeon.level.length()) {
            return false;
        }

        return Dungeon.level.heroFOV[cell]
                && trap.canPlaceOnTerrain(Dungeon.level.map[cell]);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("row", row);
        bundle.put("column", column);
        bundle.put(TRAP_CLASS, selectedTrapClass == null ? "" : selectedTrapClass.getName());
        bundle.put("trigger", triggerWhenPut);
        bundle.put("enableInvalid", enableInvalidImage);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        row = bundle.getInt("row");
        column = bundle.getInt("column");
        triggerWhenPut = bundle.getBoolean("trigger");
        enableInvalidImage = bundle.getBoolean("enableInvalid");
        ArrayList<TrapEntry> entries = trapEntries(enableInvalidImage);
        selectedEntry = -1;
        Class<? extends Trap> restoredType = trapClassFromBundle(bundle);
        if (restoredType != null) {
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).type == restoredType) {
                    selectedEntry = i;
                    break;
                }
            }
        }
        if (selectedEntry < 0) {
            int legacySprite = row * LEGACY_COLS + column;
            int emptyEntry = -1;
            for (int i = 0; i < entries.size(); i++) {
                TrapEntry entry = entries.get(i);
                if (entry.legacySprite == legacySprite) {
                    if (entry.type != null) {
                        selectedEntry = i;
                        break;
                    }
                    emptyEntry = i;
                }
            }
            if (selectedEntry < 0) selectedEntry = emptyEntry;
        }
        selectedEntry = clampSelection(selectedEntry, entries.size());
        applySelection(entries.get(selectedEntry));
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", (trap == null ? Messages.get(TrapPlacer.class, "no_trap_selected") : Messages.get(trap.getClass(), "name")));
    }

    private int color(){ return column; }
    private int shape(){ return row; }

    private void applySelection(TrapEntry entry) {
        selectedTrapClass = entry.type;
        if (entry.type == null) {
            row = entry.legacySprite / LEGACY_COLS;
            column = entry.legacySprite % LEGACY_COLS;
            trap = new EmptyTrap();
            trap.color = enableInvalidImage ? color() : Trap.BLACK;
            trap.shape = shape();
        } else {
            trap = Reflection.newInstance(entry.type);
            int sprite = spriteIndex(trap);
            if (sprite >= 0) {
                row = sprite / LEGACY_COLS;
                column = sprite % LEGACY_COLS;
            }
        }
    }

    private class SettingsWindow extends Window {
        private static final int WIDTH = 120;
        private static final int BTN_SIZE = 16;
        private static final int COLS = 7;
        private static final int PANE_HEIGHT = 64;
        private static final int GAP = 2;

        private ArrayList<TrapEntry> entries = trapEntries(enableInvalidImage);
        private final ArrayList<SelectionCell> trapCells = new ArrayList<>();
        private final Component trapContent = new Component();
        private final ScrollPane trapPane;
        private RenderedTextBlock selected;

        public SettingsWindow() {
            super();

            RenderedTextBlock ttl = PixelScene.renderTextBlock(M.L(SettingsWindow.class, "title"), 9);
            PixelScene.align(ttl);
            add(ttl);
            ttl.setPos(1, 1);
            ttl.hardlight(0x44A8E4);

            CheckBox enableInvalid = new CheckBox(M.L(TrapPlacer.class, "invalid_trap_display")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    enableInvalidImage = checked();
                    rebuildTrapCells(true);
                }
            };
            enableInvalid.checked(enableInvalidImage);
            add(enableInvalid);
            enableInvalid.setRect(2, ttl.bottom() + GAP, WIDTH - 4, 16);

            trapPane = new ScrollPane(trapContent) {
                @Override
                public void onClick(float x, float y) {
                    int index = gridIndexAt(x, y, entries.size(), COLS, BTN_SIZE, BTN_SIZE);
                    if (index >= 0) select(index);
                }
            };
            add(trapPane);

            selected = PixelScene.renderTextBlock("", 6);
            selected.maxWidth(WIDTH / 2);
            add(selected);

            CheckBox trigger = new CheckBox(M.L(TrapPlacer.class, "trigger")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    triggerWhenPut = checked();
                }
            };
            trigger.checked(triggerWhenPut);
            add(trigger);

            rebuildTrapCells(false);

            float paneTop = enableInvalid.bottom() + GAP;
            float selectedTop = paneTop + PANE_HEIGHT + GAP;
            selected.setPos(WIDTH / 2f + 4, selectedTop + (16 - selected.height()) / 2f);
            trigger.setRect(1, selectedTop, WIDTH / 2f - GAP, 16);

            // ScrollPane cameras must be laid out after resize centers the window camera.
            resize(WIDTH, (int) Math.ceil(selectedTop + 16 + GAP));
            PaneBounds paneBounds = trapPaneBounds(WIDTH);
            trapPane.setRect(paneBounds.left, paneTop, paneBounds.width, PANE_HEIGHT);
            trapPane.scrollTo(0, 0);
            trapPane.update();
        }

        private void rebuildTrapCells(boolean resetScroll) {
            Class<? extends Trap> previousType = selectedTrapClass;
            int previousSprite = row * LEGACY_COLS + column;
            entries = trapEntries(enableInvalidImage);

            selectedEntry = -1;
            for (int i = 0; i < entries.size(); i++) {
                TrapEntry entry = entries.get(i);
                if (previousType != null && entry.type == previousType) {
                    selectedEntry = i;
                    break;
                }
                if (previousType == null && entry.type == null
                        && entry.legacySprite == previousSprite) {
                    selectedEntry = i;
                }
            }
            selectedEntry = clampSelection(selectedEntry, entries.size());
            applySelection(entries.get(selectedEntry));

            trapContent.clear();
            trapCells.clear();
            for (int i = 0; i < entries.size(); i++) {
                SelectionCell cell = new SelectionCell();
                cell.setRect((i % COLS) * BTN_SIZE, (i / COLS) * BTN_SIZE, BTN_SIZE, BTN_SIZE);
                trapContent.add(cell);
                trapCells.add(cell);
            }
            trapContent.setRect(0, 0, COLS * BTN_SIZE,
                    Math.max(BTN_SIZE, gridContentHeight(entries.size(), COLS, BTN_SIZE)));
            refreshImages();
            refreshSelection();
            updateText();
            if (resetScroll) trapPane.scrollTo(0, 0);
        }

        private void select(int index) {
            selectedEntry = index;
            applySelection(entries.get(index));
            refreshSelection();
            updateText();
        }

        private void updateText() {
            selected.text(statDesc());
        }

        private String statDesc() {
            if (trap == null || selectedTrapClass == null) return M.L(this, "null_trap");
            return Messages.get(selectedTrapClass, "name");
        }

        private void refreshImages() {
            for (int i = 0; i < entries.size(); i++) {
                TrapEntry entry = entries.get(i);
                int sprite = entry.legacySprite;
                if (entry.type == null && !enableInvalidImage) {
                    trapCells.get(i).setIcon(new Image(Assets.Environment.TERRAIN_FEATURES,
                            128, 16 * (sprite / LEGACY_COLS), 16, 16));
                } else if (sprite >= 0) {
                    trapCells.get(i).setIcon(new Image(Assets.Environment.TERRAIN_FEATURES,
                            16 * (sprite % LEGACY_COLS), 16 * (sprite / LEGACY_COLS), 16, 16));
                } else {
                    trapCells.get(i).setIcon(new Image(Assets.Environment.TERRAIN_FEATURES, 128, 0, 16, 16));
                }
            }
        }

        private void refreshSelection() {
            for (int i = 0; i < trapCells.size(); i++) trapCells.get(i).selected(i == selectedEntry);
        }

        @Override
        public void offset(int xOffset, int yOffset) {
            super.offset(xOffset, yOffset);
            trapPane.setPos(trapPane.left(), trapPane.top());
        }

        private class SelectionCell extends Component {
            private final NinePatch background = Chrome.get(Chrome.Type.RED_BUTTON);
            private Image icon;

            private SelectionCell() {
                add(background);
            }

            private void setIcon(Image icon) {
                if (this.icon != null) remove(this.icon);
                this.icon = icon;
                add(icon);
                layout();
            }

            private void selected(boolean selected) {
                background.resetColor();
                background.alpha(selected ? 1f : 0.45f);
            }

            @Override
            protected void layout() {
                background.x = x;
                background.y = y;
                background.size(width, height);
                if (icon != null) {
                    icon.x = x + (width - icon.width()) / 2f;
                    icon.y = y + (height - icon.height()) / 2f;
                    PixelScene.align(icon);
                }
            }
        }
    }

    public static class EmptyTrap extends Trap{
        @Override
        public void activate() {

        }

        {
            active = true;
            canBeHidden = false;
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put("trap_shape_val", shape);
            bundle.put("trap_color_val", color);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            shape = bundle.getInt("trap_shape_val");
            color = bundle.getInt("trap_color_val");
        }
    }
}
