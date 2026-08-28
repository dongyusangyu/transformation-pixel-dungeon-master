package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MagicalRangedAttack;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDeepSleep;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

import org.junit.Test;

import java.util.List;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TapirCrocodileTest {

	@Test
	public void inheritsMimicCrocodileStatsAndUsesMagicalRangedAttacks() {
		TapirCrocodile tapir = new TapirCrocodile();
		MimicCrocodile base = new MimicCrocodile();

		assertTrue(tapir instanceof MimicCrocodile);
		assertTrue(tapir instanceof MagicalRangedAttack);
		assertEquals(base.HT, tapir.HT);
		assertEquals(base.defenseSkill, tapir.defenseSkill);
		assertEquals(base.EXP, tapir.EXP);
		assertEquals(30, tapir.maxLvl);
		assertSame(tapir.WANDERING, tapir.state);
	}

	@Test
	public void bothAmbushAndNormalZapUseWardingMissile() {
		TestTapir tapir = new TestTapir();

		assertEquals(MagicMissile.WARD, tapir.missileTypeForTest(true));
		assertEquals(MagicMissile.WARD, tapir.missileTypeForTest(false));
	}

	@Test
	public void revealedFirstAttackRemainsMarkedAsMentalAmbush() {
		TestTapir tapir = new TestTapir();

		tapir.beginAmbushForTest();

		assertFalse(tapir.isLurking());
		assertTrue(tapir.mentalAmbushPendingForTest());
	}

	@Test
	public void ambushDealsSixtyBeforeApplyingMagicalSleep() {
		TestTapir tapir = new TestTapir();
		OrderTarget target = new OrderTarget();

		tapir.resolveZapForTest(target, true, true);

		assertEquals(40, target.HP);
		assertFalse(target.sawSleepDuringDamage);
		assertNotNull(target.buff(MagicalSleep.class));
		assertSame(target.SLEEPING, target.state);
	}

	@Test
	public void lethalAmbushDoesNotAttachSleep() {
		TestTapir tapir = new TestTapir();
		OrderTarget target = new OrderTarget();
		target.HP = 50;

		tapir.resolveZapForTest(target, true, true);

		assertFalse(target.isAlive());
		assertTrue(target.buff(MagicalSleep.class) == null);
	}

	@Test
	public void ordinaryZapDealsTwentyToThirtyAndUsesFiftyPercentSleepRoll() {
		TestTapir tapir = new TestTapir();
		assertEquals(20, tapir.normalZapDamageForTest(0f));
		assertEquals(30, tapir.normalZapDamageForTest(0.999999f));
		assertTrue(tapir.normalZapSleepsForTest(0.499999f));
		assertFalse(tapir.normalZapSleepsForTest(0.5f));
	}

	@Test
	public void sleepingTargetsCannotBeZappedButAreMeleedWhenAdjacent() throws Exception {
		Level previous = Dungeon.level;
		try {
			Dungeon.level = openLevel(11, 7);
			TestTapir tapir = new TestTapir();
			TestTarget target = new TestTarget();
			tapir.pos = 12;
			target.pos = 14;

			assertFalse(tapir.sleepingForTest(target));
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(target, Sleep.class);
			assertTrue(tapir.sleepingForTest(target));
			assertFalse(tapir.canAttackForTest(target));

			target.pos = 13;
			assertTrue(tapir.canAttackForTest(target));
			tapir.revealForTest();
			assertFalse(tapir.isLurking());
			tapir.noValidTargetForTest();
			assertFalse(tapir.isLurking());

			String source = new String(Files.readAllBytes(sourcePath()), StandardCharsets.UTF_8);
			assertTrue(source.contains("return super.doAttack(target);"));
			assertFalse(source.contains("if (isSleepingTarget(target)) {\n\t\t\tspend(TICK);"));
			assertFalse(tapir.isLurking());
		} finally {
			Dungeon.level = previous;
		}
	}

	private static Path sourcePath() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/tmobs/TapirCrocodile.java");
	}

	@Test
	public void targetThatFallsAsleepBeforeMissileImpactTakesNoDamage() {
		TestTapir tapir = new TestTapir();
		TestTarget target = new TestTarget();
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
				target, MagicalSleep.class);

		tapir.resolveZapForTest(target, false, true);

		assertEquals(100, target.HP);
	}

	@Test
	public void adjacentMeleeRetainsMimicBleeding() {
		TestTapir tapir = new TestTapir();
		Gnoll target = new TestTarget();

		tapir.resolveMeleeForTest(target, 20);

		assertNotNull(target.buff(Bleeding.class));
		assertEquals(10f, target.buff(Bleeding.class).level(), 0f);
	}

	@Test
	public void lootOutcomesPreserveOneHalfAndOneTwelfthMarginals() {
		TestTapir tapir = new TestTapir();

		assertEquals(13f / 24f, tapir.lootChanceForTest(), 0.000001f);
		assertLoot(tapir.lootForTest(0f), StoneOfDeepSleep.class);
		assertLoot(tapir.lootForTest(11f / 13f), ShockingBrew.class);
		List<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.Item>> both =
				tapir.lootForTest(12f / 13f);
		assertEquals(2, both.size());
		assertSame(StoneOfDeepSleep.class, both.get(0));
		assertSame(ShockingBrew.class, both.get(1));
	}

	@Test
	public void createLootHasNoGroundDropSideEffectForThieveryCallers() {
		SideEffectTapir tapir = new SideEffectTapir();

		tapir.createLoot();

		assertEquals(0, tapir.additionalGroundDrops);
	}

	@Test
	public void primaryDeathDropHookReturnsBothIndependentDrops() {
		SideEffectTapir tapir = new SideEffectTapir();

		assertEquals(2, tapir.deathDropsForTest().size());
	}

	private static void assertLoot(
			List<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.Item>> loot,
			Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.Item> type) {
		assertEquals(1, loot.size());
		assertSame(type, loot.get(0));
	}

	private static final class TestTapir extends TapirCrocodile {
		private int missileTypeForTest(boolean ambush) {
			return missileType(ambush);
		}

		private void beginAmbushForTest() {
			beginAmbushAttack();
		}

		private boolean mentalAmbushPendingForTest() {
			return isAmbushAttackPending();
		}

		private void resolveZapForTest(Char target, boolean ambush, boolean sleepRoll) {
			resolveZapHit(target, ambush, sleepRoll);
		}

		private int normalZapDamageForTest(float roll) {
			return normalZapDamage(roll);
		}

		private boolean normalZapSleepsForTest(float roll) {
			return normalZapSleeps(roll);
		}

		private boolean sleepingForTest(Char target) {
			return isSleepingTarget(target);
		}

		private boolean canAttackForTest(Char target) {
			return canAttack(target);
		}

		private void revealForTest() {
			beginAmbushAttack();
			finishAmbushAttack();
		}

		private void noValidTargetForTest() {
			onNoValidTarget();
		}

		private boolean doAttackForTest(Char target) {
			return doAttack(target);
		}

		private void resolveMeleeForTest(Char target, int damage) {
			onAttackResolved(target, true, damage, DamageTag.PHYSICAL, DamageTag.MELEE);
		}

		private float lootChanceForTest() {
			return baseCombinedLootChance();
		}

		private List<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.Item>>
		lootForTest(float roll) {
			return lootClassesForRoll(roll);
		}
	}

	private static class TestTarget extends Gnoll {
		{
			HP = HT = 100;
			state = WANDERING;
		}

		@Override
		public float resist(Class effect) {
			return 1f;
		}

		@Override
		public void damage(int dmg, Object src, DamageTag... damageTags) {
			HP = Math.max(0, HP - dmg);
		}
	}

	private static final class OrderTarget extends TestTarget {
		private boolean sawSleepDuringDamage;

		@Override
		public void damage(int dmg, Object src, DamageTag... damageTags) {
			sawSleepDuringDamage = buff(MagicalSleep.class) != null;
			HP = Math.max(0, HP - dmg);
		}
	}

	private static final class SideEffectTapir extends TapirCrocodile {
		private int additionalGroundDrops;

		@Override
		protected float lootOutcomeRoll() {
			return 1f;
		}

		@Override
		protected List<com.shatteredpixel.shatteredpixeldungeon.items.Item>
		createLootOutcome(float roll) {
			return java.util.Arrays.asList(
					new com.shatteredpixel.shatteredpixeldungeon.items.Item(),
					new com.shatteredpixel.shatteredpixeldungeon.items.Item());
		}

		@Override
		protected void dropAdditionalLoot(
				com.shatteredpixel.shatteredpixeldungeon.items.Item item) {
			additionalGroundDrops++;
		}

		private List<com.shatteredpixel.shatteredpixeldungeon.items.Item>
		deathDropsForTest() {
			return createLootDrops();
		}
	}

	private static Level openLevel(int width, int height) {
		TestLevel level = new TestLevel();
		level.setSize(width, height);
		Arrays.fill(level.passable, true);
		Arrays.fill(level.solid, false);
		return level;
	}

	private static final class TestLevel extends Level {
		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}
}
