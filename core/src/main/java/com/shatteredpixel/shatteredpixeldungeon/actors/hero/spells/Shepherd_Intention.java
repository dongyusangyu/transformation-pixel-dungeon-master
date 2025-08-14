package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class Shepherd_Intention extends TargetedClericSpell {
    public static final Shepherd_Intention INSTANCE = new Shepherd_Intention();

    @Override
    public int icon() {
        return HeroIcon.SHEPHERD_INTENTION;
    }

    @Override
    public int targetingFlags(){
        return -1; //auto-targeting behaviour is often wrong, so we don't use it
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.SHEPHERD_INTENTION);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 3;
    }

    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null){
            return;
        }

        int cell =target;

        PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), hero.pointsInTalent(Talent.SHEPHERD_INTENTION) );
        ArrayList<Integer> spawnPoints = new ArrayList<>();
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                spawnPoints.add(i);
            }
        }

        for (int i : spawnPoints){
            if (Dungeon.level.insideMap(i)
                    && Actor.findChar(i) == null
                    && !(Dungeon.level.pit[i])) {
                Sheep sheep = new Sheep();
                sheep.initialize(10);
                sheep.pos = i;
                GameScene.add(sheep);
                Dungeon.level.occupyCell(sheep);
                CellEmitter.get(i).burst(Speck.factory(Speck.WOOL), 4);
            }
        }

        CellEmitter.get(cell).burst(Speck.factory(Speck.WOOL), 4);
        Sample.INSTANCE.play(Assets.Sounds.PUFF);
        Sample.INSTANCE.play(Assets.Sounds.SHEEP);
        hero.sprite.zap(cell);
        hero.spendAndNext( 1f );

        onSpellCast(tome, hero);
    }



    public String desc(){
        return Messages.get(this, "desc") +"\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
    }

}
