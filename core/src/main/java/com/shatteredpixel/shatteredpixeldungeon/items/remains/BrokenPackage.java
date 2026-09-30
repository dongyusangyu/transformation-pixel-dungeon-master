package com.shatteredpixel.shatteredpixeldungeon.items.remains;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class BrokenPackage extends RemainsItem {

    private static final int GOLD_AMOUNT = 114;

    {
        image = ItemSpriteSheet.BROKEN_PACKAGE;
    }

    @Override
    protected void doEffect(Hero hero) {
        Dungeon.gold += GOLD_AMOUNT;
        if (hero.sprite != null) {
            hero.sprite.showStatusWithIcon(CharSprite.NEUTRAL,
                    Integer.toString(GOLD_AMOUNT), FloatingText.GOLD);
        }
        Sample.INSTANCE.play( Assets.Sounds.GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
    }
}
