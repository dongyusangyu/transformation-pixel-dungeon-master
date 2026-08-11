package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DeceptiveHeroTarget;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingBonusProvider;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.watabou.utils.Random;

/** Shared combat implementation for hostile creatures which copy hero equipment. */
public abstract class HeroReplicaMob extends Mob implements DeceptiveHeroTarget, RingBonusProvider {

	protected final HeroEquipmentReplica replica = new HeroEquipmentReplica();

	protected abstract HeroEquipmentReplica.Scope replicaScope();

	protected HeroEquipmentReplica.EquipmentSource equipmentSource() {
		return HeroEquipmentReplica.dungeonHeroSource();
	}

	protected void refreshReplica() {
		replica.refresh(equipmentSource(), replicaScope());
	}

	@Override
	protected boolean act() {
		refreshReplica();
		return performReplicaBaseAct();
	}

	protected boolean performReplicaBaseAct() {
		return super.act();
	}

	@Override
	public int damageRoll() {
		KindOfWeapon weapon = replica.weapon();
		if (weapon != null) {
			return weapon.damageRoll(this) + armedForceDamageBonus();
		}
		return unarmedDamageRoll();
	}

	protected int armedForceDamageBonus() {
		return RingOfForce.armedDamageBonus(this);
	}

	protected int unarmedDamageRoll() {
		int level = replica.ringBonus(RingOfForce.Force.class, true);
		if (replicaScope() != HeroEquipmentReplica.Scope.GUARD || level == 0) {
			return super.damageRoll();
		}

		float tier = Math.max(1f, (replica.strength() - 8) / 2f);
		if (tier > 5f) {
			tier = 5f + (tier - 5f) / 2f;
		}
		if (level <= 0) {
			tier = 1f;
		}
		int min = Math.max(0, Math.round(tier + level));
		int max = Math.max(0, Math.round(5f * (tier + 1f) + level * (tier + 1f)));
		return Random.NormalIntRange(min, max);
	}

	@Override
	public int attackSkill(Char target) {
		float weaponAccuracy = replica.weapon() == null
				? 1f : replica.weapon().accuracyFactor(this, target);
		return Math.round(40f * weaponAccuracy * accuracyRingMultiplier());
	}

	protected float accuracyRingMultiplier() {
		return RingOfAccuracy.accuracyMultiplier(this);
	}

	@Override
	public int defenseSkill(Char enemy) {
		float defense = 20f;
		Armor armor = replica.armor();
		if (armor != null) {
			defense = armor.evasionFactor(this, defense);
		}
		return Math.round(defense * evasionRingMultiplier());
	}

	protected float evasionRingMultiplier() {
		return RingOfEvasion.evasionMultiplier(this);
	}

	@Override
	public float attackDelay() {
		float delay = super.attackDelay();
		if (replica.weapon() != null) {
			delay *= replica.weapon().delayFactor(this);
		}
		return delay / furorRingMultiplier();
	}

	protected float furorRingMultiplier() {
		return RingOfFuror.attackSpeedMultiplier(this);
	}

	@Override
	public float speed() {
		float speed = super.speed() * hasteRingMultiplier();
		Armor armor = replica.armor();
		return armor == null ? speed : armor.speedFactor(this, speed);
	}

	protected float hasteRingMultiplier() {
		return RingOfHaste.speedMultiplier(this);
	}

	@Override
	protected boolean canAttack(Char target) {
		KindOfWeapon weapon = replica.weapon();
		return weapon != null && weapon.canReach(this, target.pos, 0) || super.canAttack(target);
	}

	@Override
	public int drRoll() {
		int dr = baseDrRoll();
		Armor armor = replica.armor();
		if (armor != null) {
			dr += Random.NormalIntRange(armor.DRMin(), armor.DRMax());
		}
		KindOfWeapon weapon = replica.weapon();
		if (weapon != null) {
			dr += rollWeaponDefense(Math.max(0, weapon.defenseFactor(this)));
		}
		return dr;
	}

	protected int baseDrRoll() {
		return super.drRoll();
	}

	protected int rollWeaponDefense(int maximum) {
		return Random.NormalIntRange(0, maximum);
	}

	@Override
	public int attackProc(Char target, int damage, DamageTag... tags) {
		damage = processBaseAttackProc(target, damage, tags);
		KindOfWeapon weapon = replica.weapon();
		return weapon == null ? damage : weapon.proc(this, target, damage);
	}

	protected int processBaseAttackProc(Char target, int damage, DamageTag... tags) {
		return super.attackProc(target, damage, tags);
	}

	@Override
	public int defenseProc(Char enemy, int damage, DamageTag... tags) {
		damage = processBaseDefenseProc(enemy, damage, tags);
		Armor armor = replica.armor();
		if (armor != null) {
			damage = armor.proc(enemy, this, damage);
		}
		return damage;
	}

	protected int processBaseDefenseProc(Char enemy, int damage, DamageTag... tags) {
		return super.defenseProc(enemy, damage, tags);
	}

	@Override
	public int glyphLevel(Class<? extends Armor.Glyph> glyph) {
		Armor armor = replica.armor();
		if (armor != null && armor.hasGlyph(glyph, this)) {
			return Math.max(super.glyphLevel(glyph), armor.buffedLvl());
		}
		return super.glyphLevel(glyph);
	}

	@Override
	public void damage(int damage, Object source, DamageTag... tags) {
		damage = Math.round(damage * tenacityRingMultiplier());
		super.damage(damage, source, tags);
	}

	protected float tenacityRingMultiplier() {
		return RingOfTenacity.damageMultiplier(this);
	}

	@Override
	public int copiedRingBonus(Class<? extends Ring.RingBuff> type, boolean buffed) {
		return replica.ringBonus(type, buffed);
	}
}
