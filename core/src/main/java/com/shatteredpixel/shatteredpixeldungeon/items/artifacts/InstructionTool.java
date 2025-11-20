package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.dm400.HoneyComb;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja.Decoy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AttackDrone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AuxiliaryDrone;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;

import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DronesSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndNinjaAbilities;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;

import jdk.internal.classfile.Instruction;

public class InstructionTool extends Artifact {
    {
        image = ItemSpriteSheet.ARTIFACT_TOOL;
        defaultAction = AC_MAKE;
        levelCap = 10;
        charge = Math.min(level()+3, 10);
        partialCharge = 0;
        chargeCap = Math.min(level()+3, 10);
        unique = true;
        bones = false;

    }
    private final ArrayList<Class> scrolls = new ArrayList<>();

    private Class scroll;
    public static final String AC_MAKE = "make";
    public static final String AC_ENTER = "enter";

    public Drone curdrone = null;

    public static final String AC_GUIDE = "guide";
    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if ((isEquipped( hero ) || hero.hasTalent(Talent.QUICK_TOOL)) && !cursed && hero.buff(MagicImmune.class) == null && hero.buff(MagicImmune.class) == null) {
            actions.add(AC_MAKE);
            actions.add(AC_ENTER);
            //actions.add(AC_GUIDE);
        }
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;
        if (!isEquipped(hero) && !hero.hasTalent(Talent.QUICK_TOOL)){
            GLog.i( Messages.get(Artifact.class, "need_to_equip") );
            return;
        }else if (cursed) {
            GLog.i( Messages.get(this, "cursed") );
            return;
        }

        if (action.equals(AC_MAKE)) {
            ArrayList<Drone> drone = getDroneAlly();
            if((drone != null && drone.size()+1>charge) || charge<1){
                GLog.w( Messages.get(InstructionTool.class, "more_drone") );
                return;
            }else{
                int chargeuse=1;
                if(drone!=null){
                    chargeuse+=drone.size();
                }
                if(scrolls.isEmpty()){
                    createDrone(chargeuse,null);
                }else{
                    ArrayList<String> Dronestext = new ArrayList<String>();
                    for(Class s:scrolls){
                        Dronestext.add(Messages.titleCase(Messages.get(types.get(s), "name")));
                    }
                    GameScene.show(new WndOptions(Messages.get(InstructionTool.class, "create"),
                            Messages.get(InstructionTool.class, "create2"),
                            Dronestext){
                        @Override
                        protected void onSelect(int index) {
                            ArrayList<Drone> drone = getDroneAlly();
                            if(drone!=null){
                                createDrone(drone.size()+1,scrolls.get(index));
                            }else{
                                createDrone(1,scrolls.get(index));
                            }

                        }
                    });
                }
                //createDrone(chargeuse);
                Talent.onArtifactUsed(hero);
            }
        }else if(action.equals(AC_ENTER)){
            GameScene.selectItem(itemSelector);
        }
    }








    public CellSelector.Listener selectDrone = new CellSelector.Listener(){

        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;
            Drone d = getDrone(cell);
            if(d!=null){
                curdrone = d;

            }else{
                GLog.w( Messages.get(InstructionTool.class, "no_drone") );
                return;
            }

        }

        @Override
        public String prompt() {
            return  "\"" + Messages.get(InstructionTool.class, "selectdrone") + "\"";
        }
    };

    public CellSelector.Listener droneDirector = new CellSelector.Listener(){
        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;
            ArrayList<Drone> drone = getDroneAlly();
            if(drone !=null){
                for(Drone d:drone){
                    d.directTocell(cell);
                }
            }
            Sample.INSTANCE.play( Assets.Sounds.INTERCOM,1f );
            return;
        }
        @Override
        public String prompt() {
            return  Messages.get(InstructionTool.class, "direct_prompt");
        }
    };

    public CellSelector.Listener dronerecharge = new CellSelector.Listener(){
        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;
            Char drone = Actor.findChar(cell);
            if(drone!=null && drone instanceof InstructionTool.Drone){
                if(drone.HP>drone.HT*4/5){
                    InstructionTool.this.charging(1);
                    updateQuickslot();
                }
                Sample.INSTANCE.play( Assets.Sounds.RECYCLE,1f );
                hero.sprite.emitter().burst(EnergyParticle.FACTORY, 10);
                drone.destroy();
                drone.sprite.die();
            }
            return;
        }
        @Override
        public String prompt() {
            return  Messages.get(InstructionTool.class, "direct_prompt3");
        }
    };

    public Mob createDrone(int chargeUse,Class s){
        ArrayList<Integer> respawnPoints = new ArrayList<>();
        Mob mob1 = null;
        for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
            int p = hero.pos + PathFinder.NEIGHBOURS9[i];
            if (Actor.findChar( p ) == null && !Dungeon.level.solid[p]) {
                respawnPoints.add( p );
            }
        }
        if(respawnPoints.isEmpty()){
            GLog.w(Messages.get(InstructionTool.class,"less"));
            return mob1;
        }
        charge-=chargeUse;
        //GLog.w(Messages.get(InstructionTool.class,"usecharge",chargeUse-1,chargeUse));
        int spawned = 0;
        int maxSpawned=1;
        while (spawned < maxSpawned && respawnPoints.size() > 0) {
            int index = Random.index( respawnPoints );
            Mob mob = null;
            if(s==null){
                mob = new Drone();
            }else{
                if(types.containsKey(s)){
                    mob = Reflection.newInstance(types.get(s));
                }else{
                    mob = new Drone();
                }
            }
            GameScene.add(mob);
            mob1 =mob;
            Drone.appear( mob, respawnPoints.get( index ) );
            respawnPoints.remove( index );
            spawned++;
        }



        Invisibility.dispel();
        hero.spendAndNext(Actor.TICK);
        return mob1;
    }



    public void createDrone(int chargeUse,int pos){

        Mob mob = null;
        if(scroll==null){

        }else{
            if(types.containsKey(scroll)){
                mob = Reflection.newInstance(types.get(scroll));
            }else{
                mob = new Drone();
            }
        }
        GameScene.add(mob);
        Drone.appear( mob, pos);
    }

    public static ArrayList<Drone> getDroneAlly(){
        ArrayList<Drone> drone = new ArrayList<Drone>();
        for (Char ch : Actor.chars()){
            if(ch instanceof Drone){
                drone.add((Drone) ch);
            }
        }
        if(drone.isEmpty()){
            return null;
        }else{
            return drone;
        }
    }

    public static Drone getDrone(int pos){
        Char c=Actor.findChar(pos);
        if(c!=null && c instanceof Drone){
            return (Drone)c;
        }else{
            return null;
        }
    }

    private static final String SCROLLS =   "scrolls";

    private static final String SCROLL =   "scroll";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put( SCROLLS, scrolls.toArray(new Class[scrolls.size()]) );
        bundle.put( SCROLL, scroll );

    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        scrolls.clear();
        if (bundle.contains(SCROLLS) && bundle.getClassArray(SCROLLS) != null) {
            for (Class<?> scroll : bundle.getClassArray(SCROLLS)) {
                if (scroll != null) scrolls.add(scroll);
            }
        }
        scroll = bundle.getClass(SCROLL);

    }

    public void charging(float amount){
        partialCharge += amount;
        if(partialCharge>1){
            charge+=Math.min((int)partialCharge,chargeCap-charge);
            if(charge>=charge){
                partialCharge=0;
            }else{
                partialCharge-=(int)partialCharge;
            }
        }
    }



    @Override
    protected ArtifactBuff passiveBuff() {
        return new toolRecharge();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (cursed || target.buff(MagicImmune.class) != null) return;

        if (charge < chargeCap){
            if (!isEquipped(target)) amount *= 0.75f*target.pointsInTalent(Talent.QUICK_TOOL)/3f;
            partialCharge += 0.2f*amount;
            if (partialCharge >= 1) {
                charge += (int)partialCharge;
                partialCharge -=(int)partialCharge;;
            }
            updateQuickslot();
        }
    }
    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if (super.doUnequip(hero, collect, single)){
            if (!collect || !hero.hasTalent(Talent.QUICK_TOOL)){
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
                    && ((Hero) container.owner).hasTalent(Talent.QUICK_TOOL)){
                activate((Hero) container.owner);
            }
            return true;
        } else{
            return false;
        }
    }



    public class toolRecharge extends ArtifactBuff implements ActionIndicator.Action {
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
            if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null) {
                if (activeBuff == null && Regeneration.regenOn()) {

                    float turnsToCharge = 80-4*level();
                    turnsToCharge /= RingOfEnergy.artifactChargeMultiplier(target);
                    float chargeToGain = (1f / turnsToCharge);

                    if (!isEquipped(hero)){
                        chargeToGain *= 0.75f* hero.pointsInTalent(Talent.QUICK_TOOL)/3f;
                    }
                    int cnt=hero.pointsInTalent(Talent.ENERGY_CONVERSION);
                    if(hero.speed()>1 && cnt>=1){
                        chargeToGain *=Math.max(1,(hero.speed())*0.5*cnt);
                    }
                    partialCharge += chargeToGain;
                }

                while (partialCharge >= 1) {
                    charge++;
                    partialCharge -= 1;
                    if (charge == chargeCap){
                        partialCharge = 0;
                    }

                }
            } else {
                partialCharge = 0;
            }

            if (cooldown > 0)
                cooldown --;
            ActionIndicator.setAction(this);
            updateQuickslot();

            spend( TICK );

            return true;
        }
        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            ActionIndicator.setAction(this);
        }

        @Override
        public int actionIcon() {
            return HeroIcon.DRONEDIRECT;
        }
        @Override
        public int indicatorColor() {
            return 0x000000;
        }
        @Override
        public String actionName() {
            return Messages.get(InstructionTool.class, "action");
        }

        @Override
        public void doAction() {
            ArrayList<Drone> drone = getDroneAlly();
            if(drone != null){
                //GameScene.selectCell(droneDirector);
                GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.ARTIFACT_TOOL),
                        Messages.get(InstructionTool.class, "name"),
                        Messages.get(InstructionTool.class, "prompt2"),
                        Messages.get(InstructionTool.class, "huishou"),
                        Messages.get(InstructionTool.class, "guide")){
                    @Override
                    protected void onSelect(int index) {
                        if (index == 0){
                            GameScene.selectCell(dronerecharge);
                        } else if(index == 1){
                            GameScene.selectCell(droneDirector);
                        }
                    }
                });
            }else{
                GLog.w( Messages.get(InstructionTool.class, "no_drone") );
            }
        }
    }


    public static class InstructionMark extends Buff {
        private float left;
        public int icon() {
            if(hero.subClass == HeroSubClass.AT400){
                return BuffIndicator.AT400MARK;
            }else if(hero.subClass == HeroSubClass.AU400){
                return BuffIndicator.AU400MARK;
            }else{
                return BuffIndicator.DM400MARK;
            }

        }
        public void reset(){
            left = 3f;
        }
        public void reset(int turns){
            if(hero!=null && hero.hasTalent(Talent.SUSTAIN_MARK)){
                turns+=2*hero.pointsInTalent(Talent.SUSTAIN_MARK);
            }
            left = turns + 1; //add 1 as we're spending it on our action
        }

        @Override
        public boolean act(){
            left--;
            if(left<=0){
                detach();
            }
            spend( TICK );

            return true;
        }
        @Override
        public String desc() {
            if(hero.subClass == HeroSubClass.AT400){
                return Messages.get(this, "desc_at", left);
            }else if(hero.subClass == HeroSubClass.AU400){
                return Messages.get(this, "desc_au", left);
            }else{
                return Messages.get(this, "desc", left);
            }

        }

        private static final String LEFT = "left";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( LEFT, left);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            left = bundle.getFloat(LEFT);
        }
    }

    private static final String CUR_SEED_EFFECT = "cur_seed_effect";

    public static class Drone extends DirectableAlly {

        {
            spriteClass = DronesSprite.DroneSprite.class;

            HP = HT = 3;

            immunities.add(AllyBuff.class);
            defenseSkill=5;
            flying = true;

            properties.add(Property.INORGANIC);
        }

        private float partHP;

        public void onZapComplete() {
            zap();
            next();
        }

        @Override
        public int attackSkill(Char target) {
            if(hero!=null){
                return hero.attackSkill(this);
            }else{
                return defenseSkill+5; //equal to base hero attack skill
            }

        }




        @Override
        public int defenseSkill(Char target) {
            if(hero!=null){
                return hero.defenseSkill(this);
            }else{
                return defenseSkill; //equal to base hero attack skill
            }

        }

        protected void zap() {
            spend( 1f );

            Invisibility.dispel(this);
            Char enemy = this.enemy;
            if (hit( this, enemy, true )) {

                attack( enemy );

            } else {
                enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
            }
        }

        protected boolean doAttack( Char enemy ) {

            if (Dungeon.level.adjacent( pos, enemy.pos )
                    || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos != enemy.pos) {

                return super.doAttack( enemy );

            } else {

                if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                    sprite.zap( enemy.pos );
                    return false;
                } else {
                    zap();
                    return true;
                }
            }
        }

        public Drone(){
            super();
            if(hero!=null){
                int hpBonus = (int)(hero.HT*0.33f);
                if (hpBonus > 0){
                    HT += hpBonus;
                    HP += hpBonus;
                }
                defenseSkill = hero.lvl+5;
            }

        }

        @Override
        protected boolean act() {
            if(HP<HT){
                float turn =15f;
                InstructionTool tool = Dungeon.hero.belongings.getItem(InstructionTool.class);
                if(tool==null){
                    turn =15f;
                }else{
                    turn -=tool.level();
                }
                partHP+=1/turn;
                if(partHP>1){
                    partHP-=1;
                    HP+=1;
                    sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(1), FloatingText.HEALING);
                }
            }
            int oldPos = pos;
            boolean result = super.act();
            //partially simulates how the hero switches to idle animation
            if ((pos == target || oldPos == pos) && sprite.looping()){
                sprite.idle();
            }
            return result;
        }

        public static int getTool(){
            InstructionTool tool = Dungeon.hero.belongings.getItem(InstructionTool.class);
            if(tool==null){
                return 0;
            }else{
                return tool.level()+1;
            }
        }




        @Override
        public void defendPos(int cell) {
            yell(Messages.get(this, "direct_defend"));
            super.defendPos(cell);
        }

        @Override
        public void followHero() {
            yell(Messages.get(this, "direct_follow"));
            super.followHero();
        }

        @Override
        public void targetChar(Char ch) {
            yell(Messages.get(this, "direct_attack"));
            super.targetChar(ch);
        }

        @Override
        public void wander(Char ch) {
            yell(Messages.get(this, "direct_wander"));
            super.wander(ch);
        }


        @Override
        public int damageRoll() {
            int lvl = getTool();
            int damage = Random.NormalIntRange(1+lvl, lvl*2+1);
            return damage;
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            damage = super.attackProc( enemy, damage );
            if(hero!=null && hero.hasTalent(Talent.BATTLE_UPGRADE)){
                damage *= 1.075f+0.075f*hero.pointsInTalent(Talent.BATTLE_UPGRADE);
                damage += (int)(enemy.drRoll()*0.8f/3*hero.pointsInTalent(Talent.BATTLE_UPGRADE));
            }
            return damage;
        }
        @Override
        public int drRoll() {
            int dr = super.drRoll() + Random.NormalIntRange(1, 2);
            if(hero!=null && hero.hasTalent(Talent.BODY_REINFORCE)){
                dr+= (int)(hero.drRoll()*(0.25f+0.25f*hero.pointsInTalent(Talent.BODY_REINFORCE)));
            }
            return dr;
        }

        @Override
        public boolean isImmune(Class effect) {
            return super.isImmune(effect);
        }

        @Override
        public int defenseProc(Char enemy, int damage) {
            damage = super.defenseProc(enemy, damage);
            if(hero.pointsInTalent(Talent.SHINKAGE)>1){
                Buff.affect(hero, Adrenaline.class,0.5f+0.5f*(int)(hero.pointsInTalent(Talent.SHINKAGE)/2));
            }
            return damage;
        }

        @Override
        public void damage(int dmg, Object src) {

            //TODO improve this when I have proper damage source logic


            super.damage(dmg, src);
        }
        @Override
        public void die( Object cause ) {
            if(hero!=null && hero.buff(HoneyComb.HoneyCombBuff.class)!=null){
                int heal=Math.min((int)(hero.HP*(0.04f+0.015f*hero.pointsInTalent(Talent.GLORIOUS_DEAD))),hero.HT-hero.HP);
                hero.HP+=heal;
                hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
                if(hero.hasTalent(Talent.PROCESS_EXTEND)){
                    Buff.affect(hero,HoneyComb.HoneyCombBuff.class,0.5f*hero.pointsInTalent(Talent.PROCESS_EXTEND));
                }
                Buff.affect(hero, ArtifactRecharge.class).extend(2+hero.pointsInTalent(Talent.UPON_WAVE));

            }
            //Dungeon.level.updateFieldOfView( this, fieldOfView );
            //GameScene.updateFog(pos, 1+(int)Math.ceil(speed()));
            Sample.INSTANCE.play( Assets.Sounds.DRONEDIED,1f );
            super.die(cause);
        }

        @Override
        public float speed() {
            float speed = super.speed();
            if(hero!=null){
                speed*=hero.speed();
            }
            return speed;
        }

        @Override
        public float attackDelay() {
            float delay = super.attackDelay();
            if(hero!=null){
                delay*=hero.attackDelay();
            }
            return delay;
        }

        @Override
        public boolean canInteract(Char c) {
            return true;
        }

        @Override
        public boolean interact(Char c) {
            if(c instanceof Hero && distance(c)>1){
                if(hero.hasTalent(Talent.ALLY_WARP) && Dungeon.level.distance(pos, c.pos) <= 2* hero.pointsInTalent(Talent.ALLY_WARP)){
                    Game.runOnRenderThread(new Callback() {
                        @Override
                        public void call() {
                            GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.ARTIFACT_TOOL),
                                    Messages.get(InstructionTool.class, "name"),
                                    Messages.get(InstructionTool.class, "prompt3"),
                                    Messages.get(InstructionTool.class, "huanwei"),
                                    Messages.get(InstructionTool.class, "guide2")){
                                @Override
                                protected void onSelect(int index) {
                                    if (index == 0){
                                        //((Mob)(Drone.this)).interact(hero);
                                        Drone.super.interact(hero);
                                        //GLog.i("2");
                                    } else if(index == 1){
                                        Game.runOnRenderThread(new Callback() {
                                            @Override
                                            public void call() {
                                                GameScene.selectCell(droneDirector2);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }else{
                    Game.runOnRenderThread(new Callback() {
                        @Override
                        public void call() {
                            GameScene.selectCell(droneDirector2);
                        }
                    });
                }
                return true;
            }else{
                return super.interact(c);
            }
        }
        public CellSelector.Listener droneDirector2 = new CellSelector.Listener(){
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Drone.this.directTocell(cell);
                if(Drone.this instanceof AuxiliaryDrone.ChaosDrone){
                    Sample.INSTANCE.play( Assets.Sounds.CONTROLM,1f);
                }else if(Drone.this instanceof AuxiliaryDrone){
                    Sample.INSTANCE.play( Assets.Sounds.CONTROLU ,1f);
                }else if(Drone.this instanceof AttackDrone){
                    Sample.INSTANCE.play( Assets.Sounds.CONTROLA ,1f);
                }else{
                    Sample.INSTANCE.play( Assets.Sounds.CONTROL ,1f);
                }
            }
            @Override
            public String prompt() {
                return  Messages.get(InstructionTool.class, "direct_prompt");
            }
        };

        public static void appear( Char ch, int pos ) {

            ch.sprite.interruptMotion();

            if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[ch.pos]){
                Sample.INSTANCE.play( Assets.Sounds.MANUFACTURING ,1f);
            }

            ch.move( pos );
            if (ch.pos == pos) ch.sprite.place( pos );

            if (Dungeon.level.heroFOV[pos] || ch == hero ) {
                ch.sprite.emitter().burst(EnergyParticle.FACTORY, 10);
            }
        }

        private static final String DEF_SKILL = "def_skill";
        private static final String PARTHP = "parthp";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(DEF_SKILL, defenseSkill);
            bundle.put(PARTHP, partHP);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            defenseSkill = bundle.getInt(DEF_SKILL);
            partHP = bundle.getFloat(PARTHP);
        }
    }



    protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(UnstableSpellbook.class, "prompt");
        }

        @Override
        public Class<?extends Bag> preferredBag(){
            return ScrollHolder.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            if(curUser.subClass==HeroSubClass.AT400){
                return ((item instanceof ScrollOfUpgrade) || (item instanceof ScrollOfRemoveCurse)
                        || (item instanceof ScrollOfRecharging) || (item instanceof ScrollOfLullaby)
                        || (item instanceof ScrollOfRage) ||  (item instanceof ScrollOfTerror)
                        ||  (item instanceof ScrollOfTransmutation)) && item.isIdentified();
            }else if(curUser.subClass==HeroSubClass.AU400){
                return ((item instanceof ScrollOfUpgrade) || (item instanceof ScrollOfIdentify)
                        || (item instanceof ScrollOfMirrorImage) || (item instanceof ScrollOfMagicMapping)
                        || (item instanceof ScrollOfRetribution) ||  (item instanceof ScrollOfTeleportation)
                        ||  (item instanceof ScrollOfTransmutation)) && item.isIdentified();
            }else{
                return item instanceof ScrollOfUpgrade && item.isIdentified();
            }

        }

        @Override
        public void onSelect(Item item) {
            if (item != null && item instanceof Scroll && item.isIdentified()){
                Hero hero = Dungeon.hero;
                if(item instanceof ScrollOfUpgrade){
                    if(InstructionTool.this.trueLevel()>=10){
                        GLog.w( Messages.get(InstructionTool.class, "unable_scroll") );
                        return;
                    }
                    hero.sprite.operate( hero.pos );
                    hero.busy();
                    hero.spend( 1f );
                    Sample.INSTANCE.play(Assets.Sounds.ENTRY,1f);
                    hero.sprite.emitter().burst( ElmoParticle.FACTORY, 12 );
                    upgrade();
                    upgrade();
                    Catalog.countUses(InstructionTool.class, 2);
                    Talent.onScrollUsed(hero,hero.pos,2,item.getClass());
                    item.detach(hero.belongings.backpack);
                    if(hero.hasTalent(Talent.RECOVER_CHARGE)){
                        charging(hero.pointsInTalent(Talent.RECOVER_CHARGE));
                    }
                }else{
                    scroll=item.getClass();
                    ArrayList<Drone> drones = getDroneAlly();
                    if(drones!=null){
                        for(Drone drone:drones){
                            if(drone.getClass()==Drone.class){
                                int pos=drone.pos;
                                Actor.remove( drone );
                                drone.sprite.killAndErase();
                                Dungeon.level.mobs.remove(drone);

                                createDrone(0,pos);
                            }
                        }
                    }
                    hero.sprite.operate( hero.pos );
                    hero.busy();
                    hero.spend( 1f );
                    Sample.INSTANCE.play(Assets.Sounds.ENTRY,1f);
                    hero.sprite.emitter().burst( ElmoParticle.FACTORY, 12 );
                    Talent.onScrollUsed(hero,hero.pos,1,item.getClass());
                    if(!scrolls.contains(item.getClass())){
                        scrolls.add(item.getClass());
                    }
                    item.detach(hero.belongings.backpack);
                    if(hero.hasTalent(Talent.RECOVER_CHARGE)){
                        charging(hero.pointsInTalent(Talent.RECOVER_CHARGE));
                    }
                    String desc ="";
                    desc += Messages.get(InstructionTool.class, "desc_enter",Messages.titleCase(Messages.get(scroll, "name")),Messages.titleCase(Messages.get(types.get(scroll), "name")));
                    GLog.i(desc);

                }
            }else if (item instanceof Scroll && !item.isIdentified()) {
                GLog.w( Messages.get(InstructionTool.class, "unknown_scroll") );
            }
        }
    };


    @Override
    public String desc() {
        String desc = super.desc();

        if (hero!=null && (isEquipped( Dungeon.hero ) || hero.hasTalent(Talent.QUICK_TOOL))){
            desc += "\n\n";
            if (cursed){
                desc += Messages.get(this, "desc_cursed");
            }else{
                desc += Messages.get(this, "desc_equipped");
                if(scrolls.isEmpty()){
                    desc += Messages.get(this, "desc_noenter");
                }else{
                    String scrolltext="";
                    String dronestext="";
                    for(Class s:scrolls){
                        if(s==scrolls.get(scrolls.size()-1)){
                            scrolltext+="_"+Messages.titleCase(Messages.get(s, "name"))+"_";
                            dronestext+="_"+Messages.titleCase(Messages.get(types.get(s), "name"))+"_";
                        }else{
                            scrolltext+="_"+Messages.titleCase(Messages.get(s, "name"))+"_，";
                            dronestext+="_"+Messages.titleCase(Messages.get(types.get(s), "name"))+"_，";
                        }
                    }
                    //desc += Messages.get(this, "desc_enter","_"+Messages.titleCase(Messages.get(scroll, "name"))+"_","_"+Messages.titleCase(Messages.get(types.get(scroll), "name"))+"_");
                    desc += Messages.get(this, "desc_enter",scrolltext,dronestext);
                }

                ArrayList<Drone> drone = getDroneAlly();
                desc += "\n\n";
                if(drone==null){
                    desc += Messages.get(this, "usecharge2");
                }else{
                    desc += Messages.get(this, "usecharge",drone.size(),drone.size()+1);
                }
            }

        }
        return desc;
    }

    @Override
    public Item upgrade() {
        chargeCap = Math.min(3+level(),10);
        return super.upgrade();
    }

    public static HashMap<Class<?extends Scroll>, Class<?extends Drone>> types = new HashMap<>();
    static {
        types.put(ScrollOfRemoveCurse.class,     AttackDrone.FlashDrone.class);
        types.put(ScrollOfRecharging.class,     AttackDrone.LaserDrone.class);
        types.put(ScrollOfLullaby.class,     AttackDrone.AnesthesiaDrone.class);
        types.put(ScrollOfRage.class,      AttackDrone.RaidDrone.class);
        types.put(ScrollOfTerror.class,     AttackDrone.ShockDrone.class);
        types.put(ScrollOfIdentify.class,        AuxiliaryDrone.ScoutDrone.class);
        types.put(ScrollOfMirrorImage.class,      AuxiliaryDrone.MirrorDrone.class);
        types.put(ScrollOfTeleportation.class,    AuxiliaryDrone.ProtectDrone.class);
        types.put(ScrollOfMagicMapping.class,    AuxiliaryDrone.EscortDrone.class);
        types.put(ScrollOfRetribution.class,     AuxiliaryDrone.BombDrone.class);
        types.put(ScrollOfTransmutation.class,      AuxiliaryDrone.ChaosDrone.class);
    }



}
