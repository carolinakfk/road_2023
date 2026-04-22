package com.dts.roadp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

public final class DeviceIds {
    private static final String PREFS = "ROAD_PREFS";
    private static final String KEY_FALLBACK_ID = "device_fallback_id";

    private DeviceIds() {}

    /** Devuelve un ID estable sin pedir READ_PHONE_STATE (AND 10+). */
    public static String deviceId(Context ctx) {
        // 1) ANDROID_ID (no requiere permisos)
        try {
            String id = Settings.Secure.getString(
                    ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (id != null && id.length() >= 8) return id;
        } catch (Exception ignored) {}

        // 2) Fallback persistente por app (por si ANDROID_ID fallara)
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String gen = sp.getString(KEY_FALLBACK_ID, null);
        if (gen == null) {
            gen = java.util.UUID.randomUUID().toString();
            sp.edit().putString(KEY_FALLBACK_ID, gen).apply();
        }
        return gen;
    }

    public static String deviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }
}

