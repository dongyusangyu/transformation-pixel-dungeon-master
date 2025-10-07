package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TransformSpell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class TestTalent  extends TestGenerator {
    {
        image = ItemSpriteSheet.TRANSFORMATION_ELE;
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        return super.actions(hero);
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_GIVE)) {
            GameScene.show(new WndMetamorphChoose());
        }
    }
    public  static final String[] NAME_IDS = {
            "attack",
            "magic",
            "effect",
            "resource",
            "spell",
            "assist",
            "other"
    };
    public  static int getType(String name){
        switch (name){
            case"attack": default:
                return Talent.ATTACK;
            case"magic":
                return Talent.MAGIC;
            case"effect":
                return Talent.EFFECT;
            case"resource":
                return Talent.RESOURCE;
            case"spell":
                return Talent.SPELL;
            case"assist":
                return Talent.ASSIST;
            case"other":
                return Talent.OTHER;
        }

    }

    public static class WndMetamorphChoose extends Window {

        public static WndMetamorphChoose INSTANCE;

        TalentsPane pane;

        public WndMetamorphChoose() {
            super();

            INSTANCE = this;

            float top = 0;

            IconTitle title = new IconTitle(curItem);
            title.color(TITLE_COLOR);
            title.setRect(0, 0, 120, 0);
            add(title);

            top = title.bottom() + 2;

            RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(ScrollOfMetamorphosis.class, "choose_desc"), 6);
            text.maxWidth(120);
            text.setPos(0, top);
            add(text);

            top = text.bottom() + 2;

            ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
            Talent.initClassTalents(hero.heroClass, talents, hero.metamorphedTalents, new LinkedHashMap<>());


            for (LinkedHashMap<Talent, Integer> tier : talents) {
                for (Talent talent : tier.keySet()) {
                    tier.put(talent, hero.pointsInTalent(talent));
                }
            }


            pane = new TalentsPane(TalentButton.Mode.METAMORPH_ALL, talents);
            add(pane);
            pane.setPos(0, top);
            pane.setSize(120, pane.content().height());
            resize((int)pane.width(), (int)pane.bottom());
            pane.setPos(0, top);
        }

        @Override
        public void hide() {
            super.hide();
            INSTANCE = null;
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
        }

        @Override
        public void offset(int xOffset, int yOffset) {
            super.offset(xOffset, yOffset);
            pane.setPos(pane.left(), pane.top()); //triggers layout
        }
    }


    public static class WndMetamorphReplace extends Window {

        public static WndMetamorphReplace INSTANCE;
        private static final int WIDTH		= 120;

        public Talent replacing;
        public int tier;
        public int number= Statistics.metamorphosis;
        private static final int BUTTON_HEIGHT = 26;
        private static final int GAP        = 1;
        protected static final int MARGIN 		= 2;
        protected static final int BUTTON_WIDTH	= 20;
        LinkedHashMap<Talent, Integer> replaceOptions;

        //for window restoring
        public WndMetamorphReplace(){
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

        public WndMetamorphReplace(Talent replacing, int tier,int type){
            super();
            INSTANCE = this;
            this.replacing = replacing;
            this.tier = tier;



            LinkedHashMap<Talent, Integer> options = new LinkedHashMap<>();
            Set<Talent> curTalentsAtTier = hero.talents.get(tier-1).keySet();
            List<Talent> availableTalents = Talent.typeTalent.get(tier-1).get(type);


            List<Talent> selectedTalents = new ArrayList<>();
            for(Talent talent :availableTalents){
                if (!curTalentsAtTier.contains(talent)){
                    selectedTalents.add(talent);
                }
            }

            for (Talent talent : selectedTalents) {
                options.put(talent, hero.pointsInTalent(replacing));
            }

            replaceOptions = options;
            setup(replacing, tier, options);
        }

        private void setup(Talent replacing, int tier, LinkedHashMap<Talent, Integer> replaceOptions){

            float top = 0;
            this.replacing = replacing;
            IconTitle title = new IconTitle( curItem );
            title.color( TITLE_COLOR );
            title.setRect(0, 0, WIDTH, 0);
            add(title);

            top = title.bottom() + 2;

            RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(ScrollOfMetamorphosis.class, "replace_desc"), 6);
            text.maxWidth(WIDTH);
            text.setPos(0, top);
            add(text);
            int bottom=(int)text.bottom();
            resize(WIDTH, bottom+82);
            float x = 0;
            ScrollingGridPane pane = new ScrollingGridPane();
            add(pane);
            pane.setRect(0, text.bottom()+2,WIDTH,80);


            Component content = pane.content();
            int pos = 2 ;

            for (Talent talent: replaceOptions.keySet()){

                TalentButton gridItem = new TalentButton(tier,talent,0,TalentButton.Mode.METAMORPH_REPLACE);
                //content.add(gridItem);
                content.add(gridItem);
                gridItem.setPos(x,pos);
                x += BUTTON_WIDTH;
                if(x >= WIDTH){
                    x=0;
                    pos+=BUTTON_HEIGHT;
                }
            }
            content.setRect(0,0,WIDTH, pos+BUTTON_HEIGHT);
            pane.update();
            //resize(120, (int)pane.bottom());
        }

        @Override
        public void hide() {
            if(curItem instanceof TransformSpell && number<Statistics.metamorphosis && hero.pointsInTalent(Talent.MORE_CHANCE)<=Random.Int(10)){
                curItem.detach(curUser.belongings.backpack);
            }
            super.hide();

            if (INSTANCE == this) {
                INSTANCE = null;
            }
        }

        @Override
        public void onBackPressed() {

            super.onBackPressed();
            /*
            if(curItem instanceof TransformSpell){
                ((TransformSpell)curItem).confirmCancelation(this);
            }else{
                super.onBackPressed();
            }

             */

        }
    }


    public static class WndType extends Window {

        private static final int WIDTH		= 120;
        private static final int TTL_HEIGHT = 16;
        private static final int BTN_HEIGHT = 16;
        private static final int GAP        = 1;
        protected static final int MARGIN 		= 2;
        protected static final int BUTTON_HEIGHT	= 18;

        private boolean editable;
        private ArrayList<RedButton> boxes;

        public WndType( int tier,Talent talent ) {

            super();

            float pos = 0;

            IconTitle tfTitle = new IconTitle(new TalentIcon( talent ), Messages.get(ScrollOfMetamorphosis.class, "type_name"));
            tfTitle.setRect(0, pos, WIDTH, 0);
            add(tfTitle);

            pos = tfTitle.bottom() + 2*MARGIN;

            RenderedTextBlock tfMesage = PixelScene.renderTextBlock( 6 );
            tfMesage.text(Messages.get(ScrollOfMetamorphosis.class, "type_desc"), WIDTH);
            tfMesage.setPos( 0, pos );
            add( tfMesage );

            pos = tfMesage.bottom() + 2*MARGIN;

            boxes = new ArrayList<>();

            for (int i=0; i < 7 ; i++) {

                final String name = ScrollOfMetamorphosis.NAME_IDS[i];

                RedButton cb = new RedButton( Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, name)) ){
                    @Override
                    protected void onClick() {
                        hide();
                        GameScene.show(new WndMetamorphReplace(talent, tier,ScrollOfMetamorphosis.getType(name)));
                    }
                };

                if (i > 0) {
                    pos += GAP;
                }
                cb.setRect( 0, pos, WIDTH, BTN_HEIGHT );

                add( cb );
                boxes.add( cb );
                /*
                IconButton info = new IconButton(Icons.get(Icons.INFO)){
                    @Override
                    protected void onClick() {
                        super.onClick();
                        GameScene.show(new WndShowTalent(tier-1,ScrollOfMetamorphosis.getType(name),talent));
                    }
                };
                info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
                add(info);

                 */

                pos = cb.bottom();
            }

            resize( WIDTH, (int)pos );
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
        }
    }

    public static class WndShowTalent extends Window{
        private static final int WIDTH		= 100;
        private static final int TTL_HEIGHT = 16;
        private static final int BUTTON_HEIGHT = 26;
        private static final int GAP        = 1;
        protected static final int MARGIN 		= 2;
        protected static final int BUTTON_WIDTH	= 20;

        private boolean editable;
        private ArrayList<RedButton> boxes;


        public WndShowTalent(int tier, int type,Talent talent0){
            super();

            float pos = 0;

            IconTitle tfTitle = new IconTitle(new TalentIcon( talent0 ), Messages.get(ScrollOfMetamorphosis.class, ScrollOfMetamorphosis.NAME_IDS[type]));
            tfTitle.setRect(0, pos, WIDTH, 0);
            add(tfTitle);

            pos = tfTitle.bottom() + 2*MARGIN;

            RenderedTextBlock tfMesage = PixelScene.renderTextBlock( 6 );
            tfMesage.text(Messages.get(ScrollOfMetamorphosis.class, "intype"), WIDTH);
            tfMesage.setPos( 0, pos );
            add( tfMesage );


            pos = tfMesage.bottom() + 2*MARGIN;
            resize( WIDTH, (int)pos+80);
            ArrayList<Talent> talents = Talent.typeTalent.get(tier).get(type);
            float x = 0;
            ScrollingGridPane pane = new ScrollingGridPane();
            add(pane);
            pane.setRect(0, pos,WIDTH,80);
            Component content = pane.content();
            pos = 2 ;
            for (Talent talent : talents){

                TalentButton gridItem = new TalentButton(tier,talent,0,TalentButton.Mode.INFO);
                //content.add(gridItem);
                content.add(gridItem);
                gridItem.setPos(x,pos);
                x += BUTTON_WIDTH;
                if(x >= WIDTH){
                    x=0;
                    pos+=BUTTON_HEIGHT;
                }
            }
            content.setRect(0,0,WIDTH, pos+BUTTON_HEIGHT);
            pane.update();
        }
        @Override
        public void onBackPressed() {
            super.onBackPressed();
        }
    }
}
