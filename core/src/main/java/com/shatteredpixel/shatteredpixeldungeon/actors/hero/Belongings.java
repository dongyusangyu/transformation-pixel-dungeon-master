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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.HikingBackpack;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	public static class Backpack extends Bag {

		private boolean rebalancingFallbackStorage;

		{
			image = ItemSpriteSheet.BACKPACK;
		}
		public int capacity(){
			int cap = super.capacity();
			for (Item item : items){
				if (item instanceof Bag){
					cap++;
				}
			}
			if (owner instanceof Hero && ((Hero) owner).belongings.secondWep != null){
				//secondary weapons still occupy an inv. slot
				cap--;
			}
			return cap;
		}

		public boolean canReserveSecondaryWeaponSlot(Item secondaryWeapon) {
			if (items.size() < capacity() || items.contains(secondaryWeapon)) return true;

			Bag fallback = fallbackStorage();
			if (fallback == null) return false;
			if (fallback.contains(secondaryWeapon)) return true;

			Item overflow = lastDirectOrdinaryItem();
			return overflow != null && fallback.canHold(overflow);
		}

		public void rebalanceFallbackStorage() {
			if (rebalancingFallbackStorage || isLoading() || !(owner instanceof Hero)
					|| owner.buff(LostInventory.class) != null) {
				return;
			}

			Bag fallback = fallbackStorage();
			if (fallback == null || fallback.isLoading()) return;

			rebalancingFallbackStorage = true;
			try {
				// A secondary weapon reserves a main-backpack slot without being
				// stored in the backpack list. Move the hidden item to the fallback bag.
				while (items.size() > capacity()) {
					Item overflow = null;
					for (int i = items.size() - 1; i >= 0; i--) {
						if (!(items.get(i) instanceof Bag)) {
							overflow = items.get(i);
							break;
						}
					}
					if (overflow == null || !fallback.canHold(overflow)) break;

					int quickslot = Dungeon.quickslot.getSlot(overflow);
					overflow.detachAll(this);
					if (!overflow.collect(fallback)) {
						items.add(overflow);
						break;
					}
					if (quickslot >= 0) {
						Item stored = fallback.contains(overflow)
								? overflow
								: ((Hero) owner).belongings.getSimilar(overflow);
						if (stored != null) Dungeon.quickslot.setSlot(quickslot, stored);
					}
				}

				ArrayList<Item> ordered = new ArrayList<>(fallback.items);
				Collections.sort(ordered, Item.itemComparator);
				for (Item item : ordered) {
					if (items.size() >= capacity() && !hasNonFallbackDestination(item)) {
						continue;
					}
					int quickslot = Dungeon.quickslot.getSlot(item);
					item.detachAll(fallback);
					if (!item.collect(this)) {
						fallback.items.add(item);
						Collections.sort(fallback.items, Item.itemComparator);
					}
					if (quickslot >= 0) {
						Item stored = contains(item)
								? item
								: ((Hero) owner).belongings.getSimilar(item);
						if (stored != null) Dungeon.quickslot.setSlot(quickslot, stored);
					}
				}
			} finally {
				rebalancingFallbackStorage = false;
			}
		}

		private boolean hasNonFallbackDestination(Item item) {
			if (items.size() < capacity()) return true;
			for (Item stored : items) {
				if (stored instanceof Bag) {
					Bag bag = (Bag) stored;
					if (!bag.isFallbackStorage() && bag.canHold(item)) return true;
				} else if (item.stackable && item.hasSameExtractionRaidOrigin(stored)
						&& item.isSimilar(stored)) {
					return true;
				}
			}
			return false;
		}

		private Bag fallbackStorage() {
			for (Item item : items) {
				if (item instanceof Bag && ((Bag) item).isFallbackStorage()) {
					return (Bag) item;
				}
			}
			return null;
		}

		private Item lastDirectOrdinaryItem() {
			for (int i = items.size() - 1; i >= 0; i--) {
				Item item = items.get(i);
				if (!(item instanceof Bag)) return item;
			}
			return null;
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			rebalanceFallbackStorage();
		}
	}

	public Backpack backpack;

	@SuppressWarnings("unchecked")
	private static final Class<? extends Bag>[] BAG_TAB_ORDER = new Class[]{
			HikingBackpack.class,
			VelvetPouch.class,
			ScrollHolder.class,
			PotionBandolier.class,
			MagicalHolster.class
	};
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;
	}

	public void onSecondaryWeaponChanged() {
		if (backpack != null) backpack.rebalanceFallbackStorage();
	}

	public KindOfWeapon weapon = null;
	public Armor armor = null;
	//public Artifact artifact = null;
    public KindofMisc artifact = null;
	public KindofMisc misc = null;
	public Ring ring = null;

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;

	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	// we still want to access the raw equipped items in cases where effects should be ignored though,
	// such as when equipping something, showing an interface, or dealing with items from a dead hero

	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	public KindOfWeapon attackingWeapon(){
		if (thrownWeapon != null) return thrownWeapon;
		if (abilityWeapon != null) return abilityWeapon;
		return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		if (lostInvent == val) return;
		lostInvent = val;

		EquipableItem[] equippedItems = {weapon, armor, artifact, misc, ring, secondWep};
		for (EquipableItem item : equippedItems) {
			if (item != null) item.onInventoryAvailabilityChanged(owner);
		}
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	public KindOfWeapon weapon(){
		if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())){
			return weapon;
		} else {
			return null;
		}
	}

	public Armor armor(){
		if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())){
			return armor;
		} else {
			return null;
		}
	}

	public KindofMisc artifact(){
		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){
			return  artifact;
		} else {
			return null;
		}
	}

	public KindofMisc misc(){
		if (!lostInventory() || (misc != null && misc.keptThroughLostInventory())){
			return misc;
		} else {
			return null;
		}
	}

	public Ring ring(){
		if (!lostInventory() || (ring != null && ring.keptThroughLostInventory())){
			return ring;
		} else {
			return null;
		}
	}

	public KindOfWeapon secondWep(){
		if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
			return secondWep;
		} else {
			return null;
		}
	}

	// ***
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String MISC       = "misc";
	private static final String RING       = "ring";

	private static final String SECOND_WEP = "second_wep";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( ARTIFACT, artifact );
		bundle.put( MISC, misc );
		bundle.put( RING, ring );
		bundle.put( SECOND_WEP, secondWep );
	}
	
	public void restoreFromBundle( Bundle bundle ) {
		
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon() != null)       weapon().activate(owner);
		
		armor = (Armor)bundle.get( ARMOR );
		if (armor() != null)        armor().activate( owner );

		artifact = (KindofMisc) bundle.get(ARTIFACT);
		if (artifact() != null)     artifact().activate(owner);

		misc = (KindofMisc) bundle.get(MISC);
		if (misc() != null)         misc().activate( owner );

		ring = (Ring) bundle.get(RING);
		if (ring() != null)         ring().activate( owner );

		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
		if (secondWep() != null)    secondWep().activate(owner);
		onSecondaryWeaponChanged();
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
	public ArrayList<Bag> getBags(){
		ArrayList<Bag> result = new ArrayList<>();

		result.add(backpack);

		for (Class<? extends Bag> bagClass : BAG_TAB_ORDER) {
			for (Item item : backpack.items) {
				if (bagClass.isInstance(item) && !result.contains(item)) {
					result.add((Bag) item);
				}
			}
		}
		for (Item item : backpack.items) {
			if (item instanceof Bag && !result.contains(item)) {
				result.add((Bag) item);
			}
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return (T) item;
				}
			}
		}
		
		return null;
	}

	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
		ArrayList<T> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add((T) item);
				}
			}
		}

		return result;
	}
	
	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}
		
		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}
	
	public void observe() {
		if (weapon() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && weapon() instanceof Weapon){
				((Weapon) weapon()).setIDReady();
			} else {
				weapon().identify();
				Badges.validateItemLevelAquired(weapon());
			}
		}
		if (secondWep() != null){
			if (ShardOfOblivion.passiveIDDisabled() && secondWep() instanceof Weapon){
				((Weapon) secondWep()).setIDReady();
			} else {
				secondWep().identify();
				Badges.validateItemLevelAquired(secondWep());
			}
		}
		if (armor() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				armor().setIDReady();
			} else {
				armor().identify();
				Badges.validateItemLevelAquired(armor());
			}
		}
		if (artifact() != null) {
			//oblivion shard does not prevent artifact IDing
			artifact().identify();
			Badges.validateItemLevelAquired(artifact());
		}
		if (misc() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && misc() instanceof Ring){
				((Ring) misc()).setIDReady();
			} else {
				misc().identify();
				Badges.validateItemLevelAquired(misc());
			}
		}
		if (ring() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				ring().setIDReady();
			} else {
				ring().identify();
				Badges.validateItemLevelAquired(ring());
			}
		}
		if (ShardOfOblivion.passiveIDDisabled()){
			GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready_worn"));
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, armor(), weapon(), artifact(), misc(), ring(), secondWep());
	}
	
	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		return Random.element( backpack.items );
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {weapon, armor, artifact, misc, ring, secondWep};
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (index) {
			case 0:
				equipped[0] = weapon = null;
				break;
			case 1:
				equipped[1] = armor = null;
				break;
			case 2:
				equipped[2] = artifact = null;
				break;
			case 3:
				equipped[3] = misc = null;
				break;
			case 4:
				equipped[4] = ring = null;
				break;
			case 5:
				equipped[5] = secondWep = null;
				onSecondaryWeaponChanged();
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
