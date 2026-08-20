package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

final class UpgradeExtraction {

	private UpgradeExtraction() {
	}

	static boolean canExtract(Item item) {
		return !(item instanceof MissileWeapon)
				&& item.isUpgradable() && item.upgradeScrollUses > 0;
	}

	static boolean canExtract(Item item, Hero hero) {
		if (!item.isUpgradable()) return false;
		if (item instanceof MissileWeapon) {
			return MissileWeapon.UpgradedSetTracker.availableUpgradeScrollUses(
					hero, (MissileWeapon) item) > 0;
		}
		return item.upgradeScrollUses > 0;
	}

	static boolean canExtract(Item item, MissileWeapon.UpgradedSetTracker tracker) {
		if (!item.isUpgradable()) return false;
		if (item instanceof MissileWeapon && tracker != null) {
			return tracker.availableUpgradeScrollUses((MissileWeapon) item) > 0;
		}
		return item.upgradeScrollUses > 0;
	}

	static int extractUpgradeUses(Item item) {
		if (item instanceof MissileWeapon) {
			throw new IllegalArgumentException("Missile extraction requires a set tracker");
		}
		int extracted = item.upgradeScrollUses;
		degradeAndClear(item, extracted);
		return extracted;
	}

	static int extractUpgradeUses(Item item, Bag inventory, Hero hero) {
		int extracted = item instanceof MissileWeapon
				? MissileWeapon.UpgradedSetTracker.consumeUpgradeScrollUses(
						hero, (MissileWeapon) item)
				: item.upgradeScrollUses;
		degradeAndClear(item, extracted);

		if (item instanceof MissileWeapon) {
			MissileWeapon selected = (MissileWeapon) item;
			if (inventory != null && selected.setID != MissileWeapon.UNASSIGNED_SET_ID) {
				degradeMissileSet(inventory, selected, extracted);
			}
			MissileWeapon.UpgradedSetTracker.setCanonicalLevel(
					hero, selected, selected.trueLevel());
		}
		return extracted;
	}

	static int extractUpgradeUses(Item item, Bag inventory,
			MissileWeapon.UpgradedSetTracker tracker) {
		int extracted = item instanceof MissileWeapon && tracker != null
				? tracker.consumeUpgradeScrollUses((MissileWeapon) item)
				: item.upgradeScrollUses;
		degradeAndClear(item, extracted);

		if (item instanceof MissileWeapon) {
			MissileWeapon selected = (MissileWeapon) item;
			if (inventory != null && selected.setID != MissileWeapon.UNASSIGNED_SET_ID) {
				degradeMissileSet(inventory, selected, extracted);
			}
			if (tracker != null) tracker.setCanonicalLevel(selected, selected.trueLevel());
		}
		return extracted;
	}

	private static void degradeMissileSet(Bag bag, MissileWeapon selected, int levels) {
		for (Item item : bag.items.toArray(new Item[0])) {
			if (item instanceof MissileWeapon) {
				MissileWeapon missile = (MissileWeapon) item;
				if (missile != selected && missile.setID == selected.setID) {
					degradeAndClear(missile, levels);
				}
			} else if (item instanceof Bag) {
				degradeMissileSet((Bag) item, selected, levels);
			}
		}
	}

	private static void degradeAndClear(Item item, int levels) {
		item.degrade(Math.min(Math.max(0, item.trueLevel()), levels));
		item.upgradeScrollUses = 0;
	}
}
