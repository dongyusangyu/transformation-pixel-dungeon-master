package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class TestSublimation extends TestGenerator {
    {
        image = ItemSpriteSheet.SCROLL_SUBLITION;
    }

    private String type = "Goo";
    private ArrayList<String> types() {
        ArrayList<String> types = new ArrayList<>();
        types.add("Goo");
        types.add("WARRIOR");
        types.add("TENGU");
        types.add("DM300");
        types.add("DWARFKING");
        types.add("YOG");
        //请在此处增加新Boss，再修改对应文本，无需修改其他代码
        return types;
    }

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

    private void createScroll() {
        ScrollOfSublimation scroll = new ScrollOfSublimation().type(type);
        if (scroll.collect()) {
            GLog.i(Messages.get(this, "collect_success", scroll.name()));
        } else {
            scroll.doDrop(curUser);
        }
    }

    private class SettingsWindow extends Window {
        private static final int WIDTH = 120;
        private static final int GAP = 2;
        private RenderedTextBlock t_scrollInfo;
        private OptionSlider o_type;
        private RedButton b_create;

        public SettingsWindow() {
            super();

            o_type = new OptionSlider(Messages.get(this, "type"),
                    "0",
                    String.valueOf(types().size()-1),
                    0,
                    types().size()-1) {
                @Override
                protected void onChange() {
                    type = types().get(getSelectedValue());
                    updateText();
                }
            };
            add(o_type);

            t_scrollInfo = PixelScene.renderTextBlock("", 6);
            t_scrollInfo.text(infoBuilder());
            t_scrollInfo.visible = true;
            t_scrollInfo.maxWidth(WIDTH);
            add(t_scrollInfo);

            b_create = new RedButton(Messages.get(this, "create_button")) {
                @Override
                protected void onClick() {
                    createScroll();
                }
            };
            add(b_create);

            layout();
        }

        private void layout() {
            o_type.setRect(0, GAP, WIDTH, 24);
            t_scrollInfo.setPos(0, GAP + o_type.bottom());
            b_create.setRect(0, t_scrollInfo.bottom() + GAP, WIDTH, 16);
            resize(WIDTH, (int) b_create.bottom());
        }

        private String infoBuilder() {
            String desc = "";
            desc += Messages.get(this, "type_name");
            return desc;
        }

        private void updateText() {
            t_scrollInfo.text(infoBuilder());
            layout();
        }
    }
}