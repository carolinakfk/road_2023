package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class clsBonFiltro {

	public String estr;
	public int ival;

	private Context cont;
	
	private int active;
	private SQLiteDatabase db;
	private BaseDatos Con;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private String vSQL;
	
	private DateUtils DU;
	
	private String cliid,rutaid;
	private long fecha;
	
	public clsBonFiltro(Context context,String ruta,String cliente) {
		
		cont=context;
		
		cliid=cliente;rutaid=ruta;
		
		active=0;
		Con = new BaseDatos(context);
	    opendb();
	    ins=Con.Ins;upd=Con.Upd;
	    
	    DU=new DateUtils();fecha=DU.getActDate();
	    
	    processFilter();
	    
	    closedb();
	}
	
	private void processFilter(){
		
		try {
			vSQL="DELETE FROM T_BONIF";
			db.execSQL(vSQL);
		} catch (SQLException e) {
			return;
		}
		
		if (!validaPermisos()) return;
		
		filtrarBonif();
		
	}
	
	private void filtrarBonif() {
		Cursor DT;
		int i,NivelPrec;
		String  CTipoNeg,CTipo,CSubTipo,CCanal,CSubCanal,CSucursal;
		
		try {

			opendb();
			vSQL="SELECT TIPONEG,TIPO,SUBTIPO,CANAL,SUBCANAL,SUCURSAL,NIVELPRECIO FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
           	DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			
			CTipoNeg = DT.getString(0);
			CTipo = DT.getString(1);
			CSubTipo = DT.getString(2);
			CCanal = DT.getString(3);
			CSubCanal = DT.getString(4);
			CSucursal = DT.getString(5);
			NivelPrec = DT.getInt(6);

			if(DT!=null) DT.close();

		} catch (Exception e) {
		   	return ;
	    }
		
		i=0;
		
		
		try {

			vSQL="SELECT CLIENTE,CTIPO,PRODUCTO,PTIPO,TIPORUTA,RANGOINI,RANGOFIN,TIPOBON,VALOR,GLOBBON,PORCANT,FECHAINI,FECHAFIN,CODDESC,NOMBRE,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,EMP,UMPRODUCTO,UMBONIFICACION "+
					"FROM P_BONIF WHERE (CTIPO=0) OR "+
					"((CTIPO=1) AND (CLIENTE='" + cliid + "')) OR "+
					"((CTIPO=2) AND (CLIENTE='" + CTipoNeg + "')) OR "+
					"((CTIPO=3) AND (CLIENTE='" + CTipo + "')) OR "+
					"((CTIPO=4) AND (CLIENTE='" + CSubTipo + "')) OR "+
					"((CTIPO=5) AND (CLIENTE='" + CCanal + "')) OR "+
					"((CTIPO=6) AND (CLIENTE='" + CSubCanal + "')) OR "+
					"((CTIPO=8) AND (CLIENTE='" + CSucursal + "')) OR "+
					"((CTIPO=9) AND (CLIENTE='" + NivelPrec + "')) "+
					" AND ((FECHAINI<="+fecha+") AND (FECHAFIN>="+fecha+")) ";
			
			DT=Con.OpenDT(vSQL);
				
			if (DT.getCount()>0) {
			
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
				  
					try {
						
						ins.init("T_BONIF");
						
						ins.add("ID",i);
						ins.add("PRODUCTO",DT.getString(2));
						ins.add("PTIPO",DT.getInt(3));
						ins.add("RANGOINI",DT.getDouble(5));
						ins.add("RANGOFIN",DT.getDouble(6));
						ins.add("TIPOBON",DT.getString(7));
						
						ins.add("VALOR",DT.getDouble(8));
						ins.add("GLOBBON",DT.getString(9));
						ins.add("PORCANT",DT.getString(10));
						ins.add("NOMBRE",DT.getString(14));
						
						ins.add("TIPOLISTA",DT.getInt(15));
						ins.add("TIPOCANT",DT.getString(16));
						ins.add("LISTA",DT.getString(17));
						ins.add("CANTEXACT",DT.getString(18));
						ins.add("EMP",DT.getString(19));

						ins.add("UMPRODUCTO",DT.getString(20));
						ins.add("UMBONIFICACION",DT.getString(21));
						
				    	db.execSQL(ins.sql());
				    	
					} catch (SQLException e) {
						Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
					}	
			  
					DT.moveToNext();i+=1;
				}	
			}

			if(DT!=null) DT.close();

			ival=i;
			
		} catch (Exception e) {
			estr=e.getMessage();
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
	    }
		
		
		
		try {
			vSQL="SELECT CLIENTE,CTIPO,PRODUCTO,PTIPO,TIPORUTA,RANGOINI,RANGOFIN,TIPOBON,VALOR,GLOBBON,PORCANT,FECHAINI,FECHAFIN,CODDESC,NOMBRE,TIPOLISTA,TIPOCANT,LISTA,CANTEXACT,EMP,UMPRODUCTO,UMBONIFICACION "+
			     "FROM P_BONIF WHERE (CTIPO=10) "+
				 "AND (CLIENTE IN (SELECT DISTINCT CODIGO FROM P_CLIGRUPO WHERE CLIENTE='"+cliid+"'))  "+
				 " AND ((FECHAINI<="+fecha+") AND (FECHAFIN>="+fecha+")) ";
			
			DT=Con.OpenDT(vSQL);
			
			estr=vSQL+"\n"+DT.getCount();
			
			if (DT.getCount()>0) {
			
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
				  
					try {
						
						ins.init("T_BONIF");
						
						ins.add("ID",i);
						ins.add("PRODUCTO",DT.getString(2));
						ins.add("PTIPO",DT.getInt(3));
						ins.add("RANGOINI",DT.getDouble(5));
						ins.add("RANGOFIN",DT.getDouble(6));
						ins.add("TIPOBON",DT.getString(7));
						
						ins.add("VALOR",DT.getDouble(8));
						ins.add("GLOBBON",DT.getString(9));
						ins.add("PORCANT",DT.getString(10));
						ins.add("NOMBRE",DT.getString(14));
						
						ins.add("TIPOLISTA",DT.getInt(15));
						ins.add("TIPOCANT",DT.getString(16));
						ins.add("LISTA",DT.getString(17));
						ins.add("CANTEXACT",DT.getString(18));
						ins.add("EMP",DT.getString(19));

						ins.add("UMPRODUCTO",DT.getString(20));
						ins.add("UMBONIFICACION",DT.getString(21));
						
				    	db.execSQL(ins.sql());
				    	
					} catch (SQLException e) {
						Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
					}	
			  
					DT.moveToNext();i+=1;
				}	
			}

			if(DT!=null) DT.close();
			ival=i;
			
		} catch (Exception e) {
			estr=e.getMessage();
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
	    }		

		}


		// Aux

		private boolean validaPermisos(){
			Cursor DT;

			try {

				opendb();

			vSQL="SELECT BONIFICACION FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
           	DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			if (DT.getString(0).equalsIgnoreCase("N")) {
				if(DT!=null) DT.close();
				return false;
			}
			if(DT!=null) DT.close();
		} catch (Exception e) {
		   	return false;
	    }		
		return true;
	}
	
	private void opendb() {
		try {
			db = Con.getWritableDatabase();
			if (db!= null) {
				Con.vDatabase=db;
				active=1;
			} else {
				active = 0;
			}
		} catch (Exception e) {
			active= 0;
		}
	}		

	private void closedb(){
		try {
			Con.close();  
		} catch (Exception e) { }
	}	
	

}
