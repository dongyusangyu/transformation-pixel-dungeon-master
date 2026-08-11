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
		assertEquals(28, HundredTonHammer.maxForLevel(0));
		assertEquals(13, HundredTonHammer.minForLevel(7));
		assertEquals(70, HundredTonHammer.maxForLevel(7));
		assertEquals(1.16f, HundredTonHammer.ACCURACY, 0f);
		assertEquals(1, HundredTonHammer.RANGE);
		assertEquals(1f, HundredTonHammer.DELAY, 0f);
		assertEquals(EXItemSpriteSheet.HUNDRED_TON_HAMMER, weapon.image);
		assertEquals(6, weapon.min(0));
		assertEquals(28, weapon.max(0));
		assertEquals(13, weapon.min(7));
		assertEquals(70, weapon.max(7));
		assertEquals(20, weapon.STRReq(0));
		assertEquals(1.16f, weapon.actualAccuracy(), 0f);
		assertEquals(1, weapon.actualRange());
		assertEquals(1f, weapon.actualDelay(), 0f);
	}

	@Test
	public void normalKnockbackIncreasesEverySevenLevels() {
		assertEquals(1, HundredTonHammer.normalKnockbackDistance(-3));
		assertEquals(1, HundredTonHammer.normalKnockbackDistance(6));
		assertEquals(2, HundredTonHammer.normalKnockbackDistance(7));
		assertEquals(2, HundredTonHammer.normalKnockbackDistance(13));
		assertEquals(3, HundredTonHammer.normalKnockbackDistance(14));
	}

	@Test
	public void bounceDamageUsesActualDistanceAndIntegerDivision() {
		assertEquals(0, HundredTonHammer.bounceDamage(0, 8));
		assertEquals(7, HundredTonHammer.bounceDamage(5, 3));
		assertEquals(28, HundredTonHammer.bounceDamage(7, 8));
		assertEquals(8, HundredTonHammer.bouncePower(7));
	}

	@Test
	public void abilityWaitsForImmediateKnockbackBeforeResolvingBounceDamage()
			throws IOException {
		String compactSource = compactSource();
		String abilityBody = blockStartingAt(
				compactSource, "protectedvoidduelistAbility(");
		assertTrue("ability entry point must delegate bounce resolution",
				abilityBody.contains("pushAndResolve(hero,target)")
						|| abilityBody.contains("this.pushAndResolve(hero,target)"));

		String pushBody = blockStartingAt(compactSource, "privatevoidpushAndResolve(");
		int immediateThrow = pushBody.indexOf(
				"WandOfBlastWave.throwCharImmediatelyWithResult(");
		assertTrue("bounce helper must use immediate knockback", immediateThrow >= 0);

		String callbackMarker =
				"publicvoidcall(booleanresolvedByThisPush,intreportedDistance)";
		String argumentPrefix = argumentPrefixBeforeMarker(pushBody,
				"WandOfBlastWave.throwCharImmediatelyWithResult(", callbackMarker);
		List<String> arguments = splitTopLevelArguments(argumentPrefix);
		assertEquals("callback must be the seventh immediate-knockback argument",
				6, arguments.size());
		assertEquals("collideDmg must be false", "false", arguments.get(4));

		String afterImmediateThrow = pushBody.substring(immediateThrow);
		int callback = afterImmediateThrow.indexOf(callbackMarker);
		assertTrue("immediate knockback must provide a callback", callback >= 0);
		String callbackBody = blockStartingAt(afterImmediateThrow, callbackMarker);

		Pattern distanceAssignment = Pattern.compile(
				"(?:final)?int([A-Za-z_$][A-Za-z0-9_$]*)="
						+ "resolvedByThisPush\\?"
						+ "Dungeon\\.level\\.distance\\(startPos,target\\.pos\\):0;");
		Matcher distanceMatcher = distanceAssignment.matcher(callbackBody);
		assertTrue("external cancellation must force actual distance to zero",
				distanceMatcher.find());
		String distanceVariable = distanceMatcher.group(1);

		String quotedDistance = Pattern.quote(distanceVariable);
		Pattern reportedDistanceGuard = Pattern.compile(
				"if\\((?:" + quotedDistance + "!=reportedDistance|reportedDistance!="
						+ quotedDistance + ")\\)\\{?" + quotedDistance + "=0;");
		Matcher reportedDistanceMatcher = reportedDistanceGuard.matcher(callbackBody);
		assertTrue("a mismatched reported distance must force damage distance to zero",
				reportedDistanceMatcher.find(distanceMatcher.end()));

		Pattern directDamageFlow = Pattern.compile(
				"resolveBounceDamage\\(hero,target,"
						+ "bounceDamage\\(buffedLvl\\(\\),"
						+ quotedDistance + "\\)\\)");
		boolean directFlow = directDamageFlow.matcher(callbackBody)
				.find(reportedDistanceMatcher.end());

		Pattern damageAssignment = Pattern.compile(
				"(?:final)?int([A-Za-z_$][A-Za-z0-9_$]*)="
						+ "bounceDamage\\(buffedLvl\\(\\),"
						+ quotedDistance + "\\);");
		Matcher assignmentMatcher = damageAssignment.matcher(callbackBody);
		boolean localFlow = false;
		if (assignmentMatcher.find(reportedDistanceMatcher.end())) {
			String damageVariable = assignmentMatcher.group(1);
			Pattern localResolution = Pattern.compile(
					"resolveBounceDamage\\(hero,target,"
							+ Pattern.quote(damageVariable) + "\\)");
			localFlow = localResolution.matcher(callbackBody).find(assignmentMatcher.end());
		}
		assertTrue("bounce damage result must be passed to damage resolution",
				directFlow || localFlow);

		String resolutionBody = blockStartingAt(
				compactSource, "privatevoidresolveBounceDamage(");
		assertTrue("bounce splash must inspect all eight neighbours",
				resolutionBody.contains("PathFinder.NEIGHBOURS8"));
		assertTrue("bounce damage must be tagged physical",
				resolutionBody.contains("DamageTag.PHYSICAL"));
		int firstDamage = resolutionBody.indexOf(".damage(");
		int firstSnapshotAdd = resolutionBody.indexOf("splashTargets.add(");
		int lastSnapshotAdd = resolutionBody.lastIndexOf("splashTargets.add(");
		assertTrue("all splash targets must be snapshotted before damage begins",
				firstSnapshotAdd >= 0 && lastSnapshotAdd < firstDamage);
		assertTrue("the splash snapshot must deduplicate character identities",
				resolutionBody.contains("!splashTargets.contains("));
		String splashLoopMarker = "for(CharsplashTarget:splashTargets)";
		int splashLoop = resolutionBody.indexOf(splashLoopMarker);
		assertTrue("each snapshotted identity must be processed by one later loop",
				splashLoop > firstDamage
						&& splashLoop == resolutionBody.lastIndexOf(splashLoopMarker));
		assertFalse("bounce damage must not recursively perform a hero attack",
				resolutionBody.contains("hero.attack("));
		assertFalse("bounce damage must not recursively invoke weapon procs",
				resolutionBody.contains(".proc("));
	}

	@Test
	public void normalProcUsesLevelScaledKnockbackWithoutCollisionDamage()
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
		assertEquals("normal knockback must disable collision damage",
				"false", arguments.get(4));
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
		for (float weight : Generator.Category.WEP_T6.defaultProbs) {
			assertTrue("tier-six generator weights must be positive", weight > 0f);
			assertEquals("tier-six generator weights must be equal",
					expectedWeight, weight, 0f);
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
