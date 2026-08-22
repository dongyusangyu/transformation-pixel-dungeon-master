package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FightStance;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

import org.junit.After;
import org.junit.Test;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class FightStanceActionLifecycleTest {

	@After
	public void clearStaticState() {
		Dungeon.hero = null;
		ActionIndicator.action = null;
	}

	@Test
	public void detachedFightStanceCannotRemainAsTheCurrentAction() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;

		FightStance staleStance = new FightStance();
		staleStance.target = hero;
		ActionIndicator.action = staleStance;

		staleStance.detach();

		assertFalse(ActionIndicator.canShowAction(staleStance));
		assertNull(ActionIndicator.action);
	}

	private static Hero emptyHero() throws Exception {
		Hero hero = (Hero) unsafe().allocateInstance(Hero.class);
		setField(Char.class, hero, "buffs", new LinkedHashSet<>());
		setField(Char.class, hero, "resistances", new HashSet<>());
		setField(Char.class, hero, "immunities", new HashSet<>());
		setField(Char.class, hero, "properties", new HashSet<>());
		return hero;
	}

	private static void setField(Class<?> declaringClass, Object target,
			String name, Object value) throws Exception {
		Field field = declaringClass.getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static Unsafe unsafe() throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		return (Unsafe) unsafeField.get(null);
	}
}
