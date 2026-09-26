package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EtherealBody;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import com.watabou.utils.Random;
import com.watabou.utils.GameSettings;

import static org.junit.Assert.*;

/** Behaviour checks for the cooked food and its visible ethereal effect. */
public class SoulRoastMeatTest {
    private static HeadlessItemSprites sprites;
    private static Application oldApp;
    private static Files oldFiles;
    private static Preferences oldPreferences;
    @BeforeClass public static void sheets() throws Exception {
        sprites = new HeadlessItemSprites();
        oldApp = Gdx.app;
        oldFiles = Gdx.files;
        Field field = GameSettings.class.getDeclaredField("prefs");
        field.setAccessible(true);
        oldPreferences = (Preferences) field.get(null);
        Preferences preferences = (Preferences) Proxy.newProxyInstance(Preferences.class.getClassLoader(),
                new Class<?>[]{Preferences.class}, (proxy, method, args) -> {
                    if (method.getName().startsWith("get") && args != null && args.length == 2) return args[1];
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == Preferences.class) return proxy;
                    return null;
                });
        GameSettings.set(preferences);
        Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
                new Class<?>[]{Application.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getType")) return Application.ApplicationType.Desktop;
                    if (method.getName().equals("getPreferences")) return preferences;
                    return null;
                });
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(),
                new Class<?>[]{Files.class}, (proxy, method, args) -> {
                    File root = new File("src/main/assets");
                    if (!root.isDirectory()) root = new File("core/src/main/assets");
                    return new FileHandle(new File(root, (String) args[0]));
                });
    }
    @AfterClass public static void restoreSheets() {
        sprites.close();
        Gdx.app = oldApp;
        Gdx.files = oldFiles;
        GameSettings.set(oldPreferences);
    }

    @Test public void cookedFoodProvides150HungerAndPreservesRequestedCount() {
        Food food = new SoulRoastMeat();
        assertEquals(150f, food.energy, 0.001f);
        SoulRoastMeat output = SoulRoastMeat.cook(new MysteryMeat(), 3);
        assertEquals(3, output.quantity());
    }

    @Test public void etherealBodyBlocksCursedFireWithoutGrantingInvisibility() {
        Gnoll target = new Gnoll();
        EtherealBody body = Buff.affect(target, EtherealBody.class, 10f);
        assertNotNull(body);
        assertEquals(BuffIndicator.XIA, body.icon());
        assertTrue(body.announced);
        assertEquals(10f, body.cooldown(), 0.001f);
        assertTrue(target.isImmune(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedBurning.class));
        assertTrue(target.isImmune(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedFlameDamage.class));
        assertEquals(0, target.invisible);
    }

    @Test public void eatingRestores150HungerAndRemovesCursedBurning() {
        Hero previous = Dungeon.hero;
        try {
            Hero hero = TestHeroFactory.create();
            hero.heroClass = HeroClass.WARRIOR;
            hero.subClass = HeroSubClass.NONE;
            Dungeon.hero = hero;
            Hunger hunger = Buff.affect(hero, Hunger.class);
            hunger.level = 300f;
            assertNotNull(Buff.affect(hero, CursedBurning.class));
            new SoulRoastMeat().satisfy(hero);
            assertEquals(150f, hunger.level, 0.001f);
            assertNull(hero.buff(CursedBurning.class));
            assertEquals(10f, hero.buff(EtherealBody.class).cooldown(), 0.001f);
        } finally { Dungeon.hero = previous; }
    }

    @Test public void etherealBodyHalvesActualHitChanceWhenAttackingOrDefending() {
        Hero previous = Dungeon.hero;
        try {
            Dungeon.hero = TestHeroFactory.create();
            Dungeon.hero.heroClass = HeroClass.WARRIOR;
            Dungeon.hero.subClass = HeroSubClass.NONE;
            Gnoll attacker = new Gnoll();
            Gnoll defender = new Gnoll();
            int ordinary = hits(attacker, defender);
            Buff.affect(attacker, EtherealBody.class, 10f);
            int etherealAttack = hits(attacker, defender);
            Buff.detach(attacker, EtherealBody.class);
            Buff.affect(defender, EtherealBody.class, 10f);
            int etherealDefense = hits(attacker, defender);
            assertTrue("accuracy should fall: " + etherealAttack + "/" + ordinary,
                    etherealAttack < ordinary * 0.8f);
            assertTrue("evasion should rise: " + etherealDefense + "/" + ordinary,
                    etherealDefense < ordinary * 0.8f);
        } finally { Dungeon.hero = previous; }
    }

    private static int hits(Gnoll attacker, Gnoll defender) {
        int count = 0;
        Random.pushGenerator(0x51A7);
        try {
            for (int i = 0; i < 512; i++) if (Char.hit(attacker, defender, false)) count++;
        } finally { Random.popGenerator(); }
        return count;
    }

    @Test public void catalogListsSoulMeatImmediatelyAfterFrozenMeat() {
        ArrayList<Class<?>> food = new ArrayList<>(Catalog.FOOD.items());
        assertEquals(FrozenCarpaccio.class, food.get(food.indexOf(SoulRoastMeat.class) - 1));
    }
}
