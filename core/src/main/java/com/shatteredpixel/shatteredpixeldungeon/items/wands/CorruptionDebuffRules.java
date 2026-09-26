package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;

import java.util.HashMap;

/** The wand's single classification table for debuff rolls and resistance weakening. */
public final class CorruptionDebuffRules {
	private CorruptionDebuffRules() {}

	static final HashMap<Class<? extends Buff>, Float> MINOR_DEBUFFS = new HashMap<>();
	static final HashMap<Class<? extends Buff>, Float> MAJOR_DEBUFFS = new HashMap<>();

	static {
		MINOR_DEBUFFS.put(Weakness.class, 2f);
		MINOR_DEBUFFS.put(Vulnerable.class, 2f);
		MINOR_DEBUFFS.put(Cripple.class, 1f);
		MINOR_DEBUFFS.put(Blindness.class, 1f);
		MINOR_DEBUFFS.put(Terror.class, 1f);
		MINOR_DEBUFFS.put(Chill.class, 0f);
		MINOR_DEBUFFS.put(Ooze.class, 0f);
		MINOR_DEBUFFS.put(Roots.class, 0f);
		MINOR_DEBUFFS.put(Vertigo.class, 0f);
		MINOR_DEBUFFS.put(Drowsy.class, 0f);
		MINOR_DEBUFFS.put(Bleeding.class, 0f);
		MINOR_DEBUFFS.put(Burning.class, 0f);
		MINOR_DEBUFFS.put(Poison.class, 0f);

		MAJOR_DEBUFFS.put(Amok.class, 3f);
		MAJOR_DEBUFFS.put(Slow.class, 2f);
		MAJOR_DEBUFFS.put(Hex.class, 2f);
		MAJOR_DEBUFFS.put(Paralysis.class, 1f);
		MAJOR_DEBUFFS.put(Daze.class, 0f);
		MAJOR_DEBUFFS.put(Dread.class, 0f);
		MAJOR_DEBUFFS.put(Charm.class, 0f);
		MAJOR_DEBUFFS.put(MagicalSleep.class, 0f);
		MAJOR_DEBUFFS.put(SoulMark.class, 0f);
		MAJOR_DEBUFFS.put(Corrosion.class, 0f);
		MAJOR_DEBUFFS.put(Frost.class, 0f);
		MAJOR_DEBUFFS.put(CursedBurning.class, 0f);
		MAJOR_DEBUFFS.put(Doom.class, 0f);
	}

	public static float resistanceMultiplier(Char target) {
		float multiplier = 1f;
		for (Buff buff : target.buffs()) {
			if (MAJOR_DEBUFFS.containsKey(buff.getClass())) multiplier *= 0.5f;
			else if (MINOR_DEBUFFS.containsKey(buff.getClass()) || buff.type == Buff.buffType.NEGATIVE)
				multiplier *= 0.75f;
		}
		return multiplier;
	}
}
