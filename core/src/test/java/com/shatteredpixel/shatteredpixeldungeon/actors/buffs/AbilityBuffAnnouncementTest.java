package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Blowpipe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RoundShield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scimitar;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WalkStick;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.AuxiliaryCore;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.MercuryBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.MountainGuard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.tier6.SoulBlade;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbilityBuffAnnouncementTest {

	@Test
	public void weaponAbilityBuffsDoNotRepeatTheAbilityPopupInGreen() {
		assertFalse(new Quarterstaff.DefensiveStance().announced);
		assertFalse(new WalkStick.DefensiveStance().announced);
		assertFalse(new Scimitar.SwordDance().announced);
		assertFalse(new RoundShield.GuardTracker().announced);
		assertFalse(new Crossbow.ChargedShot().announced);
		assertFalse(new Blowpipe.WeakChargedShot().announced);
		assertFalse(new AuxiliaryCore.MagicPowerBoost().announced);
		assertFalse(new MercuryBlade.MercurySolidification().announced);
		assertFalse(new MountainGuard.MountainWallCounter().announced);
		assertFalse(new SoulBlade.DuskBuff().announced);
	}

	@Test
	public void forceStanceAndAlternatingWeaponsDoNotAnnounceGreenText() {
		assertFalse(new RingOfForce.BrawlersStance().announced);
		assertFalse(new AlternatingWeapons().announced);
		assertFalse(new SkilledParry().announced);
	}

	@Test
	public void alternatingWeaponsShowsGreenTextOnEachSuccessfulTrigger() throws Exception {
		String source = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/buffs/AlternatingWeapons.java");
		String trigger = source.substring(source.indexOf("public static void trigger(Hero hero)"),
				source.indexOf("@Override", source.indexOf("public static void trigger(Hero hero)")));

		assertTrue(trigger.contains("hero.sprite.showStatus(CharSprite.POSITIVE"));
		assertTrue(trigger.contains("Messages.titleCase(refreshed.name())"));
	}

	@Test
	public void ringOfForceAndSkilledParryUseTheYellowAbilityFeedbackColor() throws Exception {
		String ringSource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/items/rings/RingOfForce.java");
		String parrySource = readCoreSource(
				"com/shatteredpixel/shatteredpixeldungeon/actors/buffs/SkilledParry.java");

		assertTrue(ringSource.contains("showBrawlerAbilityStatus(hero)"));
		assertTrue(ringSource.contains("showStatus(CharSprite.NEUTRAL, Messages.get(this, \"ability_name\"))"));
		assertTrue(parrySource.contains("showStatus(CharSprite.NEUTRAL, Messages.titleCase(parry.name()))"));
	}

	private static String readCoreSource(String relativePath) throws Exception {
		Path working = Paths.get(System.getProperty("user.dir"));
		Path core = Files.isDirectory(working.resolve("core"))
				? working.resolve("core") : working;
		Path source = core.resolve("src/main/java").resolve(relativePath);
		return Files.readString(source, StandardCharsets.UTF_8);
	}
}
