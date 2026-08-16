package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MirrorSprite;
import com.watabou.noosa.MovieClip.Animation;

/** Half-transparent violet afterimage which still follows the live hero appearance. */
public class TwistedMirrorSprite extends MirrorSprite {

	private static final float MIRROR_ALPHA = 0.52f;

	public TwistedMirrorSprite() {
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
		tint(0.025f, 0.01f, 0.06f, 0.80f);
		boolean invisible = isState(State.INVISIBLE);
		alpha(invisible ? MIRROR_ALPHA * 0.2f : MIRROR_ALPHA);
		if (invisible) {
			remove(State.AURA);
		} else if (parent != null) {
			aura(0x7A36B5, 2);
		}
	}

}
