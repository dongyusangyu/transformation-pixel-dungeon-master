package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bandit;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SoulRoastMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Bundle;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import static org.junit.Assert.*;

public class CursedBurningTest {
	private static HeadlessItemSprites sprites;
	@BeforeClass public static void installSheets() { sprites = new HeadlessItemSprites(); }
	@AfterClass public static void restoreSheets() { sprites.close(); }

	@Test
	public void fourTurnBurnHitsImmediatelyThenTwiceOnceTwice() {
		RecordingGnoll target = new RecordingGnoll();
		CursedBurning burn = CursedBurning.apply(target, 4f);
		assertNotNull(burn);
		assertEquals(1, target.hits);
		assertEquals("application should show the requested four-turn duration", 4f,
				burn.remaining(), 0.001f);
		burn.act();
		assertEquals(3, target.hits);
		assertEquals("one completed turn should consume exactly one round", 3f,
				burn.remaining(), 0.001f);
		burn.act();
		assertEquals(4, target.hits);
		burn.act();
		assertEquals(6, target.hits);
		assertEquals(1f, burn.remaining(), 0.001f);
		burn.act();
		assertEquals(7, target.hits);
		assertNull("the final damaging tick ends the burn", target.buff(CursedBurning.class));
		burn.act();
		assertEquals(7, target.hits);
		assertNull(target.buff(CursedBurning.class));
	}

	@Test
	public void reapplicationRefreshesSameInstanceAndLongerBurnContinuesAlternating() {
		RecordingGnoll target = new RecordingGnoll();
		CursedBurning burn = CursedBurning.apply(target, 5f);
		burn.act();
		assertSame(burn, CursedBurning.apply(target, 2f));
		assertEquals(1, target.buffs(CursedBurning.class).size());
		assertEquals(4f, burn.remaining(), 0.001f);
		burn.act();
		burn.act();
		burn.act();
		assertEquals(8, target.hits);
	}

	@Test
	public void saveRestoresDurationAndNextTickParity() {
		RecordingGnoll target = new RecordingGnoll();
		CursedBurning burn = CursedBurning.apply(target, 4f);
		burn.act();
		Bundle saved = new Bundle();
		burn.storeInBundle(saved);
		burn.detach();
		CursedBurning restored = new CursedBurning();
		restored.restoreFromBundle(saved);
		assertTrue(restored.attachTo(target));
		assertEquals(3f, restored.remaining(), 0.001f);
		restored.act();
		assertEquals(4, target.hits);
	}

	@Test
	public void cleansingBlocksNewBurnButOrdinaryDetachRemovesIt() {
		RecordingGnoll target = new RecordingGnoll();
		assertNotNull(Buff.affect(target, PotionOfCleansing.Cleanse.class));
		assertNull(CursedBurning.apply(target, 4f));
		assertEquals(0, target.hits);
		Buff.detach(target, PotionOfCleansing.Cleanse.class);
		CursedBurning burn = CursedBurning.apply(target, 4f);
		assertNotNull(burn);
		burn.detach();
		assertNull(target.buff(CursedBurning.class));
	}

	@Test
	public void sixApplicationsReallyReduceMobHp() {
		Hero previous = Dungeon.hero;
		try {
			Dungeon.hero = TestHeroFactory.create();
			Dungeon.hero.subClass = HeroSubClass.NONE;
			DamagedGnoll target = new DamagedGnoll();
			CursedBurning burn = CursedBurning.apply(target, 4f);
			for (int i = 0; i < 4; i++) burn.act();
			assertEquals(7, target.hits);
			assertTrue(target.HP < 1000);
			assertTrue(target.HP >= 970);
		} finally {
			Dungeon.hero = previous;
		}
	}

	@Test
	public void firstApplicationConvertsOneHeroMeatAndLaterAttemptsConvertAgain() {
		Hero previous = Dungeon.hero;
		try {
			Hero hero = TestHeroFactory.create();
			hero.subClass = HeroSubClass.NONE;
			hero.HT = hero.HP = 1000;
			Dungeon.hero = hero;
			MysteryMeat meat = new MysteryMeat();
			meat.quantity(3);
			hero.belongings.backpack.items.add(meat);
			CursedBurning burn = CursedBurning.apply(hero, 5f);
			assertNotNull(burn);
			assertEquals(2, meat.quantity());
			assertEquals(1, count(hero, SoulRoastMeat.class));
			for (int i = 0; i < 3; i++) burn.act();
			assertEquals(3, meat.quantity() + count(hero, SoulRoastMeat.class));
			assertTrue("second conversion must happen within 3 turns", count(hero, SoulRoastMeat.class) >= 2);
		} finally { Dungeon.hero = previous; }
	}

	@Test
	public void frozenMeatInHeroInventoryBecomesSoulRoastMeat() {
		Hero previous = Dungeon.hero;
		try {
			Hero hero = TestHeroFactory.create();
			hero.subClass = HeroSubClass.NONE;
			hero.HT = hero.HP = 1000;
			Dungeon.hero = hero;
			FrozenCarpaccio frozen = (FrozenCarpaccio) FrozenCarpaccio.cook(new MysteryMeat());
			frozen.quantity(2);
			hero.belongings.backpack.items.add(frozen);

			assertNotNull(CursedBurning.apply(hero, 4f));

			assertEquals(1, frozen.quantity());
			assertEquals(1, count(hero, SoulRoastMeat.class));
		} finally { Dungeon.hero = previous; }
	}

	@Test
	public void firstApplicationConvertsBanditStolenMeat() {
		Bandit target = new Bandit() {
			@Override public void damage(int amount, Object source, DamageTag... tags) {}
		};
		target.HT = target.HP = 1000;
		MysteryMeat stolen = new MysteryMeat();
		stolen.quantity(1);
		((Thief) target).item = stolen;
		assertNotNull(CursedBurning.apply(target, 4f));
		assertTrue(((Thief) target).item instanceof SoulRoastMeat);
		assertEquals(1, ((Thief) target).item.quantity());
	}

	@Test
	public void stolenMeatStackIsPreservedWhenNoLevelCanReceiveConvertedPiece() {
		Thief target = new Thief() {
			@Override public void damage(int amount, Object source, DamageTag... tags) {}
		};
		target.HT = target.HP = 1000;
		MysteryMeat stolen = new MysteryMeat();
		stolen.quantity(3);
		target.item = stolen;
		com.shatteredpixel.shatteredpixeldungeon.levels.Level previous = Dungeon.level;
		try {
			Dungeon.level = null;
			CursedBurning.apply(target, 4f);
			assertSame(stolen, target.item);
			assertEquals(3, stolen.quantity());
		} finally { Dungeon.level = previous; }
	}

	@Test
	public void frozenMeatStolenByThiefBecomesSoulRoastMeat() {
		Thief target = new Thief() {
			@Override public void damage(int amount, Object source, DamageTag... tags) {}
		};
		target.HT = target.HP = 1000;
		FrozenCarpaccio stolen = (FrozenCarpaccio) FrozenCarpaccio.cook(new MysteryMeat());
		stolen.quantity(1);
		target.item = stolen;

		assertNotNull(CursedBurning.apply(target, 4f));

		assertTrue(target.item instanceof SoulRoastMeat);
		assertEquals(1, target.item.quantity());
	}

	@Test
	public void cursedBurningFxUsesOwnSpriteStateAndCanRemoveIt() {
		RecordingGnoll target = new RecordingGnoll();
		CursedBurning burn = CursedBurning.apply(target, 4f);
		CharSprite sprite = new CharSprite();
		target.sprite = sprite;
		burn.fx(true);
		CharSprite.State state;
		try { state = CharSprite.State.valueOf("CURSED_BURNING"); }
		catch (IllegalArgumentException e) { fail("Missing distinct cursed-burning sprite state"); return; }
		assertTrue(sprite.isState(state));
		burn.fx(false);
		assertFalse(sprite.isState(state));
	}

	@Test
	public void firstApplicationBurnsOneScrollButIgnoresUniqueAndLostInventory() {
		Hero previous = Dungeon.hero;
		try {
			Hero hero = TestHeroFactory.create();
			hero.subClass = HeroSubClass.NONE;
			hero.HT = hero.HP = 1000;
			Dungeon.hero = hero;
			ScrollOfIdentify scrolls = new ScrollOfIdentify();
			scrolls.quantity(3);
			hero.belongings.backpack.items.add(scrolls);
			CursedBurning.apply(hero, 4f);
			assertEquals(2, scrolls.quantity());
			Buff.detach(hero, CursedBurning.class);
			scrolls.unique = true;
			CursedBurning.apply(hero, 4f);
			assertEquals(2, scrolls.quantity());
			Buff.detach(hero, CursedBurning.class);
			scrolls.unique = false;
			hero.belongings.lostInventory(true);
			CursedBurning.apply(hero, 4f);
			assertEquals(2, scrolls.quantity());
		} finally { Dungeon.hero = previous; }
	}

	@Test
	public void stolenScrollStackLosesOnlyOneOnFirstApplication() {
		Thief target = new Thief() {
			@Override public void damage(int amount, Object source, DamageTag... tags) {}
		};
		target.HT = target.HP = 1000;
		ScrollOfIdentify stolen = new ScrollOfIdentify();
		stolen.quantity(3);
		target.item = stolen;
		CursedBurning.apply(target, 4f);
		assertSame(stolen, target.item);
		assertEquals(2, target.item.quantity());
	}

	private static int count(Hero hero, Class<? extends Item> type) {
		int total = 0;
		for (Item item : hero.belongings.backpack.items) {
			if (type.isInstance(item)) total += item.quantity();
		}
		return total;
	}

	private static class RecordingGnoll extends Gnoll {
		int hits;
		RecordingGnoll() { HT = HP = 1000; }
		@Override public float resist(Class effect) { return 1f; }
		@Override public void damage(int amount, Object source, DamageTag... tags) { hits++; }
	}
	private static class DamagedGnoll extends Gnoll {
		int hits;
		DamagedGnoll() { HT = HP = 1000; }
		@Override public void damage(int amount, Object source, DamageTag... tags) {
			hits++;
			super.damage(amount, source, tags);
		}
	}
}
