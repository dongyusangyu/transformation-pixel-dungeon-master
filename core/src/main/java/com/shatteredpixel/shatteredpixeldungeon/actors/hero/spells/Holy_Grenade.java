package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.HolyBomb;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Holy_Grenade extends TargetedClericSpell {
    public static final Holy_Grenade INSTANCE = new Holy_Grenade();

    @Override
    public int icon() {
        return HeroIcon.HOLY_GRENADE;
    }


    @Override
    public float chargeUse(Hero hero) {
        return 2;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.HOLY_GRENADE);
    }
    @Override
    public int targetingFlags() {
        return Ballistica.STOP_TARGET;
    }

    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null){
            return;
        }
        if (Dungeon.level.heroFOV[target]) {
            new Flare(10, 64).show(Dungeon.hero.sprite.parent, DungeonTilemap.tileCenterToWorld(target), 2f);
        }
        int area = 1 + Dungeon.hero.pointsInTalent(Talent.HOLY_GRENADE)/3;
        ArrayList<Char> affected = new ArrayList<>();
        PathFinder.buildDistanceMap( target, BArray.not( Dungeon.level.solid, null ), area );
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                Char ch = Actor.findChar(i);
                if (ch != null) {
                    affected.add(ch);

                }
            }
        }
        for (Char ch : affected){
            if(ch instanceof Hero){

            } else if (ch.properties().contains(Char.Property.UNDEAD) || ch.properties().contains(Char.Property.DEMONIC)){
                ch.sprite.emitter().start( ShadowParticle.UP, 0.05f, 10 );
                //bomb deals an additional 50% damage to unholy enemies
                int damage =20 + 10*Math.min(Dungeon.hero.pointsInTalent(Talent.HOLY_GRENADE),2);
                ch.damage(damage, new HolyBomb.HolyDamage());
                if (ch.isAlive()){
                    Buff.affect(ch, GuidingLight.Illuminated.class);
                }
            }else{
                int damage =Random.Int(10 + 5*Math.min(Dungeon.hero.pointsInTalent(Talent.HOLY_GRENADE),2),20 + 10*Math.min(Dungeon.hero.pointsInTalent(Talent.HOLY_GRENADE),2));
                ch.damage(damage, new HolyBomb.HolyDamage());
                if (ch.isAlive() && hero.subClass== HeroSubClass.PRIEST){
                    Buff.affect(ch, GuidingLight.Illuminated.class);
                }
            }
        }



        Sample.INSTANCE.play( Assets.Sounds.HOLY_HAND,1f );
        //Sample.INSTANCE.play( Assets.Sounds.BLAST );
        hero.busy();
        hero.sprite.operate(target);
        hero.spend( 1f );
        onSpellCast(tome, hero);
    }



    public String desc(){
        int area = 3 + 2*(hero.pointsInTalent(Talent.HOLY_GRENADE)/2);
        int mindmg = 10 + 5*Math.min(hero.pointsInTalent(Talent.HOLY_GRENADE),2);
        int maxdmg = 20 + 10*Math.min(hero.pointsInTalent(Talent.HOLY_GRENADE),2);
        return Messages.get(this, "desc", area,mindmg,maxdmg) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }

}
