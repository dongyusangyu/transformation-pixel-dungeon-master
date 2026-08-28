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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.PhaseShift;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class LloydsBeacon extends Artifact {

	public static final float TIME_TO_USE = 1;

	public static final String AC_ZAP       = "ZAP";
	public static final String AC_SET		= "SET";
	public static final String AC_RETURN	= "RETURN";
	
	public int returnDepth	= -1;
	public int returnBranch	= 0;
	public int returnPos;
	private boolean returnMarkerValid;
	private boolean returnMarkerReachable;
	
	{
		image = ItemSpriteSheet.ARTIFACT_BEACON;

		levelCap = 3;

		charge = 0;
		chargeCap = 3+level();

		defaultAction = AC_ZAP;
		usesTargeting = true;
	}
	
	private static final String DEPTH	= "depth";
	private static final String BRANCH	= "branch";
	private static final String POS		= "pos";
	private static final String MARKER_VALID = "marker_valid";
	private static final String MARKER_REACHABLE = "marker_reachable";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DEPTH, returnDepth );
		if (hasReturnMarker()) {
			refreshReturnMarkerReachability();
			bundle.put( BRANCH, returnBranch );
			bundle.put( POS, returnPos );
			bundle.put( MARKER_VALID, true );
			bundle.put( MARKER_REACHABLE, returnMarkerReachable );
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		returnDepth	= bundle.getInt( DEPTH );
		if (returnDepth != -1 && bundle.contains(BRANCH)
				&& bundle.getBoolean(MARKER_VALID)) {
			returnBranch = bundle.getInt(BRANCH);
			returnPos = bundle.getInt(POS);
			returnMarkerValid = Dungeon.returnTeleportMarkerLocationAllowed(returnDepth, returnBranch);
			returnMarkerReachable = !bundle.contains(MARKER_REACHABLE)
					|| bundle.getBoolean(MARKER_REACHABLE);
		} else {
			clearReturnMarker();
		}
	}

	private boolean hasReturnMarker() {
		return returnDepth != -1 && Dungeon.returnTeleportMarkerAllowed(
				returnDepth, returnBranch, returnMarkerValid);
	}

	private void refreshReturnMarkerReachability() {
		if (Dungeon.sameLocation(returnDepth, returnBranch, Dungeon.depth, Dungeon.branch)) {
			returnMarkerReachable = Dungeon.returnTeleportPositionAllowed(returnPos);
		}
	}

	private void clearReturnMarker() {
		returnDepth = -1;
		returnBranch = 0;
		returnPos = 0;
		returnMarkerValid = false;
		returnMarkerReachable = false;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );

		if (isEquipped(hero) && charge > 0 && !cursed && hero.buff(MagicImmune.class) == null) {
            actions.add( AC_ZAP );
            actions.add( AC_SET );
			if (hasReturnMarker()) {
                actions.add( AC_RETURN );
            }
        }else if(isEquipped(hero) && !cursed && hero.buff(MagicImmune.class) == null){
            actions.add( AC_SET );
        }
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		if (AC_ZAP.equals(action) || AC_SET.equals(action) || AC_RETURN.equals(action)) {
			if (!canUseActiveAction(hero)) {
				usesTargeting = false;
				return;
			}
		}
		super.execute( hero, action );

		if (AC_SET.equals(action) || AC_RETURN.equals(action)) {
			refreshReturnMarkerReachability();
			boolean actionAllowed = AC_SET.equals(action)
					? Dungeon.returnTeleportMarkerPlacementAllowed()
					: hasReturnMarker() && Dungeon.returnTeleportAllowed(
							returnDepth, returnBranch, returnPos, returnMarkerReachable);
			if (!actionAllowed) {
				GLog.w( Messages.get(this, "preventing") );
				return;
			}
			
			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
				Char ch = Actor.findChar(hero.pos + PathFinder.NEIGHBOURS8[i]);
				if (ch != null && ch.alignment == Char.Alignment.ENEMY) {
					GLog.w( Messages.get(this, "creatures") );
					return;
				}
			}
		}

		if (AC_ZAP.equals(action)){
			usesTargeting = true;
			GameScene.selectCell(zapper);

		} else if (AC_SET.equals(action)) {
			Invisibility.dispel();
			returnDepth = Dungeon.depth;
			returnBranch = Dungeon.branch;
			returnPos = hero.pos;
			returnMarkerValid = true;
			returnMarkerReachable = Dungeon.returnTeleportPositionAllowed(returnPos);
			
			hero.spend( LloydsBeacon.TIME_TO_USE );
			hero.busy();
			
			hero.sprite.operate( hero.pos );
			Sample.INSTANCE.play( Assets.Sounds.BEACON );
			
			GLog.i( Messages.get(this, "return") );

		} else if (AC_RETURN.equals(action)) {

			if (charge < 3) {
				GLog.w( Messages.get(this, "no_charge_return") );

			}else{
                Invisibility.dispel();
                charge -= 3;
                gainExp(12);
                updateQuickslot();
				Talent.onArtifactUsed(hero);

				if (Dungeon.sameLocation(returnDepth, returnBranch, Dungeon.depth, Dungeon.branch)) {
                    ScrollOfTeleportation.appear( hero, returnPos );
                    for(Mob m : Dungeon.level.mobs){
                        if (m.pos == hero.pos){
                            //displace mob
                            for(int i : PathFinder.NEIGHBOURS8){
                                if (Actor.findChar(m.pos+i) == null && Dungeon.level.passable[m.pos + i]){
                                    m.pos += i;
                                    m.sprite.point(m.sprite.worldToCamera(m.pos));
                                    break;
                                }
                            }
                        }
                    }
                    Dungeon.level.occupyCell(hero );
                    Dungeon.observe();
                    GameScene.updateFog();
                    hero.spendAndNext( LloydsBeacon.TIME_TO_USE );
                }else {
                    Level.beforeTransition();
					InterlevelScene.mode = InterlevelScene.Mode.RETURN;
					InterlevelScene.returnDepth = returnDepth;
					InterlevelScene.returnBranch = returnBranch;
					InterlevelScene.returnPos = returnPos;
                    Game.switchScene( InterlevelScene.class );
                }
			}
		}
	}

	protected CellSelector.Listener zapper = new  CellSelector.Listener() {

		@Override
		public void onSelect(Integer target) {


			if (target == null || curUser == null) return;
			if (curItem == LloydsBeacon.this) {
				if (!canUseActiveAction(curUser)) return;
			} else if (curItem == null || !curItem.isEquipped(curUser)){
                return;
            }
			if (curUser.buff(MagicImmune.class) != null) {
				GLog.w(Messages.get(Artifact.class, "no_magic"));
				return;
			} else if (charge < 1) {
                GLog.w( Messages.get(LloydsBeacon.class, "no_charge_zap", 1) );
                return;
            }

            Invisibility.dispel();
			charge--;
			gainExp(4);
			updateQuickslot();
			Talent.onArtifactUsed(curUser);

			if (Actor.findChar(target) == curUser){
				ScrollOfTeleportation.teleportChar(curUser);
				curUser.spendAndNext(1f);
			} else {
				final Ballistica bolt = new Ballistica( curUser.pos, target, Ballistica.MAGIC_BOLT );
				final Char ch = Actor.findChar(bolt.collisionPos);

				if (ch == curUser){
					ScrollOfTeleportation.teleportChar(curUser);
					curUser.spendAndNext( 1f );
				} else {
					Sample.INSTANCE.play( Assets.Sounds.ZAP );
					curUser.sprite.zap(bolt.collisionPos);
					curUser.busy();

					MagicMissile.boltFromChar(curUser.sprite.parent,
							MagicMissile.BEACON,
							curUser.sprite,
							bolt.collisionPos,
							new Callback() {
								@Override
								public void call() {
									if (ch != null) {
										if (ScrollOfTeleportation.teleportChar(ch)) {
											if (ch.isAlive() && ch.alignment == Char.Alignment.ENEMY) {
												artifactProc(ch, level(), 1);
											}
											if (ch instanceof Mob) {
												if (((Mob) ch).state == ((Mob) ch).HUNTING) {
													((Mob) ch).state = ((Mob) ch).WANDERING;
												}
												((Mob) ch).beckon(Dungeon.level.randomDestination(ch));
											}
										}
									} else {
										GLog.w( Messages.get(PhaseShift.class, "no_target") );
									}
									curUser.spendAndNext(1f);
								}
							});

				}


			}

		}

		@Override
		public String prompt() {
			return Messages.get(LloydsBeacon.class, "prompt");
		}
	};

	public void useTrinityTeleport(ClassArmor armor) {
		curUser = Dungeon.hero;
		if (curUser == null || curUser.buff(MagicImmune.class) != null) {
			usesTargeting = false;
			GLog.w(Messages.get(Artifact.class, "no_magic"));
			return;
		}
		charge = Math.max(charge, 1);
		GameScene.selectCell(zapper);
		if (Dungeon.quickslot.contains(armor)) {
			QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(armor));
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new beaconRecharge();
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null) return;
		gainCharge(0.133f*amount, true);
	}


	@Override
	public void onHeroGainExp( float levelPercent, Hero hero ) {
		if (isEquipped( hero ) && !cursed) {
			gainExpCharge(levelPercent, hero);
		}
	}

	@Override
	public Item upgrade() {
		if (level() == levelCap) return this;
		chargeCap = 3 + level() + 1;
		return super.upgrade();
	}

	@Override
	public int visiblyUpgraded() {
		return levelKnown ? (int)(level()*3.5f): 0;
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return visiblyUpgraded();
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (isEquipped( Dungeon.hero )) {
			if (cursed) {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			} else {
				desc += "\n\n" + Messages.get(this, "desc_worn");
			}
		}
		if (hasReturnMarker()){
			desc += "\n\n" + Messages.get(this,
					returnBranch == TowerLevel.BRANCH ? "desc_set_tower" : "desc_set",
					returnDepth);
		}
		return desc;
	}
	
	private static final Glowing WHITE = new Glowing( 0xFFFFFF );
	
	@Override
	public Glowing glowing() {
		return hasReturnMarker() ? WHITE : null;
	}

	private void gainCharge(float amount, boolean announceFull) {
		if (charge >= chargeCap || cursed || amount <= 0) {
			return;
		}

		boolean wasFull = charge >= chargeCap;
		partialCharge += amount;
		while (partialCharge >= 1){
			partialCharge--;
			charge++;
			if (charge >= chargeCap){
				partialCharge = 0;
				charge = chargeCap;
				break;
			}
		}

		if (!wasFull && charge >= chargeCap && announceFull) {
			GLog.p( Messages.get(this, "full_charge") );
		}
		updateQuickslot();
	}

	private void gainExp(int amount) {
		if (amount <= 0 || level() >= levelCap) {
			return;
		}

		exp += amount;
		while (level() < levelCap && exp >= 15 + level()*9) {
			exp -= 15 + level()*9;
			upgrade();
			chargeCap = 3 + level();
			Catalog.countUse(LloydsBeacon.class);
			GLog.p( Messages.get(this, "levelup") );
		}
	}


	private void gainExpCharge(float levelPercent, Char target) {
		if (cursed || target.buff(MagicImmune.class) != null) return;
		gainCharge(chargeCap * levelPercent * RingOfEnergy.artifactChargeMultiplier(target), true);
		//gainExp(Math.round((15 + level()*9) * levelPercent));
	}



	public class beaconRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
			if (cursed) {
				if (Random.Float() < 0.01f) {
					Buff.affect(target, Vertigo.class, 10f);
				}
			}

			updateQuickslot();
			spend( TICK );
			return true;
		}

		public void gainCharge(float levelPortion) {
			gainExpCharge(levelPortion, target);
		}
	}
}
