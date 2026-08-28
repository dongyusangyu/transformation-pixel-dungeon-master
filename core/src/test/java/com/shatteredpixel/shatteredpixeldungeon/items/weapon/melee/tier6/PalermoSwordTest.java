package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.watabou.utils.Callback;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PalermoSwordTest {

	@Test
	public void tierSixStatsUseTheSpecifiedCurves() {
		assertEquals(6, PalermoSword.TIER);
		assertEquals(6, PalermoSword.minForLevel(0));
		assertEquals(18, PalermoSword.maxForLevel(0));
		assertEquals(8, PalermoSword.minForLevel(1));
		assertEquals(22, PalermoSword.maxForLevel(1));
		assertEquals(20, PalermoSword.strengthRequirementForLevel(0));
		assertEquals(1.2f, PalermoSword.ACCURACY, 0f);
		assertEquals(0.5f, PalermoSword.DELAY, 0f);
		assertEquals(1, PalermoSword.RANGE);
	}

	@Test
	public void fourthSuccessfulHitRefreshesBothOneShotEffects() {
		PalermoSword.HitState state = new PalermoSword.HitState();
		assertFalse(state.recordEnemyHit());
		assertFalse(state.recordEnemyHit());
		assertFalse(state.recordEnemyHit());
		assertTrue(state.recordEnemyHit());
		assertEquals(0, state.hitsSinceReward());
	}

	@Test
	public void breathingUsesOnePointTwoTimesCurrentMaximumDamage() {
		assertEquals(22, PalermoSword.breathDamageForMaximum(18));
		assertEquals(26, PalermoSword.breathDamageForMaximum(21));
	}

	@Test
	public void breathingKeepsStrengthAndLaterDamageBonusesOnTopOfItsFixedWeaponDamage() {
		assertEquals(27, PalermoSword.breathDamageWithStrength(18, 5));
		assertEquals(26, PalermoSword.breathDamageWithStrength(21, 0));
	}

	@Test
	public void futureAccelerationConsumesOnlyTheNextAttackDelay() {
		assertEquals(0f, PalermoSword.delayAfterFutureAcceleration(0.5f, true), 0f);
		assertEquals(0.5f, PalermoSword.delayAfterFutureAcceleration(0.5f, false), 0f);

		PalermoSword.FutureAcceleration future = new PalermoSword.FutureAcceleration();
		assertFalse(future.appliesToThisAttack());
		future.armForNextAttack();
		assertTrue(future.appliesToThisAttack());
		future.refreshForNextAttack();
		assertFalse(future.appliesToThisAttack());
	}

	@Test
	public void xiexiangExtendsTheCurrentAttackReachWithoutExtendingItsLunge() {
		assertEquals(2, PalermoSword.xiexiangTargetRange(1));
		assertEquals(3, PalermoSword.xiexiangTargetRange(2));
		assertEquals(5, PalermoSword.xiexiangTargetRange(4));
		assertEquals(1, PalermoSword.xiexiangMovementRange());

		assertTrue(PalermoSword.xiexiangTargetAllowed(true, 1, 1));
		assertTrue(PalermoSword.xiexiangTargetAllowed(true, 2, 1));
		assertFalse(PalermoSword.xiexiangTargetAllowed(true, 3, 1));
		assertTrue(PalermoSword.xiexiangTargetAllowed(true, 3, 2));
		assertFalse(PalermoSword.xiexiangTargetAllowed(false, 1, 4));
		assertFalse(PalermoSword.xiexiangTargetAllowed(true, 6, 4));

		assertEquals(4, PalermoSword.maxXiexiangStrikes());
		assertEquals(2, PalermoSword.xiexiangChargeCost());
		assertEquals(4, PalermoSword.xiexiangDamageBoost(0));
		assertEquals(7, PalermoSword.xiexiangDamageBoost(3));
	}

	@Test
	public void xiexiangArmsOnlyTheFutureAccelerationPresentAtItsFirstStrike() {
		PalermoSword.FutureAcceleration future = new PalermoSword.FutureAcceleration();
		assertFalse(future.appliesToThisAttack());
		assertTrue(PalermoSword.armFutureAccelerationForAbility(future));
		assertTrue(future.appliesToThisAttack());
		assertFalse(PalermoSword.armFutureAccelerationForAbility(null));
	}

	@Test
	public void repeatedXiexiangStrikesMayAttackInPlaceWhenKnockbackFails() {
		assertTrue(PalermoSword.xiexiangMayStrikeAtStep(0, false));
		assertTrue(PalermoSword.xiexiangMayStrikeAtStep(1, true));
		assertTrue(PalermoSword.xiexiangMayStrikeAtStep(1, false));
	}

	@Test
	public void xiexiangStopsAsSoonAsTheHeroCanNoLongerAct() {
		assertTrue(PalermoSword.xiexiangCanContinue(true, 0, false));
		assertFalse(PalermoSword.xiexiangCanContinue(false, 0, false));
		assertFalse(PalermoSword.xiexiangCanContinue(true, 1, false));
		assertFalse(PalermoSword.xiexiangCanContinue(true, 2, false));
		assertFalse(PalermoSword.xiexiangCanContinue(true, 0, true));
	}

	@Test
	public void immediatePushingCompletesExactlyOnceWhenNoSpriteCanAnimate() {
		final int[] callbacks = {0};
		Pushing pushing = new Pushing(new Char() {}, 0, 1, new Callback() {
			@Override
			public void call() {
				callbacks[0]++;
			}
		});

		pushing.startImmediately();
		pushing.startImmediately();
		assertEquals(1, callbacks[0]);
	}

	@Test
	public void xiexiangUsesRenderDrivenKnockbackWhileTheHeroIsBusy() throws Exception {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/PalermoSword.java");
		String source = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);

		assertTrue(source.contains("knockBack(target, hero.pos, new Callback()"));
		assertTrue(source.contains(
				"WandOfBlastWave.throwCharImmediately(target, trajectory, 1, true, false, this, callback);"));
		assertFalse(source.contains(
				"WandOfBlastWave.throwChar(target, trajectory, 1, true, false, this, callback);"));
	}

	@Test
	public void xiexiangCanReplaceTheInitialTargetAfterTheFirstStrike() throws Exception {
		String source = new String(Files.readAllBytes(sourcePath()), StandardCharsets.UTF_8);

		assertTrue(source.contains("findXiexiangTarget(hero, target)"));
		assertTrue(source.contains("strikeIndex + 1 < maxXiexiangStrikes()"));
	}

	private static Path sourcePath() {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return coreDirectory.resolve(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/tier6/PalermoSword.java");
	}
}
