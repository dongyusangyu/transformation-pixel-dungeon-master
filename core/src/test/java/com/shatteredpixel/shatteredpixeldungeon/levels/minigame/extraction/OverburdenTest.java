package com.shatteredpixel.shatteredpixeldungeon.levels.minigame.extraction;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OverburdenTest {

	private static final float EPSILON = 0.0001f;

	@Test
	public void retentionUsesTwoThirdsExponentialDecay() {
		assertEquals(1f, Overburden.retentionForSlots(0), EPSILON);
		assertEquals(2f / 3f, Overburden.retentionForSlots(1), EPSILON);
		assertEquals(8f / 27f, Overburden.retentionForSlots(3), EPSILON);
	}

	@Test
	public void attenuationOnlyReducesPositiveMultipliersTowardOne() {
		assertEquals(1.5f, Overburden.attenuatePositiveMultiplier(2f, 0.5f), EPSILON);
		assertEquals(1f, Overburden.attenuatePositiveMultiplier(1f, 0.1f), EPSILON);
		assertEquals(0.8f, Overburden.attenuatePositiveMultiplier(0.8f, 0.1f), EPSILON);
	}

	@Test
	public void countsOccupiedStacksRecursivelyWithoutCountingBagContainers() {
		Bag backpack = new Bag();
		Item stack = new Item().quantity(99);
		Bag nestedBag = new Bag();
		nestedBag.items.add(new Item().quantity(4));

		backpack.items.add(stack);
		backpack.items.add(new Item());
		backpack.items.add(nestedBag);

		assertEquals(3, Overburden.countSlots(backpack));
	}

	@Test
	public void speedEquipmentEntrypointsUseOverburdenButConsumableSpeedDoesNot() throws IOException {
		String ring = readCoreSource("items/rings/RingOfHaste.java");
		String swiftness = readCoreSource("items/armor/glyphs/Swiftness.java");
		String flow = readCoreSource("items/armor/glyphs/Flow.java");
		String charSource = readCoreSource("actors/Char.java");

		assertTrue(ring.contains("Overburden.attenuateEquipmentSpeed(target, multiplier)"));
		assertTrue(swiftness.contains("Overburden.attenuateEquipmentSpeed(owner, multiplier)"));
		assertTrue(flow.contains("Overburden.attenuateEquipmentSpeed(owner, multiplier)"));
		assertTrue(charSource.contains("if ( buff( Adrenaline.class ) != null) speed *= 2f;"));
		assertTrue(charSource.contains("if ( buff( Haste.class ) != null) speed *= 3f;"));
	}

	@Test
	public void iconChangesAtLightMediumAndHeavyThresholds() {
		assertEquals(BuffIndicator.HASTE, Overburden.iconForRetention(0.5f));
		assertEquals(BuffIndicator.CRIPPLE, Overburden.iconForRetention(0.2f));
		assertEquals(BuffIndicator.TIME, Overburden.iconForRetention(0.199f));
	}

	@Test
	public void waterskinIsExplicitlyExcludedFromSlotCount() throws IOException {
		String source = readCoreSource(
				"levels/minigame/extraction/Overburden.java");
		assertTrue(source.contains("!(item instanceof Waterskin)"));
	}

	@Test
	public void englishAndChineseBuffMessagesArePresent() throws IOException {
		String english = readCoreResource("messages/actors/actors.properties");
		String chinese = readCoreResource("messages/actors/actors_zh.properties");
		String nameKey = "levels.minigame.extraction.overburden.name=";
		String descKey = "levels.minigame.extraction.overburden.desc=";

		assertTrue(english.contains(nameKey));
		assertTrue(english.contains(descKey));
		assertTrue(chinese.contains(nameKey));
		assertTrue(chinese.contains(descKey));
	}

	private static String readCoreSource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
				.resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}

	private static String readCoreResource(String relativePath) throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) {
			coreDirectory = workingDirectory;
		}
		Path source = coreDirectory.resolve("src/main/assets").resolve(relativePath);
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
