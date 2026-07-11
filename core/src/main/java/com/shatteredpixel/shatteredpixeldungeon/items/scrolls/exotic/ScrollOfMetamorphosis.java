/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.getClericTalent;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.TalentCatalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournalItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public class ScrollOfMetamorphosis extends Scroll {
	
	{
		image = ItemSpriteSheet.SCROLL_META;
		icon = ItemSpriteSheet.Icons.SCROLL_META;
		talentFactor = 1f;
		anonymous = true;
		unique = true;
	}

	protected static boolean identifiedByUse = true;
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public void doRead() {
		if (!isKnown()) {
			identify();
			curItem = detach(curUser.belongings.backpack);
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}
		GameScene.show(new WndMetamorphChoose());
	}

	public static Talent.TalentType[] commonTypes() {
		return Talent.COMMON_TYPES;
	}

    public enum TalentSource {
        METAMORPHOSIS,
        TRANSFORM_SPELL,
        OTHER
    }

	public static void onMetamorph( Talent oldTalent, Talent newTalent ){
        onMetamorph(oldTalent, newTalent, TalentSource.METAMORPHOSIS);
    }

	public static void onMetamorph( Talent oldTalent, Talent newTalent, TalentSource source ){
		if (curItem instanceof ScrollOfMetamorphosis) {
			((ScrollOfMetamorphosis) curItem).readAnimation();
			Sample.INSTANCE.play(Assets.Sounds.READ);
		}
		curUser.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
		Transmuting.show(curUser, oldTalent, newTalent);
        if (source == TalentSource.METAMORPHOSIS){
		    TalentCatalog.countUse(newTalent);
        } else if (source == TalentSource.TRANSFORM_SPELL){
            TalentCatalog.countTransformSpellUse(newTalent);
        }
        Badges.validateTalent(newTalent);
        if (source == TalentSource.METAMORPHOSIS){
            Catalog.countUse(ScrollOfMetamorphosis.class);
            Talent.onScrollUsed(hero,hero.pos,1f, ScrollOfMetamorphosis.class);
        }
		if (hero.hasTalent(newTalent)) {
			Talent.onTalentUpgraded(hero, newTalent);
		}else{
            Talent.onTalentUpgraded(hero,null);
        }
	}

	private void confirmCancelation( Window chooseWindow ) {
		GameScene.show( new WndOptions(new ItemSprite(this),
				Messages.titleCase(name()),
				Messages.get(ScrollOfMetamorphosis.class, "cancel_warn"),
				Messages.get(InventoryScroll.class, "yes"),
				Messages.get(InventoryScroll.class, "no") ) {
			@Override
			protected void onSelect( int index ) {
				switch (index) {
					case 0:
						curUser.spendAndNext( TIME_TO_READ );
						identifiedByUse = false;
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
			Talent.initClassTalents(hero, talents, hero.metamorphedTalents, new LinkedHashMap<>());


			for (LinkedHashMap<Talent, Integer> tier : talents) {
				for (Talent talent : tier.keySet()) {
					tier.put(talent, hero.pointsInTalent(talent));
				}
				tier.keySet().removeIf(Talent::excludedAsMetamorphSource);
			}
			if (Dungeon.isChallenged(Challenges.MAX_WHEAT)){
				for (LinkedHashMap<Talent, Integer> tier : talents) {
					for (Talent talent : Dungeon.hero.metamorphedTalents.values()) {
						if (tier.containsKey(talent)) {
							tier.remove(talent);
						}
					}
				}
			}

			pane = new TalentsPane(TalentButton.Mode.METAMORPH_CHOOSE, talents);
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

			if (curItem instanceof ScrollOfMetamorphosis && identifiedByUse){
				((ScrollOfMetamorphosis)curItem).confirmCancelation(this);
			} else {
				super.onBackPressed();
			}
		}

		@Override
		public void offset(int xOffset, int yOffset) {
			super.offset(xOffset, yOffset);
			pane.setPos(pane.left(), pane.top()); //triggers layout
		}
	}


	public static class WndMetamorphReplace extends Window {

		public static WndMetamorphReplace INSTANCE;

		public Talent replacing;
		public int tier;
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

		public WndMetamorphReplace(Talent replacing, int tier, Talent.TalentType preferredType){
			super();


			if (!identifiedByUse && curItem instanceof ScrollOfMetamorphosis && hero.pointsInTalent(Talent.MORE_CHANCE)<=Random.Int(10)) {
				curItem.detach(curUser.belongings.backpack);
			}
			identifiedByUse = false;

			INSTANCE = this;

			this.replacing = replacing;
			this.tier = tier;

			LinkedHashMap<Talent, Integer> options = new LinkedHashMap<>();
			HashSet<Talent> curTalentsAtTier = new HashSet<>(hero.talents.get(tier-1).keySet());
			int beilv = 6;
			if(Dungeon.isChallenged(Challenges.HARSH_ENVIRONMENT) && Dungeon.isChallenged(Challenges.MAX_WHEAT)){
				beilv = 3;
			}
			if(hero!=null && hero.buff(Pasty.TranCake.class)!=null){
                GLog.p(Messages.get(Pasty.class, "tran_up"));
				beilv *= 2;
                hero.buff(Pasty.TranCake.class).detach();
			}
			boolean randomMode = hero != null && hero.randomMode;
			List<Talent> availableTalents = Talent.metamorphCandidatePool(tier, preferredType, curTalentsAtTier, randomMode ? 1 : beilv);
			int cnt=4;

			if(hero.pointsInTalent(Talent.MORE_TALENT)>Random.Int(2)){
				cnt+=1;
			}
			cnt-=hero.pointsNegative(Talent.FATE_DECISION);

			List<Talent> selectedTalents = new ArrayList<>();
			while (selectedTalents.size() < cnt && !availableTalents.isEmpty()) {
				Talent randomTalent = Random.element(availableTalents);
				if(!selectedTalents.contains(randomTalent)){
					selectedTalents.add(randomTalent);
					availableTalents.remove(randomTalent);
				}else{
					availableTalents.remove(randomTalent);
				}

			}
			for (Talent talent : selectedTalents) {
				options.put(talent, hero.pointsInTalent(replacing));
                TalentCatalog.countAppearance(talent);
			}

			replaceOptions = options;
			setup(replacing, tier, options);
		}

		private void setup(Talent replacing, int tier, LinkedHashMap<Talent, Integer> replaceOptions){
			float top = 0;

			IconTitle title = new IconTitle( curItem );
			title.color( TITLE_COLOR );
			title.setRect(0, 0, 120, 0);
			add(title);

			top = title.bottom() + 2;

			RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(ScrollOfMetamorphosis.class, "replace_desc"), 6);
			text.maxWidth(120);
			text.setPos(0, top);
			add(text);

			top = text.bottom() + 2;

			TalentsPane.TalentTierPane optionsPane = new TalentsPane.TalentTierPane(replaceOptions, tier, TalentButton.Mode.METAMORPH_REPLACE);
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

			if (INSTANCE == this) {
				INSTANCE = null;
			}
		}

		@Override
		public void onBackPressed() {
			if (curItem instanceof ScrollOfMetamorphosis) {
				((ScrollOfMetamorphosis) curItem).confirmCancelation(this);
			} else {
				super.onBackPressed();
			}
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

			for (Talent.TalentType type : ScrollOfMetamorphosis.commonTypes()) {

				RedButton cb = new RedButton( Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, type.messageKey())) ){
					@Override
					protected void onClick() {
						hide();
						GameScene.show(new ScrollOfMetamorphosis.WndMetamorphReplace(talent, tier, type));
					}
				};

				if (!boxes.isEmpty()) {
					pos += GAP;
				}
				cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

				add( cb );
				boxes.add( cb );

				IconButton info = new IconButton(Icons.get(Icons.INFO)){
					@Override
					protected void onClick() {
						super.onClick();
						GameScene.show(new ScrollOfMetamorphosis.WndShowTalent(tier, type, talent));
					}
				};
				info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
				add(info);

				pos = cb.bottom();
			}

			resize( WIDTH, (int)pos );
		}

		@Override
		public void onBackPressed() {
			super.onBackPressed();
		}
	}

    @Override
    public int energyVal() {
        return 0;
    }
    @Override
    public int value() {
        return 0;
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
		ScrollPane pane;


		public WndShowTalent(int tier, Talent.TalentType type, Talent talent0){
			super();

			float pos = 0;

			IconTitle tfTitle = new IconTitle(new TalentIcon( talent0 ), Messages.get(ScrollOfMetamorphosis.class, type.messageKey()));
			tfTitle.setRect(0, pos, WIDTH, 0);
			add(tfTitle);

			pos = tfTitle.bottom() + 2*MARGIN;

			RenderedTextBlock tfMesage = PixelScene.renderTextBlock( 6 );
			tfMesage.text(Messages.get(ScrollOfMetamorphosis.class, "intype"), WIDTH);
			tfMesage.setPos( 0, pos );
			add( tfMesage );


			pos = tfMesage.bottom() + 2*MARGIN;
			resize( WIDTH, (int)pos+80);
			List<Talent> talents = new ArrayList<>();
			for (Talent talent : Talent.talentsByTierAndType(tier, type)) {
				if (!Talent.forbiddenInCatalogOrMetamorphosis(talent)) {
					talents.add(talent);
				}
			}
			float x = 0;
			/*
			for (Talent talent : talents){
				TalentButton gridItem = new TalentButton(tier,talent,0,TalentButton.Mode.INFO);

				gridItem.setPos(x,pos);
				add(gridItem);
				x += BUTTON_HEIGHT;
				if(x > WIDTH){
					x=0;
					pos+=BUTTON_HEIGHT;
				}
			}
			resize( WIDTH, (int)pos+BUTTON_HEIGHT + 5 );
			 */
			//Component content=new Component();
			//ScrollPane pane = new ScrollPane(content);
			pane = new ScrollPane(new Component()){
				public void onClick(float x, float y) { WndShowTalent.this.onClick(x, y);}
			};;
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
			pane.setRect(0, tfMesage.bottom() + 2*MARGIN,WIDTH,80);
			pane.scrollTo(0, 0);
			pane.update();
		}
		@Override
		public void onBackPressed() {
			super.onBackPressed();
		}
		@Override
		public void offset(int xOffset, int yOffset) {
			super.offset(xOffset, yOffset);
			// refresh the scrollbar pane
			pane.setPos(pane.left(), pane.top());
		}
		protected void onClick(float x, float y) {/* do nothing */}
	}


}
