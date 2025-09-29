package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestTalent;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TransformSpell extends Spell {
    {
        image = ItemSpriteSheet.TRANSFORM_SPELL;
    }
    @Override
    protected void onCast(Hero hero) {
        updateQuickslot();
        Catalog.countUse(getClass());
        GameScene.show(new TestTalent.WndMetamorphChoose());
    }
    public void confirmCancelation( Window chooseWindow ) {
        GameScene.show( new WndOptions(new ItemSprite(this),
                Messages.titleCase(name()),
                Messages.get(InventoryScroll.class, "warning"),
                Messages.get(InventoryScroll.class, "yes"),
                Messages.get(InventoryScroll.class, "no") ) {
            @Override
            protected void onSelect( int index ) {
                switch (index) {
                    case 0:
                        curUser.spendAndNext( 1f );
                        chooseWindow.hide();
                        break;
                    case 1:
                        //do nothing
                        break;
                }
            }
            public void onBackPressed() {}
        } );
    }
    @Override
    public int value() {
        return (int)(50 * quantity);
    }

    @Override
    public int energyVal() {
        return (int)(24 * quantity);
    }

    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

        private static final int OUT_QUANTITY = 1;

        {
            inputs =  new Class[]{ScrollOfMetamorphosis.class};
            inQuantity = new int[]{8};

            cost = 1;

            output = TransformSpell.class;
            outQuantity = OUT_QUANTITY;
        }

    }
}
