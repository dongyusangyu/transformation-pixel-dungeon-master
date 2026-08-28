package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.tmobs.DarkMechanicalFistSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.audio.Sample;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

import java.util.ArrayList;

public class DarkMechanicalFist extends MechanicalFist {

	{
		spriteClass = DarkMechanicalFistSprite.class;
		loot = MetalShard.class;
		lootChance = 1f;
		properties.add(Property.DEMONIC);
	}

	@Override
	public float lootChance() {
		return 1f;
	}

	@Override
	public int attackProc(Char target, int damage, DamageTag... damageTags) {
		damage = super.attackProc(target, damage, damageTags);
		if (target != null) {
			Buff.prolong(target, Blindness.class, 3f);
		}
		return damage;
	}

	@Override
	protected boolean act() {
		if (canAttemptMiniLaser()) {
			Char target = chooseMiniLaserTarget();
			if (target != null) {
				fireMiniLaser(target);
			}
		}
		return performBaseAct();
	}

	protected boolean performBaseAct() {
		return super.act();
	}

	protected boolean canAttemptMiniLaser() {
		return isAlive() && state != SLEEPING && paralysed <= 0 && Dungeon.level != null
				&& pos >= 0 && pos < Dungeon.level.length();
	}

	protected int laserDamageFrom(int baseDamage) {
		return Math.round(baseDamage * 0.5f);
	}

	protected int miniLaserDamageRoll() {
		return laserDamageFrom(damageRoll());
	}

	protected Char chooseMiniLaserTarget() {
		if (Dungeon.level == null) return null;
		refreshLaserFieldOfView();
		if (isValidMiniLaserTarget(enemy)) return enemy;

		Char best = null;
		for (Char candidate : new ArrayList<>(Actor.chars())) {
			if (!isValidMiniLaserTarget(candidate)) continue;
			if (best == null
					|| Dungeon.level.distance(pos, candidate.pos) < Dungeon.level.distance(pos, best.pos)
					|| Dungeon.level.distance(pos, candidate.pos) == Dungeon.level.distance(pos, best.pos)
							&& candidate.id() < best.id()) {
				best = candidate;
			}
		}
		return best;
	}

	protected boolean isValidMiniLaserTarget(Char candidate) {
		if (candidate == null || candidate == this || !Actor.chars().contains(candidate)
				|| !candidate.isAlive() || !candidate.isActive()
				|| candidate.invisible > 0 || Dungeon.level == null || fieldOfView == null
				|| candidate.pos < 0 || candidate.pos >= fieldOfView.length || !fieldOfView[candidate.pos]
				|| !isMiniLaserHostile(candidate) || isCharmedBy(candidate)) {
			return false;
		}
		return new Ballistica(pos, candidate.pos, Ballistica.MAGIC_BOLT).collisionPos == candidate.pos;
	}

	/**
	 * Actor.isHostile() intentionally treats ordinary enemy mobs as hostile to
	 * each other for several special mechanics. That relationship is too broad
	 * for this attack, which should follow normal mob target selection.
	 */
	protected boolean isMiniLaserHostile(Char candidate) {
		if (candidate == null || alignment == null || candidate.alignment == null
				|| alignment == Char.Alignment.NEUTRAL || candidate.alignment == Char.Alignment.NEUTRAL) {
			return false;
		}

		if (buff(Amok.class) != null || candidate.buff(StoneOfAggression.Aggression.class) != null) {
			return true;
		}

		if (Actor.isSpecialEnemyAlignment(alignment)
				|| Actor.isSpecialEnemyAlignment(candidate.alignment)) {
			return Actor.isHostile(this, candidate);
		}

		return alignment != candidate.alignment;
	}

	protected void refreshLaserFieldOfView() {
		if (Dungeon.level == null) return;
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView(this, fieldOfView);
	}

	protected void fireMiniLaser(Char target) {
		showMiniLaser(target);
		target.damage(miniLaserDamageRoll(), this, DamageTag.MAGICAL);
		if (!target.isAlive() && target == Dungeon.hero) {
			Badges.validateDeathFromEnemyMagic();
			Dungeon.fail(this);
			GLog.n(Messages.get(Char.class, "kill", name()));
		}
	}

	protected void showMiniLaser(Char target) {
		if (sprite != null && sprite.parent != null && target != null && target.sprite != null) {
			sprite.parent.add(new Beam.HealthRay(
					sprite.center(),
					DungeonTilemap.raisedTileCenterToWorld(target.pos)));
			Sample.INSTANCE.play(Assets.Sounds.ZAP);
		}
	}
}
