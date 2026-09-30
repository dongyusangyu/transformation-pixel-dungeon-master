package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlchemySceneTest {
	@Test
	public void energyRowFitsAboveNavigationBarIncludingSixteenPixelButton() {
		assertEquals(160f, AlchemyScene.energyRowTop(200, 16, 12, 16), 0.001f);
		assertEquals(176f, AlchemyScene.energyRowTop(200, 0, 12, 16), 0.001f);
	}

	@Test
	public void energyRowFitsTallerTextAndKeepsEightPixelMargin() {
		assertEquals(152f, AlchemyScene.energyRowTop(200, 16, 24, 16), 0.001f);
	}

	@Test
	public void navigationInsetsShiftTheCenterOfAlchemyControls() {
		assertEquals(-8f, AlchemyScene.safeCenterShift(0, 0, 0, 16), 0.001f);
		assertEquals(12f, AlchemyScene.safeCenterShift(0, 16, 24, 16), 0.001f);
	}

	@Test
	public void energyRowStaysInsideLandscapeSideInsets() {
		assertEquals(52f, AlchemyScene.energyRowLeft(100, 20, 180, 16, 64, 16), 0.001f);
		assertEquals(20f, AlchemyScene.energyRowLeft(25, 20, 180, 16, 64, 16), 0.001f);
		assertEquals(84f, AlchemyScene.energyRowLeft(170, 20, 180, 16, 64, 16), 0.001f);
	}

	@After
	public void clearDungeonHero() {
		Dungeon.hero = null;
	}

	public static final class TrackingSpawner extends CorpseDust.DustGhostSpawner {
		boolean dispelled;

		@Override
		public void dispel() {
			dispelled = true;
		}
	}
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

	@Test
	public void corpseDustUsesTemporaryDetachWhenPlacedInAlchemyInput() {
		Hero hero = TestHeroFactory.create();
		Dungeon.hero = hero;
		CorpseDust dust = TestHeroFactory.allocateItem(CorpseDust.class);
		dust.quantity(1);
		hero.belongings.backpack.items.add(dust);
		TrackingSpawner spawner = Buff.affect(hero, TrackingSpawner.class);
		assertTrue(spawner != null);

		Item detached = AlchemyScene.detachIngredient(dust, hero.belongings.backpack);

		assertSame(dust, detached);
		assertFalse(spawner.dispelled);
	}

	@Test
	public void alchemyAccidentSchedulesExplosionAfterGameSceneCreated() throws IOException {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/scenes/AlchemyScene.java");

		assertTrue(source.contains("new Game.SceneChangeCallback()"));
		assertTrue(source.contains("public void afterCreate()"));
		assertTrue(source.contains("new Bomb.ConjuredBomb().explode(accidentPos)"));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/java").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
