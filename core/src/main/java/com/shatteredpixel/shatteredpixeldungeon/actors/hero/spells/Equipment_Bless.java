package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

public class Equipment_Bless extends ClericSpell {

    public static Equipment_Bless INSTANCE = new Equipment_Bless();

    @Override
    public int icon() {
        return HeroIcon.EQUIPMENT_BLESS;
    }

    @Override
    public String desc() {

        return Messages.get(this, "desc",hero.pointsInTalent(Talent.EQUIPMENT_BLESS)) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
    }

    @Override
    public float chargeUse(Hero hero) {
        return 3f;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.EQUIPMENT_BLESS);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        Buff.affect(hero, Blessbuff.class, 50);


        Sample.INSTANCE.play(Assets.Sounds.READ);
        hero.spendAndNext( 1f );
        hero.busy();
        hero.sprite.operate(hero.pos);
        new Flare( 6, 32 ).color(0xFFFF00, true).show( hero.sprite, 2f );
        onSpellCast(tome, hero);
        updateQuickslot();

    }
    public static class Blessbuff extends FlavourBuff {

        public static float DURATION = 50f;

        private Emitter particles;

        {
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.EQUIPMENT_BLESS;
        }


        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

    }
}