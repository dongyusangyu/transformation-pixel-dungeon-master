package com.shatteredpixel.shatteredpixeldungeon.items;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.BrokenPackage;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessGameMessages;
import com.shatteredpixel.shatteredpixeldungeon.testutil.HeadlessItemSprites;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.MovieClip.Animation;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.HashSet;

import static org.junit.Assert.*;

public class ItemPresentationRegressionTest {
    private HeadlessGameMessages messages;
    private HeadlessItemSprites sheets;
    private Hero oldHero;
    private Hero oldUser;
    private int oldGold;
    private int oldUpgrades;
    private int oldChallenges;
    private boolean oldSound;
    private Object oldGlobalBadges;
    private final ArrayDeque<Runnable> callbacks = new ArrayDeque<>();

    @Before public void setUp() throws Exception {
        GdxNativesLoader.load();
        sheets = new HeadlessItemSprites();
        messages = new HeadlessGameMessages();
        Application delegate = Gdx.app;
        Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
                new Class<?>[]{Application.class}, (proxy, method, args) -> {
                    if (method.getName().equals("postRunnable")) {
                        callbacks.add((Runnable) args[0]);
                        return null;
                    }
                    return method.invoke(delegate, args);
                });
        oldHero = Dungeon.hero;
        oldGold = Dungeon.gold;
        oldUpgrades = Statistics.upgradesUsed;
        oldChallenges = Dungeon.challenges;
        Dungeon.challenges |= Challenges.TEST_MODE;
        oldSound = Sample.INSTANCE.isEnabled();
        Sample.INSTANCE.enable(false);
        Field user = Item.class.getDeclaredField("curUser");
        user.setAccessible(true);
        oldUser = (Hero) user.get(null);
        Field global = Badges.class.getDeclaredField("global");
        global.setAccessible(true);
        oldGlobalBadges = global.get(null);
        HashSet<Badges.Badge> badges = new HashSet<>();
        badges.add(Badges.Badge.UNLOCK_MAGE);
        global.set(null, badges);
    }

    @After public void tearDown() throws Exception {
        Dungeon.hero = oldHero;
        Dungeon.gold = oldGold;
        Statistics.upgradesUsed = oldUpgrades;
        Dungeon.challenges = oldChallenges;
        Sample.INSTANCE.enable(oldSound);
        Field user = Item.class.getDeclaredField("curUser");
        user.setAccessible(true);
        user.set(null, oldUser);
        Field global = Badges.class.getDeclaredField("global");
        global.setAccessible(true);
        global.set(null, oldGlobalBadges);
        messages.close();
        sheets.close();
    }

    @Test public void sealCooldownUsesUntintedClockAndRestoresComboIcon() throws Exception {
        CountingHero hero = new CountingHero();
        hero.subClass = HeroSubClass.GLADIATOR;
        ClothArmor armor = new ClothArmor();
        armor.affixSeal(new BrokenSeal());
        hero.belongings.armor = armor;
        BrokenSeal.SealComboTracker tracker = new BrokenSeal.SealComboTracker();
        tracker.target = hero;
        assertEquals(BuffIndicator.SEAL_COMBO, tracker.icon());
        Field cooldown = tracker.getClass().getDeclaredField("cooldown");
        cooldown.setAccessible(true);
        cooldown.setInt(tracker, 1);
        assertEquals(BuffIndicator.TIME, tracker.icon());
        assertEquals("1", tracker.iconTextDisplay());
        assertEquals(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.class,
                tracker.getClass().getMethod("tintIcon", com.watabou.noosa.Image.class).getDeclaringClass());
        tracker.act();
        assertEquals(BuffIndicator.SEAL_COMBO, tracker.icon());
        assertEquals("", tracker.iconTextDisplay());
        hero.belongings.armor = null;
        assertEquals(BuffIndicator.NONE, tracker.icon());
    }

    @Test public void brokenPackageShowsExactlyTheGoldItAddsWithoutSpendingAnotherTurn() throws Exception {
        CountingHero hero = new CountingHero();
        RecordingSprite sprite = new RecordingSprite();
        hero.sprite = sprite;
        Dungeon.gold = 20;
        Method effect = BrokenPackage.class.getDeclaredMethod("doEffect", Hero.class);
        effect.setAccessible(true);
        effect.invoke(new BrokenPackage(), hero);
        assertEquals(134, Dungeon.gold);
        assertEquals(1, sprite.statusCalls);
        assertEquals("114", sprite.statusText);
        assertEquals(FloatingText.GOLD, sprite.statusIcon);
        assertEquals(CharSprite.NEUTRAL, sprite.statusColor);
        assertEquals(0f, hero.spent, 0f);
    }

    @Test public void recastCompletesExactlyOnceWithAnimation() throws Exception {
        verifyRecast(true);
    }

    @Test public void recastCompletesExactlyOnceWithoutAnimation() throws Exception {
        verifyRecast(false);
    }

    @Test public void recastWithoutSpriteStillUpgradesAndResumesExactlyOnce() throws Exception {
        CountingHero hero = new CountingHero();
        Dungeon.hero = hero;
        WornShortsword sword = new WornShortsword();
        hero.belongings.weapon = sword;
        Sword target = new Sword();
        invokeRecast(hero, sword, target);
        assertNull(hero.belongings.weapon);
        assertEquals(1, target.level());
        assertEquals(1f, hero.spent, 0f);
        assertEquals(1, hero.busyCalls);
        assertEquals(1, hero.nextCalls);
    }

    @Test public void cancelledRecastDoesNotSpendTimeOrConsumeSword() throws Exception {
        CountingHero hero = new CountingHero();
        WornShortsword sword = new WornShortsword();
        hero.belongings.weapon = sword;
        Field field = WornShortsword.class.getDeclaredField("itemSelector");
        field.setAccessible(true);
        ((WndBag.ItemSelector) field.get(sword)).onSelect(null);
        assertSame(sword, hero.belongings.weapon);
        assertEquals(0f, hero.spent, 0f);
        assertEquals(0, hero.nextCalls);
        assertEquals(0, hero.busyCalls);
    }

    @Test public void brokenPackageWithoutSpriteStillAddsGold() throws Exception {
        CountingHero hero = new CountingHero();
        Dungeon.gold = 0;
        Method effect = BrokenPackage.class.getDeclaredMethod("doEffect", Hero.class);
        effect.setAccessible(true);
        effect.invoke(new BrokenPackage(), hero);
        assertEquals(114, Dungeon.gold);
    }

    private void verifyRecast(boolean animated) throws Exception {
        SPDSettings.charAnimations(animated);
        CountingHero hero = new CountingHero();
        Dungeon.hero = hero;
        RecordingSprite sprite = new RecordingSprite();
        sprite.ch = hero;
        hero.sprite = sprite;
        WornShortsword sword = new WornShortsword();
        hero.belongings.weapon = sword;
        Sword target = new Sword();
        invokeRecast(hero, sword, target);
        assertNull(hero.belongings.weapon);
        assertEquals(1, target.level());
        assertEquals(1f, hero.spent, 0f);
        assertEquals(1, hero.busyCalls);
        assertEquals(0, hero.nextCalls);
        if (animated) {
            assertEquals(1, sprite.operatePlays);
            sprite.onComplete(sprite.operation());
        }
        while (!callbacks.isEmpty()) callbacks.remove().run();
        assertEquals(1, hero.nextCalls);
    }

    private void invokeRecast(Hero hero, WornShortsword sword, Item target) throws Exception {
        Field user = Item.class.getDeclaredField("curUser");
        user.setAccessible(true);
        user.set(null, hero);
        Method recast = WornShortsword.class.getDeclaredMethod("recast", Item.class);
        recast.setAccessible(true);
        recast.invoke(sword, target);
    }

    private static class CountingHero extends Hero {
        int nextCalls;
        int busyCalls;
        float spent;
        @Override public void next() { nextCalls++; }
        @Override public void busy() { busyCalls++; }
        @Override public void spend(float time) { spent += time; }
    }

    private static class RecordingSprite extends CharSprite {
        int statusCalls;
        int statusIcon;
        int statusColor;
        int operatePlays;
        String statusText;
        RecordingSprite() { operate = new Animation(10, false); }
        Animation operation() { return operate; }
        @Override public void play(Animation animation) { if (animation == operate) operatePlays++; }
        @Override public void turnTo(int from, int to) {}
        @Override public void idle() {}
        @Override public Emitter emitter() { return new Emitter(); }
        @Override public void showStatusWithIcon(int color, String text, int icon, Object... args) {
            statusCalls++;
            statusColor = color;
            statusText = text;
            statusIcon = icon;
        }
    }
}
