package com.shatteredpixel.shatteredpixeldungeon.items.remains;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class BottledEnergy extends RemainsItem {
    {
        image = ItemSpriteSheet.BOTTLEDENERGY;
    }

    @Override
    protected void doEffect(Hero hero) {
        Catalog.setSeen(getClass());
        Statistics.itemTypesDiscovered.add(getClass());
        int energy=2+hero.lvl/5;
        Dungeon.energy += energy;
        hero.sprite.showStatusWithIcon( 0x44CCFF, Integer.toString(energy), FloatingText.ENERGY );
        hero.spendAndNext( pickupDelay() );

        Sample.INSTANCE.play( Assets.Sounds.ITEM );

        updateQuickslot();
    }
}
