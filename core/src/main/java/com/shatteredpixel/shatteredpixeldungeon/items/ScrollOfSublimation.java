package com.shatteredpixel.shatteredpixeldungeon.items;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDivineInspiration;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.TalentCatalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class ScrollOfSublimation extends Item{
    {
        image = ItemSpriteSheet.SCROLL_SUBLITION;
        icon = ItemSpriteSheet.Icons.SCROLL_SUB;
        stackable = false;
        unique = true;
        bones = false;

    }
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_READ );
        return actions;
    }
    public static final String AC_READ	= "READ";
    protected static final float TIME_TO_READ	= 1f;
    public void execute(Hero hero, String action ) {
        super.execute( hero, action );
        if (action.equals( AC_READ )) {
                doRead();
        }
    }
    public void readAnimation() {
        Invisibility.dispel();
        curUser.spend( TIME_TO_READ );
        curUser.busy();
        ((HeroSprite)curUser.sprite).read();
        Sample.INSTANCE.play( Assets.Sounds.READ );
    }
    public boolean isIdentified() {
        return true;
    }
    @Override
    public boolean isUpgradable() {
        return false;
    }
    public int value() {
        return 30 * quantity;
    }
    public String type="Goo";
    protected static boolean identifiedByUse = false;
    public String type() {
        return type;
    }

    public String targetingPrompt() {
        return Messages.get(this, "prompt",this.type);
    }

    @Override
    public String title() {
        return super.title()+"："+getBoss(this.type);
    }


    public String desc() {
        return Messages.get(this, "desc","_"+getBoss(this.type)+"_");
    }
    public String getBoss(String type){
        String name ="";
        switch(this.type){
            case "GOO" : default:
                name="粘咕";
                break;
            case "WARRIOR":
                name="战士？";
                break;
            case "TENGU" :
                name="天狗";
                break;
            case "ROGUE":
                name="盗贼？";
                break;
            case "DM300":
                name="DM-300";
                break;
            case "DWARFKING":
                name="矮人国王";
                break;
            case "YOG":
                name="Yog-Dzewa";
                break;
        }
        return name;
    }
    public static void onSublimation( Talent newTalent ){
        Invisibility.dispel();
        curUser.spend( TIME_TO_READ );
        curUser.busy();
        ((HeroSprite)curUser.sprite).read();
        Sample.INSTANCE.play( Assets.Sounds.READ);
        StatusPane.talentBlink = 10f;
        new Flare(6, 32).color(0xFFAA00, true).show(hero.sprite, 4f);
        if (hero.hasTalent(newTalent)) {
            Talent.onTalentUpgraded(hero, newTalent);
        }
        Talent.onScrollUsed(hero,hero.pos,1f, ScrollOfSublimation.class);
    }

    public ScrollOfSublimation type(String value) {
        type = value;
        return this;
    }

    public void doRead() {
        if (Dungeon.hero != null && Dungeon.hero.randomMode) {
            applyRandomSublimation();
        } else {
            GameScene.show(new ScrollOfSublimation.WndSublimation(this.type));
        }
    }
    private void confirmCancelation( Window chooseWindow ) {
        GameScene.show( new WndOptions(new ItemSprite(this),
                Messages.titleCase(Messages.get(this, "name")),
                Messages.get(this, "warning"),
                Messages.get(InventoryScroll.class, "yes"),
                Messages.get(InventoryScroll.class, "no") ) {
            @Override
            protected void onSelect( int index ) {
                switch (index) {
                    case 0:
                        curUser.spendAndNext( TIME_TO_READ );
                        identifiedByUse = false;
                        quantity++;
                        chooseWindow.hide();
                        break;
                    case 1:
                        //do nothing
                        break;
                }
            }
            public void onBackPressed() {}
        } );
    }


    public static class WndSublimation extends Window {

        public static ScrollOfSublimation.WndSublimation INSTANCE;

        public Talent replacing;
        public String type;
        public int index;
        LinkedHashMap<Talent, Integer> replaceOptions;
        public boolean use = false;

        public WndSublimation(String type){
            super();

            identifiedByUse = false;

            INSTANCE = this;

            this.replacing = replacing;
            this.type = type;
            int tier=1;
            this.index=1;



            LinkedHashMap<Talent, Integer> options = new LinkedHashMap<>();
            //Set<Talent> curTalentsAtTier = Dungeon.hero.talents.get(tier-1).keySet();

            List<Talent> availableTalents = new ArrayList<>();
            switch(this.type){
                case "GOO" : default:
                    tier=1;
                    this.index=1;
                    Collections.addAll(availableTalents, Talent.AQUATIC_RECOVER, Talent.PUMP_ATTACK,Talent.OOZE_ATTACK);
                    break;
                case "WARRIOR":
                    tier=1;
                    this.index=1;
                    Collections.addAll(availableTalents, Talent.STRONGEST_SHIELD, Talent.COMBO_PACKAGE,Talent.BREAK_ENEMY_RANKS);
                    break;
                case "TENGU" :
                    tier=1;
                    this.index=2;
                    Collections.addAll(availableTalents, Talent.SURPRISE_THROW, Talent.SMOKE_MASK,Talent.RUSH);
                    break;
                case "ROGUE" :
                    tier=1;
                    this.index=2;
                    Collections.addAll(availableTalents, Talent.SHADOW_KILLER, Talent.KILL_SPREE,Talent.SEAOFPEOPLE,Talent.PHANTOM_STEP);
                    break;
                case "DM300":
                    tier=2;
                    this.index=2;
                    Collections.addAll(availableTalents, Talent.FASTING,Talent.THUNDER_STRIKE,Talent.DIRECTIONAL_COLLAPSE);
                    break;
                case "DWARFKING":
                    tier=2;
                    this.index=3;
                    Collections.addAll(availableTalents, Talent.KING_PROTECT, Talent.SUMMON_FOLLOWER,Talent.WOLFISH_GAZE,Talent.ENERGY_CONVERSION);
                    break;
                case "YOG":
                    tier=3;
                    this.index=3;
                    Collections.addAll(availableTalents, Talent.YOG_LARVA,Talent.YOG_FIST,Talent.YOG_RAY);
                    break;
            }
            /*
            ArrayList<String> S= new ArrayList<String>();
            S.add("DM300");
            S.add("YOG");
            if(S.contains(this.type)){
                Buff.affect(curUser,Sublimation1.class).setBoosted(index);
                //Buff.affect(curUser, Haste.class,666);
            }else{
                Buff.affect(curUser,Sublimation.class).setBoosted(index);
            }

             */

            /*
            List<Talent> selectedTalents = new ArrayList<>();
            while (selectedTalents.size() < 3 && !availableTalents.isEmpty()) {
                Talent randomTalent = Random.element(availableTalents);
                selectedTalents.add(randomTalent);
                availableTalents.remove(randomTalent);
            }

             */
            List<Talent> selectedTalents = availableTalents;
            for (Talent talent : selectedTalents) {
                options.put(talent, 0);
            }
            replaceOptions = options;
            setup(tier, options);
        }

        private void setup(int tier, LinkedHashMap<Talent, Integer> replaceOptions){
            float top = 0;

            IconTitle title = new IconTitle( curItem );
            title.color( TITLE_COLOR );
            title.setRect(0, 0, 120, 0);
            add(title);

            top = title.bottom() + 2;

            RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(ScrollOfSublimation.class, "replace_desc"), 6);
            text.maxWidth(120);
            text.setPos(0, top);
            add(text);

            top = text.bottom() + 2;

            TalentsPane.TalentTierPane optionsPane = new TalentsPane.TalentTierPane(replaceOptions, tier, TalentButton.Mode.SUBLIMATION);

            add(optionsPane);
            optionsPane.title.text(" ");
            optionsPane.setPos(0, top);
            optionsPane.setSize(120, optionsPane.height());
            resize((int)optionsPane.width(), (int)optionsPane.bottom());

            resize(120, (int)optionsPane.bottom());
        }

        @Override
        public void hide() {
            super.hide();
            if(this.use){
                curItem.detach(curUser.belongings.backpack);
                Catalog.countUse(curItem.getClass());
            }


            if (INSTANCE == this) {
                INSTANCE = null;
            }
        }

        @Override
        public void onBackPressed() {
            //curItem.quantity++;
            super.onBackPressed();
            /*
            if (curItem instanceof ScrollOfSublimation) {
                ((ScrollOfSublimation) curItem).confirmCancelation(this);

            } else {
                super.onBackPressed();
            }

             */
        }
    }

    private void applyRandomSublimation() {
        String type = this.type;
        int[] tierAndIndex = tierAndIndex(type);
        Talent talent = Random.element(sublimationTalentPool(type));
        applySublimationTalent(talent, type, tierAndIndex[0], tierAndIndex[1]);
        detach(curUser.belongings.backpack);
        Catalog.countUse(getClass());
        onSublimation(talent);
    }

    private static List<Talent> sublimationTalentPool(String type) {
        List<Talent> availableTalents = new ArrayList<>();
        switch(type){
            case "GOO" : default:
                Collections.addAll(availableTalents, Talent.AQUATIC_RECOVER, Talent.PUMP_ATTACK,Talent.OOZE_ATTACK);
                break;
            case "WARRIOR":
                Collections.addAll(availableTalents, Talent.STRONGEST_SHIELD, Talent.COMBO_PACKAGE,Talent.BREAK_ENEMY_RANKS);
                break;
            case "TENGU" :
                Collections.addAll(availableTalents, Talent.SURPRISE_THROW, Talent.SMOKE_MASK,Talent.RUSH);
                break;
            case "ROGUE" :
                Collections.addAll(availableTalents, Talent.SHADOW_KILLER, Talent.KILL_SPREE,Talent.SEAOFPEOPLE,Talent.PHANTOM_STEP);
                break;
            case "DM300":
                Collections.addAll(availableTalents, Talent.FASTING,Talent.THUNDER_STRIKE,Talent.DIRECTIONAL_COLLAPSE);
                break;
            case "DWARFKING":
                Collections.addAll(availableTalents, Talent.KING_PROTECT, Talent.SUMMON_FOLLOWER,Talent.WOLFISH_GAZE,Talent.ENERGY_CONVERSION);
                break;
            case "YOG":
                Collections.addAll(availableTalents, Talent.YOG_LARVA,Talent.YOG_FIST,Talent.YOG_RAY);
                break;
        }
        return availableTalents;
    }

    private static int[] tierAndIndex(String type) {
        switch(type){
            case "DM300":
                return new int[]{2, 2};
            case "DWARFKING":
                return new int[]{2, 3};
            case "YOG":
                return new int[]{3, 3};
            case "TENGU":
            case "ROGUE":
                return new int[]{1, 2};
            case "GOO":
            case "WARRIOR":
            default:
                return new int[]{1, 1};
        }
    }

    public static void applySublimationTalent(Talent talent, String type, int tier, int index) {
        Talent targetSlot = Talent.bossTalentSlot(type);
        Talent currentSlotTalent = Talent.bossTalentForSlot(targetSlot, Dungeon.hero.sublimationTalents);
        int cnt = 1;
        for (LinkedHashMap<Talent, Integer> tiers : Dungeon.hero.talents){
            if(cnt == tier){
                LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
                boolean replacedSlot = false;
                boolean hasCorrespondingTalent = currentSlotTalent != targetSlot;
                for (Talent t : tiers.keySet()){
                    if (t == targetSlot || t == currentSlotTalent || Talent.bossTalentSlot(t) == targetSlot){
                        newTier.put(talent, 0);
                        replacedSlot = true;
                        hasCorrespondingTalent = true;
                    } else {
                        newTier.put(t,  tiers.get(t));
                    }
                }
                if (!replacedSlot && !hasCorrespondingTalent){
                    newTier.put(talent, 0);
                }
                TalentCatalog.countUse(talent);
                Dungeon.hero.talents.set(tier-1, newTier);
                Dungeon.hero.sublimationTalents.remove(currentSlotTalent);
                Dungeon.hero.sublimationTalents.put(targetSlot, talent.name());
                ArrayList<String> S= new ArrayList<String>();
                S.add("DM300");
                S.add("YOG");
                if(S.contains(type)){
                    Buff.affect(hero, ScrollOfSublimation.Sublimation1.class).setBoosted(index);
                }else{
                    Buff.affect(hero, ScrollOfSublimation.Sublimation.class).setBoosted(index);
                }
                WndHero.lastIdx = 1;
                break;
            }else{cnt++;}
        }
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put( "type", type );

    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {

        type=bundle.getString( "type");
        super.restoreFromBundle(bundle);
    }
    public static class Sublimation extends Buff {

        {
            type = buffType.POSITIVE;
            revivePersists = true;
        }

        private boolean[] boostedTiers = new boolean[5];

        private static final String BOOSTED_TIERS = "boosted_tiers";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(BOOSTED_TIERS, boostedTiers);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            boostedTiers = bundle.getBooleanArray(BOOSTED_TIERS);
        }

        public void setBoosted( int tier ){
            boostedTiers[tier] = true;
        }

        public boolean isBoosted( int tier ){
            return boostedTiers[tier];
        }

    }
    public static class Sublimation1 extends Buff {

        {
            type = buffType.POSITIVE;
            revivePersists = true;
        }

        private boolean[] boostedTiers = new boolean[5];

        private static final String BOOSTED_TIERS = "boosted_tiers";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(BOOSTED_TIERS, boostedTiers);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            boostedTiers = bundle.getBooleanArray(BOOSTED_TIERS);
        }

        public void setBoosted( int tier ){
            boostedTiers[tier] = true;
        }

        public boolean isBoosted( int tier ){
            return boostedTiers[tier];
        }

    }

}
