package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WandmakerSprite;

import org.junit.Test;

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
}
