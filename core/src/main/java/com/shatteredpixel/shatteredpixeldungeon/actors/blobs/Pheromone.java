package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ChaosDisciples;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

public class Pheromone extends Blob {
    {
        //acts after mobs, to give them a chance to resist paralysis
        actPriority = MOB_PRIO - 1;
    }
    @Override
    protected void evolve() {
        super.evolve();

        Char ch;
        int cell;

        for (int i = area.left; i < area.right; i++){
            for (int j = area.top; j < area.bottom; j++){
                cell = i + j*Dungeon.level.width();
                if (cur[cell] > 0 && (ch = Actor.findChar( cell )) != null) {
                    if (!ch.isImmune(this.getClass())) {
                        if (!ch.buffs(ScrollOfSirensSong.Enthralled.class).isEmpty() && (ch instanceof Mob)) {
                            ch.remove(ScrollOfSirensSong.Enthralled.class);
                            ch.alignment= Char.Alignment.ENEMY;
                            ((Mob) ch).aggro(null);
                        }
                        if(ch instanceof Hero || ch.alignment == hero.alignment){
                            Buff.affect(ch, Charm.class,10);
                        }else{
                            if(!(ch instanceof ChaosDisciples.SuccBoss)) {
                                Buff.affect(ch, Adrenaline.class,5);
                                ch.HP = Math.min(ch.HP + (int) (ch.HT / 50), ch.HT);
                                ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString((int) (ch.HT / 50)), FloatingText.HEALING);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void use( BlobEmitter emitter ) {
        super.use( emitter );

        emitter.pour( Speck.factory( Speck.PHEROMONE ), 0.4f );
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }

}
