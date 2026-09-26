package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DungeonRulesVersionTest {
	private final int previousRulesVersion = Dungeon.rulesVersion;

	@After
	public void restoreRulesVersion() {
		Dungeon.rulesVersion = previousRulesVersion;
	}

	@Test
	public void missingRulesVersionKeepsOldSaveOnLegacyEconomy() {
		Dungeon.rulesVersion = Dungeon.CURRENT_RULES_VERSION;
		Dungeon.restoreRulesVersion(new Bundle());
		assertEquals(Dungeon.LEGACY_RULES_VERSION, Dungeon.rulesVersion);

		Bundle resaved = new Bundle();
		Dungeon.storeRulesVersion(resaved);
		Dungeon.rulesVersion = Dungeon.CURRENT_RULES_VERSION;
		Dungeon.restoreRulesVersion(resaved);
		assertEquals(Dungeon.LEGACY_RULES_VERSION, Dungeon.rulesVersion);
	}

	@Test
	public void newRunRulesVersionSurvivesSaveRoundTrip() {
		Dungeon.rulesVersion = Dungeon.CURRENT_RULES_VERSION;
		Bundle saved = new Bundle();
		Dungeon.storeRulesVersion(saved);
		Dungeon.rulesVersion = Dungeon.LEGACY_RULES_VERSION;
		Dungeon.restoreRulesVersion(saved);
		assertEquals(Dungeon.CURRENT_RULES_VERSION, Dungeon.rulesVersion);
	}
}
