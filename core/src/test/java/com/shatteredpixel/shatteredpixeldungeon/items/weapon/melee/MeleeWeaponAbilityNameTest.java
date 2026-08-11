package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MeleeWeaponAbilityNameTest {

	@Test
	public void completedAbilityShowsYellowNameAboveVisibleHero() throws Exception {
		RecordingSprite sprite = new RecordingSprite();
		sprite.visible = true;
		Hero hero = headlessHero(sprite);

		new TestWeapon().finishAbility(hero);

		assertEquals(CharSprite.NEUTRAL, sprite.color);
		assertEquals("Test Ability", sprite.text);
		assertEquals(1, sprite.calls);
	}

	@Test
	public void completedAbilityDoesNotShowNameWhenHeroIsHidden() throws Exception {
		RecordingSprite sprite = new RecordingSprite();
		sprite.visible = false;
		Hero hero = headlessHero(sprite);

		new TestWeapon().finishAbility(hero);

		assertEquals(0, sprite.calls);
		assertNull(sprite.text);
	}

	private static Hero headlessHero(CharSprite sprite) throws Exception {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		Unsafe unsafe = (Unsafe) unsafeField.get(null);

		Hero hero = (Hero) unsafe.allocateInstance(Hero.class);
		hero.belongings = (Belongings) unsafe.allocateInstance(Belongings.class);
		hero.talents = new ArrayList<>();
		hero.sprite = sprite;

		Field buffs = Char.class.getDeclaredField("buffs");
		buffs.setAccessible(true);
		buffs.set(hero, new LinkedHashSet<>());
		return hero;
	}

	private static class TestWeapon extends MeleeWeapon {

		void finishAbility(Hero hero) {
			afterAbilityUsed(hero);
		}

		@Override
		protected String abilityName() {
			return "Test Ability";
		}
	}

	private static class RecordingSprite extends CharSprite {

		int color;
		String text;
		int calls;

		@Override
		public void showStatus(int color, String text, Object... args) {
			this.color = color;
			this.text = text;
			calls++;
		}
	}
}
