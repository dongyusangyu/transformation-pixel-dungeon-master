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

package com.shatteredpixel.shatteredpixeldungeon.ui;



import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.ShopBoss;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollOfSublimation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournalItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndNegative;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TalentButton extends Button {

	public static final int WIDTH = 20;
	public static final int HEIGHT = 26;

	int tier;
	Talent talent;
	int pointsInTalent;
	Mode mode;

	TalentIcon icon;
	Image bg;


	ColorBlock fill;

	public enum Mode {
		INFO,
		UPGRADE,
		METAMORPH_CHOOSE,
		METAMORPH_REPLACE,
		SUBLIMATION,
		NEGATIVE,
		METAMORPH_TYPE,
	}

	public TalentButton(int tier, Talent talent, int points, Mode mode){
		super();
		hotArea.blockLevel = PointerArea.NEVER_BLOCK;

		this.tier = tier;
		this.talent = talent;
		this.pointsInTalent = points;
		this.mode = mode;

		bg.frame(20*(talent.maxPoints()-1), 0, WIDTH, HEIGHT);

		icon = new TalentIcon( talent );
		add(icon);
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		fill = new ColorBlock(0, 4, 0xFFFFFF44);
		add(fill);

		bg = new Image(Assets.Interfaces.TALENT_BUTTON);
		add(bg);
	}

	@Override
	protected void layout() {
		width = WIDTH;
		height = HEIGHT;

		super.layout();

		fill.x = x+2;
		fill.y = y + WIDTH - 1;
		fill.size( pointsInTalent/(float)talent.maxPoints() * (WIDTH-4), 5);

		bg.x = x;
		bg.y = y;

		icon.x = x + 2;
		icon.y = y + 2;
		PixelScene.align(icon);
	}

	@Override
	protected void onClick() {
		super.onClick();

		Window toAdd;
		if (mode == Mode.UPGRADE
				&& Dungeon.hero != null
				&& Dungeon.hero.isAlive()
				&& Dungeon.hero.talentPointsAvailable(tier) > 0
				&& Dungeon.hero.canUpgradeTalent(talent)){
			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(WndInfoTalent.class, "upgrade"));
				}

				@Override
				public void call() {
					upgradeTalent();
				}
			});
		} else if (mode == Mode.METAMORPH_CHOOSE && Dungeon.hero != null && Dungeon.hero.isAlive()) {
			/*
			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
				}

				@Override
				public boolean metamorphDesc() {
					return true;
				}

				@Override
				public void call() {
					if (ScrollOfMetamorphosis.WndMetamorphChoose.INSTANCE != null){
						ScrollOfMetamorphosis.WndMetamorphChoose.INSTANCE.hide();
					}
					GameScene.show(new ScrollOfMetamorphosis.WndMetamorphReplace(talent, tier));
				}
			});

			 */
			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
				}

				@Override
				public boolean metamorphDesc() {
					return true;
				}

				@Override
				public void call() {
					if (ScrollOfMetamorphosis.WndMetamorphChoose.INSTANCE != null){
						ScrollOfMetamorphosis.WndMetamorphChoose.INSTANCE.hide();
					}
					GameScene.show(new ScrollOfMetamorphosis.WndType(tier,talent));
					/*
					Game.runOnRenderThread(new Callback() {
						@Override
						public void call() {
							GameScene.show(new WndOptions(new TalentIcon( talent ),
									Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "type_name")),
									Messages.get(ScrollOfMetamorphosis.class, "type_desc"),
									Messages.get(ScrollOfMetamorphosis.class, "attack"),
									Messages.get(ScrollOfMetamorphosis.class, "magic"),
									Messages.get(ScrollOfMetamorphosis.class, "effect"),
									Messages.get(ScrollOfMetamorphosis.class, "resource"),
									Messages.get(ScrollOfMetamorphosis.class, "spell"),
									Messages.get(ScrollOfMetamorphosis.class, "assist"),
									Messages.get(ScrollOfMetamorphosis.class, "other") ) {
								@Override
								protected void onSelect( int index ) {
									GameScene.show(new ScrollOfMetamorphosis.WndMetamorphReplace(talent, tier,index));
								}
							} );
						}
					});

					 */
				}
			});
		} else if (mode == Mode.METAMORPH_REPLACE && Dungeon.hero != null && Dungeon.hero.isAlive()) {
			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
				}

				@Override
				public boolean metamorphDesc() {
					return true;
				}



				@Override
				public void call() {

					Talent replacing = ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.replacing;

					for (LinkedHashMap<Talent, Integer> tier : Dungeon.hero.talents){
						if (tier.containsKey(replacing)){
							LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
							for (Talent t : tier.keySet()){
								if (t == replacing){
									newTier.put(talent, tier.get(replacing));

									if (!Dungeon.hero.metamorphedTalents.containsValue(replacing)){
										Dungeon.hero.metamorphedTalents.put(replacing, talent);

									//if what we're replacing is already a value, we need to simplify the data structure
									} else {
										//a->b->a, we can just remove the entry entirely
										if (Dungeon.hero.metamorphedTalents.get(talent) == replacing){
											Dungeon.hero.metamorphedTalents.remove(talent);

										//a->b->c, we need to simplify to a->c
										} else {
											for (Talent t2 : Dungeon.hero.metamorphedTalents.keySet()){
												if (Dungeon.hero.metamorphedTalents.get(t2) == replacing){
													Dungeon.hero.metamorphedTalents.put(t2, talent);
												}
											}
										}
									}

								} else {
									newTier.put(t, tier.get(t));
								}
							}

							Dungeon.hero.talents.set(ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.tier-1, newTier);
							break;
						}
					}

					ScrollOfMetamorphosis.onMetamorph(replacing, talent);
					Statistics.metamorphosis++;
					Badges.validateFreemanUnlock();

					if (ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE != null){
						ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.hide();
					}

				}
			});
		}else if (mode == Mode.SUBLIMATION && Dungeon.hero != null && Dungeon.hero.isAlive()) {

			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(ScrollOfSublimation.class, "metamorphose_talent"));
				}

				@Override
				public boolean metamorphDesc() {
					return true;
				}

				@Override
				public void call() {
					int tier=1;
					String type = ScrollOfSublimation.WndSublimation.INSTANCE.type;
					int index = ScrollOfSublimation.WndSublimation.INSTANCE.index;
					switch(type){
						case "GOO" : case "TENGU" : default:
							tier=1;
							break;
						case "DM300":case "DWARFKING":
							tier=2;
							break;
						case"YOG":
							tier=3;
							break;
					}

					int cnt=1;
					for (LinkedHashMap<Talent, Integer> tiers : Dungeon.hero.talents){
						if(cnt==tier){
							LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
							for (Talent t : tiers.keySet()){
								newTier.put(t,  tiers.get(t));
							}
							newTier.put(talent, 0);
							Dungeon.hero.talents.set(tier-1, newTier);
							Dungeon.hero.sublimationTalents.put(talent, type);
							ArrayList<String> S= new ArrayList<String>();
							S.add("DM300");
							S.add("YOG");
							if(S.contains(type)){
								Buff.affect(hero, ScrollOfSublimation.Sublimation1.class).setBoosted(index);
								//Buff.affect(curUser, Haste.class,666);
							}else{
								Buff.affect(hero, ScrollOfSublimation.Sublimation.class).setBoosted(index);
							}
							new Flare(6, 32).color(0xFFAA00, true).show(hero.sprite, 4f);
							ScrollOfSublimation.WndSublimation.INSTANCE.use = true;
							StatusPane.talentBlink = 10f;
							WndHero.lastIdx = 1;
							break;
						}else{cnt++;}
					}

					//ScrollOfMetamorphosis.onMetamorph(replacing, talent);
					if (ScrollOfSublimation.WndSublimation.INSTANCE != null){
						ScrollOfSublimation.WndSublimation.INSTANCE.hide();
					}

				}
			});
		}else if (mode == Mode.NEGATIVE && Dungeon.hero != null && Dungeon.hero.isAlive()) {

			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(ScrollOfSublimation.class, "metamorphose_talent"));
				}

				@Override
				public boolean metamorphDesc() {
					return true;
				}

				@Override
				public void call() {
					int tier=Dungeon.hero.negativeTalents.size()+1;


					int cnt=1;
					for (LinkedHashMap<Talent, Integer> tiers : Dungeon.hero.talents){
						if(cnt==tier){
							LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
							for (Talent t : tiers.keySet()){
								newTier.put(t,  tiers.get(t));
							}
							newTier.put(talent, 0);
							Dungeon.hero.talents.set(tier-1, newTier);
							Dungeon.hero.negativeTalents.add(talent);
							Talent.onTalentUpgraded(Dungeon.hero,talent);
							Dungeon.hero.updateHT(true);
							GameScene.updateFog();
							break;
						}else{cnt++;}
					}

					//ScrollOfMetamorphosis.onMetamorph(replacing, talent);
					if (WndNegative.INSTANCE != null){
						WndNegative.INSTANCE.hide();
					}

				}
			});
		} else {
			toAdd = new WndInfoTalent(talent, pointsInTalent, null);
		}

		if (ShatteredPixelDungeon.scene() instanceof GameScene){
			GameScene.show(toAdd);
		} else {
			ShatteredPixelDungeon.scene().addToFront(toAdd);
		}
	}

	@Override
	protected void onPointerDown() {
		icon.brightness( 1.5f );
		bg.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK );
	}

	@Override
	protected void onPointerUp() {
		icon.resetColor();
		bg.resetColor();
	}

	@Override
	protected String hoverText() {
		return Messages.titleCase(talent.title());
	}

	public void enable(boolean value ) {
		active = value;
		icon.alpha( value ? 1.0f : 0.3f );
		bg.alpha( value ? 1.0f : 0.3f );
	}

	public void upgradeTalent(){
		if (Dungeon.hero.talentPointsAvailable(tier) > 0 && parent != null) {
			Dungeon.hero.upgradeTalent(talent);
			float oldWidth = fill.width();
			pointsInTalent++;
			layout();
			Sample.INSTANCE.play(Assets.Sounds.LEVELUP, 0.7f, 1.2f);
			Emitter emitter = (Emitter) parent.recycle(Emitter.class);
			emitter.revive();
			emitter.pos(fill.x + (fill.width() + oldWidth) / 2f, fill.y + fill.height() / 2f);
			emitter.burst(Speck.factory(Speck.STAR), 12);
		}
	}
}
