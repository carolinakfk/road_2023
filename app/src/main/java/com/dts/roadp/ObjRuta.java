package com.dts.roadp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class ObjRuta extends PBase {

	private TextView lblMeta,lblAcum,lblFalta,lblPerc;
	
	private String rutatipo;
	private int tipo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_obj_ruta);
		
		super.InitBase();
		addlog("ObjRuta",""+du.getActDateTime(),gl.vend);
		
		lblMeta= (TextView) findViewById(R.id.lblMeta);
		lblAcum= (TextView) findViewById(R.id.lblpSaldo);
		lblFalta= (TextView) findViewById(R.id.lblFalta);
		lblPerc= (TextView) findViewById(R.id.lblPerc);
		
		tipo=((appGlobals) vApp).tipo;
		rutatipo=((appGlobals) vApp).rutatipog;
		
		if (tipo==0) {
			this.setTitle("Objetivo por ruta");
			if (rutatipo.equalsIgnoreCase("P")) {
				cargaPedidos();
			} else {	
				cargaVentas();
			}
		
		} else {
			this.setTitle("Objetivo por Cobro");
			cargaCobros();
		}
		
	}


	// Main
	
	private void cargaVentas(){
		Cursor DT;
		double meta=0,acum=0,metau=0,acumu=0,falta=0,perc=0,vmonto=0,vcant=0;
		
		try {
			
			sql="SELECT METAV,METAU,ACUMV,ACUMU FROM O_RUTA WHERE (RUTA='"+((appGlobals) vApp).ruta+"') ";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()>0) {
				DT.moveToFirst();
				
				meta=DT.getDouble(0);
				metau=DT.getDouble(1);
				acum=DT.getDouble(2);
				acumu=DT.getDouble(3);
			}
			
			sql="SELECT SUM(D_FACTURAD.TOTAL),SUM(D_FACTURAD.CANT) "+
			     "FROM D_FACTURAD INNER JOIN D_FACTURA ON D_FACTURA.COREL=D_FACTURAD.COREL "+
				 "WHERE (D_FACTURA.ANULADO='N')  AND (D_FACTURA.STATCOM='N') AND (D_FACTURA.RUTA='"+((appGlobals) vApp).ruta+"') ";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()>0) {
				DT.moveToFirst();
				vmonto=DT.getDouble(0);
				vcant=DT.getDouble(1);
			}
			
			acum=acum+vmonto;
			acumu=acumu+vcant;
			
			if (meta==0) {
				meta=metau;
				acum=acumu;
			}
			
			if (meta>0) {
				falta=meta-acum;if (falta<0) falta=0;
				perc=100*acum/meta;
			}
				
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
		    meta=0;acum=0;falta=0;perc=0;
	    }
		
		lblMeta.setText(mu.frmcur(meta));
		lblAcum.setText(mu.frmcur(acum));
		lblFalta.setText(mu.frmcur(falta));
		lblPerc.setText(mu.frmdec(perc)+" %");
		
		// Agregar ventas D_PEDIDO
		
	}
	
	private void cargaPedidos(){
		Cursor DT;
		double meta=0,acum=0,metau=0,acumu=0,falta=0,perc=0,vmonto=0,vcant=0;
		
		try {
			
			sql="SELECT METAV,METAU,ACUMV,ACUMU FROM O_RUTA WHERE (RUTA='"+((appGlobals) vApp).ruta+"') ";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()>0) {
				DT.moveToFirst();
				
				meta=DT.getDouble(0);
				metau=DT.getDouble(1);
				acum=DT.getDouble(2);
				acumu=DT.getDouble(3);
			}
			
			sql="SELECT SUM(D_PEDIDOD.TOTAL),SUM(D_PEDIDOD.CANT) "+
			     "FROM D_PEDIDOD INNER JOIN D_PEDIDO ON D_PEDIDO.COREL=D_PEDIDOD.COREL "+
				 "WHERE (D_PEDIDO.ANULADO='N') AND (D_PEDIDO.STATCOM='N') AND (D_PEDIDO.RUTA='"+((appGlobals) vApp).ruta+"') ";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()>0) {
				DT.moveToFirst();
				vmonto=DT.getDouble(0);
				vcant=DT.getDouble(1);
			}
			
			acum=acum+vmonto;
			acumu=acumu+vcant;
			
			if (meta==0) {
				meta=metau;
				acum=acumu;
			}
			
			if (meta>0) {
				falta=meta-acum;if (falta<0) falta=0;
				perc=100*acum/meta;
			}
				
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
		    meta=0;acum=0;falta=0;perc=0;
	    }
		
		lblMeta.setText(mu.frmcur(meta));
		lblAcum.setText(mu.frmcur(acum));
		lblFalta.setText(mu.frmcur(falta));
		lblPerc.setText(mu.frmdec(perc)+" %");
				
	}
	
	private void cargaCobros(){
		Cursor DT;
		double meta=0,acum=0,metau=0,acumu=0,falta=0,perc=0,acumc=0;
		
		try {
			
			sql="SELECT METAV,METAU,ACUMV,ACUMU FROM O_COBRO "+
			     "WHERE (RUTA='"+((appGlobals) vApp).ruta+"') AND (VENDEDOR='"+((appGlobals) vApp).vend+"')";
			
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()>0) {
				DT.moveToFirst();
				
				meta=DT.getDouble(0);
				metau=DT.getDouble(1);
				acum=DT.getDouble(2);
				acumu=DT.getDouble(3);
			}
			
			if (meta==0) {
				meta=metau;
				acum=acumu;
			}
			
			if (meta>0) {
				acumc=totalCobros();
				acum=acum+acumc;
				falta=meta-acum;
				perc=100*acum/meta;
			}
				
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	mu.msgbox( e.getMessage());
		    meta=0;acum=0;falta=0;perc=0;
	    }
		
		lblMeta.setText(mu.frmcur(meta));
		lblAcum.setText(mu.frmcur(acum));
		lblFalta.setText(mu.frmcur(falta));
		lblPerc.setText(mu.frmdec(perc)+" %");	
		
		
		
		// Agregar cobros D_COBRO
		
	}
	
	private double totalCobros() {
		Cursor DT;
		double tot=0;
		
		try {
			sql="SELECT SUM(TOTAL) FROM D_COBRO WHERE (ANULADO='N')";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			tot=DT.getDouble(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox( e.getMessage());
			tot=0;
		}
		
		return tot;
	}
	
	
	// Aux
	
}
