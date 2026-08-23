package com.watabou.utils;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.SaveManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SaveFileRecoveryTest {

	private Files previousFiles;
	private Path tempDir;

	@Before
	public void setUp() throws IOException {
		previousFiles = Gdx.files;
		tempDir = java.nio.file.Files.createTempDirectory("spd-save-test");
		Gdx.files = new AbsoluteFiles();
		FileUtils.setDefaultFileProperties(Files.FileType.Absolute,
				tempDir.toString() + File.separator);
		SaveManager.clearCache();
	}

	@After
	public void tearDown() throws IOException {
		SaveManager.flushCheckpointSaves(2000);
		Gdx.files = previousFiles;
		SaveManager.clearCache();
		if (tempDir != null) {
			java.nio.file.Files.walk(tempDir)
					.sorted(Comparator.reverseOrder())
					.forEach(path -> path.toFile().delete());
		}
	}

	@Test
	public void successfulReplacementKeepsPreviousGenerationAsBackup() throws IOException {
		FileUtils.bundleToFile("save.json", bundleWithValue(1));
		FileUtils.bundleToFile("save.json", bundleWithValue(2));

		assertEquals(2, FileUtils.bundleFromFile("save.json").getInt("value"));
		assertTrue(FileUtils.fileExists("save.json.bak"));
		assertEquals(1, FileUtils.bundleFromFile("save.json.bak").getInt("value"));
	}

	@Test
	public void cleanupRestoresBackupWhenPrimaryIsCorrupt() throws IOException {
		FileUtils.bundleToFile("save.json", bundleWithValue(1));
		FileUtils.bundleToFile("save.json", bundleWithValue(2));
		java.nio.file.Files.write(tempDir.resolve("save.json"),
				"not a bundle".getBytes(StandardCharsets.UTF_8));

		assertTrue(FileUtils.cleanTempFiles());
		assertEquals(1, FileUtils.bundleFromFile("save.json").getInt("value"));
	}

	@Test
	public void directReadRestoresBackupWithoutStartupCleanup() throws IOException {
		FileUtils.bundleToFile("save.json", bundleWithValue(1));
		FileUtils.bundleToFile("save.json", bundleWithValue(2));
		java.nio.file.Files.write(tempDir.resolve("save.json"),
				"not a bundle".getBytes(StandardCharsets.UTF_8));

		assertEquals(1, FileUtils.bundleFromFile("save.json").getInt("value"));
		assertEquals(1, FileUtils.bundleFromFile("save.json").getInt("value"));
	}

	@Test
	public void recoveringNewerTempRotatesCurrentPrimaryIntoBackup() throws IOException {
		FileUtils.bundleToFile("save.json", bundleWithValue(1));
		Path temp = tempDir.resolve("save.json.tmp");
		assertTrue(Bundle.write(bundleWithValue(2), java.nio.file.Files.newOutputStream(temp)));
		java.nio.file.Files.setLastModifiedTime(temp,
				FileTime.fromMillis(System.currentTimeMillis() + 2000L));

		assertTrue(FileUtils.cleanTempFiles());
		assertEquals(2, FileUtils.bundleFromFile("save.json").getInt("value"));
		assertEquals(1, FileUtils.bundleFromFile("save.json.bak").getInt("value"));
	}

	@Test
	public void explicitBundleDeletionCannotBeUndoneByCleanup() throws IOException {
		FileUtils.bundleToFile("save.json", bundleWithValue(1));
		FileUtils.bundleToFile("save.json", bundleWithValue(2));

		assertTrue(FileUtils.deleteBundleFile("save.json"));
		assertFalse(FileUtils.cleanTempFiles());
		assertFalse(FileUtils.fileExists("save.json"));
		assertFalse(FileUtils.fileExists("save.json.bak"));
	}

	@Test
	public void leftoverDeletionMarkerBlocksBackupResurrection() throws IOException {
		FileUtils.bundleToFile("save.json", bundleWithValue(1));
		FileUtils.bundleToFile("save.json", bundleWithValue(2));
		FileUtils.deleteFile("save.json");
		java.nio.file.Files.write(tempDir.resolve("save.json.del"),
				"deleted".getBytes(StandardCharsets.UTF_8));

		assertFalse(FileUtils.fileExists("save.json"));
		assertTrue(FileUtils.cleanTempFiles());
		assertFalse(FileUtils.fileExists("save.json"));
		assertFalse(FileUtils.fileExists("save.json.bak"));
	}

	@Test
	public void serializationFailureIsReportedAsIOException() throws Exception {
		Method method = FileUtils.class.getDeclaredMethod(
				"bundleToStream", OutputStream.class, Bundle.class);
		method.setAccessible(true);

		try {
			method.invoke(null, new FailingOutputStream(), bundleWithValue(1));
			fail("Expected bundleToStream to reject a failed write");
		} catch (InvocationTargetException e) {
			assertTrue(e.getCause() instanceof IOException);
		}
	}

	@Test
	public void checkpointOverridesLiveStateWithoutDroppingHistoricalLevels() throws IOException {
		Bundle base = bundleWithValue(1);
		SaveManager.putLevel(base, 1, 0, bundleWithValue(10));
		SaveManager.saveGame(1, base);

		Bundle checkpoint = bundleWithValue(2);
		SaveManager.putLevel(checkpoint, 2, 0, bundleWithValue(20));
		SaveManager.saveCheckpoint(1, checkpoint);
		Bundle rawCheckpoint = FileUtils.bundleFromFile("save-001.checkpoint");
		assertFalse(rawCheckpoint.getBundle("levels").contains("depth_1_0"));
		assertTrue(rawCheckpoint.getBundle("levels").contains("depth_2_0"));

		Bundle loaded = SaveManager.loadGame(1);
		assertEquals(2, loaded.getInt("value"));
		assertEquals(10, SaveManager.loadLevel(1, 1, 0).getInt("value"));
		assertEquals(20, SaveManager.loadLevel(1, 2, 0).getInt("value"));
		assertTrue(FileUtils.fileExists("save-001.checkpoint"));

		Bundle committed = bundleWithValue(3);
		SaveManager.saveGame(1, committed);
		assertFalse(FileUtils.fileExists("save-001.checkpoint"));
		assertEquals(3, SaveManager.loadGame(1).getInt("value"));
	}

	@Test
	public void checkpointCanRecoverWhenPrimaryAndBackupAreMissing() throws IOException {
		SaveManager.saveGame(1, bundleWithValue(1));
		SaveManager.saveCheckpoint(1, bundleWithValue(2));
		FileUtils.deleteFile("save-001.json");
		FileUtils.deleteFile("save-001.json.bak");

		assertTrue(SaveManager.saveExists(1));
		assertEquals(2, SaveManager.loadGame(1).getInt("value"));
	}

	@Test
	public void corruptCheckpointFallsBackToCommittedPrimary() throws IOException {
		SaveManager.saveGame(1, bundleWithValue(1));
		SaveManager.saveCheckpoint(1, bundleWithValue(2));
		java.nio.file.Files.write(tempDir.resolve("save-001.checkpoint"),
				"not a bundle".getBytes(StandardCharsets.UTF_8));

		assertEquals(1, SaveManager.loadGame(1).getInt("value"));
		assertFalse(FileUtils.fileExists("save-001.checkpoint"));
	}

	@Test
	public void checkpointWithSameTimestampCannotOverrideCommittedPrimary() throws IOException {
		Bundle primary = bundleWithValue(1);
		primary.put("lastPlayed", 100L);
		FileUtils.bundleToFile("save-001.json", primary);
		Bundle staleCheckpoint = bundleWithValue(2);
		staleCheckpoint.put("lastPlayed", 100L);
		FileUtils.bundleToFile("save-001.checkpoint", staleCheckpoint);

		assertEquals(1, SaveManager.loadGame(1).getInt("value"));
		assertFalse(FileUtils.fileExists("save-001.checkpoint"));
	}

	@Test
	public void newerRevisionWinsEvenWhenWallClockMovesBackward() throws IOException {
		Bundle primary = bundleWithValue(1);
		primary.put("lastPlayed", 200L);
		primary.put("save_revision", 2L);
		FileUtils.bundleToFile("save-001.json", primary);
		Bundle checkpoint = bundleWithValue(2);
		checkpoint.put("lastPlayed", 100L);
		checkpoint.put("save_revision", 3L);
		FileUtils.bundleToFile("save-001.checkpoint", checkpoint);

		assertEquals(2, SaveManager.loadGame(1).getInt("value"));
	}

	@Test
	public void revisionRemainsMonotonicAfterCacheIsCleared() throws IOException {
		SaveManager.saveGame(1, bundleWithValue(1));
		long primaryRevision = FileUtils.bundleFromFile("save-001.json").getLong("save_revision");
		SaveManager.clearCache();
		SaveManager.saveCheckpoint(1, bundleWithValue(2));
		long checkpointRevision = FileUtils.bundleFromFile("save-001.checkpoint").getLong("save_revision");

		assertEquals(primaryRevision + 1L, checkpointRevision);
	}

	@Test
	public void queuedCheckpointCanBeFlushedAndLoaded() throws IOException {
		SaveManager.saveGame(1, bundleWithValue(1));
		SaveManager.queueCheckpoint(1, bundleWithValue(2));

		assertTrue(SaveManager.flushCheckpointSaves(2000));
		assertEquals(2, SaveManager.loadGame(1).getInt("value"));
	}

	@Test
	public void committedSaveInvalidatesAnOlderQueuedCheckpoint() throws IOException {
		SaveManager.saveGame(1, bundleWithValue(1));
		synchronized (SaveManager.class) {
			SaveManager.queueCheckpoint(1, bundleWithValue(2));
			SaveManager.saveGame(1, bundleWithValue(3));
		}

		assertTrue(SaveManager.flushCheckpointSaves(2000));
		assertEquals(3, SaveManager.loadGame(1).getInt("value"));
		assertFalse(FileUtils.fileExists("save-001.checkpoint"));
	}

	@Test
	public void queueingCheckpointDoesNotWaitForDiskWriterLock() throws Exception {
		CountDownLatch lockHeld = new CountDownLatch(1);
		CountDownLatch releaseLock = new CountDownLatch(1);
		CountDownLatch queued = new CountDownLatch(1);

		Thread lockHolder = new Thread(() -> {
			synchronized (SaveManager.class) {
				lockHeld.countDown();
				try {
					releaseLock.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		lockHolder.start();
		assertTrue(lockHeld.await(1, TimeUnit.SECONDS));

		Thread submitter = new Thread(() -> {
			SaveManager.queueCheckpoint(1, bundleWithValue(2));
			queued.countDown();
		});
		submitter.start();
		try {
			assertTrue("Checkpoint submission waited for the disk writer lock",
					queued.await(250, TimeUnit.MILLISECONDS));
		} finally {
			releaseLock.countDown();
			lockHolder.join(1000);
			submitter.join(1000);
		}
	}

	@Test
	public void saveDuringSnapshotInvalidatesThatSnapshot() throws Exception {
		Method epochMethod = SaveManager.class.getDeclaredMethod("checkpointEpoch", int.class);
		Method queueMethod = SaveManager.class.getDeclaredMethod(
				"queueCheckpoint", int.class, long.class, Bundle.class);
		epochMethod.setAccessible(true);
		queueMethod.setAccessible(true);

		SaveManager.saveGame(1, bundleWithValue(1));
		long snapshotEpoch = (Long) epochMethod.invoke(null, 1);
		SaveManager.saveGame(1, bundleWithValue(2));
		queueMethod.invoke(null, 1, snapshotEpoch, bundleWithValue(3));

		assertTrue(SaveManager.flushCheckpointSaves(2000));
		assertEquals(2, SaveManager.loadGame(1).getInt("value"));
		assertFalse(FileUtils.fileExists("save-001.checkpoint"));
	}

	private static Bundle bundleWithValue(int value) {
		Bundle bundle = new Bundle();
		bundle.put("value", value);
		return bundle;
	}

	private static class FailingOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			throw new IOException("simulated write failure");
		}
	}

	private static class AbsoluteFiles implements Files {
		@Override
		public FileHandle getFileHandle(String path, FileType type) {
			return new FileHandle(path);
		}

		@Override
		public FileHandle classpath(String path) {
			return new FileHandle(path);
		}

		@Override
		public FileHandle internal(String path) {
			return new FileHandle(path);
		}

		@Override
		public FileHandle external(String path) {
			return new FileHandle(path);
		}

		@Override
		public FileHandle absolute(String path) {
			return new FileHandle(path);
		}

		@Override
		public FileHandle local(String path) {
			return new FileHandle(path);
		}

		@Override
		public String getExternalStoragePath() {
			return tempDirPath();
		}

		@Override
		public boolean isExternalStorageAvailable() {
			return true;
		}

		@Override
		public String getLocalStoragePath() {
			return tempDirPath();
		}

		@Override
		public boolean isLocalStorageAvailable() {
			return true;
		}

		private String tempDirPath() {
			return "";
		}
	}
}
