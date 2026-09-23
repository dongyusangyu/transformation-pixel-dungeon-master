package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.testutil.TestHeroFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NecronomiconSoulBoundTest {

    @Test
    public void unmarkedMobContinuesNormalDeath() {
        Mob enemy = new Rat();
        enemy.alignment = Char.Alignment.ENEMY;
        assertEquals(Necronomicon.SoulBoundDeathResult.CONTINUE_DEATH,
                Necronomicon.resolveSoulBoundDeath(enemy, null));
    }

    @Test
    public void soulBoundMarkerIsRecognizedExactlyOnce() {
        Mob enemy = new Rat();
        Buff.affect(enemy, Necronomicon.SoulBound.class);
        assertTrue(Necronomicon.hasSoulBound(enemy));
    }

    @Test
    public void enthrallingSoulBoundMobClearsTheMarker() {
        Mob target = new Rat();
        Buff.affect(target, Necronomicon.SoulBound.class);

        Buff.affect(target, ScrollOfSirensSong.Enthralled.class);

        assertNull(target.buff(Necronomicon.SoulBound.class));
        assertFalse(target.alignment == Char.Alignment.ENEMY);
    }

    @Test
    public void soulBoundOnConvertedMobDoesNotInterceptDeath() {
        Mob target = new Rat();
        target.alignment = Char.Alignment.ALLY;
        Buff.affect(target, Necronomicon.SoulBound.class);

        assertEquals(Necronomicon.SoulBoundDeathResult.CONTINUE_DEATH,
                Necronomicon.resolveSoulBoundDeath(target, null));
        assertFalse(Necronomicon.hasSoulBound(target));
    }

    @Test
    public void soulBoundOnEnemyStillInterceptsDeath() {
        Mob target = new Rat();
        target.alignment = Char.Alignment.ENEMY;
        Buff.affect(target, Necronomicon.SoulBound.class);

        assertEquals(Necronomicon.SoulBoundDeathResult.INTERCEPTED_DEATH,
                Necronomicon.resolveSoulBoundDeath(target, null));
        assertFalse(Necronomicon.hasSoulBound(target));
    }

    @Test
    public void soulBoundConversionUsesRewardingAllyConversion() throws Exception {
        String source = Files.readString(sourcePath(), StandardCharsets.UTF_8);

        assertTrue(source.contains("AllyBuff.affectAndLoot(mob, Dungeon.hero, Corruption.class)"));
        assertTrue(source.contains("mob.HP = mob.HT;"));
    }

    private static Path sourcePath() {
        Path root = Paths.get("").toAbsolutePath();
        Path core = Files.isDirectory(root.resolve("src/main/java"))
                ? root
                : root.resolve("core");
        return core.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/Necronomicon.java");
    }
}
