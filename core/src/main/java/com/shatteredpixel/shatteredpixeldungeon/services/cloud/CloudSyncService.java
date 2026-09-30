package com.shatteredpixel.shatteredpixeldungeon.services.cloud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SaveManager;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.journal.TalentCatalog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CloudSyncService {

    private static final String SERVER_URL = "http://103.236.98.149:44141";
    private static final String PENDING_RESTORE_FILE = "cloud_restore_pending.dat";
    private static final AtomicLong restoreRequests = new AtomicLong();
    private static final AtomicLong uploadRequests = new AtomicLong();
    private static final AtomicLong aggregateRequests = new AtomicLong();

    public interface Callback {
        void onSuccess();
        void onFailure();
    }

    public enum RestoreFailure {
        INVALID_UUID,
        NOT_FOUND,
        NOT_ALLOWED,
        INVALID_RESPONSE,
        STALE,
        NETWORK
    }

    public interface RestoreCallback {
        void onSuccess();
        void onFailure(RestoreFailure failure);
    }

    public static void uploadLocalData(Callback callback){
        Badges.loadGlobal();
        Journal.loadGlobal();
        Badges.saveGlobal(true);
        Journal.saveGlobal(true);

        Bundle payload = new Bundle();
        payload.put("player_uuid", playerUUID());
        payload.put("device_key", stableDeviceKey());
        payload.put("device_ip", deviceKey());
        payload.put("local_ip", localIp());
        payload.put("version", Game.versionCode);
        payload.put("global_data", SaveManager.loadGlobal());
        payload.put("talent_stats", TalentCatalog.localStatsBundle());

        post("/api/upload", payload, new Callback() {
            @Override
            public void onSuccess() {
                fetchAggregate(callback);
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }
        });
    }

    public static void syncServerData(Callback callback){
        syncServerData(playerUUID(), new RestoreCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(RestoreFailure failure) {
                callback.onFailure();
            }
        });
    }

    public static void syncServerData(String requestedUUID, RestoreCallback callback){
        String normalizedUUID = CloudRestoreIdentity.normalize(requestedUUID);
        if (normalizedUUID == null){
            restoreFail(callback, RestoreFailure.INVALID_UUID);
            return;
        }
        final RestoreContext context;
        try {
            deviceKey();
            Badges.saveGlobal();
            Journal.saveGlobal();
            context = newRestoreContext();
        } catch (Exception e) {
            ShatteredPixelDungeon.reportException(e);
            restoreFail(callback, RestoreFailure.INVALID_RESPONSE);
            return;
        }
        String restoreDeviceKey = stableDeviceKey();

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(SERVER_URL + "/api/download?player_uuid=" + encode(normalizedUUID)
                + "&device_key=" + encode(restoreDeviceKey)
                + "&device_ip=" + encode(deviceKey())
                + "&prepare_restore=1"
                + "&t=" + System.currentTimeMillis());
        request.setTimeOut(15000);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    Bundle response = read(httpResponse.getResultAsString());
                    if (!response.getBoolean("ok")){
                        restoreFail(callback, restoreFailure(response));
                        return;
                    }
                    String restoredUUID = CloudRestoreIdentity.resolveRestoredUUID(
                            normalizedUUID,
                            response.getString("player_uuid")
                    );
                    String restoreToken = response.getString("restore_token");
                    if (restoredUUID == null || restoreToken == null || restoreToken.isEmpty()
                            || !response.contains("global_data")){
                        restoreFail(callback, RestoreFailure.INVALID_RESPONSE);
                        return;
                    }
                    applyPreparedRestoreOnGameThread(
                            response,
                            restoredUUID,
                            restoreDeviceKey,
                            restoreToken,
                            context,
                            callback
                    );
                } catch (Exception e){
                    ShatteredPixelDungeon.reportException(e);
                    restoreFail(callback, RestoreFailure.INVALID_RESPONSE);
                }
            }

            @Override
            public void failed(Throwable t) {
                restoreFail(callback, RestoreFailure.NETWORK);
            }

            @Override
            public void cancelled() {
                restoreFail(callback, RestoreFailure.NETWORK);
            }
        });
    }

    private static void fetchAggregate(Callback callback){
        long requestID = aggregateRequests.incrementAndGet();
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(SERVER_URL + "/api/aggregate?t=" + System.currentTimeMillis());
        request.setTimeOut(15000);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    Bundle response = read(httpResponse.getResultAsString());
                    if (response.getBoolean("ok") && response.contains("aggregate")){
                        applyAggregateOnGameThread(response.getBundle("aggregate"), requestID, callback);
                    } else {
                        fail(callback);
                    }
                } catch (Exception e){
                    ShatteredPixelDungeon.reportException(e);
                    fail(callback);
                }
            }

            @Override
            public void failed(Throwable t) {
                fail(callback);
            }

            @Override
            public void cancelled() {
                fail(callback);
            }
        });
    }

    private static void post(String path, Bundle payload, Callback callback){
        long requestID = uploadRequests.incrementAndGet();
        long restoreID = restoreRequests.get();
        String expectedUUID = playerUUID();
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(SERVER_URL + path);
        request.setHeader("Content-Type", "application/json; charset=utf-8");
        request.setContent(payload.toString());
        request.setTimeOut(15000);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    Bundle response = read(httpResponse.getResultAsString());
                    if (response.getBoolean("ok")){
                        Gdx.app.postRunnable(() -> {
                            try {
                                if (requestID != uploadRequests.get() || restoreID != restoreRequests.get()
                                        || !expectedUUID.equals(playerUUID())) {
                                    callback.onFailure();
                                    return;
                                }
                                applyUploadResponse(response, expectedUUID);
                                callback.onSuccess();
                            } catch (Exception e) {
                                ShatteredPixelDungeon.reportException(e);
                                callback.onFailure();
                            }
                        });
                    } else {
                        fail(callback);
                    }
                } catch (Exception e){
                    ShatteredPixelDungeon.reportException(e);
                    fail(callback);
                }
            }

            @Override
            public void failed(Throwable t) {
                fail(callback);
            }

            @Override
            public void cancelled() {
                fail(callback);
            }
        });
    }

    private static Bundle read(String json) throws Exception {
        return Bundle.read(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8.name())));
    }

    private static void applyUploadResponse(Bundle response, String expectedUUID){
        if (response.getBoolean("blacklisted")) return;
        String uuid = CloudRestoreIdentity.resolveRestoredUUID(expectedUUID, response.getString("player_uuid"));
        if (uuid == null) throw new IllegalArgumentException("Invalid upload identity");
        SPDSettings.cloudPlayerUUID(uuid);
        // Upload responses are acknowledgements, never permission to restore personal data.
        if (response.contains("rankings_accepted") && !response.getBoolean("rankings_accepted")) {
            throw new IllegalArgumentException("Outdated rankings; explicit restore or reset required");
        }
    }

    private static void applyAggregateOnGameThread(Bundle aggregate, long requestID, Callback callback){
        Gdx.app.postRunnable(() -> {
            try {
                if (requestID != aggregateRequests.get()) {
                    callback.onSuccess();
                    return;
                }
                TalentCatalog.restoreServerStats(aggregate);
                Journal.saveGlobal(true);
                callback.onSuccess();
            } catch (Exception e){
                ShatteredPixelDungeon.reportException(e);
                callback.onFailure();
            }
        });
    }

    private static void applyPreparedRestoreOnGameThread(
            Bundle response,
            String restoredUUID,
            String restoreDeviceKey,
            String restoreToken,
            RestoreContext context,
            RestoreCallback callback
    ){
        Gdx.app.postRunnable(() -> {
            String currentDeviceID = SPDSettings.cloudDeviceID();
            try {
                Badges.saveGlobal();
                Journal.saveGlobal();
                if (!context.isCurrent()) {
                    callback.onFailure(RestoreFailure.STALE);
                    return;
                }
                savePendingRestore(
                        response,
                        restoredUUID,
                        restoreDeviceKey,
                        restoreToken,
                        currentDeviceID,
                        context,
                        false
                );
                commitRestore(
                        response,
                        restoredUUID,
                        restoreDeviceKey,
                        restoreToken,
                        currentDeviceID,
                        context,
                        callback,
                        1
                );
            } catch (Exception e){
                ShatteredPixelDungeon.reportException(e);
                callback.onFailure(RestoreFailure.INVALID_RESPONSE);
            }
        });
    }

    public static boolean hasPendingRestore(){
        return FileUtils.fileExists(PENDING_RESTORE_FILE);
    }

    public static void resumePendingRestore(RestoreCallback callback){
        try {
            Badges.saveGlobal();
            Journal.saveGlobal();
            Bundle pending = FileUtils.bundleFromFile(PENDING_RESTORE_FILE);
            Bundle response = pending.getBundle("response");
            String restoredUUID = CloudRestoreIdentity.resolveRestoredUUID(
                    "",
                    pending.getString("player_uuid")
            );
            String responseUUID = CloudRestoreIdentity.resolveRestoredUUID(
                    restoredUUID,
                    response.getString("player_uuid")
            );
            String restoreDeviceKey = pending.getString("device_key");
            String restoreToken = pending.getString("restore_token");
            String currentDeviceID = pending.getString("local_device_id");
            RestoreContext context = new RestoreContext(restoreRequests.incrementAndGet(),
                    pending.getString("local_fingerprint"));
            if (
                    restoredUUID == null
                    || responseUUID == null
                    || restoreDeviceKey == null
                    || restoreDeviceKey.isEmpty()
                    || restoreToken == null
                    || restoreToken.isEmpty()
                    || !response.contains("global_data")
            ){
			FileUtils.deleteBundleFile(PENDING_RESTORE_FILE);
                restoreFail(callback, RestoreFailure.INVALID_RESPONSE);
                return;
            }
            if (!context.isCurrent()) {
                discardPendingRestore(restoreToken);
                restoreFail(callback, RestoreFailure.STALE);
                return;
            }
            if (pending.getBoolean("committed")){
                finishCommittedRestoreOnGameThread(
                        response,
                        restoredUUID,
                        restoreDeviceKey,
                        restoreToken,
                        currentDeviceID,
                        context,
                        callback
                );
            } else {
                commitRestore(
                        response,
                        restoredUUID,
                        restoreDeviceKey,
                        restoreToken,
                        currentDeviceID,
                        context,
                        callback,
                        1
                );
            }
        } catch (Exception e){
            ShatteredPixelDungeon.reportException(e);
			FileUtils.deleteBundleFile(PENDING_RESTORE_FILE);
            restoreFail(callback, RestoreFailure.INVALID_RESPONSE);
        }
    }

    private static void savePendingRestore(
            Bundle response,
            String restoredUUID,
            String restoreDeviceKey,
            String restoreToken,
            String currentDeviceID,
            RestoreContext context,
            boolean committed
    ) throws Exception {
        Bundle pending = new Bundle();
        pending.put("response", response);
        pending.put("player_uuid", restoredUUID);
        pending.put("device_key", restoreDeviceKey);
        pending.put("restore_token", restoreToken);
        pending.put("local_device_id", currentDeviceID == null ? "" : currentDeviceID);
        pending.put("committed", committed);
        pending.put("local_fingerprint", context.fingerprint);
        FileUtils.bundleToFile(PENDING_RESTORE_FILE, pending);
    }

    private static void commitRestore(
            Bundle response,
            String restoredUUID,
            String restoreDeviceKey,
            String restoreToken,
            String currentDeviceID,
            RestoreContext context,
            RestoreCallback callback,
            int retriesRemaining
    ){
        if (context.requestID != restoreRequests.get()) {
            restoreFail(callback, RestoreFailure.STALE);
            return;
        }
        Bundle payload = new Bundle();
        payload.put("player_uuid", restoredUUID);
        payload.put("device_key", restoreDeviceKey);
        payload.put("restore_token", restoreToken);

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(SERVER_URL + "/api/restore/commit");
        request.setHeader("Content-Type", "application/json; charset=utf-8");
        request.setContent(payload.toString());
        request.setTimeOut(15000);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    Bundle commitResponse = read(httpResponse.getResultAsString());
                    String committedUUID = CloudRestoreIdentity.resolveRestoredUUID(
                            restoredUUID,
                            commitResponse.getString("player_uuid")
                    );
                    if (commitResponse.getBoolean("ok") && committedUUID != null){
                        finishCommittedRestoreOnGameThread(
                                response,
                                committedUUID,
                                restoreDeviceKey,
                                restoreToken,
                                currentDeviceID,
                                context,
                                callback
                        );
                    } else {
                        discardPendingRestore(restoreToken);
                        restoreFail(callback, RestoreFailure.INVALID_RESPONSE);
                    }
                } catch (Exception e){
                    ShatteredPixelDungeon.reportException(e);
                    retryCommitOrKeepPending(
                            response,
                            restoredUUID,
                            restoreDeviceKey,
                            restoreToken,
                            currentDeviceID,
                            context,
                            callback,
                            retriesRemaining
                    );
                }
            }

            @Override
            public void failed(Throwable t) {
                retryCommitOrKeepPending(
                        response,
                        restoredUUID,
                        restoreDeviceKey,
                        restoreToken,
                        currentDeviceID,
                        context,
                        callback,
                        retriesRemaining
                );
            }

            @Override
            public void cancelled() {
                failed(null);
            }
        });
    }

    private static void retryCommitOrKeepPending(
            Bundle response,
            String restoredUUID,
            String restoreDeviceKey,
            String restoreToken,
            String currentDeviceID,
            RestoreContext context,
            RestoreCallback callback,
            int retriesRemaining
    ){
        Gdx.app.postRunnable(() -> {
            try {
                Badges.saveGlobal();
                Journal.saveGlobal();
                if (!context.isCurrent()) {
                    discardPendingRestore(restoreToken);
                    callback.onFailure(RestoreFailure.STALE);
                    return;
                }
                if (retriesRemaining > 0) {
                    commitRestore(response, restoredUUID, restoreDeviceKey, restoreToken,
                            currentDeviceID, context, callback, retriesRemaining - 1);
                } else {
                    callback.onFailure(RestoreFailure.NETWORK);
                }
            } catch (Exception e) {
                ShatteredPixelDungeon.reportException(e);
                callback.onFailure(RestoreFailure.INVALID_RESPONSE);
            }
        });
    }

    private static void finishCommittedRestoreOnGameThread(
            Bundle response,
            String restoredUUID,
            String restoreDeviceKey,
            String restoreToken,
            String currentDeviceID,
            RestoreContext context,
            RestoreCallback callback
    ){
        Gdx.app.postRunnable(() -> {
            boolean applying = false;
            try {
                Badges.saveGlobal();
                Journal.saveGlobal();
                if (!context.isCurrent()) {
                    discardPendingRestore(restoreToken);
                    callback.onFailure(RestoreFailure.STALE);
                    return;
                }
                savePendingRestore(
                        response,
                        restoredUUID,
                        restoreDeviceKey,
                        restoreToken,
                        currentDeviceID,
                        context,
                        true
                );
                applying = true;
                restoreLocalIdentity(restoredUUID, currentDeviceID);
                applyRestoreResponse(response);
                restoreLocalIdentity(restoredUUID, currentDeviceID);
				FileUtils.deleteBundleFile(PENDING_RESTORE_FILE);
                callback.onSuccess();
            } catch (Exception e){
                ShatteredPixelDungeon.reportException(e);
                if (applying) {
                    restoreLocalIdentity(restoredUUID, currentDeviceID);
                    try {
                        savePendingRestore(response, restoredUUID, restoreDeviceKey, restoreToken, currentDeviceID,
                                new RestoreContext(context.requestID, localDataFingerprint()), true);
                    } catch (Exception saveError) {
                        ShatteredPixelDungeon.reportException(saveError);
                    }
                }
                callback.onFailure(RestoreFailure.INVALID_RESPONSE);
            }
        });
    }

    private static void restoreLocalIdentity(String playerUUID, String deviceID){
        SPDSettings.cloudDeviceID(deviceID == null ? "" : deviceID);
        SPDSettings.cloudPlayerUUID(playerUUID == null ? "" : playerUUID);
    }

    private static void applyRestoreResponse(Bundle response) throws Exception {
        boolean journalChanged = false;
        String restoredUUID = null;
        if (response.contains("player_uuid")){
            String uuid = response.getString("player_uuid");
            if (uuid != null && !uuid.isEmpty()){
                restoredUUID = uuid;
            }
        }
        if (response.contains("global_data")){
            SaveManager.saveGlobal(response.getBundle("global_data"));
            refreshGlobalState();
        }
        if (response.contains("talent_stats")){
            TalentCatalog.restoreLocalStats(response.getBundle("talent_stats"));
            journalChanged = true;
        }
        if (response.contains("aggregate")){
            TalentCatalog.restoreServerStats(response.getBundle("aggregate"));
            journalChanged = true;
        }
        if (journalChanged){
            Journal.saveGlobal(true);
        }
        if (restoredUUID != null){
            SPDSettings.cloudPlayerUUID(restoredUUID);
        }
    }

    private static void refreshGlobalState(){
        Badges.reloadGlobal();
        Rankings.INSTANCE.records = null;
        Rankings.INSTANCE.latestDaily = null;
        Rankings.INSTANCE.latestDailyReplay = null;
        Rankings.INSTANCE.dailyScoreHistory.clear();
        Rankings.INSTANCE.load();
        Journal.reloadGlobal();
    }

    private static RestoreContext newRestoreContext() throws Exception {
        return new RestoreContext(restoreRequests.incrementAndGet(), localDataFingerprint());
    }

    private static final class RestoreContext {
        final long requestID;
        final String fingerprint;

        RestoreContext(long requestID, String fingerprint) {
            this.requestID = requestID;
            this.fingerprint = fingerprint;
        }

        boolean isCurrent() throws Exception {
            return requestID == restoreRequests.get() && fingerprint != null && !fingerprint.isEmpty()
                    && fingerprint.equals(localDataFingerprint());
        }
    }

    private static String localDataFingerprint() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update((playerUUID() + "\0" + String.valueOf(SPDSettings.cloudDeviceID()) + "\0")
                .getBytes(StandardCharsets.UTF_8));
        for (String file : new String[]{Rankings.RANKINGS_FILE, Badges.BADGES_FILE, Journal.JOURNAL_FILE}) {
            digest.update(file.getBytes(StandardCharsets.UTF_8));
            if (FileUtils.fileExists(file)) {
                byte[] bytes = FileUtils.getFileHandle(file).readBytes();
                digest.update((":" + bytes.length + ":").getBytes(StandardCharsets.UTF_8));
                digest.update(bytes);
            } else {
                digest.update((byte) 0);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder result = new StringBuilder();
        for (byte value : hash) result.append(String.format(java.util.Locale.ROOT, "%02x", value & 0xff));
        return result.toString();
    }

    private static void discardPendingRestore(String token) {
        Gdx.app.postRunnable(() -> {
            try {
                if (hasPendingRestore() && token.equals(FileUtils.bundleFromFile(PENDING_RESTORE_FILE).getString("restore_token"))) {
                    FileUtils.deleteBundleFile(PENDING_RESTORE_FILE);
                }
            } catch (Exception e) {
                ShatteredPixelDungeon.reportException(e);
            }
        });
    }

    private static void fail(Callback callback){
        Gdx.app.postRunnable(callback::onFailure);
    }

    private static void restoreFail(RestoreCallback callback, RestoreFailure failure){
        Gdx.app.postRunnable(() -> callback.onFailure(failure));
    }

    private static RestoreFailure restoreFailure(Bundle response){
        String error = response.getString("error");
        if ("not_found".equals(error)){
            return RestoreFailure.NOT_FOUND;
        }
        if ("restore_not_allowed".equals(error)){
            return RestoreFailure.NOT_ALLOWED;
        }
        return RestoreFailure.INVALID_RESPONSE;
    }

    private static String deviceKey(){
        String stored = SPDSettings.cloudDeviceID();
        if (stored == null || stored.isEmpty()){
            stored = UUID.randomUUID().toString();
            SPDSettings.cloudDeviceID(stored);
        }
        return stored;
    }

    private static String playerUUID(){
        String stored = SPDSettings.cloudPlayerUUID();
        return stored == null ? "" : stored;
    }

    public static String currentPlayerUUID(){
        String stored = SPDSettings.cloudPlayerUUID();
        return stored == null ? "" : stored;
    }

    private static String stableDeviceKey(){
        String fingerprint = Game.platform.cloudDeviceFingerprint();
        if (fingerprint != null && !fingerprint.isEmpty()){
            return fingerprint;
        }
        return "local:" + deviceKey();
    }

    private static String encode(String value){
        try {
            return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return "";
        }
    }

    private static String localIp(){
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()){
                    continue;
                }
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()){
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e){
            ShatteredPixelDungeon.reportException(e);
        }
        return null;
    }

}
