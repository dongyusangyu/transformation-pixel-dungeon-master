package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.watabou.utils.Bundle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RaidDroneSprites;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;

/**
 * An armored statue which is already active when the raid begins.
 */
public class VaultArmoredStatue extends ArmoredStatue {

	{
		spriteClass = RaidDroneSprites.Guard.class;
		properties.add(Property.MINIBOSS);
		properties.add(Property.INORGANIC);
		properties.remove(Property.DEMONIC);
		flying = true;
		// Temporary levitation must not cancel intrinsic flight when it expires.
		immunities.add(Levitation.class);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (alignment == Alignment.ALLY) {
			alignment = Alignment.ENEMY;
			state = WANDERING;
			enemy = null;
			enemyID = -1;
			target = -1;
		}
	}

	public VaultArmoredStatue() {
		super();
		state = WANDERING;
	}

	public void generateRaidEquipment(long raidId) {
		createWeapon(false);
		weapon().markForExtractionRaid(raidId);
		armor().markForExtractionRaid(raidId);
	}

	public boolean isRaidActive() {
		return state != PASSIVE;
	}

	@Override
	public int attackSkill(Char target) {
		return super.attackSkill(target) + 10;
	}
}
