package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MobSelectionGrid extends Component {

    interface Listener {
        void onSelected(Class<? extends Mob> mobClass);
    }

    static final int COLUMNS = 5;
    static final float CELL_SIZE = 24f;
    static final float GAP = 2f;
    static final float ICON_SIZE = 20f;

    private final Component content = new Component();
    private final ScrollPane pane;
    private final Listener listener;
    private final int maxVisibleRows;
    private final ArrayList<SelectionCell> cells = new ArrayList<>();
    private List<Class<? extends Mob>> entries = Collections.emptyList();
    private Class<? extends Mob> selected;

    MobSelectionGrid(float width, float viewportHeight, Listener listener) {
        this.listener = listener;
        pane = new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                int index = indexAt(x, y, entries.size(), COLUMNS,
                        CELL_SIZE, GAP, MobSelectionGrid.this.width());
                if (index >= 0) selectIndex(index);
            }
        };
        add(pane);
        this.width = width;
        this.height = viewportHeight;
        maxVisibleRows = Math.max(1, (int) ((viewportHeight + GAP) / (CELL_SIZE + GAP)));
    }

    void setEntries(List<Class<? extends Mob>> entries,
                   Class<? extends Mob> selected) {
        this.entries = entries == null ? Collections.<Class<? extends Mob>>emptyList()
                : entries;
        this.selected = selected;
        content.clear();
        cells.clear();

        for (int i = 0; i < this.entries.size(); i++) {
            final Class<? extends Mob> type = this.entries.get(i);
            SelectionCell cell = new SelectionCell(createSprite(type));
            cell.setRect(gridLeft(this.entries.size(), i),
                    (i / COLUMNS) * (CELL_SIZE + GAP), CELL_SIZE, CELL_SIZE);
            cell.selected(type == this.selected);
            content.add(cell);
            cells.add(cell);
        }

        int count = Math.max(1, this.entries.size());
        height = viewportHeightForEntries(count, COLUMNS, maxVisibleRows, CELL_SIZE, GAP);
        content.setRect(0, 0, width(), contentHeight(count, COLUMNS, CELL_SIZE, GAP));
        pane.scrollTo(0, 0);
        layout();
    }

    void scrollToTop() {
        pane.scrollTo(0, 0);
    }

    Class<? extends Mob> selected() {
        return selected;
    }

    private void selectIndex(int index) {
        selected = entries.get(index);
        for (int i = 0; i < cells.size(); i++) {
            cells.get(i).selected(i == index);
        }
        if (listener != null) listener.onSelected(selected);
    }

    static float contentHeight(int count, int columns, float cell, float gap) {
        if (count <= 0 || columns <= 0) return cell;
        int rows = (count + columns - 1) / columns;
        return rows * cell + (rows - 1) * gap;
    }

    static float viewportHeightForEntries(int count, int columns, int maxRows,
                                          float cell, float gap) {
        if (columns <= 0 || maxRows <= 0) return cell;
        int rows = count <= 0 ? 1 : (count + columns - 1) / columns;
        rows = Math.min(rows, maxRows);
        return rows * cell + (rows - 1) * gap;
    }

    static int indexAt(float x, float y, int count, int columns,
                       float cell, float gap, float width) {
        if (count <= 0 || columns <= 0 || x < 0 || y < 0) return -1;
        float stride = cell + gap;
        int row = (int) (y / stride);
        if (y - row * stride >= cell) return -1;
        int rowStart = row * columns;
        if (rowStart >= count) return -1;
        int rowCount = Math.min(columns, count - rowStart);
        float left = (width - (rowCount * cell + (rowCount - 1) * gap)) / 2f;
        if (x < left) return -1;
        int column = (int) ((x - left) / stride);
        if (column < 0 || column >= rowCount
                || x - left - column * stride >= cell) return -1;
        int index = row * columns + column;
        return index < count ? index : -1;
    }

    static float fitScale(float width, float height, float available) {
        if (width <= 0 || height <= 0 || available <= 0) return 1f;
        return Math.min(1f, Math.min(available / width, available / height));
    }

    private float gridLeft(int count, int index) {
        int rowStart = (index / COLUMNS) * COLUMNS;
        int rowCount = Math.min(COLUMNS, count - rowStart);
        return (width() - (rowCount * CELL_SIZE + (rowCount - 1) * GAP)) / 2f
                + (index % COLUMNS) * (CELL_SIZE + GAP);
    }

    private CharSprite createSprite(Class<? extends Mob> type) {
        try {
            Mob mob = Reflection.newInstance(type);
            CharSprite sprite;
            if (mob instanceof ArmoredStatue) {
                sprite = new StatueSprite();
                ((StatueSprite) sprite).setArmor(1);
            } else {
                sprite = mob.sprite();
            }
            float scale = fitScale(sprite.width(), sprite.height(), ICON_SIZE);
            sprite.scale = new PointF(scale, scale);
            return sprite;
        } catch (Exception exception) {
            return new StatueSprite();
        }
    }

    @Override
    protected void layout() {
        if (pane != null && camera() != null) pane.setRect(x, y, width(), height());
    }

    private class SelectionCell extends Component {
        private final NinePatch background = Chrome.get(Chrome.Type.RED_BUTTON);
        private final Image icon;

        SelectionCell(Image icon) {
            this.icon = icon;
            add(background);
            add(icon);
        }

        void selected(boolean value) {
            background.alpha(value ? 1f : 0.45f);
            if (value) background.hardlight(Window.TITLE_COLOR);
            else background.resetColor();
        }

        @Override
        protected void layout() {
            background.x = x;
            background.y = y;
            background.size(width, height);
            icon.x = x + (width - icon.width()) / 2f;
            icon.y = y + (height - icon.height()) / 2f;
            PixelScene.align(icon);
        }

    }
}
