package com.codearena.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Utility to share the app's APK via Android sharing intents (e.g., WhatsApp).
 *
 * Usage:
 *   ShareHelper.shareApk(this);
 */
public class ShareHelper {
    private static final String AUTHORITY_SUFFIX = ".provider";

    /**
     * Copies the current APK to the app's cache directory and launches a share intent.
     * The Intent is limited to packages that can handle "application/vnd.android.package-archive"
     * such as WhatsApp, Gmail, etc.
     */
    public static void shareApk(Context context) {
        try {
            // Path to the installed APK
            String sourceApkPath = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), 0).sourceDir;
            File sourceApk = new File(sourceApkPath);

            // Destination file in cache
            File cacheDir = new File(context.getCacheDir(), "share");
            if (!cacheDir.mkdirs() && !cacheDir.isDirectory()) {
                // If we cannot create the cache folder, fall back to cacheDir itself
                cacheDir = context.getCacheDir();
            }
            File destApk = new File(cacheDir, "CodeArena.apk");
            copyFile(sourceApk, destApk);

            // Get content URI via FileProvider
            String authority = context.getPackageName() + AUTHORITY_SUFFIX;
            Uri uri = FileProvider.getUriForFile(context, authority, destApk);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.android.package-archive");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // Optionally set package to WhatsApp to open directly:
            // shareIntent.setPackage("com.whatsapp");
            context.startActivity(Intent.createChooser(shareIntent, "Share app via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        if (!dst.exists()) {
            dst.createNewFile();
        }
        try (FileChannel inChannel = new FileInputStream(src).getChannel();
             FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }
}

