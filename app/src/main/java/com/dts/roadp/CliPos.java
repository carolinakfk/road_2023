package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class CliPos extends PBase {

	private EditText txtNIT,txtNom,txtRef;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cli_pos);

		super.InitBase();
		addlog("CliPos",""+du.getActDateTime(),gl.vend);

		txtNIT = (EditText) findViewById(R.id.txtBoleta);txtNIT.requestFocus();
		txtNom = (EditText) findViewById(R.id.editText2);
		txtRef = (EditText) findViewById(R.id.editText1);

		setHandlers();	

		txtNIT.setText("");
		txtNom.setText("");
		txtRef.setText("");
	}

	
	//  Events

	public void consFinal(View view) {
		if (agregaCliente("C.F.","Consumidor final","")) procesaCF() ;	
	}

	public void clienteNIT(View view) {
		String snit,sname,sdir;

		try{
			snit=txtNIT.getText().toString();
			sname=txtNom.getText().toString();
			sdir=txtRef.getText().toString();

			if (!validaNIT(snit)) {
				mu.msgbox("NIT Incorrecto");
				toast("NIT incorrecto");return;
			}
			if (mu.emptystr(sname)) {
				toast("Nombre incorrecto");return;
			}

			if (agregaCliente(snit,sname,sdir)) procesaNIT(snit) ;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void setHandlers() {

		try{
			txtNIT.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						switch (arg1) {
							case KeyEvent.KEYCODE_ENTER:
								txtNom.requestFocus();
								buscaCliente();
								return true;
						}
					}
					return false;
				}
			});

			txtNom.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
					if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
						switch (arg1) {
							case KeyEvent.KEYCODE_ENTER:
								clienteNIT(arg0);
								return true;
						}
					}
					return false;
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}


	// Main

	private void procesaCF() {

		try{
			((appGlobals) vApp).rutatipo="V";

			((appGlobals) vApp).closeCliDet=false;
			((appGlobals) vApp).closeVenta=false;

			((appGlobals) vApp).cliente="C.F.";
			//((appGlobals) vApp).cliente="0001000000";
			((appGlobals) vApp).nivel=1;
			((appGlobals) vApp).percepcion=0;
			((appGlobals) vApp).contrib="";

			Intent intent = new Intent(this,Venta.class);
			startActivity(intent);

			limpiaCampos();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void procesaNIT(String snit) {

		try{
			((appGlobals) vApp).rutatipo="V";

			((appGlobals) vApp).closeCliDet=false;
			((appGlobals) vApp).closeVenta=false;

			((appGlobals) vApp).cliente=snit;
			((appGlobals) vApp).nivel=1;
			((appGlobals) vApp).percepcion=0;
			((appGlobals) vApp).contrib="";

			Intent intent = new Intent(this,Venta.class);
			startActivity(intent);

			limpiaCampos();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	
	// Aux

	private boolean validaNIT(String N)  {

		try{
			String P, C, s, NC;
			int[] v = {0,0,0,0,0,0,0,0,0,0};
			int j, mp, sum, d11, m11, r11, cn, ll;

			N=N.trim();
			N=N.replaceAll(" ","");
			if (mu.emptystr(N)) return false;



		/*
		N=N.toUpperCase();
		if (N.equalsIgnoreCase("CF")) N="C.F.";
		if (N.equalsIgnoreCase("C/F")) N="C.F.";
		if (N.equalsIgnoreCase("C.F")) N="C.F.";
		if (N.equalsIgnoreCase("CF.")) N="C.F.";

		if (N.equalsIgnoreCase("C.F.")) return true;

		ll = N.length();
		if (ll<5) return false;

		P = N.substring(0,ll-2);
		C = N.substring(ll-1, ll);

		ll = ll - 1;
		sum = 0;

		try {

			for (int i = 0; i <ll-1; i++) {
				s =P.substring( i, i+1);
				j=Integer.parseInt(s);
				mp = ll + 1 - i-1;
				sum = sum + j * mp;
			}

			d11 =(int) Math.floor(sum/11);
			m11 = d11 * 11;
			r11 = sum - m11;
			cn = 11 - r11;

			if (cn == 10) s = "K"; else s=""+cn;

			if (cn>10) {
				cn = cn % 11;
				s =""+cn;
			}

			NC = P+"-"+s;
			//mu.msgbox(NC);

			if (N.equalsIgnoreCase(NC)) return true; else return false;

		} catch (Exception e) {
			return false;
		}
		*/
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return true;

	}
	
	private void buscaCliente() {
		Cursor DT;

		try{
			String NIT=txtNIT.getText().toString();
			if (mu.emptystr(NIT)) {
				txtNIT.requestFocus();return;
			}

		//	try {
				sql="SELECT Nombre FROM P_CLIENTE WHERE CODIGO='"+NIT+"'";
				DT=Con.OpenDT(sql);
				DT.moveToFirst();

				txtNom.setText(DT.getString(0));
		/*	} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			}*/
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			txtNom.setText("");
		}

	}

	private boolean agregaCliente(String NIT,String Nom,String dir) {

		try {

			ins.init("P_CLIENTE");

			ins.add("CODIGO",NIT);
			ins.add("NOMBRE",Nom);
			ins.add("BLOQUEADO","N");
			ins.add("TIPONEG","");
			ins.add("TIPO","");
			ins.add("SUBTIPO","");
			ins.add("CANAL","");
			ins.add("SUBCANAL","");
			ins.add("NIVELPRECIO",1);

			ins.add("MEDIAPAGO","1");
			ins.add("LIMITECREDITO",0);
			ins.add("DIACREDITO",0);
			ins.add("DESCUENTO","N");
			ins.add("BONIFICACION","N");
			ins.add("ULTVISITA",fecha);

			ins.add("IMPSPEC",0);
			ins.add("INVTIPO","N");
			ins.add("INVEQUIPO","N");
			ins.add("INV1","N");
			ins.add("INV2","N");
			ins.add("INV3","N");

			ins.add("NIT",NIT);
			ins.add("MENSAJE","N");
			ins.add("TELEFONO"," ");
			ins.add("DIRTIPO","N");
			//ins.add("DIRECCION","Ciudad");
			ins.add("DIRECCION",dir);
			ins.add("SUCURSAL","1");

			ins.add("COORX",0);
			ins.add("COORY",0);
			ins.add("FIRMADIG","N");
			ins.add("CODBARRA","");
			ins.add("VALIDACREDITO","N");
			ins.add("PRECIO_ESTRATEGICO","N");
			ins.add("NOMBRE_PROPIETARIO","");
			ins.add("NOMBRE_REPRESENTANTE","");

			ins.add("BODEGA","");
			ins.add("COD_PAIS","");
			ins.add("FACT_VS_FACT","0");
			ins.add("CHEQUEPOST","N");

			ins.add("PERCEPCION",0);
			ins.add("TIPO_CONTRIBUYENTE","");
			ins.add("ID_DESPACHO",0);
			ins.add("ID_FACTURACION",0);
			ins.add("MODIF_PRECIO",0);		

			db.execSQL(ins.sql());

			return true;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);

			//toast(e.getMessage());
			//MU.msgbox("E", e.getMessage());

			try {

				upd.init("P_CLIENTE");
				upd.add("NOMBRE",Nom);
				upd.Where("CODIGO='"+NIT+"'");

				db.execSQL(upd.SQL());

				return true;

			} catch (SQLException e1) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				mu.msgbox(e1.getMessage());return false;
			}

		}

	}

	private void limpiaCampos() {
		try{
			txtNIT.setText("");
			txtNom.setText("");
			txtNIT.requestFocus();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

}
