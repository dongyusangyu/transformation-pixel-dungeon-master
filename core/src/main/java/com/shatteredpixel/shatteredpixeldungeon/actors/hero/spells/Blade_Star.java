package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Blade_Star extends TargetedClericSpell {

    public static final Blade_Star INSTANCE = new Blade_Star();

    @Override
    public int icon() {
        return HeroIcon.BLADE_STAR;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 3;
    }

    @Override
    public int targetingFlags() {
        return Ballistica.STOP_TARGET;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.BLADE_STAR);
    }

    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {

        if (target == null){
            return;
        }

        if (Dungeon.level.solid[target] || !Dungeon.level.heroFOV[target]){
            GLog.w(Messages.get(this, "invalid_target"));
            return;
        }

        ArrayList<Char> affected = new ArrayList<>();

        PathFinder.buildDistanceMap(target, BArray.not(Dungeon.level.solid, null), hero.pointsInTalent(Talent.BLADE_STAR));
        for (int i = 0; i < Dungeon.level.length(); i++){
            if (PathFinder.distance[i] != Integer.MAX_VALUE){
                int c = Dungeon.level.map[i];
                GameScene.add(Blob.seed(i, 10,Blade_Stair.class));
                CellEmitter.get(i).burst(StairParticle.FACTORY, 2);

                Char ch = Actor.findChar(i);
                if (ch != null){
                    affected.add(ch);
                }
            }
        }


        for (Char ch : affected){
            affectChar(ch);
        }


        Sample.INSTANCE.play(Assets.Sounds.MELD);
        hero.sprite.zap(target);
        hero.spendAndNext( 1f );

        onSpellCast(tome, hero);

    }

    private void affectChar( Char ch ){
        if (ch.alignment != Char.Alignment.ALLY && !(ch instanceof Hero)){
            int dmg = Random.Int(4,9);
            ch.damage(dmg,this);
            Buff.affect(ch,Cripple.class,3);
        }
    }

    public String desc(){
        int area = 1 + 2*Dungeon.hero.pointsInTalent(Talent.BLADE_STAR);
        return Messages.get(this, "desc", area) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
    }

    public static class Blade_Stair extends Blob {

        @Override
        protected void evolve() {

            int cell;


            ArrayList<Char> affected = new ArrayList<>();


            for (int i = area.left-1; i <= area.right; i++) {
                for (int j = area.top-1; j <= area.bottom; j++) {
                    cell = i + j*Dungeon.level.width();
                    if (cur[cell] > 0) {


                        int c = Dungeon.level.map[cell];

                        Char ch = Actor.findChar(cell);
                        if (ch != null && !(ch instanceof Hero)){
                            affected.add(ch);
                        }

                        off[cell] = cur[cell] - 1;
                        volume += off[cell];
                    } else {
                        off[cell] = 0;
                    }
                }
            }

            //max of 100 turns of grass per hero level before it starts to furrow


            for (Char ch :affected){
                affectChar(ch);
            }

        }

        private void affectChar( Char ch ){
            if (ch.alignment == Char.Alignment.ENEMY){
                int dmg = Random.Int(4,9);
                ch.damage(dmg,this);
                Buff.affect(ch,Cripple.class,3);
            }
        }

        @Override
        public void use(BlobEmitter emitter) {
            super.use( emitter );
            emitter.pour( StairParticle.FACTORY, 1f );
        }

        @Override
        public String tileDesc() {
            return Messages.get(this, "desc");
        }
    }

    public static class StairParticle extends PixelParticle {

        public static final Emitter.Factory FACTORY = new Emitter.Factory() {
            @Override
            public void emit( Emitter emitter, int index, float x, float y ) {
                ((StairParticle)emitter.recycle( StairParticle.class )).reset( x, y );
            }
            @Override
            public boolean lightMode() {
                return true;
            }
        };

        public StairParticle() {
            super();
            color( 0xFFD800 );
            lifespan = 1.2f;
            speed.set( 0, -6 );
        }

        private float offs;

        public void reset( float x, float y ) {
            revive();

            this.x = x;
            this.y = y;

            offs = -Random.Float( lifespan );
            left = lifespan - offs;
        }

        @Override
        public void update() {
            super.update();

            float p = left / lifespan;
            am = p < 0.5f ? p : 1 - p;
            scale.x = (1 - p) * 4;
            scale.y = 16 + (1 - p) * 16;
        }
    }


}

