package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

final class TowerBossMusic {

	private TowerBossMusic() {
	}

	static String musicFor(String bossId) {
		if (TowerBossGenerator.PESTILENCE_KNIGHT_ID.equals(bossId)) {
			return Assets.Music.PESTILENCE_BOSS;
		}
		if (TowerBossGenerator.DEATH_KNIGHT_ID.equals(bossId)) {
			return Assets.Music.DEATH_KNIGHT_BOSS;
		}
		if (TowerBossGenerator.GENTLEMAN_ELF_ID.equals(bossId)) {
			return Assets.Music.HALLS_BOSS;
		}
		return Assets.Music.HALLS_BOSS;
	}
}
