package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.GentlemanElfArena;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** A single, persistent risk state: deal and receive 20% more damage. */
public class Exhilaration extends Buff implements Char.DamageMultiplier {
	{ type = buffType.POSITIVE; announced = true; }

	public static Exhilaration affect(Char target) {
		if (GentlemanElf.isFinalDuelDrunk(target)) {
			Drunkenness.affect(target);
			return null;
		}
		Drunkenness other = target.buff(Drunkenness.class);
		if (other != null) other.detach();
		return Buff.affect(target, Exhilaration.class);
	}
	public static float encounterMovementMultiplier(Hero hero) {
		return movementMultiplier(hero != null && hero.buff(Exhilaration.class) != null,
				GentlemanElf.isEncounterActive());
	}

	static float movementMultiplier(boolean hasBuff, boolean encounterActive) {
		return hasBuff && encounterActive ? 1.5f : 1f;
	}

	@Override public boolean act() { spend(TICK); return true; }
	@Override public int icon() { return BuffIndicator.EXHILARATION; }
	@Override public float outgoingDamageMultiplier(Object source, DamageTag... tags) { return 1.2f; }
	@Override public float incomingDamageMultiplier(Object source, DamageTag... tags) {
		return source instanceof GentlemanElfArena ? 1f : 1.2f;
	}
}
