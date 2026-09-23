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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlchemySceneTest {

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
