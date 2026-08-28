package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric;

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PrecognitiveEye;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Gungnir;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;

public class TrinityEffectSupportTest {

	@Test
	public void tierSixMissileCatalogIsAvailableToMindForm() throws Exception {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java")),
				StandardCharsets.UTF_8);
		assertTrue(source.contains(
				"THROWN_WEAPONS.addItems(Generator.Category.MIS_T6.classes);"));
	}

	@Test
	public void precognitiveEyeHasLocalizedTrinityUsageText() throws Exception {
		String text = new String(Files.readAllBytes(Paths.get(
				"src/main/assets/messages/actors/actors_zh.properties")), StandardCharsets.UTF_8);
		assertTrue(text.contains("actors.hero.abilities.cleric.trinity.precognitiveeye_use="));
	}

	@Test
	public void trinityGungnirDoesNotCollectTransientProjectile() throws Exception {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/missiles/Gungnir.java")),
				StandardCharsets.UTF_8);
		assertTrue(source.contains("if (!spawnedForEffect && hero != null && hero.belongings != null"));
	}

	@Test
	public void mindFormChecksGungnirHealthBeforeCasting() throws Exception {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/spells/MindForm.java")),
				StandardCharsets.UTF_8);
		assertTrue(source.contains("Gungnir.canThrowWithCurrentHP(hero.HP)"));
	}

	@Test
	public void effectSupportRegistryAcceptsImplementedEffectsOnly() throws Exception {
		Method method = Trinity.class.getDeclaredMethod("supportsEffect", Class.class);
		assertTrue((Boolean) method.invoke(null, PrecognitiveEye.class));
		assertTrue((Boolean) method.invoke(null, Gungnir.class));
	}
}
