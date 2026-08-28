package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Shuriken_Box;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Tatteki;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.MercuryBlade;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QianfaThrowingTest {

	@Test
	public void ordinaryMissilesUseTheWholeStackAfterAProc() {
		TestMissile missile = new TestMissile();
		missile.quantity(5);

		assertEquals(5, MissileWeapon.qianfaVolleyCount(missile, 1, 0, true));
	}

	@Test
	public void procRollAndCharTargetAreCheckedOncePerAction() {
		TestMissile missile = new TestMissile();
		missile.quantity(5);

		assertEquals(1, MissileWeapon.qianfaVolleyCount(missile, 1, 1, true));
		assertEquals(1, MissileWeapon.qianfaVolleyCount(missile, 3, 3, true));
		assertEquals(1, MissileWeapon.qianfaVolleyCount(missile, 3, 0, false));
	}

	@Test
	public void virtualPlayerProjectilesRepeatThreeTimes() {
		assertRepeatProjectile(Shuriken_Box.SmallShuriken.class);
		assertRepeatProjectile(SpiritBow.SpiritArrow.class);
		assertRepeatProjectile(Tatteki.Tamaru.class);
		assertRepeatProjectile(MercuryBlade.MercuryProjectile.class);

		assertEquals(3, MissileWeapon.qianfaVolleyCount(
				new TestRepeatMissile(), 3, 0, true));
	}

	@Test
	public void missileCastOwnsTheVolleyAndOldTripleDamageIsRemoved() throws Exception {
		String missileSource = readMainSource(
				"items/weapon/missiles/MissileWeapon.java");
		String talentSource = readMainSource("actors/hero/Talent.java");

		assertTrue(missileSource.contains("castQianfaVolley"));
		assertTrue(missileSource.contains("qianfaVolleyCount(this"));
		assertTrue(missileSource.contains("projectile.onThrow(shotCell)"));
		assertTrue(missileSource.contains("finishQianfaVolley"));
		assertTrue(!talentSource.contains(
				"hero.hasTalent(QIANFA_THROWING) && hero.pointsInTalent(QIANFA_THROWING)>Random.Int(10)"));
	}

	@Test
	public void talentTextDescribesStacksAndVirtualTripleShots() throws Exception {
		String chinese = readAsset("messages/actors/actors_zh.properties");
		String english = readAsset("messages/actors/actors.properties");

		assertTrue(chinese.contains("将整组投掷武器连续投出"));
		assertTrue(chinese.contains("连续攻击3次"));
		assertTrue(!chinese.contains("qianfa_throwing.desc=_+1：_当忍者掷出投掷武器或小小手里剑时，有_10%概率_造成3倍伤害"));
		assertTrue(english.contains("throws the entire stack in succession"));
		assertTrue(english.contains("attack 3 times in succession"));
	}

	private static void assertRepeatProjectile(Class<? extends MissileWeapon> type) {
		assertTrue(MissileWeapon.QianfaRepeatProjectile.class.isAssignableFrom(type));
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

	private static class TestMissile extends MissileWeapon {
		@Override
		public int STRReq(int lvl) {
			return 0;
		}
	}

	private static class TestRepeatMissile extends TestMissile
			implements MissileWeapon.QianfaRepeatProjectile {
	}
}
