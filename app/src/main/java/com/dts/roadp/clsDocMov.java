package com.dts.roadp;

import java.util.ArrayList;


import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class clsDocMov extends clsDocument {
	
	private ArrayList<itemData> items= new ArrayList<itemData>();
	
	private int modo,totitem;
	private double totb,totm;
	
	public clsDocMov(Context context, int printwidth,String pnombre,String pruta,String pvend,String cursymbol,int decimpres, String archivo) {
		super(context, printwidth,cursymbol,decimpres, archivo);
		docfactura=false;
		
		modo=1;
		if (pnombre.equalsIgnoreCase("RECARGA")) modo=0; 
		
		nombre=pnombre;
		numero="";
		serie="";
		ruta=pruta;
		vendedor=pvend;
		cliente="";
		fsfecha="";
	}
	
	protected boolean loadHeadData(String corel) {
		Cursor DT;
		itemData item;
	
		items.clear();
		
		try {
			sql="SELECT D_MOVD.PRODUCTO,P_PRODUCTO.DESCLARGA,D_MOVD.CANT,D_MOVD.CANTM,D_MOVD.PESO,D_MOVD.PESOM " +
				"FROM D_MOVD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO = D_MOVD.PRODUCTO " +
				"WHERE (D_MOVD.COREL='"+corel+"')";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			while (!DT.isAfterLast()) {
				
				item =new itemData();

				item.cod=DT.getString(0);
				item.nombre=DT.getString(1);
				
				item.cant=DT.getDouble(2);
				item.cantm=DT.getDouble(3);
				
				if (item.cant>0) {
					item.valor=decfrm.format(item.cant);
					if (modo==1) item.valor+=" B";
				} else {	
					item.valor=" ";
				}
				
				if (item.cantm>0) {
					item.valorm=decfrm.format(item.cantm)+" M";
				} else {	
					item.valorm=" ";
				}
				
				items.add(item);
				DT.moveToNext();					
			}							
			return true;
		} catch (Exception e) {
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}		
	}
	
	protected boolean buildDetail() {
		itemData item;
		
		if (modo==0) {
			rep.add("RECARGA MANUAL");			
		} else {	
			rep.add("DEVOLUCION A BODEGA");
		}
		
		rep.line();
		//rep.empty();
		
		totitem=items.size();
		totb=0;totm=0;
		
		for (int i = 0; i <items.size(); i++) {
			
			item=items.get(i);
			
			totb+=item.cant;
			totm+=item.cantm;
			
			if (modo==0) {
				rep.addtot(item.cod+" "+item.nombre,item.valor);
			} else {	
				if (item.cantm==0) {
					rep.addtot(item.nombre,item.valor);	
				} else {	
					rep.add(item.nombre);
					rep.add3lrr(item.cod,item.valorm,item.valor);		
				}
			}

		}
		
		rep.line();
		
		return true;	
	} 	
	
	protected boolean buildFooter() {
		String s1,s2,s3;
		
		if (modo==1) {
			
			rep.empty();
			
			s1="Prod : "+totitem;
			s2=rep.frmdec(totm)+" M";
			s3=rep.frmdec(totb)+" B";
			
			rep.add3lrr(s1,s2,s3);	
		}
		
		return super.buildFooter();
	}	
	
	private class itemData {
		public String cod,nombre,valor,valorm;
		public double cant,cantm;
	}

}
