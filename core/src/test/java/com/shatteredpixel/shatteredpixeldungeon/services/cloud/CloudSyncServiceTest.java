package com.shatteredpixel.shatteredpixeldungeon.services.cloud;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.journal.TalentCatalog;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.GameSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

public class CloudSyncServiceTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private Application oldApp;
    private Files oldFiles;
    private Preferences oldPreferences;
    private Object oldFileType;
    private Object oldPath;
    private Object oldBadges;
    private Object oldBadgeDirty;
    private Object oldJournalLoaded;
    private Object oldJournalDirty;
    private PlatformSupport oldPlatform;
    private Bundle oldTalentData;
    private String oldVersion;
    private com.badlogic.gdx.Net oldNet;
    private final ArrayList<com.badlogic.gdx.Net.HttpRequest> requests = new ArrayList<>();
    private final ArrayList<com.badlogic.gdx.Net.HttpResponseListener> listeners = new ArrayList<>();
    private final Map<Field, Object> rankingState = new HashMap<>();
    private static final String UUID_A = "11111111-1111-4111-8111-111111111111";
    private static final String UUID_B = "22222222-2222-4222-8222-222222222222";

    @Before public void setUp() throws Exception {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        oldApp = Gdx.app;
        oldFiles = Gdx.files;
        oldVersion = Game.version; Game.version = "test";
        oldNet = Gdx.net;
        Gdx.net = (com.badlogic.gdx.Net) Proxy.newProxyInstance(com.badlogic.gdx.Net.class.getClassLoader(),
                new Class[]{com.badlogic.gdx.Net.class}, (proxy, method, args) -> {
                    if (method.getName().equals("sendHttpRequest")) {
                        requests.add((com.badlogic.gdx.Net.HttpRequest) args[0]);
                        listeners.add((com.badlogic.gdx.Net.HttpResponseListener) args[1]);
                    }
                    return null;
                });
        oldPreferences = (Preferences) field(GameSettings.class, "prefs").get(null);
        oldFileType = field(FileUtils.class, "defaultFileType").get(null);
        oldPath = field(FileUtils.class, "defaultPath").get(null);
        Map<String, Object> values = new HashMap<>();
        Preferences preferences = (Preferences) Proxy.newProxyInstance(Preferences.class.getClassLoader(),
                new Class[]{Preferences.class}, (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.startsWith("put") && args.length == 2) { values.put((String) args[0], args[1]); return proxy; }
                    if (name.startsWith("get") && args != null && args.length == 2) return values.getOrDefault(args[0], args[1]);
                    if (name.equals("contains")) return values.containsKey(args[0]);
                    if (method.getReturnType() == Preferences.class) return proxy;
                    return null;
                });
        GameSettings.set(preferences);
        Gdx.app = (Application) Proxy.newProxyInstance(Application.class.getClassLoader(),
                new Class[]{Application.class}, (proxy, method, args) -> {
                    if (method.getName().equals("postRunnable")) ((Runnable) args[0]).run();
                    if (method.getName().equals("getType")) return Application.ApplicationType.Desktop;
                    if (method.getName().equals("getPreferences")) return preferences;
                    return null;
                });
        Gdx.files = (Files) Proxy.newProxyInstance(Files.class.getClassLoader(), new Class[]{Files.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("internal")) {
                        java.io.File assets = new java.io.File("src/main/assets");
                        if (!assets.isDirectory()) assets = new java.io.File("core/src/main/assets");
                        return new FileHandle(new java.io.File(assets, (String) args[0]));
                    }
                    return new FileHandle((String) args[0]);
                });
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute, temporary.getRoot().getAbsolutePath() + "/");
        oldBadges = field(Badges.class, "global").get(null);
        oldBadgeDirty = field(Badges.class, "saveNeeded").get(null);
        field(Badges.class, "global").set(null, new HashSet<Badges.Badge>());
        field(Badges.class, "saveNeeded").set(null, false);
        oldJournalLoaded = field(Journal.class, "loaded").get(null);
        oldJournalDirty = field(Journal.class, "saveNeeded").get(null);
        field(Journal.class, "loaded").set(null, true);
        field(Journal.class, "saveNeeded").set(null, false);
        oldTalentData = new Bundle(); TalentCatalog.store(oldTalentData);
        oldPlatform = Game.platform;
        Game.platform = new PlatformSupport() {
            @Override public void updateDisplaySize() {}
            @Override public void updateSystemUI() {}
            @Override public boolean connectedToUnmeteredNetwork() { return false; }
            @Override public boolean supportsVibration() { return false; }
            @Override public void setupFontGenerators(int size, boolean systemFont) {}
            @Override protected com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator getGeneratorForString(String input) { return null; }
            @Override public String[] splitforTextBlock(String text, boolean multiline) { return new String[]{text}; }
            @Override public String cloudDeviceFingerprint() { return "test-device"; }
        };
        for (Field field : Rankings.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            Object value = field.get(Rankings.INSTANCE);
            rankingState.put(field, Modifier.isFinal(field.getModifiers()) && value instanceof Collection
                    ? new ArrayList<>((Collection<?>) value) : value);
        }
    }

    @After @SuppressWarnings({"rawtypes", "unchecked"}) public void tearDown() throws Exception {
        for (Map.Entry<Field, Object> entry : rankingState.entrySet()) {
            if (Modifier.isFinal(entry.getKey().getModifiers()) && entry.getValue() instanceof Collection) {
                Collection collection = (Collection) entry.getKey().get(Rankings.INSTANCE);
                collection.clear();
                collection.addAll((Collection) entry.getValue());
            } else entry.getKey().set(Rankings.INSTANCE, entry.getValue());
        }
        Gdx.app = oldApp;
        Gdx.files = oldFiles;
        GameSettings.set(oldPreferences);
        field(FileUtils.class, "defaultFileType").set(null, oldFileType);
        field(FileUtils.class, "defaultPath").set(null, oldPath);
        field(Badges.class, "global").set(null, oldBadges);
        field(Badges.class, "saveNeeded").set(null, oldBadgeDirty);
        TalentCatalog.restore(oldTalentData);
        field(Journal.class, "loaded").set(null, oldJournalLoaded);
        field(Journal.class, "saveNeeded").set(null, oldJournalDirty);
        Game.platform = oldPlatform;
        Game.version = oldVersion;
        Gdx.net = oldNet;
    }

    @Test public void uploadAcknowledgementNeverRestoresLocalHistory() throws Exception {
        Bundle local = new Bundle(); local.put("marker", "local");
        FileUtils.bundleToFile(Rankings.RANKINGS_FILE, local);
        FileUtils.bundleToFile(Badges.BADGES_FILE, local);
        Bundle remote = new Bundle(); remote.put("marker", "remote");
        Bundle global = new Bundle(); global.put("rankings", remote); global.put("badges", remote);
        Bundle response = new Bundle(); response.put("global_data", global);
        response.put("player_uuid", UUID_A);
        response.put("talent_stats", remote);
        invoke("applyUploadResponse", new Class[]{Bundle.class, String.class}, response, "");
        assertEquals("local", FileUtils.bundleFromFile(Rankings.RANKINGS_FILE).getString("marker"));
        assertEquals("local", FileUtils.bundleFromFile(Badges.BADGES_FILE).getString("marker"));
        assertEquals(UUID_A, SPDSettings.cloudPlayerUUID());
    }

    @Test public void mismatchedUploadIdentityIsRejected() throws Exception {
        SPDSettings.cloudPlayerUUID(UUID_A);
        Bundle response = new Bundle(); response.put("player_uuid", UUID_B);
        try {
            invoke("applyUploadResponse", new Class[]{Bundle.class, String.class}, response, UUID_A);
            fail("Mismatched identity must not replace the current player");
        } catch (java.lang.reflect.InvocationTargetException expected) {
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
        assertEquals(UUID_A, SPDSettings.cloudPlayerUUID());
    }

    @Test public void localChangeInvalidatesRestoreSnapshot() throws Exception {
        Object context = invoke("newRestoreContext", new Class[0]);
        assertTrue(isCurrent(context));
        Bundle newer = new Bundle(); newer.put("marker", "new victory or reset");
        FileUtils.bundleToFile(Rankings.RANKINGS_FILE, newer);
        assertFalse(isCurrent(context));
    }

    @Test public void newerRestoreRequestInvalidatesEarlierResponse() throws Exception {
        Object first = invoke("newRestoreContext", new Class[0]);
        Object second = invoke("newRestoreContext", new Class[0]);
        assertFalse(isCurrent(first));
        assertTrue(isCurrent(second));
    }

    @Test public void rankingsSavePersistsLatestIdentityAndDeletionMetadata() throws Exception {
        Rankings.INSTANCE.records = null;
        Rankings.INSTANCE.load();
        Rankings.Record record = new Rankings.Record();
        record.gameID = "deleted"; record.customSeed = "";
        Rankings.INSTANCE.records.add(record); Rankings.INSTANCE.lastRecord = 0;
        assertTrue(Rankings.INSTANCE.saveWithResult());
        assertEquals("deleted", FileUtils.bundleFromFile(Rankings.RANKINGS_FILE).getString("latest_game_id"));
        Method remove = Rankings.class.getMethod("removeRecord", Rankings.Record.class);
        // Avoid real HTTP; deletion is persisted even when upload fails.
        com.badlogic.gdx.Net oldNet = Gdx.net;
        Gdx.net = (com.badlogic.gdx.Net) Proxy.newProxyInstance(com.badlogic.gdx.Net.class.getClassLoader(),
                new Class[]{com.badlogic.gdx.Net.class}, (proxy, method, args) -> null);
        try { assertEquals(true, remove.invoke(Rankings.INSTANCE, record)); }
        finally { Gdx.net = oldNet; }
        Bundle saved = FileUtils.bundleFromFile(Rankings.RANKINGS_FILE);
        assertArrayEquals(new String[]{"deleted"}, saved.getStringArray("deleted_game_ids"));
        Rankings.INSTANCE.records = null; Rankings.INSTANCE.load();
        assertTrue(Rankings.INSTANCE.records.isEmpty());
        assertEquals(-1, Rankings.INSTANCE.lastRecord);
    }

    @Test public void uploadRefreshesAggregateButNeverPersonalTalentStats() throws Exception {
        String before = TalentCatalog.localStatsBundle().toString();
        RecordingCallback callback = new RecordingCallback();
        CloudSyncService.uploadLocalData(callback);
        String badges = FileUtils.bundleFromFile(Badges.BADGES_FILE).toString();
        Bundle remote = new Bundle(); remote.put("player_uuid", UUID_A); remote.put("ok", true);
        Bundle global = new Bundle(); Bundle rankings = new Bundle(); rankings.put("marker", "remote");
        global.put("rankings", rankings); global.put("badges", rankings);
        remote.put("global_data", global);
        String talent = TalentCatalog.T1.entities().keySet().iterator().next().name();
        Bundle counts = new Bundle(); counts.put("selected", 99); counts.put("appeared", 100); counts.put("targeted", 20);
        Bundle stats = new Bundle(); stats.put(talent, counts); remote.put("talent_stats", stats);
        respond(0, remote);
        assertTrue(requests.get(1).getUrl().contains("/api/aggregate"));
        Bundle aggregate = new Bundle(); aggregate.put("ok", true); aggregate.put("aggregate", stats);
        respond(1, aggregate);
        assertEquals(1, callback.successes);
        assertEquals(before, TalentCatalog.localStatsBundle().toString());
        assertFalse(FileUtils.fileExists(Rankings.RANKINGS_FILE));
        assertEquals(badges, FileUtils.bundleFromFile(Badges.BADGES_FILE).toString());
        Bundle stored = new Bundle(); TalentCatalog.store(stored);
        assertEquals(99, stored.getBundle("server_talent_stats").getBundle(talent).getInt("selected"));
    }

    @Test public void clearIsDurableAndDelayedUploadCannotRestoreHistory() throws Exception {
        Rankings.INSTANCE.records = null; Rankings.INSTANCE.load();
        Rankings.Record record = new Rankings.Record(); record.gameID = "old"; record.customSeed = "";
        Rankings.INSTANCE.records.add(record); Rankings.INSTANCE.totalNumber = 20;
        Rankings.INSTANCE.wonNumber = 10; Rankings.INSTANCE.lastRecord = 0;
        assertTrue(Rankings.INSTANCE.saveWithResult());
        long generation = FileUtils.bundleFromFile(Rankings.RANKINGS_FILE).getLong("sync_generation");
        CloudSyncService.uploadLocalData(new RecordingCallback());
        assertTrue(Rankings.INSTANCE.clearRecords());
        Bundle cleared = FileUtils.bundleFromFile(Rankings.RANKINGS_FILE);
        assertTrue(cleared.getLong("sync_generation") > generation);
        Bundle response = new Bundle(); response.put("ok", true); response.put("player_uuid", UUID_A);
        Bundle global = new Bundle(); Bundle old = new Bundle(); old.put("marker", "old cloud history");
        global.put("rankings", old); response.put("global_data", global);
        respond(0, response);
        Rankings.INSTANCE.records = null; Rankings.INSTANCE.load();
        assertTrue(Rankings.INSTANCE.records.isEmpty());
        assertEquals(0, Rankings.INSTANCE.totalNumber);
        assertEquals(0, Rankings.INSTANCE.wonNumber);
        assertEquals(cleared.getLong("sync_generation"), FileUtils.bundleFromFile(Rankings.RANKINGS_FILE).getLong("sync_generation"));
    }

    @Test public void explicitRestoreStillRestoresRankingsAndReloadsBadgeCache() throws Exception {
        RecordingRestore callback = new RecordingRestore();
        CloudSyncService.syncServerData(UUID_A, callback);
        respond(0, restoreResponse());
        assertTrue(requests.get(1).getUrl().endsWith("/api/restore/commit"));
        Bundle commit = new Bundle(); commit.put("ok", true); commit.put("player_uuid", UUID_A);
        respond(1, commit);
        assertEquals(1, callback.successes);
        assertNull(callback.failure);
        assertEquals(UUID_A, SPDSettings.cloudPlayerUUID());
        assertTrue(Badges.isUnlocked(Badges.Badge.VICTORY));
        assertEquals(7, Rankings.INSTANCE.totalNumber);
        assertFalse(CloudSyncService.hasPendingRestore());
    }

    @Test public void delayedCommitDoesNotOverwriteNewLocalData() throws Exception {
        RecordingRestore callback = new RecordingRestore();
        CloudSyncService.syncServerData(UUID_A, callback);
        respond(0, restoreResponse());
        Bundle newer = new Bundle(); newer.put("marker", "new local victory");
        FileUtils.bundleToFile(Rankings.RANKINGS_FILE, newer);
        Bundle commit = new Bundle(); commit.put("ok", true); commit.put("player_uuid", UUID_A);
        respond(1, commit);
        assertEquals(CloudSyncService.RestoreFailure.STALE, callback.failure);
        assertEquals("new local victory", FileUtils.bundleFromFile(Rankings.RANKINGS_FILE).getString("marker"));
        assertFalse(Badges.isUnlocked(Badges.Badge.VICTORY));
        assertFalse(CloudSyncService.hasPendingRestore());
    }

    @Test public void pendingRestoreCannotOverwriteChangesAfterRestart() throws Exception {
        CloudSyncService.syncServerData(UUID_A, new RecordingRestore());
        respond(0, restoreResponse());
        Bundle newer = new Bundle(); newer.put("marker", "changed while offline");
        FileUtils.bundleToFile(Rankings.RANKINGS_FILE, newer);
        RecordingRestore callback = new RecordingRestore();
        CloudSyncService.resumePendingRestore(callback);
        assertEquals(CloudSyncService.RestoreFailure.STALE, callback.failure);
        assertEquals(2, requests.size());
        assertEquals("changed while offline", FileUtils.bundleFromFile(Rankings.RANKINGS_FILE).getString("marker"));
        assertFalse(CloudSyncService.hasPendingRestore());
    }

    @Test public void legacyRankingFileDefaultsMetadataAndResolvesLatestById() throws Exception {
        Bundle legacy = new Bundle(); legacy.put("total", 10); legacy.put("won", 4);
        FileUtils.bundleToFile(Rankings.RANKINGS_FILE, legacy);
        Rankings.INSTANCE.records = null; Rankings.INSTANCE.load();
        assertTrue(Rankings.INSTANCE.saveWithResult());
        Bundle saved = FileUtils.bundleFromFile(Rankings.RANKINGS_FILE);
        assertEquals(0L, saved.getLong("sync_generation"));
        assertEquals(0, saved.getStringArray("deleted_game_ids").length);
        assertEquals(10, saved.getInt("total"));
        assertEquals(4, saved.getInt("won"));
    }

    @Test public void corruptLocalFileCanStillBeRepairedByExplicitRestore() throws Exception {
        FileUtils.getFileHandle(Rankings.RANKINGS_FILE).writeBytes(new byte[]{1, 2, 3}, false);
        RecordingRestore callback = new RecordingRestore();
        CloudSyncService.syncServerData(UUID_A, callback);
        respond(0, restoreResponse());
        Bundle commit = new Bundle(); commit.put("ok", true); commit.put("player_uuid", UUID_A);
        respond(1, commit);
        assertEquals(1, callback.successes);
        assertEquals(7, FileUtils.bundleFromFile(Rankings.RANKINGS_FILE).getInt("total"));
    }

    @Test public void staleCallbackDoesNotRemoveNewerPendingRestore() throws Exception {
        CloudSyncService.syncServerData(UUID_A, new RecordingRestore());
        respond(0, restoreResponse());
        Object newer = invoke("newRestoreContext", new Class[0]);
        Bundle pending = new Bundle(); pending.put("restore_token", "newer-token");
        FileUtils.bundleToFile("cloud_restore_pending.dat", pending);
        Bundle commit = new Bundle(); commit.put("ok", true); commit.put("player_uuid", UUID_A);
        respond(1, commit);
        assertTrue(isCurrent(newer));
        assertEquals("newer-token", FileUtils.bundleFromFile("cloud_restore_pending.dat").getString("restore_token"));
    }

    @Test public void savingFailureRollsBackClearingAndDoesNotUpload() throws Exception {
        Rankings.INSTANCE.records = null; Rankings.INSTANCE.load();
        Rankings.Record record = new Rankings.Record(); record.gameID = "kept"; record.customSeed = "";
        Rankings.INSTANCE.records.add(record); Rankings.INSTANCE.totalNumber = 3;
        Rankings.INSTANCE.lastRecord = 0;
        java.io.File notDirectory = temporary.newFile("not-a-directory");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute, notDirectory.getAbsolutePath() + "/");
        assertFalse(Rankings.INSTANCE.clearRecords());
        assertEquals(record, Rankings.INSTANCE.records.get(0));
        assertEquals(3, Rankings.INSTANCE.totalNumber);
        assertEquals(0, Rankings.INSTANCE.lastRecord);
        assertTrue(requests.isEmpty());
    }

    @Test public void retryDoesNotCommitWhenLocalDataHasChanged() throws Exception {
        RecordingRestore callback = new RecordingRestore();
        CloudSyncService.syncServerData(UUID_A, callback);
        respond(0, restoreResponse());
        Bundle newer = new Bundle(); newer.put("marker", "new local data");
        FileUtils.bundleToFile(Rankings.RANKINGS_FILE, newer);
        listeners.get(1).failed(new RuntimeException("simulated timeout"));
        assertEquals(2, requests.size());
        assertEquals(CloudSyncService.RestoreFailure.STALE, callback.failure);
        assertFalse(CloudSyncService.hasPendingRestore());
    }

    @Test public void networkRetryAndPendingResumeStillWorkWhenLocalDataIsUnchanged() throws Exception {
        RecordingRestore callback = new RecordingRestore();
        CloudSyncService.syncServerData(UUID_A, callback);
        respond(0, restoreResponse());
        listeners.get(1).failed(new RuntimeException("simulated timeout"));
        assertEquals(3, requests.size());
        listeners.get(2).failed(new RuntimeException("simulated second timeout"));
        assertEquals(CloudSyncService.RestoreFailure.NETWORK, callback.failure);
        assertTrue(CloudSyncService.hasPendingRestore());
        RecordingRestore resumed = new RecordingRestore();
        CloudSyncService.resumePendingRestore(resumed);
        Bundle commit = new Bundle(); commit.put("ok", true); commit.put("player_uuid", UUID_A);
        respond(3, commit);
        assertEquals(1, resumed.successes);
        assertFalse(CloudSyncService.hasPendingRestore());
    }

    private Bundle restoreResponse() {
        Bundle response = new Bundle(); response.put("ok", true); response.put("player_uuid", UUID_A);
        response.put("restore_token", "test-token");
        Bundle global = new Bundle(); Bundle rankings = new Bundle(); rankings.put("total", 7); rankings.put("latest", -1);
        global.put("rankings", rankings);
        Bundle badges = new Bundle(); badges.put("badges", new String[]{"VICTORY"}); global.put("badges", badges);
        response.put("global_data", global);
        return response;
    }

    private void respond(int index, Bundle body) {
        com.badlogic.gdx.Net.HttpResponse response = (com.badlogic.gdx.Net.HttpResponse) Proxy.newProxyInstance(
                com.badlogic.gdx.Net.HttpResponse.class.getClassLoader(), new Class[]{com.badlogic.gdx.Net.HttpResponse.class},
                (proxy, method, args) -> method.getName().equals("getResultAsString") ? body.toString() : null);
        listeners.get(index).handleHttpResponse(response);
    }

    private static class RecordingCallback implements CloudSyncService.Callback {
        int successes;
        int failures;
        @Override public void onSuccess() { successes++; }
        @Override public void onFailure() { failures++; }
    }

    private static class RecordingRestore implements CloudSyncService.RestoreCallback {
        int successes;
        CloudSyncService.RestoreFailure failure;
        @Override public void onSuccess() { successes++; }
        @Override public void onFailure(CloudSyncService.RestoreFailure failure) { this.failure = failure; }
    }

    private boolean isCurrent(Object context) throws Exception {
        Method method = context.getClass().getDeclaredMethod("isCurrent"); method.setAccessible(true);
        return (Boolean) method.invoke(context);
    }

    private static Object invoke(String name, Class<?>[] types, Object... arguments) throws Exception {
        Method method = CloudSyncService.class.getDeclaredMethod(name, types); method.setAccessible(true);
        return method.invoke(null, arguments);
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field;
    }
}
