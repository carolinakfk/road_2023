package com.dts.roadp;

import android.os.AsyncTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;

public class PWebService {

	public String URL,status,statusex;
	public int running;
	public boolean complete;
	
	public ArrayList<String> items=new ArrayList<String>();
	public ArrayList<String> results=new ArrayList<String>();
	
	public String sql,istr,sstr,fstr,rcstr;
	private int scon;
	private final String NAMESPACE ="http://tempuri.org/";
	private String METHOD_NAME;
	
	private PBase parent; 
	
	public PWebService(PBase ParentActivity) {
		parent=ParentActivity;
	}

	public void execute(){
		AsyncCallWS wstask = new AsyncCallWS();
		wstask.execute();
	}
	
	public boolean getData(){
		
		items.clear();
		results.clear();
		
		istr="";
		
		return true;
		
	}
	
	
	// Web Service handling Methods
	
	public void wsExecute(){
		
		running=1;fstr="No connect";scon=0;complete=false;
		
		try {
				
			if (getTest()==1) {scon=1;}
			
			if (scon==1) {
				fstr="Sync OK";
				if (getData()) {
					complete=true;
				} else {	
					fstr="Ocurrio error : "+istr;
					return;
				}
			} else {	
				fstr="No se puede conectar al web service : \n"+URL+"\n"+sstr;
			}
			
		} catch (Exception e) {
			scon=0;
			fstr="No se puede conectar al web service : \n"+URL+"\n"+e.getMessage();
		}
	}
	
	public void wsFinished(){
		running=0;
		
		if (fstr.equalsIgnoreCase("Sync OK")) {
			complete=true;
			status="";statusex=istr;
		} else {	
			complete=false;
			status=fstr;
			statusex=istr;
		}

		try {
			parent.wsCallBack(false," ");
		} catch (Exception e) {}

	}
	
	// Async Call Class
	
	private class AsyncCallWS extends AsyncTask<String, Void, Void> {

		@Override
        protected Void doInBackground(String... params) {
			
			try {
				wsExecute();
			} catch (Exception e) {
			}
            
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
        	wsFinished();
        }
 
        @Override
        protected void onPreExecute() {
    		try {
    		} catch (Exception e) {
    		}
        }
 
        @Override
        protected void onProgressUpdate(Void... values) {
    		try {
    		} catch (Exception e) {
    		}
        }
 
    }			

	// Web service Methods

	public int fillTable(String value,String delcmd) {
		int rc;
		String s,ss;
	
		METHOD_NAME = "getIns";
		sstr="OK";
		
		try {
		
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
	           
	        s="";
	        
	        for (int i = 0; i < rc; i++)
	        {
	        	String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);
	        	//s=s+str+"\n";
	        	
	        	if (i==0) {
	        		if (str.equalsIgnoreCase("#")) {
	        			items.add(delcmd);
	        		} else {
	        			sstr=str;return 0;
	        		}
	        	} else {
	        		try {
	    			    sql=str;	
	    			    items.add(sql);
	    			    sstr=str;
		    		} catch (Exception e) {
		    		   	sstr=e.getMessage();
		    	    }	
	        	}
	        }
	        
	        return 1;
	    } catch (Exception e) {
	    	return 0;
	    }
	}	

	public int OpenDT(String sql) {
		int rc;
	
		METHOD_NAME = "GetDT";
		
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
	        rcstr=""+rc;
	        
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
	        
	        sstr = response.toString();
	        	        
	        return 1;
	    } catch (Exception e) {
		    sstr=e.getMessage();          
	    }
		
		return 0;
	}	
	
}
	
