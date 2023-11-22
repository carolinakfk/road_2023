package com.dts.roadp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.app.Application;


public class MessageBox extends Activity {

	private TextView lblMsg;
	
	private Application vApp;
	
	private String s;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		lblMsg= (TextView) findViewById(R.id.lblETipo);
				
		vApp=this.getApplication();
		s=((appGlobals) vApp).gstr;

		lblMsg.setText(s);
	}


}
