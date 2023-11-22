package com.dts.roadp;

import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LicRegis extends PBase {

	private TextView lblInfo,lblParam,lblMac,lblDevName;
	private ProgressBar barInfo;
	private EditText txtRuta,txtWS,txtEmp;
	
	private int isbusy,lickey,appid;
	private String ruta,mac,dname;
	
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayList<String> results=new ArrayList<String>();
	
	private ArrayList<clsClasses.clsEnvio> items=new ArrayList<clsClasses.clsEnvio>();
	private ListAdaptEnvio adapter;
	
	private clsDataBuilder dbld;
	
	// Web Service
	
	public AsyncCallSend wsStask;
	
	private static String sstr,fstr,fprog,finf,ferr,fterr,idbg,dbg;
	private int scon,running,pflag,stockflag;
	private String ftext,slsync,senv,gEmpresa,ActRuta;
	private boolean rutapos;
	
	private final String NAMESPACE ="http://tempuri.org/";
	private String METHOD_NAME,URL,URL_Remota;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lic_regis);
				
		super.InitBase();
		addlog("LicRegis",""+du.getActDateTime(),gl.vend);
		
		System.setProperty("line.separator","\r\n");
		
		dbld=new clsDataBuilder(this);
		
		lblInfo= (TextView) findViewById(R.id.lblETipo);
		lblParam= (TextView) findViewById(R.id.lblProd);
		lblMac= (TextView) findViewById(R.id.textView1);
		lblDevName= (TextView) findViewById(R.id.textView5);
		barInfo= (ProgressBar) findViewById(R.id.progressBar2);
		txtRuta= (EditText) findViewById(R.id.txtRuta);
		txtWS= (EditText) findViewById(R.id.txtWS);
		txtEmp= (EditText) findViewById(R.id.txtEmp);
		
		isbusy=0;
		
		lblInfo.setText("");lblParam.setText("");
		barInfo.setVisibility(View.INVISIBLE);
		
		ruta=((appGlobals) vApp).ruta;
		ActRuta=ruta;
	    gEmpresa=((appGlobals) vApp).emp;
	    rutapos=((appGlobals) vApp).rutapos;
		
		if (((appGlobals) vApp).tipo==0) {
			this.setTitle("Comunicación");			
		} else {	
			this.setTitle("Comunicación Local");			
		}
		
		getWSURL();		
		
		appid=103157;
		
		mac=getMac().toUpperCase();
		dname=getDeviceName();
		lickey=getLicKey(mac);//msgbox(""+lickey+"   "+getLKey(lickey));
		
		lblMac.setText(mac);
		lblDevName.setText(dname);
		
	}
	

	// Main
	
	public void runSend() {

		try{
			if (isbusy==1) {return;}

			if (!setComParams()) return;

			isbusy=1;

			barInfo.setVisibility(View.VISIBLE);barInfo.invalidate();
			lblInfo.setText("Conectando ...");

			wsStask = new AsyncCallSend();
			wsStask.execute();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
			
	}
	
	// +++ WEB SERVICE METHODS
	
	public int fillTable(String value,String delcmd) {
		int rc;
		String s,ss;
	
		METHOD_NAME = "getIns";
		sstr="OK";
		
		try {
			
			idbg=idbg+" filltable ";
		
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
	    
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");param.setValue(value);
			
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapObject resSoap =(SoapObject) envelope.getResponse();
	        SoapObject result = (SoapObject) envelope.bodyIn;
	        
	        rc=resSoap.getPropertyCount()-1;
	        idbg=idbg+" rec " +rc +"  ";
	           
	        s="";
	        if (delcmd.equalsIgnoreCase("DELETE FROM P_STOCK")) {
	        	if (rc==1) { 
	        		stockflag=0;return 1;
	        	} else {
	        		stockflag=1;	
	        	}
	        }
	         
	        // if (delcmd.equalsIgnoreCase("DELETE FROM P_COBRO")) {
	        // 	idbg=idbg+" RC ="+rc+"---";
	        //}
	        
	        for (int i = 0; i < rc; i++) {
	        	
	        	String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);
	        	//s=s+str+"\n";
	        	
	        	if (i==0) {
	        		
	        		 idbg=idbg+" ret " +str +"  ";
	        		
	        		if (str.equalsIgnoreCase("#")) {
	        			listItems.add(delcmd);
	        		} else {
	        			idbg=idbg+str;
	        			sstr=str;return 0;
	        		}
	        	} else {
	        		try {
	    			    sql=str;	
	    			    listItems.add(sql);
	    			    sstr=str;
		    		} catch (Exception e) {
		    		   	sstr=e.getMessage();
		    	    }	
	        	}
	        }
	        
	        return 1;
	    } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    	
	    	idbg=idbg+" ERR "+e.getMessage();
	    	return 0;
	    }
	}
	
	public int commitSQL() {
		int rc;
		String s,ss;
	
		METHOD_NAME = "Commit";
		sstr="OK";
		
		if (dbld.size()==0) return 1;
		
		s="";
		for (int i = 0; i < dbld.size(); i++) {
			ss=dbld.items.get(i);
			s=s+ss+"\n";
	    }
		
		fprog="Enviando ...";wsStask.onProgressUpdate();
		
		try {
		
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
	    
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");param.setValue(s);
			
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
	        
	        s = response.toString();
	        
	        sstr = "#";
	        if (s.equalsIgnoreCase("#")) return 1;
	           
	        sstr = s;
	        return 0;
	    } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    	sstr=e.getMessage(); 
	    }

    	return 0;
	}	
	
	public int OpenDTt(String sql) {
		int rc;
	
		METHOD_NAME = "OpenDT";
		
		results.clear();
		
		try {
		
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
	    
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");param.setValue(sql);
			
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapObject resSoap =(SoapObject) envelope.getResponse();
	        SoapObject result = (SoapObject) envelope.bodyIn;
	        
	        rc=resSoap.getPropertyCount()-1;
	        
	        for (int i = 0; i < rc+1; i++)
	        {
	        	String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);
	        	
	        	if (i==0) {
	        		sstr=str;
	        		if (!str.equalsIgnoreCase("#")) {
	        			sstr=str;
	        			return 0;
	        		}
	        	} else {	
	        		results.add(str);
	        	}
	        }
	 
	        return 1;
	    } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    	sstr=e.getMessage(); 
	    }
		
		return 0;
		
	}	

	public int getTest() {
		
		METHOD_NAME = "TestWS";
		
		try {
		
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
	    
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Value");param.setValue("OK");
	    
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
			
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
	        
	        sstr = response.toString()+"..";
	        	        
	        return 1;
	    } catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		    sstr=e.getMessage();          
	    }
		
		return 0;
	}	
		
	
	// +++ WEB SERVICE - ENVIO
	
	private boolean sendData(){

		try{
			senv="Registracion enviada \n \n";

			items.clear();
			envioRegist();

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return true;
	}
	
	public void envioRegist() {
		String ss,tstr;

		fterr="";fprog=" ";wsStask.onProgressUpdate();
		tstr=encodeValue(57,getLicKey(mac));

		try {

			dbld.clear();
			ss="INSERT INTO LIC_CLIENTE VALUES ('"+mac+"','"+tstr+"','','"+dname+"','')";					
			dbld.add(ss);
			fterr=ss;

			if (commitSQL()==1) {
				fstr="Sync OK";
			} else {
				fstr=sstr;fterr+="\n"+sstr;					
			}	
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			fstr=e.getMessage();
			fterr+="\n"+e.getMessage();
		}

	}
		
	public void addItem(String nombre,int env,int pend) {
		clsClasses.clsEnvio item;
		
		try {
			item=clsCls.new clsEnvio();
			
			item.Nombre=nombre;
			item.env=env;
			item.pend=pend;
			
			items.add(item);
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	
	// Web Service handling Methods
	
	public void wsSendExecute(){
					
		running=1;fstr="No connect";scon=0;
					
		try {

			if (getTest() == 1) {
				scon = 1;
			} else {
				URL = URL_Remota;
				if (getTest() == 1) {
					scon = 1;
				}
			}

			if (scon==1) {
				fstr="Sync OK";	
				if (!sendData()) fstr="Envio incompleto : "+sstr;
			} else {	
				fstr="No se puede conectar al web service : "+sstr;
			}



		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			scon=0;
			fstr="No se puede conectar al web service. "+e.getMessage();
		}
	}
			
	public void wsSendFinished(){

		try{
			barInfo.setVisibility(View.INVISIBLE);
			lblParam.setVisibility(View.INVISIBLE);
			running=0;

			if (fstr.equalsIgnoreCase("Sync OK")) {
				lblInfo.setText(" ");
			} else {
				lblInfo.setText(fstr);
				mu.msgbox(fstr+"\n"+fterr);
			}

			mu.msgbox(senv);

			isbusy=0;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
			
	private class AsyncCallSend extends AsyncTask<String, Void, Void> {

		@Override
	    protected Void doInBackground(String... params) {
				
			try {
				wsSendExecute();
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}
	            
	        return null;
	    }
	 
	    @Override
	    protected void onPostExecute(Void result) {
			try{
				wsSendFinished();
			}catch (Exception e){
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}

	    }
	 
        @Override
        protected void onPreExecute() {
    		try {
    		} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");}
        }
	 
        @Override
        protected void onProgressUpdate(Void... values) {
    		try {
    			lblInfo.setText(fprog);
    		} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
    		}
        }
	 
    }	
		
	
	// Aux
		
	public void askSend(View view) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Registro");
			dialog.setMessage("Enviar registro ?");

			dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					runSend();
				}
			});

			dialog.setNegativeButton("Cancelar", null);

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}	

	public void getWSURL() {
		Cursor DT;
		String wsurl;

		if (!gl.debug) {
			txtRuta.setText(ruta);
			txtEmp.setText(gEmpresa);
		}

		try {

			sql = "SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='" + ruta + "'";
			DT = Con.OpenDT(sql);

			if (DT.getCount() > 0) {
				DT.moveToFirst();

				URL = DT.getString(0);
				URL_Remota = DT.getString(1);

				gl.URLtemp = URL;

				if (!URL.isEmpty()) {
					txtWS.setText(URL);
				} else if (!URL_Remota.isEmpty()) {
					txtWS.setText(URL);
				} else {
					toast("No hay configurada URL para transferencia de datos");
				}

			}

			if (DT != null) DT.close();

		} catch (Exception e) {
			addlog(new Object() {
			}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
			URL = "*";
			txtWS.setText("http://192.168.1.142/wsAndr/wsandr.asmx");

		}

	}

	private boolean setComParams() {
		String ss;

		try{
			ss=txtRuta.getText().toString().trim();
			if (mu.emptystr(ss)) {
				mu.msgbox("La ruta no esta definida.");return false;
			}
			ActRuta=ss;

			ss=txtEmp.getText().toString().trim();
			if (mu.emptystr(ss)) {
				mu.msgbox("La empresa no esta definida.");return false;
			}
			gEmpresa=ss;

			ss=txtWS.getText().toString().trim();
			if (mu.emptystr(ss) || ss.equalsIgnoreCase("*")) {
				mu.msgbox("La direccion de Web service no esta definida.");return false;
			}
			URL=ss;
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		
		return true;
	}
	
	private String getMac() {
		WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		return info.getMacAddress();
	}
	
	private String getDeviceName() {		
		return android.os.Build.MODEL;
	}
	
	private int getLicKey(String mac) {
		String s1,s2,s3;
		String[] sp;
		int val,v1,v2,v3;

		try {
			sp=mac.split(":");

			s1=sp[0];v1=Integer.parseInt(s1,16);
			s2=sp[4];v2=Integer.parseInt(s2,16);
			s3=sp[5];v3=Integer.parseInt(s3,16);		
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return 0;
		}

		val=v3*65536+256*v2+v1;

		return val;
	}
	
	private String getLKey(int lickey) {
		int val, mval;
		
		try {
			mval = lickey % 957;
			val=lickey+appid+mval;
			return Integer.toHexString(val).toUpperCase();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return "*";
		}
	}
	
	private String encodeValue(int val) {
		int mval;
		
		try {
			mval = lickey % 957;
			val=lickey+appid+mval;
			return Integer.toHexString(val).toUpperCase();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return "*";
		}
	}
	
	private String encodeValue(int lickey,int val) {
		int maskid=491387;
		
		try {
			val = val+lickey + maskid;
			return Integer.toHexString(val).toUpperCase();
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return "*";
		}  
	}

	
	// Activity Events
	
	@Override
	public void onBackPressed() {
		try{
			if (isbusy==0) {
				super.onBackPressed();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}		
	
	
}
