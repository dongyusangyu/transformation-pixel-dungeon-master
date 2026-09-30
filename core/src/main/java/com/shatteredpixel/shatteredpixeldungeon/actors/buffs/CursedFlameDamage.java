package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;
import com.watabou.utils.Random;

/** Shared resistance calculation for environmental and lingering cursed fire. */
public final class CursedFlameDamage {

	private CursedFlameDamage() {}

	public static int roll(Char target) {
		return Random.IntRange(1, 5 + Dungeon.scalingDepth()/3);
	}

	public static int rollBurning(Char target) {
		int floor = Dungeon.branch == TowerLevel.BRANCH ? Dungeon.depth : Dungeon.scalingDepth();
		return Random.IntRange(1, maxBurningDamageForDepth(floor));
	}

	static int maxBurningDamageForDepth(int floor) {
		return 3 + Math.max(0, floor) / 8;
	}

	/** Protective effects of the same kind do not stack; elemental resistance is separate. */
	public static float protectionFactor(Char target) {
		float factor = 1f;
		if (Char.hasProp(target, Char.Property.FIERY) || target.buff(Blazing.class) != null) {
			factor = 0.75f;
		}
		if (target.buff(FireImbue.class) != null) factor = Math.min(factor, 0.5f);
		factor = Math.min(factor, Brimstone.cursedProtectionFactor(target));
		return factor;
	}

	public static int apply(Char target, int raw, Object source) {
		return apply(target, raw, source, DamageTag.PHYSICAL);
	}

	private static int apply(Char target, int raw, Object source, DamageTag nature) {
		if (target == null || raw <= 0 || !target.isAlive()
				|| target.isImmune(CursedFlameDamage.class)) return 0;
		if (nature != DamageTag.MAGICAL && target instanceof Elemental.FrostElemental) {
			raw = Math.round(target.HT * Random.Float(0.5f, 0.6f));
		}
		int adjusted = Math.max(0, Math.round(raw * target.resist(CursedFlameDamage.class)
				* protectionFactor(target)));
		if (adjusted > 0) target.damage(adjusted, source,
				nature, DamageTag.CURSED_FIRE, DamageTag.FIRE, DamageTag.CURSED_FIRE_RESOLVED);
		return adjusted;
	}
}
