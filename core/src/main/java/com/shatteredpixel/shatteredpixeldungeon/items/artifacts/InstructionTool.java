package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja.Decoy;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DronesSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

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
    public static final String AC_MAKE = "make";
    public static final String AC_ENTER = "enter";

    public Drone curdrone = null;

    public static final String AC_GUIDE = "guide";
    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if ((isEquipped( hero ) || hero.hasTalent(Talent.QUICK_TOOL)) && !cursed && hero.buff(MagicImmune.class) == null) {
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
        if (!isEquipped(hero) && !hero.hasTalent(Talent.LIGHT_CLOAK)){
            GLog.i( Messages.get(Artifact.class, "need_to_equip") );
            return;
        }else if (cursed) {
            GLog.i( Messages.get(this, "cursed") );
            return;
        }

        if (action.equals(AC_MAKE)) {
            ArrayList<Drone> drone = getDroneAlly();
            if(drone != null && drone.size()+1>charge){
                GLog.w( Messages.get(InstructionTool.class, "more_drone") );
                return;
            }else{
                int chargeuse=1;
                if(drone!=null){
                    chargeuse+=drone.size();
                }
                createDrone(chargeuse);
            }

        }else if(action.equals(AC_ENTER)){
            GameScene.selectItem(itemSelector);
        }else if(action.equals(AC_GUIDE)){
            ArrayList<Drone> drone = getDroneAlly();
            if(drone != null){
                GameScene.selectCell(selectDrone);

                /*
                if(curdrone!=null){
                    GameScene.selectCell(droneDirector);
                }

                 */
            }else{
                GLog.w( Messages.get(InstructionTool.class, "no_drone") );
            }
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
            if(curdrone==null){
                GLog.w( Messages.get(InstructionTool.class, "no_drone") );
                return;
            }
            curdrone.directTocell(cell);
            curdrone=null;
            return;
        }
        @Override
        public String prompt() {
            return  "\"" + Messages.get(InstructionTool.class, "direct_prompt") + "\"";
        }
    };

    public void createDrone(int chargeUse){
        ArrayList<Integer> respawnPoints = new ArrayList<>();
        for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
            int p = hero.pos + PathFinder.NEIGHBOURS9[i];
            if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                respawnPoints.add( p );
            }
        }
        if(respawnPoints.isEmpty()){
            GLog.w(Messages.get(InstructionTool.class,"less"));
            return;
        }
        charge-=chargeUse;
        GLog.w(Messages.get(InstructionTool.class,"usecharge",chargeUse-1,chargeUse));
        int spawned = 0;
        int maxSpawned=1;
        while (spawned < maxSpawned && respawnPoints.size() > 0) {
            int index = Random.index( respawnPoints );
            Drone mob = new Drone();
            GameScene.add(mob);
            Drone.appear( mob, respawnPoints.get( index ) );
            respawnPoints.remove( index );
            spawned++;
        }
        Invisibility.dispel();
        hero.spendAndNext(Actor.TICK);
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

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put( SCROLLS, scrolls.toArray(new Class[scrolls.size()]) );

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


    public class toolRecharge extends ArtifactBuff {

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

            updateQuickslot();

            spend( TICK );

            return true;
        }
    }

    public static class InstructionMark extends Buff {
        private float left;
        public int icon() {
            if(hero.subClass== HeroSubClass.AT400){
                return BuffIndicator.AT400MARK;
            }else if(hero.subClass== HeroSubClass.AU400){
                return BuffIndicator.AU400MARK;
            }else{
                return BuffIndicator.DM400MARK;
            }

        }
        public void reset(){
            left = 3f;
        }
        public void reset(int turns){
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
            if(hero.subClass== HeroSubClass.AT400){
                return Messages.get(this, "desc_at", left);
            }else if(hero.subClass== HeroSubClass.AU400){
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
        public int attackSkill(Char target) {
            return defenseSkill+5; //equal to base hero attack skill
        }
        public int getTool(){
            InstructionTool tool = Dungeon.hero.belongings.getItem(InstructionTool.class);
            if(tool==null){
                return 0;
            }else{
                return tool.level();
            }
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

            return damage;
        }



        @Override
        public int drRoll() {
            int dr = super.drRoll();
            if(hero!=null && hero.hasTalent(Talent.BODY_REINFORCE)){
                dr+= (int)(hero.drRoll()*(0.5f*hero.pointsInTalent(Talent.BODY_REINFORCE)));
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
        public float speed() {
            float speed = super.speed();
            if(hero!=null){
                speed*=hero.speed();
            }
            //moves 2 tiles at a time when returning to the hero
            if (state == WANDERING
                    && defendingPos == -1
                    && Dungeon.level.distance(pos, hero.pos) > 1){
                speed *= 2;
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
            if(c instanceof Hero){
                Game.runOnRenderThread(new Callback() {
                    @Override
                    public void call() {
                        GameScene.selectCell(droneDirector2);
                    }
                });
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
            }
            @Override
            public String prompt() {
                return  "\"" + Messages.get(InstructionTool.class, "direct_prompt") + "\"";
            }
        };

        public static void appear( Char ch, int pos ) {

            ch.sprite.interruptMotion();

            if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[ch.pos]){
                Sample.INSTANCE.play(Assets.Sounds.PUFF);
            }

            ch.move( pos );
            if (ch.pos == pos) ch.sprite.place( pos );

            if (Dungeon.level.heroFOV[pos] || ch == hero ) {
                ch.sprite.emitter().burst(SmokeParticle.FACTORY, 10);
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
            return item instanceof Scroll && item.isIdentified();
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
                    Sample.INSTANCE.play(Assets.Sounds.BURNING);
                    hero.sprite.emitter().burst( ElmoParticle.FACTORY, 12 );
                    upgrade();
                    upgrade();
                    item.detach(hero.belongings.backpack);
                    if(hero.hasTalent(Talent.RECOVER_CHARGE)){
                        charge+=Math.min(10-charge,hero.pointsInTalent(Talent.RECOVER_CHARGE));
                    }
                }else{
                    GLog.w( Messages.get(InstructionTool.class, "unable_scroll") );
                }
            }else if (item instanceof Scroll && !item.isIdentified()) {
                GLog.w( Messages.get(InstructionTool.class, "unknown_scroll") );
            }
        }
    };


    @Override
    public String desc() {
        String desc = super.desc();

        if (isEquipped( Dungeon.hero )){
            desc += "\n\n";
            if (cursed){
                desc += Messages.get(this, "desc_cursed");
            }else{
                desc += Messages.get(this, "desc_equipped");
                desc += Messages.get(this, "desc_noenter");
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



}
