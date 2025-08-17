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
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

public class ScrollOfMetamorphosis extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_METAMORPH;

		talentFactor = 2f;
	}
	public int energyVal() {
		return 3* quantity;
	}
	protected static boolean identifiedByUse = true;
	
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

	public static void onMetamorph( Talent oldTalent, Talent newTalent ){
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

	private void confirmCancelation( Window chooseWindow ) {
		GameScene.show( new WndOptions(new ItemSprite(this),
				Messages.titleCase(name()),
				Messages.get(InventoryScroll.class, "warning"),
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
			Talent.initClassTalents(hero.heroClass, talents, hero.metamorphedTalents, new LinkedHashMap<>());


			for (LinkedHashMap<Talent, Integer> tier : talents) {
				for (Talent talent : tier.keySet()) {
					tier.put(talent, hero.pointsInTalent(talent));
				}
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

			if (identifiedByUse){
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

		public WndMetamorphReplace(Talent replacing, int tier,int type){
			super();


			if (!identifiedByUse && curItem instanceof ScrollOfMetamorphosis && hero.pointsInTalent(Talent.MORE_CHANCE)<=Random.Int(10)) {
				curItem.detach(curUser.belongings.backpack);
			}
			identifiedByUse = false;

			INSTANCE = this;

			this.replacing = replacing;
			this.tier = tier;


			LinkedHashMap<Talent, Integer> options = new LinkedHashMap<>();
			Set<Talent> curTalentsAtTier = hero.talents.get(tier-1).keySet();
			List<Talent> availableTalents = new ArrayList<>();
			int beilv = 3;
			int maxTpye = 7;
			if(type != 4){
			//if(false){
				for(int i = 0;i < maxTpye;i++){
					ArrayList<Talent> typeTalents = Talent.typeTalent.get(tier-1).get(i);
					for (Talent talent : typeTalents){
						if (!curTalentsAtTier.contains(talent)){
							availableTalents.add(talent);
						}
					}
				}
			}
			for(int i = 0;i < beilv-1;i++){
				ArrayList<Talent> typeTalents = Talent.typeTalent.get(tier-1).get(type);
				for (Talent talent : typeTalents){
					if (!curTalentsAtTier.contains(talent)){
						availableTalents.add(talent);
					}
				}
			}
			/*
			for (HeroClass cls : HeroClass.values()){
				if(cls== HeroClass.FREEMAN){
					continue;
				}
				if (cls==HeroClass.ADVANCED){
					break;
				}
				ArrayList<LinkedHashMap<Talent, Integer>> clsTalents = new ArrayList<>();
				Talent.initClassTalents(cls, clsTalents);

				Set<Talent> clsTalentsAtTier = clsTalents.get(tier-1).keySet();
				for (Talent talent : clsTalentsAtTier){
					if (!curTalentsAtTier.contains(talent)){
						availableTalents.add(talent);
					}
				}
			}
			 */
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
			/*
			List<Talent> ClericTalent=getClericTalent(tier);
			for(Talent talent : ClericTalent){
				if(!curTalentsAtTier.contains(talent) && !selectedTalents.contains(talent)){
					selectedTalents.add(talent);
					break;
				}
			}
			 */
			for (Talent talent : selectedTalents) {
				options.put(talent, hero.pointsInTalent(replacing));
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

			for (int i=0; i < 7 ; i++) {

				final String name = ScrollOfMetamorphosis.NAME_IDS[i];

				RedButton cb = new RedButton( Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, name)) ){
					@Override
					protected void onClick() {
						hide();
						GameScene.show(new ScrollOfMetamorphosis.WndMetamorphReplace(talent, tier,ScrollOfMetamorphosis.getType(name)));
					}
				};

				if (i > 0) {
					pos += GAP;
				}
				cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

				add( cb );
				boxes.add( cb );

				IconButton info = new IconButton(Icons.get(Icons.INFO)){
					@Override
					protected void onClick() {
						super.onClick();
						GameScene.show(new ScrollOfMetamorphosis.WndShowTalent(tier-1,ScrollOfMetamorphosis.getType(name),talent));
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
