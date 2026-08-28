package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.boss.YogRealFist;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.ArcaneResin;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.Stylus;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.Torch;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ArcaneBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Firebomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.FlashBangBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.FrostBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.HolyBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Noisemaker;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.RegrowthBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.SmokeBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ShrapnelBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.WoollyBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Berry;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.ChargrilledMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.food.PhantomMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StewedMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SupplyRation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.BlizzardBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.Brew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.CausticBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.InfernalBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfIcyTouch;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfToxicEssence;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.BowFragment;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.BrokenHilt;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.BrokenPackage;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.BrokenStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.BrokenTool;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.CloakScrap;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.MatchaPudding;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.MetalFragment;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.RemainsItem;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.SealShard;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.TornPage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.BeaconOfReturning;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.CurseInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion;
//import com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalPorter;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.PhaseShift;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ReclaimTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Recycle;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TelekineticGrab;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TransformSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.WildEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.treasures.Treasures;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.AdrenalineDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.BlindingDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ChillingDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.CleansingDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.DisplacingDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.HealingDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.HolyDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.IncendiaryDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ParalyticDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.PoisonDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.RotDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ShockingDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class TestPotion extends TestGenerator {
    private static final int TREASURE_CATEGORY = 15;
    private static final int ITEM_COLUMNS = 7;
    private static final int ITEM_CELL_SIZE = 16;
    private static final int ITEM_PANE_HEIGHT = 64;

    {
        image = ItemSpriteSheet.POTION_HOLDER;
    }

    private int cateSelected = 0;
    private int item_quantity = 1;
    private int selected = 0;
    private boolean multiply = false;


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

    private void createItem(){
        boolean collect = false;
        Item item = Reflection.newInstance(idToItem(selected));
        if(Challenges.isItemBlocked(item)) return;
        if (item != null) {
            if(item.stackable){
                int qu = item_quantity * (multiply?10:1);
                collect = item.quantity(qu).collect();
            }
            else collect = item.collect();
            item.identify();
            if(collect){
                GLog.i(Messages.get(this, "collect_success", item.name()));
            }else{
                item.doDrop(curUser);
            }
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("item_quantity", item_quantity);
        bundle.put("selected", selected);
        bundle.put("multiply", multiply);
        bundle.put("cate_selected", cateSelected);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        item_quantity = bundle.getInt("item_quantity");
        selected = bundle.getInt("selected");
        multiply = bundle.getBoolean("multiply");
        cateSelected = bundle.getInt("cate_selected");
    }

    private Class<? extends Item> idToItem(int id){
        switch (cateSelected){
            case 0: default: return potionList.get(id);
            case 1: return exoticPotionList.get(id);
            case 2: return seedList.get(id);
            case 3: return dartList.get(id);
            case 4: return scrollList.get(id);
            case 5: return exoticScrollList.get(id);
            case 6: return stoneList.get(id);
            case 7: return bombList.get(id);
            case 8: return brewList.get(id);
            case 9: return spellList.get(id);
            case 10: return foodList.get(id);
            case 11: return miscList.get(id);
            case 12: return remainList.get(id);
            case 13: return trinketList.get(id);
            case 14: return equipmentList.get(id);
            case TREASURE_CATEGORY: return treasureList.get(id);
        }
    }

    private int idToCategoryImage(int selected){
        switch (selected){
            case 0: return ItemSpriteSheet.POTION_AZURE;
            case 1: return ItemSpriteSheet.EXOTIC_AZURE;
            case 2: return ItemSpriteSheet.SEED_ICECAP;
            case 3: return ItemSpriteSheet.CHILLING_DART;
            case 4: return ItemSpriteSheet.SCROLL_LAGUZ;
            case 5: return ItemSpriteSheet.EXOTIC_LAGUZ;
            case 6: return ItemSpriteSheet.STONE_AUGMENTATION;
            case 7: return ItemSpriteSheet.BOMB;
            case 8: return ItemSpriteSheet.BREW_CAUSTIC;
            case 9: return ItemSpriteSheet.PHASE_SHIFT;
            case 10: return ItemSpriteSheet.RATION;
            case 11: default: return ItemSpriteSheet.CHEST;
            case 12: return ItemSpriteSheet.SEAL_SHARD;
            case 13: return ItemSpriteSheet.TRINKET_CATA;
            case 14: return ItemSpriteSheet.BACKPACK;
            case TREASURE_CATEGORY: return EXItemSpriteSheet.MUISCA_GOLDEN_RAFT;
        }
    }

    private int maxCategory(){
        return TREASURE_CATEGORY;
    }

    private int maxIndex(int cate){
        switch (cate){
            case 0: default: return potionList.toArray().length-1;
            case 1: return exoticPotionList.toArray().length-1;
            case 2: return seedList.toArray().length-1;
            case 3: return dartList.toArray().length-1;
            case 4: return scrollList.toArray().length-1;
            case 5: return exoticScrollList.toArray().length-1;
            case 6: return stoneList.toArray().length-1;
            case 7: return bombList.toArray().length-1;
            case 8: return brewList.toArray().length-1;
            case 9: return spellList.toArray().length-1;
            case 10: return foodList.toArray().length-1;
            case 11: return miscList.toArray().length-1;
            case 12: return remainList.toArray().length-1;
            case 13: return trinketList.toArray().length-1;
            case 14: return equipmentList.toArray().length-1;
            case TREASURE_CATEGORY: return treasureList.toArray().length-1;
        }
    }

    private static ArrayList<Class<? extends Potion>> potionList = new ArrayList<>();
    private static ArrayList<Class<? extends ExoticPotion>> exoticPotionList = new ArrayList<>();
    private static ArrayList<Class<? extends Plant.Seed>> seedList = new ArrayList<>();
    private static ArrayList<Class<? extends Scroll>> scrollList = new ArrayList<>();
    private static ArrayList<Class<? extends ExoticScroll>> exoticScrollList = new ArrayList<>();
    private static ArrayList<Class<? extends Runestone>> stoneList = new ArrayList<>();
    private static ArrayList<Class<? extends TippedDart>> dartList = new ArrayList<>();
    private static ArrayList<Class<? extends Item>> bombList = new ArrayList<>();
    private static ArrayList<Class<? extends Potion>> brewList = new ArrayList<>();
    private static ArrayList<Class<? extends Spell>> spellList = new ArrayList<>();
    private static ArrayList<Class<? extends Food>> foodList = new ArrayList<>();
    private static ArrayList<Class<? extends Item>> miscList = new ArrayList<>();
    private static ArrayList<Class<? extends Item>> remainList = new ArrayList<>();
    private static ArrayList<Class<? extends Item>> trinketList = new ArrayList<>();
    private static ArrayList<Class<? extends Item>> equipmentList = new ArrayList<>();
    private static ArrayList<Class<? extends Treasures>> treasureList = new ArrayList<>();

    private void buildList() {
        if (potionList.isEmpty() || exoticPotionList.isEmpty()) {
            Class<?>[] classes = Generator.Category.POTION.classes;
            for (Class<?> t : classes) {
                potionList.add((Class<? extends Potion>) t);
                exoticPotionList.add(ExoticPotion.regToExo.get(t));
            }
        }

        if (seedList.isEmpty()) {
            Class<?>[] classes = Generator.Category.SEED.classes;
            for (Class<?> t : classes) {
                seedList.add((Class<? extends Plant.Seed>) t);
            }
        }

        if(dartList.isEmpty()){
            Class<?>[] classes = Catalog.TIPPED_DARTS.items().toArray(new Class[0]);
            for (Class<?> t : classes) {
                dartList.add((Class<? extends TippedDart>) t);
            }
        }

        if (scrollList.isEmpty() || exoticScrollList.isEmpty()) {
            Class<?>[] classes = Generator.Category.SCROLL.classes;
            for (Class<?> t : classes) {
                scrollList.add((Class<? extends Scroll>) t);
                exoticScrollList.add(ExoticScroll.regToExo.get((Class<? extends Scroll>) t));
            }
        }

        if (stoneList.isEmpty()) {
            Class<?>[] classes = Generator.Category.STONE.classes;
            for (Class<?> t : classes) {
                stoneList.add((Class<? extends Runestone>) t);
            }
        }

        if(bombList.isEmpty()){
            Class<?>[] classes = Catalog.BOMBS.items().toArray(new Class[0]);
            for (Class<?> t : classes) {
                bombList.add((Class<? extends Bomb>) t);
            }
        }

        if(brewList.isEmpty()){
            Class<?>[] classes = Catalog.BREWS_ELIXIRS.items().toArray(new Class[0]);
            for (Class<?> t : classes) {
                brewList.add((Class<? extends Brew>) t);
            }
        }

        if(spellList.isEmpty()){
            Class<?>[] classes = Catalog.SPELLS.items().toArray(new Class[0]);
            for (Class<?> t : classes) {
                spellList.add((Class<? extends Spell>) t);
            }
        }

        if(foodList.isEmpty()){
            Class<?>[] classes = Catalog.FOOD.items().toArray(new Class[0]);
            for (Class<?> t : classes) {
                foodList.add((Class<? extends Food>) t);
            }
        }

        if(miscList.isEmpty() || remainList.isEmpty()){
            Class<?>[] classes = Catalog.MISC_CONSUMABLES.items().toArray(new Class[0]);
            splitMiscCatalogItems(Arrays.asList(classes), miscList, remainList);
        }

        if(trinketList.isEmpty()){
            trinketList.add(TrinketCatalyst.class);
            // 假设在某个静态初始化块或方法中
            Class<?>[] classes = Generator.Category.TRINKET.classes;
            for (Class<?> t : classes) {
                // 由于 Trinket 继承自 Item，可以安全地强制转换
                trinketList.add((Class<? extends Item>) t);
            }
        }

        if(equipmentList.isEmpty()){
            Class<?>[] classes = Catalog.MISC_EQUIPMENT.items().toArray(new Class[0]);
            for (Class<?> t : classes) {
                equipmentList.add((Class<? extends Item>) t);
            }
        }

        if(treasureList.isEmpty()){
            Class<?>[] classes = Catalog.TREASURES.items().toArray(new Class[0]);
            for (Class<?> t : classes) {
                treasureList.add((Class<? extends Treasures>) t);
            }
        }
    }

    static int clampSelection(int selection, int itemCount) {
        return itemCount <= 0 ? 0 : Math.max(0, Math.min(selection, itemCount - 1));
    }

    static float itemGridContentHeight(int itemCount) {
        if (itemCount <= 0) return 0;
        return ((itemCount + ITEM_COLUMNS - 1) / ITEM_COLUMNS) * ITEM_CELL_SIZE;
    }

    static int itemGridIndexAt(float x, float y, int itemCount) {
        if (x < 0 || y < 0 || itemCount <= 0
                || x >= ITEM_COLUMNS * ITEM_CELL_SIZE) {
            return -1;
        }
        int index = (int) (y / ITEM_CELL_SIZE) * ITEM_COLUMNS
                + (int) (x / ITEM_CELL_SIZE);
        return index < itemCount ? index : -1;
    }

    static float itemPaneHeight() {
        return ITEM_PANE_HEIGHT;
    }

    @SuppressWarnings("unchecked")
    static void splitMiscCatalogItems(Collection<Class<?>> classes,
                                      List<Class<? extends Item>> miscItems,
                                      List<Class<? extends Item>> remainsItems) {
        boolean remainsSection = false;
        for (Class<?> itemClass : classes) {
            if (itemClass == SealShard.class) {
                remainsSection = true;
            } else if (itemClass == ScrollOfMetamorphosis.class) {
                remainsSection = false;
            }

            if (remainsSection) {
                remainsItems.add((Class<? extends Item>) itemClass);
            } else {
                miscItems.add((Class<? extends Item>) itemClass);
            }
        }
    }

    private class SettingsWindow extends Window {
        private OptionSlider o_quantity;
        private RenderedTextBlock t_select;
        private CheckBox c_multiply;
        private RedButton b_create;
        private ArrayList<IconButton> cateButtonList = new ArrayList<>();
        private Component itemContent;
        private ScrollPane itemPane;
        private final ArrayList<SelectionCell> itemCells = new ArrayList<>();
        private static final int WIDTH = 120;
        private static final int BTN_SIZE = 16;
        private static final int GAP = 2;
        private static final int TITLE_BTM = 8;
        private int CATEGORY_BTM = TITLE_BTM + GAP + BTN_SIZE*2 + GAP;

        public SettingsWindow() {
            buildList();

            createCategoryImage();

            itemContent = new Component();
            itemPane = new ScrollPane(itemContent) {
                @Override
                public void onClick(float x, float y) {
                    int index = itemGridIndexAt(x - itemGridLeft(), y, currentItemCount());
                    if (index >= 0) {
                        selected = index;
                        refreshItemSelection();
                        updateText();
                    }
                }
            };
            add(itemPane);
            createImage();

            t_select = PixelScene.renderTextBlock("", 8);
            t_select.text();
            add(t_select);

            o_quantity = new OptionSlider(Messages.get(this, "quantity"), "1", "10", 1, 10) {
                @Override
                protected void onChange() {
                    item_quantity = getSelectedValue();
                }
            };
            o_quantity.setSelectedValue(item_quantity);
            o_quantity.setRect(0, t_select.bottom() + 2 * GAP, WIDTH, 24);
            add(o_quantity);

            c_multiply = new CheckBox(Messages.get(this, "multiply")){
                @Override
                protected void onClick() {
                    super.onClick();
                    multiply = checked();
                }
            };
            c_multiply.checked(multiply);
            c_multiply.setRect(0, o_quantity.bottom() + GAP, WIDTH, 18);
            add(c_multiply);

            b_create = new RedButton(Messages.get(this, "create_button")) {
                @Override
                protected void onClick() {
                    createItem();
                    updateText();
                }
            };
            add(b_create);

            updateText();
        }

        private void layout() {
            float itemPaneTop = CATEGORY_BTM + GAP*3;
            t_select.setPos(0, itemPaneTop + ITEM_PANE_HEIGHT + GAP);
            o_quantity.setRect(0, t_select.bottom() + 2 * GAP, WIDTH, 24);
            c_multiply.setRect(0, o_quantity.bottom() + GAP, WIDTH/2f - GAP/2f, 16);
            b_create.setRect(WIDTH/2f + GAP/2f, o_quantity.bottom() + GAP, WIDTH/2f - GAP/2f, 16);
            resize(WIDTH, (int) b_create.bottom());
            itemPane.setRect(0, itemPaneTop, WIDTH, ITEM_PANE_HEIGHT);
        }

        private void createCategoryImage(){
            float top = TITLE_BTM;
            int number = maxCategory();
            int maxCol = 6;
            int rowNumber = maxCategory() / maxCol;
            for (int i = 0; i <= number; ++i) {
                final int j = i;
                IconButton btn = new IconButton() {
                    @Override
                    protected void onClick() {
                        cateButtonList.get(cateSelected).icon().resetColor();
                        cateSelected = Math.min(j, maxCategory());
                        selected = clampSelection(selected, currentItemCount());
                        cateButtonList.get(cateSelected).icon().color(0xFFFF44);
                        updateImage();
                        updateText();

                        super.onClick();
                    }
                };
                Image im = new ItemSprite(idToCategoryImage(i));
                im.scale.set(1.0f);
                btn.icon(im);

                int row = i / maxCol;
                int col = i % maxCol;
                int colNumber = rowNumber > row? maxCol:maxCategory()+1 - rowNumber*maxCol;
                float left = (WIDTH - BTN_SIZE * (colNumber)) / 2f;

                btn.setRect(left + col * BTN_SIZE, top + row  * BTN_SIZE, BTN_SIZE, BTN_SIZE);

                add(btn);
                cateButtonList.add(btn);
            }
            CATEGORY_BTM = TITLE_BTM + (rowNumber+1) * BTN_SIZE;
            cateButtonList.get(cateSelected).icon().color(0xFFFF44);
        }

        private Image createItemImage(Class<? extends Item> itemClass) {
            Image im = new ItemSprite(Objects.requireNonNull(Reflection.newInstance(itemClass)));
            im.scale.set(1.0f);
            return im;
        }

        private int currentItemCount() {
            return Math.max(0, maxIndex(cateSelected) + 1);
        }

        private float itemGridLeft() {
            return (WIDTH - ITEM_COLUMNS * BTN_SIZE) / 2f;
        }

        private void createImage() {
            itemContent.clear();
            itemCells.clear();
            int number = currentItemCount();
            for (int i = 0; i < number; ++i) {
                Image im;
                switch (cateSelected){
                    case 0 :{
                        im = new Image(Assets.Sprites.ITEM_ICONS);
                        im.frame(ItemSpriteSheet.Icons.film.get(Objects.requireNonNull(Reflection.newInstance(potionList.get(i))).icon));
                        im.scale.set(1.6f);
                    } break;
                    case 1:{
                        im = new Image(Assets.Sprites.ITEM_ICONS);
                        im.frame(ItemSpriteSheet.Icons.film.get(Objects.requireNonNull(Reflection.newInstance(exoticPotionList.get(i))).icon));
                        im.scale.set(1.6f);
                    } break;
                    case 2:{
                        im = createItemImage(seedList.get(i));
                    } break;
                    case 3:{
                        im = createItemImage(dartList.get(i));
                    } break;
                    case 4:{
                        im = new Image(Assets.Sprites.ITEM_ICONS);
                        im.frame(ItemSpriteSheet.Icons.film.get(Objects.requireNonNull(Reflection.newInstance(scrollList.get(i))).icon));
                        im.scale.set(1.6f);
                    }break;
                    case 5:{
                        im = new Image(Assets.Sprites.ITEM_ICONS);
                        im.frame(ItemSpriteSheet.Icons.film.get(Objects.requireNonNull(Reflection.newInstance(exoticScrollList.get(i))).icon));
                        im.scale.set(1.6f);
                    } break;
                    case 6:{
                        im = createItemImage(stoneList.get(i));
                    } break;
                    case 7:{
                        im = createItemImage(bombList.get(i));
                    } break;
                    case 8:{
                        im = createItemImage(brewList.get(i));
                    } break;
                    case 9: {
                        im = new ItemSprite(Objects.requireNonNull(
                                Reflection.newInstance(spellList.get(i))));
                        im.scale.set(1.0f);
                    } break;
                    case 10: {
                        im = createItemImage(foodList.get(i));
                    } break;
                    case 11: default:{
                        im = new ItemSprite(Objects.requireNonNull(Reflection.newInstance(miscList.get(i))));
                        im.scale.set(1.0f);
                    } break;
                    case 12: {
                        im = createItemImage(remainList.get(i));
                    } break;
                    case 13: {
                        im = createItemImage(trinketList.get(i));
                    }break;
                    case 14: {
                        im = createItemImage(equipmentList.get(i));
                    } break;
                    case TREASURE_CATEGORY: {
                        im = new ItemSprite(Objects.requireNonNull(Reflection.newInstance(treasureList.get(i))));
                        im.scale.set(1.0f);
                    } break;
                    /*
                    case 14:{
                        Mob mob = (Mob) Reflection.newInstance(SublimationList.get(i));
                        CharSprite sprite = mob.sprite();
                        sprite.idle();

                        Image im = new Image(sprite);
                        if (im.width() >= 17 || im.height() >= 17) {
                            RectF frame = im.frame();

                            float wShrink = frame.width() * (1f - 17f / im.width());
                            if (wShrink > 0) {
                                frame.left += wShrink / 2f;
                                frame.right -= wShrink / 2f;
                            }
                            float hShrink = frame.height() * (1f - 17f / im.height());
                            if (hShrink > 0) {
                                frame.top += hShrink / 2f;
                                frame.bottom -= hShrink / 2f;
                            }
                            im.frame(frame);
                        }
                        btn.icon(im);
                    }*/
                }

                SelectionCell cell = new SelectionCell(im);
                cell.setRect(itemGridLeft() + (i % ITEM_COLUMNS) * BTN_SIZE,
                        (i / ITEM_COLUMNS) * BTN_SIZE, BTN_SIZE, BTN_SIZE);
                itemContent.add(cell);
                itemCells.add(cell);
            }

            itemContent.setRect(0, 0, WIDTH,
                    Math.max(BTN_SIZE, itemGridContentHeight(number)));
            refreshItemSelection();
        }

        private void refreshItemSelection() {
            for (int i = 0; i < itemCells.size(); i++) {
                itemCells.get(i).selected(i == selected);
            }
        }

        private void updateImage() {
            createImage();
            itemPane.scrollTo(0, 0);
            itemPane.update();
        }

        private void updateText() {
            Class<? extends Item> item = idToItem(selected);
            String s = Messages.get(item, "name");
            if (item == Pasty.class) {
                s = "馅饼";
            }
            t_select.text(Messages.get(TestPotion.class, "select", s));
            layout();
        }

        @Override
        public void offset(int xOffset, int yOffset) {
            super.offset(xOffset, yOffset);
            itemPane.setPos(itemPane.left(), itemPane.top());
        }

        private class SelectionCell extends Component {
            private final NinePatch background;
            private final Image icon;

            private SelectionCell(Image icon) {
                this.icon = icon;
                background = Chrome.get(Chrome.Type.RED_BUTTON);
                add(background);
                add(icon);
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
                icon.x = x + (width - icon.width()) / 2f;
                icon.y = y + (height - icon.height()) / 2f;
                PixelScene.align(icon);
            }
        }
    }
}
