package com.dts.roadp;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Locale;

final class DiscountProductDialog {

    private static final int[] COLUMN_WIDTH_DP = {90, 260, 220, 130, 150};

    private DiscountProductDialog() {
    }

    static void show(Activity activity, BaseDatos connection) {
        TableLayout table = new TableLayout(activity);
        table.setStretchAllColumns(false);
        addRow(activity, table, new String[]{
                "CODIGO", "PRODUCTO", "DESCUENTO / RECARGO",
                "PRECIO BASE", "PRECIO PROMOCIONAL"
        }, true, false);

        Cursor cursor = null;
        boolean hasRows = false;

        try {
            // #EJC20260727 feat(hh-ajustes-resumen): muestra descuento y recargo
            // en filas separadas cuando ambos fueron aplicados al mismo producto.
            String sql = "SELECT PRODUCTO,PRODUCTO_NOMBRE,AJUSTE,PRECIO_BASE," +
                    "PRECIO_PROMOCIONAL FROM (" +
                    "SELECT DISTINCT V.PRODUCTO AS PRODUCTO, " +
                    "COALESCE(NULLIF(P.DESCLARGA,''),P.DESCCORTA,'') AS PRODUCTO_NOMBRE, " +
                    "COALESCE((SELECT D.NOMBRE FROM P_DESCUENTO D " +
                    "WHERE D.CODDESC=V.CODDESC_APLICADO AND D.ES_RECARGO=0 " +
                    "ORDER BY D.FECHAINI DESC LIMIT 1)," +
                    "CAST(V.CODDESC_APLICADO AS TEXT)) AS AJUSTE, " +
                    "IFNULL(V.PRECIO_BASE,V.PRECIO) AS PRECIO_BASE," +
                    "V.PRECIO AS PRECIO_PROMOCIONAL,0 AS ORDEN " +
                    "FROM T_VENTA V LEFT JOIN P_PRODUCTO P ON P.CODIGO=V.PRODUCTO " +
                    "WHERE IFNULL(V.CODDESC_APLICADO,0)<>0 " +
                    "UNION ALL " +
                    "SELECT DISTINCT V.PRODUCTO AS PRODUCTO, " +
                    "COALESCE(NULLIF(P.DESCLARGA,''),P.DESCCORTA,'') AS PRODUCTO_NOMBRE, " +
                    "COALESCE((SELECT R.NOMBRE FROM P_DESCUENTO R " +
                    "WHERE R.CODDESC=V.CODRECARGO_APLICADO AND R.ES_RECARGO=1 " +
                    "ORDER BY R.FECHAINI DESC LIMIT 1)," +
                    "CAST(V.CODRECARGO_APLICADO AS TEXT)) AS AJUSTE, " +
                    "IFNULL(V.PRECIO_BASE,V.PRECIO) AS PRECIO_BASE," +
                    "V.PRECIO AS PRECIO_PROMOCIONAL,1 AS ORDEN " +
                    "FROM T_VENTA V LEFT JOIN P_PRODUCTO P ON P.CODIGO=V.PRODUCTO " +
                    "WHERE IFNULL(V.CODRECARGO_APLICADO,0)<>0" +
                    ") ORDER BY PRODUCTO,ORDEN";

            cursor = connection.OpenDT(sql);
            if (cursor != null && cursor.moveToFirst()) {
                int rowIndex = 0;
                do {
                    addRow(activity, table, new String[]{
                            safeText(cursor.getString(0)),
                            safeText(cursor.getString(1)),
                            safeText(cursor.getString(2)),
                            formatBasePrice(cursor.getDouble(3)),
                            formatPromotionalPrice(cursor.getDouble(4))
                    }, false, rowIndex % 2 != 0);
                    rowIndex++;
                    hasRows = true;
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (!hasRows) {
            addRow(activity, table, new String[]{
                    "", "No hay descuentos ni recargos aplicados por producto.", "", "", ""
            }, false, false);
        }

        ScrollView verticalScroll = new ScrollView(activity);
        verticalScroll.addView(table);

        HorizontalScrollView horizontalScroll = new HorizontalScrollView(activity);
        horizontalScroll.setFillViewport(true);
        horizontalScroll.addView(verticalScroll);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Descuentos y recargos aplicados")
                .setView(horizontalScroll)
                .setPositiveButton("Cerrar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
    }

    private static void addRow(Activity activity, TableLayout table, String[] values,
                               boolean header, boolean alternate) {
        TableRow row = new TableRow(activity);
        for (int i = 0; i < values.length; i++) {
            TextView cell = new TextView(activity);
            cell.setText(values[i]);
            cell.setTextColor(Color.BLACK);
            cell.setTextSize(header ? 13 : 12);
            cell.setGravity(i >= 3 ? Gravity.END : Gravity.START);
            cell.setPadding(dp(activity, 6), dp(activity, 6),
                    dp(activity, 6), dp(activity, 6));
            cell.setBackground(cellBackground(header ? Color.rgb(190, 190, 190)
                    : alternate ? Color.rgb(242, 242, 242) : Color.WHITE));
            cell.setTypeface(null, header ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
            row.addView(cell, new TableRow.LayoutParams(dp(activity, COLUMN_WIDTH_DP[i]),
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        table.addView(row);
    }

    private static GradientDrawable cellBackground(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setStroke(1, Color.rgb(110, 110, 110));
        return background;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }

    private static String formatBasePrice(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static String formatPromotionalPrice(double value) {
        return String.format(Locale.US, "%.6f", value);
    }
}
