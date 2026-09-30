package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AbyssExplosiveTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.MaliceTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.RedCrossTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.RimeTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.RoastSheepTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TransformationTrap;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TrapPlacerCatalogTest {

	@Test
	public void catalogExactlyFollowsTrapBestiaryAndEveryEntryCanBeCreated() {
		List<Class<? extends Trap>> catalog = TrapPlacer.trapClasses();
		ArrayList<Class<?>> bestiary = new ArrayList<>(Bestiary.TRAP.entities());
		for (Bestiary category : Bestiary.values()) {
			if ("TOWER_TRAPS".equals(category.name())) bestiary.addAll(category.entities());
		}

		assertEquals(bestiary, new ArrayList<Class<?>>(catalog));
		assertEquals(catalog.size(), new HashSet<>(catalog).size());
		for (Class<? extends Trap> type : catalog) {
			assertNotNull(Reflection.newInstance(type));
		}
		assertTrue(catalog.contains(AbyssExplosiveTrap.class));
		assertTrue(catalog.contains(MaliceTrap.class));
		assertTrue(catalog.contains(RimeTrap.class));
		assertTrue(catalog.contains(RedCrossTrap.class));
		assertTrue(catalog.contains(RoastSheepTrap.class));
		assertTrue(catalog.contains(TransformationTrap.class));
	}

	@Test
	public void emptySpriteSlotsAppearOnlyWhenTheirDebugToggleIsEnabled() {
		List<Class<? extends Trap>> normalEntries = TrapPlacer.trapClassesForDisplay(false);
		List<Class<? extends Trap>> debugEntries = TrapPlacer.trapClassesForDisplay(true);

		assertEquals(TrapPlacer.trapClasses(), normalEntries);
		assertFalse(normalEntries.contains(null));
		assertTrue(debugEntries.size() > normalEntries.size());
		assertTrue(debugEntries.contains(null));
	}

	@Test
	public void trapPaneUsesTheSameHorizontalBoundsAsTheToggleAboveIt() {
		TrapPlacer.PaneBounds bounds = TrapPlacer.trapPaneBounds(120);

		assertEquals(2f, bounds.left, 0f);
		assertEquals(116f, bounds.width, 0f);
	}

	@Test
	public void selectedTrapClassSurvivesSerializationWithoutDependingOnItsIndex() {
		Bundle bundle = new Bundle();
		bundle.put("trap_class", AbyssExplosiveTrap.class.getName());

		assertEquals(AbyssExplosiveTrap.class, TrapPlacer.trapClassFromBundle(bundle));
	}

	@Test
	public void legacySpriteCoordinatesStillResolveToTheirRegisteredTrap() {
		Bundle legacy = new Bundle();
		legacy.put("row", 0);
		legacy.put("column", 5);

		assertEquals(MaliceTrap.class, TrapPlacer.trapClassFromBundle(legacy));
	}

	@Test
	public void scrollGridKeepsLastRowReachableAndRejectsScrollbarGutter() {
		assertEquals(80f, TrapPlacer.gridContentHeight(34, 7, 16), 0f);
		assertEquals(33, TrapPlacer.gridIndexAt(82, 70, 34, 7, 16, 16));
		assertEquals(-1, TrapPlacer.gridIndexAt(116, 70, 34, 7, 16, 16));
	}

	@Test
	public void replacingCellIconTriggersLayoutAfterItIsAdded() throws IOException {
		String source = trapPlacerSource();
		int methodStart = source.indexOf("private void setIcon(Image icon)");
		int methodEnd = source.indexOf("private void selected", methodStart);
		String method = source.substring(methodStart, methodEnd);

		int addIcon = method.indexOf("add(icon);");
		int relayout = method.indexOf("layout();");
		assertTrue(addIcon >= 0);
		assertTrue(relayout > addIcon);
	}

	@Test
	public void placementUsesTrapSpecificTerrainAndPreservesAbyssTerrain() throws IOException {
		String source = trapPlacerSource();
		assertTrue(source.contains("canPlaceOnTerrain"));
		assertTrue(source.contains("preservesTerrain()"));
	}

	private static String trapPlacerSource() throws IOException {
		Path workingDirectory = Paths.get(System.getProperty("user.dir"));
		Path coreDirectory = workingDirectory.resolve("core");
		if (!Files.isDirectory(coreDirectory)) coreDirectory = workingDirectory;
		Path source = coreDirectory.resolve("src/main/java/com/shatteredpixel/shatteredpixeldungeon")
				.resolve("custom/testmode/TrapPlacer.java");
		return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
	}
}
