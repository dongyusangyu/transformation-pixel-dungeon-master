package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Tatteki extends Weapon {

    public static final String AC_SHOOT		= "SHOOT";
    public static final String AC_LOAD		= "LOAD";
    {
        image = ItemSpriteSheet.TATTEKI;

        defaultAction = AC_SHOOT;
        usesTargeting = true;

        unique = true;
        bones = false;

    }

    public Bomb bomb = null;
    public int bombnumber = 0;

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_SHOOT);
        if(hero.hasTalent(Talent.KUNIKUCHI)){
            actions.add(AC_LOAD);
        }
        return actions;
    }
    /*
    public String targetingPrompt(){
        if(hero!=null && hero.buff(LoadCooldown.class)==null){
            return Messages.get(MissileWeapon.class, "prompt");
        }else{
            return null;
        }

    }
    public boolean useTargeting(){
        return targetingPrompt() != null;
    }

     */
    public static class LoadCooldown extends FlavourBuff {
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(1f, 0f, 0f); }
        public float iconFadePercent() { return Math.max(0, visualcooldown() / 30); }
    };
    public static class Fix extends Buff {

        {
            type = buffType.POSITIVE;
        }
        @Override
        public int icon() {
            return BuffIndicator.FIX;
        }
        @Override
        public String iconTextDisplay() {
            return Integer.toString(left);
        }
        @Override
        public String desc() {
            return Messages.get(this, "desc");
        }
        public int left;
        public void set(int shots){
            left = Math.max(left, shots);
        }
    }

    @Override
    public void execute(Hero hero, String action) {
        if (action.equals(AC_SHOOT)) {
            curUser = hero;
            curItem = this;
            if(curUser.buff(LoadCooldown.class)==null && curItem.isEquipped(curUser)){
                usesTargeting = true;
                GameScene.selectCell( shooter );
            }else if(curUser.buff(LoadCooldown.class)!=null && curItem.isEquipped(curUser)){
                GLog.w( Messages.get(this, "unload"));
                usesTargeting = false;
                return;
            }else{
                GLog.w(Messages.get(this, "need_equip"));
                usesTargeting = false;
                return;
            }
        }else if(action.equals(AC_EQUIP)){
            equipSecondary(hero);
        }else if(action.equals(AC_LOAD)){
            GameScene.selectItem(itemSelector);
        }else{
            super.execute(hero, action);
        }
    }

    public boolean canUseBomb(Item item){
        return item instanceof Bomb;
    }

    protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(Tatteki.class, "prompt1");
        }

        @Override
        public Class<?extends Bag> preferredBag(){
            return MagicalHolster.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return canUseBomb(item);
        }

        @Override
        public void onSelect( Item item ) {
            if (item != null && item instanceof Bomb) {
                bomb = (Bomb) item;
                Hero hero = Dungeon.hero;
                bombnumber = hero.pointsInTalent(Talent.KUNIKUCHI)*2-1;
                hero.sprite.operate( hero.pos );
                Sample.INSTANCE.play( Assets.Sounds.CLICK );
                hero.busy();
                hero.spend( Actor.TICK );
                item.detach(hero.belongings.backpack);
            }
        }
    };


    @Override
    public int proc(Char attacker, Char defender, int damage) {
        return super.proc(attacker, defender, damage);
    }

    @Override
    public String info() {
        String info = super.info();

        info += "\n\n" + Messages.get( this, "stats",
                Math.round(augment.damageFactor(min())),
                Math.round(augment.damageFactor(max())),
                STRReq());

        switch (augment) {
            case SPEED:
                info += "\n\n" + Messages.get(Weapon.class, "faster");
                break;
            case DAMAGE:
                info += "\n\n" + Messages.get(Weapon.class, "stronger");
                break;
            case NONE:
        }

        if (enchantment != null && (cursedKnown || !enchantment.curse())){
            info += "\n\n" + Messages.capitalize(Messages.get(Weapon.class, "enchanted", enchantment.name()));
            if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
            info += " " + enchantment.desc();
        } else if (enchantHardened){
            info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
        }

        if (cursed && isEquipped( hero )) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
        } else if (cursedKnown && cursed) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed");
        } else if (!isIdentified() && cursedKnown){
            info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
        }

        if(bomb!=null){
            info += "\n\n" + Messages.get(this, "bomb",bomb.name(),bombnumber);
        }

        info += "\n\n" + Messages.get(MissileWeapon.class, "distance");

        return info;
    }

    @Override
    public int STRReq(int lvl) {
        if(hero!=null){
            return hero.STR;
        }else{
            return 10;
        }
    }

    @Override
    public int min(int lvl) {
        int dmg = lvl*2;
        if(hero!=null){
            dmg+=hero.lvl*2;
        }
        //return Math.max(0, dmg);
        return 0;
    }

    @Override
    public int max(int lvl) {
        int dmg = lvl*10;
        if(hero!=null){
            dmg+=hero.lvl*5;
        }
        return Math.max(0, dmg);
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return knockTamaru().targetingPos(user, dst);
    }
    private static final String BOMB = "bomb";
    private static final String NUMBER= "number";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(BOMB, bomb);
        bundle.put(NUMBER, bombnumber);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        bomb = (Bomb) bundle.get(BOMB);
        bombnumber = bundle.getInt(NUMBER);
    }

    private int targetPos;

    @Override
    public int damageRoll(Char owner) {
        int damage = augment.damageFactor(super.damageRoll(owner));

        return damage;
    }


    @Override
    public int buffedLvl() {
        if (hero != null && isEquipped(hero)){
            KindOfWeapon other = null;
            if (hero.belongings.weapon() != this) other = hero.belongings.weapon();
            if (hero.belongings.secondWep() != this) other = hero.belongings.secondWep();
            if (other instanceof MeleeWeapon) {
                int otherLevel = other.buffedLvl();
                return otherLevel;
            }
        }
        return super.buffedLvl();
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    public Tamaru knockTamaru(){
        return new Tamaru();
    }

    public class Tamaru extends MissileWeapon {

        {
            image = ItemSpriteSheet.TAMARU;

            hitSound = Assets.Sounds.HIT_ARROW;

            setID = 0;
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
            if (hero.buff(NaturesPower.naturesPowerTracker.class) != null){
                Emitter e = new Emitter();
                e.pos(5, 5);
                e.fillTarget = false;
                e.pour(LeafParticle.GENERAL, 0.01f);
                return e;
            } else {
                return super.emitter();
            }
        }
        @Override
        protected float baseDelay(Char owner) {
            /*
            if(Tatteki.this.augment==Augment.DAMAGE){
                return 1.5f;
            }else if(Tatteki.this.augment==Augment.SPEED){
                return 0.8f;
            }else{
                return 1f;
            }

             */
            return Tatteki.this.baseDelay(owner);
        }


        @Override
        public int damageRoll(Char owner) {
            return Tatteki.this.damageRoll(owner);
        }

        @Override
        public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
            return Tatteki.this.hasEnchant(type, owner);
        }

        @Override
        public int proc(Char attacker, Char defender, int damage) {
            return Tatteki.this.proc(attacker, defender, damage);
        }

        @Override
        public float delayFactor(Char user) {
            return Tatteki.this.delayFactor(user);
        }

        @Override
        public float accuracyFactor(Char owner, Char target) {
            if (target.buff(Tatteki.Fix.class)!=null){
                Tatteki.Fix f = target.buff(Tatteki.Fix.class);
                f.detach();
                return Float.POSITIVE_INFINITY;
            } else {
                return super.accuracyFactor(owner, target)*0.2f;
            }
        }

        @Override
        public int STRReq(int lvl) {
            return Tatteki.this.STRReq();
        }

        @Override
        protected void onThrow( int cell ) {
            for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]) {
                    Buff.affect( mob, Terror.class, 3 ).object = curUser.id();
                }
            }
            if(Tatteki.this.bomb!=null){
                Tatteki.this.bombnumber--;
                Bomb b = Reflection.newInstance(Tatteki.this.bomb.getClass());
                b.explode(cell);
                if(Tatteki.this.bombnumber<1){
                    Tatteki.this.bombnumber=0;
                    Tatteki.this.bomb=null;
                }
            }
            Char enemy = Actor.findChar( cell );
            if (enemy == null || enemy == curUser) {
                parent = null;
                Splash.at( cell, 0x88FFFFFF, 1 );
            } else {
                if (!curUser.shoot( enemy, this )) {
                    Splash.at(cell, 0x88FFFFFF, 1);
                }
            }
        }
        @Override
        public void throwSound() {
            Sample.INSTANCE.play( Assets.Sounds.BLAST, 1, Random.Float(0.87f, 1.15f) );
        }


        @Override
        public void cast(final Hero user, final int dst) {
            int cooldown =0;
            if(((Hero)user).hasTalent(Talent.SOKO)){
                cooldown += 1+ 3*((Hero)user).pointsInTalent(Talent.SOKO);
            }
            Buff.affect(user,Tatteki.LoadCooldown.class,30-cooldown);
            final int cell = throwPos( user, dst );
            Tatteki.this.targetPos = cell;

            super.cast(user, dst);
        }
    }

    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null && hero.buff(LoadCooldown.class)==null) {
                knockTamaru().cast(curUser, target);
            }
        }
        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };
}
