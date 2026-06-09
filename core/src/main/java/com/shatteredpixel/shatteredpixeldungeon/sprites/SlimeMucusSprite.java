package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SlimeMucus;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Random;

public class SlimeMucusSprite extends MobSprite{



    public SlimeMucusSprite(){
        super();
        if(Dungeon.skin==1){
            texture( Assets.Sprites.SLIMEMUCUS1 );
        }else if(Dungeon.skin==2){
            texture( Assets.Sprites.SLIMEMUCUS2 );
        }else if(Dungeon.skin==3){
            texture( Assets.Sprites.SLIMEMUCUS3 );
        }else if(Dungeon.skin==4){
            texture( Assets.Sprites.SLIMEMUCUS4 );
        }else{
            texture( Assets.Sprites.SLIMEMUCUS );
        }

        TextureFilm frames = new TextureFilm( texture, 20, 16 );

        idle = new Animation( 8, true );
        idle.frames( frames, 0);

        run = idle.clone();
        attack = idle.clone();

        die = new Animation( 20, false );
        die.frames( frames, 0 );

        play( idle );
        curFrame = Random.Int( curAnim.frames.length );

    }

    @Override
    public int blood() {
        return 0x000800FF;
    }
}
