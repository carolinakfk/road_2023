package models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.roadp.BaseDatos;
import com.dts.roadp.MiscUtils;
import com.dts.roadp.PBase;
import com.dts.roadp.appGlobals;
import com.dts.roadp.clsClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Catalogo extends PBase {
    protected clsClasses clsCls = new clsClasses();
    private android.database.sqlite.SQLiteDatabase db;
    private BaseDatos Con;
    private String vSQL;
    private Context cont;
    private appGlobals gll;
    private MiscUtils mu;

    public Catalogo(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
        this.gll = ((appGlobals) context.getApplicationContext());
        mu = new MiscUtils(context);
        cont = context;
        Con = dbconnection;
        db = dbase;
        ins=Con.Ins;upd=Con.Upd;
    }

    public clsClasses.clsBeP_DESCUENTO GetDescuentoCombo(String cliente, boolean esRecargo) {

        clsClasses.clsBeP_DESCUENTO TmpDescuento = null;
        Cursor DT;

        try {
            vSQL = "SELECT D.* " +
                    "FROM P_DESCUENTO D " +
                    "INNER JOIN P_CLIENTE C ON C.CODIGO = '" + cliente + "' " +
                    "WHERE D.ES_RECARGO = " + (esRecargo ? 1 : 0) +
                    " AND D.DESCTIPO = 'C' " +
                    " AND ( " +
                    "      EXISTS (SELECT 1 FROM P_CLIRUTA CR WHERE CR.CLIENTE = C.CODIGO AND D.CLIENTE = CR.RUTA) " +
                    "   OR (IFNULL(C.TIPOLOGIA, '') <> '' AND D.CLIENTE = C.TIPOLOGIA) " +
                    "   OR (IFNULL(C.SUBTIPOLOGIA, '') <> '' AND D.CLIENTE = C.SUBTIPOLOGIA) " +
                    "   OR (IFNULL(C.PRIORIZACION, '') <> '' " +
                    "       AND IFNULL(C.SUCURSAL, '') <> '' " +
                    "       AND IFNULL(C.TIPO, '') <> '' " +
                    "       AND D.CLIENTE = C.PRIORIZACION " +
                    "       AND IFNULL(D.SUCURSAL, '') = C.SUCURSAL " +
                    "       AND IFNULL(D.TIPOLOGIA, '') = C.TIPOLOGIA) " +
                    "    ) " +
                    "ORDER BY D.PRIORIDAD ASC ";

            DT = Con.OpenDT(vSQL);

            if (DT.getCount() == 0) return null;

            DT.moveToFirst();
            TmpDescuento = Cargar(DT);

        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }

        return TmpDescuento;
    }

    public List<clsClasses.clsBeP_DESCUENTO_COMBO_DET> GetDetalleComboDescuento(int descuento) {
        List<clsClasses.clsBeP_DESCUENTO_COMBO_DET> lReturnList = new ArrayList<>();
        Cursor DT;

        try {
            vSQL = "SELECT * FROM P_DESCUENTO_COMBO_DET " +
                    "WHERE CODDESC = " + descuento + " " +
                    "ORDER BY SECUENCIA";

            DT = Con.OpenDT(vSQL);

            if (DT.getCount() == 0) return lReturnList;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                clsClasses.clsBeP_DESCUENTO_COMBO_DET TmpDetalle = clsCls.new clsBeP_DESCUENTO_COMBO_DET();
                TmpDetalle.codDesc = DT.getInt(0);
                TmpDetalle.secuencia = DT.getInt(1);
                TmpDetalle.producto = DT.getString(3);
                TmpDetalle.cantidad = DT.getDouble(4);

                lReturnList.add(TmpDetalle);
                DT.moveToNext();
            }

        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }

        return lReturnList;
    }

    public clsClasses.clsBeP_DESCUENTO Cargar(Cursor c) {
        clsClasses.clsBeP_DESCUENTO d = null;
        try {
            d = clsCls.new clsBeP_DESCUENTO();

            d.cliente = c.getString(c.getColumnIndexOrThrow("CLIENTE"));
            d.ctipo = c.getString(c.getColumnIndexOrThrow("CTIPO"));
            d.producto = c.getString(c.getColumnIndexOrThrow("PRODUCTO"));
            d.ptipo = c.getString(c.getColumnIndexOrThrow("PTIPO"));
            d.tiporuta = c.getString(c.getColumnIndexOrThrow("TIPORUTA"));
            d.rangoIni = c.getDouble(c.getColumnIndexOrThrow("RANGOINI"));
            d.rangoFin = c.getDouble(c.getColumnIndexOrThrow("RANGOFIN"));
            d.descTipo = c.getString(c.getColumnIndexOrThrow("DESCTIPO"));
            d.valor = c.getDouble(c.getColumnIndexOrThrow("VALOR"));
            d.globDesc = c.getString(c.getColumnIndexOrThrow("GLOBDESC"));
            d.porcAnt = c.getDouble(c.getColumnIndexOrThrow("PORCANT"));
            d.fechaIni = c.getString(c.getColumnIndexOrThrow("FECHAINI"));
            d.fechaFin = c.getString(c.getColumnIndexOrThrow("FECHAFIN"));
            d.codDesc = c.getInt(c.getColumnIndexOrThrow("CODDESC"));
            d.nombre = c.getString(c.getColumnIndexOrThrow("NOMBRE"));
            d.esRecargo = c.getInt(c.getColumnIndexOrThrow("ES_RECARGO")) == 1;
            d.porPorcentaje = c.getString(c.getColumnIndexOrThrow("PORPORCENTAJE"));
            d.prioridad = c.getInt(c.getColumnIndexOrThrow("PRIORIDAD"));
            d.umVenta = c.getString(c.getColumnIndexOrThrow("UMVENTA"));
            d.sucursal = c.getString(c.getColumnIndexOrThrow("SUCURSAL"));
            d.tipologia = c.getString(c.getColumnIndexOrThrow("TIPOLOGIA"));

        } catch (Exception e) {
            return d;
        }
        return d;
    }

    public List<clsClasses.VentaLinea> CargarLineasTVenta() {
        List<clsClasses.VentaLinea> lista = new ArrayList<>();
        Cursor DT;
        try {
            vSQL = "SELECT PRODUCTO, UM, CANT, PESO, PRECIO FROM T_VENTA";
            DT = Con.OpenDT(vSQL);
            if (DT.getCount() == 0) return lista;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                clsClasses.VentaLinea l = clsCls.new VentaLinea();
                l.producto = DT.getString(0);
                l.um = DT.getString(1);
                l.cant = DT.getDouble(2);
                l.peso = DT.getDouble(3);
                l.precio = DT.getDouble(4);
                lista.add(l);
                DT.moveToNext();
            }
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }
        return lista;
    }

    public void ActualizarLineaTVenta(clsClasses.VentaLinea l) {
        try {
            upd.init("T_VENTA");
            upd.Where("PRODUCTO='"+ l.producto +"'");
            upd.add("PRECIO", l.precio);
            upd.add("PRECIODOC",l.precio);
            upd.add("DES", l.des);
            upd.add("DESMON", l.desMon);
            upd.add("RECARGO", l.recargo);
            upd.add("RECARGOMONTO", l.recargoMonto);
            upd.add("TOTAL", l.total);

            db.execSQL(upd.SQL());
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }
    }

    public void AplicarAjusteComboEnTVenta(List<clsClasses.clsBeP_DESCUENTO_COMBO_DET> pDetalleCombo,
                                            clsClasses.clsBeP_DESCUENTO pBeDescuento,
                                            clsClasses.clsBeP_DESCUENTO pBeRecargo) {

        if (pBeDescuento == null && pBeRecargo == null) return;

        List<clsClasses.VentaLinea> lineas = CargarLineasTVenta();

        //#AT20260710 Validar que todos los productos este en la venta.
        Map<String, clsClasses.VentaLinea> filasAAjustar = new HashMap<>();

        for (clsClasses.clsBeP_DESCUENTO_COMBO_DET itemCombo : pDetalleCombo) {
            clsClasses.VentaLinea filaEncontrada = null;

            for (clsClasses.VentaLinea l : lineas) {
                if (l.producto == null) continue;
                if (l.producto.equals(itemCombo.producto)) {
                    double cantidadRow = ObtenerCantidadLinea(l);
                    if (cantidadRow >= itemCombo.cantidad) {
                        filaEncontrada = l;
                    }
                    break;
                }
            }

            if (filaEncontrada == null) {
                return;
            }

            filasAAjustar.put(itemCombo.producto, filaEncontrada);
        }

        //#AT20260710 Calcular precio nuevo y actualizar T_VENTA.
        for (clsClasses.clsBeP_DESCUENTO_COMBO_DET itemCombo : pDetalleCombo) {
            clsClasses.VentaLinea l = filasAAjustar.get(itemCombo.producto);

            double cantidadRow = ObtenerCantidadLinea(l);
            double nuevoPrecio = l.precio;

            if (pBeDescuento != null) {
                double valorDescuento = Calcular_Valor_Descuento_Recargo(pBeDescuento, nuevoPrecio);
                l.des    = pBeDescuento.valor;
                l.desMon = mu.round2(valorDescuento);

                nuevoPrecio -= valorDescuento;
            }
            if (pBeRecargo != null) {
                double valorRecargo = Calcular_Valor_Descuento_Recargo(pBeRecargo, nuevoPrecio);
                l.recargo    = pBeRecargo.valor;
                l.recargoMonto = mu.round2(valorRecargo);
                nuevoPrecio += valorRecargo;
            }

            l.precio       = mu.round2(nuevoPrecio);
            l.total = mu.round2(nuevoPrecio * cantidadRow);

            ActualizarLineaTVenta(l);
        }
    }

    private double Calcular_Valor_Descuento_Recargo(clsClasses.clsBeP_DESCUENTO beDescuento, double precioBase) {

        if (beDescuento == null) {
            return 0;
        }

        double valor = beDescuento.valor;

        String porPorcentaje = "";
        if (beDescuento.porPorcentaje != null) {
            porPorcentaje = beDescuento.porPorcentaje.toString().trim().toUpperCase();
        }

        if (porPorcentaje.equals("S")) {
            return mu.round2((precioBase * valor) / 100);
        }

        return mu.round2(valor);
    }

    public double ObtenerCantidadLinea(clsClasses.VentaLinea l) {
        boolean esPorPeso = (l.um != null && l.um.equals(gll.umpeso));
        return esPorPeso ? l.peso : l.cant;
    }

    public void opendb() {
        try {
            db = Con.getWritableDatabase();
            Con.vDatabase =db;
        } catch (Exception ignored) {
        }
    }
}
