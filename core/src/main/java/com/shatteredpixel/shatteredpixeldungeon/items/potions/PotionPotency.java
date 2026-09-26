package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;

public final class PotionPotency {

	private PotionPotency() {
	}

	public static boolean drunk(Hero hero) {
		return GentlemanElf.isEncounterDrunk(hero);
	}

	public static int amount(boolean drunk, int normal) {
		return drunk ? normal / 2 : normal;
	}

	public static float duration(boolean drunk, float normal) {
		return drunk ? normal / 2f : normal;
	}
}
