package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TriggeredThrowValidationTest {

    @Test
    public void qianfaAndPhantomShotsUseTheCommonThrowCondition() throws Exception {
        String source = readMainSource("items/weapon/missiles/MissileWeapon.java");

        assertTrue(source.contains("canPerformTriggeredThrow(user, enemy"));
        assertTrue(source.contains("canPerformTriggeredThrow(user, enemy, true)"));
    }

    @Test
    public void gungnirProvidesTheConditionUsedByTriggeredThrows() throws Exception {
        String source = readMainSource("items/weapon/missiles/Gungnir.java");

        assertTrue(source.contains("canPerformTriggeredThrow"));
        assertTrue(source.contains("hero.HP = Math.max(1, hero.HP)"));
    }

    private static String readMainSource(String relativePath) throws Exception {
        Path workingDirectory = Paths.get(System.getProperty("user.dir"));
        Path coreDirectory = workingDirectory.resolve("core");
        if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
        Path sourcePath = coreDirectory.resolve(
                "src/main/java/com/shatteredpixel/shatteredpixeldungeon").resolve(relativePath);
        return new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
    }
}
