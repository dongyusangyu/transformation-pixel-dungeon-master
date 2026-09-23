package com.shatteredpixel.shatteredpixeldungeon.levels.towers;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TransformationTrap;
import com.watabou.noosa.Game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TowerTransformationTrapTest {
	private Files previousFiles;
	private String previousVersion;

	@Before
	public void prepareAssets() {
		GdxNativesLoader.load();
		previousFiles = Gdx.files;
		previousVersion = Game.version;
		if (Gdx.files == null) Gdx.files = new HeadlessFiles();
		if (Game.version == null) Game.version = "test";
	}

	@After
	public void restoreAssets() {
		Gdx.files = previousFiles;
		Game.version = previousVersion;
	}

	@Test
	public void regularTowerTrapPoolContainsTransformationTrapAtWeightOne() {
		ExposedTowerLevel level = new ExposedTowerLevel();
		Class<?>[] classes = level.exposedTrapClasses();
		float[] chances = level.exposedTrapChances();

		assertEquals(classes.length, chances.length);
		for (int i = 0; i < classes.length; i++) {
			if (classes[i] == TransformationTrap.class) {
				assertEquals(1f, chances[i], 0f);
				return;
			}
		}
		assertTrue("TransformationTrap is absent from the high-tower pool", false);
	}

	private static class ExposedTowerLevel extends TowerLevel {
		Class<?>[] exposedTrapClasses() {
			return trapClasses();
		}

		float[] exposedTrapChances() {
			return trapChances();
		}
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
