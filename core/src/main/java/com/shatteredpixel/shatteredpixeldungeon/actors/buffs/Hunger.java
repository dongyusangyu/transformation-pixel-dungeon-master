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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.SaltCube;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class Hunger extends Buff implements Hero.Doom {

	public static final float HUNGRY	= 300f;
	public static final float STARVING	= 450f;

	public float level;
	private float partialDamage;

	private static final String LEVEL			= "level";
	private static final String PARTIALDAMAGE 	= "partialDamage";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( LEVEL, level );
		bundle.put( PARTIALDAMAGE, partialDamage );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getFloat( LEVEL );
		partialDamage = bundle.getFloat(PARTIALDAMAGE);
	}

	@Override
	public boolean act() {

		if (Dungeon.level.locked
				|| target.buff(WellFed.class) != null
				|| SPDSettings.intro()
				|| target.buff(ScrollOfChallenge.ChallengeArena.class) != null){
			spend(TICK);
			return true;
		}

		if (target.isAlive() && target instanceof Hero) {

			Hero hero = (Hero)target;

			if (isStarving()) {


				partialDamage += target.HT/1000f;
				if(hero.pointsNegative(Talent.EATER)>0){
					partialDamage += target.HT/1000f*hero.pointsNegative(Talent.EATER)*0.3;
				}
				if (partialDamage > 1){
					if(hero.hasTalent(Talent.CHOCOLATE_COINS) && Dungeon.gold>100-25*hero.pointsInTalent(Talent.CHOCOLATE_COINS)){
						Dungeon.gold-=49-12*hero.pointsInTalent(Talent.CHOCOLATE_COINS);
					}else{
						target.damage( (int)partialDamage, this);
						if(hero.pointsNegative(Talent.MALNUTRITION)==2){
							Buff.affect(hero, Hex.class,10);
							Buff.affect(hero, Weakness.class,10);
						}else if (hero.pointsNegative(Talent.MALNUTRITION)==1){
							Buff.affect(hero, Weakness.class,10);
						}
					}
					partialDamage -= (int)partialDamage;
				}
				
			} else {


				float hungerDelay = 1f;
				if (target.buff(Shadows.class) != null){
					hungerDelay *= 1.5f;
				}
				hungerDelay /= SaltCube.hungerGainMultiplier();
				if(hero.pointsInTalent(Talent.NATURAL_AFFINITY)>0){
					hungerDelay*=1+hero.pointsInTalent(Talent.NATURAL_AFFINITY)*0.25f;
				}
				float newLevel = level;
				if (hero.pointsInTalent(Talent.FASTING)==2 && hero.HP>=hero.HT*0.8){
					newLevel+=0;
				}else if (hero.pointsInTalent(Talent.FASTING)==1 && hero.HP>=hero.HT){
					newLevel+=0;
				}else{
					newLevel+=(1f/hungerDelay);
					if(hero.pointsNegative(Talent.EATER)>0){
						newLevel+=(1f/hungerDelay)*hero.pointsNegative(Talent.EATER)*0.15;
					}
				}
				if (newLevel >= STARVING) {

					if(hero.heroClass== HeroClass.DM400){
						GLog.n( Messages.get(this, "ondmstarving") );
					}else{
						GLog.n( Messages.get(this, "onstarving") );
					}
					hero.damage( 1, this );

					hero.interrupt();
					newLevel = STARVING;

				} else if (newLevel >= HUNGRY && level < HUNGRY) {

					if(hero.heroClass== HeroClass.DM400){
						GLog.w( Messages.get(this, "ondmhungry") );
					}else{
						GLog.w( Messages.get(this, "onhungry") );
					}

					if (!Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_FOOD)){
						GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_FOOD);
					}

				}
				level = newLevel;

			}
			
			spend( TICK );

		} else {

			diactivate();

		}

		return true;
	}

	public void satisfy( float energy ) {
		affectHunger( energy, false );
	}

	public void affectHunger(float energy ){
		affectHunger( energy, false );
	}

	public void affectHunger(float energy, boolean overrideLimits ) {

		if (energy < 0 && target.buff(WellFed.class) != null){
			target.buff(WellFed.class).left += energy;
			BuffIndicator.refreshHero();
			return;
		}

		float oldLevel = level;

		level -= energy;
		if (level < 0 && !overrideLimits) {
			level = 0;
		} else if (level > STARVING) {
			float excess = level - STARVING;
			level = STARVING;
			partialDamage += excess * (target.HT/1000f);
			if (partialDamage > 1f){
				target.damage( (int)partialDamage, this );
				partialDamage -= (int)partialDamage;

			}
		}

		if (oldLevel < HUNGRY && level >= HUNGRY){
			if(hero.heroClass== HeroClass.DM400){
				GLog.w( Messages.get(this, "ondmhungry") );
			}else{
				GLog.w( Messages.get(this, "onhungry") );
			}

		} else if (oldLevel < STARVING && level >= STARVING){
			if(hero.heroClass== HeroClass.DM400){
				GLog.n( Messages.get(this, "ondmstarving") );
			}else{
				GLog.n( Messages.get(this, "onstarving") );
			}

			target.damage( 1, this );
		}

		BuffIndicator.refreshHero();
	}

	public boolean isStarving() {
		return level >= STARVING;
	}

	public int hunger() {
		return (int)Math.ceil(level);
	}

	@Override
	public int icon() {
		if (level < HUNGRY) {
			return BuffIndicator.NONE;
		} else if (level < STARVING) {
			if(hero.heroClass== HeroClass.DM400){
				return BuffIndicator.DMHUNGER;
			}else{
				return BuffIndicator.HUNGER;
			}

		} else {
			if(hero.heroClass== HeroClass.DM400){
				return BuffIndicator.DMSTARVATION;
			}else{
				return BuffIndicator.STARVATION;
			}

		}
	}

	@Override
	public String name() {
		if (level < STARVING) {
			if(hero.heroClass== HeroClass.DM400){
				return Messages.get(this, "dmhungry");
			}else{
				return Messages.get(this, "hungry");
			}

		} else {
			if(hero.heroClass== HeroClass.DM400){
				return Messages.get(this, "dmstarving");
			}else{
				return Messages.get(this, "starving");
			}
		}
	}

	@Override
	public String desc() {
		String result;
		if (level < STARVING) {
			if(hero.heroClass== HeroClass.DM400){
				result = Messages.get(this, "desc_intro_dmhungry");
			}else{
				result = Messages.get(this, "desc_intro_hungry");
			}

		} else {
			if(hero.heroClass== HeroClass.DM400){
				result = Messages.get(this, "desc_intro_dmstarving");
			}else{
				result = Messages.get(this, "desc_intro_starving");
			}

		}
		if(hero.heroClass== HeroClass.DM400){
			result += Messages.get(this, "dmdesc");
		}else{
			result += Messages.get(this, "desc");
		}



		return result;
	}

	@Override
	public void onDeath() {

		Badges.validateDeathFromHunger();

		Dungeon.fail( this );
		GLog.n( Messages.get(this, "ondeath") );
	}
}
