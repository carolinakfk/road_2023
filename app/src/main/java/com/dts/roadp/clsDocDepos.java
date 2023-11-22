package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import java.util.ArrayList;

public class clsDocDepos extends clsDocument {
	
	private ArrayList<itemData> items= new ArrayList<itemData>();
	private ArrayList<itemData> itemsD= new ArrayList<itemData>();
	private ArrayList<itemData> itemsC= new ArrayList<itemData>();

	private double tot,tote,totc,desgloseTotal=0;
	private int numc;
	private String banco,cuenta,ref,ss,st="";
	protected appGlobals gl;
	protected MiscUtils mu;
	protected DateUtils du;

	public clsDocDepos(Context context, int printwidth,String pruta,String pvend,String cursymbol,int decimpres, String archivo) {
		super(context, printwidth,cursymbol,decimpres, archivo);
		docfactura=false;

		gl=((appGlobals) context.getApplicationContext());
		du=new DateUtils();

		nombre="DEPOSITO";
		numero="";
		serie="";
		cliente="";	
		ruta=pruta;
		vendedor=pvend;
		vendcod=gl.vend;
		qrCode="";

	}

	protected boolean loadHeadData(String corel) {
		Cursor DT;
		String val;
				
		//super.loadHeadData(corel);
		
		//nombre="DEPOSITO";
			
		try {
			sql="SELECT BANCO,CUENTA,REFERENCIA,TOTAL,TOTEFEC,TOTCHEQ,NUMCHEQ,FECHA FROM D_DEPOS WHERE COREL='"+corel+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			val=DT.getString(0);
			cuenta=DT.getString(1);	
			ref=DT.getString(2);
			
			tot=DT.getDouble(3);
			tote=DT.getDouble(4);
			totc=DT.getDouble(5);
			numc=DT.getInt(6);
			serie=corel;

			fsfecha=du.sfecha(DT.getLong(7));

		} catch (Exception e) {
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
	    }	
		
		try {
			sql="SELECT NOMBRE FROM P_BANCO  WHERE CODIGO='"+val+"'";
			DT=Con.OpenDT(sql);	
			DT.moveToFirst();
			
			banco=DT.getString(0);
		} catch (Exception e) {
			banco=val;
	    }	
			
		
		return true;
		
	}	
	
	protected boolean loadDocData(String corel) {
		Cursor DT;
		Cursor D;
		Cursor T;
		Cursor DTS;
		itemData item;
		double val;
		double cantDenom;
		
		items.clear();
		itemsD.clear();
	
		
		try {
			
			sql="SELECT D_FACTURA.SERIE,D_FACTURA.CORELATIVO,D_DEPOSD.MONTO,P_MEDIAPAGO.NOMBRE,D_DEPOSD.NUMERO,D_DEPOSD.CHEQUE " +
				"FROM D_DEPOSD INNER JOIN D_FACTURA ON D_DEPOSD.DOCCOREL = D_FACTURA.COREL INNER JOIN P_MEDIAPAGO ON D_DEPOSD.CODPAGO=P_MEDIAPAGO.CODIGO " +
				"WHERE (D_DEPOSD.TIPODOC='F') AND (D_DEPOSD.COREL='"+corel+"') ORDER BY D_FACTURA.SERIE,D_FACTURA.CORELATIVO";

			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			while (!DT.isAfterLast()) {
		
				item =new itemData();
		  	
				item.nombre=DT.getString(0)+" - "+DT.getInt(1);
				item.monto=DT.getDouble(2);
				item.tipo=DT.getString(3);
				
				ss=DT.getString(5);
				item.num=" ";
				if (ss.equalsIgnoreCase("S")) {item.num=""+DT.getString(4); st="S";}
			
				items.add(item);	
				DT.moveToNext();					
			}				
			
		} catch (Exception e) {
	    }

		try {
			sql="SELECT D.DENOMINACION, D.CANTIDAD "+
				"FROM D_DEPOSB D WHERE COREL = '"+ corel +"'";
			DTS=Con.OpenDT(sql);
			DTS.moveToFirst();

			while (!DTS.isAfterLast()) {

				item = new itemData();

				item.denom=DTS.getDouble(0);

				item.cant=DTS.getInt(1);

				cantDenom = Double.parseDouble( item.denom.toString());

				item.total = cantDenom * item.cant;

				desgloseTotal += item.total;

				itemsD.add(item);

				DTS.moveToNext();
			}

			try {
				sql="SELECT VALOR,DESC1,DESC2 FROM D_COBROP WHERE TIPO='C'";
				D=Con.OpenDT(sql);
				D.moveToFirst();

				while (!D.isAfterLast()){

					item = new itemData();

					item.bancoCorr=D.getString(2);
					item.cuenta=D.getString(1);

					item.totc=D.getDouble(0);

					try {
						sql="SELECT NOMBRE FROM P_BANCO  WHERE CODIGO='"+item.bancoCorr+"'";
						T=Con.OpenDT(sql);
						T.moveToFirst();

						while (!T.isAfterLast()){

							item.banco=T.getString(0);

							T.moveToNext();
						}
					} catch (Exception e) {
						item.banco=item.bancoCorr;
					}


					D.moveToNext();

					itemsC.add(item);
				}

				sql="SELECT VALOR,DESC1,DESC2 FROM D_FACTURAP WHERE TIPO='C'";
				D=Con.OpenDT(sql);
				D.moveToFirst();

				while (!D.isAfterLast()){

					item = new itemData();

					item.bancoCorr=D.getString(2);
					item.cuenta=D.getString(1);

					item.totc=D.getDouble(0);

					try {
						sql="SELECT NOMBRE FROM P_BANCO  WHERE CODIGO='"+item.bancoCorr+"'";
						T=Con.OpenDT(sql);
						T.moveToFirst();

						while (!T.isAfterLast()){

							item.banco=T.getString(0);

							T.moveToNext();
						}
					} catch (Exception e) {
						item.banco=item.bancoCorr;
					}


					D.moveToNext();

					itemsC.add(item);
				}


			} catch (Exception e) {
				Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
			}

		} catch (Exception e) {
			Toast.makeText(cont,"loadHeadData"+e.getMessage(), Toast.LENGTH_SHORT).show();return false;
		}
		
		return true;
	}	
		
	protected boolean buildDetail() {
		
		rep.empty();
		rep.add("Banco : "+banco);

		/*rep.add("Cuenta : "+cuenta);
		rep.add("Boleto : "+ref);*/

		if(modofact.equalsIgnoreCase("*"))detail();
		if(modofact.equalsIgnoreCase("TOL"))detailToledano();

		rep.addtot("Total efectivo :    ", rep.rtrim(rep.frmdec(tote),12));
		rep.addtot("Total cheques :    ", rep.rtrim(rep.frmdec(totc),12));
		rep.line();
		rep.addtot("Total  :", rep.rtrim(rep.frmdec(tot),12));
		rep.line();
		rep.empty();
		rep.add("Observaciones:");
		rep.add("");
		rep.line();
		rep.line();
		rep.line();
		rep.empty();
		rep.add("");
		rep.line();
		rep.add("Firma Vendedor");
		rep.empty();
		rep.add("");
		rep.line();
		rep.add("Firma Cajero");
		return true;
	}


	protected boolean detailToledano(){

		itemData itemD;
		itemData itemC;

		rep.add("");
		rep.add("DESGLOSE DE EFECTIVO");
		rep.line();

		for (int i = 0; i <itemsD.size(); i++) {
			itemD=itemsD.get(i);
		}

		rep.add("");

		detailDesgloseEfec();

		if (st.equalsIgnoreCase("S")){
			rep.add("");
			rep.add("DESGLOSE DE CHEQUES");
			rep.line();

			for (int i = 0; i <itemsC.size(); i++) {
				itemC=itemsC.get(i);
			}

			rep.add("");

			detailDesgloseCheque();

			rep.addtotD("Cheques :",numc);
			rep.add("");
		}

		return true;
	}

	protected boolean detail(){

		itemData item;

		rep.line();
		for (int i = 0; i <items.size(); i++) {
			item=items.get(i);

			rep.add(item.nombre);
			rep.add3lrr(item.tipo,item.num,item.monto);
		}
		rep.line();
		rep.add("");

		return true;
	}

	protected boolean detailDesgloseEfec() {
		itemData item;
		String ss;

		rep.add("CANTIDAD   DENOMINACION    TOTAL");
		rep.line();

		for (int i = 0; i <itemsD.size(); i++) {
			item=itemsD.get(i);

			ss=rep.ltrim(frmdecimal(item.cant,0), prw-20);
			ss=ss+rep.rtrim(item.denom+" ",5)+" "+rep.rtrim(frmdecimal(item.total,2),10);
			rep.add(ss);

		}

		rep.line();

		return true;
	}

	protected boolean detailDesgloseCheque() {
		itemData item;
		String ss;

		rep.add("BANCO                           ");
		rep.add("   CHEQUE      FECHA       VALOR");
		rep.line();

		for (int i = 0; i <itemsC.size(); i++) {
			item=itemsC.get(i);

			ss=rep.ltrim(item.banco, prw-20);
			rep.add(ss);
			ss=rep.rtrim(item.cuenta,10)+" "+rep.rtrim(fsfecha,10);
			ss=rep.ltrim(ss,prw-12);
			ss=ss+" "+rep.rtrim(frmdecimal(item.totc,2),7);
			rep.add(ss);

		}

		rep.line();

		return true;
	}
	
	// Aux
	
	private class itemData {
		public String cod,nombre,tipo,num,serie,banco,cuenta;
		public double monto,totc;


		public Double denom,total;
		public String corr,type,moneda,bancoCorr;
		public int cant,numc;
	}
		
	
	
}
