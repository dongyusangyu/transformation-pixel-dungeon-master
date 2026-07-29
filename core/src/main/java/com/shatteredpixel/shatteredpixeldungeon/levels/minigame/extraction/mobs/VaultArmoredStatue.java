package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;

/**
 * An armored statue which is already active when the raid begins.
 */
public class VaultArmoredStatue extends ArmoredStatue {

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
