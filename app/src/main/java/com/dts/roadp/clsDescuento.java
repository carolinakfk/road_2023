package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;

public class clsDescuento {
		
	public double monto;
	
	private int active;
	private android.database.sqlite.SQLiteDatabase db;
	private BaseDatos Con;
	private String vSQL;
	
	private MiscUtils MU;

	private ArrayList<Double> items = new ArrayList<Double>();
	private ArrayList<Double> montos = new ArrayList<Double>();
	
	private Context cont;
	
	private String prodid,lineaid,slineaid,marcaid,canttipo;
	private double cant,vmax,dmax;
	private boolean acum;
	
	public clsDescuento(Context context,String producto,double cantidad) {
		
		cont=context;
		
		prodid=producto;cant=cantidad;

		try {
			active=0;
			Con = new BaseDatos(context);
			opendb();
		} catch (Exception e) {
		}
	    
	    MU=new MiscUtils(context);
		
		vmax=0;dmax=0;acum=true;
		lineaid="";slineaid="";marcaid="";
		
	}
	
	// Descuento local
	
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
