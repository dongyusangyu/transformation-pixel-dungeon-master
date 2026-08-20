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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class QuickSlot {

	/**
	 * Slots contain objects which are also in a player's inventory. The one exception to this is when quantity is 0,
	 * which can happen for a stackable item that has been 'used up', these are referred to as placeholders.
	 */

	//note that the current max size is coded at 6, due to UI constraints, but it could be much much bigger with no issue.
	public static int SIZE = 6;
	private Item[] slots = new Item[SIZE];


	//direct array interaction methods, everything should build from these methods.
	public void setSlot(int slot, Item item){
		clearItem(item); //we don't want to allow the same item in multiple slots.
		slots[slot] = item;
	}

	public void clearSlot(int slot){
		slots[slot] = null;
	}

	public void reset(){
		slots = new Item[SIZE];
	}

	public Item getItem(int slot){
		return slots[slot];
	}

	//utility methods, for easier use of the internal array.
	public int getSlot(Item item) {
		for (int i = 0; i < SIZE; i++) {
			if (getItem(i) == item) {
				return i;
			}
		}
		return -1;
	}

	public Item[] getItems(){
		return slots;
	}

	public Boolean isPlaceholder(int slot){
		return getItem(slot) != null && getItem(slot).quantity() == 0;
	}

	public Boolean isNonePlaceholder(int slot){
		return getItem(slot) != null && getItem(slot).quantity() > 0;
	}

	public void clearItem(Item item){
		if (contains(item)) {
			clearSlot(getSlot(item));
		}
	}

	public boolean contains(Item item){
		return getSlot(item) != -1;
	}

	public void replacePlaceholder(Item item) {
		for (int i = 0; i < SIZE; i++) {
			if (isPlaceholder(i) && item.isSimilar(getItem(i))) {
				setSlot(i, item);
			}
		}
	}

	public void convertToPlaceholder(Item item){
		
		if (contains(item)) {
			Item placeholder = item.virtual();
			if (placeholder == null) return;
			
			for (int i = 0; i < SIZE; i++) {
				if (getItem(i) == item) setSlot(i, placeholder);
			}
		}
	}

	public Item randomNonePlaceholder(){

		ArrayList<Item> result = new ArrayList<>();
		for (int i = 0; i < SIZE; i ++) {
			if (getItem(i) != null && !isPlaceholder(i)) {
				result.add(getItem(i));
			}
		}
		return Random.element(result);
	}

	private final String PLACEHOLDERS = "placeholders";
	private final String PLACEMENTS = "placements";
	private final String RANKING_ITEMS = "ranking_quickslot_items";
	private final String RANKING_PLACEMENTS = "ranking_quickslot_placements";
	private static final String ITEM_BUNDLE_KEY = "item";
	private static final String ITEM_QUICKSLOT_KEY = "quickslotpos";

	/**
	 * Placements array is used as order is preserved while bundling, but exact index is not, so if we
	 * bundle both the placeholders (which preserves their order) and an array telling us where the placeholders are,
	 * we can reconstruct them perfectly.
	 */

	public void storePlaceholders(Bundle bundle){
		ArrayList<Item> placeholders = new ArrayList<>(SIZE);
		boolean[] placements = new boolean[SIZE];

		for (int i = 0; i < SIZE; i++) {
			if (isPlaceholder(i)) {
				placeholders.add(getItem(i));
				placements[i] = true;
			}
		}
		bundle.put( PLACEHOLDERS, placeholders );
		bundle.put( PLACEMENTS, placements );
	}

	public void restorePlaceholders(Bundle bundle){
		Collection<Bundlable> placeholders = bundle.getCollection(PLACEHOLDERS);
		boolean[] placements = bundle.getBooleanArray( PLACEMENTS );

		int i = 0;
		for (Bundlable item : placeholders){
			while (!placements[i]){
				i++;
			}
			setSlot( i, (Item)item );
			i++;
		}

	}

	public void storeRankingSnapshot(Bundle bundle) {
		ArrayList<Item> items = new ArrayList<>(SIZE);
		boolean[] placements = new boolean[SIZE];
		for (int i = 0; i < SIZE; i++) {
			if (slots[i] != null) {
				items.add(slots[i].duplicate());
				placements[i] = true;
			}
		}
		bundle.put(RANKING_ITEMS, items);
		bundle.put(RANKING_PLACEMENTS, placements);
	}

	public boolean restoreRankingSnapshot(Bundle bundle) {
		return restoreRankingSnapshot(bundle, null);
	}

	public boolean restoreRankingSnapshot(Bundle bundle, Iterable<Item> inventory) {
		if (!bundle.contains(RANKING_ITEMS) || !bundle.contains(RANKING_PLACEMENTS)) {
			return false;
		}

		Collection<Bundlable> items = bundle.getCollection(RANKING_ITEMS);
		boolean[] placements = bundle.getBooleanArray(RANKING_PLACEMENTS);
		Set<Item> reboundItems = new HashSet<>();
		reset();
		int slot = 0;
		for (Bundlable bundled : items) {
			while (slot < SIZE && (slot >= placements.length || !placements[slot])) {
				slot++;
			}
			if (slot >= SIZE) {
				break;
			}
			Item snapshot = (Item) bundled;
			Item restored = matchingInventoryItem(inventory, snapshot, reboundItems);
			if (restored != null) {
				setSlot(slot, restored);
			} else if (inventory == null) {
				setSlot(slot, snapshot);
			}
			slot++;
		}
		return true;
	}

	private Item matchingInventoryItem(Iterable<Item> inventory, Item snapshot, Set<Item> usedItems) {
		if (inventory == null) {
			return null;
		}
		for (Item item : inventory) {
			if (!usedItems.contains(item) && rankingSnapshotMatches(item, snapshot)) {
				usedItems.add(item);
				return item;
			}
		}
		for (Item item : inventory) {
			if (!usedItems.contains(item) && item.isSimilar(snapshot)) {
				usedItems.add(item);
				return item;
			}
		}
		return null;
	}

	static boolean rankingSnapshotMatches(Item item, Item snapshot) {
		if (item == null || snapshot == null || item.getClass() != snapshot.getClass()) {
			return false;
		}
		Bundle itemBundle = new Bundle();
		Bundle snapshotBundle = new Bundle();
		itemBundle.put(ITEM_BUNDLE_KEY, item);
		snapshotBundle.put(ITEM_BUNDLE_KEY, snapshot);
		itemBundle.getBundle(ITEM_BUNDLE_KEY).remove(ITEM_QUICKSLOT_KEY);
		snapshotBundle.getBundle(ITEM_BUNDLE_KEY).remove(ITEM_QUICKSLOT_KEY);
		return itemBundle.contentEquals(snapshotBundle);
	}

}
