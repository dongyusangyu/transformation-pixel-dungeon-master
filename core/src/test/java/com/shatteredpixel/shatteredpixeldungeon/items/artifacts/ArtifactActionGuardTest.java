package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/** Guards quickslot active actions from leaking curse state before equip validation. */
public class ArtifactActionGuardTest {

	@Test
	public void lloydsBeaconRequiresEquipBeforeCheckingCurse() throws Exception {
		assertEquipGuardPrecedesCurseAndReturns(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/LloydsBeacon.java");
	}

	@Test
	public void capeOfThornsRequiresEquipBeforeCheckingCurse() throws Exception {
		assertEquipGuardPrecedesCurseAndReturns(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/CapeOfThorns.java");
	}

	@Test
	public void lloydsBeaconBlocksMagicImmuneActiveUse() throws Exception {
		assertMagicImmuneGuard(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/LloydsBeacon.java");
	}

	@Test
	public void capeOfThornsBlocksMagicImmuneActiveUse() throws Exception {
		assertMagicImmuneGuard(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/CapeOfThorns.java");
	}

	@Test
	public void capeOfThornsHidesActionDuringMagicImmunity() throws Exception {
		assertActionsCheckMagicImmunity(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/CapeOfThorns.java");
	}

	@Test
	public void lloydsBeaconHidesActionsDuringMagicImmunity() throws Exception {
		assertActionsCheckMagicImmunity(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/LloydsBeacon.java");
	}

	@Test
	public void sharedArtifactGuardChecksEquipmentBeforeMagicAndCurse() throws Exception {
		String source = readSource(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/Artifact.java");
		int guard = source.indexOf("protected boolean canUseActiveAction");
		int equip = source.indexOf("!isEquipped", guard);
		int magic = source.indexOf("hero.buff(MagicImmune.class)", guard);
		int curse = source.indexOf("cursed", magic);

		assertTrue("shared artifact guard must exist", guard >= 0);
		assertTrue("equipment must be checked first", equip > guard && equip < magic);
		assertTrue("magic immunity must be checked before curse", magic > equip && magic < curse);
	}

	private static void assertEquipGuardPrecedesCurseAndReturns(String path) throws Exception {
		String source = readSource(path);
		int execute = source.indexOf("public void execute( Hero hero, String action )");
		if (execute < 0) execute = source.indexOf("public void execute(Hero hero, String action)");
		if (source.substring(execute).contains("canUseActiveAction(hero)")) {
			return;
		}
		int equip = source.indexOf("!isEquipped", execute);
		int curse = source.indexOf("cursed", execute);
		int actionEnd = source.indexOf("\n\t}\n", execute);

		assertTrue("active execute method must exist", execute >= 0);
		assertTrue("equip validation must happen before curse validation", equip > execute && equip < curse);
		assertTrue("unequipped active action must stop immediately",
				source.substring(equip, actionEnd).contains("return;"));
	}

	private static void assertMagicImmuneGuard(String path) throws Exception {
		String source = readSource(path);
		int execute = source.indexOf("public void execute");
		int actionEnd = source.indexOf("\n\t}\n", execute);

		assertTrue("active execute method must exist", execute >= 0);
		String executeBody = source.substring(execute, actionEnd);
		assertTrue("active action must use the shared guard",
				executeBody.contains("canUseActiveAction(hero)"));
	}

	private static void assertActionsCheckMagicImmunity(String path) throws Exception {
		String source = readSource(path);
		int actions = source.indexOf("public ArrayList<String> actions");
		int execute = source.indexOf("public void execute", actions);

		assertTrue("actions method must exist", actions >= 0);
		assertTrue("actions method must end before execute method", execute > actions);
		assertTrue("active action must be hidden during magic immunity",
				source.substring(actions, execute).contains("MagicImmune.class"));
	}

	private static String readSource(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}
