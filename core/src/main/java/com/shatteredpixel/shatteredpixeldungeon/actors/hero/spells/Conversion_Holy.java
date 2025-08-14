package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Conversion_Holy  extends TargetedClericSpell {
    public static final Conversion_Holy INSTANCE = new Conversion_Holy();

    @Override
    public int icon() {
        return HeroIcon.CONVERSION_HOLY;
    }


    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.CONVERSION_HOLY);
    }
    @Override
    public float chargeUse(Hero hero) {
        return 5;

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

        if (!ch.isImmune(ScrollOfSirensSong.Enthralled.class)){
            Buff.affect( ch, ScrollOfSirensSong.Enthralled.class);
            if(hero.pointsInTalent(Talent.CONVERSION_HOLY)==2){
                Buff.affect(ch, Bless.class,10);
            }
        } else {
            Buff.affect( ch, Charm.class, Charm.DURATION ).object = hero.id();

        }


        hero.busy();
        hero.sprite.operate(ch.pos);
        hero.spend( 1f );

        onSpellCast(tome, hero);
    }
    public String desc(){
        return Messages.get(this, "desc") +"\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
    }

}
