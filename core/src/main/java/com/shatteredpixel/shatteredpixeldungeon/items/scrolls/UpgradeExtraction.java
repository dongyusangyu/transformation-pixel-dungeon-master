package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

final class UpgradeExtraction {

	private UpgradeExtraction() {
	}

	static boolean canExtract(Item item) {
		return item.isUpgradable() && item.upgradeScrollUses > 0;
	}

	static int extractUpgradeUses(Item item) {
		return extractUpgradeUses(item, null);
	}

	static int extractUpgradeUses(Item item, Bag inventory) {
		int extracted = item.upgradeScrollUses;
		degradeAndClear(item, extracted);

		if (item instanceof MissileWeapon && inventory != null) {
			MissileWeapon selected = (MissileWeapon) item;
			if (selected.setID != MissileWeapon.UNASSIGNED_SET_ID) {
				degradeMissileSet(inventory, selected, extracted);
			}
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
