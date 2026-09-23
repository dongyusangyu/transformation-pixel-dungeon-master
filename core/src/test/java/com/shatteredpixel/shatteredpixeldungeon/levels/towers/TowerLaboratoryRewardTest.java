package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.LaboratoryRoom;
import com.watabou.utils.Point;
import com.watabou.utils.SparseArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TowerLaboratoryRewardTest {

	private Level previousLevel;
	private Files previousFiles;

	@Before
	public void rememberDungeonLevel() {
		GdxNativesLoader.load();
		previousLevel = Dungeon.level;
		previousFiles = Gdx.files;
		if (Gdx.files == null) Gdx.files = new HeadlessFiles();
		Dungeon.level = null;
	}

	@After
	public void restoreDungeonLevel() {
		Dungeon.level = previousLevel;
		Gdx.files = previousFiles;
	}

	@Test
	public void missingTowerLaboratoryRewardsAreAdded() {
		TestFixture fixture = new TestFixture();

		TowerGenerationRules.ensureLaboratoryRewards(
				fixture.level, Collections.<Room>singletonList(fixture.room));

		assertEquals(5, fixture.quantityOf(EnergyCrystal.class));
		assertTrue(fixture.hasNonStrengthPotion());
	}

	@Test
	public void existingTowerLaboratoryRewardsAreNotDuplicated() {
		TestFixture fixture = new TestFixture();
		fixture.dropInRoom(new EnergyCrystal().quantity(5));
		fixture.dropInRoom(new PotionOfHealing());

		TowerGenerationRules.ensureLaboratoryRewards(
				fixture.level, Collections.<Room>singletonList(fixture.room));
		TowerGenerationRules.ensureLaboratoryRewards(
				fixture.level, Collections.<Room>singletonList(fixture.room));

		assertEquals(5, fixture.quantityOf(EnergyCrystal.class));
		assertEquals(1, fixture.potionCount());
	}

	@Test
	public void strengthPotionDoesNotSatisfyTowerLaboratoryPotionReward() {
		TestFixture fixture = new TestFixture();
		fixture.dropInRoom(new PotionOfStrength());

		TowerGenerationRules.ensureLaboratoryRewards(
				fixture.level, Collections.<Room>singletonList(fixture.room));

		assertTrue(fixture.hasNonStrengthPotion());
		assertFalse(fixture.onlyHasStrengthPotions());
	}

	private static class TestFixture {
		final TestLevel level = new TestLevel();
		final LaboratoryRoom room = new LaboratoryRoom();
		int nextDropCell;

		TestFixture() {
			level.setSize(10, 10);
			level.heaps = new SparseArray<>();
			room.set(1, 1, 7, 7);
			for (Point point : room.itemPlaceablePoints(level)) {
				level.map[level.pointToCell(point)] = Terrain.EMPTY_SP;
			}
			nextDropCell = level.pointToCell(new Point(2, 2));
		}

		void dropInRoom(Item item) {
			while (level.heaps.get(nextDropCell) != null) {
				nextDropCell++;
			}
			level.drop(item, nextDropCell++);
		}

		int quantityOf(Class<? extends Item> type) {
			int quantity = 0;
			for (Heap heap : level.heaps.valueList()) {
				if (!room.inside(level.cellToPoint(heap.pos))) continue;
				for (Item item : heap.items) {
					if (type.isInstance(item)) quantity += item.quantity();
				}
			}
			return quantity;
		}

		int potionCount() {
			int count = 0;
			for (Heap heap : level.heaps.valueList()) {
				if (!room.inside(level.cellToPoint(heap.pos))) continue;
				for (Item item : heap.items) {
					if (item instanceof Potion) count++;
				}
			}
			return count;
		}

		boolean hasNonStrengthPotion() {
			for (Heap heap : level.heaps.valueList()) {
				if (!room.inside(level.cellToPoint(heap.pos))) continue;
				for (Item item : heap.items) {
					if (item instanceof Potion && !(item instanceof PotionOfStrength)) {
						return true;
					}
				}
			}
			return false;
		}

		boolean onlyHasStrengthPotions() {
			return potionCount() > 0 && !hasNonStrengthPotion();
		}
	}

	private static class TestLevel extends Level {
		@Override protected boolean build() { return true; }
		@Override protected void createMobs() { }
		@Override protected void createItems() { }
	}

	private static class HeadlessFiles implements Files {
		private FileHandle asset(String path) {
			java.io.File root = new java.io.File("core/src/main/assets");
			if (!root.isDirectory()) root = new java.io.File("src/main/assets");
			return new FileHandle(new java.io.File(root, path));
		}

		@Override public FileHandle getFileHandle(String path, FileType type) { return asset(path); }
		@Override public FileHandle classpath(String path) { return asset(path); }
		@Override public FileHandle internal(String path) { return asset(path); }
		@Override public FileHandle external(String path) { return asset(path); }
		@Override public FileHandle absolute(String path) { return asset(path); }
		@Override public FileHandle local(String path) { return asset(path); }
		@Override public String getExternalStoragePath() { return ""; }
		@Override public boolean isExternalStorageAvailable() { return false; }
		@Override public String getLocalStoragePath() { return "."; }
		@Override public boolean isLocalStorageAvailable() { return true; }
	}
}
