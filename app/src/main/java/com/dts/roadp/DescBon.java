package com.dts.roadp;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

public class DescBon extends PBase {

	private TextView lblDesc;
	private EditText txtDesc;
	
	private String prodid;
	private double cant,desc;
	private int prommodo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_desc_bon);
		
		super.InitBase();
		addlog("DescBon",""+du.getActDateTime(),gl.vend);
		
		lblDesc= (TextView) findViewById(R.id.lblFecha);
		txtDesc = (EditText) findViewById(R.id.txtDesc);
		
		prodid=((appGlobals) vApp).promprod;
		cant=((appGlobals) vApp).promcant;
		desc=((appGlobals) vApp).promdesc;
		prommodo=((appGlobals) vApp).prommodo;
		
		((appGlobals) vApp).promapl=false;
		
		setHandlers();
		
		showProm();
	}
	
	private void showProm(){
		String ss;

		try{
			if (prommodo==0) ss=" %"; else ss=" (monto) ";

			lblDesc.setText("Máximo : "+mu.frmdecno(desc)+ss);
			txtDesc.setText(mu.frmdecno(desc));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void setHandlers(){

		try{
			txtDesc.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						aplProm(v);
						return true;
					}
					return false;
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}
	
	public void aplProm(View view){
		String svd;
		double vd;

		try{
			try {
				svd=txtDesc.getText().toString();
				svd=svd.replace(",",".");
				vd=Double.parseDouble(svd);

				if (vd<0) throw new Exception() ;
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
				mu.msgbox("Valor de descuento incorrecto");return;
			}

			if (vd>desc) {
				mu.msgbox("Valor de descuento es mayor que máximo");return;
			}

			desc=vd;
			((appGlobals) vApp).promprod=prodid;
			if (prommodo==0) {
				((appGlobals) vApp).promdesc=desc;
				((appGlobals) vApp).prommdesc=0;
			} else {
				((appGlobals) vApp).promdesc=0;
				((appGlobals) vApp).prommdesc=desc;
			}
			((appGlobals) vApp).promapl=true;

			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}
	
	
	// Activity Events

	@Override
	public void onBackPressed() {
		try{

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		//super.onBackPressed();
	}	

}
