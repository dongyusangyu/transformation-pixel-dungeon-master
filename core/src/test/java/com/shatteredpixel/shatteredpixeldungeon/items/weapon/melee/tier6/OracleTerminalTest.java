package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EXItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OracleTerminalTest {

	@Test
	public void usesSpecifiedTierSixStats() {
		TestableOracleTerminal weapon = new TestableOracleTerminal();

		assertEquals(6, OracleTerminal.TIER);
		assertEquals(6, weapon.min(0));
		assertEquals(30, weapon.max(0));
		assertEquals(13, weapon.min(7));
		assertEquals(79, weapon.max(7));
		assertEquals(20, weapon.STRReq(0));
		assertEquals(1f, weapon.actualAccuracy(), 0f);
		assertEquals(1f, weapon.actualDelay(), 0f);
		assertEquals(1, weapon.actualRange());
		assertEquals(EXItemSpriteSheet.ORACLE_TERMINAL, weapon.image);
	}

	@Test
	public void rollBoundariesAreThirtyThirtyThirtyTen() {
		assertEquals(OracleTerminal.Form.BLUNT, OracleTerminal.formForRoll(0));
		assertEquals(OracleTerminal.Form.BLUNT, OracleTerminal.formForRoll(29));
		assertEquals(OracleTerminal.Form.SLASH, OracleTerminal.formForRoll(30));
		assertEquals(OracleTerminal.Form.SLASH, OracleTerminal.formForRoll(59));
		assertEquals(OracleTerminal.Form.THRUST, OracleTerminal.formForRoll(60));
		assertEquals(OracleTerminal.Form.THRUST, OracleTerminal.formForRoll(89));
		assertEquals(OracleTerminal.Form.SCYTHE, OracleTerminal.formForRoll(90));
		assertEquals(OracleTerminal.Form.SCYTHE, OracleTerminal.formForRoll(99));
	}

	@Test
	public void formMathMatchesSpecification() {
		assertEquals(0, OracleTerminal.slashDamage(3));
		assertEquals(1, OracleTerminal.slashDamage(4));
		assertEquals(25, OracleTerminal.slashDamage(100));
		assertEquals(4, OracleTerminal.bleedLevel(-1));
		assertEquals(11, OracleTerminal.bleedLevel(7));
		assertEquals(15, OracleTerminal.scytheDamage(10));
		assertEquals(17, OracleTerminal.scytheDamage(11));
	}

	@Test
	public void eachFormHasItsOwnVisibleBuffAndExactIcon() {
		assertEquals(5f, OracleTerminal.FORM_DURATION, 0f);
		assertEquals(OracleTerminal.Form.BLUNT,
				new OracleTerminal.BluntForm().form());
		assertEquals(OracleTerminal.Form.SLASH,
				new OracleTerminal.SlashForm().form());
		assertEquals(OracleTerminal.Form.THRUST,
				new OracleTerminal.ThrustForm().form());
		assertEquals(OracleTerminal.Form.SCYTHE,
				new OracleTerminal.ScytheForm().form());
		assertEquals(BuffIndicator.ORACLE_BLUNT,
				new OracleTerminal.BluntForm().icon());
		assertEquals(BuffIndicator.ORACLE_SLASH,
				new OracleTerminal.SlashForm().icon());
		assertEquals(BuffIndicator.ORACLE_THRUST,
				new OracleTerminal.ThrustForm().icon());
		assertEquals(BuffIndicator.ORACLE_SCYTHE,
				new OracleTerminal.ScytheForm().icon());
		assertTrue(new OracleTerminal.ScytheExecutionMark().icon()
				== BuffIndicator.NONE);
	}

	@Test
	public void heavyBlowUsesTheMaceDamageGrowth() {
		assertEquals(5, OracleTerminal.heavyBlowBonus(0));
		assertEquals(16, OracleTerminal.heavyBlowBonus(7));
	}

	@Test
	public void heavyBlowOpensTheNormalMaceTargetSelector() throws IOException {
		String source = compactSourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/"
						+ "melee/tier6/OracleTerminal.java");
		assertTrue(source.contains("publicStringtargetingPrompt()"));
		assertTrue(source.contains("returnMessages.get(this,\"prompt\")"));
		assertTrue(source.contains("Mace.heavyBlowAbility(hero,target,1f,damageBonus,this)"));
	}

	@Test
	public void applyingAFormReplacesEveryPreviousOracleForm() {
		Rat target = new Rat();

		OracleTerminal.applyFormForRoll(target, 0);
		assertNotNull(target.buff(OracleTerminal.BluntForm.class));
		assertEquals(1, target.buffs(OracleTerminal.OracleFormBuff.class).size());

		OracleTerminal.applyFormForRoll(target, 30);
		assertNull(target.buff(OracleTerminal.BluntForm.class));
		assertNotNull(target.buff(OracleTerminal.SlashForm.class));
		assertEquals(1, target.buffs(OracleTerminal.OracleFormBuff.class).size());

		OracleTerminal.applyFormForRoll(target, 95);
		assertNull(target.buff(OracleTerminal.SlashForm.class));
		assertNotNull(target.buff(OracleTerminal.ScytheForm.class));
		assertEquals(1, target.buffs(OracleTerminal.OracleFormBuff.class).size());
	}

	@Test
	public void equippedCheckCoversBothWeaponSlots() {
		OracleTerminal primary = new OracleTerminal();
		OracleTerminal secondary = new OracleTerminal();

		assertFalse(OracleTerminal.hasEquippedOracle(null, null));
		assertTrue(OracleTerminal.hasEquippedOracle(primary, null));
		assertTrue(OracleTerminal.hasEquippedOracle(null, secondary));
	}

	@Test
	public void expiredFormDetachesWhenNoOracleRemainsEquipped() {
		Rat target = new Rat();
		OracleTerminal.applyFormForRoll(target, 60);
		OracleTerminal.ThrustForm form = target.buff(OracleTerminal.ThrustForm.class);

		assertNotNull(form);
		form.act();
		assertNull(target.buff(OracleTerminal.ThrustForm.class));
		assertEquals(0, target.buffs(OracleTerminal.OracleFormBuff.class).size());
	}

	@Test
	public void heroForwardsFinalResolvedDamageToTheAttackingWeapon()
			throws IOException {
		String weaponSource = compactSourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/"
						+ "KindOfWeapon.java");
		assertTrue(weaponSource.contains(
				"publicvoidonAttackResolved(Heroattacker,Chardefender,booleanhit,"
						+ "intdamageDealt,DamageTag...damageTags)"));

		String heroSource = compactSourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/"
						+ "Hero.java");
		assertTrue(heroSource.contains(
				"weapon.onAttackResolved(this,enemy,hit,damageDealt,damageTags)"));
	}

	@Test
	public void bluntChanceUsesAFixedTwentyFivePercentBoundary() {
		assertTrue(OracleTerminal.bluntTriggers(0f));
		assertTrue(OracleTerminal.bluntTriggers(0.249999f));
		assertFalse(OracleTerminal.bluntTriggers(0.25f));
		assertFalse(OracleTerminal.bluntTriggers(0.99f));
	}

	@Test
	public void onlySlashMayResolveAfterThePrimaryTargetDies() {
		assertFalse(OracleTerminal.requiresLivingTarget(OracleTerminal.Form.SLASH));
		assertTrue(OracleTerminal.requiresLivingTarget(OracleTerminal.Form.BLUNT));
		assertTrue(OracleTerminal.requiresLivingTarget(OracleTerminal.Form.THRUST));
		assertTrue(OracleTerminal.requiresLivingTarget(OracleTerminal.Form.SCYTHE));
	}

	@Test
	public void resolvedDamageIncludesDirectAttackProcDamageEvenWhenItKills() {
		RecordingAttacker attacker = new RecordingAttacker();
		TestTarget target = new TestTarget(8);
		int healthBeforeAttackProc = target.HP + target.shielding();

		//Simulates direct damage inside attackProc without invoking rendering code.
		target.HP = 0;
		int resolved = attacker.finishForTest(target, healthBeforeAttackProc);

		assertFalse(target.isAlive());
		assertEquals(8, resolved);
		assertEquals(8, attacker.resolvedDamage);
		assertEquals(1, attacker.resolutionCount);
	}

	@Test
	public void attackCapturesHealthBeforeProcAndResolvesProcKills() throws IOException {
		String source = compactSourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/Char.java");
		int capture = source.indexOf(
				"intattackHealthBefore=enemy.HP+enemy.shielding()");
		int proc = source.indexOf("effectiveDamage=attackProc(", capture);
		int deadCheck = source.indexOf("if(!enemy.isAlive())", proc);
		int deadResolution = source.indexOf(
				"finishAttackResolution(enemy,attackHealthBefore,resolvedAttackTags)",
				deadCheck);
		int deadReturn = source.indexOf("returntrue", deadCheck);
		int primaryCapture = source.indexOf(
				"intprimaryHealthBefore=enemy.HP+enemy.shielding()", deadReturn);
		int primaryDamage = source.indexOf(
				"resolvedAttackDamage(enemy,primaryHealthBefore)", primaryCapture);
		int berserk = source.indexOf("onPhysicalDamageDealt(damageDealt,melee)",
				primaryDamage);
		int finalResolution = source.indexOf(
				"finishAttackResolution(enemy,attackHealthBefore,resolvedAttackTags)",
				berserk);

		assertTrue(capture >= 0);
		assertTrue(proc > capture);
		assertTrue(deadCheck > proc);
		assertTrue(deadResolution > deadCheck);
		assertTrue(deadReturn > deadResolution);
		assertTrue(primaryCapture > deadReturn);
		assertTrue(primaryDamage > primaryCapture);
		assertTrue(berserk > primaryDamage);
		assertTrue(finalResolution > berserk);
	}

	@Test
	public void scytheFormMultipliesThePostEnchantmentDamage() {
		Rat attacker = new Rat();

		OracleTerminal.applyFormForRoll(attacker, 90);
		assertEquals(15, OracleTerminal.applyFormDamage(attacker, 10));
		assertEquals(17, OracleTerminal.applyFormDamage(attacker, 11));

		OracleTerminal.applyFormForRoll(attacker, 0);
		assertEquals(10, OracleTerminal.applyFormDamage(attacker, 10));
	}

	@Test
	public void resolvedEffectsAreNonRecursiveAndUseTheSpecifiedBuffs()
			throws IOException {
		String source = compactSourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/"
						+ "melee/tier6/OracleTerminal.java");
		assertTrue(source.contains("Buff.affect(target,Paralysis.class,1f)"));
		assertTrue(source.contains("Buff.affect(target,Daze.class,1f)"));
		assertTrue(source.contains(
				"Buff.affect(target,Bleeding.class).set(bleedLevel(buffedLvl()))"));
		assertTrue(source.contains("Buff.affect(target,Vulnerable.class,2f)"));
		assertTrue(source.contains("target.buff(ScytheExecutionMark.class)==null"));
		assertTrue(source.contains("WandOfBlastWave.throwCharImmediately("));
		assertTrue(source.contains("canReach(hero,candidate.pos)"));
		assertTrue(source.contains("slashTargets.add(candidate)"));
		assertTrue(source.contains("candidate.damage(splashDamage,hero,DamageTag.PHYSICAL)"));
		assertFalse(source.contains("candidate.attack("));
	}

	@Test
	public void detailedEffectsStayInBuffsAndTheJournalCatalog() throws IOException {
		String weaponSource = compactSourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/"
						+ "melee/tier6/OracleTerminal.java");
		assertTrue(weaponSource.contains("publicStringcatalogDesc()"));
		assertTrue(weaponSource.contains("publicStringabilityInfo()"));
		assertTrue(weaponSource.contains("publicStringupgradeAbilityStat(intlevel)"));

		String journalSource = compactSourceAt(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndJournal.java");
		assertTrue(journalSource.contains("iteminstanceofOracleTerminal"));
		assertTrue(journalSource.contains("((OracleTerminal)item).catalogDesc()"));

		String chinese = sourceAt("src/main/assets/messages/items/items_zh.properties");
		assertTrue(chinese.contains("items.weapon.melee.tier6.oracleterminal.name=神谕终端"));
		assertTrue(chinese.contains("items.weapon.melee.tier6.oracleterminal.blunt_desc="));
		assertTrue(chinese.contains("oracleterminal.blunt_desc=神谕终端当前幻化为打击武器（抽取概率30%%）"));
		assertTrue(chinese.contains("items.weapon.melee.tier6.oracleterminal.slash_desc="));
		assertTrue(chinese.contains("oracleterminal.slash_desc=神谕终端当前幻化为斩击武器（抽取概率30%%）"));
		assertTrue(chinese.contains("items.weapon.melee.tier6.oracleterminal.thrust_desc="));
		assertTrue(chinese.contains("oracleterminal.thrust_desc=神谕终端当前幻化为突刺武器（抽取概率30%%）"));
		assertTrue(chinese.contains("items.weapon.melee.tier6.oracleterminal.scythe_desc="));
		assertTrue(chinese.contains("oracleterminal.scythe_desc=神谕终端当前幻化为镰刀（抽取概率10%%）"));
		assertTrue(chinese.contains("items.weapon.melee.tier6.oracleterminal.catalog_desc="));
	}

	private static String compactSourceAt(String relativePath) throws IOException {
		return sourceAt(relativePath).replaceAll("\\s+", "");
	}

	private static String sourceAt(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)),
				StandardCharsets.UTF_8);
	}

	private static class TestableOracleTerminal extends OracleTerminal {
		float actualAccuracy() {
			return ACC;
		}

		float actualDelay() {
			return DLY;
		}

		int actualRange() {
			return RCH;
		}
	}

	private static class RecordingAttacker extends Rat {
		private int resolvedDamage = -1;
		private int resolutionCount;

		private int finishForTest(Char enemy, int healthBefore) {
			return finishAttackResolution(enemy, healthBefore, DamageTag.PHYSICAL);
		}

		@Override
		protected void onAttackResolved(Char enemy, boolean hit, int damageDealt,
				DamageTag... damageTags) {
			resolvedDamage = damageDealt;
			resolutionCount++;
		}
	}

	private static class TestTarget extends Rat {
		private TestTarget(int health) {
			HP = HT = health;
		}

		@Override
		public int drRoll() {
			return 0;
		}
	}

}
