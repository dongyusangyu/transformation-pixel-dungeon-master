package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeaponMagicAugmentTest {

	@Test
	public void magicAugmentKeepsBaseDamageAndDelayFactors() {
		assertEquals(100, Weapon.Augment.MAGIC.damageFactor(100));
		assertEquals(1f, Weapon.Augment.MAGIC.delayFactor(1f), 0f);
	}

	@Test
	public void magicDamageUsesWeaponTierAsLowerBoundAndTenAsUpperBound() {
		for (int tier = 1; tier <= 5; tier++) {
			assertEquals(tier, Weapon.magicDamageMinForTier(tier));
			assertEquals(10, Weapon.magicDamageMax());
		}
		assertEquals(1, Weapon.magicDamageMinForTier(0));
		assertEquals(5, Weapon.magicDamageMinForTier(6));
	}

	@Test
	public void magicAugmentChoosesReachOrSpeedFromBaseReach() {
		assertEquals(1, Weapon.magicReachBonus(1));
		assertEquals(0, Weapon.magicReachBonus(2));
		assertEquals(0, Weapon.magicReachBonus(3));
		assertEquals(1f, Weapon.magicDelayMultiplier(1), 0f);
		assertEquals(2f / 3f, Weapon.magicDelayMultiplier(2), 0.0001f);
		assertEquals(2f / 3f, Weapon.magicDelayMultiplier(3), 0.0001f);
	}

	@Test
	public void magicAugmentUsesExistingWeaponBundleField() throws Exception {
		String source = readMainSource("items/weapon/Weapon.java");
		assertTrue(source.contains("bundle.put( AUGMENT, augment )"));
		assertTrue(source.contains("bundle.getEnum(AUGMENT, Augment.class)"));
	}

	@Test
	public void weaponSourceAppliesMagicDamageAndDynamicReachAndDelay() throws Exception {
		String source = readMainSource("items/weapon/Weapon.java");
		assertTrue(source.contains("Random.IntRange(weaponTier(), magicDamageMax())"));
		assertTrue(source.contains("DamageTag.MAGICAL"));
		assertTrue(source.contains("reach += magicReachBonus(RCH)"));
		assertTrue(source.contains("delay *= magicDelayMultiplier(RCH)"));
	}

	@Test
	public void augmentationStoneCannotCreateMagicAugmentation() throws Exception {
		String source = readMainSource("items/stones/StoneOfAugmentation.java");
		assertTrue(source.contains("if (aug == Weapon.Augment.MAGIC) continue;"));
	}

	@Test
	public void meleeAndMissileInfoDescribeMagicAugmentation() throws Exception {
		String melee = readMainSource("items/weapon/melee/MeleeWeapon.java");
		String missile = readMainSource("items/weapon/missiles/MissileWeapon.java");
		String ritualDagger = readMainSource("items/weapon/melee/RitualDagger.java");
		assertTrue(melee.contains("case MAGIC:"));
		assertTrue(melee.contains("Messages.get(Weapon.class, \"magical\", weaponTier())"));
		assertTrue(missile.contains("case MAGIC:"));
		assertTrue(missile.contains("Messages.get(Weapon.class, \"magical\", weaponTier())"));
		assertTrue(ritualDagger.contains("case MAGIC:"));
		assertTrue(ritualDagger.contains("Messages.get(Weapon.class, \"magical\", weaponTier())"));
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
		{
			tier = 1;
		}
	}
}
