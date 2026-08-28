package com.shatteredpixel.shatteredpixeldungeon.testutil;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

/** Constructor-free model Hero for tests which do not have libGDX natives. */
public final class TestHeroFactory {

    private TestHeroFactory() {}

    public static Hero create() {
        try {
            MinimalHero hero = allocate(MinimalHero.class);
            setField(Char.class, hero, "buffs", new LinkedHashSet<Buff>());
            setField(Char.class, hero, "resistances", new HashSet<Class>());
            setField(Char.class, hero, "immunities", new HashSet<Class>());
            setField(Char.class, hero, "properties", new HashSet<Char.Property>());
            hero.talents = new ArrayList<>();

            Belongings belongings = allocate(Belongings.class);
            Belongings.Backpack backpack = allocate(Belongings.Backpack.class);
            backpack.items = new ArrayList<>();
            belongings.backpack = backpack;
            hero.belongings = belongings;
            return hero;
        } catch (Exception e) {
            throw new AssertionError("Unable to build a constructor-free Hero", e);
        }
    }

    public static void addBackpackItem(Hero hero) {
        try {
            hero.belongings.backpack.items.add(allocate(Item.class));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static <T extends Item> T allocateItem(Class<T> type) {
        try {
            return allocate(type);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static void setField(Class<?> owner, Object target, String name, Object value)
            throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (T) ((Unsafe) field.get(null)).allocateInstance(type);
    }

    private static class MinimalHero extends Hero {
        @Override public void updateHT(boolean boostHP) {}
        @Override public boolean isImmune(Class effect) { return false; }
        @Override public float resist(Class effect) { return 1f; }
    }
}
