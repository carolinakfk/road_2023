package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class clsDocCobro extends clsDocument {

	private ArrayList<itemData> items= new ArrayList<itemData>();
	private ArrayList<itemDataPago> itemspago= new ArrayList<itemDataPago>();
	
	private double tot,desc,imp,stot,percep;
	private boolean sinimp;
	private String 	contrib,recfact;
	private boolean cobroSR=false;
	protected MiscUtils mu;
	
	public clsDocCobro(Context context,int printwidth,String cursymbol,int decimpres, String deviceId, String archivo) {
		super(context, printwidth,cursymbol,decimpres, archivo);
		docfactura=false;
		docrecibo=true;
		docpedido=false;
		docdevolucion=false;
		mu=new MiscUtils(context,cursymbol);
		deviceid=deviceId;
	}

	private boolean esCobroSR(String corel){

		boolean result=false;
		Cursor DT;

		try{

			sql="SELECT COREL FROM D_COBRO WHERE COREL IN (SELECT COREL FROM D_COBROD_SR) AND COREL='" + corel +"'";
			DT=Con.OpenDT(sql);

			result=(DT.getCount()>0?true:false);

		} catch (Exception ex){
		}

		return result;
	}
	protected boolean buildDetail() {

		itemData item;
		itemDataPago itempago;

		double vTempEfectivo=0;
		double vTempCheque=0;

		rep.empty();

		if (!cobroSR) {
			rep.line();
			rep.addp("DOCUMENTO","PAGO");
			rep.line();
		}

		for (int i = 0; i <items.size(); i++) {
			item=items.get(i);
			rep.addp("No. "+ item.cod,mu.frmcur(item.tot));
		}

		if (!cobroSR) {
			rep.line();
			rep.addtot("TOTAL PAGO", tot);
		}else{
			rep.addtot("TOTAL DE COBROS", tot);
		}

		rep.empty();

		rep.add("--------- DETALLE DE PAGO ------------");
		rep.add("CHEQUES:");

		for (int i = 0; i <itemspago.size(); i++) {
			itempago=itemspago.get(i);

			if(itempago.tipo.equals("E")){
				vTempEfectivo+=itempago.valor;
			}else{
				vTempCheque+=itempago.valor;
				rep.addg("No. "+ itempago.desc1,itempago.nombreBanco,mu.frmcur(itempago.valor));
			}

		}

		rep.empty();
		rep.add("EFECTIVO          :" + StringUtils.leftPad(mu.frmcur(vTempEfectivo), 13));
		rep.add("CHEQUE            :" + StringUtils.leftPad(mu.frmcur(vTempCheque), 13));

		rep.empty();
		
		return true;
	}
	
	protected boolean buildFooter() {

		rep.empty();
		rep.empty();
		rep.line();
		rep.addc("FIRMA CLIENTE");

		rep.empty();
		rep.add( "Serial PDA      : " + deviceid);
		rep.empty();
		rep.empty();
		rep.empty();


		return super.buildFooter();
	}	
		
	protected boolean loadHeadData(String corel) {
		Cursor DT;
		String cli = "",vend = "",val, anulado;
		int impres, cantimpres;
				
		super.loadHeadData(corel);

		nombre="RECIBO";
		
		try {

			cobroSR=esCobroSR(corel);

			sql="SELECT SERIE,CORELATIVO,RUTA,VENDEDOR,CLIENTE,TOTAL,FECHA, IMPRES, ANULADO FROM D_COBRO WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

				DT.moveToFirst();

				serie=DT.getString(0);
				numero=""+DT.getInt(1);
				ruta=DT.getString(2);

				vend=DT.getString(3);
				cli=DT.getString(4);

				tot=DT.getDouble(5);
				ffecha=DT.getLong(6);
				fsfecha=sfecha(ffecha);

				anulado=DT.getString(8);
				impres=DT.getInt(7);
				cantimpres=0;

				if (anulado.equals("S")?true:false){
					cantimpres = -1;
				}else if (cantimpres == 0 && impres > 0){
					cantimpres = 1;
				}

				if (cantimpres>0){
					nombre = "COPIA DE RECIBO";
				}else if (cantimpres==-1){
					nombre = "RECIBO ANULADO";
				}else if (cantimpres==0){
					nombre = "RECIBO";
				}
			}

		} catch (Exception e) {
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
	    }	
		
		
		try {
			sql="SELECT NOMBRE FROM P_VENDEDOR  WHERE CODIGO='"+vend+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			val=DT.getString(0);
		} catch (Exception e) {
			val=vend;
	    }
		
		vendedor=val;
		vendcod=vend;
		
		try {
			sql="SELECT NOMBRE,PERCEPCION,TIPO_CONTRIBUYENTE,DIRECCION,NIT FROM P_CLIENTE WHERE CODIGO='"+cli+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			val=DT.getString(0);
            percep=DT.getDouble(1);
            
            contrib=""+DT.getString(2);
			if (contrib.equalsIgnoreCase("C")) sinimp=true;
			if (contrib.equalsIgnoreCase("F")) sinimp=false;
			
			clicod=cli;
			clidir=DT.getString(3);
			nit=DT.getString(4);
			
		} catch (Exception e) {
			val=cli;
	    }	
		
		//Toast.makeText(cont,"Percep "+percep+"  Sinimp "+sinimp, Toast.LENGTH_SHORT).show();
		
		cliente=val;
		
		return true;
		
	}
	
	protected boolean loadDocData(String corel) {
		Cursor DT;
		itemData item;
		
		//loadHeadData(corel);

		loadDocDataPago(corel);

		items.clear();
		
		try {
			sql="SELECT DOCUMENTO, PAGO FROM D_COBROD WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()==0) {

				item =new itemData();
				item.cod="SIN REFERENCIA";
				items.add(item);

				return true;
			}

			DT.moveToFirst();
			
			while (!DT.isAfterLast()) {
		
				item =new itemData();		  	
				item.cod=DT.getString(0);
				item.tot=DT.getDouble(1);
				items.add(item);	
				
				DT.moveToNext();					
			}

		} catch (Exception e) {
	    }		
		
		return true;
	}

	protected boolean loadDocDataPago(String corel) {
		Cursor DT;
		itemDataPago item;

		itemspago.clear();

		try {
			sql = "SELECT P.TIPO, P.VALOR, P.DESC1, P.DESC2, P.DESC3 FROM D_COBROP AS P  WHERE P.COREL  ='" + corel + "' ORDER BY P.CODPAGO";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){

				DT.moveToFirst();

				while (!DT.isAfterLast()) {

					item =new itemDataPago();
					item.tipo=DT.getString(0);
					item.valor=DT.getDouble(1);
					item.desc1=DT.getString(2);
					item.desc2=DT.getString(3);
					item.desc3=DT.getString(4);

					if (!item.desc2.isEmpty()){
						item.nombreBanco = getNombreBanco(item.desc2);
					}

					itemspago.add(item);

					DT.moveToNext();
				}
			}

		} catch (Exception e) {
			mu.msgbox("Ocurrió un error" + e.getMessage());
		}

		return true;
	}

	// Aux
	
	public double round2(double val){
		int ival;
		
		val=(double) (100*val);
		double rslt=Math.round(val);
		rslt=Math.floor(rslt);
		
		ival=(int) rslt;
		rslt=(double) ival;
		
		return (double) (rslt/100);
	}
	
	private class itemData {
		public String cod,nombre;
		public double cant,prec,imp,descper,desc,tot;
	}

	private class itemDataPago {
		public String tipo,desc1, desc2, desc3, nombreBanco;
		public double valor;
	}

	private String getNombreBanco(String codigo){

		Cursor DT;
		String vNombre="";

		try {
			sql = "SELECT NOMBRE FROM P_BANCO WHERE CODIGO = '" + codigo + "'";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0){
				DT.moveToFirst();
				vNombre=DT.getString(0);
			}

		} catch (Exception e) {
			mu.msgbox("Ocurrió un error" + e.getMessage());
		}

		return vNombre;
	}

}
