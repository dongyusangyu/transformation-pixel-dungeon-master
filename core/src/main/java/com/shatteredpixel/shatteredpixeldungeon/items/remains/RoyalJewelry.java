package com.shatteredpixel.shatteredpixeldungeon.items.remains;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

public class RoyalJewelry extends RemainsItem {

    {
        image = ItemSpriteSheet.ROYAL_JEWELRY;
    }

    @Override
    protected void doEffect(Hero hero) {
        Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
        Buff.affect(hero, JewelryEnhanced.class,30);
    }

    public static class JewelryEnhanced extends FlavourBuff {

        {
            type = Buff.buffType.POSITIVE;
        }

        @Override
        public boolean attachTo(Char target) {
            if (super.attachTo(target)){
                if (target instanceof Hero) ((Hero) target).updateHT(false);
                return true;
            }
            return false;
        }

        @Override
        public void detach() {
            super.detach();
            if (target instanceof Hero) ((Hero) target).updateHT(false);
        }

        @Override
        public int icon() {
            return BuffIndicator.UPGRADE;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(1.0f, 0.071f, 0.290f);
        }

        @Override
        public float iconFadePercent() {
            float max = 30;
            return Math.max(0, (max-visualcooldown()) / max);
        }

    }

}
