package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class Ascension_Curse  extends ClericSpell {
    public static final Ascension_Curse INSTANCE = new Ascension_Curse();

    @Override
    public int icon() {
        return HeroIcon.ASCENSION_CURSE;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 1;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.ASCENSION_CURSE);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        Buff.affect(hero, Levitation.class,hero.pointsInTalent(Talent.ASCENSION_CURSE)*3+2);
        Sample.INSTANCE.play(Assets.Sounds.READ);
        hero.spend( 1f );
        hero.busy();
        hero.sprite.operate(hero.pos);
        onSpellCast(tome, hero);
    }

    @Override
    public String desc(){
        String desc = Messages.get(this, "desc",hero.pointsInTalent(Talent.ASCENSION_CURSE)*3+2);
        return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }


}
