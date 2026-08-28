package com.shatteredpixel.shatteredpixeldungeon.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shatteredpixel.shatteredpixeldungeon.SaveManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AndroidMissingNativesHandler extends Activity {

    private ImageView imageView;
    private TextView infoTextView;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private int currentIndex = 0;
    private Bitmap[] bitmaps = new Bitmap[2];
    private static final long SWITCH_DELAY = 2000;

    private String versionName = "???";
    private long versionCode = 0;
    private String installer = "???";
    private String crashInfo;
    private Typeface customTypeface = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crashInfo = getIntent().getStringExtra("CRASH_STACK");

        // 1. 快速显示基础UI（使用默认字体，无任何耗时操作）
        View layout = initBasicUI();
        setContentView(layout);

        // 2. 异步执行所有耗时操作（字体加载、图片加载、包信息查询）
        executor.execute(() -> {
            // 加载自定义字体（耗时）
            loadCustomTypeface();
            //infoTextView.setTypeface(Typeface.DEFAULT);
            // 加载图片（耗时）
            loadBitmaps();
            // 查询包信息（耗时）
            queryPackageInfo();

            // 3. 回到主线程更新UI
            mainHandler.post(() -> {
                // 更新文本内容
                updateInfoText();
                // 更新字体（如果加载成功）
                if (customTypeface != null) {
                    infoTextView.setTypeface(customTypeface);
                }
                // 更新图片并启动轮播
                updateImageView();
            });
        });
    }

    /**
     * 初始化基础UI（无耗时操作，仅创建视图）
     */
    private View initBasicUI() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int horizontalPadding = dp(16);
        int verticalPadding = dp(8);

        // 图片视图（先使用系统默认图标）
        imageView = new ImageView(this);
        imageView.setImageResource(android.R.drawable.ic_dialog_alert);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxHeight(reportImageMaxHeight(metrics.heightPixels, metrics.density));
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        imgParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(imgParams);

        // 文本视图（先使用等宽默认字体，快速显示）
        infoTextView = new TextView(this);
        infoTextView.setText("正在加载错误信息...");
        infoTextView.setTextSize(16);
        infoTextView.setTextColor(0xFFFFFFFF);
        infoTextView.setTypeface(Typeface.MONOSPACE); // 使用系统等宽字体，无延迟
        infoTextView.setGravity(Gravity.CENTER_VERTICAL);
        infoTextView.setPadding(0, dp(8), 0, dp(8));

        // 复制按钮
        Button copyBtn = new Button(this);
        copyBtn.setText("复制错误信息");
        copyBtn.setOnClickListener(v -> copyCrashInfo());

        Button shareBtn = new Button(this);
        shareBtn.setText("分享存档");
        shareBtn.setOnClickListener(v -> shareLatestSave());

        // 退出按钮
        Button exitBtn = new Button(this);
        exitBtn.setText("退出游戏");
        exitBtn.setOnClickListener(v -> {
            executor.shutdownNow();
            System.exit(0);
        });

        LinearLayout actions = new LinearLayout(this);
        boolean verticalActions = useVerticalActionButtons(
                Math.round(metrics.widthPixels / metrics.density));
        actions.setOrientation(verticalActions
                ? LinearLayout.VERTICAL
                : LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        addActionButton(actions, copyBtn, verticalActions);
        addActionButton(actions, shareBtn, verticalActions);
        addActionButton(actions, exitBtn, verticalActions);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(horizontalPadding, verticalPadding,
                horizontalPadding, verticalPadding);
        content.setBackgroundColor(0xFF000000);
        content.addView(imageView, imgParams);
        content.addView(infoTextView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        content.addView(actions, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(0xFF000000);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        return scrollView;
    }

    private void addActionButton(LinearLayout actions, Button button, boolean vertical) {
        int gap = dp(4);
        LinearLayout.LayoutParams params;
        if (vertical) {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, gap / 2, 0, gap / 2);
        } else {
            params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f);
            params.setMargins(gap / 2, 0, gap / 2, 0);
        }
        button.setTextSize(14);
        button.setAllCaps(false);
        actions.addView(button, params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    static boolean useVerticalActionButtons(int widthDp) {
        return widthDp < 360;
    }

    static int reportImageMaxHeight(int screenHeightPx, float density) {
        return Math.min(Math.round(screenHeightPx * 0.4f), Math.round(320 * density));
    }

    /**
     * 异步加载自定义字体
     */
    private void loadCustomTypeface() {
        try {
            customTypeface = Typeface.createFromAsset(getAssets(), "fonts/pixel_font.ttf");
        } catch (Exception e) {
            // 字体加载失败则保持默认字体
            e.printStackTrace();
        }
    }

    /**
     * 异步加载两张图片
     */
    private void loadBitmaps() {
        AssetManager assetManager = getAssets();
        try {
            InputStream is1 = assetManager.open("sprites/bug_report0.png");
            bitmaps[0] = BitmapFactory.decodeStream(is1);
            is1.close();
            InputStream is2 = assetManager.open("sprites/bug_report1.png");
            bitmaps[1] = BitmapFactory.decodeStream(is2);
            is2.close();
        } catch (IOException e) {
            // 加载失败则使用默认图标
            bitmaps[0] = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_alert);
            bitmaps[1] = bitmaps[0];
            e.printStackTrace();
        }
    }

    /**
     * 异步查询包信息（版本、安装器等）
     */
    private void queryPackageInfo() {
        try {
            PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pkgInfo.versionName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                versionCode = pkgInfo.getLongVersionCode();
            } else {
                versionCode = pkgInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                installer = getPackageManager().getInstallSourceInfo(getPackageName()).getInstallingPackageName();
            } else {
                installer = getPackageManager().getInstallerPackageName(getPackageName());
            }
            installer = (installer == null || installer.isEmpty()) ? "unknown" : installer;
        } catch (Exception e) {
            installer = "???";
            e.printStackTrace();
        }
    }

    /**
     * 主线程更新提示文本
     */
    private void updateInfoText() {
        StringBuilder message = new StringBuilder();
        message.append("很抱歉，蜕变地牢崩溃了！\n\n");
        message.append("如果你不能确保自己的游戏版本为最新，请在QQ群 1015634881 或 940628873 重新下载游戏。\n" +
                "若为最新版本，请复制报错信息并在QQ群发送，我们会尝试在下次更新中修复这一问题。\n\n");
        message.append("▸ 版本信息: ").append(versionName).append(" (").append(versionCode).append(")\n");
        message.append("▸ 设备信息: ").append(Build.MODEL).append(" (Android ").append(Build.VERSION.RELEASE).append(")\n");
        infoTextView.setText(message);
    }

    /**
     * 主线程更新图片并启动轮播
     */
    private void updateImageView() {
        if (bitmaps[0] != null) {
            imageView.setImageBitmap(bitmaps[0]);
            startSwitching();
        }
    }

    /**
     * 复制崩溃信息到剪贴板
     */
    private void copyCrashInfo() {
        StringBuilder copyContent = new StringBuilder();
        copyContent.append("▸ 版本信息: ").append(versionName).append(" (").append(versionCode).append(")\n");
        copyContent.append("崩溃堆栈信息:\n").append(crashInfo == null ? "无崩溃堆栈信息" : crashInfo);

        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Crash log", copyContent.toString()));
        Toast.makeText(this, "错误信息已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    /**
     * 图片定时轮播
     */
    private void startSwitching() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !isDestroyed()) {
                    currentIndex = (currentIndex + 1) % bitmaps.length;
                    imageView.setImageBitmap(bitmaps[currentIndex]);
                    mainHandler.postDelayed(this, SWITCH_DELAY);
                }
            }
        }, SWITCH_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
        for (Bitmap bm : bitmaps) {
            if (bm != null && !bm.isRecycled()) {
                bm.recycle();
            }
        }
    }
    /**
     * 分享最新存档
     */
    private void shareLatestSave() {
        new Thread(() -> {
            File saveDir = getFilesDir();
			File[] saves = saveDir.listFiles((dir, name) ->
					name.matches("save-\\d{3}\\.(json|checkpoint)"));
            if (saves == null || saves.length == 0) {
                runOnUiThread(() -> Toast.makeText(this, "没有找到存档文件", Toast.LENGTH_SHORT).show());
                return;
            }

            Arrays.sort(saves, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
			String name = saves[0].getName();
			int slot = Integer.parseInt(name.substring(5, 8));
			prepareAndShareSave(slot);
        }).start();
    }

    /**
     * 分享指定槽位的存档
     * @param slot 存档槽位 (1-12)
     */
    public void shareSaveBySlot(int slot) {
        new Thread(() -> {
			if (!SaveManager.saveExists(slot)) {
                runOnUiThread(() -> Toast.makeText(this, "存档槽 " + slot + " 不存在", Toast.LENGTH_SHORT).show());
                return;
            }

			prepareAndShareSave(slot);
        }).start();
    }

	private void prepareAndShareSave(int slot) {
		String filename = String.format("shared-save-%03d.json", slot);
		try {
			SaveManager.writePortableSave(slot, filename);
			shareSaveFile(new File(getFilesDir(), filename));
		} catch (IOException | RuntimeException e) {
			runOnUiThread(() -> Toast.makeText(this, "存档打包失败", Toast.LENGTH_SHORT).show());
		}
	}

    /**
     * 分享指定的存档文件
     * @param saveFile 存档文件
     */
    private void shareSaveFile(File saveFile) {
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = getPackageName() + ".fileprovider";
            try {
                fileUri = FileProvider.getUriForFile(this, authority, saveFile);
            } catch (IllegalArgumentException e) {
                runOnUiThread(() -> Toast.makeText(this, "FileProvider 配置错误", Toast.LENGTH_SHORT).show());
                return;
            }
        } else {
            fileUri = Uri.fromFile(saveFile);
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/json");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "蜕变地牢存档");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "分享存档: " + saveFile.getName());
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        runOnUiThread(() -> {
            try {
                startActivity(Intent.createChooser(shareIntent, "分享存档到..."));
            } catch (Exception e) {
                Toast.makeText(this, "无法分享", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
