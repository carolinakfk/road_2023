package com.dts.roadp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.epson.eposdevice.Device;
import com.epson.eposdevice.printer.Printer;

import datamaxoneil.connection.Connection_Bluetooth;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class printEpson extends printBase{
	
	private Context mContext;
	private Device mDevice = null;
	private Printer mPrinter = null;
	private final int mRequestCode = 1001;

	
	public printEpson(Context context,String printerMAC,Device device,Printer printer) {
		super(context,printerMAC);
		
		mContext=context;
		mDevice = device;
		mPrinter = printer;
	}
	
	
	// Main
	
	public void printask(Runnable callBackHook)
	{

		hasCallback=true;
		callback=callBackHook;
		
		fname="print.txt";errmsg="";
		msgAskPrint();
	}
	
	public void printask()
	{
		hasCallback=false;
		
		fname="print.txt";errmsg="";
		msgAskPrint();				
	}
	
	public boolean print()
	{
		hasCallback=false;
		
		fname="print.txt";errmsg="";
		try {
			if (loadFile())	doStartPrint();else return false;
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			
		
		return true;
	}
	
	public void printask(String fileName)
	{
		hasCallback=false;
		
		fname=fileName;	errmsg="";
		msgAskPrint();				
	}
		
	public boolean print(String fileName)
	{
		hasCallback=false;
		
		fname=fileName;errmsg="";
		
		try {
			if (loadFile())	doStartPrint();else return false;
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			
		
		return true;
	}
	

	// Private
	
	private boolean loadFile() 	{

		File ffile;
		BufferedReader dfile;
		String ss;
		
		try {
			
			File file1 = new File(Environment.getExternalStorageDirectory(), "/"+fname);
			ffile = new File(file1.getPath());
					
			FileInputStream fIn = new FileInputStream(ffile);
			dfile = new BufferedReader(new InputStreamReader(fIn));
			
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());
			return false;
		}			
		
		try {
			
			mPrinter.clearCommandBuffer();
			
			while ((ss = dfile.readLine()) != null) {
				mPrinter.addText(ss); mPrinter.addFeed();
			}
			
			mPrinter.addFeedLine(1);
			mPrinter.addFeedLine(1);
			mPrinter.addCut(Printer.CUT_FEED);
			
			dfile.close();	
				       
			mPrinter.sendData();
	
			return true;

		} catch (Exception e) {
			try {
				dfile.close();
			} catch (Exception e1) {}	
			
			showmsg("Error: " + e.getMessage());
			
			return false;
		}			
	}
	
	private void doStartPrint()
	{
		showmsg("Imprimiendo ...");
		AsyncPrintCall wsRtask = new AsyncPrintCall();
		wsRtask.execute();
	}
	
	private class AsyncPrintCall extends AsyncTask<String, Void, Void>
	{

		@Override
	    protected Void doInBackground(String... params) {
			try {
				processPrint();
			} catch (Exception e) {}
	            
	        return null;
	    }
	 
	    @Override
	    protected void onPostExecute(Void result)
		{
	    	try {
	    		doCallBack();
			} catch (Exception e) {}
	    }
	 
        @Override
        protected void onPreExecute() {}
	 
        @Override
        protected void onProgressUpdate(Void... values) {}
	 
    }	
	
	private void doCallBack()
	{
		//showmsg("Impresión completa.");
		
		if (!hasCallback) return;
		
		final Handler cbhandler = new Handler();
		cbhandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				callback.run();
			}
		}, 500);
			
	}
	
	public void processPrint()
	{
		
		try {

			prconn = null;
			
			Looper.prepare();
			prconn = Connection_Bluetooth.createClient(printerAddress);
		
			if(!prconn.getIsOpen()) prconn.open();
				
			prconn.write(printData);
			prthread.sleep(1500);
			
			prconn.close();

		} catch (Exception e) {
			if (prconn != null) prconn.close();
			//showmsg("Error : " + e.getMessage());
		}				
	}
	
	public void showmsg(String MsgStr) {
		errmsg=MsgStr;
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(cont, errmsg, Toast.LENGTH_SHORT).show();
			}
		});
			
	}	
	
	
	// Aux
	
	private void msgAskPrint() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage("Impresora está lista ?");

		dialog.setCancelable(false);
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	try {
					if (loadFile())	doStartPrint();
				} catch (Exception e) {
					showmsg("Error: " + e.getMessage());
				}
		    }
		    
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	final Handler cbhandler = new Handler();
				cbhandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						printclose.run();
					}
				}, 200);		    	
		    }
		});
		
		dialog.show();
			
	}		
	

}
