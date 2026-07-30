package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;

import com.dts.roadp.promotions.PromotionTrace;

import java.util.ArrayList;

public class clsDescuento {
	protected clsClasses clsCls = new clsClasses();
	public double monto;
	
	private int active;
	private android.database.sqlite.SQLiteDatabase db;
	private BaseDatos Con;
	private String vSQL;
	
	private MiscUtils MU;

	private ArrayList<Double> items = new ArrayList<Double>();
	private ArrayList<Double> montos = new ArrayList<Double>();
	
	private Context cont;
	
	private String prodid,lineaid,slineaid,marcaid,canttipo, umVenta;
	private double cant,vmax,dmax, ppeso;
	private boolean acum;
	private appGlobals gll;
	
	public clsDescuento(Context context,String producto,double cantidad, double peso, String umventa) {
		this(context, producto, cantidad, peso, umventa, null, null);
	}

	//#EJC20260721 fix(hh-desc-transaction): reutiliza la conexion activa en flujos transaccionales.
	public clsDescuento(Context context,String producto,double cantidad, double peso, String umventa,
					 BaseDatos dbconnection, android.database.sqlite.SQLiteDatabase database) {
		this.gll = ((appGlobals) context.getApplicationContext());
		cont=context;
		
		prodid=producto;cant=cantidad;
		umVenta =  umventa;
		ppeso = peso;

		try {
			active=0;
			if (dbconnection != null && database != null) {
				Con=dbconnection;
				db=database;
				Con.vDatabase=db;
				active=1;
			} else {
				Con = new BaseDatos(context);
				opendb();
			}
		} catch (Exception e) {
		}
	    
	    MU=new MiscUtils(context);
		
		vmax=0;dmax=0;acum=true;
		lineaid="";slineaid="";marcaid="";
		
	}
	
	// Descuento local
	public  clsClasses.clsBeDescuento getDescuentoRecargo(boolean esRecargo) {
		clsClasses.clsBeDescuento TmpDescuento = null;
		boolean encontrado = false;
		Cursor DT;

		try {
			if (validaPermisos()) {
				//#EJC20260724 fix(TC0015-hh-escala-um): la base se resuelve por la UMVENTA de cada descuento.
				String umPesoSql = sqlLiteral(gll.umpeso);
				String umStockSql = gll.umstock;
				String baseEvaluacionSql = "CASE WHEN UMVENTA='"+umPesoSql+"' THEN "+ppeso+
						" WHEN UMVENTA='"+umStockSql+"' THEN "+cant+" ELSE "+cant+" END";
				vSQL= "SELECT PRODUCTO,PTIPO,VALOR,PORCANT,PORPORCENTAJE,CODDESC,DESCTIPO,PRIORIDAD,IFNULL(PRIORIDAD_DESCUENTO,0),UMVENTA,RANGOINI,RANGOFIN "+
						"FROM T_DESC WHERE ES_RECARGO="+(esRecargo ? 1 : 0)+
						" AND PTIPO<4 AND GLOBDESC='N' AND ("+
						" (DESCTIPO='M' AND "+baseEvaluacionSql+">=RANGOINI) OR "+
						" (DESCTIPO='R' AND "+baseEvaluacionSql+">=RANGOINI AND "+baseEvaluacionSql+"<=RANGOFIN "+
						")) ORDER BY CASE WHEN DESCTIPO='M' THEN 0 ELSE 1 END,PRIORIDAD_DESCUENTO ASC,PRIORIDAD ASC";

				DT=Con.OpenDT(vSQL);

				if (DT.getCount()==0) return null;
				PromotionTrace.write(cont,"PROMO_CANDIDATES","producto="+prodid+";recargo="+esRecargo+
						";base=por_umventa;umStock="+umVenta+";umPeso="+gll.umpeso+
						";cantidad="+cant+";peso="+ppeso+";rows="+DT.getCount());

				DT.moveToFirst();
				while (!DT.isAfterLast()) {
					TmpDescuento = clsCls.new clsBeDescuento();
					TmpDescuento.porPorcentaje = DT.getString(4);
					TmpDescuento.codDesc = DT.getInt(5);
					TmpDescuento.descTipo = DT.getString(6);
					TmpDescuento.prioridad = DT.getInt(7);
					TmpDescuento.prioridadDescuento = DT.getInt(8);
					TmpDescuento.umVenta = DT.getString(9);
					TmpDescuento.rangoIni = DT.getDouble(10);
					TmpDescuento.rangoFin = DT.getDouble(11);
					TmpDescuento.pTipo = DT.getInt(1);
					TmpDescuento.producto = DT.getString(0);
					double baseCandidato = gll.umpeso.equalsIgnoreCase(TmpDescuento.umVenta) ? ppeso : cant;
					PromotionTrace.write(cont,"PROMO_CANDIDATE","producto="+prodid+";codDesc="+TmpDescuento.codDesc+
							";tipo="+TmpDescuento.descTipo+";prioridadDescuento="+TmpDescuento.prioridadDescuento+
							";prioridad="+TmpDescuento.prioridad+";rango="+TmpDescuento.rangoIni+"-"+TmpDescuento.rangoFin+
							";um="+TmpDescuento.umVenta+";base="+baseCandidato+";valor="+DT.getDouble(2));

					String valor = DT.getString(0);

					switch (DT.getInt(1)) {
						case 0:
							if (valor.equalsIgnoreCase(prodid) || valor.equalsIgnoreCase("*")) {
								TmpDescuento.valor = DT.getDouble(2);
								encontrado = true;
							}
							break;

						case 1:
							if (valor.equalsIgnoreCase(slineaid)) {
								TmpDescuento.valor = DT.getDouble(2);
								encontrado = true;
							}
							break;

						case 2:
							if (valor.equalsIgnoreCase(lineaid)) {
								TmpDescuento.valor = DT.getDouble(2);
								encontrado = true;
							}
							break;

						case 3:
							if (valor.equalsIgnoreCase(marcaid)) {
								TmpDescuento.valor = DT.getDouble(2);
								encontrado = true;
							}
							break;
					}

					if (encontrado) {
						PromotionTrace.write(cont,"PROMO_SELECTED","producto="+prodid+";codDesc="+TmpDescuento.codDesc+
								";tipo="+TmpDescuento.descTipo+";recargo="+esRecargo+";base="+baseCandidato+
								";prioridadDescuento="+TmpDescuento.prioridadDescuento+";prioridad="+TmpDescuento.prioridad);
						break;
					}

					DT.moveToNext();
				}

				if (!encontrado) {
					return null;
				}
			}
		} catch (Exception e) {
			MU.msgbox(e.getMessage());
		}

		return TmpDescuento;
	}

	private String sqlLiteral(String value) {
		return value == null ? "" : value.replace("'", "''");
	}

	public double getDesc(){
		double dval=0;
		
		items.clear();
		
		if (!validaPermisos()) return 0;
		
		listaDescRango();
		listaDescMult();
		
		dval=descFinal();
		monto=montoFinal();
		
		return dval;
	}
	
	private double descFinal() {
		double df=0,dm=0,sd=0;
		double vd;
		
		if (items.size()==0) return 0;
		
		for(int i = 0; i < items.size(); i++ ) {
			vd=items.get(i);
			sd+=vd;
			if (vd>dm) dm=vd;
		}	
		
		if (acum) {
			df=sd;			
		} else {	
			df=dm;
		}
		
		if (dmax>0) {
			if (df>dmax) df=dmax;
		}
		
		return df;
	}
	
	private double montoFinal() {
		double df=0,dm=0,sd=0;
		double vd;
		
		if (montos.size()==0) return 0;
		
		for(int i = 0; i < montos.size(); i++ ) {
			vd=montos.get(i);
			sd+=vd;
			if (vd>dm) dm=vd;
		}	
		
		if (acum) {
			df=sd;			
		} else {	
			df=dm;
		}
		
		if (dmax>0) {
			if (df>dmax) df=dmax;
		}
		
		return df;
	}
	
	private void listaDescRango() {
		Cursor DT;
		String iid;
		double val;
		
		try {
			vSQL="SELECT PRODUCTO,PTIPO,VALOR,PORCANT "+
				 "FROM T_DESC WHERE  ("+cant+">=RANGOINI) AND ("+cant+"<=RANGOFIN) "+
				 "AND (PTIPO<4) AND (DESCTIPO='R') AND (GLOBDESC='N') ";
			     //"AND (PTIPO<4) AND (DESCTIPO='R') AND (GLOBDESC='N') AND ((PORCANT='S') OR (PORCANT='1'))";

			DT=Con.OpenDT(vSQL);
		
			
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
				iid=DT.getString(0);
				canttipo=DT.getString(3);
				val=0;
				
				//Toast.makeText(cont,""+iid, Toast.LENGTH_LONG).show();
				
				switch (DT.getInt(1)) {
					case 0: 
						if (iid.equalsIgnoreCase(prodid) || iid.equalsIgnoreCase("*")) val=DT.getDouble(2);break;
					case 1: 
						if (iid.equalsIgnoreCase(slineaid)) val=DT.getDouble(2);break;
					case 2: 
						if (iid.equalsIgnoreCase(lineaid)) val=DT.getDouble(2);break;
					case 3: 
						if (iid.equalsIgnoreCase(marcaid)) val=DT.getDouble(2);break;
				}		
				
				if (val>0) {
					if (canttipo.equalsIgnoreCase("1")) montos.add(val); else items.add(val);
				}
				
				DT.moveToNext();
			}	
			
		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }
		
		
	}
	
	private void listaDescMult(){
		Cursor DT;
		String iid;
		double val,mcant,mul;
		
		try {
			vSQL="SELECT PRODUCTO,PTIPO,RANGOINI,RANGOFIN,VALOR,PORCANT "+
				 "FROM T_DESC WHERE ("+cant+">=RANGOINI) "+
				 "AND (PTIPO<4) AND (DESCTIPO='M') AND (GLOBDESC='N') ";
			   //"AND (PTIPO<4) AND (DESCTIPO='M') AND (GLOBDESC='N') AND ((PORCANT='S') OR (PORCANT='1'))";

			DT=Con.OpenDT(vSQL);
			
			if (DT.getCount()==0) return;
						
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
					
				iid=DT.getString(0);
				canttipo=DT.getString(3);
				val=0;
				
				switch (DT.getInt(1)) {
					case 0: 
						if (iid.equalsIgnoreCase(prodid) || iid.equalsIgnoreCase("*")) val=DT.getDouble(4);break;
					case 1: 
						if (iid.equalsIgnoreCase(slineaid)) val=DT.getDouble(4);break;
					case 2: 
						if (iid.equalsIgnoreCase(lineaid)) val=DT.getDouble(4);break;
					case 3: 
						if (iid.equalsIgnoreCase(marcaid)) val=DT.getDouble(4);break;
				}
				
				if (val>0) {				
					mcant=cant-DT.getDouble(2);
					mul=DT.getDouble(3);
					
					if (mul>0) {
						mcant=(int) (mcant/mul);mcant+=1;	
						val=val*mcant;
					} else {	
						val=0;
					}
					
					//if (val>0) items.add(val);
					if (val>0) {
						if (canttipo.equalsIgnoreCase("1")) montos.add(val); else items.add(val);
					}					
				}
				
				DT.moveToNext();
			}	
			
		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }
		
		
	}
	
	
	// Aux
	
 	private boolean validaPermisos(){
		Cursor DT;
		
		try {
			vSQL="SELECT DESCUENTO,LINEA,SUBLINEA,MARCA FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			
			if (DT.getString(0).equalsIgnoreCase("N")) return false;
			
			lineaid=DT.getString(1);
			slineaid=DT.getString(2);
			marcaid=DT.getString(3);
			
		} catch (Exception e) {
		   	return false;
	    }
		
		try {
			vSQL="SELECT ACUMDESC,DESCMAX FROM P_EMPRESA";
           	DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			
			if (DT.getString(0).equalsIgnoreCase("N")) acum=false;
			dmax=DT.getDouble(1);
			
		} catch (Exception e) {
			dmax=0;acum=true;
	    }

		return true;
	}
 	
 	private void opendb() {
		try {
			db = Con.getWritableDatabase();
		 	Con.vDatabase =db;
			active=1;	
	    } catch (Exception e) {
	    	active= 0;
	    }
	}		
	
}
