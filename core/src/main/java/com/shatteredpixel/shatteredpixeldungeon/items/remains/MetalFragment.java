package com.shatteredpixel.shatteredpixeldungeon.items.remains;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MetalFragment extends RemainsItem {

    {
        image = ItemSpriteSheet.SMALLSKURIKEN;
    }

    @Override
    protected void doEffect(Hero hero) {
        ArrayList<MissileWeapon> missileWeapons=hero.belongings.getAllItems(MissileWeapon.class);
        for(MissileWeapon missileWeapon:missileWeapons){
            if(!(missileWeapon instanceof Dart)){
                missileWeapon.repair(1);
            }
        }
        curUser.sprite.operate(curUser.pos);
        Sample.INSTANCE.play(Assets.Sounds.DRINK);
        updateQuickslot();
        curUser.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.1f, 10);
    }
}
