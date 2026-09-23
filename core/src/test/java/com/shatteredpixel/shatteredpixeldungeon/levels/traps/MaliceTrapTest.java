package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MaliceTrapTest {

    private Application previousApplication;
    private Files previousFiles;

    @Before
    public void installHeadlessLibGDX() {
        previousApplication = Gdx.app;
        previousFiles = Gdx.files;
        Gdx.app = mock(Application.class);
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
                new Class<?>[]{Files.class}, (proxy, method, args) -> {
                    if (method.getReturnType() == FileHandle.class) {
                        File assetRoot = new File("core/src/main/assets");
                        if (!assetRoot.isDirectory()) assetRoot = new File("src/main/assets");
                        return new FileHandle(new File(assetRoot, (String) args[0]));
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    @After
    public void restoreLibGDX() {
        Gdx.app = previousApplication;
        Gdx.files = previousFiles;
    }

    @Test
    public void usesNormalVioletDotTrapRules() {
        MaliceTrap trap = new MaliceTrap();

        assertFalse(trap.preservesTerrain());
        assertFalse(trap.triggersOnEntry());
        assertTrue(trap.canBeHidden);
        assertTrue(trap.canBeSearched);
        assertTrue(trap.color == Trap.VIOLET);
        assertTrue(trap.shape == Trap.DOTS);
    }

    @Test
    public void cursesAllEquippedItemsButNotBackpackItems() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            TestLevel level = new TestLevel();
            level.setSize(5, 5);
            level.heroFOV = new boolean[level.length()];
            level.traps = new SparseArray<>();
            level.plants = new SparseArray<>();
            level.heaps = new SparseArray<>();
            level.blobs = new HashMap<>();
            Dungeon.level = level;

            Hero hero = TestHeroFactory.create();
            Dungeon.hero = hero;
            hero.pos = 12;

            TestEquipable[] equipped = new TestEquipable[6];
            for (int i = 0; i < equipped.length; i++) {
                equipped[i] = new TestEquipable(true);
                hero.belongings.backpack.items.add(equipped[i]);
            }
            TestEquipable backpackItem = new TestEquipable(false);
            hero.belongings.backpack.items.add(backpackItem);

            assertEquals(6, MaliceTrap.curseEquippedItems(hero));

            for (TestEquipable item : equipped) {
                assertTrue(item.cursed && item.cursedKnown);
            }
            assertFalse(backpackItem.cursed);
            assertFalse(backpackItem.cursedKnown);
        } finally {
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void appliesCursingTrapEffectsToEquippedWeaponAndArmor() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            TestLevel level = testLevel();
            Hero hero = TestHeroFactory.create();
            Dungeon.hero = hero;
            hero.pos = 12;

            Shortsword weapon = TestHeroFactory.allocateItem(Shortsword.class);
            LeatherArmor armor = TestHeroFactory.allocateItem(LeatherArmor.class);
            hero.belongings.weapon = weapon;
            hero.belongings.armor = armor;

            new MaliceTrap().set(12).activate();

            assertTrue(weapon.cursed && weapon.cursedKnown);
            assertTrue(weapon.hasCurseEnchant());
            assertTrue(armor.cursed && armor.cursedKnown);
            assertTrue(armor.hasCurseGlyph());
        } finally {
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void doesNotCurseHeroWhenHeroIsNotOnTrapCell() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            TestLevel level = new TestLevel();
            level.setSize(5, 5);
            level.heroFOV = new boolean[level.length()];
            level.traps = new SparseArray<>();
            level.plants = new SparseArray<>();
            level.heaps = new SparseArray<>();
            level.blobs = new HashMap<>();
            Dungeon.level = level;

            Hero hero = TestHeroFactory.create();
            Dungeon.hero = hero;
            hero.pos = 13;
            Shortsword weapon = TestHeroFactory.allocateItem(Shortsword.class);
            hero.belongings.weapon = weapon;

            new MaliceTrap().set(12).activate();

            assertFalse(weapon.cursed);
            assertFalse(weapon.cursedKnown);
        } finally {
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void cursesCurseableEquipmentOnTrapWhenThrownByHero() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            TestLevel level = testLevel();
            Hero hero = TestHeroFactory.create();
            Dungeon.hero = hero;
            hero.pos = 13;

            Shortsword weapon = TestHeroFactory.allocateItem(Shortsword.class);
            Heap heap = new Heap();
            heap.pos = 12;
            heap.drop(weapon);
            level.heaps.put(12, heap);

            new MaliceTrap().set(12).activate();

            assertTrue(weapon.cursed);
            assertTrue(weapon.cursedKnown);
            assertTrue(((Weapon) weapon).hasCurseEnchant());
        } finally {
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void doesNotCurseMissileWeaponsOnTrap() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            TestLevel level = testLevel();
            Hero hero = TestHeroFactory.create();
            Dungeon.hero = hero;
            hero.pos = 13;

            Dart dart = TestHeroFactory.allocateItem(Dart.class);
            Heap heap = new Heap();
            heap.pos = 12;
            heap.drop(dart);
            level.heaps.put(12, heap);

            new MaliceTrap().set(12).activate();

            assertFalse(dart.cursed);
            assertFalse(dart.cursedKnown);
        } finally {
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    @Test
    public void preservesExistingEnchantmentWhileAddingCurseState() {
        Level previousLevel = Dungeon.level;
        Hero previousHero = Dungeon.hero;
        try {
            TestLevel level = testLevel();
            Hero hero = TestHeroFactory.create();
            Dungeon.hero = hero;
            hero.pos = 13;

            Shortsword weapon = TestHeroFactory.allocateItem(Shortsword.class);
            Weapon.Enchantment enchantment = new Blazing();
            weapon.enchant(enchantment);
            Heap heap = new Heap();
            heap.pos = 12;
            heap.drop(weapon);
            level.heaps.put(12, heap);

            new MaliceTrap().set(12).activate();

            assertTrue(weapon.cursed);
            assertTrue(weapon.cursedKnown);
            assertSame(enchantment, weapon.enchantment);
        } finally {
            Dungeon.level = previousLevel;
            Dungeon.hero = previousHero;
        }
    }

    private static TestLevel testLevel() {
        TestLevel level = new TestLevel();
        level.setSize(5, 5);
        level.heroFOV = new boolean[level.length()];
        level.traps = new SparseArray<>();
        level.plants = new SparseArray<>();
        level.heaps = new SparseArray<>();
        level.blobs = new HashMap<>();
        Dungeon.level = level;
        return level;
    }

    private static class TestLevel extends Level {
        @Override protected boolean build() { return true; }
        @Override protected void createMobs() {}
        @Override protected void createItems() {}
    }

    private static class TestEquipable extends EquipableItem {
        private final boolean equipped;

        private TestEquipable(boolean equipped) {
            this.equipped = equipped;
        }

        @Override
        public boolean isEquipped(Hero hero) {
            return equipped;
        }

        @Override
        public boolean doEquip(Hero hero) {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T mock(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getReturnType() == Preferences.class) return mock(Preferences.class);
                    return defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        return null;
    }
}
