package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tboss.GentlemanElf;
import com.shatteredpixel.shatteredpixeldungeon.items.food.GreenGlowFruit;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.Reader;
import java.util.Properties;
import static org.junit.Assert.*;

public class GentlemanElfRewardsTest {
	private static HeadlessItemSprites sprites;

	@BeforeClass public static void installSprites() { sprites = new HeadlessItemSprites(); }
	@AfterClass public static void restoreSprites() { sprites.close(); }

    @Test public void rewardsAreStackableConsumablesAndCataloguedAfterExtraction() throws Exception {
        assertTrue(new GreenGlowFruit().stackable);
        assertTrue(new SourWineAroma().stackable);
        assertEquals(0f, new SourWineAroma().useTimeForTest(), 0f);
        Path cwd=Paths.get(System.getProperty("user.dir")); Path root=Files.isDirectory(cwd.resolve("core"))?cwd:cwd.getParent();
        String catalog=new String(Files.readAllBytes(root.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java")), StandardCharsets.UTF_8);
		assertTrue(catalog.contains("ScrollOfExtraction.class, SourWineAroma.class"));
        assertFalse(catalog.contains("ElfWine.class"));
		String aroma = new String(Files.readAllBytes(root.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/SourWineAroma.java")), StandardCharsets.UTF_8);
		assertFalse(aroma.contains("spendAndNext"));
    }
    @Test public void fruitBranchDropsThreeAndAromaBranchIsIndependent() {
        GentlemanElf boss = new GentlemanElf();
        Item fruit = boss.createLootForRoll(0.49f);
        Item aroma = boss.createLootForRoll(0.50f);
        assertTrue(fruit instanceof GreenGlowFruit); assertEquals(3, fruit.quantity());
        assertTrue(aroma instanceof SourWineAroma);
    }
	@Test public void bothLocalesUseRuntimeClassKeysForAllThreeItems() throws Exception {
		Path cwd=Paths.get(System.getProperty("user.dir")); Path root=Files.isDirectory(cwd.resolve("core"))?cwd:cwd.getParent();
		for (String file : new String[]{"items.properties", "items_zh.properties"}) {
			Properties messages = new Properties();
			try (Reader reader = Files.newBufferedReader(root.resolve("core/src/main/assets/messages/items").resolve(file), StandardCharsets.UTF_8)) {
				messages.load(reader);
			}
			for (String key : new String[]{"items.greenglowfruit.name", "items.greenglowfruit.desc", "items.greenglowfruit.ac_use",
					"items.sourwinearoma.name", "items.sourwinearoma.desc", "items.sourwinearoma.ac_use",
					"items.elfwine.name", "items.elfwine.desc"}) assertNotNull(file + " missing " + key, messages.getProperty(key));
		}
	}

	@Test public void sourWineAromaUsesHeroAnimationGasSoundAndWarningMessage() throws Exception {
		Path cwd=Paths.get(System.getProperty("user.dir")); Path root=Files.isDirectory(cwd.resolve("core"))?cwd:cwd.getParent();
		String aroma = new String(Files.readAllBytes(root.resolve("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/SourWineAroma.java")), StandardCharsets.UTF_8);
		assertTrue(aroma.contains("hero.sprite.operate(hero.pos)"));
		assertTrue(aroma.contains("Sample.INSTANCE.play(Assets.Sounds.GAS)"));
		assertTrue(aroma.contains("GLog.w(Messages.get(this, \"use\"))"));
		for (String file : new String[]{"items.properties", "items_zh.properties"}) {
			Properties messages = new Properties();
			try (Reader reader = Files.newBufferedReader(root.resolve("core/src/main/assets/messages/items").resolve(file), StandardCharsets.UTF_8)) {
				messages.load(reader);
			}
			assertNotNull(file + " missing use prompt", messages.getProperty("items.sourwinearoma.use"));
		}
		Properties chinese = new Properties();
		try (Reader reader = Files.newBufferedReader(root.resolve("core/src/main/assets/messages/items/items_zh.properties"), StandardCharsets.UTF_8)) {
			chinese.load(reader);
		}
		assertEquals("逸散而出的酒香使你周边的生物变得头晕目眩！", chinese.getProperty("items.sourwinearoma.use"));
	}
}
