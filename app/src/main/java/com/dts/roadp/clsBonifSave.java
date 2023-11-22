package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class clsBonifSave extends FacturaRes{

	public String ruta,cliente,emp;
	public long fecha;
	private int active;
	private MiscUtils mu;
	private clsClasses clsCls = new clsClasses();
	
	private Context cont;
	
	private String corel,venped;
	
	public clsBonifSave(Context context,String corelativo,String ventaped) {
		
		cont=context;
		corel=corelativo;
		venped=ventaped;
		active=0;
	    mu=new MiscUtils(context);	
	}

	public void save() {

		if (loadItems()) {

			//saveDBonif();
			saveDRelProdBon();
			if (venped.equalsIgnoreCase("V")) saveDBonifLotes();
	
		}

		//saveDBonifFalt();
		
		try {
			Con.close();
		} catch (Exception e) {
		}
		
	}
	
	
	// Private
	
	private boolean loadItems(){
		Cursor dt;
		
		try {
			sql="SELECT ITEM FROM T_BONITEM";
			dt=Con.OpenDT(sql);

			return dt.getCount()>0;
		} catch (Exception e) {
			return false;
		}				
	}
	
	private void saveDBonif(){
		Cursor dt;
		double cant,prec,cost,tot;
		int ii;
		
		try {
			
			sql="SELECT BONIID, SUM(CANT), AVG(PRECIO), AVG(COSTO) " +
				"FROM T_BONITEM	GROUP BY BONIID";
			dt=Con.OpenDT(sql);
			
			dt.moveToFirst();ii=1;
			while (!dt.isAfterLast()) {

				try {
					
					cant=dt.getDouble(1);
					prec=dt.getDouble(2);
					cost=dt.getDouble(3);
					tot=cant*prec;
					
					ins.init("D_BONIF");
					
					ins.add("COREL",corel);
					ins.add("ITEM",ii);
					ins.add("FECHA",fecha);
					ins.add("ANULADO","N");
					ins.add("EMPRESA",emp);
					ins.add("RUTA",ruta);
					ins.add("CLIENTE",cliente);
					ins.add("PRODUCTO",dt.getString(0));
					ins.add("CANT",cant);
					ins.add("VENPED",venped);
					ins.add("TIPO","");
					ins.add("PRECIO",prec);
					ins.add("COSTO",cost);
					ins.add("TOTAL",tot);
					ins.add("STATCOM","N");
					ins.add("UMVENTA","");
					ins.add("UMSTOCK","");
					ins.add("UMPESO","");
					ins.add("FACTOR",1);

					db.execSQL(ins.sql());

				} catch (Exception e) {
				}				

				dt.moveToNext();ii++;
			}

			if(dt!=null) dt.close();

		} catch (Exception e) {
		}	
	}
	
	private void saveDRelProdBon() {
		Cursor dt;
		int ii;
			
		try {
			sql="SELECT PRODID,BONIID,SUM(CANT),AVG(PRECIO) " +
				"FROM T_BONITEM GROUP BY PRODID,BONIID HAVING PRODID<>'*'";
			dt=Con.OpenDT(sql);
			
			dt.moveToFirst();ii=1;
			while (!dt.isAfterLast()) {
				
				try {
					
					ins.init("D_REL_PROD_BON");
					
					ins.add("COREL",corel);
					ins.add("PRODUCTO",dt.getString(0));
					ins.add("BONIFICADO",dt.getString(1));
					ins.add("CANTIDAD",dt.getDouble(2));
					ins.add("CONSECUTIVO",0);
					ins.add("PRECIO",dt.getDouble(3));
				
					db.execSQL(ins.sql());

				} catch (Exception e) {
				}						
				
				dt.moveToNext();ii++;
			}

			if(dt!=null) dt.close();

		} catch (Exception e) {
		}			
	}
	
	private void saveDBonifLotes() {
		Cursor dt;
		
		try {
			sql="SELECT BONIID, SUM(CANT) FROM T_BONITEM  GROUP BY BONIID";
			dt=Con.OpenDT(sql);
			
			dt.moveToFirst();
			while (!dt.isAfterLast()) {
				
				//rebajaStock(dt.getString(0),dt.getDouble(1));
				
				dt.moveToNext();
			}

			if(dt!=null) dt.close();

		} catch (Exception e) {
		}			
	}
	
	private void saveDBonifFalt() {
		Cursor dt;
			
		try {
			sql="SELECT PRODID,PRODUCTO,CANT FROM T_BONIFFALT";
			dt=Con.OpenDT(sql);
			
			if (dt.getCount()==0) return;

			dt.moveToFirst();
			while (!dt.isAfterLast()) {
				
				try {
					ins.init("D_BONIFFALT");
					
					ins.add("COREL",corel);
					ins.add("FECHA",fecha);
					ins.add("ANULADO","N");
					ins.add("RUTA",ruta);
					ins.add("CLIENTE",cliente);
					ins.add("PRODUCTO",dt.getString(1));
					ins.add("CANT",dt.getDouble(2));
					
					db.execSQL(ins.sql());
					
				} catch (Exception e) {
				}
				
				dt.moveToNext();
			}

			if(dt!=null) dt.close();

		} catch (Exception e) {
		}			
	}
	
	
	// Aux
	
	private void rrebajaStock(String prid,double cant) {
		Cursor DT;
		double acant,val,disp,cantapl;
		String lote,doc,stat;
		
		acant=cant;
			
		sql="SELECT CANT,LOTE,DOCUMENTO,STATUS FROM P_STOCK WHERE CODIGO='"+prid+"'";
		DT=Con.OpenDT(sql);

		DT.moveToFirst();
		while (!DT.isAfterLast()) {
				
			val=DT.getDouble(0);
			lote=DT.getString(1);
			doc=DT.getString(2);
			stat=DT.getString(3);
			
			if (val>acant) {
				cantapl=acant;
				disp=val-acant;
			} else {
				cantapl=val;
				disp=0;
			}
			acant=acant-val;
			
			// Stock
			
			//sql="UPDATE P_STOCK SET CANT="+disp+" WHERE CODIGO='"+prid+"' AND LOTE='"+lote+"' AND DOCUMENTO='"+doc+"' AND STATUS='"+stat+"'";
			//db.execSQL(sql);
			
			// Factura lotes
			/*
			try {
				ins.init("D_BONIF_LOTES");
				
				ins.add("COREL",corel);
				ins.add("PRODUCTO",prid );
				ins.add("LOTE",lote );
				ins.add("CANT",cantapl);
				ins.add("PESO",0);
				ins.add("UMVENTA","");
				ins.add("UMSTOCK","");
				ins.add("UMPESO","");
				ins.add("FACTOR",1);
				
				db.execSQL(ins.sql());
				
				//Toast.makeText(this,ins.SQL(),Toast.LENGTH_LONG).show();
				
			} catch (SQLException e) {
				mu.msgbox(e.getMessage()+"\n"+ins.sql());
			}
			*/
			
			if (acant<=0) return;
				
		    DT.moveToNext();
		}

		if(DT!=null) DT.close();

	}
}
