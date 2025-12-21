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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.ally.AuxiliaryDrone;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Bundle;

public class DirectableAlly extends NPC {

	{
		alignment = Char.Alignment.ALLY;
		intelligentAlly = true;
		WANDERING = new Wandering();
		HUNTING = new Hunting();
		state = WANDERING;

		//before other mobs
		actPriority = MOB_PRIO + 1;

	}

	protected boolean attacksAutomatically = true;

	protected int defendingPos = -1;
	protected boolean movingToDefendPos = false;

	protected boolean movingToHeroPos = false;

	public void defendPos( int cell ){
		defendingPos = cell;
		movingToDefendPos = true;
		movingToHeroPos = false;
		aggro(null);
		state = WANDERING;
	}

	public void clearDefensingPos(){
		defendingPos = -1;
		movingToDefendPos = false;
		movingToHeroPos = false;
	}

	public void followHero(){
		defendingPos = -1;
		movingToDefendPos = false;
		movingToHeroPos = true;
		aggro(null);
		state = WANDERING;
	}

	public void targetChar( Char ch ){
		defendingPos = -1;
		movingToDefendPos = false;
		movingToHeroPos = false;
		aggro(ch);
		target = ch.pos;
	}

	public void wander( Char ch ){
		defendingPos = -1;
		movingToDefendPos = false;
		movingToHeroPos = false;
		aggro(null);
		state = WANDERING;
	}

	@Override
	public void aggro(Char ch) {
		enemy = ch;
		if (!movingToDefendPos && state != PASSIVE){
			state = HUNTING;
		}
	}

	public void directTocell( int cell ){
		if (!Dungeon.level.heroFOV[cell]
				|| Actor.findChar(cell) == null
				|| (Actor.findChar(cell) != Dungeon.hero
				&& Actor.findChar(cell).alignment != Char.Alignment.ENEMY
				&& Actor.findChar(cell).alignment != Char.Alignment.ALLY)){
			defendPos( cell );
			return;
		}

		if (Actor.findChar(cell) == Dungeon.hero){
			followHero();

		} else if (Actor.findChar(cell).alignment == Char.Alignment.ENEMY){
			targetChar(Actor.findChar(cell));

		}else if (Actor.findChar(cell).alignment == Alignment.ALLY){
			wander(this);

		}

	}

	private static final String DEFEND_POS = "defend_pos";
	private static final String MOVING_TO_DEFEND = "moving_to_defend";
	private static final String MOVING_TO_HERO = "moving_to_hero";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DEFEND_POS, defendingPos);
		bundle.put(MOVING_TO_DEFEND, movingToDefendPos);
		bundle.put(MOVING_TO_HERO, movingToHeroPos);
	}

	@Override
	public void die( Object cause ) {
		if(hero.pointsInTalent(Talent.BEHEST)>=1){
			Buff.affect(hero, Adrenaline.class,hero.pointsInTalent(Talent.BEHEST)*5);}
		super.die(cause);
	}


	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(DEFEND_POS)) defendingPos = bundle.getInt(DEFEND_POS);
		movingToDefendPos = bundle.getBoolean(MOVING_TO_DEFEND);
		movingToHeroPos= bundle.getBoolean(MOVING_TO_HERO);
	}

	private class Wandering extends Mob.Wandering {

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if ( enemyInFOV
					&& attacksAutomatically
					&& !movingToDefendPos
					&& (defendingPos == -1 || !Dungeon.level.heroFOV[defendingPos] || canAttack(enemy))) {

				enemySeen = true;

				notice();
				alerted = true;
				state = HUNTING;
				target = enemy.pos;

			} else {

				enemySeen = false;

				int oldPos = pos;
				//
				if(movingToHeroPos){
					target = defendingPos != -1 ? defendingPos : Dungeon.hero.pos;
				}else{
					target = defendingPos != -1 ? defendingPos : randomDestination();
				}

				//always move towards the hero when wandering
				if (getCloser( target )) {
					spend( 1 / speed() );
					if (pos == defendingPos) movingToDefendPos = false;
					return moveSprite( oldPos, pos );
				} else {
					//if it can't move closer to defending pos, then give up and defend current position
					if (movingToDefendPos){
						defendingPos = pos;
						movingToDefendPos = false;
					}
					spend( TICK );
				}

			}
			return true;
		}

	}

	private class Hunting extends Mob.Hunting {

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV && defendingPos != -1 && Dungeon.level.heroFOV[defendingPos] && !canAttack(enemy)){
				target = defendingPos;
				state = WANDERING;
				return true;
			}
			return super.act(enemyInFOV, justAlerted);
		}

	}

}
