package com.dts.roadp.promotions;

import android.database.sqlite.SQLiteDatabase;

/** #EJC20260721 feat(hh-precio-base): migracion compatible para genealogia monetaria. */
public final class PromotionSchema {
    private PromotionSchema() { }

    public static void ensure(SQLiteDatabase db) {
        if (db == null) return;

        //#EJC20260809 feat(hh-factura-promo-persistencia): D_FACTURAD es el
        //contrato durable que viaja a ROAD. BaseDatosScript cubre instalaciones
        //nuevas y estas sentencias actualizan bases ya existentes.
        add(db,"D_FACTURAD","PRECIO_BASE REAL DEFAULT 0 NOT NULL");
        add(db,"D_FACTURAD","TOTAL_BASE REAL DEFAULT 0 NOT NULL");
        add(db,"D_FACTURAD","DESCUENTOUNITARIO REAL DEFAULT 0 NOT NULL");
        add(db,"D_FACTURAD","RECARGOUNITARIO REAL DEFAULT 0 NOT NULL");
        add(db,"D_FACTURAD","CODDESC_APLICADO INTEGER DEFAULT 0 NOT NULL");
        add(db,"D_FACTURAD","CODRECARGO_APLICADO INTEGER DEFAULT 0 NOT NULL");

        add(db,"T_VENTA","PRECIO_BASE REAL");
        add(db,"T_VENTA","TOTAL_BASE REAL");
        add(db,"T_VENTA","CODDESC_APLICADO INTEGER");
        add(db,"T_VENTA","CODRECARGO_APLICADO INTEGER");
        //#EJC20260724 fix(hh-combo-local-fallback): estos nueve campos son estado
        //de sesion y nunca deben formar parte de D_FACTURAD ni del payload backend.
        add(db,"T_VENTA","INDIVIDUAL_SNAPSHOT INTEGER DEFAULT 0 NOT NULL");
        add(db,"T_VENTA","INDIVIDUAL_PRECIO REAL DEFAULT 0 NOT NULL");
        add(db,"T_VENTA","INDIVIDUAL_TOTAL REAL DEFAULT 0 NOT NULL");
        add(db,"T_VENTA","INDIVIDUAL_DES REAL DEFAULT 0 NOT NULL");
        add(db,"T_VENTA","INDIVIDUAL_DESMON REAL DEFAULT 0 NOT NULL");
        add(db,"T_VENTA","INDIVIDUAL_RECARGO REAL DEFAULT 0 NOT NULL");
        add(db,"T_VENTA","INDIVIDUAL_RECARGOMONTO REAL DEFAULT 0 NOT NULL");
        add(db,"T_VENTA","INDIVIDUAL_CODDESC INTEGER DEFAULT 0 NOT NULL");
        add(db,"T_VENTA","INDIVIDUAL_CODRECARGO INTEGER DEFAULT 0 NOT NULL");
        //#EJC20260724 feat(hh-pedido-promo-local-state): conserva genealogia para
        //reabrir pedidos sin ampliar D_PEDIDOD ni el contrato de sincronizacion.
        db.execSQL("CREATE TABLE IF NOT EXISTS T_PEDIDO_PROMO_STATE ("+
                "COREL TEXT NOT NULL,PRODUCTO TEXT NOT NULL,SIN_EXISTENCIA INTEGER NOT NULL,"+
                "PRECIO_BASE REAL,TOTAL_BASE REAL,CODDESC_APLICADO INTEGER,"+
                "CODRECARGO_APLICADO INTEGER,"+
                "PRIMARY KEY (COREL,PRODUCTO,SIN_EXISTENCIA))");
    }

    private static void add(SQLiteDatabase db,String table,String definition) {
        try { db.execSQL("ALTER TABLE "+table+" ADD COLUMN "+definition); } catch (Exception ignored) { }
    }
}
