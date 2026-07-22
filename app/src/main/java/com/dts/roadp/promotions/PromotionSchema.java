package com.dts.roadp.promotions;

import android.database.sqlite.SQLiteDatabase;

/** #EJC20260721 feat(hh-precio-base): migracion compatible para genealogia monetaria. */
public final class PromotionSchema {
    private PromotionSchema() { }

    public static void ensure(SQLiteDatabase db) {
        if (db == null) return;
        add(db,"T_VENTA","PRECIO_BASE REAL");
        add(db,"T_VENTA","TOTAL_BASE REAL");
        add(db,"T_VENTA","CODDESC_APLICADO INTEGER");
        add(db,"T_VENTA","CODRECARGO_APLICADO INTEGER");
        add(db,"D_FACTURAD","PRECIO_BASE REAL");
        add(db,"D_FACTURAD","TOTAL_BASE REAL");
        add(db,"D_FACTURAD","CODDESC_APLICADO INTEGER");
        add(db,"D_FACTURAD","CODRECARGO_APLICADO INTEGER");
    }

    private static void add(SQLiteDatabase db,String table,String definition) {
        try { db.execSQL("ALTER TABLE "+table+" ADD COLUMN "+definition); } catch (Exception ignored) { }
    }
}
