package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;

import org.junit.After;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RankingRestartUpgradeStateTest {

	@After
	public void resetDungeonState() {
		Dungeon.depth = 0;
		Dungeon.hero = null;
	}

	@Test
	public void newCycleClearsExternalLevelModifiersWithoutRemovingCurses() throws Exception {
		Bag bag = new Bag();

		Sword weapon = allocateWithoutConstructor(Sword.class);
		weapon.level(5);
		weapon.upgradeScrollUses = 5;
		weapon.cursed = true;
		weapon.curseInfusionBonus = true;

		LeatherArmor armor = allocateWithoutConstructor(LeatherArmor.class);
		armor.level(4);
		armor.upgradeScrollUses = 4;
		armor.cursed = true;
		armor.curseInfusionBonus = true;

		bag.items.add(weapon);
		bag.items.add(armor);

		RankingRestart.prepareInventory(bag);

		assertEquals(0, weapon.trueLevel());
		assertEquals(0, weapon.level());
		assertEquals(0, weapon.upgradeScrollUses);
		assertFalse(weapon.curseInfusionBonus);
		assertTrue(weapon.cursed);

		assertEquals(0, armor.trueLevel());
		assertEquals(0, armor.level());
		assertEquals(0, armor.upgradeScrollUses);
		assertFalse(armor.curseInfusionBonus);
		assertTrue(armor.cursed);
	}

	@Test
	public void newCycleResetContractCoversWandsAndNestedStaffWands() throws Exception {
		String item = readItemSource("Item.java");
		String wand = readItemSource("wands/Wand.java");
		String staff = readItemSource("weapon/melee/MagesStaff.java");
		String ring = readItemSource("rings/RingOfKing.java");

		assertTrue(item.contains("public void resetUpgradeStateForNewCycle() {\n"
				+ "\t\tlevel(0);\n"
				+ "\t\tupgradeScrollUses = 0;"));
		assertTrue(wand.contains("public void resetUpgradeStateForNewCycle() {\n"
				+ "\t\tcurseInfusionBonus = false;\n"
				+ "\t\tresinBonus = 0;\n"
				+ "\t\tsuper.resetUpgradeStateForNewCycle();"));
		assertTrue(staff.contains("public void resetUpgradeStateForNewCycle() {\n"
				+ "\t\tsuper.resetUpgradeStateForNewCycle();\n"
				+ "\t\tif (wand != null) {\n"
				+ "\t\t\twand.resetUpgradeStateForNewCycle();\n"
				+ "\t\t\tupdateWand(false);"));
		assertTrue(ring.contains("public void resetUpgradeStateForNewCycle() {\n"
				+ "        curseInfusionBonus = false;\n"
				+ "        super.resetUpgradeStateForNewCycle();"));
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		return (T) ((Unsafe) unsafeField.get(null)).allocateInstance(type);
	}

	private static String readItemSource(String relativePath) throws Exception {
		Path coreDirectory = Paths.get(System.getProperty("user.dir"));
		if (!Files.isDirectory(coreDirectory.resolve("src/main/java"))) {
			coreDirectory = coreDirectory.resolve("core");
		}
		Path source = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8)
				.replace("\r\n", "\n");
	}
}
