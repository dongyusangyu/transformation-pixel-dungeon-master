package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MimicTooth;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MimicSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class TransformMimic extends Mimic {
    {
        spriteClass = MimicSprite.Transform.class;
    }
    @Override
    public String name() {
        if (alignment == Alignment.NEUTRAL){
            return Messages.get(this, "hidden_name");
        } else {
            return super.name();
        }
    }

    @Override
    public String description() {
        if (alignment == Alignment.NEUTRAL){
            return Messages.get(this, "hidden_desc");
        } else {
            return super.description();
        }
    }

    @Override
    protected void generatePrize( boolean useDecks ){}

}
