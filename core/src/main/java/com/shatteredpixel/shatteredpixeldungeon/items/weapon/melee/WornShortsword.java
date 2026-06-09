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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;



import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class WornShortsword extends MeleeWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.1f;

		tier = 1;
		
		bones = false;
	}
	public static final String AC_RECAST	= "RECAST";

	@Override
	protected int baseChargeUse(Hero hero, Char target){
		if (hero.buff(Sword.CleaveTracker.class) != null){
			return 0;
		} else {
			return 1;
		}
	}
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		//if (level() > 0 && !curseInfusionBonus) {
		if (super.truelevel() > 0) {
			actions.add( AC_RECAST );
		}
		return actions;
	}
	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_RECAST)) {

			curUser = hero;
			GameScene.selectItem(itemSelector);

		}
	}
	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(WornShortsword.class, "prompt");
		}


		@Override
		public boolean itemSelectable(Item item) {
			return (item instanceof MeleeWeapon) && item!=WornShortsword.this && item.isUpgradable();
		}

		@Override
		public void onSelect( final Item item ) {
			if (item != null) {
				recast(item);
			}
		}
	};

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	private void recast(Item item){
		Sample.INSTANCE.play( Assets.Sounds.EVOKE );
		if(this.isEquipped(curUser)){
			boolean second = curUser.belongings.secondWep == this;
			if (second){
				//do this first so that the item can go to a full inventory
				curUser.belongings.secondWep = null;
			}else{
				curUser.belongings.weapon = null;
			}
		}else{
			detach(curUser.belongings.backpack);
		}

		new ScrollOfUpgrade().upgradeItem(item);
		curUser.spend(1f);
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(3+lvl) damage, roughly +55% base dmg, +67% scaling
		int dmgBoost = augment.damageFactor(3 + buffedLvl());
		Sword.cleaveAbility(hero, target, 1, dmgBoost, this);
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = levelKnown ? 3 + buffedLvl() : 3;
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(min()+dmgBoost), augment.damageFactor(max()+dmgBoost));
		} else {
			return Messages.get(this, "typical_ability_desc", min(0)+dmgBoost, max(0)+dmgBoost);
		}
	}

	public String upgradeAbilityStat(int level){
		int dmgBoost = 3 + level;
		return augment.damageFactor(min(level)+dmgBoost) + "-" + augment.damageFactor(max(level)+dmgBoost);
	}

}
