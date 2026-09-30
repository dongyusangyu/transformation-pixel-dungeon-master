package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SoulRoastMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.BronzeWatch;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** A separate fire debuff: normal Burning immunity and water do not extinguish it. */
public class CursedBurning extends Buff {

	public static final float DURATION = 4f;
	private static final String REMAINING = "remaining";
	private static final String NEXT_HITS = "next_hits";
	private static final String ITEM_BURN_IN = "item_burn_in";
	private float remaining;
	private int nextHits = 2;
	private int itemBurnIn = 2;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	public static CursedBurning apply(Char target, float turns) {
		if (target == null || turns <= 0 || !target.isAlive()
				|| target.isImmune(CursedBurning.class)) return null;
		float duration = BronzeWatch.adjustDuration(target, turns)
				* target.resist(CursedBurning.class)
				* CursedFlameDamage.protectionFactor(target);
		// The ring's damage marker also controls debuff duration.
		duration *= target.resist(CursedFlameDamage.class);
		if (duration <= 0) return null;
		CursedBurning burn = target.buff(CursedBurning.class);
		if (burn == null) {
			burn = new CursedBurning();
			if (!burn.attachTo(target)) return null;
			burn.itemBurnIn = Random.IntRange(2, 3);
			burn.burnItem();
		}
		burn.remaining = Math.max(burn.remaining, duration);
		if (burn.remaining <= 0) burn.detach();
		return burn;
	}

	public float remaining() { return remaining; }

	@Override
	public boolean act() {
		if (target == null || !target.isAlive() || target.isImmune(CursedBurning.class)) {
			detach();
			return true;
		}
		if (remaining > 0) {
			for (int i = 0; i < nextHits && target != null && target.isAlive(); i++) {
				CursedFlameDamage.apply(target, CursedFlameDamage.rollBurning(target), this);
			}
			nextHits = nextHits == 2 ? 1 : 2;
			if (--itemBurnIn <= 0) {
				burnItem();
				itemBurnIn = Random.IntRange(2, 3);
			}
		}
		if (target == null || !target.isAlive()) return true;
		remaining -= TICK;
		if (remaining <= 0) detach();
		else spend(TICK);
		return true;
	}

	private void burnItem() {
		if (target instanceof Hero) {
			Hero hero = (Hero) target;
			if (hero.belongings == null || hero.belongings.backpack == null
					|| hero.belongings.lostInventory()) return;
			ArrayList<Item> candidates = new ArrayList<>();
			for (Item item : hero.belongings.backpack.items) {
				if (!item.unique && (item instanceof Scroll || item instanceof MysteryMeat
						|| item instanceof FrozenCarpaccio)) {
					candidates.add(item);
				}
			}
			if (candidates.isEmpty()) return;
			Item burned = Random.element(candidates).detach(hero.belongings.backpack);
			if (burned == null) return;
			if (Dungeon.level != null) {
				GLog.w(Messages.capitalize(Messages.get(this, "burnsup", burned.title())));
			}
			if (burned instanceof MysteryMeat || burned instanceof FrozenCarpaccio) {
				SoulRoastMeat cooked = SoulRoastMeat.cook(burned, burned.quantity());
				if (Dungeon.level == null) {
					hero.belongings.backpack.items.add(cooked);
				} else if (!cooked.collect(hero.belongings.backpack)) {
					Dungeon.level.drop(cooked, hero.pos).sprite.drop();
				}
			}
		} else if (target instanceof Thief) {
			Thief thief = (Thief) target;
			Item item = thief.item;
			if (item == null || item.unique) return;
			if (item instanceof Scroll) {
				if (item.quantity() > 1) item.quantity(item.quantity() - 1);
				else thief.item = null;
			}
			else if (item instanceof MysteryMeat || item instanceof FrozenCarpaccio) {
				if (item.quantity() == 1) {
					thief.item = SoulRoastMeat.cook(item, 1);
				} else if (Dungeon.level != null) {
					item.quantity(item.quantity() - 1);
					Dungeon.level.drop(SoulRoastMeat.cook(item, 1), thief.pos).sprite.drop();
				}
			}
		}
	}

	@Override public int icon() { return BuffIndicator.CURSE_BURNING; }
	@Override public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.CURSED_BURNING);
		else target.sprite.remove(CharSprite.State.CURSED_BURNING);
	}
	@Override public String iconTextDisplay() { return Integer.toString((int)Math.ceil(remaining)); }
	@Override public float iconFadePercent() { return Math.max(0, (DURATION - remaining)/DURATION); }
	@Override public String desc() { return Messages.get(this, "desc", dispTurns(remaining)); }

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(REMAINING, remaining);
		bundle.put(NEXT_HITS, nextHits);
		bundle.put(ITEM_BURN_IN, itemBurnIn);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		remaining = bundle.getFloat(REMAINING);
		nextHits = bundle.getInt(NEXT_HITS);
		itemBurnIn = bundle.getInt(ITEM_BURN_IN);
	}
}
