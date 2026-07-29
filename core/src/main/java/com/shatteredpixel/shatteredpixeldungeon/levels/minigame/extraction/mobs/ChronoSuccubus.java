package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * A succubus which can end the hero's stopped-time effects.
 */
public class ChronoSuccubus extends Succubus {

	@Override
	public int attackSkill(Char target) {
		return super.attackSkill(target) + 10;
	}

	@Override
	public String description() {
		return RaidKeyCarrier.appendDescription(this, super.description());
	}

	public static boolean tryBreakTimeStop(Char target, float remainingTurns) {
		if (!(target instanceof Hero) || Dungeon.level == null) {
			return false;
		}

		ChronoSuccubus nearest = null;
		int nearestDistance = Integer.MAX_VALUE;
		for (Mob mob : Dungeon.level.mobs) {
			if (!(mob instanceof ChronoSuccubus) || !mob.isAlive()) {
				continue;
			}

			ChronoSuccubus succubus = (ChronoSuccubus) mob;
			if (succubus.fieldOfView == null
					|| succubus.fieldOfView.length != Dungeon.level.length()) {
				succubus.fieldOfView = new boolean[Dungeon.level.length()];
			}
			Dungeon.level.updateFieldOfView(succubus, succubus.fieldOfView);
			if (!succubus.fieldOfView[target.pos]) {
				continue;
			}

			int distance = Dungeon.level.distance(succubus.pos, target.pos);
			if (distance < nearestDistance) {
				nearest = succubus;
				nearestDistance = distance;
			}
		}

		if (nearest != null && Random.Float() < breakChance(remainingTurns)) {
			GLog.w(Messages.get(nearest, "break_time"));
			return true;
		}
		return false;
	}

	public static float breakChance(float remainingTurns) {
		return 1f / Math.max(1, (int) Math.ceil(remainingTurns));
	}
}
