package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.BronzeWatch;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArena;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/** A single, persistent risk state: deal and receive 20% less damage. */
public class Drunkenness extends Buff implements Char.DamageMultiplier {
	public static final float FRUIT_DURATION = 50f;
	private static final String PERMANENT = "permanent";
	private static final String LEFT = "left";
	private static final String DURATION = "duration";

	{ type = buffType.POSITIVE; announced = true; }

	private boolean permanent;
	private int left;
	private int duration;

	public static Drunkenness affect(Char target) {
		Exhilaration other = target.buff(Exhilaration.class);
		if (other != null) other.detach();
		Drunkenness drunk = Buff.affect(target, Drunkenness.class);
		if (!drunk.permanent) drunk.timeToNow();
		drunk.permanent = true;
		drunk.left = 0;
		drunk.duration = 0;
		return drunk;
	}

	public static Drunkenness affectTemporary(Char target) {
		return affectTemporary(target, FRUIT_DURATION);
	}

	public static Drunkenness affectTemporary(Char target, float duration) {
		Exhilaration other = target.buff(Exhilaration.class);
		if (other != null) other.detach();
		Drunkenness drunk = Buff.affect(target, Drunkenness.class);
		if (drunk.permanent) return drunk;

		drunk.duration = Math.max(1, Math.round(
				BronzeWatch.adjustDuration(target, duration)));
		drunk.left = drunk.duration;
		drunk.timeToNow();
		drunk.spend(TICK);
		return drunk;
	}

	@Override public boolean act() {
		if (!permanent && --left <= 0) {
			detach();
		} else {
			spend(TICK);
		}
		return true;
	}
	@Override public int icon() { return BuffIndicator.DRUNKENNESS; }
	@Override public float iconFadePercent() {
		return permanent || duration <= 0 ? 0f : Math.max(0f, (duration - left) / (float) duration);
	}
	@Override public String iconTextDisplay() { return permanent ? "" : Integer.toString(left); }
	@Override public String desc() {
		return permanent ? Messages.get(this, "desc") : Messages.get(this, "timed_desc", dispTurns(left));
	}
	@Override public float outgoingDamageMultiplier(Object source, DamageTag... tags) { return 0.8f; }
	@Override public float incomingDamageMultiplier(Object source, DamageTag... tags) {
		return source instanceof GentlemanElfArena ? 1f : 0.8f;
	}

	@Override public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PERMANENT, permanent);
		if (!permanent) {
			bundle.put(LEFT, left);
			bundle.put(DURATION, duration);
		}
	}

	@Override public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		// Saves created before timed fruit drunkenness existed only contained the original permanent buff.
		permanent = !bundle.contains(PERMANENT) || bundle.getBoolean(PERMANENT);
		if (!permanent) {
			left = Math.max(0, bundle.getInt(LEFT));
			duration = Math.max(1, bundle.getInt(DURATION));
		}
	}
}
