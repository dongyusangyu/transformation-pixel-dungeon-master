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

package com.shatteredpixel.shatteredpixeldungeon.items;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Berry;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Waterskin extends Item {

	private static final int MAX_VOLUME	= 20;

	private static final String AC_DRINK	= "DRINK";

	private static final float TIME_TO_DRINK = 1f;

	private static final String TXT_STATUS	= "%d/%d";

	{
		image = ItemSpriteSheet.WATERSKIN;

		defaultAction = AC_DRINK;

		unique = true;
	}

	public int volume = 0;

	private static final String VOLUME	= "volume";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( VOLUME, volume );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		volume	= bundle.getInt( VOLUME );
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (volume > 0) {
			actions.add( AC_DRINK );
		}
		return actions;
	}

	@Override
	public void execute( final Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_DRINK )) {

			if (volume > 0) {
				
				float missingHealthPercent = 1f - (hero.HP / (float)hero.HT);

				int curShield = 0;
				if (hero.buff(Barrier.class) != null) curShield = hero.buff(Barrier.class).shielding();
				int maxShield = Math.round(hero.HT *0.2f*hero.pointsInTalent(Talent.SHIELDING_DEW));
				if (hero.hasTalent(Talent.SHIELDING_DEW)){
					float missingShieldPercent = 1f - (curShield / (float)maxShield);
					missingShieldPercent *= 0.2f*hero.pointsInTalent(Talent.SHIELDING_DEW);
					if (missingShieldPercent > 0){
						missingHealthPercent += missingShieldPercent;
					}
				}
				if(hero.hasTalent(Talent.JASMINE_TEA)){
					Buff.affect(hero, Bless.class,hero.pointsInTalent(Talent.JASMINE_TEA)*2+1);
				}
				if(hero.pointsNegative(Talent.CHILL_WATER)*3> Random.Int(20)){
					Buff.affect(hero, Chill.class,3);
				}

				
				//trimming off 0.01 drops helps with floating point errors
				int dropsNeeded = (int)Math.ceil((missingHealthPercent / 0.05f) - 0.01f);
				dropsNeeded = (int)GameMath.gate(1, dropsNeeded, volume);
				if(hero.hasTalent(Talent.QUALITY_ABSORPTION)){
					int dropsNeeded1 = (int)Math.ceil((missingHealthPercent/(0.05f+ (float)((hero.pointsInTalent(Talent.QUALITY_ABSORPTION)+1)/hero.HT))) - 0.01f);
					dropsNeeded1 = (int)GameMath.gate(1, dropsNeeded1, volume);
					dropsNeeded=Math.max(1,dropsNeeded1);
				}
				if (Dewdrop.consumeDew(dropsNeeded, hero, true)){
					volume -= dropsNeeded;
					Catalog.countUses(Dewdrop.class, dropsNeeded);
					if(hero.pointsInTalent(Talent.WATER_ISFOOD)>=2){
						Buff.affect(hero, Hunger.class).affectHunger(dropsNeeded*10);
						if(hero.pointsInTalent(Talent.WATER_ISFOOD)==3){
							Talent.onFoodEaten(hero,0,null);
						}
					}

					hero.spend(TIME_TO_DRINK);
					hero.busy();

					Sample.INSTANCE.play(Assets.Sounds.DRINK);
					hero.sprite.operate(hero.pos);

					updateQuickslot();
				}


			} else {
				GLog.w( Messages.get(this, "empty") );
			}

		}
	}

	@Override
	public String info() {
		String info = super.info();

		if (volume == 0){
			info += "\n\n" + Messages.get(this, "desc_water");
		} else {
			info += "\n\n" + Messages.get(this, "desc_heal");
		}

		if (isFull()){
			info += "\n\n" + Messages.get(this, "desc_full");
		}

		return info;
	}

	public void empty() {
		volume = 0;
		updateQuickslot();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public boolean isFull() {
		//return volume >= MAX_VOLUME;
		return volume >= 20;
	}

	public void collectDew( Dewdrop dew ) {

		GLog.i( Messages.get(this, "collected") );
		volume += dew.quantity;
		if (volume >= MAX_VOLUME+hero.pointsInTalent(Talent.MILITARY_WATERSKIN)*15) {
			volume = MAX_VOLUME+hero.pointsInTalent(Talent.MILITARY_WATERSKIN)*15;
			GLog.p( Messages.get(this, "full") );
		}

		updateQuickslot();
	}

	public void fill() {

		if (hero.hasTalent(Talent.MILITARY_WATERSKIN)) {
			volume = MAX_VOLUME+hero.pointsInTalent(Talent.MILITARY_WATERSKIN)*15;
		}else{
			volume = MAX_VOLUME;
		}
		updateQuickslot();
	}

	@Override
	public String status() {
		if(hero != null){
			return Messages.format( TXT_STATUS, volume, MAX_VOLUME+hero.pointsInTalent(Talent.MILITARY_WATERSKIN)*15);
		}else{
			return Messages.format( TXT_STATUS, volume, MAX_VOLUME);
		}

	}

}
