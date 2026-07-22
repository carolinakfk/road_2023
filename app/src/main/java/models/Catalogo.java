package models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.roadp.BaseDatos;
import com.dts.roadp.MiscUtils;
import com.dts.roadp.PBase;
import com.dts.roadp.appGlobals;
import com.dts.roadp.clsClasses;
import com.dts.roadp.promotions.PromotionTrace;
import com.dts.roadp.promotions.SapPromotionCalculator;

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
    private final Map<String, Double> preciosBaseSesion = new HashMap<>();

    public Catalogo(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
        this.gll = ((appGlobals) context.getApplicationContext());
        mu = new MiscUtils(context);
        cont = context;
        Con = dbconnection;
        db = dbase;
        ins=Con.Ins;upd=Con.Upd;
    }

    public clsClasses.clsBeP_DESCUENTO GetDescuentoCombo(String cliente, boolean esRecargo, long fechaDocumento) {

        clsClasses.clsBeP_DESCUENTO TmpDescuento = null;
        Cursor DT;

        try {
            vSQL = "SELECT D.* " +
                    "FROM P_DESCUENTO D " +
                    "INNER JOIN P_CLIENTE C ON C.CODIGO = '" + cliente + "' " +
                    "WHERE D.ES_RECARGO = " + (esRecargo ? 1 : 0) +
					//#EJC20260721 fix(hh-combo-catalog): combo es PTIPO=6 y DESCTIPO conserva R/M.
                    " AND D.PTIPO = 6 AND D.DESCTIPO IN ('R','M') " +
					" AND D.FECHAINI <= " + fechaDocumento + " AND D.FECHAFIN >= " + fechaDocumento +
					" AND NOT EXISTS (SELECT 1 FROM P_CLIENTE_PROD_EXCLUIDOS E " +
					" WHERE E.CLIENTE=C.CODIGO AND E.PRODUCTO=D.PRODUCTO AND E.ACTIVO=1 " +
					" AND E.FECHAINI <= " + fechaDocumento + " AND E.FECHAFIN >= " + fechaDocumento + ") " +
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
                    "ORDER BY CASE WHEN D.DESCTIPO='M' THEN 0 ELSE 1 END,D.PRIORIDAD ASC ";

            DT = Con.OpenDT(vSQL);

            if (DT.getCount() == 0) return null;

            DT.moveToFirst();
            TmpDescuento = Cargar(DT);
			PromotionTrace.write(cont,"PROMO_COMBO_SELECTED","cliente="+cliente+";codDesc="+TmpDescuento.codDesc+";tipo="+TmpDescuento.descTipo+";recargo="+esRecargo);

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
				TmpDetalle.grupo = DT.getString(2);
                TmpDetalle.producto = DT.getString(3);
                TmpDetalle.cantidad = DT.getDouble(4);
				TmpDetalle.umStock = DT.getString(5);
				TmpDetalle.umVenta = DT.getString(6);
				TmpDetalle.obligatorio = DT.isNull(7) || DT.getInt(7) == 1;
				TmpDetalle.empresa = DT.getString(8);
				TmpDetalle.tipoParticipacion = DT.getString(9);

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
			//#EJC20260721 fix(hh-combo-aplicacion): conserva PK completa de la línea.
			//#EJC20260721 fix(hh-promo-bonificacion): solo T_VENTA participa; T_BONITEM queda fuera.
            vSQL = "SELECT PRODUCTO,UM,CANT,PESO,PRECIO,SIN_EXISTENCIA FROM T_VENTA";
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
				l.precioBase = preciosBaseSesion.containsKey(lineKey(l.producto,l.um,DT.getInt(5)))
						? preciosBaseSesion.get(lineKey(l.producto,l.um,DT.getInt(5))) : l.precio;
				l.sinExistencia = DT.getInt(5);
				l.lineKey = lineKey(l.producto,l.um,(int)l.sinExistencia);
				if (!preciosBaseSesion.containsKey(l.lineKey)) preciosBaseSesion.put(l.lineKey,l.precioBase);
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
			//#EJC20260721 fix(hh-combo-aplicacion): evita actualizar otras UM del producto.
            upd.Where("PRODUCTO='"+ l.producto +"' AND UM='"+l.um+"' AND SIN_EXISTENCIA="+l.sinExistencia);
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

		//#EJC20260721 fix(hh-combo-requisitos): agrega por producto/UM y respeta OBLIGATORIO.
		Map<String, List<clsClasses.VentaLinea>> filasAAjustar = new HashMap<>();

        for (clsClasses.clsBeP_DESCUENTO_COMBO_DET itemCombo : pDetalleCombo) {
			List<clsClasses.VentaLinea> filasEncontradas = new ArrayList<>();
			double cantidadAcumulada = 0;

            for (clsClasses.VentaLinea l : lineas) {
                if (l.producto == null) continue;
				if (l.producto.equals(itemCombo.producto) && umCompatible(l,itemCombo)) {
					double cantidadRow = ObtenerCantidadLinea(l);
					cantidadAcumulada += cantidadRow;
					filasEncontradas.add(l);
				}
			}

			if (cantidadAcumulada < itemCombo.cantidad) {
				PromotionTrace.write(cont,"PROMO_COMBO_REQUIREMENT","codDesc="+itemCombo.codDesc+";producto="+itemCombo.producto+";obligatorio="+itemCombo.obligatorio+";cantidad="+cantidadAcumulada+";requerida="+itemCombo.cantidad);
				if (itemCombo.obligatorio) return;
				continue;
			}

			filasAAjustar.put(comboKey(itemCombo), filasEncontradas);
		}

        //#AT20260710 Calcular precio nuevo y actualizar T_VENTA.
        for (clsClasses.clsBeP_DESCUENTO_COMBO_DET itemCombo : pDetalleCombo) {
			List<clsClasses.VentaLinea> participantes = filasAAjustar.get(comboKey(itemCombo));
			if (participantes == null) continue;
			for (clsClasses.VentaLinea l : participantes) {
				double cantidadRow = ObtenerCantidadLinea(l);
				SapPromotionCalculator.Result r = SapPromotionCalculator.calculate(
						SapPromotionCalculator.decimal(l.precioBase), SapPromotionCalculator.decimal(cantidadRow),
						toAdjustment(pBeDescuento), toAdjustment(pBeRecargo));
				l.des = pBeDescuento == null ? 0 : pBeDescuento.valor;
				l.desMon = r.discountTotal.doubleValue();
				l.recargo = pBeRecargo == null ? 0 : pBeRecargo.valor;
				l.recargoMonto = r.surchargeTotal.doubleValue();
				l.precio = r.derivedUnitPrice.doubleValue();
				l.total = r.authoritativeFinalTotal.doubleValue();
				ActualizarLineaTVenta(l);
				PromotionTrace.write(cont,"PROMO_COMBO_APPLIED","producto="+l.producto+";linea="+l.lineKey+";base="+l.precioBase+";total="+l.total);
			}
		}
	}

	private SapPromotionCalculator.Adjustment toAdjustment(clsClasses.clsBeP_DESCUENTO condicion) {
		if (condicion == null) return SapPromotionCalculator.Adjustment.none();
		SapPromotionCalculator.AdjustmentKind kind;
		if (!"S".equalsIgnoreCase(condicion.porPorcentaje)) kind=SapPromotionCalculator.AdjustmentKind.FIXED;
		else if ("M".equalsIgnoreCase(condicion.descTipo)) kind=SapPromotionCalculator.AdjustmentKind.PERCENTAGE_MULTIPLE;
		else kind=SapPromotionCalculator.AdjustmentKind.PERCENTAGE_RANGE;
		return new SapPromotionCalculator.Adjustment(kind,SapPromotionCalculator.decimal(condicion.valor));
	}

	private boolean umCompatible(clsClasses.VentaLinea linea, clsClasses.clsBeP_DESCUENTO_COMBO_DET detalle) {
		return detalle.umVenta == null || detalle.umVenta.trim().isEmpty() || detalle.umVenta.equalsIgnoreCase(linea.um);
	}

	private String comboKey(clsClasses.clsBeP_DESCUENTO_COMBO_DET detalle) {
		return detalle.producto+"|"+(detalle.umVenta==null?"":detalle.umVenta);
	}

	private String lineKey(String producto,String um,int sinExistencia) {
		return producto+"|"+um+"|"+sinExistencia;
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
