package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.dts.roadp.promotions.PromotionTrace;

public class clsDescFiltro {
	
	public String estr;
	public int ival;
	
	private int active;
	private SQLiteDatabase db;
	private BaseDatos Con;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private String vSQL;
	
	private DateUtils DU;
	
	private String cliid,rutaid;
	private Context context;
	private long fecha;
	
	public clsDescFiltro(Context context,String ruta,String cliente) {
		this(context, ruta, cliente, new DateUtils().getActDate());
	}

	//#EJC20260721 fix(hh-desc-selection): permite filtrar con fecha del documento.
	public clsDescFiltro(Context context,String ruta,String cliente,long fechaDocumento) {
		this.context=context;
		
		cliid=cliente;rutaid=ruta;
		
		active=0;
		Con = new BaseDatos(context);
	    opendb();
	    ins=Con.Ins;upd=Con.Upd;
	    
	    DU=new DateUtils();fecha=fechaDocumento;
	    
	    processFilter();
	    
	    closedb();
	}	
	
	private void processFilter(){
		ensureTraceColumns();
		
		try {
			vSQL="DELETE FROM T_DESC";
			db.execSQL(vSQL);
		} catch (SQLException e) {
			return;
		}
		
		if (!validaPermisos()) return;
		
		filtrarDescuentos();
		
	}

	private void ensureTraceColumns() {
		//#EJC20260721 refactor(hh-desc-result): migra T_DESC sin exigir recrear la BD.
		try { db.execSQL("ALTER TABLE T_DESC ADD COLUMN CODDESC INTEGER DEFAULT 0 NOT NULL"); } catch (Exception ignored) { }
		try { db.execSQL("ALTER TABLE T_DESC ADD COLUMN CTIPO INTEGER DEFAULT 0 NOT NULL"); } catch (Exception ignored) { }
		try { db.execSQL("ALTER TABLE T_DESC ADD COLUMN CLIENTE TEXT DEFAULT '' NOT NULL"); } catch (Exception ignored) { }
		try { db.execSQL("ALTER TABLE T_DESC ADD COLUMN PRIORIDAD_DESCUENTO INTEGER DEFAULT 0"); } catch (Exception ignored) { }
	}
	
	private void filtrarDescuentos() {
		Cursor DT;
		int i,NivelPrec;
		String CTipoNeg,CTipo,CSubTipo,CCanal,CSubCanal,CSucursal;
		String CTipologia,CSubTipologia,CPriorizacion;
		        
		try {
			vSQL="SELECT TIPONEG,TIPO,SUBTIPO,CANAL,SUBCANAL,SUCURSAL,NIVELPRECIO,"+
					"TIPOLOGIA,SUBTIPOLOGIA,PRIORIZACION FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
           	DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			
			CTipoNeg = DT.getString(0);
			CTipo = DT.getString(1);
			CSubTipo = DT.getString(2);
			CCanal = DT.getString(3);
			CSubCanal = DT.getString(4);
			CSucursal = DT.getString(5);
			NivelPrec = DT.getInt(6);
			CTipologia = DT.getString(7);
			CSubTipologia = DT.getString(8);
			CPriorizacion = DT.getString(9);
			
		} catch (Exception e) {
		   	return ;
	    }
		
		i=0;
		
		try {

			//#AT20260719 Filtrar descuento validando que  el cliente no este excluido
			vSQL="SELECT D.CLIENTE,D.CTIPO,D.PRODUCTO,D.PTIPO,D.TIPORUTA,D.RANGOINI,D.RANGOFIN,D.DESCTIPO,D.VALOR,D.GLOBDESC,D.PORCANT,D.FECHAINI,D.FECHAFIN,D.CODDESC, " +
					"NOMBRE, ES_RECARGO, PORPORCENTAJE, PRIORIDAD, IFNULL(PRIORIDAD_DESCUENTO,0), UMVENTA "+
				 "FROM P_DESCUENTO D WHERE ((CTIPO=0) OR "+
				  "((CTIPO=1) AND (CLIENTE='" + cliid + "')) OR "+
				  "((CTIPO=2) AND (CLIENTE='" + CTipoNeg + "')) OR "+
				  "((CTIPO=3) AND (CLIENTE='" + CTipo + "')) OR "+
				  "((CTIPO=4) AND (CLIENTE='" + CSubTipo + "')) OR "+
				  "((CTIPO=5) AND (CLIENTE='" + CCanal + "')) OR "+
				  "((CTIPO=6) AND (CLIENTE='" + CSubCanal + "')) OR "+
				  "((CTIPO=8) AND (CLIENTE='" + CSucursal + "')) OR "+
				  "((CTIPO=9) AND (CLIENTE='" + NivelPrec + "')) OR "+
				  //#EJC20260728 fix(hh-combo-access-sequences): A912 valida ruta y
				  //asignacion del cliente; el dia queda pendiente de confirmacion funcional.
				  "((CTIPO=11) AND (CLIENTE='" + rutaid + "') AND EXISTS ("+
				  " SELECT 1 FROM P_CLIRUTA CR WHERE CR.CLIENTE='" + cliid + "'"+
				  " AND CR.RUTA=D.CLIENTE)) OR "+
				  //A910: Ramo 3 se representa en ROAD como tipologia.
				  "((CTIPO=12) AND (CLIENTE='" + CTipologia + "')) OR "+
				  //A909: Clasificacion AB + Ramo 3 + Centro.
				  "((CTIPO=13) AND (CLIENTE='" + CPriorizacion + "')"+
				  " AND IFNULL(D.TIPOLOGIA,'')='" + CTipologia + "'"+
				  " AND IFNULL(D.SUCURSAL,'')='" + CSucursal + "') OR "+
				  //A911: Ramo 4 se representa en ROAD como subtipologia.
				  "((CTIPO=14) AND (CLIENTE='" + CSubTipologia + "'))) "+
				  " AND ((FECHAINI<="+fecha+") AND (FECHAFIN>="+fecha+")) " +
				  " AND NOT EXISTS (SELECT 1 FROM P_CLIENTE_PROD_EXCLUIDOS E "+
				  "   WHERE E.CLIENTE='" + cliid + "' "+
				  "     AND E.PRODUCTO=D.PRODUCTO "+
				  "     AND E.ACTIVO=1 "+
			      "     AND (E.FECHAINI<="+fecha+") AND (E.FECHAFIN>="+fecha+")) ";
			
			DT=Con.OpenDT(vSQL);
			if (DT == null) {
				PromotionTrace.write(context,"PROMO_FILTER_ERROR","cliente="+cliid+";ruta="+rutaid+";query=principal");
				return;
			}
				
			if (DT.getCount()>0) {
			
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
				  
					try {
						
						ins.init("T_DESC");
						
						ins.add("ID",i);
						ins.add("PRODUCTO",DT.getString(2));
						ins.add("PTIPO",DT.getInt(3));
						ins.add("RANGOINI",DT.getDouble(5));
						ins.add("RANGOFIN",DT.getDouble(6));
						ins.add("DESCTIPO",DT.getString(7));
						ins.add("VALOR",DT.getDouble(8));
						ins.add("GLOBDESC",DT.getString(9));
						ins.add("PORCANT",DT.getString(10));
						ins.add("NOMBRE",DT.getString(14));
						ins.add("ES_RECARGO",DT.getInt(15));
						ins.add("PORPORCENTAJE",DT.getString(16));
						ins.add("PRIORIDAD",DT.getInt(17));
						ins.add("PRIORIDAD_DESCUENTO",DT.getInt(18));
						ins.add("UMVENTA",DT.getString(19));
						ins.add("CODDESC",DT.getInt(13));
						ins.add("CTIPO",DT.getInt(1));
						ins.add("CLIENTE",DT.getString(0));
						
				    	db.execSQL(ins.sql());
				    	
					} catch (SQLException e) {
					}	
			  
					DT.moveToNext();i+=1;
				}	
			}
			
			ival=i;
			PromotionTrace.write(context,"PROMO_FILTER_LOADED","cliente="+cliid+";ruta="+rutaid+";fecha="+fecha+";rows="+ival);
			
		} catch (Exception e) {
			estr=e.getMessage();
	    }
		
		i=0;
		
		
		try {
			vSQL="SELECT CLIENTE,CTIPO,PRODUCTO,PTIPO,TIPORUTA,RANGOINI,RANGOFIN,DESCTIPO,VALOR,GLOBDESC,PORCANT,FECHAINI,FECHAFIN,CODDESC,NOMBRE,ES_RECARGO,PORPORCENTAJE,PRIORIDAD,IFNULL(PRIORIDAD_DESCUENTO,0),UMVENTA "+
				 "FROM P_DESCUENTO D WHERE (CTIPO=10) "+
				 "AND (CLIENTE IN (SELECT DISTINCT CODIGO FROM P_CLIGRUPO WHERE CLIENTE='"+cliid+"'))  "+
				 " AND ((FECHAINI<="+fecha+") AND (FECHAFIN>="+fecha+")) "+
				 " AND NOT EXISTS (SELECT 1 FROM P_CLIENTE_PROD_EXCLUIDOS E "+
				 " WHERE E.CLIENTE='"+cliid+"' AND E.PRODUCTO=D.PRODUCTO AND E.ACTIVO=1 "+
				 " AND E.FECHAINI<="+fecha+" AND E.FECHAFIN>="+fecha+") ";
			
			DT=Con.OpenDT(vSQL);
			estr=vSQL+"\n"+DT.getCount();
			
			if (DT.getCount()>0) {
			
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
				  
					try {
						
						ins.init("T_DESC");
						
						ins.add("ID",i);
						ins.add("PRODUCTO",DT.getString(2));
						ins.add("PTIPO",DT.getInt(3));
						ins.add("RANGOINI",DT.getDouble(5));
						ins.add("RANGOFIN",DT.getDouble(6));
						ins.add("DESCTIPO",DT.getString(7));
						ins.add("VALOR",DT.getDouble(8));
						ins.add("GLOBDESC",DT.getString(9));
						ins.add("PORCANT",DT.getString(10));
						ins.add("NOMBRE",DT.getString(14));
						ins.add("ES_RECARGO",DT.getInt(15));
						ins.add("PORPORCENTAJE",DT.getString(16));
						ins.add("PRIORIDAD",DT.getInt(17));
						ins.add("PRIORIDAD_DESCUENTO",DT.getInt(18));
						ins.add("UMVENTA",DT.getString(19));
						ins.add("CODDESC",DT.getInt(13));
						ins.add("CTIPO",DT.getInt(1));
						ins.add("CLIENTE",DT.getString(0));
						
				    	db.execSQL(ins.sql());
				    	
					} catch (SQLException e) {
					}	
			  
					DT.moveToNext();i+=1;
				}	
			}
			
			ival=i;
			
		} catch (Exception e) {
			estr=e.getMessage();
	    }		
		
			  	    
	}
	
	
	// Aux
	
	private boolean validaPermisos(){
		Cursor DT;
	
		//try {
		//	vSQL="SELECT DESCUENTO FROM P_RUTA WHERE CODIGO='"+rutaid+"'";
        //   	DT=Con.OpenDT(vSQL);
		//	DT.moveToFirst();
		//	if (DT.getString(0).equalsIgnoreCase("N")) return false;
		//} catch (Exception e) {
		//   	return false;
	    //}
		
		try {
			vSQL="SELECT DESCUENTO FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
           	DT=Con.OpenDT(vSQL);
			DT.moveToFirst();
			if (DT.getString(0).equalsIgnoreCase("N")) return false;
		} catch (Exception e) {
		   	return false;
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

	private void closedb(){
		try {
			Con.close();  
		} catch (Exception e) { }
	}
	
}
