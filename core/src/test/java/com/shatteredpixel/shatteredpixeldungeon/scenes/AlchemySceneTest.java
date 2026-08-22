package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlchemySceneTest {
	@Test
	public void onlyMetamorphosisScrollRemaindersReturnAfterCrafting() {
		assertTrue(AlchemyScene.returnsRemainderAfterCraft(ScrollOfMetamorphosis.class));
		assertFalse(AlchemyScene.returnsRemainderAfterCraft(ScrollOfTransmutation.class));
		assertFalse(AlchemyScene.returnsRemainderAfterCraft(LiquidMetal.class));
	}

	@Test
	public void craftedResultDoesNotReplacePreviewForRemainingRecipes() {
		assertFalse(AlchemyScene.shouldShowCraftedResult(1));
	}

	@Test
	public void craftedResultShowsWhenNoRecipesRemain() {
		assertTrue(AlchemyScene.shouldShowCraftedResult(0));
	}
}
