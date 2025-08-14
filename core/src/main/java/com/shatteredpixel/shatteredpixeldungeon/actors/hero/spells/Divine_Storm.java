package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Inferno;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

public class Divine_Storm extends TargetedClericSpell {

    public static final Divine_Storm INSTANCE = new Divine_Storm();

    @Override
    public int icon() {
        return HeroIcon.DIVINE_STORM;
    }

    @Override
    public int targetingFlags(){
        return -1; //auto-targeting behaviour is often wrong, so we don't use it
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.DIVINE_STORM);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 4;

    }
    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null){
            return;
        }

        Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
        if(hero.pointsInTalent(Talent.DIVINE_STORM)==1){
            for( int i : PathFinder.NEIGHBOURS9) {
                if (!Dungeon.level.solid[target + i]) {
                    GameScene.add(Blob.seed(target + i, 10, Electricity.class));
                }
            }
        }else{
            int cell = target;
            PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 3 );
            for (int i = 0; i < PathFinder.distance.length; i++) {
                if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                    GameScene.add(Blob.seed(i, 20, Electricity.class));
                }
            }
            if(hero.pointsInTalent(Talent.DIVINE_STORM)==3){
                PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 2 );
                for (int i = 0; i < PathFinder.distance.length; i++) {
                    if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                        if (Dungeon.level.pit[i] || Dungeon.level.water[i])
                            GameScene.add(Blob.seed(i, 1, Fire.class));
                        else
                            GameScene.add(Blob.seed(i, 5, Fire.class));
                        CellEmitter.get(i).burst(FlameParticle.FACTORY, 5);
                    }
                }
                Sample.INSTANCE.play(Assets.Sounds.BURNING);
            }
        }



        hero.busy();
        hero.sprite.operate(target);
        hero.spend( 1f );

        onSpellCast(tome, hero);
    }
    public String desc(){
        return Messages.get(this, "desc") +"\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
    }
}
