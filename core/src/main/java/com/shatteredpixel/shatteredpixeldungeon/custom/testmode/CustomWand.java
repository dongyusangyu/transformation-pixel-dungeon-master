package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;


import java.util.ArrayList;

public class CustomWand extends Item {

    {
        image = ItemSpriteSheet.WAND_HOLDER;
        defaultAction = AC_ZAP;
        usesTargeting = true;
        unique = true;
        bones = false;

    }

    private Wand wand = getWand();

    // 模拟法杖相关参数
    private int selectedWandType = 0;      // 当前选择的法杖类型索引
    private int wandLevel = 0;             // 法杖等级 (0-12)

    // 诅咒相关参数
    private boolean cursed = false;
    private boolean positiveOnly = false;
    private int curseRarity = 0;             // 0=common,1=uncommon,2=rare,3=veryRare
    private int selectedCurse = 0;         // 具体效果索引
    private static final String AC_ZAP = "ZAP";

    private static final String AC_SETTING = "setting";

    // 所有可模拟的法杖列表（13 种原版法杖）
    private static final Class<? extends Wand>[] WAND_LIST = (Class<? extends Wand>[])Generator.Category.WAND.classes;

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ZAP);
        actions.add(AC_SETTING);
        return actions;
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return wand.targetingPos(user, dst);
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_SETTING)) {
            GameScene.show(new SettingsWindow());
        } else if (action.equals(AC_ZAP)) {
            curUser = hero;
            curItem = this;
            wand.charge(curUser, 1);
            if (cursed) {
                // 自定义诅咒施法流程
                GameScene.selectCell(curseZapper);
            } else {
                // 正常施法：委托给内部法杖
                wand.curCharges = wand.maxCharges;
                wand.execute(curUser, AC_ZAP);
            }
        }
    }


    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("selectedWandType", selectedWandType);
        bundle.put("wandLevel", wandLevel);
        bundle.put("cursed", cursed);
        bundle.put("positiveOnly", positiveOnly);
        bundle.put("curseRarity", curseRarity);
        bundle.put("selectedCurse", selectedCurse);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        selectedWandType = bundle.getInt("selectedWandType");
        wandLevel = bundle.getInt("wandLevel");
        cursed = bundle.getBoolean("cursed");
        positiveOnly = bundle.getBoolean("positiveOnly");
        curseRarity = bundle.getInt("curseRarity");
        selectedCurse = bundle.getInt("selectedCurse");
    }
    /**
     * 获取当前要模拟的法杖（根据选择的类型和等级创建）
     */
    private Wand getWand() {
        // 如果类型或等级变化，或者首次调用，才重新创建
        if (wand == null ||
                wand.getClass() != WAND_LIST[selectedWandType] ||
                wand.level() != wandLevel) {
            wand = Reflection.newInstance(WAND_LIST[selectedWandType]);
            wand.level(wandLevel);
        }
        wand.cursed = cursed;
        return wand;
    }

    /**
     * 刷新法杖等级（当等级滑动条变化时调用）
     */
    private void setWandLevel(int level) {
        wandLevel = level;
        super.level(level);   // 同步到 CustomWand 自身的等级（影响信息显示等）
        if (wand != null) {
            wand.level(level);
        }
    }

    // ================== 诅咒效果数组定义 ==================
    private final CursedWand.CursedEffect[] commonEffects = {
            new CursedWand.BurnAndFreeze(),
            new CursedWand.SpawnRegrowth(),
            new CursedWand.RandomTeleport(),
            new CursedWand.RandomGas(),
            new CursedWand.RandomAreaEffect(),
            new CursedWand.Bubbles(),
            new CursedWand.RandomWand(),
            new CursedWand.SelfOoze()
    };

    private final CursedWand.CursedEffect[] uncommonEffects = {
            new CursedWand.RandomPlant(),
            new CursedWand.HealthTransfer(),
            new CursedWand.Explosion(),
            new CursedWand.LightningBolt(),
            new CursedWand.Geyser(),
            new CursedWand.SummonSheep(),
            new CursedWand.Levitate(),
            new CursedWand.Alarm()
    };

    private final CursedWand.CursedEffect[] rareEffects = {
            new CursedWand.SheepPolymorph(),
            new CursedWand.CurseEquipment(),
            new CursedWand.InterFloorTeleport(),
            new CursedWand.SummonMonsters(),
            new CursedWand.FireBall(),
            new CursedWand.ConeOfColors(),
            new CursedWand.MassInvuln(),
            new CursedWand.Petrify()
    };

    private final CursedWand.CursedEffect[] veryRareEffects = {
            new CursedWand.ForestFire(),
            new CursedWand.SpawnGoldenMimic(),
            new CursedWand.AbortRetryFail(),
            new CursedWand.RandomTransmogrify(),
            new CursedWand.HeroShapeShift(),
            new CursedWand.SuperNova(),
            new CursedWand.SinkHole(),
            new CursedWand.GravityChaos()
    };

    private CursedWand.CursedEffect getSelectedEffect() {
        int category = curseRarity;
        int index = selectedCurse;
        switch (category) {
            case 0: default:
                return commonEffects[index];
            case 1:
                return uncommonEffects[index];
            case 2:
                return rareEffects[index];
            case 3:
                return veryRareEffects[index];
        }
    }
    /**
     * 诅咒施法的具体执行
     */
    private void doCurseZap(int target) {
        final Ballistica shot = new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT);
        final int cell = shot.collisionPos;
        if (target == curUser.pos || cell == curUser.pos) {
            GLog.i(Messages.get(Wand.class, "self_target"));
            return;
        }

        curUser.sprite.zap(cell);
        curUser.busy();

        final CursedWand.CursedEffect effect = getSelectedEffect();
        effect.FX(this, curUser, shot, new Callback() {
            @Override
            public void call() {
                effect.effect(CustomWand.this, curUser, shot, positiveOnly);
                afterZap();
            }
        });
    }

    private void afterZap() {
        curUser.spendAndNext(1f);
        updateQuickslot();
    }

    // ================== 施法监听器 ==================
    private final CellSelector.Listener curseZapper = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null) {
                doCurseZap(target);
            }
        }
        @Override
        public String prompt() {
            return Messages.get(CustomWand.this, "prompt");
        }
    };

    // ================== UI 与杂项 ==================
    @Override
    public String status() {
        return "Inf/Inf";
    }

    @Override
    public String info(){
        return Messages.get(this, "desc", wand.name(), wand.name());
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    // ========== 设置窗口 ==========
    private class SettingsWindow extends Window {
        private static final int WIDTH = 120;
        private static final int BTN_SIZE = 16;
        private static final int GAP = 2;

        private ArrayList<IconButton> wandButtons = new ArrayList<>();
        private OptionSlider o_level;
        private CheckBox c_curse;
        private CheckBox c_positive;
        private RenderedTextBlock t_curseDesc;
        private OptionSlider o_curseRarity;
        private OptionSlider o_selectedCurse;

        public SettingsWindow() {
            // 创建法杖图标网格
            createWandGrid();

            // 等级滑动条
            o_level = new OptionSlider(Messages.get(this, "level"), "0", "12", 0, 12) {
                @Override
                protected void onChange() {
                    setWandLevel(getSelectedValue());
                }
            };
            o_level.setSelectedValue(wandLevel);
            add(o_level);

            // 诅咒复选框
            c_curse = new CheckBox(Messages.get(this, "curse")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    cursed = checked();
                    if (wand != null) wand.cursed = cursed;
                }
            };
            c_curse.checked(cursed);
            add(c_curse);

            // 正面效果复选框
            c_positive = new CheckBox(Messages.get(this, "positive")) {
                @Override
                protected void onClick() {
                    super.onClick();
                    positiveOnly = checked();
                }
            };
            c_positive.checked(positiveOnly);
            add(c_positive);

            t_curseDesc = PixelScene.renderTextBlock("",6);
            t_curseDesc.maxWidth(WIDTH);
            add(t_curseDesc);

            // 诅咒类别滑动条
            o_curseRarity = new OptionSlider(Messages.get(this, "curse_rarity"), "0", "3", 0, 3) {
                @Override
                protected void onChange() {
                    curseRarity = getSelectedValue();
                    updateCurseDesc();
                }
            };
            o_curseRarity.setSelectedValue(curseRarity);
            add(o_curseRarity);

            // 诅咒效果索引滑动条
            o_selectedCurse = new OptionSlider(Messages.get(this, "selected_curse"), "0", "7", 0, 7) {
                @Override
                protected void onChange() {
                    selectedCurse = getSelectedValue();
                }
            };
            o_selectedCurse.setSelectedValue(selectedCurse);
            add(o_selectedCurse);

            updateCurseDesc();
        }

        private void createWandGrid() {
            float top = GAP;
            int placed = 0;
            int length = WAND_LIST.length;
            int firstRow = (length % 2 == 0 ? length / 2 : (length / 2 + 1));
            for (int i = 0; i < length; i++) {
                final int index = i;
                IconButton btn = new IconButton() {
                    @Override
                    protected void onClick() {
                        selectedWandType = index;
                        // 清除旧的 targetWand，下次 getTargetWand 会重新创建
                        getWand();
                        // 可选：高亮当前选中的按钮
                        for (IconButton ib : wandButtons) ib.icon().resetColor();
                        icon().color(0xFFFF44);
                    }
                };
                // 获取法杖图标
                Wand sample = Reflection.newInstance(WAND_LIST[i]);
                Image im = new Image(Assets.Sprites.ITEMS);
                im.frame(ItemSpriteSheet.film.get(sample.image));
                im.scale.set(0.9f);
                btn.icon(im);
                // 布局
                float left;
                if (i < firstRow) {
                    left = (WIDTH - BTN_SIZE * firstRow) / 2f;
                    btn.setRect(left + placed * BTN_SIZE, top, BTN_SIZE, BTN_SIZE);
                } else {
                    left = (WIDTH - BTN_SIZE * (length - firstRow)) / 2f;
                    btn.setRect(left + (placed - firstRow) * BTN_SIZE, top + GAP + BTN_SIZE, BTN_SIZE, BTN_SIZE);
                }
                add(btn);
                wandButtons.add(btn);
                placed++;
            }
            // 高亮当前选中的法杖
            if (selectedWandType >= 0 && selectedWandType < wandButtons.size()) {
                wandButtons.get(selectedWandType).icon().color(0xFFFF44);
            }
        }

        private void layout() {
            // 法杖网格占位高度：两行 + 间隙
            float top = wandButtons.isEmpty() ? GAP : wandButtons.get(wandButtons.size() - 1).bottom() + GAP;
            // 等级滑动条
            o_level.setRect(0, top, WIDTH, 20);
            top = o_level.bottom() + GAP;

            // 两个复选框并排
            float halfWidth = (WIDTH - GAP) / 2f;
            c_curse.setRect(0, top, halfWidth, 18);
            c_positive.setRect(halfWidth + GAP, top, halfWidth, 18);
            top = c_curse.bottom() + GAP;

            // 描述文本
            t_curseDesc.setPos(0, top);
            t_curseDesc.maxWidth(WIDTH);
            top = t_curseDesc.bottom()+GAP;

            // 诅咒类别滑动条
            o_curseRarity.setRect(0, top, WIDTH, 20);
            top = o_curseRarity.bottom() + GAP;

            // 诅咒效果索引滑动条
            o_selectedCurse.setRect(0, top, WIDTH, 20);
            top = o_selectedCurse.bottom() + GAP;

            resize(WIDTH, (int) top);
        }
        private void updateCurseDesc() {
            t_curseDesc.text(Messages.get(this,"curse_rarity_" + curseRarity));
            layout(); // 重新布局以调整窗口高度
        }
    }
}