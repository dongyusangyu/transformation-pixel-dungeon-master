package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MirrorSprite;
import com.watabou.noosa.MovieClip.Animation;

/** Dynamic hero silhouette used by the alienated guard. */
public class AlienatedPrismaticGuardSprite extends MirrorSprite {

	private static final float GUARD_ALPHA = 1f;

	public AlienatedPrismaticGuardSprite() {
		super();
		applyReplicaFilter();
	}

	@Override
	public void updateArmor() {
		int tier = Dungeon.hero == null ? 0 : Dungeon.hero.tier();
		super.updateArmor(tier);
		applyReplicaFilter();
	}

	@Override
	public void link(Char ch) {
		super.link(ch);
		applyReplicaFilter();
	}

	@Override
	public void play(Animation animation) {
		super.play(animation);
		applyReplicaFilter();
	}

	@Override
	public void resetColor() {
		super.resetColor();
		applyReplicaFilter();
	}

	protected void applyReplicaFilter() {
		tint(0.02f, 0.06f, 0.10f, 0.72f);
		boolean invisible = isState(State.INVISIBLE);
		alpha(invisible ? GUARD_ALPHA * 0.2f : GUARD_ALPHA);
		if (invisible) {
			remove(State.AURA);
		} else if (parent != null) {
			aura(0x79CFFF, 2);
		}
	}

	public void splitEffect(int targetCell) {
		if (Dungeon.level != null && targetCell >= 0 && targetCell < Dungeon.level.length()) {
			CellEmitter.get(targetCell).burst(ShadowParticle.CURSE, 6);
		}
	}
}
