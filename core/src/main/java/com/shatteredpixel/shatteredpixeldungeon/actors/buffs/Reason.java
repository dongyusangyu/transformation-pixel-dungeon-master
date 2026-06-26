package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RitualDagger;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMonkAbilities;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.io.IOException;

public class Reason extends Buff implements ActionIndicator.Action {
    {
        type = buffType.POSITIVE;
        revivePersists = true;
        actPriority = HERO_PRIO  +1;
    }
    public int reason=100;
    public boolean kaoyan=false;
    @Override
    public int icon() {
        return BuffIndicator.REASON;
    }

    @Override
    public void tintIcon(Image icon) {
        if(reason < 21){
            icon.hardlight(1f, 0f, 0);
        }else if (reason < 41){
            icon.hardlight(1f, 0.67f, 0);
        } else if (reason < 61) {
            icon.hardlight(1, 1f, 0);
        } else if (reason < 101){
            icon.hardlight(0f, 1f, 0);
        }else{
            icon.hardlight(0f, 0f, 1f);
        }
    }
    @Override
    public String actionName() {
        return Messages.get(this, "action");
    }

    @Override
    public int actionIcon() {
        RitualDagger dagger = hero == null ? null : hero.belongings.getItem(RitualDagger.class);
        if (dagger != null && dagger.ritualReady() && hero.subClass.is(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.PIOUS)){
            return HeroIcon.RITUAL;
        }
        return HeroIcon.REASON;
    }

    @Override
    public Visual secondaryVisual() {
        BitmapText txt = new BitmapText(PixelScene.pixelFont);
        txt.text( Integer.toString((int)reason) );
        txt.hardlight(CharSprite.POSITIVE);
        txt.measure();
        return txt;
    }
    @Override
    public Visual primaryVisual() {
        Image actionIco = new HeroIcon(this);
        if(actionIcon()!=HeroIcon.RITUAL) tintIcon(actionIco);
        return actionIco;
    }
    @Override
    public void doAction() {
        RitualDagger dagger = hero == null ? null : hero.belongings.getItem(RitualDagger.class);
        if (dagger != null && dagger.ritualReady() && hero.subClass.is(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.PIOUS)){
            GameScene.show(new WndOptions(
                    new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite(dagger),
                    Messages.get(RitualDagger.class, "name"),
                    Messages.get(RitualDagger.class, "wnd_desc"),
                    Messages.get(RitualDagger.class, "stab_enemy_opt"),
                    Messages.get(RitualDagger.class, "stab_self_opt")){
                @Override
                protected void onSelect(int index) {
                    if (index == 0){
                        dagger.execute(hero, RitualDagger.AC_STAB);
                    } else if (index == 1){
                        dagger.stabSelf(hero);
                    }
                }
            });
        }
    }
    @Override
    public String desc() {
        String desc = "";
        if(reason < 21){
            desc+=Messages.get(this, "desc_zero");
        }else if (reason < 41){
            desc+=Messages.get(this, "desc_quarter");
        } else if (reason < 61) {
            desc+=Messages.get(this, "desc_half");
        } else if (reason < 101){
            desc+=Messages.get(this, "desc_full");
        }else{
            desc+="_你这理智不对啊_\n\n";
        }
        desc+=Messages.get(this, "desc_simple", reason);
        return desc;
    }

    public static void gainReason(Char c,int gain) {
        Reason r=null;
        if(c!=null && c.buff(Reason.class)!=null){
            r=c.buff(Reason.class);
        }else{
            return;
        }
        if(r!=null){
            int trueGain = Math.min(gain,100-r.reason);
            if(trueGain>0){
                r.reason += trueGain;
                if(ShatteredPixelDungeon.scene() instanceof GameScene && c.sprite!=null){
                    c.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(trueGain), FloatingText.UP_WHITE);
                }

                ActionIndicator.refresh();
            }

        }
    }

    public static void loseReason(Char c,int lose) {
        Reason r=null;
        if(c!=null && c.buff(Reason.class)!=null){
            r=c.buff(Reason.class);
        }else{
            return;
        }
        if(r!=null){
            if (c == hero){
                lose = com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.beforeSufferingReasonLoss(hero, lose);
            }
            int trueLose = Math.min(lose,r.reason);
            if(trueLose>0){
                int oldreason=r.reason;
                r.reason -= trueLose;
                if(ShatteredPixelDungeon.scene() instanceof GameScene){
                    r.target.sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(trueLose), FloatingText.DOWN_WHITE);
                }
                ActionIndicator.refresh();
                if(r.reason < 21 && oldreason>=21){
                    GLog.n(Messages.get(Reason.class, "zero"));
                }else if (r.reason < 41 && oldreason>=41){
                    GLog.w(Messages.get(Reason.class, "quarter"));
                } else if (r.reason < 61 && oldreason>=61) {
                    GLog.i(Messages.get(Reason.class, "half"));
                }
            }

        }
    }
    public static void panicReason(Char c,int lose) {
        Reason r=null;
        if(c!=null && c.buff(Reason.class)!=null){
            r=c.buff(Reason.class);
        }else{
            return;
        }
        if(r!=null){
            if (c == hero){
                lose = com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.beforeSufferingReasonLoss(hero, lose);
            }
            int trueLose = Math.min(lose,r.reason);
            if(trueLose>0){
                int oldreason=r.reason;
                r.reason -= trueLose;
                if(r.target.sprite!=null) r.target.sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(trueLose), FloatingText.TERROR);

                ActionIndicator.refresh();
                if(r.reason<=0 && c.buff(Suffering.class)==null && c.buff(Virtue.class)==null ){
                    GLog.n(Messages.get(Reason.class, "kaoyan"));
                }else if(r.reason < 21 && oldreason>=21){
                    GLog.n(Messages.get(Reason.class, "zero"));
                }else if (r.reason < 41 && oldreason>=41){
                    GLog.w(Messages.get(Reason.class, "quarter"));
                } else if (r.reason < 61 && oldreason>=61) {
                    GLog.i(Messages.get(Reason.class, "half"));
                }
            }
            //BuffIndicator.refreshHero();
        }
    }

    public static void sufferingReason(Char c,int lose) {
        Reason r=null;
        if(c!=null && c.buff(Reason.class)!=null){
            r=c.buff(Reason.class);
        }else{
            return;
        }
        if(r!=null){
            if (c == hero){
                lose = com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.beforeSufferingReasonLoss(hero, lose);
            }
            int trueLose = Math.min(lose,r.reason);
            if(trueLose>0){
                int oldreason=r.reason;
                r.reason -= trueLose;
                if(r.target.sprite!=null) r.target.sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(trueLose), FloatingText.DOWN_RED);

                ActionIndicator.refresh();
                if(r.reason < 21 && oldreason>=21){
                    GLog.n(Messages.get(Reason.class, "zero"));
                }else if (r.reason < 41 && oldreason>=41){
                    GLog.w(Messages.get(Reason.class, "quarter"));
                } else if (r.reason < 61 && oldreason>=61) {
                    GLog.i(Messages.get(Reason.class, "half"));
                }
            }
            //BuffIndicator.refreshHero();
        }
    }

    public static void VirtueReason(Char c,int gain) {
        Reason r=null;
        if(c!=null && c.buff(Reason.class)!=null){
            r=c.buff(Reason.class);
        }else{
            return;
        }
        if(r!=null){
            int trueGain = Math.min(gain,100-r.reason);
            if(trueGain>0){
                r.reason += trueGain;
                if(ShatteredPixelDungeon.scene() instanceof GameScene && c.sprite!=null){
                    c.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(trueGain), FloatingText.UP_GLOD);
                }

                ActionIndicator.refresh();
            }
        }
    }

    @Override
    public boolean attachTo(Char target) {
        if (super.attachTo(target)) {
            ActionIndicator.setAction(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean act() {
        //GLog.i(kaoyan+"");
        if(reason<=0 && target.buff(TimekeepersHourglass.timeStasis.class) == null && (target.buff(Suffering.Fear.class)!=null || target.buff(Suffering.Despair.class)!=null  || target.buff(Suffering.Paranoia.class)!=null || target.buff(Suffering.Ecstasy.class)!=null )){
            target.HP=1;
            target.damage(999999, this);
            if (!target.isAlive()) {
                Dungeon.fail( this );
                GLog.n( Messages.get(this, "die") );
                return true;
            }
        }
        if(reason<=0 && target.buff(TimekeepersHourglass.timeStasis.class) == null && (target.buff(Virtue.Fearless.class)!=null || target.buff(Virtue.Firm.class)!=null  || target.buff(Virtue.Inspire.class)!=null )){
            Reason.VirtueReason(hero,100);
            if(target.buff(Virtue.Fearless.class)!=null) target.buff(Virtue.Fearless.class).detach();
            else if(target.buff(Virtue.Firm.class)!=null) target.buff(Virtue.Firm.class).detach();
            else if(target.buff(Virtue.Inspire.class)!=null) target.buff(Virtue.Inspire.class).detach();
            //ActionIndicator.refresh();
        }
        if(kaoyan){
            kaoyan=false;
            int virtueChance = 25;
            if (target instanceof Hero && target.buff(Reason.class)!=null && ((Hero) target).hasTalent(Talent.HUMAN_GLORY)){
                virtueChance += 5 * ((Hero) target).pointsInTalent(Talent.HUMAN_GLORY);
            }
            if(Random.Int(100) < virtueChance){
                Virtue.rollGiveVirtue(target,150);
            }else{
                Suffering.rollGiveSuffer(target,300);
            }
        }
        if(reason<=0 && target.buff(Suffering.class)==null && target.buff(Virtue.class)==null && !kaoyan){
            kaoyan=true;
            GLog.n(Messages.get(Reason.class, "kaoyan"));
        }

        spend( TICK );
        return true;
    }


    public static String REASON = "reason";
    public static String KAOYAN = "kaoyan";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(REASON, reason);
        bundle.put(KAOYAN, kaoyan);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        reason = bundle.getInt(REASON);
        kaoyan = bundle.getBoolean(KAOYAN);
        ActionIndicator.setAction(this);
        if (reason>100){
            reason=100;
        }else if(reason<0){
            reason=0;
        }
    }

    @Override
    public int indicatorColor() {
        return 0x444444;
    }


}
