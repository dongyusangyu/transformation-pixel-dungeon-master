package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Genesis;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Indulgence extends ClericSpell {
    public static Indulgence INSTANCE = new Indulgence();

    @Override
    public int icon() {
        return HeroIcon.INDULGENCE;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 0;
    }

    public String desc(){
        return Messages.get(this, "desc",250-50*hero.pointsInTalent(Talent.INDULGENCE)) +"\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.INDULGENCE) && Dungeon.gold>250-50*hero.pointsInTalent(Talent.INDULGENCE);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        HolyTome tome1 = hero.belongings.getItem(HolyTome.class);
        if (tome1 != null) {
            tome1.directCharge( 1f);
            ScrollOfRecharging.charge(hero);
            Dungeon.gold-=250-50*hero.pointsInTalent(Talent.INDULGENCE);
        }else{
            GLog.w(Messages.get(this, "no_tome"));
            return;
        }

        hero.busy();
        hero.spend( 1f );
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.READ);
        new Flare( 6, 32 ).color(0xFFFF00, true).show( hero.sprite, 2f );

        onSpellCast(tome, hero);

    }
}
