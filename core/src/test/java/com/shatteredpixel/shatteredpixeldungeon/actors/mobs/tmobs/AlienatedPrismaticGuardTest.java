package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.tmobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.DamageTag;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AlienatedPrismaticGuardTest {

	@Test
	public void hasTowerBaseStatsAndConfiguredExperience() {
		TestGuard guard = new TestGuard(null);

		assertEquals(100, guard.HT);
		assertEquals(100, guard.HP);
		assertEquals(20, guard.defenseSkill(null));
		assertEquals(13, guard.EXP);
		assertEquals(30, guard.maxLvl);
	}

	@Test
	public void hostileGuardAndMirrorMatchNormalImageEnvironmentalImmunities() {
		AlienatedPrismaticGuard guard = new AlienatedPrismaticGuard();
		TwistedMirror mirror = new TwistedMirror();
		Class<?>[] effects = {
				ToxicGas.class,
				CorrosiveGas.class,
				Burning.class,
				AllyBuff.class
		};

		for (Class<?> effect : effects) {
			assertTrue(guard.isImmune(effect));
			assertTrue(mirror.isImmune(effect));
		}
	}

	@Test
	public void mirrorScrollUsesOneEighthAdjustedDropPipeline() {
		TestGuard guard = new TestGuard(null);

		assertEquals(1f / 8f, guard.lootChance(), 0f);
		assertEquals(1f / 8f, guard.adjustedBaseChance, 0f);
		assertSame(ScrollOfMirrorImage.class, guard.lootClassForTest());
	}

	@Test
	public void armorCombatStatsAndProcUseGuardAsEquipmentOwner() {
		RecordingArmor armor = new RecordingArmor();
		TestGuard guard = new TestGuard(new FixedSource(null, armor, 18, 0));
		Char attacker = new Char() {
		};
		guard.refreshForTest();

		assertEquals(54, guard.defenseSkill(attacker));
		assertEquals(5, guard.drRoll());
		assertEquals(3f, guard.speed(), 0f);
		assertEquals(26, guard.defenseProc(attacker, 10, DamageTag.MELEE));
		assertSame(guard, armor.evasionOwner);
		assertSame(attacker, armor.procAttacker);
		assertSame(guard, armor.procDefender);
	}

	@Test
	public void forceCanEnhanceUnarmedDamageWithoutChangingFixedHealth() {
		TestGuard guard = new TestGuard(new FixedSource(null, null, 18, 2));
		guard.refreshForTest();

		int damage = guard.unarmedDamageForTest();
		assertTrue(damage >= 7 && damage <= 42);
		assertEquals(100, guard.HT);
		assertEquals(100, guard.HP);
	}

	@Test
	public void firstSightSplitsForFreeBeforeNormalAction() {
		TestGuard guard = new TestGuard(null);
		guard.heroVisible = true;
		guard.splitCell = 11;

		assertTrue(guard.actForTest());
		assertEquals(1, guard.spawned.size());
		assertEquals(1, guard.baseActCalls);
		assertFalse(guard.firstSplitPendingForTest());
		assertEquals(0, guard.splitChargeForTest());
		assertSame(guard.spawned.get(0).HUNTING, guard.spawned.get(0).state);
	}

	@Test
	public void blockedFirstSplitRetriesWhileStillActingNormally() {
		TestGuard guard = new TestGuard(null);
		guard.heroVisible = true;
		guard.splitCell = -1;

		guard.actForTest();
		guard.actForTest();
		assertEquals(0, guard.spawned.size());
		assertEquals(2, guard.baseActCalls);
		assertTrue(guard.firstSplitPendingForTest());

		guard.splitCell = 11;
		guard.actForTest();
		assertEquals(1, guard.spawned.size());
		assertEquals(3, guard.baseActCalls);
		assertFalse(guard.firstSplitPendingForTest());
	}

	@Test
	public void recurringSplitUsesFiveActionableTurnsAndOwnsSuccessfulTurn() {
		TestGuard guard = guardAfterFirstSplit();

		for (int turn = 1; turn <= 4; turn++) {
			guard.actForTest();
			assertEquals(turn, guard.splitChargeForTest());
		}
		assertEquals(5, guard.baseActCalls);

		guard.actForTest();
		assertEquals(2, guard.spawned.size());
		assertEquals(5, guard.baseActCalls);
		assertEquals(0, guard.splitChargeForTest());
	}

	@Test
	public void blockedRecurringSplitStaysFullButContinuesNormalActions() {
		TestGuard guard = guardAfterFirstSplit();
		for (int i = 0; i < 4; i++) {
			guard.actForTest();
		}
		guard.splitCell = -1;

		guard.actForTest();
		guard.actForTest();
		assertEquals(1, guard.spawned.size());
		assertEquals(7, guard.baseActCalls);
		assertEquals(5, guard.splitChargeForTest());

		guard.splitCell = 12;
		guard.actForTest();
		assertEquals(2, guard.spawned.size());
		assertEquals(7, guard.baseActCalls);
		assertEquals(0, guard.splitChargeForTest());
	}

	@Test
	public void sleepingAndParalysisDoNotAdvanceOrRetrySplit() {
		TestGuard guard = guardAfterFirstSplit();
		guard.actForTest();
		assertEquals(1, guard.splitChargeForTest());

		guard.state = guard.SLEEPING;
		guard.actForTest();
		assertEquals(1, guard.splitChargeForTest());
		guard.state = guard.WANDERING;

		guard.paralysed = 1;
		guard.actForTest();
		assertEquals(1, guard.splitChargeForTest());
		guard.paralysed = 0;

		guard.actForTest();
		assertEquals(2, guard.splitChargeForTest());
	}

	@Test
	public void failedSceneAddDoesNotCommitFirstOrRecurringSplit() {
		TestGuard guard = new TestGuard(null);
		guard.heroVisible = true;
		guard.splitCell = 11;
		guard.addSucceeds = false;
		guard.actForTest();
		assertTrue(guard.firstSplitPendingForTest());
		assertEquals(0, guard.spawned.size());

		guard.addSucceeds = true;
		guard.actForTest();
		for (int i = 0; i < 4; i++) {
			guard.actForTest();
		}
		guard.addSucceeds = false;
		guard.actForTest();
		assertEquals(5, guard.splitChargeForTest());
		assertEquals(1, guard.spawned.size());
	}

	@Test
	public void splitStateRoundTripAndCorruptValuesAreNormalized() {
		TestGuard source = guardAfterFirstSplit();
		source.actForTest();
		source.actForTest();
		Bundle stored = new Bundle();
		source.storeInBundle(stored);

		TestGuard restored = new TestGuard(null);
		restored.restoreFromBundle(stored);
		assertTrue(restored.heroSeenForTest());
		assertFalse(restored.firstSplitPendingForTest());
		assertEquals(2, restored.splitChargeForTest());

		stored.put("split_charge", 99);
		restored.restoreFromBundle(stored);
		assertEquals(5, restored.splitChargeForTest());
		stored.put("split_charge", -7);
		restored.restoreFromBundle(stored);
		assertEquals(0, restored.splitChargeForTest());

		stored.put("first_split_pending", true);
		stored.put("split_charge", 4);
		restored.restoreFromBundle(stored);
		assertTrue(restored.firstSplitPendingForTest());
		assertEquals(0, restored.splitChargeForTest());
	}

	@Test
	public void summoningUsesFiniteBurstWithoutPersistentMirrorParticles() throws IOException {
		String guardSprite = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/"
						+ "AlienatedPrismaticGuardSprite.java");
		String mirrorSprite = sourceFile(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/tmobs/"
						+ "TwistedMirrorSprite.java");

		assertTrue(guardSprite.contains("burst(ShadowParticle.CURSE, 6)"));
		assertFalse(mirrorSprite.contains("pour(ShadowParticle.CURSE"));
	}

	private static String sourceFile(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		return new String(Files.readAllBytes(coreDirectory.resolve(relativePath)),
				StandardCharsets.UTF_8);
	}

	private static TestGuard guardAfterFirstSplit() {
		TestGuard guard = new TestGuard(null);
		guard.heroVisible = true;
		guard.splitCell = 11;
		guard.actForTest();
		return guard;
	}

	private static final class TestGuard extends AlienatedPrismaticGuard {

		private final HeroEquipmentReplica.EquipmentSource source;
		private float adjustedBaseChance;
		private boolean heroVisible;
		private int splitCell = -1;
		private boolean addSucceeds = true;
		private int baseActCalls;
		private final ArrayList<TwistedMirror> spawned = new ArrayList<>();

		private TestGuard(HeroEquipmentReplica.EquipmentSource source) {
			this.source = source;
			state = WANDERING;
		}

		@Override
		protected HeroEquipmentReplica.EquipmentSource equipmentSource() {
			return source;
		}

		@Override
		protected float adjustedLootChance(float baseChance) {
			adjustedBaseChance = baseChance;
			return baseChance;
		}

		@Override
		protected float evasionRingMultiplier() {
			return source == null ? 1f : 2f;
		}

		@Override
		protected float hasteRingMultiplier() {
			return 1f;
		}

		@Override
		protected int baseDrRoll() {
			return 0;
		}

		@Override
		protected int processBaseDefenseProc(Char enemy, int damage, DamageTag... tags) {
			return damage + 3;
		}

		@Override
		protected boolean canSeeHeroForSplit() {
			return heroVisible;
		}

		@Override
		protected int findSplitCell() {
			return splitCell;
		}

		@Override
		protected boolean addTwistedMirrorToLevel(TwistedMirror mirror, int cell) {
			if (!addSucceeds) {
				return false;
			}
			spawned.add(mirror);
			return true;
		}

		@Override
		protected boolean performBaseAct() {
			baseActCalls++;
			spend(TICK);
			return true;
		}

		private void refreshForTest() {
			refreshReplica();
		}

		private int unarmedDamageForTest() {
			return unarmedDamageRoll();
		}

		private Object lootClassForTest() {
			return loot;
		}

		private boolean actForTest() {
			return act();
		}

		private boolean heroSeenForTest() {
			return heroSeen();
		}

		private boolean firstSplitPendingForTest() {
			return firstSplitPending();
		}

		private int splitChargeForTest() {
			return splitCharge();
		}
	}

	private static final class FixedSource implements HeroEquipmentReplica.EquipmentSource {

		private final KindOfWeapon weapon;
		private final Armor armor;
		private final int strength;
		private final int force;

		private FixedSource(KindOfWeapon weapon, Armor armor, int strength, int force) {
			this.weapon = weapon;
			this.armor = armor;
			this.strength = strength;
			this.force = force;
		}

		@Override
		public KindOfWeapon weapon() {
			return weapon;
		}

		@Override
		public Armor armor() {
			return armor;
		}

		@Override
		public int strength() {
			return strength;
		}

		@Override
		public int ringBonus(Class<? extends Ring.RingBuff> type, boolean buffed) {
			return type == RingOfForce.Force.class ? force : 0;
		}
	}

	private static final class RecordingArmor extends Armor {

		private Char evasionOwner;
		private Char procAttacker;
		private Char procDefender;

		private RecordingArmor() {
			super(4);
		}

		@Override
		public float replicaEvasionFactor(Char owner, float evasion) {
			evasionOwner = owner;
			return evasion + 7f;
		}

		@Override
		public int replicaDRMin() {
			return 5;
		}

		@Override
		public int replicaDRMax() {
			return 5;
		}

		@Override
		public float replicaSpeedFactor(Char owner, float speed) {
			return speed * 3f;
		}

		@Override
		public int proc(Char attacker, Char defender, int damage) {
			procAttacker = attacker;
			procDefender = defender;
			return damage * 2;
		}
	}
}
