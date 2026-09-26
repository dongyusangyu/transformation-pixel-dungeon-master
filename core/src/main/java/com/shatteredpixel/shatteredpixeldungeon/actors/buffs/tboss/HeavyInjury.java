package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** The elf's pounce wound blocks healing, but not shielding. */
public class HeavyInjury extends Buff implements Char.HealingBlocker {
	private static final String STACKS = "elf_heavy_injury_stacks";
	private int stacks;
	{ type = buffType.NEGATIVE; announced = true; }

	public static HeavyInjury apply(Char target) {
		HeavyInjury injury = Buff.affect(target, HeavyInjury.class);
		if (injury != null) {
			injury.stacks = 10;
			injury.timeToNow();
		}
		return injury;
	}

	public int stacks() { return stacks; }
	@Override public boolean blocksIncomingHealing() { return stacks > 0; }
	@Override public boolean act() {
		if (--stacks <= 0) detach();
		else spend(TICK);
		return true;
	}
	@Override public int icon() { return BuffIndicator.BLEEDING; }
	@Override public String iconTextDisplay() { return Integer.toString(stacks); }
	@Override public String desc() { return Messages.get(this, "desc", stacks); }
	@Override public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STACKS, stacks);
	}
	@Override public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		stacks = Math.max(0, Math.min(10, bundle.getInt(STACKS)));
	}
}
