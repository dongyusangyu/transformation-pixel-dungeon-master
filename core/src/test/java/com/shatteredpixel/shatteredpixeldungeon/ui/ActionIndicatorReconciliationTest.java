package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DarkHook;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FightStance;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;

import org.junit.After;
import org.junit.Test;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import com.watabou.noosa.Visual;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ActionIndicatorReconciliationTest {

	@After
	public void clearStaticState() {
		Dungeon.hero = null;
		ActionIndicator.action = null;
		ActionIndicator1.action = null;
	}

	@Test
	public void primaryActionIsRecoveredAfterColdLoadAttachment() throws Exception {
		Hero hero = emptyHero();
		FightStance stance = new FightStance();

		assertTrue(stance.attachTo(hero));
		assertNull(ActionIndicator.action);

		Dungeon.hero = hero;
		ActionIndicator.reconcileActionState();

		assertSame(stance, ActionIndicator.action);
	}

	@Test
	public void reconciliationPreservesCurrentUsableAction() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		FightStance stance = new FightStance();
		assertTrue(stance.attachTo(hero));
		assertTrue(ActionIndicator.setAction(stance));

		ActionIndicator.reconcileActionState();

		assertSame(stance, ActionIndicator.action);
	}

	@Test
	public void reconciliationClearsActionOwnedByPreviousHero() throws Exception {
		Hero firstHero = emptyHero();
		Dungeon.hero = firstHero;
		FightStance stance = new FightStance();
		assertTrue(stance.attachTo(firstHero));
		assertTrue(ActionIndicator.setAction(stance));

		Dungeon.hero = emptyHero();
		ActionIndicator.reconcileActionState();

		assertNull(ActionIndicator.action);
	}

	@Test
	public void comboWithoutAvailableMoveIsNotCountedAsUsable() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		Combo combo = new Combo();
		assertTrue(combo.attachTo(hero));

		assertEquals(0, ActionIndicator.usableActionCount());
	}

	@Test
	public void darkHookDuringCooldownIsNotCountedAsUsable() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		assertTrue(new DarkHook().attachTo(hero));
		assertTrue(new Talent.DarkHookCooldown().attachTo(hero));

		assertEquals(0, ActionIndicator.usableActionCount());
	}

	@Test
	public void momentumRequiresStacksAndNoActiveRunOrCooldown() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		Momentum momentum = new Momentum();
		assertTrue(momentum.attachTo(hero));
		assertEquals(0, ActionIndicator.usableActionCount());

		setField(Momentum.class, momentum, "momentumStacks", 1);
		assertEquals(1, ActionIndicator.usableActionCount());

		setField(Momentum.class, momentum, "freerunCooldown", 1);
		assertEquals(0, ActionIndicator.usableActionCount());
	}

	@Test
	public void monkEnergyRequiresAtLeastOneEnergyAndNoCooldown() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		MonkEnergy energy = new MonkEnergy();
		assertTrue(energy.attachTo(hero));
		assertEquals(0, ActionIndicator.usableActionCount());

		energy.energy = 1;
		assertEquals(1, ActionIndicator.usableActionCount());

		energy.cooldown = 1;
		assertEquals(0, ActionIndicator.usableActionCount());
	}

	@Test
	public void ninjaEnergyRequiresAtLeastOneEnergy() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		Ninja_Energy energy = new Ninja_Energy();
		assertTrue(energy.attachTo(hero));
		energy.energy = 0;
		assertEquals(0, ActionIndicator.usableActionCount());

		energy.energy = 1;
		assertEquals(1, ActionIndicator.usableActionCount());
	}

	@Test
	public void preparationWhileVisibleIsNotCountedAsUsable() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		Preparation preparation = new Preparation();
		assertTrue(preparation.attachTo(hero));

		assertEquals(0, ActionIndicator.usableActionCount());
	}

	@Test
	public void secondaryActionIsRecoveredAfterColdLoadAttachment() throws Exception {
		Hero hero = emptyHero(Talent.SMOKE_MASK);
		Talent.SmokeMask smokeMask = new Talent.SmokeMask();

		assertTrue(smokeMask.attachTo(hero));
		assertNull(ActionIndicator1.action);

		Dungeon.hero = hero;
		ActionIndicator1.reconcileActionState();

		assertSame(smokeMask, ActionIndicator1.action);
	}

	@Test
	public void secondaryActionIsHiddenDuringCooldown() throws Exception {
		Hero hero = emptyHero(Talent.SMOKE_MASK);
		Dungeon.hero = hero;
		Talent.SmokeMask smokeMask = new Talent.SmokeMask();
		assertTrue(smokeMask.attachTo(hero));
		assertTrue(new Talent.SmokeCooldown().attachTo(hero));

		ActionIndicator1.reconcileActionState();

		assertNull(ActionIndicator1.action);
	}

	@Test
	public void secondaryReconciliationClearsActionOwnedByPreviousHero() throws Exception {
		Hero firstHero = emptyHero(Talent.SMOKE_MASK);
		Dungeon.hero = firstHero;
		Talent.SmokeMask smokeMask = new Talent.SmokeMask();
		assertTrue(smokeMask.attachTo(firstHero));
		ActionIndicator1.setAction(smokeMask);

		Dungeon.hero = emptyHero(Talent.SMOKE_MASK);
		ActionIndicator1.reconcileActionState();

		assertNull(ActionIndicator1.action);
	}

	@Test
	public void primaryClickDoesNotClearValidActionWhileHeroIsBusy() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		TestAction action = new TestAction();
		assertTrue(ActionIndicator.setAction(action));

		((TestActionIndicator) unsafe().allocateInstance(TestActionIndicator.class)).click();

		assertSame(action, ActionIndicator.action);
		assertEquals(0, action.calls);
	}

	@Test
	public void secondaryClickDoesNotClearValidActionWhileHeroIsBusy() throws Exception {
		Hero hero = emptyHero();
		Dungeon.hero = hero;
		TestAction action = new TestAction();
		assertTrue(ActionIndicator1.setAction(action));

		((TestActionIndicator1) unsafe().allocateInstance(TestActionIndicator1.class)).click();

		assertSame(action, ActionIndicator1.action);
		assertEquals(0, action.calls);
	}

	private static class TestAction implements ActionIndicator.Action, ActionIndicator1.Action {
		int calls;

		@Override
		public String actionName() {
			return "test";
		}

		@Override
		public boolean usable() {
			return true;
		}

		@Override
		public Visual secondaryVisual() {
			return null;
		}

		@Override
		public Visual primaryVisual() {
			return null;
		}

		@Override
		public int actionIcon() {
			return HeroIcon.NONE;
		}

		@Override
		public int indicatorColor() {
			return 0;
		}

		@Override
		public void doAction() {
			calls++;
		}
	}

	private static class TestActionIndicator extends ActionIndicator {
		void click() {
			super.onClick();
		}
	}

	private static class TestActionIndicator1 extends ActionIndicator1 {
		void click() {
			super.onClick();
		}
	}

	private static Hero emptyHero(Talent... talents) throws Exception {
		Hero hero = (Hero) unsafe().allocateInstance(Hero.class);
		Belongings belongings = (Belongings) unsafe().allocateInstance(Belongings.class);
		Bag backpack = (Bag) unsafe().allocateInstance(Belongings.Backpack.class);
		setField(Bag.class, backpack, "items", new ArrayList<>());
		setField(Belongings.class, belongings, "backpack", backpack);
		setField(Hero.class, hero, "belongings", belongings);
		setField(Char.class, hero, "buffs", new LinkedHashSet<>());
		setField(Char.class, hero, "resistances", new HashSet<>());
		setField(Char.class, hero, "immunities", new HashSet<>());
		setField(Char.class, hero, "properties", new HashSet<>());

		ArrayList<LinkedHashMap<Talent, Integer>> talentTiers = new ArrayList<>();
		LinkedHashMap<Talent, Integer> firstTier = new LinkedHashMap<>();
		for (Talent talent : talents) firstTier.put(talent, 1);
		talentTiers.add(firstTier);
		setField(Hero.class, hero, "talents", talentTiers);
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
