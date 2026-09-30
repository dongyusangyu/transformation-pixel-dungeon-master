package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Fix2WeaponTextTest {

	@Test
	public void bothLanguagesFormatTheNewWeaponValues() throws IOException {
		for (String file : new String[]{"items.properties", "items_zh.properties"}) {
			Properties items = load("src/main/assets/messages/items/" + file);
			String sakura = "items.weapon.melee.tier6.sakurablossomblade.";
			for (String variant : new String[]{"ability_desc", "typical_ability_desc",
					"evolved_ability_desc", "evolved_typical_ability_desc"}) {
				assertFormatted(items, sakura + variant, 5);
			}
			assertFormatted(items, "items.weapon.melee.tier6.chainmace.ability_desc", 22, 39);
			assertFormatted(items, "items.weapon.melee.tier6.chainmace.typical_ability_desc", 22, 39);
			assertFormatted(items, "items.weapon.melee.tier6.chainmace.stats_desc", 12, 46);
			assertFormatted(items, "items.weapon.melee.tier6.palermosword.ability_desc", 12, 24);
			assertFormatted(items, "items.weapon.melee.tier6.palermosword.typical_ability_desc", 12, 24);
			assertTrue(items.containsKey("items.weapon.melee.weaponspecialaction.insufficient_strength"));
		}
	}

	@Test
	public void upgradeRowsHaveLocalizedTitles() throws IOException {
		for (String file : new String[]{"windows.properties", "windows_zh.properties"}) {
			Properties windows = load("src/main/assets/messages/windows/" + file);
			assertTrue(windows.containsKey("windows.wndupgrade.ability_mirror_health"));
			assertTrue(windows.containsKey("windows.wndupgrade.iron_ball_damage"));
			assertTrue(windows.containsKey("windows.wndupgrade.ability_extra_range"));
			assertTrue(windows.containsKey("windows.wndupgrade.feature_blade_shadow_damage"));
		}
	}

	@Test
	public void weaponDescriptionsExposeTheDocumentedDynamicValues() throws IOException {
		for (String file : new String[]{"items.properties", "items_zh.properties"}) {
			Properties items = load("src/main/assets/messages/items/" + file);
			assertFormatted(items, "items.weapon.melee.tier6.mercuryblade.stats_desc", 8, 20);
			assertFormatted(items, "items.weapon.melee.tier6.mercuryblade.typical_stats_desc", 8, 20);
			assertFormatted(items, "items.weapon.melee.tier6.auxiliarycore.stats_desc", 10);
			assertFormatted(items, "items.weapon.melee.tier6.auxiliarycore.typical_stats_desc", 10);
			assertFormatted(items, "items.weapon.melee.tier6.auxiliarycore.ability_desc", 2);
			assertFormatted(items, "items.weapon.melee.tier6.auxiliarycore.typical_ability_desc", 2);
			assertFormatted(items, "items.weapon.melee.tier6.soulblade.stats_desc", 6, 15, "ready", 0);
			assertFormatted(items, "items.weapon.melee.tier6.soulblade.typical_stats_desc", 6, 15, "ready", 0);
			assertFormatted(items, "items.weapon.melee.tier6.radiantgoldhalberd.ability_desc", 7, 28);
			assertFormatted(items, "items.weapon.melee.tier6.radiantgoldhalberd.typical_ability_desc", 7, 28);
			assertFormatted(items, "items.weapon.melee.tier6.oracleterminal.ability_desc", 11, 35);
			assertFormatted(items, "items.weapon.melee.tier6.oracleterminal.typical_ability_desc", 11, 35);
			assertFormatted(items, "items.weapon.melee.tier6.oracleterminal.slash_ability_desc", 2);
			assertFormatted(items, "items.weapon.melee.tier6.oracleterminal.typical_slash_ability_desc", 2);
			assertFormatted(items, "items.weapon.melee.tier6.oracleterminal.thrust_ability_desc", 2);
			assertFormatted(items, "items.weapon.melee.tier6.oracleterminal.typical_thrust_ability_desc", 2);
			assertFormatted(items, "items.weapon.melee.tier6.oracleterminal.scythe_ability_desc", 25);
			assertFormatted(items, "items.weapon.melee.tier6.oracleterminal.typical_scythe_ability_desc", 25);
			assertTrue(items.getProperty("items.weapon.melee.tier6.oracleterminal.unequipped_ability_desc") != null);
			assertTrue(items.getProperty("items.trinkets.twindemoneyes.stats_desc_flame") != null);
			assertTrue(items.getProperty("items.trinkets.twindemoneyes$eyelock.desc_flame") != null);
			assertTrue(items.getProperty("items.trinkets.twindemoneyes$eyelock.desc_laser") != null);
		}
	}

	@Test
	public void mountainGuardAbilityDurationsFormatAsStringsInBothLanguages() throws IOException {
		for (String file : new String[]{"items.properties", "items_zh.properties"}) {
			Properties items = load("src/main/assets/messages/items/" + file);
			assertFormatted(items, "items.weapon.melee.tier6.mountainguard.ability_desc", "3", 3, 16);
			assertFormatted(items, "items.weapon.melee.tier6.mountainguard.typical_ability_desc", "3", 3, 16);
		}
		assertEquals("魔法伤害", load("src/main/assets/messages/items/items_zh.properties")
				.getProperty("items.wands.wandofcursedflame.upgrade_stat_name_1"));
		assertEquals("Magic Damage", load("src/main/assets/messages/items/items.properties")
				.getProperty("items.wands.wandofcursedflame.upgrade_stat_name_1"));
		assertTrue(load("src/main/assets/messages/items/items_zh.properties")
				.getProperty("items.weapon.melee.tier6.mountainguard.ability_desc")
				.contains("直接物理攻击"));
	}

	@Test
	public void twinDemonEyesDescriptionsMatchTheirDynamicValues() throws IOException {
		for (String file : new String[]{"items.properties", "items_zh.properties"}) {
			Properties items = load("src/main/assets/messages/items/" + file);
			assertFormatted(items, "items.trinkets.twindemoneyes.stats_desc_flame", 8);
			assertFormatted(items, "items.trinkets.twindemoneyes$eyelock.desc_flame", 7, 4, 3, 6);
			assertFormatted(items, "items.trinkets.twindemoneyes$eyelock.desc_laser", 8, 48, 5);
		}

		Properties items = load("src/main/assets/messages/items/items_zh.properties");
		assertTrue(items.getProperty("items.trinkets.twindemoneyes.stats_desc_flame")
				.contains("间歇性迸射一发咒焰火球"));
		assertTrue(items.getProperty("items.trinkets.twindemoneyes$eyelock.desc_flame")
				.contains("最近一次被远程武器命中的目标"));
		assertTrue(items.getProperty("items.trinkets.twindemoneyes$eyelock.desc_laser")
				.contains("12格射程"));
		assertTrue(items.getProperty("items.trinkets.twindemoneyes$eyelock.desc_laser")
				.contains("不可闪避"));
	}

	@Test
	public void documentedUpgradeRowsUseSpecificLabels() throws IOException {
		Properties windows = load("src/main/assets/messages/windows/windows_zh.properties");
		assertTrue(windows.getProperty("windows.wndupgrade.ability_knockback_distance").equals("击退"));
		assertTrue(windows.getProperty("windows.wndupgrade.feature_blade_shadow_damage").equals("刀影伤害"));
		assertTrue(windows.getProperty("windows.wndupgrade.feature_proc_chance").equals("减益施加概率"));
		assertTrue(windows.getProperty("windows.wndupgrade.feature_draw_damage").equals("拔剑伤害"));
		assertTrue(windows.getProperty("windows.wndupgrade.feature_draw_range").equals("拔剑射程上限"));
		assertTrue(windows.getProperty("windows.wndupgrade.feature_soul_tear_chance").equals("割裂灵魂概率"));
		assertTrue(windows.getProperty("windows.wndupgrade.feature_release_duration").equals("减益持续时间"));
	}

	private static void assertFormatted(Properties items, String key, Object... values) {
		String pattern = items.getProperty(key);
		assertTrue(key, pattern != null);
		String formatted = String.format(Locale.ROOT, pattern, values);
		for (Object value : values) assertTrue(key, formatted.contains(value.toString()));
		assertFalse(key, formatted.contains("%1$d"));
		assertFalse(key, formatted.contains("%2$d"));
	}

	private static Properties load(String path) throws IOException {
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}
}
