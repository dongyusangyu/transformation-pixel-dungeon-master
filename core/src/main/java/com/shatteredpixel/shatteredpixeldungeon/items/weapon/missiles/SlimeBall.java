package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;

public class SlimeBall extends MissileWeapon{
    {
        image = ItemSpriteSheet.SLIMEBALL;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;

        bones = false;

        tier = 1;
        baseUses = 5;
    }


    @Override
    public int max(int lvl) {
        return  3 + 2*lvl;
    }

    @Override
    public int min(int lvl) {
        return  2 + lvl;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage ) {
        Buff.affect( defender, SlimeOoze.class,3);
        defender.sprite.showStatus(CharSprite.WARNING, Messages.get(SlimeOoze.class, "name"));
        return super.proc( attacker, defender, damage );
    }

    public static class SlimeOoze extends FlavourBuff {
        { type = buffType.NEGATIVE; }
        public int icon() { return BuffIndicator.OOZE; }
        public void tintIcon(Image icon) { icon.hardlight(0f, 1f, 0.5f); }
        @Override
        public boolean attachTo(Char target) {
            //target.sprite.showStatus(CharSprite.WARNING, Messages.get(this, "name"));
            return super.attachTo(target);
        }

    }
}
