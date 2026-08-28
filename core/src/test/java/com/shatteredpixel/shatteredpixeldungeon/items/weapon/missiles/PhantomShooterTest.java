package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhantomShooterTest {

	@Test
	public void talentPointsMapToTenTwentyAndThirtyPercent() {
		assertEquals(0, MissileWeapon.phantomShooterChance(0));
		assertEquals(10, MissileWeapon.phantomShooterChance(1));
		assertEquals(20, MissileWeapon.phantomShooterChance(2));
		assertEquals(30, MissileWeapon.phantomShooterChance(3));
		assertEquals(30, MissileWeapon.phantomShooterChance(99));
	}

	@Test
	public void phantomShooterRollUsesTheConfiguredProbability() {
		assertTrue(MissileWeapon.shouldTriggerPhantomShooter(1, 0));
		assertFalse(MissileWeapon.shouldTriggerPhantomShooter(1, 1));
		assertTrue(MissileWeapon.shouldTriggerPhantomShooter(2, 1));
		assertFalse(MissileWeapon.shouldTriggerPhantomShooter(2, 2));
		assertTrue(MissileWeapon.shouldTriggerPhantomShooter(3, 2));
		assertFalse(MissileWeapon.shouldTriggerPhantomShooter(3, 3));
	}

	@Test
	public void ordinaryPhantomProjectilePreservesCombatStateWithoutARealStack() {
		TestMissile original = new TestMissile();
		original.level(3);
		original.augment = Weapon.Augment.DAMAGE;
		original.enchantment = new Blazing();
		original.cursed = true;
		original.curseInfusionBonus = true;
		original.masteryPotionBonus = true;
		original.quantity(4);

		MissileWeapon phantom = original.phantomCopy();

		assertEquals(original.trueLevel(), phantom.trueLevel());
		assertEquals(original.level(), phantom.level());
		assertEquals(Weapon.Augment.DAMAGE, phantom.augment);
		assertTrue(phantom.enchantment instanceof Blazing);
		assertTrue(phantom.cursed);
		assertTrue(phantom.curseInfusionBonus);
		assertTrue(phantom.masteryPotionBonus);
		assertEquals(0, phantom.quantity());
		assertTrue(phantom.phantomProjectile);
		assertTrue(phantom.spawnedForEffect);
	}

	@Test
	public void phantomProjectileDoesNotConsumeDurabilityOrReachBreakState() {
		TestMissile phantom = new TestMissile();
		phantom.durability = 1f;
		phantom.spawnedForEffect = true;

		phantom.decrementDurability();

		assertEquals(1f, phantom.durability, 0f);
	}

	@Test
	public void successfulThrowUsesVirtualProjectilePipelineAndGuardsRecursion() throws Exception {
		String source = readMainSource("items/weapon/missiles/MissileWeapon.java");

		assertTrue(source.contains("onSuccessfulThrow"));
		assertTrue(source.contains("createPhantomProjectile"));
		assertTrue(source.contains("phantomProjectile"));
		assertTrue(source.contains("schedulePhantomThrow"));
		assertTrue(source.contains("Actor.add(new Actor()"));
		assertTrue(source.contains("phantom.onThrow"));
		assertFalse(source.contains("defender.damage((int)(damage*hero.pointsInTalent(Talent.PHANTOM_SHOOTER)*0.1)"));
	}

	@Test
	public void specialProjectilesCallTheCommonSuccessfulThrowHook() throws Exception {
		assertTrue(readMainSource("items/artifacts/Shuriken_Box.java")
				.contains("onSuccessfulThrow(enemy)"));
		assertTrue(readMainSource("items/weapon/SpiritBow.java")
				.contains("onSuccessfulThrow(enemy)"));
		assertTrue(readMainSource("items/weapon/Tatteki.java")
				.contains("onSuccessfulThrow(enemy)"));
	}

	@Test
	public void talentTextDescribesAChanceForAFullVirtualThrow() throws Exception {
		String chinese = readAsset("messages/actors/actors_zh.properties");
		assertTrue(chinese.contains("有_10%概率_额外进行一次完整的幻影投掷"));
		assertTrue(chinese.contains("有_20%概率_额外进行一次完整的幻影投掷"));
		assertTrue(chinese.contains("有_30%概率_额外进行一次完整的幻影投掷"));
	}

	private static String readMainSource(String relativePath) throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon").resolve(relativePath);
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
	}

	private static String readAsset(String relativePath) throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve("src/main/assets")
				.resolve(relativePath)), StandardCharsets.UTF_8);
	}

	public static class TestMissile extends MissileWeapon {
		MissileWeapon phantomCopy() {
			return createPhantomProjectile();
		}
	}
}
