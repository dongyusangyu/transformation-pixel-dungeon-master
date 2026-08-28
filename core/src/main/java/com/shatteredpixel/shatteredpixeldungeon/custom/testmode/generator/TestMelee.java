package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Annoying;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Dazzling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Displacing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Explosive;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Friendly;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Heavy;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Polarized;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blooming;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Chilling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.CorrosionEnchanted;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Corrupting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Sweeping;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Unstable;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vampiric;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
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

public class TestMelee extends TestGenerator {
    {
        image = ItemSpriteSheet.WEAPON_HOLDER;
    }

    private int tier = 1;
    private boolean cursed = false;
    private int levelToGen = 0;
    private int enchant_id = 0;
    private int enchant_rarity = 0;
    private int weapon_id = 0;

    @Override
    public ArrayList<String> actions(Hero hero) {
        return super.actions(hero);
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_GIVE)) {
            GameScene.show(new SettingsWindow());
        }
    }

    private Weapon modifyWeapon(Weapon w) {
        if(w instanceof MagesStaff) w = new MagesStaff((Wand)Generator.random(Generator.Category.WAND));
        w.level(levelToGen);
        Class<? extends Weapon.Enchantment> ench = generateEnchant(enchant_rarity, enchant_id);
        if (ench == null) {
            w.enchant(null);
        } else {
            w.enchant(Reflection.newInstance(ench));
        }
        w.cursed = cursed;
        return w;
    }

    private void createWeapon(){
        Class<? extends Weapon>[] weapons = weaponList(tier);
        weapon_id = Math.min(weapon_id, weapons.length - 1);
        Weapon melee = Reflection.newInstance(weapons[weapon_id]);
        melee = modifyWeapon(melee);
        melee.identify();
        if(melee.collect()){
            GLog.i(Messages.get(this, "collect_success", melee.name()));
        }else{
            melee.doDrop(curUser);
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("tier", tier);
        bundle.put("is_cursed", cursed);
        bundle.put("level_to_gen", levelToGen);
        bundle.put("enchant_rarity", enchant_rarity);
        bundle.put("enchant_id", enchant_id);
        bundle.put("weapon_id", weapon_id);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        tier = bundle.getInt("tier");
        cursed = bundle.getBoolean("is_cursed");
        levelToGen = bundle.getInt("level_to_gen");
        enchant_rarity = bundle.getInt("enchant_rarity");
        enchant_id = clampSelection(bundle.getInt("enchant_id"),
                enchantmentList(enchant_rarity).length);
        weapon_id = bundle.getInt("weapon_id");
    }

    private Class<? extends Weapon.Enchantment> generateEnchant(int category, int id) {
        Class<? extends Weapon.Enchantment>[] enchantments = enchantmentList(category);
        return id >= 0 && id < enchantments.length ? enchantments[id] : null;
    }

    @SuppressWarnings("unchecked")
    static Class<? extends Weapon.Enchantment>[] enchantmentList(int category) {
        switch (category) {
            case 1:
                return new Class[]{Blazing.class, Shocking.class, Chilling.class, Kinetic.class};
            case 2:
                return new Class[]{Blocking.class, Blooming.class, Projecting.class, Elastic.class,
                        Lucky.class, Unstable.class, CorrosionEnchanted.class};
            case 3:
                return new Class[]{Corrupting.class, Grim.class, Vampiric.class, Sweeping.class};
            case 4:
                return new Class[]{Annoying.class, Displacing.class, Explosive.class, Dazzling.class,
                        Friendly.class, Polarized.class, Sacrificial.class, Wayward.class, Heavy.class};
            default:
                return new Class[]{null};
        }
    }

    static int clampSelection(int selection, int itemCount) {
        return itemCount <= 0 ? 0 : Math.max(0, Math.min(selection, itemCount - 1));
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

    private static final int SETTINGS_GAP = 2;
    private static final int SETTINGS_SLIDER_HEIGHT = 24;
    private static final int SETTINGS_WEAPON_PANE_HEIGHT = 32;
    private static final int SETTINGS_ENCHANT_PANE_HEIGHT = 48;
    private static final int SETTINGS_ACTIONS_HEIGHT = 16;

    static SelectionLayout selectionLayout(float selectedWeaponHeight, float enchantInfoHeight) {
        float weaponPaneTop = SETTINGS_GAP + SETTINGS_SLIDER_HEIGHT + SETTINGS_GAP;
        float selectedWeaponTop = weaponPaneTop + SETTINGS_WEAPON_PANE_HEIGHT + SETTINGS_GAP;
        float levelTop = selectedWeaponTop + selectedWeaponHeight + SETTINGS_GAP;
        float enchantRarityTop = levelTop + SETTINGS_SLIDER_HEIGHT + SETTINGS_GAP;
        float enchantPaneTop = enchantRarityTop + SETTINGS_SLIDER_HEIGHT + SETTINGS_GAP;
        float enchantInfoTop = enchantPaneTop + SETTINGS_ENCHANT_PANE_HEIGHT + SETTINGS_GAP;
        float actionsTop = enchantInfoTop + enchantInfoHeight + SETTINGS_GAP;
        int windowHeight = (int) Math.ceil(actionsTop + SETTINGS_ACTIONS_HEIGHT + SETTINGS_GAP);

        return new SelectionLayout(weaponPaneTop, selectedWeaponTop, levelTop,
                enchantRarityTop, enchantPaneTop, enchantInfoTop, actionsTop, windowHeight);
    }

    static final class SelectionLayout {
        final float weaponPaneTop;
        final float selectedWeaponTop;
        final float levelTop;
        final float enchantRarityTop;
        final float enchantPaneTop;
        final float enchantInfoTop;
        final float actionsTop;
        final int windowHeight;

        private SelectionLayout(float weaponPaneTop, float selectedWeaponTop, float levelTop,
                                float enchantRarityTop, float enchantPaneTop,
                                float enchantInfoTop, float actionsTop, int windowHeight) {
            this.weaponPaneTop = weaponPaneTop;
            this.selectedWeaponTop = selectedWeaponTop;
            this.levelTop = levelTop;
            this.enchantRarityTop = enchantRarityTop;
            this.enchantPaneTop = enchantPaneTop;
            this.enchantInfoTop = enchantInfoTop;
            this.actionsTop = actionsTop;
            this.windowHeight = windowHeight;
        }
    }

    private static Class<? extends Weapon>[] weaponList(int t) {
        int tierIndex = Math.max(0, Math.min(t - 1, Generator.wepTiers.length - 1));
        return (Class<? extends Weapon>[]) Generator.wepTiers[tierIndex].classes.clone();
    }

    private String currentEnchName(Class<? extends Weapon.Enchantment> ench) {
        if (enchant_rarity < 4)
            return currentEnchName(ench, Messages.get(Weapon.Enchantment.class, "enchant"));
        else
            return currentEnchName(ench, Messages.get(Item.class, "curse"));
    }

    private String currentEnchName(Class<? extends Weapon.Enchantment> ench, String wepName) {
        return Messages.get(ench, "name", wepName);
    }

    private class SettingsWindow extends Window {
        private static final int WIDTH = 120;
        private static final int BTN_SIZE = 16;
        private static final int GAP = SETTINGS_GAP;
        private static final int WEAPON_COLUMNS = 7;
        private static final int WEAPON_PANE_HEIGHT = SETTINGS_WEAPON_PANE_HEIGHT;
        private static final int ENCHANT_COLUMNS = 2;
        private static final int ENCHANT_CELL_WIDTH = WIDTH / ENCHANT_COLUMNS;
        private static final int ENCHANT_CELL_HEIGHT = 16;
        private static final int ENCHANT_PANE_HEIGHT = SETTINGS_ENCHANT_PANE_HEIGHT;

        private OptionSlider o_tier;
        private OptionSlider o_level;
        private OptionSlider o_enchant_rarity;
        private CheckBox c_curse;
        private RenderedTextBlock t_selectedWeapon;
        private RenderedTextBlock t_infoEnchant;
        private Class<? extends Weapon>[] all;
        private Class<? extends Weapon.Enchantment>[] enchantments;
        private Component weaponContent;
        private Component enchantContent;
        private ScrollPane weaponPane;
        private ScrollPane enchantPane;
        private final ArrayList<SelectionCell> weaponCells = new ArrayList<>();
        private final ArrayList<SelectionCell> enchantCells = new ArrayList<>();
        private RedButton b_create;

        private void createWeaponArray() {
            all = weaponList(tier);
            weapon_id = clampSelection(weapon_id, all.length);
        }

        public SettingsWindow() {
            super();
            createWeaponArray();

            o_tier = new OptionSlider(Messages.get(this, "tier"), "1",
                    String.valueOf(Generator.wepTiers.length), 1, Generator.wepTiers.length) {
                @Override
                protected void onChange() {
                    tier = getSelectedValue();
                    createWeaponArray();
                    rebuildWeaponCells(true);
                    updateSelectedWeaponText();
                }
            };
            o_tier.setSelectedValue(tier);
            add(o_tier);

            weaponContent = new Component();
            weaponPane = new ScrollPane(weaponContent) {
                @Override
                public void onClick(float x, float y) {
                    int index = gridIndexAt(x - weaponGridLeft(), y, all.length,
                            WEAPON_COLUMNS, BTN_SIZE, BTN_SIZE);
                    if (index >= 0) {
                        weapon_id = index;
                        refreshWeaponSelection();
                        updateSelectedWeaponText();
                    }
                }
            };
            add(weaponPane);
            rebuildWeaponCells(false);

            t_selectedWeapon = PixelScene.renderTextBlock("", 6);
            t_selectedWeapon.text(selectedWeaponText());
            t_selectedWeapon.maxWidth(WIDTH);
            add(t_selectedWeapon);

            o_level = new OptionSlider(Messages.get(this, "level"), "0", "12", 0, 12) {
                @Override
                protected void onChange() {
                    levelToGen = getSelectedValue();
                }
            };
            o_level.setSelectedValue(levelToGen);
            add(o_level);

            o_enchant_rarity = new OptionSlider(Messages.get(this, "enchant_rarity"), "0", "4", 0, 4) {
                @Override
                protected void onChange() {
                    enchant_rarity = getSelectedValue();
                    enchant_id = clampSelection(enchant_id, enchantmentList(enchant_rarity).length);
                    rebuildEnchantCells(true);
                    updateEnchantText();
                }
            };
            o_enchant_rarity.setSelectedValue(enchant_rarity);
            add(o_enchant_rarity);

            enchantContent = new Component();
            enchantPane = new ScrollPane(enchantContent) {
                @Override
                public void onClick(float x, float y) {
                    int index = gridIndexAt(x, y, enchantments.length, ENCHANT_COLUMNS,
                            ENCHANT_CELL_WIDTH, ENCHANT_CELL_HEIGHT);
                    if (index >= 0) {
                        enchant_id = index;
                        refreshEnchantSelection();
                        updateEnchantText();
                    }
                }
            };
            add(enchantPane);
            rebuildEnchantCells(false);

            t_infoEnchant = PixelScene.renderTextBlock("", 6);
            t_infoEnchant.text(enchantDesc());
            t_infoEnchant.maxWidth(WIDTH);
            add(t_infoEnchant);

            c_curse = new CheckBox(Messages.get(this, "curse")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    cursed = checked();
                }
            };
            c_curse.checked(cursed);
            add(c_curse);

            b_create = new RedButton(Messages.get(this, "create_button")) {
                @Override
                protected void onClick() {
                    createWeapon();
                }
            };
            add(b_create);

            layout();
            weaponPane.scrollTo(0, 0);
            enchantPane.scrollTo(0, 0);
            weaponPane.update();
            enchantPane.update();
        }

        private float weaponGridLeft() {
            return (WIDTH - WEAPON_COLUMNS * BTN_SIZE) / 2f;
        }

        private void rebuildWeaponCells(boolean resetScroll) {
            weaponContent.clear();
            weaponCells.clear();
            float left = weaponGridLeft();
            for (int i = 0; i < all.length; i++) {
                Image image = new ItemSprite(Reflection.newInstance(all[i]));
                image.scale.set(1f);
                SelectionCell cell = new SelectionCell(image, null);
                cell.setRect(left + (i % WEAPON_COLUMNS) * BTN_SIZE,
                        (i / WEAPON_COLUMNS) * BTN_SIZE, BTN_SIZE, BTN_SIZE);
                weaponContent.add(cell);
                weaponCells.add(cell);
            }
            weaponContent.setRect(0, 0, WIDTH,
                    Math.max(BTN_SIZE, gridContentHeight(all.length, WEAPON_COLUMNS, BTN_SIZE)));
            refreshWeaponSelection();
            if (resetScroll) weaponPane.scrollTo(0, 0);
        }

        private void rebuildEnchantCells(boolean resetScroll) {
            enchantments = enchantmentList(enchant_rarity);
            enchant_id = clampSelection(enchant_id, enchantments.length);
            enchantContent.clear();
            enchantCells.clear();
            for (int i = 0; i < enchantments.length; i++) {
                Class<? extends Weapon.Enchantment> enchantment = enchantments[i];
                String name = enchantment == null
                        ? Messages.get(TestMelee.class, "null_enchant")
                        : currentEnchName(enchantment);
                SelectionCell cell = new SelectionCell(null, name);
                cell.setRect((i % ENCHANT_COLUMNS) * ENCHANT_CELL_WIDTH,
                        (i / ENCHANT_COLUMNS) * ENCHANT_CELL_HEIGHT,
                        ENCHANT_CELL_WIDTH, ENCHANT_CELL_HEIGHT);
                enchantContent.add(cell);
                enchantCells.add(cell);
            }
            enchantContent.setRect(0, 0, WIDTH, Math.max(ENCHANT_CELL_HEIGHT,
                    gridContentHeight(enchantments.length, ENCHANT_COLUMNS, ENCHANT_CELL_HEIGHT)));
            refreshEnchantSelection();
            if (resetScroll) enchantPane.scrollTo(0, 0);
        }

        private void refreshWeaponSelection() {
            for (int i = 0; i < weaponCells.size(); i++) {
                weaponCells.get(i).selected(i == weapon_id);
            }
        }

        private void refreshEnchantSelection() {
            for (int i = 0; i < enchantCells.size(); i++) {
                enchantCells.get(i).selected(i == enchant_id);
            }
        }

        private void layout() {
            SelectionLayout positions = selectionLayout(t_selectedWeapon.height(), t_infoEnchant.height());

            o_tier.setRect(0, GAP, WIDTH, 24);
            t_selectedWeapon.setPos(0, positions.selectedWeaponTop);
            o_level.setRect(0, positions.levelTop, WIDTH, 24);
            o_enchant_rarity.setRect(0, positions.enchantRarityTop, WIDTH, 24);
            t_infoEnchant.setPos(0, positions.enchantInfoTop);
            c_curse.setRect(0, positions.actionsTop, WIDTH/2f - GAP/2f, 16);
            b_create.setRect(WIDTH/2f+GAP/2f, c_curse.top(), WIDTH/2f - GAP/2f, 16);

            // ScrollPane cameras must be laid out after resize centers the window camera.
            resize(WIDTH, positions.windowHeight);
            weaponPane.setRect(0, positions.weaponPaneTop, WIDTH, WEAPON_PANE_HEIGHT);
            enchantPane.setRect(0, positions.enchantPaneTop, WIDTH, ENCHANT_PANE_HEIGHT);
        }

        private String selectedWeaponText() {
            return Messages.get(this, "selected", Messages.get(all[weapon_id], "name"));
        }

        private void updateSelectedWeaponText() {
            t_selectedWeapon.text(selectedWeaponText());
            layout();
        }

        private void updateEnchantText() {
            t_infoEnchant.text(enchantDesc());
            layout();
        }

        private String enchantDesc() {
            //String desc = Messages.get(BossRushMelee.class, "enchant_id_pre", enchant_rarity);
            String desc = "";
            String key = "enchant_id_e" + String.valueOf(enchant_rarity);
            Class<? extends Weapon.Enchantment> ench = generateEnchant(enchant_rarity, enchant_id);
            desc += Messages.get(TestMelee.class, key, (ench == null ? Messages.get(TestMelee.class, "null_enchant") : currentEnchName(ench)));
            return desc;
        }

        @Override
        public void offset(int xOffset, int yOffset) {
            super.offset(xOffset, yOffset);
            weaponPane.setPos(weaponPane.left(), weaponPane.top());
            enchantPane.setPos(enchantPane.left(), enchantPane.top());
        }

        private class SelectionCell extends Component {
            private final NinePatch background;
            private final Image icon;
            private final RenderedTextBlock label;

            private SelectionCell(Image icon, String text) {
                this.icon = icon;
                this.label = text == null ? null : PixelScene.renderTextBlock(text, 6);
                background = Chrome.get(Chrome.Type.RED_BUTTON);
                add(background);
                if (icon != null) add(icon);
                if (label != null) add(label);
            }

            private void selected(boolean selected) {
                background.resetColor();
                background.alpha(selected ? 1f : 0.45f);
                if (label != null) {
                    if (selected) {
                        label.hardlight(Window.TITLE_COLOR);
                    } else {
                        label.resetColor();
                    }
                }
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
                if (label != null) {
                    label.maxWidth((int) width - 4);
                    label.setPos(x + (width - label.width()) / 2f,
                            y + (height - label.height()) / 2f);
                    PixelScene.align(label);
                }
            }
        }
    }
}
