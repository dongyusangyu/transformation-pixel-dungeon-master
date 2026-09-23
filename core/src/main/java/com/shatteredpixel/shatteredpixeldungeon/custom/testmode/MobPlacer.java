package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MobPlacer extends TestItem {

    private static final String AC_PLACE = "place";
    private static final String AC_SET = "set";
    private String categoryKey = "regional";
    private String mobClassName;
    private int eliteOptions;

    {
        image = ItemSpriteSheet.CANDLE;
        defaultAction = AC_PLACE;
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
        if (AC_PLACE.equals(action)) {
            GameScene.selectCell(new CellSelector.Listener() {
                @Override
                public void onSelect(Integer cell) {
                    if (cell == null) return;
                    try {
                        MobPlacementService.place(selectedClass(), selectedPage().spawnMode(),
                                cell, eliteOptions);
                        curUser.next();
                    } catch (MobPlacementService.PlacementException exception) {
                        MobPlacementService.logFailure(MobPlacer.class, exception.failure());
                    }
                }

                @Override
                public String prompt() {
                    return M.L(MobPlacer.class, "prompt");
                }
            });
        } else if (AC_SET.equals(action)) {
            GameScene.show(new WndSetMob());
        }
    }

    private MobPlacementCatalog.Page selectedPage() {
        MobPlacementCatalog.Page page = MobPlacementCatalog.findPage(
                MobPlacementCatalog.mobPages(), categoryKey);
        categoryKey = page.key();
        return page;
    }

    private Class<? extends Mob> selectedClass() {
        MobPlacementCatalog.Page page = selectedPage();
        Class<? extends Mob> selected = MobPlacementCatalog.resolveClass(page, mobClassName);
        mobClassName = selected == null ? null : selected.getName();
        return selected;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        new MobPlacementState(categoryKey, mobClassName, eliteOptions).storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        MobPlacementState state = MobPlacementState.restoreMob(bundle);
        categoryKey = state.categoryKey;
        mobClassName = state.mobClassName;
        eliteOptions = state.eliteOptions;
        selectedClass();
    }

    String categoryKeyForTest() {
        return categoryKey;
    }

    Class<? extends Mob> mobClassForTest() {
        return selectedClass();
    }

    int eliteOptionsForTest() {
        return eliteOptions;
    }

    void selectForTest(String categoryKey, Class<? extends Mob> mobClass, int eliteOptions) {
        this.categoryKey = categoryKey;
        this.mobClassName = mobClass == null ? null : mobClass.getName();
        this.eliteOptions = eliteOptions;
        selectedClass();
    }

    private class WndSetMob extends Window {
        private static final int WIDTH = 140;
        private static final int GRID_MAX_HEIGHT = 76;
        private static final int ELITE_HEIGHT = 58;
        private static final int GRID_TOP = 25;
        private static final int SECTION_GAP = 4;
        private static final int WINDOW_BOTTOM_GAP = 8;
        private static final int ELITE_ROW_HEIGHT = 16;

        private final List<MobPlacementCatalog.Page> pages = MobPlacementCatalog.mobPages();
        private int pageIndex;
        private final RenderedTextBlock pageTitle;
        private final RenderedTextBlock selectedMob;
        private final RenderedTextBlock eliteTitle;
        private final MobSelectionGrid mobGrid;
        private final Component eliteContent = new Component();
        private final ScrollPane elitePane = new ScrollPane(eliteContent);
        private final ArrayList<CheckBox> eliteChecks = new ArrayList<>();

        WndSetMob() {
            pageIndex = pageIndexFor(categoryKey);
            pageTitle = PixelScene.renderTextBlock("", 8);
            selectedMob = PixelScene.renderTextBlock("", 8);
            eliteTitle = PixelScene.renderTextBlock(M.L(MobPlacer.class, "elite"), 8);
            pageTitle.hardlight(Window.TITLE_COLOR);
            selectedMob.hardlight(Window.TITLE_COLOR);
            eliteTitle.hardlight(Window.TITLE_COLOR);
            add(pageTitle);
            add(selectedMob);
            add(eliteTitle);

            RedButton previous = new RedButton("<<<", 8) {
                @Override
                protected void onClick() {
                    changePage(-1);
                }
            };
            previous.setRect(2, 2, 24, 18);
            add(previous);

            RedButton next = new RedButton(">>>", 8) {
                @Override
                protected void onClick() {
                    changePage(1);
                }
            };
            next.setRect(WIDTH - 26, 2, 24, 18);
            add(next);

            mobGrid = new MobSelectionGrid(WIDTH, GRID_MAX_HEIGHT, new MobSelectionGrid.Listener() {
                @Override
                public void onSelected(Class<? extends Mob> mobClass) {
                    mobClassName = mobClass.getName();
                    updateText();
                }
            });
            add(mobGrid);

            for (int i = 0; i < MobPlacementService.ELITE_BUFFS.size(); i++) {
                final int option = i;
                CheckBox check = new CheckBox(M.L(MobPlacer.class, "elite_name" + i)) {
                    @Override
                    protected void createChildren() {
                        super.createChildren();
                        hotArea.blockLevel = PointerArea.NEVER_BLOCK;
                    }

                    @Override
                    protected void onClick() {
                        super.onClick();
                        if (checked()) eliteOptions |= 1 << option;
                        else eliteOptions &= ~(1 << option);
                    }
                };
                check.checked((eliteOptions & (1 << i)) != 0);
                eliteContent.add(check);
                eliteChecks.add(check);
            }
            add(elitePane);
            refreshGrid();
        }

        private int pageIndexFor(String key) {
            for (int i = 0; i < pages.size(); i++) {
                if (pages.get(i).key().equals(key)) return i;
            }
            return 0;
        }

        private void changePage(int delta) {
            pageIndex = (pageIndex + delta + pages.size()) % pages.size();
            categoryKey = pages.get(pageIndex).key();
            mobClassName = pages.get(pageIndex).entries().get(0).getName();
            refreshGrid();
        }

        private void refreshGrid() {
            MobPlacementCatalog.Page page = pages.get(pageIndex);
            mobGrid.setEntries(page.entries(), selectedClass());
            updateText();
        }

        private void updateText() {
            MobPlacementCatalog.Page page = pages.get(pageIndex);
            pageTitle.text(M.L(MobPlacer.class, "category_" + page.key()));
            pageTitle.maxWidth(WIDTH - 50);
            pageTitle.setPos((WIDTH - pageTitle.width()) / 2f, 5);
            selectedMob.text(MobPlacementService.displayName(selectedClass()));
            selectedMob.maxWidth(WIDTH);
            layoutWindow();
        }

        private void layoutWindow() {
            float selectedTop = GRID_TOP + mobGrid.height() + SECTION_GAP;
            float eliteTitleTop = selectedTop + selectedMob.height() + SECTION_GAP;
            float eliteTop = eliteTitleTop + eliteTitle.height() + 2;
            resize(WIDTH, (int) Math.ceil(eliteTop + ELITE_HEIGHT + WINDOW_BOTTOM_GAP));
            mobGrid.setRect(0, GRID_TOP, WIDTH, mobGrid.height());
            selectedMob.setPos((WIDTH - selectedMob.width()) / 2f, selectedTop);
            eliteTitle.setPos((WIDTH - eliteTitle.width()) / 2f, eliteTitleTop);
            int eliteRows = (eliteChecks.size() + 1) / 2;
            eliteContent.setRect(0, 0, WIDTH, eliteRows * ELITE_ROW_HEIGHT);
            for (int i = 0; i < eliteChecks.size(); i++) {
                CheckBox check = eliteChecks.get(i);
                int row = i / 2;
                int column = i % 2;
                check.setRect(column * WIDTH / 2f, row * ELITE_ROW_HEIGHT,
                        WIDTH / 2f, ELITE_ROW_HEIGHT);
            }
            elitePane.setRect(0, eliteTop, WIDTH, ELITE_HEIGHT);
        }

        @Override
        public void onBackPressed() {
            for (int i = 0; i < eliteChecks.size(); i++) {
                if (eliteChecks.get(i).checked()) eliteOptions |= 1 << i;
                else eliteOptions &= ~(1 << i);
            }
            super.onBackPressed();
        }
    }
}
