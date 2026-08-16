package com.shatteredpixel.shatteredpixeldungeon.items;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class Tier6WeaponTextTest {

	private static final String[] GUIDE_KEYS = {
			"melee_greatgreatgreatsword",
			"melee_sakurablossomblade",
			"melee_chainmace",
			"melee_twohandedgreatsword",
			"melee_palermosword",
			"melee_hundredtonhammer",
			"melee_oracleterminal",
			"melee_demontailwhip",
			"melee_mercuryblade",
			"melee_auxiliarycore",
			"missile_gungnir"
	};
	private static final String[] DAMAGING_ABILITY_KEYS = {
			"items.weapon.melee.tier6.chainmace.ability_desc",
			"items.weapon.melee.tier6.twohandedgreatsword.ability_desc",
			"items.weapon.melee.tier6.palermosword.ability_desc"
	};
	private static final String[] CHAIN_MACE_THROW_KEYS = {
			"items.weapon.melee.tier6.chainmace.need_near_hero",
			"items.weapon.melee.tier6.chainmace.wrong_path",
			"items.weapon.melee.tier6.chainmace.invalid_target",
			"items.weapon.melee.tier6.chainmace.stats_desc"
	};

	@Test
	public void everyTierSixWeaponHasARegisteredDetailedGuideEntry() throws Exception {
		String customText = new String(Files.readAllBytes(Paths.get(
				"src/main/assets/messages/custom/custom_zh.properties")),
				StandardCharsets.UTF_8);
		String dictionarySource = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/shatteredpixel/shatteredpixeldungeon/custom/dict/DictionaryJournal.java")),
				StandardCharsets.UTF_8);
		Map<String, String> customProperties = readProperties(customText);

		for (String key : GUIDE_KEYS) {
			assertTrue(key, dictionarySource.contains("WEAPONS.d.put(\"" + key + "\""));
			assertTrue(key, customProperties.containsKey("custom.dict.dict." + key));
			String detail = customProperties.get("custom.dict.dict." + key + "_d");
			assertTrue(key, detail != null);
			assertTrue(key, detail.contains("_6_阶"));
			assertTrue(key, detail.contains("力量需求"));
			assertTrue(key, detail.contains("基础伤害"));
			assertTrue(key, detail.contains("伤害成长"));
			assertTrue(key, detail.contains("精准修正"));
			assertTrue(key, detail.contains("延迟"));
			assertTrue(key, detail.contains("距离"));
			if (key.startsWith("melee_")) {
				assertTrue(key, detail.contains("_决斗家_武技"));
			}
		}
	}

	@Test
	public void damagingMultiTargetAbilitiesExposeDamageRanges() throws Exception {
		String itemText = new String(Files.readAllBytes(Paths.get(
				"src/main/assets/messages/items/items_zh.properties")),
				StandardCharsets.UTF_8);

		Map<String, String> itemProperties = readProperties(itemText);
		for (String key : DAMAGING_ABILITY_KEYS) {
			String ability = itemProperties.get(key);
			assertTrue(key, ability != null);
			assertTrue(key, ability.contains("%1$d"));
			assertTrue(key, ability.contains("%2$d"));
		}
	}

	@Test
	public void chainMaceThrowRulesHaveLocalizedText() throws Exception {
		String itemText = new String(Files.readAllBytes(Paths.get(
				"src/main/assets/messages/items/items_zh.properties")),
				StandardCharsets.UTF_8);
		Map<String, String> itemProperties = readProperties(itemText);
		for (String key : CHAIN_MACE_THROW_KEYS) {
			assertTrue(key, itemProperties.containsKey(key));
			assertTrue(key, !itemProperties.get(key).trim().isEmpty());
		}
		assertTrue(itemProperties.get(
				"items.weapon.melee.tier6.chainmace.stats_desc").contains("四分之一"));
	}

	private Map<String, String> readProperties(String text) {
		Map<String, String> result = new HashMap<>();
		for (String line : text.split("\\R")) {
			int equals = line.indexOf('=');
			if (equals > 0 && !line.startsWith("#")) {
				result.put(line.substring(0, equals).trim(), line.substring(equals + 1));
			}
		}
		return result;
	}
}
