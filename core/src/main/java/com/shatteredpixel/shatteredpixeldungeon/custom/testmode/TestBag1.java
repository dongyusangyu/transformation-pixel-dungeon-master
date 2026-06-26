package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class TestBag1 extends Bag {

    private static final String TESTMODE_PACKAGE = "com.shatteredpixel.shatteredpixeldungeon.custom.testmode";

    {
        image = ItemSpriteSheet.CRYSTAL_CHEST;
    }

    @Override
    public boolean canHold(Item item) {
        if (item instanceof Bag) {
            return false;
        }

        Package itemPackage = item.getClass().getPackage();
        if (itemPackage != null && itemPackage.getName().startsWith(TESTMODE_PACKAGE)) {
            return super.canHold(item);
        } else {
            return false;
        }
    }

    @Override
    public int value() {
        return 40;
    }

    public int capacity(){
        return 24;
    }
}
