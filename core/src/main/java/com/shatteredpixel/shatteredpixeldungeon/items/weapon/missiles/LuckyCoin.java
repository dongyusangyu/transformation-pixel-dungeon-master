package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class LuckyCoin extends MissileWeapon{
    {
        image = ItemSpriteSheet.LUCKYCOIN;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;

        bones = false;

        tier = 1;
        baseUses = 10;
        sticky = false;
    }

    @Override
    public int max(int lvl) {
        return  1 + 2*lvl;
    }

    @Override
    public int min(int lvl) {
        return  lvl;
    }

    @Override
    public int proc( Char attacker, Char defender, int damage ) {
        Buff.affect( attacker, LuckyStrikeTracker.class,3+buffedLvl());
        return super.proc( attacker, defender, damage );
    }

    public static class LuckyStrikeTracker extends FlavourBuff {
        { type = buffType.POSITIVE; }
        public int icon() { return BuffIndicator.LUCKYCOIN; }
        public void tintIcon(Image icon) { icon.hardlight(0f, 1f, 0f); }//颜色还未自定义

    }
}
