package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RitualDaggerBloodGiftTest {

	private Hero previousHero;
	private Game previousGame;
	private Hero hero;
	private Reason reason;
	private Rat target;

	@Before
	public void setUp() {
		previousHero = Dungeon.hero;
		previousGame = Game.instance;
		new Game(Scene.class, null);
		hero = TestHeroFactory.create();
		hero.subClass = HeroSubClass.PIOUS;
		Dungeon.hero = hero;
		reason = Buff.affect(hero, Reason.class);
		reason.reason = 0;
		target = new TestRat();
		target.HT = target.HP = 1000;
		Buff.affect(target, RitualDagger.BloodGift.class);
	}

	@After
	public void tearDown() {
		Dungeon.hero = previousHero;
		Game.instance = previousGame;
	}

	@Test
	public void bossRecoveryIsCappedButNotDisabled() {
		target.addProperties(Char.Property.BOSS);
		target.damage(100, hero, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(5, reason.reason);
	}

	@Test
	public void ordinaryEnemyKeepsOriginalRecovery() {
		RitualDagger.BloodGift.restoreReason(hero, target, 100);
		assertEquals(20, reason.reason);
	}

	@Test
	public void minibossRecoveryHasTheSameCap() {
		target.addProperties(Char.Property.MINIBOSS);
		RitualDagger.BloodGift.restoreReason(hero, target, 100);
		assertEquals(5, reason.reason);
	}

	@Test
	public void anotherCreatureDamagingTheMarkDoesNotRestoreReason() {
		target.damage(20, new Rat(), DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(0, reason.reason);
	}

	@Test
	public void magicalDamageDoesNotRestoreReason() {
		target.damage(20, hero, DamageTag.MAGICAL);
		assertEquals(0, reason.reason);
	}

	@Test
	public void zeroDamageDoesNotRestoreReason() {
		target.damage(0, hero, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(0, reason.reason);
	}

	@Test
	public void shieldOnlyDamageDoesNotRestoreReason() {
		Buff.affect(target, Barrier.class).incShield(30);
		target.damage(20, hero, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(1000, target.HP);
		assertEquals(0, reason.reason);
	}

	@Test
	public void rangedPhysicalDamageAlsoRestoresReason() {
		target.damage(20, hero, DamageTag.PHYSICAL, DamageTag.RANGED);
		assertEquals(4, reason.reason);
	}

	@Test
	public void directPhysicalDamageRestoresFromActualHpLoss() {
		target.damage(20, hero, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(4, reason.reason);
	}

	@Test
	public void overkillOnlyCountsRemainingHp() {
		target.HP = 3;
		target.damage(100, hero, DamageTag.PHYSICAL, DamageTag.MELEE);
		assertEquals(0, target.HP);
		assertEquals(1, reason.reason);
	}

	private static class TestRat extends Rat {
		@Override public float resist(Class effect) { return 1f; }
		@Override public boolean isImmune(Class effect) { return false; }
		@Override public void die(Object cause) {}
	}
}
