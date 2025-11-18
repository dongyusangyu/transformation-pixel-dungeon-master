package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Shuriken_Box extends Artifact {
    public static final String AC_SHOOT       = "SHOOT";

    {
        image = ItemSpriteSheet.ARTIFACT_SHURIKEN;

        levelCap = 10;

        charge = 10;
        cooldown = 0;
        usesTargeting = true;
        //chargeCap = 5+level()*2;

        defaultAction = AC_SHOOT; //so it can be quickslotted
        bones = false;
        unique = true;
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions( hero );
        if ((isEquipped( hero ) || hero.hasTalent(Talent.LIGHT_BOX)) && charge > 0 && !cursed && hero.buff(MagicImmune.class) == null) {
            actions.add(AC_SHOOT);
        }
        return actions;
    }
    @Override
    public int value() {
        return 0;
    }
    @Override
    public void resetForTrinity(int visibleLevel) {
        super.resetForTrinity(visibleLevel);
        charge = 5+(level()*2); //sets charge to soft cap
    }


    public void directCharge( float amount){
        int chargeTarget = 25+(level()*2);
        if (charge < chargeTarget) {
            ;
            partialCharge += amount;
            while (partialCharge >= 1f) {
                charge++;
                partialCharge--;
            }
            if (charge >= chargeTarget){
                partialCharge = 0;
                charge = chargeTarget;
            }
            updateQuickslot();
        }
        updateQuickslot();
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if (super.doUnequip(hero, collect, single)){
            if (!collect || !hero.hasTalent(Talent.LIGHT_BOX)){
                if (activeBuff != null){
                    activeBuff.detach();
                    activeBuff = null;
                }
            } else {
                activate(hero);
            }

            return true;
        } else
            return false;
    }
    @Override
    public boolean collect( Bag container ) {
        if (super.collect(container)){
            if (container.owner instanceof Hero
                    && passiveBuff == null
                    && ((Hero) container.owner).hasTalent(Talent.LIGHT_BOX)){
                activate((Hero) container.owner);
            }
            return true;
        } else{
            return false;
        }
    }
    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_SHOOT)){

            curUser = hero;

            if ((!isEquipped(hero) && !hero.hasTalent(Talent.LIGHT_BOX))) {
                GLog.i( Messages.get(Artifact.class, "need_to_equip") );
                usesTargeting = false;

            } else if (charge < 1) {
                GLog.i( Messages.get(this, "no_charge") );
                usesTargeting = false;

            } else if (cursed) {
                GLog.w( Messages.get(this, "cursed") );
                usesTargeting = false;

            } else {
                curUser = hero;
                curItem = this;
                usesTargeting = true;
                GameScene.selectCell( shooter );
            }

        }
    }
    @Override
    public int targetingPos(Hero user, int dst) {
        return knockArrow().targetingPos(user, dst);
    }

    private int targetPos;

    public CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null) {
                knockArrow().cast(curUser, target);
            }
        }
        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };



    public SmallShuriken knockArrow(){
        return new SmallShuriken();
    }

    public class SmallShuriken extends Shuriken {

        {
            image = ItemSpriteSheet.SMALLSKURIKEN;
            setID = 0;
            tier = 1;
        }

        public boolean isIdentified() {
            return true;
        }

        @Override
        public int defaultQuantity() {
            return 1;
        }

        @Override
        public Emitter emitter() {
            return super.emitter();
        }

        @Override
        public int damageRoll(Char owner) {
            return super.damageRoll(owner);
        }

        @Override
        public int level() {
            return Shuriken_Box.this.level()/2;
        }
        @Override
        public int proc(Char attacker, Char defender, int damage) {
            if (defender instanceof Mob
                    && defender.buff(Talent.SuckerPunchTracker.class) == null
            && Random.Int(10)<Shuriken_Box.this.level()+1){
                Buff.affect(defender, Paralysis.class,2);
                Buff.affect(defender, Talent.SuckerPunchTracker.class);
            }
            Talent.onArtifactUsed(curUser);
            return super.proc(attacker, defender, damage);
        }


        @Override
        public int STRReq(int lvl) {
            return curUser.STR;
        }

        @Override
        protected void onThrow( int cell ) {
            Char enemy = Actor.findChar( cell );
            if (enemy == null || enemy == curUser) {
                parent = null;
                Splash.at( cell, 0xA9A6ABFF, 1 );
                if(hero.buff(Ninja_Energy.Throw_Skill.class)!=null && Dungeon.level.water[hero.pos]){
                    if(hero.buff(Ninja_Energy.Gas_Storage.class)!=null){
                        Ninja_Energy.Gas_Storage gas_storage=hero.buff(Ninja_Energy.Gas_Storage.class);
                        for(Blob blob:gas_storage.blobs.values()){
                            GameScene.add(Blob.seed(cell,10,blob.getClass()));
                        }
                        gas_storage.detach();
                    }
                    Ninja_Energy.Throw_Skill b = hero.buff(Ninja_Energy.Throw_Skill.class);
                    b.detach();
                    Ninja_Energy.NinjaAbility.Throw_Water(hero.pos,cell);
                }
            } else {
                if (!curUser.shoot( enemy, this )) {
                    Splash.at(cell, 0xA9A6ABFF, 1);
                }
            }
        }

        @Override
        public void throwSound() {
            Sample.INSTANCE.play( Assets.Sounds.HIT_STAB, 1, Random.Float(0.87f, 1.15f) );
        }

        int flurryCount = -1;
        Actor flurryActor = null;

        @Override
        public void cast(final Hero user, final int dst) {
            final int cell = throwPos( user, dst );
            Shuriken_Box.this.targetPos = cell;
            Shuriken_Box.this.charge--;
            super.cast(user, dst);
        }
    }
    private static final String BUFF = "buff";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        if (activeBuff != null) bundle.put(BUFF, activeBuff);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(BUFF)){
            activeBuff = new shurikenRecharge();
            activeBuff.restoreFromBundle(bundle.getBundle(BUFF));
        }
        if(charge<0){
            charge=0;
        }
        if(partialCharge<0){
            partialCharge=0;
        }
        if(charge>100){
            charge=25;
        }
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new shurikenRecharge();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (cursed || target.buff(MagicImmune.class) != null) return;
        int chargeTarget = 5+(level()*2);
        if (charge < chargeTarget*2){
            if (!isEquipped(target)) amount *= 0.75f*target.pointsInTalent(Talent.LIGHT_BOX)/3f;
            partialCharge += 0.5f*amount;
            if (partialCharge >= 1) {
                charge += (int)partialCharge;
                partialCharge -=(int)partialCharge;;
            }
            updateQuickslot();
        }
    }

    @Override
    public String desc() {
        String desc = super.desc();

        if (isEquipped( Dungeon.hero )){
            desc += "\n\n";
            if (cursed)
                desc += Messages.get(this, "desc_cursed");
            else
                desc += Messages.get(this, "desc_equipped",level()/2+2,level()+4,Messages.decimalFormat("##.#",Math.min(100,(level()+1)*10)));
        }
        return desc;
    }

    public class shurikenRecharge extends ArtifactBuff{

        @Override
        public boolean act() {
            int chargeTarget = 5+(level()*2);
            if (charge < chargeTarget
                    && !cursed
                    && target.buff(MagicImmune.class) == null
                    && Regeneration.regenOn()) {

                //gains a charge in 40 - 2*missingCharge turns

                float chargeGain = (1 / Math.max((40f - 2*(chargeTarget - charge)),10));
                if (!isEquipped(hero)){
                    chargeGain *= 0.75f* hero.pointsInTalent(Talent.LIGHT_BOX)/3f;
                }
                chargeGain *= (float)(RingOfEnergy.artifactChargeMultiplier(target)*Math.max(1,(hero.speed())*0.5*hero.pointsInTalent(Talent.ENERGY_CONVERSION)));
                partialCharge += chargeGain;
                if (partialCharge >= 1) {
                    charge += (int)partialCharge;
                    partialCharge -=(int)partialCharge;;
                }
            }



            updateQuickslot();

            spend( TICK );

            return true;
        }

        public void gainExp( float levelPortion ) {
            if (cursed || target.buff(MagicImmune.class) != null || levelPortion == 0) return;

            exp += Math.round(levelPortion*100);

            //past the soft charge cap, gaining  charge from leveling is slowed.
            if (charge > 5+(level()*2)){
                levelPortion *= (5+((float)level()*2))/charge;
            }
            if (!isEquipped(hero)){
                levelPortion *= 0.75f* hero.pointsInTalent(Talent.LIGHT_BOX)/3f;
            }
            partialCharge += levelPortion*6f;

            if (exp > 100+level()*30 && level() < levelCap){
                exp -= 100+level()*30;
                GLog.p( Messages.get(this, "levelup") );
                Catalog.countUses(Shuriken_Box.class, 2);
                upgrade();
            }

        }
    }
}
