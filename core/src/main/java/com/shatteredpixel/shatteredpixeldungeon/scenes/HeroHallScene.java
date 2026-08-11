/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndRanking;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;

import java.util.ArrayList;

public class HeroHallScene extends PixelScene {

	private static final float MAX_ROW_WIDTH = 160;
	private static final float ROW_HEIGHT = 20;
	private static final float ROW_GAP = 2;
	private static final float GAP = 4;
	private static final float NAV_BUTTON_HEIGHT = 20;

	@Override
	public void create() {
		super.create();

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);
		uiCamera.visible = false;

		int screenWidth = Camera.main.width;
		int screenHeight = Camera.main.height;
		RectF insets = getCommonInsets();
		add(new TitleBackground(screenWidth, screenHeight));

		float width = screenWidth - insets.left - insets.right;
		float height = screenHeight - insets.top - insets.bottom;

		ItemSprite crown = new ItemSprite(ItemSpriteSheet.CROWN, null);
		IconTitle title = new IconTitle(crown, Messages.get(this, "title"));
		title.setSize(200, 0);
		title.setPos(
				insets.left + (width - title.reqWidth()) / 2f,
				insets.top + (20 - title.height()) / 2f);
		align(title);
		add(title);

		StyledButton normalRankings = new StyledButton(
				Chrome.Type.TOAST_TR, Messages.get(this, "normal_rankings"), 6) {
			@Override
			protected void onClick() {
				super.onClick();
				ShatteredPixelDungeon.switchNoFade(RankingsScene.class);
			}
		};
		normalRankings.icon(new ItemSprite(ItemSpriteSheet.CROWN, null));
		float buttonHeight = NAV_BUTTON_HEIGHT;
		float buttonWidth = normalRankings.reqWidth() + 6;
		normalRankings.setRect(
				insets.left,
				insets.top + height - buttonHeight - 2,
				buttonWidth,
				buttonHeight);
		add(normalRankings);

		Rankings.INSTANCE.load();
		ArrayList<Rankings.Record> records = Rankings.INSTANCE.heroHallRecords();
		float listTop = insets.top + 24;
		float listBottom = normalRankings.top() - 3;
		float listHeight = Math.max(1, listBottom - listTop);
		float listWidth = Math.min(MAX_ROW_WIDTH + 3, width - GAP * 2);
		float listLeft = insets.left + (width - listWidth) / 2f;

		if (records.isEmpty()) {
			RenderedTextBlock noRecords = renderTextBlock(Messages.get(this, "no_games"), 8);
			noRecords.maxWidth((int) Math.max(1, width - GAP * 2));
			noRecords.hardlight(0xCCCCCC);
			noRecords.setPos(
					insets.left + (width - noRecords.width()) / 2f,
					listTop + (listHeight - noRecords.height()) / 2f);
			align(noRecords);
			add(noRecords);
		} else {
			Component content = new Component();
			ArrayList<RankingsScene.Record> rows = new ArrayList<>();
			ScrollPane pane = new ScrollPane(content) {
				@Override
				public void onClick(float x, float y) {
					for (int i = 0; i < rows.size(); i++) {
						RankingsScene.Record row = rows.get(i);
						if (row.inside(x, y)) {
							HeroHallScene.this.addToFront(new WndRanking(records.get(i), true));
							return;
						}
					}
				}
			};
			float rowWidth = listWidth - 3;
			float contentHeight = 0;
			for (int i = 0; i < records.size(); i++) {
				RankingsScene.Record row = new ScrollableRecord(i, records.get(i));
				row.setRect(0, contentHeight, rowWidth, ROW_HEIGHT);
				content.add(row);
				rows.add(row);
				contentHeight += ROW_HEIGHT + ROW_GAP;
			}
			contentHeight -= ROW_GAP;
			content.setSize(rowWidth, Math.max(listHeight, contentHeight));
			add(pane);
			pane.setRect(listLeft, listTop, listWidth, listHeight);
			pane.scrollTo(0, 0);
		}

		ExitButton exit = new ExitButton();
		exit.setPos(Camera.main.width - exit.width() - insets.right, insets.top);
		add(exit);

		fadeIn();
	}

	private static class ScrollableRecord extends RankingsScene.Record {

		private ScrollableRecord(int position, Rankings.Record record) {
			super(position, false, record, true);
			hotArea.active = false;
		}

		@Override
		public void update() {
			super.update();
			hotArea.active = false;
		}
	}

	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchNoFade(RankingsScene.class);
	}
}
