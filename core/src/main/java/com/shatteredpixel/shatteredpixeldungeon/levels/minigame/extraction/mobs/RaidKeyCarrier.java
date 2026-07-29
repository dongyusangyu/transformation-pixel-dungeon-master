package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.ExtractionRaidLevel;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/**
 * Visible marker for the ordinary raid monster which holds a crystal key.
 */
public class RaidKeyCarrier extends Buff {

	{
		type = buffType.NEUTRAL;
		announced = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.MARK;
	}

	@Override
	public void detach() {
		// Char.destroy removes buffs before subclass die() methods regain control.
		// Drop here while the carrier and its position are still available.
		if (target instanceof Mob
				&& !target.isAlive()
				&& Dungeon.level instanceof ExtractionRaidLevel) {
			ExtractionRaidLevel level = (ExtractionRaidLevel) Dungeon.level;
			level.drop(level.createCarrierCrystalKey(), target.pos);
		}
		super.detach();
	}

	@Override
	public void fx(boolean on) {
		if (on) {
			target.sprite.aura(0x4CA8FF, 4);
		} else {
			target.sprite.clearAura();
		}
	}

	public static String appendDescription(Mob mob, String description) {
		RaidKeyCarrier marker = mob == null ? null : mob.buff(RaidKeyCarrier.class);
		if (marker == null) {
			return description;
		}
		return description + "\n\n_" + Messages.titleCase(marker.name()) + "_\n" + marker.desc();
	}
}
