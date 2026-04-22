package com.dts.roadp;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class CrashReporter {

    private CrashReporter() {}

    public static void install(Context appCtx) {
        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                // 1) Escribe/append del crash en tu carpeta app (ROAD/Logs/crash.txt)
                File logsDir = new File(new File(appCtx.getExternalFilesDir(null), "ROAD"), "Logs");
                if (!logsDir.exists()) logsDir.mkdirs();
                File crashFile = new File(logsDir, "crash.txt");

                try (PrintWriter pw = new PrintWriter(new FileWriter(crashFile, true))) {
                    String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
                    pw.println(ts + " | Thread: " + t.getName());
                    e.printStackTrace(pw);
                    pw.println("----");
                }

                // 2) Snapshot a Downloads con timestamp (sin UI, vía MediaStore)
                String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new java.util.Date());
                String display = "road_crash_" + stamp + ".txt";
                Backups.exportFileToDownloads(appCtx, crashFile, display, "text/plain");

                // 3) (Opcional) si llevas un log “runtime.txt”, exporta también
                File runtimeFile = new File(logsDir, "runtime.txt");
                if (runtimeFile.exists()) {
                    String display2 = "road_runtime_" + stamp + ".txt";
                    Backups.exportFileToDownloads(appCtx, runtimeFile, display2, "text/plain");
                }

            } catch (Throwable ignore) {
                // Nunca lances excepciones desde aquí
            }

            // Deja que el sistema cierre la app
            if (prev != null) prev.uncaughtException(t, e);
            else {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        });
    }
}

