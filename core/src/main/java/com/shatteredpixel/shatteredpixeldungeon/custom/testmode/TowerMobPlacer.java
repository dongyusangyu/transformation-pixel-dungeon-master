package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.List;

public class TowerMobPlacer extends TestItem {

    private static final String AC_PLACE = "place";
    private static final String AC_SET = "set";
    private String categoryKey = "test_bosses";
    private String mobClassName;

    {
        image = ItemSpriteSheet.MOB_HOLDER;
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
                                cell, 0);
                        curUser.next();
                    } catch (MobPlacementService.PlacementException exception) {
                        MobPlacementService.logFailure(TowerMobPlacer.class, exception.failure());
                    }
                }

                @Override
                public String prompt() {
                    return M.L(TowerMobPlacer.class, "prompt");
                }
            });
        } else if (AC_SET.equals(action)) {
            GameScene.show(new WndSetBoss());
        }
    }

    private MobPlacementCatalog.Page selectedPage() {
        MobPlacementCatalog.Page page = MobPlacementCatalog.findPage(
                MobPlacementCatalog.bossPages(), categoryKey);
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
        new MobPlacementState(categoryKey, mobClassName, 0).storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        MobPlacementState state = MobPlacementState.restoreBoss(bundle);
        categoryKey = state.categoryKey;
        mobClassName = state.mobClassName;
        selectedClass();
    }

    static List<Class<? extends Mob>> bossesOnPageForTest(int page) {
        List<MobPlacementCatalog.Page> pages = MobPlacementCatalog.bossPages();
        if (page < 0 || page >= pages.size()) return java.util.Collections.emptyList();
        return pages.get(page).entries();
    }

    static List<String> pageKeysForTest() {
        ArrayList<String> keys = new ArrayList<>();
        for (MobPlacementCatalog.Page page : MobPlacementCatalog.bossPages()) {
            keys.add(page.key());
        }
        return keys;
    }

    private class WndSetBoss extends Window {
        private static final int WIDTH = 140;
        private static final int GRID_MAX_HEIGHT = 76;
        private static final int GRID_TOP = 25;
        private static final int SECTION_GAP = 4;
        private static final int WINDOW_BOTTOM_GAP = 8;
        private final List<MobPlacementCatalog.Page> pages = MobPlacementCatalog.bossPages();
        private int pageIndex;
        private final RenderedTextBlock pageTitle;
        private final RenderedTextBlock selectedBoss;
        private final MobSelectionGrid bossGrid;

        WndSetBoss() {
            pageIndex = pageIndexFor(categoryKey);
            pageTitle = PixelScene.renderTextBlock("", 8);
            selectedBoss = PixelScene.renderTextBlock("", 8);
            pageTitle.hardlight(Window.TITLE_COLOR);
            selectedBoss.hardlight(Window.TITLE_COLOR);
            add(pageTitle);
            add(selectedBoss);

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

            bossGrid = new MobSelectionGrid(WIDTH, GRID_MAX_HEIGHT, new MobSelectionGrid.Listener() {
                @Override
                public void onSelected(Class<? extends Mob> mobClass) {
                    mobClassName = mobClass.getName();
                    updateText();
                }
            });
            add(bossGrid);
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
            bossGrid.setEntries(pages.get(pageIndex).entries(), selectedClass());
            updateText();
        }

        private void updateText() {
            pageTitle.text(M.L(TowerMobPlacer.class,
                    "category_" + pages.get(pageIndex).key()));
            pageTitle.maxWidth(WIDTH - 50);
            pageTitle.setPos((WIDTH - pageTitle.width()) / 2f, 5);
            selectedBoss.text(MobPlacementService.displayName(selectedClass()));
            selectedBoss.maxWidth(WIDTH);
            layoutWindow();
        }

        private void layoutWindow() {
            float selectedTop = GRID_TOP + bossGrid.height() + SECTION_GAP;
            resize(WIDTH, (int) Math.ceil(selectedTop + selectedBoss.height() + WINDOW_BOTTOM_GAP));
            bossGrid.setRect(0, GRID_TOP, WIDTH, bossGrid.height());
            selectedBoss.setPos((WIDTH - selectedBoss.width()) / 2f, selectedTop);
        }
    }
}
