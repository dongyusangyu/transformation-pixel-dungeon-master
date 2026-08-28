package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.utils.Bundle;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DungeonCrossLevelPersistenceTest {
	private Files previousFiles;

	@Before
	public void setUp() {
		previousFiles = Gdx.files;
		if (Gdx.files == null) Gdx.files = new HeadlessFiles();
	}

	@After
	public void tearDown() throws Exception {
		Dungeon.droppedItems = new SparseArray<>();
		heldAllies().clear();
		Gdx.files = previousFiles;
	}

	@Test
	public void towerPendingDropSurvivesBundleRoundTrip() {
		int towerFloor22 = Integer.MIN_VALUE + 22;
		Dungeon.droppedItems = new SparseArray<>();
		ArrayList<Item> items = new ArrayList<>();
		items.add(new PlainItem().quantity(7));
		Dungeon.droppedItems.put(towerFloor22, items);

		Bundle bundle = new Bundle();
		Dungeon.storeDroppedItems(bundle);
		Dungeon.droppedItems = new SparseArray<>();
		Dungeon.restoreDroppedItems(bundle);

		assertEquals(1, Dungeon.droppedItems.get(towerFloor22).size());
		assertEquals(7, Dungeon.droppedItems.get(towerFloor22).get(0).quantity());
	}

	@Test
	public void legacyTowerPendingDropIsRestoredFromNegativeKey() {
		int towerFloor22 = Integer.MIN_VALUE + 22;
		ArrayList<Item> items = new ArrayList<>();
		items.add(new PlainItem().quantity(9));
		Bundle legacy = new Bundle();
		legacy.put("dropped" + towerFloor22, items);

		Dungeon.restoreDroppedItems(legacy);

		assertEquals(9, Dungeon.droppedItems.get(towerFloor22).get(0).quantity());
	}

	@Test
	public void heldAlliesSurviveTransferPayloadRoundTrip() throws Exception {
		Rat rat = new Rat();
		rat.HP = 3;
		heldAllies().add(rat);
		Bundle bundle = new Bundle();

		Mob.storeHeldAllies(bundle);
		heldAllies().clear();
		Mob.restoreHeldAllies(bundle);

		assertEquals(1, heldAllies().size());
		assertTrue(heldAllies().get(0) instanceof Rat);
		assertEquals(3, heldAllies().get(0).HP);
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<Mob> heldAllies() throws Exception {
		Field field = Mob.class.getDeclaredField("heldAllies");
		field.setAccessible(true);
		return (ArrayList<Mob>) field.get(null);
	}

	public static class PlainItem extends Item {
	}

	private static class HeadlessFiles implements Files {
		@Override public FileHandle getFileHandle(String path, FileType type) { return new FileHandle(path); }
		@Override public FileHandle classpath(String path) { return new FileHandle(path); }
		@Override public FileHandle internal(String path) { return new FileHandle("core/src/main/assets/" + path); }
		@Override public FileHandle external(String path) { return new FileHandle(path); }
		@Override public FileHandle absolute(String path) { return new FileHandle(path); }
		@Override public FileHandle local(String path) { return new FileHandle(path); }
		@Override public String getExternalStoragePath() { return ""; }
		@Override public boolean isExternalStorageAvailable() { return true; }
		@Override public String getLocalStoragePath() { return ""; }
		@Override public boolean isLocalStorageAvailable() { return true; }
	}
}
