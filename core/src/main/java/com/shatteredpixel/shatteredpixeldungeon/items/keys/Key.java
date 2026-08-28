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

package com.shatteredpixel.shatteredpixeldungeon.items.keys;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public abstract class Key extends Item {

	public static final float TIME_TO_UNLOCK = 1f;
	
	{
		stackable = true;
		unique = true;
	}

	public int depth;
	public int branch;
	private boolean legacyLocation;

	protected Key() {
		depth = Dungeon.depth;
		branch = Dungeon.branch;
	}

	public boolean migrateLegacyLocation(int currentDepth, int currentBranch) {
		if (!legacyLocation) {
			return false;
		}
		legacyLocation = false;
		if (currentBranch == TowerLevel.BRANCH) {
			int towerContentDepth = 16 + Math.min(9, Math.max(0, currentDepth - 1));
			if (depth == towerContentDepth) {
				depth = currentDepth;
				branch = currentBranch;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isSimilar( Item item ) {
		return super.isSimilar(item)
				&& ((Key)item).depth == depth
				&& ((Key)item).branch == branch;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		Catalog.setSeen(getClass());
		Statistics.itemTypesDiscovered.add(getClass());
		GameScene.pickUpJournal(this, pos);
		WndJournal.last_index = 0;
		Notes.add(this);
		Sample.INSTANCE.play( Assets.Sounds.ITEM );
		hero.spendAndNext(pickupDelay());
		GameScene.updateKeyDisplay();
		if (hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey.KeyReplacementTracker.class) != null){
			hero.buff(SkeletonKey.KeyReplacementTracker.class).processExcessKeys();
		}
		return true;
	}

	private static final String DEPTH = "depth";
	private static final String BRANCH = "branch";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DEPTH, depth );
		bundle.put( BRANCH, branch );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		depth = bundle.getInt( DEPTH );
		if (bundle.contains(BRANCH)) {
			branch = bundle.getInt(BRANCH);
			legacyLocation = false;
		} else {
			branch = 0;
			legacyLocation = true;
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}

}
