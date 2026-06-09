/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewConfiguration;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidAudio;
import com.badlogic.gdx.backends.android.AsynchronousAndroidAudio;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.news.NewsImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.UpdateImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AndroidLauncher extends AndroidApplication {
	
	public static AndroidApplication instance;
	
	private static AndroidPlatformSupport support;
    public Handler killHandler;
    public Runnable killRunnable;
	
	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



		// 确保 instance 已赋值（全局异常处理器中会用到）
		if (instance == null) {
			instance = this;
		}

		// 设置全局未捕获异常处理器，捕获所有 Java 层未处理异常

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Intent intent = new Intent(AndroidLauncher.this, AndroidMissingNativesHandler.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // 将异常栈转为字符串，通过Intent传递
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                intent.putExtra("CRASH_STACK", sw.toString());
                startActivity(intent);
                // 延迟杀死进程，确保Activity启动完成

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }, 100);
            }
        });





        try {
            GdxNativesLoader.load();
            FreeType.initFreeType();
        } catch (Exception e) {
            Intent intent = new Intent(this, AndroidMissingNativesHandler.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            intent.putExtra("CRASH_STACK", sw.toString());
            startActivity(intent);
            finish();
            return;
        }

		// 正常的游戏初始化（以下代码与原来相同）
		if (instance == null) {
			instance = this;
		}

		try {
			Game.version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Game.version = "???";
		}
		try {
			Game.versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			Game.versionCode = 0;
		}

		if (UpdateImpl.supportsUpdates()) {
			Updates.service = UpdateImpl.getUpdateService();
		}
		if (NewsImpl.supportsNews()) {
			News.service = NewsImpl.getNewsService();
		}

		FileUtils.setDefaultFileProperties(Files.FileType.Local, "");
		SPDSettings.set(instance.getPreferences("ShatteredPixelDungeon"));

		if (SPDSettings.landscape() != null) {
			instance.setRequestedOrientation(SPDSettings.landscape() ?
					ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE :
					ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.depth = 0;
		config.useCompass = false;
		config.useAccelerometer = false;

		if (support == null) support = new AndroidPlatformSupport();
		else support.reloadGenerators();

		support.updateSystemUI();

		Button.longClick = ViewConfiguration.getLongPressTimeout() / 1000f;

		initialize(new ShatteredPixelDungeon(support), config);
		
	}

	@Override
	public AndroidAudio createAudio(Context context, AndroidApplicationConfiguration config) {
		return new AsynchronousAndroidAudio(context, config);
	}

	@Override
	protected void onResume() {
		if (instance != this) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				finishAndRemoveTask();
			} else {
				finish();
			}
		}
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		// do nothing, game should catch all back presses
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		support.updateSystemUI();
	}

	@Override
	public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
		super.onMultiWindowModeChanged(isInMultiWindowMode);
		support.updateSystemUI();
	}
}