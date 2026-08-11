package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

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

public class MercuryBladeTest {

	@Test
	public void tierSixScimitarStatsUseSpecifiedCurves() {
		assertEquals(6, MercuryBlade.TIER);
		assertEquals(20, MercuryBlade.strengthRequirementForLevel(0));
		assertEquals(6, MercuryBlade.minForLevel(0));
		assertEquals(28, MercuryBlade.maxForLevel(0));
		assertEquals(9, MercuryBlade.minForLevel(3));
		assertEquals(49, MercuryBlade.maxForLevel(3));
		assertEquals(0.8f, MercuryBlade.DELAY, 0f);
		assertEquals(1, MercuryBlade.RANGE);
	}

	@Test
	public void virtualThrowUsesTierFiveTridentDamageCurve() {
		assertEquals(10, MercuryBlade.throwMinForLevel(0));
		assertEquals(25, MercuryBlade.throwMaxForLevel(0));
		assertEquals(16, MercuryBlade.throwMinForLevel(3));
		assertEquals(40, MercuryBlade.throwMaxForLevel(3));
	}

	@Test
	public void solidificationChangesResourceAndOozeRules() {
		assertEquals(1, MercuryBlade.liquidMetalCost(false));
		assertEquals(0, MercuryBlade.liquidMetalCost(true));
		assertEquals(2f, MercuryBlade.oozeDuration(false), 0f);
		assertEquals(3f, MercuryBlade.oozeDuration(true), 0f);
		assertEquals(5f, MercuryBlade.solidificationDuration(), 0f);
		assertEquals(1, MercuryBlade.abilityChargeCost());
	}

	@Test
	public void quickActionPrefersAbilityOnlyWhenItIsUsableAndBuffIsAbsent() {
		assertTrue(MercuryBlade.defaultActionPrefersAbility(true, false));
		assertFalse(MercuryBlade.defaultActionPrefersAbility(false, false));
		assertFalse(MercuryBlade.defaultActionPrefersAbility(true, true));
		assertFalse(MercuryBlade.defaultActionPrefersAbility(false, true));
	}

	@Test
	public void shootingHasItsOwnActionAndQuickslotTargetingState() {
		assertEquals("SHOOT", MercuryBlade.AC_SHOOT);
		assertTrue(MercuryBlade.actionUsesTargeting(MercuryBlade.AC_SHOOT));
		assertFalse(MercuryBlade.actionUsesTargeting(MercuryBlade.AC_THROW));
		assertFalse(MercuryBlade.actionUsesTargeting(MercuryBlade.AC_ABILITY));
		assertFalse(MercuryBlade.canShootAt(17, 17));
		assertTrue(MercuryBlade.canShootAt(17, 18));
	}

	@Test
	public void equippedQuickActionUsesShootWheneverAbilityIsNotPreferred() {
		assertEquals(MercuryBlade.AC_ABILITY,
				MercuryBlade.equippedDefaultAction(true, false));
		assertEquals(MercuryBlade.AC_SHOOT,
				MercuryBlade.equippedDefaultAction(false, false));
		assertEquals(MercuryBlade.AC_SHOOT,
				MercuryBlade.equippedDefaultAction(true, true));
	}

	@Test
	public void virtualProjectileDelegatesWeaponStateAndCannotDrop() {
		MercuryBlade blade = new MercuryBlade();
		blade.level(3);
		blade.augment = Weapon.Augment.DAMAGE;
		blade.enchant(new Blazing());
		MercuryBlade.MercuryProjectile projectile = blade.new MercuryProjectile();

		assertEquals(blade.buffedLvl(), projectile.buffedLvl());
		assertSame(blade.augment, projectile.augment);
		assertSame(blade.getEnchant(), projectile.getEnchant());
		assertEquals(MercuryBlade.throwMinForLevel(3), projectile.min(3));
		assertEquals(MercuryBlade.throwMaxForLevel(3), projectile.max(3));
		assertTrue(projectile.spawnedForEffect);
		assertFalse(projectile.benefitsFromSharpshooting());
		assertTrue(new MissileWeapon() {
			@Override
			public int min(int lvl) {
				return 0;
			}

			@Override
			public int max(int lvl) {
				return 0;
			}

			@Override
			public int STRReq(int lvl) {
				return 0;
			}
		}.benefitsFromSharpshooting());
		assertEquals(1, projectile.defaultQuantity());
	}

	@Test
	public void generatorIncludesMercuryBladeWithMatchingWeights() {
		assertTrue(Arrays.asList(Generator.Category.WEP_T6.classes).contains(MercuryBlade.class));
		assertEquals(Generator.Category.WEP_T6.classes.length,
				Generator.Category.WEP_T6.defaultProbs.length);
	}

	@Test
	public void sourceKeepsVirtualProjectileAndBuffInsideWeaponFile() throws Exception {
		String source = readMainSource(
				"items/weapon/melee/tier6/MercuryBlade.java");

		assertTrue(source.contains("class MercuryProjectile extends MissileWeapon"));
		assertTrue(source.contains("class MercurySolidification extends FlavourBuff"));
		assertTrue(source.contains("MercuryBlade.this.proc(attacker, defender, damage)"));
		assertTrue(source.contains("MercuryBlade.this.delayFactor(user)"));
		assertTrue(source.contains("spawnedForEffect = true"));
		assertTrue(source.contains("metal.detach(user.belongings.backpack)"));
		assertTrue(source.contains(
				"return new MercuryProjectile().targetingPos(user, dst)"));
	}

	@Test
	public void shootUsesDedicatedSelectorWhileThrowUsesInheritedCast() throws Exception {
		String source = readMainSource(
				"items/weapon/melee/tier6/MercuryBlade.java");

		assertTrue(source.contains("GameScene.selectCell"));
		assertTrue(source.contains("shoot(hero, cell)"));
		assertFalse(source.contains("public void cast(Hero user, int dst)"));
	}

	@Test
	public void exSpriteConstantUsesFrame150WithMeasuredBounds() throws Exception {
		String source = readMainSource("sprites/EXItemSpriteSheet.java");
		assertTrue(source.contains("MERCURY_BLADE = encode(150, 15, 16)"));
	}

	@Test
	public void mercuryProjectileFlightDoesNotRotate() throws Exception {
		String source = readMainSource("sprites/MissileSprite.java");
		assertTrue(source.contains(
				"ANGULAR_SPEEDS.put(MercuryBlade.MercuryProjectile.class, 0)"));
	}

	private static String readMainSource(String relativePath) throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon").resolve(relativePath);
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
	}
}
