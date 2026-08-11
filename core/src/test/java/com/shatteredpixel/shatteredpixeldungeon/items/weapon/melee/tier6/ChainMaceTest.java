package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ChainMaceTest {

	@Test
	public void followerIsNeutralAndInvulnerableToMonsterAttacks() {
		ChainMace.BallFollower follower = new ChainMace.BallFollower();
		Rat monster = new Rat();

		assertEquals(Char.Alignment.NEUTRAL, follower.alignment);
		assertFalse(Actor.isHostile(monster, follower));
		assertTrue(follower.isInvulnerable(monster.getClass()));
		assertFalse(follower.blocksBallistica());
	}

	@Test
	public void followerCannotReceiveAlignmentChangingOrAggressionEffects() {
		ChainMace.BallFollower follower = new ChainMace.BallFollower();

		assertTrue(follower.isImmune(Charm.class));
		assertTrue(follower.isImmune(Amok.class));
		assertTrue(follower.isImmune(AllyBuff.class));
		assertTrue(follower.isImmune(Corruption.class));
		assertTrue(follower.isImmune(ScrollOfSirensSong.Enthralled.class));
		assertTrue(follower.isImmune(StoneOfAggression.Aggression.class));

		assertFalse(new Charm().attachTo(follower));
		assertFalse(new Corruption().attachTo(follower));
		assertFalse(new ScrollOfSirensSong.Enthralled().attachTo(follower));
		assertFalse(new StoneOfAggression.Aggression().attachTo(follower));
		assertEquals(Char.Alignment.NEUTRAL, follower.alignment);
	}

	@Test
	public void followerRejectsEveryBuffLikeCrystalSpire() {
		ChainMace.BallFollower follower = new ChainMace.BallFollower();

		assertFalse(follower.add(new Buff()));
		assertFalse(new Buff().attachTo(follower));
	}

	@Test
	public void bossTransitionsPreserveDirectableAllies() throws IOException {
		String prisonBossLevel = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/PrisonBossLevel.java");
		String prisonNewBossLevel = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/PrisonNewBossLevel.java");
		String cityBossLevel = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/CityBossLevel.java");
		String greatShoper = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/GreatShoper.java");

		assertTrue(prisonBossLevel.contains("!(mob instanceof DirectableAlly)"));
		assertTrue(prisonBossLevel.contains(
				"m.alignment == Char.Alignment.ALLY || m instanceof DirectableAlly"));
		assertTrue(prisonNewBossLevel.contains("!(mob instanceof DirectableAlly)"));
		assertTrue(prisonNewBossLevel.contains(
				"m.alignment == Char.Alignment.ALLY || m instanceof DirectableAlly"));
		assertTrue(cityBossLevel.contains("!(c instanceof DirectableAlly)"));
		assertTrue(greatShoper.contains("instanceof DirectableAlly"));
	}

	@Test
	public void restoredFollowerCanBeFoundBeforeActorInitialization() {
		ChainMace.BallFollower follower = new ChainMace.BallFollower();

		assertSame(follower, ChainMace.findFollowerInLevel(
				follower.id(), Collections.singletonList(follower)));
	}

	@Test
	public void returnMovementInitializesFieldOfViewBeforePathfinding() throws IOException {
		String source = source();
		int allocation = source.indexOf(
				"fieldOfView = new boolean[Dungeon.level.length()]");
		int refresh = source.indexOf(
				"Dungeon.level.updateFieldOfView(this, fieldOfView)");
		int pathfinding = source.indexOf("if (getCloser(owner.pos))");

		assertTrue(allocation >= 0);
		assertTrue(refresh > allocation);
		assertTrue(pathfinding > refresh);
	}

	@Test
	public void launchSoundAndHitShakeAreSeparated() throws IOException {
		String source = source();
		assertTrue(source.contains("playLaunchFeedback();\n\t\tdispatchAbilityStep"));
		assertTrue(source.contains("playLaunchFeedback();\n\t\tfinal int from = ball.pos"));
		assertTrue(source.contains("private void playLaunchFeedback() {\n\t\tSample.INSTANCE.play(Assets.Sounds.CHAINS)"));
		assertTrue(source.contains("private void playHitFeedback() {\n\t\tPixelScene.shake(2, 0.33f)"));
		assertTrue(source.contains("if (hit) {\n\t\t\t\t\tSample.INSTANCE.play(Assets.Sounds.HIT_CRUSH, 1f, 0.9f);\n\t\t\t\t\tplayHitFeedback();"));
		assertTrue(source.contains("if (hero.attack(target, 1f, 0f, Char.INFINITE_ACCURACY)) {\n\t\t\t\t\t\tSample.INSTANCE.play(Assets.Sounds.HIT_CRUSH, 1f, 0.9f);\n\t\t\t\t\t\tplayHitFeedback();"));
		assertTrue(source.contains("public int proc(Char attacker, Char defender, int damage)"));
		assertTrue(source.contains("Dungeon.hero.belongings.thrownWeapon != this"));
		assertTrue(source.contains("Dungeon.hero.belongings.abilityWeapon != this"));
		assertTrue(source.contains("private void playMeleeHitFeedback() {\n\t\tPixelScene.shake(2, 0.25f)"));
	}

	@Test
	public void usesTierSixGreataxeDamageAndStrengthCurve() {
		ChainMace weapon = new ChainMace();

		assertEquals(6, weapon.min(0));
		assertEquals(50, weapon.max(0));
		assertEquals(9, weapon.min(3));
		assertEquals(71, weapon.max(3));
		assertEquals(22, weapon.STRReq(0));
		assertEquals(21, weapon.STRReq(1));
		assertEquals(1, weapon.reachFactor(null));
		assertEquals(ChainMace.AC_THROW, weapon.defaultAction());
	}

	@Test
	public void followerAndSpriteAreNestedInTheWeaponClass() throws IOException {
		assertTrue(DirectableAlly.class.isAssignableFrom(ChainMace.BallFollower.class));
		assertTrue(MobSprite.class.isAssignableFrom(ChainMace.BallFollowerSprite.class));

		String source = source();
		assertTrue(source.contains("static class BallFollower extends DirectableAlly"));
		assertTrue(source.contains("static class BallFollowerSprite extends MobSprite"));
		assertFalse(source.contains("actors.mobs.ChainMaceFollower"));
		assertFalse(source.contains("sprites.ChainMaceFollowerSprite"));
	}

	@Test
	public void ballFollowerHasLocalizedNameAndDescription() throws IOException {
		String english = itemMessages("items.properties");
		String chinese = itemMessages("items_zh.properties");

		assertTrue(english.contains(
				"items.weapon.melee.tier6.chainmace$ballfollower.name=chain mace iron ball"));
		assertTrue(english.contains(
				"items.weapon.melee.tier6.chainmace$ballfollower.desc=This solid iron ball is bound to its wielder by a heavy chain. It follows close by and rushes at targets when commanded. Enemies cannot target or harm it."));
		assertTrue(chinese.contains(
				"items.weapon.melee.tier6.chainmace$ballfollower.name=链锤铁球"));
		assertTrue(chinese.contains(
				"items.weapon.melee.tier6.chainmace$ballfollower.desc=这颗实心铁球由沉重的锁链牵引，始终紧跟链球的持有者。它会在命令下冲向敌人并发动撞击；敌人无法锁定或伤害它。"));
	}

	@Test
	public void returnStateLocksCommandsUntilFollowerReachesHero() {
		ChainMace.CommandState state = new ChainMace.CommandState();

		assertTrue(state.canDispatch());
		state.beginSingleAttack();
		assertFalse(state.canDispatch());
		state.beginReturn();
		assertTrue(state.isReturning());
		assertFalse(state.canDispatch());
		state.finishReturn();
		assertFalse(state.isReturning());
		assertTrue(state.canDispatch());
	}

	@Test
	public void interruptedAnimationsRestoreAsSafeReturn() {
		ChainMace.CommandState original = new ChainMace.CommandState();
		original.beginAbility();

		Bundle bundle = new Bundle();
		original.storeInBundle(bundle);
		ChainMace.CommandState restored = new ChainMace.CommandState();
		restored.restoreFromBundle(bundle);

		assertTrue(restored.isReturning());
		assertFalse(restored.canDispatch());
		restored.finishReturn();
		assertTrue(restored.canDispatch());
	}

	@Test
	public void nearestTargetOrderUsesActorIdAsStableTieBreaker() {
		int[] ids = {30, 10, 20, 40};
		int[] cells = {11, 1, 10, 33};

		assertArrayEquals(new int[]{10, 20, 30, 40},
				ChainMace.stableNearestTargetOrder(0, 10, ids, cells));
	}

	@Test
	public void commandTargetRequiresVisibilityEnemyAndTrajectoryUnlessProjecting() {
		assertTrue(ChainMace.commandTargetAllowed(true, true, false, 42, 42));
		assertFalse(ChainMace.commandTargetAllowed(true, true, false, 42, 41));
		assertTrue(ChainMace.commandTargetAllowed(true, true, true, 42, 41));
		assertFalse(ChainMace.commandTargetAllowed(false, true, true, 42, 42));
		assertFalse(ChainMace.commandTargetAllowed(true, false, true, 42, 42));
	}

	@Test
	public void sourceQualifiesCommandsFromHeroAndReconcilesFloorChanges()
			throws IOException {
		String source = source();

		assertTrue(source.contains(
				"new Ballistica(hero.pos, target.pos, Ballistica.PROJECTILE)"));
		assertTrue(source.contains("hasEnchant(Projecting.class, hero)"));
		assertTrue(source.contains("private transient Level followerLevel"));
		assertTrue(source.contains("reconcileFollowerLevel"));
		assertTrue(source.contains("previousLevel.mobs.remove(ball)"));
		assertTrue(source.contains("activeCommandToken = 0"));
		assertTrue(source.contains("hero.belongings.abilityWeapon = null"));
		assertTrue(source.contains("if (isCommandTarget(hero, ch)) enemies.add(ch)"));
		assertTrue(source.contains("isVisibleEnemy(hero, target)"));

		int floorMismatchCheck = source.indexOf(
				"if (followerLevel != null && followerLevel != Dungeon.level)");
		int currentLevelMembershipCheck = source.indexOf(
				"Dungeon.level.mobs.contains(existing)");
		assertTrue(floorMismatchCheck >= 0);
		assertTrue(floorMismatchCheck < currentLevelMembershipCheck);
		assertTrue(source.contains("discardStaleFollower(existing, hero)"));
		assertFalse(source.contains("currentLevel.mobs.add(ball)"));
		assertTrue(source.contains("landFollower(ball, safeReturnCell(hero, ball))"));
	}

	@Test
	public void sourceInterceptsEquippedThrowAndUsesNormalVersusCertainHits()
			throws IOException {
		String source = source();

		assertTrue(source.contains("public void cast(Hero user, int dst)"));
		assertTrue(source.contains("if (!isEquipped(user))"));
		assertTrue(source.contains("super.cast(user, dst)"));
		assertTrue(source.contains("hero.attack(target)"));
		assertTrue(source.contains("Char.INFINITE_ACCURACY"));
		assertTrue(source.contains("hero.belongings.thrownWeapon = ChainMace.this"));
		assertTrue(source.contains("finally"));
		assertFalse(source.contains("hero.belongings.abilityWeapon = ChainMace.this"));
	}

	@Test
	public void sourcePersistsRealReturnAndSynchronizesFollowerPosition()
			throws IOException {
		String source = source();

		assertTrue(source.contains("landFollower(ball, target.pos)"));
		assertTrue(source.contains("ball.returning = true"));
		assertTrue(source.contains("Dungeon.level.occupyCell(ball)"));
		assertTrue(source.contains("onFollowerReturned"));
		assertTrue(source.contains("hero.spendAndNext(Actor.TICK)"));
	}

	@Test
	public void sourceHandlesCrowdedSpawnAndValidatesWeaponOwnership()
			throws IOException {
		String source = source();

		assertTrue(source.contains("int spawnCell = hero.pos"));
		assertTrue(source.contains("linkedWeapon()"));
		assertTrue(source.contains("isCommandValid"));
		assertTrue(source.contains("abortCommand"));
		assertTrue(source.contains("canUseWeaponAbility(Dungeon.hero)"));
		assertTrue(source.contains("return AC_ABILITY"));
		assertTrue(source.contains("return AC_THROW"));
	}

	private static String source() throws IOException {
		return sourceFile("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/"
				+ "tier6/ChainMace.java");
	}

	private static String sourceFile(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)),
				StandardCharsets.UTF_8);
	}

	private static String itemMessages(String languageFile) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path messagePath = coreDirectory.resolve(
				"src/main/assets/messages/items/" + languageFile);
		return new String(Files.readAllBytes(messagePath), StandardCharsets.UTF_8);
	}
}
