package com.dts.roadp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.nio.channels.FileChannel;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public final class Backups {

    private Backups() {}

    public static final class Output {
        // Copias en ROAD/
        public final File roadDb, roadWal, roadShm;
        // Copias en Downloads
        public final Uri downloadsDb, downloadsWal, downloadsShm;

        public Output(File rDb, File rWal, File rShm, Uri dDb, Uri dWal, Uri dShm) {
            this.roadDb = rDb; this.roadWal = rWal; this.roadShm = rShm;
            this.downloadsDb = dDb; this.downloadsWal = dWal; this.downloadsShm = dShm;
        }
    }

    /**
     * Hace backup a ROAD/ (cíclico por día) y a Descargas (con timestamp para no sobrescribir).
     * @param c Context (usa Activity/Service o appGlobals.app()).
     * @param dia Número de día (1..7) para el nombre cíclico: road<dia>.db.
     * @param timestampDownloads Si true, agrega timestamp en Downloads (recomendado).
     */
    public static Output backupDbBoth(Context c, long dia, boolean timestampDownloads) throws Exception {
        if (dia < 1 || dia > 7) dia = 0;  // por si acaso

        // 0) Fuente correcta: base de datos interna
        File srcDb  = c.getDatabasePath("road.db");
        File srcWal = new File(srcDb.getPath() + "-wal");
        File srcShm = new File(srcDb.getPath() + "-shm");
        if (!srcDb.exists()) throw new FileNotFoundException("No existe road.db en: " + srcDb.getAbsolutePath());

        // 1) Checkpoint WAL (si aplica) para mayor consistencia
        safeCheckpoint(srcDb);

        // 2) Copia a carpeta de la app: .../Android/data/<pkg>/files/ROAD/
        File roadDir = AppPaths.road(c);
        String baseRoad = (dia > 0 ? ("road" + dia) : "road") + ".db";

        File dstDb  = new File(roadDir, baseRoad);
        copyFileFast(srcDb, dstDb);

        File dstWal = null, dstShm = null;
        if (srcWal.exists()) {
            dstWal = new File(roadDir, baseRoad + "-wal");   // ej: road3.db-wal
            copyFileFast(srcWal, dstWal);
        }
        if (srcShm.exists()) {
            dstShm = new File(roadDir, baseRoad + "-shm");   // ej: road3.db-shm
            copyFileFast(srcShm, dstShm);
        }

        // 3) Copia a Descargas (visible)
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String dbName  = timestampDownloads ? ("road_" + ts + ".db")      : "road.db";
        String walName = timestampDownloads ? ("road_" + ts + ".db-wal")  : "road.db-wal";
        String shmName = timestampDownloads ? ("road_" + ts + ".db-shm")  : "road.db-shm";

        Uri dDb  = writeToDownloads(c, srcDb,  dbName);
        Uri dWal = srcWal.exists() ? writeToDownloads(c, srcWal, walName) : null;
        Uri dShm = srcShm.exists() ? writeToDownloads(c, srcShm, shmName) : null;

        return new Output(dstDb, dstWal, dstShm, dDb, dWal, dShm);
    }

    // --- helpers ---

    /** Minimiza datos pendientes en WAL; seguro si no estás en WAL. */
    private static void safeCheckpoint(File dbFile) {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            db.execSQL("PRAGMA wal_checkpoint(FULL)");
            db.close();
        } catch (Throwable ignored) {}
    }

    private static void copyFileFast(File src, File dst) throws IOException {
        if (dst.getParentFile() != null && !dst.getParentFile().exists()) dst.getParentFile().mkdirs();
        try (FileChannel in = new FileInputStream(src).getChannel();
             FileChannel out = new FileOutputStream(dst).getChannel()) {
            long size = in.size(), pos = 0;
            while (pos < size) pos += in.transferTo(pos, size - pos, out);
            out.force(true);
        }
    }

    private static Uri writeToDownloads(Context c, File src, String displayName) throws Exception {
        if (!src.exists() || !src.isFile())
            throw new FileNotFoundException("No existe: " + src.getAbsolutePath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver cr = c.getContentResolver();
            ContentValues v = new ContentValues();
            v.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
            v.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
            v.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
            if (uri == null) throw new IOException("No se pudo crear entrada en Downloads.");

            try (InputStream in = new FileInputStream(src);
                 OutputStream out = cr.openOutputStream(uri)) {
                copyStream(in, out);
            }

            v.clear(); v.put(MediaStore.Downloads.IS_PENDING, 0);
            cr.update(uri, v, null, null);
            return uri;
        } else {
            // Android 9 o menor (requiere WRITE_EXTERNAL_STORAGE si compilas para ≤28)
            File pub = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!pub.exists()) pub.mkdirs();
            File dst = new File(pub, displayName);
            try (InputStream in = new FileInputStream(src);
                 OutputStream out = new FileOutputStream(dst)) {
                copyStream(in, out);
            }
            return Uri.fromFile(dst);
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192]; int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        out.flush();
        if (out instanceof FileOutputStream) ((FileOutputStream) out).getFD().sync();
    }

    // Backups.java  (añade debajo de los otros helpers)
    public static Uri copyAnyToDownloads(Context ctx, File src,
                                         @Nullable String displayName,
                                         @Nullable String mimeType,
                                         boolean addTimestamp) throws Exception {
        if (!src.exists() || !src.isFile()) {
            throw new FileNotFoundException(src.getAbsolutePath());
        }

        // Nombre a mostrar (usa el del archivo si no te pasan uno)
        String name = (displayName == null || displayName.isEmpty())
                ? src.getName() : displayName;

        if (addTimestamp) {
            int dot = name.lastIndexOf('.');
            String ts = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss",
                    java.util.Locale.US).format(new java.util.Date());
            name = (dot > 0 && dot < name.length() - 1)
                    ? name.substring(0, dot) + "_" + ts + name.substring(dot)
                    : name + "_" + ts;
        }

        String mt = (mimeType == null || mimeType.isEmpty())
                ? "application/octet-stream" : mimeType;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.content.ContentResolver cr = ctx.getContentResolver();
            android.content.ContentValues v = new android.content.ContentValues();
            v.put(android.provider.MediaStore.Downloads.DISPLAY_NAME, name);
            v.put(android.provider.MediaStore.Downloads.MIME_TYPE, mt);
            v.put(android.provider.MediaStore.Downloads.IS_PENDING, 1);

            android.net.Uri uri = cr.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
            if (uri == null) throw new java.io.IOException("No se pudo crear entrada en Downloads");

            try (java.io.InputStream in = new java.io.FileInputStream(src);
                 java.io.OutputStream out = cr.openOutputStream(uri)) {
                byte[] buf = new byte[8192]; int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                out.flush();
            }

            v.clear(); v.put(android.provider.MediaStore.Downloads.IS_PENDING, 0);
            cr.update(uri, v, null, null);
            return uri;
        } else {
            java.io.File pub = android.os.Environment
                    .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            if (!pub.exists()) pub.mkdirs();
            java.io.File dst = new java.io.File(pub, name);
            try (java.io.InputStream in = new java.io.FileInputStream(src);
                 java.io.OutputStream out = new java.io.FileOutputStream(dst)) {
                byte[] buf = new byte[8192]; int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                out.flush();
                if (out instanceof java.io.FileOutputStream)
                    ((java.io.FileOutputStream) out).getFD().sync();
            }
            return android.net.Uri.fromFile(dst);
        }
    }

    // Sobrecarga por si tienes un String con la ruta
    public static android.net.Uri copyAnyToDownloads(Context ctx, String srcPath,
                                                     boolean addTimestamp) throws Exception {
        return copyAnyToDownloads(ctx, new java.io.File(srcPath), null, null, addTimestamp);
    }

    /** Copia un archivo a Descargas/ROAD usando MediaStore (Q+) o carpeta pública (L–P). */
    public static Uri saveToDownloads(Context ctx, File src, String displayName, String mimeType) throws IOException {
        if (src == null || !src.exists() || src.length() == 0) {
            throw new FileNotFoundException("Origen no existe o vacío: " + src);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver cr = ctx.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
            cv.put(MediaStore.MediaColumns.MIME_TYPE,
                    (mimeType == null || mimeType.isEmpty()) ? "application/octet-stream" : mimeType);
            cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ROAD");

            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri item = cr.insert(collection, cv);
            if (item == null) throw new IOException("No se pudo crear item en MediaStore");

            try (InputStream in = new FileInputStream(src);
                 OutputStream out = cr.openOutputStream(item, "w")) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                out.flush();
            }
            return item;

        } else {
            File dstDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "ROAD");
            if (!dstDir.exists() && !dstDir.mkdirs()) {
                throw new IOException("No se pudo crear carpeta: " + dstDir.getAbsolutePath());
            }
            File dst = new File(dstDir, displayName);
            try (InputStream in = new FileInputStream(src);
                 OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                out.flush();
            }
            try { android.media.MediaScannerConnection.scanFile(
                    ctx, new String[]{ dst.getAbsolutePath() }, null, null); } catch (Exception ignore) {}
            return Uri.fromFile(dst);
        }
    }

    /** Conveniencia: hace checkpoint y respalda road.db a Descargas/ROAD. */
    public static Uri backupRoadDbToDownloads(Context ctx) throws IOException {
        // Si tu helper te da la DB abierta, haz checkpoint para WAL
        if (ctx instanceof PBase) {
            BaseDatos con = ((PBase) ctx).Con;
            if (con != null && con.vDatabase != null && con.vDatabase.isOpen()) {
                try { con.vDatabase.execSQL("PRAGMA wal_checkpoint(FULL)"); } catch (Exception ignored) {}
            }
        }
        File srcDb = ctx.getDatabasePath("road.db");
        String ts = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(new java.util.Date());
        String rutaSafe = (appGlobals.app() != null && ((appGlobals) appGlobals.app()).ruta != null)
                ? ((appGlobals) appGlobals.app()).ruta.replaceAll("[^A-Za-z0-9_-]", "_")
                : "SINRUTA";
        String name = "road_" + rutaSafe + "_" + ts + ".db";
        return saveToDownloads(ctx, srcDb, name, "application/vnd.sqlite3");
    }

    /** /sdcard/Android/data/<pkg>/files/ROAD/Logs */
    public static File getRoadLogsDir(Context ctx) {
        File base = ctx.getExternalFilesDir(null);
        File road = new File(base, "ROAD");
        File logs = new File(road, "Logs");
        if (!logs.exists()) logs.mkdirs();
        return logs;
    }

    /** Crash log que venimos usando. Cambia el nombre si el tuyo es otro. */
    public static File getCrashLogFile(Context ctx) {
        return new File(getRoadLogsDir(ctx), "crash_a15.txt");
    }

    @Nullable
    public static Uri exportCrashLogToDownloads(Context ctx) {
        File src = getCrashLogFile(ctx);
        if (!src.exists() || src.length() == 0) {
            Toast.makeText(ctx, "No hay crash log para exportar.", Toast.LENGTH_SHORT).show();
            return null;
        }
        String ts = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss",
                java.util.Locale.getDefault()).format(new java.util.Date());
        String name = "road_crash_" + ts + ".txt";
        return exportFileToDownloads(ctx, src, "ROAD", name, "text/plain");
    }

    /**
     * Exporta un archivo a /Descargas/<subdir>/ usando MediaStore (Android 10+)
     * o la carpeta pública (Android 9-). Devuelve Uri en 10+ o null en 9-.
     *
     * @param subdir       subcarpeta dentro de Descargas (p.ej. "ROAD"). Acepta null/"".
     * @param displayName  nombre final que verá el usuario (p.ej. "backup_20250823.db")
     * @param mimeType     p.ej. "application/octet-stream", "text/plain", "application/zip"
     */
    @Nullable
    public static Uri exportFileToDownloads(Context ctx, File src,
                                            @Nullable String subdir,
                                            String displayName,
                                            String mimeType) {
        if (src == null || !src.exists()) {
            Toast.makeText(ctx, "Archivo de origen no encontrado.", Toast.LENGTH_LONG).show();
            return null;
        }

        try {
            if (Build.VERSION.SDK_INT >= 29) {
                // Android 10+ — MediaStore (sin permisos)
                ContentResolver cr = ctx.getContentResolver();
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
                cv.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                String rel = Environment.DIRECTORY_DOWNLOADS +
                        (subdir == null || subdir.isEmpty() ? "" : ("/" + subdir));
                cv.put(MediaStore.Downloads.RELATIVE_PATH, rel);

                // IS_PENDING sólo aplica estrictamente a API 29; en 30+ se ignora
                if (Build.VERSION.SDK_INT == 29) cv.put(MediaStore.MediaColumns.IS_PENDING, 1);

                Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (uri == null) throw new IOException("No se pudo crear entrada en Descargas");

                try (InputStream in = new FileInputStream(src);
                     OutputStream out = cr.openOutputStream(uri)) {
                    copy(in, out);
                }

                if (Build.VERSION.SDK_INT == 29) {
                    ContentValues done = new ContentValues();
                    done.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    cr.update(uri, done, null, null);
                }

                Toast.makeText(ctx,
                        "Exportado a Descargas/" + (subdir == null ? "" : (subdir + "/")) + displayName,
                        Toast.LENGTH_LONG).show();
                return uri;

            } else {
                // Android 9 o menor — carpeta pública (requiere WRITE_EXTERNAL_STORAGE)
                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ctx, "Permiso de almacenamiento requerido (Android 9 o menor).",
                            Toast.LENGTH_LONG).show();
                    return null;
                }

                File base = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File dstDir = (subdir == null || subdir.isEmpty()) ? base : new File(base, subdir);
                if (!dstDir.exists()) dstDir.mkdirs();
                File dst = new File(dstDir, displayName);

                try (InputStream in = new FileInputStream(src);
                     OutputStream out = new FileOutputStream(dst)) {
                    copy(in, out);
                }

                Toast.makeText(ctx, "Exportado a: " + dst.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return null; // en pre-29 podrías devolver Uri.fromFile(dst) si lo necesitas
            }
        } catch (Exception e) {
            Toast.makeText(ctx, "Error exportando: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /* =================
       Utilidad de copia
       ================= */

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        out.flush();
        if (out instanceof FileOutputStream) {
            try { ((FileOutputStream) out).getFD().sync(); } catch (Exception ignored) {}
        }
    }

    // === Nuevo overload (4 args) ==============================
    public static boolean exportFileToDownloads(Context ctx,
                                                File src,
                                                @Nullable String displayName,
                                                @Nullable String mimeType) {
        if (ctx == null || src == null || !src.exists()) return false;
        if (displayName == null || displayName.trim().isEmpty())
            displayName = src.getName();
        if (mimeType == null || mimeType.trim().isEmpty())
            mimeType = guessMime(displayName);

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.Downloads.DISPLAY_NAME, displayName);
                values.put(android.provider.MediaStore.Downloads.MIME_TYPE, mimeType);
                values.put(android.provider.MediaStore.Downloads.IS_PENDING, 1);

                android.net.Uri uri = ctx.getContentResolver().insert(
                        android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return false;

                try (java.io.InputStream in = new java.io.FileInputStream(src);
                     java.io.OutputStream out = ctx.getContentResolver().openOutputStream(uri)) {
                    if (out == null) return false;
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                    out.flush();
                }

                values.clear();
                values.put(android.provider.MediaStore.Downloads.IS_PENDING, 0);
                ctx.getContentResolver().update(uri, values, null, null);
                return true;

            } else {
                // Pre-Android 10: copia a /sdcard/Download/
                java.io.File dl = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS);
                if (!dl.exists()) dl.mkdirs();
                java.io.File dst = new java.io.File(dl, displayName);
                try (java.io.InputStream in = new java.io.FileInputStream(src);
                     java.io.OutputStream out = new java.io.FileOutputStream(dst)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                    out.flush();
                }
                return true;
            }
        } catch (Exception e) {
            // si quieres, loguéalo a tu archivo interno
            return false;
        }
    }

    private static String guessMime(String name) {
        String m = java.net.URLConnection.guessContentTypeFromName(name);
        return (m != null) ? m : "application/octet-stream";
    }

    /** Hace backup de la BD (y borra -wal/-shm si deseas aparte) a la carpeta segura de la app. */
    public static File backupDbToAppFolder(Context ctx, String dbName) {
        try {
            File db  = appDbPath(ctx, dbName);
            if (!db.exists()) return null;

            File outDir = appBackupsDir(ctx);
            String outName = tsName("road_backup", ".db");
            File out = new File(outDir, outName);
            copyFile(db, out);

            // (Opcional) Copiar -wal/-shm si existen, con mismo timestamp
            File wal = new File(db.getPath() + "-wal");
            File shm = new File(db.getPath() + "-shm");
            if (wal.exists()) copyFile(wal, new File(outDir, outName + ".wal"));
            if (shm.exists()) copyFile(shm, new File(outDir, outName + ".shm"));

            return out; // devuelve el archivo principal .db
        } catch (Exception e) {
            return null;
        }
    }

    /** Carpeta segura de la app: .../Android/data/<pkg>/files/ROAD/Backups */
    public static File appBackupsDir(Context ctx) {
        File base = ctx.getExternalFilesDir(null);
        File dir  = new File(new File(base, "ROAD"), "Backups");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** Ruta del archivo de base de datos de la app */
    public static File appDbPath(Context ctx, String dbName) {
        return ctx.getDatabasePath(dbName);
    }

    /** Crea nombre con sello de tiempo */
    public static String tsName(String prefix, String ext) {
        String ts = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new java.util.Date());
        return prefix + "_" + ts + ext;
    }

    /** Copia binaria simple con sincronización de disco */
    public static void copyFile(File src, File dst) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.getFD().sync();
        }
    }

    /** Exporta un archivo a Downloads/ROAD/Backups usando MediaStore (Android 10+) o ruta pública (<10). */
    public static boolean exportFileToDownloads(Context ctx, File src, String displayName) {
        if (src == null || !src.exists()) return false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // MediaStore -> /storage/emulated/0/Download/ROAD/Backups
                ContentResolver cr = ctx.getContentResolver();
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
                cv.put(MediaStore.Downloads.MIME_TYPE, "application/x-sqlite3");
                cv.put(MediaStore.Downloads.RELATIVE_PATH, "Download/ROAD/Backups");

                Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (uri == null) return false;

                try (InputStream in = new FileInputStream(src);
                     OutputStream out = cr.openOutputStream(uri)) {
                    if (out == null) return false;
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                    out.flush();
                }
                return true;

            } else {
                // <= Android 9: carpeta pública (requiere WRITE_EXTERNAL_STORAGE concedido)
                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File outDir = new File(new File(downloads, "ROAD"), "Backups");
                if (!outDir.exists()) outDir.mkdirs();
                File out = new File(outDir, displayName);
                copyFile(src, out);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /** Atajo: hace backup a carpeta segura y también intenta copiar a Downloads. */
    public static boolean backupDbToAppAndDownloads(Context ctx, String dbName) {
        File appCopy = backupDbToAppFolder(ctx, dbName);
        if (appCopy == null) return false;

        // mismo nombre para Downloads
        boolean dlOk = exportFileToDownloads(ctx, appCopy, appCopy.getName());
        // el backup "seguro" ya quedó aunque falle el de Downloads
        return true; // reporta éxito global por haber al menos el backup app-specific
    }
}
