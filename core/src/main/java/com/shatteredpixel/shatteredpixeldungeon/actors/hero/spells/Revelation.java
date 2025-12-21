package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

public class Revelation  extends ClericSpell {

    public static Revelation INSTANCE = new Revelation();

    @Override
    public int icon() {
        return HeroIcon.REVELATION;
    }

    @Override
    public String desc() {
        int turns = 1 + 2*hero.pointsInTalent(Talent.REVELATION);

        return Messages.get(this, "desc", turns) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }

    @Override
    public float chargeUse(Hero hero) {
        return 1f;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.REVELATION);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        if(hero.buff(RevelationBuff.class) == null){
            Buff.affect(hero, RevelationBuff.class, 2*hero.pointsInTalent(Talent.REVELATION));
        }else{
            Buff.affect(hero, RevelationBuff.class, 1 + 2*hero.pointsInTalent(Talent.REVELATION));
        }


        Sample.INSTANCE.play(Assets.Sounds.READ);

        hero.busy();
        hero.sprite.operate(hero.pos);
        new Flare( 6, 32 ).color(0xFFFF00, true).show( hero.sprite, 2f );
        onSpellCast(tome, hero);

    }
    public static class RevelationBuff extends FlavourBuff {

        public static float DURATION = 5f;

        private Emitter particles;

        {
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.REVELATION;
        }


        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

    }
}
