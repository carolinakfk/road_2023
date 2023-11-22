package com.dts.roadp;

import java.text.DecimalFormat;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RecargCant extends PBase {

	private EditText txtCant;
	private RelativeLayout rlCant;
	private TextView lblDesc,lblPrec,lblBU;
		
	private String prodid,estado,raz;
	private double cant,icant;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recarg_cant);
		
		super.InitBase();
		addlog("RecargCant",""+du.getActDateTime(),gl.vend);
		
		setControls();
		
		prodid=((appGlobals) vApp).prod;
		estado=((appGlobals) vApp).devtipo;
		raz=((appGlobals) vApp).gstr;
	
		setHandlers();
		
		((appGlobals) vApp).dval=-1;
		
		showkeyb();
	
		showData();		
		
		//txtCant.setText(((appGlobals) vApp).gstr2);
	}
	

	// Events
	
	public void sendCant(View view) {

		try{
			setCant();

			if (cant<0){
				mu.msgbox("Cantidad incorrecta");return;
			}

			((appGlobals) vApp).dval=cant;
			((appGlobals) vApp).devrazon="0";

			hidekeyb();
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}
	
	// Main
	
	private void setHandlers(){

		try{
			if (gl.peDecCant==0) {
				txtCant.setInputType(InputType.TYPE_CLASS_NUMBER );
			} else {
				txtCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
	}
	
	private void showData() {
		Cursor DT;
		String ubas;
		double costo;
		
		try {
			sql="SELECT UNIDBAS,UNIDMED,UNIMEDFACT,UNIGRA,UNIGRAFACT,DESCCORTA,IMAGEN,DESCLARGA,COSTO "+
				 "FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
							  
			ubas=DT.getString(0);
			
			lblBU.setText(ubas);((appGlobals) vApp).ubas=ubas;
			lblDesc.setText(DT.getString(7));
			costo=DT.getDouble(8);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   mu.msgbox("1-"+ e.getMessage());costo=0;
	    }	
		
		lblPrec.setText(mu.frmdec(costo));gl.costo=costo;
		
		try {
			sql="SELECT CANT FROM T_CxCD WHERE CODIGO='"+prodid+"' AND CODDEV='"+raz+"'";
           	DT=Con.OpenDT(sql);
           	
       		DT.moveToFirst();
  			icant=DT.getDouble(0);
  				
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			icant=0;
	    }	

		if (icant>0) parseCant(icant);
		
	}
	
	private void setCant(){
		try {
			cant=Double.parseDouble(txtCant.getText().toString());			
			cant=mu.round(cant,gl.peDecCant);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			cant=-1; 
		}
	}
	
	private void parseCant(double c) {
		try{
			DecimalFormat frmdec = new DecimalFormat("#.####");
			double ub;

			ub=c;
			if (ub>0) txtCant.setText(frmdec.format(ub));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	
	// Aux

	private void setControls() {
		try{
			txtCant= (EditText) findViewById(R.id.txtMonto);
			rlCant= (RelativeLayout) findViewById(R.id.rlCant);
			lblDesc=(TextView) findViewById(R.id.lblFecha);
			lblPrec=(TextView) findViewById(R.id.lblPNum);
			lblBU=(TextView) findViewById(R.id.lblBU);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
}
