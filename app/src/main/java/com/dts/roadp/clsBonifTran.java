package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.roadp.clsClasses.clsBonifItem;

import java.util.ArrayList;

public class clsBonifTran {

	public  ArrayList<clsBonifItem> items = new ArrayList<clsBonifItem>();

	private int active;
	private SQLiteDatabase db;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private BaseDatos Con;
	private String sql;

	private MiscUtils MU;
	private clsClasses clsCls = new clsClasses();

	private Context cont;

	private String prodid,lineaid,slineaid,marcaid;
	private double cant,monto,vmax;

	public clsBonifTran(Context context, String producto, double cantidad, double montoventa, BaseDatos dbconnection, SQLiteDatabase database) {
		
		cont=context;
		
		prodid=producto;cant=cantidad;monto=montoventa;

		Con=dbconnection;
		db=database;
		ins=Con.Ins;upd=Con.Upd;

	    MU=new MiscUtils(context);
		
		vmax=0;
		lineaid="";slineaid="";marcaid="";
		
	}
	
	
	// Bonif local
	
	public boolean tieneBonif(){
		double dval=0;
		
		items.clear();
		
		if (!validaPermisos()) return false;
		
		listaDescRangoCant();
		listaDescRangoMonto();
		listaDescMultCant();
		listaDescMultMonto();
		
		return items.size()>0;
		
	}
	
	private void listaDescRangoCant() {
		clsBonifItem item;
		Cursor DT;
		String iid;
		double val;
		
		try {
			sql ="SELECT PRODUCTO,PTIPO,VALOR,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,PORCANT "+
				 "FROM T_BONIF WHERE  ("+cant+">=RANGOINI) AND ("+cant+"<=RANGOFIN) "+
				 "AND (PTIPO<4) AND (TIPOCANT='U') AND (TIPOBON='R') AND (GLOBBON='N') AND (PORCANT='S')";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
				iid=DT.getString(0);
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
					
					item=clsCls.new clsBonifItem();
					
					item.prodid=prodid;
					item.lista=DT.getString(5);
					item.cantexact=DT.getString(6);
					item.globbon="N";
					item.porcant=DT.getString(7);
					
					item.tipolista=DT.getInt(3);
					item.tipocant=DT.getString(4);
					
					item.valor=val;
					item.mul=1;
					
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
			sql ="SELECT PRODUCTO,PTIPO,VALOR,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,PORCANT "+
				 "FROM T_BONIF WHERE  ("+monto+">=RANGOINI) AND ("+monto+"<=RANGOFIN) "+
				 "AND (PTIPO<4) AND (TIPOCANT='V') AND (TIPOBON='R') AND (GLOBBON='N') AND (PORCANT='N')";
			DT=Con.OpenDT(sql);
		
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
				iid=DT.getString(0);
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
					
					item=clsCls.new clsBonifItem();
					
					item.prodid=prodid;
					item.lista=DT.getString(5);
					item.cantexact=DT.getString(6);
					item.globbon="N";
					item.porcant=DT.getString(7);
					
					item.tipolista=DT.getInt(3);
					item.tipocant=DT.getString(4);
					
					item.valor=val;
					item.mul=1;
					
					items.add(item);
				}
				
				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }	
		
	}
	
	private void listaDescMultCant() {
		clsBonifItem item;
		Cursor DT;
		String iid;
		double val,mcant,mul;
		
		try {
			sql ="SELECT PRODUCTO,PTIPO,RANGOINI,RANGOFIN,VALOR,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,PORCANT "+
				 "FROM T_BONIF WHERE ("+cant+">=RANGOINI) "+
				 "AND (PTIPO<4) AND (TIPOCANT='U') AND (TIPOBON='M') AND (GLOBBON='N') AND (PORCANT='S')";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()==0) return;
						
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
					
				iid=DT.getString(0);val=0;
				
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
					mcant=cant-DT.getDouble(2)+1;
					mul=DT.getDouble(3);

					if (mul>0) {
						mcant=(int) (mcant/mul);//mcant+=1;
						val=val*mcant;
					} else {	
						val=0;
					}

					if (val > 0) {

						item = clsCls.new clsBonifItem();

						item.prodid = prodid;
						item.lista = DT.getString(7);
						item.cantexact = DT.getString(8);
						item.globbon = "N";
						item.porcant = DT.getString(9);

						item.tipolista = DT.getInt(5);
						item.tipocant = DT.getString(6);

						item.valor = val;
						item.mul = mcant;

						items.add(item);

					}
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
			sql ="SELECT PRODUCTO,PTIPO,RANGOINI,RANGOFIN,VALOR,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,PORCANT "+
				 "FROM T_BONIF WHERE ("+monto+">=RANGOINI) "+
				 "AND (PTIPO<4) AND (TIPOCANT='V') AND (TIPOBON='M') AND (GLOBBON='N') AND (PORCANT='N')";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()==0) return;
						
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
					
				iid=DT.getString(0);
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
					mcant=cant-DT.getDouble(2)+1;
					mul=DT.getDouble(3);

					if (mul>0) {
						mcant=(int) (mcant/mul);//mcant+=1;
						val=val*mcant;
					} else {	
						val=0;
					}

					if (val > 0) {
						item = clsCls.new clsBonifItem();

						item.prodid = prodid;
						item.lista = DT.getString(7);
						item.cantexact = DT.getString(8);
						item.globbon = "N";
						item.porcant = DT.getString(9);

						item.tipolista = DT.getInt(5);
						item.tipocant = DT.getString(6);

						item.valor = val;
						item.mul = mcant;

						items.add(item);
					}
				}

				DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	MU.msgbox(e.getMessage());
	    }	
		
	}	
	
	
	// Aux
	
 	private boolean validaPermisos(){
		Cursor DT;
		
		try {
			sql ="SELECT BONIFICACION,LINEA,SUBLINEA,MARCA FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			if (DT.getString(0).equalsIgnoreCase("N")) return false;
			
			lineaid=DT.getString(1);
			slineaid=DT.getString(2);
			marcaid=DT.getString(3);

			if(DT!=null) DT.close();

			return true;
			
		} catch (Exception e) {
		   	return false;
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
