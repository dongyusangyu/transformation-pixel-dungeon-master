package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Tatteki;
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
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Objects;

public class TestMissile extends TestGenerator {
    {
        image = ItemSpriteSheet.MISSILE_HOLDER;
    }

    private int tier = 1;
    private boolean cursed = false;
    private int item_quantity = 1;
    private int levelToGen = 0;
    private int enchant_id = 0;
    private int enchant_rarity = 0;
    private int weapon_id = 0;
    private boolean bowGenerated = false;
    private boolean tattekiGenerated = false;

    private static final String AC_BOW = "bow";
    private static final String AC_TATTEKI = "tatteki";

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (!bowGenerated) actions.add(AC_BOW);
        if (!tattekiGenerated) actions.add(AC_TATTEKI);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_GIVE)) {
            GameScene.show(new SettingsWindow());
        } else if (action.equals(AC_BOW)) {
            SpiritBow bow = new SpiritBow();
            bow.identify().collect();
            bowGenerated = true;
        } else if (action.equals(AC_TATTEKI)) {
            Tatteki tatteki = new Tatteki();
            tatteki.identify().collect();
            tattekiGenerated = true;
        }
    }

    private MissileWeapon modifyWeapon(MissileWeapon m) {
        m.level(levelToGen);
        Class<? extends Weapon.Enchantment> ench = generateEnchant(enchant_rarity, enchant_id);
        if (ench == null) {
            m.enchant(null);
        } else {
            m.enchant(Reflection.newInstance(ench));
        }
        m.cursed = cursed;
        return m;
    }

    private void createMissiles(){
        MissileWeapon m = Reflection.newInstance(missileList(tier)[weapon_id]);
        m = modifyWeapon(m);
        m.quantity(item_quantity);
        m.identify();
        if(m.collect()){
            GLog.i(Messages.get(this, "collect_success", m.name()));
        }else{
            m.doDrop(curUser);
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("tier", tier);
        bundle.put("is_cursed", cursed);
        bundle.put("item_quantity", item_quantity);
        bundle.put("level_to_gen", levelToGen);
        bundle.put("enchant_rarity", enchant_rarity);
        bundle.put("enchant_id", enchant_id);
        bundle.put("weapon_id", weapon_id);
        bundle.put("bow_generated", bowGenerated);
        bundle.put("tatteki_generated", tattekiGenerated);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        tier = bundle.contains("tier") ? bundle.getInt("tier") : 1;
        cursed = bundle.getBoolean("is_cursed");
        item_quantity = bundle.contains("item_quantity") ? bundle.getInt("item_quantity") : 1;
        levelToGen = bundle.getInt("level_to_gen");
        enchant_rarity = bundle.getInt("enchant_rarity");
        enchant_id = bundle.getInt("enchant_id");
        weapon_id = bundle.contains("weapon_id") ? bundle.getInt("weapon_id") : bundle.getInt("selected");
        weapon_id = Math.max(0, Math.min(weapon_id, maxSlots(tier) - 1));
        bowGenerated = bundle.getBoolean("bow_generated");
        tattekiGenerated = bundle.contains("tatteki_generated") ? bundle.getBoolean("tatteki_generated") : bundle.getBoolean("bow_tatteki");
    }

    private Class<? extends Weapon.Enchantment> generateEnchant(int category, int id) {
        if (category == 1) switch (id) {
            case 0:
                return Blazing.class;
            case 1:
                return Shocking.class;
            case 2:
                return Chilling.class;
            case 3:
                return Kinetic.class;

            default:
                return null;
        }
        else if (category == 2) switch (id) {
            case 0:
                return Blocking.class;
            case 1:
                return Blooming.class;
            case 2:
                return Projecting.class;
            case 3:
                return Elastic.class;
            case 4:
                return Lucky.class;
            case 5:
                return Unstable.class;
            case 6:
                return CorrosionEnchanted.class;
            default:
                return null;
        }
        else if (category == 3) switch (id) {
            case 0:
                return Corrupting.class;
            case 1:
                return Grim.class;
            case 2:
                return Vampiric.class;
            case 3:
                return Sweeping.class;
            default:
                return null;
        }
        else if (category == 4) switch (id) {
            case 0:
                return Annoying.class;
            case 1:
                return Displacing.class;
            case 2:
                return Explosive.class;
            case 3:
                return Dazzling.class;
            case 4:
                return Friendly.class;
            case 5:
                return Polarized.class;
            case 6:
                return Sacrificial.class;
            case 7:
                return Wayward.class;
            case 8:
                return Heavy.class;
            default:
                return null;
        }
        return null;
    }

    private Class<? extends MissileWeapon>[] missileList(int t) {
        switch (t) {
            case 1:
            default:
                return (Class<? extends MissileWeapon>[]) Generator.Category.MIS_T1.classes.clone();
            case 2:
                return (Class<? extends MissileWeapon>[]) Generator.Category.MIS_T2.classes.clone();
            case 3:
                return (Class<? extends MissileWeapon>[]) Generator.Category.MIS_T3.classes.clone();
            case 4:
                return (Class<? extends MissileWeapon>[]) Generator.Category.MIS_T4.classes.clone();
            case 5:
                return (Class<? extends MissileWeapon>[]) Generator.Category.MIS_T5.classes.clone();
        }
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

    private int maxSlots(int t){
        return missileList(t).length;
    }

    private class SettingsWindow extends Window {
        private static final int WIDTH = 120;
        private static final int BTN_SIZE = 16;
        private static final int GAP = 2;
        private OptionSlider o_tier;
        private OptionSlider o_level;
        private OptionSlider o_quantity;
        private OptionSlider o_enchant_rarity;
        private OptionSlider o_enchant_id;
        private CheckBox c_curse;
        private RenderedTextBlock t_selectedMissile;
        private RenderedTextBlock t_infoEnchant;
        private Class<? extends MissileWeapon>[] all;
        private ArrayList<IconButton> iconButtons = new ArrayList<IconButton>();
        private RedButton b_create;

        private void createMissileArray() {
            all = missileList(tier);
            weapon_id = Math.max(0, Math.min(weapon_id, all.length - 1));
        }

        public SettingsWindow() {
            super();
            createMissileArray();
            o_tier = new OptionSlider(Messages.get(this, "tier"), "1", "5", 1, 5) {
                @Override
                protected void onChange() {
                    tier = getSelectedValue();
                    for(IconButton ib : iconButtons.toArray(new IconButton[0])){
                        ib.destroy();
                    }
                    iconButtons.clear();
                    createMissileArray();
                    createMissileImage(all);
                    if (t_selectedMissile != null) {
                        updateSelectedMissileText();
                    }
                }
            };
            o_tier.setSelectedValue(tier);
            add(o_tier);
            o_tier.setRect(0, GAP, WIDTH, 24);
            createMissileImage(all);

            t_selectedMissile = PixelScene.renderTextBlock("", 6);
            t_selectedMissile.text(Messages.get(TestMissile.class, "select", Messages.get(all[Math.min(weapon_id, all.length-1)], "name")));
            t_selectedMissile.maxWidth(WIDTH);
            add(t_selectedMissile);

            o_level = new OptionSlider(Messages.get(this, "level"), "0", "12", 0, 12) {
                @Override
                protected void onChange() {
                    levelToGen = getSelectedValue();
                }
            };
            o_level.setSelectedValue(levelToGen);
            add(o_level);

            o_quantity = new OptionSlider(Messages.get(this, "quantity"), "1", "10", 1, 10) {
                @Override
                protected void onChange() {
                    item_quantity = getSelectedValue();
                }
            };
            o_quantity.setSelectedValue(item_quantity);
            add(o_quantity);

            t_infoEnchant = PixelScene.renderTextBlock("", 6);
            t_infoEnchant.text(enchantDesc());
            add(t_infoEnchant);

            o_enchant_rarity = new OptionSlider(Messages.get(this, "enchant_rarity"), "0", "4", 0, 4) {
                @Override
                protected void onChange() {
                    enchant_rarity = getSelectedValue();
                    updateEnchantText();
                }
            };
            o_enchant_rarity.setSelectedValue(enchant_rarity);
            add(o_enchant_rarity);

            o_enchant_id = new OptionSlider(Messages.get(this, "enchant_id"), "0", "8", 0, 8) {
                @Override
                protected void onChange() {
                    enchant_id = getSelectedValue();
                    updateEnchantText();
                }
            };
            o_enchant_id.setSelectedValue(enchant_id);
            add(o_enchant_id);

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
                    createMissiles();
                }
            };
            add(b_create);

            layout();
        }

        private void createMissileImage(Class<? extends MissileWeapon>[] all) {
            float left;
            float top = o_tier.bottom() + GAP;
            int placed = 0;
            int length = all.length;
            int perRow = 7;
            left = (WIDTH - BTN_SIZE * Math.min(length, perRow)) / 2f;
            for (int i = 0; i < length; ++i) {
                final int j = i;
                IconButton btn = new IconButton() {
                    @Override
                    protected void onClick() {
                        weapon_id = Math.min(maxSlots(tier)-1, j);
                        updateSelectedMissileText();
                        super.onClick();
                    }
                };
                Image im = new Image(Assets.Sprites.ITEMS);
                im.frame(ItemSpriteSheet.film.get(Objects.requireNonNull(Reflection.newInstance(all[i])).image));
                im.scale.set(1f);
                btn.icon(im);
                btn.setRect(left + Math.floorMod(placed, perRow) * BTN_SIZE, top+(int)(placed/perRow)*BTN_SIZE, BTN_SIZE, BTN_SIZE);
                add(btn);
                placed++;
                if (placed % perRow == 0) {
                    left = (WIDTH - BTN_SIZE * Math.min(length - placed, perRow)) / 2f;
                }
                iconButtons.add(btn);
            }
        }

        private float iconGridBottom() {
            return iconButtons.isEmpty() ? o_tier.bottom() : iconButtons.get(iconButtons.size() - 1).bottom();
        }

        private void layout() {
            o_tier.setRect(0, GAP, WIDTH, 24);
            t_selectedMissile.setPos(0, GAP + iconGridBottom());
            o_level.setRect(0, t_selectedMissile.bottom() + GAP, WIDTH, 24);
            o_quantity.setRect(0, o_level.bottom() + GAP, WIDTH, 24);
            t_infoEnchant.setPos(0, GAP + o_quantity.bottom());
            o_enchant_rarity.setRect(0, GAP + t_infoEnchant.bottom(), WIDTH, 24);
            o_enchant_id.setRect(0, GAP + o_enchant_rarity.bottom(), WIDTH, 24);
            c_curse.setRect(0, GAP + o_enchant_id.bottom(), WIDTH/2f - GAP/2f, 16);
            b_create.setRect(WIDTH/2f+GAP/2f, c_curse.top(), WIDTH/2f - GAP/2f, 16);
            resize(WIDTH, (int) (c_curse.bottom() + GAP));
        }

        private void updateSelectedMissileText() {
            t_selectedMissile.text(Messages.get(TestMissile.class, "select", Messages.get(all[Math.min(weapon_id, all.length-1)], "name")));
            layout();
        }

        private void updateEnchantText() {
            t_infoEnchant.text(enchantDesc());
            layout();
        }

        private String enchantDesc() {
            String key = "enchant_id_e" + enchant_rarity;
            Class<? extends Weapon.Enchantment> ench = generateEnchant(enchant_rarity, enchant_id);
            return Messages.get(TestMelee.class, key, (ench == null ? Messages.get(TestMelee.class, "null_enchant") : currentEnchName(ench)));
        }
    }
}
