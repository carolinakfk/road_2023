package com.dts.roadp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MercLista extends PBase {

	private TextView btnProp,btnComp,btnEnc,btnAct;
	
	private String cliid;
	private boolean mprop,mcomp,menc,mact;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_merc_lista);
		
		super.InitBase();
		addlog("MercLista",""+du.getActDateTime(),gl.vend);
		
		cliid=((appGlobals) vApp).cliente;
		
		btnProp= (TextView) findViewById(R.id.btnPropio);
		btnComp= (TextView) findViewById(R.id.btnComp);
		btnEnc= (TextView) findViewById(R.id.btnEnc);
		btnAct= (TextView) findViewById(R.id.btnActivos);
		
		setMerc();
	}
	

	// Events
	
	public void mercProp(View view){
		try{
			Intent intent = new Intent(this,MercProp.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void mercComp(View view){
		try{
			Intent intent = new Intent(this,MercComp.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	public void mercEnc(View view){
		try{
			Intent intent = new Intent(this,MercPreg.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	public void mercAct(View view){
		try{
			Intent intent = new Intent(this,MercAct.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	// Main
	
	private void setMerc(){
		Cursor DT;
		String s;
		int vis=0;
		
		try {
			sql="SELECT INV1,INV2,INV3,INVEQUIPO FROM P_CLIENTE WHERE CODIGO='"+cliid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
				
			s=DT.getString(1);
			mprop=s.equalsIgnoreCase("S");if (mprop) vis+=1;
			
			s=DT.getString(2);
			mcomp=s.equalsIgnoreCase("S");if (mcomp) vis+=1;
			
			s=DT.getString(0);
			menc=s.equalsIgnoreCase("S");if (menc) vis+=1;
			
			s=DT.getString(3);
			mact=s.equalsIgnoreCase("S");if (mact) vis+=1;
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   	super.finish();
		   	return;
	    }	

	    if (mprop) btnProp.setVisibility(View.VISIBLE); else btnProp.setVisibility(View.GONE);
	    if (mcomp) btnComp.setVisibility(View.VISIBLE); else btnComp.setVisibility(View.GONE);
	    if (menc)  btnEnc.setVisibility(View.VISIBLE);  else btnEnc.setVisibility(View.GONE);
	    if (mact)  btnAct.setVisibility(View.VISIBLE);  else btnAct.setVisibility(View.GONE);
	
	    if (vis==0) super.finish();
	    
	}


}
