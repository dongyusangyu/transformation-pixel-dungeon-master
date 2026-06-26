package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndActionList extends Window {

    private static final int WIDTH = 117;
    private static int HEIGHT;
    private static final int GAP  = 2;
    private static final int BUTTON_SIZE  = 21;

    public WndActionList(){
        super();

        float pos = GAP;
        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(this, "title")), 9);
        title.hardlight(TITLE_COLOR);
        title.setPos((WIDTH-title.width())/2, pos);
        title.maxWidth(WIDTH - GAP * 2);
        add(title);
        pos += GAP + title.height();

        RenderedTextBlock desc = PixelScene.renderTextBlock(Messages.capitalize(Messages.get(this, "desc")), 6);
        desc.setPos((WIDTH-desc.width())/2, pos);
        desc.maxWidth(WIDTH - GAP * 2);
        add(desc);
        pos += GAP + desc.height();

        int index = 0;

        ArrayList<ActionIndicator.Action> possibleActions = new ArrayList<>();
        for (Class<? extends Buff> possibleAction : ActionIndicator.actionBuffClasses){
            for(Buff b : Dungeon.hero.buffs(possibleAction)){
                if (ActionIndicator.canShowAction((ActionIndicator.Action)b))
                    possibleActions.add((ActionIndicator.Action) b);
            }
        }

        for (ActionIndicator.Action possibleAction : possibleActions) {
            ActionButton actionBtn = new ActionButton(possibleAction);
            actionBtn.hardlight(possibleAction.indicatorColor());
            float x = GAP + index%5 * (BUTTON_SIZE+GAP);
            float y = (pos + index/5 * (BUTTON_SIZE+GAP));
            actionBtn.setRect(x, y, BUTTON_SIZE, BUTTON_SIZE);
            add(actionBtn);

            index++;
            HEIGHT = (int) actionBtn.bottom();
        }
        resize(WIDTH, HEIGHT);
    }

    public class ActionButton extends StyledButton {

        ActionIndicator.Action action;
        Visual primaryVis;
        Visual secondVis;

        public ActionButton(ActionIndicator.Action action){
            super(Chrome.Type.TAG_BUTTUN, "");
            hotArea.blockLevel = PointerArea.NEVER_BLOCK;
            this.leftJustify = true;
            this.multiline = true;
            this.enable(true);

            this.action = action;
            primaryVis = action.primaryVisual();
            secondVis = action.secondaryVisual();
            add(primaryVis);
            if (secondVis != null)
                add(secondVis);
        }

        @Override
        protected void onClick() {
            super.onClick();
            ActionIndicator.setAction(action);
            //action.doAction();
            WndActionList.this.hide();
        }

        @Override
        protected boolean onLongClick() {
            ActionIndicator.setAction(action);
            WndActionList.this.hide();
            return true;
        }

        public void hardlight(int color){
            bg.hardlight(color);
        }

        @Override
        public void update() {
            super.update();

            if (primaryVis != null){
                primaryVis.x = x + (20 - primaryVis.width()) / 2f + 1;
                primaryVis.y = y + (20 - primaryVis.height()) / 2f;
                PixelScene.align(primaryVis);
                if (secondVis != null){
                    if (secondVis.width() > 16) secondVis.x = primaryVis.center().x - secondVis.width()/2f;
                    else                        secondVis.x = primaryVis.center().x + 8 - secondVis.width();
                    if (secondVis instanceof BitmapText){
                        //need a special case here for text unfortunately
                        secondVis.y = primaryVis.center().y + 8 - ((BitmapText) secondVis).baseLine();
                    } else {
                        secondVis.y = primaryVis.center().y + 8 - secondVis.height();
                    }
                    PixelScene.align(secondVis);
                }
            }
        }
    }
}
