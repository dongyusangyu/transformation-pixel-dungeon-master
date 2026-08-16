package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.SpearShield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AssassinsBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dirk;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.SakuraBlossomBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Kunai;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SurpriseWeaponSpearShieldTest {

	private static final Map<KindOfWeapon, RangeConfig> RANGE_CONFIGS = new IdentityHashMap<>();
	private Files previousFiles;

	@Before
	public void installHeadlessFiles() {
		previousFiles = Gdx.files;
		if (Gdx.files == null) Gdx.files = new HeadlessFiles();
	}

	@After
	public void resetDungeonState() {
		Dungeon.hero = null;
		Gdx.files = previousFiles;
		RANGE_CONFIGS.clear();
	}

	@Test
	public void heroRangeRollUsesOriginalBoundsWithoutSpearShield() throws Exception {
		TestHero hero = testHero();
		Dungeon.hero = hero;
		RangeProbeDagger weapon = allocateWithoutConstructor(RangeProbeDagger.class);

		for (int i = 0; i < 100; i++) {
			int damage = weapon.rollHeroRange(20, 80);
			assertTrue(damage >= 20 && damage <= 80);
		}
	}

	@Test
	public void heroRangeRollAppliesSpearModeToExplicitSurpriseBounds() throws Exception {
		TestHero hero = testHero();
		Dungeon.hero = hero;
		addSpearShield(hero, 1, 3);

		assertEquals(80, allocateWithoutConstructor(RangeProbeDagger.class).rollHeroRange(20, 80));
	}

	@Test
	public void heroRangeRollAppliesShieldModeAndClampsAtSuppliedMinimum() throws Exception {
		TestHero hero = testHero();
		Dungeon.hero = hero;
		addSpearShield(hero, 2, 3);

		assertEquals(60, allocateWithoutConstructor(RangeProbeDagger.class).rollHeroRange(60, 80));
	}

	@Test
	public void heroRangeRollDelegatesBothBoundsToSpearShield() throws Exception {
		TestHero hero = testHero();
		Dungeon.hero = hero;
		TrackingSpearShield shield = allocateWithoutConstructor(TrackingSpearShield.class);
		hero.belongings.backpack.items.add(shield);

		int damage = allocateWithoutConstructor(RangeProbeDagger.class).rollHeroRange(20, 80);

		assertEquals(1, shield.minCalls);
		assertEquals(1, shield.maxCalls);
		assertTrue(damage >= 3 && damage <= 7);
	}

	@Test
	public void spearCooldownStillDelegatesMinimumToSpearShield() throws Exception {
		TestHero hero = testHero();
		Dungeon.hero = hero;
		SpearShield shield = addSpearShield(hero, 1, 3);
		SpearShield.SpearShieldCooldown cooldown = Buff.affect(
				hero, SpearShield.SpearShieldCooldown.class);

		assertEquals(0, shield.changeDmgMin(60, 80));
		assertTrue(allocateWithoutConstructor(RangeProbeDagger.class).rollHeroRange(60, 80) >= 0);
		assertTrue(cooldown != null);
	}

	@Test
	public void nonHeroDamageRollIgnoresSpearShieldInHeroBackpack() throws Exception {
		TestHero hero = testHero();
		Dungeon.hero = hero;
		addSpearShield(hero, 1, 3);
		Rat mob = new Rat();
		Dagger weapon = allocateWithoutConstructor(Dagger.class);
		weapon.tier = 1;
		weapon.augment = Weapon.Augment.NONE;

		Random.pushGenerator(18731L);
		try {
			boolean rolledBelowMaximum = false;
			for (int i = 0; i < 100; i++) {
				if (weapon.damageRoll(mob) < weapon.max()) {
					rolledBelowMaximum = true;
					break;
				}
			}
			assertTrue(rolledBelowMaximum);
		} finally {
			Random.popGenerator();
		}
	}

	@Test
	public void surpriseWeaponsUseSpearShieldAdjustedBounds() throws Exception {
		TestHero hero = testHero();
		Dungeon.hero = hero;
		AlwaysSurprisedRat enemy = allocateWithoutConstructor(AlwaysSurprisedRat.class);
		hero.chooseEnemy(enemy);
		TrackingSpearShield shield = allocateWithoutConstructor(TrackingSpearShield.class);
		hero.belongings.backpack.items.add(shield);

		assertSurpriseRoll(allocateWeapon(TrackingDagger.class, 1), 0.75f);
		assertSurpriseRoll(allocateWeapon(TrackingDirk.class, 2), 0.67f);
		assertSurpriseRoll(allocateWeapon(TrackingAssassinsBlade.class, 4), 0.50f);
		assertSurpriseRoll(allocateWeapon(TrackingThrowingKnife.class, 1), 0.75f);
		assertSurpriseRoll(allocateWeapon(TrackingKunai.class, 3), 0.60f);
		assertSurpriseRoll(allocateWeapon(TrackingSakuraBlossomBlade.class, 6), 0.50f);
		assertEquals(8, shield.minCalls);
		assertEquals(8, shield.maxCalls);
	}

	@Test
	public void dirkAndKunaiStillKeepTheirOrdinaryDamageFallback() throws Exception {
		TestHero hero = testHero();
		Dungeon.hero = hero;
		AlwaysSurprisedRat enemy = allocateWithoutConstructor(AlwaysSurprisedRat.class);
		hero.chooseEnemy(enemy);

		TrackingDirk dirk = allocateWeapon(TrackingDirk.class, 2);
		TrackingKunai kunai = allocateWeapon(TrackingKunai.class, 3);
		assertFallbackCalls(dirk, 0.67f);
		assertFallbackCalls(kunai, 0.60f);
	}

	private static void assertSurpriseRoll(Weapon weapon, float surpriseFactor) {
		Hero hero = Dungeon.hero;
		Dungeon.hero = null;
		int originalMin = weapon.min();
		int originalMax = weapon.max();
		Dungeon.hero = hero;
		int expectedMin = originalMin + Math.round((originalMax - originalMin) * surpriseFactor);
		RangeConfig config = RANGE_CONFIGS.get(weapon);

		damageRollWithoutGlobalHero(weapon, hero);

		assertTrue(containsRange(config.ranges, expectedMin, originalMax));
	}

	private static void assertFallbackCalls(Weapon weapon, float surpriseFactor) {
		RangeConfig config = RANGE_CONFIGS.get(weapon);
		Hero hero = Dungeon.hero;
		Dungeon.hero = null;
		config.normalMin = weapon.min();
		config.normalMax = weapon.max();
		Dungeon.hero = hero;
		config.surpriseMin = config.normalMin
				+ Math.round((config.normalMax - config.normalMin) * surpriseFactor);
		config.surpriseMax = config.normalMax;
		damageRollWithoutGlobalHero(weapon, hero);

		assertEquals(2, config.ranges.size());
		assertTrue(containsRange(config.ranges, config.normalMin, config.normalMax));
		assertTrue(containsRange(config.ranges, config.surpriseMin, config.surpriseMax));
	}

	private static int damageRollWithoutGlobalHero(Weapon weapon, Hero hero) {
		Dungeon.hero = null;
		try {
			return weapon.damageRoll(hero);
		} finally {
			Dungeon.hero = hero;
		}
	}

	private static boolean containsRange(List<int[]> ranges, int min, int max) {
		for (int[] range : ranges) {
			if (range[0] == min && range[1] == max) return true;
		}
		return false;
	}

	private static <T extends Weapon> T allocateWeapon(Class<T> type, int tier) throws Exception {
		T weapon = allocateWithoutConstructor(type);
		if (weapon instanceof MeleeWeapon) {
			((MeleeWeapon) weapon).tier = tier;
		} else if (weapon instanceof MissileWeapon) {
			((MissileWeapon) weapon).tier = tier;
		}
		weapon.augment = Weapon.Augment.NONE;
		RANGE_CONFIGS.put(weapon, new RangeConfig());
		return weapon;
	}

	private static SpearShield addSpearShield(TestHero hero, int mode, int level) throws Exception {
		SpearShield shield = allocateWithoutConstructor(SpearShield.class);
		Bundle bundle = new Bundle();
		bundle.put(SpearShield.MODE, mode);
		shield.restoreFromBundle(bundle);
		shield.level(level);
		hero.belongings.backpack.items.add(shield);
		return shield;
	}

	private static class RangeProbeDagger extends Dagger {
		int rollHeroRange(int min, int max) {
			return heroDamageRangeRoll(min, max);
		}
	}

	private static class TrackingDagger extends Dagger {
		@Override
		protected int heroDamageRangeRoll(Hero hero, int min, int max) {
			RANGE_CONFIGS.get(this).ranges.add(new int[]{min, max});
			return super.heroDamageRangeRoll(hero, min, max);
		}
	}

	private static class TrackingDirk extends Dirk {
		@Override
		protected int heroDamageRangeRoll(Hero hero, int min, int max) {
			RANGE_CONFIGS.get(this).ranges.add(new int[]{min, max});
			return super.heroDamageRangeRoll(hero, min, max);
		}
	}

	private static class TrackingAssassinsBlade extends AssassinsBlade {
		@Override
		protected int heroDamageRangeRoll(Hero hero, int min, int max) {
			RANGE_CONFIGS.get(this).ranges.add(new int[]{min, max});
			return super.heroDamageRangeRoll(hero, min, max);
		}
	}

	private static class TrackingThrowingKnife extends ThrowingKnife {
		@Override
		protected int heroDamageRangeRoll(Hero hero, int min, int max) {
			RANGE_CONFIGS.get(this).ranges.add(new int[]{min, max});
			return super.heroDamageRangeRoll(hero, min, max);
		}
	}

	private static class TrackingKunai extends Kunai {
		@Override
		protected int heroDamageRangeRoll(Hero hero, int min, int max) {
			RANGE_CONFIGS.get(this).ranges.add(new int[]{min, max});
			return super.heroDamageRangeRoll(hero, min, max);
		}
	}

	private static class TrackingSakuraBlossomBlade extends SakuraBlossomBlade {
		@Override
		protected int heroDamageRangeRoll(Hero hero, int min, int max) {
			RANGE_CONFIGS.get(this).ranges.add(new int[]{min, max});
			return super.heroDamageRangeRoll(hero, min, max);
		}
	}

	private static class RangeConfig {
		final List<int[]> ranges = new ArrayList<>();
		int normalMin = Integer.MIN_VALUE;
		int normalMax = Integer.MIN_VALUE;
		int surpriseMin = Integer.MIN_VALUE;
		int surpriseMax = Integer.MIN_VALUE;
	}

	private static class TrackingSpearShield extends SpearShield {
		int minCalls;
		int maxCalls;

		@Override
		public int changeDmgMin(int min, int max) {
			minCalls++;
			return 3;
		}

		@Override
		public int changeDmgMax(int min, int max) {
			maxCalls++;
			return 7;
		}
	}

	private static class TestHero extends Hero {
		@Override
		public int STR() {
			return 0;
		}
	}

	private static class AlwaysSurprisedRat extends Rat {
		@Override
		public boolean surprisedBy(Char enemy, boolean attacking) {
			return true;
		}
	}

	private static TestHero testHero() throws Exception {
		TestHero hero = allocateWithoutConstructor(TestHero.class);
		hero.talents = new ArrayList<>();
		hero.metamorphedTalents = new LinkedHashMap<>();
		hero.sublimationTalents = new LinkedHashMap<>();
		hero.negativeTalents = new LinkedHashMap<>();

		Field buffs = Char.class.getDeclaredField("buffs");
		buffs.setAccessible(true);
		buffs.set(hero, new LinkedHashSet<>());
		Field immunities = Char.class.getDeclaredField("immunities");
		immunities.setAccessible(true);
		immunities.set(hero, new HashSet<>());
		Field properties = Char.class.getDeclaredField("properties");
		properties.setAccessible(true);
		properties.set(hero, new HashSet<>());

		Belongings belongings = allocateWithoutConstructor(Belongings.class);
		Belongings.Backpack backpack = allocateWithoutConstructor(Belongings.Backpack.class);
		backpack.items = new ArrayList<>();
		backpack.owner = hero;
		belongings.backpack = backpack;
		Field owner = Belongings.class.getDeclaredField("owner");
		owner.setAccessible(true);
		owner.set(belongings, hero);
		hero.belongings = belongings;
		return hero;
	}

	private static class HeadlessFiles implements Files {
		private FileHandle asset(String path) {
			return new FileHandle("src/main/assets/" + path);
		}

		@Override
		public FileHandle getFileHandle(String path, FileType type) {
			return asset(path);
		}

		@Override
		public FileHandle classpath(String path) {
			return asset(path);
		}

		@Override
		public FileHandle internal(String path) {
			return asset(path);
		}

		@Override
		public FileHandle external(String path) {
			return asset(path);
		}

		@Override
		public FileHandle absolute(String path) {
			return asset(path);
		}

		@Override
		public FileHandle local(String path) {
			return asset(path);
		}

		@Override
		public String getExternalStoragePath() {
			return "";
		}

		@Override
		public boolean isExternalStorageAvailable() {
			return false;
		}

		@Override
		public String getLocalStoragePath() {
			return ".";
		}

		@Override
		public boolean isLocalStorageAvailable() {
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		return (T) ((Unsafe) unsafeField.get(null)).allocateInstance(type);
	}
}
