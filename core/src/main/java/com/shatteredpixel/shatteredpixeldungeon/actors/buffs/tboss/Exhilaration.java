package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArena;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** A single, persistent risk state: deal and receive 20% more damage. */
public class Exhilaration extends Buff implements Char.DamageMultiplier {
	{ type = buffType.POSITIVE; announced = true; }

	public static Exhilaration affect(Char target) {
		Drunkenness other = target.buff(Drunkenness.class);
		if (other != null) other.detach();
		return Buff.affect(target, Exhilaration.class);
	}

	@Override public boolean act() { spend(TICK); return true; }
	@Override public int icon() { return BuffIndicator.EXHILARATION; }
	@Override public float outgoingDamageMultiplier(Object source, DamageTag... tags) { return 1.2f; }
	@Override public float incomingDamageMultiplier(Object source, DamageTag... tags) {
		return source instanceof GentlemanElfArena ? 1f : 1.2f;
	}
}
