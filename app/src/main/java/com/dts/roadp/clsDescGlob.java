package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;

public class clsDescGlob {
		
	public double dmax;
	public boolean acum;
	
	private int active;
	private SQLiteDatabase db;
	private BaseDatos Con;
	private String vSQL;
	
	private MiscUtils MU;

	private ArrayList<Double> items = new ArrayList<Double>();
	
	private Context cont;
	
	private double vtotm,vtotu;
	
	
	public clsDescGlob(Context context) {
		
		cont=context;
		
		active=0;
		Con = new BaseDatos(context);
	    opendb();
	    
	    MU=new MiscUtils(context);
		
		descParam();
		
	}
	
	// Tipo=1 Sublinea
	// Tipo=2 Linea
	// Tipo=3 Marca
	// Tipo=4 Por grupo
	// Tipo=5 Volumen compra
	
	public double getDescGlob(){
		double dval=0;
		
		items.clear();
		
		getTotVenta();
		
		listaDescGlob();
		
		dval=descFinal();
		
		return dval;	
		
	}
	
	private void listaDescGlob(){
		Cursor DT;
		String iid,dprod;
		double dval,val,rini,rfin,rval;
		int ptipo;
		
		try {
			vSQL="SELECT PORCANT,RANGOINI,RANGOFIN,VALOR,PTIPO,PRODUCTO "+
				 "FROM T_DESC WHERE (GLOBDESC='S') AND (PTIPO>0)";
			DT=Con.OpenDT(vSQL);
		
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
				iid=DT.getString(0);dprod=DT.getString(5);
				rini=DT.getDouble(1);rfin=DT.getDouble(2);
				dval=DT.getDouble(3);ptipo=DT.getInt(4);
				
				val=0;rval=0;
				
				switch (ptipo) {
					case 1:  
						rval=getValueTipo(1,iid,dprod);break;
					case 2:  
						rval=getValueTipo(2,iid,dprod);break;	
					case 3:  
						rval=getValueTipo(3,iid,dprod);break;	
					case 4:  
						rval=getValueGrupo(iid,dprod);break;	
					case 5:  
						rval=getValueTotal(iid);break;	
				}
				
				if ((rval>=rini) && (rval<=rfin)) val=dval;		
				//Toast.makeText(cont," % "+val, Toast.LENGTH_LONG).show();	
				if (val>0) items.add(val);
				
				DT.moveToNext();
			}	
			
		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }
		
	}
	
	private double getValueTipo(int ptipo,String porcant,String dprod) {
		
		
		
		if (porcant.equalsIgnoreCase("N")) { // Por monto
			
		} else {	// Por cantidad
			
		}
		
		return 0;
	}
	
	private double getValueGrupo(String porcant,String dprod) {
		
		
		
		if (porcant.equalsIgnoreCase("N")) { // Por monto
			
		} else {	// Por cantidad
			
		}
		
		return 0;
	}
	
	private double getValueTotal(String porcant) {
		if (porcant.equalsIgnoreCase("N")) { // Por monto
			return vtotm;
		} else {	// Por cantidad
			return vtotu;
		}
	}
	
	private void getTotVenta(){
		Cursor DT;
		
		try {
			vSQL="SELECT SUM(TOTAL),SUM(CANT) FROM T_VENTA";
           	DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			
			vtotm=DT.getDouble(0);
			vtotu=DT.getDouble(1);
		} catch (Exception e) {
			vtotm=0;vtotu=0;
			MU.msgbox(e.getMessage());
	    }
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
		
		return df;
	}
	
	// Aux
	
 	private void descParam(){
		Cursor DT;
		
		try {
			vSQL="SELECT ACUMDESC,DESCMAX FROM P_EMPRESA";
           	DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			
			acum=true;
			if (DT.getString(0).equalsIgnoreCase("N")) acum=false;
			dmax=DT.getDouble(1);
			
		} catch (Exception e) {
			dmax=0;acum=true;
	    }
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
