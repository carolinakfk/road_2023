package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsBonifItem;

public class clsBonifGlob {
	
	public  ArrayList<clsBonifItem> items = new ArrayList<clsBonifItem>();
			
	private int active;
	private android.database.sqlite.SQLiteDatabase db;
	private BaseDatos Con;
	private String vSQL;
	
	private MiscUtils MU;
	private clsClasses clsCls = new clsClasses();
	
	private Context cont;
	
	private clsBonifGlobTipo bontipo;
	
	private ArrayList<String> codes = new ArrayList<String>();
	
	private String lineaid,slineaid,marcaid,grupoid;
	private double total,vcnt,vmonto;
	
	public clsBonifGlob(Context context,double tot) {
		
		cont=context;
		
		total=tot;
		
		active=0;
		Con = new BaseDatos(context);
	    opendb();
	    
	    MU=new MiscUtils(context);
	    
	    bontipo=new clsBonifGlobTipo(context); 
		
		lineaid="";slineaid="";marcaid="";grupoid="";
		
	}
	
	// Main
	
	public boolean tieneBonif(){
		
		items.clear();
		
		listBonTipo(1);	// Sublinea 
		listBonTipo(2);	// Linea 
		listBonTipo(3);	// Marca 
		listBonTipo(4);	// Grupo 
		listBonTipo(5);	// Volumen 
			
		return items.size()>0;
		
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
			//Toast.makeText(cont,ptipo+"    "+ccod+"  // cant "+cnt, Toast.LENGTH_SHORT).show();
			
			if (cnt>0) {
		
				cbon=bontipo.cantBonif(ptipo,ccod,vcnt,vmonto);
				
				//Toast.makeText(cont,"PTipo  "+ptipo+"    "+ccod+"  // cant "+cnt +"  ,  res "+cbon, Toast.LENGTH_SHORT).show();
				
				if (cbon==0) return;

				for (int n = 0; n<cbon; n++) {
					item=bontipo.items.get(n);

					s=item.valor+"  "+item.lista+"   "+item.prodid;
					//Toast.makeText(cont,s, Toast.LENGTH_SHORT).show();

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
			vSQL="SELECT DISTINCT PRODUCTO FROM T_BONIF WHERE (PTIPO="+ptipo+") AND (GLOBBON='S')";
			DT=Con.OpenDT(vSQL);
			
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				iid=DT.getString(0);
				codes.add(iid);
				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }			
		
	}
	
	private double cantVenta(int ptipo, String codigo) {
		Cursor DT;
		double cnt=0,monto=0;
	
		try {
			
			switch (ptipo) {
			case 1: // Sublinea 
				vSQL="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA WHERE PRODUCTO IN (SELECT CODIGO FROM P_PRODUCTO WHERE SUBLINEA='"+codigo+"') ";
				;break;
			case 2:	// Linea   
				vSQL="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA WHERE PRODUCTO IN (SELECT CODIGO FROM P_PRODUCTO WHERE LINEA='"+codigo+"') ";
				;break;	
			case 3:	// Marca  
				vSQL="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA WHERE PRODUCTO IN (SELECT CODIGO FROM P_PRODUCTO WHERE MARCA='"+codigo+"') ";
				;break;	
			case 4:	// Grupo  
				vSQL="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA WHERE PRODUCTO IN (SELECT PRODUCTO FROM P_PRODGRUP WHERE CODIGO='"+codigo+"') ";
				;break;	
			case 5:	// Volumen 
				vSQL="SELECT SUM(CANT),SUM(TOTAL) FROM T_VENTA ";
				;break;	
			}
		
			DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			
			cnt=DT.getDouble(0);
			monto=DT.getDouble(1);

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }	
		
		vcnt=cnt;
		vmonto=monto;
				
		if (ptipo==5) {
			return monto;	
		} else {	
			return cnt;	
		}
		
	}
	
	
	// Aux
	
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
