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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Light extends FlavourBuff {
	
	{
		type = buffType.POSITIVE;
	}

	public static final float DURATION	= 250f;
	public static final int DISTANCE	= 6;
	
	@Override
	public boolean attachTo( Char target ) {
		if(hero != null && target==hero && hero.hasTalent(Talent.HOLY_FAITH)){
			Buff.affect((Hero)target, Recharging.class,3+2*hero.pointsInTalent(Talent.HOLY_FAITH));
			Buff.affect((Hero)target, Bless.class,3+2*hero.pointsInTalent(Talent.HOLY_FAITH));
			if(hero.pointsInTalent(Talent.HOLY_FAITH)==3){
				Buff.affect((Hero)target, ArtifactRecharge.class).extend(4);
			}
		}
		if (super.attachTo( target )) {
			if (Dungeon.level != null) {
				target.viewDistance = Math.max( Dungeon.level.viewDistance, DISTANCE );
				if(target.viewDistance == Light.DISTANCE && Dungeon.isChallenged(Challenges.HARSH_ENVIRONMENT) && Dungeon.isChallenged(Challenges.DARKNESS)){
					target.viewDistance = 4;
				}
				Dungeon.observe();
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		target.viewDistance = Dungeon.level.viewDistance;
		Dungeon.observe();
		super.detach();
	}

	public void weaken( int amount ){
		spend(-amount);
	}
	
	@Override
	public int icon() {
		return BuffIndicator.LIGHT;
	}
	
	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.ILLUMINATED);
		else target.sprite.remove(CharSprite.State.ILLUMINATED);
	}
}
