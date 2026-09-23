package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.badlogic.gdx.Files;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FightStance;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ninja.OneSword;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;

import static org.junit.Assert.assertEquals;

public class MissileWeaponAttackSpeedTest {

    private Hero previousHero;
    private Files previousFiles;

    @Before
    public void setupHeadlessResources() {
        GdxNativesLoader.load();
        previousFiles = Gdx.files;
        Gdx.files = (Files) java.lang.reflect.Proxy.newProxyInstance(
                Files.class.getClassLoader(), new Class[]{Files.class}, (proxy, method, args) -> {
                    if (method.getName().equals("internal")) {
                        File assets = new File("src/main/assets");
                        if (!assets.isDirectory()) assets = new File("core/src/main/assets");
                        return new FileHandle(new File(assets, (String) args[0]));
                    }
                    return null;
                });
    }

    @After
    public void restoreHero() {
        Dungeon.hero = previousHero;
        Gdx.files = previousFiles;
    }

    @Test
    public void rangedDelayIncludesGladiatorAndCombatMasterBonuses() throws Exception {
        Hero hero = heroWithTalents(Talent.RELENTLESS_COMBAT, 2,
                Talent.STANCE_MASTERY, 3);
        previousHero = Dungeon.hero;
        Dungeon.hero = hero;

        Combo combo = com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
                .affect(hero, Combo.class);
        setComboCount(combo, 8);
        FightStance stance = com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff
                .affect(hero, FightStance.class);
        stance.stance = stance.balance;

        float expectedMultiplier = 2f * 1.15f;
        assertEquals(1f / expectedMultiplier,
                MissileWeapon.adjustAttackDelay(hero, 1f), 0.0001f);
    }

    @Test
    public void rangedDelayIncludesCombatMasterSwiftnessGlyph() {
        Hero hero = heroWithTalents();
        previousHero = Dungeon.hero;
        Dungeon.hero = hero;
        LeatherArmor armor = TestHeroFactory.allocateItem(LeatherArmor.class);
        armor.inscribe(new Swiftness());
        hero.belongings.armor = armor;

        assertEquals(1f / 1.15f,
                MissileWeapon.adjustAttackDelay(hero, 1f), 0.0001f);
    }

    @Test
    public void oneSwordSpeedRemainsMeleeOnly() {
        Hero hero = heroWithTalents();
        previousHero = Dungeon.hero;
        Dungeon.hero = hero;
        hero.belongings.weapon = TestHeroFactory.allocateItem(Shortsword.class);
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
                hero, OneSword.OKU_OneSword.class, 10f);

        assertEquals(1f, MissileWeapon.adjustAttackDelay(hero, 1f), 0.0001f);
        assertEquals(1.5f, hero.attackSpeedMultiplier(true), 0.0001f);
    }

    private static Hero heroWithTalents(Object... entries) {
        Hero hero = TestHeroFactory.create();
        for (int i = 0; i < 4; i++) {
            hero.talents.add(new LinkedHashMap<>());
        }
        for (int i = 0; i < entries.length; i += 2) {
            Talent talent = (Talent) entries[i];
            hero.talents.get(talent.tier() - 1)
                    .put(talent, (Integer) entries[i + 1]);
        }
        hero.subClass = HeroSubClass.COMBATMASTER;
        return hero;
    }

    private static void setComboCount(Combo combo, int count) throws Exception {
        Field field = Combo.class.getDeclaredField("count");
        field.setAccessible(true);
        field.setInt(combo, count);
    }
}
