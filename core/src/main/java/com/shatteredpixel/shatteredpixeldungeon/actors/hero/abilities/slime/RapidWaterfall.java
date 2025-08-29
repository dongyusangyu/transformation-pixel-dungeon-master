package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.slime;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StormCloud;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.ReclaimTrap;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class RapidWaterfall extends ArmorAbility {

    {
        baseChargeUse =35f;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        int vol =5;
        vol *= 1+hero.pointsInTalent(Talent.VIOLENT_STORM)*0.25f;
        int thisvol = vol;
        for (int i : PathFinder.NEIGHBOURS8){
            if (!Dungeon.level.solid[hero.pos+i]){
                GameScene.add( Blob.seed( hero.pos+i, vol, RapidWater.class ) );
            } else {
                thisvol += vol;
            }
        }
        GameScene.add( Blob.seed( hero.pos, thisvol, RapidWater.class ) );
        hero.spendAndNext(Actor.TICK);
        armor.charge -= chargeUse(hero);
        Talent.onArmorAbility(hero, chargeUse(hero));
        armor.updateQuickslot();

    }

    @Override
    public int icon() {
        return HeroIcon.RAPID_WATERFALL;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.VIOLENT_STORM, Talent.NEW_TRAP, Talent.HOLY_BATH, Talent.HEROIC_ENERGY};
    }
    public static class RapidWater extends Blob {
        {
            actPriority = BLOB_PRIO;
        }
        public int Reclaim = 0;

        @Override
        protected void evolve() {
            super.evolve();

            int cell;

            Fire fire = (Fire) Dungeon.level.blobs.get(Fire.class);
            for (int i = area.left; i < area.right; i++){
                for (int j = area.top; j < area.bottom; j++){
                    cell = i + j*Dungeon.level.width();
                    if (cur[cell] > 0) {
                        setCellToWater(true, cell);
                        if (fire != null){
                            fire.clear(cell);
                        }
                        //fiery enemies take damage as if they are in toxic gas
                        Char ch = Actor.findChar(cell);
                        if (ch != null
                                && !ch.isImmune(getClass())
                                && Char.hasProp(ch, Char.Property.FIERY)){
                            ch.damage(1 + Dungeon.scalingDepth()/5, this);
                        }
                        if (ch != null){
                            if ( (ch.properties().contains(Char.Property.BOSS_MINION)
                                    || ch.properties().contains(Char.Property.BOSS))
                                    || (ch instanceof Hero)) {
                            }else if(ch.alignment != Char.Alignment.ALLY){
                                Clobber(ch);
                            }
                        }


                    }
                }
            }

        }

        public boolean Clobber(Char enemy){
            int target = hero.pos;
            int oldPos = enemy.pos;
            /*
            for (int i : PathFinder.NEIGHBOURS8){
                if (Dungeon.level.passable[enemy.pos+i]){
                    target = enemy.pos+i;
                }
            }

             */
            if(target == enemy.pos){
                return false;
            }
            //trace a ballistica to our target (which will also extend past them
            Ballistica trajectory = new Ballistica(target, enemy.pos, Ballistica.STOP_TARGET);
            //trim it to just be the part that goes past them
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
            //knock them back along that ballistica, ensuring they don't fall into a pit
            int dist = Math.max(1,hero.pointsInTalent(Talent.HOLY_BATH));
            if (!enemy.flying) {
                while (dist > trajectory.dist ||
                        (dist > 0 && Dungeon.level.pit[trajectory.path.get(dist)])) {
                    dist--;
                }
            }
            if (enemy.pos == oldPos) {
                WandOfBlastWave.throwChar(enemy, trajectory, dist, true, false, hero);
            }
            return true;
        }

        public boolean setCellToWater( boolean includeTraps, int cell ){
            Point p = Dungeon.level.cellToPoint(cell);

            //if a custom tilemap is over that cell, don't put water there
            for (CustomTilemap cust : Dungeon.level.customTiles){
                Point custPoint = new Point(p);
                custPoint.x -= cust.tileX;
                custPoint.y -= cust.tileY;
                if (custPoint.x >= 0 && custPoint.y >= 0
                        && custPoint.x < cust.tileW && custPoint.y < cust.tileH){
                    if (cust.image(custPoint.x, custPoint.y) != null){
                        return false;
                    }
                }
            }

            int terr = Dungeon.level.map[cell];
            if (terr == Terrain.EMPTY || terr == Terrain.GRASS ||
                    terr == Terrain.EMBERS || terr == Terrain.EMPTY_SP ||
                    terr == Terrain.HIGH_GRASS || terr == Terrain.FURROWED_GRASS
                    || terr == Terrain.EMPTY_DECO){
                Dungeon.level.set(cell, Terrain.WATER);
                GameScene.updateMap(cell);
                return true;
            } else if (includeTraps && (terr == Terrain.SECRET_TRAP ||
                    terr == Terrain.TRAP || terr == Terrain.INACTIVE_TRAP)){

                Dungeon.level.set(cell, Terrain.WATER);
                Dungeon.level.traps.remove(cell);
                if(hero.pointsInTalent(Talent.NEW_TRAP)> Random.Int(5) && Reclaim<3){
                    Reclaim++;
                    ReclaimTrap r = new ReclaimTrap();
                    Dungeon.level.drop(r,cell);
                }
                GameScene.updateMap(cell);
                return true;
            }

            return false;
        }

        @Override
        public void use( BlobEmitter emitter ) {
            super.use( emitter );
            emitter.pour( Speck.factory( Speck.STORM ), 0.4f );
        }

        @Override
        public String tileDesc() {
            return Messages.get(this, "desc");
        }
        private static final String RECLAIM = "reclaim";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(RECLAIM, Reclaim);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            Reclaim = bundle.getInt(RECLAIM);
        }
    }
}