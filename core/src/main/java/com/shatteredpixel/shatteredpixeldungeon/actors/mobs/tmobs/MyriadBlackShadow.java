package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerMobRules;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.MyriadBlackShadowSprite;

/** Rare, fully black guard variant whose split offspring are fragile tower echoes. */
public class MyriadBlackShadow extends AlienatedPrismaticGuard {

	{
		spriteClass = MyriadBlackShadowSprite.class;
		loot = ScrollOfTransmutation.class;
		lootChance = 1f;
	}

	@Override
	protected Mob createSplitOffspring() {
		return TowerMobRules.createMyriadEcho();
	}

	@Override
	public float lootChance() {
		return 1f;
	}
}
