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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;


public class HeroDisguise extends FlavourBuff {

	{
		announced = true;
	}

	private HeroClass cls = null;
	private int skin = 0;
	private boolean magicGirlDisguise = false;

	public static float DURATION = 1000f;

	public HeroClass getDisguise(){
		return cls;
	}
	public int getSkin(){
		return skin;
	}
	public boolean isMagicGirlDisguise(){
		return magicGirlDisguise;
	}

	@Override
	public int icon() {
		return BuffIndicator.DISGUISE;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (target instanceof Hero && target.sprite instanceof HeroSprite){
			Hero targetHero = (Hero)target;
			if (cls == null) {
				if(targetHero.hasTalent(Talent.MAGIC_GIRL)){
					cls = targetHero.heroClass;
					magicGirlDisguise = true;
				}else{
					magicGirlDisguise = false;
					//HeroClass[] canDisguise=new HeroClass[]{HeroClass.DUELIST,HeroClass.WARRIOR,HeroClass.MAGE,HeroClass.HUNTRESS,HeroClass.ROGUE};
					do {
						//cls = Random.oneOf(canDisguise);
						cls = Random.oneOf(HeroClass.values());
						skin = Random.Int(cls.getSkinNums());
					} while (cls == targetHero.heroClass || cls == HeroClass.RATKING);}
			} else if (targetHero.hasTalent(Talent.MAGIC_GIRL) && cls == targetHero.heroClass) {
				magicGirlDisguise = true;
			}

			if (on) {
				if (magicGirlDisguise){
					((HeroSprite)target.sprite).magic_girl();
				}else{
					((HeroSprite)target.sprite).disguise(cls,skin);}
			}
			else    ((HeroSprite)target.sprite).disguise(targetHero.heroClass, Dungeon.skin);
			updateHeroView(targetHero);
		}
	}

	private void updateHeroView(Hero targetHero){
		GameScene.updateAvatar();
		if (Dungeon.level != null && Dungeon.hero == targetHero && GameScene.fogReady()) {
			Dungeon.observe();
		}
	}

	private static final String CLASS = "class";
	private static final String SKIN = "skin";
	private static final String MAGIC_GIRL = "magic_girl";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CLASS, cls);
		bundle.put(SKIN, skin);
		bundle.put(MAGIC_GIRL, magicGirlDisguise);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		cls = bundle.getEnum(CLASS, HeroClass.class);
		skin = bundle.getInt(SKIN);
		magicGirlDisguise = bundle.getBoolean(MAGIC_GIRL);
	}
}
