package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 统一存档管理器
 *
 * 文件结构：
 * - badges.dat       全局成就数据
 * - rankings.dat     排行榜数据
 * - journal.dat      图鉴、日志、天赋统计等全局数据
 * - save-001.json    存档槽1的全局状态与分片清单
 * - save-001-levels  存档槽1的楼层分片
 * - save-001-events  存档槽1的跨层事件分片
 * - save-002.json    存档槽2
 * - ...
 * - save-012.json    存档槽12（最多12个）
 *
 * 每个存档文件包含：
 * - 游戏主数据（玩家、深度、金币等）
 * - 地图和跨层事件分片的版本清单
 * - 元数据（用于快速显示）
 *
 * 使用示例：
 * <pre>
 * // 保存游戏
 * SaveManager.saveGame(1, gameBundle);
 *
 * // 加载游戏
 * Bundle data = SaveManager.loadGame(1);
 *
 * // 保存全局数据
 * SaveManager.saveGlobal(globalBundle);
 *
 * // 获取存档列表
 * List&lt;SaveInfo&gt; saves = SaveManager.listSaves();
 * </pre>
 */
public class SaveManager {

    // 配置
    public static final int MAX_SLOTS = 12;
    private static final String SAVE_FILE_PATTERN = "save-%03d.json";
	private static final String CHECKPOINT_FILE_PATTERN = "save-%03d.checkpoint";
	private static final String LEVEL_DIR_PATTERN = "save-%03d-levels";
	private static final String EVENT_DIR_PATTERN = "save-%03d-events";

    private static final String LEVELS_KEY = "levels";
	static final String PENDING_LEVEL_EVENTS_KEY = "pending_level_events";
	private static final String LEVEL_MANIFEST_KEY = "level_manifest";
	private static final String EVENT_MANIFEST_KEY = "event_manifest";
	private static final String SPLIT_SAVE_FORMAT_KEY = "split_save_format";
	private static final int SPLIT_SAVE_FORMAT = 1;
	private static final int LEVEL_CACHE_LIMIT = 11;
    private static final String BADGES_KEY = "badges";
    private static final String RANKINGS_KEY = "rankings";
    private static final String JOURNAL_KEY = "journal";
	private static final String SAVE_REVISION_KEY = "save_revision";
    private static final String LEGACY_GLOBAL_FILE = "global.json";
    private static final String CLOUD_DEVICE_ID_KEY = "cloud_device_id";
    private static final String CLOUD_PLAYER_UUID_KEY = "cloud_player_uuid";
    private static final Pattern LEGACY_LEVEL_FILE = Pattern.compile("depth(\\d+)(?:-branch(\\d+))?\\.dat");
	private static final Pattern LEVEL_KEY_PATTERN = Pattern.compile("depth_(-?\\d+)_(-?\\d+)");

	private static final ConcurrentHashMap<Integer, SaveInfo> saveInfoCache = new ConcurrentHashMap<>();
	private static final LinkedHashMap<String, Bundle> levelCache =
			new LinkedHashMap<String, Bundle>(LEVEL_CACHE_LIMIT, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, Bundle> eldest) {
					return size() > LEVEL_CACHE_LIMIT;
				}
			};
	private static final AtomicLongArray checkpointEpochs = new AtomicLongArray(MAX_SLOTS + 1);
	private static final CheckpointSaveQueue<CheckpointWrite> checkpointSaves =
			new CheckpointSaveQueue<>("SHPD Checkpoint Writer", SaveManager::writeQueuedCheckpoint);

	private static class CheckpointWrite {
		final int slot;
		final long epoch;
		final Bundle bundle;

		CheckpointWrite(int slot, long epoch, Bundle bundle) {
			this.slot = slot;
			this.epoch = epoch;
			this.bundle = bundle;
		}
	}


    public static class SaveInfo {
        public int slot;
        public String heroClass;
        public int heroLevel;
        public int depth;
        public int gold;
        public long playTime;      // 游戏时长（毫秒）
        public long lastPlayed;    // 最后游玩时间
		public long saveRevision;
        public boolean exists;

        public SaveInfo(int slot) {
            this.slot = slot;
            this.exists = false;
        }

        /**
         * 从Bundle提取元数据
         */
        public void extractFromBundle(Bundle bundle) {
            // 从Bundle中提取基本信息
            this.heroClass = bundle.contains("heroClass") ? bundle.getString("heroClass") : "Unknown";
            this.heroClass = bundle.contains("heroClass") ? bundle.getString("heroClass") : "Unknown";
            this.heroLevel = bundle.contains("heroLevel") ? bundle.getInt("heroLevel") : 1;
            this.depth = bundle.contains("depth") ? bundle.getInt("depth") : 1;
            this.gold = bundle.contains("gold") ? bundle.getInt("gold") : 0;
            this.playTime = bundle.contains("playTime") ? bundle.getLong("playTime") : 0L;
            this.lastPlayed = bundle.contains("lastPlayed") ? bundle.getLong("lastPlayed") : System.currentTimeMillis();
			this.saveRevision = bundle.contains(SAVE_REVISION_KEY) ? bundle.getLong(SAVE_REVISION_KEY) : 0L;
            this.exists = true;
        }

        /**
         * 保存元数据到Bundle
         */
        public void saveToBundle(Bundle bundle) {
            bundle.put("heroClass", heroClass);
            bundle.put("heroLevel", heroLevel);
            bundle.put("depth", depth);
            bundle.put("gold", gold);
            bundle.put("playTime", playTime);
			bundle.put("lastPlayed", lastPlayed);
			bundle.put(SAVE_REVISION_KEY, saveRevision);
        }
    }

    // ========================================
    // 游戏存档管理
    // ========================================

    /**
     * 保存游戏数据
     *
     * @param slot 存档槽（1-12）
     * @param bundle 游戏数据Bundle
     * @throws IOException 保存失败
     */
    public static synchronized void saveGame(int slot, Bundle bundle) throws IOException {
        validateSlot(slot);
		checkpointEpochs.incrementAndGet(slot);

        String filename = String.format(SAVE_FILE_PATTERN, slot);

        try {
			Bundle committed = loadCommittedGameOrNull(slot);
			Bundle previousBackup = loadBundleOrNull(filename + ".bak");
			long revision = nextSaveRevision(slot);
			Bundle levelManifest = copyManifest(committed, LEVEL_MANIFEST_KEY);
			Bundle eventManifest = copyManifest(committed, EVENT_MANIFEST_KEY);
			Set<String> changedLevels = new HashSet<>();
			Set<String> changedEvents = new HashSet<>();

			// A pre-split save keeps every historical level in the main bundle. Extract
			// those levels before applying the current snapshot so migration is lossless.
			if (committed != null && committed.contains(LEVELS_KEY)) {
				writeLevels(slot, committed.getBundle(LEVELS_KEY), levelManifest,
						revision, changedLevels);
			}
			if (bundle.contains(LEVELS_KEY)) {
				writeLevels(slot, bundle.getBundle(LEVELS_KEY), levelManifest,
						revision, changedLevels);
				bundle.remove(LEVELS_KEY);
			}

			// Pending cross-level effects are an authoritative set when present. Each
			// destination is persisted independently, while the manifest is committed
			// together with the global game state below.
			if (bundle.contains(PENDING_LEVEL_EVENTS_KEY)) {
				changedEvents.addAll(eventManifest.getKeys());
				eventManifest = new Bundle();
				writeEvents(slot, bundle.getBundle(PENDING_LEVEL_EVENTS_KEY), eventManifest,
						revision, changedEvents);
				bundle.remove(PENDING_LEVEL_EVENTS_KEY);
			}

			bundle.put(LEVEL_MANIFEST_KEY, levelManifest);
			bundle.put(EVENT_MANIFEST_KEY, eventManifest);
			bundle.put(SPLIT_SAVE_FORMAT_KEY, SPLIT_SAVE_FORMAT);

			// Add metadata only after every referenced shard has been written and
			// validated. Replacing this file is the transaction commit point.
            bundle.put("slot", slot);
			bundle.put("lastPlayed", System.currentTimeMillis());
			bundle.put(SAVE_REVISION_KEY, revision);
            if (!bundle.contains("version")) {
                bundle.put("version", Game.versionCode);
            }

            FileUtils.bundleToFile(filename, bundle);
			deleteCheckpointFiles(slot);
			cleanupSupersededShards(slot, previousBackup, changedLevels, changedEvents);

            // 更新缓存
            SaveInfo info = new SaveInfo(slot);
            info.extractFromBundle(bundle);
            saveInfoCache.put(slot, info);

        } catch (IOException e) {
            throw e;
        }
    }

	public static synchronized void saveCheckpoint(int slot, Bundle bundle) throws IOException {
		validateSlot(slot);
		writeCheckpoint(slot, bundle);
	}

	public static void queueCheckpoint(int slot, Bundle bundle) {
		queueCheckpoint(slot, checkpointEpoch(slot), bundle);
	}

	static long checkpointEpoch(int slot) {
		validateSlot(slot);
		return checkpointEpochs.get(slot);
	}

	static void queueCheckpoint(int slot, long epoch, Bundle bundle) {
		validateSlot(slot);
		checkpointSaves.submit(new CheckpointWrite(slot, epoch, bundle));
	}

	public static boolean flushCheckpointSaves(long timeoutMillis) throws IOException {
		return checkpointSaves.flush(timeoutMillis);
	}

	public static IOException pollCheckpointFailure() {
		return checkpointSaves.pollFailure();
	}

	private static void writeQueuedCheckpoint(CheckpointWrite write) throws IOException {
		if (write.epoch != checkpointEpochs.get(write.slot)) return;
		synchronized (SaveManager.class) {
			if (write.epoch != checkpointEpochs.get(write.slot)) return;
			writeCheckpoint(write.slot, write.bundle);
		}
	}

	private static void writeCheckpoint(int slot, Bundle bundle) throws IOException {
		bundle.put("slot", slot);
		bundle.put("lastPlayed", System.currentTimeMillis());
		bundle.put(SAVE_REVISION_KEY, nextSaveRevision(slot));
		if (!bundle.contains("version")) {
			bundle.put("version", Game.versionCode);
		}

		FileUtils.bundleToFile(String.format(CHECKPOINT_FILE_PATTERN, slot), bundle);
		SaveInfo info = new SaveInfo(slot);
		info.extractFromBundle(bundle);
		saveInfoCache.put(slot, info);
	}

    /**
     * 更新单个地图数据
     */
    public static void putLevel(Bundle gameBundle, int depth, int branch, Bundle levelBundle) {
        Bundle levels = gameBundle.getBundle(LEVELS_KEY);
        if (levels == null || levels.isNull()) {
            levels = new Bundle();
        }
        levels.put(levelKey(depth, branch), levelBundle);
        gameBundle.put(LEVELS_KEY, levels);
    }

	private static Bundle loadCommittedGameOrNull(int slot) {
		String filename = String.format(SAVE_FILE_PATTERN, slot);
		return loadBundleOrNull(filename);
	}

	private static Bundle loadBundleOrNull(String filename) {
		if (!FileUtils.fileExists(filename)) return null;
		try {
			return FileUtils.bundleFromFile(filename);
		} catch (IOException e) {
			return null;
		}
	}

	private static Bundle copyManifest(Bundle source, String key) {
		Bundle result = new Bundle();
		if (source == null || !source.contains(key)) return result;
		Bundle manifest = source.getBundle(key);
		if (manifest == null || manifest.isNull()) return result;
		for (String entry : manifest.getKeys()) {
			result.put(entry, manifest.getLong(entry));
		}
		return result;
	}

	private static void writeLevels(int slot, Bundle levels, Bundle manifest,
			long revision, Set<String> changed) throws IOException {
		if (levels == null || levels.isNull()) return;
		for (String key : levels.getKeys()) {
			Bundle level = levels.getBundle(key);
			if (level == null || level.isNull()) continue;
			FileUtils.bundleToFile(levelFile(slot, key, revision), level);
			manifest.put(key, revision);
			changed.add(key);
			removeCachedLevel(slot, key);
		}
	}

	private static void writeEvents(int slot, Bundle events, Bundle manifest,
			long revision, Set<String> changed) throws IOException {
		if (events == null || events.isNull()) return;
		for (String key : events.getKeys()) {
			Bundle event = events.getBundle(key);
			if (event == null || event.isNull()) continue;
			FileUtils.bundleToFile(eventFile(slot, key, revision), event);
			manifest.put(key, revision);
			changed.add(key);
		}
	}

	private static String levelDirectory(int slot) {
		return String.format(LEVEL_DIR_PATTERN, slot);
	}

	private static String eventDirectory(int slot) {
		return String.format(EVENT_DIR_PATTERN, slot);
	}

	private static String levelFile(int slot, String key, long revision) {
		return levelDirectory(slot) + "/" + key + "-r" + revision + ".dat";
	}

	private static String eventFile(int slot, String key, long revision) {
		return eventDirectory(slot) + "/event_" + key + "-r" + revision + ".dat";
	}

	private static String cacheKey(int slot, String key, long revision) {
		return slot + ":" + key + ":" + revision;
	}

	private static void removeCachedLevel(int slot, String key) {
		String prefix = slot + ":" + key + ":";
		ArrayList<String> stale = new ArrayList<>();
		for (String cacheKey : levelCache.keySet()) {
			if (cacheKey.startsWith(prefix)) stale.add(cacheKey);
		}
		for (String cacheKey : stale) levelCache.remove(cacheKey);
	}

	private static void removeCachedSlot(int slot) {
		String prefix = slot + ":";
		ArrayList<String> stale = new ArrayList<>();
		for (String cacheKey : levelCache.keySet()) {
			if (cacheKey.startsWith(prefix)) stale.add(cacheKey);
		}
		for (String cacheKey : stale) levelCache.remove(cacheKey);
	}

	private static Bundle loadCompleteGame(int slot) throws IOException {
		Bundle game = loadGame(slot);
		Bundle completeLevels = new Bundle();
		Bundle manifest = game.getBundle(LEVEL_MANIFEST_KEY);
		if (manifest != null && !manifest.isNull()) {
			for (String key : manifest.getKeys()) {
				completeLevels.put(key, FileUtils.bundleFromFile(
						levelFile(slot, key, manifest.getLong(key))));
			}
		}
		if (game.contains(LEVELS_KEY)) {
			Bundle embedded = game.getBundle(LEVELS_KEY);
			if (embedded != null && !embedded.isNull()) {
				for (String key : embedded.getKeys()) {
					completeLevels.put(key, embedded.getBundle(key));
				}
			}
		}
		game.put(LEVELS_KEY, completeLevels);
		game.remove(LEVEL_MANIFEST_KEY);
		game.remove(EVENT_MANIFEST_KEY);
		game.remove(SPLIT_SAVE_FORMAT_KEY);
		return game;
	}

	private static void inheritManifest(Bundle source, Bundle destination, String key) {
		if (source != null && source.contains(key) && !destination.contains(key)) {
			destination.put(key, copyManifest(source, key));
		}
	}

	private static void hydratePendingEvents(int slot, Bundle game) throws IOException {
		if (game.contains(PENDING_LEVEL_EVENTS_KEY)) return;
		Bundle manifest = game.getBundle(EVENT_MANIFEST_KEY);
		if (manifest == null || manifest.isNull() || manifest.getKeys().isEmpty()) return;

		Bundle events = new Bundle();
		for (String key : manifest.getKeys()) {
			long revision = manifest.getLong(key);
			events.put(key, FileUtils.bundleFromFile(eventFile(slot, key, revision)));
		}
		game.put(PENDING_LEVEL_EVENTS_KEY, events);
	}

	private static void trimLevelCache(int slot, int depth, int branch) {
		ArrayList<String> stale = new ArrayList<>();
		String slotPrefix = slot + ":depth_";
		for (String cacheKey : levelCache.keySet()) {
			if (!cacheKey.startsWith(slotPrefix)) continue;
			int revisionSeparator = cacheKey.lastIndexOf(':');
			String key = cacheKey.substring((slot + ":").length(), revisionSeparator);
			Matcher matcher = LEVEL_KEY_PATTERN.matcher(key);
			if (matcher.matches()) {
				int cachedDepth = Integer.parseInt(matcher.group(1));
				int cachedBranch = Integer.parseInt(matcher.group(2));
				if (cachedBranch == branch && Math.abs((long) cachedDepth - depth) > 5L) {
					stale.add(cacheKey);
				}
			}
		}
		for (String cacheKey : stale) levelCache.remove(cacheKey);
	}

	private static void cleanupSupersededShards(int slot, Bundle previousBackup,
			Set<String> changedLevels, Set<String> changedEvents) {
		if (previousBackup == null) return;
		Bundle current = loadCommittedGameOrNull(slot);
		Bundle backup = loadBundleOrNull(String.format(SAVE_FILE_PATTERN, slot) + ".bak");
		for (String key : changedLevels) {
			Long obsolete = manifestRevision(previousBackup, LEVEL_MANIFEST_KEY, key);
			if (obsolete != null
					&& !manifestReferences(current, backup, LEVEL_MANIFEST_KEY, key, obsolete)) {
				FileUtils.deleteBundleFile(levelFile(slot, key, obsolete));
			}
		}
		for (String key : changedEvents) {
			Long obsolete = manifestRevision(previousBackup, EVENT_MANIFEST_KEY, key);
			if (obsolete != null
					&& !manifestReferences(current, backup, EVENT_MANIFEST_KEY, key, obsolete)) {
				FileUtils.deleteBundleFile(eventFile(slot, key, obsolete));
			}
		}
	}

	private static boolean manifestReferences(Bundle current, Bundle backup,
			String manifestKey, String key, long revision) {
		Long currentRevision = manifestRevision(current, manifestKey, key);
		Long backupRevision = manifestRevision(backup, manifestKey, key);
		return (currentRevision != null && currentRevision == revision)
				|| (backupRevision != null && backupRevision == revision);
	}

	private static Long manifestRevision(Bundle game, String manifestKey, String key) {
		if (game == null || !game.contains(manifestKey)) return null;
		Bundle manifest = game.getBundle(manifestKey);
		if (manifest == null || manifest.isNull() || !manifest.contains(key)) return null;
		return manifest.getLong(key);
	}

	private static void mergeLevels(Bundle existing, Bundle gameBundle) {
		Bundle existingLevels = existing.getBundle(LEVELS_KEY);
        if (existingLevels == null || existingLevels.isNull()) {
            return;
        }

        Bundle mergedLevels = new Bundle();
        for (String key : existingLevels.getKeys()) {
            mergedLevels.put(key, existingLevels.getBundle(key));
        }

        if (gameBundle.contains(LEVELS_KEY)) {
            Bundle newLevels = gameBundle.getBundle(LEVELS_KEY);
            if (newLevels != null && !newLevels.isNull()) {
                for (String key : newLevels.getKeys()) {
                    mergedLevels.put(key, newLevels.getBundle(key));
                }
            }
        }

        gameBundle.put(LEVELS_KEY, mergedLevels);
    }

    public static synchronized void saveLevel(int slot, int depth, int branch, Bundle levelBundle) throws IOException {
        validateSlot(slot);

        Bundle bundle = loadGameOrNew(slot);
        putLevel(bundle, depth, branch, levelBundle);

        saveGame(slot, bundle);
    }

    /**
     * 加载游戏数据
     *
     * @param slot 存档槽（1-12）
     * @return 游戏数据Bundle
     * @throws IOException 加载失败
     */
    public static synchronized Bundle loadGame(int slot) throws IOException {
        validateSlot(slot);

        String filename = String.format(SAVE_FILE_PATTERN, slot);

		try {
			Bundle bundle = null;
			Bundle committed = null;
			IOException primaryFailure = null;
			try {
				bundle = committed = FileUtils.bundleFromFile(filename);
			} catch (IOException e) {
				primaryFailure = e;
			}
			String checkpointFile = String.format(CHECKPOINT_FILE_PATTERN, slot);
			if (FileUtils.fileExists(checkpointFile)) {
				try {
					Bundle checkpoint = FileUtils.bundleFromFile(checkpointFile);
					long checkpointRevision = checkpoint.getLong(SAVE_REVISION_KEY);
					long primaryRevision = bundle == null ? -1L : bundle.getLong(SAVE_REVISION_KEY);
					boolean checkpointNewer = checkpointRevision > primaryRevision
							|| (checkpointRevision == 0L && primaryRevision == 0L
							&& checkpoint.getLong("lastPlayed") > bundle.getLong("lastPlayed"));
					if (bundle == null || checkpointNewer) {
						if (bundle != null) mergeLevels(bundle, checkpoint);
						inheritManifest(committed, checkpoint, LEVEL_MANIFEST_KEY);
						inheritManifest(committed, checkpoint, EVENT_MANIFEST_KEY);
						bundle = checkpoint;
					} else {
						deleteCheckpointFiles(slot);
					}
				} catch (IOException e) {
					deleteCheckpointFiles(slot);
					if (bundle == null) {
						if (primaryFailure != null) e.addSuppressed(primaryFailure);
						throw e;
					}
					Game.reportException(e);
				}
			}
			if (bundle == null) {
				throw primaryFailure != null ? primaryFailure : new IOException("No save data for slot " + slot);
			}
			hydratePendingEvents(slot, bundle);

            // 更新缓存
            SaveInfo info = new SaveInfo(slot);
            info.extractFromBundle(bundle);
            saveInfoCache.put(slot, info);

            return bundle;
        } catch (IOException e) {
            log("LOAD", filename, false);
            throw e;
        }
    }

    /**
     * 加载地图数据
     */
    public static synchronized Bundle loadLevel(int slot, int depth, int branch) throws IOException {
        validateSlot(slot);

        Bundle game = loadGame(slot);
		String key = levelKey(depth, branch);
		if (game.contains(LEVELS_KEY)) {
			Bundle levels = game.getBundle(LEVELS_KEY);
			if (levels != null && !levels.isNull()) {
				Bundle embedded = levels.getBundle(key);
				if (embedded != null && !embedded.isNull()) return embedded;
			}
		}

		Bundle manifest = game.getBundle(LEVEL_MANIFEST_KEY);
		if (manifest == null || manifest.isNull() || !manifest.contains(key)) {
			throw new IOException("Level data missing for depth " + depth + ", branch " + branch);
		}
		long revision = manifest.getLong(key);
		String cacheKey = cacheKey(slot, key, revision);
		Bundle cached = levelCache.get(cacheKey);
		if (cached != null) return cached;

		Bundle level = FileUtils.bundleFromFile(levelFile(slot, key, revision));
		trimLevelCache(slot, depth, branch);
		levelCache.put(cacheKey, level);
		return level;
    }

    /**
     * 删除游戏存档
     *
     * @param slot 存档槽（1-12）
     * @return 是否成功删除
     */
    public static synchronized boolean deleteGame(int slot) {
        validateSlot(slot);
		checkpointEpochs.incrementAndGet(slot);

        String filename = String.format(SAVE_FILE_PATTERN, slot);

		boolean success = saveExists(slot);
		deleteFileAndArtifacts(filename);
		deleteCheckpointFiles(slot);
		FileUtils.deleteDir(levelDirectory(slot));
		FileUtils.deleteDir(eventDirectory(slot));
		removeCachedSlot(slot);

        if (success) {
            saveInfoCache.remove(slot);
        } else {
            log("DELETE", filename, false);
        }

        return success;
    }

	private static void deleteCheckpointFiles(int slot) {
		deleteFileAndArtifacts(String.format(CHECKPOINT_FILE_PATTERN, slot));
	}

	private static long nextSaveRevision(int slot) {
		SaveInfo previous = saveInfoCache.get(slot);
		long revision = previous == null ? 0L : previous.saveRevision;
		if (previous == null) {
			revision = Math.max(revision, revisionFromFile(String.format(SAVE_FILE_PATTERN, slot)));
			revision = Math.max(revision, revisionFromFile(String.format(CHECKPOINT_FILE_PATTERN, slot)));
		}
		return revision + 1L;
	}

	private static long revisionFromFile(String filename) {
		if (!FileUtils.fileExists(filename)) return 0L;
		try {
			return FileUtils.bundleFromFile(filename).getLong(SAVE_REVISION_KEY);
		} catch (IOException e) {
			return 0L;
		}
	}

	private static void deleteFileAndArtifacts(String filename) {
		FileUtils.deleteBundleFile(filename);
	}

    /**
     * 检查存档是否存在
     *
     * @param slot 存档槽（1-12）
     * @return 是否存在
     */
    public static boolean saveExists(int slot) {
        validateSlot(slot);

		return FileUtils.fileExists(String.format(SAVE_FILE_PATTERN, slot))
				|| FileUtils.fileExists(String.format(CHECKPOINT_FILE_PATTERN, slot));
    }

    /**
     * 复制存档到新槽位
     *
     * @param fromSlot 源槽位
     * @param toSlot 目标槽位
     * @return 是否成功
     */
    public static boolean copySave(int fromSlot, int toSlot) {
        validateSlot(fromSlot);
        validateSlot(toSlot);

        try {
			Bundle data = loadCompleteGame(fromSlot);
            saveGame(toSlot, data);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void migrateLegacySavesIfNeeded() {
        if (SPDSettings.legacySavesMigrated()) {
            return;
        }

        boolean blocked = false;

        try {
            migrateLegacyGlobalData();

            for (int slot = 1; slot <= MAX_SLOTS; slot++) {
                if (!FileUtils.fileExists(GamesInProgress.gameFile(slot))) {
                    continue;
                }

                int targetSlot = saveExists(slot) ? getFirstEmptySlot() : slot;
                if (targetSlot == -1) {
                    blocked = true;
                    continue;
                }

                if (migrateLegacyGame(slot, targetSlot)) {
                    FileUtils.deleteDir(GamesInProgress.gameFolder(slot));
                }
            }
        } finally {
            clearCache();
            if (!blocked) {
                SPDSettings.legacySavesMigrated(true);
            }
        }
    }

    private static boolean migrateLegacyGame(int legacySlot, int targetSlot) {
        try {
            Bundle game = FileUtils.bundleFromFile(GamesInProgress.gameFile(legacySlot));
            for (String file : FileUtils.filesInDir(GamesInProgress.gameFolder(legacySlot))) {
                Matcher matcher = LEGACY_LEVEL_FILE.matcher(file);
                if (!matcher.matches()) {
                    continue;
                }

                int depth = Integer.parseInt(matcher.group(1));
                int branch = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
                Bundle level = FileUtils.bundleFromFile(GamesInProgress.gameFolder(legacySlot) + "/" + file);
                putLevel(game, depth, branch, level);
            }

            saveGame(targetSlot, game);
            return true;
        } catch (Exception e) {
            Game.reportException(new RuntimeException("Legacy save migration failed for slot " + legacySlot, e));
            return false;
        }
    }

    private static void migrateLegacyGlobalData() {
        if (!FileUtils.fileExists(LEGACY_GLOBAL_FILE)) {
            return;
        }

        try {
            Bundle global = FileUtils.bundleFromFile(LEGACY_GLOBAL_FILE);
            String deviceID = global.getString(CLOUD_DEVICE_ID_KEY);
            if (deviceID != null && !deviceID.isEmpty() && SPDSettings.cloudDeviceID() == null) {
                SPDSettings.cloudDeviceID(deviceID);
            }
            String playerUUID = global.getString(CLOUD_PLAYER_UUID_KEY);
            if (playerUUID != null && !playerUUID.isEmpty() && SPDSettings.cloudPlayerUUID() == null) {
                SPDSettings.cloudPlayerUUID(playerUUID);
            }
            saveGlobal(global);
			FileUtils.deleteBundleFile(LEGACY_GLOBAL_FILE);
        } catch (Exception e) {
            Game.reportException(new RuntimeException("Legacy global data migration failed", e));
        }
    }

    // ========================================
    // 全局数据管理
    // ========================================

    /**
     * 保存全局数据（图鉴、成就、排行榜、设置等）
     *
     * @param bundle 全局数据Bundle
     * @throws IOException 保存失败
     */
    public static void saveGlobal(Bundle bundle) throws IOException {
        if (bundle.contains(CLOUD_DEVICE_ID_KEY)) {
            SPDSettings.cloudDeviceID(bundle.getString(CLOUD_DEVICE_ID_KEY));
        }
        if (bundle.contains(CLOUD_PLAYER_UUID_KEY)) {
            SPDSettings.cloudPlayerUUID(bundle.getString(CLOUD_PLAYER_UUID_KEY));
        }
        if (bundle.contains(BADGES_KEY)) {
            FileUtils.bundleToFile(Badges.BADGES_FILE, bundle.getBundle(BADGES_KEY));
        }
        if (bundle.contains(RANKINGS_KEY)) {
            Bundle cloudRankings = bundle.getBundle(RANKINGS_KEY);
            if (FileUtils.fileExists(Rankings.RANKINGS_FILE)) {
                try {
                    Bundle localRankings = FileUtils.bundleFromFile(Rankings.RANKINGS_FILE);
                    Rankings.preserveLocalHeroHall(localRankings, cloudRankings);
                } catch (IOException ignored) {
                    // A damaged local ranking file should not block a valid cloud restore.
                }
            }
            FileUtils.bundleToFile(Rankings.RANKINGS_FILE, cloudRankings);
            Rankings.INSTANCE.records = null;
        }
        if (bundle.contains(JOURNAL_KEY)) {
            FileUtils.bundleToFile(Journal.JOURNAL_FILE, bundle.getBundle(JOURNAL_KEY));
        }
    }

    /**
     * 加载全局数据
     *
     * @return 全局数据Bundle，如果不存在返回空Bundle
     */
    public static Bundle loadGlobal() {
        Bundle global = new Bundle();
        global.put("lastSaved", System.currentTimeMillis());
        global.put("version", Game.versionCode);
        if (SPDSettings.cloudDeviceID() != null) {
            global.put(CLOUD_DEVICE_ID_KEY, SPDSettings.cloudDeviceID());
        }
        if (SPDSettings.cloudPlayerUUID() != null) {
            global.put(CLOUD_PLAYER_UUID_KEY, SPDSettings.cloudPlayerUUID());
        }
        putFileBundle(global, BADGES_KEY, Badges.BADGES_FILE);
        putFileBundle(global, RANKINGS_KEY, Rankings.RANKINGS_FILE);
        putFileBundle(global, JOURNAL_KEY, Journal.JOURNAL_FILE);
        return global;
    }

    /**
     * 检查全局数据是否存在
     */
    public static boolean globalExists() {
        return FileUtils.fileExists(Badges.BADGES_FILE)
                || FileUtils.fileExists(Rankings.RANKINGS_FILE)
                || FileUtils.fileExists(Journal.JOURNAL_FILE);
    }

    /**
     * 导出全局数据到剪贴板（用于设置界面）
     * 直接导出JSON，不加密
     */
    public static boolean exportGlobalToClipboard() {
        try {
            Bundle global = loadGlobal();
            String json = global.toString();

            Gdx.app.getClipboard().setContents(json);
            return true;
        } catch (Exception e) {
            log("EXPORT_GLOBAL", "to clipboard", false);
            return false;
        }
    }

    /**
     * 从剪贴板导入全局数据（用于设置界面）
     */
    public static boolean importGlobalFromClipboard() {
        try {
            String json = Gdx.app.getClipboard().getContents();

            if (json == null || json.isEmpty()) {
                return false;
            }

            // 从JSON字符串重建Bundle
            Bundle imported = Bundle.read(new java.io.ByteArrayInputStream(json.getBytes("UTF-8")));

            saveGlobal(imported);
            return true;
        } catch (Exception e) {
            log("IMPORT_GLOBAL", "from clipboard", false);
            return false;
        }
    }


    // ========================================
    // 存档信息查询
    // ========================================

    /**
     * 获取存档信息（快速，从缓存或文件头读取）
     *
     * @param slot 存档槽
     * @return 存档信息，不存在返回null
     */
    public static SaveInfo getSaveInfo(int slot) {
        validateSlot(slot);

        // 先查缓存
        if (saveInfoCache.containsKey(slot)) {
            return saveInfoCache.get(slot);
        }

        // 读取文件
        if (!saveExists(slot)) {
            return null;
        }

        try {
            Bundle bundle = loadGame(slot);
            SaveInfo info = new SaveInfo(slot);
            info.extractFromBundle(bundle);
            saveInfoCache.put(slot, info);
            return info;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 列出所有存档
     *
     * @return 存档信息列表（按槽位排序）
     */
    public static List<SaveInfo> listSaves() {
        List<SaveInfo> saves = new ArrayList<>();

        for (int slot = 1; slot <= MAX_SLOTS; slot++) {
            SaveInfo info = getSaveInfo(slot);
            if (info != null && info.exists) {
                saves.add(info);
            }
        }

        return saves;
    }

    /**
     * 列出所有存档（按最后游玩时间排序）
     */
    public static List<SaveInfo> listSavesByLastPlayed() {
        List<SaveInfo> saves = listSaves();
        Collections.sort(saves, (a, b) -> Long.compare(b.lastPlayed, a.lastPlayed));
        return saves;
    }

    /**
     * 列出所有存档（按深度排序）
     */
    public static List<SaveInfo> listSavesByDepth() {
        List<SaveInfo> saves = listSaves();
        Collections.sort(saves, (a, b) -> Integer.compare(b.depth, a.depth));
        return saves;
    }

    /**
     * 获取第一个空槽位
     *
     * @return 空槽位编号，如果全满返回-1
     */
    public static int getFirstEmptySlot() {
        for (int slot = 1; slot <= MAX_SLOTS; slot++) {
            if (!saveExists(slot)) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * 获取存档数量
     */
    public static int getSaveCount() {
        int count = 0;
        for (int slot = 1; slot <= MAX_SLOTS; slot++) {
            if (saveExists(slot)) {
                count++;
            }
        }
        return count;
    }

    // ========================================
    // 备份与导入导出
    // ========================================

    /**
     * 导出存档到剪贴板
     * 直接导出JSON，不加密
     *
     * @param slot 存档槽
     * @return 是否成功
     */
    public static boolean exportToClipboard(int slot) {
        try {
			Bundle bundle = loadCompleteGame(slot);
            String json = bundle.toString();

            Gdx.app.getClipboard().setContents(json);
            return true;
        } catch (Exception e) {
            log("EXPORT", "slot " + slot + " to clipboard", false);
            return false;
        }
    }

	/**
	 * Writes a self-contained save for platform sharing. Runtime saves keep
	 * levels and cross-level events sharded, while this explicit export path
	 * intentionally pays the cost of assembling them into one portable file.
	 */
	public static synchronized void writePortableSave(int slot, String filename) throws IOException {
		validateSlot(slot);
		FileUtils.bundleToFile(filename, loadCompleteGame(slot));
	}

    /**
     * 从剪贴板导入存档
     * 直接读取JSON，不解密
     *
     * @return 导入到的槽位，失败返回-1
     */
    public static int importFromClipboard() {
        try {
            String json = Gdx.app.getClipboard().getContents();

            if (json == null || json.isEmpty()) {
                return -1;
            }

            // 从JSON字符串重建Bundle
            Bundle bundle = Bundle.read(new java.io.ByteArrayInputStream(json.getBytes("UTF-8")));

            // 找空槽位
            int slot = getFirstEmptySlot();
            if (slot == -1) {
                return -1;
            }

            saveGame(slot, bundle);
            return slot;
        } catch (Exception e) {
            log("IMPORT", "from clipboard", false);
            return -1;
        }
    }

    // ========================================
    // 辅助方法
    // ========================================

    /**
     * 验证槽位编号
     */
    private static void validateSlot(int slot) {
        if (slot < 1 || slot > MAX_SLOTS) {
            throw new IllegalArgumentException("Invalid slot: " + slot + " (must be 1-" + MAX_SLOTS + ")");
        }
    }


    /**
     * 记录日志
     */
    private static void log(String operation, String target, boolean success) {
        String status = success ? "✓" : "✗";
        System.out.println(String.format("[SaveManager] %s %s: %s", status, operation, target));
    }

    /**
     * 清除缓存
     */
    public static synchronized void clearCache() {
        saveInfoCache.clear();
		levelCache.clear();
    }

	static synchronized int levelCacheSizeForTesting() {
		return levelCache.size();
	}

    /**
     * 获取所有存档文件的总大小
     */
    public static long getTotalSize() {
        long total = 0;

        // 全局文件
        total += FileUtils.fileLength(Badges.BADGES_FILE);
        total += FileUtils.fileLength(Rankings.RANKINGS_FILE);
        total += FileUtils.fileLength(Journal.JOURNAL_FILE);

        // 所有存档
        for (int slot = 1; slot <= MAX_SLOTS; slot++) {
            String filename = String.format(SAVE_FILE_PATTERN, slot);
            total += FileUtils.fileLength(filename);
			total += FileUtils.fileLength(filename + ".bak");
			String checkpoint = String.format(CHECKPOINT_FILE_PATTERN, slot);
			total += FileUtils.fileLength(checkpoint);
			total += FileUtils.fileLength(checkpoint + ".bak");
			total += directorySize(levelDirectory(slot));
			total += directorySize(eventDirectory(slot));
        }

        return total;
    }

	private static long directorySize(String directory) {
		long total = 0L;
		for (String file : FileUtils.filesInDir(directory)) {
			total += FileUtils.fileLength(directory + "/" + file);
		}
		return total;
	}

    /**
     * 打印存档信息（调试用）
     */
    public static void printSaveInfo() {
        System.out.println("=== SaveManager Info ===");
        System.out.println("Total saves: " + getSaveCount() + "/" + MAX_SLOTS);
        System.out.println("Total size: " + formatSize(getTotalSize()));
        System.out.println();

        List<SaveInfo> saves = listSaves();
        for (SaveInfo info : saves) {
            System.out.println(String.format("Slot %02d: %s Lv.%d | Depth %d | %s gold | Last played: %s",
                    info.slot,
                    info.heroClass,
                    info.heroLevel,
                    info.depth,
                    info.gold,
                    formatTimestamp(info.lastPlayed)));
        }

        System.out.println("========================");
    }

    /**
     * 格式化文件大小
     */
    private static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / 1024.0 / 1024.0);
        }
    }

    /**
     * 格式化时间戳
     */
    private static String formatTimestamp(long time) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date(time));
    }

    private static String levelKey(int depth, int branch) {
        return "depth_" + depth + "_" + branch;
    }

    private static Bundle loadGameOrNull(int slot) {
        try {
            return loadGame(slot);
        } catch (IOException e) {
            return null;
        }
    }

    private static Bundle loadGameOrNew(int slot) throws IOException {
        Bundle bundle = loadGameOrNull(slot);
        if (bundle == null) {
            bundle = new Bundle();
            bundle.put("slot", slot);
        }
        return bundle;
    }

    private static void putFileBundle(Bundle parent, String key, String file) {
        try {
            parent.put(key, FileUtils.bundleFromFile(file));
        } catch (IOException e) {
            parent.put(key, new Bundle());
        }
    }
}
