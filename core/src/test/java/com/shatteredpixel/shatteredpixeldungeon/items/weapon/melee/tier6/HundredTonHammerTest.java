package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HundredTonHammerTest {

	@Test
	public void tierSixPreciseStatsUseReducedGrowth() {
		TestableHundredTonHammer weapon = new TestableHundredTonHammer();

		assertEquals(6, HundredTonHammer.TIER);
		assertEquals(20, HundredTonHammer.strengthRequirementForLevel(0));
		assertEquals(6, HundredTonHammer.minForLevel(0));
		assertEquals(21, HundredTonHammer.maxForLevel(0));
		assertEquals(13, HundredTonHammer.minForLevel(7));
		assertEquals(70, HundredTonHammer.maxForLevel(7));
		assertEquals(1.18f, HundredTonHammer.ACCURACY, 0f);
		assertEquals(1, HundredTonHammer.RANGE);
		assertEquals(1f, HundredTonHammer.DELAY, 0f);
		assertEquals(EXItemSpriteSheet.HUNDRED_TON_HAMMER, weapon.image);
		assertEquals(6, weapon.min(0));
		assertEquals(21, weapon.max(0));
		assertEquals(13, weapon.min(7));
		assertEquals(70, weapon.max(7));
		assertEquals(20, weapon.STRReq(0));
		assertEquals(1.18f, weapon.actualAccuracy(), 0f);
		assertEquals(1, weapon.actualRange());
		assertEquals(1f, weapon.actualDelay(), 0f);
	}

	@Test
	public void normalKnockbackStartsAtTwoAndIncreasesEveryFourLevels() {
		assertEquals(2, HundredTonHammer.normalKnockbackDistance(-3));
		assertEquals(2, HundredTonHammer.normalKnockbackDistance(3));
		assertEquals(3, HundredTonHammer.normalKnockbackDistance(4));
		assertEquals(3, HundredTonHammer.normalKnockbackDistance(7));
		assertEquals(4, HundredTonHammer.normalKnockbackDistance(8));
	}

	@Test
	public void knockbackUpgradePreviewAppearsOnlyAsAWeaponFeature() {
		HundredTonHammer weapon = new HundredTonHammer();
		assertEquals(1, weapon.upgradeFeatureStats(3).size());
		assertEquals("ability_ambush_damage",
				weapon.upgradeAbilityStats(3).get(0).type.messageKey());
		assertEquals("21-54", weapon.upgradeAbilityStats(3).get(0).value);
	}

	@Test
	public void ambushAbilityPreviewUsesTheSameAugmentAndBonusAsAbilityInfo() {
		HundredTonHammer weapon = new HundredTonHammer();
		assertEquals("13-28", weapon.upgradeAbilityStats(0).get(0).value);
		assertEquals("16-37", weapon.upgradeAbilityStats(1).get(0).value);
		weapon.augment = com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Augment.DAMAGE;
		int bonus = weapon.augment.damageFactor(HundredTonHammer.ambushDamageBonus(3));
		assertEquals((weapon.augment.damageFactor(weapon.min(3)) + bonus) + "-"
				+ (weapon.augment.damageFactor(weapon.max(3)) + bonus),
				weapon.upgradeAbilityStats(3).get(0).value);
	}

	@Test
	public void ambushDamageAndSplashUseDocumentedFormulas() {
		assertEquals(7, HundredTonHammer.ambushDamageBonus(0));
		assertEquals(12, HundredTonHammer.ambushDamageBonus(3));
		assertEquals(13, HundredTonHammer.ambushDamageBonus(4));
		assertEquals(3, HundredTonHammer.bounceDamage(0, 10));
		assertEquals(3, HundredTonHammer.bouncePower(4));
	}

	@Test
	public void abilityAttacksFirstThenKnocksBackAndAppliesLandingSplash() throws IOException {
		String compactSource = compactSource();
		String abilityBody = blockStartingAt(compactSource, "protectedvoidduelistAbility(");
		int strike = abilityBody.indexOf("hero.attack(target,1f,bonus,Float.POSITIVE_INFINITY");
		int push = abilityBody.indexOf("pushAndResolve(hero,target,directDamage)");
		assertTrue("the ability's guaranteed primary strike must occur before displacement",
				strike >= 0 && push > strike);

		String pushBody = blockStartingAt(compactSource, "privatevoidpushAndResolve(");
		assertTrue("terrain collisions must use the wand collision-damage behavior",
				pushBody.contains("bouncePower(buffedLvl()),true,true,this"));
		assertTrue("the callback must defer splash until the push has resolved",
				pushBody.contains("resolveBounceDamage(hero,target,directDamage/3)"));

		String resolutionBody = blockStartingAt(compactSource, "privatevoidresolveBounceDamage(");
		assertTrue("landing damage must be physical", resolutionBody.contains("DamageTag.PHYSICAL"));
		assertTrue("the landing target receives the five-turn daze", resolutionBody.contains("Daze.class,5f"));
		assertTrue("other landing-area targets are snapshotted before damage",
				resolutionBody.contains("PathFinder.NEIGHBOURS8")
						&& resolutionBody.indexOf("splashTargets.add(") < resolutionBody.indexOf(".damage("));
	}

	@Test
	public void normalProcUsesLevelScaledKnockbackWithCollisionDamage()
			throws IOException {
		String compactSource = compactSource();
		String procBody = blockStartingAt(compactSource, "publicintproc(");
		String throwCall = callStartingAt(procBody,
				"WandOfBlastWave.throwCharImmediately(");
		int argumentsStart = throwCall.indexOf('(');
		List<String> arguments = splitTopLevelArguments(
				throwCall.substring(argumentsStart + 1, throwCall.length() - 1));

		assertEquals("normal knockback must use the callback overload",
				7, arguments.size());
		assertEquals("normal knockback target", "defender", arguments.get(0));
		assertEquals("normal knockback trajectory", "trajectory", arguments.get(1));
		assertTrue("proc must use its buffed level for knockback power",
				arguments.get(2).contains("normalKnockbackDistance(buffedLvl())"));
		assertEquals("normal knockback must close doors", "true", arguments.get(3));
		assertEquals("normal knockback must apply collision damage",
				"true", arguments.get(4));
		assertEquals("normal knockback cause", "this", arguments.get(5));
		String callbackArgument = arguments.get(6).replace("(Callback)", "");
		assertEquals("normal knockback must not schedule a callback",
				"null", callbackArgument);

		int superProc = procBody.indexOf("super.proc(");
		int aliveGate = procBody.indexOf("!defender.isAlive()");
		int enemyGate = procBody.indexOf(
				"defender.alignment!=Char.Alignment.ENEMY");
		int immediateThrow = procBody.indexOf(
				"WandOfBlastWave.throwCharImmediately(");
		assertTrue("proc must call super before checking defender survival",
				superProc >= 0 && superProc < aliveGate);
		assertTrue("proc must call super before checking enemy alignment",
				superProc < enemyGate);
		assertTrue("survival gate must precede knockback",
				aliveGate < immediateThrow);
		assertTrue("enemy gate must precede knockback",
				enemyGate < immediateThrow);
		assertFalse("normal knockback must not suppress enchantments",
				compactSource.contains("enchantment=null"));
	}

	@Test
	public void wandResultCallbackCancelsBeforeCommittingDisplacedPosition()
			throws IOException {
		String wandSource = sourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/wands/"
						+ "WandOfBlastWave.java").replaceAll("\\s+", "");
		assertTrue("legacy immediate knockback entry point must remain available",
				wandSource.contains("publicstaticvoidthrowCharImmediately("));
		assertTrue("result-reporting immediate knockback entry point must exist",
				wandSource.contains("publicstaticvoidthrowCharImmediatelyWithResult("));

		String legacyBody = blockStartingAt(
				wandSource, "publicstaticvoidthrowCharImmediately(");
		String legacyDelegate = callStartingAt(legacyBody, "throwChar(");
		List<String> legacyArguments = splitTopLevelArguments(legacyDelegate.substring(
				legacyDelegate.indexOf('(') + 1, legacyDelegate.length() - 1));
		assertEquals("legacy entry point must delegate all private throw arguments",
				9, legacyArguments.size());
		assertEquals("legacy entry point must retain its ordinary callback",
				"callback", legacyArguments.get(6));
		assertEquals("legacy entry point must not opt into result cancellation",
				"null", legacyArguments.get(7));

		String resultBody = blockStartingAt(
				wandSource, "publicstaticvoidthrowCharImmediatelyWithResult(");
		String resultDelegate = callStartingAt(resultBody, "throwChar(");
		List<String> resultArguments = splitTopLevelArguments(resultDelegate.substring(
				resultDelegate.indexOf('(') + 1, resultDelegate.length() - 1));
		assertEquals("result entry point must delegate all private throw arguments",
				9, resultArguments.size());
		assertEquals("result entry point must not use the ordinary callback slot",
				"null", resultArguments.get(6));
		assertEquals("only the result entry point may pass a knockback callback",
				"callback", resultArguments.get(7));

		String throwBody = blockStartingAt(wandSource, "privatestaticvoidthrowChar(");
		int pushing = throwBody.indexOf("Pushingpushing=newPushing(");
		assertTrue("knockback must resolve through a Pushing callback", pushing >= 0);
		String pushingCallback = blockStartingAt(
				throwBody.substring(pushing), "publicvoidcall()");
		String invalidAssignmentMarker = "booleanresultTargetInvalid=";
		int invalidAssignment = pushingCallback.indexOf(invalidAssignmentMarker);
		assertTrue("result-target validity gate must be explicit", invalidAssignment >= 0);
		int invalidExpressionStart = invalidAssignment + invalidAssignmentMarker.length();
		int invalidExpressionEnd = pushingCallback.indexOf(';', invalidExpressionStart);
		assertTrue("result-target validity gate must end before later cancellation checks",
				invalidExpressionEnd > invalidExpressionStart);
		String invalidExpression = pushingCallback.substring(
				invalidExpressionStart, invalidExpressionEnd);
		assertTrue("dead and replaced checks must be gated by a result callback",
				invalidExpression.matches("knockbackCallback!=null&&\\(.*\\)"));
		assertTrue("the gated result check must reject dead characters",
				invalidExpression.contains("!ch.isAlive()"));
		assertTrue("the gated result check must reject replaced identities",
				invalidExpression.contains("Actor.findById(ch.id())!=ch"));

		int dead = pushingCallback.indexOf("!ch.isAlive()");
		int replaced = pushingCallback.indexOf("Actor.findById(ch.id())!=ch");
		int cancelled = pushingCallback.indexOf(
				"completeKnockback(callback,knockbackCallback,false,0)");
		int commitPosition = pushingCallback.indexOf("ch.pos=newPos");
		int occupyCell = pushingCallback.indexOf("Dungeon.level.occupyCell(ch)");
		assertTrue("cancel gate must reject dead characters", dead >= 0);
		assertTrue("cancel gate must reject replaced character identities", replaced >= 0);
		assertTrue("cancelled pushes must report false and zero distance",
				cancelled > dead && cancelled > replaced);
		assertTrue("cancellation must happen before committing the new position",
				cancelled < commitPosition);
		assertTrue("cancellation must happen before occupying the destination cell",
				cancelled < occupyCell);
	}

	@Test
	public void generatorIncludesHundredTonHammerWithMatchingWeights() {
		int hammerIndex = Arrays.asList(Generator.Category.WEP_T6.classes)
				.indexOf(HundredTonHammer.class);
		assertTrue(hammerIndex >= 0);
		assertEquals(Generator.Category.WEP_T6.classes.length,
				Generator.Category.WEP_T6.defaultProbs.length);

		float expectedWeight = Generator.Category.WEP_T6.defaultProbs[0];
		for (int i = 0; i < Generator.Category.WEP_T6.defaultProbs.length; i++) {
			float weight = Generator.Category.WEP_T6.defaultProbs[i];
			if (Generator.Category.WEP_T6.classes[i] == LakeSword.class) {
				assertEquals("the Lake Sword remains unavailable through normal drops", 0f, weight, 0f);
			} else {
				assertTrue("available tier-six weapon weights must be positive", weight > 0f);
				assertEquals("available tier-six generator weights must be equal",
						expectedWeight, weight, 0f);
			}
		}
		assertEquals(expectedWeight,
				Generator.Category.WEP_T6.defaultProbs[hammerIndex], 0f);
	}

	private static String compactSource() throws IOException {
		return source().replaceAll("\\s+", "");
	}

	private static String blockStartingAt(String source, String marker) {
		int markerStart = source.indexOf(marker);
		assertTrue("missing source marker: " + marker, markerStart >= 0);
		int blockStart = source.indexOf('{', markerStart);
		assertTrue("missing block for source marker: " + marker, blockStart >= 0);

		int depth = 0;
		for (int i = blockStart; i < source.length(); i++) {
			char current = source.charAt(i);
			if (current == '{') depth++;
			if (current == '}' && --depth == 0) {
				return source.substring(blockStart, i + 1);
			}
		}
		throw new AssertionError("unterminated block for source marker: " + marker);
	}

	private static String callStartingAt(String source, String marker) {
		int callStart = source.indexOf(marker);
		assertTrue("missing call: " + marker, callStart >= 0);
		int argumentsStart = source.indexOf('(', callStart);

		int depth = 0;
		for (int i = argumentsStart; i < source.length(); i++) {
			char current = source.charAt(i);
			if (current == '(') depth++;
			if (current == ')' && --depth == 0) {
				return source.substring(callStart, i + 1);
			}
		}
		throw new AssertionError("unterminated call: " + marker);
	}

	private static String argumentPrefixBeforeMarker(String source,
			String callMarker, String innerMarker) {
		int callStart = source.indexOf(callMarker);
		assertTrue("missing call: " + callMarker, callStart >= 0);
		int argumentsStart = source.indexOf('(', callStart);
		int innerStart = source.indexOf(innerMarker, argumentsStart);
		assertTrue("missing callback marker: " + innerMarker, innerStart >= 0);

		int parentheses = 0;
		int braces = 0;
		int brackets = 0;
		int callbackArgumentSeparator = -1;
		for (int i = argumentsStart; i < innerStart; i++) {
			char current = source.charAt(i);
			if (current == ',' && parentheses == 1 && braces == 0 && brackets == 0) {
				callbackArgumentSeparator = i;
			}
			if (current == '(') parentheses++;
			if (current == ')') parentheses--;
			if (current == '{') braces++;
			if (current == '}') braces--;
			if (current == '[') brackets++;
			if (current == ']') brackets--;
			assertTrue("callback marker must belong to the immediate knockback call",
					parentheses > 0);
		}
		assertTrue("missing callback argument separator", callbackArgumentSeparator >= 0);
		return source.substring(argumentsStart + 1, callbackArgumentSeparator);
	}

	private static List<String> splitTopLevelArguments(String argumentPrefix) {
		List<String> arguments = new ArrayList<>();
		int parentheses = 0;
		int braces = 0;
		int brackets = 0;
		int argumentStart = 0;

		for (int i = 0; i < argumentPrefix.length(); i++) {
			char current = argumentPrefix.charAt(i);
			switch (current) {
				case '(':
					parentheses++;
					break;
				case ')':
					parentheses--;
					break;
				case '{':
					braces++;
					break;
				case '}':
					braces--;
					break;
				case '[':
					brackets++;
					break;
				case ']':
					brackets--;
					break;
				case ',':
					if (parentheses == 0 && braces == 0 && brackets == 0) {
						arguments.add(argumentPrefix.substring(argumentStart, i));
						argumentStart = i + 1;
					}
					break;
				default:
					break;
			}
		}
		arguments.add(argumentPrefix.substring(argumentStart));
		return arguments;
	}

	private static String source() throws IOException {
		return sourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/"
						+ "tier6/HundredTonHammer.java");
	}

	private static String sourceAt(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path sourcePath = coreDirectory.resolve(relativePath);
		return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
	}

	private static class TestableHundredTonHammer extends HundredTonHammer {
		float actualAccuracy() {
			return ACC;
		}

		int actualRange() {
			return RCH;
		}

		float actualDelay() {
			return DLY;
		}
	}
}
