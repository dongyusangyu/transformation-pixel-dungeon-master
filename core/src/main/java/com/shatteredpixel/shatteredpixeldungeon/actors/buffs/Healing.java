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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class Healing extends Buff {

	private int healingLeft;
	private int latestHealingSource;
	
	private float percentHealPerTick;
	private int flatHealPerTick;

	private int bloodVialLimitingLevel = -1;
	private static final int LEGACY_DYNAMIC_VIAL_LIMIT = -2;
	
	{
		//unlike other buffs, this one acts after the hero and takes priority against other effects
		//healing is much more useful if you get some of it off before taking damage
		actPriority = HERO_PRIO - 1;
		
		type = buffType.POSITIVE;
	}
	
	@Override
	public boolean act(){
        boolean rest = target.HP < target.HT;
        target.heal(healingThisTick());
        if (target.HP == target.HT && target instanceof Hero && rest) {
            ((Hero) target).resting = false;
        }


		healingLeft -= healingThisTick();
		
		if (healingLeft <= 0){
			if (target instanceof Hero) {
				((Hero) target).resting = false;
			}
			detach();
		}
		
		spend( TICK );
		
		return true;
	}
	
	private int healingThisTick(){
		int heal = (int)GameMath.gate(1,
				Math.round(healingLeft * percentHealPerTick) + flatHealPerTick,
				healingLeft);
		if (bloodVialLimitingLevel != -1){
			int limit = bloodVialLimitingLevel == LEGACY_DYNAMIC_VIAL_LIMIT
					? VialOfBlood.maxHealPerTurn()
					: VialOfBlood.maxHealPerTurn(bloodVialLimitingLevel);
			heal = Math.min(heal, limit);
		}
		return heal;
	}

	public void setHeal(int amount, float percentPerTick, int flatPerTick){
		//multiple sources of healing do not overlap, but do combine the best of their properties
		latestHealingSource = amount;
		bloodVialLimitingLevel = -1;
		healingLeft = Math.max(healingLeft, amount);
		percentHealPerTick = Math.max(percentHealPerTick, percentPerTick);
		flatHealPerTick = Math.max(flatHealPerTick, flatPerTick);
	}

	public void applyVialEffect(){
		bloodVialLimitingLevel = VialOfBlood.bloodVialLevel();
		if (bloodVialLimitingLevel != -1){
			int boostedSource = Math.round(latestHealingSource * VialOfBlood.totalHealMultiplier());
			healingLeft = Math.max(healingLeft, boostedSource);
		}
	}
	
	public void increaseHeal( int amount ){
		healingLeft += amount;
	}
	
	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add( CharSprite.State.HEALING );
		else    target.sprite.remove( CharSprite.State.HEALING );
	}
	
	private static final String LEFT = "left";
	private static final String PERCENT = "percent";
	private static final String FLAT = "flat";

	private static final String HEALING_LIMITED = "healing_limited";
	private static final String VIAL_LEVEL = "vial_level";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, healingLeft);
		bundle.put(PERCENT, percentHealPerTick);
		bundle.put(FLAT, flatHealPerTick);
		bundle.put(VIAL_LEVEL, bloodVialLimitingLevel);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		healingLeft = bundle.getInt(LEFT);
		percentHealPerTick = bundle.getFloat(PERCENT);
		flatHealPerTick = bundle.getInt(FLAT);
		if (bundle.contains(VIAL_LEVEL)){
			bloodVialLimitingLevel = bundle.getInt(VIAL_LEVEL);
		} else if (bundle.getBoolean(HEALING_LIMITED)){
			// Older saves only recorded a flag, so preserve their dynamic limit.
			bloodVialLimitingLevel = LEGACY_DYNAMIC_VIAL_LIMIT;
		}
	}
	
	@Override
	public int icon() {
		return BuffIndicator.HEALING;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(healingLeft);
	}
	
	@Override
	public String desc() {
		return Messages.get(this, "desc", healingThisTick(), healingLeft);
	}
}
