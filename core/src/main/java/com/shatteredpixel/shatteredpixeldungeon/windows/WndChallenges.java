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

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingListPane;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WndChallenges extends Window {

	private final int WIDTH = 115;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;

	private boolean editable;
	private ArrayList<IconButton> infos = new ArrayList<>();
	private ArrayList<ConduitBox> boxes;

	public WndChallenges( int checked, boolean editable ) {

		super();

		this.editable = editable;

		OrderedMap<String, Integer> challenges = new OrderedMap<>();
		for(int i=0;i<Challenges.MASKS.length;i++){
			challenges.put(Challenges.NAME_IDS[i], Challenges.MASKS[i]);
		}
		int HEIGHT = Math.min((challenges.size + 1) * (BTN_HEIGHT + GAP),
				(int) (PixelScene.uiCamera.height * 0.8));
		resize(WIDTH, HEIGHT);

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2
		);
		PixelScene.align(title);
		add( title );

		boxes = new ArrayList<>();

		float pos = 2;
		int i = 0;

		OrderedMap<String, Integer> finalChallenges = challenges;
		ScrollPane pane = new ScrollPane(new Component()) {
			@Override
			public void onClick(float x, float y) {
				int size = boxes.size();
				if (editable) {
					for (int i = 0; i < size; i++) {
						if (boxes.get(i).onClick(x, y)) break;
					}

				}

				size = infos.size();
				for (int i = 0; i < size; i++) {
					if (infos.get(i).inside(x, y)) {
						String challenge = finalChallenges.keys().toArray().get(i);

						ShatteredPixelDungeon.scene().add(
								new WndTitledMessage(Icons.get(Icons.CHALLENGE_COLOR),
										Messages.titleCase(Messages.get(Challenges.class, challenge)),
										Messages.get(Challenges.class, challenge+"_desc"))
						);

						break;
					}
				}
			}
		};
		add(pane);
		pane.setRect(0, title.bottom()+2, WIDTH, HEIGHT - title.bottom() - 2);
		Component content = pane.content();

		for (ObjectMap.Entry<String, Integer> chal : challenges.entries()) {

			final String challenge = chal.key;
			String chaltitle = Messages.titleCase(Messages.get(Challenges.class, challenge));

			ConduitBox cb = new ConduitBox( chaltitle );
			cb.checked( (checked & chal.value) != 0 );
			cb.active = editable;
			if (chal.value == Challenges.HARSH_ENVIRONMENT){
				cb.textColor(0xFFA500);
			}
			if (chal.value == Challenges.EXTREME_ENVIRONMENT){
				cb.textColor(0xFF0000);
			}

			if (++i > 0) {
				pos += GAP;
			}
			cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

			content.add( cb );
			boxes.add( cb );

			IconButton info = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void layout() {
					super.layout();
					hotArea.y = -5000;
				}
			};
			info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
			content.add(info);
			infos.add(info);

			pos = cb.bottom();
		}

		content.setSize(WIDTH, pos);
	}

	@Override
	public void onBackPressed() {

		if (editable) {
			int value = 0;
			int mark = 0 ;
			boolean mark1 = false;
			for (int i=0; i < boxes.size(); i++) {
				if (boxes.get( i ).checked()) {
					value |= Challenges.MASKS[i];
					mark = i;
					if(i < 2){
						mark1 = true;
					}
					if (i == 1){
						value |= Challenges.MASKS[i-1];
					}

				}
			}
			if(mark1 && mark <  2){
				value |= Challenges.MASKS[Random.Int(Challenges.MASKS.length-2)+2];
			}
			SPDSettings.challenges( value );
		}

		super.onBackPressed();
	}

	public class ConduitBox extends CheckBox{

		public ConduitBox(String label) {
			super(label);
		}

		@Override
		protected void onClick() {
			super.onClick();
		}

		protected boolean onClick(float x, float y) {
			if (!inside(x, y) || !editable) return false;
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
			onClick();
			return true;
		}

		@Override
		protected void layout() {
			super.layout();
			hotArea.width = hotArea.height = 0;
		}
	}
}
/*
public class WndChallenges extends Window {

	private static final int WIDTH		= 120;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;


	private boolean editable;
	private ArrayList<CheckBox> boxes;
	public WndChallenges( int checked, boolean editable ) {

		super();

		this.editable = editable;

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2
		);
		PixelScene.align(title);
		add( title );

		boxes = new ArrayList<>();

		float pos = TTL_HEIGHT;
		for (int i=0; i < Challenges.NAME_IDS.length; i++) {

			final String challenge = Challenges.NAME_IDS[i];

			CheckBox cb = new CheckBox( Messages.titleCase(Messages.get(Challenges.class, challenge)) );
			cb.checked( (checked & Challenges.MASKS[i]) != 0 );
			cb.active = editable;

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
					ShatteredPixelDungeon.scene().add(
							new WndMessage(Messages.get(Challenges.class, challenge+"_desc"))
					);
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

		if (editable) {
			int value = 0;
			for (int i=0; i < boxes.size(); i++) {
				if (boxes.get( i ).checked()) {
					value |= Challenges.MASKS[i];
				}
			}
			SPDSettings.challenges( value );
		}

		super.onBackPressed();
	}
}

 */



