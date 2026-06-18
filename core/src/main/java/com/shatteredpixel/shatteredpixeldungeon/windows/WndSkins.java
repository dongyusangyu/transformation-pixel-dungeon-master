/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Image;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Objects;

public class WndSkins extends Window {

    private final int WIDTH = 80;
    private final int HEIGHT = 80;
    private static final int TTL_HEIGHT = 15;
    private static final int GAP = 5;

    private boolean editable;
    private HeroClass heroClass;

    public static int value=0;
    public RenderedTextBlock SkinText;
    public RenderedTextBlock title;
    ScrollPane pane;
    protected static final int BUTTON_WIDTH	= 20;
    private ArrayList<StyledButton> SkinBtns = new ArrayList<>();

    public WndSkins(HeroClass heroClass) {

        super();

        this.editable = editable;
        resize(WIDTH, HEIGHT);

        title = PixelScene.renderTextBlock(Messages.get(this, "title"), 10);

        title.hardlight(TITLE_COLOR);
        title.setPos(
                (WIDTH - title.width()) / 2,
                (TTL_HEIGHT - title.height()) / 2
        );
        PixelScene.align(title);
        add(title);
        if(heroClass==null){
            heroClass=HeroClass.WARRIOR;
        }
        this.heroClass = heroClass;
        GamesInProgress.skin = validSkin(heroClass, GamesInProgress.skin);
        float pos = title.bottom()+GAP;
        if(GamesInProgress.skin==0){
            SkinText = PixelScene.renderTextBlock(Messages.get(this, "cur0"), 6);
        }else{
            SkinText = PixelScene.renderTextBlock(Messages.get(this, "cur",GamesInProgress.skin), 6);

        }

        SkinText.text();
        updateText();

        pane = new ScrollPane(new Component()){
            public void onClick(float x, float y) { WndSkins.this.onClick(x, y);}
        };
        add(pane);
        pane.setRect(0, pos,WIDTH,60);
        Component content = pane.content();
        float x = 0;
        pos=0;
        for (int sk=0 ; sk<heroClass.getSkinNums();sk++) {
            SkinBtn button = new SkinBtn(heroClass,sk){
                @Override
                protected void onClick() {
                    super.onClick();
                    updateText();
                }
            };
            content.add(button);
            //button.setPos(x,pos);
            button.setRect(x, pos, SkinBtn.WIDTH, SkinBtn.HEIGHT);
            x += BUTTON_WIDTH;
            if(x>= WIDTH){
                x=0;
                pos+=BUTTON_WIDTH;
            }

            SkinBtns.add(button);
            //pos = button.right() + GAP;
        }
        content.setRect(0,0,WIDTH, pos+BUTTON_WIDTH);
        pane.setRect(0, title.bottom()+GAP,WIDTH,60);
        pane.scrollTo(0, 0);
        pane.update();

        add(SkinText);
        layout();
    }
    private void updateText() {
        if(GamesInProgress.skin==0){
            SkinText.text(Messages.get(this, "cur0"));
        }else{
            SkinText.text(Messages.get(this, "cur",GamesInProgress.skin));
        }
        PixelScene.align(SkinText);

        layout();
    }
    private void layout() {
        SkinText.setPos(GAP, HEIGHT-GAP-SkinText.height());
    }

    @Override
    public void onBackPressed() {
        SPDSettings.Skin(heroClass, GamesInProgress.skin);
        super.onBackPressed();
    }
    @Override
    public void offset(int xOffset, int yOffset) {
        super.offset(xOffset, yOffset);
        // refresh the scrollbar pane
        pane.setPos(pane.left(), pane.top());
    }
    protected void onClick(float x, float y) {/* do nothing */}

    private static class SkinBtn extends StyledButton {

        private int sk;
        private HeroClass heroClass;

        private static final int WIDTH = 20;
        private static final int HEIGHT = 20;
        ScrollPane pane;

        SkinBtn(HeroClass heroClass,int sk) {
            super(Chrome.Type.GREY_BUTTON_TR, "");
            this.heroClass = heroClass;
            this.sk = sk;
            icon(new Image(heroClass.spritesheet(sk), 0, 90, 12, 15));
        }
        @Override
        protected void onClick() {

            if(!isSkinUnlocked(heroClass, sk)){
                showUnlockMessage(heroClass, sk);
                return;
            }else{
                GamesInProgress.skin = sk;
                SPDSettings.Skin(heroClass, GamesInProgress.skin);
                SPDSettings.Skin(GamesInProgress.skin);
            }
            super.onClick();
        }


        @Override
        public void update() {
            super.update();
            if (sk != GamesInProgress.skin){
                icon.brightness(0.3f);
            } else {
                icon.brightness(1f);
            }
        }
    }

    public static int validSkin(HeroClass heroClass, int skin) {
        if (heroClass == null || skin < 0 || skin >= heroClass.getSkinNums() || !isSkinUnlocked(heroClass, skin)) {
            return 0;
        }
        return skin;
    }

    public static int savedSkin(HeroClass heroClass) {
        return validSkin(heroClass, SPDSettings.Skin(heroClass));
    }

    public static boolean isSkinUnlocked(HeroClass heroClass, int skin) {
        if (heroClass == null || skin < 0 || skin >= heroClass.getSkinNums()) {
            return false;
        }
        if(heroClass==HeroClass.SLIMEGIRL && skin==4 && !Badges.isUnlocked(Badges.Badge.HEROBOSS_COUNTER_1)){
            return false;
        }else if(heroClass==HeroClass.NINJA && skin==2 && !Badges.isUnlocked(Badges.Badge.HEROBOSS_COUNTER_2)){
            return false;
        }else if(heroClass==HeroClass.DM400 && skin==2 && !Badges.isUnlocked(Badges.Badge.HEROBOSS_COUNTER_3)){
            return false;
        }else if(heroClass==HeroClass.FREEMAN && skin==2 && !Badges.isUnlocked(Badges.Badge.BETTER_TALENT)){
            return false;
        }
        return true;
    }

    private static void showUnlockMessage(HeroClass heroClass, int skin) {
        if(heroClass==HeroClass.SLIMEGIRL && skin==4){
                ShatteredPixelDungeon.scene().addToFront( new WndMessage(Messages.get(WndSkins.class, "unlock1")));
        }else if(heroClass==HeroClass.NINJA && skin==2){
                ShatteredPixelDungeon.scene().addToFront( new WndMessage(Messages.get(WndSkins.class, "unlock2")));
        }else if(heroClass==HeroClass.DM400 && skin==2){
                ShatteredPixelDungeon.scene().addToFront( new WndMessage(Messages.get(WndSkins.class, "unlock3")));
        }else if(heroClass==HeroClass.FREEMAN && skin==2){
                ShatteredPixelDungeon.scene().addToFront( new WndMessage(Messages.get(WndSkins.class, "unlock4")));
        }
    }
}
