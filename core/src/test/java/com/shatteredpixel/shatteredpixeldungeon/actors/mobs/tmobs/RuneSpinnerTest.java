package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RuneSpinnerTest {

	@Test
	public void usesApprovedPanelAndInheritsSpinnerBehavior() {
		TestRuneSpinner spider = new TestRuneSpinner(false, new RecordingCurse());

		assertTrue(spider instanceof Spinner);
		assertEquals(120, spider.HT);
		assertEquals(120, spider.HP);
		assertEquals(40, spider.attackSkill(null));
		assertEquals(20, spider.defenseSkill(new Char() { }));
		assertEquals(13, spider.EXP);
		assertEquals(30, spider.maxLvl);
		for (int i = 0; i < 500; i++) {
			int damage = spider.damageRoll();
			int armor = spider.drRoll();
			assertTrue(damage >= 15 && damage <= 35);
			assertTrue(armor >= 0 && armor <= 10);
		}
	}

	@Test
	public void onePercentCurseThresholdUsesStrictBoundary() {
		assertTrue(RuneSpinner.curseRollSucceeds(0f));
		assertTrue(RuneSpinner.curseRollSucceeds(0.009999f));
		assertFalse(RuneSpinner.curseRollSucceeds(0.01f));
		assertFalse(RuneSpinner.curseRollSucceeds(0.5f));
	}

	@Test
	public void failedRollPreservesOrdinaryGlyphAndDoesNotArmCurse() {
		RecordingGlyph blessing = new RecordingGlyph();
		Armor armor = new Armor(1).inscribe(blessing);
		Char defender = new Char() { };
		TestRuneSpinner spider = new TestRuneSpinner(false, new RecordingCurse());

		spider.prepareArmorCurseForTest(armor, defender);

		assertSame(blessing, armor.glyph);
		assertFalse(armor.cursed);
		assertFalse(Armor.Glyph.forcedCurseProcPending(defender));
	}

	@Test
	public void successfulRollReplacesGlyphMarksArmorCursedAndForcesNewCurse() {
		RecordingCurse curse = new RecordingCurse();
		Armor armor = new Armor(1).inscribe(new RecordingGlyph());
		Char defender = new Char() { };
		TestRuneSpinner spider = new TestRuneSpinner(true, curse);

		spider.prepareArmorCurseForTest(armor, defender);

		assertSame(curse, armor.glyph);
		assertTrue(armor.cursed);
		assertTrue(Armor.Glyph.forcedCurseProcPending(defender));
		curse.proc(armor, spider, defender, 12);
		assertTrue(Float.isInfinite(curse.lastMultiplier));
		assertFalse(Armor.Glyph.forcedCurseProcPending(defender));
	}

	@Test
	public void existingCurseIsNotReplacedButItsNextProcIsForced() {
		RecordingCurse existing = new RecordingCurse();
		Armor armor = new Armor(1).inscribe(existing);
		Char defender = new Char() { };
		TestRuneSpinner spider = new TestRuneSpinner(true, new RecordingCurse());

		spider.prepareArmorCurseForTest(armor, defender);

		assertSame(existing, armor.glyph);
		assertTrue(Armor.Glyph.forcedCurseProcPending(defender));
	}

	@Test
	public void forcedProcIsBoundToEquippedArmorCurse() {
		RecordingCurse equippedCurse = new RecordingCurse();
		RecordingCurse otherCurse = new RecordingCurse();
		Char defender = new Char() { };

		Armor.Glyph.forceNextCurseProc(defender, equippedCurse);

		assertFalse(Armor.Glyph.forcedCurseProcTargets(defender, otherCurse));
		assertTrue(Armor.Glyph.forcedCurseProcTargets(defender, equippedCurse));
		assertTrue(Armor.Glyph.forcedCurseProcPending(defender));

		equippedCurse.proc(new Armor(1), new Char() { }, defender, 10);
		assertTrue(Float.isInfinite(equippedCurse.lastMultiplier));
		assertFalse(Armor.Glyph.forcedCurseProcPending(defender));
	}

	@Test
	public void nonHeroTargetsAreIgnored() {
		TestRuneSpinner spider = new TestRuneSpinner(true, new RecordingCurse());

		assertFalse(spider.prepareTargetArmorCurseForTest(new Char() { }));
	}

	private static class TestRuneSpinner extends RuneSpinner {
		private final boolean curseRoll;
		private final Armor.Glyph curse;

		private TestRuneSpinner(boolean curseRoll, Armor.Glyph curse) {
			this.curseRoll = curseRoll;
			this.curse = curse;
		}

		@Override
		protected boolean rollArmorCurse() {
			return curseRoll;
		}

		@Override
		protected Armor.Glyph randomArmorCurse() {
			return curse;
		}

		void prepareArmorCurseForTest(Armor armor, Char defender) {
			prepareArmorCurse(armor, defender);
		}

		boolean prepareTargetArmorCurseForTest(Char defender) {
			return prepareTargetArmorCurse(defender);
		}
	}

	private static class RecordingGlyph extends Armor.Glyph {
		@Override
		public int proc(Armor armor, Char attacker, Char defender, int damage) {
			return damage;
		}

		@Override
		public ItemSprite.Glowing glowing() {
			return null;
		}
	}

	private static class RecordingCurse extends RecordingGlyph {
		float lastMultiplier;

		@Override
		public int proc(Armor armor, Char attacker, Char defender, int damage) {
			lastMultiplier = procChanceMultiplier(defender);
			return damage;
		}

		@Override
		public boolean curse() {
			return true;
		}
	}
}
