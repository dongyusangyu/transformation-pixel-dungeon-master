package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;

import org.junit.After;
import org.junit.Test;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SnipersMarkActionLifecycleTest {

	@After
	public void clearStaticState() {
		Dungeon.hero = null;
		ActionIndicator.action = null;
	}

	@Test
	public void attachedSnipersMarkRegistersFollowupAction() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;

		SnipersMark mark = new SnipersMark();

		assertTrue(mark.attachTo(hero));
		assertSame(mark, ActionIndicator.action);
		assertTrue(ActionIndicator.canShowAction(mark));
	}

	@Test
	public void detachedSnipersMarkClearsFollowupAction() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;

		SnipersMark mark = new SnipersMark();
		assertTrue(mark.attachTo(hero));

		mark.detach();

		assertFalse(ActionIndicator.canShowAction(mark));
		assertNull(ActionIndicator.action);
	}

	private static Hero emptyHero() throws Exception {
		Hero hero = (Hero) unsafe().allocateInstance(Hero.class);
		Belongings belongings = (Belongings) unsafe().allocateInstance(Belongings.class);
		Bag backpack = (Bag) unsafe().allocateInstance(Belongings.Backpack.class);
		setField(Bag.class, backpack, "items", new java.util.ArrayList<>());
		setField(Belongings.class, belongings, "backpack", backpack);
		setField(Hero.class, hero, "belongings", belongings);
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
