package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.dts.roadp.promotions.PromotionTrace;
import com.dts.roadp.promotions.SapPromotionCalculator;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Precio {

	public double costo,descmon,imp,impval,tot,precsin,totsin,precdoc,precioespecial, recargoMonto, recargo,precioBase,totalBase;
	public int codDescAplicado,codRecargoAplicado;
	
	private int active;
	private android.database.sqlite.SQLiteDatabase db;
	private BaseDatos Con;
	private String sql;
	
	private Context cont;
	
	private DecimalFormat ffrmprec;
	private MiscUtils mu;
	
	private String prodid,um,umpeso,umventa;
	private double cant,desc,prec;
	private int nivel,ndec;
	private boolean porpeso;
	public clsClasses.clsBeDescuento BeDescuento = null;
	public clsClasses.clsBeDescuento BeRecargo = null;
	private appGlobals gll;

	public Precio(Context context,MiscUtils mutil,int numdec) {
		
		cont=context;
		mu=mutil;
		ndec=numdec;

		try {
			active=0;
			Con = new BaseDatos(context);
			opendb();
		} catch (Exception e) {
		}
		
		costo=0;descmon=0;imp=0;tot=0;recargoMonto=0;
		
		ffrmprec = new DecimalFormat("#0.00");

		this.gll = ((appGlobals) context.getApplicationContext());
		
	}

	public double precio(String prod,double pcant,int nivelprec,String unimedida,String unimedidapeso,double ppeso,String umven) {
		return precio(prod,pcant,nivelprec,unimedida,unimedidapeso,ppeso,umven,0);
	}

	//#EJC20260728 fix(hh-rosti-price-factor): separa cantidad comercial de base monetaria convertida.
	public double precio(String prod,double pcant,int nivelprec,String unimedida,String unimedidapeso,
						 double ppeso,String umven,double baseFacturacionConvertida) {

		prodid=prod;cant=pcant;nivel=nivelprec;
		um=unimedida;umpeso=unimedidapeso;umventa=umven;
		prec=0;costo=0;descmon=0;imp=0;tot=0;precioespecial=0;
		recargo = 0;recargoMonto=0;

		clsDescuento clsDesc=new clsDescuento(cont,prodid,cant, ppeso, umventa);
		BeDescuento = clsDesc.getDescuentoRecargo(false);
		BeRecargo = clsDesc.getDescuentoRecargo(true);

		//#EJC20260805 fix(hh-desc-range-edit): el valor temporal solo pertenece a la
		//misma confirmacion de producto y cantidad; nunca debe sobrevivir una edicion.
		boolean descuentoTemporalVigente = gll.promdesc != 0 &&
				prodid.equalsIgnoreCase(gll.promprod) &&
				Double.compare(cant, gll.promcant) == 0;
		if (descuentoTemporalVigente && BeDescuento != null) {
			if (gll.promdesc != BeDescuento.valor) {
				BeDescuento.valor = gll.promdesc;
			}
		}

		if (gll.recargo != 0 && BeRecargo != null) {
			if (gll.recargo != BeRecargo.valor) {
				BeRecargo.valor = gll.recargo;
			}
		}

		if (cant>0) prodPrecio(ppeso,baseFacturacionConvertida);else prodPrecioBase();

		return prec;
	}

	public boolean existePrecioEspecial(String prod,double pcant,String cliente,String clitipo,String unimedida,String unimedidapeso,double ppeso) {
		prodid=prod;cant=pcant;um=unimedida;umventa=unimedida;
		umpeso=unimedidapeso;
		precioespecial=0;

		//#EJC20260728 fix(hh-base-price-only): P_PRODPRECIO es la unica fuente
		//del precio base; TMP_PRECESPEC se conserva solo por compatibilidad de esquema.
		return false;
	}

	
	// Private
	
	private void prodPrecio(double ppeso,double baseFacturacionConvertida) {
		Cursor DT;
		double pr,stot,pprec,tsimp;
		String sprec="";
	
		try {

			opendb();

			if (ppeso>0) {
				sql="SELECT PRECIO FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+nivel+") AND (UNIDADMEDIDA='"+umpeso+"') ";
			} else {
				sql="SELECT PRECIO FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+nivel+") AND (UNIDADMEDIDA='"+um+"') ";
			}

			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			pr=DT.getDouble(0);

			if(DT!=null) DT.close();
			
		} catch (Exception e) {
			pr=0;

			try {
				sql="SELECT PRECIO FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+nivel+") AND (UNIDADMEDIDA='"+umventa+"') ";
				DT=Con.OpenDT(sql);
				DT.moveToFirst();
				pr=DT.getDouble(0);

				if(DT!=null) DT.close();
			} catch (Exception ee) {
				pr=0;
			}
	    }

		//#EJC20260721 feat(hh-sap-calculator): total extendido autoritativo y precio derivado.
		double baseFacturacion = baseFacturacionConvertida > 0
				? baseFacturacionConvertida : (ppeso > 0 ? ppeso : cant);
		if (baseFacturacion <= 0) baseFacturacion = 1;
		SapPromotionCalculator.Result resultado = SapPromotionCalculator.calculate(
				SapPromotionCalculator.decimal(pr), SapPromotionCalculator.decimal(baseFacturacion),
				toAdjustment(BeDescuento), toAdjustment(BeRecargo));

		desc = BeDescuento == null ? 0 : BeDescuento.valor;
		recargo = BeRecargo == null ? 0 : BeRecargo.valor;
		descmon = resultado.discountTotal.doubleValue();
		recargoMonto = resultado.surchargeTotal.doubleValue();
		precioBase = resultado.baseUnitPrice.doubleValue();
		totalBase = resultado.extendedBaseTotal.doubleValue();
		codDescAplicado = BeDescuento == null ? 0 : BeDescuento.codDesc;
		codRecargoAplicado = BeRecargo == null ? 0 : BeRecargo.codDesc;
		totsin = resultado.authoritativeFinalTotal.doubleValue();
		precsin = resultado.derivedUnitPrice.doubleValue();

		imp=getImp();
		BigDecimal impuestoTotal = SapPromotionCalculator.money(resultado.authoritativeFinalTotal
				.multiply(SapPromotionCalculator.decimal(imp)).divide(new BigDecimal("100")));
		BigDecimal totalConImpuesto = resultado.authoritativeFinalTotal.add(impuestoTotal)
				.setScale(SapPromotionCalculator.MONEY_SCALE, SapPromotionCalculator.SAP_ROUNDING);
		impval = impuestoTotal.doubleValue();
		tot = totalConImpuesto.doubleValue();
		prec = totalConImpuesto.divide(SapPromotionCalculator.decimal(baseFacturacion),
				SapPromotionCalculator.UNIT_PRICE_SCALE, SapPromotionCalculator.SAP_ROUNDING).doubleValue();
		precdoc = prec;

		String detalleCalculo="producto="+prodid+";cantidad="+cant+";peso="+ppeso+
				";umPrecio="+umventa+";basePrecio="+pr+";baseFacturacion="+baseFacturacion+
				";baseConvertida="+baseFacturacionConvertida+
				";codDesc="+(BeDescuento==null?0:BeDescuento.codDesc)+";tipo="+(BeDescuento==null?"":BeDescuento.descTipo)+
				";descuento="+descmon+";recargo="+recargoMonto+";total="+tot+";precioDerivado="+prec;
		PromotionTrace.write(cont,"PROMO_CALCULATION",detalleCalculo);
		//#EJC20260731 trace(hh-rosti-price): evidencia visible en Logcat del cálculo final.
		Log.i("ROAD_PRICE_TRACE",detalleCalculo);

	}

	private SapPromotionCalculator.Adjustment toAdjustment(clsClasses.clsBeDescuento condicion) {
		if (condicion == null || condicion.valor == 0) return SapPromotionCalculator.Adjustment.none();
		SapPromotionCalculator.AdjustmentKind kind;
		if (!"S".equalsIgnoreCase(condicion.porPorcentaje)) {
			kind = SapPromotionCalculator.AdjustmentKind.FIXED;
		} else if ("M".equalsIgnoreCase(condicion.descTipo)) {
			kind = SapPromotionCalculator.AdjustmentKind.PERCENTAGE_MULTIPLE;
		} else {
			kind = SapPromotionCalculator.AdjustmentKind.PERCENTAGE_RANGE;
		}
		return new SapPromotionCalculator.Adjustment(kind, SapPromotionCalculator.decimal(condicion.valor));
	}
	
	private void prodPrecioBase() {

		Cursor DT;
		double pr,stot,pprec,tsimp;
		String sprec="";
	
		try {

			opendb();

			sql="SELECT PRECIO FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+nivel+") AND (UNIDADMEDIDA='"+um+"') ";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
							  
			pr=DT.getDouble(0);
			if(DT!=null) DT.close();
			
		} catch (Exception e) {
			pr=0;
	    }	
		
		totsin=pr;tsimp=mu.round(totsin,ndec);
		
		imp=getImp();
		pr=pr*(1+imp/100);
		
		stot=pr;stot=mu.round(stot,ndec);
		
		if (imp>0) impval=stot-tsimp; else impval=0;
		
		descmon=0;
		
		tot=stot-descmon;
		prec=tot;
			
		try {
			sprec=ffrmprec.format(prec);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			pprec=mu.round(pprec,ndec);
		} catch (Exception e) {
			pprec=prec;
		}
		prec=pprec;
		
		if (imp==0) precsin=prec; else precsin=prec/(1+imp/100);
		
		totsin=mu.round(precsin,ndec);
		precsin=totsin;	
		
		try {
			sprec=ffrmprec.format(precsin);sprec=sprec.replace(",",".");
			pprec=Double.parseDouble(sprec);
			pprec=mu.round(pprec,ndec);
		} catch (Exception e) {
			pprec=precsin;
		}
		precsin=pprec;

	}
	
	private double getImp() {
		Cursor DT;
		double imv=0,im1=0,im2=0,im3=0;
		int ic1,ic2,ic3;
		
		try {
			sql="SELECT IMP1,IMP2,IMP3 FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
							  
			ic1=DT.getInt(0);
			ic2=DT.getInt(1);
			ic3=DT.getInt(2);
			
			if (ic1>0) {
				sql="SELECT VALOR FROM P_IMPUESTO WHERE CODIGO="+ic1;
	           	DT=Con.OpenDT(sql);
	           	
				try {
					DT.moveToFirst();
					im1=DT.getDouble(0);
				} catch (Exception e) {
					im1=0;
				}	
				
			}
			
			if (ic2>0) {
				sql="SELECT VALOR FROM P_IMPUESTO WHERE CODIGO="+ic2;
	           	DT=Con.OpenDT(sql);
	           	
				try {
					DT.moveToFirst();
					im2=DT.getDouble(0);
				} catch (Exception e) {
					im2=0;
				}	
				
			}
			
			if (ic3>0) {
				sql="SELECT VALOR FROM P_IMPUESTO WHERE CODIGO="+ic3;
	           	DT=Con.OpenDT(sql);
	           	
				try {
					DT.moveToFirst();
					im3=DT.getDouble(0);
				} catch (Exception e) {
					im3=0;
				}	
				
			}

			if(DT!=null) DT.close();

			imv=im1+im2+im3;
			
			return imv;
			
		} catch (Exception e) {
			return 0;
	    }		
		
	}

	private boolean prodPrecioEsp(double ppeso,String cliente,String clitipo) {
		// #EJC20260730 feat(hh-no-special-price): road_2028 usa P_PRODPRECIO y promociones.
		if (!preciosEspecialesHabilitados()) return false;
		Cursor dt = null;
		double pr,prr,stot,pprec,tsimp;
		String sprec="",vcod,vval;

		pr=0;
		try {

			opendb();

			if (ppeso>0) {
				sql=" SELECT PRECIO,CODIGO,VALOR FROM TMP_PRECESPEC "+
					" WHERE (PRODUCTO='"+prodid+"') AND (UNIDADMEDIDA='"+umpeso+"') "+
					" AND ((VALOR='" + cliente + "' AND CODIGO ='CLIENTE') " +
				    " OR (CODIGO='RUTA' AND VALOR='" + cliente + "') OR  " +
				    " (CODIGO ='TIPO' AND VALOR IN (SELECT TIPO FROM P_CLIENTE WHERE CODIGO='" + cliente + "')))" +
					" ORDER BY CODIGO ASC ";
			} else {
				sql=" SELECT PRECIO,CODIGO,VALOR FROM TMP_PRECESPEC "+
					" WHERE (PRODUCTO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"') "+
					" AND ((VALOR='" + cliente + "' AND CODIGO ='CLIENTE') " +
					" OR (CODIGO='RUTA' AND VALOR='" + cliente + "') OR  " +
					" (CODIGO ='TIPO' AND VALOR IN (SELECT TIPO FROM P_CLIENTE WHERE CODIGO='" + cliente + "')))" +
					" ORDER BY CODIGO ASC ";
			}
			dt=Con.OpenDT(sql);

			if (dt.getCount() > 0) {
				dt.moveToFirst();
				while (!dt.isAfterLast()) {
					prr = dt.getDouble(0);
					vcod = dt.getString(1);
					vval=dt.getString(2);

					if (vcod.equalsIgnoreCase("TIPO")) {
						if (vval.equalsIgnoreCase(clitipo)){
							pr=prr;break;
						}
					} else {
						if (vval.equalsIgnoreCase(cliente)){
							pr=prr;break;
						}
					}

					dt.moveToNext();
				}
			}

			if(dt!=null) dt.close();

		} catch (Exception e) {
			pr=0;
		}finally {
			if (dt!=null) dt.close();
		}

		if (pr <= 0) {
			PromotionTrace.write(cont,"PRICE_MODE",
					"producto="+prodid+";modo=STANDARD_PROMOTION;precioEspecial=NO;precioBase="+precioBase+";total="+tot);
			return false;
		}

		//#EJC20260724 fix(hh-special-price-exclusive): precio especial y promociones son
		//mutuamente excluyentes; nunca aplicar descuento/recargo sobre TMP_PRECESPEC.
		double baseFacturacion=ppeso>0?ppeso:cant;
		if (baseFacturacion<=0) baseFacturacion=1;
		SapPromotionCalculator.Result resultado=SapPromotionCalculator.calculate(
				SapPromotionCalculator.decimal(pr),SapPromotionCalculator.decimal(baseFacturacion),
				SapPromotionCalculator.Adjustment.none(),SapPromotionCalculator.Adjustment.none());
		BeDescuento=null;
		BeRecargo=null;
		desc=0;
		recargo=0;
		descmon=resultado.discountTotal.doubleValue();
		recargoMonto=resultado.surchargeTotal.doubleValue();
		precioBase=resultado.baseUnitPrice.doubleValue();
		totalBase=resultado.extendedBaseTotal.doubleValue();
		codDescAplicado=0;
		codRecargoAplicado=0;
		totsin=resultado.authoritativeFinalTotal.doubleValue();
		precsin=resultado.derivedUnitPrice.doubleValue();
		imp=getImp();
		BigDecimal impuestoTotal=SapPromotionCalculator.money(resultado.authoritativeFinalTotal
				.multiply(SapPromotionCalculator.decimal(imp)).divide(new BigDecimal("100")));
		BigDecimal totalConImpuesto=resultado.authoritativeFinalTotal.add(impuestoTotal)
				.setScale(SapPromotionCalculator.MONEY_SCALE,SapPromotionCalculator.SAP_ROUNDING);
		impval=impuestoTotal.doubleValue();
		tot=totalConImpuesto.doubleValue();
		prec=totalConImpuesto.divide(SapPromotionCalculator.decimal(baseFacturacion),
				SapPromotionCalculator.UNIT_PRICE_SCALE,SapPromotionCalculator.SAP_ROUNDING).doubleValue();
		precdoc=prec;
		precioespecial=prec;

		PromotionTrace.write(cont,"PRICE_MODE",
				"producto="+prodid+";modo=SPECIAL;precioEspecial="+pr+";baseFacturacion="+baseFacturacion+
				";descuento=0;recargo=0;total="+tot+";precioDerivado="+prec);
		return true;

	}

	private boolean preciosEspecialesHabilitados() {
		return false;
	}


	// Aux
	
	public double round2(double val){
		int ival;
		
		val=(double) (100*val);
		double rslt=Math.round(val);
		rslt=Math.floor(rslt);
		
		ival=(int) rslt;
		rslt=(double) ival;
		
		return (double) (rslt/100);
	}
	
 	private void opendb() {
		try {
			db = Con.getWritableDatabase();
			if (db!= null) {
				Con.vDatabase=db;
				active=1;
			} else {
				active = 0;
			}
		} catch (Exception e) {
				active= 0;
		}
	}		

}
