package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WandmakerQuestItemTest {

	@Test
	public void dustQuestAcceptsBookAsFallbackAndKeepsIt() {
		assertTrue(Wandmaker.Quest.isDustQuestAlternative(1,
				com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon.class));
		assertTrue(Wandmaker.Quest.isQuestItemConsumed(CorpseDust.class));
		assertFalse(Wandmaker.Quest.isQuestItemConsumed(
				com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon.class));
	}

	@Test
	public void bookDoesNotSubstituteForOtherWandmakerQuests() {
		assertFalse(Wandmaker.Quest.isDustQuestAlternative(2,
				com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon.class));
	}
}
