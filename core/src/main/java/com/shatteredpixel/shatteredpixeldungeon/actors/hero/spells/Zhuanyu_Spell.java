package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class Zhuanyu_Spell extends ClericSpell {
    public static Zhuanyu_Spell INSTANCE = new Zhuanyu_Spell();

    @Override
    public int icon() {
        return HeroIcon.ZHUANYU_SPELL;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 2;
    }

    public String desc(){
        return Messages.get(this, "desc",25 * hero.pointsInTalent(Talent.ZHUANYU_SPELL)+25) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.ZHUANYU_SPELL);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        int shield = hero.shielding();
        Buff.affect(hero, Healing.class).setHeal((int)(Math.min(shield/4*(hero.pointsInTalent(Talent.ZHUANYU_SPELL)+1),hero.HT/2)),0.25f,1);
        for (ShieldBuff s : hero.buffs(ShieldBuff.class)){
            s.detach();
        }
        hero.spend( 1f );
        hero.busy();
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.READ);
        onSpellCast(tome, hero);

    }
}
