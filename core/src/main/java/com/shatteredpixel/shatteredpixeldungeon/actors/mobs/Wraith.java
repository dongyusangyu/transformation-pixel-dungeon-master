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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.freeman.SalesContract;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ChallengeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WraithSprite;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Wraith extends Mob {

	private static final float SPAWN_DELAY	= 2f;
	
	protected int level;
	
	{
		spriteClass = WraithSprite.class;
		
		HP = HT = 1;
		EXP = 0;

		maxLvl = -2;
		
		flying = true;

		properties.add(Property.UNDEAD);
		properties.add(Property.INORGANIC);
	}
	
	private static final String LEVEL = "level";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getInt( LEVEL );
		adjustStats( level );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1 + level/2, 2 + level );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 10 + level;
	}
	
	public void adjustStats( int level ) {
		this.level = level;
		defenseSkill = attackSkill( null ) * 5;
		enemySeen = true;
	}
	@Override
	public int attackProc( Char enemy, int damage ) {
		if(hero.pointsNegative(Talent.HUNGRY_GHOST)==2 && enemy instanceof Hero && enemy.buff(Hunger.class)!=null){
			enemy.buff(Hunger.class).affectHunger(-5f);
		}
		return super.attackProc(enemy,damage);
	}

	@Override
	public float spawningWeight() {
		return 0f;
	}

	@Override
	public boolean reset() {
		state = WANDERING;
		return true;
	}

	public static void spawnAround( int pos ) {
		spawnAround( pos, null );
	}
	
	public static void spawnAround( int pos, Class<? extends Wraith> wraithClass ) {
		spawnAround( pos, wraithClass, null );
	}

	public static void spawnAround( int pos, Class<? extends Wraith> wraithClass, Char.Alignment alignment ) {
		for (int n : PathFinder.NEIGHBOURS4) {
			spawnAt( pos + n, wraithClass, false, alignment );
		}
	}

	public static Wraith spawnAt( int pos ) {
		return spawnAt( pos, null );
	}

	public static Wraith spawnAt( int pos, Class<? extends Wraith> wraithClass ) {
		return spawnAt( pos, wraithClass, true );
	}

	public static Wraith spawnAt( int pos, Class<? extends Wraith> wraithClass, Char.Alignment alignment ) {
		return spawnAt( pos, wraithClass, true, alignment );
	}

	private static Wraith spawnAt( int pos, Class<? extends Wraith> wraithClass, boolean allowAdjacent ) {
		return spawnAt( pos, wraithClass, allowAdjacent, null );
	}

	private static Wraith spawnAt( int pos, Class<? extends Wraith> wraithClass, boolean allowAdjacent, Char.Alignment alignment ) {

		//if the position itself is blocked, try to place in an adjacent cell if allowed
		if (Dungeon.level.solid[pos] || Actor.findChar( pos ) != null){
			ArrayList<Integer> candidates = new ArrayList<>();

			for (int i : PathFinder.NEIGHBOURS8){
				if (!Dungeon.level.solid[pos+i] && Actor.findChar( pos+i ) == null){
					candidates.add(pos+i);
				}
			}

			if (allowAdjacent && !candidates.isEmpty()){
				pos = Random.element(candidates);
			} else {
				pos = -1;
			}

		}

		if (pos != -1) {

			Wraith w;
			//if no wraith type is specified, 1/100 chance for exotic, otherwise normal
			if (wraithClass == null){
				float altChance = 1/100f * RatSkull.exoticChanceMultiplier();
				if (Random.Float() < altChance){
					w = new TormentedSpirit();
				} else {
					w = new Wraith();
				}
			} else {
				w = Reflection.newInstance(wraithClass);

			}
			w.adjustStats( Dungeon.scalingDepth() );
			if (alignment != null) {
				w.alignment = alignment;
                w.updateSpriteState();
			}
			w.pos = pos;
			w.state = w.HUNTING;
			GameScene.add( w, SPAWN_DELAY );
			Dungeon.level.occupyCell(w);

			w.sprite.alpha( 0 );
			w.sprite.parent.add( new AlphaTweener( w.sprite, 1, 0.5f ) );

			if (w instanceof TormentedSpirit){
				w.sprite.emitter().burst(ChallengeParticle.FACTORY, 10);
			} else {
				w.sprite.emitter().burst(ShadowParticle.CURSE, 5);
			}

			return w;
		} else {
			return null;
		}
	}

}
