package com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.MovieClip.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;

/** Alienated guard replica with a four-state hard-edged shadow overlay. */
public class MyriadBlackShadowSprite extends AlienatedPrismaticGuardSprite {

	private static final int OVERLAY_FRAME_SIZE = 16;
	private static final int DEEP_VIOLET = 0x6B2A76;

	private static final Emitter.Factory VIOLET_LIGHT = new Emitter.Factory() {
		@Override
		public void emit(Emitter emitter, int index, float x, float y) {
			Speck speck = (Speck) emitter.recycle(Speck.class);
			speck.reset(index, x, y, Speck.LIGHT);
			speck.hardlight(DEEP_VIOLET);
		}
	};

	private MovieClip overlay;
	private Animation overlayIdle;
	private Animation overlayRun;
	private Animation overlayAttack;
	private Animation overlayDie;

	public MyriadBlackShadowSprite() {
		super();

		TextureFilm film = new TextureFilm(
				Assets.Sprites.MYRIAD_BLACK_SHADOW_OVERLAY,
				OVERLAY_FRAME_SIZE,
				OVERLAY_FRAME_SIZE);
		overlay = new MovieClip(Assets.Sprites.MYRIAD_BLACK_SHADOW_OVERLAY);
		overlayIdle = new Animation(1, true).frames(film, 0);
		overlayRun = new Animation(1, true).frames(film, 1);
		overlayAttack = new Animation(1, false).frames(film, 2);
		overlayDie = new Animation(1, false).frames(film, 3);
		playOverlay(curAnim);
		synchronizeOverlay();
	}

	@Override
	protected void applyReplicaFilter() {
		tint(0f, 0f, 0f, 0.94f);
		boolean invisible = isState(State.INVISIBLE);
		alpha(invisible ? 0.18f : 0.94f);
		if (invisible) {
			remove(State.AURA);
		} else if (parent != null) {
			aura(0x24102E, 2);
		}
	}

	@Override
	public void play(Animation animation) {
		super.play(animation);
		playOverlay(curAnim);
	}

	@Override
	public void update() {
		super.update();
		attachOverlay();
		synchronizeOverlay();
	}

	@Override
	public void splitEffect(int targetCell) {
		if (Dungeon.level != null && Dungeon.level.insideMap(targetCell)) {
			CellEmitter.get(targetCell).burst(ShadowParticle.CURSE, 10);
			CellEmitter.get(targetCell).burst(VIOLET_LIGHT, 2);
		}
	}

	@Override
	public void kill() {
		detachOverlay();
		super.kill();
	}

	@Override
	public void destroy() {
		detachOverlay();
		if (overlay != null) {
			overlay.destroy();
			overlay = null;
		}
		super.destroy();
	}

	private void playOverlay(Animation animation) {
		if (overlay == null) {
			return;
		}
		if (animation == die) {
			overlay.play(overlayDie);
		} else if (animation != null && (animation == attack || animation == zap || animation == operate)) {
			overlay.play(overlayAttack);
		} else if (animation == run) {
			overlay.play(overlayRun);
		} else {
			overlay.play(overlayIdle);
		}
	}

	private void attachOverlay() {
		if (overlay != null && parent != null && overlay.parent != parent) {
			parent.addToFront(overlay);
		}
	}

	private void synchronizeOverlay() {
		if (overlay == null) {
			return;
		}
		// Hero replica frames are 12x15 while the authored shadow cell is 16x16.
		// Anchor both to the same cell center and floor line instead of sharing the
		// top-left coordinate, otherwise the shadow is visibly shifted down/right.
		overlay.x = x + overlayOffsetX(width, overlay.width);
		overlay.y = y + overlayOffsetY(height, overlay.height);
		overlay.visible = visible;
		overlay.scale.set(scale);
		overlay.origin.set(origin);
		overlay.angle = angle;
		overlay.am = am;
		overlay.aa = aa;
		if (overlay.flipHorizontal != flipHorizontal) {
			overlay.flipHorizontal = flipHorizontal;
			overlay.frame(overlay.frame());
		}
	}

	static float overlayOffsetX(float baseWidth, float overlayWidth) {
		return (baseWidth - overlayWidth) * 0.5f;
	}

	static float overlayOffsetY(float baseHeight, float overlayHeight) {
		return baseHeight - overlayHeight;
	}

	private void detachOverlay() {
		if (overlay != null && overlay.parent != null) {
			overlay.parent.erase(overlay);
		}
	}
}
