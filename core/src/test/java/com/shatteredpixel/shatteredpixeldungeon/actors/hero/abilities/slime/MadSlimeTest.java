package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.slime;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MadSlimeTest {

    @Test
    public void chargeUseDoesNotAssumeEquippedArmorIsClassArmor() {
        Hero hero = heroWithDeliciousDigestion();
        hero.belongings.armor = new Armor(1);

        assertEquals(90f, new MadSlime().chargeUse(hero), 0.001f);
    }

    @Test
    public void chargeUseDoesNotAssumeAnArmorIsEquipped() {
        Hero hero = heroWithDeliciousDigestion();

        assertEquals(90f, new MadSlime().chargeUse(hero), 0.001f);
    }

    @Test
    public void chargeUseCanUseTheArmorBeingDescribed() throws Exception {
        Hero hero = heroWithDeliciousDigestion();
        ClassArmor describedArmor = new ClassArmor() {
        };
        describedArmor.charge = 100f;

        Method method;
        try {
            method = MadSlime.class.getMethod("chargeUse", Hero.class, ClassArmor.class);
        } catch (NoSuchMethodException e) {
            fail("Armor abilities must accept the ClassArmor being described");
            return;
        }

        float chargeUse = (Float) method.invoke(new MadSlime(), hero, describedArmor);
        assertEquals(90f * 0.84f, chargeUse, 0.001f);
    }

    @Test
    public void classArmorDescriptionPassesTheDisplayedArmorToChargeUse() throws Exception {
        Path sourcePath = Paths.get("core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/armor/ClassArmor.java");
        String source = Files.readString(sourcePath, StandardCharsets.UTF_8);

        assertTrue(source.contains("ability.chargeUse(Dungeon.hero, this)"));
    }

    private static Hero heroWithDeliciousDigestion() {
        Hero hero = TestHeroFactory.create();
        LinkedHashMap<Talent, Integer> tier = new LinkedHashMap<>();
        tier.put(Talent.DELICIOUS_DIGESTION, 1);
        hero.talents.add(tier);
        return hero;
    }
}
