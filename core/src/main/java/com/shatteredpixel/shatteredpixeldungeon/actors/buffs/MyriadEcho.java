package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.Image;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** Hidden marker for one-HP Myriad Black Shadow echoes. */
public class MyriadEcho extends Buff {

	{
		type = buffType.NEUTRAL;
		announced = false;
		revivePersists = true;
	}

	@Override
	public boolean attachTo(Char target) {
		if (!(target instanceof Mob) || !super.attachTo(target)) {
			return false;
		}
		normalize((Mob) target);
		return true;
	}

	@Override
	public boolean act() {
		if (target instanceof Mob) {
			normalize((Mob) target);
		}
		spend(TICK);
		return true;
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public void tintIcon(Image icon) {
		// Hidden marker; no icon tint is shown.
	}

	public static boolean isMarked(Mob mob) {
		return mob != null && mob.buff(MyriadEcho.class) != null;
	}

	/** Restores the invariant without reviving an already-dead echo. */
	public static void normalize(Mob mob) {
		if (mob == null) return;
		mob.HT = 1;
		mob.HP = Math.max(0, Math.min(1, mob.HP));
		mob.EXP = 0;
		mob.alignment = Char.Alignment.ENEMY;
	}
}
