package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** Contract and mechanics tests for the tier-six Soul Blade. */
public class SoulBladeTest {

	private static final String SOUL_BLADE_CLASS =
			"com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.SoulBlade";

	@Test
	public void hasTierSixWeaponPanel() throws Exception {
		Object weapon = newSoulBlade();
		Method level = weapon.getClass().getMethod("level", int.class);
		level.invoke(weapon, 0);
		assertEquals(6, invokeInt(weapon, "min", 0));
		assertEquals(24, invokeInt(weapon, "max", 0));
		assertEquals(20, invokeInt(weapon, "STRReq"));
		level.invoke(weapon, 3);
		assertEquals(9, invokeInt(weapon, "min", 3));
		assertEquals(42, invokeInt(weapon, "max", 3));
		assertEquals(6, invokeInt(weapon, "weaponTier"));
	}

	@Test
	public void deterministicSoulBladeFormulasMatchTheDesign() throws Exception {
		Class<?> type = Class.forName(SOUL_BLADE_CLASS);
		assertEquals(6, invokeStaticInt(type, "maxBlockForLevel", 0));
		assertEquals(12, invokeStaticInt(type, "maxBlockForLevel", 3));
		assertEquals(20, invokeStaticInt(type, "soulDamageForTarget", 200, false));
		assertEquals(6, invokeStaticInt(type, "soulDamageForTarget", 200, true));
		assertEquals(0.20f, invokeStaticFloat(type, "soulProcChance", 0), 0.0001f);
		assertEquals(1.00f, invokeStaticFloat(type, "soulProcChance", 20), 0.0001f);
		assertEquals(15, invokeStaticInt(type, "shieldAmountForMaxHealth", 100, 0));
		assertEquals(25, invokeStaticInt(type, "shieldAmountForMaxHealth", 100, 10));
		assertEquals(35, invokeStaticInt(type, "shieldAmountForMaxHealth", 200, 10));
		assertEquals(25, invokeStaticInt(type, "shieldCooldown", 0));
		assertEquals(10, invokeStaticInt(type, "shieldCooldown", 15));
		assertEquals(1, invokeStaticInt(type, "shieldCooldown", 30));
	}

	@Test
	public void soulBladeNoLongerContainsMindVisionProc() throws Exception {
		String source = read(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/SoulBlade.java");
		assertFalse(source.contains("MindVision"));
		assertFalse(source.contains("mindVisionChance"));
		assertTrue(source.contains("Barrier.class"));
	}

	@Test
	public void duskCounterConsumesThreeAttacksAndRefreshesWithoutStacking() throws Exception {
		Class<?> type = Class.forName(SOUL_BLADE_CLASS + "$DuskBuff");
		Object buff = type.getDeclaredConstructor().newInstance();
		Method refresh = type.getDeclaredMethod("refreshAttacks");
		Method consume = type.getDeclaredMethod("consumeAttack");
		Method remaining = type.getDeclaredMethod("remainingAttacks");
		refresh.invoke(buff);
		assertEquals(3, remaining.invoke(buff));
		assertTrue((Boolean) consume.invoke(buff));
		assertEquals(2, remaining.invoke(buff));
		assertTrue((Boolean) consume.invoke(buff));
		assertTrue((Boolean) consume.invoke(buff));
		assertEquals(0, remaining.invoke(buff));
		assertTrue(!(Boolean) consume.invoke(buff));
		refresh.invoke(buff);
		assertEquals(3, remaining.invoke(buff));
	}

	@Test
	public void tierSixGeneratorAndSpriteSlotContainSoulBlade() throws Exception {
		Generator.Category category = Generator.Category.valueOf("WEP_T6");
		Class<?> soulBlade = Class.forName(SOUL_BLADE_CLASS);
		boolean found = false;
		for (Class<?> candidate : category.classes) {
			if (candidate == soulBlade) found = true;
		}
		assertTrue("Soul Blade must be in WEP_T6", found);
		Field image = EXItemSpriteSheet.class.getField("SOUL_BLADE");
		assertEquals(157, image.getInt(null) & 0xFFFF);
	}

	@Test
	public void soulBladeSpriteCellIsOpaqueOnlyWhereTheSwordExists() throws Exception {
		BufferedImage sheet = ImageIO.read(Paths.get("src/main/assets/sprites/ex_items.png").toFile());
		int opaque = 0;
		int transparent = 0;
		for (int y = 144; y < 160; y++) {
			for (int x = 208; x < 224; x++) {
				if ((sheet.getRGB(x, y) >>> 24) == 0) transparent++;
				else opaque++;
			}
		}
		assertEquals(80, opaque);
		assertEquals(176, transparent);
	}

	@Test
	public void weaponRecipeUsesOneGreatswordThreeMindVisionAndThreeShielding() throws Exception {
		Class<?> soulBlade = Class.forName(SOUL_BLADE_CLASS);
		Recipe.WeaponRecipe match = null;
		for (Recipe.WeaponRecipe recipe : Recipe.weaponRecipes()) {
			Field output = Recipe.WeaponRecipe.class.getDeclaredField("output");
			output.setAccessible(true);
			if (output.get(recipe) == soulBlade) {
				match = recipe;
				break;
			}
		}
		assertNotNull("Soul Blade recipe must be registered", match);
		Field inputs = Recipe.WeaponRecipe.class.getDeclaredField("inputs");
		Field quantities = Recipe.WeaponRecipe.class.getDeclaredField("inQuantity");
		inputs.setAccessible(true);
		quantities.setAccessible(true);
		Class<?>[] inputClasses = (Class<?>[]) inputs.get(match);
		int[] inputQuantities = (int[]) quantities.get(match);
		assertEquals(3, inputClasses.length);
		assertEquals("Greatsword", inputClasses[0].getSimpleName());
		assertEquals(1, inputQuantities[0]);
		assertEquals("PotionOfMindVision", inputClasses[1].getSimpleName());
		assertEquals(3, inputQuantities[1]);
		assertEquals("PotionOfShielding", inputClasses[2].getSimpleName());
		assertEquals(3, inputQuantities[2]);
		assertEquals(5, match.cost(new ArrayList<Item>()));
	}

	@Test
	public void sacrificialFireSoulBladeBranchCreatesUncursedUnidentifiedLevelZeroWeapon()
			throws Exception {
		Class<?> room = Class.forName(
				"com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SacrificeRoom");
		Method prize = room.getDeclaredMethod("prize", Level.class, float.class);
		prize.setAccessible(true);
		Item result = (Item) prize.invoke(null, null, 0.099f);

		assertEquals(SoulBlade.class, result.getClass());
		assertEquals(0, result.trueLevel());
		assertFalse(result.cursed);
		assertFalse(result.cursedKnown);
		assertFalse(result.levelKnown);
		assertFalse(result.isIdentified());
		assertEquals(0, result.upgradeScrollUses);
	}

	@Test
	public void itemAndCustomTextDescribeTheWeaponInBothLocales() throws Exception {
		String zh = read("src/main/assets/messages/items/items_zh.properties");
		String en = read("src/main/assets/messages/items/items.properties");
		String customZh = read("src/main/assets/messages/custom/custom_zh.properties");
		String customEn = read("src/main/assets/messages/custom/custom.properties");
		assertTrue(zh.contains("items.weapon.melee.tier6.soulblade.name="));
		assertTrue(zh.contains("items.weapon.melee.tier6.soulblade.ability_name="));
		assertTrue(en.contains("items.weapon.melee.tier6.soulblade.name="));
		assertTrue(customZh.contains("custom.dict.dict.melee_soulblade_d"));
		assertTrue(customEn.contains("custom.dict.dict.melee_soulblade_d="));
	}

	private Object newSoulBlade() throws Exception {
		Class<?> type = Class.forName(SOUL_BLADE_CLASS);
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();
	}

	private int invokeInt(Object target, String method, Object... args) throws Exception {
		Class<?>[] types = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) types[i] = args[i].getClass() == Integer.class ? int.class : args[i].getClass();
		return (Integer) target.getClass().getMethod(method, types).invoke(target, args);
	}

	private int invokeStaticInt(Class<?> type, String method, Object... args) throws Exception {
		return (Integer) staticMethod(type, method, args).invoke(null, args);
	}

	private float invokeStaticFloat(Class<?> type, String method, Object... args) throws Exception {
		return (Float) staticMethod(type, method, args).invoke(null, args);
	}

	private Method staticMethod(Class<?> type, String name, Object... args) throws Exception {
		for (Method method : type.getDeclaredMethods()) {
			if (method.getName().equals(name) && method.getParameterTypes().length == args.length) {
				method.setAccessible(true);
				return method;
			}
		}
		throw new NoSuchMethodException(name);
	}

	private int quantity(Object item) throws Exception {
		return (Integer) item.getClass().getMethod("quantity").invoke(item);
	}

	private String read(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}
