package com.shatteredpixel.shatteredpixeldungeon.items;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class ScrollOfSublimation extends Item{
    {
        image = ItemSpriteSheet.SCROLL_SUBLITION;
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
    public int energyVal() {
        return 6 * quantity;
    }
    public String type="Goo";
    protected static boolean identifiedByUse = false;
    public String type() {
        return type;
    }

    public String targetingPrompt() {
        return Messages.get(this, "prompt",this.type);
    }
    public static void onSublimation( Talent oldTalent, Talent newTalent ){
        if (curItem instanceof ScrollOfMetamorphosis) {
            ((ScrollOfMetamorphosis) curItem).readAnimation();
            Sample.INSTANCE.play(Assets.Sounds.READ);
        }
        curUser.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
        Transmuting.show(curUser, oldTalent, newTalent);

        if (hero.hasTalent(newTalent)) {
            Talent.onTalentUpgraded(hero, newTalent);
        }
    }

    public ScrollOfSublimation type(String value) {
        type = value;
        return this;
    }

    public void doRead() {

        GameScene.show(new ScrollOfSublimation.WndSublimation(this.type));
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

        //for window restoring
        /*
        public WndSublimation(){
            super();

            if (INSTANCE != null){
                replacing = INSTANCE.replacing;
                tier = INSTANCE.tier;
                replaceOptions = INSTANCE.replaceOptions;
                INSTANCE = this;
                setup(replacing, tier, replaceOptions);
            } else {
                hide();
            }
        }

         */

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
                case "DM300":
                    tier=2;
                    this.index=2;
                    Collections.addAll(availableTalents, Talent.OVERLOAD_CHARGE, Talent.GAS_SPURT,Talent.FASTING);
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
