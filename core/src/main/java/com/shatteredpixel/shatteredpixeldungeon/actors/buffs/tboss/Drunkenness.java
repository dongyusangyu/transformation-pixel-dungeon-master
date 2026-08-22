package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArena;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** A single, persistent risk state: deal and receive 20% less damage. */
public class Drunkenness extends Buff implements Char.DamageMultiplier {
	{ type = buffType.POSITIVE; announced = true; }

	public static Drunkenness affect(Char target) {
		Exhilaration other = target.buff(Exhilaration.class);
		if (other != null) other.detach();
		return Buff.affect(target, Drunkenness.class);
	}

	@Override public boolean act() { spend(TICK); return true; }
	@Override public int icon() { return BuffIndicator.DRUNKENNESS; }
	@Override public float outgoingDamageMultiplier(Object source, DamageTag... tags) { return 0.8f; }
	@Override public float incomingDamageMultiplier(Object source, DamageTag... tags) {
		return source instanceof GentlemanElfArena ? 1f : 0.8f;
	}
}
