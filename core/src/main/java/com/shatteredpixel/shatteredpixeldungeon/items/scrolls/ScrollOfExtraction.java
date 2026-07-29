package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class ScrollOfExtraction extends InventoryScroll {

	{
		image = EXItemSpriteSheet.SCROLL_EXTRACTION;
		//icon = ItemSpriteSheet.Icons.SCROLL_UPGRADE;
		preferredBag = Belongings.Backpack.class;
	}

	@Override
	public void reset() {
		super.reset();
		image = EXItemSpriteSheet.SCROLL_EXTRACTION;
	}

	@Override
	public boolean isKnown() {
		return true;
	}

	@Override
	public int value() {
		return 40 * quantity;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return UpgradeExtraction.canExtract(item);
	}

	@Override
	protected void onItemSelected(Item item) {
		int extracted = UpgradeExtraction.extractUpgradeUses(item, curUser.belongings.backpack);
		if (item instanceof MissileWeapon) {
			MissileWeapon missile = (MissileWeapon) item;
			MissileWeapon.UpgradedSetTracker tracker =
					curUser.buff(MissileWeapon.UpgradedSetTracker.class);
			if (tracker != null && tracker.levelThresholds.containsKey(missile.setID)) {
				tracker.levelThresholds.put(missile.setID, missile.trueLevel());
			}
		}
		Item upgrades = new ScrollOfUpgrade().quantity(extracted);
		if (!upgrades.collect(curUser.belongings.backpack)) {
			Dungeon.level.drop(upgrades, curUser.pos);
		}
		Item.updateQuickslot();
		GLog.p(Messages.get(this, "extract", extracted));
	}
}
