package models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dts.roadp.BaseDatos;
import com.dts.roadp.DateUtils;
import com.dts.roadp.MiscUtils;
import com.dts.roadp.PBase;
import com.dts.roadp.appGlobals;
import com.dts.roadp.clsClasses;
import com.dts.roadp.clsDescuento;
import com.dts.roadp.promotions.PromotionTrace;
import com.dts.roadp.promotions.SapPromotionCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Catalogo extends PBase {
    protected clsClasses clsCls = new clsClasses();
    private android.database.sqlite.SQLiteDatabase db;
    private BaseDatos Con;
    private String vSQL;
    private Context cont;
    private appGlobals gll;
    private MiscUtils mu;
    private DateUtils dateUtils;
    private final Map<String, Double> preciosBaseSesion = new HashMap<>();

    public Catalogo(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
        this.gll = ((appGlobals) context.getApplicationContext());
        mu = new MiscUtils(context);
        dateUtils = new DateUtils();
        cont = context;
        Con = dbconnection;
        db = dbase;
        ins=Con.Ins;upd=Con.Upd;
        asegurarSnapshotIndividual();
    }

    public clsClasses.clsBeP_DESCUENTO GetDescuentoCombo(String cliente, boolean esRecargo, long fechaDocumento) {
        List<clsClasses.clsBeP_DESCUENTO> candidatos = GetDescuentosCombo(cliente, esRecargo, fechaDocumento);
        return candidatos.isEmpty() ? null : candidatos.get(0);
    }

    //#EJC20260724 fix(hh-combo-candidates): devuelve todos los candidatos porque el primero
    //puede estar incompleto o empatado; CODDESC no es un criterio funcional de desempate.
    public List<clsClasses.clsBeP_DESCUENTO> GetDescuentosCombo(String cliente, boolean esRecargo,
                                                                long fechaDocumento) {
        List<clsClasses.clsBeP_DESCUENTO> candidatos = new ArrayList<>();
        Cursor DT;

        try {
            //#EJC20260724 fix(hh-combo-fecha-vigencia): P_FECHA conserva el
            //formato legacy yyMMddHHmmss; promociones comparan siempre yyyyMMddHHmmss.
            long fechaVigencia = normalizarFechaVigencia(fechaDocumento);
            //#EJC20260724 fix(hh-combo-client-scope): T_DESC ya representa las
            //condiciones comerciales filtradas para el cliente activo; el join evita
            //omitir CTIPO o ampliar combos a clientes no elegibles.
            vSQL = "SELECT DISTINCT D.* " +
                    "FROM P_DESCUENTO D INNER JOIN T_DESC T ON T.CODDESC=D.CODDESC " +
                    " AND T.DESCTIPO=D.DESCTIPO AND T.ES_RECARGO=D.ES_RECARGO " +
                    "WHERE D.ES_RECARGO = " + (esRecargo ? 1 : 0) +
					//#EJC20260724 fix(hh-combo-desctipo-c): SAP sincroniza los
					//combos reales con PTIPO=6 y DESCTIPO=C; R/M se conservan por compatibilidad.
                    " AND D.PTIPO = 6 AND D.DESCTIPO IN ('R','M','C') " +
					" AND D.FECHAINI <= " + fechaVigencia + " AND D.FECHAFIN >= " + fechaVigencia +
					//#EJC20260728 fix(hh-combo-detail-exclusions): una exclusion de
					//cualquier material requerido invalida la condicion completa.
					" AND NOT EXISTS (SELECT 1 FROM P_DESCUENTO_COMBO_DET DD " +
					" INNER JOIN P_CLIENTE_PROD_EXCLUIDOS E ON E.PRODUCTO=DD.PRODUCTO " +
					" WHERE DD.CODDESC=D.CODDESC AND E.CLIENTE='" + cliente + "' AND E.ACTIVO=1 " +
					" AND E.FECHAINI <= " + fechaVigencia + " AND E.FECHAFIN >= " + fechaVigencia + ") " +
                    "ORDER BY IFNULL(D.PRIORIDAD_DESCUENTO,0),D.PRIORIDAD ASC,D.CODDESC ASC ";

            DT = Con.OpenDT(vSQL);

            if (DT.getCount() == 0) return candidatos;

            DT.moveToFirst();
            while (!DT.isAfterLast()) {
                clsClasses.clsBeP_DESCUENTO candidato = Cargar(DT);
                if (candidato != null && !contieneCandidato(candidatos,candidato.codDesc)) {
                    candidatos.add(candidato);
                }
                DT.moveToNext();
            }
            PromotionTrace.write(cont,"PROMO_COMBO_CANDIDATES","cliente="+cliente+";recargo="+
                    esRecargo+";cantidad="+candidatos.size());

        } catch (Exception e) {
            mu.msgbox(e.getMessage());
            PromotionTrace.write(cont,"PROMO_COMBO_CANDIDATES_ERROR","recargo="+esRecargo+
                    ";error="+e.getClass().getSimpleName());
        }

        return candidatos;
    }

    private boolean contieneCandidato(List<clsClasses.clsBeP_DESCUENTO> candidatos,
                                      int codDesc) {
        for (clsClasses.clsBeP_DESCUENTO candidato : candidatos) {
            if (candidato.codDesc == codDesc) return true;
        }
        return false;
    }

    private long normalizarFechaVigencia(long fechaDocumento) {
        try {
            long fechaNormalizada = dateUtils.convertirFecha(fechaDocumento);
            if (fechaNormalizada != fechaDocumento) {
                PromotionTrace.write(cont,"PROMO_DATE_NORMALIZED",
                        "origen=P_FECHA;formatoEntrada=yyMMddHHmmss;fechaNormalizada="+fechaNormalizada);
            }
            return fechaNormalizada;
        } catch (IllegalArgumentException e) {
            PromotionTrace.write(cont,"PROMO_DATE_INVALID",
                    "origen=resolver_combo;digitos="+String.valueOf(fechaDocumento).length()+
                            ";accion=individual");
            throw e;
        }
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
            int prioridadDescuentoIndex=c.getColumnIndex("PRIORIDAD_DESCUENTO");
            d.prioridadDescuento=prioridadDescuentoIndex<0?0:c.getInt(prioridadDescuentoIndex);
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
			//#EJC20260721 feat(hh-precio-base): usa base persistida y no el precio ya promocionado.
            vSQL = "SELECT PRODUCTO,UM,CANT,PESO,PRECIO,SIN_EXISTENCIA,"+
					"IFNULL(PRECIO_BASE,PRECIO),IFNULL(TOTAL_BASE,0),IFNULL(CODDESC_APLICADO,0),IFNULL(CODRECARGO_APLICADO,0),"+
					"DES,DESMON,RECARGO,RECARGOMONTO,TOTAL,IFNULL(INDIVIDUAL_SNAPSHOT,0),"+
					"IFNULL(INDIVIDUAL_PRECIO,0),IFNULL(INDIVIDUAL_TOTAL,0),IFNULL(INDIVIDUAL_DES,0),"+
					"IFNULL(INDIVIDUAL_DESMON,0),IFNULL(INDIVIDUAL_RECARGO,0),"+
					"IFNULL(INDIVIDUAL_RECARGOMONTO,0),IFNULL(INDIVIDUAL_CODDESC,0),"+
					"IFNULL(INDIVIDUAL_CODRECARGO,0),IFNULL(UMSTOCK,UM),IFNULL(FACTOR,1) FROM T_VENTA";
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
				l.precioBase=DT.getDouble(6);
				l.totalBase=DT.getDouble(7);
				l.codDescAplicado=DT.getInt(8);
				l.codRecargoAplicado=DT.getInt(9);
				l.des=DT.getDouble(10);
				l.desMon=DT.getDouble(11);
				l.recargo=DT.getDouble(12);
				l.recargoMonto=DT.getDouble(13);
				l.total=DT.getDouble(14);
				l.individualSnapshot=DT.getInt(15)==1;
				l.individualPrecio=DT.getDouble(16);
				l.individualTotal=DT.getDouble(17);
				l.individualDes=DT.getDouble(18);
				l.individualDesMon=DT.getDouble(19);
				l.individualRecargo=DT.getDouble(20);
				l.individualRecargoMonto=DT.getDouble(21);
				l.individualCodDesc=DT.getInt(22);
				l.individualCodRecargo=DT.getInt(23);
				//#EJC20260731 fix(hh-combo-umstock): conserva la UM comercial y el
				//factor para evaluar requisitos sin confundirlos con la UM del precio.
				l.umStock=DT.getString(24);
				l.factor=DT.getDouble(25);
				if (l.precioBase<=0) l.precioBase = preciosBaseSesion.containsKey(lineKey(l.producto,l.um,DT.getInt(5)))
						? preciosBaseSesion.get(lineKey(l.producto,l.um,DT.getInt(5))) : l.precio;
				l.sinExistencia = DT.getInt(5);
				l.lineKey = lineKey(l.producto,l.um,(int)l.sinExistencia);
				if (!preciosBaseSesion.containsKey(l.lineKey)) preciosBaseSesion.put(l.lineKey,l.precioBase);
				if (!l.individualSnapshot) {
					if (esAjusteCombo(l.codDescAplicado,false) || esAjusteCombo(l.codRecargoAplicado,true)) {
						reconstruirSnapshotIndividual(l);
					} else guardarSnapshotIndividual(l);
				}
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
			upd.add("PRECIO_BASE",l.precioBase);
			upd.add("TOTAL_BASE",l.totalBase);
			upd.add("CODDESC_APLICADO",l.codDescAplicado);
			upd.add("CODRECARGO_APLICADO",l.codRecargoAplicado);

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
					double cantidadRow = ObtenerCantidadRequisito(l,itemCombo);
					cantidadAcumulada += cantidadRow;
					filasEncontradas.add(l);
				}
			}
			//#EJC20260724 rule(hh-combo-bonus-excluded): solo las lineas cobradas de
			//T_VENTA completan el requisito; T_BONITEM queda fuera aunque conserve precio.

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
				l.totalBase=r.extendedBaseTotal.doubleValue();
				l.codDescAplicado=pBeDescuento==null?0:pBeDescuento.codDesc;
				l.codRecargoAplicado=pBeRecargo==null?0:pBeRecargo.codDesc;
				ActualizarLineaTVenta(l);
				PromotionTrace.write(cont,"PROMO_COMBO_APPLIED","producto="+l.producto+";linea="+l.lineKey+";base="+l.precioBase+";total="+l.total);
			}
		}
	}

    //#EJC20260724 fix(hh-combo-document-resolver): resuelve descuento y recargo juntos,
    //conserva el individual provisional y evita que un lado borre al otro.
    public void ResolverCombosEnTVenta(String cliente, long fechaDocumento, boolean notificarEmpate) {
        List<clsClasses.VentaLinea> lineas = CargarLineasTVenta();
        if (lineas.isEmpty()) return;
        restaurarIndividuales(lineas);

        ComboResolution descuento = resolverMejorCombo(
                GetDescuentosCombo(cliente, false, fechaDocumento), lineas, false);
        ComboResolution recargo = resolverMejorCombo(
                GetDescuentosCombo(cliente, true, fechaDocumento), lineas, true);

        if (descuento.ambiguo || recargo.ambiguo) {
            String lados = (descuento.ambiguo ? "descuento" : "")+
                    (descuento.ambiguo && recargo.ambiguo ? " y " : "")+
                    (recargo.ambiguo ? "recargo" : "");
            if (notificarEmpate) {
                mu.msgbox("Existen varios combos completos con la misma prioridad de "+
                        lados+". No se aplicara ninguno; se conservaran los ajustes individuales.");
            }
        }

        aplicarResolucionConjunta(lineas, descuento, recargo);
        PromotionTrace.write(cont,"PROMO_DOCUMENT_REEVALUATED","cliente="+cliente+
                ";filas="+lineas.size()+";descuentoCombo="+
                (descuento.seleccion==null?0:descuento.seleccion.condicion.codDesc)+
                ";recargoCombo="+(recargo.seleccion==null?0:recargo.seleccion.condicion.codDesc));
    }

    //#EJC20260724 fix(hh-combo-customer-return): las devoluciones de cliente tambien
    //resuelven individual provisional, combo unico y empate sobre el documento completo.
    public void ResolverCombosEnDevolucion(String cliente, long fechaDocumento,
                                           boolean notificarEmpate) {
        List<clsClasses.VentaLinea> lineas = cargarLineasDevolucion();
        if (lineas.isEmpty()) return;
        recalcularIndividualesDevolucion(lineas);

        ComboResolution descuento = resolverMejorCombo(
                GetDescuentosCombo(cliente, false, fechaDocumento), lineas, false);
        ComboResolution recargo = resolverMejorCombo(
                GetDescuentosCombo(cliente, true, fechaDocumento), lineas, true);
        if ((descuento.ambiguo || recargo.ambiguo) && notificarEmpate) {
            mu.msgbox("Existen varios combos completos con la misma prioridad en la devolucion. "+
                    "No se aplicara ninguno; se conservaran los ajustes individuales.");
        }
        aplicarResolucionDevolucion(lineas, descuento, recargo);
    }

    private List<clsClasses.VentaLinea> cargarLineasDevolucion() {
        List<clsClasses.VentaLinea> lineas = new ArrayList<>();
        Cursor cursor = null;
        try {
            asegurarPromoElegibleDevolucion();
            asegurarTrazabilidadPromocionDevolucion();
            cursor = Con.OpenDT("SELECT ITEM,CODIGO,CANT,PESO,PRECIO,PRECLISTA,TOTAL,"+
                    "UMVENTA,UMSTOCK,POR_PESO,IFNULL(PROMO_ELEGIBLE,1),UMPESO,"+
                    "IFNULL(PRECIO_BASE,0) "+
                    "FROM T_CxCD WHERE CANT>0");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    clsClasses.VentaLinea linea = clsCls.new VentaLinea();
                    linea.lineKey = "DEV|"+cursor.getInt(0);
                    linea.producto = cursor.getString(1);
                    linea.cant = cursor.getDouble(2);
                    linea.peso = cursor.getDouble(3);
                    linea.precio = cursor.getDouble(4);
                    linea.precioBase = cursor.getDouble(12)>0 ? cursor.getDouble(12) :
                            (cursor.getDouble(5)>0 ? cursor.getDouble(5) : linea.precio);
                    linea.total = cursor.getDouble(6);
                    linea.um = cursor.getString(7);
                    linea.umStock = cursor.getString(8);
                    linea.sinExistencia = "S".equalsIgnoreCase(cursor.getString(9)) ? 1 : 0;
                    linea.promoEligible = cursor.getInt(10)==1;
                    linea.umPeso = cursor.getString(11);
                    lineas.add(linea);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            PromotionTrace.write(cont,"PROMO_RETURN_LOAD_ERROR","error="+e.getClass().getSimpleName());
        } finally {
            if (cursor != null) cursor.close();
        }
        return lineas;
    }

    private void recalcularIndividualesDevolucion(List<clsClasses.VentaLinea> lineas) {
        for (clsClasses.VentaLinea linea : lineas) {
            double baseFacturacion = ObtenerCantidadLinea(linea);
            if (!linea.promoEligible) {
                linea.totalBase=SapPromotionCalculator.money(
                        SapPromotionCalculator.decimal(linea.precioBase)
                                .multiply(SapPromotionCalculator.decimal(baseFacturacion))).doubleValue();
                linea.total=linea.totalBase;
                linea.precio=linea.precioBase;
                linea.desMon=0;
                linea.recargoMonto=0;
                continue;
            }
            clsDescuento selector = new clsDescuento(cont,linea.producto,linea.cant,linea.peso,
                    linea.umStock,Con,db);
            clsClasses.clsBeDescuento descuento = selector.getDescuentoRecargo(false);
            clsClasses.clsBeDescuento recargo = selector.getDescuentoRecargo(true);
            //#CKFK20260730 fix(hh-return-individual-discount): la devolucion no debe
            //depender de la UM global que haya dejado otra linea. Si el selector legacy
            //no resuelve la condicion, usar la genealogia de UM persistida en T_CxCD.
            if (descuento == null) descuento = getAjusteIndividualDevolucion(linea,false);
            if (recargo == null) recargo = getAjusteIndividualDevolucion(linea,true);
            SapPromotionCalculator.Result calculo = SapPromotionCalculator.calculate(
                    SapPromotionCalculator.decimal(linea.precioBase),
                    SapPromotionCalculator.decimal(baseFacturacion),
                    toAdjustment(descuento),toAdjustment(recargo));
            linea.totalBase=calculo.extendedBaseTotal.doubleValue();
            linea.desMon=calculo.discountTotal.doubleValue();
            linea.recargoMonto=calculo.surchargeTotal.doubleValue();
            linea.des=descuento==null?0:descuento.valor;
            linea.recargo=recargo==null?0:recargo.valor;
            linea.codDescAplicado=descuento==null?0:descuento.codDesc;
            linea.codRecargoAplicado=recargo==null?0:recargo.codDesc;
            linea.total=calculo.authoritativeFinalTotal.doubleValue();
            linea.precio=calculo.derivedUnitPrice.doubleValue();
        }
        PromotionTrace.write(cont,"INDIVIDUAL_FALLBACK","documento=DEVOLUCION;filas="+lineas.size()+
                ";accion=recalculado_desde_precio_base");
    }

    private clsClasses.clsBeDescuento getAjusteIndividualDevolucion(
            clsClasses.VentaLinea linea, boolean esRecargo) {
        Cursor cursor = null;
        try {
            String producto = linea.producto == null ? "" : linea.producto.replace("'","''");
            String umPeso = linea.umPeso == null ? "" : linea.umPeso.replace("'","''");
            String baseSql = "CASE WHEN UMVENTA='"+umPeso+"' THEN "+linea.peso+
                    " ELSE "+linea.cant+" END";
            String sql = "SELECT PRODUCTO,PTIPO,VALOR,PORPORCENTAJE,CODDESC,DESCTIPO,"+
                    "PRIORIDAD,IFNULL(PRIORIDAD_DESCUENTO,0),UMVENTA,RANGOINI,RANGOFIN "+
                    "FROM T_DESC WHERE ES_RECARGO="+(esRecargo ? 1 : 0)+
                    " AND PTIPO=0 AND GLOBDESC='N' AND (PRODUCTO='"+producto+
                    "' OR PRODUCTO='*') AND ("+
                    "(DESCTIPO='M' AND "+baseSql+">=RANGOINI) OR "+
                    "(DESCTIPO='R' AND "+baseSql+">=RANGOINI AND "+baseSql+"<=RANGOFIN)) "+
                    "ORDER BY CASE WHEN DESCTIPO='M' THEN 0 ELSE 1 END,"+
                    "PRIORIDAD_DESCUENTO ASC,PRIORIDAD ASC,RANGOINI DESC";
            cursor=Con.OpenDT(sql);
            if (cursor == null || !cursor.moveToFirst()) return null;
            clsClasses.clsBeDescuento ajuste=clsCls.new clsBeDescuento();
            ajuste.producto=cursor.getString(0);
            ajuste.pTipo=cursor.getInt(1);
            ajuste.valor=cursor.getDouble(2);
            ajuste.porPorcentaje=cursor.getString(3);
            ajuste.codDesc=cursor.getInt(4);
            ajuste.descTipo=cursor.getString(5);
            ajuste.prioridad=cursor.getInt(6);
            ajuste.prioridadDescuento=cursor.getInt(7);
            ajuste.umVenta=cursor.getString(8);
            ajuste.rangoIni=cursor.getDouble(9);
            ajuste.rangoFin=cursor.getDouble(10);
            double selectedBase=ajuste.umVenta != null && ajuste.umVenta.equalsIgnoreCase(linea.umPeso)
                    ? linea.peso : linea.cant;
            PromotionTrace.write(cont,"PROMO_RETURN_INDIVIDUAL_FALLBACK",
                    "producto="+linea.producto+";recargo="+esRecargo+
                    ";codDesc="+ajuste.codDesc+";base="+selectedBase+";um="+linea.um);
            return ajuste;
        } catch (Exception e) {
            PromotionTrace.write(cont,"PROMO_RETURN_INDIVIDUAL_FALLBACK_ERROR",
                    "producto="+linea.producto+";recargo="+esRecargo+
                    ";error="+e.getClass().getSimpleName());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void aplicarResolucionDevolucion(List<clsClasses.VentaLinea> lineas,
                                              ComboResolution descuento,
                                              ComboResolution recargo) {
        for (clsClasses.VentaLinea linea : lineas) {
            boolean aplicaDescuento = descuento.seleccion != null &&
                    descuento.seleccion.participantes.contains(linea.lineKey);
            boolean aplicaRecargo = recargo.seleccion != null &&
                    recargo.seleccion.participantes.contains(linea.lineKey);
            if (aplicaDescuento) {
                linea.desMon=calcularAjusteCombo(linea,descuento.seleccion.condicion).doubleValue();
                linea.des=descuento.seleccion.condicion.valor;
                linea.codDescAplicado=descuento.seleccion.condicion.codDesc;
            }
            if (aplicaRecargo) {
                linea.recargoMonto=calcularAjusteCombo(linea,recargo.seleccion.condicion).doubleValue();
                linea.recargo=recargo.seleccion.condicion.valor;
                linea.codRecargoAplicado=recargo.seleccion.condicion.codDesc;
            }
            BigDecimal total=SapPromotionCalculator.decimal(linea.totalBase)
                    .subtract(SapPromotionCalculator.decimal(linea.desMon))
                    .add(SapPromotionCalculator.decimal(linea.recargoMonto))
                    .setScale(SapPromotionCalculator.MONEY_SCALE,SapPromotionCalculator.SAP_ROUNDING);
            double baseFacturacion=ObtenerCantidadLinea(linea);
            linea.total=total.doubleValue();
            linea.precio=total.divide(SapPromotionCalculator.decimal(baseFacturacion),
                    SapPromotionCalculator.UNIT_PRICE_SCALE,SapPromotionCalculator.SAP_ROUNDING).doubleValue();
            try {
                int item=Integer.parseInt(linea.lineKey.substring(4));
                // #EJC20260730 feat(hh-return-promo-trace): conserva en la tabla temporal
                // la base inmutable y la condicion final resuelta para UI y auditoria local.
                db.execSQL("UPDATE T_CxCD SET PRECIO="+linea.precio+",TOTAL="+linea.total+
                        ",PRECIO_BASE="+linea.precioBase+",TOTAL_BASE="+linea.totalBase+
                        ",DES="+linea.des+",DESMON="+linea.desMon+
                        ",RECARGO="+linea.recargo+",RECARGOMONTO="+linea.recargoMonto+
                        ",CODDESC_APLICADO="+linea.codDescAplicado+
                        ",CODRECARGO_APLICADO="+linea.codRecargoAplicado+
                        " WHERE ITEM="+item);
                PromotionTrace.write(cont,"PROMO_RETURN_APPLIED","item="+item+
                        ";producto="+linea.producto+";codDesc="+linea.codDescAplicado+
                        ";codRecargo="+linea.codRecargoAplicado+";total="+linea.total);
            } catch (Exception e) {
                PromotionTrace.write(cont,"PROMO_RETURN_APPLY_ERROR","linea="+linea.lineKey+
                        ";error="+e.getClass().getSimpleName());
            }
        }
    }

    private void asegurarTrazabilidadPromocionDevolucion() {
        String[] columnas = {
                "PRECIO_BASE REAL DEFAULT 0 NOT NULL",
                "TOTAL_BASE REAL DEFAULT 0 NOT NULL",
                "DES REAL DEFAULT 0 NOT NULL",
                "DESMON REAL DEFAULT 0 NOT NULL",
                "RECARGO REAL DEFAULT 0 NOT NULL",
                "RECARGOMONTO REAL DEFAULT 0 NOT NULL",
                "CODDESC_APLICADO INTEGER DEFAULT 0 NOT NULL",
                "CODRECARGO_APLICADO INTEGER DEFAULT 0 NOT NULL"
        };
        for (String columna : columnas) {
            try { db.execSQL("ALTER TABLE T_CxCD ADD COLUMN "+columna); }
            catch (Exception ignored) { }
        }
    }

    private void restaurarIndividuales(List<clsClasses.VentaLinea> lineas) {
        for (clsClasses.VentaLinea linea : lineas) {
            linea.precio = linea.individualPrecio;
            linea.total = linea.individualTotal;
            linea.des = linea.individualDes;
            linea.desMon = linea.individualDesMon;
            linea.recargo = linea.individualRecargo;
            linea.recargoMonto = linea.individualRecargoMonto;
            linea.codDescAplicado = linea.individualCodDesc;
            linea.codRecargoAplicado = linea.individualCodRecargo;
            ActualizarLineaTVenta(linea);
        }
        PromotionTrace.write(cont,"INDIVIDUAL_FALLBACK","filas="+lineas.size()+
                ";accion=restaurado_antes_de_evaluar_combos");
    }

    private void asegurarSnapshotIndividual() {
        String[] columnas = {
                "INDIVIDUAL_SNAPSHOT INTEGER DEFAULT 0 NOT NULL",
                "INDIVIDUAL_PRECIO REAL DEFAULT 0 NOT NULL",
                "INDIVIDUAL_TOTAL REAL DEFAULT 0 NOT NULL",
                "INDIVIDUAL_DES REAL DEFAULT 0 NOT NULL",
                "INDIVIDUAL_DESMON REAL DEFAULT 0 NOT NULL",
                "INDIVIDUAL_RECARGO REAL DEFAULT 0 NOT NULL",
                "INDIVIDUAL_RECARGOMONTO REAL DEFAULT 0 NOT NULL",
                "INDIVIDUAL_CODDESC INTEGER DEFAULT 0 NOT NULL",
                "INDIVIDUAL_CODRECARGO INTEGER DEFAULT 0 NOT NULL"
        };
        for (String columna : columnas) {
            try { db.execSQL("ALTER TABLE T_VENTA ADD COLUMN "+columna); }
            catch (Exception ignored) { }
        }
    }

    private void guardarSnapshotIndividual(clsClasses.VentaLinea linea) {
        try {
            upd.init("T_VENTA");
            upd.Where("PRODUCTO='"+linea.producto+"' AND UM='"+linea.um+
                    "' AND SIN_EXISTENCIA="+linea.sinExistencia);
            upd.add("INDIVIDUAL_SNAPSHOT",1);
            upd.add("INDIVIDUAL_PRECIO",linea.precio);
            upd.add("INDIVIDUAL_TOTAL",linea.total);
            upd.add("INDIVIDUAL_DES",linea.des);
            upd.add("INDIVIDUAL_DESMON",linea.desMon);
            upd.add("INDIVIDUAL_RECARGO",linea.recargo);
            upd.add("INDIVIDUAL_RECARGOMONTO",linea.recargoMonto);
            upd.add("INDIVIDUAL_CODDESC",linea.codDescAplicado);
            upd.add("INDIVIDUAL_CODRECARGO",linea.codRecargoAplicado);
            db.execSQL(upd.SQL());
            linea.individualSnapshot=true;
            linea.individualPrecio=linea.precio;
            linea.individualTotal=linea.total;
            linea.individualDes=linea.des;
            linea.individualDesMon=linea.desMon;
            linea.individualRecargo=linea.recargo;
            linea.individualRecargoMonto=linea.recargoMonto;
            linea.individualCodDesc=linea.codDescAplicado;
            linea.individualCodRecargo=linea.codRecargoAplicado;
        } catch (Exception e) {
            PromotionTrace.write(cont,"INDIVIDUAL_FALLBACK_ERROR","linea="+linea.lineKey+
                    ";error="+e.getClass().getSimpleName());
        }
    }

    //#EJC20260817 fix(hh-combo-modified-order): un pedido guardado puede traer
    //el precio y CODDESC finales del combo. Ese resultado no es un fallback
    //individual valido y debe reconstruirse desde la base antes de reevaluar.
    private void reconstruirSnapshotIndividual(clsClasses.VentaLinea linea) {
        try {
            clsDescuento selector = new clsDescuento(cont,linea.producto,linea.cant,linea.peso,
                    linea.umStock,Con,db);
            clsClasses.clsBeDescuento descuento = selector.getDescuentoRecargo(false);
            clsClasses.clsBeDescuento recargo = selector.getDescuentoRecargo(true);
            double baseFacturacion = ObtenerCantidadLinea(linea);
            SapPromotionCalculator.Result calculo = SapPromotionCalculator.calculate(
                    SapPromotionCalculator.decimal(linea.precioBase),
                    SapPromotionCalculator.decimal(baseFacturacion),
                    toAdjustment(descuento),toAdjustment(recargo));

            linea.precio=calculo.derivedUnitPrice.doubleValue();
            linea.total=calculo.authoritativeFinalTotal.doubleValue();
            linea.totalBase=calculo.extendedBaseTotal.doubleValue();
            linea.des=descuento==null?0:descuento.valor;
            linea.desMon=calculo.discountTotal.doubleValue();
            linea.recargo=recargo==null?0:recargo.valor;
            linea.recargoMonto=calculo.surchargeTotal.doubleValue();
            linea.codDescAplicado=descuento==null?0:descuento.codDesc;
            linea.codRecargoAplicado=recargo==null?0:recargo.codDesc;
            guardarSnapshotIndividual(linea);
            PromotionTrace.write(cont,"INDIVIDUAL_FALLBACK_REBUILT",
                    "linea="+linea.lineKey+";motivo=PEDIDO_CARGADO_CON_COMBO"+
                    ";codDesc="+linea.codDescAplicado+
                    ";codRecargo="+linea.codRecargoAplicado);
        } catch (Exception e) {
            PromotionTrace.write(cont,"INDIVIDUAL_FALLBACK_ERROR","linea="+linea.lineKey+
                    ";motivo=REBUILD_MODIFIED_ORDER;error="+e.getClass().getSimpleName());
            guardarSnapshotIndividual(linea);
        }
    }

    private boolean esAjusteCombo(int codDesc, boolean esRecargo) {
        if (codDesc == 0) return false;
        Cursor cursor = null;
        try {
            cursor=Con.OpenDT("SELECT 1 FROM P_DESCUENTO WHERE CODDESC="+codDesc+
                    " AND ES_RECARGO="+(esRecargo?1:0)+" AND PTIPO=6 LIMIT 1");
            return cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            PromotionTrace.write(cont,"PROMO_COMBO_ID_ERROR","codDesc="+codDesc+
                    ";recargo="+esRecargo+";error="+e.getClass().getSimpleName());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private ComboResolution resolverMejorCombo(List<clsClasses.clsBeP_DESCUENTO> candidatos,
                                                List<clsClasses.VentaLinea> lineas,
                                                boolean esRecargo) {
        List<ComboEvaluation> completos = new ArrayList<>();
        int bonificados = contarBonificaciones();
        if (bonificados > 0) {
            //#EJC20260724 rule(hh-combo-bonus-excluded): una bonificacion puede conservar
            //precio de referencia, pero no completa requisitos ni recibe promociones.
            PromotionTrace.write(cont,"PROMO_COMBO_BONUS_EXCLUDED","recargo="+esRecargo+
                    ";filas="+bonificados+";motivo=regla_negocio");
        }

        for (clsClasses.clsBeP_DESCUENTO candidato : candidatos) {
            ComboEvaluation evaluacion = evaluarCombo(candidato,
                    GetDetalleComboDescuento(candidato.codDesc), lineas);
            if (evaluacion.completo) completos.add(evaluacion);
        }

        ComboResolution result = new ComboResolution();
        if (completos.isEmpty()) {
            PromotionTrace.write(cont,"PROMO_COMBO_SELECTION","recargo="+esRecargo+
                    ";accion=individual;motivo=ningun_combo_completo");
            return result;
        }

        ComboEvaluation mejor = completos.get(0);
        List<Integer> empatados = new ArrayList<>();
        for (ComboEvaluation actual : completos) {
            if (actual.condicion.prioridadDescuento == mejor.condicion.prioridadDescuento &&
                    actual.condicion.prioridad == mejor.condicion.prioridad) {
                empatados.add(actual.condicion.codDesc);
            }
        }

        if (empatados.size() > 1) {
            result.ambiguo = true;
            PromotionTrace.write(cont,"COMBO_SELECTION_AMBIGUOUS","recargo="+esRecargo+
                    ";prioridadDescuento="+mejor.condicion.prioridadDescuento+
                    ";prioridad="+mejor.condicion.prioridad+";coddesc="+empatados+
                    ";accion=individual");
            return result;
        }

        result.seleccion = mejor;
        PromotionTrace.write(cont,"PROMO_COMBO_SELECTED","codDesc="+mejor.condicion.codDesc+
                ";recargo="+esRecargo+";prioridadDescuento="+mejor.condicion.prioridadDescuento+
                ";prioridad="+mejor.condicion.prioridad);
        return result;
    }

    private ComboEvaluation evaluarCombo(clsClasses.clsBeP_DESCUENTO condicion,
                                          List<clsClasses.clsBeP_DESCUENTO_COMBO_DET> detalles,
                                          List<clsClasses.VentaLinea> lineas) {
        ComboEvaluation result = new ComboEvaluation();
        result.condicion = condicion;
        result.completo = !detalles.isEmpty();

        for (clsClasses.clsBeP_DESCUENTO_COMBO_DET detalle : detalles) {
            double acumulado = 0;
            Set<String> participantes = new HashSet<>();
            for (clsClasses.VentaLinea linea : lineas) {
                if (linea.promoEligible && linea.producto != null &&
                        linea.producto.equals(detalle.producto) &&
                        umCompatible(linea, detalle)) {
					double cantidadCompatible=ObtenerCantidadRequisito(linea,detalle);
					acumulado += cantidadCompatible;
                    participantes.add(linea.lineKey);
					String detalleUm="codDesc="+condicion.codDesc+";producto="+linea.producto+
							";umRequerida="+detalle.umVenta+";umLinea="+linea.um+
							";umStock="+linea.umStock+";factor="+linea.factor+
							";cantidadLinea="+linea.cant+";cantidadCompatible="+cantidadCompatible;
					Log.i("ROAD_COMBO_TRACE",detalleUm);
					PromotionTrace.write(cont,"PROMO_COMBO_UM_RESOLVED",detalleUm);
                }
            }

            boolean cumple = acumulado >= detalle.cantidad;
            PromotionTrace.write(cont,"PROMO_COMBO_REQUIREMENT","codDesc="+condicion.codDesc+
                    ";producto="+detalle.producto+";um="+detalle.umVenta+
                    ";obligatorio="+detalle.obligatorio+";cantidadCobrada="+acumulado+
                    ";requerida="+detalle.cantidad+";cumple="+cumple+
                    ";bonificadosIncluidos=0;criterio=UM_LINEA_O_UMSTOCK");

            if (!cumple) {
                if (detalle.obligatorio) result.completo = false;
                continue;
            }
            result.participantes.addAll(participantes);
        }
        return result;
    }

    private void aplicarResolucionConjunta(List<clsClasses.VentaLinea> lineas,
                                           ComboResolution descuento,
                                           ComboResolution recargo) {
        for (clsClasses.VentaLinea linea : lineas) {
            boolean aplicaDescuento = descuento.seleccion != null &&
                    descuento.seleccion.participantes.contains(linea.lineKey);
            boolean aplicaRecargo = recargo.seleccion != null &&
                    recargo.seleccion.participantes.contains(linea.lineKey);
            if (!aplicaDescuento && !aplicaRecargo) continue;

            double baseFacturacion = ObtenerCantidadLinea(linea);
            if (baseFacturacion <= 0) continue;
            BigDecimal baseTotal = linea.totalBase > 0
                    ? SapPromotionCalculator.decimal(linea.totalBase)
                    : SapPromotionCalculator.money(SapPromotionCalculator.decimal(linea.precioBase)
                    .multiply(SapPromotionCalculator.decimal(baseFacturacion)));

            BigDecimal descuentoTotal = aplicaDescuento
                    ? calcularAjusteCombo(linea, descuento.seleccion.condicion)
                    : SapPromotionCalculator.decimal(linea.desMon);
            BigDecimal recargoTotal = aplicaRecargo
                    ? calcularAjusteCombo(linea, recargo.seleccion.condicion)
                    : SapPromotionCalculator.decimal(linea.recargoMonto);
            BigDecimal totalFinal = baseTotal.subtract(descuentoTotal).add(recargoTotal)
                    .setScale(SapPromotionCalculator.MONEY_SCALE, SapPromotionCalculator.SAP_ROUNDING);

            if (aplicaDescuento) {
                linea.des = descuento.seleccion.condicion.valor;
                linea.desMon = descuentoTotal.doubleValue();
                linea.codDescAplicado = descuento.seleccion.condicion.codDesc;
            }
            if (aplicaRecargo) {
                linea.recargo = recargo.seleccion.condicion.valor;
                linea.recargoMonto = recargoTotal.doubleValue();
                linea.codRecargoAplicado = recargo.seleccion.condicion.codDesc;
            }
            linea.total = totalFinal.doubleValue();
            linea.precio = totalFinal.divide(SapPromotionCalculator.decimal(baseFacturacion),
                    SapPromotionCalculator.UNIT_PRICE_SCALE, RoundingMode.HALF_UP).doubleValue();
            ActualizarLineaTVenta(linea);
            PromotionTrace.write(cont,"PROMO_COMBO_APPLIED","producto="+linea.producto+
                    ";linea="+linea.lineKey+";codDesc="+linea.codDescAplicado+
                    ";codRecargo="+linea.codRecargoAplicado+";descuento="+linea.desMon+
                    ";recargo="+linea.recargoMonto+";total="+linea.total);
        }
    }

    private BigDecimal calcularAjusteCombo(clsClasses.VentaLinea linea,
                                            clsClasses.clsBeP_DESCUENTO condicion) {
        double baseFacturacion = ObtenerCantidadLinea(linea);
        SapPromotionCalculator.Result calculo = SapPromotionCalculator.calculate(
                SapPromotionCalculator.decimal(linea.precioBase),
                SapPromotionCalculator.decimal(baseFacturacion),
                condicion.esRecargo ? SapPromotionCalculator.Adjustment.none() : toAdjustment(condicion),
                condicion.esRecargo ? toAdjustment(condicion) : SapPromotionCalculator.Adjustment.none());
        return condicion.esRecargo ? calculo.surchargeTotal : calculo.discountTotal;
    }

    private int contarBonificaciones() {
        Cursor cursor = null;
        try {
            cursor = Con.OpenDT("SELECT COUNT(*) FROM T_BONITEM");
            return cursor != null && cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } catch (Exception ignored) {
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private static class ComboEvaluation {
        clsClasses.clsBeP_DESCUENTO condicion;
        boolean completo;
        Set<String> participantes = new HashSet<>();
    }

    private static class ComboResolution {
        ComboEvaluation seleccion;
        boolean ambiguo;
    }

	private Map<String,Double> CargarBonificacionesParaCombo() {
		Map<String,Double> result=new HashMap<>();
		Cursor cursor=null;
		try {
			cursor=Con.OpenDT("SELECT BONIID,UMVENTA,SUM(CASE WHEN POR_PESO='S' THEN PESO ELSE CANT END) "+
					"FROM T_BONITEM GROUP BY BONIID,UMVENTA");
			if (cursor!=null && cursor.moveToFirst()) {
				do { result.put(cursor.getString(0)+"|"+cursor.getString(1),cursor.getDouble(2)); }
				while (cursor.moveToNext());
			}
		} catch (Exception e) {
			PromotionTrace.write(cont,"PROMO_BONUS_READ_ERROR","mensaje="+e.getClass().getSimpleName());
		} finally { if (cursor!=null) cursor.close(); }
		return result;
	}

	private double cantidadBonificadaCompatible(Map<String,Double> bonificaciones,
			clsClasses.clsBeP_DESCUENTO_COMBO_DET detalle) {
		double total=0;
		for (Map.Entry<String,Double> item:bonificaciones.entrySet()) {
			String[] key=item.getKey().split("\\|",-1);
			if (!key[0].equalsIgnoreCase(detalle.producto)) continue;
			if (detalle.umVenta==null || detalle.umVenta.trim().isEmpty() || detalle.umVenta.equalsIgnoreCase(key[1])) total+=item.getValue();
		}
		return total;
	}

	private SapPromotionCalculator.Adjustment toAdjustment(clsClasses.clsBeP_DESCUENTO condicion) {
		if (condicion == null) return SapPromotionCalculator.Adjustment.none();
		SapPromotionCalculator.AdjustmentKind kind;
		if (!"S".equalsIgnoreCase(condicion.porPorcentaje)) kind=SapPromotionCalculator.AdjustmentKind.FIXED;
		else if ("M".equalsIgnoreCase(condicion.descTipo)) kind=SapPromotionCalculator.AdjustmentKind.PERCENTAGE_MULTIPLE;
		else kind=SapPromotionCalculator.AdjustmentKind.PERCENTAGE_RANGE;
		return new SapPromotionCalculator.Adjustment(kind,SapPromotionCalculator.decimal(condicion.valor));
	}

	private SapPromotionCalculator.Adjustment toAdjustment(clsClasses.clsBeDescuento condicion) {
		if (condicion == null || condicion.valor == 0) return SapPromotionCalculator.Adjustment.none();
		SapPromotionCalculator.AdjustmentKind kind;
		if (!"S".equalsIgnoreCase(condicion.porPorcentaje)) {
			kind=SapPromotionCalculator.AdjustmentKind.FIXED;
		} else if ("M".equalsIgnoreCase(condicion.descTipo)) {
			kind=SapPromotionCalculator.AdjustmentKind.PERCENTAGE_MULTIPLE;
		} else {
			kind=SapPromotionCalculator.AdjustmentKind.PERCENTAGE_RANGE;
		}
		return new SapPromotionCalculator.Adjustment(kind,
				SapPromotionCalculator.decimal(condicion.valor));
	}

	private boolean umCompatible(clsClasses.VentaLinea linea, clsClasses.clsBeP_DESCUENTO_COMBO_DET detalle) {
		if (detalle.umVenta == null || detalle.umVenta.trim().isEmpty()) return true;
		return detalle.umVenta.equalsIgnoreCase(linea.um) ||
				detalle.umVenta.equalsIgnoreCase(linea.umStock) ||
				detalle.umVenta.equalsIgnoreCase(gll.umpeso);
	}

	//#EJC20260731 fix(hh-combo-cantidad-comercial): los rangos/requisitos usan la
	//UM solicitada por el detalle. CANT esta persistida en UMSTOCK; solo se convierte
	//al factor cuando el requisito esta expresado en la UM del precio.
	private double ObtenerCantidadRequisito(clsClasses.VentaLinea linea,
			clsClasses.clsBeP_DESCUENTO_COMBO_DET detalle) {
		String umRequerida=detalle.umVenta==null?"":detalle.umVenta.trim();
		if (umRequerida.equalsIgnoreCase(gll.umpeso)) return linea.peso;
		if (umRequerida.equalsIgnoreCase(linea.umStock)) return linea.cant;
		if (umRequerida.equalsIgnoreCase(linea.um) && linea.umStock != null &&
				!linea.um.equalsIgnoreCase(linea.umStock) && linea.factor>0) {
			return linea.cant*linea.factor;
		}
		return linea.cant;
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
		//#EJC20260731 fix(hh-combo-base-extendida): TOTAL_BASE/PRECIO_BASE conserva
		//la base monetaria autoritativa (por ejemplo, 3 CA x 16 = 48 UN para Rosti).
		if (l.totalBase>0 && l.precioBase>0) return l.totalBase/l.precioBase;
        boolean esDevolucionPorPeso = l.lineKey != null && l.lineKey.startsWith("DEV|") &&
                l.sinExistencia == 1;
        boolean esPorPeso = esDevolucionPorPeso || (l.um != null && l.um.equals(gll.umpeso));
        return esPorPeso ? l.peso : l.cant;
    }

    private void asegurarPromoElegibleDevolucion() {
        try { db.execSQL("ALTER TABLE T_CxCD ADD COLUMN PROMO_ELEGIBLE INTEGER DEFAULT 1 NOT NULL"); }
        catch (Exception ignored) { }
    }

    public void opendb() {
        try {
            db = Con.getWritableDatabase();
            Con.vDatabase =db;
        } catch (Exception ignored) {
        }
    }
}
