package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WandmakerSprite;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DungeonDoctorTest {

	@Test
	public void usesOldWandmakerSpriteAndCannotMove() {
		DungeonDoctor doctor = new DungeonDoctor();

		assertEquals(WandmakerSprite.class, doctor.spriteClass);
		assertTrue(doctor.properties().contains(Char.Property.IMMOVABLE));
	}

	@Test
	public void hasInfiniteEvasionAndIgnoresDamageAndBuffs() {
		DungeonDoctor doctor = new DungeonDoctor();
		int initialHp = doctor.HP;

		assertEquals(Char.INFINITE_EVASION, doctor.defenseSkill(null));
		doctor.damage(Integer.MAX_VALUE, new Object());
		assertEquals(initialHp, doctor.HP);
		assertFalse(new Buff().attachTo(doctor));
		assertTrue(doctor.buffs().isEmpty());
	}

	@Test
	public void resetsAllInvestedPointsOnceWithoutChangingTalentLayout() {
		DungeonDoctor doctor = new DungeonDoctor();
		ArrayList<LinkedHashMap<Talent, Integer>> talents = talentTiers();
		talents.get(0).put(Talent.HEARTY_MEAL, 2);
		talents.get(1).put(Talent.IRON_STOMACH, 1);
		talents.get(2).put(Talent.ENDLESS_RAGE, 3);
		talents.get(3).put(Talent.HEROIC_ENERGY, 2);

		assertEquals(8, doctor.resetTalentPoints(talents));
		assertFalse(doctor.canResetTalentPoints());
		for (LinkedHashMap<Talent, Integer> tier : talents) {
			for (int points : tier.values()) {
				assertEquals(0, points);
			}
		}
		assertTrue(talents.get(0).containsKey(Talent.HEARTY_MEAL));
		assertEquals(0, doctor.resetTalentPoints(talents));
	}

	@Test
	public void doesNotConsumeResetWhenNoPointsWereInvested() {
		DungeonDoctor doctor = new DungeonDoctor();

		assertEquals(0, doctor.resetTalentPoints(talentTiers()));
		assertTrue(doctor.canResetTalentPoints());
	}

	@Test
	public void persistsUsedTalentResetAcrossSaveAndLoad() {
		DungeonDoctor doctor = new DungeonDoctor();
		ArrayList<LinkedHashMap<Talent, Integer>> talents = talentTiers();
		talents.get(0).put(Talent.HEARTY_MEAL, 1);
		assertEquals(1, doctor.resetTalentPoints(talents));

		Bundle bundle = new Bundle();
		doctor.storeInBundle(bundle);
		DungeonDoctor restored = new DungeonDoctor();
		restored.restoreFromBundle(bundle);

		assertFalse(restored.canResetTalentPoints());
	}

	@Test
	public void oldSavesReceiveAnUnusedTalentReset() {
		DungeonDoctor restored = new DungeonDoctor();

		restored.restoreFromBundle(new Bundle());

		assertTrue(restored.canResetTalentPoints());
	}

	private static ArrayList<LinkedHashMap<Talent, Integer>> talentTiers() {
		ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
		for (int i = 0; i < Talent.MAX_TALENT_TIERS; i++) {
			talents.add(new LinkedHashMap<>());
		}
		return talents;
	}
}
