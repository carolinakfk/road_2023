package com.dts.roadp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DemoDataDlg extends PBase {

	private EditText txtNom,txtPrec;
	private RelativeLayout rlPrec;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_data_dlg);
		
		super.InitBase();
		addlog("DemoDataDlg",""+du.getActDateTime(),gl.vend);
		
		txtNom = (EditText) findViewById(R.id.txtBoleta);
		txtPrec = (EditText) findViewById(R.id.editText2);
		rlPrec = (RelativeLayout) findViewById(R.id.relativeLayout1);
		
		txtNom.setText(gl.clsDemo.Desc);
		if (gl.clsDemo.tipo>0) {
			rlPrec.setVisibility(View.VISIBLE); 
			txtPrec.setText(""+gl.clsDemo.val);
		} else {
			rlPrec.setVisibility(View.GONE);
		}
		
		txtNom.requestFocus();
	}
	
	public void applyInput(View view) {
		double val;
		
		s=txtNom.getText().toString();

		try{
			if (mu.emptystr(s)) {
				toasttop("Descripción incorrecta.");return;
			}


			if (gl.clsDemo.tipo>0) {
				try {
					s=txtPrec.getText().toString();
					val=Double.parseDouble(s);
					if (val<=0.1) throw new Exception();

					gl.clsDemo.val=val;
				} catch (Exception e) {
					toasttop("Precio incorrecto.");return;
				}

			}

			gl.clsDemo.Desc=txtNom.getText().toString();
			gl.clsDemo.flag=1;

			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	
	private void toasttop(String msg) {

		try{
			if (mu.emptystr(msg)) return;

			Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP , 0, 0);
			toast.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	

	
	
}
