package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsBonifItem;

public class clsBonifGlobTipo {
		
	public  ArrayList<clsBonifItem> items = new ArrayList<clsBonifItem>();
	
	private int active;
	private android.database.sqlite.SQLiteDatabase db;
	private BaseDatos Con;
	private String vSQL;
	
	private MiscUtils MU;
	private clsClasses clsCls = new clsClasses();

	private Context cont;
	
	private String idid;
	private int tipo;
	private double cant,monto,vmax;
	
	public clsBonifGlobTipo(Context context) {
		
		cont=context;
		
		active=0;
		Con = new BaseDatos(context);
	    opendb();
	    
	    MU=new MiscUtils(context);
		
	}
	
	// Main
	
	public int cantBonif(int btipo,String producto,double cantidad,double mon){
		
		idid=producto;
		cant=cantidad;
		monto=mon;
		tipo=btipo;
		
		items.clear();
		
		listaDescRango();
		listaDescRangoMonto();
		listaDescMult();
		listaDescMultMonto();
		
		return items.size();
	}
		
	private void listaDescRango() {
		clsBonifItem item;
		Cursor DT;
		String iid;
		double val;
		
		try {
			vSQL="SELECT PRODUCTO,PTIPO,VALOR,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,PORCANT "+
				 "FROM T_BONIF WHERE  ("+cant+">=RANGOINI) AND ("+cant+"<=RANGOFIN) "+
				 "AND (PTIPO="+tipo+") AND (TIPOBON='R') AND (GLOBBON='S') AND (PORCANT='S')";
			DT=Con.OpenDT(vSQL);

			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
				iid=DT.getString(0);
				val=0;
				
				//Toast.makeText(cont,""+iid, Toast.LENGTH_LONG).show();
				
				switch (DT.getInt(1)) {
					case 1: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;
					case 2: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;
					case 3: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;
					case 4: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;
					case 5: 
						val=DT.getDouble(2);break;		
				}
				
				if (val>0) {
					
					item=clsCls.new clsBonifItem();
					
					item.prodid=idid;
					item.lista=DT.getString(5);
					item.cantexact=DT.getString(6);
					item.globbon="S";
					item.porcant=DT.getString(7);
					
					item.tipolista=DT.getInt(3);
					item.tipocant=DT.getString(4);
					
					item.valor=val;
					
					items.add(item);
				}
				
				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }
		
	}
	
	private void listaDescRangoMonto() {
		clsBonifItem item;
		Cursor DT;
		String iid;
		double val;
		
		try {
			vSQL="SELECT PRODUCTO,PTIPO,VALOR,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,PORCANT "+
				 "FROM T_BONIF WHERE  ("+monto+">=RANGOINI) AND ("+monto+"<=RANGOFIN) "+
				 "AND (PTIPO="+tipo+") AND (TIPOBON='R') AND (GLOBBON='S') AND (PORCANT='N')";
			DT=Con.OpenDT(vSQL);
				
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
				iid=DT.getString(0);
				val=0;
				
				//Toast.makeText(cont,""+iid, Toast.LENGTH_LONG).show();
				
				switch (DT.getInt(1)) {
					case 1: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;
					case 2: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;
					case 3: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;
					case 4: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;
					case 5: 
						val=DT.getDouble(2);break;		
				}
				
				if (val>0) {
					
					item=clsCls.new clsBonifItem();
					
					item.prodid=idid;
					item.lista=DT.getString(5);
					item.cantexact=DT.getString(6);
					item.globbon="S";
					item.porcant=DT.getString(7);
					item.tipolista=DT.getInt(3);
					item.tipocant=DT.getString(4);
					item.valor=val;
					
					items.add(item);
					
				}
				
				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }
			
	}
	
	private void listaDescMult() {
		clsBonifItem item;
		Cursor DT;
		String iid;
		double val,mcant,mul;
		
		try {
			vSQL="SELECT PRODUCTO,PTIPO,RANGOINI,RANGOFIN,VALOR,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,PORCANT "+
				 "FROM T_BONIF WHERE ("+cant+">=RANGOINI) "+
				 "AND (PTIPO="+tipo+") AND (TIPOBON='M') AND (GLOBBON='S') AND (PORCANT='S')";
			DT=Con.OpenDT(vSQL);
			
			if (DT.getCount()==0) return;
						
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
					
				iid=DT.getString(0);
				val=0;
				
				switch (DT.getInt(1)) {
					case 1: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(4);break;
					case 2: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(4);break;
					case 3: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(4);break;
					case 4: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;						
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

					item=clsCls.new clsBonifItem();

					item.prodid=idid;
					item.lista=DT.getString(5);
					item.cantexact=DT.getString(6);
					item.globbon="S";
					item.porcant=DT.getString(9);
					item.tipolista=DT.getInt(3);
					item.tipocant=DT.getString(6);
					item.valor=val;

					items.add(item);
				}

				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }
		
	}
	
	private void listaDescMultMonto() {
		clsBonifItem item;
		Cursor DT;
		String iid;
		double val,mcant,mul;
		
		try {
			vSQL="SELECT PRODUCTO,PTIPO,RANGOINI,RANGOFIN,VALOR,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,PORCANT "+
				 "FROM T_BONIF WHERE ("+monto+">=RANGOINI) "+
				 "AND (PTIPO="+tipo+") AND (TIPOBON='M') AND (GLOBBON='S') AND (PORCANT='N')";
			DT=Con.OpenDT(vSQL);
			
			if (DT.getCount()==0) return;
						
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
					
				iid=DT.getString(0);
				val=0;
				
				switch (DT.getInt(1)) {
					case 1: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(4);break;
					case 2: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(4);break;
					case 3: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(4);break;
					case 4: 
						if (iid.equalsIgnoreCase(idid)) val=DT.getDouble(2);break;						
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

					item=clsCls.new clsBonifItem();

					item.prodid=idid;
					item.lista=DT.getString(5);
					item.cantexact=DT.getString(6);
					item.globbon="S";
					item.porcant=DT.getString(9);
					item.tipolista=DT.getInt(3);
					item.tipocant=DT.getString(6);
					item.valor=val;

					items.add(item);
				}

				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
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
