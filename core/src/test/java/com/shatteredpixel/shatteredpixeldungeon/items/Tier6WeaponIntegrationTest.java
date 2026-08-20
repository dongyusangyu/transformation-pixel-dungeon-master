package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestMelee;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.AuxiliaryCore;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.ChainMace;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.DemonTailWhip;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.GreatGreatGreatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.HundredTonHammer;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.MercuryBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.OracleTerminal;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.SakuraBlossomBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.TwoHandedGreatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.VenomousSickle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.PalermoSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.RadiantGoldHalberd;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Gungnir;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.PortableBlackHole;
import com.shatteredpixel.shatteredpixeldungeon.levels.towers.TowerLevel;

import org.junit.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Tier6WeaponIntegrationTest {

	@Test
	public void tierSixHasItsOwnGeneratorCategory() {
		Generator.Category tierSix = Generator.Category.valueOf("WEP_T6");

		assertArrayEquals(new Class<?>[]{
				GreatGreatGreatsword.class,
				SakuraBlossomBlade.class,
				ChainMace.class,
				TwoHandedGreatsword.class,
				PalermoSword.class,
				MercuryBlade.class,
				AuxiliaryCore.class,
				HundredTonHammer.class,
				DemonTailWhip.class,
				OracleTerminal.class,
				VenomousSickle.class,
				RadiantGoldHalberd.class
		}, tierSix.classes);
		assertArrayEquals(new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, tierSix.defaultProbs, 0f);
		assertEquals(6, Generator.wepTiers.length);
		assertEquals(tierSix, Generator.wepTiers[5]);
	}

	@Test
	public void tierSixOnlyHasOnePercentWeightOnDepthsTwentyOneToTwentyFive() throws Exception {
		assertArrayEquals(new float[]{0, 0, 0, 20, 80, 0}, weaponTierProbs(4, 20, false), 0f);
		assertArrayEquals(new float[]{0, 0, 0, 19, 80, 1}, weaponTierProbs(4, 21, false), 0f);
		assertArrayEquals(new float[]{0, 0, 0, 19, 80, 1}, weaponTierProbs(4, 25, false), 0f);
		assertArrayEquals(new float[]{0, 0, 0, 20, 80, 0}, weaponTierProbs(4, 26, false), 0f);
		assertEquals(0f, weaponTierProbs(0, 21, false)[5], 0f);
	}

	@Test
	public void randomModeStillRespectsTheTierSixDepthAndPercentageLimits() throws Exception {
		float[] outsideHalls = weaponTierProbs(4, 20, true);
		float[] insideHalls = weaponTierProbs(4, 21, true);

		assertEquals(0f, outsideHalls[5], 0f);
		assertEquals(1f, insideHalls[5], 0f);
		assertEquals(100f, sum(insideHalls), 0.0001f);
	}

	@Test
	public void towerFloorsUseDedicatedEquipmentWeights() throws Exception {
		float[] expected = {0, 0, 0, 5, 60, 35};

		assertArrayEquals(expected,
				weaponTierProbs(0, 1, TowerLevel.BRANCH, false), 0f);
		assertArrayEquals(expected,
				weaponTierProbs(4, 30, TowerLevel.BRANCH, false), 0f);
		assertArrayEquals(expected,
				weaponTierProbs(2, 8, TowerLevel.BRANCH, true), 0f);
	}

	@Test
	public void towerArmorFoldsMissingTierSixWeightIntoTierFive() throws Exception {
		assertArrayEquals(new float[]{0, 0, 0, 5, 95},
				armorTierProbs(0, 1, TowerLevel.BRANCH, false), 0f);
		assertArrayEquals(new float[]{0, 0, 0, 5, 95},
				armorTierProbs(4, 30, TowerLevel.BRANCH, true), 0f);
	}

	@Test
	public void nonTowerBranchesKeepTheirExistingWeights() throws Exception {
		assertArrayEquals(new float[]{0, 0, 0, 19, 80, 1},
				weaponTierProbs(4, 21, 0, false), 0f);
		assertArrayEquals(new float[]{0, 75, 20, 4, 1},
				armorTierProbs(0, 1, 0, false), 0f);
	}

	@Test
	public void journalAndTestGeneratorIncludeTierSixWeapon() throws Exception {
		String catalogSource = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java")),
				StandardCharsets.UTF_8);
		assertTrue(catalogSource.contains(
				"MELEE_WEAPONS.addItems(Generator.Category.WEP_T6.classes);"));

		Method weaponList = TestMelee.class.getDeclaredMethod("weaponList", int.class);
		weaponList.setAccessible(true);
		Class<? extends Weapon>[] tierSix = (Class<? extends Weapon>[]) weaponList.invoke(null, 6);
		assertArrayEquals(new Class<?>[]{
				GreatGreatGreatsword.class,
				SakuraBlossomBlade.class,
				ChainMace.class,
				TwoHandedGreatsword.class,
				PalermoSword.class,
				MercuryBlade.class,
				AuxiliaryCore.class,
				HundredTonHammer.class,
				DemonTailWhip.class,
				OracleTerminal.class,
				VenomousSickle.class,
				RadiantGoldHalberd.class
		}, tierSix);
	}

	@Test
	public void tierSixMissilePoolContainsGungnirAndPortableBlackHole() throws Exception {
		Generator.Category tierSix = Generator.Category.valueOf("MIS_T6");
		assertArrayEquals(new Class<?>[]{Gungnir.class, PortableBlackHole.class}, tierSix.classes);
		assertArrayEquals(new float[]{1f, 1f}, tierSix.defaultProbs, 0f);
		assertEquals(6, Generator.misTiers.length);

		Method availableTier = Generator.class.getDeclaredMethod("availableMissileTier", int.class);
		availableTier.setAccessible(true);
		assertEquals(5, availableTier.invoke(null, 5));
	}

	@Test
	public void customGuideExplainsTierSixGeneration() throws Exception {
		String customText = new String(Files.readAllBytes(Paths.get(
				"src/main/assets/messages/custom/custom_zh.properties")), StandardCharsets.UTF_8);

		assertTrue(customText.contains("_6阶：_0%，0%，0%，0%，1%"));
		assertTrue(customText.contains("近战武器与投掷武器均按此权重生成"));
		assertTrue(customText.contains("高塔(-1层及更小楼层)使用独立装备阶数权重"));
		assertTrue(customText.contains("护甲实际为4阶5%、5阶95%"));
		assertFalse(customText.contains("当前没有6阶投掷武器"));
	}

	private float[] weaponTierProbs(int floorSet, int depth, boolean randomMode) throws Exception {
		Method method = Generator.class.getDeclaredMethod("weaponTierProbs", int.class, int.class, boolean.class);
		method.setAccessible(true);
		return (float[]) method.invoke(null, floorSet, depth, randomMode);
	}

	private float[] weaponTierProbs(int floorSet, int depth, int branch, boolean randomMode)
			throws Exception {
		Method method = Generator.class.getDeclaredMethod("weaponTierProbs",
				int.class, int.class, int.class, boolean.class);
		method.setAccessible(true);
		return (float[]) method.invoke(null, floorSet, depth, branch, randomMode);
	}

	private float[] armorTierProbs(int floorSet, int depth, int branch, boolean randomMode)
			throws Exception {
		Method method = Generator.class.getDeclaredMethod("armorTierProbs",
				int.class, int.class, int.class, boolean.class);
		method.setAccessible(true);
		return (float[]) method.invoke(null, floorSet, depth, branch, randomMode);
	}

	private float sum(float[] values) {
		float result = 0;
		for (float value : values) {
			result += value;
		}
		return result;
	}
}
