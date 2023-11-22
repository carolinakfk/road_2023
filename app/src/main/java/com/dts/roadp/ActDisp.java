package com.dts.roadp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

public class ActDisp extends PBase {

	private ProgressBar pBar;
	
	private clsWebService ws;
	
	private String URL,prodid;
	private boolean isRunning=true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_act_disp);
		
		super.InitBase();
		addlog("ActDisp",""+du.getActDateTime(),gl.vend);
		
		pBar = (ProgressBar) findViewById(R.id.pBar);
		
		URL = ((appGlobals) vApp).wsURL;
		prodid=((appGlobals) vApp).gstr;

		ws=new clsWebService(this);
		ws.URL=URL;
		ws.execute();
		
	}

	private class clsWebService extends PWebService {
		
		public clsWebService(PBase ParentActivity) {
			super(ParentActivity);
		}

		@Override
		public boolean getData(){
				
			super.getData();
				
			complete=false;
				
			try {
				
				if (prodid.equalsIgnoreCase("*")) {
					if (fillTable("SELECT * FROM P_STOCKINV","DELETE FROM P_STOCKINV")==0) return false;
				} else {	
					if (fillTable("SELECT * FROM P_STOCKINV WHERE Codigo='"+prodid+"'","DELETE FROM P_STOCKINV WHERE Codigo='"+prodid+"'")==0) return false;
				}
						
				complete=true;
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				istr=e.getMessage();
			}
			
			return true;
		}
	}

	@Override
	public void wsCallBack(Boolean throwing,String errmsg){
		try{
			if (ws.complete) {
				processData();
			} else {
				mu.msgbox("No se puede conectar al servidor.");
			}

			pBar.setVisibility(View.INVISIBLE);
			isRunning=false;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	} 	

	private void processData(){
		int rc;
		
		try {
			rc=ws.items.size();
			if (rc==0) {return;}
			
			for (int i = 0; i < rc; i++)
		    {
		       sql=ws.items.get(i);
		       //MU.msgbox(sql);
		       db.execSQL(sql); 
		    }
			
			((appGlobals) vApp).gstr="&";
			
			super.finish();
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}
		
		pBar.setVisibility(View.INVISIBLE);
		
	}

	// Activity Events
	
	@Override
	public void onBackPressed() {
		try{

			if (!isRunning) super.onBackPressed();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
}


//el dia que teco temio.

//no temais. per favore.
