package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss.Drunkenness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import java.util.ArrayList;

/** One-use reward: grants the low-risk side of the wine duel and a buffer. */
public class GreenGlowFruit extends Food {
	{ image = EXItemSpriteSheet.GREEN_GLOW_FRUIT; stackable = true; bones = false;
        energy = Hunger.HUNGRY/3f;}

    @Override
    protected void satisfy(Hero hero) {
        if(hero.hasTalent(Talent.VEGETARIANISM)){
            energy*=2f;
            Buff.affect(hero, WellFed.class).reset();
            if (hero.pointsInTalent(Talent.VEGETARIANISM)==2){
                Buff.affect(hero, Healing.class).setHeal((int)(hero.HT/4),0.25f,0);
            }
        }
        Drunkenness.affectTemporary(hero);
        Buff.affect(hero, Barrier.class).setShield(40);
        super.satisfy(hero);

    }
}
