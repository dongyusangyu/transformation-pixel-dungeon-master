package com.shatteredpixel.shatteredpixeldungeon.services.cloud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.SaveManager;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.journal.TalentCatalog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.io.ByteArrayInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

public class CloudSyncService {

    private static final String SERVER_URL = "http://103.236.98.149:44141";

    public interface Callback {
        void onSuccess();
        void onFailure();
    }

    public static void uploadLocalData(Callback callback){
        Badges.saveGlobal(true);
        Journal.saveGlobal(true);

        Bundle payload = new Bundle();
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
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(SERVER_URL + "/api/download?device_ip=" + deviceKey() + "&t=" + System.currentTimeMillis());
        request.setTimeOut(15000);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    Bundle response = read(httpResponse.getResultAsString());
                    if (!response.getBoolean("ok")){
                        fail(callback);
                        return;
                    }
                    if (response.contains("global_data")){
                        SaveManager.saveGlobal(response.getBundle("global_data"));
                    }
                    if (response.contains("talent_stats")){
                        TalentCatalog.restoreLocalStats(response.getBundle("talent_stats"));
                        Journal.saveGlobal(true);
                    }
                    if (response.contains("aggregate")){
                        TalentCatalog.restoreServerStats(response.getBundle("aggregate"));
                        Journal.saveGlobal(true);
                    }
                    success(callback);
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

    private static void fetchAggregate(Callback callback){
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(SERVER_URL + "/api/aggregate?t=" + System.currentTimeMillis());
        request.setTimeOut(15000);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    Bundle response = read(httpResponse.getResultAsString());
                    if (response.getBoolean("ok") && response.contains("aggregate")){
                        TalentCatalog.restoreServerStats(response.getBundle("aggregate"));
                        Journal.saveGlobal(true);
                        success(callback);
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
                        if (response.contains("aggregate")){
                            TalentCatalog.restoreServerStats(response.getBundle("aggregate"));
                            Journal.saveGlobal(true);
                        }
                        success(callback);
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

    private static void success(Callback callback){
        Gdx.app.postRunnable(callback::onSuccess);
    }

    private static void fail(Callback callback){
        Gdx.app.postRunnable(callback::onFailure);
    }

    private static String deviceKey(){
        String stored = SPDSettings.cloudDeviceID();
        if (stored == null || stored.isEmpty()){
            stored = UUID.randomUUID().toString();
            SPDSettings.cloudDeviceID(stored);
        }
        return stored;
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
