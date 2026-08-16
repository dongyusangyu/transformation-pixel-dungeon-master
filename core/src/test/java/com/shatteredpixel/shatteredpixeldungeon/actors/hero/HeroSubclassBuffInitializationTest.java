package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DarkHook;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FightStance;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ninja_Energy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Reason;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;

import org.junit.After;
import org.junit.Test;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertNotNull;

public class HeroSubclassBuffInitializationTest {

	@After
	public void resetDungeonHero() {
		Dungeon.hero = null;
	}

	@Test
	public void subclassBuffsAreRestoredThroughOneInitializationEntryPoint() {
		Hero assassin = heroWith(HeroSubClass.ASSASSIN);
		assassin.invisible = 1;
		assassin.ensureSubclassBuffs();
		assertNotNull(assassin.buff(Preparation.class));

		Hero darkSlime = heroWith(HeroSubClass.DARKSLIME);
		darkSlime.ensureSubclassBuffs();
		assertNotNull(darkSlime.buff(DarkHook.class));

		Hero ninjaMaster = heroWith(HeroSubClass.NINJA_MASTER);
		ninjaMaster.ensureSubclassBuffs();
		assertNotNull(ninjaMaster.buff(Ninja_Energy.class));

		Hero monk = heroWith(HeroSubClass.MONK);
		monk.ensureSubclassBuffs();
		assertNotNull(monk.buff(MonkEnergy.class));

		Hero combatMaster = heroWith(HeroSubClass.COMBATMASTER);
		combatMaster.ensureSubclassBuffs();
		assertNotNull(combatMaster.buff(FightStance.class));

		Hero pious = heroWith(HeroSubClass.PIOUS);
		pious.ensureSubclassBuffs();
		assertNotNull(pious.buff(Reason.class));

		Hero champion = heroWith(HeroSubClass.CHAMPION);
		champion.ensureSubclassBuffs();
		assertNotNull(champion.buff(MeleeWeapon.Charger.class));
	}

	private static Hero heroWith(HeroSubClass subClass) {
		try {
			Hero hero = (Hero) unsafe().allocateInstance(Hero.class);
			hero.subClass = subClass;
			hero.talents = new ArrayList<>();
			setField(Char.class, hero, "buffs", new LinkedHashSet<>());
			setField(Char.class, hero, "resistances", new HashSet<>());
			setField(Char.class, hero, "immunities", new HashSet<>());
			setField(Char.class, hero, "properties", new HashSet<>());

			Belongings belongings = (Belongings) unsafe().allocateInstance(Belongings.class);
			Belongings.Backpack backpack =
					(Belongings.Backpack) unsafe().allocateInstance(Belongings.Backpack.class);
			backpack.items = new ArrayList<>();
			backpack.owner = hero;
			belongings.backpack = backpack;
			setField(Belongings.class, belongings, "owner", hero);
			hero.belongings = belongings;
			Dungeon.hero = hero;
			return hero;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
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
