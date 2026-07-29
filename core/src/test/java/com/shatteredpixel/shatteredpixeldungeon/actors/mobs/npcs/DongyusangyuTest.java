package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DongyusangyuSprite;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DongyusangyuTest {

	@Test
	public void usesRequestedSpriteAndCannotMove() {
		Dongyusangyu npc = new Dongyusangyu();

		assertEquals(DongyusangyuSprite.class, npc.spriteClass);
		assertTrue(npc.properties().contains(Char.Property.IMMOVABLE));
	}

	@Test
	public void hasInfiniteEvasionAndIgnoresAllDamage() {
		Dongyusangyu npc = new Dongyusangyu();
		int initialHp = npc.HP;

		assertEquals(Char.INFINITE_EVASION, npc.defenseSkill(null));
		npc.damage(Integer.MAX_VALUE, new Object());
		assertEquals(initialHp, npc.HP);
	}

	@Test
	public void rejectsBuffs() {
		Dongyusangyu npc = new Dongyusangyu();

		assertFalse(new Buff().attachTo(npc));
		assertTrue(npc.buffs().isEmpty());
	}
}
