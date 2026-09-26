package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CorruptionDebuffRules;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCursedFlame;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;


import static org.junit.Assert.*;

public class CursedFlameResistanceTest {
	private static HeadlessItemSprites sprites;
	@BeforeClass public static void installSheets() { sprites = new HeadlessItemSprites(); }
	@AfterClass public static void restoreSheets() { sprites.close(); }

	@Test
	public void fieryTargetsTakeThreeQuartersOfRawFireTaggedDamage() {
		RecordingGnoll target = new RecordingGnoll();
		target.addProperties(com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.FIERY);
		CursedFlameDamage.apply(target, 20, this);
		assertEquals(15, target.lastDamage);
		assertTrue(DamageTag.of(target.lastTags).contains(DamageTag.FIRE));
	}

	@Test
	public void frostElementalHitUsesHalfToThreeFifthsOfMaxHealth() {
		RecordingFrost target = new RecordingFrost();
		target.HT = target.HP = 100;
		CursedFlameDamage.apply(target, 1, this);
		assertTrue(target.lastDamage >= 50 && target.lastDamage <= 60);
	}

	@Test
	public void directWandHitDoesNotUseFrostElementalsBurningOverride() {
		RecordingFrost target = new RecordingFrost();
		target.HT = target.HP = 100;
		CursedFlameDamage.applyMagical(target, 10, new WandOfCursedFlame());
		assertEquals(10, target.lastDamage);
	}

	@Test
	public void fireImbueHalvesCursedDamageWithoutBlockingBurnApplication() {
		RecordingGnoll target = new RecordingGnoll();
		assertTrue(new FireImbue().attachTo(target));
		CursedFlameDamage.apply(target, 20, this);
		assertEquals(10, target.lastDamage);
		assertNotNull(CursedBurning.apply(target, 4f));
		assertEquals(2f, target.buff(CursedBurning.class).remaining(), 0.001f);
	}

	@Test
	public void rollStaysWithinDepthAdjustedInclusiveBounds() {
		int oldDepth = com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth;
		try {
			com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth = 9;
			for (int i = 0; i < 100; i++) {
				int roll = CursedFlameDamage.roll(new Gnoll());
				assertTrue(roll >= 1 && roll <= 8);
			}
		} finally {
			com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth = oldDepth;
		}
	}

	@Test
	public void boostedBrimstoneCanReachCompleteProtection() {
		assertEquals(1f, Brimstone.cursedProtectionFactor(-1, 2f), 0.0001f);
		assertEquals(0.5f, Brimstone.cursedProtectionFactor(0, 1f), 0.0001f);
		assertEquals(0f, Brimstone.cursedProtectionFactor(0, 2f), 0.0001f);
	}

	@Test
	public void unifiedResistanceIsNotAppliedAgainBySourceClass() {
		Hero oldHero = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
		try {
			com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = TestHeroFactory.create();
			com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.subClass = HeroSubClass.NONE;
			ResistantGnoll target = new ResistantGnoll();
			CursedFlameDamage.apply(target, 20, CursedBurning.class);
			assertEquals(90, target.HP);
		} finally {
			com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero = oldHero;
		}
	}

	@Test
	public void fireElementalAndBurningFistTakeRealThreeQuarterDamage() {
		try (HeadlessDamageRun ignored = new HeadlessDamageRun()) {
			Elemental.FireElemental fire = new Elemental.FireElemental();
			fire.HT = fire.HP = 100;
			CursedFlameDamage.apply(fire, 20, CursedBurning.class);
			assertEquals(85, fire.HP);

			YogFist.BurningFist fist = new TestBurningFist();
			fist.HT = fist.HP = 100;
			CursedFlameDamage.apply(fist, 20, CursedBurning.class);
			assertEquals(85, fist.HP);
		}
	}

	@Test
	public void blazingAndAntiMagicChampionsUseRealDamagePipeline() {
		try (HeadlessDamageRun ignored = new HeadlessDamageRun()) {
			Gnoll blazing = new Gnoll();
			blazing.HT = blazing.HP = 100;
			assertNotNull(Buff.affect(blazing, ChampionEnemy.Blazing.class));
			CursedFlameDamage.apply(blazing, 20, CursedBurning.class);
			assertEquals(85, blazing.HP);

			Gnoll antiMagic = new Gnoll();
			antiMagic.HT = antiMagic.HP = 100;
			assertNotNull(Buff.affect(antiMagic, ChampionEnemy.AntiMagic.class));
			CursedFlameDamage.apply(antiMagic, 20, CursedBurning.class);
			assertEquals(90, antiMagic.HP);
		}
	}

	@Test
	public void antiMagicChampionBlocksTheWandsDirectMagicButNotCursedBurning() {
		try (HeadlessDamageRun ignored = new HeadlessDamageRun()) {
			Gnoll target = new Gnoll();
			target.HT = target.HP = 100;
			assertNotNull(Buff.affect(target, ChampionEnemy.AntiMagic.class));
			CursedFlameDamage.applyMagical(target, 20, new WandOfCursedFlame());
			assertEquals("the crafted wand is subject to the champion's wand immunity", 100, target.HP);
			CursedFlameDamage.apply(target, 20, CursedBurning.class);
			assertEquals("cursed burning retains the normal elemental damage reduction", 90, target.HP);
		}
	}

	@Test
	public void fireImbueAndFieryProtectionSelectStrongestOnly() {
		try (HeadlessDamageRun ignored = new HeadlessDamageRun()) {
			Gnoll target = new Gnoll();
			target.HT = target.HP = 100;
			target.addProperties(com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.FIERY);
			assertTrue(new FireImbue().attachTo(target));
			CursedFlameDamage.apply(target, 20, CursedBurning.class);
			assertEquals(90, target.HP);
			assertEquals(2f, CursedBurning.apply(target, 4f).remaining(), 0.001f);
		}
	}

	@Test
	public void equippedElementsRingReducesRealDamageAndBurnDuration() {
		Hero previous = Dungeon.hero;
		try {
			Hero hero = new Hero();
			Dungeon.hero = hero;
			hero.HT = hero.HP = 100;
			hero.damageInterrupt = false;
			RingOfElements ring = new RingOfElements();
			hero.belongings.ring = ring;
			ring.activate(hero);
			CursedFlameDamage.apply(hero, 20, CursedBurning.class);
			assertEquals(83, hero.HP);
			CursedBurning burn = CursedBurning.apply(hero, 4f);
			assertNotNull(burn);
			assertEquals(3.3f, burn.remaining(), 0.001f);
		} finally {
			Dungeon.hero = previous;
		}
	}

	@Test
	public void equippedBrimstoneReducesRealDamageAndBurnDuration() {
		Hero previous = Dungeon.hero;
		try {
			Hero hero = new Hero();
			Dungeon.hero = hero;
			hero.HT = hero.HP = 100;
			hero.damageInterrupt = false;
			ClothArmor armor = new ClothArmor();
			armor.glyph = new Brimstone();
			hero.belongings.armor = armor;
			CursedFlameDamage.apply(hero, 20, CursedBurning.class);
			assertEquals(90, hero.HP);
			CursedBurning burn = CursedBurning.apply(hero, 4f);
			assertNotNull(burn);
			assertEquals(2f, burn.remaining(), 0.001f);
		} finally {
			Dungeon.hero = previous;
		}
	}

	@Test
	public void ringAndTwoFireProtectionsUseOneFireFactorThenRingFactor() {
		Hero previous = Dungeon.hero;
		try {
			Hero hero = new Hero();
			Dungeon.hero = hero;
			hero.HT = hero.HP = 100;
			hero.damageInterrupt = false;
			RingOfElements ring = new RingOfElements();
			hero.belongings.ring = ring;
			ring.activate(hero);
			ClothArmor armor = new ClothArmor();
			armor.glyph = new Brimstone();
			hero.belongings.armor = armor;
			assertTrue(new FireImbue().attachTo(hero));
			CursedFlameDamage.apply(hero, 20, CursedBurning.class);
			assertEquals(92, hero.HP);
			assertEquals(1.65f, CursedBurning.apply(hero, 4f).remaining(), 0.001f);
		} finally {
			Dungeon.hero = previous;
		}
	}

	@Test
	public void boostedEquippedBrimstoneBlocksDamageAndNewBurn() {
		Hero previous = Dungeon.hero;
		try {
			Hero hero = new Hero();
			Dungeon.hero = hero;
			hero.HT = hero.HP = 100;
			hero.damageInterrupt = false;
			RingOfArcana ring = new RingOfArcana();
			ring.upgrade(4);
			hero.belongings.ring = ring;
			ring.activate(hero);
			ClothArmor armor = new ClothArmor();
			armor.glyph = new Brimstone();
			hero.belongings.armor = armor;
			CursedFlameDamage.apply(hero, 20, CursedBurning.class);
			assertEquals(100, hero.HP);
			assertNull(CursedBurning.apply(hero, 4f));
		} finally {
			Dungeon.hero = previous;
		}
	}

	@Test
	public void attachedCursedBurnHalvesWandsResistanceWithoutChangingGenericResist() {
		try (HeadlessDamageRun ignored = new HeadlessDamageRun()) {
			Gnoll target = new Gnoll();
			target.HT = target.HP = 100;
			assertEquals(1f, CorruptionDebuffRules.resistanceMultiplier(target), 0.001f);
			assertNotNull(CursedBurning.apply(target, 4f));
			assertEquals(0.5f, CorruptionDebuffRules.resistanceMultiplier(target), 0.001f);
			assertEquals(1f, target.resist(Corruption.class), 0.001f);

			Gnoll oldMajor = new Gnoll();
			assertTrue(new Frost().attachTo(oldMajor));
			assertEquals(0.5f, CorruptionDebuffRules.resistanceMultiplier(oldMajor), 0.001f);
			Gnoll oldMinor = new Gnoll();
			assertTrue(new Burning().attachTo(oldMinor));
			assertEquals(0.75f, CorruptionDebuffRules.resistanceMultiplier(oldMinor), 0.001f);
		}
	}

	private static class TestBurningFist extends YogFist.BurningFist {
		@Override protected boolean isNearYog() { return false; }
	}

	private static class HeadlessDamageRun implements AutoCloseable {
		private final Hero previous = Dungeon.hero;
		HeadlessDamageRun() {
			Dungeon.hero = TestHeroFactory.create();
			Dungeon.hero.subClass = HeroSubClass.NONE;
		}
		@Override public void close() { Dungeon.hero = previous; }
	}

	private static class RecordingGnoll extends Gnoll {
		int lastDamage;
		DamageTag[] lastTags;
		RecordingGnoll() { HT = HP = 1000; }
		@Override public float resist(Class effect) { return 1f; }
		@Override public void damage(int amount, Object source, DamageTag... tags) {
			lastDamage = amount;
			lastTags = tags;
		}
	}

	private static class RecordingFrost extends Elemental.FrostElemental {
		int lastDamage;
		@Override public float resist(Class effect) { return 1f; }
		@Override public int glyphLevel(Class<? extends Armor.Glyph> type) { return -1; }
		@Override public void damage(int amount, Object source, DamageTag... tags) { lastDamage = amount; }
	}

	private static class ResistantGnoll extends Gnoll {
		ResistantGnoll() { HT = HP = 100; }
		@Override public float resist(Class effect) {
			return effect == CursedFlameDamage.class || effect == CursedBurning.class ? 0.5f : 1f;
		}
	}

}
