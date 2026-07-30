package com.dts.roadp.promotions;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** #EJC20260721 feat(hh-promo-trace): traza fina local, segura y no bloqueante. */
public final class PromotionTrace {
    private static final String TAG = "ROAD_PROMO_TRACE";
    private static final Object LOCK = new Object();
    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

    private PromotionTrace() { }

    public static void write(Context context, String event, String detail) {
        if (context == null) return;
        String safeEvent = sanitize(event);
        String safeDetail = sanitize(detail);
        String row = TS.format(new Date()) + ";" + safeEvent + ";" + safeDetail + System.lineSeparator();
        try {
            synchronized (LOCK) {
                File dir = new File(context.getFilesDir(), "trace_promociones");
                if (!dir.exists() && !dir.mkdirs()) return;
                File file = new File(dir, "hh_promo_" + new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()) + ".csv");
                FileWriter writer = new FileWriter(file, true);
                try { writer.write(row); } finally { writer.close(); }
            }
        } catch (Exception ex) {
            Log.w(TAG, "trace skipped: " + ex.getMessage());
        }
    }

    private static String sanitize(String value) {
        if (value == null) return "";
        return value.replace(';', '|').replace('\r', ' ').replace('\n', ' ');
    }
}
