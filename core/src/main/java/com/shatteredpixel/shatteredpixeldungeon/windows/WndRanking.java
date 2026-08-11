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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.RankingRestart;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroHallScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BadgesGrid;
import com.shatteredpixel.shatteredpixeldungeon.ui.BadgesList;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.CustomNoteButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingListPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class WndRanking extends WndTabbed {

	private static final int WIDTH			= 115;
	private static final int HEIGHT			= 162;

	private static WndRanking INSTANCE;

	private String gameID;
	private Rankings.Record record;
	private boolean fromHeroHall;

	public WndRanking( final Rankings.Record rec ) {
		this(rec, false);
	}

	public WndRanking( final Rankings.Record rec, boolean fromHeroHall ) {

		super();
		resize( WIDTH, HEIGHT );

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;

		this.gameID = rec.gameID;
		this.record = rec;
		this.fromHeroHall = fromHeroHall;
		Badges.loadGlobal();
		Rankings.INSTANCE.loadGameData( rec );
		createControls();
		/*
		try {
			Badges.loadGlobal();
			Rankings.INSTANCE.loadGameData( rec );
			createControls();
		} catch ( Exception e ) {
			Game.reportException( new RuntimeException("Rankings Display Failed!",e));
			Dungeon.hero = null;
			createControls();
		}

		 */
	}

	@Override
	public void destroy() {
		super.destroy();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}

	private void createControls() {

		if (Dungeon.hero != null) {
			Icons[] icons =
					{Icons.RANKINGS, Icons.TALENT, Icons.BACKPACK_LRG, Icons.BADGES, Icons.CHALLENGE_COLOR};
			/*
			Group[] pages =
					{new StatsTab(), new TalentsTab(), new ItemsTab(), new BadgesTab(), null};

			if (Dungeon.challenges != 0){
				pages[4] = new ChallengesTab();

			}

			 */
			Group[] pages =
					{new StatsTab(), new TalentsTab(), new ItemsTab(), new BadgesTab(), new ChallengesTab()};

			for (int i = 0; i < pages.length; i++) {

				if (pages[i] == null) {
					break;
				}

				add(pages[i]);

				Tab tab = new RankingTab(icons[i], pages[i]);
				add(tab);
			}


			layoutTabs();

			select(0);
		} else {
			StatsTab tab = new StatsTab();
			add(tab);

		}
	}

	private class RankingTab extends IconTab {

		private Group page;

		public RankingTab( Icons icon, Group page ) {
			super( Icons.get(icon) );
			this.page = page;
		}

		@Override
		protected void select( boolean value ) {
			super.select( value );
			if (page != null) {
				page.visible = page.active = selected;
			}
		}
	}

	private class StatsTab extends Group {

		private int GAP	= 4;

		public StatsTab() {
			super();

			String heroClass = record.heroClass.name();
			if (Dungeon.hero != null){
				heroClass = Dungeon.hero.className();
			}

			IconTitle title = new IconTitle();
			title.icon( HeroSprite.avatar( record.heroClass, record.armorTier, record.skin ) );
			title.label( Messages.get(this, "title", record.herolevel, heroClass ).toUpperCase( Locale.ENGLISH ) );
			title.color(Window.TITLE_COLOR);
			title.setRect( 0, 0, WIDTH, 0 );
			add( title );

			if (Dungeon.hero != null && Dungeon.seed != -1){
				GAP--;
			}

			float pos = title.bottom() + 1;

			RenderedTextBlock date = PixelScene.renderTextBlock(record.date, 7);
			date.hardlight(0xCCCCCC);
			date.setPos(0, pos);
			add(date);

			RenderedTextBlock version = PixelScene.renderTextBlock(record.version, 7);
			version.hardlight(0xCCCCCC);
			version.setPos(WIDTH-version.width(), pos);
			add(version);

			pos = date.bottom()+5;

			NumberFormat num = NumberFormat.getInstance(Messages.locale());

			if (Dungeon.hero == null){
				pos = statSlot( this, Messages.get(this, "score"), num.format( record.score ), pos );
				pos += GAP;

				Image errorIcon = Icons.WARNING.get();
				errorIcon.y = pos;
				add(errorIcon);

				RenderedTextBlock errorText = PixelScene.renderTextBlock(Messages.get(WndRanking.class, "error"), 6);
				errorText.maxWidth((int)(WIDTH-errorIcon.width()-GAP));
				errorText.setPos(errorIcon.width()+GAP, pos + (errorIcon.height()-errorText.height())/2);
				add(errorText);

			} else {

				pos = statSlot(this, Messages.get(this, "score"), num.format(Statistics.totalScore), pos);

				IconButton scoreInfo = new IconButton(Icons.get(Icons.INFO)) {
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().addToFront(new WndScoreBreakdown());
					}
				};
				scoreInfo.setSize(16, 16);
				scoreInfo.setPos(WIDTH - scoreInfo.width(), pos - 10 - GAP);
				add(scoreInfo);

				pos += GAP;

				int strBonus = Dungeon.hero.STR() - Dungeon.hero.STR;
				if (strBonus > 0)
					pos = statSlot(this, Messages.get(this, "str"), Dungeon.hero.STR + " + " + strBonus, pos);
				else if (strBonus < 0)
					pos = statSlot(this, Messages.get(this, "str"), Dungeon.hero.STR + " - " + -strBonus, pos);
				else
					pos = statSlot(this, Messages.get(this, "str"), Integer.toString(Dungeon.hero.STR), pos);
				pos = statSlot(this, Messages.get(this, "duration"), num.format((int) Statistics.duration), pos);
				if (Statistics.highestAscent == 0) {
					pos = statSlot(this, Messages.get(this, "depth"), num.format(Statistics.deepestFloor), pos);
				} else {
					pos = statSlot(this, Messages.get(this, "ascent"), num.format(Statistics.highestAscent), pos);
				}
				if (Dungeon.seed != -1) {
					if (Dungeon.daily) {
						if (Dungeon.dailyReplay) {
							pos = statSlot(this, Messages.get(this, "replay_for"), "_" + Dungeon.customSeedText + "_", pos);
						} else {
							pos = statSlot(this, Messages.get(this, "daily_for"), "_" + Dungeon.customSeedText + "_", pos);
						}
					} else if (!Dungeon.customSeedText.isEmpty()) {
						pos = statSlot(this, Messages.get(this, "custom_seed"), "_" + Dungeon.customSeedText + "_", pos);
					} else {
						pos = statSlot(this, Messages.get(this, "seed"), DungeonSeed.convertToCode(Dungeon.seed), pos);
					}
				} else {
					pos += GAP + 5;
				}

				pos += GAP;

				if (record.randomMode || Dungeon.hero.randomMode) {
					float randomInfoTop = pos;
					pos = statSlot(this, Messages.get(this, "random_mode"), Messages.get(this, "random_mode_on"), pos);
					IconButton randomInfo = new IconButton(Icons.get(Icons.INFO)) {
						@Override
						protected void onClick() {
							super.onClick();
							ShatteredPixelDungeon.scene().addToFront(new WndMessage(randomModeDetails()));
						}
					};
					randomInfo.setSize(16, 16);
					randomInfo.setPos(WIDTH - randomInfo.width(), randomInfoTop - 3);
					add(randomInfo);
				}

				pos = statSlot(this, Messages.get(this, "enemies"), num.format(Statistics.enemiesSlain), pos);
				pos = statSlot(this, Messages.get(this, "gold"), num.format(Statistics.goldCollected), pos);
				pos = statSlot(this, Messages.get(this, "food"), num.format(Statistics.foodEaten), pos);
				pos = statSlot(this, Messages.get(this, "alchemy"), num.format(Statistics.itemsCrafted), pos);
			}

			int buttontop = HEIGHT - 16 - (record.newCycle ? 0 : 18);

			if (Dungeon.hero != null && Dungeon.seed != -1 && !Dungeon.daily &&
					(DeviceCompat.isDebug() || Badges.isUnlocked(Badges.Badge.VICTORY))){
				final Image icon = Icons.get(Icons.SEED);
				RedButton btnSeed = new RedButton(Messages.get(this, "copy_seed")){
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().addToFront(new WndOptions(new Image(icon),
								Messages.get(WndRanking.StatsTab.this, "copy_seed"),
								Messages.get(WndRanking.StatsTab.this, "copy_seed_desc"),
								Messages.get(WndRanking.StatsTab.this, "copy_seed_copy"),
								Messages.get(WndRanking.StatsTab.this, "copy_seed_cancel")){
							@Override
							protected void onSelect(int index) {
								super.onSelect(index);
								if (index == 0){
									SPDSettings.customSeed(DungeonSeed.convertToCode(Dungeon.seed));
									icon.hardlight(1f, 1.5f, 0.67f);
								}
							}
						});
					}
				};
				if (DungeonSeed.convertFromText(SPDSettings.customSeed()) == Dungeon.seed){
					icon.hardlight(1f, 1.5f, 0.67f);
				}
				btnSeed.icon(icon);
				btnSeed.setRect(0, buttontop, 115, 16);
				add(btnSeed);
			}

			if (!record.newCycle) {
				final RedButton heroHall = new RedButton(Messages.get(
						this, fromHeroHall ? "remove_from_hero_hall" : "copy_to_hero_hall")) {
					@Override
					protected void onClick() {
						super.onClick();
						if (fromHeroHall) {
							ShatteredPixelDungeon.scene().addToFront(new WndOptions(
									new ItemSprite(ItemSpriteSheet.CROWN, null),
									Messages.get(StatsTab.this, "remove_title"),
									Messages.get(StatsTab.this, "remove_desc"),
									Messages.get(StatsTab.this, "remove_confirm"),
									Messages.get(StatsTab.this, "remove_cancel")) {
								@Override
								protected void onSelect(int index) {
									super.onSelect(index);
									if (index == 0 && Rankings.INSTANCE.removeFromHeroHall(record)) {
										WndRanking.this.hide();
										ShatteredPixelDungeon.switchNoFade(HeroHallScene.class);
									} else if (index == 0) {
										ShatteredPixelDungeon.scene().addToFront(
												new WndMessage(Messages.get(StatsTab.this, "hero_hall_save_failed")));
									}
								}
							});
						} else if (Rankings.INSTANCE.addToHeroHall(record)) {
							text(Messages.get(StatsTab.this, "in_hero_hall"));
							enable(false);
						} else if (!Rankings.INSTANCE.isInHeroHall(record)) {
							ShatteredPixelDungeon.scene().addToFront(
									new WndMessage(Messages.get(StatsTab.this, "hero_hall_save_failed")));
						}
					}
				};
				heroHall.icon(new ItemSprite(ItemSpriteSheet.CROWN, null));
				heroHall.setRect(0, HEIGHT - 16, WIDTH, 16);
				if (!fromHeroHall && Rankings.INSTANCE.isInHeroHall(record)) {
					heroHall.text(Messages.get(this, "in_hero_hall"));
					heroHall.enable(false);
				}
				add(heroHall);
			}

		}

		private float statSlot( Group parent, String label, String value, float pos ) {

			RenderedTextBlock txt = PixelScene.renderTextBlock( label, 7 );
			txt.setPos(0, pos);
			parent.add( txt );

			txt = PixelScene.renderTextBlock( value, 7 );
			txt.setPos(WIDTH * 0.6f, pos);
			PixelScene.align(txt);
			parent.add( txt );

			return pos + GAP + txt.height();
		}

		private String randomSubClassName() {
			if (Dungeon.hero != null && Dungeon.hero.subClass != null && Dungeon.hero.subClass != HeroSubClass.NONE) {
				return Messages.titleCase(Dungeon.hero.subClass.title());
			}
			if (record.subClass != null && !record.subClass.isEmpty()) {
				try {
					HeroSubClass subClass = HeroSubClass.valueOf(record.subClass);
					if (subClass != HeroSubClass.NONE) {
						return Messages.titleCase(subClass.title());
					}
				} catch (Exception ignored) {
				}
			}
			return "";
		}

		private String randomArmorAbilityName() {
			if (Dungeon.hero != null && Dungeon.hero.armorAbility != null) {
				return Messages.titleCase(Dungeon.hero.armorAbility.name());
			}
			if (record.armorAbility != null && !record.armorAbility.isEmpty()) {
				return simpleClassName(record.armorAbility);
			}
			return "";
		}

		private String simpleClassName(String name) {
			int lastDot = name.lastIndexOf('.');
			return lastDot == -1 ? name : name.substring(lastDot + 1);
		}

		private String randomModeDetails() {
			String subClass = randomSubClassName();
			String armorAbility = randomArmorAbilityName();
			String initialTalents = talentList(record.randomTalents);
			String selectedTalents = talentList(record.selectedTalents);

			return Messages.get(this, "random_mode") + ": " + Messages.get(this, "random_mode_on")
					+ "\n" + Messages.get(this, "subclass") + ": " + (subClass.isEmpty() ? Messages.get(this, "none") : subClass)
					+ "\n" + Messages.get(this, "armor_ability") + ": " + (armorAbility.isEmpty() ? Messages.get(this, "none") : armorAbility)
					+ "\n\n" + Messages.get(this, "initial_talents") + ": " + initialTalents
					+ "\n\n" + Messages.get(this, "selected_talents") + ": " + selectedTalents;
		}

		private String talentList(String[] talents) {
			if (talents == null || talents.length == 0) {
				return Messages.get(this, "none");
			}
			StringBuilder list = new StringBuilder();
			for (String talent : talents) {
				if (talent == null || talent.isEmpty()) {
					continue;
				}
				for (String token : talent.split(",")) {
					if (token.isEmpty()) {
						continue;
					}
					if (list.length() > 0) {
						list.append(", ");
					}
					list.append(talentName(token));
				}
			}
			return list.length() == 0 ? Messages.get(this, "none") : list.toString();
		}

		private String talentName(String token) {
			int separator = token.indexOf(':');
			String name = separator == -1 ? token : token.substring(0, separator);
			String points = separator == -1 ? "" : token.substring(separator);
			try {
				return Talent.valueOf(name).title() + points;
			} catch (Exception ignored) {
				return token;
			}
		}
	}

	private class TalentsTab extends Group{

		public TalentsTab(){
			super();


			camera = WndRanking.this.camera;

			int tiers = 1;
			if (Dungeon.hero.lvl >= 6) tiers++;
			if (Dungeon.hero.lvl >= 12 && (Dungeon.hero.subClass != HeroSubClass.NONE || Dungeon.hero.heroClass== HeroClass.FREEMAN)) tiers++;
			if (Dungeon.hero.lvl >= 20 && Dungeon.hero.armorAbility != null) tiers++;
			/*
			while (Dungeon.hero.talents.size() > tiers){
				Dungeon.hero.talents.remove(Dungeon.hero.talents.size()-1);
			}

			 */
			//Talent.initClassTalents(Dungeon.hero);
			TalentsPane p = new TalentsPane(TalentButton.Mode.INFO);
			add(p);
			p.setPos(0, 0);
			p.setSize(WIDTH, HEIGHT);
			p.setPos(0, 0);

		}

	}

	private class ItemsTab extends Group {
		private static final int MAX_ITEMS_PER_ROW = 4;

		private float pos;

		public ItemsTab() {
			super();

			Belongings stuff = Dungeon.hero.belongings;
			if (stuff.weapon != null) {
				addItem( stuff.weapon );
			}
			if (stuff.armor != null) {
				addItem( stuff.armor );
			}
			if (stuff.artifact != null) {
				addItem( stuff.artifact );
			}
			if (stuff.misc != null) {
				addItem( stuff.misc );
			}
			if (stuff.ring != null) {
				addItem( stuff.ring );
			}

			pos = 0;

			ArrayList<Item> displayItems = rankingDisplayItems(
					stuff.backpack, Dungeon.quickslot.getItems());
			int rows = (displayItems.size() + MAX_ITEMS_PER_ROW - 1) / MAX_ITEMS_PER_ROW;
			float slotHeight = rows > 1 ? 20 : 23;

			for (int row = 0; row < rows; row++) {
				int rowStart = row * MAX_ITEMS_PER_ROW;
				int rowSize = Math.min(MAX_ITEMS_PER_ROW, displayItems.size() - rowStart);
				float slotWidth = Math.min(28, (WIDTH - rowSize + 1) / (float) rowSize);
				pos = 0;

				for (int column = 0; column < rowSize; column++) {
					QuickSlotButton slot = new QuickSlotButton(displayItems.get(rowStart + column));
					slot.setRect(pos, 120 + row * (slotHeight + 1), slotWidth, slotHeight);
					PixelScene.align(slot);
					add(slot);
					pos += slotWidth + 1;
				}
			}
		}

		private void addItem( Item item ) {
			ItemButton slot = new ItemButton( item );
			slot.setRect( 0, pos, width, ItemButton.HEIGHT );
			add( slot );

			pos += slot.height() + 1;
		}
	}

	static ArrayList<Item> rankingDisplayItems(Iterable<Item> backpackItems, Item[] quickslotItems) {
		ArrayList<Item> result = new ArrayList<>();

		for (Item item : backpackItems) {
			if (item instanceof Trinket) {
				result.add(item);
				if (result.size() == 2) {
					break;
				}
			}
		}

		for (Item item : quickslotItems) {
			if (item != null && item.quantity() > 0 && !result.contains(item)) {
				result.add(item);
			}
		}

		return result;
	}

	private class BadgesTab extends Group {

		public BadgesTab() {
			super();

			camera = WndRanking.this.camera;


			Component badges;
			if (Badges.filterReplacedBadges(false).size() <= 8){
				badges = new BadgesList(false);
			} else {
				badges = new BadgesGrid(false);
			}
			add(badges);
			badges.setSize( WIDTH, HEIGHT );
		}
	}
	/*

	private class ChallengesTab extends Group{

		private ScrollingListPane list;
		protected void createChildren() {
			list = new ScrollingListPane();
			add( list );
		}

		public ChallengesTab(){


			camera = WndRanking.this.camera;

			float pos = 0;

			for (int i=0; i < Challenges.NAME_IDS.length; i++) {

				final String challenge = Challenges.NAME_IDS[i];

				CheckBox cb = new CheckBox( Messages.titleCase(Messages.get(Challenges.class, challenge)) );
				cb.checked( (Dungeon.challenges & Challenges.MASKS[i]) != 0 );
				cb.active = false;

				if (i > 0) {
					pos += 1;
				}
				cb.setRect( 0, pos, WIDTH-16, 8 );

				add( cb );

				IconButton info = new IconButton(Icons.get(Icons.INFO)){
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().add(
								new WndMessage(Messages.get(Challenges.class, challenge+"_desc"))
						);
					}
				};
				info.setRect(cb.right(), pos, 16, 9);
				add(info);

				pos = cb.bottom();
			}


		}

	}

	 */
	private class ChallengesTab extends Group{

		public ChallengesTab(){
			super();
			camera = WndRanking.this.camera;
			IconTitle title = new IconTitle();

			title.label( Messages.get(WndChallenges.class, "titlerank"));
			title.setRect( 0, 10, WIDTH, 0 );
			add( title );
			IconButton scoreInfo = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void onClick() {
					super.onClick();
					ShatteredPixelDungeon.scene().addToFront(new WndChallenges(Dungeon.challenges, false));
				}
			};
			scoreInfo.setSize(16, 16);
			scoreInfo.setPos(WIDTH - scoreInfo.width(), 10);
			add(scoreInfo);

			if (!fromHeroHall && RankingRestart.isEligible(record)
					&& GamesInProgress.firstEmpty() != -1) {
				RedButton restart = new RedButton(Messages.get(this, "restart")) {
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().addToFront(new WndOptions(
								Icons.get(Icons.STAIRS),
								Messages.get(ChallengesTab.this, "restart_title"),
								Messages.get(ChallengesTab.this, "restart_desc"),
								Messages.get(ChallengesTab.this, "restart_confirm"),
								Messages.get(ChallengesTab.this, "restart_cancel")) {
							@Override
							protected void onSelect(int index) {
								super.onSelect(index);
								if (index != 0) {
									return;
								}
								int slot = GamesInProgress.firstEmpty();
								if (slot == -1) {
									ShatteredPixelDungeon.scene().addToFront(
											new WndMessage(Messages.get(ChallengesTab.this, "no_slots")));
								} else if (RankingRestart.begin(record)) {
									GamesInProgress.curSlot = slot;
									InterlevelScene.mode = InterlevelScene.Mode.RESTART;
									Game.switchScene(InterlevelScene.class);
								}
							}
						});
					}
				};
				restart.icon(Icons.get(Icons.STAIRS));
				restart.setRect(0, title.bottom() + 12, WIDTH, 18);
				add(restart);
			}

			/*
			IconButton game = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void onClick() {
					super.onClick();
					Dungeon.daily = Dungeon.dailyReplay = false;
					Dungeon.initSeed();
					ActionIndicator.clearAction();
					Dungeon.hero.HP=Dungeon.hero.HT;
					GamesInProgress.curSlot =GamesInProgress.checkAll().size()+1;
					InterlevelScene.mode = InterlevelScene.Mode.RESTART;
					Game.switchScene( InterlevelScene.class );
				}
			};
			game.setSize(16, 16);
			game.setPos(WIDTH - scoreInfo.width(), 40);

			if(record.win &&  GamesInProgress.checkAll().size() < GamesInProgress.MAX_SLOTS){
				add(game);
			}

			 */




			/*
			WndChallenges wc = new WndChallenges(Dungeon.challenges, false){
				@Override
				public void onBackPressed() {
					//WndRanking.this.hide();
					super.onBackPressed();
				}
			};
			add(wc);

			 */

		}

	}



	private class ItemButton extends Button {

		public static final int HEIGHT	= 23;

		private Item item;

		private ItemSlot slot;
		private ColorBlock bg;
		private RenderedTextBlock name;

		public ItemButton( Item item ) {

			super();

			this.item = item;

			slot.item( item );
			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.2f;
				bg.ga = -0.1f;
			} else if (!item.isIdentified()) {
				bg.ra = 0.1f;
				bg.ba = 0.1f;
			}
		}

		@Override
		protected void createChildren() {

			bg = new ColorBlock( 28, HEIGHT, 0x9953564D );
			add( bg );

			slot = new ItemSlot();
			add( slot );

			name = PixelScene.renderTextBlock( 7 );
			add( name );

			super.createChildren();
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			slot.setRect( x, y, 28, HEIGHT );
			PixelScene.align(slot);

			name.maxWidth((int)(width - slot.width() - 2));
			name.text(Messages.titleCase(item.name()));
			name.setPos(
					slot.right()+2,
					y + (height - name.height()) / 2
			);
			PixelScene.align(name);

			super.layout();
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
		}

		protected void onPointerUp() {
			bg.brightness( 1.0f );
		}

		@Override
		protected void onClick() {
			Game.scene().add( new WndInfoItem( item ) );
		}
	}

	private class QuickSlotButton extends ItemSlot{

		private Item item;
		private ColorBlock bg;

		QuickSlotButton(Item item){
			super(item);
			this.item = item;

			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.2f;
				bg.ga = -0.1f;
			} else if (!item.isIdentified()) {
				bg.ra = 0.1f;
				bg.ba = 0.1f;
			}
		}

		@Override
		protected void createChildren() {
			bg = new ColorBlock( 1, 1, 0x9953564D );
			add( bg );

			super.createChildren();
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			bg.size( width(), height() );

			super.layout();
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
		}

		protected void onPointerUp() {
			bg.brightness( 1.0f );
		}

		@Override
		protected void onClick() {
			Game.scene().add(new WndInfoItem(item));
		}
	}
}
