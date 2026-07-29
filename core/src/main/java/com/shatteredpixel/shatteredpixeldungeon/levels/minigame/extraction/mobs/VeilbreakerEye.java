package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * An evil eye which can reveal an invisible hero in its field of view.
 */
public class VeilbreakerEye extends Eye {

	private static final float CONE_ANGLE = 60f;
	private static final float HALF_CONE_COSINE = 0.8660254f;

	{
		spriteClass = VeilbreakerEyeSprite.class;
	}

	@Override
	public int attackSkill(Char target) {
		return super.attackSkill(target) + 10;
	}

	@Override
	public String description() {
		return RaidKeyCarrier.appendDescription(this, super.description());
	}

	@Override
	protected boolean act() {
		tryRevealHero();
		return super.act();
	}

	private void tryRevealHero() {
		if (Dungeon.level == null || Dungeon.hero == null || Dungeon.hero.invisible <= 0) {
			return;
		}

		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView(this, fieldOfView);

		if (!fieldOfView[Dungeon.hero.pos]) {
			return;
		}

		int distance = Dungeon.level.distance(pos, Dungeon.hero.pos);
		if (Random.Float() < revealChance(distance) && dispelActualInvisibility()) {
			GLog.w(Messages.get(this, "reveal"));
		}
	}

	private boolean dispelActualInvisibility() {
		boolean removed = false;
		for (Buff invisibility : Dungeon.hero.buffs(Invisibility.class)) {
			invisibility.detach();
			removed = true;
		}

		CloakOfShadows.cloakStealth cloak =
				Dungeon.hero.buff(CloakOfShadows.cloakStealth.class);
		if (cloak != null) {
			cloak.dispel();
			removed = true;
		}

		if (removed) {
			Buff.detach(Dungeon.hero, Preparation.class);
		}
		return removed;
	}

	public static float revealChance(int distance) {
		return 1f / (Math.max(0, distance) + 1f);
	}

	@Override
	protected Iterable<Integer> deathGazeCells() {
		float range = Dungeon.level.trueDistance(pos, beam.collisionPos);
		ConeAOE cone = new ConeAOE(beam, range, CONE_ANGLE, Ballistica.STOP_SOLID);
		if (sprite != null && sprite.parent != null) {
			for (Ballistica ray : cone.outerRays) {
				((MagicMissile) sprite.parent.recycle(MagicMissile.class)).reset(
						MagicMissile.SHADOW_CONE, sprite, ray.path.get(ray.dist), null);
			}
		}
		return cone.cells;
	}

	/** Returns whether a candidate point lies in the 60-degree cone from origin to aim. */
	public static boolean coneContains(
			float originX, float originY, float aimX, float aimY,
			float candidateX, float candidateY, float range) {
		float aimDeltaX = aimX - originX;
		float aimDeltaY = aimY - originY;
		float candidateDeltaX = candidateX - originX;
		float candidateDeltaY = candidateY - originY;
		float aimDistance = (float) Math.hypot(aimDeltaX, aimDeltaY);
		float candidateDistance = (float) Math.hypot(candidateDeltaX, candidateDeltaY);
		if (aimDistance == 0 || candidateDistance == 0 || candidateDistance > range) {
			return false;
		}
		float cosine = (aimDeltaX * candidateDeltaX + aimDeltaY * candidateDeltaY)
				/ (aimDistance * candidateDistance);
		return cosine >= HALF_CONE_COSINE;
	}
}
