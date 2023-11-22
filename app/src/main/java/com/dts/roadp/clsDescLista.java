package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCDB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class clsDescLista {

	public ArrayList<clsClasses.clsPromoItem> items = new ArrayList<clsClasses.clsPromoItem>();
	
	private int active;
	private SQLiteDatabase db;
	private BaseDatos Con;
	private String vSQL;

	private clsClasses clsCls = new clsClasses();
	
	
	
	private String prodid;
	
	
	public clsDescLista(Context context,String producto) {
		
		prodid=producto;
		
		active=0;
		Con = new BaseDatos(context);
	    opendb();
		
	    crearLista();
	}	
	
	private void crearLista(){
		
		if (prodid.equalsIgnoreCase("*")) {
			listaCliente();
		} else {	
			listaProd();
		}
	}
	
	// Main
	
	private void listaProd() {
		
	}
	
	private void listaCliente(){
		Cursor DT;
		clsClasses.clsPromoItem item;
		String pid,nom,s;
		int ptipo;
		
		items.clear();
		
		try {
			vSQL="SELECT PRODUCTO,PTIPO,RANGOINI,RANGOFIN,DESCTIPO,VALOR,GLOBDESC,PORCANT,NOMBRE "+
				 "FROM T_DESC ORDER BY PRODUCTO,RANGOINI ";
			DT=Con.OpenDT(vSQL);
			
			if (DT.getCount()>0) {
			
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
				  
					item = clsCls.new clsPromoItem();
					
					pid=DT.getString(0);
					ptipo=DT.getInt(1);
					
					if (ptipo==4) {
						nom=DT.getString(8);
					} else {	
						nom=getName(pid,ptipo);
					}
					
					item.Nombre=nom;
					item.RIni=DT.getDouble(2);
					item.RFin=DT.getDouble(3);
					item.Valor=DT.getDouble(5);
					
					s=DT.getString(4);
					if (s.equalsIgnoreCase("R")) item.Porrango=true; else item.Porrango=false;
					
					s=DT.getString(6);
					if (s.equalsIgnoreCase("N")) item.Porprod=true; else item.Porprod=false;
					
					item.Tipo="D";
					item.Bon="";
					
					items.add(item);					
			  
					DT.moveToNext();
				}	
			}
			
		} catch (Exception e) {
	
	    }
		
	}
	
	// Aux
	
	private String getName(String tid,int ptipo) {
		Cursor DT;
		String s="";
		
		if (ptipo==5) return "Volumen de compra";
		
		if (ptipo==0) vSQL="SELECT DESCLARGA FROM P_PRODUCTO WHERE CODIGO='"+tid+"'";
		if (ptipo==1) vSQL="SELECT NOMBRE FROM P_SUBLINEA WHERE CODIGO='"+tid+"'";
		if (ptipo==2) vSQL="SELECT NOMBRE FROM P_LINEA WHERE CODIGO='"+tid+"'";
		if (ptipo==3) vSQL="SELECT NOMBRE FROM P_MARCA WHERE CODIGO='"+tid+"'";
		
		try {
			DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			s=DT.getString(0);
		} catch (Exception e) {
			s="";
	    }
		
		return s;
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
