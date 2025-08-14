package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Silver_Language extends TargetedClericSpell {
    public static final Silver_Language INSTANCE = new Silver_Language();

    @Override
    public int icon() {
        return HeroIcon.SILVER_LANGUAGE;
    }


    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.SILVER_LANGUAGE);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 2;

    }
    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null){
            return;
        }

        Char ch = Actor.findChar(target);
        if (ch == null || !Dungeon.level.heroFOV[target] || (ch instanceof  Hero)){
            GLog.w(Messages.get(this, "no_target"));
            return;
        }

        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

        Buff.affect(ch, Amok.class,1+2*hero.pointsInTalent(Talent.SILVER_LANGUAGE));


        hero.busy();
        hero.sprite.operate(ch.pos);
        hero.spend( 1f );

        onSpellCast(tome, hero);
    }
    @Override
    public String desc(){
        String desc = Messages.get(this, "desc",hero.pointsInTalent(Talent.SILVER_LANGUAGE)*2+1);
        return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }
}
