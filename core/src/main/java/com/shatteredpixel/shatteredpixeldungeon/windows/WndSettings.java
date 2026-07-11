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

import static com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.seamlessResetScene;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.custom.seedfinder.SeedFinder;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.SaveManager;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.services.cloud.CloudSyncService;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.GameLog;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class WndSettings extends WndTabbed {

	private static final int WIDTH_P	    = 122;
	private static final int WIDTH_L	    = 223;

	private static final int SLIDER_HEIGHT	= 21;
	private static final int BTN_HEIGHT	    = 16;
	private static final float GAP          = 1;

	private DisplayTab  display;
	private UITab       ui;
	private InputTab    input;
	private DataTab     data;
	private AudioTab    audio;
	private LangsTab    langs;

	public static int last_index = 0;

	public WndSettings() {
		super();

		float height;

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		display = new DisplayTab();
		display.setSize(width, 0);
		height = display.height();
		add( display );

		add( new IconTab(Icons.get(Icons.DISPLAY)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				display.visible = display.active = value;
				if (value) last_index = 0;
			}
		});

		ui = new UITab();
		ui.setSize(width, 0);
		height = Math.max(height, ui.height());
		add( ui );

		add( new IconTab(Icons.get(Icons.PREFS)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				ui.visible = ui.active = value;
				if (value) last_index = 1;
			}
		});

		input = new InputTab();
		input.setSize(width, 0);
		height = Math.max(height, input.height());

		if (DeviceCompat.hasHardKeyboard() || ControllerHandler.isControllerConnected()) {
			add( input );
			Image icon;
			if (ControllerHandler.controllerActive || !DeviceCompat.hasHardKeyboard()){
				icon = Icons.get(Icons.CONTROLLER);
			} else {
				icon = Icons.get(Icons.KEYBOARD);
			}
			add(new IconTab(icon) {
				@Override
				protected void select(boolean value) {
					super.select(value);
					input.visible = input.active = value;
					if (value) last_index = 2;
				}
			});
		}

		data = new DataTab();
		data.setSize(width, 0);
		height = Math.max(height, data.height());
		add( data );

		add( new IconTab(Icons.get(Icons.DATA)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				data.visible = data.active = value;
				if (value) last_index = 3;
			}
		});

		audio = new AudioTab();
		audio.setSize(width, 0);
		height = Math.max(height, audio.height());
		add( audio );

		add( new IconTab(Icons.get(Icons.AUDIO)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				audio.visible = audio.active = value;
				if (value) last_index = 4;
			}
		});

		langs = new LangsTab();
		langs.setSize(width, 0);
		height = Math.max(height, langs.height());
		add( langs );


		IconTab langsTab = new IconTab(Icons.get(Icons.LANGS)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				langs.visible = langs.active = value;
				if (value) last_index = 5;
			}

			@Override
			protected void createChildren() {
				super.createChildren();
				switch(Messages.lang().status()){
					case X_UNFINISH:
						icon.hardlight(1.5f, 0, 0);
						break;
					case __UNREVIEW:
						icon.hardlight(1.5f, 0.75f, 0f);
						break;
				}
			}

		};
		add( langsTab );

		resize(width, (int)Math.ceil(height));

		layoutTabs();

		if (tabs.size() == 5 && last_index >= 3){
			//input tab isn't visible
			select(last_index-1);
		} else {
			select(last_index);
		}

	}

	@Override
	public void hide() {
		super.hide();
		//resets generators because there's no need to retain chars for languages not selected
		seamlessResetScene(new Game.SceneChangeCallback() {
			@Override
			public void beforeCreate() {
				Game.platform.resetGenerators();
			}
			@Override
			public void afterCreate() {
				//do nothing
			}
		});
	}

	private static class DisplayTab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		CheckBox chkFullscreen;
		OptionSlider optScale;
		CheckBox chkSaver;
		RedButton btnOrientation;
		ColorBlock sep2;
		OptionSlider optBrightness;
		OptionSlider optVisGrid;
		OptionSlider optFollowIntensity;
		OptionSlider optScreenShake;
		CheckBox chkCharAnimations;


		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			chkFullscreen = new CheckBox( Messages.get(this, "fullscreen") ) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.fullscreen(checked());
				}
			};
			if (Game.platform.supportsFullScreen()){
				chkFullscreen.checked(SPDSettings.fullscreen());
			} else {
				chkFullscreen.checked(true);
				chkFullscreen.enable(false);
			}
			add(chkFullscreen);

			//power saver is being slowly phased out, only show it on old (4.3-) android devices
			// this is being phased out as the setting is useless on all but very old devices anyway
			// and support is going to be dropped for 4.3- in the forseeable future
			if (DeviceCompat.isAndroid() && PixelScene.maxScreenZoom >= 2
				&& (SPDSettings.powerSaver() || !DeviceCompat.supportsFullScreen())) {
				chkSaver = new CheckBox(Messages.get(this, "saver")) {
					@Override
					protected void onClick() {
						super.onClick();
						if (checked()) {
							checked(!checked());
							ShatteredPixelDungeon.scene().add(new WndOptions(Icons.get(Icons.DISPLAY),
									Messages.get(DisplayTab.class, "saver"),
									Messages.get(DisplayTab.class, "saver_desc"),
									Messages.get(DisplayTab.class, "okay"),
									Messages.get(DisplayTab.class, "cancel")) {
								@Override
								protected void onSelect(int index) {
									if (index == 0) {
										checked(!checked());
										SPDSettings.powerSaver(checked());
									}
								}
							});
						} else {
							SPDSettings.powerSaver(checked());
						}
					}
				};
				chkSaver.checked( SPDSettings.powerSaver() );
				add( chkSaver );
			}

			if (DeviceCompat.isAndroid()) {
				Boolean landscape = SPDSettings.landscape();
				if (landscape == null){
					landscape = Game.width > Game.height;
				}
				Boolean finalLandscape = landscape;
				btnOrientation = new RedButton(finalLandscape ?
						Messages.get(this, "portrait")
						: Messages.get(this, "landscape")) {
					@Override
					protected void onClick() {
						SPDSettings.landscape(!finalLandscape);
					}
				};
				add(btnOrientation);
			}

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);

			optBrightness = new OptionSlider(Messages.get(this, "brightness"),
					Messages.get(this, "dark"), Messages.get(this, "bright"), -1, 1) {
				@Override
				protected void onChange() {
					SPDSettings.brightness(getSelectedValue());
				}
			};
			optBrightness.setSelectedValue(SPDSettings.brightness());
			add(optBrightness);

			optVisGrid = new OptionSlider(Messages.get(this, "visual_grid"),
					Messages.get(this, "off"), Messages.get(this, "high"), -1, 2) {
				@Override
				protected void onChange() {
					SPDSettings.visualGrid(getSelectedValue());
				}
			};
			optVisGrid.setSelectedValue(SPDSettings.visualGrid());
			add(optVisGrid);

			optFollowIntensity = new OptionSlider(Messages.get(this, "camera_follow"),
					Messages.get(this, "low"), Messages.get(this, "high"), 1, 4) {
				@Override
				protected void onChange() {
					SPDSettings.cameraFollow(getSelectedValue());
				}
			};
			optFollowIntensity.setSelectedValue(SPDSettings.cameraFollow());
			add(optFollowIntensity);

			optScreenShake = new OptionSlider(Messages.get(this, "screenshake"),
					Messages.get(this, "off"), Messages.get(this, "high"), 0, 4) {
				@Override
				protected void onChange() {
					SPDSettings.screenShake(getSelectedValue());
				}
			};
			optScreenShake.setSelectedValue(SPDSettings.screenShake());
			add(optScreenShake);

			chkCharAnimations = new CheckBox(Messages.get(this, "char_animations")) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.charAnimations(checked());
				}
			};
			chkCharAnimations.checked(SPDSettings.charAnimations());
			add(chkCharAnimations);

		}

		@Override
		protected void layout() {

			float bottom = y;

			title.setPos((width - title.width())/2, bottom + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 3*GAP;

			bottom = sep1.y + 1;

			if (width > 200 && chkSaver != null) {
				chkFullscreen.setRect(0, bottom + GAP, width/2-1, BTN_HEIGHT);
				chkSaver.setRect(chkFullscreen.right()+ GAP, bottom + GAP, width/2-1, BTN_HEIGHT);
				bottom = chkFullscreen.bottom();
			} else {
				chkFullscreen.setRect(0, bottom + GAP, width, BTN_HEIGHT);
				bottom = chkFullscreen.bottom();

				if (chkSaver != null) {
					chkSaver.setRect(0, bottom + GAP, width, BTN_HEIGHT);
					bottom = chkSaver.bottom();
				}
			}

			if (btnOrientation != null) {
				btnOrientation.setRect(0, bottom + GAP, width, BTN_HEIGHT);
				bottom = btnOrientation.bottom();
			}

			if (optScale != null){
				optScale.setRect(0, bottom + GAP, width, SLIDER_HEIGHT);
				bottom = optScale.bottom();
			}

			sep2.size(width, 1);
			sep2.y = bottom + GAP;
			bottom = sep2.y + 1;

			if (width > 200){
				optBrightness.setRect(0, bottom + GAP, width/2-GAP/2, SLIDER_HEIGHT);
				optVisGrid.setRect(optBrightness.right() + GAP, optBrightness.top(), width/2-GAP/2, SLIDER_HEIGHT);

				optFollowIntensity.setRect(0, optVisGrid.bottom() + GAP, width/2-GAP/2, SLIDER_HEIGHT);
				optScreenShake.setRect(optFollowIntensity.right() + GAP, optFollowIntensity.top(), width/2-GAP/2, SLIDER_HEIGHT);
				chkCharAnimations.setRect(0, optScreenShake.bottom() + GAP, width, BTN_HEIGHT);

			} else {
				optBrightness.setRect(0, bottom + GAP, width, SLIDER_HEIGHT);
				optVisGrid.setRect(0, optBrightness.bottom() + GAP, width, SLIDER_HEIGHT);

				optFollowIntensity.setRect(0, optVisGrid.bottom() + GAP, width, SLIDER_HEIGHT);
				optScreenShake.setRect(0, optFollowIntensity.bottom() + GAP, width, SLIDER_HEIGHT);
				chkCharAnimations.setRect(0, optScreenShake.bottom() + GAP, width, BTN_HEIGHT);

			}

			height = chkCharAnimations.bottom();
		}

	}

	private static class UITab extends Component {

		RenderedTextBlock title;

		ColorBlock sep1;
		OptionSlider optUIMode;
		OptionSlider optUIScale;
		RedButton btnToolbarSettings;
		RedButton btnUIStyle;
		CheckBox chkFlipTags;
		ColorBlock sep2;
		CheckBox chkFont;
		CheckBox chkVibrate;
		RedButton optScreenHunger;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			//add slider for UI size only if device has enough space to support it
			float wMin = Game.width / PixelScene.MIN_WIDTH_FULL;
			float hMin = Game.height / PixelScene.MIN_HEIGHT_FULL;
			if (Math.min(wMin, hMin) >= 2*Game.density){
				optUIMode = new OptionSlider(
						Messages.get(this, "ui_mode"),
						Messages.get(this, "mobile"),
						Messages.get(this, "full"),
						0,
						2
				) {
					@Override
					protected void onChange() {
						SPDSettings.interfaceSize(getSelectedValue());
						seamlessResetScene();
					}
				};
				optUIMode.setSelectedValue(SPDSettings.interfaceSize());
				add(optUIMode);
			}

			if ((int)Math.ceil(2* Game.density) < PixelScene.maxDefaultZoom) {
				optUIScale = new OptionSlider(Messages.get(this, "scale"),
						(int)Math.ceil(2* Game.density)+ "X",
						PixelScene.maxDefaultZoom + "X",
						(int)Math.ceil(2* Game.density),
						PixelScene.maxDefaultZoom ) {
					@Override
					protected void onChange() {
						if (getSelectedValue() != SPDSettings.scale()) {
							SPDSettings.scale(getSelectedValue());
							seamlessResetScene();
						}
					}
				};
				optUIScale.setSelectedValue(PixelScene.defaultZoom);
				add(optUIScale);
			}

			if (SPDSettings.interfaceSize() == 0) {
				btnToolbarSettings = new RedButton(Messages.get(this, "toolbar_settings"), 9){
					@Override
					protected void onClick() {
						ShatteredPixelDungeon.scene().addToFront(new Window(){

							RenderedTextBlock barDesc;
							RedButton btnSplit; RedButton btnGrouped; RedButton btnCentered;
							CheckBox chkQuickSwapper;
							RenderedTextBlock swapperDesc;
							CheckBox chkFlipToolbar;
							CheckBox chkFlipTags;

							{
								barDesc = PixelScene.renderTextBlock(Messages.get(WndSettings.UITab.this, "mode"), 9);
								add(barDesc);

								btnSplit = new RedButton(Messages.get(WndSettings.UITab.this, "split")) {
									@Override
									protected void onClick() {
										textColor(TITLE_COLOR);
										btnGrouped.textColor(WHITE);
										btnCentered.textColor(WHITE);
										SPDSettings.toolbarMode(Toolbar.Mode.SPLIT.name());
										Toolbar.updateLayout();
									}
								};
								if (SPDSettings.toolbarMode().equals(Toolbar.Mode.SPLIT.name())) {
									btnSplit.textColor(TITLE_COLOR);
								}
								add(btnSplit);

								btnGrouped = new RedButton(Messages.get(WndSettings.UITab.this, "group")) {
									@Override
									protected void onClick() {
										btnSplit.textColor(WHITE);
										textColor(TITLE_COLOR);
										btnCentered.textColor(WHITE);
										SPDSettings.toolbarMode(Toolbar.Mode.GROUP.name());
										Toolbar.updateLayout();
									}
								};
								if (SPDSettings.toolbarMode().equals(Toolbar.Mode.GROUP.name())) {
									btnGrouped.textColor(TITLE_COLOR);
								}
								add(btnGrouped);

								btnCentered = new RedButton(Messages.get(WndSettings.UITab.this, "center")) {
									@Override
									protected void onClick() {
										btnSplit.textColor(WHITE);
										btnGrouped.textColor(WHITE);
										textColor(TITLE_COLOR);
										SPDSettings.toolbarMode(Toolbar.Mode.CENTER.name());
										Toolbar.updateLayout();
									}
								};
								if (SPDSettings.toolbarMode().equals(Toolbar.Mode.CENTER.name())) {
									btnCentered.textColor(TITLE_COLOR);
								}
								add(btnCentered);

								chkQuickSwapper = new CheckBox(Messages.get(WndSettings.UITab.this, "quickslot_swapper")) {
									@Override
									protected void onClick() {
										super.onClick();
										SPDSettings.quickSwapper(checked());
										Toolbar.updateLayout();
									}
								};
								chkQuickSwapper.checked(SPDSettings.quickSwapper());
								add(chkQuickSwapper);

								swapperDesc = PixelScene.renderTextBlock(Messages.get(WndSettings.UITab.this, "swapper_desc"), 5);
								swapperDesc.hardlight(0x888888);
								add(swapperDesc);

								chkFlipToolbar = new CheckBox(Messages.get(WndSettings.UITab.this, "flip_toolbar")) {
									@Override
									protected void onClick() {
										super.onClick();
										SPDSettings.flipToolbar(checked());
										Toolbar.updateLayout();
									}
								};
								chkFlipToolbar.checked(SPDSettings.flipToolbar());
								add(chkFlipToolbar);

								chkFlipTags = new CheckBox(Messages.get(WndSettings.UITab.this, "flip_indicators")){
									@Override
									protected void onClick() {
										super.onClick();
										SPDSettings.flipTags(checked());
										GameScene.layoutTags();
									}
								};
								chkFlipTags.checked(SPDSettings.flipTags());
								add(chkFlipTags);

								//layout
								resize(WIDTH_P, 0);

								barDesc.setPos((width - barDesc.width()) / 2f, GAP);
								PixelScene.align(barDesc);

								int btnWidth = (int) (width - 2 * GAP) / 3;
								btnSplit.setRect(0, barDesc.bottom() + GAP, btnWidth, BTN_HEIGHT-2);
								btnGrouped.setRect(btnSplit.right() + GAP, btnSplit.top(), btnWidth, BTN_HEIGHT-2);
								btnCentered.setRect(btnGrouped.right() + GAP, btnSplit.top(), btnWidth, BTN_HEIGHT-2);

								chkQuickSwapper.setRect(0, btnGrouped.bottom() + GAP, width, BTN_HEIGHT);

								swapperDesc.maxWidth(width);
								swapperDesc.setPos(0, chkQuickSwapper.bottom()+1);

								if (width > 200) {
									chkFlipToolbar.setRect(0, swapperDesc.bottom() + GAP, width / 2 - 1, BTN_HEIGHT);
									chkFlipTags.setRect(chkFlipToolbar.right() + GAP, chkFlipToolbar.top(), width / 2 - 1, BTN_HEIGHT);
								} else {
									chkFlipToolbar.setRect(0, swapperDesc.bottom() + GAP, width, BTN_HEIGHT);
									chkFlipTags.setRect(0, chkFlipToolbar.bottom() + GAP, width, BTN_HEIGHT);
								}

								resize(WIDTH_P, (int)chkFlipTags.bottom());

							}
						});
					}
				};
				add(btnToolbarSettings);

			} else {

				chkFlipTags = new CheckBox(Messages.get(this, "flip_indicators")) {
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.flipTags(checked());
						GameScene.layoutTags();
					}
				};
				chkFlipTags.checked(SPDSettings.flipTags());
				add(chkFlipTags);

			}

			btnUIStyle = new RedButton(Messages.get(this, "ui_style_settings"), 9) {
				@Override
				protected void onClick() {
					SPDSettings.UIStyle current = SPDSettings.uiStyle();
					String currentName = Messages.get(WndSettings.UITab.class,
							current == SPDSettings.UIStyle.SPD ? "spd_style" : "transformation_style");
					ShatteredPixelDungeon.scene().addToFront(new WndOptions(
							Messages.get(WndSettings.UITab.class, "ui_style_settings"),
							Messages.get(WndSettings.UITab.class, "ui_style_prompt", currentName),
							Messages.get(WndSettings.UITab.class, "transformation_style"),
							Messages.get(WndSettings.UITab.class, "spd_style")) {
						@Override
						protected void onSelect(int index) {
							SPDSettings.UIStyle selected = index == 1
									? SPDSettings.UIStyle.SPD
									: SPDSettings.UIStyle.TRANSFORMATION;
							if (selected != SPDSettings.uiStyle()) {
								seamlessResetScene(new Game.SceneChangeCallback() {
									@Override
									public void beforeCreate() {
										SPDSettings.uiStyle(selected);
									}

									@Override
									public void afterCreate() {
										// The rebuilt scene has already loaded the selected UI assets.
									}
								});
							}
						}
					});
				}
			};
			add(btnUIStyle);

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);

			chkFont = new CheckBox(Messages.get(this, "system_font")){
				@Override
				protected void onClick() {
					super.onClick();
					seamlessResetScene(new Game.SceneChangeCallback() {
						@Override
						public void beforeCreate() {
							SPDSettings.systemFont(checked());
						}

						@Override
						public void afterCreate() {
							//do nothing
						}
					});
				}
			};
			chkFont.checked(SPDSettings.systemFont());
			add(chkFont);

			chkVibrate = new CheckBox(Messages.get(this, "vibration")){
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.vibration(checked());
					if (checked()){
						Game.vibrate(250);
					}
				}
			};
			chkVibrate.enable(Game.platform.supportsVibration());
			if (chkVibrate.active) {
				chkVibrate.checked(SPDSettings.vibration());
			}
			add(chkVibrate);
			optScreenHunger = new RedButton(Messages.get(this, "hunger")) {
				@Override
				protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront(new WndOptions(Messages.get(WndSettings.UITab.class, "hunger_prompt"),
							Messages.get(WndSettings.UITab.class, "hunger_warning"),
							Messages.get(WndSettings.UITab.class, "tpd"),
							Messages.get(WndSettings.UITab.class, "tpd1"),
							Messages.get(WndSettings.UITab.class, "tpd2"),
							Messages.get(WndSettings.UITab.class, "mc"),
							Messages.get(WndSettings.UITab.class, "energy"),
                            Messages.get(WndSettings.UITab.class, "hide")){
						@Override
						protected void onSelect(int index) {
							SPDSettings.hunger(index);
                            seamlessResetScene();
						}
						@Override
						public void onBackPressed() {
							super.onBackPressed();
						}
					});
				}
			};
			add(optScreenHunger);
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 3*GAP;

			height = sep1.y + 1;

			if (optUIMode != null && optUIScale != null && width > 200){
				optUIMode.setRect(0, height + GAP, width/2-1, SLIDER_HEIGHT);
				optUIScale.setRect(width/2+1, height + GAP, width/2-1, SLIDER_HEIGHT);
				height = optUIScale.bottom();
			} else {
				if (optUIMode != null) {
					optUIMode.setRect(0, height + GAP, width, SLIDER_HEIGHT);
					height = optUIMode.bottom();
				}

				if (optUIScale != null) {
					optUIScale.setRect(0, height + GAP, width, SLIDER_HEIGHT);
					height = optUIScale.bottom();
				}
			}

			if (btnToolbarSettings != null) {
				btnToolbarSettings.setRect(0, height + GAP, width, BTN_HEIGHT);
				height = btnToolbarSettings.bottom();
			} else {
				chkFlipTags.setRect(0, height + GAP, width, BTN_HEIGHT);
				height = chkFlipTags.bottom();
			}
			btnUIStyle.setRect(0, height + GAP, width, BTN_HEIGHT);
			height = btnUIStyle.bottom();
			optScreenHunger.setRect(0, height + GAP, width, BTN_HEIGHT);

			sep2.size(width, 1);
			sep2.y = optScreenHunger.bottom() + GAP;

			if (width > 200) {
				chkFont.setRect(0, sep2.y + 1 + GAP, width/2-1, BTN_HEIGHT);
				chkVibrate.setRect(chkFont.right()+2, chkFont.top(), width/2-1, BTN_HEIGHT);
				height = chkVibrate.bottom();

			} else {
				chkFont.setRect(0, sep2.y + 1 + GAP, width, BTN_HEIGHT);
				chkVibrate.setRect(0, chkFont.bottom() + GAP, width, BTN_HEIGHT);
				height = chkVibrate.bottom();
			}

		}

	}

	private static class InputTab extends Component{

		RenderedTextBlock title;
		ColorBlock sep1;

		RedButton btnKeyBindings;
		RedButton btnControllerBindings;

		ColorBlock sep2;

		OptionSlider optControlSens;
		OptionSlider optHoldMoveSens;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			if (DeviceCompat.hasHardKeyboard()){

				btnKeyBindings = new RedButton(Messages.get(this, "key_bindings")){
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().addToFront(new WndKeyBindings(false));
					}
				};

				add(btnKeyBindings);
			}

			if (ControllerHandler.isControllerConnected()){
				btnControllerBindings = new RedButton(Messages.get(this, "controller_bindings")){
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().addToFront(new WndKeyBindings(true));
					}
				};

				add(btnControllerBindings);
			}

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);


			optControlSens = new OptionSlider(
					Messages.get(this, "controller_sensitivity"),
					"1",
					"10",
					1,
					10
			) {
				@Override
				protected void onChange() {
					SPDSettings.controllerPointerSensitivity(getSelectedValue());
				}
			};
			optControlSens.setSelectedValue(SPDSettings.controllerPointerSensitivity());
			add(optControlSens);

			optHoldMoveSens = new OptionSlider(
					Messages.get(this, "movement_sensitivity"),
					Messages.get(this, "off"),
					Messages.get(this, "high"),
					0,
					4
			) {
				@Override
				protected void onChange() {
					SPDSettings.movementHoldSensitivity(getSelectedValue());
				}
			};
			optHoldMoveSens.setSelectedValue(SPDSettings.movementHoldSensitivity());
			add(optHoldMoveSens);
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 3*GAP;

			height = sep1.y+1;

			if (width > 200 && btnKeyBindings != null && btnControllerBindings != null){
				btnKeyBindings.setRect(0, height + GAP, width/2-1, BTN_HEIGHT);
				btnControllerBindings.setRect(width/2+1, height + GAP, width/2-1, BTN_HEIGHT);
				height = btnControllerBindings.bottom();
			} else {
				if (btnKeyBindings != null) {
					btnKeyBindings.setRect(0, height + GAP, width, BTN_HEIGHT);
					height = btnKeyBindings.bottom();
				}

				if (btnControllerBindings != null) {
					btnControllerBindings.setRect(0, height + GAP, width, BTN_HEIGHT);
					height = btnControllerBindings.bottom();
				}
			}

			sep2.size(width, 1);
			sep2.y = height+ GAP;

			if (width > 200){
				optControlSens.setRect(0, sep2.y + 1 + GAP, width/2-1, SLIDER_HEIGHT);
				optHoldMoveSens.setRect(width/2 + 1, optControlSens.top(), width/2 -1, SLIDER_HEIGHT);
			} else {
				optControlSens.setRect(0, sep2.y + 1 + GAP, width, SLIDER_HEIGHT);
				optHoldMoveSens.setRect(0, optControlSens.bottom() + GAP, width, SLIDER_HEIGHT);
			}

			height = optHoldMoveSens.bottom();

		}
	}

	private static class DataTab extends Component{

		RenderedTextBlock title;
		ColorBlock sep1;
		RedButton btnUploadData;
		RedButton btnSyncData;
		RedButton btnCloudUUID;
		CheckBox chkUpdates;
		CheckBox chkBetas;
		RedButton btnExportData;
		RedButton btnImportData;
		ColorBlock sep2;
		OptionSlider optSeedThreshold;
		OptionSlider optSeedTime;
		OptionSlider optSeedAlgorithm;
		RenderedTextBlock txtSeedThreshold;
		RenderedTextBlock txtSeedTime;
		RenderedTextBlock txtSeedAlgorithm;
		CheckBox chkSeedThreads;
		private static final int[] SEED_TIME_LIMITS = {60, 300, 600, 1800, 3600};
		private static final String[] SEED_TIME_NAMES = {"1min", "5min", "10min", "30min", "1h"};

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			btnUploadData = new RedButton(Messages.get(this, "upload_data")){
				@Override
				protected void onClick() {
					super.onClick();
					enable(false);
					text(Messages.get(DataTab.class, "cloud_working"));
					CloudSyncService.uploadLocalData(new CloudSyncService.Callback() {
						@Override
						public void onSuccess() {
							enable(true);
							text(Messages.get(DataTab.class, "upload_data"));
							updateCloudUUIDText();
							parent.add(new WndMessage(Messages.get(DataTab.class, "upload_success")));
						}

						@Override
						public void onFailure() {
							enable(true);
							text(Messages.get(DataTab.class, "upload_data"));
							parent.add(new WndMessage(Messages.get(DataTab.class, "upload_failed")));
						}
					});
				}
			};
			btnUploadData.icon(Icons.get(Icons.COPY));
			add(btnUploadData);

			btnCloudUUID = new RedButton("") {
				@Override
				protected void onClick() {
					super.onClick();
					String uuid = CloudSyncService.currentPlayerUUID();
					if (uuid == null || uuid.isEmpty()) {
						parent.add(new WndMessage(Messages.get(DataTab.class, "cloud_uuid_missing")));
					} else {
						Gdx.app.getClipboard().setContents(uuid);
						parent.add(new WndMessage(Messages.get(DataTab.class, "cloud_uuid_copied")));
					}
				}
			};
			btnCloudUUID.icon(Icons.get(Icons.COPY));
			add(btnCloudUUID);
			updateCloudUUIDText();

			if (Updates.supportsUpdates() && Updates.supportsUpdatePrompts()) {
				chkUpdates = new CheckBox(Messages.get(this, "updates")) {
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.updates(checked());
						Updates.clearUpdate();
					}
				};
				chkUpdates.checked(SPDSettings.updates());
				add(chkUpdates);

				if (Updates.supportsBetaChannel()){
					chkBetas = new CheckBox(Messages.get(this, "betas")) {
						@Override
						protected void onClick() {
							super.onClick();
							SPDSettings.betas(checked());
							Updates.clearUpdate();
						}
					};
					chkBetas.checked(SPDSettings.betas());
					add(chkBetas);
				}
			}

			btnSyncData = new RedButton(Messages.get(this, "sync_data")){
				@Override
				protected void onClick() {
					super.onClick();
					enable(false);
					text(Messages.get(DataTab.class, "cloud_working"));
					CloudSyncService.syncServerData(new CloudSyncService.Callback() {
						@Override
						public void onSuccess() {
							enable(true);
							text(Messages.get(DataTab.class, "sync_data"));
							updateCloudUUIDText();
							parent.add(new WndMessage(Messages.get(DataTab.class, "sync_success")));
						}

						@Override
						public void onFailure() {
							enable(true);
							text(Messages.get(DataTab.class, "sync_data"));
							parent.add(new WndMessage(Messages.get(DataTab.class, "sync_failed")));
						}
					});
				}
			};
			btnSyncData.icon(Icons.get(Icons.PASTE));
			//add(btnSyncData);

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);

			optSeedThreshold = new OptionSlider(Messages.get(this, "seed_threshold"), "10%", "100%", 1, 10) {
				@Override
				protected void onChange() {
					SPDSettings.seedFinderThreshold(getSelectedValue() * 10);
					updateSeedThresholdText();
				}
			};
			optSeedThreshold.setSelectedValue(Math.max(1, SPDSettings.seedFinderThreshold() / 10));
			add(optSeedThreshold);

			optSeedTime = new OptionSlider(Messages.get(this, "seed_time"), "1m", "1h", 0, SEED_TIME_LIMITS.length - 1) {
				@Override
				protected void onChange() {
					SPDSettings.seedFinderTimeLimit(SEED_TIME_LIMITS[getSelectedValue()]);
					updateSeedTimeText();
				}
			};
			optSeedTime.setSelectedValue(seedTimeIndex());
			add(optSeedTime);

			optSeedAlgorithm = new OptionSlider(Messages.get(this, "seed_algorithm"),
					Messages.get(SeedFinder.class, "algorithm_sequential"), Messages.get(SeedFinder.class, "algorithm_hill_climb"),
					0, SeedFinder.Algorithm.values().length - 1) {
				@Override
				protected void onChange() {
					SPDSettings.seedFinderAlgorithm(getSelectedValue());
					updateSeedAlgorithmText();
				}
			};
			optSeedAlgorithm.setSelectedValue(SPDSettings.seedFinderAlgorithm());
			add(optSeedAlgorithm);

			txtSeedThreshold = PixelScene.renderTextBlock(6);
			txtSeedThreshold.hardlight(0xBBBBBB);
			add(txtSeedThreshold);
			updateSeedThresholdText();

			txtSeedTime = PixelScene.renderTextBlock(6);
			txtSeedTime.hardlight(0xBBBBBB);
			add(txtSeedTime);
			updateSeedTimeText();

			txtSeedAlgorithm = PixelScene.renderTextBlock(6);
			txtSeedAlgorithm.hardlight(0xBBBBBB);
			add(txtSeedAlgorithm);
			updateSeedAlgorithmText();

			chkSeedThreads = new CheckBox(Messages.get(this, "seed_threads")){
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.seedFinderThreads(checked());
				}
			};
			chkSeedThreads.checked(SPDSettings.seedFinderThreads());
			add(chkSeedThreads);


			// 导出全局数据到剪贴板按钮
			btnExportData = new RedButton(Messages.get(this, "export_data")){
				@Override
				protected void onClick() {
					if (SaveManager.exportGlobalToClipboard()) {
						parent.add(new WndMessage(Messages.get(DataTab.class, "export_success")));
					} else {
						parent.add(new WndMessage(Messages.get(DataTab.class, "export_failed")));
					}
				}
			};
			btnExportData.icon(Icons.get(Icons.COPY));
			//add(btnExportData);

			// 从剪贴板导入全局数据按钮
			btnImportData = new RedButton(Messages.get(this, "import_data")){
				@Override
				protected void onClick() {
					if (SaveManager.importGlobalFromClipboard()) {
						parent.add(new WndMessage(Messages.get(DataTab.class, "import_success")));
					} else {
						parent.add(new WndMessage(Messages.get(DataTab.class, "import_failed")));
					}
				}
			};
			btnImportData.icon(Icons.get(Icons.PASTE));
			//add(btnImportData);
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 3*GAP;

			float pos;
			if (width > 200 && chkUpdates != null){
				btnUploadData.setRect(0, sep1.y + 1 + GAP, width/2-1, BTN_HEIGHT);
				chkUpdates.setRect(btnUploadData.right() + GAP, btnUploadData.top(), width/2-1, BTN_HEIGHT);
				pos = chkUpdates.bottom();
			} else {
				btnUploadData.setRect(0, sep1.y + 1 + GAP, width, BTN_HEIGHT);
				pos = btnUploadData.bottom();
				if (chkUpdates != null) {
					chkUpdates.setRect(0, btnUploadData.bottom() + GAP, width, BTN_HEIGHT);
					pos = chkUpdates.bottom();
				}
			}
			btnCloudUUID.setRect(0, pos + GAP, width, BTN_HEIGHT);
			pos = btnCloudUUID.bottom();

			if (chkBetas != null){
				chkBetas.setRect(0, pos + GAP, width, BTN_HEIGHT);
				pos = chkBetas.bottom();
			}

			//btnSyncData.setRect(0, pos + GAP, width, BTN_HEIGHT);
			//pos = btnSyncData.bottom();

			sep2.size(width, 1);
			sep2.y = pos + GAP;
			pos = sep2.y + 1;

			if (width > 200) {
				optSeedThreshold.setRect(0, pos + GAP, width/2 - GAP/2, SLIDER_HEIGHT);
				optSeedTime.setRect(width/2 + GAP/2, pos + GAP, width/2 - GAP/2, SLIDER_HEIGHT);
				txtSeedThreshold.maxWidth((int)(width/2 - GAP/2));
				txtSeedThreshold.setPos(0, optSeedThreshold.bottom() + GAP);
				txtSeedTime.maxWidth((int)(width/2 - GAP/2));
				txtSeedTime.setPos(width/2 + GAP/2, optSeedTime.bottom() + GAP);
				pos = Math.max(txtSeedThreshold.bottom(), txtSeedTime.bottom());

				optSeedAlgorithm.setRect(0, pos + GAP, width/2 - GAP/2, SLIDER_HEIGHT);
				txtSeedAlgorithm.maxWidth((int)(width/2 - GAP/2));
				txtSeedAlgorithm.setPos(width/2 + GAP/2, pos + GAP);
				chkSeedThreads.setRect(width/2 + GAP/2, txtSeedAlgorithm.bottom() + GAP, width/2 - GAP/2, BTN_HEIGHT);
				pos = Math.max(optSeedAlgorithm.bottom(), chkSeedThreads.bottom());
			} else {
				optSeedThreshold.setRect(0, pos + GAP, width, SLIDER_HEIGHT);
				txtSeedThreshold.maxWidth((int)width);
				txtSeedThreshold.setPos(0, optSeedThreshold.bottom() + GAP);
				optSeedTime.setRect(0, txtSeedThreshold.bottom() + GAP, width, SLIDER_HEIGHT);
				txtSeedTime.maxWidth((int)width);
				txtSeedTime.setPos(0, optSeedTime.bottom() + GAP);
				optSeedAlgorithm.setRect(0, txtSeedTime.bottom() + GAP, width, SLIDER_HEIGHT);
				txtSeedAlgorithm.maxWidth((int)width);
				txtSeedAlgorithm.setPos(0, optSeedAlgorithm.bottom() + GAP);
				chkSeedThreads.setRect(0, txtSeedAlgorithm.bottom() + GAP, width, BTN_HEIGHT);
				pos = chkSeedThreads.bottom();
			}

			// 导出导入按钮布局
			btnExportData.setRect(0, pos + GAP, width/2 - GAP/2, BTN_HEIGHT);
			btnImportData.setRect(width/2 + GAP/2, pos + GAP, width/2 - GAP/2, BTN_HEIGHT);
			pos = btnExportData.bottom();

			height = pos;

		}

		private int seedTimeIndex() {
			int timeLimit = SPDSettings.seedFinderTimeLimit();
			for (int i = 0; i < SEED_TIME_LIMITS.length; i++) {
				if (timeLimit <= SEED_TIME_LIMITS[i]) {
					return i;
				}
			}
			return SEED_TIME_LIMITS.length - 1;
		}

		private void updateSeedAlgorithmText() {
			if (txtSeedAlgorithm != null) {
				SeedFinder.Algorithm algorithm = SeedFinder.Algorithm.values()[SPDSettings.seedFinderAlgorithm()];
				txtSeedAlgorithm.text(Messages.get(this, "seed_selected") + Messages.get(SeedFinder.class, "algorithm_" + algorithm.name().toLowerCase()));
			}
		}

		private void updateSeedThresholdText() {
			if (txtSeedThreshold != null) {
				txtSeedThreshold.text(Messages.get(this, "seed_selected_threshold") + SPDSettings.seedFinderThreshold() + "%");
			}
		}

		private void updateSeedTimeText() {
			if (txtSeedTime != null) {
				txtSeedTime.text(Messages.get(this, "seed_selected_time") + SEED_TIME_NAMES[seedTimeIndex()]);
			}
		}

		private void updateCloudUUIDText() {
			if (btnCloudUUID != null) {
				String uuid = CloudSyncService.currentPlayerUUID();
				if (uuid == null || uuid.isEmpty()) {
					uuid = Messages.get(this, "cloud_uuid_unassigned");
				}
				btnCloudUUID.text(Messages.get(this, "cloud_uuid_copy") + uuid);
			}
		}
	}

	private static class AudioTab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		OptionSlider optMusic;
		CheckBox chkMusicMute;
		ColorBlock sep2;
		OptionSlider optSFX;
		CheckBox chkMuteSFX;
		ColorBlock sep3;
		CheckBox chkIgnoreSilent;
		CheckBox chkMusicBG;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			optMusic = new OptionSlider(Messages.get(this, "music_vol"), "0", "10", 0, 10) {
				@Override
				protected void onChange() {
					SPDSettings.musicVol(getSelectedValue());
				}
			};
			optMusic.setSelectedValue(SPDSettings.musicVol());
			add(optMusic);

			chkMusicMute = new CheckBox(Messages.get(this, "music_mute")){
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.music(!checked());
				}
			};
			chkMusicMute.checked(!SPDSettings.music());
			add(chkMusicMute);

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);

			optSFX = new OptionSlider(Messages.get(this, "sfx_vol"), "0", "10", 0, 10) {
				@Override
				protected void onChange() {
					SPDSettings.SFXVol(getSelectedValue());
					if (Random.Int(100) == 0){
						Sample.INSTANCE.play(Assets.Sounds.MIMIC);
					} else {
						Sample.INSTANCE.play(Random.oneOf(Assets.Sounds.GOLD,
								Assets.Sounds.HIT,
								Assets.Sounds.ITEM,
								Assets.Sounds.SHATTER,
								Assets.Sounds.EVOKE,
								Assets.Sounds.SECRET));
					}
				}
			};
			optSFX.setSelectedValue(SPDSettings.SFXVol());
			add(optSFX);

			chkMuteSFX = new CheckBox( Messages.get(this, "sfx_mute") ) {
				@Override
				protected void onClick() {
					super.onClick();
					SPDSettings.soundFx(!checked());
					Sample.INSTANCE.play( Assets.Sounds.CLICK );
				}
			};
			chkMuteSFX.checked(!SPDSettings.soundFx());
			add( chkMuteSFX );

			if (DeviceCompat.isiOS()){

				sep3 = new ColorBlock(1, 1, 0xFF000000);
				add(sep3);

				chkIgnoreSilent = new CheckBox( Messages.get(this, "ignore_silent") ){
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.ignoreSilentMode(checked());
					}
				};
				chkIgnoreSilent.checked(SPDSettings.ignoreSilentMode());
				add(chkIgnoreSilent);

			} else if (DeviceCompat.isDesktop()){

				sep3 = new ColorBlock(1, 1, 0xFF000000);
				add(sep3);

				chkMusicBG = new CheckBox( Messages.get(this, "music_bg") ){
					@Override
					protected void onClick() {
						super.onClick();
						SPDSettings.playMusicInBackground(checked());
					}
				};
				chkMusicBG.checked(SPDSettings.playMusicInBackground());
				add(chkMusicBG);
			}
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 3*GAP;

			if (width > 200) {
				optMusic.setRect(0, sep1.y + 1 + GAP, width/2-1, SLIDER_HEIGHT);
				chkMusicMute.setRect(0, optMusic.bottom() + GAP, width/2-1, BTN_HEIGHT);

				sep2.size(width, 1);
				sep2.y = sep1.y; //just have them overlap

				optSFX.setRect(optMusic.right()+2, sep2.y + 1 + GAP, width/2-1, SLIDER_HEIGHT);
				chkMuteSFX.setRect(chkMusicMute.right()+2, optSFX.bottom() + GAP, width/2-1, BTN_HEIGHT);

			} else {
				optMusic.setRect(0, sep1.y + 1 + GAP, width, SLIDER_HEIGHT);
				chkMusicMute.setRect(0, optMusic.bottom() + GAP, width, BTN_HEIGHT);

				sep2.size(width, 1);
				sep2.y = chkMusicMute.bottom() + GAP;

				optSFX.setRect(0, sep2.y + 1 + GAP, width, SLIDER_HEIGHT);
				chkMuteSFX.setRect(0, optSFX.bottom() + GAP, width, BTN_HEIGHT);
			}

			height = chkMuteSFX.bottom();

			if (chkIgnoreSilent != null){
				sep3.size(width, 1);
				sep3.y = chkMuteSFX.bottom() + GAP;

				chkIgnoreSilent.setRect(0, sep3.y + 1 + GAP, width, BTN_HEIGHT);
				height = chkIgnoreSilent.bottom();
			} else if (chkMusicBG != null){
				sep3.size(width, 1);
				sep3.y = chkMuteSFX.bottom() + GAP;

				chkMusicBG.setRect(0, sep3.y + 1 + GAP, width, BTN_HEIGHT);
				height = chkMusicBG.bottom();
			}
		}

	}

	private static class LangsTab extends Component{

		final static int COLS_P = 3;
		final static int COLS_L = 6;

		final static int BTN_HEIGHT = 11;

		RenderedTextBlock title;
		ColorBlock sep1;
		RenderedTextBlock txtLangInfo;
		ColorBlock sep2;
		RedButton[] lanBtns;
		ColorBlock sep3;
		RenderedTextBlock txtTranifex;
		RedButton btnCredits;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			final ArrayList<Languages> langs = new ArrayList<>(Arrays.asList(Languages.values()));

			Languages nativeLang = Languages.matchLocale(Locale.getDefault());
			langs.remove(nativeLang);
			//move the native language to the top.
			langs.add(0, nativeLang);

			final Languages currLang = Messages.lang();

			txtLangInfo = PixelScene.renderTextBlock(6);
			String info = "_" + Messages.titleCase(currLang.nativeName()) + "_ - ";
			if (currLang == Languages.ENGLISH) info += "This is the source language, written by the developer.";
			else if (currLang.status() == Languages.Status.O_COMPLETE) info += Messages.get(this, "completed");
			else if (currLang.status() == Languages.Status.__UNREVIEW) info += Messages.get(this, "unreviewed");
			else if (currLang.status() == Languages.Status.X_UNFINISH) info += Messages.get(this, "unfinished");
			txtLangInfo.text(info);

			if (currLang.status() == Languages.Status.__UNREVIEW) txtLangInfo.setHightlighting(true, CharSprite.WARNING);
			else if (currLang.status() == Languages.Status.X_UNFINISH) txtLangInfo.setHightlighting(true, CharSprite.NEGATIVE);
			add(txtLangInfo);

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);

			lanBtns = new RedButton[langs.size()];
			for (int i = 0; i < langs.size(); i++){
				final int langIndex = i;
				RedButton btn = new RedButton(Messages.titleCase(langs.get(i).nativeName()), 6){
					@Override
					protected void onClick() {
						super.onClick();
						Messages.setup(langs.get(langIndex));
						seamlessResetScene(new Game.SceneChangeCallback() {
							@Override
							public void beforeCreate() {
								SPDSettings.language(langs.get(langIndex));
								GameLog.wipe();
								Game.platform.resetGenerators();
							}
							@Override
							public void afterCreate() {
								//do nothing
							}
						});
					}
				};
				if (currLang == langs.get(i)){
					btn.textColor(TITLE_COLOR);
				} else {
					switch (langs.get(i).status()) {
						case X_UNFINISH:
							btn.textColor(0x888888);
							break;
						case __UNREVIEW:
							btn.textColor(0xBBBBBB);
							break;
					}
				}
				lanBtns[i] = btn;
				add(btn);
			}

			sep3 = new ColorBlock(1, 1, 0xFF000000);
			add(sep3);

			txtTranifex = PixelScene.renderTextBlock(5);
			txtTranifex.text(Messages.get(this, "transifex"));
			add(txtTranifex);

			if (currLang != Languages.ENGLISH) {
				String credText = Messages.titleCase(Messages.get(this, "credits"));
				btnCredits = new RedButton(credText, credText.length() > 9 ? 6 : 9) {
					@Override
					protected void onClick() {
						super.onClick();
						String[] reviewers = currLang.reviewers();
						String[] translators = currLang.translators();

						int totalCredits = 2*reviewers.length + translators.length;
						int totalTokens = 2*totalCredits; //for spaces

						//additional space for titles, and newline chars
						if (reviewers.length > 0) totalTokens+=6;
						totalTokens +=4;

						String[] entries = new String[totalTokens];
						int index = 0;
						if (reviewers.length > 0){
							entries[0] = "_";
							entries[1] = Messages.titleCase(Messages.get(LangsTab.this, "reviewers"));
							entries[2] = "_";
							entries[3] = "\n";
							index = 4;
							for (int i = 0; i < reviewers.length; i++){
								entries[index] = reviewers[i];
								if (i < reviewers.length-1) entries[index] += ", ";
								entries[index+1] = " ";
								index += 2;
							}
							entries[index] = "\n";
							entries[index+1] = "\n";
							index += 2;
						}

						entries[index] = "_";
						entries[index+1] = Messages.titleCase(Messages.get(LangsTab.this, "translators"));
						entries[index+2] = "_";
						entries[index+3] = "\n";
						index += 4;

						//reviewers are also shown as translators
						for (int i = 0; i < reviewers.length; i++){
							entries[index] = reviewers[i];
							if (i < reviewers.length-1 || translators.length > 0) entries[index] += ", ";
							entries[index+1] = " ";
							index += 2;
						}

						for (int i = 0; i < translators.length; i++){
							entries[index] = translators[i];
							if (i < translators.length-1) entries[index] += ", ";
							entries[index+1] = " ";
							index += 2;
						}

						Window credits = new Window(0, 0, Chrome.get(Chrome.Type.TOAST));

						int w = PixelScene.landscape() ? 120 : 80;
						if (totalCredits >= 25) w *= 1.5f;

						RenderedTextBlock title = PixelScene.renderTextBlock(9);
						title.text(Messages.titleCase(Messages.get(LangsTab.this, "credits")), w);
						title.hardlight(TITLE_COLOR);
						title.setPos((w - title.width()) / 2, 0);
						credits.add(title);

						RenderedTextBlock text = PixelScene.renderTextBlock(7);
						text.maxWidth(w);
						text.tokens(entries);

						text.setPos(0, title.bottom() + 4);
						credits.add(text);

						credits.resize(w, (int) text.bottom() + 2);
						ShatteredPixelDungeon.scene().addToFront(credits);
					}
				};
				add(btnCredits);
			}

		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 3*GAP;

			txtLangInfo.setPos(0, sep1.y + 1 + GAP);
			txtLangInfo.maxWidth((int)width);

			y = txtLangInfo.bottom() + 2*GAP;
			int x = 0;

			sep2.size(width, 1);
			sep2.y = y;
			y += 2;

			int cols = PixelScene.landscape() ? COLS_L : COLS_P;
			int btnWidth = (int)Math.floor((width - (cols-1)) / cols);
			for (RedButton btn : lanBtns){
				btn.setRect(x, y, btnWidth, BTN_HEIGHT);
				btn.setPos(x, y);
				x += btnWidth+1;
				if (x + btnWidth > width){
					x = 0;
					y += BTN_HEIGHT+1;
				}
			}
			if (x > 0){
				y += BTN_HEIGHT+1;
			}

			sep3.size(width, 1);
			sep3.y = y;
			y += 2;

			if (btnCredits != null){
				btnCredits.setSize(btnCredits.reqWidth() + 2, 16);
				btnCredits.setPos(width - btnCredits.width(), y);

				txtTranifex.setPos(0, y);
				txtTranifex.maxWidth((int)btnCredits.left());

				height = Math.max(btnCredits.bottom(), txtTranifex.bottom());
			} else {
				txtTranifex.setPos(0, y);
				txtTranifex.maxWidth((int)width);

				height = txtTranifex.bottom();
			}

		}
	}
}
