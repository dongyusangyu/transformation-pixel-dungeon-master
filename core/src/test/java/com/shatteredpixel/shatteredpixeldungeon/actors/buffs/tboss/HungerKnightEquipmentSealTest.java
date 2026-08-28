package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.tboss;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HungerKnightEquipmentSealTest {

    @Test
    public void onlyTheOwningEncounterCanReleaseTheSeal() {
        Hero hero = TestHeroFactory.create();
        HungerKnightEquipmentSeal seal = HungerKnightEquipmentSeal.attach(hero, 12);
        assertTrue(HungerKnightEquipmentSeal.isActive(hero));
        assertFalse(HungerKnightEquipmentSeal.release(hero, 13));
        assertTrue(HungerKnightEquipmentSeal.isActive(hero));
        assertTrue(HungerKnightEquipmentSeal.release(hero, 12));
        assertFalse(HungerKnightEquipmentSeal.isActive(hero));
    }

    @Test
    public void ownerSurvivesBundleRoundTrip() {
        HungerKnightEquipmentSeal source = new HungerKnightEquipmentSeal();
        source.setOwnerId(44);
        Bundle bundle = new Bundle();
        source.storeInBundle(bundle);
        HungerKnightEquipmentSeal restored = new HungerKnightEquipmentSeal();
        restored.restoreFromBundle(bundle);
        assertEquals(44, restored.ownerId());
    }

    @Test
    public void directMissileAndDartForwardersConsultTheEquipmentSeal() throws Exception {
        Path root = Paths.get(System.getProperty("user.dir"));
        if (!Files.isDirectory(root.resolve("core"))) root = root.getParent();
        String missile = new String(Files.readAllBytes(root.resolve(
                "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/missiles/MissileWeapon.java")),
                StandardCharsets.UTF_8);
        String dart = new String(Files.readAllBytes(root.resolve(
                "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/missiles/darts/Dart.java")),
                StandardCharsets.UTF_8);

        assertTrue(missile.contains("!HungerKnightEquipmentSeal.isActive(attacker)"));
        assertTrue(dart.contains("!HungerKnightEquipmentSeal.isActive(owner)"));
    }
}
