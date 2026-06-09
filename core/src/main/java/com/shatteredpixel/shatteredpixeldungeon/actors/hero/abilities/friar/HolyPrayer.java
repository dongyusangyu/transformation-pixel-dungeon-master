package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.friar;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Virtue;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class HolyPrayer extends ArmorAbility {

    public static final float DURATION = 30f;

    {
        baseChargeUse = 50f;
    }

    @Override
    public int icon() {
        return HeroIcon.HOLY_PRAYER;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.PIOUS_FAITH, Talent.SUPREME_BLESSING, Talent.HATRED_OF_EVIL, Talent.HEROIC_ENERGY};
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        float chargeUse = chargeUse(hero);
        armor.charge -= chargeUse;
        Talent.onArmorAbility(hero, chargeUse);
        armor.updateQuickslot();

        int virtueExtension = hero.hasTalent(Talent.SUPREME_BLESSING) ? 50 + 25 * hero.pointsInTalent(Talent.SUPREME_BLESSING) : 50;
        int allyHealPercent = hero.hasTalent(Talent.SUPREME_BLESSING) ? 20 + 20 * hero.pointsInTalent(Talent.SUPREME_BLESSING) : 20;

        applyBlessing(hero, hero, virtueExtension, allyHealPercent);
        for (Char ch : Actor.chars()) {
            if (ch != hero && ch.alignment == Char.Alignment.ALLY) {
                applyBlessing(ch, hero, virtueExtension, allyHealPercent);
            }
        }

        if (hero.hasTalent(Talent.PIOUS_FAITH)) {
            restoreReason(hero, 25 * hero.pointsInTalent(Talent.PIOUS_FAITH));
        }

        GLog.p(Messages.get(this, "cast"));
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.READ);
    }

    private void applyBlessing(Char ch, Hero hero, int virtueExtension, int allyHealPercent) {
        HolyPrayerBlessing blessing = Buff.affect(ch, HolyPrayerBlessing.class, DURATION-1);
        blessing.set(ch == hero, hero.hasTalent(Talent.HATRED_OF_EVIL) ? hero.pointsInTalent(Talent.HATRED_OF_EVIL) : 0);

        if (ch == hero) {
            extendVirtue(hero, virtueExtension);
        } else {
            int heal = Math.round(ch.HT * allyHealPercent / 100f);
            if (heal > 0) {
                ch.heal(heal);
            }
        }
    }

    private void extendVirtue(Hero hero, int duration) {
        if (hero.buff(Virtue.Firm.class) != null) {
            Buff.extend(hero, Virtue.Firm.class, duration);
        } else if (hero.buff(Virtue.Fearless.class) != null) {
            Buff.extend(hero, Virtue.Fearless.class, duration);
        } else if (hero.buff(Virtue.Inspire.class) != null) {
            Buff.extend(hero, Virtue.Inspire.class, duration);
        }
    }

    private void restoreReason(Hero hero, int amount) {
        Reason reason = hero.buff(Reason.class);
        if (reason == null || amount <= 0) {
            return;
        }

        int overflow = Math.max(0, amount - (100 - reason.reason));
        Reason.gainReason(hero, amount);
        if (overflow > 0) {
            hero.heal(overflow);
        }
    }

    public static class HolyPrayerBlessing extends FlavourBuff {

        private static final String HERO_BLESSING = "hero_blessing";


        private boolean heroBlessing;


        {
            type = buffType.POSITIVE;
            announced = true;
        }

        public void set(boolean heroBlessing, int evilBonus) {
            this.heroBlessing = heroBlessing;

        }

        public float accuracyAndEvasionFactor() {
            return heroBlessing ? 1.5f : 2f;
        }



        @Override
        public int icon() {
            return BuffIndicator.HOLYPRAER;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }
        protected int rays=4;

        @Override
        public void fx(boolean on) {
            if (on) target.sprite.aura( 0xFFFF00, rays );
            else target.sprite.clearAura();
        }

        @Override
        public String desc() {
            if (hero!=null && hero.hasTalent(Talent.HATRED_OF_EVIL)) {
                return Messages.get(this, "desc_bane", 20 + 10 * hero.pointsInTalent(Talent.HATRED_OF_EVIL), dispTurns());
            }
            return Messages.get(this, "desc", dispTurns());
        }

        @Override
        public void storeInBundle(com.watabou.utils.Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(HERO_BLESSING, heroBlessing);

        }

        @Override
        public void restoreFromBundle(com.watabou.utils.Bundle bundle) {
            super.restoreFromBundle(bundle);
            heroBlessing = bundle.getBoolean(HERO_BLESSING);

        }
    }
}
