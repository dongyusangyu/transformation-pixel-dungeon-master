package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpearShield extends Trinket{
    public static final String AC_SWITCH_MODE	= "SWITCH_MODE";

    {
        image = ItemSpriteSheet.SPEAR_SHIELD;
        //defaultAction = AC_SWITCH_MODE;
    }

    private int mode = 0;

    protected Class<? extends Plant> plantClass;

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_SWITCH_MODE );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute (hero, action );

        if (action.equals( AC_SWITCH_MODE )) {
            switchMode();
        }
    }

    @Override
    protected int upgradeEnergyCost() {
        //6 -> 10(16) -> 15(31) -> 20(51)
        return 20+5*level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){

            if (mode == 1)
                return Messages.get(this, "spear_stats_desc",
                        Messages.decimalFormat("#.##", (hero.buff(SpearShieldCooldown.class)==null)?(buffedLvl() * 20)+40:0),
                        Messages.decimalFormat("#.##", 80-(buffedLvl() * 20)));
            else
                return Messages.get(this, "shield_stats_desc",
                        Messages.decimalFormat("#.##", (hero.buff(SpearShieldCooldown.class)==null)?(buffedLvl() * 20)+40:0),
                        Messages.decimalFormat("#.##", 80-(buffedLvl() * 20)));

        } else {
            return Messages.get(this, "typical_stats_desc",
                    Messages.decimalFormat("#.##", 40),
                    Messages.decimalFormat("#.##", 80));
        }
    }

    @Override
    public boolean collect( Bag container ) {
        if (mode == 0) mode = Random.Int(1,3);
        super.collect(container);
        return true;
    }

    public void switchMode(){
        if (hero.buff(SpearShieldCooldown.class)!=null) {
            GLog.w(Messages.get(this, "switch_fail"));
            return;
        }
        Buff.affect(Dungeon.hero, SpearShieldCooldown.class);
        hero.buff(SpearShieldCooldown.class).reset();

        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.UNLOCK);

        if (mode == 1){
            mode = 2;
            GLog.w(Messages.get(this, "switch_shield"));
        }else{
            mode = 1;
            GLog.w(Messages.get(this, "switch_spear"));
        }
    }

    public int changeDmgMin(int min, int max){
        if (mode == 1){
            min = (int) Math.ceil(max * (0.4f + 0.2f*buffedLvl()));
            if (hero.buff(SpearShieldCooldown.class)!=null){
                min = 0;
            }
        }
        return min;
    }

    public int changeDmgMax(int min, int max){
        if (mode == 2){
            max = (int) Math.ceil(max * (0.8f - 0.2f*buffedLvl()));
            max = Math.max(max, min);
        }
        return max;
    }

    public int changeDrMin(int min, int max){
        if (mode == 2){
            min = (int) Math.ceil(max * (0.4f + 0.2f*buffedLvl()));
            if (hero.buff(SpearShieldCooldown.class)!=null){
                min = 0;
            }
        }
        return min;
    }

    public int changeDrMax(int min, int max){
        if (mode == 1){
            max = (int) Math.ceil(max * (0.8f - 0.2f*buffedLvl()));
            max = Math.max(max, min);
        }
        return max;
    }


    public static final String MODE = "mode";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MODE, mode);

    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        mode = bundle.getInt(MODE);
    }

    public static class SpearShieldCooldown extends Buff {
        public int icon() { return BuffIndicator.TIME; }
        private float levelRecovery;

        public void tintIcon(Image icon) { icon.hardlight(0.8f, 0.533f, 0.0f); }
        public float iconFadePercent() { return Math.max(0, 1-levelRecovery); }

        @Override
        public String desc() {
            return Messages.get(this, "desc", levelRecovery);
        }

        public void reset(){
            levelRecovery = 1;
        }
        public void recover(float percent){
            if (levelRecovery > 0){
                levelRecovery -= percent;
                if (levelRecovery <= 0) {
                    this.detach();
                }
            }
        }

        private static final String RECOVERY    = "recovery";

        @Override
        public void storeInBundle( Bundle bundle ) {
            bundle.put( RECOVERY, levelRecovery );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            levelRecovery = bundle.getFloat( RECOVERY );
        }
    };
}
