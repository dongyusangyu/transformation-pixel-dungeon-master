package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM300Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HuntressBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.KingSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RogueBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TenguSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WarriorBossSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.YogSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class TestSublimation extends TestGenerator {
    {
        image = ItemSpriteSheet.SCROLL_SUBLITION;
    }

    private String type = "GOO";

    private static final BossOption[] FIRST_ROW = new BossOption[]{
            new BossOption("GOO", "Goo"),
            new BossOption("TENGU", "TENGU"),
            new BossOption("DM300", "DM300"),
            new BossOption("DWARFKING", "DWARFKING"),
            new BossOption("YOG", "YOG")
    };

    private static final BossOption[] SECOND_ROW = new BossOption[]{
            new BossOption("WARRIOR", "WARRIOR"),
            new BossOption("ROGUE", "ROGUE"),
            new BossOption("HUNTRESS", "HUNTRESS")
    };

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
        private static final int WIDTH = 132;
        private static final int GAP = 2;
        private static final int BTN_SIZE = 24;
        private static final int ICON_SIZE = 20;
        private RenderedTextBlock t_scrollInfo;
        private RedButton b_create;
        private BossButton[] bossButtons;

        public SettingsWindow() {
            super();

            createBossButtons();

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
            layoutBossButtons(FIRST_ROW, 0, GAP);
            layoutBossButtons(SECOND_ROW, 1, GAP + BTN_SIZE + GAP);
            t_scrollInfo.setPos(0, GAP + (BTN_SIZE + GAP) * 2);
            b_create.setRect(0, t_scrollInfo.bottom() + GAP, WIDTH, 16);
            resize(WIDTH, (int) b_create.bottom());
        }

        private String infoBuilder() {
            return Messages.get(this, "type_name", "_" + bossName(type) + "_");
        }

        private void updateText() {
            updateBossButtons();
            t_scrollInfo.text(infoBuilder());
            layout();
        }

        private void createBossButtons(){
            bossButtons = new BossButton[FIRST_ROW.length + SECOND_ROW.length];
            int index = 0;
            for (BossOption option : FIRST_ROW){
                bossButtons[index++] = createBossButton(option);
            }
            for (BossOption option : SECOND_ROW){
                bossButtons[index++] = createBossButton(option);
            }
            updateBossButtons();
        }

        private BossButton createBossButton(final BossOption option){
            BossButton button = new BossButton(option);
            button.icon(bossImage(option.type));
            add(button);
            return button;
        }

        private void layoutBossButtons(BossOption[] options, int row, float top){
            int count = options.length;
            float firstRowLeft = (WIDTH - FIRST_ROW.length * BTN_SIZE - (FIRST_ROW.length - 1) * GAP) / 2f;
            float left = row == 0 ? firstRowLeft : firstRowLeft;
            int start = row == 0 ? 0 : FIRST_ROW.length;
            for (int i = 0; i < count; i++){
                bossButtons[start + i].setRect(left + i * (BTN_SIZE + GAP), top, BTN_SIZE, BTN_SIZE);
            }
        }

        private void updateBossButtons(){
            if (bossButtons == null) return;
            for (BossButton button : bossButtons){
                button.updateSelected();
            }
        }

        private Image bossImage(String type){
            switch (type){
                case "TENGU":
                    return spriteImage(new TenguSprite());
                case "DM300":
                    return spriteImage(new DM300Sprite());
                case "DWARFKING":
                    return spriteImage(new KingSprite());
                case "YOG":
                    return spriteImage(new YogSprite());
                case "WARRIOR":
                    return spriteImage(new WarriorBossSprite());
                case "ROGUE":
                    return spriteImage(new RogueBossSprite());
                case "HUNTRESS":
                    return spriteImage(new HuntressBossSprite());
                case "GOO": default:
                    return spriteImage(new GooSprite());
            }
        }

        private String bossName(String type){
            switch (type){
                case "TENGU":
                    return "TENGU";
                case "DM300":
                    return "DM300";
                case "DWARFKING":
                    return "DWARFKING";
                case "YOG":
                    return "YOG";
                case "WARRIOR":
                    return "WARRIOR";
                case "ROGUE":
                    return "ROGUE";
                case "HUNTRESS":
                    return "HUNTRESS";
                case "GOO": default:
                    return "Goo";
            }
        }

        private Image spriteImage(CharSprite sprite){
            Image image = new Image(sprite);
            float scale = Math.min(1f, ICON_SIZE / Math.max(image.width(), image.height()));
            image.scale.set(scale);
            return image;
        }

        private class BossButton extends IconButton {
            private final BossOption option;

            private BossButton(BossOption option){
                super();
                this.option = option;
            }

            @Override
            protected void onClick() {
                type = option.type;
                updateText();
                super.onClick();
            }

            @Override
            protected void onPointerUp() {
                super.onPointerUp();
                updateSelected();
            }

            @Override
            protected String hoverText() {
                return option.label;
            }

            private void updateSelected(){
                if (icon() == null) return;
                icon().resetColor();
                if (option.type.equals(type)){
                    icon().hardlight(1.6f, 1.35f, 0.45f);
                    icon().alpha(1f);
                } else {
                    icon().alpha(0.55f);
                }
            }
        }
    }

    private static class BossOption {
        private final String type;
        private final String label;

        private BossOption(String type, String label){
            this.type = type;
            this.label = label;
        }
    }
}
