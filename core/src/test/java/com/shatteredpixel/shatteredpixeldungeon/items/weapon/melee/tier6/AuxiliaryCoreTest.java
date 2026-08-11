package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AuxiliaryCoreTest {

	@Test
	public void tierSixStatsUseSpecifiedDamageCurve() {
		assertEquals(6, AuxiliaryCore.TIER);
		assertEquals(6, AuxiliaryCore.minForLevel(0));
		assertEquals(25, AuxiliaryCore.maxForLevel(0));
		assertEquals(9, AuxiliaryCore.minForLevel(3));
		assertEquals(46, AuxiliaryCore.maxForLevel(3));
		assertEquals(20, AuxiliaryCore.strengthRequirementForLevel(0));
		assertEquals(1, AuxiliaryCore.RANGE);
		assertEquals(1f, AuxiliaryCore.DELAY, 0f);
	}

	@Test
	public void ownMagicDamageAndAbilityNumbersAreFixed() {
		assertEquals(10, AuxiliaryCore.magicDamage(false));
		assertEquals(20, AuxiliaryCore.magicDamage(true));
		assertEquals(1, AuxiliaryCore.abilityChargeCost());
		assertEquals(3f, AuxiliaryCore.boostDuration(), 0f);
		assertEquals(BuffIndicator.UPGRADE,
				new AuxiliaryCore.MagicPowerBoost().icon());
	}

	@Test
	public void infusionAcceptsOnlyTierOneToFiveMeleeAndMissileWeapons() {
		AuxiliaryCore core = new AuxiliaryCore();

		assertTrue(core.canInfuse(new TestMeleeWeapon(1)));
		assertTrue(core.canInfuse(new TestMissileWeapon(5)));
		assertFalse(core.canInfuse(new TestMissileWeapon(6)));
		assertFalse(core.canInfuse(new AuxiliaryCore()));
		assertFalse(core.canInfuse(core));
		assertFalse(core.canInfuse(new Item()));
	}

	@Test
	public void infusionOverwritesOnlyAugmentState() {
		AuxiliaryCore core = new AuxiliaryCore();
		TestMeleeWeapon target = new TestMeleeWeapon(3);
		Blazing enchantment = new Blazing();
		target.level(3);
		target.augment = Weapon.Augment.DAMAGE;
		target.enchant(enchantment);

		core.infuseWeapon(target);

		assertEquals(Weapon.Augment.MAGIC, target.augment);
		assertEquals(3, target.level());
		assertSame(enchantment, target.getEnchant());
	}

	@Test
	public void generatorIncludesAuxiliaryCoreWithMatchingWeights() {
		assertTrue(Arrays.asList(Generator.Category.WEP_T6.classes)
				.contains(AuxiliaryCore.class));
		assertEquals(Generator.Category.WEP_T6.classes.length,
				Generator.Category.WEP_T6.defaultProbs.length);
	}

	@Test
	public void sourceHandlesBackpackAndBothEquippedWeaponSlots() throws Exception {
		String source = readMainSource(
				"items/weapon/melee/tier6/AuxiliaryCore.java");
		assertTrue(source.contains("belongings.weapon == this"));
		assertTrue(source.contains("belongings.secondWep == this"));
		assertTrue(source.contains("detach(hero.belongings.backpack)"));
		assertTrue(source.contains("target.augment = Weapon.Augment.MAGIC"));
	}

	private static String readMainSource(String relativePath) throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon").resolve(relativePath);
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
	}

	private static class TestMeleeWeapon extends MeleeWeapon {
		TestMeleeWeapon(int weaponTier) {
			tier = weaponTier;
		}
	}

	private static class TestMissileWeapon extends MissileWeapon {
		TestMissileWeapon(int weaponTier) {
			tier = weaponTier;
		}
	}
}
