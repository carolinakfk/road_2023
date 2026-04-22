// AppPaths.java
package com.dts.roadp;

import android.content.Context;
import java.io.File;

public final class AppPaths {
    private AppPaths() {}

    // Base app-specific external: /sdcard/Android/data/com.dts.roadp/files/
    public static File base(Context c) {
        return c.getExternalFilesDir(null);
    }

    // Carpeta "ROAD" para agrupar todo lo visible/compartible
    public static File road(Context c) {
        File d = new File(base(c), "ROAD");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File printDir(Context c) { // documentos de impresión
        File d = new File(road(c), "Print");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File syncFold(Context c) {
        File d = new File(road(c), "SyncFold");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File roadFotos(Context c) {
        File d = new File(road(c), "RoadFotos");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File roadFotosCliNue(Context c) {
        File d = new File(roadFotos(c), "clinue");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File roadFotosCliDocs(Context c) {
        File d = new File(roadFotos(c), "clidocs");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File roadPedidos(Context c) {
        File d = new File(road(c), "RoadPedidos");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File logs(Context c) {
        File d = new File(road(c), "Logs");
        if (!d.exists()) d.mkdirs();
        return d;
    }
}

