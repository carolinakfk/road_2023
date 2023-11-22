package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;

import com.dts.roadp.clsClasses.clsBonifItem;

import java.util.ArrayList;

public class clsDeGlob {

	public ArrayList<clsBonifItem> items = new ArrayList<clsBonifItem>();
	public boolean acum;
	public double valor,vmonto,valacum,valmax,maxlimit;
	
	private int active;
	private android.database.sqlite.SQLiteDatabase db;
	private BaseDatos Con;
	private String sql;
	
	private MiscUtils mu;
	private clsClasses clsCls = new clsClasses();
	
	private Context cont;
	
	private clsDeGlobTipo bontipo;
	
	private ArrayList<String> codes = new ArrayList<String>();
	
	private String lineaid,slineaid,marcaid,grupoid;
	private double total,vcnt;	
	
	
	public clsDeGlob(Context context,double tot) {
		
		cont=context;
		
		total=tot;active=0;
		lineaid="";slineaid="";marcaid="";grupoid="";

		Con = new BaseDatos(context);
	    opendb();
	    
	    mu=new MiscUtils(context);
	    bontipo=new clsDeGlobTipo(context);

		configuracion();
	}
	
	// Main
	
	public boolean tieneDesc(){
		items.clear();
		
		listBonTipo(1);	// Sublinea 
		listBonTipo(2);	// Linea 
		listBonTipo(3);	// Marca 
		listBonTipo(4);	// Grupo 
		listBonTipo(5);	// Volumen 
		
		calcValores();
		
		return items.size()>0;
	}
	
	private void calcValores() {
		double val;

		valor=0;valacum=0;valmax=0;

		if (items.size()==0) return;

		for (int i = 0; i <items.size(); i++) {
			val=items.get(i).valor;
			valacum+=val;
			if (val>valmax) valmax=val;
		}

		valor=valmax;if (acum) valor=valacum;
		
		if (maxlimit>0) {
			if (valor>maxlimit) valor=maxlimit;
		}
		
	}
	
	private void listBonTipo(int ptipo) {
		clsBonifItem item;
		int cbon;
		double cnt;
		String ccod,s;

		listaCodigos(ptipo);

		if (codes.size()==0) return;

		for (int i = 0; i <codes.size(); i++) {

			ccod=codes.get(i);
			cnt=cantVenta(ptipo,ccod);

			if (cnt>0) {
		
				cbon=bontipo.cantBonif(ptipo,ccod,vcnt,vmonto);
				if (cbon==0) return;

				for (int n = 0; n<cbon; n++) {
					item=bontipo.items.get(n);
					s=item.valor+"  "+item.lista+"   "+item.prodid;
					items.add(item);
				}			
			}

		}			

	}
		
	private void listaCodigos(int ptipo) {
		Cursor DT;
		String iid;
		
		codes.clear();
		
		try {
			//vSQL="SELECT PRODUCTO FROM T_BONIF WHERE (PTIPO="+ptipo+") AND (GLOBBON='S') AND (PORCANT='S')";
			sql="SELECT DISTINCT PRODUCTO FROM T_DESC WHERE (PTIPO="+ptipo+") AND (GLOBDESC='S')";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				iid=DT.getString(0);
				codes.add(iid);
				DT.moveToNext();
			}
		} catch (Exception e) {
		   	mu.msgbox(e.getMessage());
	    }
	}
	
	private double cantVenta(int ptipo, String codigo) {
		Cursor DT;
		double cnt=0,monto=0;
	
		try {
			switch (ptipo) {
			case 1: // Sublinea 
				sql="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA WHERE PRODUCTO IN (SELECT CODIGO FROM P_PRODUCTO WHERE SUBLINEA='"+codigo+"') ";
				break;
			case 2:	// Linea   
				sql="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA WHERE PRODUCTO IN (SELECT CODIGO FROM P_PRODUCTO WHERE LINEA='"+codigo+"') ";
				break;	
			case 3:	// Marca  
				sql="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA WHERE PRODUCTO IN (SELECT CODIGO FROM P_PRODUCTO WHERE MARCA='"+codigo+"') ";
				break;	
			case 4:	// Grupo  
				sql="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA WHERE PRODUCTO IN (SELECT PRODUCTO FROM P_PRODGRUP WHERE CODIGO='"+codigo+"') ";
				break;	
			case 5:	// Volumen 
				sql="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA ";
				break;	
			}
		
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			cnt=DT.getDouble(0);
			monto=DT.getDouble(1);
		} catch (Exception e) {
		   	mu.msgbox(e.getMessage());
	    }		
		
		vcnt=cnt;
		vmonto=monto;
		
		if (ptipo==5) return monto;else return cnt;

	}
	
	
	// Aux
	
	private void configuracion() {
		Cursor DT;
		
		try {
			sql="SELECT ACUMDESC,DESCMAX FROM P_EMPRESA";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			if (DT.getString(0).equalsIgnoreCase("N")) acum=false;
			maxlimit=DT.getDouble(1);
		} catch (Exception e) {
			maxlimit=0;acum=true;
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
