package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WandmakerQuestItemTest {

	@Test
	public void bookIsScoredAtDepthTenWithoutBecomingAQuestTurnIn() {
		assertFalse(Wandmaker.Quest.isQuestItemConsumed(
				com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon.class));
		assertTrue(Wandmaker.Quest.isQuestItemConsumed(CorpseDust.class));
		assertFalse(Wandmaker.Quest.isQuestItemConsumed(
				com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Necronomicon.class));
		assertTrue(Wandmaker.Quest.shouldAwardBookQuestScore(10, 0, true));
		assertFalse(Wandmaker.Quest.shouldAwardBookQuestScore(9, 0, true));
		assertFalse(Wandmaker.Quest.shouldAwardBookQuestScore(10, 1, true));
		assertFalse(Wandmaker.Quest.shouldAwardBookQuestScore(10, 0, false));
	}

	@Test
	public void bookDoesNotSubstituteForOtherWandmakerQuests() {
		assertFalse(Wandmaker.Quest.shouldAwardBookQuestScore(10, 1, true));
	}

	@Test
	public void noRewardCompletionAwardsQuestScoreWithoutOfferingWands() {
		Notes.reset();
		Wandmaker.Quest.completeWithoutReward();

		assertEquals(2000, Statistics.questScores[1], 0.0);
		assertNull(Wandmaker.Quest.wand1);
		assertNull(Wandmaker.Quest.wand2);
	}
}
