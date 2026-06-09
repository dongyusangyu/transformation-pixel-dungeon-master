package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndNinjaAbilities;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class FightStance extends Buff implements ActionIndicator.Action {
    {
        revivePersists = true;
    }
    public int stance = 0;
    public final int balance = 0;
    public final int invasion = 1;
    public final int parry = 2;
    public float duration = 0;
    @Override
    public int icon() {
        if(stance == balance) return BuffIndicator.BALANCE;
        if(stance == invasion) return BuffIndicator.INVASION;
        if(stance == parry) return BuffIndicator.PARRY;
        return BuffIndicator.BALANCE;
    }
    @Override
    public boolean act() {

        duration += TICK;
        spend(TICK);
        if(target.buff(Coordination.class)!=null){
            ActionIndicator.clearAction(this);
        }else{
            ActionIndicator.setAction(this);
        }
        if(stance==parry){
            int times=30-10*hero.pointsInTalent(Talent.STANCE_MASTERY)/3;
            if(Math.floorMod((int)duration,times)==0){
                Buff.affect(target, FightStance.Focus.class);
            }
        }
        return true;
    }
    @Override
    public String desc() {
        String desc = "";
        switch (stance){
            case balance:
                desc += "\n\n"+Messages.get(this, "balance_desc");
                if(hero.pointsInTalent(Talent.STANCE_MASTERY)>2){
                    desc += "\n\n"+Messages.get(this, "balance_desc3");
                } else if(hero.pointsInTalent(Talent.STANCE_MASTERY)>0){
                    desc += "\n\n"+Messages.get(this, "balance_desc2");
                } else {
                    desc += "\n\n"+Messages.get(this, "balance_desc1");
                }
                break;
            case invasion:
                desc += "\n\n"+Messages.get(this, "invasion_desc");
                if(hero.pointsInTalent(Talent.STANCE_MASTERY)>2){
                    desc += "\n\n"+Messages.get(this, "invasion_desc3");
                } else if(hero.pointsInTalent(Talent.STANCE_MASTERY)>1){
                    desc += "\n\n"+Messages.get(this, "invasion_desc2");
                } else {
                    desc += "\n\n"+Messages.get(this, "invasion_desc1");
                }
                break;
            case parry:
                desc += "\n\n"+Messages.get(this, "parry_desc");
                if(hero.pointsInTalent(Talent.STANCE_MASTERY)>2){
                    desc += "\n\n"+Messages.get(this, "parry_desc3");
                } else if(hero.pointsInTalent(Talent.STANCE_MASTERY)>1){
                    desc += "\n\n"+Messages.get(this, "parry_desc2");
                } else {
                    desc += "\n\n"+Messages.get(this, "parry_desc1");
                }
                break;
        }
        desc += "\n\n"+Messages.get(this, "duration", (int)duration);
        return desc;
    }
    @Override
    public String actionName() {
        return Messages.get(this, "action");
    }
    @Override
    public String name() {
        switch (stance){
            case balance:default:
                return Messages.get(this, "balance_name");
            case invasion:
                return Messages.get(this, "invasion_name");
            case parry:
                return Messages.get(this, "parry_name");
        }
    }
    public static String STANCE = "stance";
    public static String DURATION = "duration";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STANCE, stance);
        bundle.put(DURATION, duration);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stance = bundle.getInt(STANCE);
        duration = bundle.getFloat(DURATION);
        ActionIndicator.setAction(this);
    }

    @Override
    public int actionIcon() {
        if(stance == balance) return HeroIcon.BALANCE;
        if(stance == invasion) return HeroIcon.INVASION;
        if(stance == parry) return HeroIcon.PARRY;
        return HeroIcon.BALANCE;
    }
    @Override
    public Visual secondaryVisual() {
        BitmapText txt = new BitmapText(PixelScene.pixelFont);
        txt.text( Integer.toString((int)duration) );
        txt.hardlight(CharSprite.POSITIVE);
        txt.measure();
        return txt;
    }
    @Override
    public int indicatorColor() {
        switch (stance){
            case balance:default:
                return 0x52009A;
            case invasion:
                return 0xFF0000;
            case parry:
                return 0x2391FF;
        }
    }
    @Override
    public void doAction() {
        if(target.buff(Coordination.class)==null) GameScene.show(new WndFightStance(this));

    }
    public static class  WndFightStance extends WndOptions {

        private static final int WIDTH_P = 120;
        private static final int WIDTH_L = 180;

        private static final int MARGIN  = 2;
        private FightStance fightStance;

        public WndFightStance( FightStance fightStance ){

            super(new BuffIcon( fightStance, true ),
                    Messages.titleCase(fightStance.name()),
                    Messages.get(fightStance, "action"),
                    Messages.get(fightStance, "balance_name"),
                    Messages.get(fightStance, "invasion_name"),
                    Messages.get(fightStance, "parry_name"));
            this.fightStance = fightStance;

        }
        @Override
        protected void onSelect(int index) {
            fightStance.duration = 0;
            if(fightStance.stance==fightStance.parry){
                FightStance.Focus focus = fightStance.target.buff(FightStance.Focus.class);
                if(focus!=null){
                    focus.detach();
                }
            }
            if(fightStance.target.buff(Stabilize.class)!=null){
                Buff.affect(fightStance.target, Coordination.class,Coordination.DURATION);
                if(!hero.hasTalent(Talent.FLUENT)){
                    Buff.affect(fightStance.target, Daze.class,9f);
                }
                fightStance.target.buff(Stabilize.class).detach();
            }else{
                Buff.affect(fightStance.target, Stabilize.class,Stabilize.DURATION);
            }

            if(hero.pointsInTalent(Talent.FLUENT)>2){
                float dur=0;
                if(fightStance.stance==fightStance.invasion){

                    if(index==fightStance.balance){
                        Buff r=fightStance.target.buff(Recharging.class);
                        if(r!=null){
                            float cooldown=r.cooldown()+1f;
                            dur= Math.max(4-cooldown,0);
                        }else{
                            dur = 3;
                        }
                        Buff.affect(fightStance.target, Recharging.class,dur);
                    }
                }else if(fightStance.stance==fightStance.parry){
                    Buff r=fightStance.target.buff(ArtifactRecharge.class);
                    if(r!=null){
                        float cooldown=r.cooldown()+1f;
                        dur= Math.max(4-cooldown,0);
                    }else{
                        dur = 3;
                    }
                    if(index==fightStance.balance){
                        Buff.affect(fightStance.target, ArtifactRecharge.class).extend(dur);
                    }
                }
            }
            if(hero.pointsInTalent(Talent.FLUENT)>1){
                if(fightStance.stance==fightStance.balance){
                    if(index==fightStance.invasion){
                        float dur=0;
                        Buff r=fightStance.target.buff(Haste.class);
                        if(r!=null){
                            float cooldown=r.cooldown()+1f;
                            dur= Math.max(4-cooldown,0);
                        }else{
                            dur = 4;
                        }
                        Buff.affect(fightStance.target, Haste.class,dur);
                    }else if(index==fightStance.parry){
                        int shield = fightStance.target.shielding();
                        Buff.affect(fightStance.target, Barrier.class).incShield(Math.max(20-shield,0));
                    }
                }
            }

            fightStance.stance = index;

            if(fightStance.target.buff(Coordination.class)==null) ActionIndicator.refresh();
            else ActionIndicator.clearAction();

        }

        @Override
        protected boolean enabled(int index) {
            if(hero.buff(FightStance.class).stance==index){
                return false;
            }else{
                return true;
            }
        }

    }

    public static class Stabilize extends FlavourBuff {
        public static final float DURATION = 5f;
        @Override
        public int icon() {
            return BuffIndicator.STANCESTABILIZE;
        }

    }
    public static class Coordination extends FlavourBuff {
        public static final float DURATION = 9f;
        @Override
        public int icon() {
            return BuffIndicator.STANCECOORDINATION;
        }
        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            ActionIndicator.clearAction();
        }
    }

    public static class Focus extends MonkEnergy.MonkAbility.Focus.FocusBuff{

    }
}
