package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.items.ArcaneResin;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.StewedMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.MetamorphosisPrism;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.RubbingsTome;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.TransformSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TalentMiracleAlchemyTest {

	@Test
	public void miracleAlchemyUsesTwentyPercentChancePerPoint() {
		int onePointTriggers = 0;
		int twoPointTriggers = 0;
		for (int roll = 0; roll < 5; roll++) {
			onePointTriggers += Talent.miracleAlchemyBonus(1, roll, ArcaneResin.class);
			twoPointTriggers += Talent.miracleAlchemyBonus(2, roll, ArcaneResin.class);
		}

		assertEquals(1, onePointTriggers);
		assertEquals(2, twoPointTriggers);
	}

	@Test
	public void miracleAlchemyDoesNotTriggerWithoutTalent() {
		for (int roll = 0; roll < 5; roll++) {
			assertEquals(0, Talent.miracleAlchemyBonus(0, roll, ArcaneResin.class));
		}
	}

	@Test
	public void miracleAlchemyExcludesConfiguredProducts() {
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, Trinket.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, StewedMeat.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, Blandfruit.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, Bomb.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, LiquidMetal.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, ScrollOfEnchantment.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, RubbingsTome.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, MagicalInfusion.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, ElixirOfMight.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, TransformSpell.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, MetamorphosisPrism.class));
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, PotionOfMastery.class));
	}

	@Test
	public void miracleAlchemyRejectsMissingProductType() {
		assertEquals(0, Talent.miracleAlchemyBonus(2, 0, null));
	}

	@Test
	public void miracleAlchemyCallSitesUseTheCentralHelper() throws IOException {
		String[] sources = {
				"com/shatteredpixel/shatteredpixeldungeon/items/Recipe.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/ArcaneResin.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/food/MeatPie.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/potions/Potion.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/potions/brews/UnstableBrew.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/potions/exotic/ExoticPotion.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/scrolls/Scroll.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/scrolls/exotic/ExoticScroll.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/spells/Alchemize.java",
				"com/shatteredpixel/shatteredpixeldungeon/items/spells/UnstableSpell.java"
		};

		int helperCalls = 0;
		Path sourceRoot = Paths.get("src/main/java");
		for (String sourcePath : sources) {
			String source = new String(Files.readAllBytes(sourceRoot.resolve(sourcePath)),
					StandardCharsets.UTF_8);
			assertFalse(source.contains("pointsInTalent(Talent.MIRACLE_ALCHEMY)"));
			helperCalls += occurrences(source, "Talent.miracleAlchemyBonus(hero, result)");
		}
		assertEquals(11, helperCalls);
	}

	private static int occurrences(String text, String target) {
		int count = 0;
		int index = 0;
		while ((index = text.indexOf(target, index)) >= 0) {
			count++;
			index += target.length();
		}
		return count;
	}
}
