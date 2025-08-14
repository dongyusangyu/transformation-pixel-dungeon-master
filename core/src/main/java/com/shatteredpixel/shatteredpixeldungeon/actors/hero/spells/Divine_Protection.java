package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class Divine_Protection  extends ClericSpell {
    public static final Divine_Protection INSTANCE = new Divine_Protection();

    @Override
    public int icon() {
        return HeroIcon.DIVINE_PROTECTION;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 5;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.DIVINE_PROTECTION);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        Buff.affect(hero, Invulnerability.class,hero.pointsInTalent(Talent.DIVINE_PROTECTION)*2+1);
        Sample.INSTANCE.play(Assets.Sounds.READ);
        hero.spend( 1f );
        hero.busy();
        hero.sprite.operate(hero.pos);
        onSpellCast(tome, hero);
    }

    @Override
    public String desc(){
        String desc = Messages.get(this, "desc",hero.pointsInTalent(Talent.DIVINE_PROTECTION)*2+1);
        return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }


}